import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordFormModal from '../../components/records/RecordFormModal.vue'

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
    locale: { value: 'ko' }
  })
}))

describe('RecordFormModal.vue - JSON Table Sub-Schema', () => {
  it('renders sub-table grid when JSON field has tableSchema', async () => {
    const tableSchemaOptions = JSON.stringify({
      tableSchema: {
        columns: [
          { key: 'school_name', name: { ko: '학교명', en: 'School Name' }, type: 'TEXT', required: true, width: 150 },
          { key: 'major', name: { ko: '전공', en: 'Major' }, type: 'TEXT', required: false, width: 120 }
        ]
      }
    })

    const wrapper = mount(RecordFormModal, {
      props: {
        show: true,
        isEdit: false,
        nodeLabel: '정규직',
        fields: [
          {
            id: 'f-1',
            key: 'MAJOR_HISTORY',
            name: { ko: '학력 이력', en: 'Major History' },
            type: 'JSON',
            options: tableSchemaOptions,
            order: 1
          }
        ],
        record: {
          MAJOR_HISTORY: [
            { school_name: '한국대학교', major: '컴퓨터공학' }
          ]
        }
      },
      global: {
        mocks: {
          $t: (k: string, p?: any) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /><slot name="footer" /></div>',
            props: ['modelValue']
          },
          'va-tabs': true,
          'va-tab': true,
          'va-accordion': { template: '<div><slot /></div>' },
          'va-collapse': { template: '<div><slot /></div>' },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('total_rows_count')
    expect(wrapper.text()).toContain('add_row')
    expect(wrapper.text()).toContain('학교명')
    expect(wrapper.text()).toContain('전공')
  })

  it('renders multilingual SELECT options in sub-table grid', async () => {
    const tableSchemaOptions = JSON.stringify({
      tableSchema: {
        columns: [
          {
            key: 'degree',
            name: { ko: '학위', en: 'Degree' },
            type: 'SELECT',
            required: true,
            width: 150,
            options: [
              { key: 'BACHELOR', value: 'BACHELOR', label: { ko: '학사', en: 'Bachelor' } },
              { key: 'MASTER', value: 'MASTER', label: { ko: '석사', en: 'Master' } }
            ]
          }
        ]
      }
    })

    const wrapper = mount(RecordFormModal, {
      props: {
        show: true,
        isEdit: false,
        nodeLabel: '정규직',
        fields: [
          {
            id: 'f-2',
            key: 'MAJOR_HISTORY',
            name: { ko: '학력 이력', en: 'Major History' },
            type: 'JSON',
            options: tableSchemaOptions,
            order: 1
          }
        ],
        record: {
          MAJOR_HISTORY: [{ degree: 'BACHELOR' }]
        }
      },
      global: {
        mocks: {
          $t: (k: string, p?: any) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /><slot name="footer" /></div>',
            props: ['modelValue']
          },
          'va-tabs': true,
          'va-tab': true,
          'va-accordion': { template: '<div><slot /></div>' },
          'va-collapse': { template: '<div><slot /></div>' },
          'va-input': true,
          'va-select': {
            template: '<div class="va-select-stub"><span v-for="opt in options" :key="opt.value">{{ opt.text }}</span></div>',
            props: ['options', 'modelValue']
          },
          'va-checkbox': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('학위')
    expect(wrapper.text()).toContain('학사')
    expect(wrapper.text()).toContain('석사')
  })

  it('preserves existing form inputs when domain reference record prop is merged', async () => {
    const wrapper = mount(RecordFormModal, {
      props: {
        show: true,
        isEdit: false,
        nodeLabel: '정규직',
        fields: [
          { id: 'f-1', key: 'EP_NO', name: { ko: '사번' }, type: 'TEXT', order: 1 },
          { id: 'f-2', key: 'EP_NAME', name: { ko: '이름' }, type: 'TEXT', order: 2 },
          { id: 'f-3', key: 'REF_TEST', name: { ko: '레퍼런스' }, type: 'DOMAIN_REFERENCE', order: 3 }
        ],
        record: {}
      },
      global: {
        mocks: {
          $t: (k: string, p?: any) => k
        },
        stubs: {
          'va-modal': {
            template: '<div><slot /><slot name="footer" /></div>',
            props: ['modelValue']
          },
          'va-tabs': true,
          'va-tab': true,
          'va-accordion': { template: '<div><slot /></div>' },
          'va-collapse': { template: '<div><slot /></div>' },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    // 사용자가 사번과 이름을 입력한 상황 시뮬레이션
    ;(wrapper.vm as any).localRecord = {
      EP_NO: '0000001',
      EP_NAME: '인치국'
    }

    // 도메인 참조 레코드 선택 완료 시 props.record 업데이트
    await wrapper.setProps({
      record: { REF_TEST: 'REC-9999' }
    })

    // 기존 입력값(사번, 이름)이 보존되고 REF_TEST가 안전하게 병합되었는지 검증
    expect((wrapper.vm as any).localRecord.EP_NO).toBe('0000001')
    expect((wrapper.vm as any).localRecord.EP_NAME).toBe('인치국')
    expect((wrapper.vm as any).localRecord.REF_TEST).toBe('REC-9999')
  })
})
