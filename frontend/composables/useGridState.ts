/**
 * AG-Grid 컬럼 너비/순서/정렬 상태 저장 및 복원 Composable
 */

export function serializeGridState(state: any): string {
  try {
    return JSON.stringify(state)
  } catch {
    return ''
  }
}

export function deserializeGridState(jsonStr: string | null): any | null {
  if (!jsonStr) return null
  try {
    return JSON.parse(jsonStr)
  } catch {
    return null
  }
}

export function useGridState(gridKey: string) {
  const storageKey = `ag_grid_state_${gridKey}`

  const saveState = (gridApi: any) => {
    if (!gridApi || !gridKey) return
    try {
      const columnState = gridApi.getColumnState()
      const stateObj = { columnState }
      const serialized = serializeGridState(stateObj)
      if (typeof window !== 'undefined' && window.localStorage) {
        localStorage.setItem(storageKey, serialized)
      }
    } catch (e) {
      console.error('Failed to save grid state:', e)
    }
  }

  const restoreState = (gridApi: any) => {
    if (!gridApi || !gridKey) return
    try {
      if (typeof window !== 'undefined' && window.localStorage) {
        const saved = localStorage.getItem(storageKey)
        const parsed = deserializeGridState(saved)
        if (parsed && parsed.columnState) {
          gridApi.applyColumnState({
            state: parsed.columnState,
            applyOrder: true,
          })
        }
      }
    } catch (e) {
      console.error('Failed to restore grid state:', e)
    }
  }

  return {
    saveState,
    restoreState,
  }
}
