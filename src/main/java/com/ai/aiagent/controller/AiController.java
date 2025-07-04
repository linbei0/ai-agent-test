package com.ai.aiagent.controller;

import com.ai.aiagent.agent.MyManus;
import com.ai.aiagent.app.LoveApp;
import com.ai.aiagent.tools.SpeechToTextTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;
    @Value("${youdao.appKey}")
    private String appKey;

    @Value("${youdao.appSecret}")
    private String appSecret;

//    @Resource
//    private SpeechToTextTool speechToTextTool;

    /**
     * 语音问答（AI 恋爱大师应用）
     * @param audioFile 上传的音频文件
     * @param chatId 会话ID
     * @return 流式响应
     */
    @PostMapping(value = "/love_app/audio_chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> audioChatWithLoveApp(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam(required = false) String chatId) {
        return Flux.defer(() -> {
            try {
                // 保存临时文件
                String tempFilePath = Files.createTempFile("audio_", ".wav").toString();
                audioFile.transferTo(Paths.get(tempFilePath));
                SpeechToTextTool speechToTextTool = new SpeechToTextTool(appKey, appSecret);
                // 语音转文字
                String text = speechToTextTool.convertSpeechToText(tempFilePath, "zh-CHS");

                // 调用AI流式接口
                return loveApp.doChatByStream(text, chatId);
            } catch (Exception e) {
                log.error("语音问答处理失败: {}", e.getMessage(), e);
                return Flux.just("语音识别失败: " + e.getMessage());
            }
        });
    }

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * SSE 流式调用 AI 恋爱大师应用
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/love_app/chat/sse_emitter")
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter sseEmitter = new SseEmitter(180000L); // 3 分钟超时
        // 获取 Flux 响应式数据流并且直接通过订阅推送给 SseEmitter
        loveApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        // 返回
        return sseEmitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        MyManus myManus = new MyManus(allTools, dashscopeChatModel);
        return myManus.runStream(message);
    }
}

