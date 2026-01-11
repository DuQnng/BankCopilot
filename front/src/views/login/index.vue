<script setup>
import { ref } from 'vue'
import { loginApi} from '@/api/login'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'


const loginForm = ref({username:'', password:''})
const router = useRouter();

// 登录
const login = async () => {
  const result = await loginApi(loginForm.value);
  if(result.code){ //成功
    //1. 提示信息
    ElMessage.success('登录成功');
    //2. 存储当前登录员工信息
    localStorage.setItem('loginUser',JSON.stringify(result.data));
    //3. 跳转页面 - 首页
    router.push('/index');
  }else { //失败
    ElMessage.error(result.msg);
  }
}


const goRegister = () => {
  router.push('/register')
}
</script>

<template>
  <div id="container">
    <div class="login-form">
      <el-form label-width="80px">
        <p class="title">银行智能机器人</p>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>

        <el-form-item>
          <el-button class="button" type="primary" @click="login">登 录</el-button>
          <el-button class="button" type="info" @click="goRegister">注 册</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
#container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-image: url('../../assets/bg1.jpg');
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
  z-index: -1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-form {
  max-width: 410px;
  width: 90%; /* 改为 90% 更合理，110% 会超出 */
  padding: 30px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  background-color: white;
  box-sizing: border-box;
  /* 不要在这里写嵌套样式！scoped 下 .title 和 .button 必须在外层定义 */
}

.title {
  font-size: 30px;
  font-family: '微软雅黑', sans-serif;
  text-align: center;
  margin-bottom: 35px;
  font-weight: bold;
}

/* 关键：强制所有表单项的内容区域居中 */
:deep(.el-form-item__content) {
  text-align: center !important;
}

/* 按钮组居中 */
.button-group {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.button {
  width: 110px;
}
</style>