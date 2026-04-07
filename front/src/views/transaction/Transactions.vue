<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getTransactionsApi, exportTransactionsApi } from '@/api/transaction'

// 查询条件
const filters = ref({
  accountNo: '',
  type: '',
  startTime: '',
  endTime: ''
})

// 数据 & 分页
const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(5)

// 加载数据
const loadData = async () => {
  try {
    const res = await getTransactionsApi({
      ...filters.value,
      page: page.value,
      size: size.value
    })
    // 需要注意如果在 request.js 配置了统一处理 res.data，可以按需调整判断
    if (res.code === 1) {
      tableData.value = res.data.list
      total.value = res.data.total
    } else {
      ElMessage.error(res.msg || '查询失败')
    }
  } catch (e) {
    ElMessage.error('接口调用异常')
  }
}

// 分页事件
const handlePageChange = (newPage) => {
  page.value = newPage
  loadData()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  page.value = 1
  loadData()
}

// 筛选按钮
const handleSearch = () => {
  page.value = 1
  loadData()
}

// 清空筛选
const handleClear = () => {
  filters.value = {
    accountNo: '',
    type: '',
    startTime: '',
    endTime: ''
  }
  page.value = 1
  loadData()
}

// 导出 Excel
const handleExport = async () => {
  try {
    // 构造请求参数，不需要分页信息
    const params = {
      ...filters.value
    }
    // 如果日期格式需要转换（例如 Date 对象转为 'YYYY-MM-DD' 字符串），请在此处格式化
    if (params.startTime) {
      params.startTime = dayjs(params.startTime).format('YYYY-MM-DD')
    }
    if (params.endTime) {
      params.endTime = dayjs(params.endTime).format('YYYY-MM-DD')
    }

    const res = await exportTransactionsApi(params)
    
    // 创建 blob 对象，并使用 a 标签下载
    // 注意这里 res 可能是原生的 blob (如果 axios 配置了不对 blob 剥离外层)，或者直接是 blob 数据
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', '交易流水.xlsx')
    document.body.appendChild(link)
    link.click()
    
    // 释放资源
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败', error)
    ElMessage.error('导出失败，请重试')
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="transaction-page">
    <el-card>
      <div class="filters">
        <el-input
          v-model="filters.accountNo"
          placeholder="账户卡号"
          clearable
        />

        <el-select v-model="filters.type" placeholder="交易类型" clearable>
          <el-option label="收入" value="收入" />
          <el-option label="支出" value="支出" />
        </el-select>

        <el-date-picker
          v-model="filters.startTime"
          type="date"
          placeholder="起始时间"
          clearable
        />
        
        <el-date-picker
          v-model="filters.endTime"
          type="date"
          placeholder="结束时间"
          clearable
        />

        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleClear">清空</el-button>
        <el-button type="success" @click="handleExport">导出 Excel</el-button>
      </div>

      <el-table :data="tableData" stripe style="width: 100%">
        <el-table-column prop="tradeTime" label="交易时间" />
        <el-table-column prop="accountNo" label="账户卡号" />
        <el-table-column prop="counterpartyAccountNo" label="对方账号" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="description" label="描述" />
      </el-table>

      <div class="page">
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :total="total"
          :current-page="page"
          :page-size="size"
          :page-sizes="[5,10,20,50]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.filters {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
.page {
  margin-top: 16px;
  text-align: right;
}
</style>
