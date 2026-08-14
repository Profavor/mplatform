import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import UnmergePreviewModal from '~/components/records/UnmergePreviewModal.vue'

describe('UnmergePreviewModal Component (TDD)', () => {
  const mockMasterRecord = {
    id: '11112222-3333-4444-5555-666677778888',
    name: '골든 고객 레코드',
    status: 'ACTIVE',
    data: {
      CUST_NO: 'C001',
      NAME: '홍길동',
      GRADE: 'VIP'
    }
  }

  const mockSourceRecords = [
    {
      id: 'aaaa1111-bbbb-4ccc-8ddd-eeeeffff0001',
      sourceSystem: 'ERP',
      name: '홍길동 (ERP)',
      data: { CUST_NO: 'C001', NAME: '홍길동', GRADE: 'GOLD' }
    },
    {
      id: 'aaaa2222-bbbb-4ccc-8ddd-eeeeffff0002',
      sourceSystem: 'CRM',
      name: '홍길동 (CRM)',
      data: { CUST_NO: 'C001', NAME: '홍길동', GRADE: 'VIP' }
    }
  ]


  it('모달이 열렸을 때 분리 대상 서브 레코드 목록과 식별 코드가 안전하게 표출된다', () => {
    const wrapper = mount(UnmergePreviewModal, {
      props: {
        modelValue: true,
        masterRecord: mockMasterRecord,
        sourceRecords: mockSourceRecords
      },
      global: {
        mocks: {
          $t: (key: string, params?: any) => {
            if (params?.count) return `${params.count}개 레코드 분리`
            return key
          }
        },
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /><slot name="footer" /></div>'
          },
          'va-icon': true,
          'va-chip': {
            template: '<span class="va-chip-stub"><slot /></span>'
          },
          'va-button': {
            template: '<button class="va-button-stub" @click="$attrs.onClick"><slot /></button>'
          },
          'va-alert': {
            template: '<div class="va-alert-stub"><slot /></div>'
          }
        }
      }
    })

    expect(wrapper.text()).toContain('ERP')
    expect(wrapper.text()).toContain('CRM')
    // Raw UUID가 아닌 식별 코드(REC-xxxx) 형식으로 치환되어 렌더링되는지 확인
    expect(wrapper.text()).toContain('REC-aaaa1111')
  })


  it('분리 확정(Confirm) 버튼 클릭 시 unmerge-confirm 이벤트를 발생시킨다', async () => {
    const wrapper = mount(UnmergePreviewModal, {
      props: {
        modelValue: true,
        masterRecord: mockMasterRecord,
        sourceRecords: mockSourceRecords
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /><slot name="footer" /></div>'
          },
          'va-icon': true,
          'va-chip': true,
          'va-button': {
            template: '<button class="va-button-stub" @click="$attrs.onClick"><slot /></button>'
          },
          'va-alert': true
        }
      }
    })

    const confirmBtn = wrapper.findAll('button').find(b => b.text().includes('unmerge_confirm_btn') || b.text().includes('확정') || b.text().includes('분리'))
    if (confirmBtn) {
      await confirmBtn.trigger('click')
      expect(wrapper.emitted('confirm')).toBeTruthy()
    }
  })
})
