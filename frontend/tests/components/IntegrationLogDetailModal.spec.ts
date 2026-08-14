import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import IntegrationLogDetailModal from '../../components/admin/IntegrationLogDetailModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('IntegrationLogDetailModal.vue (TDD Component Test)', () => {
  const mockLog = {
    id: 'log-101',
    channel: 'SAP_ERP',
    status: 'FAIL',
    direction: 'INBOUND',
    eventType: 'CUSTOMER_SYNC',
    retryCount: 2,
    createdAt: '2026-08-14 12:00:00',
    errorMessage: 'Connection timeout with SAP ERP Gateway',
    stackTrace: 'java.net.SocketTimeoutException: Read timed out\n  at com.classification.IntegrationService.call()',
    originalPayload: '{"sapCustomerId":"SAP-999","name":"Acme Corp"}',
    mappedPayload: '{"id":null,"name":"Acme Corp"}'
  }

  it('연동 로그 상세 모달 기본 렌더링 및 메트릭/에러 정보 검증', async () => {
    const wrapper = mount(IntegrationLogDetailModal, {
      props: {
        modelValue: true,
        log: mockLog,
        hasPermission: () => true
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-icon': true,
          'va-badge': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
    expect(wrapper.text()).toContain('FAIL')
    expect(wrapper.text()).toContain('CUSTOMER_SYNC')
    expect(wrapper.text()).toContain('Connection timeout with SAP ERP Gateway')
  })

  it('재시도 버튼 클릭 시 retry 이벤트 방출', async () => {
    const wrapper = mount(IntegrationLogDetailModal, {
      props: {
        modelValue: true,
        log: mockLog,
        hasPermission: () => true
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-icon': true,
          'va-badge': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    wrapper.vm.onRetry()
    expect(wrapper.emitted('retry')).toBeTruthy()
    expect(wrapper.emitted('retry')![0]).toEqual(['log-101'])
  })

  it('페이로드 복사 기능 정상 동작 검증', async () => {
    // navigator.clipboard mocking using Object.defineProperty
    const writeTextMock = vi.fn().mockResolvedValue(undefined)
    Object.defineProperty(navigator, 'clipboard', {
      value: {
        writeText: writeTextMock
      },
      writable: true,
      configurable: true
    })

    const wrapper = mount(IntegrationLogDetailModal, {
      props: {
        modelValue: true,
        log: mockLog,
        hasPermission: () => true
      },
      global: {
        stubs: {
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-icon': true,
          'va-badge': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    await wrapper.vm.copyPayload(mockLog.originalPayload, 'original')
    expect(writeTextMock).toHaveBeenCalledWith(mockLog.originalPayload)
    expect(wrapper.vm.copySuccess).toBe('original')
  })
})
