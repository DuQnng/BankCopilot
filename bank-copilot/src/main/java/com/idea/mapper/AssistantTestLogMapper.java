package com.idea.mapper;

import com.idea.entity.AssistantTestLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssistantTestLogMapper {

    @Insert("""
            insert into assistant_test_log(
                user_id,
                source,
                scenario,
                request_text,
                intent,
                trace_tool,
                trace_status,
                result_code,
                biz_code,
                success,
                duration_ms,
                response_chars,
                error_message,
                test_session_id,
                participant_code,
                participant_group,
                task_code,
                task_name,
                completed,
                operation_steps,
                understanding_score,
                satisfaction_score,
                safety_score,
                language_score,
                user_feedback,
                create_time
            ) values (
                #{userId},
                #{source},
                #{scenario},
                #{requestText},
                #{intent},
                #{traceTool},
                #{traceStatus},
                #{resultCode},
                #{bizCode},
                #{success},
                #{durationMs},
                #{responseChars},
                #{errorMessage},
                #{testSessionId},
                #{participantCode},
                #{participantGroup},
                #{taskCode},
                #{taskName},
                #{completed},
                #{operationSteps},
                #{understandingScore},
                #{satisfactionScore},
                #{safetyScore},
                #{languageScore},
                #{userFeedback},
                now()
            )
            """)
    int insert(AssistantTestLog log);
}
