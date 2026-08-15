import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface SearchFilterItem {
  field: string
  operator: string
  value: any
}

export interface SearchPreset {
  id: string
  domainId: string
  name: string
  filters: SearchFilterItem[]
  createdAt: string
}

export const useSearchPresetStore = defineStore('searchPresets', () => {
  const presets = ref<SearchPreset[]>([])

  const STORAGE_KEY = 'mdm_search_presets'

  const loadFromStorage = () => {
    if (typeof localStorage !== 'undefined') {
      try {
        const saved = localStorage.getItem(STORAGE_KEY)
        if (saved) {
          presets.value = JSON.parse(saved)
        }
      } catch {}
    }
  }

  const saveToStorage = () => {
    if (typeof localStorage !== 'undefined') {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(presets.value))
      } catch {}
    }
  }

  loadFromStorage()

  const getPresetsByDomain = (domainId: string): SearchPreset[] => {
    return presets.value.filter(p => p.domainId === domainId)
  }

  const savePreset = (domainId: string, data: { name: string; filters: SearchFilterItem[] }): SearchPreset => {
    const newPreset: SearchPreset = {
      id: `PRESET-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
      domainId,
      name: data.name,
      filters: data.filters || [],
      createdAt: new Date().toISOString()
    }
    presets.value.push(newPreset)
    saveToStorage()
    return newPreset
  }

  const deletePreset = (presetId: string) => {
    presets.value = presets.value.filter(p => p.id !== presetId)
    saveToStorage()
  }

  return {
    presets,
    getPresetsByDomain,
    savePreset,
    deletePreset
  }
})
