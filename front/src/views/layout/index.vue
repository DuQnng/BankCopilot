<script setup>
import {ref, onMounted} from 'vue';
import {ElMessageBox, ElMessage} from 'element-plus';
import { useRouter } from 'vue-router';
import axios from 'axios'  
import { changePasswordApi } from '@/api/user' 

//当前登录员工
const loginName = ref('');
const router = useRouter();
const pwdDialogVisible = ref(false)

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
//钩子函数
onMounted(() => {
  const loginUser = JSON.parse(localStorage.getItem('loginUser'));
  if(loginUser && loginUser.name){
    loginName.value = loginUser.name;
  }
})

//退出登录
const logout = () => {
  //弹出确认框
  ElMessageBox.confirm('您确认退出登录吗?','提示',
    { confirmButtonText: '确认',cancelButtonText: '取消',type: 'warning'}
  ).then(async () => { //确认
    ElMessage.success('退出成功');
    localStorage.removeItem('loginUser');
    //跳转页面-登录
    router.push('/login');
  }).catch(() => { //取消
    ElMessage.info('您已取消退出');
  })
}

// 修改密码部分逻辑：


// 确认按钮点击事件
const submitPwd = async () => {

  // ========= 前端校验 =========
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword || !pwdForm.value.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }

  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  try {
    const result = await changePasswordApi({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    })

    if (result.code) {
      ElMessage.success('密码修改成功，请重新登录')
      pwdDialogVisible.value = false
      localStorage.removeItem('loginUser')
      router.push('/login')
    } else {
      ElMessage.error(result.msg || '修改失败')
    }

  } catch (error) {
    ElMessage.error('修改密码接口异常')
  }
}

//以下为聊天机器人部分逻辑
import { nextTick } from 'vue'
import { chatAssistantApi } from '@/api/assistant'
import * as echarts from 'echarts'
import request from '@/utils/request'

// ====== 智能客服 Drawer ======
const assistantVisible = ref(false)
const chatInput = ref('')
const chatLoading = ref(false)

// 聊天消息列表：role = 'user' | 'assistant'
const chatList = ref([
  {
    role: 'assistant',
    content:
      '尊敬的用户您好，我是您的银行智能客服\n' +
      '我可以帮你查余额、查流水、转账确认，以及做账单统计与分析。\n' +
      '您可以尝试问：\n' +
      '1. 查一下我的余额\n' +
      '2. 查最近3条支出记录\n' +
      '3. 向6222...转200元，备注午餐费\n' +
      '4. 统计这个月总支出\n' +
      '5. 看一下今年每个月的支出趋势'
  }
])

const chatBodyRef = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  const el = chatBodyRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const initChart = async (idx, type, data) => {
  await nextTick()
  const dom = document.getElementById('chart-' + idx)
  if (!dom) return
  
  // 确保图表容器有实际宽高后再初始化
  const myChart = echarts.init(dom)
  const xAxisData = data.map(item => item.label)
  const seriesData = data.map(item => item.value)
  
  myChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '15%', right: '5%', bottom: '15%', top: '10%' },
    xAxis: { type: 'category', data: xAxisData },
    yAxis: { type: 'value' },
    series: [{
      data: seriesData,
      type: type,
      smooth: true,
      itemStyle: { color: '#409EFF' },
      barWidth: '40%'
    }]
  })
}

const initOverviewCharts = async (idx, data) => {
  await nextTick()
  
  // 初始化柱状图 (收入/支出)
  const barDom = document.getElementById(`chart-bar-${idx}`)
  if (barDom) {
    const myChart = echarts.init(barDom)
    myChart.setOption({
      title: { text: '收支对比', left: 'center', textStyle: { fontSize: 13, fontWeight: 'normal' } },
      tooltip: { trigger: 'item' },
      grid: { left: '15%', right: '5%', bottom: '15%', top: '25%' },
      xAxis: { type: 'category', data: data.bar.map(item => item.label) },
      yAxis: { type: 'value' },
      series: [{
        data: data.bar.map(item => ({
          value: item.value,
          itemStyle: { color: item.label === '收入' ? '#67c23a' : '#f56c6c' }
        })),
        type: 'bar',
        barWidth: '40%'
      }]
    })
  }

  // 初始化支出饼图
  if (data.expensePie && data.expensePie.length > 0) {
    const expDom = document.getElementById(`chart-pie-exp-${idx}`)
    if (expDom) {
      const myChart = echarts.init(expDom)
      myChart.setOption({
        title: { text: '支出占比(按对方)', left: 'center', textStyle: { fontSize: 13, fontWeight: 'normal' } },
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: '50%',
          center: ['50%', '60%'],
          data: data.expensePie.map(item => ({ name: item.label, value: item.value }))
        }]
      })
    }
  }

  // 初始化收入饼图
  if (data.incomePie && data.incomePie.length > 0) {
    const incDom = document.getElementById(`chart-pie-inc-${idx}`)
    if (incDom) {
      const myChart = echarts.init(incDom)
      myChart.setOption({
        title: { text: '收入占比(按对方)', left: 'center', textStyle: { fontSize: 13, fontWeight: 'normal' } },
        tooltip: { trigger: 'item' },
        series: [{
          type: 'pie',
          radius: '50%',
          center: ['50%', '60%'],
          data: data.incomePie.map(item => ({ name: item.label, value: item.value }))
        }]
      })
    }
  }
}

