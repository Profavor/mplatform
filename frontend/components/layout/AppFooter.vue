<template>
  <footer class="app-footer" :class="{ 'dark-footer': isDark }">
    <div class="footer-inner">
      <!-- Top Row / Left: Branding & Status & Version -->
      <div class="footer-top-row">
        <div class="brand-title-wrap">
          <div class="brand-icon-box">
            <va-icon name="hub" size="small" color="primary" />
          </div>
          <div class="brand-text">
            <span class="brand-name font-bold">{{ $t('footer.system_name') }}</span>
            <span class="brand-desc text-secondary">{{ $t('footer.system_desc') }}</span>
          </div>
        </div>

        <!-- Badges & Status in top row for high visibility -->
        <div class="footer-meta-section">
          <!-- Live Status Indicator -->
          <div class="system-status-indicator" :title="$t('footer.status_operational')">
            <span class="status-pulse-dot"></span>
            <span class="status-text font-medium">{{ $t('footer.status_operational') }}</span>
          </div>

          <!-- Badges -->
          <div class="footer-badges-group">
            <span class="footer-badge env-badge">
              <va-icon name="verified_user" size="12px" class="mr-1" />
              {{ $t('footer.environment') }}
            </span>
            <span v-if="appVersion" class="footer-badge version-badge">
              {{ appVersion }}
            </span>
          </div>
        </div>
      </div>

      <!-- Bottom Row / Center: Quick Links & GitHub Button & Copyright -->
      <div class="footer-bottom-row">
        <div class="footer-links-section">
          <a
            :href="repositoryUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="github-repo-link footer-link-btn"
            :title="$t('footer.github_repo')"
          >
            <va-icon name="code" size="small" class="mr-1" />
            <span>{{ $t('footer.github_repo') }}</span>
            <va-icon name="open_in_new" size="14px" class="ml-1 icon-external" />
          </a>
          <span class="footer-separator">•</span>
          <button
            type="button"
            class="footer-link-btn footer-link-privacy"
            @click="activeModal = 'privacy'"
          >
            {{ $t('footer.privacy_policy') }}
          </button>
          <span class="footer-separator">•</span>
          <button
            type="button"
            class="footer-link-btn footer-link-terms"
            @click="activeModal = 'terms'"
          >
            {{ $t('footer.terms_of_service') }}
          </button>
          <span class="footer-separator">•</span>
          <button
            type="button"
            class="footer-link-btn footer-link-support"
            @click="activeModal = 'support'"
          >
            {{ $t('footer.support') }}
          </button>
        </div>

        <div class="copyright-text text-secondary">
          {{ $t('footer.copyright', { year: currentYear }) }}
        </div>
      </div>
    </div>

    <!-- Modals for Policies and Support -->
    <AppModal
      v-model="showPrivacyModal"
      :title="$t('footer.privacy_modal_title')"
      icon="security"
      hide-default-actions
    >
      <div class="footer-modal-body">
        <div class="modal-intro-card">
          <va-icon name="lock" color="primary" size="large" class="mr-3" />
          <div>
            <div class="font-bold text-base">{{ $t('footer.privacy_policy') }}</div>
            <div class="text-secondary text-sm">{{ $t('footer.system_name') }} Data Protection Standard</div>
          </div>
        </div>
        <p class="modal-desc-content">
          {{ $t('footer.privacy_modal_content') }}
        </p>
      </div>
    </AppModal>

    <AppModal
      v-model="showTermsModal"
      :title="$t('footer.terms_modal_title')"
      icon="gavel"
      hide-default-actions
    >
      <div class="footer-modal-body">
        <div class="modal-intro-card">
          <va-icon name="policy" color="warning" size="large" class="mr-3" />
          <div>
            <div class="font-bold text-base">{{ $t('footer.terms_of_service') }}</div>
            <div class="text-secondary text-sm">Enterprise Governance & Compliance Rules</div>
          </div>
        </div>
        <p class="modal-desc-content">
          {{ $t('footer.terms_modal_content') }}
        </p>
      </div>
    </AppModal>

    <AppModal
      v-model="showSupportModal"
      :title="$t('footer.support_modal_title')"
      icon="support_agent"
      hide-default-actions
    >
      <div class="footer-modal-body">
        <div class="modal-intro-card">
          <va-icon name="help_outline" color="success" size="large" class="mr-3" />
          <div>
            <div class="font-bold text-base">{{ $t('footer.support') }}</div>
            <div class="text-secondary text-sm">24/7 Enterprise Tech Support & Issue Tracking</div>
          </div>
        </div>
        <p class="modal-desc-content">
          {{ $t('footer.support_modal_content') }}
        </p>
        <div class="modal-action-row mt-4">
          <a
            :href="repositoryUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="git-button-link"
          >
            <va-icon name="bug_report" size="small" class="mr-1" />
            <span>GitHub Issue Tracker</span>
          </a>
        </div>
      </div>
    </AppModal>
  </footer>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useColors } from 'vuestic-ui'
import AppModal from '~/components/common/AppModal.vue'

const config = useRuntimeConfig()
const colors = useColors()
const currentPresetName = colors?.currentPresetName
const isDark = computed(() => currentPresetName?.value === 'dark')

const appVersion = computed(() => config?.public?.appVersion || '')
const repositoryUrl = computed(() => config?.public?.repositoryUrl || 'https://github.com/Profavor/mplatform')
const currentYear = computed(() => new Date().getFullYear())

