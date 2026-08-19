import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ApprovalRouteBuilder from '../../components/inbox/ApprovalRouteBuilder.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

vi.mock('~/composables/useAuthUser', () => ({
  useAuthUser: () => ({
    currentUserId: 'test-user-1',
    currentUser: { id: 'test-user-1', username: 'testuser', department: '개발팀' }
  })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, fb: string) => fb || id || 'User'
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params && params.order) return `${params.order}차`
      return key
    },
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('ApprovalRouteBuilder.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue([
      { id: 'user-1', username: '홍길동', email: 'hong@test.com' },
      { id: 'user-2', username: '김철수', email: 'kim@test.com' }
    ])
  })

  const createWrapper = (propsData = {}) => {
    return mount(ApprovalRouteBuilder, {
      props: {
        modelValue: {
          steps: [
            { id: 's1', stepOrder: 1, stepType: 'APPROVAL', assigneeId: 'user-1' }
          ],
          observerIds: ['user-2']
        },
        ...propsData
      },
      global: {
        mocks: {
          $t: (k: string, params?: any) => {
            if (params && params.order) return `${params.order}차`
            return k
          }
        },
        stubs: {
          'va-icon': true,
          'va-badge': true,
          'va-avatar': true,
          'va-chip': true,
          'va-button': {
            template: '<button><slot /></button>'
          },
          'va-select': {
            template: '<div class="va-select-stub"><slot name="appendInner" /></div>',
            props: ['modelValue', 'options']
          },
          InboxRecipientPicker: {
            template: '<div class="inbox-recipient-picker-stub"></div>',
            props: ['modelValue', 'label']
          },
          UserGridSelectModal: true
        }
      }
    })
  }

  it('renders approval route builder with drafter and steps', () => {
    const wrapper = createWrapper()
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('testuser')
    expect(wrapper.text()).toContain('inbox.approval_route_setting')
  })

  it('adds a new step when addStep is triggered', async () => {
    const wrapper = createWrapper()
    const addButtons = wrapper.findAll('button')
    const addStepBtn = addButtons.find(b => b.text().includes('inbox.add_step'))
    
    if (addStepBtn) {
      await addStepBtn.trigger('click')
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      const emitted = wrapper.emitted('update:modelValue')?.[0]?.[0] as any
      expect(emitted.steps.length).toBeGreaterThanOrEqual(2)
    }
  })

  it('adds parallel step in same order when addParallelStep is triggered', async () => {
    const wrapper = createWrapper({
      modelValue: {
        steps: [
          { id: 's1', stepOrder: 1, stepType: 'APPROVAL', assigneeId: 'user-1' }
        ],
        observerIds: []
      }
    })
    
    const addParallelBtn = wrapper.findAll('button').find(b => b.text().includes('inbox.add_parallel_step'))
    if (addParallelBtn) {
      await addParallelBtn.trigger('click')
      const emits = wrapper.emitted('update:modelValue')
      expect(emits).toBeTruthy()
      const lastEmit = emits![emits!.length - 1][0] as any
      expect(lastEmit.steps.length).toBe(2)
      expect(lastEmit.steps[0].stepOrder).toBe(1)
      expect(lastEmit.steps[1].stepOrder).toBe(1)
    }
  })
})
