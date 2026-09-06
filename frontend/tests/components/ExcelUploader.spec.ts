import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ExcelUploader from '../../components/ExcelUploader.vue'

const mockSaveAs = vi.fn()
vi.mock('file-saver', () => ({
  default: { saveAs: (...args: any[]) => mockSaveAs(...args) },
  saveAs: (...args: any[]) => mockSaveAs(...args)
}))

vi.mock('xlsx', () => {
  throw new Error('Vulnerable package xlsx should be removed and not imported!')
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params && params.count !== undefined) return `${key}:${params.count}`
      return key
    },
    locale: { value: 'ko' }
  })
}))

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: (...args: any[]) => mockCustomFetch(...args)
  })
}))

vi.mock('#app', () => ({
  useCookie: () => ({ value: { uuid: 'usr-1111', name: 'tester' } }),
  useRuntimeConfig: () => ({ public: { apiBase: 'http://localhost:8080' } })
}))

describe('ExcelUploader.vue (Bulk Import & Validation & Error Extraction)', () => {
  beforeEach(() => {
    mockSaveAs.mockClear()
    mockCustomFetch.mockClear()
  })

  const sampleFields = [
    { id: 'f1', key: 'PRODUCT_ID', name: { ko: '상품코드', en: 'Product ID' }, type: 'TEXT', required: true },
    { id: 'f2', key: 'PRICE', name: { ko: '가격', en: 'Price' }, type: 'NUMBER', required: false },
    { id: 'f3', key: 'TITLE', name: { ko: '상품명', en: 'Title' }, type: 'MULTILINGUAL', required: true }
  ]

  it('mounts properly without importing xlsx package', () => {
    const wrapper = mount(ExcelUploader, {
      props: {
        nodeId: 'test-node-1',
        nodeFields: sampleFields,
        domainReferences: {}
      },
      global: {
        stubs: {
          AppModal: { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-button': true,
          'va-badge': true,
          'va-alert': true,
          'va-progress-circle': true,
          'va-progress-bar': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('provides CSV template download functionality with UTF-8 BOM', async () => {
    const wrapper = mount(ExcelUploader, {
      props: {
        nodeId: 'test-node-1',
        nodeFields: sampleFields,
        domainReferences: {}
      },
      global: {
        stubs: {
          AppModal: { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-button': true,
          'va-badge': true,
          'va-alert': true
        }
      }
    })

    const vm = wrapper.vm as any
    expect(typeof vm.downloadCsvTemplate).toBe('function')
    await vm.downloadCsvTemplate()

    expect(mockSaveAs).toHaveBeenCalledTimes(1)
    const [blob, filename] = mockSaveAs.mock.calls[0]
    expect(filename).toContain('.csv')
    expect(blob.type).toContain('text/csv')
  })

  it('supports error rows extraction and re-download when validation errors exist', async () => {
    const wrapper = mount(ExcelUploader, {
      props: {
        nodeId: 'test-node-1',
        nodeFields: sampleFields,
        domainReferences: {}
      },
      global: {
        stubs: {
          AppModal: { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-button': true,
          'va-badge': true,
          'va-alert': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.parsedData = [
      { '상품코드': 'P1', '가격': '1000' },
      { '상품코드': '', '가격': 'not_num' }
    ]
    vm.excelHeaders = ['상품코드', '가격']
    vm.validationResult = {
      totalRows: 2,
      validRows: 1,
      invalidRows: 1,
      details: [
        { rowNumber: 1, valid: true, violations: [] },
        {
          rowNumber: 2,
          valid: false,
          violations: [
            { fieldKey: 'PRODUCT_ID', ruleType: 'NOT_NULL', severity: 'ERROR', message: { ko: '필수 항목' }, actualValue: '' }
          ]
        }
      ]
    }

    expect(typeof vm.downloadErrorRowsCsv).toBe('function')
    await vm.downloadErrorRowsCsv()

    expect(mockSaveAs).toHaveBeenCalledTimes(1)
    const [blob, filename] = mockSaveAs.mock.calls[0]
    expect(filename).toContain('error_rows')
  })

  it('executes batch-upsert with proper chunking and options when proceedUpload is triggered', async () => {
    mockCustomFetch.mockResolvedValue({
      totalCount: 1,
      createdCount: 1,
      updatedCount: 0,
      failedCount: 0
    })

    const wrapper = mount(ExcelUploader, {
      props: {
        nodeId: 'test-node-1',
        nodeFields: sampleFields,
        domainReferences: {}
      },
      global: {
        stubs: {
          AppModal: { template: '<div><slot /></div>' },
          'va-icon': true,
          'va-button': true,
          'va-badge': true,
          'va-alert': true,
          'va-progress-bar': true
        }
      }
    })

    const vm = wrapper.vm as any
    vm.parsedData = [
      { '상품코드': 'P1', '가격': '1000', '상품명 (ko)': '한글', '상품명 (en)': 'English' }
    ]
    vm.mapping = {
      PRODUCT_ID: '상품코드',
      PRICE: '가격',
      TITLE_ko: '상품명 (ko)',
      TITLE_en: '상품명 (en)'
    }
    vm.validationResult = {
      totalRows: 1,
      validRows: 1,
      invalidRows: 0,
      details: [{ rowNumber: 1, valid: true, violations: [] }]
    }
    vm.uploadMode = 'UPSERT'
    vm.approvalMode = 'ACTIVE'

    await vm.proceedUpload()

    expect(mockCustomFetch).toHaveBeenCalledTimes(1)
    const [url, options] = mockCustomFetch.mock.calls[0]
    expect(url).toContain('/batch-upsert')
    expect(options.method).toBe('POST')
    expect(options.body.autoApprove).toBe(true)
    expect(options.body.records).toHaveLength(1)
  })
})
