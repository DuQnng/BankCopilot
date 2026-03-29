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
      chatList.value.push({ role: 'assistant', content: res.data?.reply || '（机器人没有返回内容）' })
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
            <div class="bubble">{{ m.content }}</div>
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
  white-space: pre-line; /* 关键：让 \n 换行 */
  background: white;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
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
