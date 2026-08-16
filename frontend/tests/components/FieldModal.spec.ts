import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import FieldModal from '../../components/schema/FieldModal.vue'

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
    }
  })
}))

describe('FieldModal.vue - Table Sub-Schema Builder', () => {
  it('renders table schema settings when type is JSON and emits add-table-column', async () => {
    const wrapper = mount(FieldModal, {
      props: {
        modelValue: true,
        isEditMode: true,
        newField: {
          name: { ko: '학력 이력', en: 'Major History' },
          hint: { ko: '', en: '' },
          key: 'MAJOR_HISTORY',
          type: 'JSON',
          required: false,
          order: 1
        },
        newFieldTableColumns: [],
        fieldTypes: [{ label: 'JSON 문서', value: 'JSON' }]
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
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-icon': true,
          'ag-grid-vue': true,
          'va-alert': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('table_schema_settings')
    expect(wrapper.text()).toContain('no_table_columns_defined')

    const buttons = wrapper.findAll('button')
    const addColBtn = buttons.find(b => b.text().includes('add_column'))
    expect(addColBtn).toBeDefined()
    await addColBtn?.trigger('click')

    expect(wrapper.emitted('add-table-column')).toBeTruthy()
  })

  it('renders defined table columns and emits remove-table-column when delete button clicked', async () => {
    const wrapper = mount(FieldModal, {
      props: {
        modelValue: true,
        isEditMode: true,
        newField: {
          name: { ko: '학력 이력', en: 'Major History' },
          hint: { ko: '', en: '' },
          key: 'MAJOR_HISTORY',
          type: 'JSON',
          required: false,
          order: 1
        },
        newFieldTableColumns: [
          { key: 'school_name', name: { ko: '학교명', en: 'School Name' }, type: 'TEXT', required: true, width: 150, optionsStr: '' },
          { key: 'degree', name: { ko: '학위', en: 'Degree' }, type: 'SELECT', required: false, width: 120, optionsStr: '학사,석사,박사' }
        ],
        fieldTypes: [{ label: 'JSON 문서', value: 'JSON' }]
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
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-icon': true,
          'ag-grid-vue': true,
          'va-alert': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('table_schema_settings')
    expect(wrapper.text()).not.toContain('no_table_columns_defined')
  })
})
