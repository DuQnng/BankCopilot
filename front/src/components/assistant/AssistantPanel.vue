<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { chatAssistantApi, streamAssistantApi, fetchAssistantBriefApi } from '@/api/assistant'
import * as echarts from 'echarts'
import request from '@/utils/request'

const props = defineProps({
  mode: {
    type: String,
    default: 'drawer'
  },
  defaultBriefPeriod: {
    type: String,
    default: 'auto'
  }
})
const instanceKey = `${props.mode}-${Math.random().toString(36).slice(2, 8)}`

const chatInput = ref('')
const chatLoading = ref(false)
const briefLoading = ref(false)
const briefData = ref(null)
const chatBodyRef = ref(null)
const selectedBriefPeriod = ref('month')

const chatList = ref([
  {
    role: 'assistant',
    content:
      '尊敬的用户您好，我是您的银行智能客服\n' +
      '我可以帮你查余额、查流水、转账确认，以及做账单统计与分析。\n' +
      '您可以尝试问：\n' +
      '1. 查一下我的余额\n' +
      '2. 查最近3条支出记录\n' +
      '3. 给老李转200元，备注午餐费\n' +
      '4. 统计这个月总支出\n' +
      '5. 看一下今年每个月的支出趋势'
  }
])

const briefPeriodText = (period) => (period === 'month' ? '本月' : '本周')

const resolveBriefPeriod = () => {
  if (props.defaultBriefPeriod === 'week' || props.defaultBriefPeriod === 'month') {
    return props.defaultBriefPeriod
  }
  const day = new Date().getDay()
  return day === 1 ? 'week' : 'month'
}

const buildExplainSummary = (explain) => {
  if (!explain) return ''
  const metricMap = { sum: '总额', count: '笔数', max: '最大值', trend: '趋势' }
  const txnType = explain.txnType || '全部'
  const metric = metricMap[explain.metric] || explain.metric || '统计'
  const start = explain.startTime ? explain.startTime.substring(0, 10) : '未指定'
  const end = explain.endTime ? explain.endTime.substring(0, 10) : '未指定'
  return `统计口径：${txnType} / ${metric}，时间范围 ${start} 至 ${end}`
}

const getAnomalyMessages = (brief) => {
  if (!brief) return []
  const messages = []
  if (brief.anomaly) {
    messages.push(brief.anomaly)
  }
  if (brief.anomalies && brief.anomalies.length) {
    brief.anomalies.forEach((item) => {
      if (item?.message) {
        messages.push(item.message)
      }
    })
  }
  return [...new Set(messages)]
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

const demoScripts = [
  '统计这个月总支出',
  '本月收入有多少笔',
  '看最近7天支出趋势',
  '查最近5条支出记录',
  '向6222000012345678转100元，备注午餐'
]

const scrollToBottom = async () => {
  await nextTick()
  const el = chatBodyRef.value
  if (el) el.scrollTop = el.scrollHeight
}

const initChart = async (idx, type, data) => {
  await nextTick()
  const dom = document.getElementById(`chart-${instanceKey}-${idx}`)
  if (!dom) return

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
      type,
      smooth: true,
      itemStyle: { color: '#409EFF' },
      barWidth: '40%'
    }]
  })
}

