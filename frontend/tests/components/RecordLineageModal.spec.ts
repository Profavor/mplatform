import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordLineageModal from '../../components/RecordLineageModal.vue'

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockResolvedValue({
      recordId: 'rec-1234',
      recordCode: 'REC-1234',
      recordNameObj: { ko: '홍길동', en: 'Gildong' },
      empNo: 'EMP001',
      nodes: [
        { id: 'SRC-1', label: 'Source System: Portal', type: 'SOURCE', timestamp: '2026-08-01 10:00:00' },
        { id: 'REC-1234', label: 'Golden Master Record', type: 'RECORD', timestamp: '2026-08-01 11:00:00', details: { status: 'ACTIVE' } }
      ],
      edges: [
        { source: 'SRC-1', target: 'REC-1234', relationship: 'EVOLVED_TO' }
      ]
    })
  })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

describe('RecordLineageModal.vue', () => {
  it('renders correctly when modelValue is true and recordId is provided', async () => {
    const wrapper = mount(RecordLineageModal, {
      props: {
        modelValue: true,
        recordId: 'rec-1234'
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': { template: '<div><slot /></div>' },
          'va-progress-circle': true,
          'va-badge': true,
          'va-alert': true,
          'va-card': true,
          'va-card-content': true,
          'va-chip': true,
          'va-icon': true,
          'va-button': true,
          'v-chart': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
