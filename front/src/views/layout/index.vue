<script setup>
import { ref, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { changePasswordApi } from '@/api/user'
import AssistantPanel from '@/components/assistant/AssistantPanel.vue'

const loginName = ref('')
const router = useRouter()
const pwdDialogVisible = ref(false)
const assistantVisible = ref(false)

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  const loginUser = JSON.parse(localStorage.getItem('loginUser'))
  if (loginUser && loginUser.name) {
    loginName.value = loginUser.name
  }
})

const logout = () => {
  ElMessageBox.confirm('您确认退出登录吗?', '提示',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    ElMessage.success('退出成功')
    localStorage.removeItem('loginUser')
    router.push('/login')
  }).catch(() => {
    ElMessage.info('您已取消退出')
  })
}

const submitPwd = async () => {
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
</script>

<template>
  <div class="common-layout">
    <el-container>
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

      <el-dialog
        title="修改密码"
        v-model="pwdDialogVisible"
        width="420px"
      >
        <el-form :model="pwdForm" label-width="90px">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="pwdDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPwd">确认修改</el-button>
        </template>
      </el-dialog>

      <el-drawer
        v-model="assistantVisible"
        title="智能客服"
        size="420px"
      >
        <AssistantPanel mode="drawer" default-brief-period="auto" />
      </el-drawer>

      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu router>
            <el-menu-item index="/index">
              <el-icon><Promotion /></el-icon> 首页
            </el-menu-item>
            <el-menu-item index="/account">
              <el-icon><Wallet /></el-icon> 账户总览
            </el-menu-item>
            <el-menu-item index="/transfer">
              <el-icon><Switch /></el-icon> 转账
            </el-menu-item>
            <el-menu-item index="/transactions">
              <el-icon><Document /></el-icon> 交易流水
            </el-menu-item>
            <el-menu-item index="/payees">
              <el-icon><User /></el-icon> 收款人管理
            </el-menu-item>
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
</style>
