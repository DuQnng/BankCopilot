<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const openAssistantTip = () => {
  // 最省事：提示用户点右上角“智能客服”
  // 如果你愿意做全局通信，后面我再带你升级成“一键打开 Drawer”
  alert('请点击右上角【智能客服】开始对话～')
}
</script>

<template>
  <el-card style="max-width: 900px; margin: 20px auto; padding: 10px;">
    <h2 style="margin: 0 0 8px;">欢迎进入银行智能机器人系统</h2>
    <div style="color:#666; line-height:1.6;">
      右上角打开智能客服机器人。
    </div>

    <div style="margin-top: 14px; display:flex; gap:10px; flex-wrap:wrap;">
      <el-button type="primary" @click="openAssistantTip">打开智能客服</el-button>
      <el-button @click="router.push('/account')">账户总览</el-button>
      <el-button @click="router.push('/transfer')">转账</el-button>
      <el-button @click="router.push('/transactions')">交易流水</el-button>
    </div>
  </el-card>

  <el-card style="max-width: 900px; margin: 0 auto; padding: 10px;">
    <h3 style="margin:0 0 10px;">试试这样问</h3>
    <div style="display:flex; gap:8px; flex-wrap:wrap;">
      <el-tag>查余额</el-tag>
      <el-tag>查最近10条流水</el-tag>
      <el-tag>给 6222...3210 转 200 备注111</el-tag>
      <el-tag type="success">确认</el-tag>
      <el-tag type="info">取消</el-tag>
    </div>
    <div style="margin-top: 10px; color:#888;">
      提示：涉及转账等敏感操作会让你再次确认，这是为了安全。
    </div>
  </el-card>
</template>

<style scoped>
/* 先不花里胡哨，干净就行 */
</style>
