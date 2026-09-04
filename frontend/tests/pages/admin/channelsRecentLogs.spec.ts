import { describe, it, expect, vi } from 'vitest'

describe('Channels Admin Page - Recent Logs Direction Column Spec', () => {
  const mockI18n = {
    t: (key: string) => {
      const messages: Record<string, string> = {
        'integration.channels.direction': '연계 방향',
        'integration.channels.inbound': 'Inbound (수신)',
        'integration.channels.outbound': 'Outbound (발신)',
      }
      return messages[key] || key
    }
  }

  // Helper simulating the updated valueGetter
  const getDirectionValue = (params: any, channels: any[]) => {
    if (params.data?.direction) return params.data.direction
    if (params.data?.channel?.direction) return params.data.channel.direction
    const ch = channels.find(c => c.id === params.data?.channelId)
    return ch?.direction || 'OUTBOUND'
  }

  // Helper simulating the updated cellRenderer
  const renderDirectionCell = (params: any, t: (k: string) => string) => {
    const div = document.createElement('div')
    div.style.cssText = 'display: flex; align-items: center; height: 100%;'
    const isInbound = params.value === 'INBOUND'
    const pill = document.createElement('span')
    pill.style.cssText = `padding: 2px 8px; border-radius: 12px; font-weight: 700; font-size: 0.75rem; font-family: inherit; ${
      isInbound
        ? 'background: rgba(237, 108, 2, 0.12); color: var(--va-warning); border: 1px solid rgba(237, 108, 2, 0.3);'
        : 'background: rgba(25, 118, 210, 0.12); color: var(--va-primary); border: 1px solid rgba(25, 118, 210, 0.3);'
    }`
    pill.textContent = isInbound ? t('integration.channels.inbound') : t('integration.channels.outbound')
    div.appendChild(pill)
    return div
  }

  it('correctly resolves INBOUND when channel.direction is present on the log item', () => {
    const logItem = {
      id: 'log-1',
      channelId: 'ch-inbound-1',
      channel: {
        id: 'ch-inbound-1',
        name: 'KRX & Global Stock Inbound Pipeline',
        direction: 'INBOUND'
      },
      status: 'SUCCESS'
    }
    const channels = [
      { id: 'ch-inbound-1', name: 'KRX & Global Stock Inbound Pipeline', direction: 'INBOUND' }
    ]

    const direction = getDirectionValue({ data: logItem }, channels)
    expect(direction).toBe('INBOUND')

    const rendered = renderDirectionCell({ value: direction }, mockI18n.t)
    expect(rendered.textContent).toBe('Inbound (수신)')
    expect(rendered.innerHTML).toContain('color: var(--va-warning)')
  })

  it('correctly resolves INBOUND from channels list when log item only has channelId', () => {
    const logItemWithoutChannelObj = {
      id: 'log-2',
      channelId: 'ch-inbound-1',
      status: 'SUCCESS'
    }
    const channels = [
      { id: 'ch-inbound-1', name: 'KRX & Global Stock Inbound Pipeline', direction: 'INBOUND' }
    ]

    const direction = getDirectionValue({ data: logItemWithoutChannelObj }, channels)
    expect(direction).toBe('INBOUND')

    const rendered = renderDirectionCell({ value: direction }, mockI18n.t)
    expect(rendered.textContent).toBe('Inbound (수신)')
  })

  it('correctly resolves OUTBOUND and renders localized outbound label', () => {
    const logItem = {
      id: 'log-3',
      channelId: 'ch-outbound-1',
      channel: {
        id: 'ch-outbound-1',
        name: 'ERP Sync Outbound',
        direction: 'OUTBOUND'
      },
      status: 'SUCCESS'
    }
    const channels = [
      { id: 'ch-outbound-1', name: 'ERP Sync Outbound', direction: 'OUTBOUND' }
    ]

    const direction = getDirectionValue({ data: logItem }, channels)
    expect(direction).toBe('OUTBOUND')

    const rendered = renderDirectionCell({ value: direction }, mockI18n.t)
    expect(rendered.textContent).toBe('Outbound (발신)')
    expect(rendered.innerHTML).toContain('color: var(--va-primary)')
  })
})
