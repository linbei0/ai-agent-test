<template>
  <ChatRoom 
    ref="chatRoom"
    title="AI 恋爱大师"
    welcomeMessage="你好！我是AI恋爱大师，有什么恋爱问题需要咨询吗？"
    themeColor="#9c27b0"
    secondaryColor="#e1bee7"
    @send-message="sendMessage"
    @send-audio="sendAudio"
  />
</template>

<script lang="ts" setup>
import { ref, onUnmounted } from 'vue';
import ChatRoom from '../components/ChatRoom.vue';
import { loveAppService } from '../services/apiService';
import { handleSSEConnection } from '../utils/chatUtils';

// 引用ChatRoom组件
const chatRoom = ref<InstanceType<typeof ChatRoom> | null>(null);

// 生成聊天ID
const chatId = loveAppService.generateChatId();
let eventSource: EventSource | null = null;

// 发送消息处理函数
const sendMessage = (message: string) => {
  try {
    // 关闭之前的连接
    if (eventSource) {
      eventSource.close();
    }
    
    // 创建新的SSE连接
    eventSource = loveAppService.createChatConnection(message, chatId);
    
    // 处理SSE连接
    handleSSEConnection(
      eventSource,
      // 消息处理
      (data: string) => {
        chatRoom.value?.updateTypingText(data);
      },
      // 完成处理
      () => {
        chatRoom.value?.finishTyping();
      },
      // 错误处理
      () => {
        // 检查是否有正在输入的消息
        const lastMessage = chatRoom.value?.finishTyping();
        // 只有在没有成功接收到任何消息时才显示错误
        if (!lastMessage || lastMessage.text.trim() === '') {
          chatRoom.value?.addAIResponse('连接错误，请重试', false);
        }
      }
    );
  } catch (error) {
    console.error('Error in LoveApp.sendMessage:', error);
    chatRoom.value?.addAIResponse('抱歉，发生了错误，请稍后再试。');
  }
};

// 发送语音处理函数
const sendAudio = async (formData: FormData) => {
  try {
    // 关闭之前的连接
    if (eventSource) {
      eventSource.close();
    }
    
    // 创建新的语音SSE连接
    eventSource = await loveAppService.createAudioChatConnection(formData, chatId);
    
    // 处理SSE连接
    handleSSEConnection(
      eventSource,
      // 消息处理
      (data: string) => {
        chatRoom.value?.updateTypingText(data);
      },
      // 完成处理
      () => {
        chatRoom.value?.finishTyping();
      },
      // 错误处理
      () => {
        // 检查是否有正在输入的消息
        const lastMessage = chatRoom.value?.finishTyping();
        // 只有在没有成功接收到任何消息时才显示错误
        if (!lastMessage || lastMessage.text.trim() === '') {
          chatRoom.value?.addAIResponse('语音识别失败，请重试', false);
        }
      }
    );
  } catch (error) {
    console.error('Error in LoveApp.sendAudio:', error);
    chatRoom.value?.addAIResponse('抱歉，语音处理失败，请稍后再试。');
  }
};

// 组件卸载时清理资源
onUnmounted(() => {
  if (eventSource) {
    eventSource.close();
    eventSource = null;
  }
});
</script>

<style lang="scss" scoped>
:root {
  --theme-color: #ff6b6b;
  --theme-color-light: #ff9e9e;
  --theme-color-dark: #e55a5a;
  --background-color: #f9f9f9;
  --text-color: #333;
  --border-color: #e0e0e0;
}
/* 可以添加组件特定的样式 */
</style>