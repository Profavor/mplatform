import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import SectorGroupModal from '../../components/schema/SectorGroupModal.vue'

const translations: Record<string, string> = {
  manage_sectors_groups: '섹터 및 그룹 관리',
  sectors: '섹터',
  groups: '그룹',
  add_row: '행 추가',
  save: '저장',
  remove_selected: '선택 삭제'
}

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => translations[key] || key
  })
}))

describe('components/schema/SectorGroupModal.vue (TDD)', () => {
  it('타이틀과 탭 라벨이 하드코딩되지 않고 i18n 번역 키를 사용하는지 검증', () => {

    const wrapper = mount(SectorGroupModal, {
      props: {
        modelValue: true,
        sgActiveTab: 0,
        domainSectors: [],
        domainGroups: [],
        sectorColumnDefs: [],
        groupColumnDefs: []
      },
      global: {
        mocks: {
          $t: (key: string) => translations[key] || key,
          t: (key: string) => translations[key] || key
        },
        stubs: {
          AppModal: {
            props: ['title'],
            template: '<div class="app-modal" :data-title="title"><slot /></div>'
          },
          'va-tabs': {
            template: '<div class="va-tabs"><slot name="tabs" /></div>'
          },
          'va-tab': {
            template: '<div class="va-tab"><slot /></div>'
          },
          'va-button': true,
          AgGridVue: true
        }
      }
    })

    const appModal = wrapper.find('.app-modal')
    expect(appModal.attributes('data-title')).toBe('섹터 및 그룹 관리')
    
    const tabs = wrapper.findAll('.va-tab')
    expect(tabs.length).toBeGreaterThanOrEqual(2)
    expect(tabs[0].text()).toBe('섹터')
    expect(tabs[1].text()).toBe('그룹')
  })
})
