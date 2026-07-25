import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import RecordCompareModal from '~/components/records/RecordCompareModal.vue'

describe('RecordCompareModal Component', () => {

  const sampleFields = [
    { id: 'f1', key: 'empNo', name: '사번', type: 'STRING' },
    { id: 'f2', key: 'name', name: '이름', type: 'MULTILINGUAL' },
    { id: 'f3', key: 'dept', name: '부서', type: 'STRING' }
  ]

  const sampleRecords = [
    { id: 'rec-1', empNo: '0000001', name: '홍길동', dept: '개발팀' },
    { id: 'rec-2', empNo: '0000002', name: '김철수', dept: '개발팀' }
  ]

  it('컴포넌트가 성공적으로 마운트되어야 함', () => {
    const wrapper = mount(RecordCompareModal, {
      props: {
        show: true,
        records: sampleRecords,
        fields: sampleFields
      },
      global: {
        stubs: {
          VaModal: { template: '<div><slot/><slot name="footer"/></div>' },
          VaBadge: true,
          VaChip: true,
          VaButton: true,
          VaSwitch: true,
          VaIcon: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)
    expect(wrapper.text()).toContain('0000001')
    expect(wrapper.text()).toContain('0000002')
  })

  it('차이점이 있는 필드(empNo, name)와 일치하는 필드(dept)를 정확하게 구분해야 함', () => {
    const wrapper = mount(RecordCompareModal, {
      props: {
        show: true,
        records: sampleRecords,
        fields: sampleFields
      },
      global: {
        stubs: {
          VaModal: { template: '<div><slot/></div>' },
          VaBadge: true,
          VaChip: true,
          VaButton: true,
          VaSwitch: true
        }
      }
    })

    const vm = wrapper.vm as any
    // empNo values differ: '0000001' vs '0000002'
    expect(vm.isFieldDifferent(sampleFields[0])).toBe(true)
    // dept values match: '개발팀' vs '개발팀'
    expect(vm.isFieldDifferent(sampleFields[2])).toBe(false)
  })
})
