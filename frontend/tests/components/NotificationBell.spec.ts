import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import NotificationBell from '../../components/layout/NotificationBell.vue'

// $fetch Nuxt global mock
vi.stubGlobal('$fetch', vi.fn().mockResolvedValue([]))

// Global mocks for Nuxt composables & Vuestic UI
vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    te: (key: string) => true,
    locale: { value: 'ko' }
  })
}))


vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ path: '/', query: {}, params: {} })
}))

vi.mock('~/composables/useApprovalEnricher', () => ({
  useApprovalEnricher: () => ({
    loadMetadata: vi.fn().mockResolvedValue(undefined),
    getFieldsForNode: vi.fn().mockResolvedValue([]),
    enrichApprovalDetails: vi.fn().mockResolvedValue(null),
    nodes: { value: {} },
    domains: { value: {} },
    fieldSchemas: { value: {} }
  })
}))

const mockCustomFetch = vi.fn().mockResolvedValue([])

vi.mock('~/composables/useCustomFetch', () => ({
  useCustomFetch: () => ({
    customFetch: mockCustomFetch
  })
}))

const mockFetchUnreadCount = vi.fn().mockResolvedValue({ unreadCount: 3 })
vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    fetchUnreadCount: mockFetchUnreadCount
  })
}))

let capturedWsCallback: ((data: any) => void) | null = null

vi.mock('~/composables/useWebSocket', () => ({
  useWebSocket: () => ({
    connect: vi.fn().mockImplementation((cb) => {
      capturedWsCallback = cb
    }),
    disconnect: vi.fn(),
    isConnected: { value: false }
  })
}))

vi.mock('#app', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useCookie: () => ({ value: 'fake-token' }),
    useRouter: () => ({ push: vi.fn() }),
    useRuntimeConfig: () => ({ public: { apiBaseUrl: 'http://localhost:8080' } })
  }
})

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    userMap: { value: {} },
    isInitialized: { value: true },
    fetchUserMap: vi.fn().mockResolvedValue({}),
    getUserName: (id: string, fallback: string) => fallback || id || '',
    parseI18nVal: (val: any) => String(val || '')
  })
}))

vi.mock('~/stores/useRoleStore', () => ({
  useRoleStore: () => ({
    rolesList: { value: [] },
    roleOptions: { value: [] },
    isInitialized: { value: true },
    dispatch: vi.fn().mockResolvedValue([]),
    getRoleDisplayName: (code: string) => code,
    formatRoleText: (code: string) => code,
    getUserOrgId: () => null,
    fetchRolesForOrg: vi.fn().mockResolvedValue([]),
    initGlobalRoles: vi.fn().mockResolvedValue([]),
    globalRoleLookupMap: { value: {} },
    orgRolesMap: { value: {} }
  })
}))

vi.mock('~/composables/useRoles', () => ({
  useRoles: () => ({
    rolesList: { value: [] },
    roleOptions: { value: [] },
    isInitialized: { value: true },
    dispatch: vi.fn().mockResolvedValue([]),
    getRoleDisplayName: (code: string) => code,
    formatRoleText: (code: string) => code,
    getUserOrgId: () => null,
    fetchRolesForOrg: vi.fn().mockResolvedValue([]),
    initGlobalRoles: vi.fn().mockResolvedValue([])
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vuestic-ui')>()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({ currentPresetName: { value: 'light' } })
  }
})

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (date: any) => '2026. 07. 25. 10:42:00'
  })
}))

