<template>
  <div class="roi-calculator-page">
    <!-- Hero Header -->
    <section class="roi-hero">
      <div class="roi-hero-container">
        <span class="badge-tag">MDM ROI ESTIMATOR</span>
        <h1 class="hero-title">{{ $t('title') }}</h1>
        <p class="hero-subtitle">{{ $t('subtitle') }}</p>
      </div>
    </section>

    <!-- Main Calculator Body -->
    <main class="roi-main-container">
      <div class="calculator-grid">
        <!-- Left Column: Input Sliders -->
        <va-card class="inputs-card">
          <va-card-title class="inputs-card-title">
            <va-icon name="tune" color="primary" class="mr-2" />
            <div>
              <div class="title-text">{{ $t('inputsTitle') }}</div>
              <div class="subtitle-text">{{ $t('inputsSubtitle') }}</div>
            </div>
          </va-card-title>

          <va-card-content class="inputs-body">
            <!-- 1. Partner Count -->
            <div class="input-group">
              <div class="group-header">
                <label class="group-label">
                  <va-icon name="apartment" size="small" class="mr-1" color="primary" />
                  {{ $t('partnerCount') }}
                </label>
                <span class="value-highlight">{{ formatNumber(form.partnerCount) }} {{ $t('unitCount') }}</span>
              </div>
              <va-slider
                v-model="form.partnerCount"
                :min="100"
                :max="10000"
                :step="100"
                class="roi-slider"
              />
              <span class="hint-text">{{ $t('partnerCountHint') }}</span>
            </div>

            <!-- 2. Monthly Invoice Count -->
            <div class="input-group">
              <div class="group-header">
                <label class="group-label">
                  <va-icon name="receipt_long" size="small" class="mr-1" color="primary" />
                  {{ $t('monthlyInvoiceCount') }}
                </label>
                <span class="value-highlight">{{ formatNumber(form.monthlyInvoiceCount) }} {{ $t('unitInvoice') }}</span>
              </div>
              <va-slider
                v-model="form.monthlyInvoiceCount"
                :min="500"
                :max="50000"
                :step="500"
                class="roi-slider"
              />
              <span class="hint-text">{{ $t('monthlyInvoiceCountHint') }}</span>
            </div>

            <!-- 3. Hourly Wage -->
            <div class="input-group">
              <div class="group-header">
                <label class="group-label">
                  <va-icon name="payments" size="small" class="mr-1" color="primary" />
                  {{ $t('hourlyWage') }}
                </label>
                <span class="value-highlight">{{ formatNumber(form.hourlyWage) }} {{ $t('unitWon') }}</span>
              </div>
              <va-slider
                v-model="form.hourlyWage"
                :min="15000"
                :max="60000"
                :step="2500"
                class="roi-slider"
              />
              <span class="hint-text">{{ $t('hourlyWageHint') }}</span>
            </div>

            <!-- 4. Manual Audit Hours -->
            <div class="input-group">
              <div class="group-header">
                <label class="group-label">
                  <va-icon name="schedule" size="small" class="mr-1" color="primary" />
                  {{ $t('manualAuditHoursMonthly') }}
                </label>
                <span class="value-highlight">{{ formatNumber(form.manualAuditHoursMonthly) }} {{ $t('unitHour') }}</span>
              </div>
              <va-slider
                v-model="form.manualAuditHoursMonthly"
                :min="5"
                :max="200"
                :step="5"
                class="roi-slider"
              />
              <span class="hint-text">{{ $t('manualAuditHoursMonthlyHint') }}</span>
            </div>
          </va-card-content>
        </va-card>

        <!-- Right Column: Results & Highlights -->
        <div class="results-column">
          <!-- Big KPI Card -->
          <va-card class="results-kpi-card">
            <va-card-content class="kpi-content">
              <div class="kpi-header">
                <span class="results-tag">{{ $t('resultsTitle') }}</span>
                <span class="payback-pill">
                  <va-icon name="bolt" size="small" class="mr-1" />
                  {{ $t('paybackDays', { days: calculatedRoi.paybackDays }) }}
                </span>
              </div>

              <!-- Annual Total Savings (Big Hero Number) -->
              <div class="primary-metric">
                <div class="metric-label">{{ $t('annualSavings') }}</div>
                <div class="metric-number-wrap">
                  <span class="metric-number">{{ formatNumber(calculatedRoi.annualTotalSavings) }}</span>
                  <span class="metric-unit">{{ $t('unitWon') }}</span>
                </div>
              </div>

              <!-- Secondary Metrics Grid -->
              <div class="sub-metrics-grid">
                <div class="sub-metric-box">
                  <div class="sub-label">{{ $t('monthlySavings') }}</div>
                  <div class="sub-value">{{ formatNumber(calculatedRoi.monthlyTotalSavings) }} {{ $t('unitWon') }}</div>
                </div>
                <div class="sub-metric-box">
                  <div class="sub-label">{{ $t('annualHoursSaved') }}</div>
                  <div class="sub-value">{{ formatNumber(calculatedRoi.annualHoursSaved) }} {{ $t('unitHour') }}</div>
                </div>
                <div class="sub-metric-box">
                  <div class="sub-label">{{ $t('monthlyHoursSaved') }}</div>
                  <div class="sub-value">{{ formatNumber(calculatedRoi.monthlyHoursSaved) }} {{ $t('unitHour') }}</div>
                </div>
                <div class="sub-metric-box">
                  <div class="sub-label">{{ $t('starterMultiplier') }}</div>
                  <div class="sub-value highlight-roi">{{ calculatedRoi.starterRoiMultiplier }}{{ $t('multiplierUnit') }}</div>
                </div>
              </div>

              <!-- CTA in KPI Card -->
              <div class="kpi-cta-wrap">
                <va-button
                  to="/register"
                  color="primary"
                  size="large"
                  class="w-full cta-hero-btn"
                >
                  <va-icon name="rocket_launch" class="mr-2" />
                  {{ $t('ctaButton') }}
                </va-button>
                <div class="cta-micro-hints">
                  <span>✓ {{ $t('ctaBenefit1') }}</span>
                  <span>✓ {{ $t('ctaBenefit2') }}</span>
                </div>
              </div>
            </va-card-content>
          </va-card>
        </div>
      </div>

      <!-- 4 Core Breakdown Cards Section -->
      <section class="breakdown-section">
        <h2 class="section-title">
          <va-icon name="pie_chart" color="primary" class="mr-2" />
          {{ $t('breakdownTitle') }}
        </h2>

        <div class="breakdown-grid">
          <!-- 1. Master Error Correction -->
          <va-card class="breakdown-card">
            <va-card-content class="breakdown-card-inner">
              <div class="breakdown-icon-wrap icon-blue">
                <va-icon name="verified" size="large" />
              </div>
              <div class="breakdown-info">
                <div class="breakdown-name">{{ $t('areaMasterError') }}</div>
                <div class="breakdown-amount">
                  {{ formatNumber(calculatedRoi.masterErrorSavings) }} {{ $t('unitWon') }}
                  <span class="per-month">/월</span>
                </div>
                <p class="breakdown-desc">{{ $t('areaMasterErrorDesc') }}</p>
              </div>
            </va-card-content>
          </va-card>

          <!-- 2. Duplicate Partner Reconciliation -->
          <va-card class="breakdown-card">
            <va-card-content class="breakdown-card-inner">
              <div class="breakdown-icon-wrap icon-purple">
                <va-icon name="call_merge" size="large" />
              </div>
              <div class="breakdown-info">
                <div class="breakdown-name">{{ $t('areaDuplicate') }}</div>
                <div class="breakdown-amount">
                  {{ formatNumber(calculatedRoi.duplicateSavings) }} {{ $t('unitWon') }}
                  <span class="per-month">/월</span>
                </div>
                <p class="breakdown-desc">{{ $t('areaDuplicateDesc') }}</p>
              </div>
            </va-card-content>
          </va-card>

          <!-- 3. Manual Audit Automation -->
          <va-card class="breakdown-card">
            <va-card-content class="breakdown-card-inner">
              <div class="breakdown-icon-wrap icon-teal">
                <va-icon name="auto_mode" size="large" />
              </div>
              <div class="breakdown-info">
                <div class="breakdown-name">{{ $t('areaManualAudit') }}</div>
                <div class="breakdown-amount">
                  {{ formatNumber(calculatedRoi.manualAuditSavings) }} {{ $t('unitWon') }}
                  <span class="per-month">/월</span>
                </div>
                <p class="breakdown-desc">{{ $t('areaManualAuditDesc') }}</p>
              </div>
            </va-card-content>
          </va-card>

          <!-- 4. Tax & External Audit Preparation -->
          <va-card class="breakdown-card">
            <va-card-content class="breakdown-card-inner">
              <div class="breakdown-icon-wrap icon-amber">
                <va-icon name="security" size="large" />
              </div>
              <div class="breakdown-info">
                <div class="breakdown-name">{{ $t('areaTaxAudit') }}</div>
                <div class="breakdown-amount">
                  {{ formatNumber(calculatedRoi.taxAuditSavings) }} {{ $t('unitWon') }}
                  <span class="per-month">/월</span>
                </div>
                <p class="breakdown-desc">{{ $t('areaTaxAuditDesc') }}</p>
              </div>
            </va-card-content>
          </va-card>
        </div>
      </section>

      <!-- Plan Recommendations Section -->
      <section class="plans-section">
        <h2 class="section-title">
          <va-icon name="military_tech" color="primary" class="mr-2" />
          {{ $t('plansTitle') }}
        </h2>

        <div class="plans-grid">
          <!-- Starter Plan -->
          <va-card :class="['plan-card', isStarterRecommended ? 'recommended-card' : '']">
            <va-card-content class="plan-card-body">
              <div class="plan-badge-row">
                <span class="plan-tier">{{ $t('starterPlan') }}</span>
                <va-badge
                  v-if="isStarterRecommended"
                  :text="$t('recommended')"
                  color="primary"
                  class="recommend-badge"
                />
              </div>
              <div class="plan-price">{{ $t('starterPrice') }}</div>
              <p class="plan-desc">{{ $t('starterDesc') }}</p>

              <div class="plan-roi-box">
                <span class="plan-roi-label">{{ $t('planRoi') }}</span>
                <span class="plan-roi-val">{{ calculatedRoi.starterRoiMultiplier }}{{ $t('multiplierUnit') }}</span>
              </div>

              <va-button
                to="/register"
                :color="isStarterRecommended ? 'primary' : 'secondary'"
                preset="outline"
                class="w-full mt-4"
              >
                {{ $t('ctaButton') }}
              </va-button>
            </va-card-content>
          </va-card>

          <!-- Pro Plan -->
          <va-card :class="['plan-card', !isStarterRecommended ? 'recommended-card' : '']">
            <va-card-content class="plan-card-body">
              <div class="plan-badge-row">
                <span class="plan-tier">{{ $t('proPlan') }}</span>
                <va-badge
                  v-if="!isStarterRecommended"
                  :text="$t('recommended')"
                  color="primary"
                  class="recommend-badge"
                />
              </div>
              <div class="plan-price">{{ $t('proPrice') }}</div>
              <p class="plan-desc">{{ $t('proDesc') }}</p>

              <div class="plan-roi-box">
                <span class="plan-roi-label">{{ $t('planRoi') }}</span>
                <span class="plan-roi-val">{{ calculatedRoi.proRoiMultiplier }}{{ $t('multiplierUnit') }}</span>
              </div>

              <va-button
                to="/register"
                :color="!isStarterRecommended ? 'primary' : 'secondary'"
                preset="outline"
                class="w-full mt-4"
              >
                {{ $t('ctaButton') }}
              </va-button>
            </va-card-content>
          </va-card>
        </div>
      </section>

      <!-- Bottom Lead Magnet Banner -->
      <section class="bottom-lead-banner">
        <div class="banner-inner">
          <div class="banner-content">
            <h2 class="banner-title">{{ $t('ctaTitle') }}</h2>
            <p class="banner-subtitle">{{ $t('ctaSubtitle') }}</p>
            <div class="banner-benefits">
              <span class="benefit-item"><va-icon name="check_circle" size="small" class="mr-1" /> {{ $t('ctaBenefit1') }}</span>
              <span class="benefit-item"><va-icon name="check_circle" size="small" class="mr-1" /> {{ $t('ctaBenefit2') }}</span>
              <span class="benefit-item"><va-icon name="check_circle" size="small" class="mr-1" /> {{ $t('ctaBenefit3') }}</span>
            </div>
          </div>
          <div class="banner-action">
            <va-button
              to="/register"
              color="primary"
              size="large"
              class="banner-cta-btn"
            >
              <va-icon name="arrow_forward" class="mr-2" />
              {{ $t('ctaButton') }}
            </va-button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'

