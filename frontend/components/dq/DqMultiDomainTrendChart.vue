<template>
  <va-card class="dq-multi-trend-card">
    <!-- Header -->
    <div class="chart-header">
      <div class="title-group">
        <va-icon name="insights" color="primary" />
        <div>
          <h3 class="chart-title">{{ t('benchmark.trend_chart_title') }}</h3>
          <p class="chart-desc">{{ t('benchmark.trend_chart_desc') }}</p>
        </div>
      </div>

      <div class="controls-group">
        <!-- Period Selector -->
        <div class="period-toggle-group">
          <button
            v-for="opt in [
              { label: t('benchmark.trend_days_7'), value: 7 },
              { label: t('benchmark.trend_days_30'), value: 30 },
              { label: t('benchmark.trend_days_90'), value: 90 }
            ]"
            :key="opt.value"
            type="button"
            class="period-btn"
            :class="{ active: days === opt.value }"
            @click="emit('change-days', opt.value)"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>
    </div>

    <va-card-content class="chart-content">
      <!-- Domain Legend Pills -->
      <div v-if="multiDomainTrends && multiDomainTrends.length > 0" class="legend-bar">
        <div
          v-for="(domain, idx) in multiDomainTrends"
          :key="domain.domainId"
          class="legend-item"
          :class="{ inactive: hiddenDomainIds.has(domain.domainId) }"
          @click="toggleDomain(domain.domainId)"
        >
          <span class="legend-color-dot" :style="{ backgroundColor: getDomainColor(idx) }"></span>
          <span class="legend-name">{{ getLocalizedName(domain.domainName, domain.domainCode) }}</span>
          <span class="legend-points">({{ domain.dataPoints?.length ?? 0 }})</span>
        </div>
      </div>

      <!-- Loading / Empty State -->
      <div v-if="loading" class="chart-loading">
        <va-progress-circle indeterminate size="2.5rem" color="primary" />
      </div>
      <div v-else-if="!hasAnyDataPoints" class="empty-chart">
        <va-icon name="show_chart" size="2.5rem" color="secondary" />
        <p class="empty-chart-text">{{ t('benchmark.no_benchmark_data') }}</p>
      </div>

      <!-- SVG Multi-Line Chart -->
      <div v-else class="svg-container" ref="containerRef">
        <svg
          class="trend-svg"
          :viewBox="`0 0 ${chartWidth} ${chartHeight}`"
          preserveAspectRatio="none"
        >
          <!-- Grid Lines (Y-Axis) -->
          <g class="grid-lines">
            <line
              v-for="yVal in [100, 80, 60, 40, 20, 0]"
              :key="yVal"
              :x1="paddingLeft"
              :y1="getYPos(yVal)"
              :x2="chartWidth - paddingRight"
              :y2="getYPos(yVal)"
              stroke="var(--va-background-border)"
              stroke-dasharray="3,3"
              stroke-width="1"
            />
            <!-- Y-Axis Labels -->
            <text
              v-for="yVal in [100, 80, 60, 40, 20, 0]"
              :key="`lbl-${yVal}`"
              :x="paddingLeft - 8"
              :y="getYPos(yVal) + 4"
              fill="var(--va-text-secondary)"
              font-size="10"
              font-family="monospace"
              text-anchor="end"
            >
              {{ yVal }}%
            </text>
          </g>

          <!-- Domain Lines and Area -->
          <g
            v-for="(domain, idx) in visibleDomains"
            :key="domain.domainId"
            class="domain-series"
          >
            <!-- Line Path -->
            <path
              v-if="domain.dataPoints && domain.dataPoints.length > 1"
              :d="buildLinePath(domain.dataPoints)"
              fill="none"
              :stroke="getDomainColor(idx)"
              stroke-width="2.5"
              stroke-linejoin="round"
              stroke-linecap="round"
            />

            <!-- Points -->
            <circle
              v-for="(pt, pIdx) in domain.dataPoints"
              :key="pIdx"
              :cx="getXPos(pIdx, domain.dataPoints.length)"
              :cy="getYPos(pt.score)"
              r="4.5"
              :fill="getDomainColor(idx)"
              stroke="#ffffff"
              stroke-width="1.5"
              class="chart-point"
              @mouseenter="showPointTooltip($event, domain, pt)"
              @mouseleave="hidePointTooltip"
            />
          </g>
        </svg>

        <!-- Floating Tooltip -->
        <div
          v-if="activeTooltip"
          class="chart-tooltip"
          :style="{
            left: `${activeTooltip.x}px`,
            top: `${activeTooltip.y}px`
          }"
        >
          <div class="tooltip-header">
            <span class="tooltip-dot" :style="{ backgroundColor: activeTooltip.color }"></span>
            <span class="tooltip-title">{{ activeTooltip.domainName }}</span>
          </div>
          <div class="tooltip-score">
            {{ activeTooltip.score }}%
          </div>
          <div class="tooltip-sub">
            <span>{{ activeTooltip.date }}</span>
            <span v-if="activeTooltip.violations !== undefined">
              {{ t('benchmark.col_violations') }}: {{ activeTooltip.violations }}
            </span>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'

