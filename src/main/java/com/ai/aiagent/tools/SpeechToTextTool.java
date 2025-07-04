package com.ai.aiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import cn.hutool.http.HttpUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Base64;

/**
 * 语音转文字工具类，基于有道AI开放平台API
 */
@Slf4j
public class SpeechToTextTool {

    private static final String YOUDAO_URL = "https://openapi.youdao.com/asrapi";
    private final String appKey;
    private final String appSecret;

    public SpeechToTextTool(String appKey, String appSecret) {
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    /**
     * 将音频文件转换为文字
     * @param filePath 音频文件路径
     * @param langType 源语言类型
     * @return 识别后的文本
     */
    @Tool(description = "Convert audio file to text using Youdao ASR API")
    public String convertSpeechToText(
            @ToolParam(description = "Audio file path") String filePath,
            @ToolParam(description = "Source language type") String langType) {
        try {
            // 读取音频文件并转为Base64编码
            String q = loadAsBase64(filePath);

            // 生成签名参数
            Map<String, String> params = new HashMap<>();
            params.put("appKey", appKey);
            params.put("q", q);
            params.put("format", "wav");
            params.put("rate", "16000");
            params.put("channel", "1");
            params.put("type", "1");
            params.put("langType", langType);

            String salt = UUID.randomUUID().toString();
            params.put("salt", salt);

            String curtime = String.valueOf(System.currentTimeMillis() / 1000);
            params.put("curtime", curtime);

            String signStr = appKey + truncate(q) + salt + curtime + appSecret;
            String sign = getDigest(signStr);
            params.put("sign", sign);
            params.put("signType", "v2");

            // 发送HTTP请求
            return doRequest(YOUDAO_URL, params);

        } catch (Exception e) {
            log.error("语音转文字处理失败：{}", e.getMessage(), e);
            return "Error converting speech to text: " + e.getMessage();
        }
    }

    /**
     * 加载音频文件为Base64编码字符串
     */
    private String loadAsBase64(String filename) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(filename));
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 截断处理方法
     */
    private String truncate(String q) {
        if (q == null) {
            return null;
        }
        int len = q.length();
        return len <= 20 ? q : (q.substring(0, 10) + len + q.substring(len - 10, len));
    }

    /**
     * SHA256加密生成签名
     */
    private String getDigest(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        byte[] btInput = string.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest mdInst = MessageDigest.getInstance("SHA-256");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 执行HTTP POST请求
     */
    private String doRequest(String url, Map<String, String> requestParams) {
        try {
            // 将Map参数转换为查询字符串形式
            List<String> paramList = new ArrayList<>();
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                paramList.add(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            String formData = String.join("&", paramList);

            // 使用Hutool构建POST请求
            String result = HttpUtil.createPost(url)
                    .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .body(formData)
                    .timeout(10000)
                    .execute().body();
            return result != null ? result : "";
        } catch (Exception e) {
            log.error("网络请求失败：{}", e.getMessage(), e);
            return "网络请求失败：" + e.getMessage();
        }
    }
}