/**
 * 模拟打字机效果
 * @param text 完整文本
 * @param callback 每次更新时的回调函数
 * @param completeCallback 完成时的回调函数
 * @param speed 打字速度（毫秒）
 */
export const simulateTyping = (
  text: string,
  callback: (text: string) => void,
  completeCallback: () => void,
  speed = 30
): { stop: () => void } => {
  let currentIndex = 0;
  let currentText = '';
  let intervalId: number | null = null;
  
  const type = () => {
    if (currentIndex < text.length) {
      currentText += text[currentIndex];
      callback(currentText);
      currentIndex++;
    } else {
      if (intervalId !== null) {
        clearInterval(intervalId);
        intervalId = null;
      }
      completeCallback();
    }
  };
  
  intervalId = window.setInterval(type, speed);
  
  // 返回一个对象，允许提前停止打字
  return {
    stop: () => {
      if (intervalId !== null) {
        clearInterval(intervalId);
        intervalId = null;
        completeCallback();
      }
    }
  };
};

/**
 * 处理SSE连接
 * @param eventSource EventSource实例
 * @param onMessage 消息处理函数
 * @param onComplete 完成处理函数
 * @param onError 错误处理函数
 */
export const handleSSEConnection = (
  eventSource: EventSource,
  onMessage: (data: string) => void,
  onComplete: () => void,
  onError: (error: Event) => void
): void => {
  // 标记是否已经完成，避免在正常完成后仍然触发错误处理
  let isCompleted = false;
  
  eventSource.onmessage = (event) => {
    const data = event.data;
    if (data === '[DONE]') {
      isCompleted = true;
      eventSource.close();
      onComplete();
    } else {
      onMessage(data);
    }
  };
  
  eventSource.onerror = (error) => {
    // 只有在未完成状态下才触发错误处理
    if (!isCompleted) {
      // 标记为已完成，防止多次触发错误处理
      isCompleted = true;
      eventSource.close();
      onError(error);
    }
  };
};

/**
 * 格式化聊天时间
 * @returns 格式化的时间字符串
 */
export const formatChatTime = (): string => {
  const now = new Date();
  return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};