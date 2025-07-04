<template>
  <div class="chat-container">
    <div class="chat-header">
      <router-link to="/" class="back-button">
        <span>&larr;</span> 返回
      </router-link>
      <h1>{{ title }}</h1>
    </div>
    
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(message, index) in messages" :key="index" 
           :class="['message', message.sender === 'ai' ? 'ai-message' : 'user-message']">
        <div class="message-content" :data-time="message.timestamp">
          <template v-if="message.sender === 'ai' && message.isTyping">
            <span class="typing-indicator" v-html="formatMessageText(message.partialText)"></span>
          </template>
          <template v-else>
            <span v-html="formatMessageText(message.text)"></span>
          </template>
        </div>
      </div>
    </div>
    
    <div class="chat-input">
      <input 
        type="text" 
        v-model="userInput" 
        placeholder="输入消息..." 
        @keyup.enter="handleSendMessage"
        :disabled="isProcessing"
      />
      <button 
        class="mic-button" 
        @click="toggleRecording" 
        :disabled="isProcessing"
        :class="{ 'recording': isRecording }"
      >
        <span class="mic-icon">🎤</span>
      </button>
      <button @click="handleSendMessage" :disabled="isProcessing || !userInput.trim()">
        发送
      </button>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted, watch, nextTick, defineProps, defineEmits, defineComponent } from 'vue';
import { formatChatTime } from '../utils/chatUtils';

// 确保组件有默认导出
defineComponent({name: 'ChatRoom'});

interface Message {
  text: string;
  sender: 'user' | 'ai';
  isTyping?: boolean;
  partialText?: string;
  timestamp?: string; // 添加时间戳
}

const props = defineProps<{
  title: string;
  welcomeMessage: string;
  themeColor: string;
  secondaryColor: string;
}>();

const emit = defineEmits<{
  (e: 'send-message', message: string): void;
  (e: 'send-audio', formData: FormData): void;
}>();

const userInput = ref('');
const messages = reactive<Message[]>([]);
const isProcessing = ref(false);
const isRecording = ref(false);
const messagesContainer = ref<HTMLElement | null>(null);
const mediaRecorder = ref<MediaRecorder | null>(null);
const audioChunks = ref<Blob[]>([]);
// 格式化消息文本，处理换行符和特殊字符
const formatMessageText = (text: string): string => {
  if (!text) return '';
  
  // 替换换行符为<br>
  let formattedText = text.replace(/\n/g, '<br>');
  
  // 处理连续空格（HTML会合并连续空格）
  formattedText = formattedText.replace(/ {2,}/g, (match) => {
    return '&nbsp;'.repeat(match.length);
  });
  
  return formattedText;
};
// 发送消息
const handleSendMessage = () => {
  const message = userInput.value.trim();
  if (!message || isProcessing.value) return;
  
  // 添加用户消息
  messages.push({
    text: message,
    sender: 'user',
    timestamp: formatChatTime()
  });
  
  // 清空输入框并设置处理状态
  userInput.value = '';
  isProcessing.value = true;
  
  // 添加AI消息占位
  messages.push({
    text: '',
    sender: 'ai',
    isTyping: true,
    partialText: '',
    timestamp: formatChatTime()
  });
  
  // 通知父组件
  emit('send-message', message);
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom();
  });
};

// 添加AI回复
const addAIResponse = (text: string, typing = true) => {
  if (typing) {
    // 查找最后一条AI消息
    const lastAIMessage = [...messages].reverse().find(m => m.sender === 'ai' && m.isTyping);
    if (lastAIMessage) {
      lastAIMessage.text = text;
      lastAIMessage.isTyping = false;
    } else {
      messages.push({
        text,
        sender: 'ai',
        timestamp: formatChatTime()
      });
    }
  } else {
    messages.push({
      text,
      sender: 'ai',
      timestamp: formatChatTime()
    });
  }
  
  isProcessing.value = false;
  nextTick(() => {
    scrollToBottom();
  });
};