interface TrendPoint {
  recordedAt: string
  score: number
  totalViolations: number
}

interface MultiDomainTrend {
  domainId: string
  domainCode: string
  domainName: Record<string, string>
  dataPoints: TrendPoint[]
}

const props = withDefaults(
  defineProps<{
    multiDomainTrends: MultiDomainTrend[] | null
    days?: number
    loading?: boolean
  }>(),
  {
    days: 30,
    loading: false
  }
)

const emit = defineEmits<{
  (e: 'change-days', days: number): void
  (e: 'select-domain', domainId: string): void
}>()

const { t, locale } = useI18n()

const chartWidth = 800
const chartHeight = 260
const paddingLeft = 45
const paddingRight = 20
const paddingTop = 20
const paddingBottom = 30

const hiddenDomainIds = ref<Set<string>>(new Set())

const domainColors = [
  '#3b82f6', // Blue
  '#10b981', // Emerald
  '#f59e0b', // Amber
  '#8b5cf6', // Violet
  '#ec4899', // Pink
  '#06b6d4', // Cyan
  '#f97316'  // Orange
]

function getDomainColor(index: number): string {
  return domainColors[index % domainColors.length]
}

function getLocalizedName(domainName?: Record<string, string>, fallback = ''): string {
  if (!domainName) return fallback
  return domainName[locale.value] || domainName['ko'] || domainName['en'] || fallback
}

const hasAnyDataPoints = computed(() => {
  if (!props.multiDomainTrends) return false
  return props.multiDomainTrends.some(d => d.dataPoints && d.dataPoints.length > 0)
})

const visibleDomains = computed(() => {
  if (!props.multiDomainTrends) return []
  return props.multiDomainTrends.filter(d => !hiddenDomainIds.value.has(d.domainId))
})

function toggleDomain(domainId: string) {
  if (hiddenDomainIds.value.has(domainId)) {
    hiddenDomainIds.value.delete(domainId)
  } else {
    // Keep at least one domain visible
    if (visibleDomains.value.length > 1) {
      hiddenDomainIds.value.add(domainId)
    }
  }
}

function getYPos(score: number): number {
  const boundedScore = Math.max(0, Math.min(100, score))
  const availableHeight = chartHeight - paddingTop - paddingBottom
  return paddingTop + availableHeight * (1 - boundedScore / 100)
}

function getXPos(index: number, totalPoints: number): number {
  const availableWidth = chartWidth - paddingLeft - paddingRight
  if (totalPoints <= 1) return paddingLeft + availableWidth / 2
  return paddingLeft + (index / (totalPoints - 1)) * availableWidth
}

