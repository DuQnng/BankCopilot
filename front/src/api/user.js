import request from '@/utils/request'

// 修改密码
export const changePasswordApi = (data) => {
  return request.post('/user/change_password', data)
}
