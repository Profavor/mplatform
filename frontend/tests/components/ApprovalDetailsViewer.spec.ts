import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import ApprovalDetailsViewer from '~/components/ApprovalDetailsViewer.vue'

vi.mock('#app', () => ({
  useCookie: () => ({ value: 'ko' })
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

// Mock global customFetch for component unit tests
export const mockCustomFetch = vi.fn().mockImplementation(() => Promise.resolve([]))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: (url: string, opts?: any) => mockCustomFetch(url, opts)
  })
}))

// @ts-ignore
globalThis.$fetch = vi.fn().mockResolvedValue([])

describe('ApprovalDetailsViewer Component - RECORD_UPDATE Filtering Test', () => {
  it('RECORD_UPDATE 시 변경되지 않은 영문 이름은 제외하고 새 파일 추가 항목만 표출되어야 함', () => {
    const mockRequest = {
      id: 'req-1',
      targetType: 'RECORD_UPDATE',
      changes: JSON.stringify({
        before: {
          file: null,
          name: { ko: '인유하', en: 'Test233' }
        },
        after: {
          file: '["/api/files/download/test.xlsx?name=%EB%A0%EC%BD%94%EB%93%9C%EB%B9%84%EA%B5%9F.xlsx"]',
          name: { ko: '인유하', en: 'Test233' }
        }
      }),
      steps: [
        { stepOrder: 0, stepType: 'DRAFT', assigneeName: 'profavor.manager', status: 'APPROVED' },
        { stepOrder: 1, stepType: 'APPROVAL', assigneeName: 'profavor', status: 'PENDING' }
      ]
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: {
        request: mockRequest
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaBadge: true,
          VaButton: true,
          VaChip: true,
          ApprovalSteps: true,
          VaModal: true,
          VaInput: true,
          'va-modal': true,
          'va-input': true
        }
      }
    })

    const text = wrapper.text()
    // 1. 새 파일 항목(file)은 포함되어야 함
    expect(text).toContain('test.xlsx')

    // 2. 변경되지 않은 영문 이름(Test233)은 변경 항목(수정됨)에 표출되지 않아야 함
    expect(text).not.toContain('Test233')
  })

  it('SCHEMA_FIELD_UPDATE 시 스키마 전용 카드 UI와 필드 속성 비교표가 표출되어야 함', () => {
    const mockSchemaRequest = {
      id: 'req-schema-1',
      targetType: 'SCHEMA_FIELD_UPDATE',
      targetId: 'domain-uuid-123',
      changes: JSON.stringify({
        fieldId: 'field-uuid-456',
        request: {
          name: { ko: '입사일', en: 'Join Date' },
          key: 'JOIN_DATE',
          type: 'DATE',
          required: true,
          order: 4
        },
        before: {
          name: { ko: '입사일', en: 'Join Date' },
          key: 'JOIN_DATE',
          type: 'DATE',
          required: false,
          order: 4
        }
      }),
      steps: [
        { stepOrder: 1, stepType: 'APPROVAL', assigneeRole: 'DOMAIN_EDITOR', status: 'PENDING' }
      ]
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: {
        request: mockSchemaRequest
      },
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          VaIcon: true,
          VaBadge: true,
          VaButton: true,
          VaChip: true,
          VaModal: true,
          VaInput: true,
          'va-modal': true,
          'va-input': true
        }
      }
    })

    const text = wrapper.text()
    expect(text).toContain('입사일')
    expect(text).toContain('JOIN_DATE')
    expect(text).toContain('schema_change_comparison')
  })

  it('마스킹된 주민등록번호(860104-1******)가 정상적으로 표출되어야 함', () => {
    const mockMaskedRequest = {
      id: 'req-masked-1',
      targetType: 'RECORD_UPDATE',
      changes: JSON.stringify({
        before: { jumin: '-' },
        after: { jumin: '860104-1******' }
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: { request: mockMaskedRequest },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    expect(wrapper.text()).toContain('860104-1******')
  })

  it('_MASK_CONTACT_EMAIL 등 내부 메타 키는 결재 내역에 노출되지 않아야 함', () => {
    const mockRequest = {
      id: 'req-meta-key-1',
      targetType: 'RECORD_UPDATE',
      changes: JSON.stringify({
        before: {
          _MASK_CONTACT_EMAIL: null,
          contact_email: 'test@old.com'
        },
        after: {
          _MASK_CONTACT_EMAIL: 'r***@naver.com',
          contact_email: 'test@new.com'
        }
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: { request: mockRequest },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    const text = wrapper.text()
    expect(text).not.toContain('_MASK_CONTACT_EMAIL')
    expect(text).toContain('test@new.com')
  })

  it('변경 전후 값이 동일한 필드(예: 니가 나를 모르는데)는 diff에 노출되지 않아야 함', () => {
    const mockRequest = {
      id: 'req-same-val-1',
      targetType: 'RECORD_UPDATE',
      changes: JSON.stringify({
        before: {
          MEMO: '니가 나를 모르는데',
          PHONE: '010-1111-2222'
        },
        after: {
          MEMO: '니가 나를 모르는데',
          PHONE: '010-9999-8888'
        }
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: { request: mockRequest },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    const text = wrapper.text()
    expect(text).not.toContain('니가 나를 모르는데')
    expect(text).toContain('010-9999-8888')
  })

  it('HTML <p> 태그 차이만 있고 실제 텍스트가 동일한 경우(<p>니가 나를 모르는데</p> vs 니가 나를 모르는데) diff에 노출되지 않아야 함', () => {
    const mockRequest = {
      id: 'req-html-tag-1',
      targetType: 'RECORD_UPDATE',
      changes: JSON.stringify({
        before: {
          MEMO: '<p>니가 나를 모르는데</p>',
          TITLE: '이전 제목'
        },
        after: {
          MEMO: '니가 나를 모르는데',
          TITLE: '새로운 제목'
        }
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: { request: mockRequest },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    const text = wrapper.text()
    expect(text).not.toContain('니가 나를 모르는데')
    expect(text).toContain('새로운 제목')
  })

  it('INBOUND_MERGE 시 이전 데이터(before)와 신규 데이터(after)의 변경 속성 Diff가 정상 표출되어야 함', () => {
    const mockMergeRequest = {
      id: 'req-inbound-merge-1',
      targetType: 'INBOUND_MERGE',
      isIntegration: true,
      sourceSystem: 'SPRING_BATCH',
      changes: JSON.stringify({
        before: {
          ticker_code: '034220',
          stock_name: 'LG디스플레이',
          current_price: '10000',
          market: 'KOSPI'
        },
        after: {
          ticker_code: '034220',
          stock_name: 'LG디스플레이',
          current_price: '12500',
          market: 'KOSPI'
        }
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: { request: mockMergeRequest },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    const text = wrapper.text()
    // 1. 변경된 필드(current_price)의 이전 값과 이후 값이 모두 포함되어야 함
    expect(text).toContain('10000')
    expect(text).toContain('12500')
    // 2. 변경되지 않은 ticker_code는 필터링되어 diff에 나타나지 않음
    expect(text).not.toContain('034220')
  })

  it('request.changes가 reactive 객체로 전달되어도 원본 객체를 변이시키지 않고 정상 렌더링되어야 함', () => {
    const rawBefore = { ticker_code: '034220', current_price: '10000' }
    const rawAfter = { ticker_code: '034220', current_price: '12500' }
    const changesObj = {
      before: rawBefore,
      after: rawAfter,
      changedFields: ['current_price']
    }

    const mockReactiveRequest = {
      id: 'req-reactive-1',
      targetType: 'INBOUND_MERGE',
      isIntegration: true,
      changes: changesObj,
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: { request: mockReactiveRequest },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    const text = wrapper.text()
    expect(text).toContain('10000')
    expect(text).toContain('12500')
    // 원본 changesObj.before가 그대로 보존되어야 함
    expect(changesObj.before).toBe(rawBefore)
  })

  it('연계 이력(BATCH_MERGE)에서 변경된 필드 키가 대문자(FOREIGN_OWNERSHIP_RATIO)이더라도 fieldNameMap을 통해 다국어 라벨로 정상 표출되어야 함', async () => {
    const mockFields = [
      {
        id: 'field-uuid-1',
        key: 'foreign_ownership_ratio',
        name: { ko: '외국인 지분율(%)', en: 'Foreign Ownership Ratio (%)' },
        type: 'NUMBER',
        fieldGroup: {
          id: 'group-1',
          name: { ko: '투자자별 매매/지분', en: 'Investor Trading' },
          sector: { id: 'sector-1', name: { ko: '투자 지표', en: 'Investment Indicators' }, sortOrder: 1 },
          sortOrder: 1
        }
      },
      {
        id: 'field-uuid-2',
        key: 'margin_balance_shares',
        name: { ko: '신용잔고주수', en: 'Margin Balance Shares' },
        type: 'NUMBER',
        fieldGroup: null
      }
    ]

    mockCustomFetch.mockImplementation((url: string) => {
      if (url.includes('/fields/effective')) {
        return Promise.resolve(mockFields)
      }
      return Promise.resolve([])
    })

    const mockIntegrationRequest = {
      id: 'req-integration-1',
      targetType: 'BATCH_MERGE',
      isIntegration: true,
      nodeId: 'node-uuid-1',
      changes: JSON.stringify({
        before: {
          FOREIGN_OWNERSHIP_RATIO: 27.16,
          MARGIN_BALANCE_SHARES: 373348
        },
        after: {
          FOREIGN_OWNERSHIP_RATIO: 27.19,
          MARGIN_BALANCE_SHARES: 257534
        },
        changedFields: ['FOREIGN_OWNERSHIP_RATIO', 'MARGIN_BALANCE_SHARES']
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: {
        request: mockIntegrationRequest,
        nodeId: 'node-uuid-1'
      },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    await flushPromises()
    await wrapper.vm.$nextTick()

    const text = wrapper.text()
    expect(text).toContain('외국인 지분율(%)')
    expect(text).toContain('신용잔고주수')
  })

  it('changedFields가 소문자이고 before/after에 대문자/혼합 키가 존재하더라도 각 속성은 1개 행으로만 렌더링(중복 방지)되어야 함', async () => {
    const mockFields = [
      {
        id: 'field-uuid-1',
        key: 'current_price',
        name: { ko: '최근 기준가/종가', en: 'Current Price' },
        type: 'NUMBER',
        fieldGroup: null
      }
    ]

    mockCustomFetch.mockImplementation((url: string) => {
      if (url.includes('/fields/effective')) {
        return Promise.resolve(mockFields)
      }
      return Promise.resolve([])
    })

    const mockRequest = {
      id: 'req-dup-test-1',
      targetType: 'BATCH_MERGE',
      isIntegration: true,
      nodeId: 'node-uuid-1',
      changes: JSON.stringify({
        before: {
          CURRENT_PRICE: 8970,
          MARGIN_BALANCE_SHARES: 373348
        },
        after: {
          CURRENT_PRICE: 8900,
          MARGIN_BALANCE_SHARES: 257534
        },
        changedFields: ['current_price', 'margin_balance_shares']
      }),
      steps: []
    }

    const wrapper = mount(ApprovalDetailsViewer, {
      props: {
        request: mockRequest,
        nodeId: 'node-uuid-1'
      },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: { VaIcon: true, VaBadge: true, VaButton: true, VaChip: true, ApprovalSteps: true, VaModal: true, VaInput: true, 'va-modal': true, 'va-input': true }
      }
    })

    await flushPromises()
    await wrapper.vm.$nextTick()

    // 1. 최근 기준가/종가 라벨이 화면에 정확히 1번만 등장해야 함 (중복 없음)
    const matchesPrice = wrapper.text().match(/최근 기준가\/종가/g)
    expect(matchesPrice).toHaveLength(1)

    // 2. margin_balance_shares 또는 MARGIN_BALANCE_SHARES 또한 1번만 등장해야 함
    const matchesMargin = wrapper.text().match(/margin_balance_shares/gi)
    expect(matchesMargin).toHaveLength(1)
  })
})


