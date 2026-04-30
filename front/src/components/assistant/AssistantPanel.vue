<script setup>
import { ref, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import {
  chatAssistantApi,
  streamAssistantApi,
  fetchAssistantBriefApi,
  startAnonymousFeedbackTestApi,
  finishAnonymousFeedbackTestApi
} from '@/api/assistant'
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
const voiceSupported = ref(true)
const voiceListening = ref(false)
const feedbackSessionKey = 'assistantAnonymousFeedbackSession'
const feedbackTestActive = ref(false)
const feedbackStartVisible = ref(false)
const feedbackEndVisible = ref(false)
const feedbackStartLoading = ref(false)
const feedbackEndLoading = ref(false)
const feedbackSession = ref(null)
const selectedFeedbackTaskCode = ref('U1')
const feedbackTaskStats = ref({})
const feedbackInputStepRecorded = ref(false)
const feedbackGuideCollapsed = ref(false)
const createDefaultFeedbackScores = () => ({
  understandingScore: 5,
  satisfactionScore: 5,
  safetyScore: 5,
  languageScore: 5,
  userFeedback: ''
})
const feedbackTaskScores = ref({})
const feedbackTaskScoreForm = ref(createDefaultFeedbackScores())
const feedbackStartForm = ref({
  participantGroup: '普通手机银行用户',
  roleNote: ''
})
const feedbackEndForm = ref({
  understandingScore: 5,
  satisfactionScore: 5,
  safetyScore: 5,
  languageScore: 5,
  feedback: ''
})

const participantGroupOptions = [
  '计算机专业学生',
  '普通手机银行用户',
  '计算机行业开发者',
  '银行业务/企业导师',
  '其他'
]

const feedbackScoreItems = [
  { field: 'understandingScore', label: '理解感' },
  { field: 'satisfactionScore', label: '满足感' },
  { field: 'safetyScore', label: '安全感' },
  { field: 'languageScore', label: '语言表达' }
]

const feedbackTasks = [
  {
    taskCode: 'U1',
    taskName: '查询账户余额',
    guide: '用自己的话询问当前虚拟账户余额。',
    examples: ['我卡里余额现在是多少', '帮我看一下现在账户还有多少钱']
  },
  {
    taskCode: 'U2',
    taskName: '查询最近5条支出流水',
    guide: '询问最近几笔支出、消费或交易明细。',
    examples: ['最近五条花出去的钱列出来', '给我最近5条支出交易']
  },
  {
    taskCode: 'U3',
    taskName: '银行卡安全FAQ',
    guide: '咨询银行卡丢失、冻结、挂失等安全类问题。',
    examples: ['银行卡疑似丢失怎么冻结', '卡不见了要不要先挂失']
  },
  {
    taskCode: 'U4',
    taskName: '转账到账FAQ',
    guide: '咨询行内、跨行、夜间或节假日转账到账时间。',
    examples: ['晚上转账会不会第二天才到', '跨行转账一般多久能到账']
  },
  {
    taskCode: 'U5',
    taskName: '语音查询余额',
    guide: '点击语音输入，说出余额查询需求后发送。',
    examples: ['语音输入：账户里还有多少钱', '语音输入：看一下我卡里余额']
  },
  {
    taskCode: 'U6',
    taskName: '支出统计图表',
    guide: '询问本月支出总额，并观察文字统计和图表。',
    examples: ['本月消费总数和图表给我看看', '这个月花费汇总一下']
  },
  {
    taskCode: 'U7',
    taskName: '转账后取消',
    guide: '发起一笔转账，出现确认信息后点击取消。',
    examples: ['给弟弟转300元午餐费', '转300给弟弟备注午餐费']
  },
  {
    taskCode: 'U8',
    taskName: '投诉情绪安抚',
    guide: '输入带有投诉、焦虑或生气的表达，观察人工客服引导。',
    examples: ['我现在很生气，想投诉这个服务', '我要投诉，刚才的处理让我不舒服']
  }
]

let speechRecognition = null
let voiceBaseInput = ''

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

const getActiveFeedbackPayload = () => {
  if (!feedbackTestActive.value || !feedbackSession.value) return null
  const task = getCurrentFeedbackTask()
  const taskEvaluation = normalizeFeedbackScores(feedbackTaskScoreForm.value)
  return {
    testSessionId: feedbackSession.value.testSessionId,
    participantCode: feedbackSession.value.participantCode,
    participantGroup: feedbackSession.value.participantGroup,
    taskCode: task.taskCode,
    taskName: task.taskName,
    operationSteps: getPendingFeedbackSteps(task.taskCode) + 1,
    ...taskEvaluation
  }
}

const getCurrentFeedbackTask = () => {
  return feedbackTasks.find(item => item.taskCode === selectedFeedbackTaskCode.value) || feedbackTasks[0]
}

const getFeedbackTaskCount = (taskCode) => {
  return feedbackTaskStats.value[taskCode]?.count || 0
}

const getPendingFeedbackSteps = (taskCode) => {
  return feedbackTaskStats.value[taskCode]?.pendingSteps || 0
}

const getCompletedFeedbackCount = () => {
  return feedbackTasks.filter(item => getFeedbackTaskCount(item.taskCode) > 0).length
}

const persistFeedbackState = () => {
  if (!feedbackSession.value) return
  localStorage.setItem(feedbackSessionKey, JSON.stringify({
    ...feedbackSession.value,
    selectedTaskCode: selectedFeedbackTaskCode.value,
    taskStats: feedbackTaskStats.value,
    taskScores: feedbackTaskScores.value,
    guideCollapsed: feedbackGuideCollapsed.value
  }))
}

const selectFeedbackTask = (taskCode) => {
  saveFeedbackTaskScores()
  selectedFeedbackTaskCode.value = taskCode
  loadFeedbackTaskScores(taskCode)
  addFeedbackTaskStep(taskCode)
}

const toggleFeedbackGuideCollapsed = () => {
  feedbackGuideCollapsed.value = !feedbackGuideCollapsed.value
  persistFeedbackState()
}

const fillFeedbackExample = (text, taskCode = selectedFeedbackTaskCode.value) => {
  if (selectedFeedbackTaskCode.value !== taskCode) {
    selectFeedbackTask(taskCode)
  }
  chatInput.value = text
  feedbackInputStepRecorded.value = true
  addFeedbackTaskStep(taskCode)
}

const normalizeFeedbackScores = (scores = {}) => {
  const normalized = {}
  feedbackScoreItems.forEach(({ field }) => {
    const value = Number(scores[field])
    normalized[field] = Number.isInteger(value) ? Math.min(5, Math.max(1, value)) : 5
  })
  normalized.userFeedback = (scores.userFeedback || '').trim()
  return normalized
}

const loadFeedbackTaskScores = (taskCode = selectedFeedbackTaskCode.value) => {
  feedbackTaskScoreForm.value = normalizeFeedbackScores(feedbackTaskScores.value[taskCode])
}

const saveFeedbackTaskScores = () => {
  if (!/^U[1-8]$/.test(selectedFeedbackTaskCode.value || '')) return
  feedbackTaskScores.value = {
    ...feedbackTaskScores.value,
    [selectedFeedbackTaskCode.value]: normalizeFeedbackScores(feedbackTaskScoreForm.value)
  }
  persistFeedbackState()
}

const handleFeedbackScoreChange = () => {
  saveFeedbackTaskScores()
}

const markFeedbackInputEdited = () => {
  if (!feedbackTestActive.value || feedbackInputStepRecorded.value || !chatInput.value.trim()) return
  feedbackInputStepRecorded.value = true
  addFeedbackTaskStep(selectedFeedbackTaskCode.value)
}

const addFeedbackTaskStep = (taskCode, amount = 1) => {
  if (!feedbackTestActive.value || !/^U[1-8]$/.test(taskCode || '')) return
  const current = feedbackTaskStats.value[taskCode] || { count: 0, pendingSteps: 0 }
  feedbackTaskStats.value = {
    ...feedbackTaskStats.value,
    [taskCode]: {
      ...current,
      pendingSteps: (current.pendingSteps || 0) + amount
    }
  }
  persistFeedbackState()
}

const markFeedbackTaskAttempt = (taskCode, operationSteps) => {
  if (!/^U[1-8]$/.test(taskCode || '')) return
  const current = feedbackTaskStats.value[taskCode] || { count: 0, pendingSteps: 0 }
  feedbackTaskStats.value = {
    ...feedbackTaskStats.value,
    [taskCode]: {
      ...current,
      count: current.count + 1,
      pendingSteps: 0,
      lastOperationSteps: operationSteps
    }
  }
  persistFeedbackState()
}

const appendAssistantNotice = async (content) => {
  chatList.value.push({ role: 'assistant', content })
  await scrollToBottom()
}

const restoreFeedbackSession = () => {
  try {
    const raw = localStorage.getItem(feedbackSessionKey)
    if (!raw) return
    const session = JSON.parse(raw)
    if (session?.testSessionId && session?.participantCode) {
      feedbackSession.value = session
      feedbackTestActive.value = true
      selectedFeedbackTaskCode.value = session.selectedTaskCode || 'U1'
      feedbackTaskStats.value = session.taskStats || {}
      feedbackTaskScores.value = session.taskScores || {}
      loadFeedbackTaskScores(selectedFeedbackTaskCode.value)
      feedbackGuideCollapsed.value = Boolean(session.guideCollapsed)
    }
  } catch (error) {
    localStorage.removeItem(feedbackSessionKey)
  }
}

const openFeedbackStart = () => {
  feedbackStartVisible.value = true
}

const startFeedbackTest = async () => {
  if (!feedbackStartForm.value.participantGroup) {
    ElMessage.warning('请选择匿名测试角色')
    return
  }
  try {
    feedbackStartLoading.value = true
    const res = await startAnonymousFeedbackTestApi(feedbackStartForm.value)
    if (res.code && res.data) {
      feedbackSession.value = res.data
      feedbackTestActive.value = true
      selectedFeedbackTaskCode.value = 'U1'
      feedbackTaskStats.value = {}
      feedbackTaskScores.value = {}
      feedbackTaskScoreForm.value = createDefaultFeedbackScores()
      feedbackGuideCollapsed.value = false
      persistFeedbackState()
      feedbackStartVisible.value = false
      await appendAssistantNotice(
        `已进入匿名反馈测试模式。\n匿名编号：${res.data.participantCode}\n角色：${res.data.participantGroup}\n测试期间的 AI 请求会写入 assistant_test_log，用于论文实验统计。`
        + '\n请按 U1--U8 任务指引逐项测试，发送前先选择当前任务；全部任务完成后可点击“结束匿名反馈测试”，也可以先继续测试未完成任务。'
      )
    } else {
      ElMessage.error(res.msg || '匿名反馈测试启动失败')
    }
  } catch (error) {
    ElMessage.error('匿名反馈测试启动失败')
  } finally {
    feedbackStartLoading.value = false
  }
}

const openFeedbackEnd = () => {
  if (!feedbackSession.value) {
    ElMessage.warning('当前没有进行中的匿名反馈测试')
    return
  }
  feedbackEndVisible.value = true
}

const finishFeedbackTest = async () => {
  if (!feedbackSession.value) return
  try {
    feedbackEndLoading.value = true
    const completedTaskCount = getCompletedFeedbackCount()
    const payload = {
      ...feedbackSession.value,
      ...feedbackEndForm.value
    }
    const res = await finishAnonymousFeedbackTestApi(payload)
    if (res.code) {
      const code = feedbackSession.value.participantCode
      feedbackSession.value = null
      feedbackTestActive.value = false
      feedbackTaskStats.value = {}
      feedbackTaskScores.value = {}
      feedbackTaskScoreForm.value = createDefaultFeedbackScores()
      selectedFeedbackTaskCode.value = 'U1'
      feedbackGuideCollapsed.value = false
      localStorage.removeItem(feedbackSessionKey)
      feedbackEndVisible.value = false
      await appendAssistantNotice(`匿名反馈测试已结束，${code} 的评分和反馈已写入 assistant_test_log。本次已记录 ${completedTaskCount}/8 个测试任务。`)
      feedbackEndForm.value = {
        understandingScore: 5,
        satisfactionScore: 5,
        safetyScore: 5,
        languageScore: 5,
        feedback: ''
      }
    } else {
      ElMessage.error(res.msg || '匿名反馈提交失败')
    }
  } catch (error) {
    ElMessage.error('匿名反馈提交失败')
  } finally {
    feedbackEndLoading.value = false
  }
}

const mergeSpeechText = (base, transcript) => {
  const left = base.trim()
  const right = transcript.trim()
  if (!left) return right
  if (!right) return left
  const needSpace = !/[，。！？、；：,.!?;:\s]$/.test(left)
  return `${left}${needSpace ? ' ' : ''}${right}`
}

const initSpeechRecognition = () => {
  const Recognition = window.SpeechRecognition || window.webkitSpeechRecognition
  voiceSupported.value = Boolean(Recognition)
  if (!Recognition) return

  speechRecognition = new Recognition()
  speechRecognition.lang = 'zh-CN'
  speechRecognition.continuous = false
  speechRecognition.interimResults = true
  speechRecognition.maxAlternatives = 1

  speechRecognition.onstart = () => {
    voiceBaseInput = chatInput.value
    voiceListening.value = true
  }

  speechRecognition.onresult = (event) => {
    let transcript = ''
    for (let i = 0; i < event.results.length; i += 1) {
      transcript += event.results[i][0].transcript
    }
    chatInput.value = mergeSpeechText(voiceBaseInput, transcript)
  }

  speechRecognition.onerror = (event) => {
    const messageMap = {
      'not-allowed': '浏览器未允许麦克风权限',
      'audio-capture': '未检测到可用麦克风',
      'no-speech': '没有识别到语音内容'
    }
    ElMessage.warning(messageMap[event.error] || '语音识别失败，请重试')
  }

  speechRecognition.onend = () => {
    voiceListening.value = false
  }
}

const toggleVoiceInput = () => {
  if (!voiceSupported.value || !speechRecognition) {
    ElMessage.warning('当前浏览器不支持语音识别，请使用 Chrome 或 Edge')
    return
  }
  if (chatLoading.value) return

  if (voiceListening.value) {
    speechRecognition.stop()
    return
  }

  try {
    speechRecognition.start()
    addFeedbackTaskStep(selectedFeedbackTaskCode.value)
  } catch (error) {
    ElMessage.warning('语音识别正在启动，请稍后再试')
  }
}

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
  if (feedbackTestActive.value) {
    feedbackInputStepRecorded.value = true
    addFeedbackTaskStep(selectedFeedbackTaskCode.value)
  }
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
  if (feedbackTestActive.value) {
    saveFeedbackTaskScores()
  }
  const feedbackPayload = getActiveFeedbackPayload()
  let feedbackRequestLogged = false

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
          feedbackRequestLogged = true
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
          feedbackRequestLogged = true
          current.content = data?.message || '流式请求失败，请稍后再试'
        } else if (event === 'done') {
          feedbackRequestLogged = true
          current.isStreaming = false
        }
        await scrollToBottom()
      }
    }, feedbackPayload)
  } catch (streamError) {
    try {
      const res = await chatAssistantApi(msg, feedbackPayload)
      feedbackRequestLogged = true
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
    if (feedbackPayload?.taskCode && feedbackRequestLogged) {
      markFeedbackTaskAttempt(feedbackPayload.taskCode, feedbackPayload.operationSteps)
    }
    feedbackInputStepRecorded.value = false
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
  restoreFeedbackSession()
  initSpeechRecognition()
  selectedBriefPeriod.value = resolveBriefPeriod()
  await loadAssistantBrief()
})

