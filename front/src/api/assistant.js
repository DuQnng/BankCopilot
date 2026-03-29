import request from '@/utils/request'

export function chatAssistantApi(message) {
  return request.post('/assistant/chat', { message })
}