definePageMeta({
  layout: 'landing'
})

const { t } = useI18n()

useHead({
  title: computed(() => `${t('title')} - MDM Platform`),
  meta: [
    {
      name: 'description',
      content: computed(() => t('subtitle'))
    }
  ]
})

// Input State
const form = reactive({
  partnerCount: 1000,
  monthlyInvoiceCount: 3000,
  hourlyWage: 25000,
  manualAuditHoursMonthly: 40,
  annualRevenue: 10000000000
})

// Number formatter
const formatNumber = (val: number): string => {
  if (val === undefined || val === null || isNaN(val)) return '0'
  return Math.round(val).toLocaleString()
}

// 4대 핵심 절감 공식 클라이언트 실시간 연산 (백엔드 알고리즘과 100% 동기화)
const calculatedRoi = computed(() => {
  const wage = form.hourlyWage || 25000

  // 1. 마스터 오류 정정: 월 전표 건수의 3% 오류 * 건당 1시간 * 시급
  const masterErrorHours = Math.round(form.monthlyInvoiceCount * 0.03 * 1.0)
  const masterErrorSavings = masterErrorHours * wage

  // 2. 중복 거래처 대사 단축: 거래처 50개당 월 1시간 (최소 5시간) * 시급
  const duplicateHours = Math.max(5, Math.round(form.partnerCount / 50))
  const duplicateSavings = duplicateHours * wage

  // 3. 수작업 검수 자동화: 월간 수작업 검수 시간의 80% 자동화 단축 * 시급
  const manualAuditHours = Math.round(form.manualAuditHoursMonthly * 0.8)
  const manualAuditSavings = manualAuditHours * wage

  // 4. 세무·감사 증빙 단축: 연간 32시간 단축분의 월 환산 (32 / 12 = 약 3시간) * 시급
  const taxAuditHours = Math.round(32 / 12)
  const taxAuditSavings = taxAuditHours * wage

  // 합계
  const monthlyTotalSavings = masterErrorSavings + duplicateSavings + manualAuditSavings + taxAuditSavings
  const annualTotalSavings = monthlyTotalSavings * 12

  const monthlyHoursSaved = masterErrorHours + duplicateHours + manualAuditHours + taxAuditHours
  const annualHoursSaved = monthlyHoursSaved * 12

  // 플랜별 비용 (Starter: 110,000원/월, Pro: 790,000원/월)
  const starterPrice = 110000
  const proPrice = 790000

  const starterRoiMultiplier = Math.round((monthlyTotalSavings / starterPrice) * 10) / 10
  const proRoiMultiplier = Math.round((monthlyTotalSavings / proPrice) * 10) / 10

  // 회수 기간 (일) = 기준 플랜 월 비용 / 일일 절감액
  const dailySavings = monthlyTotalSavings / 30.0
  const paybackDays = dailySavings > 0 ? Math.max(1, Math.round(starterPrice / dailySavings)) : 1

  return {
    masterErrorSavings,
    duplicateSavings,
    manualAuditSavings,
    taxAuditSavings,
    monthlyTotalSavings,
    annualTotalSavings,
    monthlyHoursSaved,
    annualHoursSaved,
    starterRoiMultiplier,
    proRoiMultiplier,
    paybackDays
  }
})

