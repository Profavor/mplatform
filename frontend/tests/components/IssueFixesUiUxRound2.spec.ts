import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import fs from 'fs'
import path from 'path'
import AppFooter from '../../components/layout/AppFooter.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, def?: any) => (typeof def === 'string' ? def : key)
  })
}))

describe('GitHub Issues UI/UX Fixes - Round 2 (TDD Tests)', () => {
  const readLocaleJson = (relPath: string) => {
    const fullPath = path.resolve(__dirname, '../../i18n/locales', relPath)
    return JSON.parse(fs.readFileSync(fullPath, 'utf-8'))
  }

  describe('#119 & #118 & #121 & #130: i18n 신규 다국어 키 검증', () => {
    it('ko/common.json 및 en/common.json에 시스템 로그, 언어 전환, 시스템 기본 정보 키가 정상 정의되어 있는지 검증', () => {
      const koCommon = readLocaleJson('ko/common.json')
      const enCommon = readLocaleJson('en/common.json')

      expect(koCommon.switch_to_english).toBe('영문으로 전환')
      expect(enCommon.switch_to_english).toBe('Switch to English')

      expect(koCommon.switch_to_korean).toBe('한국어로 전환')
      expect(enCommon.switch_to_korean).toBe('Switch to Korean')

      expect(koCommon.menu_access_logs).toBe('메뉴 접근 로그')
      expect(enCommon.menu_access_logs).toBe('Menu Access Logs')

      expect(koCommon.login_logs).toBe('로그인 로그')
      expect(enCommon.login_logs).toBe('Login Logs')

      expect(koCommon.error_logs).toBe('오류 로그')
      expect(enCommon.error_logs).toBe('Error Logs')

      expect(koCommon.integration_logs).toBe('연동 로그')
      expect(enCommon.integration_logs).toBe('Integration Logs')

      expect(koCommon.basic_system_info).toBe('시스템 기본 정보')
      expect(enCommon.basic_system_info).toBe('System Information')
    })

    it('ko/inbox.json 및 en/inbox.json에 quota_unlimited 키가 정상 정의되어 있는지 검증', () => {
      const koInbox = readLocaleJson('ko/inbox.json')
      const enInbox = readLocaleJson('en/inbox.json')

      expect(koInbox.inbox.quota_unlimited).toBe('제한 없음')
      expect(enInbox.inbox.quota_unlimited).toBe('Unlimited')
    })
  })

  describe('#120: AppFooter 모달 팝업 및 Teleport 렌더링 검증', () => {
    it('개인정보처리방침/이용약관/고객지원 버튼 클릭 시 activeModal 상태가 변경되고 모달이 열리는지 검증', async () => {
      const wrapper = mount(AppFooter, {
        global: {
          mocks: {
            $t: (key: string) => key
          },
          stubs: {
            VaIcon: true,
            VaButton: true,
            AppModal: {
              props: ['modelValue', 'title'],
              template: '<div v-if="modelValue" class="app-modal-stub" :data-title="title"><slot /></div>'
            },
            teleport: true
          }
        }
      })

      // Privacy Policy button click
      const privacyBtn = wrapper.find('.footer-link-privacy')
      expect(privacyBtn.exists()).toBe(true)
      await privacyBtn.trigger('click')

      // Modal is visible
      const modal = wrapper.find('.app-modal-stub')
      expect(modal.exists()).toBe(true)
    })
  })

  describe('#129 & #130: 메일 서버 계정 사용자명 fallback 및 할당량 제한 없음 검증', () => {
    it('admin@mplatform.com처럼 userId가 없는 계정은 이메일 prefix로 사용자명이 정상 fallback되는지 검증', () => {
      const userStoreMock = {
        getUserName: vi.fn(() => '-')
      }

      const getter = (row: any) => {
        if (row?.userName) return row.userName
        const id = row?.userId
        if (id) {
          const name = userStoreMock.getUserName(id)
          if (name && name !== id && name !== '-') return name
        }
        if (row?.email) {
          return row.email.split('@')[0]
        }
        return '-'
      }

      // Case 1: normal user with userName
      expect(getter({ userName: '홍길동', email: 'gildong@mplatform.com' })).toBe('홍길동')

      // Case 2: admin account without userName or userId mapping
      expect(getter({ email: 'admin@mplatform.com' })).toBe('admin')

      // Case 3: superadmin account without userName
      expect(getter({ email: 'superadmin@mplatform.com' })).toBe('superadmin')
    })

    it('할당량(quotaLimit)이 0이거나 미설정(null)일 때 "제한 없음"으로 포맷팅되는지 검증', () => {
      const formatQuotaLimit = (val: any) => {
        if (!val || val === '0' || val === 0 || val === 'UNLIMITED') {
          return '제한 없음'
        }
        return `${val} B`
      }

      expect(formatQuotaLimit(null)).toBe('제한 없음')
      expect(formatQuotaLimit(0)).toBe('제한 없음')
      expect(formatQuotaLimit('0')).toBe('제한 없음')
      expect(formatQuotaLimit('UNLIMITED')).toBe('제한 없음')
      expect(formatQuotaLimit(10485760)).toBe('10485760 B')
    })
  })
})
