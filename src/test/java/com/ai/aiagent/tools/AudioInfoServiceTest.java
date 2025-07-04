package com.ai.aiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AudioInfoServiceTest {

    @Test
    public void testParseAudioFile() throws Exception {
        AudioInfoService service = new AudioInfoService();

        // 假设 src/test/resources 下存在 test.wav 文件
        String filePath = "src/main/resources/Voice/OSR_cn_000_0072_8k.wav";

        AudioInfo info = service.parseAudioFile(filePath);

        assertNotNull(info);
    }
}
