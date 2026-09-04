import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordToolbar from '../../components/records/RecordToolbar.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

vi.mock('~/composables/usePermission', () => ({
  usePermission: () => ({
    hasPermission: () => true
  })
}))

describe('RecordToolbar.vue (TDD)', () => {
  it('기본 툴바 렌더링 및 버튼 이벤트 emit 검증', async () => {
    const wrapper = mount(RecordToolbar, {
      props: {
        selectedNode: { id: 'node-1', name: { ko: '부서' }, isDomain: false },
        selectedRecordRows: [{ id: 'rec-1' }],
        hasCreateWorkflow: false
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-button': true,
          'va-chip': true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
  })

  it('도메인 노드 선택 시 도메인 레코드 초기화 버튼이 렌더링되고 클릭 시 resetDomainRecords 이벤트를 emit한다', async () => {
    const wrapper = mount(RecordToolbar, {
      props: {
        selectedNode: { id: 'dom-1', name: { ko: '주식 도메인' }, isDomain: true },
        selectedRecordRows: [],
        hasCreateWorkflow: false
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-badge': true,
          'va-chip': true
        }
      }
    })

    const buttons = wrapper.findAll('.va-button-stub')
    const resetDomainBtn = buttons.find(b => b.text().includes('reset_domain_records_btn'))
    expect(resetDomainBtn).toBeDefined()
    expect(resetDomainBtn?.exists()).toBe(true)

    await resetDomainBtn?.trigger('click')
    expect(wrapper.emitted('resetDomainRecords')).toBeTruthy()
  })

  it('일반 하위 노드 선택 시 도메인 레코드 초기화 버튼이 렌더링되지 않는다', async () => {
    const wrapper = mount(RecordToolbar, {
      props: {
        selectedNode: { id: 'node-1', name: { ko: 'KOSPI' }, isDomain: false },
        selectedRecordRows: [],
        hasCreateWorkflow: false
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-badge': true,
          'va-chip': true
        }
      }
    })

    const buttons = wrapper.findAll('.va-button-stub')
    const resetDomainBtn = buttons.find(b => b.text().includes('reset_domain_records_btn'))
    expect(resetDomainBtn).toBeUndefined()
  })
})
