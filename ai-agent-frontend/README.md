# AI Agent 前端应用

这是一个基于Vue3的AI聊天应用前端项目，包含两个主要功能模块：AI恋爱大师和AI超级智能体。

## 项目特点

- 使用Vue3 + TypeScript + Vite构建
- 基于Composition API的状态管理
- 响应式设计，适配不同设备
- 组件化架构，高度可复用
- SSE长连接实时通信
- 打字机效果的消息显示

## 项目结构

```
src/
├── assets/           # 静态资源
├── components/       # 可复用组件
│   └── ChatRoom.vue  # 聊天室组件
├── views/            # 页面组件
│   ├── Home.vue      # 主页
│   ├── LoveApp.vue   # AI恋爱大师
│   └── ManusApp.vue  # AI超级智能体
├── services/         # 接口服务层
│   └── apiService.ts # API服务
├── utils/            # 工具函数
│   └── chatUtils.ts  # 聊天工具函数
├── router/           # 路由配置
│   └── index.ts      # 路由定义
├── App.vue           # 主应用组件
└── main.ts           # 入口文件
```

## 功能模块

### 主页 (Home)

- 应用切换导航
- 展示应用图标与名称
- 使用Vue Router进行页面跳转

### AI 恋爱大师 (LoveApp)

- 聊天室布局：左侧AI消息/右侧用户消息
- 使用SSE长连接实时通信
- 自动生成chatId (UUID)
- 实时渲染对话内容（打字机效果）
- 支持消息发送与接收状态管理

### AI 超级智能体 (ManusApp)

- 与LoveApp相同UI结构
- 使用Axios调用API
- 同样实现打字机效果

## 技术实现

### 响应式设计

- 使用Flexbox布局
- 媒体查询断点设置：
  - 移动端: max-width: 768px
  - 平板: min-width: 769px and max-width: 1024px

### 状态管理

- 使用Vue3 Composition API
- 聊天记录使用reactive对象管理
- 接口调用封装到services层

### SSE连接管理

- 在组件生命周期中管理连接
- 使用工具函数处理SSE事件
- 组件卸载时清理资源

## 开发环境

- Node.js v20.12.2
- Windows 11系统
- 推荐使用VSCode + Volar插件

## 安装与运行

```bash
# 安装依赖
npm install

# 开发模式运行
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

## 后端API

项目依赖以下后端API：

- AI恋爱大师: `http://localhost:7123/api/ai/love_app/chat/sse?message=xxx&chatId=xxx`
- AI超级智能体: `http://localhost:7123/api/ai/manus/chat?message=xxx`

请确保后端服务已启动并可访问。
