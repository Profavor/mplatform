import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import InboxMessageList from '../../components/inbox/InboxMessageList.vue'
import { nextTick } from 'vue'

const mockFetchMessages = vi.fn()
const mockToggleStar = vi.fn()
const mockBulkMarkAsRead = vi.fn()
const mockBulkMoveToTrash = vi.fn()
const mockCustomFetch = vi.fn()

vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))

vi.mock('~/composables/useInbox', () => ({
  useInbox: () => ({
    fetchMessages: mockFetchMessages,
    toggleStar: mockToggleStar,
    bulkMarkAsRead: mockBulkMarkAsRead,
    bulkMoveToTrash: mockBulkMoveToTrash,
    customFetch: mockCustomFetch
  })
}))

vi.mock('~/stores/useUserStore', () => ({
  useUserStore: () => ({
    getUserName: (id: string, fallback: string) => fallback || id,
    fetchUserMap: vi.fn().mockResolvedValue({})
  })
}))

vi.mock('~/composables/useTimezoneDate', () => ({
  useTimezoneDate: () => ({
    formatWithTimezone: (date: string) => date
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({ currentPresetName: { value: 'light' } })
  }
})

const AgGridVueStub = {
  name: 'AgGridVue',
  props: ['gridOptions'],
  emits: ['grid-ready', 'row-clicked'],
  template: '<div class="ag-grid-stub" />'
}

describe('InboxMessageList', () => {
  let mockGridApi: any

  beforeEach(() => {
    vi.clearAllMocks()
    mockGridApi = {
      setGridOption: vi.fn(),
      purgeInfiniteCache: vi.fn(),
      refreshInfiniteCache: vi.fn(),
      getSelectedRows: vi.fn().mockReturnValue([])
    }
    mockFetchMessages.mockResolvedValue({
      content: [
        { id: 'msg-1', subject: 'Test 1', senderEmail: 'sender1@example.com', isRead: false, isStarred: false, createdAt: '2026-09-08T00:00:00Z' }
      ],
      totalElements: 1
    })
    mockCustomFetch.mockResolvedValue({})
    mockToggleStar.mockResolvedValue({})
    mockBulkMarkAsRead.mockResolvedValue({})
    mockBulkMoveToTrash.mockResolvedValue({})
  })

  it('mounts with rowModelType "infinite" and sets datasource on grid-ready', async () => {
    const wrapper = mount(InboxMessageList, {
      props: {
        folder: 'INBOX',
        searchKeyword: ''
      },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          AgGridVue: AgGridVueStub,
          VaInput: {
            props: ['modelValue'],
            template: '<input class="va-input-stub" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
          },
          VaButton: { template: '<button class="va-btn-stub"><slot /></button>' },
          VaIcon: true
        }
      }
    })

    expect(wrapper.exists()).toBe(true)

    // AgGridVue component inspection
    const agGrid = wrapper.findComponent(AgGridVueStub)
    expect(agGrid.exists()).toBe(true)
    const gridOptions = agGrid.props('gridOptions')
    expect(gridOptions.rowModelType).toBe('infinite')

    // Simulate grid-ready event
    agGrid.vm.$emit('grid-ready', { api: mockGridApi })
    expect(mockGridApi.setGridOption).toHaveBeenCalledWith('datasource', expect.any(Object))

    // Verify datasource getRows
    const lastCall = mockGridApi.setGridOption.mock.calls.find((call: any[]) => call[0] === 'datasource')
    const datasource = lastCall[1]
    expect(datasource).toBeDefined()
    expect(typeof datasource.getRows).toBe('function')

    const successCallback = vi.fn()
    const failCallback = vi.fn()
    await datasource.getRows({
      startRow: 0,
      endRow: 50,
      successCallback,
      failCallback
    })

    expect(mockFetchMessages).toHaveBeenCalledWith('INBOX', 0, 50, '')
    expect(successCallback).toHaveBeenCalledWith(
      expect.arrayContaining([expect.objectContaining({ id: 'msg-1' })]),
      1
    )
  })

  it('refreshes datasource when folder prop changes', async () => {
    const wrapper = mount(InboxMessageList, {
      props: {
        folder: 'INBOX',
        searchKeyword: ''
      },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          AgGridVue: AgGridVueStub,
          VaInput: true,
          VaButton: true,
          VaIcon: true
        }
      }
    })

    const agGrid = wrapper.findComponent(AgGridVueStub)
    agGrid.vm.$emit('grid-ready', { api: mockGridApi })
    mockGridApi.setGridOption.mockClear()

    await wrapper.setProps({ folder: 'SENT' })
    await nextTick()

    expect(mockGridApi.setGridOption).toHaveBeenCalledWith('datasource', expect.any(Object))
  })

  it('handles search input and re-creates datasource with keyword', async () => {
    const wrapper = mount(InboxMessageList, {
      props: {
        folder: 'INBOX',
        searchKeyword: ''
      },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          AgGridVue: AgGridVueStub,
          VaInput: {
            props: ['modelValue'],
            template: '<input class="search-input" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
          },
          VaButton: true,
          VaIcon: true
        }
      }
    })

    const agGrid = wrapper.findComponent(AgGridVueStub)
    agGrid.vm.$emit('grid-ready', { api: mockGridApi })
    mockGridApi.setGridOption.mockClear()

    const input = wrapper.find('.search-input')
    await input.setValue('urgent')
    await nextTick()

    expect(wrapper.emitted('update:searchKeyword')?.[0]).toEqual(['urgent'])
    expect(mockGridApi.setGridOption).toHaveBeenCalledWith('datasource', expect.any(Object))
  })

  it('toggles star using useInbox toggleStar method', async () => {
    const wrapper = mount(InboxMessageList, {
      props: { folder: 'INBOX', searchKeyword: '' },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          AgGridVue: AgGridVueStub,
          VaInput: true,
          VaButton: true,
          VaIcon: true
        }
      }
    })

    const agGrid = wrapper.findComponent(AgGridVueStub)
    agGrid.vm.$emit('grid-ready', { api: mockGridApi })

    const gridOptions = agGrid.props('gridOptions')
    const starCol = gridOptions.columnDefs.find((col: any) => col.field === 'isStarred')
    expect(starCol).toBeDefined()
    expect(typeof starCol.onCellClicked).toBe('function')

    await starCol.onCellClicked({ data: { id: 'msg-123', isStarred: false } })

    expect(mockToggleStar).toHaveBeenCalledWith('msg-123')
    expect(mockGridApi.setGridOption).toHaveBeenCalledWith('datasource', expect.any(Object))
    expect(wrapper.emitted('refresh')).toBeTruthy()
  })

  it('performs bulk actions (markRead, archive, trash) correctly', async () => {
    const wrapper = mount(InboxMessageList, {
      props: { folder: 'INBOX', searchKeyword: '' },
      global: {
        mocks: { $t: (key: string) => key },
        stubs: {
          AgGridVue: AgGridVueStub,
          VaInput: true,
          VaButton: {
            props: ['color', 'icon'],
            template: '<button class="bulk-btn" :data-icon="icon" @click="$emit(\'click\')"><slot /></button>'
          },
          VaIcon: true
        }
      }
    })

    const agGrid = wrapper.findComponent(AgGridVueStub)
    agGrid.vm.$emit('grid-ready', { api: mockGridApi })

    // Simulate selection of row
    mockGridApi.getSelectedRows.mockReturnValue([{ id: 'msg-1' }, { id: 'msg-2' }])
    const gridOptions = agGrid.props('gridOptions')
    gridOptions.onSelectionChanged()
    await nextTick()

    // 1. Mark Read
    const markReadBtn = wrapper.findAll('.bulk-btn').find(b => b.attributes('data-icon') === 'drafts')
    expect(markReadBtn?.exists()).toBe(true)
    await markReadBtn?.trigger('click')

    expect(mockBulkMarkAsRead).toHaveBeenCalledWith(['msg-1', 'msg-2'])
    expect(wrapper.emitted('refresh')).toBeTruthy()

    // 2. Archive
    const archiveBtn = wrapper.findAll('.bulk-btn').find(b => b.attributes('data-icon') === 'archive')
    expect(archiveBtn?.exists()).toBe(true)
    await archiveBtn?.trigger('click')

    expect(mockCustomFetch).toHaveBeenCalledWith('/inbox/messages/bulk-action', {
      method: 'POST',
      body: { action: 'MOVE_TO_ARCHIVE', messageIds: ['msg-1', 'msg-2'] }
    })

    // 3. Trash
    const trashBtn = wrapper.findAll('.bulk-btn').find(b => b.attributes('data-icon') === 'delete')
    expect(trashBtn?.exists()).toBe(true)
    await trashBtn?.trigger('click')

    expect(mockBulkMoveToTrash).toHaveBeenCalledWith(['msg-1', 'msg-2'])
  })
})
