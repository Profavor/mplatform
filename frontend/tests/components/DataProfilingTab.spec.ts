import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import DataProfilingTab from '../../components/schema/DataProfilingTab.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: () => false,
    locale: { value: 'ko' }
  })
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => Promise.resolve({
    data: {
      value: {
        domainId: 'domain-1',
        totalRecords: 10,
        fieldProfiles: [
          {
            fieldKey: 'price',
            fieldName: '가격',
            fieldType: 'NUMBER',
            nullRate: 0.0,
            nullCount: 0,
            uniquenessRatio: 90.0,
            distinctCount: 9,
            minValue: 100,
            maxValue: 500,
            avgValue: 250
          }
        ],
        outliers: []
      }
    }
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: () => '2026-08-15 12:00:00'
}))

describe('DataProfilingTab.vue', () => {
  it('renders profiling statistics cards properly', async () => {
    const wrapper = mount(DataProfilingTab, {
      props: {
        domainId: 'domain-1'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-card': {
            template: '<div class="va-card"><slot /></div>'
          },
          'va-card-title': {
            template: '<div><slot /></div>'
          },
          'va-card-content': {
            template: '<div><slot /></div>'
          },
          'va-badge': true,
          'va-progress-bar': true,
          'va-button': true,
          'va-alert': true,
          'va-icon': true
        }
      }
    })

    await wrapper.vm.$nextTick()
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('profiling_title')
  })
})
