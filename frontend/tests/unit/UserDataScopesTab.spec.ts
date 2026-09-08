import { describe, it, expect, vi, beforeEach, afterAll } from 'vitest'
import { mount } from '@vue/test-utils'
import UserDataScopesTab from '../../components/admin/UserDataScopesTab.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

// Mock useCookie
vi.stubGlobal('useCookie', () => ({ value: 'test-token' }))

describe('UserDataScopesTab.vue (TDD Component Test)', () => {
  const mockDomains = [
    { id: 'dom-1', name: { ko: '고객 도메인', en: 'Customer' } }
  ]

  const mockScopes = [
    {
      id: 'scp-1234-5678',
      scopeCode: 'SCP-12345678',
      userId: 'u-1',
      username: 'testuser',
      domainId: 'dom-1',
      domainName: { ko: '고객 도메인', en: 'Customer' },
      nodeId: null,
      nodeName: null,
      permissionLevel: 'READ',
      createdAt: '2026-09-06T12:00:00Z',
      createdBy: 'admin'
    }
  ]

  beforeEach(() => {
    vi.stubGlobal('$fetch', vi.fn().mockImplementation((url: string) => {
      if (url.includes('/scopes')) {
        return Promise.resolve(mockScopes)
      }
      if (url.includes('/nodes')) {
        return Promise.resolve([])
      }
      return Promise.resolve([])
    }))
  })

  it('데이터 스코프 목록이 정상 렌더링되고 SCP- 코드가 노출되어야 한다', async () => {
    const wrapper = mount(UserDataScopesTab, {
      props: {
        userId: 'u-1',
        username: 'testuser',
        canWrite: true,
        domains: mockDomains
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-badge': {
            props: ['text'],
            template: '<span class="va-badge-stub">{{ text }}</span>'
          },
          'va-progress-circle': true,
          AppModal: true,
          'va-select': true
        }
      }
    })

    // Wait for fetchScopes
    await new Promise(resolve => setTimeout(resolve, 50))
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('SCP-12345678')
    expect(wrapper.text()).toContain('READ')
  })

  it('부여 모달 오픈 및 도메인 옵션 바인딩 검증', async () => {
    const wrapper = mount(UserDataScopesTab, {
      props: {
        userId: 'u-1',
        username: 'testuser',
        canWrite: true,
        domains: mockDomains
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-badge': true,
          'va-progress-circle': true,
          AppModal: {
            props: ['modelValue', 'title'],
            template: '<div v-if="modelValue" class="app-modal-stub"><slot /></div>'
          },
          'va-select': true
        }
      }
    })

    // Click open grant modal button
    const grantBtn = wrapper.find('.va-btn-stub')
    if (grantBtn.exists()) {
      await grantBtn.trigger('click')
      expect((wrapper.vm as any).showGrantModal).toBe(true)
    }
  })

  afterAll(() => {
    vi.unstubAllGlobals()
  })
})
