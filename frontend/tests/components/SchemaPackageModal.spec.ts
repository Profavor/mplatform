import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import SchemaPackageModal from '../../components/schema/SchemaPackageModal.vue'

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: (...args: any[]) => mockCustomFetch(...args)
}))

const mockToast = {
  init: vi.fn()
}
vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<Record<string, any>>()
  return {
    ...actual,
    useToast: () => mockToast
  }
})

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: any) => {
      if (params) {
        let res = key
        for (const [k, v] of Object.entries(params)) {
          res += `_${k}:${v}`
        }
        return res
      }
      return key
    },
    te: () => true,
    locale: { value: 'ko' }
  })
}))

describe('SchemaPackageModal.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders export and import tabs properly with i18n keys', () => {
    const wrapper = mount(SchemaPackageModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        domainName: '고객 도메인'
      },
      global: {
        mocks: {
          $t: (k: string, params?: any) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><h1>{{ title }}</h1><slot /></div>',
            props: ['title']
          },
          'va-alert': true,
          'va-card': {
            template: '<div><slot /></div>'
          },
          'va-checkbox': true,
          'va-button': {
            template: '<button><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('export_package')
    expect(wrapper.text()).toContain('import_package')
    expect(wrapper.text()).toContain('package_export_subtext')
    expect(wrapper.text()).toContain('export_download_json')
  })

  it('downloads domain package JSON when export button is clicked', async () => {
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          version: '1.0',
          domain: { name: { ko: '고객 도메인' } },
          nodes: [],
          fields: [],
          workflows: []
        }
      }
    })

    // Mock URL & document methods for download trigger
    global.URL.createObjectURL = vi.fn(() => 'blob:test')
    global.URL.revokeObjectURL = vi.fn()

    const wrapper = mount(SchemaPackageModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        domainName: '고객 도메인'
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
          'va-card': {
            template: '<div><slot /></div>'
          },
          'va-checkbox': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    const buttons = wrapper.findAll('button')
    const exportBtn = buttons.find(b => b.text().includes('export_download_json'))
    expect(exportBtn).toBeDefined()
    await exportBtn?.trigger('click')

    expect(mockCustomFetch).toHaveBeenCalledWith('/domains/domain-123/package/export')
    expect(mockToast.init).toHaveBeenCalledWith(expect.objectContaining({
      message: 'package_download_success',
      color: 'success'
    }))
  })

  it('switches to import tab and sends import request when submitted', async () => {
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          success: true,
          nodeCount: 3,
          fieldCount: 10
        }
      }
    })

    const wrapper = mount(SchemaPackageModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123'
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
          'va-card': {
            template: '<div><slot /></div>'
          },
          'va-checkbox': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    // Click Import Tab
    const importTabBtn = wrapper.findAll('button').find(b => b.text().includes('import_package'))
    await importTabBtn?.trigger('click')

    expect(wrapper.text()).toContain('package_file_select_label')
    expect(wrapper.text()).toContain('import_upload_json')

    // Simulate import package data set
    ;(wrapper.vm as any).importPackageData = {
      domain: { name: { ko: '가져온 도메인' } },
      nodes: [],
      fields: []
    }
    ;(wrapper.vm as any).overwrite = true

    await (wrapper.vm as any).submitImport()

    expect(mockCustomFetch).toHaveBeenCalledWith('/domains/package/import?overwrite=true', expect.objectContaining({
      method: 'POST',
      body: expect.objectContaining({
        domain: { name: { ko: '가져온 도메인' } }
      })
    }))
    expect(wrapper.emitted('imported')).toBeTruthy()
  })

  it('uses domainOptions fallback when domainId is empty and allows changing target domain', async () => {
    mockCustomFetch.mockResolvedValue({
      data: {
        value: {
          version: '1.0',
          domain: { name: { ko: '옵션 도메인' } },
          nodes: [],
          fields: [],
          workflows: []
        }
      }
    })

    const wrapper = mount(SchemaPackageModal, {
      props: {
        modelValue: true,
        domainOptions: [
          { value: 'domain-opt-1', text: '임직원 도메인' },
          { value: 'domain-opt-2', text: '고객 도메인' }
        ]
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
          'va-card': {
            template: '<div><slot /></div>'
          },
          'va-select': true,
          'va-checkbox': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    const buttons = wrapper.findAll('button')
    const exportBtn = buttons.find(b => b.text().includes('export_download_json'))
    await exportBtn?.trigger('click')

    expect(mockCustomFetch).toHaveBeenCalledWith('/domains/domain-opt-1/package/export')
  })
})