const initOverviewCharts = async (idx, data) => {
  await nextTick()

  const barDom = document.getElementById(`chart-bar-${instanceKey}-${idx}`)
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

  if (data.expensePie && data.expensePie.length > 0) {
    const expDom = document.getElementById(`chart-pie-exp-${instanceKey}-${idx}`)
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

  if (data.incomePie && data.incomePie.length > 0) {
    const incDom = document.getElementById(`chart-pie-inc-${instanceKey}-${idx}`)
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
  const dom = document.getElementById(`brief-chart-${instanceKey}`)
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

const loadAssistantBrief = async () => {
  if (briefLoading.value) return
  try {
    briefLoading.value = true
    const period = selectedBriefPeriod.value || resolveBriefPeriod()
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

const handleBriefPeriodChange = async (period) => {
  selectedBriefPeriod.value = period
  await loadAssistantBrief()
}

const fillDemoScript = () => {
  const random = demoScripts[Math.floor(Math.random() * demoScripts.length)]
  chatInput.value = random
}

const handleDownload = async (url) => {
  try {
    ElMessage.info('正在生成报表...')
    const res = await request({
      url,
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

const buildAssistantText = (m) => {
  const sections = [m.content || '']
  if (m.explain) {
    sections.push(buildExplainSummary(m.explain))
  }
  return sections.filter(Boolean).join('\n')
}

const copyAssistantText = async (m) => {
  try {
    const text = buildAssistantText(m)
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制回复内容')
  } catch (error) {
    ElMessage.error('复制失败，请检查浏览器权限')
  }
}

const exportAssistantText = (m, idx) => {
  const text = buildAssistantText(m)
  const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
  const objectUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = objectUrl
  link.setAttribute('download', `assistant-reply-${idx + 1}.txt`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(objectUrl)
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
    } catch (error) {
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

const needConfirm = () => {
  const last = [...chatList.value].reverse().find(x => x.role === 'assistant')
  return last && last.content.includes('回复【确认】')
}

onMounted(async () => {
  selectedBriefPeriod.value = resolveBriefPeriod()
  await loadAssistantBrief()
})
</script>

<template>
  <div class="assistant-panel" :class="{ home: mode === 'home' }">
    <div ref="chatBodyRef" class="chat-body">
      <div v-if="briefLoading" class="brief-card brief-loading">正在生成 AI 简报...</div>

      <div v-if="briefData" class="brief-card">
        <div class="brief-header">
          <div class="brief-title">{{ briefData.headline }}</div>
          <el-radio-group :model-value="selectedBriefPeriod" size="small" @change="handleBriefPeriodChange">
            <el-radio-button label="week">周报</el-radio-button>
            <el-radio-button label="month">月报</el-radio-button>
          </el-radio-group>
        </div>
        <div class="brief-subtitle">统计周期：{{ briefPeriodText(briefData.period) }}</div>
        <ul class="brief-list">
          <li v-for="(item, bIdx) in (briefData.highlights || [])" :key="`brief-${bIdx}`">{{ item }}</li>
        </ul>
        <ul v-if="briefData.suggestions && briefData.suggestions.length" class="brief-suggestion">
          <li v-for="(item, sIdx) in briefData.suggestions" :key="`suggestion-${sIdx}`">{{ item }}</li>
        </ul>
        <div v-if="getAnomalyMessages(briefData).length" class="brief-anomaly">
          <div v-for="(item, nIdx) in getAnomalyMessages(briefData)" :key="`anomaly-${nIdx}`">{{ item }}</div>
        </div>
        <div v-if="briefData.comparison" class="brief-comparison">
          <div>{{ briefData.comparison.currentPeriodLabel || '本期' }}净流入：{{ briefData.comparison.current?.net || '-' }}</div>
          <div>{{ briefData.comparison.previousPeriodLabel || '上期' }}净流入：{{ briefData.comparison.previous?.net || '-' }}</div>
          <div v-if="briefData.comparison.deltaPct && briefData.comparison.deltaPct.net != null">
            净流入变化：{{ briefData.comparison.deltaPct.net }}%
          </div>
        </div>
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
          :id="`brief-chart-${instanceKey}`"
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
          <div v-if="m.role === 'assistant' && !m.isStreaming" class="bubble-ops">
            <el-button size="small" text @click="copyAssistantText(m)">复制结果</el-button>
            <el-button size="small" text @click="exportAssistantText(m, idx)">导出文本</el-button>
          </div>

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

          <div v-if="m.chartType === 'number'" class="chart-number">
            <span>统计结果：</span>
            <span class="highlight">{{ m.chartData }}</span>
          </div>

          <div v-if="m.chartType === 'bar' || m.chartType === 'line'" class="trend-switch">
            <el-button size="small" :type="m.renderChartType === 'bar' ? 'primary' : 'default'" @click="switchTrendType(idx, 'bar')">柱状图</el-button>
            <el-button size="small" :type="m.renderChartType === 'line' ? 'primary' : 'default'" @click="switchTrendType(idx, 'line')">折线图</el-button>
          </div>

          <div
            v-if="m.chartType === 'bar' || m.chartType === 'line'"
            :id="`chart-${instanceKey}-${idx}`"
            class="chart-container"
          ></div>

          <div v-if="m.chartType === 'overview'" class="overview-charts">
            <div :id="`chart-bar-${instanceKey}-${idx}`" class="chart-container-small"></div>
            <div v-if="m.chartData.expensePie && m.chartData.expensePie.length > 0" :id="`chart-pie-exp-${instanceKey}-${idx}`" class="chart-container-small"></div>
            <div v-if="m.chartData.incomePie && m.chartData.incomePie.length > 0" :id="`chart-pie-inc-${instanceKey}-${idx}`" class="chart-container-small"></div>
          </div>

          <div v-if="m.exportUrl" class="export-link">
            <el-button type="success" size="small" plain @click="handleDownload(m.exportUrl)">
              <el-icon><Download /></el-icon> 下载明细报表
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="needConfirm()" class="quick-actions">
      <el-button type="success" @click="sendConfirm" :loading="chatLoading">确认</el-button>
      <el-button @click="sendCancel" :disabled="chatLoading">取消</el-button>
    </div>

    <div class="chat-input">
      <el-input
        v-model="chatInput"
        type="textarea"
        :autosize="{ minRows: 2, maxRows: 4 }"
        placeholder="输入一句话，例如：给6222...转200"
        @keyup.enter.exact.prevent="sendChat()"
      />
      <div class="chat-send">
        <el-button @click="fillDemoScript" :disabled="chatLoading">填入演示脚本</el-button>
        <el-button type="primary" @click="sendChat()" :loading="chatLoading">发送</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.assistant-panel {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 220px);
}

.assistant-panel.home {
  height: calc(100vh - 170px);
  max-width: 980px;
  margin: 0 auto;
}

.chat-body {
  flex: 1;
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

.chat-item.user .bubble {
  background: #d9ecff;
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

.bubble-ops {
  margin-top: 8px;
  display: flex;
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

.trend-switch {
  margin-top: 10px;
  display: flex;
  gap: 8px;
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

.brief-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
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

.brief-suggestion {
  margin: 8px 0;
  padding-left: 18px;
  color: #409eff;
  font-size: 12px;
}

.brief-comparison {
  margin: 8px 0;
  padding: 8px;
  border-left: 3px solid #67c23a;
  background: #f0f9eb;
  font-size: 12px;
  color: #606266;
  display: flex;
  flex-direction: column;
  gap: 4px;
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
</style>
