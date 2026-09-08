import { describe, it, expect, vi, beforeEach, afterAll } from 'vitest'
import { mount } from '@vue/test-utils'
import ColumnMaskingPoliciesTab from '../../components/admin/ColumnMaskingPoliciesTab.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

// Mock useCookie
vi.stubGlobal('useCookie', () => ({ value: 'test-token' }))

describe('ColumnMaskingPoliciesTab.vue (TDD Component Test)', () => {
  const mockDomains = [
    { id: 'dom-1', name: { ko: '고객 도메인', en: 'Customer' } }
  ]

  const mockDepartments = [
    { id: 'dept-1', name: '고객지원팀' }
  ]

  const mockPolicies = [
    {
      id: 'pol-9999-aaaa',
      policyCode: 'POL-9999aaaa',
      targetType: 'DEPARTMENT',
      targetId: 'dept-1',
      targetName: '고객지원팀',
      domainId: 'dom-1',
      domainName: { ko: '고객 도메인', en: 'Customer' },
      fieldKey: 'phone',
      maskingAction: 'UNMASK',
      createdAt: '2026-09-06T12:00:00Z',
      createdBy: 'admin'
    }
  ]

  beforeEach(() => {
    vi.stubGlobal('$fetch', vi.fn().mockImplementation((url: string) => {
      if (url.includes('/masking-policies')) {
        return Promise.resolve(mockPolicies)
      }
      return Promise.resolve([])
    }))
  })

  it('마스킹 정책 목록이 렌더링되고 POL- 코드 및 필드명이 표시되어야 한다', async () => {
    const wrapper = mount(ColumnMaskingPoliciesTab, {
      props: {
        canWrite: true,
        domains: mockDomains,
        departments: mockDepartments
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
          'va-select': true,
          'va-input': true
        }
      }
    })

    // Wait for fetchPolicies
    await new Promise(resolve => setTimeout(resolve, 50))
    await wrapper.vm.$nextTick()

    expect(wrapper.text()).toContain('POL-9999aaaa')
    expect(wrapper.text()).toContain('phone')
    expect(wrapper.text()).toContain('고객지원팀')
  })

  it('신규 마스킹 정책 등록 모달 오픈 동작 검증', async () => {
    const wrapper = mount(ColumnMaskingPoliciesTab, {
      props: {
        canWrite: true,
        domains: mockDomains,
        departments: mockDepartments
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
          'va-select': true,
          'va-input': true
        }
      }
    })

    const buttons = wrapper.findAll('.va-btn-stub')
    for (const btn of buttons) {
      if (btn.text().includes('create_masking_policy')) {
        await btn.trigger('click')
        break
      }
    }

    expect((wrapper.vm as any).showCreateModal).toBe(true)
  })

  afterAll(() => {
    vi.unstubAllGlobals()
  })
})
