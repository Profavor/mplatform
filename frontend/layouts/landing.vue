<template>
  <div class="landing-layout" :class="{ 'dark-mode': isDark }">
    <!-- Landing Top Sticky Navbar -->
    <header class="landing-navbar">
      <div class="landing-nav-container">
        <!-- Brand Logo -->
        <a href="/" class="landing-brand">
          <va-icon name="account_tree" size="large" color="primary" class="mr-2" />
          <span class="brand-title">{{ $t('landing.nav.brand') }}</span>
        </a>

        <!-- Desktop Navigation Links -->
        <nav class="landing-nav-links">
          <a href="#why-mdm" class="nav-link">{{ $t('landing.nav.why_mdm') }}</a>
          <a href="#features" class="nav-link">{{ $t('landing.nav.features') }}</a>
          <a href="#cases" class="nav-link">{{ $t('landing.nav.cases') }}</a>
          <a href="#trust" class="nav-link">{{ $t('landing.nav.trust') }}</a>
          <a href="#faq" class="nav-link">{{ $t('landing.nav.faq') }}</a>
        </nav>

        <!-- Right Action Controls -->
        <div class="landing-nav-actions">
          <!-- Language Toggle Button -->
          <va-button
            preset="plain"
            size="small"
            class="action-btn"
            :title="currentLocale === 'ko' ? 'Switch to English' : '한국어로 전환'"
            @click="toggleLang"
          >
            <va-icon name="language" size="small" class="mr-1" />
            <span style="font-weight: 700; font-size: 0.82rem;">{{ displayLocale }}</span>
          </va-button>

          <!-- Theme Toggle Button -->
          <va-button
            preset="plain"
            size="small"
            class="action-btn"
            :title="isDark ? '라이트 모드로 전환' : '다크 모드로 전환'"
            @click="toggleTheme"
          >
            <va-icon :name="isDark ? 'light_mode' : 'dark_mode'" size="small" />
          </va-button>

          <!-- Sign In Button -->
          <va-button
            to="/login"
            preset="outline"
            color="primary"
            size="small"
            class="login-btn"
          >
            {{ $t('landing.nav.login') }}
          </va-button>

          <!-- Start Free / Demo CTA Button -->
          <va-button
            to="/register"
            color="primary"
            size="small"
            class="cta-btn"
          >
            {{ $t('landing.nav.start_trial') }}
          </va-button>
        </div>
      </div>
    </header>

    <!-- Main Content Slot -->
    <main class="landing-main">
      <slot />
    </main>

    <!-- Landing Footer -->
    <footer class="landing-footer">
      <div class="footer-container">
        <div class="footer-brand-section">
          <div class="footer-logo">
            <va-icon name="account_tree" size="medium" color="primary" class="mr-2" />
            <strong>{{ $t('landing.nav.brand') }}</strong>
          </div>
          <p class="footer-desc">{{ $t('landing.footer.desc') }}</p>
          <span class="footer-copy">{{ $t('landing.footer.copyright') }}</span>
        </div>

        <div class="footer-links">
          <a href="#why-mdm">{{ $t('landing.nav.why_mdm') }}</a>
          <a href="#features">{{ $t('landing.nav.features') }}</a>
          <a href="#trust">{{ $t('landing.nav.trust') }}</a>
          <a href="#faq">{{ $t('landing.nav.faq') }}</a>
          <NuxtLink to="/login">{{ $t('landing.nav.login') }}</NuxtLink>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useCookie } from '#app'
import { useI18n } from 'vue-i18n'
import { useColors } from 'vuestic-ui'

const { locale, setLocale } = useI18n()
const colors = useColors()
const currentLocale = useCookie('locale', { default: () => 'ko' })
const savedTheme = useCookie('theme', { default: () => 'light' })

const displayLocale = computed(() => {
  const val = currentLocale.value || 'ko'
  return typeof val === 'string' ? val.toUpperCase() : 'KO'
})

const isDark = computed(() => {
  if (colors?.currentPresetName) {
    return colors.currentPresetName.value === 'dark'
  }
  return savedTheme.value === 'dark'
})

const toggleLang = () => {
  const nextLocale = currentLocale.value === 'ko' ? 'en' : 'ko'
  currentLocale.value = nextLocale
  setLocale(nextLocale)
}

const toggleTheme = () => {
  const next = isDark.value ? 'light' : 'dark'
  savedTheme.value = next
  if (colors?.applyPreset) {
    colors.applyPreset(next)
  }
}
</script>

<style scoped>
.landing-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--va-background-primary, #ffffff);
  color: var(--va-text-primary, #1e293b);
  transition: background-color 0.25s ease, color 0.25s ease;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.landing-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--va-background-border, #e2e8f0);
  transition: all 0.2s ease;
}

.dark-mode .landing-navbar {
  background: rgba(15, 23, 42, 0.85);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.landing-nav-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0.85rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1.5rem;
}

.landing-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--va-text-primary, #0f172a);
  letter-spacing: -0.02em;
}

.landing-nav-links {
  display: flex;
  align-items: center;
  gap: 1.75rem;
}

.nav-link {
  text-decoration: none;
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--va-text-secondary, #64748b);
  transition: color 0.15s ease;
}

.nav-link:hover {
  color: var(--va-primary, #2563eb);
}

.landing-nav-actions {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}

.action-btn {
  color: var(--va-text-primary) !important;
}

.landing-main {
  flex: 1;
}

.landing-footer {
  border-top: 1px solid var(--va-background-border, #e2e8f0);
  background: var(--va-background-element, #f8fafc);
  padding: 3rem 1.5rem 2rem 1.5rem;
  margin-top: 4rem;
}

.dark-mode .landing-footer {
  background: #0f172a;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.footer-container {
  max-width: 1240px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 2rem;
}

.footer-brand-section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 420px;
}

.footer-logo {
  display: flex;
  align-items: center;
  font-size: 1.1rem;
}

.footer-desc {
  font-size: 0.85rem;
  color: var(--va-text-secondary, #64748b);
  line-height: 1.5;
  margin: 0;
}

.footer-copy {
  font-size: 0.78rem;
  color: var(--va-text-secondary, #94a3b8);
}

.footer-links {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.footer-links a {
  text-decoration: none;
  font-size: 0.85rem;
  color: var(--va-text-secondary, #64748b);
  transition: color 0.15s ease;
}

.footer-links a:hover {
  color: var(--va-primary, #2563eb);
}

@media (max-width: 860px) {
  .landing-nav-links {
    display: none;
  }
}
</style>
