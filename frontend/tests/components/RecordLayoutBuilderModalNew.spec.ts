import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createI18n } from 'vue-i18n'
import RecordLayoutBuilderModal from '../../components/records/RecordLayoutBuilderModal.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      layout_builder_title: '레코드 화면 2D 그리드 레이아웃 빌더',
      layout_builder_desc: '모눈종이 격자 캔버스 위에서 필드와 위젯을 드래그하여 가로(W)와 세로 높이(H)를 자유롭게 배치합니다.',
      layout_scope_domain: '도메인 공통 기본 레이아웃',
      layout_scope_node: '분류 노드 개별 레이아웃',
      btn_auto_generate_layout: '기본 레이아웃 자동 생성',
      btn_clear_layout: '캔버스 초기화',
      save: '저장',
      search: '검색',
      palette_title: '위젯 파레트',
      no_unplaced_fields: '모든 필드가 배치되었습니다.',
      add_new_layout: '새 레이아웃 추가',
      duplicate_layout: '레이아웃 복제',
      rename_layout: '이름 변경',
      drag_width_resize_hint: '가로 크기 조절',
      drag_height_resize_hint: '세로 크기 조절',
      drag_to_resize_hint: '크기 조절',
      layout_select_label: '화면 레이아웃',
      default_layout_badge: '기본',
      default_layout_name: '기본 레이아웃',
      palette_fields_tab: '미배치 필드',
      palette_widgets_tab: 'UI 위젯',
      inspector_title: '속성 설정',
      position_and_size: '위치 및 크기',
      options: '설정 옵션',
      select_widget_to_edit_properties: '속성을 편집할 위젯을 캔버스에서 선택하세요.',
      layout_mode_landscape: '가로 모드 (12단)',
      layout_mode_portrait: '세로 모드 (6단)',
      palette_toggle_collapse: '팔레트 접기',
      palette_toggle_expand: '팔레트 펼치기',
      inspector_drawer_title: '위젯 속성 설정',
      inspector_apply_and_close: '적용 및 닫기',
      inspector_close: '닫기',
      widget_edit_properties: '속성 설정',
      widget_delete: '위젯 삭제',
      widget_width: '가로 칸수',
      widget_height: '세로 칸수',
      widget_pos_x: 'X 좌표',
      widget_pos_y: 'Y 좌표',
      widget_label: '표시 라벨',
      label_ko: '한국어 라벨',
      label_en: '영어 라벨',
      widget_highlight: '강조 표시',
      widget_readonly: '읽기 전용',
      widget_required: '필수 입력'
    }
  }
})

const mockCustomFetch = vi.fn()
vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