// 更新打字机效果
const updateTypingText = (text: string) => {
  const lastAIMessage = [...messages].reverse().find(m => m.sender === 'ai' && m.isTyping);
  if (lastAIMessage) {
    // 累加文本而不是替换，确保打字机效果正确显示完整内容
    lastAIMessage.partialText = (lastAIMessage.partialText || '') + text;
    // 同时更新text字段，确保在完成时有完整内容
    lastAIMessage.text = lastAIMessage.partialText;
    
    // 添加调试日志
    console.log('更新消息:', text, '当前累计:', lastAIMessage.partialText);
    
    nextTick(() => {
      scrollToBottom();
    });
  }
};

// 完成打字
const finishTyping = () => {
  const lastAIMessage = [...messages].reverse().find(m => m.sender === 'ai' && m.isTyping);
  if (lastAIMessage) {
    lastAIMessage.text = lastAIMessage.partialText || '';
    lastAIMessage.isTyping = false;
  }
  isProcessing.value = false;
  return lastAIMessage; // 返回找到的最后一条AI消息
};

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
};

// 组件挂载时添加欢迎消息
onMounted(() => {
  messages.push({
    text: props.welcomeMessage,
    sender: 'ai',
    timestamp: formatChatTime()
  });
});

// 监听消息变化，自动滚动
watch(messages, () => {
  nextTick(() => {
    scrollToBottom();
  });
});

// 切换录音状态
const toggleRecording = async () => {
  if (isRecording.value) {
    stopRecording();
  } else {
    startRecording();
  }
};

// 开始录音
const startRecording = async () => {
  try {
    // 重置音频块
    audioChunks.value = [];
    
    // 请求麦克风权限，设置音频约束
    const stream = await navigator.mediaDevices.getUserMedia({ 
      audio: {
        channelCount: 1, // 单声道
        sampleRate: 16000, // 16K采样率
        echoCancellation: true,
        noiseSuppression: true
      } 
    });
    
    // 创建AudioContext以处理音频
    const audioContext = new AudioContext({
      sampleRate: 16000 // 确保16K采样率
    });
    
    // 创建MediaRecorder实例
    // 注意：不同浏览器支持的MIME类型不同，我们会在后续处理中转换为WAV
    const options = { mimeType: 'audio/webm' };
    mediaRecorder.value = new MediaRecorder(stream, options);
    
    // 收集音频数据
    mediaRecorder.value.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.value.push(event.data);
      }
    };
    
    // 录音停止后处理
    mediaRecorder.value.onstop = () => {
      // 停止所有音轨
      stream.getTracks().forEach(track => track.stop());
      
      // 处理录音数据
      processRecording();
    };
    
    // 开始录音
    mediaRecorder.value.start();
    isRecording.value = true;
    
    // 设置最大录音时间（60秒）
    setTimeout(() => {
      if (isRecording.value) {
        stopRecording();
      }
    }, 60000);
    
    // 显示录音提示
    const toast = document.createElement('div');
    toast.className = 'recording-toast';
    toast.textContent = '正在录音...点击麦克风按钮停止';
    document.body.appendChild(toast);
    
    // 3秒后自动隐藏提示
    setTimeout(() => {
      toast.classList.add('fade-out');
      setTimeout(() => {
        if (document.body.contains(toast)) {
          document.body.removeChild(toast);
        }
      }, 500);
    }, 3000);
  } catch (error) {
    console.error('Error starting recording:', error);
    alert('无法访问麦克风，请确保已授予麦克风权限。');
  }
};

// 停止录音
const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop();
    isRecording.value = false;
  }
};

