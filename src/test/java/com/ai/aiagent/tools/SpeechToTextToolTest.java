package com.ai.aiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class SpeechToTextToolTest {
    @Value("${youdao.appKey}")
    private String appKey;
    
    @Value("${youdao.appSecret}")
    private String appSecret;
    
    @Test
    public void testSpeechToText() {
        SpeechToTextTool speechToTextTool = new SpeechToTextTool(appKey, appSecret);
        // 使用中文普通话进行测试
        String result = speechToTextTool.convertSpeechToText(
            "src/main/resources/Voice/OSR_cn_000_0072_8k.wav", "zh-CHS");
        assertNotNull(result);
    }
}