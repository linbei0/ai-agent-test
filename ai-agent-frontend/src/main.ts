import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'

const app = createApp(App)

// 注册路由
app.use(router)

// 挂载应用
app.mount('#app')
