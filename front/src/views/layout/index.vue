<script setup>
import {ref, onMounted, nextTick, watch} from 'vue';
import {ElMessageBox, ElMessage} from 'element-plus';
import { useRouter } from 'vue-router';
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
import { chatAssistantApi, streamAssistantApi, fetchAssistantBriefApi } from '@/api/assistant'
import * as echarts from 'echarts'
import request from '@/utils/request'

// ====== 智能客服 Drawer ======
const assistantVisible = ref(false)
const chatInput = ref('')
const chatLoading = ref(false)
const briefLoading = ref(false)
const briefData = ref(null)

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

const briefPeriodText = (period) => (period === 'month' ? '本月' : '本周')

const getDefaultBriefPeriod = () => {
  const day = new Date().getDay()
  return day === 1 ? 'week' : 'month'
}

const loadAssistantBrief = async () => {
  if (briefLoading.value) return
  try {
    briefLoading.value = true
    const period = getDefaultBriefPeriod()
    const res = await fetchAssistantBriefApi(period)
    if (res.code && res.data) {
      briefData.value = res.data
      await renderBriefChart()
    } else {
      briefData.value = null
    }
  } catch (error) {
    briefData.value = null
  } finally {
    briefLoading.value = false
  }
}

const openAssistantDrawer = async () => {
  assistantVisible.value = true
}

watch(assistantVisible, async (visible) => {
  if (visible) {
    await loadAssistantBrief()
  }
})

const buildExplainSummary = (explain) => {
  if (!explain) return ''
  const metricMap = { sum: '总额', count: '笔数', max: '最大值', trend: '趋势' }
  const txnType = explain.txnType || '全部'
  const metric = metricMap[explain.metric] || explain.metric || '统计'
  const start = explain.startTime ? explain.startTime.substring(0, 10) : '未指定'
  const end = explain.endTime ? explain.endTime.substring(0, 10) : '未指定'
  return `统计口径：${txnType} / ${metric}，时间范围 ${start} 至 ${end}`
}

const normalizeTraceSteps = (trace, streamSteps = []) => {
  const merged = [...streamSteps]
  if (trace?.steps?.length) {
    trace.steps.forEach((step) => {
      if (!merged.includes(step)) merged.push(step)
    })
  }
  return merged
}

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
  const current = echarts.getInstanceByDom(dom)
  if (current) current.dispose()
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
    const current = echarts.getInstanceByDom(barDom)
    if (current) current.dispose()
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
      const current = echarts.getInstanceByDom(expDom)
      if (current) current.dispose()
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
      const current = echarts.getInstanceByDom(incDom)
      if (current) current.dispose()
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

const renderAssistantCharts = async (idx) => {
  const msg = chatList.value[idx]
  if (!msg) return
  if (msg.chartType === 'bar' || msg.chartType === 'line') {
    await initChart(idx, msg.renderChartType || msg.chartType, msg.chartData || [])
  } else if (msg.chartType === 'overview') {
    await initOverviewCharts(idx, msg.chartData || {})
  }
}

const switchTrendType = async (idx, type) => {
  const msg = chatList.value[idx]
  if (!msg || !(msg.chartType === 'bar' || msg.chartType === 'line')) return
  msg.renderChartType = type
  await initChart(idx, type, msg.chartData || [])
}

