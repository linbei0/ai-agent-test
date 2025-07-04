import axios from 'axios';
import { v4 as uuidv4 } from 'uuid';

// 使用环境变量中的API基础URL
const API_BASE_URL = import.meta.env.VITE_APP_API_BASE_URL || '/api';

/**
 * AI恋爱大师聊天服务
 */
export const loveAppService = {
  /**
   * 创建SSE连接
   * @param message 用户消息
   * @param chatId 聊天ID，如果不提供则自动生成
   * @returns EventSource实例
   */
  createChatConnection(message: string, chatId?: string): EventSource {
    const id = chatId || uuidv4();
    const url = `${API_BASE_URL}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${id}`;
    return new EventSource(url);
  },
  
  /**
   * 创建语音聊天SSE连接
   * @param formData 包含音频文件的FormData
   * @param chatId 聊天ID
   * @returns EventSource实例
   */
  createAudioChatConnection(formData: FormData, chatId: string): Promise<EventSource> {
    return new Promise((resolve, reject) => {
      try {
        // 添加chatId到formData
        if (chatId) {
          formData.append('chatId', chatId);
        }
        
        // 创建一个新的XMLHttpRequest对象用于发送POST请求并接收SSE响应
        const xhr = new XMLHttpRequest();
        xhr.open('POST', `${API_BASE_URL}/ai/love_app/audio_chat`, true);
        xhr.setRequestHeader('Accept', 'text/event-stream');
        xhr.responseType = 'text';
        
        // 创建一个自定义的EventSource模拟对象
        const customEventSource = {
          onmessage: null as ((event: MessageEvent) => void) | null,
          onerror: null as ((event: Event) => void) | null,
          close: () => {
            xhr.abort();
          }
        };
        
        // 处理响应数据
        let buffer = '';
        let pendingData = ''; // 用于存储不完整的数据行
        
        xhr.onprogress = () => {
          // 获取新数据
          const newData = xhr.responseText.substring(buffer.length);
          buffer = xhr.responseText;
          
          // 将新数据添加到待处理数据
          pendingData += newData;
          
          // 处理完整的数据行
          const lines = pendingData.split('\n');
          // 保留最后一行（可能不完整）
          pendingData = lines.pop() || '';
          
          // 处理完整的行
          for (const line of lines) {
            const trimmedLine = line.trim();
            if (trimmedLine.startsWith('data:')) {
              const data = trimmedLine.substring(5).trim();
              if (data && customEventSource.onmessage) {
                customEventSource.onmessage(new MessageEvent('message', { data }));
              }
            }
          }
        };
        
        // 处理请求完成
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            // 处理最后可能剩余的数据
            if (pendingData.trim()) {
              const trimmedLine = pendingData.trim();
              if (trimmedLine.startsWith('data:')) {
                const data = trimmedLine.substring(5).trim();
                if (data && customEventSource.onmessage) {
                  customEventSource.onmessage(new MessageEvent('message', { data }));
                }
              }
            }
            
            // 请求成功完成，发送[DONE]消息
            if (customEventSource.onmessage) {
              customEventSource.onmessage(new MessageEvent('message', { data: '[DONE]' }));
            }
          } else {
            // 请求失败
            console.error('Audio chat request failed with status:', xhr.status);
            if (customEventSource.onerror) {
              customEventSource.onerror(new Event('error'));
            }
          }
        };
        
        // 处理请求错误
        xhr.onerror = (error) => {
          console.error('Audio chat request error:', error);
          if (customEventSource.onerror) {
            customEventSource.onerror(new Event('error'));
          }
        };
        
        // 发送请求
        xhr.send(formData);
        
        // 返回自定义EventSource对象
        resolve(customEventSource as unknown as EventSource);
      } catch (error) {
        console.error('音频处理异常:', error);
        reject(error);
      }
    });
  },
  
  /**
   * 生成新的聊天ID
   * @returns 新的UUID
   */
  generateChatId(): string {
    return uuidv4();
  }
};

/**
 * AI超级智能体聊天服务
 */
export const manusAppService = {
  /**
   * 发送消息到AI超级智能体
   * @param message 用户消息
   * @returns Promise包含AI响应
   */
  async sendMessage(message: string): Promise<string> {
    try {
      const response = await axios.get(`${API_BASE_URL}/ai/manus/chat`, {
        params: { message }
      });
      return response.data;
    } catch (error) {
      console.error('Error in manusAppService.sendMessage:', error);
      throw error;
    }
  }
};