function buildLinePath(points: TrendPoint[]): string {
  if (!points || points.length === 0) return ''
  return points
    .map((pt, idx) => {
      const x = getXPos(idx, points.length)
      const y = getYPos(pt.score)
      return `${idx === 0 ? 'M' : 'L'} ${x} ${y}`
    })
    .join(' ')
}

// Tooltip Handling
interface TooltipData {
  x: number
  y: number
  domainName: string
  score: number
  violations: number
  date: string
  color: string
}

const activeTooltip = ref<TooltipData | null>(null)
const containerRef = ref<HTMLElement | null>(null)

function showPointTooltip(evt: MouseEvent, domain: MultiDomainTrend, pt: TrendPoint) {
  if (!containerRef.value) return
  const rect = containerRef.value.getBoundingClientRect()
  const x = evt.clientX - rect.left
  const y = evt.clientY - rect.top - 70

  const idx = (props.multiDomainTrends || []).findIndex(d => d.domainId === domain.domainId)
  const color = getDomainColor(idx >= 0 ? idx : 0)

  let formattedDate = pt.recordedAt || ''
  try {
    const d = new Date(pt.recordedAt)
    formattedDate = `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch (_) {}

  activeTooltip.value = {
    x: Math.max(10, Math.min(x, rect.width - 160)),
    y: Math.max(10, y),
    domainName: getLocalizedName(domain.domainName, domain.domainCode),
    score: pt.score,
    violations: pt.totalViolations,
    date: formattedDate,
    color
  }
}

function hidePointTooltip() {
  activeTooltip.value = null
}
</script>

<style scoped>
.dq-multi-trend-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border);
  background: var(--va-background-primary);
  overflow: hidden;
}

.chart-header {
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--va-background-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.chart-title {
  margin: 0;
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--va-text-primary);
}

.chart-desc {
  margin: 0;
  font-size: 0.8rem;
  color: var(--va-text-secondary);
}

.period-toggle-group {
  display: flex;
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  padding: 2px;
}

.period-btn {
  padding: 4px 10px;
  font-size: 0.78rem;
  font-weight: 500;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  background: transparent;
  color: var(--va-text-secondary);
  transition: all 0.2s ease;
}

.period-btn.active {
  background: var(--va-primary);
  color: #ffffff;
  font-weight: 700;
}

.chart-content {
  padding: 1rem 1.25rem 1.5rem;
  position: relative;
}

.legend-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px dashed var(--va-background-border);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  padding: 3px 8px;
  border-radius: 6px;
  background: var(--va-background-element);
  border: 1px solid var(--va-background-border);
  user-select: none;
  transition: opacity 0.2s ease;
}

.legend-item.inactive {
  opacity: 0.4;
  text-decoration: line-through;
}

.legend-color-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-name {
  font-weight: 600;
  color: var(--va-text-primary);
}

.legend-points {
  font-size: 0.72rem;
  color: var(--va-text-secondary);
  font-family: monospace;
}

.svg-container {
  position: relative;
  width: 100%;
  height: 260px;
}

.trend-svg {
  width: 100%;
  height: 100%;
  overflow: visible;
}

.chart-point {
  cursor: pointer;
  transition: r 0.2s ease;
}

.chart-point:hover {
  r: 6.5;
}

.chart-tooltip {
  position: absolute;
  pointer-events: none;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 8px;
  padding: 8px 12px;
  color: #ffffff;
  z-index: 50;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
  min-width: 130px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.tooltip-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tooltip-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.tooltip-title {
  font-size: 0.78rem;
  font-weight: 700;
  color: #f1f5f9;
}

.tooltip-score {
  font-size: 1.1rem;
  font-weight: 800;
  color: #38bdf8;
  font-family: 'Pretendard', sans-serif;
}

.tooltip-sub {
  display: flex;
  flex-direction: column;
  font-size: 0.7rem;
  color: #94a3b8;
  font-family: monospace;
}

.chart-loading,
.empty-chart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  height: 220px;
}

.empty-chart-text {
  font-size: 0.85rem;
  color: var(--va-text-secondary);
}
</style>
