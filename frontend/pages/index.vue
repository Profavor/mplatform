<template>
  <div class="landing-page">
    <!-- 1. Hero Section -->
    <section class="hero-section">
      <div class="hero-container">
        <!-- Top Badge -->
        <div class="hero-badge">
          <va-icon name="verified" size="small" color="primary" class="mr-1" />
          <span>{{ $t('landing.hero.badge') }}</span>
        </div>

        <!-- Main Headline -->
        <h1 class="hero-title">
          {{ $t('landing.hero.title') }}
        </h1>

        <!-- Subtitle -->
        <p class="hero-subtitle">
          {{ $t('landing.hero.subtitle') }}
        </p>

        <!-- CTA Buttons -->
        <div class="hero-cta-group">
          <va-button
            to="/register"
            color="primary"
            size="large"
            icon-right="arrow_forward"
            class="hero-primary-cta"
          >
            {{ $t('landing.hero.cta_start') }}
          </va-button>
          <va-button
            href="#features"
            preset="outline"
            color="primary"
            size="large"
            icon-right="explore"
            class="hero-secondary-cta"
          >
            {{ $t('landing.hero.cta_demo') }}
          </va-button>
          <va-button
            to="/roi-calculator"
            preset="outline"
            color="primary"
            size="large"
            icon-right="calculate"
            class="hero-secondary-cta"
          >
            {{ $t('landing.hero.cta_roi') }}
          </va-button>
        </div>

        <!-- Trust Badges Under Hero -->
        <div class="hero-trust-badges">
          <span class="trust-badge-item">
            <va-icon name="check_circle" size="small" color="success" class="mr-1" />
            {{ $t('landing.hero.badge_dq') }}
          </span>
          <span class="trust-badge-item">
            <va-icon name="check_circle" size="small" color="success" class="mr-1" />
            {{ $t('landing.hero.badge_lineage') }}
          </span>
          <span class="trust-badge-item">
            <va-icon name="check_circle" size="small" color="success" class="mr-1" />
            {{ $t('landing.hero.badge_security') }}
          </span>
        </div>

        <!-- Stats Bar -->
        <div class="hero-stats-grid">
          <div class="stat-card">
            <div class="stat-value">{{ $t('landing.stats.integration_rate') }}</div>
            <div class="stat-label">{{ $t('landing.stats.integration_rate_label') }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ $t('landing.stats.dedup_saving') }}</div>
            <div class="stat-label">{{ $t('landing.stats.dedup_saving_label') }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ $t('landing.stats.audit_coverage') }}</div>
            <div class="stat-label">{{ $t('landing.stats.audit_coverage_label') }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ $t('landing.stats.active_connectors') }}</div>
            <div class="stat-label">{{ $t('landing.stats.active_connectors_label') }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. Why MDM Section -->
    <section id="why-mdm" class="content-section bg-secondary">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">{{ $t('landing.why_mdm.badge') }}</span>
          <h2 class="section-title">{{ $t('landing.why_mdm.title') }}</h2>
          <p class="section-subtitle">{{ $t('landing.why_mdm.subtitle') }}</p>
        </div>

        <div class="cards-grid-3">
          <div class="card-item problem-card">
            <div class="card-icon-box danger">
              <va-icon name="grid_view" size="medium" color="danger" />
            </div>
            <h3 class="card-title">{{ $t('landing.why_mdm.problem1_title') }}</h3>
            <p class="card-desc">{{ $t('landing.why_mdm.problem1_desc') }}</p>
          </div>

          <div class="card-item problem-card">
            <div class="card-icon-box danger">
              <va-icon name="table_view" size="medium" color="danger" />
            </div>
            <h3 class="card-title">{{ $t('landing.why_mdm.problem2_title') }}</h3>
            <p class="card-desc">{{ $t('landing.why_mdm.problem2_desc') }}</p>
          </div>

          <div class="card-item problem-card">
            <div class="card-icon-box danger">
              <va-icon name="history" size="medium" color="danger" />
            </div>
            <h3 class="card-title">{{ $t('landing.why_mdm.problem3_title') }}</h3>
            <p class="card-desc">{{ $t('landing.why_mdm.problem3_desc') }}</p>
          </div>
        </div>

        <div class="solution-banner">
          <div class="solution-icon">
            <va-icon name="auto_awesome" size="large" color="primary" />
          </div>
          <div class="solution-content">
            <h4>{{ $t('landing.why_mdm.solution_title') }}</h4>
            <p>{{ $t('landing.why_mdm.solution_desc') }}</p>
          </div>
          <div class="solution-action">
            <va-button
              to="/roi-calculator"
              color="primary"
              size="medium"
              icon-right="trending_up"
            >
              {{ $t('landing.why_mdm.calc_roi_cta') }}
            </va-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. Core Capabilities Section -->
    <section id="features" class="content-section">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">{{ $t('landing.features.badge') }}</span>
          <h2 class="section-title">{{ $t('landing.features.title') }}</h2>
          <p class="section-subtitle">{{ $t('landing.features.subtitle') }}</p>
        </div>

        <div class="features-grid">
          <div class="feature-card">
            <div class="card-icon-box primary">
              <va-icon name="schema" size="medium" color="primary" />
            </div>
            <h3 class="card-title">{{ $t('landing.features.f1_title') }}</h3>
            <p class="card-desc">{{ $t('landing.features.f1_desc') }}</p>
          </div>

          <div class="feature-card">
            <div class="card-icon-box warning">
              <va-icon name="fact_check" size="medium" color="warning" />
            </div>
            <h3 class="card-title">{{ $t('landing.features.f2_title') }}</h3>
            <p class="card-desc">{{ $t('landing.features.f2_desc') }}</p>
          </div>

          <div class="feature-card">
            <div class="card-icon-box success">
              <va-icon name="approval" size="medium" color="success" />
            </div>
            <h3 class="card-title">{{ $t('landing.features.f3_title') }}</h3>
            <p class="card-desc">{{ $t('landing.features.f3_desc') }}</p>
          </div>

          <div class="feature-card">
            <div class="card-icon-box info">
              <va-icon name="account_tree" size="medium" color="info" />
            </div>
            <h3 class="card-title">{{ $t('landing.features.f4_title') }}</h3>
            <p class="card-desc">{{ $t('landing.features.f4_desc') }}</p>
          </div>

          <div class="feature-card">
            <div class="card-icon-box purple">
              <va-icon name="sync_alt" size="medium" color="primary" />
            </div>
            <h3 class="card-title">{{ $t('landing.features.f5_title') }}</h3>
            <p class="card-desc">{{ $t('landing.features.f5_desc') }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. Success Case Studies Section -->
    <section id="cases" class="content-section bg-secondary">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">{{ $t('landing.cases.badge') }}</span>
          <h2 class="section-title">{{ $t('landing.cases.title') }}</h2>
          <p class="section-subtitle">{{ $t('landing.cases.subtitle') }}</p>
        </div>

        <div class="cards-grid-3">
          <div class="card-item case-card">
            <div class="case-metric-badge">{{ $t('landing.cases.c1_metric') }}</div>
            <h3 class="card-title">{{ $t('landing.cases.c1_title') }}</h3>
            <p class="card-desc">{{ $t('landing.cases.c1_desc') }}</p>
          </div>

          <div class="card-item case-card">
            <div class="case-metric-badge">{{ $t('landing.cases.c2_metric') }}</div>
            <h3 class="card-title">{{ $t('landing.cases.c2_title') }}</h3>
            <p class="card-desc">{{ $t('landing.cases.c2_desc') }}</p>
          </div>

          <div class="card-item case-card">
            <div class="case-metric-badge">{{ $t('landing.cases.c3_metric') }}</div>
            <h3 class="card-title">{{ $t('landing.cases.c3_title') }}</h3>
            <p class="card-desc">{{ $t('landing.cases.c3_desc') }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. Security & Trust Section -->
    <section id="trust" class="content-section">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">{{ $t('landing.trust.badge') }}</span>
          <h2 class="section-title">{{ $t('landing.trust.title') }}</h2>
          <p class="section-subtitle">{{ $t('landing.trust.subtitle') }}</p>
        </div>

        <div class="cards-grid-3">
          <div class="card-item trust-card">
            <div class="card-icon-box primary">
              <va-icon name="lock" size="medium" color="primary" />
            </div>
            <h3 class="card-title">{{ $t('landing.trust.t1_title') }}</h3>
            <p class="card-desc">{{ $t('landing.trust.t1_desc') }}</p>
          </div>

          <div class="card-item trust-card">
            <div class="card-icon-box success">
              <va-icon name="link" size="medium" color="success" />
            </div>
            <h3 class="card-title">{{ $t('landing.trust.t2_title') }}</h3>
            <p class="card-desc">{{ $t('landing.trust.t2_desc') }}</p>
          </div>

          <div class="card-item trust-card">
            <div class="card-icon-box info">
              <va-icon name="admin_panel_settings" size="medium" color="info" />
            </div>
            <h3 class="card-title">{{ $t('landing.trust.t3_title') }}</h3>
            <p class="card-desc">{{ $t('landing.trust.t3_desc') }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. FAQ Section -->
    <section id="faq" class="content-section bg-secondary">
      <div class="section-container">
        <div class="section-header">
          <span class="section-badge">{{ $t('landing.faq.badge') }}</span>
          <h2 class="section-title">{{ $t('landing.faq.title') }}</h2>
          <p class="section-subtitle">{{ $t('landing.faq.subtitle') }}</p>
        </div>

        <div class="faq-list">
          <div class="faq-item">
            <h4 class="faq-question">❓ {{ $t('landing.faq.q1') }}</h4>
            <p class="faq-answer">{{ $t('landing.faq.a1') }}</p>
          </div>
          <div class="faq-item">
            <h4 class="faq-question">❓ {{ $t('landing.faq.q2') }}</h4>
            <p class="faq-answer">{{ $t('landing.faq.a2') }}</p>
          </div>
          <div class="faq-item">
            <h4 class="faq-question">❓ {{ $t('landing.faq.q3') }}</h4>
            <p class="faq-answer">{{ $t('landing.faq.a3') }}</p>
          </div>
          <div class="faq-item">
            <h4 class="faq-question">❓ {{ $t('landing.faq.q4') }}</h4>
            <p class="faq-answer">{{ $t('landing.faq.a4') }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 7. Bottom Conversion CTA -->
    <section class="bottom-cta-section">
      <div class="bottom-cta-container">
        <h2 class="bottom-cta-title">{{ $t('landing.cta.title') }}</h2>
        <p class="bottom-cta-subtitle">{{ $t('landing.cta.subtitle') }}</p>
        <div class="bottom-cta-buttons">
          <va-button
            to="/register"
            color="primary"
            size="large"
            icon-right="rocket_launch"
            class="cta-primary-large"
          >
            {{ $t('landing.cta.btn_primary') }}
          </va-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCookie, useOidcAuth } from '#imports'

definePageMeta({
  layout: 'landing'
})

const router = useRouter()
const tokenCookie = useCookie('auth_token')
let loggedIn = ref(false)
try {
  const oidc = useOidcAuth()
  if (oidc && oidc.loggedIn) {
    loggedIn = oidc.loggedIn
  }
} catch (e) {
  // Graceful fallback for non-Nuxt unit test / SSR environment
}

// 클라이언트 측에서 로그인된 상태로 인덱스 진입 시 대시보드로 전환
// 단, 만료된 토큰이 잔존하는 경우 쿠키를 정리하고 랜딩에 머무름
onMounted(() => {
  if (tokenCookie.value) {
    try {
      const payload = JSON.parse(atob(tokenCookie.value.split('.')[1]))
      const nowSec = Math.floor(Date.now() / 1000)
      if (payload.exp && payload.exp > nowSec) {
        router.replace('/dashboard')
        return
      }
    } catch (e) {
      // 토큰 파싱 실패 — 유효하지 않은 토큰
    }
    // 만료되었거나 파싱 실패한 토큰 쿠키 정리
    tokenCookie.value = null
    const refreshCookie = useCookie('refresh_token')
    refreshCookie.value = null
  } else if (loggedIn.value) {
    router.replace('/dashboard')
    return
  }
})
</script>

<style scoped>
.landing-page {
  display: flex;
  flex-direction: column;
  overflow-x: hidden;
}

/* 1. Hero */
.hero-section {
  padding: 5rem 1.5rem 4rem 1.5rem;
  background: radial-gradient(circle at 50% 0%, rgba(37, 99, 235, 0.08) 0%, transparent 70%);
  text-align: center;
}

.hero-container {
  max-width: 1080px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.35rem 0.85rem;
  background: rgba(37, 99, 235, 0.1);
  color: var(--va-primary, #2563eb);
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 700;
  margin-bottom: 1.5rem;
}

.hero-title {
  font-size: 3.2rem;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -0.03em;
  margin: 0 0 1.25rem 0;
  color: var(--va-text-primary, #0f172a);
}

.hero-subtitle {
  font-size: 1.15rem;
  line-height: 1.6;
  color: var(--va-text-secondary, #475569);
  max-width: 780px;
  margin: 0 0 2rem 0;
}

.hero-cta-group {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  justify-content: center;
  margin-bottom: 2rem;
}

.hero-trust-badges {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
  justify-content: center;
  font-size: 0.88rem;
  font-weight: 600;
  color: var(--va-text-secondary, #64748b);
  margin-bottom: 3.5rem;
}

.trust-badge-item {
  display: inline-flex;
  align-items: center;
}

.hero-stats-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.25rem;
  padding: 1.75rem;
  border-radius: 16px;
  border: 1px solid var(--va-background-border, #e2e8f0);
  background: var(--va-background-element, #f8fafc);
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
}

.stat-value {
  font-size: 2.2rem;
  font-weight: 800;
  color: var(--va-primary, #2563eb);
}

.stat-label {
  font-size: 0.85rem;
  color: var(--va-text-secondary, #64748b);
  font-weight: 600;
}

/* Sections Common */
.content-section {
  padding: 5rem 1.5rem;
}

.bg-secondary {
  background: var(--va-background-element, #f8fafc);
}

.section-container {
  max-width: 1140px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 3.5rem;
}

.section-badge {
  text-transform: uppercase;
  font-size: 0.8rem;
  font-weight: 800;
  color: var(--va-primary, #2563eb);
  letter-spacing: 0.08em;
  display: block;
  margin-bottom: 0.5rem;
}

.section-title {
  font-size: 2.2rem;
  font-weight: 800;
  margin: 0 0 0.75rem 0;
  color: var(--va-text-primary, #0f172a);
}

.section-subtitle {
  font-size: 1.05rem;
  color: var(--va-text-secondary, #64748b);
  margin: 0;
}

/* Grids & Cards */
.cards-grid-3 {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

.card-item {
  padding: 2rem;
  border-radius: 14px;
  border: 1px solid var(--va-background-border, #e2e8f0);
  background: var(--va-background-primary, #ffffff);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
}

.card-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.card-icon-box.danger { background: rgba(239, 68, 68, 0.1); }
.card-icon-box.primary { background: rgba(37, 99, 235, 0.1); }
.card-icon-box.warning { background: rgba(245, 158, 11, 0.1); }
.card-icon-box.success { background: rgba(16, 185, 129, 0.1); }
.card-icon-box.info { background: rgba(14, 165, 233, 0.1); }
.card-icon-box.purple { background: rgba(139, 92, 246, 0.1); }

.card-title {
  font-size: 1.15rem;
  font-weight: 700;
  margin: 0;
  color: var(--va-text-primary, #0f172a);
}

.card-desc {
  font-size: 0.92rem;
  line-height: 1.6;
  color: var(--va-text-secondary, #64748b);
  margin: 0;
}

.solution-banner {
  margin-top: 2rem;
  padding: 1.5rem 2rem;
  border-radius: 14px;
  background: rgba(37, 99, 235, 0.08);
  border: 1px solid rgba(37, 99, 235, 0.2);
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.solution-content {
  flex: 1;
}

.solution-content h4 {
  font-size: 1.1rem;
  font-weight: 700;
  margin: 0 0 0.35rem 0;
  color: var(--va-primary, #2563eb);
}

.solution-content p {
  font-size: 0.92rem;
  margin: 0;
  color: var(--va-text-secondary, #475569);
  line-height: 1.5;
}

.solution-action {
  flex-shrink: 0;
}

/* Features Grid */
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  padding: 2rem;
  border-radius: 14px;
  border: 1px solid var(--va-background-border, #e2e8f0);
  background: var(--va-background-primary, #ffffff);
  display: flex;
  flex-direction: column;
  gap: 0.65rem;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
}

/* Case Studies */
.case-card {
  position: relative;
}

.case-metric-badge {
  align-self: flex-start;
  padding: 0.35rem 0.75rem;
  border-radius: 8px;
  background: rgba(16, 185, 129, 0.12);
  color: #059669;
  font-weight: 800;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
}

/* FAQ */
.faq-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 860px;
  margin: 0 auto;
}

.faq-item {
  padding: 1.5rem;
  border-radius: 12px;
  background: var(--va-background-primary, #ffffff);
  border: 1px solid var(--va-background-border, #e2e8f0);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.faq-question {
  font-size: 1.05rem;
  font-weight: 700;
  margin: 0;
  color: var(--va-text-primary, #0f172a);
}

.faq-answer {
  font-size: 0.92rem;
  line-height: 1.6;
  color: var(--va-text-secondary, #64748b);
  margin: 0;
}

/* Bottom CTA */
.bottom-cta-section {
  padding: 5rem 1.5rem;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.05) 0%, rgba(99, 102, 241, 0.1) 100%);
  text-align: center;
}

.bottom-cta-container {
  max-width: 760px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.bottom-cta-title {
  font-size: 2.2rem;
  font-weight: 800;
  margin: 0;
  color: var(--va-text-primary, #0f172a);
}

.bottom-cta-subtitle {
  font-size: 1.1rem;
  color: var(--va-text-secondary, #64748b);
  margin: 0 0 1rem 0;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 2.2rem;
  }
  .section-title {
    font-size: 1.75rem;
  }
}
</style>
