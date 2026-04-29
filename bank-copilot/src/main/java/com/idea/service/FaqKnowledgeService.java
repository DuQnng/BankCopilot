package com.idea.service;

import com.idea.entity.FaqKnowledge;
import com.idea.entity.FaqQueryLog;
import com.idea.mapper.FaqKnowledgeMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FaqKnowledgeService {

    private static final double HIT_THRESHOLD = 0.74;
    private static final double RECOMMEND_THRESHOLD = 0.45;

    private final FaqKnowledgeMapper faqKnowledgeMapper;

    public FaqSearchResult search(String userQuery) {
        String normalizedQuery = normalize(userQuery);
        if (normalizedQuery.isBlank()) {
            return FaqSearchResult.noAnswer(false);
        }

        List<FaqKnowledge> faqList;
        try {
            faqList = faqKnowledgeMapper.selectEnabled();
        } catch (Exception e) {
            log.warn("faq knowledge table unavailable, fallback to assistant default, query={}", userQuery, e);
            return FaqSearchResult.noAnswer(true);
        }

        List<ScoredFaq> scoredList = faqList.stream()
                .map(faq -> new ScoredFaq(faq, score(normalizedQuery, faq)))
                .filter(item -> item.getScore() > 0)
                .sorted(Comparator
                        .comparingDouble(ScoredFaq::getScore).reversed()
                        .thenComparing(item -> item.getFaq().getPriority() == null ? 0 : item.getFaq().getPriority(), Comparator.reverseOrder())
                        .thenComparing(item -> item.getFaq().getId() == null ? Long.MAX_VALUE : item.getFaq().getId()))
                .toList();

        if (scoredList.isEmpty()) {
            return FaqSearchResult.noAnswer(false);
        }

        ScoredFaq best = scoredList.get(0);
        if (best.getScore() >= HIT_THRESHOLD) {
            return FaqSearchResult.hit(best, takeTop(scoredList, 3));
        }

        List<ScoredFaq> recommendations = scoredList.stream()
                .filter(item -> item.getScore() >= RECOMMEND_THRESHOLD)
                .limit(3)
                .toList();
        if (!recommendations.isEmpty()) {
            return FaqSearchResult.recommend(best, recommendations);
        }

        return FaqSearchResult.noAnswer(false);
    }

    public void logQuery(Long userId, String userQuery, FaqSearchResult result) {
        try {
            FaqQueryLog log = new FaqQueryLog();
            log.setUserId(userId);
            log.setUserQuery(userQuery);
            log.setResultType(result.getResultType());
            if (result.getBest() != null) {
                log.setMatchedFaqId(result.getBest().getFaq().getId());
                log.setMatchScore(toScorePercent(result.getBest().getScore()));
            }
            faqKnowledgeMapper.insertQueryLog(log);
        } catch (Exception e) {
            log.warn("faq query log insert failed, query={}", userQuery, e);
        }
    }

    private List<ScoredFaq> takeTop(List<ScoredFaq> scoredList, int size) {
        return scoredList.stream().limit(size).toList();
    }

    private BigDecimal toScorePercent(double score) {
        return BigDecimal.valueOf(score * 100).setScale(2, RoundingMode.HALF_UP);
    }

    private double score(String normalizedQuery, FaqKnowledge faq) {
        double textScore = 0;
        for (String candidate : buildCandidateTexts(faq)) {
            String normalizedCandidate = normalize(candidate);
            if (normalizedCandidate.isBlank()) {
                continue;
            }
            textScore = Math.max(textScore, similarity(normalizedQuery, normalizedCandidate));
        }

        double keywordScore = keywordScore(normalizedQuery, faq.getKeywords());
        double domainHintScore = domainHintScore(normalizedQuery, faq);
        double baseScore = Math.min(1.0, textScore * 0.82 + keywordScore * 0.18);
        return Math.max(baseScore, domainHintScore);
    }

    private List<String> buildCandidateTexts(FaqKnowledge faq) {
        List<String> result = new ArrayList<>();
        result.add(faq.getStandardQuestion());
        result.addAll(splitTerms(faq.getAliases()));
        return result;
    }

    private double keywordScore(String normalizedQuery, String keywords) {
        List<String> terms = splitTerms(keywords);
        if (terms.isEmpty()) {
            return 0;
        }
        int matched = 0;
        for (String term : terms) {
            String normalizedTerm = normalize(term);
            if (!normalizedTerm.isBlank() && normalizedQuery.contains(normalizedTerm)) {
                matched++;
            }
        }
        return Math.min(1.0, matched / (double) Math.min(3, terms.size()));
    }

    private double domainHintScore(String normalizedQuery, FaqKnowledge faq) {
        String category = normalize(faq.getCategory());
        if (category.contains("银行卡") && (normalizedQuery.contains("银行卡") || normalizedQuery.contains("卡"))) {
            return 0.46;
        }
        if (category.contains("密码") && normalizedQuery.contains("密码")) {
            return 0.46;
        }
        if (category.contains("转账") && normalizedQuery.contains("转账")) {
            return 0.46;
        }
        if (category.contains("贷款") && normalizedQuery.contains("贷款")) {
            return 0.46;
        }
        if (category.contains("网银") && (normalizedQuery.contains("手机银行") || normalizedQuery.contains("网银"))) {
            return 0.46;
        }
        if (category.contains("收费") && (normalizedQuery.contains("收费") || normalizedQuery.contains("扣费") || normalizedQuery.contains("限额"))) {
            return 0.46;
        }
        if (category.contains("人工") && (normalizedQuery.contains("人工") || normalizedQuery.contains("客服") || normalizedQuery.contains("网点"))) {
            return 0.46;
        }
        if (category.contains("账户") && (normalizedQuery.contains("账户") || normalizedQuery.contains("开户"))) {
            return 0.46;
        }
        return 0;
    }

    private List<String> splitTerms(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String[] pieces = text.split("[;；,，、\\n\\r]+");
        List<String> result = new ArrayList<>();
        for (String piece : pieces) {
            String cleaned = piece.trim();
            if (!cleaned.isBlank()) {
                result.add(cleaned);
            }
        }
        return result;
    }

    private double similarity(String query, String candidate) {
        if (query.equals(candidate)) {
            return 1.0;
        }
        if (candidate.contains(query) || query.contains(candidate)) {
            double shorter = Math.min(query.length(), candidate.length());
            double longer = Math.max(query.length(), candidate.length());
            return Math.max(0.86, shorter / longer);
        }
        Set<String> queryGrams = grams(query);
        Set<String> candidateGrams = grams(candidate);
        if (queryGrams.isEmpty() || candidateGrams.isEmpty()) {
            return 0;
        }
        Set<String> intersection = new HashSet<>(queryGrams);
        intersection.retainAll(candidateGrams);
        return (2.0 * intersection.size()) / (queryGrams.size() + candidateGrams.size());
    }

    private Set<String> grams(String text) {
        Set<String> grams = new LinkedHashSet<>();
        if (text.length() <= 1) {
            grams.add(text);
            return grams;
        }
        for (int i = 0; i < text.length() - 1; i++) {
            grams.add(text.substring(i, i + 2));
        }
        return grams;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase()
                .replaceAll("[\\s\\p{Punct}，。！？；：、（）【】《》“”‘’￥]+", "")
                .trim();
    }

    @Getter
    public static class FaqSearchResult {
        private final String resultType;
        private final ScoredFaq best;
        private final List<ScoredFaq> recommendations;
        private final boolean knowledgeUnavailable;

        private FaqSearchResult(String resultType, ScoredFaq best, List<ScoredFaq> recommendations, boolean knowledgeUnavailable) {
            this.resultType = resultType;
            this.best = best;
            this.recommendations = recommendations;
            this.knowledgeUnavailable = knowledgeUnavailable;
        }

        public static FaqSearchResult hit(ScoredFaq best, List<ScoredFaq> recommendations) {
            return new FaqSearchResult("hit", best, recommendations, false);
        }

        public static FaqSearchResult recommend(ScoredFaq best, List<ScoredFaq> recommendations) {
            return new FaqSearchResult("recommend", best, recommendations, false);
        }

        public static FaqSearchResult noAnswer(boolean knowledgeUnavailable) {
            return new FaqSearchResult("no_answer", null, List.of(), knowledgeUnavailable);
        }

        public boolean isHit() {
            return "hit".equals(resultType);
        }

        public boolean isRecommend() {
            return "recommend".equals(resultType);
        }
    }

    @Getter
    public static class ScoredFaq {
        private final FaqKnowledge faq;
        private final double score;

        public ScoredFaq(FaqKnowledge faq, double score) {
            this.faq = faq;
            this.score = score;
        }
    }
}
