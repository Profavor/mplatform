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

let mockProfilingReport = {
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
  outliers: [] as any[]
}

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => Promise.resolve({
    data: {
      value: mockProfilingReport
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

  it('limits previewOutliers to at most 50 items when large list is returned', async () => {
    mockProfilingReport.outliers = Array.from({ length: 300 }, (_, i) => ({
      fieldKey: `col_${i}`,
      value: 99999,
      reason: 'IQR 이상치'
    }))

    const wrapper = mount(DataProfilingTab, {
      props: {
        domainId: 'domain-1'
      },
      global: {
        mocks: {
          $t: (k: string, params?: any) => (params?.count ? `${k}: ${params.count}` : k)
        },
        stubs: {
          'va-inner-loading': { template: '<div><slot /></div>' },
          'va-card': { template: '<div class="va-card"><slot /></div>' },
          'va-card-title': { template: '<div><slot /></div>' },
          'va-card-content': { template: '<div><slot /></div>' },
          'va-badge': true,
          'va-progress-bar': true,
          'va-button': true,
          'va-alert': { template: '<div class="va-alert"><slot /></div>' },
          'va-icon': true
        }
      }
    })

    await wrapper.vm.$nextTick()
    await (wrapper.vm as any).fetchProfiling()
    await wrapper.vm.$nextTick()

    // 300개 이상치 중 DOM 렌더링용 미리보기는 50개로 제한되어야 함
    expect((wrapper.vm as any).previewOutliers.length).toBe(50)
    const rows = wrapper.findAll('tbody tr')
    expect(rows.length).toBe(50)
  })
})

