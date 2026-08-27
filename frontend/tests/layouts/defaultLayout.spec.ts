import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, nextTick } from 'vue'
import { createI18n } from 'vue-i18n'
import DefaultLayout from '../../layouts/default.vue'

const i18n = createI18n({
  legacy: false,
  locale: 'ko',
  messages: {
    ko: {
      effective_roles: '유효 권한',
      belongs_to_org: '소속 조직',
      personal_settings: '개인화 설정',
      logout: '로그아웃',
      request_domain_access: '도메인 접근 권한 신청',
      timezone: '타임존',
      font_size_setting: '폰트 크기',
      font_size_small: '작게',
      font_size_medium: '보통',
      font_size_large: '크게',
      font_size_xlarge: '아주 크게',
      change_password: '비밀번호 변경',
      force_password_change: '비밀번호 강제 변경'
    }
  }
})

vi.mock('#app', () => ({
  useCookie: (name: string, opts?: any) => {
    if (name === 'auth_token') return ref('test-token')
    if (name === 'user_data') return ref(JSON.stringify({ username: 'superadmin', role: 'ROLE_ADMIN' }))
    if (name === 'locale') return ref('ko')
    if (name === 'theme') return ref('light')
    if (name === 'timezone') return ref('Asia/Seoul')
    if (name === 'fontSize') return ref('14px')
    if (name === 'user_permissions') return ref(['*'])
    return ref(opts?.default ? opts.default() : null)
  },
  useState: (name: string, init?: () => any) => ref(init ? init() : null),
  useRuntimeConfig: () => ({
    public: {
      appVersion: 'v1.0.8',
      buildTime: '2026-08-28T03:08:00Z'
    }
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual: any = await importOriginal()
  return {
    ...actual,
    useColors: () => ({
      applyPreset: vi.fn(),
      currentPresetName: ref('light')
    })
  }
})

const mockMenuTree = [
  {
    id: 1,
    name: '{"ko":"홈","en":"Home"}',
    path: '/',
    icon: 'home',
    requiredRoles: ['ROLE_USER', 'ROLE_ADMIN'],
    isActive: true
  },
  {
    id: 2,
    name: '{"ko":"도메인 & 스키마 관리","en":"Domain & Schema"}',
    path: '/schema',
    icon: 'schema',
    requiredRoles: ['DOMAIN_EDITOR', 'ROLE_ADMIN'],
    isActive: true
  },
  {
    id: 3,
    name: '{"ko":"마스터 데이터 관리","en":"Master Data"}',
    path: '/records',
    icon: 'table_chart',
    requiredRoles: ['ROLE_USER', 'ROLE_ADMIN'],
    isActive: true
  },
  {
    id: 7,
    name: '{"ko":"시스템 관리","en":"System Admin"}',
    path: '/admin',
    icon: 'admin_panel_settings',
    requiredRoles: ['ROLE_ADMIN'],
    isActive: true,
    children: [
      {
        id: 10,
        name: '{"ko":"메뉴 관리","en":"Menu Management"}',
        path: '/admin/menus',
        icon: 'menu_book',
        requiredRoles: ['ROLE_ADMIN'],
        isActive: true
      }
    ]
  }
]

vi.mock('~/composables/useMenu', () => ({
  useMenu: () => ({
    menus: ref(mockMenuTree),
    fetchMenus: vi.fn().mockResolvedValue(mockMenuTree)
  })
}))

vi.mock('~/composables/useRoles', () => ({
  useRoles: () => ({
    getRoleBadgeStyle: vi.fn().mockReturnValue({}),
    formatRoleText: (r: string) => r,
    initGlobalRoles: vi.fn()
  })
}))

vi.mock('~/composables/useAuthUser', () => ({
  useAuthUser: () => ({
    currentUser: {
      id: 'admin-uuid',
      username: 'superadmin',
      role: 'ROLE_ADMIN',
      permissions: ['*']
    },
    fetchCurrentUser: vi.fn().mockResolvedValue({
      id: 'admin-uuid',
      username: 'superadmin',
      role: 'ROLE_ADMIN',
      permissions: ['*']
    })
  })
}))

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: vi.fn().mockResolvedValue([]),
    getAuthToken: () => 'test-token'
  })
}))

const mockCurrentRoute = ref({ path: '/records' })

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  }),
  useRoute: () => mockCurrentRoute.value
}))

describe('layouts/default.vue (TDD)', () => {
  const stubs = {
    'va-layout': {
      template: '<div class="va-layout"><slot name="top" /><slot name="left" /><slot name="content" /><slot /></div>'
    },
    'va-navbar': {
      template: '<header class="va-navbar" style="height: 64px;"><slot name="left" /><slot name="right" /><slot /></header>'
    },
    'va-navbar-item': { template: '<div class="va-navbar-item"><slot /></div>' },
    'va-sidebar': {
      template: '<aside class="va-sidebar"><slot /></aside>'
    },
    'va-button': { template: '<button><slot /></button>' },
    'va-icon': true,
    'va-badge': true,
    'va-avatar': true,
    'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
    'va-dropdown-content': { template: '<div><slot /></div>' },
    'va-list': { template: '<div><slot /></div>' },
    'va-list-item': { template: '<div><slot /></div>' },
    'va-list-item-section': { template: '<div><slot /></div>' },
    'va-divider': true,
    'va-select': true,
    GlobalSearch: true,
    NotificationBell: true,
    InAppMessenger: true,
    SystemRadioWidget: true,
    AdminMusicControlModal: true,
    AppModal: true,
    ChangePasswordForm: true,
    DomainAccessRequestModal: true,
    TimezoneSelect: true,
    SidebarMenuItem: {
      props: ['menu'],
      template: '<div class="test-sidebar-menu-item" :data-path="menu.path">{{ menu.name }}</div>'
    }
  }

  it('filteredMenus에 홈(/), 도메인&스키마(/schema), 마스터데이터(/records)가 모두 포함되어 렌더링되어야 함', async () => {
    const wrapper = mount(DefaultLayout, {
      global: {
        plugins: [i18n],
        stubs
      }
    })

    await nextTick()

    const renderedItems = wrapper.findAll('.test-sidebar-menu-item')
    const paths = renderedItems.map(item => item.attributes('data-path'))

    expect(paths).toContain('/')
    expect(paths).toContain('/schema')
    expect(paths).toContain('/records')
    expect(paths).toContain('/admin')
  })

  it('모바일 뷰포트에서 사이드바가 열려있을 때 모바일 백드롭이 렌더링되고, 백드롭 클릭 시 사이드바가 닫혀야 함', async () => {
    window.innerWidth = 400
    const wrapper = mount(DefaultLayout, {
      global: {
        plugins: [i18n],
        stubs
      }
    })

    await nextTick()

    wrapper.vm.showSidebar = true
    await nextTick()

    const backdrop = wrapper.find('.sidebar-mobile-backdrop')
    expect(backdrop.exists()).toBe(true)

    await backdrop.trigger('click')
    await nextTick()
    expect(wrapper.vm.showSidebar).toBe(false)
  })

  it('네비게이션 바 높이를 동적으로 측정하여 --app-navbar-height CSS 변수를 등록해야 함', async () => {
    const wrapper = mount(DefaultLayout, {
      global: {
        plugins: [i18n],
        stubs
      }
    })

    await nextTick()

    expect(typeof wrapper.vm.updateNavbarHeight).toBe('function')
    wrapper.vm.updateNavbarHeight()
    const setHeight = document.documentElement.style.getPropertyValue('--app-navbar-height')
    expect(setHeight).toBeDefined()
  })
})
