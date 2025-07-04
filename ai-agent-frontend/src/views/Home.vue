<template>
  <div class="home-container">
    <h1 class="home-title">AI Agent 应用中心</h1>
    
    <div class="app-grid">
      <router-link to="/love-app" class="app-card">
        <div class="app-icon love-icon">❤️</div>
        <div class="app-name">AI 恋爱大师</div>
      </router-link>
      
      <router-link to="/manus-app" class="app-card">
        <div class="app-icon manus-icon">🤖</div>
        <div class="app-name">AI 超级智能体</div>
      </router-link>
    </div>
  </div>
</template>

<script lang="ts" setup>
// Home组件不需要特殊逻辑
</script>

<style lang="scss" scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  min-height: 100vh;
}

.home-title {
  margin-bottom: 3rem;
  font-size: 2rem;
  color: #333;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 2rem;
  width: 100%;
  max-width: 800px;
}

.app-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  text-decoration: none;
  color: inherit;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  }
}

.app-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.app-name {
  font-size: 1.2rem;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-grid {
    grid-template-columns: 1fr;
  }
  
  .home-title {
    font-size: 1.5rem;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .app-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>