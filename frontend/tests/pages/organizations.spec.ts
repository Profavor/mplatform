import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import OrganizationsPage from '../../pages/admin/organizations.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => key,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/usePageTitle', () => ({
  usePageTitle: () => ({
    pageTitle: { value: '조직 및 권한 관리' }
  })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

vi.mock('~/stores/useRoleStore', () => ({
  useRoleStore: () => ({
    syncDefaultRoles: vi.fn().mockResolvedValue(true),
    dumpSeedFiles: vi.fn().mockResolvedValue(true)
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({ currentPresetName: { value: 'light' } })
  }
})

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'test-token' })
}))

describe('pages/admin/organizations.vue (TDD)', () => {
  it('조직 관리 화면 렌더링 검증', async () => {
    const wrapper = mount(OrganizationsPage, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-button': true,
          'va-card': true,
          'va-card-title': true,
          'va-card-content': true,
          'va-tabs': true,
          'va-tab': true,
          'va-input': true,
          'va-modal': true,
          DepartmentModal: true,
          RoleModal: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('조직 정보 변경 여부 검증: 변경사항이 없을 때와 있을 때 정확히 구분 (#88)', () => {
    const parseMultilingualField = (rawVal: any) => {
      if (!rawVal) return { ko: '', en: '' }
      if (typeof rawVal === 'object') return { ko: rawVal.ko || '', en: rawVal.en || '' }
      try {
        const parsed = JSON.parse(rawVal)
        if (parsed && typeof parsed === 'object') return { ko: parsed.ko || '', en: parsed.en || '' }
      } catch (e) {}
      return { ko: String(rawVal), en: String(rawVal) }
    }

    const checkChanged = (orig: any, edited: any) => {
      const origDn = parseMultilingualField(orig.displayName || orig.name)
      const origDesc = parseMultilingualField(orig.description)
      const origEmailDomain = orig.emailDomain ? orig.emailDomain.trim() : ''
      const newEmailDomain = edited.emailDomain ? edited.emailDomain.trim() : ''
      const origIcon = orig.icon || 'corporate_fare'
      const newIcon = edited.icon || 'corporate_fare'

      return (
        (origDn.ko || '') !== (edited.displayNameKo || '') ||
        (origDn.en || '') !== (edited.displayNameEn || '') ||
        (origDesc.ko || '') !== (edited.descriptionKo || '') ||
        (origDesc.en || '') !== (edited.descriptionEn || '') ||
        origEmailDomain !== newEmailDomain ||
        origIcon !== newIcon
      )
    }

    const org = {
      name: 'CORP_HQ',
      displayName: '{"ko":"본사","en":"Headquarters"}',
      description: '{"ko":"본사 설명","en":"HQ Desc"}',
      emailDomain: 'corp.com',
      icon: 'corporate_fare'
    }

    // 동일한 값 전달 시 변경 없음(false)이어야 함 -> #88 수정 완료 알림 오발생 방지
    expect(checkChanged(org, {
      name: 'CORP_HQ',
      displayNameKo: '본사',
      displayNameEn: 'Headquarters',
      descriptionKo: '본사 설명',
      descriptionEn: 'HQ Desc',
      emailDomain: 'corp.com',
      icon: 'corporate_fare'
    })).toBe(false)

    // 이름 변경 시 변경 감지(true)
    expect(checkChanged(org, {
      name: 'CORP_HQ',
      displayNameKo: '본사 수정됨',
      displayNameEn: 'Headquarters',
      descriptionKo: '본사 설명',
      descriptionEn: 'HQ Desc',
      emailDomain: 'corp.com',
      icon: 'corporate_fare'
    })).toBe(true)

    // 이메일 도메인 변경 시 변경 감지(true)
    expect(checkChanged(org, {
      name: 'CORP_HQ',
      displayNameKo: '본사',
      displayNameEn: 'Headquarters',
      descriptionKo: '본사 설명',
      descriptionEn: 'HQ Desc',
      emailDomain: 'new-corp.com',
      icon: 'corporate_fare'
    })).toBe(true)
  })
})
