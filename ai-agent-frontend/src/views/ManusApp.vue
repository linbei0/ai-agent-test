<template>
  <ChatRoom 
    ref="chatRoom"
    title="AI 超级智能体"
    welcomeMessage="你好！我是AI超级智能体，有什么问题需要解答吗？"
    themeColor="#0277bd"
    secondaryColor="#bbdefb"
    @send-message="sendMessage"
  />
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import ChatRoom from '../components/ChatRoom.vue';
import { manusAppService } from '../services/apiService';
import { simulateTyping } from '../utils/chatUtils';

// 引用ChatRoom组件
const chatRoom = ref<InstanceType<typeof ChatRoom> | null>(null);

// 发送消息处理函数
const sendMessage = async (message: string) => {
  try {
    // 调用API获取响应
    const response = await manusAppService.sendMessage(message);
    
    // 模拟打字机效果
    simulateTyping(
      response,
      (text) => {
        chatRoom.value?.updateTypingText(text);
      },
      () => {
        chatRoom.value?.finishTyping();
      }
    );
  } catch (error) {
    console.error('Error in ManusApp.sendMessage:', error);
    chatRoom.value?.addAIResponse('抱歉，发生了错误，请稍后再试。');
  }
};
</script>