// 处理录音数据
const processRecording = async () => {
  try {
    if (audioChunks.value.length === 0) return;
    
    // 创建音频Blob
    const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' });
    
    // 将Blob转换为ArrayBuffer
    const arrayBuffer = await audioBlob.arrayBuffer();
    
    // 创建AudioContext
    const audioContext = new AudioContext();
    
    // 解码音频数据
    const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
    
    // 创建WAV格式转换器
    const wavBuffer = convertToWav(audioBuffer);
    
    // 创建WAV格式的Blob
    const wavBlob = new Blob([wavBuffer], { type: 'audio/wav' });
    
    // 创建FormData对象
    const formData = new FormData();
    formData.append('audio', wavBlob, 'recording.wav');
    
    // 如果有聊天ID，添加到表单数据
    if (emit) {
      // 添加用户消息占位
      messages.push({
        text: '🎤 语音输入...',
        sender: 'user',
        timestamp: formatChatTime()
      });
      
      // 添加AI消息占位
      messages.push({
        text: '',
        sender: 'ai',
        isTyping: true,
        partialText: '',
        timestamp: formatChatTime()
      });
      
      // 设置处理状态
      isProcessing.value = true;
      
      // 通知父组件处理语音
      emit('send-audio', formData);
      
      // 滚动到底部
      nextTick(() => {
        scrollToBottom();
      });
    }
  } catch (error) {
    console.error('Error processing recording:', error);
    alert('处理录音时出错，请重试。');
    isProcessing.value = false;
  }
};

// 将AudioBuffer转换为WAV格式
const convertToWav = (audioBuffer: AudioBuffer): ArrayBuffer => {
  // 获取音频数据
  const numChannels = 1; // 单声道
  const sampleRate = 16000; // 16K采样率
  const format = 1; // PCM格式
  const bitDepth = 16; // 16位
  
  // 重采样到16K（如果需要）
  let samples: Float32Array;
  if (audioBuffer.sampleRate !== sampleRate) {
    // 简单的重采样方法
    const ratio = audioBuffer.sampleRate / sampleRate;
    const newLength = Math.round(audioBuffer.length / ratio);
    samples = new Float32Array(newLength);
    const data = audioBuffer.getChannelData(0);
    
    for (let i = 0; i < newLength; i++) {
      samples[i] = data[Math.floor(i * ratio)];
    }
  } else {
    samples = audioBuffer.getChannelData(0);
  }
  
  // 计算WAV文件大小
  const dataLength = samples.length * (bitDepth / 8);
  const buffer = new ArrayBuffer(44 + dataLength);
  const view = new DataView(buffer);
  
  // WAV文件头
  // "RIFF"标识
  view.setUint8(0, 'R'.charCodeAt(0));
  view.setUint8(1, 'I'.charCodeAt(0));
  view.setUint8(2, 'F'.charCodeAt(0));
  view.setUint8(3, 'F'.charCodeAt(0));
  
  // 文件大小
  view.setUint32(4, 36 + dataLength, true);
  
  // "WAVE"标识
  view.setUint8(8, 'W'.charCodeAt(0));
  view.setUint8(9, 'A'.charCodeAt(0));
  view.setUint8(10, 'V'.charCodeAt(0));
  view.setUint8(11, 'E'.charCodeAt(0));
  
  // "fmt "子块
  view.setUint8(12, 'f'.charCodeAt(0));
  view.setUint8(13, 'm'.charCodeAt(0));
  view.setUint8(14, 't'.charCodeAt(0));
  view.setUint8(15, ' '.charCodeAt(0));
  
  // 子块大小
  view.setUint32(16, 16, true);
  
  // 音频格式（PCM）
  view.setUint16(20, format, true);
  
  // 声道数
  view.setUint16(22, numChannels, true);
  
  // 采样率
  view.setUint32(24, sampleRate, true);
  
  // 字节率
  view.setUint32(28, sampleRate * numChannels * (bitDepth / 8), true);
  
  // 块对齐
  view.setUint16(32, numChannels * (bitDepth / 8), true);
  
  // 位深度
  view.setUint16(34, bitDepth, true);
  
  // "data"子块
  view.setUint8(36, 'd'.charCodeAt(0));
  view.setUint8(37, 'a'.charCodeAt(0));
  view.setUint8(38, 't'.charCodeAt(0));
  view.setUint8(39, 'a'.charCodeAt(0));
  
  // 数据大小
  view.setUint32(40, dataLength, true);
  
  // 写入音频数据
  const volume = 0.9; // 音量控制
  let offset = 44;
  for (let i = 0; i < samples.length; i++, offset += 2) {
    const sample = Math.max(-1, Math.min(1, samples[i] * volume));
    view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true);
  }
  
  return buffer;
};

