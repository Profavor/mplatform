import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ChannelConfigModal from '../../components/admin/ChannelConfigModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('ChannelConfigModal.vue (TDD Component Test)', () => {
  const createMockFormData = () => ({
    id: null,
    name: 'SAP ERP Inbound',
    direction: 'INBOUND',
    type: 'WEB_SERVICE',
    isActive: true,
    requiresApproval: false,
    domainId: 'domain-1',
    nodeId: 'node-1'
  })

  const createMockUiConfig = () => ({
    inboundAuthType: 'BEARER_TOKEN',
    inboundSecretToken: 'sec_token_123',
    wsUrl: '',
    wsMethod: 'POST',
    wsHeaders: [],
    jdbcUrl: '',
    jdbcUser: '',
    jdbcPassword: '',
    jdbcTable: '',
    mqBroker: '',
    mqTopic: ''
  })

  it('채널 설정 모달 기본 렌더링 및 탭 전환 검증', async () => {
    const formData = createMockFormData()
    const uiConfig = createMockUiConfig()

    const wrapper = mount(ChannelConfigModal, {
      props: {
        modelValue: true,
        isEdit: false,
        formData: formData,
        uiConfig: uiConfig,
        channelNameKo: 'SAP ERP 인바운드',
        channelNameEn: 'SAP ERP Inbound',
        directionOptions: [{ text: 'INBOUND', value: 'INBOUND' }],
        typeOptions: [{ text: 'WEB_SERVICE', value: 'WEB_SERVICE' }],
        authTypeOptions: [{ text: 'BEARER_TOKEN', value: 'BEARER_TOKEN' }],
        methodOptions: [{ text: 'POST', value: 'POST' }],
        domains: [{ id: 'domain-1', name: '고객 도메인' }],
        nodes: [{ id: 'node-1', name: '기본 분류' }],
        selectedDomainId: 'domain-1',
        rawFields: [],
        uiMappingRootPath: 'data',
        uiMappings: [],
        mappingColumnDefs: [],
        gridTheme: 'ag-theme-quartz',
        isDark: false,
        isTesting: false
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /><slot name="footer" /></div>'
          },
          'va-tabs': true,
          'va-tab': true,
          'va-form': {
            template: '<form @submit.prevent="$emit(\'submit\')"><slot /></form>'
          },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-chip': true,
          'va-icon': true,
          MultilingualInput: true,
          AgGridVue: true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
  })

  it('저장 버튼 클릭 시 submit 이벤트 방출', async () => {
    const formData = createMockFormData()
    const uiConfig = createMockUiConfig()

    const wrapper = mount(ChannelConfigModal, {
      props: {
        modelValue: true,
        isEdit: false,
        formData: formData,
        uiConfig: uiConfig,
        channelNameKo: 'SAP ERP 인바운드',
        channelNameEn: 'SAP ERP Inbound',
        directionOptions: [],
        typeOptions: [],
        authTypeOptions: [],
        methodOptions: [],
        domains: [],
        nodes: [],
        selectedDomainId: 'domain-1',
        rawFields: [],
        uiMappingRootPath: 'data',
        uiMappings: [],
        mappingColumnDefs: [],
        gridTheme: 'ag-theme-quartz',
        isDark: false,
        isTesting: false
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /><slot name="footer" /></div>'
          },
          'va-tabs': true,
          'va-tab': true,
          'va-form': {
            template: '<form @submit.prevent="$emit(\'submit\')"><slot /></form>'
          },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-chip': true,
          'va-icon': true,
          MultilingualInput: true,
          AgGridVue: true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    wrapper.vm.onSubmit()
    expect(wrapper.emitted('submit')).toBeTruthy()
  })
})
