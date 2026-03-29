    <script setup>
        import { ref } from 'vue'
        import { ElMessage } from 'element-plus'
        import { useRouter } from 'vue-router' // 添加这行导入
        import request from '@/utils/request'

        const router = useRouter() // 创建路由实例
        
        const registerForm = ref({
        username: '',
        password: '',
        name: '',
        phone: '',
        code: ''
        })


        // 验证码相关状态
        const codeDisabled = ref(false)
        const codeText = ref('发送验证码')
        let timer = null

        const checkPhone = (phone) => {
            return /^1[3-9]\d{9}$/.test(phone)
        }

        //发送验证码
        const sendCode = async () => {
            const phone = registerForm.value.phone

            // 校验手机号
            if (!phone) {
                ElMessage.warning('请输入手机号')
                return
            }
            if (!/^1[3-9]\d{9}$/.test(phone)) {
                ElMessage.warning('手机号格式不正确')
                return
            }

            try {
                const res = await request.post('/register/code', { phone })
                if (res.code === 1) {
                ElMessage.success(res.msg)

                // 倒计时逻辑
                codeDisabled.value = true
                let seconds = 60
                codeText.value = `${seconds}s`
                timer = setInterval(() => {
                    seconds--
                    codeText.value = `${seconds}s`
                    if (seconds <= 0) {
                    clearInterval(timer)
                    codeDisabled.value = false
                    codeText.value = '发送验证码'
                    }
                }, 1000)
                } else {
                ElMessage.error(res.msg)
                }
            } catch (error) {
                ElMessage.error('网络异常，请稍后重试')
            }
        }


        const submitRegister = async () => {
            // 简单校验
            const { username, password, name, phone, code } = registerForm.value
            if(!username || !password ||  !name || !phone || !code){
                ElMessage.warning('请填写完整信息')
                return
            }
       
            try{
                const res = await request.post('/register/do', registerForm.value)
                if(res.code === 1){
                ElMessage.success(res.msg)
                router.push('/login')
                } else {
                ElMessage.error(res.msg)
                }
            } catch(e){
                ElMessage.error('网络异常，请稍后重试')
            }
        }

        const goLogin = () => {
            router.push('/login')
        }    

        const testPrint = () => {
            console.log(registerForm.value)
            ElMessage.success('表单数据已打印到控制台')
        }        

    </script>
    
    
    <template>
      <div class="register-container">
        <div class="register-form">
          <div class="title">用户注册</div>
    
        <el-form label-width="90px">
            <el-form-item label="用户名">
                <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>

            <el-form-item label="姓名">
                <el-input v-model="registerForm.name" placeholder="请输入姓名"></el-input>
            </el-form-item>

            <el-form-item label="手机号">
                <el-input v-model="registerForm.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>

            <el-form-item label="验证码">
                <el-row :gutter="10">
                    <el-col :span="14">
                    <el-input
                        v-model="registerForm.code"
                        placeholder="请输入验证码">
                    </el-input>
                    </el-col>
                    <el-col :span="10">
                    <el-button
                        type="primary"
                        :disabled="codeDisabled"
                        @click="sendCode"
                        style="width: 100%">
                        {{ codeText }}
                    </el-button>
                    </el-col>
                </el-row>
            </el-form-item>

            <el-form-item label="密码">
                <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码">
                </el-input>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitRegister">注册</el-button>
                <el-button type="info" @click="goLogin">返回登录</el-button>
            </el-form-item>
        </el-form>
        </div>
      </div>
    </template>
    
    <style scoped>
    .register-container {
    width: 100vw;
    min-height: 100vh;
    background-image: url('../../assets/bg1.jpg');
    background-repeat: no-repeat;
    background-position: center;
    background-size: cover;
    background-color: #f5f7fa;
    padding: 10%;
    box-sizing: border-box;
    }

    .register-form {
    max-width: 450px;
    margin: 0 auto;
    padding: 30px;
    background-color: #ffffff;
    border-radius: 8px;
    }

    .title {
    text-align: center;
    font-size: 26px;
    font-weight: bold;
    margin-bottom: 30px;
    }

    /* 修复输入框长度问题 */
    .el-form-item {
    margin-bottom: 20px;
    }

    /* 限制输入框最大宽度 */
    .el-input {
    width: 85%;
    max-width: 360px; /* 450px - 90px = 360px */
    }

    /* 修复按钮居中问题 */
    .button-group {
    display: flex;
    justify-content: center;
    gap: 15px;
    }
    </style>
    