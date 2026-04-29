package com.idea.mapper;

import com.idea.entity.FaqKnowledge;
import com.idea.entity.FaqQueryLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FaqKnowledgeMapper {

    @Select("""
            select
                id,
                category,
                standard_question,
                aliases,
                keywords,
                answer,
                status,
                priority,
                source,
                create_time,
                update_time
            from faq_knowledge
            where status = 1
            order by priority desc, update_time desc, id asc
            """)
    List<FaqKnowledge> selectEnabled();

    @Insert("""
            insert into faq_query_log(
                user_id,
                user_query,
                matched_faq_id,
                match_score,
                result_type,
                user_feedback,
                create_time
            ) values (
                #{userId},
                #{userQuery},
                #{matchedFaqId},
                #{matchScore},
                #{resultType},
                #{userFeedback},
                now()
            )
            """)
    int insertQueryLog(FaqQueryLog log);
}
