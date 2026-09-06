import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import DataLineageModal from '../../components/lineage/DataLineageModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

const mockLineageResponse = {
  recordId: '11111111-2222-3333-4444-555555555555',
  recordCode: 'REC-11111111',
  stages: [
    { stage: 'SOURCE', label: '원천 시스템', order: 1, nodeCount: 1, status: 'HEALTHY' },
    { stage: 'INBOUND_PIPELINE', label: '수집 파이프라인', order: 2, nodeCount: 1, status: 'HEALTHY' },
    { stage: 'MASTER_RECORD', label: '마스터 레코드', order: 3, nodeCount: 1, status: 'HEALTHY' },
    { stage: 'OUTBOUND_PIPELINE', label: '전파 파이프라인', order: 4, nodeCount: 1, status: 'ERROR' },
    { stage: 'DOWNSTREAM_CONSUMER', label: '다운스트림 소비자', order: 5, nodeCount: 1, status: 'ERROR' }
  ],
  nodes: [
    {
      id: 'SRC-1',
      label: 'Source System: ERP_SYSTEM',
      type: 'SOURCE',
      stage: 'SOURCE',
      healthStatus: 'HEALTHY',
      timestamp: '2026-09-06 10:00:00'
    },
    {
      id: 'INB-1',
      label: 'Inbound Pipeline: ERP Inbound Adapter',
      type: 'INBOUND',
      stage: 'INBOUND_PIPELINE',
      healthStatus: 'HEALTHY',
      timestamp: '2026-09-06 10:00:00',
      mappingRules: [
        { sourceField: 'emp_name', targetField: 'name', expression: "#jsonPath($, '$.emp_name')" },
        { sourceField: 'emp_no', targetField: 'code', expression: "#jsonPath($, '$.emp_no')" }
      ]
    },
    {
      id: 'REC-11111111',
      label: 'Master Record (홍길동)',
      type: 'RECORD',
      stage: 'MASTER_RECORD',
      healthStatus: 'HEALTHY',
      timestamp: '2026-09-06 10:05:00',
      details: {
        changedBy: 'admin',
        version: 1,
        changedFields: ['name', 'code']
      }
    },
    {
      id: 'OUT-1',
      label: 'Outbound Pipeline: KRX 공시 연계',
      type: 'OUTBOUND',
      stage: 'OUTBOUND_PIPELINE',
      healthStatus: 'ERROR',
      anomalyReason: 'Connection timed out to KRX Endpoint',
      timestamp: '2026-09-06 10:10:00'
    },
    {
      id: 'CONS-1',
      label: 'Downstream Consumer: KRX Endpoint',
      type: 'CONSUMER',
      stage: 'DOWNSTREAM_CONSUMER',
      healthStatus: 'ERROR',
      anomalyReason: 'Connection timed out to KRX Endpoint',
      timestamp: '2026-09-06 10:10:00'
    }
  ],
  edges: [
    { source: 'SRC-1', target: 'INB-1', relationship: 'INGESTED_VIA' },
    { source: 'INB-1', target: 'REC-11111111', relationship: 'MASTERED_TO' },
    { source: 'REC-11111111', target: 'OUT-1', relationship: 'DISPATCHED_TO' },
    { source: 'OUT-1', target: 'CONS-1', relationship: 'CONSUMED_BY' }
  ],
  channelConsumption: [
    {
      channelId: 'chn-krx',
      channelName: 'KRX 공시 연계',
      direction: 'OUTBOUND',
      totalSent: 5,
      successCount: 3,
      failCount: 2,
      lastDispatchedAt: '2026-09-06 10:10:00'
    }
  ]
}

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: vi.fn(() => Promise.resolve({
    data: ref(mockLineageResponse)
  }))
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  formatWithTimezone: (d: string) => d
}))

import { flushPromises } from '@vue/test-utils'

describe('DataLineageModal.vue (TDD Component Test)', () => {
  it('5단계 파이프라인 스테이지가 렌더링되고 노드 개수가 올바르게 표시되어야 한다', async () => {
    const wrapper = mount(DataLineageModal, {
      props: {
        modelValue: true,
        recordId: '11111111-2222-3333-4444-555555555555'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          AppModal: {
            template: '<div><slot /></div>'
          },
          'va-alert': {
            template: '<div><slot /></div>'
          },
          'va-inner-loading': {
            template: '<div><slot /></div>'
          },
          'va-badge': {
            template: '<span class="va-badge"><slot />{{ text }}</span>',
            props: ['text', 'color']
          },
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    await flushPromises()

    // 5단계 스테이지 라벨 검증
    expect(wrapper.text()).toContain('pipeline_stages')
    expect(wrapper.text()).toContain('STEP 1')
    expect(wrapper.text()).toContain('STEP 2')
    expect(wrapper.text()).toContain('STEP 3')
    expect(wrapper.text()).toContain('STEP 4')
    expect(wrapper.text()).toContain('STEP 5')

    // 이상 노드 감지 검증
    expect(wrapper.text()).toContain('Connection timed out to KRX Endpoint')
  })

  it('Inbound 노드를 클릭하면 SpEL 매핑 규칙 테이블이 노출되어야 한다', async () => {
    const wrapper = mount(DataLineageModal, {
      props: {
        modelValue: true,
        recordId: '11111111-2222-3333-4444-555555555555'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          AppModal: { template: '<div><slot /></div>' },
          'va-alert': { template: '<div><slot /></div>' },
          'va-inner-loading': { template: '<div><slot /></div>' },
          'va-badge': { template: '<span>{{ text }}</span>', props: ['text'] },
          'va-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
        }
      }
    })

    await flushPromises()

    // Inbound Node 선택 (클릭)
    const inboundNodeEl = wrapper.find('.lineage-node-card[data-node-id="INB-1"]')
    expect(inboundNodeEl.exists()).toBe(true)
    await inboundNodeEl.trigger('click')
    await flushPromises()

    // SpEL Mapping Rules 확인
    expect(wrapper.text()).toContain('mapping_rules')
    expect(wrapper.text()).toContain('name')
    expect(wrapper.text()).toContain('#jsonPath($, \'$.emp_name\')')
  })

  it('채널 소비 통계(channel_consumption)가 렌더링되어야 한다', async () => {
    const wrapper = mount(DataLineageModal, {
      props: {
        modelValue: true,
        recordId: '11111111-2222-3333-4444-555555555555'
      },
      global: {
        mocks: {
          $t: (k: string) => k
        },
        stubs: {
          AppModal: { template: '<div><slot /></div>' },
          'va-alert': { template: '<div><slot /></div>' },
          'va-inner-loading': { template: '<div><slot /></div>' },
          'va-badge': { template: '<span>{{ text }}</span>', props: ['text'] },
          'va-button': { template: '<button @click="$emit(\'click\')"><slot /></button>' }
        }
      }
    })

    await flushPromises()

    // Channel consumption 통계 확인
    expect(wrapper.text()).toContain('channel_consumption')
    expect(wrapper.text()).toContain('KRX 공시 연계')
    expect(wrapper.text()).toContain('5건')
  })
})
