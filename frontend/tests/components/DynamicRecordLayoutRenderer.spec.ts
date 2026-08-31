// @vitest-environment happy-dom
import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import DynamicRecordLayoutRenderer from '../../components/records/DynamicRecordLayoutRenderer.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      view_original: '원문보기',
      hide_original: '원문숨기기',
      click_to_edit: '클릭하여 수정',
      yes: '예',
      no: '아니오',
      records: {
        click_to_edit: '클릭하여 수정',
        multilingual_placeholder_ko: '한국어 명칭 입력',
        multilingual_placeholder_en: '영어 명칭 입력'
      }
    }
  }
})

describe('DynamicRecordLayoutRenderer - 암호화 필드 보안 마스킹 및 복호화 UI (TDD)', () => {
  const sampleLayout = {
    id: 'layout_2d_test',
    cols: 12,
    rowHeight: 40,
    widgets: [
      {
        id: 'w1',
        type: 'FIELD',
        fieldKey: 'resident_number',
        w: 6,
        h: 1
      },
      {
        id: 'w2',
        type: 'FIELD',
        fieldKey: 'credit_card',
        w: 6,
        h: 2
      }
    ]
  }

  const sampleFields = [
    {
      key: 'resident_number',
      name: { ko: '주민등록번호', en: 'Resident Number' },
      type: 'TEXT',
      isEncrypted: true
    },
    {
      key: 'credit_card',
      name: { ko: '신용카드번호', en: 'Credit Card Number' },
      type: 'TEXT',
      isEncrypted: true
    }
  ]

  const sampleRecord = {
    id: 'rec-100',
    resident_number: '900101-1******',
    credit_card: '********'
  }

  it('복호화되지 않은 암호화 필드는 마스킹 값과 함께 자물쇠 아이콘 및 [원문보기] 버튼이 표시되어야 한다', () => {
    const wrapper = mount(DynamicRecordLayoutRenderer, {
      props: {
        layoutConfig: sampleLayout,
        fields: sampleFields,
        record: sampleRecord,
        isEditing: false,
        decryptedValues: {},
        decryptRemainingTime: {},
        decryptingFields: {}
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaIcon: true,
          vaBadge: true,
          vaChip: true,
          vaSwitch: true,
          vaInput: true,
          vaSelect: true
        }
      }
    })

    // 1. 주민등록번호 위젯에 마스킹된 텍스트가 표시되는지 확인
    expect(wrapper.text()).toContain('900101-1******')

    // 2. 원문보기 버튼 텍스트가 존재하는지 확인
    expect(wrapper.text()).toContain('원문보기')
  })

  it('[원문보기] 버튼 클릭 시 requestDecrypt 이벤트가 올바른 fieldKey와 함께 emit되어야 한다', async () => {
    const wrapper = mount(DynamicRecordLayoutRenderer, {
      props: {
        layoutConfig: sampleLayout,
        fields: sampleFields,
        record: sampleRecord,
        isEditing: false,
        decryptedValues: {},
        decryptRemainingTime: {},
        decryptingFields: {}
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaIcon: true,
          vaBadge: true,
          vaChip: true,
          vaSwitch: true,
          vaInput: true,
          vaSelect: true
        }
      }
    })

    // 원문보기 버튼(또는 링크)을 찾아 클릭
    const viewOriginalBtn = wrapper.findAll('.decrypt-action-btn').find(b => b.text().includes('원문보기'))
    expect(viewOriginalBtn).toBeDefined()
    await viewOriginalBtn!.trigger('click')

    // requestDecrypt 이벤트가 발송되었는지 검증
    expect(wrapper.emitted('requestDecrypt')).toBeTruthy()
    expect(wrapper.emitted('requestDecrypt')![0]).toContain('resident_number')
  })

  it('decryptedValues가 전달되면 복호화된 평문과 남은 시간 및 [원문숨기기] 버튼이 표시되어야 한다', async () => {
    const wrapper = mount(DynamicRecordLayoutRenderer, {
      props: {
        layoutConfig: sampleLayout,
        fields: sampleFields,
        record: sampleRecord,
        isEditing: false,
        decryptedValues: {
          resident_number: '900101-1234567'
        },
        decryptRemainingTime: {
          resident_number: 28
        },
        decryptingFields: {}
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaIcon: true,
          vaBadge: true,
          vaChip: true,
          vaSwitch: true,
          vaInput: true,
          vaSelect: true
        }
      }
    })

    // 1. 복호화된 평문이 렌더링되는지 확인
    expect(wrapper.text()).toContain('900101-1234567')

    // 2. 남은 시간 표기 (00:28) 확인
    expect(wrapper.text()).toContain('00:28')

    // 3. 원문숨기기 버튼 확인
    expect(wrapper.text()).toContain('원문숨기기')

    // 4. [원문숨기기] 클릭 시 hideDecrypt 이벤트 발송 검증
    const hideBtn = wrapper.findAll('.decrypt-action-btn').find(b => b.text().includes('원문숨기기'))
    expect(hideBtn).toBeDefined()
    await hideBtn!.trigger('click')

    expect(wrapper.emitted('hideDecrypt')).toBeTruthy()
    expect(wrapper.emitted('hideDecrypt')![0]).toContain('resident_number')
  })

  describe('2D 레이아웃 다국어(MULTILINGUAL) 필드 렌더링 및 편집 (TDD)', () => {
    const multiLayout = {
      id: 'layout_multi_test',
      cols: 12,
      rowHeight: 40,
      widgets: [
        {
          id: 'w_single_multi',
          type: 'FIELD',
          fieldKey: 'customer_name',
          w: 6,
          h: 1
        },
        {
          id: 'w_card_multi',
          type: 'FIELD',
          fieldKey: 'company_name',
          w: 6,
          h: 2
        }
      ]
    }

    const multiFields = [
      {
        key: 'customer_name',
        name: { ko: '고객명', en: 'Customer Name' },
        type: 'MULTILINGUAL'
      },
      {
        key: 'company_name',
        name: { ko: '회사명', en: 'Company Name' },
        type: 'MULTILINGUAL'
      }
    ]

    const multiRecord = {
      customer_name: { ko: '홍길동', en: 'Hong Gil Dong' },
      company_name: '{"ko":"엠플랫폼","en":"MPlatform"}' // JSON 문자열 형태
    }

    it('단일 행(h=1) 조회 모드에서 KO와 EN 값이 뱃지와 함께 정상 렌더링되어야 한다', () => {
      const wrapper = mount(DynamicRecordLayoutRenderer, {
        props: {
          layoutConfig: multiLayout,
          fields: multiFields,
          record: multiRecord,
          isEditing: false
        },
        global: {
          plugins: [i18n],
          stubs: { vaIcon: true, vaBadge: true, vaChip: true, vaInput: true, vaSelect: true }
        }
      })

      expect(wrapper.text()).toContain('홍길동')
      expect(wrapper.text()).toContain('Hong Gil Dong')
      // JSON 문자열도 안전하게 파싱되어 렌더링되어야 함
      expect(wrapper.text()).toContain('엠플랫폼')
      expect(wrapper.text()).toContain('MPlatform')
    })

    it('단일 행(h=1) 편집 모드에서 KO와 EN 입력을 위한 컴팩트 input이 렌더링되어야 한다', () => {
      const wrapper = mount(DynamicRecordLayoutRenderer, {
        props: {
          layoutConfig: multiLayout,
          fields: multiFields,
          record: { customer_name: { ko: '홍길동', en: 'Hong Gil Dong' } },
          isEditing: true
        },
        global: {
          plugins: [i18n],
          stubs: { vaIcon: true, vaBadge: true, vaChip: true, vaInput: false, vaSelect: true }
        }
      })

      const inputs = wrapper.findAll('input')
      expect(inputs.length).toBeGreaterThanOrEqual(2)
      // 한국어와 영어 입력창에 기존 값이 세팅되어 있어야 함
      const values = inputs.map(i => (i.element as HTMLInputElement).value)
      expect(values).toContain('홍길동')
      expect(values).toContain('Hong Gil Dong')
    })
  })

  describe('2D 레이아웃 선택형(SELECT) 필드 다국어 옵션 및 선택 폼 (TDD)', () => {
    const selectLayout = {
      id: 'layout_select_test',
      cols: 12,
      rowHeight: 40,
      widgets: [
        {
          id: 'w_tier',
          type: 'FIELD',
          fieldKey: 'customer_tier',
          w: 6,
          h: 1
        }
      ]
    }

    const selectFields = [
      {
        key: 'customer_tier',
        name: { ko: '고객 등급', en: 'Customer Tier' },
        type: 'SELECT',
        options: '[{"key":"VIP","value":"VIP","label":{"ko":"최우수VIP","en":"VIP"}},{"key":"GOLD","value":"GOLD","label":{"ko":"골드회원","en":"Gold"}}]'
      }
    ]

    const selectRecord = {
      customer_tier: 'GOLD'
    }

    it('조회 모드에서 다국어 label 객체가 [object Object]가 아니라 로케일에 맞는 라벨로 렌더링되어야 한다', () => {
      const wrapper = mount(DynamicRecordLayoutRenderer, {
        props: {
          layoutConfig: selectLayout,
          fields: selectFields,
          record: selectRecord,
          isEditing: false
        },
        global: {
          plugins: [i18n],
          stubs: { vaIcon: true, vaBadge: true, vaChip: false, vaInput: true, vaSelect: true }
        }
      })

      // [object Object]가 절대 나타나지 않아야 함
      expect(wrapper.text()).not.toContain('[object Object]')
      // 한국어 라벨 '골드회원'이 표시되어야 함
      expect(wrapper.text()).toContain('골드회원')
    })
  })

  describe('2D 고도화 위젯 렌더링 (STAT_CARD, PROGRESS_BAR, TEXT_BANNER, RADIO_SEGMENT, MULTI_CHIP_SELECT, BADGE_TAG) (TDD)', () => {
    const advancedLayout = {
      id: 'layout_adv_test',
      cols: 12,
      rowHeight: 40,
      widgets: [
        {
          id: 'w_stat',
          type: 'STAT_CARD',
          fieldKey: 'revenue',
          w: 4,
          h: 2,
          options: { unit: '억원', trend: '+15.2%', theme: 'success' }
        },
        {
          id: 'w_prog',
          type: 'PROGRESS_BAR',
          fieldKey: 'achievement_rate',
          w: 4,
          h: 2,
          options: { theme: 'primary' }
        },
        {
          id: 'w_banner',
          type: 'TEXT_BANNER',
          fieldKey: 'slogan',
          w: 4,
          h: 2,
          options: { bgStyle: 'gradient', align: 'center' }
        },
        {
          id: 'w_seg',
          type: 'RADIO_SEGMENT',
          fieldKey: 'contract_type',
          w: 6,
          h: 2
        },
        {
          id: 'w_chips',
          type: 'MULTI_CHIP_SELECT',
          fieldKey: 'interests',
          w: 6,
          h: 2
        },
        {
          id: 'w_badge',
          type: 'BADGE_TAG',
          fieldKey: 'status_badge',
          w: 3,
          h: 1,
          options: { theme: 'warning', outline: true }
        }
      ]
    }

    const advancedFields = [
      { key: 'revenue', name: { ko: '총 매출' }, type: 'NUMBER' },
      { key: 'achievement_rate', name: { ko: '목표 달성률' }, type: 'NUMBER' },
      { key: 'slogan', name: { ko: '슬로건' }, type: 'TEXT' },
      {
        key: 'contract_type',
        name: { ko: '계약 유형' },
        type: 'SELECT',
        options: '[{"key":"REGULAR","value":"REGULAR","label":{"ko":"정규 계약"}},{"key":"TEMP","value":"TEMP","label":{"ko":"임시 계약"}}]'
      },
      {
        key: 'interests',
        name: { ko: '관심 분야' },
        type: 'SELECT',
        options: '[{"key":"AI","value":"AI","label":{"ko":"인공지능"}},{"key":"CLOUD","value":"CLOUD","label":{"ko":"클라우드"}}]'
      },
      { key: 'status_badge', name: { ko: '진행 상태' }, type: 'TEXT' }
    ]

    const advancedRecord = {
      revenue: 125,
      achievement_rate: 88,
      slogan: '글로벌 엔터프라이즈 DX 플랫폼',
      contract_type: 'REGULAR',
      interests: ['AI', 'CLOUD'],
      status_badge: '심사중'
    }

    it('STAT_CARD가 숫자, 단위, 트렌드 및 테마 클래스를 정상 렌더링해야 한다', () => {
      const wrapper = mount(DynamicRecordLayoutRenderer, {
        props: {
          layoutConfig: advancedLayout,
          fields: advancedFields,
          record: advancedRecord,
          isEditing: false
        },
        global: {
          plugins: [i18n],
          stubs: { vaIcon: true, vaBadge: true, vaChip: true, vaInput: true, vaSelect: true }
        }
      })

      const statBox = wrapper.find('.widget-stat-card-box')
      expect(statBox.exists()).toBe(true)
      expect(statBox.text()).toContain('125')
      expect(statBox.text()).toContain('억원')
      expect(statBox.text()).toContain('+15.2%')
      expect(statBox.classes()).toContain('theme-success')
    })

    it('PROGRESS_BAR가 퍼센티지 및 바를 정상 렌더링해야 한다', () => {
      const wrapper = mount(DynamicRecordLayoutRenderer, {
        props: {
          layoutConfig: advancedLayout,
          fields: advancedFields,
          record: advancedRecord,
          isEditing: false
        },
        global: {
          plugins: [i18n],
          stubs: { vaIcon: true, vaBadge: true, vaChip: true, vaInput: true, vaSelect: true }
        }
      })

      const progBox = wrapper.find('.widget-progress-box')
      expect(progBox.exists()).toBe(true)
      expect(progBox.text()).toContain('88%')
    })

    it('TEXT_BANNER가 타이틀과 스타일 클래스(gradient, center)를 정상 적용해야 한다', () => {
      const wrapper = mount(DynamicRecordLayoutRenderer, {
        props: {
          layoutConfig: advancedLayout,
          fields: advancedFields,
          record: advancedRecord,
          isEditing: false
        },
        global: {
          plugins: [i18n],
          stubs: { vaIcon: true, vaBadge: true, vaChip: true, vaInput: true, vaSelect: true }
        }
      })

      const bannerBox = wrapper.find('.widget-banner-box')
      expect(bannerBox.exists()).toBe(true)
      expect(bannerBox.text()).toContain('글로벌 엔터프라이즈 DX 플랫폼')
      expect(bannerBox.classes()).toContain('style-gradient')
      expect(bannerBox.classes()).toContain('align-center')
    })

    it('RADIO_SEGMENT가 옵션 목록 버튼 및 선택 상태를 렌더링해야 한다', () => {
      const wrapper = mount(DynamicRecordLayoutRenderer, {
        props: {
          layoutConfig: advancedLayout,
          fields: advancedFields,
          record: advancedRecord,
          isEditing: true
        },
        global: {
          plugins: [i18n],
          stubs: { vaIcon: true, vaBadge: true, vaChip: true, vaInput: true, vaSelect: true }
        }
      })

      const segBox = wrapper.find('.widget-segment-box')
      expect(segBox.exists()).toBe(true)
      const buttons = segBox.findAll('button')
      expect(buttons.length).toBe(2)
      // REGULAR 버튼이 active 클래스를 가져야 함
      const activeBtn = buttons.find(b => b.classes().includes('active'))
      expect(activeBtn?.text()).toBe('정규 계약')
    })
  })
})

