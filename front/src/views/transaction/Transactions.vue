<script setup>
import { ref, onMounted } from 'vue'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import { getTransactionsApi } from '@/api/transaction'

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
