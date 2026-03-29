import request from '@/utils/request'

export function getTransactionsApi(params) {
  return request({
    url: '/transactions',
    method: 'get',
    params
  })
  
}


// 只校验，不扣钱
export function transferValidateApi(data) {
  return request({
    url: '/transfer/validate',
    method: 'post',
    data
  })
}

// 真正执行转账
export function transferApi(data) {
  return request({
    url: '/transfer',
    method: 'post',
    data
  })
}