onBeforeUnmount(() => {
  if (!speechRecognition) return
  speechRecognition.onend = null
  speechRecognition.abort()
})
</script>

<template>
  <div class="assistant-panel" :class="{ home: mode === 'home' }">
    <div class="feedback-test-bar" :class="{ active: feedbackTestActive }">
      <div>
        <div class="feedback-title">匿名反馈测试</div>
        <div class="feedback-desc">
          <template v-if="feedbackTestActive && feedbackSession">
            当前匿名编号：{{ feedbackSession.participantCode }}，角色：{{ feedbackSession.participantGroup }}
          </template>
          <template v-else>
            提示：进入后，本次 AI 对话和结束评分会记入数据库，用于论文测试统计。
          </template>
        </div>
      </div>
      <div class="feedback-actions">
        <el-button v-if="!feedbackTestActive" type="primary" plain size="small" @click="openFeedbackStart">
          进入匿名反馈测试
        </el-button>
        <el-button v-else type="warning" plain size="small" @click="openFeedbackEnd">
          结束匿名反馈测试
        </el-button>
      </div>
    </div>

    <div v-if="feedbackTestActive" class="feedback-task-guide" :class="{ collapsed: feedbackGuideCollapsed }">
      <div class="task-guide-head">
        <div>
          <div class="task-guide-title">测试任务指引</div>
          <div class="task-guide-subtitle">
            <template v-if="feedbackGuideCollapsed">
              当前：{{ getCurrentFeedbackTask().taskCode }} {{ getCurrentFeedbackTask().taskName }}，已记录 {{ getCompletedFeedbackCount() }}/8 项。
            </template>
            <template v-else>
              发送前请选择当前任务，系统会把任务编号、任务名称、请求耗时和处理结果写入 assistant_test_log。
            </template>
          </div>
        </div>
        <div class="task-guide-actions">
          <el-tag type="warning" size="small">
            已记录 {{ getCompletedFeedbackCount() }}/8 项
          </el-tag>
          <el-popover placement="bottom" width="330" trigger="click">
            <template #reference>
              <el-button size="small" plain>当前任务评分/反馈</el-button>
            </template>
            <div class="task-score-editor">
              <div class="task-score-title">
                {{ getCurrentFeedbackTask().taskCode }} {{ getCurrentFeedbackTask().taskName }}
              </div>
              <div
                v-for="item in feedbackScoreItems"
                :key="item.field"
                class="task-score-row"
              >
                <span>{{ item.label }}</span>
                <el-rate
                  v-model="feedbackTaskScoreForm[item.field]"
                  :max="5"
                  show-score
                  @change="handleFeedbackScoreChange"
                />
              </div>
              <div class="task-feedback-row">
                <div class="task-feedback-label">本任务反馈</div>
                <el-input
                  v-model="feedbackTaskScoreForm.userFeedback"
                  type="textarea"
                  :rows="3"
                  maxlength="180"
                  show-word-limit
                  placeholder="可选：例如回答是否清楚、路径是否顺畅、哪里需要改进"
                  @input="handleFeedbackScoreChange"
                />
              </div>
              <div class="task-score-tip">
                评分和反馈文字会随下一条当前任务请求写入 assistant_test_log，对应本条测试记录。
              </div>
            </div>
          </el-popover>
          <el-button size="small" text @click="toggleFeedbackGuideCollapsed">
            {{ feedbackGuideCollapsed ? '展开任务指引' : '收起任务指引' }}
          </el-button>
        </div>
      </div>
      <div v-if="!feedbackGuideCollapsed" class="task-grid">
        <div
          v-for="task in feedbackTasks"
          :key="task.taskCode"
          class="task-card"
          :class="{
            active: selectedFeedbackTaskCode === task.taskCode,
            done: getFeedbackTaskCount(task.taskCode) > 0
          }"
          role="button"
          tabindex="0"
          @click="selectFeedbackTask(task.taskCode)"
          @keyup.enter="selectFeedbackTask(task.taskCode)"
        >
          <div class="task-card-main">
            <span class="task-code">{{ task.taskCode }}</span>
            <span class="task-name">{{ task.taskName }}</span>
            <span v-if="getFeedbackTaskCount(task.taskCode) > 0" class="task-count">
              已记录 {{ getFeedbackTaskCount(task.taskCode) }} 次
            </span>
          </div>
          <div class="task-guide-text">{{ task.guide }}</div>
          <div class="task-examples">
            <el-button
              v-for="example in task.examples"
              :key="example"
              size="small"
              text
              @click.stop="fillFeedbackExample(example, task.taskCode)"
            >
              {{ example }}
            </el-button>
          </div>
        </div>
      </div>
      <div v-if="!feedbackGuideCollapsed" class="task-guide-footer">
        当前任务：{{ getCurrentFeedbackTask().taskCode }} {{ getCurrentFeedbackTask().taskName }}。测试结束时可以直接提交评分，也可以关闭弹窗继续补测未完成任务。
      </div>
    </div>

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
        @input="markFeedbackInputEdited"
        @keyup.enter.exact.prevent="sendChat()"
      />
      <div class="chat-send">
        <el-button @click="fillDemoScript" :disabled="chatLoading">填入演示脚本</el-button>
        <el-button
          :type="voiceListening ? 'danger' : 'default'"
          :disabled="chatLoading"
          @click="toggleVoiceInput"
        >
          <el-icon><Microphone /></el-icon>
          {{ voiceListening ? '停止录音' : '语音输入' }}
        </el-button>
        <el-button type="primary" @click="sendChat()" :loading="chatLoading">发送</el-button>
      </div>
    </div>

    <el-dialog v-model="feedbackStartVisible" title="进入匿名反馈测试" width="420px">
      <el-form label-width="96px">
        <el-form-item label="你的角色">
          <el-select v-model="feedbackStartForm.participantGroup" placeholder="请选择角色" style="width: 100%;">
            <el-option
              v-for="item in participantGroupOptions"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="feedbackStartForm.roleNote"
            maxlength="80"
            show-word-limit
            placeholder="可选，例如：经常使用手机银行 / 首次体验"
          />
        </el-form-item>
      </el-form>
      <div class="feedback-dialog-tip">
        系统只生成匿名编号，不记录真实姓名、手机号或银行卡号。
      </div>
      <template #footer>
        <el-button @click="feedbackStartVisible = false">取消</el-button>
        <el-button type="primary" :loading="feedbackStartLoading" @click="startFeedbackTest">开始测试</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feedbackEndVisible" title="结束匿名反馈测试" width="460px">
      <el-form label-width="110px" class="feedback-score-form">
        <el-form-item label="理解感">
          <el-rate v-model="feedbackEndForm.understandingScore" :max="5" show-score />
        </el-form-item>
        <el-form-item label="满足感">
          <el-rate v-model="feedbackEndForm.satisfactionScore" :max="5" show-score />
        </el-form-item>
        <el-form-item label="安全感">
          <el-rate v-model="feedbackEndForm.safetyScore" :max="5" show-score />
        </el-form-item>
        <el-form-item label="语言表达">
          <el-rate v-model="feedbackEndForm.languageScore" :max="5" show-score />
        </el-form-item>
        <el-form-item label="反馈意见">
          <el-input
            v-model="feedbackEndForm.feedback"
            type="textarea"
            :rows="3"
            maxlength="300"
            show-word-limit
            placeholder="可选：记录你觉得清楚或需要改进的地方"
          />
        </el-form-item>
      </el-form>
      <div class="feedback-dialog-tip">
        当前已记录 {{ getCompletedFeedbackCount() }}/8 个测试任务；每条任务评分已随聊天记录写入，下面为总体评分与反馈。如未完成，可点击“继续测试”返回任务指引。
      </div>
      <template #footer>
        <el-button @click="feedbackEndVisible = false">继续测试</el-button>
        <el-button type="primary" :loading="feedbackEndLoading" @click="finishFeedbackTest">提交并结束</el-button>
      </template>
    </el-dialog>
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

