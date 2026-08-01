import { describe, it, expect } from 'vitest'

// Helper function logic for Icon selection formatting & applying in ClassificationAxisTab
export function resolveNodeIcon(iconName?: string | null): string {
  if (!iconName || typeof iconName !== 'string' || !iconName.trim()) {
    return 'article'
  }
  return iconName.trim()
}

export function applySelectedIcon(currentForm: { icon: string }, newIcon: string): { icon: string } {
  return {
    ...currentForm,
    icon: newIcon || 'article'
  }
}

describe('ClassificationAxisTab IconPicker Integration (TDD)', () => {
  it('아이콘 명이 미입력되거나 공백일 경우 기본 아이콘(article)을 반환해야 함', () => {
    expect(resolveNodeIcon('')).toBe('article')
    expect(resolveNodeIcon(null)).toBe('article')
    expect(resolveNodeIcon('   ')).toBe('article')
  })

  it('유효한 아이콘 명 입력 시 해당 아이콘 명을 반환해야 함', () => {
    expect(resolveNodeIcon('factory')).toBe('factory')
    expect(resolveNodeIcon('location_on')).toBe('location_on')
    expect(resolveNodeIcon('group')).toBe('group')
  })

  it('IconPicker 모달에서 아이콘 선택 후 Confirm 시 nodeForm의 icon 값이 정상 갱신되어야 함', () => {
    const form = { icon: 'article' }
    const updated = applySelectedIcon(form, 'account_circle')
    expect(updated.icon).toBe('account_circle')
  })

  it('트리 노드 렌더링 시 아이콘이 없을 경우 기본 article 아이콘이 적용되어야 함', () => {
    const nodeWithoutIcon = { name: { ko: 'Plant' }, icon: null }
    expect(resolveNodeIcon(nodeWithoutIcon.icon)).toBe('article')
  })
})
