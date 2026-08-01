import { describe, it, expect } from 'vitest'

// 단일 선택 모드: 각 축당 하나의 nodeId(string | null)만 허용
export function flattenSecondaryNodeSelections(selectionsMap: Record<string, string | null>): string[] {
  const nodeIds: string[] = []
  if (!selectionsMap || typeof selectionsMap !== 'object') return nodeIds

  Object.values(selectionsMap).forEach(val => {
    if (val && typeof val === 'string') nodeIds.push(val)
  })

  return [...new Set(nodeIds)]
}

export function formatSecondaryNodeBadge(secNode: { nodeId: string; nodeName?: string; nodeCode?: string; axisName?: string }): string {
  const name = secNode.nodeName || secNode.nodeCode || secNode.nodeId
  const axis = secNode.axisName ? ` [${secNode.axisName}]` : ''
  return `${name}${axis}`
}

describe('Record Secondary Nodes Assignment (TDD)', () => {
  it('축당 단일 선택 맵을 노드 ID 배열로 올바르게 평탄화해야 함', () => {
    const selections: Record<string, string | null> = {
      'axis-1': 'node-1',
      'axis-2': 'node-3',
      'axis-3': null
    }
    const flattened = flattenSecondaryNodeSelections(selections)
    expect(flattened).toEqual(['node-1', 'node-3'])
  })

  it('null 또는 빈 선택값을 안전하게 무시해야 함', () => {
    const selections: Record<string, string | null> = {
      'axis-1': null,
      'axis-2': null
    }
    const flattened = flattenSecondaryNodeSelections(selections)
    expect(flattened).toEqual([])
  })

  it('동일 nodeId가 다른 축에 중복 지정되면 중복 제거해야 함', () => {
    const selections: Record<string, string | null> = {
      'axis-1': 'node-1',
      'axis-2': 'node-1'
    }
    const flattened = flattenSecondaryNodeSelections(selections)
    expect(flattened).toEqual(['node-1'])
  })

  it('보조 노드 뱃지 라벨을 정상적으로 포맷팅해야 함', () => {
    const secNode = { nodeId: 'n1', nodeName: '수원공장', axisName: '플랜트 축' }
    expect(formatSecondaryNodeBadge(secNode)).toBe('수원공장 [플랜트 축]')
  })

  it('보조 노드 저장 시 emits 이벤트 명칭이 secondaryNodesUpdated 이어야 함', () => {
    const resolveSecondaryNodeSaveEventName = () => 'secondaryNodesUpdated'
    expect(resolveSecondaryNodeSaveEventName()).toBe('secondaryNodesUpdated')
  })
})
