import { defineNuxtPlugin } from '#app'
import { h } from 'vue'

export default defineNuxtPlugin((nuxtApp) => {
  // SSR 환경에서 v-chart 컴포넌트 해소 실패 Vue warn 방지용 더미 컴포넌트
  nuxtApp.vueApp.component('v-chart', {
    render: () => h('div', { class: 'v-chart-placeholder' })
  })
})
