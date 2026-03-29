<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { h } from 'vue'

const form = ref({
  toAccountNo: '',
  amount: null,
  description: ''
})

const submitTransfer = async () => {
  // 0) 前端基础校验
  if (!form.value.toAccountNo || !form.value.amount || form.value.amount <= 0) {
    ElMessage.warning('请填写完整信息且金额大于0')
    return
  }

  try {
    // 1) 先校验（不扣钱）
    const validateRes = await request.post('/account/transfer/validate', {
      toAccountNo: form.value.toAccountNo,
      amount: form.value.amount,
      description: form.value.description
    })

    if (!validateRes.code) {
      ElMessage.error(validateRes.msg || '转账校验失败')
      return
    }

    const v = validateRes.data

    // 2) 弹确认框（用户点取消会走 catch，所以这里用 await + try/catch 包一下）
    try {
        await ElMessageBox.confirm(
          h('div', { style: 'white-space: pre-line;' },
            `请确认转账信息：\n\n` +
            `付款账号：${v.fromAccountNoMasked}\n` +
            `收款账号：${v.toAccountNoMasked}\n` +
            `转账金额：${v.amount}\n` +
            `当前余额：${v.balance}\n` +
            (v.description ? `备注：${v.description}` : '')
          ),
          '转账确认',
          {
            confirmButtonText: '确认转账',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
    } catch (e) {
      // 用户取消，不算错误
      ElMessage.info('已取消转账')
      return
    }

    // 3) 真正执行转账（扣钱+写流水）
    const execRes = await request.post('/account/transfer', {
      toAccountNo: form.value.toAccountNo,
      amount: form.value.amount,
      description: form.value.description
    })

    if (execRes.code) {
      ElMessage.success(execRes.msg || '转账成功')
      form.value.toAccountNo = ''
      form.value.amount = null
      form.value.description = ''
    } else {
      ElMessage.error(execRes.msg || '转账失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('转账接口异常')
  }
}
</script>

<template>
  <el-card class="card">
    <h3>转账</h3>
    <el-form :model="form" label-width="100px">
      <el-form-item label="收款账号">
        <el-input v-model="form.toAccountNo" placeholder="请输入收款账号" />
      </el-form-item>

      <el-form-item label="转账金额">
        <el-input
          v-model.number="form.amount"
          type="number"
          placeholder="请输入转账金额"
        />
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="form.description" placeholder="可填写备注信息" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitTransfer">确认转账</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.card {
  max-width: 500px;
  margin: 20px auto;
  padding: 20px;
}

.transfer-confirm-box .el-message-box__message p {
  white-space: pre-line;
}
</style>