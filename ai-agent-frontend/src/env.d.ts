/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string;
  readonly VITE_APP_API_BASE_URL: string;
  // 可以添加更多环境变量
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}