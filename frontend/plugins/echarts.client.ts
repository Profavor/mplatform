import { defineNuxtPlugin } from '#app'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart, GraphChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, GridComponent, LegendComponent } from 'echarts/components'

export default defineNuxtPlugin((nuxtApp) => {
  use([CanvasRenderer, LineChart, BarChart, PieChart, GraphChart, TitleComponent, TooltipComponent, GridComponent, LegendComponent])
  nuxtApp.vueApp.component('v-chart', VChart)
})
