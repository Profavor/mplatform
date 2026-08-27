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

  it('다국어 객체 형태의 레이아웃 명칭(KO/EN)을 현재 로케일에 맞게 정확히 반환해야 한다', () => {
    const getLayoutDisplayName = (layout: any, currentLocale: string) => {
      if (!layout) return ''
      if (typeof layout.name === 'object' && layout.name !== null) {
        return layout.name[currentLocale] || layout.name.ko || layout.name.en || layout.id
      }
      return layout.name || layout.id
    }

    const multiLangLayout = {
      id: 'layout_1',
      name: { ko: '상세 계약 뷰', en: 'Detailed Contract View' }
    }

    expect(getLayoutDisplayName(multiLangLayout, 'ko')).toBe('상세 계약 뷰')
    expect(getLayoutDisplayName(multiLangLayout, 'en')).toBe('Detailed Contract View')
  })

  it('레거시 단일 문자열 레이아웃 명칭도 하위 호환되어 정상 반환되어야 한다', () => {
    const getLayoutDisplayName = (layout: any, currentLocale: string) => {
      if (!layout) return ''
      if (typeof layout.name === 'object' && layout.name !== null) {
        return layout.name[currentLocale] || layout.name.ko || layout.name.en || layout.id
      }
      return layout.name || layout.id
    }

    const legacyLayout = {
      id: 'layout_legacy',
      name: '기본 레이아웃'
    }

    expect(getLayoutDisplayName(legacyLayout, 'ko')).toBe('기본 레이아웃')
    expect(getLayoutDisplayName(legacyLayout, 'en')).toBe('기본 레이아웃')
  })

  it('서브테이블 컬럼명 및 위젯 타이틀의 다국어 객체가 현재 로케일로 올바르게 해석되어야 한다', () => {
    const getTranslatedColName = (name: any, currentLocale: string) => {
      if (!name) return ''
      if (typeof name === 'object') {
        return name[currentLocale] || name.ko || name.en || ''
      }
      return String(name)
    }

    const colObj = { ko: '학교명', en: 'School Name' }
    expect(getTranslatedColName(colObj, 'ko')).toBe('학교명')
    expect(getTranslatedColName(colObj, 'en')).toBe('School Name')

    const stringCol = 'Custom Col'
    expect(getTranslatedColName(stringCol, 'ko')).toBe('Custom Col')
  })

  // 2D Collision Push-down Algorithm Logic Helper
  const isOverlapping = (
    w1: { x: number; y: number; w: number; h: number },
    w2: { x: number; y: number; w: number; h: number }
  ) => {
    return !(
      w1.x + w1.w <= w2.x ||
      w2.x + w2.w <= w1.x ||
      w1.y + w1.h <= w2.y ||
      w2.y + w2.h <= w1.y
    )
  }

  const resolveWidgetCollisions = (
    targetWidget: { id: string; x: number; y: number; w: number; h: number },
    allWidgets: Array<{ id: string; x: number; y: number; w: number; h: number }>
  ) => {
    const others = allWidgets.filter(w => w.id !== targetWidget.id)
    let changed = true
    let iterations = 0
    const maxIterations = 50

    while (changed && iterations < maxIterations) {
      changed = false
      iterations++

      // 1. Check collisions with targetWidget
      for (const other of others) {
        if (isOverlapping(targetWidget, other)) {
          other.y = targetWidget.y + targetWidget.h
          changed = true
        }
      }

      // 2. Cascading check among other widgets (sorted by y ascending)
      others.sort((a, b) => a.y - b.y || a.x - b.x)
      for (let i = 0; i < others.length; i++) {
        for (let j = i + 1; j < others.length; j++) {
          if (isOverlapping(others[i], others[j])) {
            others[j].y = others[i].y + others[i].h
            changed = true
          }
        }
      }
    }
  }

  it('위젯 중간 끼워넣기 시 동일 위치의 기존 위젯이 아래로 밀려나야 한다', () => {
    const w1 = { id: 'w1', x: 0, y: 0, w: 6, h: 2 }
    const wNew = { id: 'wNew', x: 0, y: 0, w: 6, h: 2 }
    const allWidgets = [w1, wNew]

    resolveWidgetCollisions(wNew, allWidgets)

    expect(wNew.y).toBe(0)
    expect(w1.y).toBe(2) // wNew(0+2) 아래로 밀려남
    expect(isOverlapping(wNew, w1)).toBe(false)
  })

  it('위젯 중간 끼워넣기 시 연쇄적으로 위치한 하위 위젯들도 순차적으로 밀려나야 한다', () => {
    const w1 = { id: 'w1', x: 0, y: 0, w: 6, h: 2 }
    const w2 = { id: 'w2', x: 0, y: 2, w: 6, h: 2 }
    const w3 = { id: 'w3', x: 0, y: 4, w: 6, h: 2 }
    const wNew = { id: 'wNew', x: 0, y: 0, w: 6, h: 3 } // 높이 3짜리 위젯 끼워넣기
    const allWidgets = [w1, w2, w3, wNew]

    resolveWidgetCollisions(wNew, allWidgets)

    expect(wNew.y).toBe(0)
    expect(w1.y).toBe(3) // wNew(0+3) 바로 아래
    expect(w2.y).toBe(5) // w1(3+2) 바로 아래
    expect(w3.y).toBe(7) // w2(5+2) 바로 아래

    // 어떤 위젯도 겹치지 않아야 함
    expect(isOverlapping(wNew, w1)).toBe(false)
    expect(isOverlapping(w1, w2)).toBe(false)
    expect(isOverlapping(w2, w3)).toBe(false)
  })

  it('가로로 분할된 영역(X좌표가 다른 위젯)은 Y축 충돌이 없으므로 밀려나지 않아야 한다', () => {
    const leftWidget = { id: 'left', x: 0, y: 0, w: 6, h: 4 }
    const rightWidget = { id: 'right', x: 6, y: 0, w: 6, h: 4 }
    const insertedLeft = { id: 'insLeft', x: 0, y: 0, w: 6, h: 2 }
    const allWidgets = [leftWidget, rightWidget, insertedLeft]

    resolveWidgetCollisions(insertedLeft, allWidgets)

    expect(insertedLeft.y).toBe(0)
    expect(leftWidget.y).toBe(2) // 좌측 위젯만 밀려남
    expect(rightWidget.y).toBe(0) // 우측 위젯은 x=6으로 겹치지 않으므로 y=0 유지
  })

  // Helper functions under test
  const calculateDefaultWidgetWidth = (field: { type?: string; key?: string; gridWidth?: number; colSpan?: number; layoutWidth?: number }, maxCols = 12) => {
    const isImage = field.type === 'IMAGE' || (field.key || '').includes('photo') || (field.key || '').includes('image')
    const isEditor = field.type === 'HTML' || field.type === 'RICHTEXT' || (field.key || '').includes('desc') || (field.key || '').includes('content')
    const isTable = field.type === 'TABLE' || field.type === 'JSON'

    if (isEditor || isTable) return maxCols
    if (isImage) return 3

    // DO NOT use field.gridWidth (which is pixel width like 160px)
    const customCol = field.colSpan || field.layoutWidth
    if (customCol && typeof customCol === 'number' && customCol >= 1 && customCol <= maxCols) {
      return customCol
    }
    return 4 // default 4 cols (3 per row)
  }

  const sanitizeWidgetDimensions = (widget: { x?: number; y?: number; w?: number; h?: number }, maxCols = 12) => {
    let w = widget.w || 4
    if (w > maxCols) {
      w = 4
    }
    w = Math.max(1, Math.min(maxCols, w))
    const x = Math.max(0, Math.min(maxCols - w, widget.x || 0))
    const h = Math.max(1, widget.h || 1)
    const y = Math.max(0, widget.y || 0)
    return { ...widget, x, y, w, h }
  }

  const calculateResizeBounds = (
    widget: { x: number; y: number; w: number; h: number },
    startW: number,
    startH: number,
    colDelta: number,
    rowDelta: number,
    direction: 'e' | 's' | 'se',
    maxCols = 12
  ) => {
    let newW = startW
    let newH = startH

    if (direction === 'e' || direction === 'se') {
      newW = Math.max(1, Math.min(maxCols - widget.x, startW + colDelta))
    }
    if (direction === 's' || direction === 'se') {
      newH = Math.max(1, Math.min(30, startH + rowDelta))
    }
    return { newW, newH }
  }

  it('AG-Grid용 gridWidth(예: 160px)가 있어도 레이아웃 가로칸수(w)는 4칸으로 정상 산출되어야 한다', () => {
    const fieldWith160Px = { key: 'customer_no', type: 'TEXT', gridWidth: 160 }
    const width = calculateDefaultWidgetWidth(fieldWith160Px, 12)
    expect(width).toBe(4)
    expect(width).not.toBe(160)
  })

  it('에디터 및 테이블 필드는 가로 12칸 전체를 차지해야 한다', () => {
    const editorField = { key: 'content_html', type: 'HTML', gridWidth: 300 }
    const tableField = { key: 'items', type: 'TABLE', gridWidth: 400 }
    expect(calculateDefaultWidgetWidth(editorField, 12)).toBe(12)
    expect(calculateDefaultWidgetWidth(tableField, 12)).toBe(12)
  })

  it('기존에 w=160으로 잘못 저장된 레거시 위젯은 12컬럼 이하(기본 4칸)로 자동 정규화되어야 한다', () => {
    const legacyWidget = { id: 'leg1', x: 0, y: 0, w: 160, h: 1 }
    const sanitized = sanitizeWidgetDimensions(legacyWidget, 12)
    expect(sanitized.w).toBe(4)
    expect(sanitized.x).toBe(0)
    expect(sanitized.w + sanitized.x).toBeLessThanOrEqual(12)
  })

  it('우측 경계(East) 리사이즈 핸들 드래그 시 높이는 유지되고 너비(w)만 1~12 사이로 변경되어야 한다', () => {
    const widget = { id: 'w1', x: 0, y: 0, w: 4, h: 2 }
    // 2칸 늘리기
    const resizedPlus = calculateResizeBounds(widget, 4, 2, 2, 5, 'e', 12)
    expect(resizedPlus.newW).toBe(6)
    expect(resizedPlus.newH).toBe(2) // 높이는 유지

    // 12칸 초과 드래그 시 12로 클램핑
    const resizedMax = calculateResizeBounds(widget, 4, 2, 20, 0, 'e', 12)
    expect(resizedMax.newW).toBe(12)
  })

  it('위젯의 x 좌표가 8일 때 우측 리사이즈는 남은 컬럼(4칸)을 초과할 수 없어야 한다', () => {
    const widget = { id: 'w1', x: 8, y: 0, w: 2, h: 1 }
    const resized = calculateResizeBounds(widget, 2, 1, 10, 0, 'e', 12)
    expect(resized.newW).toBe(4) // 12 - 8 = 4
    expect(widget.x + resized.newW).toBe(12)
  })
})


