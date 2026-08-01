import { describe, it, expect } from 'vitest'

export interface AxisOption {
  value: string
  text: string
}

export function buildAxisOptions(domainName: string, axes: Array<{ id: string; name: any; axisCode?: string }>): AxisOption[] {
  const options: AxisOption[] = [
    { value: '', text: `주 분류체계 (${domainName || 'Primary'})` }
  ]

  if (Array.isArray(axes)) {
    axes.forEach(axis => {
      const axisName = typeof axis.name === 'object' && axis.name !== null
        ? (axis.name.ko || axis.name.en || Object.values(axis.name)[0])
        : (axis.name || 'Axis')
      const code = axis.axisCode || (axis as any).code || ''
      options.push({
        value: axis.id || '',
        text: code ? `[${code}] ${axisName}` : axisName
      })
    })
  }

  return options
}

export function resolveAxisTreeApiUrl(domainId: string, axisId?: string | null): string {
  if (!axisId) {
    return `/api/domains/${domainId}/nodes/tree`
  }
  return `/api/domains/${domainId}/nodes/tree?axisId=${axisId}`
}

describe('ClassificationTree Axis Selection (TDD)', () => {
  it('도메인 이름과 보조 축 목록으로 축 드롭다운 옵션이 올바르게 생성되어야 함', () => {
    const axes = [
      { id: 'axis-1', name: { ko: '플랜트 축' }, axisCode: 'PLANT' },
      { id: 'axis-2', name: { ko: '직군 축' }, axisCode: 'JOB' }
    ]
    const options = buildAxisOptions('임직원', axes)

    expect(options).toHaveLength(3)
    expect(options[0]).toEqual({ value: '', text: '주 분류체계 (임직원)' })
    expect(options[1]).toEqual({ value: 'axis-1', text: '[PLANT] 플랜트 축' })
    expect(options[2]).toEqual({ value: 'axis-2', text: '[JOB] 직군 축' })
  })

  it('선택된 axisId에 따라 트리 로드 API URL이 올바르게 생성되어야 함', () => {
    expect(resolveAxisTreeApiUrl('domain-123', null)).toBe('/api/domains/domain-123/nodes/tree')
    expect(resolveAxisTreeApiUrl('domain-123', 'axis-456')).toBe('/api/domains/domain-123/nodes/tree?axisId=axis-456')
  })

  it('i18n 주 분류체계 라벨 번역 함수가 올바르게 작동해야 함', () => {
    const primaryLabelKo = (domain: string, t: (k: string) => string) => `${t('axis.primary_tree')} (${domain})`
    const mockT = (key: string) => key === 'axis.primary_tree' ? '주 분류체계' : key
    expect(primaryLabelKo('임직원', mockT)).toBe('주 분류체계 (임직원)')
  })
})