// 暴露方法给父组件
defineExpose({
  addAIResponse,
  updateTypingText,
  finishTyping
});
</script>

<style lang="scss" scoped>
:root {
  --theme-color: v-bind('props.themeColor');
  --secondary-color: v-bind('props.secondaryColor');
  --theme-color-light: v-bind('`${props.themeColor}99`'); /* 60% opacity for lighter version */
  --theme-color-lighter: v-bind('`${props.themeColor}66`'); /* 40% opacity for even lighter */
  --theme-color-dark: v-bind('`${props.themeColor}dd`'); /* 87% opacity for darker version */
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  background-color: #f8f9fa;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 1rem;
  background-color: var(--theme-color);
  color: white;
  position: relative;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 10;
  
  h1 {
    margin: 0 auto;
    font-size: 1.5rem;
    font-weight: 600;
    letter-spacing: 0.5px;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
  
  .back-button {
    position: absolute;
    left: 1rem;
    color: white;
    text-decoration: none;
    display: flex;
    align-items: center;
    padding: 0.5rem;
    border-radius: 50%;
    transition: all 0.3s ease;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.2);
      transform: translateX(-2px);
    }
    
    span {
      margin-right: 0.5rem;
      font-size: 1.2rem;
    }
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background-image: linear-gradient(to bottom, rgba(255,255,255,0.8) 0%, rgba(255,255,255,0.9) 100%), 
                    url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23bdbdbd' fill-opacity='0.1' fill-rule='evenodd'/%3E%3C/svg%3E");
  background-position: center;
  background-size: 150px 150px;
  scroll-behavior: smooth;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.2);
    border-radius: 3px;
  }
}

.message {
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: 1rem;
  word-break: break-word;
  animation: fadeIn 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  position: relative;
  margin: 0.5rem 0;
  
  &::before {
    content: '';
    position: absolute;
    bottom: 0;
    width: 12px;
    height: 12px;
  }
  
  &.ai-message {
    align-self: flex-start;
    background-color: #f0f0f0;
    color: #333;
    border: 1px solid #e0e0e0;
    border-bottom-left-radius: 0.25rem;
    margin-right: 2rem;
    animation: fadeIn 0.3s ease, slideInLeft 0.4s ease;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    
    &::before {
      left: -6px;
      border-right: 12px solid #f0f0f0;
      border-bottom-right-radius: 12px 6px;
    }
    
    &:hover {
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
      transition: all 0.3s ease;
    }
  }
  
  &.user-message {
    align-self: flex-end;
    background-color: var(--theme-color);
    color: white;
    border-bottom-right-radius: 0.25rem;
    margin-left: 2rem;
    animation: fadeIn 0.3s ease, slideInRight 0.4s ease;
    
    &::before {
      right: -6px;
      border-left: 12px solid var(--theme-color);
      border-bottom-left-radius: 12px 6px;
    }
    
    &:hover {
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
      transform: translateY(-2px);
      transition: all 0.3s ease;
    }
  }
}

.typing-indicator {
  display: inline-block;
  position: relative;
  min-height: 1em;
  color: #333;
  font-weight: normal;
  
  &::after {
    content: '|';
    display: inline-block;
    margin-left: 2px;
    font-weight: bold;
    animation: blink 1s infinite;
    opacity: 0.9;
    color: #555;
  }
}

@keyframes blink {
  0%, 100% { opacity: 0; }
  50% { opacity: 1; }
}

