import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: 'AI Agent 应用中心'
    }
  },
  {
    path: '/love-app',
    name: 'LoveApp',
    component: () => import('../views/LoveApp.vue'),
    meta: {
      title: 'AI 恋爱大师'
    }
  },
  {
    path: '/manus-app',
    name: 'ManusApp',
    component: () => import('../views/ManusApp.vue'),
    meta: {
      title: 'AI 超级智能体'
    }
  },
  // 404路由
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由标题设置
router.beforeEach((to, from, next) => {
  document.title = to.meta.title as string || 'AI Agent';
  next();
});

export default router;