describe('RecordLayoutBuilderModal Overhaul (TDD)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockCustomFetch.mockResolvedValue({
      layouts: [
        {
          id: 'layout_default',
          name: { ko: '기본 레이아웃', en: 'Default Layout' },
          isDefault: true,
          cols: 12,
          rowHeight: 42,
          widgets: [
            { id: 'w1', type: 'FIELD', fieldKey: 'cust_name', title: { ko: '고객명', en: 'Customer Name' }, x: 0, y: 0, w: 4, h: 1, options: {} },
            { id: 'w2', type: 'FIELD', fieldKey: 'cust_email', title: { ko: '이메일', en: 'Email' }, x: 4, y: 0, w: 4, h: 1, options: {} }
          ]
        }
      ]
    })
  })

  it('1. 가로 모드(Landscape)와 세로 모드(Portrait) 토글이 정상 작동해야 한다', async () => {
    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        fields: [
          { key: 'cust_name', name: { ko: '고객명' }, type: 'TEXT' },
          { key: 'cust_email', name: { ko: '이메일' }, type: 'EMAIL' }
        ]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: {
            template: '<button class="va-button-stub" :class="[color, preset]" @click="$emit(\'click\', $event)"><slot/></button>',
            props: ['color', 'preset', 'icon', 'loading', 'disabled']
          },
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaSwitch: true,
          vaBadge: true,
          vaCheckbox: true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // 초기 상태: 가로 모드 (cols = 12)
    const landscapeBtn = wrapper.find('.btn-mode-landscape')
    const portraitBtn = wrapper.find('.btn-mode-portrait')
    expect(landscapeBtn.exists()).toBe(true)
    expect(portraitBtn.exists()).toBe(true)

    // 세로 모드(Portrait) 클릭
    await portraitBtn.trigger('click')
    await wrapper.vm.$nextTick()

    // 세로 모드 뷰포트 클래스 및 cols = 6 확인
    expect(wrapper.find('.canvas-workspace-area.is-portrait').exists()).toBe(true)
    expect(wrapper.find('.grid-canvas-container.is-portrait').exists()).toBe(true)

    // 다시 가로 모드로 복원
    await landscapeBtn.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.canvas-workspace-area.is-portrait').exists()).toBe(false)
  })

  it('2. 위젯 클릭 또는 설정 버튼 클릭 시 우측 슬라이드오버 인스펙터가 열리고 닫기 버튼으로 닫혀야 한다', async () => {
    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        fields: [
          { key: 'cust_name', name: { ko: '고객명' }, type: 'TEXT' }
        ]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot/></button>',
            props: ['color', 'preset', 'icon', 'loading', 'disabled']
          },
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaSwitch: true,
          vaBadge: true,
          vaCheckbox: true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // 초기에는 인스펙터 사이드바가 닫혀있어야 함
    const inspectorDrawer = wrapper.find('.inspector-slide-drawer')
    expect(inspectorDrawer.classes()).not.toContain('is-open')

    // 위젯 박스 찾기 및 클릭
    const widgetBox = wrapper.find('.canvas-widget-box')
    expect(widgetBox.exists()).toBe(true)
    await widgetBox.trigger('click')
    await wrapper.vm.$nextTick()

    // 클릭 후 사이드바 열림 확인
    expect(wrapper.find('.inspector-slide-drawer').classes()).toContain('is-open')

    // [적용 및 닫기] 버튼 클릭
    const applyBtn = wrapper.find('.btn-inspector-apply')
    expect(applyBtn.exists()).toBe(true)
    await applyBtn.trigger('click')
    await wrapper.vm.$nextTick()

    // 사이드바 닫힘 확인
    expect(wrapper.find('.inspector-slide-drawer').classes()).not.toContain('is-open')
  })

  it('3. 좌측 팔레트 접기/펼치기 토글이 정상 작동해야 한다', async () => {
    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        fields: [
          { key: 'cust_name', name: { ko: '고객명' }, type: 'TEXT' }
        ]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot/></button>',
            props: ['color', 'preset', 'icon', 'loading', 'disabled']
          },
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaSwitch: true,
          vaBadge: true,
          vaCheckbox: true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // 팔레트 초기 상태
    expect(wrapper.find('.palette-sidebar').classes()).not.toContain('is-collapsed')

    // 팔레트 접기 버튼 클릭
    const paletteToggleBtn = wrapper.find('.btn-toggle-palette')
    expect(paletteToggleBtn.exists()).toBe(true)

    await paletteToggleBtn.trigger('click')
    await wrapper.vm.$nextTick()

    // 접힌 상태 확인
    expect(wrapper.find('.palette-sidebar').classes()).toContain('is-collapsed')

    // 다시 펼치기
    await paletteToggleBtn.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.palette-sidebar').classes()).not.toContain('is-collapsed')
  })

  it('4. UI 위젯 탭에서 다양한 카테고리의 위젯을 캔버스에 추가할 수 있어야 한다', async () => {
    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        fields: [
          { key: 'cust_name', name: { ko: '고객명' }, type: 'TEXT' },
          { key: 'salary', name: { ko: '급여' }, type: 'NUMBER' }
        ]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot/></button>',
            props: ['color', 'preset', 'icon', 'loading', 'disabled']
          },
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaSwitch: true,
          vaBadge: true,
          vaCheckbox: true
        }
      }
    })

    await wrapper.vm.$nextTick()

    // UI 위젯 탭 클릭
    const widgetTabBtn = wrapper.findAll('.palette-tab-btn')[1]
    expect(widgetTabBtn.exists()).toBe(true)
    await widgetTabBtn.trigger('click')
    await wrapper.vm.$nextTick()

    // 카테고리 그룹 렌더링 확인
    const categories = wrapper.findAll('.palette-category-group')
    expect(categories.length).toBeGreaterThanOrEqual(4)

    // 임의의 위젯 아이템 클릭
    const firstWidgetItem = wrapper.find('.palette-item')
    expect(firstWidgetItem.exists()).toBe(true)
    await firstWidgetItem.trigger('click')
    await wrapper.vm.$nextTick()

    // 캔버스에 위젯이 추가되고 인스펙터가 열렸는지 확인
    expect(wrapper.find('.inspector-slide-drawer').classes()).toContain('is-open')
  })

  it('5. 위젯 타입별로 호환되는 필드 타입만 인스펙터 필드 바인딩 셀렉터에 노출되어야 한다 (타입 고정/제약)', async () => {
    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        fields: [
          { key: 'cust_name', name: { ko: '고객명' }, type: 'TEXT' },
          { key: 'cust_name_multi', name: { ko: '다국어 고객명' }, type: 'MULTILINGUAL' },
          { key: 'memo', name: { ko: '상세메모' }, type: 'TEXTAREA' },
          { key: 'salary', name: { ko: '급여' }, type: 'NUMBER' },
          { key: 'attachment', name: { ko: '첨부파일' }, type: 'FILE' },
          { key: 'join_date', name: { ko: '가입일자' }, type: 'DATE' },
          { key: 'profile_pic', name: { ko: '프로필사진' }, type: 'IMAGE' }
        ]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot/></button>',
            props: ['color', 'preset', 'icon', 'loading', 'disabled']
          },
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaSwitch: true,
          vaBadge: true,
          vaCheckbox: true
        }
      }
    })

    await wrapper.vm.$nextTick()

    const vm = wrapper.vm as any
    vm.widgets = [
      { id: 'widget-textarea-1', type: 'TEXT_AREA', w: 6, h: 2, x: 0, y: 0 },
      { id: 'widget-multi-1', type: 'MULTILINGUAL_INPUT', w: 6, h: 1, x: 0, y: 2 },
      { id: 'widget-file-1', type: 'FILE_ATTACHMENT', w: 6, h: 2, x: 0, y: 3 },
      { id: 'widget-date-1', type: 'DATE_PICKER', w: 4, h: 1, x: 0, y: 5 },
      { id: 'widget-number-1', type: 'NUMBER_INPUT', w: 4, h: 1, x: 4, y: 5 }
    ]
    
    // 1) TEXT_AREA 위젯 선택 시: 오직 TEXTAREA 필드(memo)만 노출
    vm.selectedWidgetId = 'widget-textarea-1'
    await wrapper.vm.$nextTick()

    const textareaOptions = vm.fieldBindingSelectOptions.map((o: any) => o.value)
    expect(textareaOptions).toEqual(['memo'])

    // 2) MULTILINGUAL_INPUT 위젯 선택 시: 오직 MULTILINGUAL 필드(cust_name_multi)만 노출
    vm.selectedWidgetId = 'widget-multi-1'
    await wrapper.vm.$nextTick()

    const multiOptions = vm.fieldBindingSelectOptions.map((o: any) => o.value)
    expect(multiOptions).toEqual(['cust_name_multi'])

    // 3) FILE_ATTACHMENT 위젯 선택 시: FILE 필드만 노출
    vm.selectedWidgetId = 'widget-file-1'
    await wrapper.vm.$nextTick()

    const fileOptions = vm.fieldBindingSelectOptions.map((o: any) => o.value)
    expect(fileOptions).toEqual(['attachment'])

    // 4) DATE_PICKER 위젯 선택 시: DATE 필드만 노출
    vm.selectedWidgetId = 'widget-date-1'
    await wrapper.vm.$nextTick()

    const dateOptions = vm.fieldBindingSelectOptions.map((o: any) => o.value)
    expect(dateOptions).toEqual(['join_date'])

    // 5) NUMBER_INPUT 위젯 선택 시: NUMBER 필드만 노출
    vm.selectedWidgetId = 'widget-number-1'
    await wrapper.vm.$nextTick()

    const numberOptions = vm.fieldBindingSelectOptions.map((o: any) => o.value)
    expect(numberOptions).toEqual(['salary'])
  })

  it('6. 접힌 상태에서 플로팅 파레트 버튼이 노출되고 클릭 시 파레트가 펼쳐져야 한다', async () => {
    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        fields: [{ key: 'cust_name', name: { ko: '고객명' }, type: 'TEXT' }]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot/></button>',
            props: ['color', 'preset', 'icon', 'loading', 'disabled']
          },
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaSwitch: true,
          vaBadge: true,
          vaCheckbox: true
        }
      }
    })

    await wrapper.vm.$nextTick()
    const vm = wrapper.vm as any

    // 강제로 접힘 상태로 설정
    vm.isPaletteCollapsed = true
    await wrapper.vm.$nextTick()

    const floatingBtn = wrapper.find('.floating-palette-btn')
    expect(floatingBtn.exists()).toBe(true)

    // 플로팅 버튼 클릭 시 펼쳐짐 확인
    await floatingBtn.trigger('click')
    await wrapper.vm.$nextTick()
    expect(vm.isPaletteCollapsed).toBe(false)
  })

  it('7. 인스펙터 하단 푸터에 완료, 닫기, 저장 액션이 노출되고 정상 동작해야 한다', async () => {
    const wrapper = mount(RecordLayoutBuilderModal, {
      props: {
        modelValue: true,
        domainId: 'domain-123',
        fields: [{ key: 'cust_name', name: { ko: '고객명' }, type: 'TEXT' }]
      },
      global: {
        plugins: [i18n],
        stubs: {
          vaModal: { template: '<div class="va-modal-stub"><slot/></div>' },
          vaButton: {
            template: '<button class="va-button-stub" @click="$emit(\'click\', $event)"><slot/></button>',
            props: ['color', 'preset', 'icon', 'loading', 'disabled']
          },
          vaIcon: true,
          vaInput: true,
          vaSelect: true,
          vaSwitch: true,
          vaBadge: true,
          vaCheckbox: true
        }
      }
    })

    await wrapper.vm.$nextTick()
    const vm = wrapper.vm as any

    // 위젯 선택하여 인스펙터 오픈
    vm.widgets = [{ id: 'w-1', type: 'TEXT_INPUT', fieldKey: 'cust_name', w: 6, h: 1, x: 0, y: 0 }]
    vm.selectedWidgetId = 'w-1'
    vm.isInspectorOpen = true
    await wrapper.vm.$nextTick()

    // 드로어 푸터 확인
    const footer = wrapper.find('.drawer-footer')
    expect(footer.exists()).toBe(true)

    // 백드롭 오버레이 클릭 시 인스펙터 닫힘 확인
    const backdrop = wrapper.find('.inspector-backdrop')
    expect(backdrop.exists()).toBe(true)
    await backdrop.trigger('click')
    await wrapper.vm.$nextTick()
    expect(vm.isInspectorOpen).toBe(false)
  })
})