const renderBriefChart = async () => {
  await nextTick()
  if (!briefData.value || briefData.value.chartType !== 'bar') return
  const dom = document.getElementById('brief-chart')
  if (!dom) return
  const current = echarts.getInstanceByDom(dom)
  if (current) current.dispose()
  const chart = echarts.init(dom)
  const list = briefData.value.chartData || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '15%', right: '5%', bottom: '15%', top: '15%' },
    xAxis: { type: 'category', data: list.map(item => item.label) },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: list.map(item => item.value),
      barWidth: '40%',
      itemStyle: { color: '#909399' }
    }]
  })
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

  const assistantMsg = {
    role: 'assistant',
    content: '正在分析你的请求...',
    chartType: null,
    renderChartType: null,
    chartData: null,
    exportUrl: null,
    trace: null,
    explain: null,
    streamSteps: ['已接收请求，准备解析意图'],
    isStreaming: true
  }
  chatList.value.push(assistantMsg)
  const assistantIndex = chatList.value.length - 1

  try {
    chatLoading.value = true
    await streamAssistantApi(msg, {
      onEvent: async (event, data) => {
        const current = chatList.value[assistantIndex]
        if (!current) return
        if (event === 'trace') {
          if (data?.message && !current.streamSteps.includes(data.message)) {
            current.streamSteps.push(data.message)
          }
        } else if (event === 'message') {
          current.content = data?.reply || '（机器人没有返回内容）'
          current.chartType = data?.chartType || null
          current.renderChartType = data?.chartType || null
          current.chartData = data?.chartData || null
          current.exportUrl = data?.exportUrl || null
          current.trace = data?.trace || null
          current.explain = data?.explain || null
          current.streamSteps = normalizeTraceSteps(current.trace, current.streamSteps)
          await renderAssistantCharts(assistantIndex)
        } else if (event === 'error') {
          current.content = data?.message || '流式请求失败，请稍后再试'
        } else if (event === 'done') {
          current.isStreaming = false
        }
        await scrollToBottom()
      }
    })
  } catch (streamError) {
    try {
      const res = await chatAssistantApi(msg)
      const current = chatList.value[assistantIndex]
      if (!current) return
      if (res.code) {
        current.content = res.data?.reply || '（机器人没有返回内容）'
        current.chartType = res.data?.chartType || null
        current.renderChartType = res.data?.chartType || null
        current.chartData = res.data?.chartData || null
        current.exportUrl = res.data?.exportUrl || null
        current.trace = res.data?.trace || null
        current.explain = res.data?.explain || null
        current.streamSteps = normalizeTraceSteps(current.trace, current.streamSteps)
        await renderAssistantCharts(assistantIndex)
      } else {
        current.content = res.msg || '请求失败'
      }
      current.isStreaming = false
    } catch (e) {
      const current = chatList.value[assistantIndex]
      if (current) {
        current.content = '网络/接口异常，请稍后再试'
        current.isStreaming = false
      }
    }
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
            <a href="javascript:;" @click="openAssistantDrawer">
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
          <div v-if="briefLoading" class="brief-card brief-loading">正在生成 AI 简报...</div>

          <div v-if="briefData" class="brief-card">
            <div class="brief-title">{{ briefData.headline }}</div>
            <div class="brief-subtitle">统计周期：{{ briefPeriodText(briefData.period) }}</div>
            <ul class="brief-list">
              <li v-for="(item, bIdx) in (briefData.highlights || [])" :key="`brief-${bIdx}`">{{ item }}</li>
            </ul>
            <div v-if="briefData.anomaly" class="brief-anomaly">{{ briefData.anomaly }}</div>
            <div class="brief-actions">
              <el-button
                v-for="(action, aIdx) in (briefData.quickActions || [])"
                :key="`action-${aIdx}`"
                size="small"
                plain
                @click="sendChat(action)"
              >
                {{ action }}
              </el-button>
            </div>
            <div
              v-if="briefData.chartType === 'bar' && briefData.chartData && briefData.chartData.length"
              id="brief-chart"
              class="brief-chart"
            ></div>
          </div>

          <div
            v-for="(m, idx) in chatList"
            :key="idx"
            class="chat-item"
            :class="m.role"
          >
            <div class="bubble">
              <div style="white-space: pre-line;">{{ m.content }}</div>

              <div v-if="m.isStreaming" class="streaming-flag">AI 正在处理中...</div>

              <div v-if="m.trace || (m.streamSteps && m.streamSteps.length)" class="trace-card">
                <div class="trace-title">AI处理过程</div>
                <div class="trace-steps">
                  <span v-for="(step, sIdx) in m.streamSteps" :key="`${idx}-${sIdx}`" class="trace-step">
                    {{ sIdx + 1 }}. {{ step }}
                  </span>
                </div>
                <div v-if="m.trace" class="trace-meta">
                  <el-tag size="small" type="info">意图：{{ m.trace.intent || 'unknown' }}</el-tag>
                  <el-tag size="small" type="success">能力：{{ m.trace.tool || 'assistant' }}</el-tag>
                  <el-tag size="small" type="warning">状态：{{ m.trace.status || 'completed' }}</el-tag>
                  <el-tag size="small">置信度：{{ m.trace.confidence || 'medium' }}</el-tag>
                </div>
              </div>

              <div v-if="m.explain" class="explain-line">
                {{ buildExplainSummary(m.explain) }}
              </div>
              
              <!-- 数字指标 (如求和、计数) -->
              <div v-if="m.chartType === 'number'" class="chart-number">
                <span>统计结果：</span>
                <span class="highlight">{{ m.chartData }}</span>
              </div>

              <div v-if="m.chartType === 'bar' || m.chartType === 'line'" class="trend-switch">
                <el-button size="small" :type="m.renderChartType === 'bar' ? 'primary' : 'default'" @click="switchTrendType(idx, 'bar')">柱状图</el-button>
                <el-button size="small" :type="m.renderChartType === 'line' ? 'primary' : 'default'" @click="switchTrendType(idx, 'line')">折线图</el-button>
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

.brief-card {
  margin: 4px 4px 12px;
  padding: 12px;
  border-radius: 10px;
  background: #f0f9ff;
  border: 1px solid #d9ecff;
}

.brief-loading {
  color: #409eff;
  font-size: 13px;
}

.brief-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.brief-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #606266;
}

.brief-list {
  margin: 8px 0;
  padding-left: 18px;
  color: #303133;
  font-size: 12px;
}

.brief-anomaly {
  margin: 8px 0;
  padding: 8px;
  border-left: 3px solid #e6a23c;
  background: #fdf6ec;
  font-size: 12px;
  color: #606266;
}

.brief-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.brief-chart {
  width: 100%;
  height: 180px;
  margin-top: 10px;
  background: #fff;
  border-radius: 8px;
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

.streaming-flag {
  margin-top: 8px;
  color: #409eff;
  font-size: 12px;
}

.trace-card {
  margin-top: 10px;
  padding: 10px;
  border-radius: 8px;
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
}

.trace-title {
  font-weight: 600;
  margin-bottom: 6px;
  font-size: 13px;
  color: #303133;
}

.trace-steps {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.trace-step {
  font-size: 12px;
  color: #606266;
}

.trace-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.explain-line {
  margin-top: 10px;
  padding: 8px 10px;
  border-left: 3px solid #409eff;
  background: #ecf5ff;
  font-size: 12px;
  color: #303133;
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

.trend-switch {
  margin-top: 10px;
  display: flex;
  gap: 8px;
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
