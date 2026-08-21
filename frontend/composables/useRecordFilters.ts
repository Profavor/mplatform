import { ref } from 'vue'

export interface FilterPreset {
  id: string
  name: string
  searchQuery: string
  activeFilters: Record<string, any>
  advancedFilters: Array<{ field: string; operator: string; value: any }>
}

export function useRecordFilters() {
  const searchQuery = ref('')
  const activeFilters = ref<Record<string, any>>({})
  const advancedFilters = ref<Array<{ field: string; operator: string; value: any }>>([])
  const dateRangeFilter = ref<{ start?: string; end?: string }>({})
  const filterPresets = ref<FilterPreset[]>([])
  const activePresetId = ref<string | null>(null)

  const setFilter = (key: string, value: any) => {
    activeFilters.value = {
      ...activeFilters.value,
      [key]: value
    }
  }

  const removeFilter = (key: string) => {
    const updated = { ...activeFilters.value }
    delete updated[key]
    activeFilters.value = updated
  }

  const resetFilters = () => {
    searchQuery.value = ''
    activeFilters.value = {}
    advancedFilters.value = []
    dateRangeFilter.value = {}
    activePresetId.value = null
  }

  const buildFilterParams = (): Record<string, any> => {
    const params: Record<string, any> = {}

    if (searchQuery.value.trim()) {
      params.search = searchQuery.value.trim()
    }

    Object.entries(activeFilters.value).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        params[`filter_${key}`] = val
      }
    })

    if (advancedFilters.value && advancedFilters.value.length > 0) {
      params.advancedFilters = JSON.stringify(advancedFilters.value)
    }

    if (dateRangeFilter.value.start) {
      params.startDate = dateRangeFilter.value.start
    }
    if (dateRangeFilter.value.end) {
      params.endDate = dateRangeFilter.value.end
    }

    return params
  }

  const saveFilterPreset = (name: string): FilterPreset => {
    const newPreset: FilterPreset = {
      id: 'preset_' + Date.now(),
      name,
      searchQuery: searchQuery.value,
      activeFilters: { ...activeFilters.value },
      advancedFilters: [...advancedFilters.value]
    }
    filterPresets.value.push(newPreset)
    return newPreset
  }

  const applyFilterPreset = (presetId: string) => {
    const target = filterPresets.value.find(p => p.id === presetId)
    if (!target) return
    searchQuery.value = target.searchQuery || ''
    activeFilters.value = { ...(target.activeFilters || {}) }
    advancedFilters.value = [...(target.advancedFilters || [])]
    activePresetId.value = presetId
  }

  const deleteFilterPreset = (presetId: string) => {
    filterPresets.value = filterPresets.value.filter(p => p.id !== presetId)
    if (activePresetId.value === presetId) {
      activePresetId.value = null
    }
  }

  return {
    searchQuery,
    activeFilters,
    advancedFilters,
    dateRangeFilter,
    filterPresets,
    activePresetId,
    setFilter,
    removeFilter,
    resetFilters,
    buildFilterParams,
    saveFilterPreset,
    applyFilterPreset,
    deleteFilterPreset
  }
}
