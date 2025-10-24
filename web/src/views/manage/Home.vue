<!--
 * 后台管理欢迎页（美化版）
 * 保持功能零改动，仅优化样式与排版
-->
<template>
  <section class="home-hero">
    <!-- 背景装饰（纯 CSS，不引入任何资源） -->
    <div class="bg-shape s1"></div>
    <div class="bg-shape s2"></div>
    <div class="bg-shape s3"></div>

    <!-- 居中欢迎卡片 -->
    <div class="hero-card">
      <h1 class="title">好物集商城后台管理系统</h1>
      <p class="subtitle">Welcome to Admin Console</p>
      <div class="divider"></div>
    </div>
  </section>
</template>

<script>
export default {
  name: "ManageHome",
};
</script>

<style scoped>
/* 容器：居中 + 柔和背景 */
.home-hero {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  /* 高度做兼容：既能撑满页面，又不顶到左右侧边栏；可按你布局适当调 */
  min-height: 72vh;

  padding: 32px;
  border-radius: 16px;

  /* 柔和浅色渐变背景 */
  background: linear-gradient(135deg, #eef2ff 0%, #f8fafc 60%, #f1f5f9 100%);
  overflow: hidden;
}

/* 玻璃拟态欢迎卡片 */
.hero-card {
  width: 100%;
  max-width: 900px;
  text-align: center;
  padding: 48px 56px;

  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);

  border-radius: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

/* 标题：渐变文字 */
.title {
  margin: 0 0 10px;
  font-weight: 800;
  letter-spacing: 0.5px;
  line-height: 1.25;
  /* 自适应字号 */
  font-size: clamp(28px, 3.2vw, 40px);

  background: linear-gradient(90deg, #1d4ed8 0%, #4f46e5 50%, #06b6d4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 副标题：低饱和提示 */
.subtitle {
  margin: 0 0 18px;
  color: #64748b;
  font-size: clamp(14px, 1.6vw, 16px);
}

/* 细分割线 */
.divider {
  height: 1px;
  width: 160px;
  margin: 0 auto;
  background: linear-gradient(90deg, transparent, #cbd5e1, transparent);
  border-radius: 999px;
}

/* 背景漂浮装饰形状（纯 CSS） */
.bg-shape {
  position: absolute;
  filter: blur(38px);
  opacity: 0.5;
  transform: translateZ(0);
  animation: float 14s ease-in-out infinite;
}

/* 三个不同位置与颜色的柔光块 */
.s1 {
  top: -60px;
  left: -60px;
  width: 320px;
  height: 320px;
  background: radial-gradient(60% 60% at 50% 50%, #60a5fa 0%, rgba(96, 165, 250, 0) 70%);
  animation-delay: 0s;
}

.s2 {
  right: -80px;
  bottom: -80px;
  width: 360px;
  height: 360px;
  background: radial-gradient(60% 60% at 50% 50%, #a78bfa 0%, rgba(167, 139, 250, 0) 70%);
  animation-delay: 2.2s;
}

.s3 {
  left: 50%;
  bottom: -100px;
  width: 300px;
  height: 300px;
  transform: translateX(-50%);
  background: radial-gradient(60% 60% at 50% 50%, #5eead4 0%, rgba(94, 234, 212, 0) 70%);
  animation-delay: 4.1s;
}

/* 轻微上下漂浮动画 */
@keyframes float {
  0%   { transform: translateY(0) translateZ(0) scale(1); }
  50%  { transform: translateY(-12px) translateZ(0) scale(1.03); }
  100% { transform: translateY(0) translateZ(0) scale(1); }
}

/* 适当缩小小屏 padding，保持观感 */
@media (max-width: 768px) {
  .hero-card {
    padding: 36px 28px;
  }
}
</style>
