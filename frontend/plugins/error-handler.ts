import { defineNuxtPlugin } from '#app'

export default defineNuxtPlugin((nuxtApp) => {
  // Nuxt 개발 서버 로그 직렬화(devalue) 시 Error 객체의 함수/순환 참조로 인한 DevalueError 방지
  nuxtApp.hook('app:error', (err) => {
    if (err && typeof err === 'object') {
      try {
        Object.defineProperty(err, 'toJSON', {
          value: () => ({
            name: (err as any).name || 'Error',
            message: (err as any).message || String(err),
            stack: (err as any).stack || ''
          }),
          configurable: true,
          writable: true
        })
      } catch (e) {}
    }
  })
})
