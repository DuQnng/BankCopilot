import request from '@/utils/request'

export function getPayeesApi(params) {
  return request({
    url: '/payees',
    method: 'get',
    params
  })
}

export function createPayeeApi(data) {
  return request({
    url: '/payees',
    method: 'post',
    data
  })
}

export function updatePayeeApi(id, data) {
  return request({
    url: `/payees/${id}`,
    method: 'put',
    data
  })
}

export function deletePayeeApi(id) {
  return request({
    url: `/payees/${id}`,
    method: 'delete'
  })
}

export function validatePayeeTransferApi(id, data) {
  return request({
    url: `/payees/${id}/transfer/validate`,
    method: 'post',
    data
  })
}

export function transferByPayeeApi(id, data) {
  return request({
    url: `/payees/${id}/transfer`,
    method: 'post',
    data
  })
}
