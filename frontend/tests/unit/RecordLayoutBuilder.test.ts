import { describe, it, expect } from 'vitest'

describe('Record 2D Grid Layout Builder & Renderer Logic', () => {
  // Helper for grid calculation
  const calculateWidgetBounds = (widget: { x: number; y: number; w: number; h: number }, rowHeight = 44) => {
    const minCols = 1
    const maxCols = 12
    const minRows = 1
    const maxRows = 30

    const clampedW = Math.max(minCols, Math.min(maxCols, widget.w))
    const clampedH = Math.max(minRows, Math.min(maxRows, widget.h))
    const clampedX = Math.max(0, Math.min(maxCols - clampedW, widget.x))
    const clampedY = Math.max(0, Math.min(maxRows - clampedH, widget.y))

    const pixelHeight = clampedH * rowHeight

    return {
      x: clampedX,
      y: clampedY,
      w: clampedW,
      h: clampedH,
      pixelHeight,
    }
  }

  it('이미지 위젯의 3x4 또는 2x4 2D 그리드 영역 계산이 정확해야 한다', () => {
    const imageWidget1 = { id: 'w_img1', type: 'IMAGE', fieldKey: 'profile_photo', x: 0, y: 0, w: 3, h: 4 }
    const bounds1 = calculateWidgetBounds(imageWidget1, 44)

    expect(bounds1.w).toBe(3)
    expect(bounds1.h).toBe(4)
    expect(bounds1.pixelHeight).toBe(4 * 44) // 176px

    const imageWidget2 = { id: 'w_img2', type: 'IMAGE', fieldKey: 'thumbnail', x: 4, y: 0, w: 2, h: 4 }
    const bounds2 = calculateWidgetBounds(imageWidget2, 44)

    expect(bounds2.w).toBe(2)
    expect(bounds2.h).toBe(4)
    expect(bounds2.pixelHeight).toBe(4 * 44) // 176px
  })

  it('웹에디터(Tiptap) 위젯의 높이 7~8칸 드래그 확장 시 높이 픽셀이 정상 계산되어야 한다', () => {
    const editorWidget1 = { id: 'w_ed1', type: 'EDITOR', fieldKey: 'content_html', x: 0, y: 4, w: 12, h: 7 }
    const bounds1 = calculateWidgetBounds(editorWidget1, 44)

    expect(bounds1.w).toBe(12)
    expect(bounds1.h).toBe(7)
    expect(bounds1.pixelHeight).toBe(7 * 44) // 308px

    const editorWidget2 = { id: 'w_ed2', type: 'EDITOR', fieldKey: 'description', x: 0, y: 4, w: 12, h: 8 }
    const bounds2 = calculateWidgetBounds(editorWidget2, 44)

    expect(bounds2.w).toBe(12)
    expect(bounds2.h).toBe(8)
    expect(bounds2.pixelHeight).toBe(8 * 44) // 352px
  })

  it('12 컬럼을 초과하는 W나 X 좌표는 캔버스 경계 내로 클램핑되어야 한다', () => {
    const overflowWidget = { id: 'w_over', type: 'FIELD', fieldKey: 'notes', x: 10, y: 0, w: 6, h: 2 }
    const bounds = calculateWidgetBounds(overflowWidget, 44)

    expect(bounds.w).toBe(6)
    // x should be clamped so x + w <= 12 (12 - 6 = 6)
    expect(bounds.x).toBe(6)
  })

  it('파레트 미배치 필드 목록 필터링이 정상 동작해야 한다', () => {
    const allFields = [
      { id: 'f1', key: 'emp_no', name: { ko: '사번' } },
      { id: 'f2', key: 'emp_name', name: { ko: '성명' } },
      { id: 'f3', key: 'photo', name: { ko: '증명사진' } },
      { id: 'f4', key: 'intro', name: { ko: '자기소개' } }
    ]

    const placedWidgets = [
      { id: 'w1', type: 'IMAGE', fieldKey: 'photo', x: 0, y: 0, w: 3, h: 4 },
      { id: 'w2', type: 'FIELD', fieldKey: 'emp_no', x: 3, y: 0, w: 4, h: 1 }
    ]

    const placedKeys = new Set(placedWidgets.map(w => w.fieldKey))
    const unplacedFields = allFields.filter(f => !placedKeys.has(f.key))

    expect(unplacedFields.length).toBe(2)
    expect(unplacedFields.map(f => f.key)).toEqual(['emp_name', 'intro'])
  })
})
