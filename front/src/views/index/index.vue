<script setup>
import AssistantPanel from '@/components/assistant/AssistantPanel.vue'
</script>

<template>
  <div class="home-ai-page">
    <div class="home-title">
      <h2>AI 银行助手</h2>
      <p>左侧可进入业务菜单，右上角可打开智能客服。</p>
    </div>
    <AssistantPanel mode="home" default-brief-period="month" />
  </div>
</template>

<style scoped>
.home-ai-page {
  padding: 10px 0;
}

.home-title {
  max-width: 980px;
  margin: 0 auto 12px;
}

.home-title h2 {
  margin: 0;
  color: #303133;
}

.home-title p {
  margin: 6px 0 0;
  color: #606266;
  font-size: 13px;
}
</style>
