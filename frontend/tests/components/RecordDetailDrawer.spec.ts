import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import RecordDetailDrawer from '../../components/records/RecordDetailDrawer.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      view_original: '원문 보기',
      hide_original: '원문 숨기기',
      btn_save: '저장',
      btn_close: '닫기'
    }
  }
})

const mockCustomFetch = vi.fn().mockResolvedValue({
  id: 'REC-001',
  data: JSON.stringify({ EP_NO: '0000001', EP_NAME: '인치국' })
})

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

describe('RecordDetailDrawer.vue - Encrypted Masking & Domain Reference', () => {
  it('renders view original button for encrypted or masked fields', () => {
    const wrapper = mount(RecordDetailDrawer, {
      props: {
        show: true,
        record: {
          id: 'REC-123',
          RR_NO: '860101-1******'
        },
        fields: [
          {
            id: 'f-1',
            key: 'RR_NO',
            name: { ko: '주민등록번호' },
            type: 'TEXT',
            isEncrypted: true,
            order: 1
          }
        ]
      },
      global: {
        plugins: [i18n],
        stubs: {
          'va-modal': {
            template: '<div><slot /><slot name="footer" /></div>'
          },
          'va-tabs': true,
          'va-tab': true,
          'va-accordion': { template: '<div><slot /></div>' },
          'va-collapse': { template: '<div><slot /></div>' },
          'va-input': true,
          'va-icon': true,
          'va-button': true,
          'UnmaskReasonModal': true,
          'UserProfileModal': true,
          'AgGridVue': true
        }
      }
    })

    expect(wrapper.text()).toContain('원문 보기')
  })

  it('renders domain reference formatted string with [ID] Name', () => {
    const wrapper = mount(RecordDetailDrawer, {
      props: {
        show: true,
        record: {
          id: 'REC-123',
          REF_EMP: 'REC-001'
        },
        domainReferences: {
          REF_EMP: {
            domainInfo: {
              identifierFieldId: 'f-sub-1',
              displayNameFieldId: 'f-sub-2'
            },
            fields: [
              { id: 'f-sub-1', key: 'EP_NO' },
              { id: 'f-sub-2', key: 'EP_NAME' }
            ],
            records: [
              { id: 'REC-001', data: JSON.stringify({ EP_NO: '0000001', EP_NAME: '인치국' }) }
            ]
          }
        },
        fields: [
          {
            id: 'f-ref',
            key: 'REF_EMP',
            name: { ko: '레퍼런스' },
            type: 'DOMAIN_REFERENCE',
            order: 1
          }
        ]
      },
      global: {
        plugins: [i18n],
        stubs: {
          'va-modal': {
            template: '<div><slot /><slot name="footer" /></div>'
          },
          'va-tabs': true,
          'va-tab': true,
          'va-accordion': { template: '<div><slot /></div>' },
          'va-collapse': { template: '<div><slot /></div>' },
          'va-input': {
            template: '<input class="va-input-stub" :value="modelValue" />',
            props: ['modelValue']
          },
          'va-icon': true,
          'va-button': true,
          'UnmaskReasonModal': true,
          'UserProfileModal': true,
          'AgGridVue': true
        }
      }
    })

    const displayVal = (wrapper.vm as any).getDomainRefDisplayName('REF_EMP', 'REC-001')
    expect(displayVal).toBe('[0000001] 인치국')
  })
})