const activeModal = ref(null)

const showPrivacyModal = computed({
  get: () => activeModal.value === 'privacy',
  set: (val) => { if (!val) activeModal.value = null }
})

const showTermsModal = computed({
  get: () => activeModal.value === 'terms',
  set: (val) => { if (!val) activeModal.value = null }
})

const showSupportModal = computed({
  get: () => activeModal.value === 'support',
  set: (val) => { if (!val) activeModal.value = null }
})
</script>

<style scoped>
.app-footer {
  width: 100%;
  flex-shrink: 0 !important;
  min-height: fit-content;
  margin-top: auto;
  padding: 1.25rem 1.5rem 1.5rem 1.5rem;
  border-top: 1px solid var(--va-background-border, rgba(0, 0, 0, 0.08));
  background: var(--va-background-element, rgba(255, 255, 255, 0.95));
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  transition: all 0.25s ease;
  box-sizing: border-box;
}

.dark-footer {
  border-top-color: rgba(255, 255, 255, 0.08);
  background: rgba(17, 24, 39, 0.9);
}

.footer-inner {
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

/* Top Row */
.footer-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
  padding-bottom: 0.75rem;
  border-bottom: 1px dashed var(--va-background-border, rgba(0, 0, 0, 0.06));
}

.dark-footer .footer-top-row {
  border-bottom-color: rgba(255, 255, 255, 0.06);
}

.brand-title-wrap {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.brand-icon-box {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--va-background-secondary, #f0f4f8);
}

.brand-name {
  font-size: 0.95rem;
  color: var(--va-text-primary, #111827);
  letter-spacing: -0.01em;
}

.brand-desc {
  font-size: 0.75rem;
  margin-left: 0.4rem;
  opacity: 0.75;
}

/* Bottom Row */
.footer-bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.copyright-text {
  font-size: 0.78rem;
  opacity: 0.8;
}

/* Links Section */
.footer-links-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.footer-link-btn {
  background: none;
  border: none;
  padding: 0.3rem 0.5rem;
  font-size: 0.82rem;
  font-weight: 600;
  color: var(--va-text-secondary, #4b5563);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
  display: inline-flex;
  align-items: center;
  text-decoration: none;
}

.footer-link-btn:hover {
  color: var(--va-primary, #2563eb);
  background: rgba(37, 99, 235, 0.08);
}

.footer-separator {
  color: var(--va-text-secondary, #9ca3af);
  font-size: 0.75rem;
  opacity: 0.5;
}

.github-repo-link {
  color: var(--va-primary, #2563eb);
  font-weight: 700;
  background: rgba(37, 99, 235, 0.08);
  border: 1px solid rgba(37, 99, 235, 0.2);
}

.github-repo-link:hover {
  background: rgba(37, 99, 235, 0.16);
}

.icon-external {
  opacity: 0.7;
}

/* Meta Section */
.footer-meta-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.system-status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.25rem 0.65rem;
  border-radius: 20px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.25);
  font-size: 0.78rem;
  color: #10b981;
}

.status-pulse-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: #10b981;
  box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  animation: pulse-green 2s infinite cubic-bezier(0.66, 0, 0, 1);
}

@keyframes pulse-green {
  0% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(16, 185, 129, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(16, 185, 129, 0);
  }
}

.footer-badges-group {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.footer-badge {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.55rem;
  border-radius: 6px;
  font-size: 0.72rem;
  font-weight: 700;
  font-family: monospace;
}

.env-badge {
  background: var(--va-background-secondary, #f3f4f6);
  color: var(--va-text-secondary, #4b5563);
  border: 1px solid var(--va-background-border, rgba(0, 0, 0, 0.08));
}

.version-badge {
  background: rgba(37, 99, 235, 0.1);
  color: var(--va-primary, #2563eb);
  border: 1px solid rgba(37, 99, 235, 0.2);
}

/* Modal Content Styles */
.footer-modal-body {
  padding: 0.5rem 0.25rem 1rem 0.25rem;
  min-width: 380px;
  max-width: 520px;
}

.modal-intro-card {
  display: flex;
  align-items: center;
  padding: 0.85rem 1rem;
  border-radius: 10px;
  background: var(--va-background-secondary, #f8fafc);
  margin-bottom: 1rem;
  border: 1px solid var(--va-background-border, rgba(0, 0, 0, 0.05));
}

.modal-desc-content {
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--va-text-primary, #374151);
  word-break: keep-all;
}

.git-button-link {
  display: inline-flex;
  align-items: center;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  background: var(--va-primary, #2563eb);
  color: white;
  text-decoration: none;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.2s ease;
}

.git-button-link:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* Responsive adjustments */
@media (max-width: 900px) {
  .app-footer {
    padding: 1rem 1rem 3.5rem 1rem; /* Clear floating chat button */
  }
  .footer-top-row {
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
  .footer-bottom-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  .brand-desc {
    display: none;
  }
}

@media (max-width: 600px) {
  .app-footer {
    padding: 1rem 1rem 4rem 1rem;
  }
  .footer-top-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.6rem;
  }
  .footer-links-section {
    font-size: 0.78rem;
    gap: 0.35rem;
  }
  .footer-modal-body {
    min-width: 260px;
  }
}
</style>
