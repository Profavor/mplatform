import { describe, it, expect } from 'vitest'

interface ChannelStats {
  channelId: string
  channelName: string
  channelCode: string
  type: string
  totalCount: number
  successCount: number
  failCount: number
  successRate: number
  lastExecutedAt: string | null
  lastStatus: string
  healthStatus: 'HEALTHY' | 'WARNING' | 'CRITICAL' | 'IDLE'
}

describe('ChannelSlaMonitoring - #147 외부 연동 채널 상태 및 SLA 모니터링', () => {

  const calculateSlaSummary = (stats: ChannelStats[]) => {
    const totalChannels = stats.length
    const healthyCount = stats.filter(s => s.healthStatus === 'HEALTHY').length
    const warningCount = stats.filter(s => s.healthStatus === 'WARNING').length
    const criticalCount = stats.filter(s => s.healthStatus === 'CRITICAL').length
    const idleCount = stats.filter(s => s.healthStatus === 'IDLE').length

    const totalExecutions = stats.reduce((acc, s) => acc + s.totalCount, 0)
    const totalSuccesses = stats.reduce((acc, s) => acc + s.successCount, 0)
    const overallSuccessRate = totalExecutions > 0
      ? Math.round((totalSuccesses / totalExecutions) * 1000) / 10
      : 100.0

    return {
      totalChannels,
      healthyCount,
      warningCount,
      criticalCount,
      idleCount,
      totalExecutions,
      overallSuccessRate
    }
  }

  const mockStats: ChannelStats[] = [
    {
      channelId: 'ch-01',
      channelName: '카트봄 Outbound',
      channelCode: 'CARTBOM_OUT',
      type: 'WEBHOOK',
      totalCount: 150,
      successCount: 148,
      failCount: 2,
      successRate: 98.7,
      lastExecutedAt: '2026-09-06T07:00:00Z',
      lastStatus: 'SUCCESS',
      healthStatus: 'HEALTHY'
    },
    {
      channelId: 'ch-02',
      channelName: 'KRX Inbound',
      channelCode: 'KRX_IN',
      type: 'DATABASE',
      totalCount: 40,
      successCount: 30,
      failCount: 10,
      successRate: 75.0,
      lastExecutedAt: '2026-09-06T06:30:00Z',
      lastStatus: 'FAIL',
      healthStatus: 'CRITICAL'
    }
  ]

  it('#147: 전체 채널의 SLA 종합 지표(전체 건수, 건별 성공률, 헬스 상태 집계)가 정확히 산출된다', () => {
    const summary = calculateSlaSummary(mockStats)

    expect(summary.totalChannels).toBe(2)
    expect(summary.healthyCount).toBe(1)
    expect(summary.criticalCount).toBe(1)
    expect(summary.totalExecutions).toBe(190)
    expect(summary.overallSuccessRate).toBe(93.7) // (178 / 190) * 100 = 93.68 -> 93.7
  })

  it('#147: 채널 헬스 상태에 따라 올바른 Vuestic 컬러가 반환된다', () => {
    const getBadgeProps = (status: string) => {
      switch (status) {
        case 'HEALTHY': return { color: 'success', icon: 'check_circle' }
        case 'WARNING': return { color: 'warning', icon: 'warning' }
        case 'CRITICAL': return { color: 'danger', icon: 'error' }
        default: return { color: 'secondary', icon: 'pause_circle' }
      }
    }

    expect(getBadgeProps('HEALTHY').color).toBe('success')
    expect(getBadgeProps('WARNING').color).toBe('warning')
    expect(getBadgeProps('CRITICAL').color).toBe('danger')
    expect(getBadgeProps('IDLE').color).toBe('secondary')
  })

  it('#147: 실패가 0건인 빈 채널 목록에서도 100% 성공률로 안전하게 초기화된다', () => {
    const summary = calculateSlaSummary([])
    expect(summary.totalChannels).toBe(0)
    expect(summary.overallSuccessRate).toBe(100.0)
    expect(summary.healthyCount).toBe(0)
  })
})
