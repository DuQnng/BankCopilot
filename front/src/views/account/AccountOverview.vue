<script setup>
    import { ref, onMounted } from 'vue'
    import { ElMessage } from 'element-plus'
    import { useRouter } from 'vue-router'
    import axios from 'axios'
    
    // 路由实例
    const router = useRouter()
    
    // 当前登录用户
    const loginUser = ref(null)
    const token = ref('')
    
    // 账户信息
    const accountInfo = ref({})
    const balance = ref(0)
    const transactions = ref([])
    
    // 页面挂载时获取登录用户信息
    onMounted(() => {
      const user = JSON.parse(localStorage.getItem('loginUser'))
      if (user && user.id) {
        loginUser.value = user
        token.value = user.token || '' // 假如你的用户对象里有 token
        fetchAccountInfo()
        fetchTransactions()
      } else {
        ElMessage.warning('请先登录')
        router.push('/login')
      }
    })
    
    // 获取账户信息
    const fetchAccountInfo = async () => {
      try {
        const result = await axios.get(`/api/account/info`, {
          headers: { token: token.value },
          params: { userId: loginUser.value.id } // 如果接口需要传用户id
        })
    
        if (result.data.code) {
          accountInfo.value = result.data.data
          balance.value = result.data.data.balance
        } else {
          ElMessage.error(result.data.msg || '获取账户信息失败')
        }
      } catch (error) {
        ElMessage.error('获取账户信息接口异常')
        console.error(error)
      }
    }
    
    // 获取交易记录
    const fetchTransactions = async () => {
      try {
        const result = await axios.get(`/api/account/transactions`, {
          headers: { token: token.value },
          params: { userId: loginUser.value.id } // 如果接口需要传用户id
        })
    
        if (result.data.code) {
          transactions.value = result.data.data
        } else {
          ElMessage.error(result.data.msg || '获取交易记录失败')
        }
      } catch (error) {
        ElMessage.error('获取交易记录接口异常')
        console.error(error)
      }
    }
    </script>
    
    <template>
      <div class="account-overview">
        <el-card class="card">
          <h3>账户信息</h3>
          <p>户名：{{ accountInfo.accountName }}</p>
          <p>账号：{{ accountInfo.accountNo }}</p>
          <p>账户类型：{{ accountInfo.accountType }}</p>
          <p>状态：{{ accountInfo.status }}</p>
        </el-card>
    
        <el-card class="card balance-card">
          <h3>账户余额</h3>
          <p class="balance">¥ {{ balance.toFixed(2) }}</p>
        </el-card>
    
        <el-card class="card">
          <h3>最近交易记录</h3>
          <el-table :data="transactions" stripe>
            <el-table-column prop="tradeTime" label="时间" />
            <el-table-column prop="counterpartyAccountNo" label="交易对方账号" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="amount" label="金额" />
            <el-table-column prop="description" label="描述" />
          </el-table>
        </el-card>
      </div>
    </template>
    
    <style scoped>
    .account-overview {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    
    .card {
      padding: 10px;
    }
    
    .balance-card {
      text-align: center;
    }
    
    .balance {
      font-size: 28px;
      font-weight: bold;
      color: #409EFF;
    }
    </style>
    