// 거래처 수 기준 권장 플랜 판단
const isStarterRecommended = computed(() => {
  return form.partnerCount <= 1000
})
</script>

<style scoped>
.roi-calculator-page {
  width: 100%;
  min-height: 100vh;
  padding-bottom: 60px;
}

/* Hero Section */
.roi-hero {
  background: linear-gradient(135deg, rgba(21, 101, 192, 0.08) 0%, rgba(13, 71, 161, 0.15) 100%);
  padding: 60px 24px 40px;
  text-align: center;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.roi-hero-container {
  max-width: 900px;
  margin: 0 auto;
}

.badge-tag {
  display: inline-block;
  padding: 4px 12px;
  background-color: rgba(21, 101, 192, 0.12);
  color: #1565c0;
  font-size: 0.75rem;
  font-weight: 700;
  border-radius: 999px;
  letter-spacing: 0.08em;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 2.3rem;
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: 14px;
  color: var(--va-text-primary, #1e293b);
}

.hero-subtitle {
  font-size: 1.1rem;
  color: var(--va-text-secondary, #64748b);
  line-height: 1.6;
}

/* Main Container */
.roi-main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 24px;
}

/* Grid Layout */
.calculator-grid {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 32px;
  align-items: stretch;
  margin-bottom: 48px;
}

@media (max-width: 960px) {
  .calculator-grid {
    grid-template-columns: 1fr;
  }
}

/* Inputs Card */
.inputs-card {
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.06), 0 8px 10px -6px rgba(0, 0, 0, 0.04);
}

.inputs-card-title {
  display: flex;
  align-items: center;
  padding: 24px 24px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.title-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--va-text-primary, #1e293b);
}

.subtitle-text {
  font-size: 0.85rem;
  color: var(--va-text-secondary, #64748b);
  margin-top: 2px;
}

.inputs-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.input-group {
  display: flex;
  flex-direction: column;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.group-label {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 0.95rem;
  color: var(--va-text-primary, #334155);
}

.value-highlight {
  font-weight: 700;
  font-size: 1.05rem;
  color: #1565c0;
}

.roi-slider {
  margin: 4px 0;
}

.hint-text {
  font-size: 0.8rem;
  color: var(--va-text-secondary, #94a3b8);
  margin-top: 4px;
}

/* Results KPI Card */
.results-kpi-card {
  border-radius: 16px;
  background: linear-gradient(145deg, #1e293b, #0f172a);
  color: #ffffff;
  box-shadow: 0 15px 30px rgba(15, 23, 42, 0.25);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.kpi-content {
  padding: 32px;
  display: flex;
  flex-direction: column;
  height: 100%;
  justify-content: space-between;
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 8px;
}

.results-tag {
  font-size: 0.85rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 700;
  color: #94a3b8;
}

.payback-pill {
  display: flex;
  align-items: center;
  background-color: rgba(34, 197, 94, 0.2);
  color: #4ade80;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 600;
  border: 1px solid rgba(74, 222, 128, 0.3);
}

.primary-metric {
  margin-bottom: 28px;
}

.metric-label {
  font-size: 0.95rem;
  color: #cbd5e1;
  margin-bottom: 6px;
}

.metric-number-wrap {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.metric-number {
  font-size: 2.8rem;
  font-weight: 800;
  color: #38bdf8;
  line-height: 1;
}

.metric-unit {
  font-size: 1.3rem;
  font-weight: 600;
  color: #94a3b8;
}

.sub-metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 28px;
}

.sub-metric-box {
  background-color: rgba(255, 255, 255, 0.05);
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.sub-label {
  font-size: 0.8rem;
  color: #94a3b8;
  margin-bottom: 4px;
}

.sub-value {
  font-size: 1.15rem;
  font-weight: 700;
  color: #f8fafc;
}

.highlight-roi {
  color: #facc15;
}

.kpi-cta-wrap {
  margin-top: 8px;
}

.cta-hero-btn {
  font-weight: 700;
  font-size: 1.05rem;
  height: 48px;
  border-radius: 10px;
}

.cta-micro-hints {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 10px;
  font-size: 0.78rem;
  color: #94a3b8;
}

/* Section Title */
.section-title {
  display: flex;
  align-items: center;
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 20px;
  color: var(--va-text-primary, #1e293b);
}

/* Breakdown Grid */
.breakdown-section {
  margin-bottom: 48px;
}

.breakdown-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
}

.breakdown-card {
  border-radius: 14px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.breakdown-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 20px -5px rgba(0, 0, 0, 0.08);
}

.breakdown-card-inner {
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.breakdown-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-blue {
  background-color: #eff6ff;
  color: #2563eb;
}

.icon-purple {
  background-color: #faf5ff;
  color: #9333ea;
}

.icon-teal {
  background-color: #f0fdfa;
  color: #0d9488;
}

.icon-amber {
  background-color: #fffbeb;
  color: #d97706;
}

.breakdown-name {
  font-weight: 700;
  font-size: 1rem;
  margin-bottom: 4px;
  color: var(--va-text-primary, #1e293b);
}

.breakdown-amount {
  font-size: 1.25rem;
  font-weight: 800;
  color: #1565c0;
  margin-bottom: 8px;
}

.per-month {
  font-size: 0.8rem;
  font-weight: 500;
  color: #64748b;
}

.breakdown-desc {
  font-size: 0.85rem;
  color: var(--va-text-secondary, #64748b);
  line-height: 1.5;
  margin: 0;
}

/* Plans Section */
.plans-section {
  margin-bottom: 48px;
}

.plans-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 768px) {
  .plans-grid {
    grid-template-columns: 1fr;
  }
}

.plan-card {
  border-radius: 14px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
}

.recommended-card {
  border: 2px solid #1565c0;
  box-shadow: 0 10px 25px -5px rgba(21, 101, 192, 0.15);
}

.plan-card-body {
  padding: 28px;
}

.plan-badge-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.plan-tier {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--va-text-primary, #1e293b);
}

.plan-price {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1565c0;
  margin-bottom: 6px;
}

.plan-desc {
  font-size: 0.9rem;
  color: var(--va-text-secondary, #64748b);
  margin-bottom: 20px;
}

.plan-roi-box {
  background-color: rgba(21, 101, 192, 0.05);
  padding: 12px 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.plan-roi-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #334155;
}

.plan-roi-val {
  font-size: 1.15rem;
  font-weight: 800;
  color: #1565c0;
}

/* Bottom Lead Magnet Banner */
.bottom-lead-banner {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
  border-radius: 20px;
  color: #ffffff;
  padding: 40px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.banner-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 32px;
  flex-wrap: wrap;
}

.banner-content {
  flex: 1;
  min-width: 300px;
}

.banner-title {
  font-size: 1.6rem;
  font-weight: 800;
  margin-bottom: 10px;
  color: #ffffff;
}

.banner-subtitle {
  font-size: 0.95rem;
  color: #cbd5e1;
  line-height: 1.6;
  margin-bottom: 16px;
}

.banner-benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 0.85rem;
  color: #94a3b8;
}

.benefit-item {
  display: flex;
  align-items: center;
  color: #4ade80;
}

.banner-action {
  flex-shrink: 0;
}

.banner-cta-btn {
  font-weight: 700;
  font-size: 1.05rem;
  padding: 0 28px;
  height: 50px;
  border-radius: 10px;
}
</style>
