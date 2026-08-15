import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordTimeMachineModal from '../../components/records/RecordTimeMachineModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('RecordTimeMachineModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          recordId: 'rec-1',
          recordCode: 'REC-0001',
          v1: 1,
          v2: 2,
          fieldDiffs: [
            { fieldKey: 'name', fieldName: '이름', v1Value: '홍길동', v2Value: '홍길동', diffStatus: 'UNCHANGED' },
            { fieldKey: 'phone', fieldName: '전화번호', v1Value: '010-1234-5678', v2Value: '010-9999-8888', diffStatus: 'MODIFIED' }
          ],
          allVersions: [
            { version: 1, changeType: 'INITIAL' },
            { version: 2, changeType: 'UPDATE' }
          ]
        }
      }
    })
  })

  it('renders time-machine modal properly', () => {
    const wrapper = mount(RecordTimeMachineModal, {
      props: {
        modelValue: true,
        recordId: 'rec-1'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><h1>{{ title }}</h1><slot /></div>',
            props: ['title']
          },
          'va-alert': true,
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-select': true,
          'va-icon': true,
          'va-badge': true,
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('time_machine')
  })
})
