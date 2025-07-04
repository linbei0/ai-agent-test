/**
 * 消息类型定义
 */
export interface Message {
  text: string;
  sender: 'user' | 'ai';
  isTyping?: boolean;
  partialText?: string;
  timestamp?: string;
}

/**
 * API响应类型定义
 */
export interface ApiResponse<T> {
  code: number;
  data: T;
  message: string;
}