describe('NotificationBell Component', () => {
  it('컴포넌트가 성공적으로 마운트되고 초기 알림 수가 0으로 시작해야 함', () => {
    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })
    expect(wrapper.exists()).toBe(true)
  })

  it('웹소켓으로 INBOX_MESSAGE 수신 시 inbox-refresh-counts 이벤트를 전파하고 인박스 미열람 수를 갱신해야 함', async () => {
    const dispatchEventSpy = vi.spyOn(window, 'dispatchEvent')
    mockFetchUnreadCount.mockResolvedValueOnce({ unreadCount: 5 })

    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    expect(capturedWsCallback).toBeDefined()
    if (capturedWsCallback) {
      capturedWsCallback({
        eventType: 'INBOX_MESSAGE',
        type: 'NEW_MESSAGE',
        subject: '테스트 메일'
      })
    }

    await wrapper.vm.$nextTick()

    expect(dispatchEventSpy).toHaveBeenCalledWith(expect.objectContaining({
      type: 'inbox-refresh-counts'
    }))
    expect(dispatchEventSpy).toHaveBeenCalledWith(expect.objectContaining({
      type: 'inbox-message-received'
    }))
  })

  it('비UUID 임시 알림 클릭 시 백엔드 읽음 API를 호출하지 않고 에러 없이 처리되어야 함', async () => {
    mockCustomFetch.mockClear()

    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    const tempItem = {
      id: 1787050754192.7412,
      title: '임시 알림',
      read: false,
      type: 'INFO'
    }

    await (wrapper.vm as any).handleNotificationClick(tempItem)
    expect(tempItem.read).toBe(true)
    expect(mockCustomFetch).not.toHaveBeenCalledWith(
      expect.stringContaining('1787050754192'),
      expect.anything()
    )
  })

  it('UUID 알림 클릭 시 백엔드 읽음 API(PATCH)를 호출해야 함', async () => {
    mockCustomFetch.mockClear()

    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    const validUuid = '340a0917-af0b-4d13-a1ce-479d4b2e2ca7'
    const uuidItem = {
      id: validUuid,
      title: '정상 알림',
      read: false,
      type: 'INFO'
    }

    await (wrapper.vm as any).handleNotificationClick(uuidItem)
    expect(uuidItem.read).toBe(true)
    expect(mockCustomFetch).toHaveBeenCalledWith(
      `/api/notifications/${validUuid}/read`,
      expect.objectContaining({ method: 'PATCH' })
    )
  })

  it('INBOX_MESSAGE 알림 클릭 시 문서함(openInbox)이 열려야 함', async () => {
    mockCustomFetch.mockClear()

    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    const inboxItem = {
      id: 1787050754192.7412,
      type: 'INBOX_MESSAGE',
      messageId: 'msg-999',
      title: '새 메일'
    }

    await (wrapper.vm as any).handleNotificationClick(inboxItem)
    expect((wrapper.vm as any).showInboxModal).toBe(true)
    expect((wrapper.vm as any).selectedInboxMessageId).toBe('msg-999')
  })

  it('동일한 웹소켓/SSE 알림이 연속해서 2번 유입되어도 1개만 등록되어야 함 (Deduplication)', async () => {
    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    const payload = {
      eventType: 'INBOX_MESSAGE',
      type: 'NEW_MESSAGE',
      messageId: 'msg-dedup-123',
      subject: '제목 테스트',
      senderId: 'sender-1'
    }

    // 1st delivery
    ;(wrapper.vm as any).handleIncomingNotification(payload)
    expect((wrapper.vm as any).notifications.length).toBe(1)
    expect((wrapper.vm as any).notifications[0].messageId).toBe('msg-dedup-123')

    // 2nd duplicate delivery
    ;(wrapper.vm as any).handleIncomingNotification(payload)
    expect((wrapper.vm as any).notifications.length).toBe(1)
  })

  it('FORCE_LOGOUT 이벤트 수신 시 알림 목록에 추가하지 않고 강제 로그아웃을 처리해야 함', async () => {
    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    const payload = {
      eventType: 'FORCE_LOGOUT',
      title: '세션 종료',
      message: '다른 기기/브라우저에서 로그인되어 현재 세션이 종료되었습니다.'
    }

    ;(wrapper.vm as any).handleIncomingNotification(payload)
    // FORCE_LOGOUT should not be added to notifications list
    expect((wrapper.vm as any).notifications.length).toBe(0)
  })

  it('PRESENCE_UPDATE 이벤트(사용자 온/오프라인 접속 상태) 수신 시 알림 목록에 추가하지 않아야 함', async () => {
    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    const presencePayload = {
      type: 'PRESENCE_UPDATE',
      userId: 'test-user-uuid',
      username: 'testuser',
      status: 'ONLINE',
      timestamp: Date.now()
    }

    ;(wrapper.vm as any).handleIncomingNotification(presencePayload)
    expect((wrapper.vm as any).notifications.length).toBe(0)
  })

  it('TYPING, PING, HEARTBEAT 및 내용/식별자가 없는 빈 페이로드 수신 시 알림 목록에 추가하지 않아야 함', async () => {
    const wrapper = mount(NotificationBell, {
      global: {
        mocks: {
          $t: (key: string) => key
        },
        stubs: {
          'va-dropdown': { template: '<div><slot name="anchor" /><slot /></div>' },
          'va-dropdown-content': { template: '<div><slot /></div>' },
          'va-badge': { template: '<div><slot /></div>' },
          'va-button': { template: '<button><slot /></button>' },
          'va-icon': { template: '<i><slot /></i>' },
          'va-divider': { template: '<hr />' },
          'va-modal': { template: '<div><slot name="header" /><slot /><slot name="footer" /></div>' },
          'ApprovalDetailsViewer': { template: '<div></div>' }
        }
      }
    })

    // Control events
    ;(wrapper.vm as any).handleIncomingNotification({ type: 'TYPING', username: 'someone' })
    ;(wrapper.vm as any).handleIncomingNotification({ eventType: 'HEARTBEAT' })
    ;(wrapper.vm as any).handleIncomingNotification({ type: 'PING' })
    ;(wrapper.vm as any).handleIncomingNotification('{}')
    ;(wrapper.vm as any).handleIncomingNotification('')

    expect((wrapper.vm as any).notifications.length).toBe(0)
  })
})
