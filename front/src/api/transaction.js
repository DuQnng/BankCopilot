import request from '@/utils/request'

export function getTransactionsApi(params) {
  return request({
    url: '/transactions',
    method: 'get',
    params
  })
}

// 导出Excel
export function exportTransactionsApi(params) {
  return request({
    url: '/transactions/export',
    method: 'get',
    params,
    responseType: 'blob' // 必须指定 blob 格式
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
