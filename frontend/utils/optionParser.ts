/**
 * Utility to parse field.options into standard [{ text, value, order }] array for va-select.
 * Supports:
 * - JSON Object with optionsList: { optionsList: [{ key, label: { ko, en }, value }] }
 * - JSON Object with options/items/list: { options: [...] }
 * - Direct Array: [{ key, label, value }] or ['A', 'B']
 * - Comma-separated string: "A,B,C"
 * - JSON Array string: '[{"value": "1", "label": "One"}]'
 */
export interface SelectOptionItem {
  text: string
  value: any
  order?: number
}

export function parseOptions(opts: any, currentLocale: string = 'ko'): SelectOptionItem[] {
  if (!opts) return []
  let rawList: any = opts

  if (typeof opts === 'string') {
    const trimmed = opts.trim()
    if (trimmed.startsWith('[') || trimmed.startsWith('{')) {
      try {
        rawList = JSON.parse(trimmed)
      } catch (e) {
        return trimmed.split(',').map((s) => ({ text: s.trim(), value: s.trim(), order: 0 }))
      }
    } else if (trimmed.includes(',')) {
      return trimmed.split(',').map((s) => ({ text: s.trim(), value: s.trim(), order: 0 }))
    } else if (trimmed.length > 0) {
      return [{ text: trimmed, value: trimmed, order: 0 }]
    } else {
      return []
    }
  }

  // Handle object wrappers like { optionsList: [...] }, { options: [...] }, { items: [...] }
  if (rawList && typeof rawList === 'object' && !Array.isArray(rawList)) {
    rawList = rawList.optionsList || rawList.options || rawList.items || rawList.list || rawList.data || []
  }

  if (Array.isArray(rawList)) {
    const mapped = rawList.map((o: any) => {
      if (typeof o === 'string' || typeof o === 'number') {
        return { text: String(o), value: String(o), order: 0 }
      }
      if (o && typeof o === 'object') {
        const val = o.value !== undefined ? o.value : (o.key !== undefined ? o.key : (o.code !== undefined ? o.code : ''))
        
        let rawLabel = o.label !== undefined ? o.label : (o.name !== undefined ? o.name : (o.text !== undefined ? o.text : (o.title || o.displayName)))
        let textLabel = ''
        
        if (rawLabel && typeof rawLabel === 'object') {
          const loc = currentLocale || 'ko'
          textLabel = rawLabel[loc] || rawLabel.ko || rawLabel.en || Object.values(rawLabel)[0] || String(val)
        } else if (typeof rawLabel === 'string' && rawLabel.trim()) {
          if (rawLabel.trim().startsWith('{')) {
            try {
              const parsedLabel = JSON.parse(rawLabel)
              const loc = currentLocale || 'ko'
              textLabel = parsedLabel[loc] || parsedLabel.ko || parsedLabel.en || Object.values(parsedLabel)[0] || String(val)
            } catch {
              textLabel = rawLabel
            }
          } else {
            textLabel = rawLabel
          }
        } else {
          textLabel = val !== undefined && val !== null ? String(val) : ''
        }

        return {
          value: val,
          text: String(textLabel || val || ''),
          order: o.order !== undefined ? o.order : (o.sortOrder !== undefined ? o.sortOrder : 0)
        }
      }
      return { text: String(o), value: String(o), order: 0 }
    })
    return mapped.sort((a, b) => (a.order || 0) - (b.order || 0))
  }

  return []
}
