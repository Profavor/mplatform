import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ExcelUploader from '../../components/ExcelUploader.vue'
import ExcelJS from 'exceljs'

vi.mock('xlsx', () => {
  throw new Error('Vulnerable package xlsx should be removed and not imported!')
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: null }),
  useRuntimeConfig: () => ({ public: { apiBase: 'http://localhost:8080' } })
}))

describe('ExcelUploader.vue (Without vulnerable xlsx)', () => {
  it('mounts properly without importing xlsx package', () => {
    const wrapper = mount(ExcelUploader, {
      props: {
        nodeId: 'test-node-1',
        nodeFields: [
          { id: '1', key: 'NAME', name: { ko: '이름', en: 'Name' }, type: 'TEXT', required: true }
        ],
        domainReferences: {}
      },
      global: {
        stubs: {
          'va-modal': { template: '<div><slot /></div>' },
          'va-button': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })
})
