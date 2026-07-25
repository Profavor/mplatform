import { hasPermission } from '~/composables/usePermission'

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.directive('permission', {
    mounted(el, binding) {
      const requiredPerm = binding.value
      if (!requiredPerm) return

      const userCookie = useCookie<any>('user').value || {}
      const permissions = userCookie.permissions || []
      const role = userCookie.role || ''

      const allowed = hasPermission(requiredPerm, permissions, role)
      if (!allowed) {
        el.parentNode?.removeChild(el)
      }
    },
  })
})
