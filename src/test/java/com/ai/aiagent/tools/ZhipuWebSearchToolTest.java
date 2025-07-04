package com.ai.aiagent.tools;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@ActiveProfiles("local")
class ZhipuWebSearchToolTest {

    @Value("${zhipu-web-search.api-key}")
    private String searchApiKey;
    @Test
    public void testSearchWeb() {
        ObjectMapper objectMapper = new ObjectMapper();
        ZhipuWebSearchTool zhipuWebSearchTool = new ZhipuWebSearchTool(objectMapper,searchApiKey);
        String query = "美食排行";
        String result = zhipuWebSearchTool.webSearch(query);
        assertNotNull(result);
    }
}