.feedback-test-bar {
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  background: linear-gradient(135deg, #fff7e6 0%, #f7fbff 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.feedback-test-bar.active {
  border-color: #e6a23c;
  background: linear-gradient(135deg, #fff2d8 0%, #ecf5ff 100%);
}

.feedback-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.feedback-desc {
  margin-top: 3px;
  font-size: 12px;
  color: #606266;
}

.feedback-actions {
  flex-shrink: 0;
}

.feedback-task-guide {
  margin-bottom: 10px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid #ead7b7;
  background: #fffaf1;
}

.feedback-task-guide.collapsed {
  padding: 8px 12px;
}

.task-guide-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 10px;
}

.feedback-task-guide.collapsed .task-guide-head {
  margin-bottom: 0;
  align-items: center;
}

.task-guide-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.task-score-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-score-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.task-score-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: #606266;
}

.task-feedback-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.task-feedback-label {
  font-size: 12px;
  color: #606266;
}

.task-score-tip {
  margin-top: 4px;
  padding-top: 8px;
  border-top: 1px dashed #e4e7ed;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.task-guide-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.task-guide-subtitle {
  margin-top: 3px;
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.task-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.task-card {
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  background: #fff;
  cursor: pointer;
  transition: border-color .2s, box-shadow .2s, background .2s;
}

.task-card:hover,
.task-card.active {
  border-color: #e6a23c;
  box-shadow: 0 4px 12px rgba(230, 162, 60, .16);
}

.task-card.active {
  background: #fff7e6;
}

.task-card.done:not(.active) {
  border-color: #b3e19d;
  background: #f6ffed;
}

.task-card-main {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.task-code {
  padding: 1px 6px;
  border-radius: 999px;
  background: #303133;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.task-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.task-count {
  margin-left: auto;
  font-size: 11px;
  color: #67c23a;
}

.task-guide-text {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
}

.task-examples {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.task-guide-footer {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px dashed #ead7b7;
  font-size: 12px;
  color: #606266;
}

.feedback-dialog-tip {
  margin-top: 4px;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f5f7fa;
  color: #606266;
  font-size: 12px;
}

.feedback-score-form :deep(.el-form-item) {
  margin-bottom: 14px;
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
  gap: 8px;
  flex-wrap: wrap;
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

@media (max-width: 720px) {
  .feedback-test-bar,
  .task-guide-head {
    flex-direction: column;
  }

  .feedback-task-guide.collapsed .task-guide-head {
    align-items: flex-start;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }
}
</style>
