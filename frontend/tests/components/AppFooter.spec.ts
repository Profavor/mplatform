import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import AppFooter from '../../components/layout/AppFooter.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      footer: {
        system_name: 'Domain Governance System',
        system_desc: 'Enterprise Master Data & Domain Governance Platform',
        copyright: '© {year} Domain Governance System. All rights reserved.',
        status_operational: '모든 시스템 정상 가동 중',
        privacy_policy: '개인정보처리방침',
        terms_of_service: '서비스 이용약관',
        documentation: '시스템 가이드 / API',
        support: '고객 지원 / 헬프데스크',
        github_repo: 'GitHub 저장소',
        environment: '엔터프라이즈 에디션',
        version: '버전',
        privacy_modal_title: '개인정보 처리방침 안내',
        privacy_modal_content: '도메인 거버넌스 시스템은 개인정보보호법 등 관련 법령에 따라 사용자의 개인정보를 안전하게 보호하며, 마스터 데이터 거버넌스 및 접근 통제 정책에 부합하도록 엄격히 관리됩니다.',
        terms_modal_title: '서비스 이용약관 안내',
        terms_modal_content: '본 플랫폼의 모든 데이터 및 분류체계 자산은 인가된 사용자 및 거버넌스 승인 정책에 따라 열람, 수정, 승인 처리되어야 하며, 인가되지 않은 무단 반출 및 조작은 금지됩니다.',
        support_modal_title: '시스템 문의 및 기술 지원',
        support_modal_content: '시스템 이용 중 오류 발생, 권한 신청 문의 또는 연동 파이프라인 장애 접수는 사내 거버넌스 운영팀 또는 GitHub Issue Tracker를 통해 문의해 주시기 바랍니다.'
      }
    }
  }
})

// Mock useRuntimeConfig
vi.stubGlobal('useRuntimeConfig', () => ({
  public: {
    appVersion: 'v1.1.6',
    repositoryUrl: 'https://github.com/Profavor/mplatform'
  }
}))

describe('AppFooter.vue', () => {
  const defaultGlobal = {
    plugins: [i18n],
    stubs: {
      VaIcon: {
        template: '<span class="va-icon-stub"><slot /></span>'
      },
      VaBadge: {
        template: '<span class="va-badge-stub"><slot /></span>'
      },
      VaButton: {
        template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
      },
      AppModal: {
        props: ['modelValue', 'title', 'icon'],
        template: '<div v-if="modelValue" class="app-modal-stub" :data-title="title"><slot /></div>'
      }
    }
  }

  it('renders copyright with current year and system branding', () => {
    const wrapper = mount(AppFooter, {
      global: defaultGlobal
    })

    const currentYear = new Date().getFullYear()
    expect(wrapper.text()).toContain(String(currentYear))
    expect(wrapper.text()).toContain('Domain Governance System')
    expect(wrapper.text()).toContain('모든 시스템 정상 가동 중')
  })

  it('renders GitHub repository link with correct href and attributes', () => {
    const wrapper = mount(AppFooter, {
      global: defaultGlobal
    })

    const gitLink = wrapper.find('a.github-repo-link')
    expect(gitLink.exists()).toBe(true)
    expect(gitLink.attributes('href')).toBe('https://github.com/Profavor/mplatform')
    expect(gitLink.attributes('target')).toBe('_blank')
    expect(gitLink.attributes('rel')).toContain('noopener')
  })

  it('renders quick policy links (Privacy Policy, Terms of Service, Support)', () => {
    const wrapper = mount(AppFooter, {
      global: defaultGlobal
    })

    expect(wrapper.text()).toContain('개인정보처리방침')
    expect(wrapper.text()).toContain('서비스 이용약관')
    expect(wrapper.text()).toContain('고객 지원 / 헬프데스크')
  })

  it('opens modal dialog when a policy link is clicked', async () => {
    const wrapper = mount(AppFooter, {
      global: defaultGlobal
    })

    // Initially modals are closed
    expect(wrapper.find('.app-modal-stub').exists()).toBe(false)

    // Click Privacy Policy button
    const privacyBtn = wrapper.find('.footer-link-privacy')
    expect(privacyBtn.exists()).toBe(true)
    await privacyBtn.trigger('click')

    // Modal should now be visible with correct title
    const modal = wrapper.find('.app-modal-stub')
    expect(modal.exists()).toBe(true)
    expect(modal.attributes('data-title')).toBe('개인정보 처리방침 안내')
  })
})
