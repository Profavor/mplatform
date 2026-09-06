<template>
  <va-card class="section-card lease-risk-card">
    <va-card-title class="card-header-title">
      <div class="header-left">
        <va-icon name="real_estate_agent" size="medium" color="primary" />
        <span class="widget-title">{{ t('lease_risk.title') }}</span>
        <va-badge
          v-if="leaseSummary?.totalLeaseContracts != null"
          :text="t('lease_risk.active_contracts', { count: leaseSummary.totalLeaseContracts })"
          color="primary"
          outline
          class="count-badge"
        />
      </div>
      <div class="header-right">
        <va-button
          v-if="showRefresh"
          preset="plain"
          icon="refresh"
          size="small"
          :loading="isLoading"
          @click="emit('refresh')"
        />
      </div>
    </va-card-title>

    <va-card-content class="card-body">
      <!-- 4 Core Risk KPI Grid -->
      <div class="risk-kpi-grid">
        <!-- 1. Expiring within 30 days -->
        <div class="risk-kpi-item warning-kpi">
          <div class="kpi-icon-box orange-box">
            <va-icon name="event_busy" size="medium" color="warning" />
          </div>
          <div class="kpi-content">
            <span class="kpi-label">{{ t('lease_risk.expiring_30_days') }}</span>
            <div class="kpi-value-row">
              <span class="kpi-val orange-text">{{ leaseSummary?.expiringWithin30DaysCount?.toLocaleString() ?? 0 }}</span>
              <span class="kpi-unit">{{ t('lease_risk.count_unit') }}</span>
            </div>
          </div>
        </div>

        <!-- 2. Overdue rent contracts -->
        <div class="risk-kpi-item danger-kpi">
          <div class="kpi-icon-box red-box">
            <va-icon name="warning" size="medium" color="danger" />
          </div>
          <div class="kpi-content">
            <span class="kpi-label">{{ t('lease_risk.overdue_contracts') }}</span>
            <div class="kpi-value-row">
              <span class="kpi-val red-text">{{ leaseSummary?.overdueContractsCount?.toLocaleString() ?? 0 }}</span>
              <span class="kpi-unit">{{ t('lease_risk.count_unit') }}</span>
            </div>
          </div>
        </div>

        <!-- 3. High Debt Ratio Units -->
        <div class="risk-kpi-item purple-kpi">
          <div class="kpi-icon-box purple-box">
            <va-icon name="trending_up" size="medium" color="#8b5cf6" />
          </div>
          <div class="kpi-content">
            <span class="kpi-label">{{ t('lease_risk.high_debt_ratio') }}</span>
            <div class="kpi-value-row">
              <span class="kpi-val purple-text">{{ leaseSummary?.highDebtRatioUnitsCount?.toLocaleString() ?? 0 }}</span>
              <span class="kpi-unit">{{ t('lease_risk.count_unit') }}</span>
            </div>
          </div>
        </div>

        <!-- 4. Financial Total Summary -->
        <div class="risk-kpi-item info-kpi">
          <div class="kpi-icon-box blue-box">
            <va-icon name="account_balance_wallet" size="medium" color="info" />
          </div>
          <div class="kpi-content financial-content">
            <div class="financial-row">
              <span class="kpi-sublabel">{{ t('lease_risk.monthly_rent_total') }}:</span>
              <span class="kpi-subval blue-text">{{ (leaseSummary?.totalMonthlyRent ?? 0).toLocaleString() }} {{ t('lease_risk.currency_unit') }}</span>
            </div>
            <div class="financial-row">
              <span class="kpi-sublabel">{{ t('lease_risk.deposit_total') }}:</span>
              <span class="kpi-subval gray-text">{{ (leaseSummary?.totalDeposit ?? 0).toLocaleString() }} {{ t('lease_risk.currency_unit') }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Urgent Action Required Contracts Section -->
      <div class="urgent-section">
        <div class="urgent-section-header">
          <div class="section-title-wrap">
            <va-icon name="priority_high" size="small" color="danger" />
            <span class="urgent-title">{{ t('lease_risk.urgent_actions') }}</span>
          </div>
          <span v-if="leaseSummary?.urgentContracts?.length" class="urgent-count-text">
            {{ leaseSummary.urgentContracts.length }}{{ t('lease_risk.count_unit') }}
          </span>
        </div>

        <!-- Empty state -->
        <div v-if="!leaseSummary?.urgentContracts || leaseSummary.urgentContracts.length === 0" class="empty-urgent-state">
          <va-icon name="check_circle_outline" size="2rem" color="success" />
          <p>{{ t('lease_risk.no_urgent_contracts') }}</p>
        </div>

        <!-- Urgent Contracts List / Table -->
        <div v-else class="urgent-contracts-list">
          <div
            v-for="contract in leaseSummary.urgentContracts"
            :key="contract.recordId"
            class="contract-item-row"
          >
            <div class="contract-main-info">
              <div class="contract-top-line">
                <span class="contract-no">{{ contract.contractNo || contract.recordId }}</span>
                <span class="building-unit">{{ contract.buildingUnit }}</span>
              </div>
              <div class="contract-bottom-line">
                <span class="tenant-name">
                  <va-icon name="person" size="14px" />
                  {{ contract.tenantName }}
                </span>
              </div>
            </div>

            <div class="contract-risk-badges">
              <!-- Expiration D-day badge -->
              <va-badge
                v-if="contract.daysUntilExpiration != null"
                :text="contract.daysUntilExpiration <= 0 ? 'D-Day' : 'D-' + contract.daysUntilExpiration"
                :color="contract.daysUntilExpiration <= 7 ? 'danger' : 'warning'"
                class="risk-badge"
              />
              <!-- Overdue badge -->
              <va-badge
                v-if="contract.overdueCount != null && contract.overdueCount > 0"
                :text="contract.overdueCount + t('lease_risk.count_unit') + ' ' + t('lease_risk.overdue_contracts')"
                color="danger"
                class="risk-badge"
              />
              <!-- Debt ratio badge -->
              <va-badge
                v-if="contract.debtRatio != null"
                :text="contract.debtRatio + '%'"
                :color="contract.debtRatio >= 80 ? 'danger' : 'primary'"
                outline
                class="risk-badge"
              />
            </div>

            <div class="contract-action">
              <va-button
                size="small"
                preset="secondary"
                color="primary"
                icon-right="arrow_forward"
                @click="emit('navigate-record', contract)"
              >
                {{ t('lease_risk.view_contract') }}
              </va-button>
            </div>
          </div>
        </div>
      </div>
    </va-card-content>
  </va-card>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

export interface UrgentContractItem {
  recordId: string
  contractNo: string
  buildingUnit: string
  tenantName: string
  daysUntilExpiration?: number
  overdueCount?: number
  debtRatio?: number
  status?: string
}

export interface LeaseSummaryData {
  totalLeaseContracts: number
  expiringWithin30DaysCount: number
  overdueContractsCount: number
  highDebtRatioUnitsCount: number
  totalMonthlyRent: number
  totalDeposit: number
  urgentContracts: UrgentContractItem[]
}

const props = withDefaults(
  defineProps<{
    leaseSummary?: LeaseSummaryData | null
    isLoading?: boolean
    showRefresh?: boolean
  }>(),
  {
    leaseSummary: null,
    isLoading: false,
    showRefresh: false
  }
)

const emit = defineEmits<{
  (e: 'navigate-record', contract: UrgentContractItem): void
  (e: 'refresh'): void
}>()

const { t } = useI18n()
</script>

<style scoped>
.lease-risk-card {
  border-radius: 12px;
  border: 1px solid var(--va-background-border, #e2e8f0);
  background: var(--va-background-primary, #ffffff);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.card-header-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--va-background-border, #f1f5f9);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.widget-title {
  font-weight: 700;
  font-size: 1.05rem;
  color: var(--va-text-primary, #1e293b);
}

.count-badge {
  font-size: 0.75rem;
  font-weight: 600;
}

.card-body {
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

/* 4 Core KPI Grid */
.risk-kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1rem;
}

.risk-kpi-item {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.9rem 1rem;
  border-radius: 10px;
  border: 1px solid var(--va-background-border, #e2e8f0);
  background: var(--va-background-element, #f8fafc);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.risk-kpi-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.kpi-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 10px;
  flex-shrink: 0;
}

.orange-box {
  background: rgba(245, 158, 11, 0.12);
}

.red-box {
  background: rgba(239, 68, 68, 0.12);
}

.purple-box {
  background: rgba(139, 92, 246, 0.12);
}

.blue-box {
  background: rgba(59, 130, 246, 0.12);
}

.kpi-content {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  flex: 1;
}

.kpi-label {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--va-text-secondary, #64748b);
}

.kpi-value-row {
  display: flex;
  align-items: baseline;
  gap: 0.25rem;
}

.kpi-val {
  font-size: 1.35rem;
  font-weight: 700;
  line-height: 1.2;
}

.kpi-unit {
  font-size: 0.8rem;
  color: var(--va-text-secondary, #64748b);
}

.orange-text {
  color: #f59e0b;
}

.red-text {
  color: #ef4444;
}

.purple-text {
  color: #8b5cf6;
}

.blue-text {
  color: #3b82f6;
}

.gray-text {
  color: var(--va-text-secondary, #64748b);
}

.financial-content {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.financial-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.78rem;
  gap: 0.5rem;
}

.kpi-sublabel {
  color: var(--va-text-secondary, #64748b);
  white-space: nowrap;
}

.kpi-subval {
  font-weight: 600;
}

/* Urgent Action Section */
.urgent-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border-top: 1px solid var(--va-background-border, #f1f5f9);
  padding-top: 1rem;
}

.urgent-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title-wrap {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.urgent-title {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--va-text-primary, #1e293b);
}

.urgent-count-text {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--va-text-secondary, #64748b);
}

.empty-urgent-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 1rem;
  gap: 0.5rem;
  color: var(--va-text-secondary, #64748b);
  font-size: 0.88rem;
  background: var(--va-background-element, #f8fafc);
  border-radius: 8px;
}

.urgent-contracts-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.contract-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  background: var(--va-background-element, #f8fafc);
  border: 1px solid var(--va-background-border, #e2e8f0);
  gap: 1rem;
  transition: background 0.15s ease;
}

.contract-item-row:hover {
  background: var(--va-background-hover, #f1f5f9);
}

.contract-main-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  min-width: 200px;
}

.contract-top-line {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.contract-no {
  font-weight: 700;
  font-size: 0.88rem;
  color: var(--va-primary, #2563eb);
}

.building-unit {
  font-size: 0.85rem;
  color: var(--va-text-primary, #1e293b);
  font-weight: 500;
}

.contract-bottom-line {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.tenant-name {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.78rem;
  color: var(--va-text-secondary, #64748b);
}

.contract-risk-badges {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.risk-badge {
  font-size: 0.75rem;
  font-weight: 600;
}

.contract-action {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .contract-item-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  .contract-action {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
