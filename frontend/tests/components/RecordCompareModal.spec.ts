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
          VaSelect: true,
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
          VaSwitch: true,
          VaSelect: true,
          VaIcon: true
        }
      }
    })

    const vm = wrapper.vm as any
    // empNo values differ: '0000001' vs '0000002'
    expect(vm.isFieldDifferent(sampleFields[0])).toBe(true)
    // dept values match: '개발팀' vs '개발팀'
    expect(vm.isFieldDifferent(sampleFields[2])).toBe(false)
  })

  it('기준 레코드를 변경하면 해당 레코드 기준으로 차이점이 계산되어야 함', () => {
    const records3 = [
      { id: 'rec-1', name: 'A', value: '100' },
      { id: 'rec-2', name: 'B', value: '200' },
      { id: 'rec-3', name: 'A', value: '100' }
    ]
    const fields = [
      { id: 'f1', key: 'name', name: '이름' },
      { id: 'f2', key: 'value', name: '값' }
    ]

    const wrapper = mount(RecordCompareModal, {
      props: { show: true, records: records3, fields },
      global: {
        stubs: { VaModal: { template: '<div><slot/></div>' }, VaBadge: true, VaChip: true, VaButton: true, VaSwitch: true, VaSelect: true, VaIcon: true }
      }
    })

    const vm = wrapper.vm as any
    expect(vm.baselineRecordIndex).toBe(0)
    // Relative to rec-1 (A): rec-2 (B) is different, so isFieldDifferent = true
    expect(vm.isFieldDifferent(fields[0])).toBe(true)

    // Change baseline to rec-3 (index 2)
    vm.baselineRecordIndex = 2
    // rec-3 (A) vs rec-1 (A) and rec-2 (B): still different
    expect(vm.isFieldDifferent(fields[0])).toBe(true)
    expect(vm.isCellDifferentFromBaseline(fields[0], 0)).toBe(false) // rec-1 has same value 'A'
    expect(vm.isCellDifferentFromBaseline(fields[0], 1)).toBe(true)  // rec-2 has different value 'B'
  })

  it('엑셀 내보내기 함수가 비교 데이터를 올바른 형식으로 반환해야 함', () => {
    const wrapper = mount(RecordCompareModal, {
      props: { show: true, records: sampleRecords, fields: sampleFields },
      global: {
        stubs: { VaModal: { template: '<div><slot/></div>' }, VaBadge: true, VaChip: true, VaButton: true, VaSwitch: true, VaSelect: true, VaIcon: true }
      }
    })
    const vm = wrapper.vm as any
    const rows = vm.buildExcelRows()
    // Should have header row + 3 data rows (sampleFields has 3 fields)
    expect(Array.isArray(rows)).toBe(true)
    expect(rows.length).toBe(4)
    // First row is header
    expect(rows[0][0]).toBe('필드')
  })

  it('숫자 타입 필드는 UI에서 천단위 콤마 포맷, 비교 시에는 원본값 기준이어야 함', () => {
    const numFields = [
      { id: 'n1', key: 'price', name: '가격', type: 'NUMBER' },
      { id: 'n2', key: 'ratio', name: '비율', type: 'DECIMAL' },
      { id: 'n3', key: 'label', name: '라벨', type: 'STRING' }
    ]
    const numRecords = [
      { id: 'r1', price: 1234567, ratio: 3.14, label: 'ABC' },
      { id: 'r2', price: 1234567, ratio: 2.71, label: 'ABC' }
    ]
    const wrapper = mount(RecordCompareModal, {
      props: { show: true, records: numRecords, fields: numFields },
      global: {
        stubs: { VaModal: { template: '<div><slot/></div>' }, VaBadge: true, VaChip: true, VaButton: true, VaSwitch: true, VaSelect: true, VaIcon: true }
      }
    })
    const vm = wrapper.vm as any

    // price is same in both records → not different
    expect(vm.isFieldDifferent(numFields[0])).toBe(false)
    // ratio differs → different
    expect(vm.isFieldDifferent(numFields[1])).toBe(true)
    // label is same → not different
    expect(vm.isFieldDifferent(numFields[2])).toBe(false)

    // UI display: NUMBER type should use toLocaleString (has commas for large numbers)
    const displayed = vm.formatFieldValue(numRecords[0], numFields[0])
    expect(displayed).toBe((1234567).toLocaleString())
  })
})
