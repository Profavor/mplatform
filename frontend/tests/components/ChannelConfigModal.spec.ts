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

  it('validate 메서드가 정상 노출되고 호출 가능한지 검증', async () => {
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
            template: '<form @submit.prevent="$emit(\'submit\')"><slot /></form>',
            methods: {
              validate: () => true
            }
          },
          'va-input': true,
          'va-select': true,
          'va-checkbox': true,
          'va-chip': true,
          'va-icon': true,
          MultilingualInput: true,
          AgGridVue: true,
          'va-button': true
        }
      }
    })

    expect(typeof (wrapper.vm as any).validate).toBe('function')
    const isValid = (wrapper.vm as any).validate()
    expect(isValid).toBe(true)
  })

  it('API 응답으로 active: true만 전달된 경우에도 활성화 상태가 정상 반영되고 체크박스 토글 시 active와 isActive가 동기화되는지 검증', async () => {
    // API 응답 형태: active만 있고 isActive는 없음
    const formData: any = {
      id: 'channel-123',
      name: 'KRX Inbound',
      direction: 'INBOUND',
      type: 'WEB_SERVICE',
      active: true,
      requiresApproval: false,
      domainId: 'domain-1',
      nodeId: 'node-1'
    }
    const uiConfig = createMockUiConfig()

    const wrapper = mount(ChannelConfigModal, {
      props: {
        modelValue: true,
        isEdit: true,
        formData: formData,
        uiConfig: uiConfig,
        channelNameKo: 'KRX 인바운드',
        channelNameEn: 'KRX Inbound',
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
          'va-checkbox': {
            props: ['modelValue', 'label'],
            template: '<input type="checkbox" class="va-checkbox-stub" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />'
          },
          'va-chip': true,
          'va-icon': true,
          MultilingualInput: true,
          AgGridVue: true,
          'va-button': true
        }
      }
    })

    const checkbox = wrapper.find<HTMLInputElement>('.va-checkbox-stub')
    expect(checkbox.exists()).toBe(true)
    expect(checkbox.element.checked).toBe(true)

    // 체크박스 해제 이벤트 발생
    await checkbox.setValue(false)
    expect(formData.active).toBe(false)
    expect(formData.isActive).toBe(false)

    // 다시 체크 이벤트 발생
    await checkbox.setValue(true)
    expect(formData.active).toBe(true)
    expect(formData.isActive).toBe(true)
  })
})
