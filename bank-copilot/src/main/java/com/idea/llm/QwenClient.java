package com.idea.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QwenClient {
    @Value("${dashscope.api-key:}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baseUrl;

    @Value("${dashscope.model:qwen-plus}")
    private String model;

    public JsonNode chat(List<Map<String, String>> messages) {
        String key = (apiKey != null && !apiKey.isBlank()) ? apiKey : System.getenv("DASHSCOPE_API_KEY");
        if (key == null || key.isBlank()) {
            throw new RuntimeException("DashScope API Key 未配置：请在 application.yml 配置 dashscope.api-key 或设置环境变量 DASHSCOPE_API_KEY");
        }

        WebClient client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + key)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "temperature", 0
        );

        String resp = client.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            return objectMapper.readTree(resp);
        } catch (Exception e) {
            throw new RuntimeException("解析大模型返回失败");
        }
    }
}
