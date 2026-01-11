import { createRouter, createWebHistory } from 'vue-router'

import IndexView from '@/views/index/index.vue'

import LayoutView from '@/views/layout/index.vue'
import LoginView from '@/views/login/index.vue'
import RegisterView from '@/views/register/index.vue'
import AccountView from '@/views/account/AccountOverview.vue'
import TransferView from '@/views/account/Transfer.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
     path: '/', 
     name: '',
     component: LayoutView,
     redirect: '/index', //重定向
     children: [
      {
        path: 'index', 
        name: 'index', 
        component: IndexView
      },
      {
        path: '/account',
        name: 'account',
        component: AccountView
      },
      {
        path: '/transfer',
        name: 'transfer',
        component: TransferView
      }
     ]
    },
    {path: '/login', name: 'login', component: LoginView},
    {
      path: '/register',
      name:'register',
      component: RegisterView
    }

    
  ]
})

router.beforeEach((to, from, next) => {
  const loginUser = JSON.parse(localStorage.getItem('loginUser'))

  if (to.path !== '/login' && to.path !== '/register' && !loginUser) {
    next('/login')
  } else {
    next()
  }
})
export default router
