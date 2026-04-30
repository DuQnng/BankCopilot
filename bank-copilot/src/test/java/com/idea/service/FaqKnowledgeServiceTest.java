package com.idea.service;

import com.idea.entity.FaqKnowledge;
import com.idea.entity.FaqQueryLog;
import com.idea.mapper.FaqKnowledgeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqKnowledgeServiceTest {

    @Mock
    private FaqKnowledgeMapper faqKnowledgeMapper;

    @InjectMocks
    private FaqKnowledgeService faqKnowledgeService;

    @Test
    void searchShouldHitStandardAnswerForCardLossQuestion() {
        when(faqKnowledgeMapper.selectEnabled()).thenReturn(List.of(
                faq(6L, "银行卡业务", "银行卡丢失后如何处理？", "卡丢了怎么挂失；银行卡掉了怎么办", "银行卡；丢失；挂失", 120),
                faq(11L, "转账汇款", "转账多久到账？", "跨行转账要多久", "转账；到账；跨行", 100)
        ));

        FaqKnowledgeService.FaqSearchResult result = faqKnowledgeService.search("卡丢了怎么挂失");

        assertEquals("hit", result.getResultType());
        assertNotNull(result.getBest());
        assertEquals(6L, result.getBest().getFaq().getId());
        assertTrue(result.getBest().getScore() >= 0.74);
    }

    @Test
    void searchShouldRecommendWhenQueryIsRelatedButNotCertain() {
        when(faqKnowledgeMapper.selectEnabled()).thenReturn(List.of(
                faq(11L, "转账汇款", "转账多久到账？", "跨行转账要多久", "转账；到账；跨行", 120)
        ));

        FaqKnowledgeService.FaqSearchResult result = faqKnowledgeService.search("转账有问题");

        assertEquals("recommend", result.getResultType());
        assertEquals(1, result.getRecommendations().size());
        assertEquals(11L, result.getRecommendations().get(0).getFaq().getId());
    }

    @Test
    void searchShouldReturnNoAnswerForUnrelatedQuestion() {
        when(faqKnowledgeMapper.selectEnabled()).thenReturn(List.of(
                faq(6L, "银行卡业务", "银行卡丢失后如何处理？", "卡丢了怎么挂失", "银行卡；丢失；挂失", 120)
        ));

        FaqKnowledgeService.FaqSearchResult result = faqKnowledgeService.search("今天西安天气怎么样");

        assertEquals("no_answer", result.getResultType());
    }

    @Test
    void logQueryShouldPersistResultTypeAndScore() {
        FaqKnowledge faq = faq(6L, "银行卡业务", "银行卡丢失后如何处理？", "卡丢了怎么挂失", "银行卡；丢失；挂失", 120);
        FaqKnowledgeService.ScoredFaq scoredFaq = new FaqKnowledgeService.ScoredFaq(faq, 0.91);
        FaqKnowledgeService.FaqSearchResult result = FaqKnowledgeService.FaqSearchResult.hit(scoredFaq, List.of(scoredFaq));

        faqKnowledgeService.logQuery(1L, "卡丢了怎么挂失", result);

        ArgumentCaptor<FaqQueryLog> captor = ArgumentCaptor.forClass(FaqQueryLog.class);
        verify(faqKnowledgeMapper).insertQueryLog(captor.capture());
        FaqQueryLog log = captor.getValue();
        assertEquals(1L, log.getUserId());
        assertEquals(6L, log.getMatchedFaqId());
        assertEquals("hit", log.getResultType());
        assertEquals("91.00", log.getMatchScore().toPlainString());
    }

    private FaqKnowledge faq(Long id, String category, String question, String aliases, String keywords, int priority) {
        FaqKnowledge faq = new FaqKnowledge();
        faq.setId(id);
        faq.setCategory(category);
        faq.setStandardQuestion(question);
        faq.setAliases(aliases);
        faq.setKeywords(keywords);
        faq.setAnswer("测试答案");
        faq.setPriority(priority);
        faq.setStatus(1);
        return faq;
    }
}
