import request from '@/utils/request'

export function chatAssistantApi(message, testSession = null) {
  return request.post('/assistant/chat', { message, ...(testSession || {}) })
}

export function fetchAssistantBriefApi(period = 'week') {
  return request.get('/assistant/brief', { params: { period } })
}

function parseSseData(text) {
  try {
    return JSON.parse(text)
  } catch (e) {
    return text
  }
}

export function startAnonymousFeedbackTestApi(data) {
  return request.post('/assistant/test/start', data)
}

export function finishAnonymousFeedbackTestApi(data) {
  return request.post('/assistant/test/end', data)
}

export async function streamAssistantApi(message, handlers = {}, testSession = null) {
  const loginUser = JSON.parse(localStorage.getItem('loginUser') || '{}')
  const token = loginUser?.token || ''
  const res = await fetch('/api/assistant/chat/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { token } : {})
    },
    body: JSON.stringify({ message, ...(testSession || {}) })
  })

  if (!res.ok || !res.body) {
    throw new Error('stream request failed')
  }

  const reader = res.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  let shouldStop = false

  while (!shouldStop) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })

    const chunks = buffer.split('\n\n')
    buffer = chunks.pop() || ''
    for (const chunk of chunks) {
      const lines = chunk.split('\n')
      let event = 'message'
      const dataLines = []
      lines.forEach((line) => {
        if (line.startsWith('event:')) {
          event = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5).trim())
        }
      })
      if (dataLines.length) {
        const data = parseSseData(dataLines.join('\n'))
        handlers.onEvent?.(event, data)
        if (event === 'done') {
          shouldStop = true
          break
        }
      }
    }
  }
}
