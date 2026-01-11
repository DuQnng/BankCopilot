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
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- Header 区域 -->
      <el-header class="header">
        <span class="title">银行智能机器人</span>
        <span class="right_tool">
            <a href="javascript:;" @click="pwdDialogVisible = true">
              <el-icon><EditPen /></el-icon> 修改密码 &nbsp;&nbsp;&nbsp; | &nbsp;&nbsp;&nbsp;
            </a>
          <a href="javascript:;" @click="logout">
            <el-icon><SwitchButton /></el-icon> 退出登录 【{{loginName}}】
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
</style>
