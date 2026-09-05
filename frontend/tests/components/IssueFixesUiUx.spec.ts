import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import fs from 'fs'
import path from 'path'
import ChannelConfigModal from '../../components/admin/ChannelConfigModal.vue'
import { formatSummaryVal } from '../../composables/useApprovalEnricher'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('GitHub Issues UI/UX Fixes (TDD Tests)', () => {
  describe('#127: 채널 코드 (channelCode) 관리', () => {
    it('ChannelConfigModal에서 formData.channelCode가 기본 정보에 정상 바인딩되는지 검증', () => {
      const formData = {
        id: 'chan-001',
        name: 'CartBom Integration',
        channelCode: 'CH-CARTBOM-001',
        direction: 'OUTBOUND',
        type: 'WEB_SERVICE',
        active: true,
        isActive: true,
        requiresApproval: false
      }

      const wrapper = mount(ChannelConfigModal, {
        props: {
          modelValue: true,
          isEdit: true,
          formData,
          uiConfig: {
            inboundAuthType: 'NONE',
            inboundSecretToken: '',
            wsUrl: 'https://example.com/api',
            wsMethod: 'POST',
            wsHeaders: [],
            jdbcUrl: '',
            jdbcUser: '',
            jdbcPassword: '',
            jdbcTable: '',
            mqBroker: '',
            mqTopic: ''
          },
          channelNameKo: '카트봄 연동',
          channelNameEn: 'CartBom Integration',
          directionOptions: [],
          typeOptions: [],
          authTypeOptions: [],
          methodOptions: [],
          domains: [],
          nodes: [],
          selectedDomainId: null,
          rawFields: [],
          uiMappingRootPath: '',
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
            'va-input': {
              props: ['modelValue', 'label', 'readonly'],
              template: '<input class="va-input-stub" :value="modelValue" :data-label="label" @input="$emit(\'update:modelValue\', $event.target.value)" />'
            },
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

      const inputs = wrapper.findAll('.va-input-stub')
      const channelCodeInput = inputs.find(i => i.attributes('data-label') === 'integration.channels.channel_code')
      expect(channelCodeInput).toBeDefined()
      expect((channelCodeInput?.element as HTMLInputElement).value).toBe('CH-CARTBOM-001')
    })
  })

  describe('#132: 결재 진행 모니터링 요약 정보의 긴 URL 축약', () => {
    it('요약 정보에 200자 이상의 긴 CDN URL이 포함된 경우 hostname/.../filename 형태로 축약되는지 검증', () => {
      const longCoupangUrl = 'https://thumbnail7.coupangcdn.com/thumbnails/remote/492x492ex/image/retail/images/2023/10/12/10/5/a917234c-6f91-45f8-8255-b46fdcf19760.jpg'
      const formatted = formatSummaryVal(longCoupangUrl)
      expect(formatted).not.toBe(longCoupangUrl)
      expect(formatted).toContain('thumbnail7.coupangcdn.com/.../a917234')
      expect(formatted.endsWith('...')).toBe(true)
    })

    it('일반 짧은 문자열은 그대로 반환되는지 검증', () => {
      expect(formatSummaryVal('일반 텍스트')).toBe('일반 텍스트')
      expect(formatSummaryVal(null, '없음')).toBe('없음')
    })
  })

  describe('#122 & #123 & #131 & #135: i18n 번역 및 오타 정정 검증', () => {
    const readLocaleJson = (relPath: string) => {
      const fullPath = path.resolve(__dirname, '../../i18n/locales', relPath)
      return JSON.parse(fs.readFileSync(fullPath, 'utf-8'))
    }

    it('DQ 규칙에서 오타 "검칙"이 모두 "규칙"으로 수정되었는지 검증', () => {
      const koDq = readLocaleJson('ko/dq.json')
      const dqJsonStr = JSON.stringify(koDq)
      expect(dqJsonStr).not.toContain('품질 검칙')
      expect(dqJsonStr).not.toContain('검칙')
    })

    it('결재함 및 결재 모니터링 부제가 알맞은 설명으로 정의되어 있는지 검증', () => {
      const koCommon = readLocaleJson('ko/common.json')
      expect(koCommon.approval_inbox_desc).toBe('데이터 등록 및 변경에 대한 결재 요청을 검토하고 승인·반려를 처리합니다.')
      expect(koCommon.approval_monitor_desc).toBe('전체 결재 요청의 진행 상태 및 승인·반려 이력을 실시간 모니터링합니다.')
    })

    it('레코드 비교 최소 선택 힌트 문구가 정상 정의되어 있는지 검증', () => {
      const koCommon = readLocaleJson('ko/common.json')
      expect(koCommon.compare_min_selection_hint).toBe('최소 2개 이상의 레코드를 선택해야 비교할 수 있습니다.')
    })
  })
})
