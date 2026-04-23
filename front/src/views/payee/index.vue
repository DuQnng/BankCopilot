<script setup>
import { h, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createPayeeApi,
  deletePayeeApi,
  getPayeesApi,
  transferByPayeeApi,
  updatePayeeApi,
  validatePayeeTransferApi
} from '@/api/payee'

const loading = ref(false)
const submitLoading = ref(false)

const filters = ref({
  payeeName: '',
  accountNo: '',
  alias: ''
})

const tableData = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref()
const form = ref({
  id: null,
  accountNo: '',
  alias: ''
})

const rules = {
  accountNo: [
    { required: true, message: '请输入银行卡号', trigger: 'blur' },
    { pattern: /^\d{8,32}$/, message: '银行卡号格式不正确（8-32位数字）', trigger: 'blur' }
  ]
}

const transferDialogVisible = ref(false)
const transferSubmitting = ref(false)
const transferFormRef = ref()
const transferForm = ref({
  payeeId: null,
  payeeName: '',
  accountNo: '',
  amount: null,
  description: ''
})

const transferRules = {
  amount: [
    {
      validator: (_rule, value, callback) => {
        if (value === null || value === undefined || value === '') {
          callback(new Error('请输入转账金额'))
          return
        }
        if (Number(value) <= 0) {
          callback(new Error('转账金额必须大于0'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const loadData = async () => {
  try {
    loading.value = true
    const res = await getPayeesApi({
      page: page.value,
      size: size.value,
      payeeName: filters.value.payeeName,
      accountNo: filters.value.accountNo,
      alias: filters.value.alias
    })
    if (res.code === 1) {
      const pageResult = res.data || {}
      tableData.value = pageResult.list || []
      total.value = pageResult.total || 0
      page.value = pageResult.page || page.value
      size.value = pageResult.size || size.value
    } else {
      ElMessage.error(res.msg || '加载收款人列表失败')
    }
  } catch (error) {
    ElMessage.error('收款人列表接口异常')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handleClear = () => {
  filters.value = {
    payeeName: '',
    accountNo: '',
    alias: ''
  }
  page.value = 1
  loadData()
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadData()
}

const handleSizeChange = (newSize) => {
  size.value = newSize
  page.value = 1
  loadData()
}

const resetForm = () => {
  form.value = {
    id: null,
    accountNo: '',
    alias: ''
  }
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogMode.value = 'edit'
  form.value = {
    id: row.id,
    accountNo: row.accountNo || '',
    alias: row.alias || ''
  }
  dialogVisible.value = true
}

const openTransferDialog = (row) => {
  transferForm.value = {
    payeeId: row.id,
    payeeName: row.payeeName || '',
    accountNo: row.accountNo || '',
    amount: null,
    description: row.alias || ''
  }
  transferDialogVisible.value = true
}

const handleDialogClosed = () => {
  formRef.value?.clearValidate()
  resetForm()
}

const handleTransferDialogClosed = () => {
  transferFormRef.value?.clearValidate()
  transferForm.value = {
    payeeId: null,
    payeeName: '',
    accountNo: '',
    amount: null,
    description: ''
  }
}

const submitDialog = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (dialogMode.value === 'create') {
      const res = await createPayeeApi({
        accountNo: form.value.accountNo,
        alias: form.value.alias
      })
      if (res.code === 1) {
        ElMessage.success('新增收款人成功')
      } else {
        ElMessage.error(res.msg || '新增失败')
        return
      }
    } else {
      const res = await updatePayeeApi(form.value.id, {
        alias: form.value.alias
      })
      if (res.code === 1) {
        ElMessage.success('更新收款人成功')
      } else {
        ElMessage.error(res.msg || '更新失败')
        return
      }
    }
    dialogVisible.value = false
    page.value = 1
    await loadData()
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除收款人「${maskPayeeName(row.payeeName)}」吗？`,
      '删除确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const res = await deletePayeeApi(row.id)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      if (tableData.value.length === 1 && page.value > 1) {
        page.value -= 1
      }
      await loadData()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    // 用户取消不提示错误
  }
}

const submitTransferDialog = async () => {
  if (!transferFormRef.value || !transferForm.value.payeeId) return
  try {
    await transferFormRef.value.validate()
    transferSubmitting.value = true

    const validateRes = await validatePayeeTransferApi(transferForm.value.payeeId, {
      amount: transferForm.value.amount,
      description: transferForm.value.description
    })
    if (!validateRes.code) {
      ElMessage.error(validateRes.msg || '转账校验失败')
      return
    }

    const v = validateRes.data || {}
    try {
      await ElMessageBox.confirm(
        h(
          'div',
          { style: 'white-space: pre-line;' },
          `请确认转账信息：\n\n` +
            `付款账号：${v.fromAccountNoMasked || ''}\n` +
            `收款账号：${v.toAccountNoMasked || transferForm.value.accountNo}\n` +
            `转账金额：${v.amount || transferForm.value.amount}\n` +
            `当前余额：${v.balance || ''}\n` +
            (v.description ? `备注：${v.description}` : '')
        ),
        '转账确认',
        {
          confirmButtonText: '确认转账',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch (_cancelError) {
      ElMessage.info('已取消转账')
      return
    }

    const execRes = await transferByPayeeApi(transferForm.value.payeeId, {
      amount: transferForm.value.amount,
      description: transferForm.value.description
    })
    if (execRes.code) {
      ElMessage.success(execRes.msg || '转账成功')
      transferDialogVisible.value = false
    } else {
      ElMessage.error(execRes.msg || '转账失败')
    }
  } catch (error) {
    if (error?.message) {
      ElMessage.error(error.message)
    }
  } finally {
    transferSubmitting.value = false
  }
}

const maskPayeeName = (name) => {
  if (!name) return ''
  const chars = Array.from(name)
  if (chars.length <= 1) return `${chars[0]}**`
  return `${chars[0]}**`
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="payee-page">
    <el-card>
      <div class="toolbar">
        <el-input v-model="filters.payeeName" placeholder="收款人姓名" clearable />
        <el-input v-model="filters.accountNo" placeholder="银行卡号" clearable />
        <el-input v-model="filters.alias" placeholder="备注/昵称" clearable />
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleClear">清空</el-button>
        <el-button type="success" @click="openCreateDialog">新增收款人</el-button>
      </div>

      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column label="收款人姓名" min-width="140">
          <template #default="{ row }">
            <span>{{ maskPayeeName(row.payeeName) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="accountNo" label="银行卡号" min-width="200" />
        <el-table-column prop="alias" label="备注/昵称" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="success" text @click="openTransferDialog(row)">转账</el-button>
            <el-button type="primary" text @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" text @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="page">
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :total="total"
          :current-page="page"
          :page-size="size"
          :page-sizes="[5, 10, 20, 50]"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增收款人' : '编辑收款人'"
      width="500px"
      @closed="handleDialogClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="银行卡号" prop="accountNo">
          <el-input
            v-model="form.accountNo"
            placeholder="请输入银行卡号"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="备注/昵称" prop="alias">
          <el-input v-model="form.alias" placeholder="可选，便于识别该收款人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitDialog">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="transferDialogVisible"
      title="向收款人转账"
      width="500px"
      @closed="handleTransferDialogClosed"
    >
      <el-form ref="transferFormRef" :model="transferForm" :rules="transferRules" label-width="100px">
        <el-form-item label="收款人">
          <el-input :model-value="maskPayeeName(transferForm.payeeName)" disabled />
        </el-form-item>
        <el-form-item label="银行卡号">
          <el-input v-model="transferForm.accountNo" disabled />
        </el-form-item>
        <el-form-item label="转账金额" prop="amount">
          <el-input v-model.number="transferForm.amount" type="number" placeholder="请输入转账金额" />
        </el-form-item>
        <el-form-item label="备注/昵称">
          <el-input v-model="transferForm.description" placeholder="可选，默认使用收款人备注昵称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="transferSubmitting" @click="submitTransferDialog">确认转账</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
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