const handleDownload = async (url) => {
  try {
    ElMessage.info('正在生成报表...')
    const res = await request({
      url: url,
      method: 'get',
      responseType: 'blob'
    })
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const objectUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.setAttribute('download', '智能统计报表.xlsx')
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(objectUrl)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const sendChat = async (text) => {
  const msg = (text ?? chatInput.value).trim()
  if (!msg || chatLoading.value) return

  chatList.value.push({ role: 'user', content: msg })
  chatInput.value = ''
  await scrollToBottom()

  try {
    chatLoading.value = true
    const res = await chatAssistantApi(msg)

    if (res.code) {
      chatList.value.push({ 
        role: 'assistant', 
        content: res.data?.reply || '（机器人没有返回内容）',
        chartType: res.data?.chartType,
        chartData: res.data?.chartData,
        exportUrl: res.data?.exportUrl
      })
      
      // 如果返回了图表数据，渲染图表
      if (res.data?.chartType === 'bar' || res.data?.chartType === 'line') {
        const newIdx = chatList.value.length - 1
        initChart(newIdx, res.data.chartType, res.data.chartData)
      } else if (res.data?.chartType === 'overview') {
        const newIdx = chatList.value.length - 1
        initOverviewCharts(newIdx, res.data.chartData)
      }
    } else {
      chatList.value.push({ role: 'assistant', content: res.msg || '请求失败' })
    }
  } catch (e) {
    chatList.value.push({ role: 'assistant', content: '网络/接口异常，请稍后再试' })
  } finally {
    chatLoading.value = false
    await scrollToBottom()
  }
}

const sendConfirm = () => sendChat('确认')
const sendCancel = () => sendChat('取消')

// 简单判断：最后一条机器人消息是否在等确认
const needConfirm = () => {
  const last = [...chatList.value].reverse().find(x => x.role === 'assistant')
  return last && last.content.includes('回复【确认】')
}


</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- Header 区域 -->
      <el-header class="header">
        <span class="title">银行智能机器人</span>
        <span class="right_tool">
            <a href="javascript:;" @click="assistantVisible = true">
              <el-icon><ChatLineRound /></el-icon> 智能客服 &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;
            </a>

            <a href="javascript:;" @click="pwdDialogVisible = true">
              <el-icon><EditPen /></el-icon> 修改密码 &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;
            </a>

            <a href="javascript:;" @click="logout">
              <el-icon><SwitchButton /></el-icon> 退出登录 
            </a>
        </span>
      </el-header>

      <!-- 修改密码 -->
  <el-dialog
  title="修改密码"
  v-model="pwdDialogVisible"
  width="420px"
  >
  <el-form
    :model="pwdForm"
    label-width="90px"
  >
    <el-form-item label="原密码">
      <el-input
        v-model="pwdForm.oldPassword"
        type="password"
        show-password
      />
    </el-form-item>

    <el-form-item label="新密码">
      <el-input
        v-model="pwdForm.newPassword"
        type="password"
        show-password
      />
    </el-form-item>

    <el-form-item label="确认密码">
      <el-input
        v-model="pwdForm.confirmPassword"
        type="password"
        show-password
      />
    </el-form-item>
  </el-form>

    <template #footer>
      <el-button @click="pwdDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitPwd">确认修改</el-button>
    </template>
  </el-dialog>
   

<!-- 以下为客服机器人部分css -->
 <!-- 智能客服 Drawer -->
      <el-drawer
        v-model="assistantVisible"
        title="智能客服"
        size="420px"
      >
        <!-- 消息区 -->
        <div ref="chatBodyRef" class="chat-body">
          <div
            v-for="(m, idx) in chatList"
            :key="idx"
            class="chat-item"
            :class="m.role"
          >
            <div class="bubble">
              <div style="white-space: pre-line;">{{ m.content }}</div>
              
              <!-- 数字指标 (如求和、计数) -->
              <div v-if="m.chartType === 'number'" class="chart-number">
                <span>统计结果：</span>
                <span class="highlight">{{ m.chartData }}</span>
              </div>

              <!-- ECharts 图表容器 -->
              <div 
                v-if="m.chartType === 'bar' || m.chartType === 'line'" 
                :id="'chart-' + idx" 
                class="chart-container"
              ></div>

              <!-- 概览多图容器 -->
              <div v-if="m.chartType === 'overview'" class="overview-charts">
                <div :id="'chart-bar-' + idx" class="chart-container-small"></div>
                <div v-if="m.chartData.expensePie && m.chartData.expensePie.length > 0" :id="'chart-pie-exp-' + idx" class="chart-container-small"></div>
                <div v-if="m.chartData.incomePie && m.chartData.incomePie.length > 0" :id="'chart-pie-inc-' + idx" class="chart-container-small"></div>
              </div>

              <!-- 原始数据下载链接 -->
              <div v-if="m.exportUrl" class="export-link">
                <el-button type="success" size="small" plain @click="handleDownload(m.exportUrl)">
                  <el-icon><Download /></el-icon> 下载明细报表
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷按钮：等确认时显示 -->
        <div v-if="needConfirm()" class="quick-actions">
          <el-button type="success" @click="sendConfirm" :loading="chatLoading">确认</el-button>
          <el-button @click="sendCancel" :disabled="chatLoading">取消</el-button>
        </div>

        <!-- 输入区 -->
        <div class="chat-input">
          <el-input
            v-model="chatInput"
            type="textarea"
            :autosize="{ minRows: 2, maxRows: 4 }"
            placeholder="输入一句话，例如：给6222...转200"
            @keyup.enter.exact.prevent="sendChat()"
          />
          <div class="chat-send">
            <el-button type="primary" @click="sendChat()" :loading="chatLoading">发送</el-button>
          </div>
        </div>
      </el-drawer>

     


      <el-container>
        <!-- 左侧菜单 -->
        <el-aside width="200px" class="aside">
          <!-- 左侧菜单栏 -->
          <el-menu router>
            <!-- 首页菜单 -->
            <el-menu-item index="/index">
              <el-icon><Promotion /></el-icon> 首页
            </el-menu-item>
            
            <el-menu-item index="/account">
              <el-icon><Wallet /></el-icon> 账户总览
            </el-menu-item>

            <el-menu-item index="/transfer">
              <el-icon><Switch  /></el-icon> 转账
            </el-menu-item>

            <el-menu-item index="/transactions">
              <el-icon><Document /></el-icon> 交易流水
            </el-menu-item>

            <!-- 数据统计管理 -->
            <!-- <el-sub-menu index="/report">
              <template #title>
                <el-icon><Histogram /></el-icon>数据统计管理
              </template>
              <el-menu-item index="/empReport">
                <el-icon><InfoFilled /></el-icon>员工信息统计
              </el-menu-item>
              <el-menu-item index="/stuReport">
                <el-icon><Share /></el-icon>学员信息统计
              </el-menu-item>
              <el-menu-item index="/log">
                <el-icon><Document /></el-icon>日志信息统计
              </el-menu-item>
            </el-sub-menu> -->

          </el-menu>
        </el-aside>
        
        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>
      
    </el-container>
  </div>
</template>

<style scoped>
.header {
  height: 64px;
  background: linear-gradient(90deg, #0f2027, #203a43, #2c5364);
  display: flex;
  align-items: center;
  padding: 0 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.title {
  color: white;
  font-size: 40px;
  font-family: 微软雅黑;
  line-height: 60px;
  font-weight: bolder;
}

.right_tool {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
}

a {
  color: white;
  text-decoration: none;
}

.aside {
  width: 220px;
  border-right: 1px solid #ccc;
  height: 730px;
}

.chat-body {
  height: calc(100vh - 220px);
  overflow-y: auto;
  padding: 12px;
  background: #f6f7fb;
  border-radius: 10px;
}

.chat-item {
  display: flex;
  margin: 8px 0;
}

.chat-item.user {
  justify-content: flex-end;
}

.chat-item.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 85%;
  padding: 10px 12px;
  border-radius: 12px;
  line-height: 1.5;
  background: white;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
}

.chart-number {
  margin-top: 12px;
  padding: 10px;
  background: #f0f9eb;
  border-radius: 6px;
  font-size: 14px;
  color: #67c23a;
  text-align: center;
}

.chart-number .highlight {
  font-size: 24px;
  font-weight: bold;
}

.chart-container {
  width: 100%;
  height: 250px;
  margin-top: 15px;
  background: white;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.overview-charts {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chart-container-small {
  width: 100%;
  height: 200px;
  background: white;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.export-link {
  margin-top: 15px;
  text-align: right;
}

.chat-item.user .bubble {
  background: #d9ecff;
}

.quick-actions {
  padding: 8px 12px;
  display: flex;
  gap: 8px;
}

.chat-input {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-send {
  display: flex;
  justify-content: flex-end;
}

</style>