.message-content {
  word-break: break-word;
  white-space: pre-wrap;
  position: relative;
  font-size: 1rem;
  line-height: 1.5;
  
  .user-message & {
    color: white;
    font-weight: 500;
    text-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
  }
  
  .ai-message & {
    color: #333;
    font-weight: normal;
  }
  
  &::after {
    content: attr(data-time);
    position: absolute;
    font-size: 0.7rem;
    color: #888;
    white-space: nowrap;
    opacity: 0;
    transition: opacity 0.2s ease;
  }
  
  .ai-message & {
    &::after {
      right: 0;
      bottom: -18px;
      color: #888;
    }
  }
  
  .user-message & {
    &::after {
      left: 0;
      bottom: -18px;
      color: rgba(255, 255, 255, 0.8);
    }
  }
  
  &:hover::after {
    opacity: 1;
  }
}

/* 删除重复的blink动画 */

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}

.chat-input {
  display: flex;
  padding: 1rem;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  
  input {
    flex: 1;
    padding: 0.75rem 1rem;
    border: 1px solid #e0e0e0;
    border-radius: 2rem;
    margin-right: 0.5rem;
    font-size: 1rem;
    outline: none;
    transition: all 0.3s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    
    &:focus {
      border-color: var(--theme-color);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      transform: translateY(-1px);
    }
    
    &:disabled {
      background-color: #f9f9f9;
      cursor: not-allowed;
    }
  }
  
  button {
    padding: 0.75rem 1.5rem;
    background-color: var(--theme-color);
    color: white;
    border: none;
    border-radius: 2rem;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    outline: none;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    text-shadow: 0 1px 1px rgba(0, 0, 0, 0.2);
    
    &:hover:not(:disabled) {
      background-color: var(--theme-color-dark);
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
    }
    
    &:disabled {
      background-color: #aaa;
      opacity: 0.7;
      cursor: not-allowed;
    }
  }
  
  .mic-button {
    padding: 0.75rem;
    margin-right: 0.5rem;
    border-radius: 50%;
    width: 2.5rem;
    height: 2.5rem;
    background-color: #f0f0f0;
    color: #555;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    
    .mic-icon {
      font-size: 1.2rem;
      position: relative;
      z-index: 2;
    }
    
    &:hover:not(:disabled) {
      background-color: #e0e0e0;
      transform: translateY(-2px);
    }
    
    &.recording {
      background-color: #ff4d4f;
      color: white;
      animation: pulse 1.5s infinite;
      
      &::before {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 5px;
        height: 5px;
        background: rgba(255, 255, 255, 0.5);
        opacity: 0;
        border-radius: 100%;
        transform: scale(1, 1) translate(-50%);
        transform-origin: 50% 50%;
        animation: ripple 2s infinite;
      }
      
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 5px;
        height: 5px;
        background: rgba(255, 255, 255, 0.5);
        opacity: 0;
        border-radius: 100%;
        transform: scale(1, 1) translate(-50%);
        transform-origin: 50% 50%;
        animation: ripple 2s 0.5s infinite;
      }
      
      &:hover:not(:disabled) {
        background-color: #ff7875;
      }
    }
  }
  
  @keyframes ripple {
  0% {
    transform: scale(0, 0);
    opacity: 1;
  }
  20% {
    transform: scale(25, 25);
    opacity: 0.8;
  }
  100% {
    opacity: 0;
    transform: scale(40, 40);
  }
}

.recording-toast {
  position: fixed;
  bottom: 80px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 14px;
  z-index: 1000;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  transition: opacity 0.5s ease;
  
  &.fade-out {
    opacity: 0;
  }
}
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message {
    max-width: 85%;
  }
  
  .chat-header h1 {
    font-size: 1.2rem;
  }
  
  .chat-input {
    padding: 0.75rem;
    
    input {
      padding: 0.5rem 0.75rem;
    }
    
    button {
      padding: 0.5rem 1rem;
    }
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .message {
    max-width: 75%;
  }
}
</style>
