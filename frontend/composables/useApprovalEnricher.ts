import { ref } from 'vue'
import { useCookie } from '#app'
import { formatMultilingual } from './useMultilingual'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '~/stores/useUserStore'
import { useCodeStore } from '~/stores/useCodeStore'
import { formatWithTimezone } from '~/composables/useTimezoneDate'

export interface EnrichedApprovalRequest {
  [key: string]: any
  domainName?: string
  classificationName?: string
  idAttribute?: string
  nameAttribute?: string
  summary?: string
}

export const useApprovalEnricher = () => {
  const { t } = useI18n()
  const userStore = useUserStore()
  const codeStore = useCodeStore()
  const { customFetch } = useCustomFetch()
  const localeCookie = useCookie<string>('locale', { default: () => 'ko' })
  const domains = ref<Record<string, any>>({})
  const domainsFull = ref<Record<string, any>>({})
  const nodes = ref<Record<string, any>>({})
  const nodeToDomainMap = ref<Record<string, string>>({})
  const fieldSchemas = ref<Record<string, any[]>>({})

  const loadMetadata = async () => {
    try {
      // Preload DD codes
      codeStore.loadGroup('TARGET_TYPE').catch(console.error)
      
      const domRes = await customFetch('/api/domains')
      
      const dMap: Record<string, any> = {}
      const dFullMap: Record<string, any> = {}
      if (Array.isArray(domRes)) {
        domRes.forEach((d: any) => {
          dMap[d.id] = d.name
          dFullMap[d.id] = d
        })
      }
      domains.value = dMap
      domainsFull.value = dFullMap
      
      const nMap: Record<string, any> = {}
      const dMapping: Record<string, string> = {}
      
      const flatten = (list: any[], currentDomainId: string) => {
        list.forEach((n: any) => {
          nMap[n.id] = n.name
          dMapping[n.id] = currentDomainId
          if (n.children) flatten(n.children, currentDomainId)
        })
      }
      
      if (Array.isArray(domRes)) {
        const treePromises = domRes.map((d: any) => 
          customFetch(`/api/domains/${d.id}/nodes/tree`)
            .catch((e: any) => {
              console.error(`Failed to load tree for domain ${d.id}`, e)
              return []
            })
        )
        
        const treeResults = await Promise.all(treePromises)
        
        treeResults.forEach((treeRes: any, idx: number) => {
          if (Array.isArray(treeRes)) {
            flatten(treeRes, domRes[idx].id)
          }
        })
      }
      
      nodes.value = nMap
      nodeToDomainMap.value = dMapping
    } catch (e) {
      console.error('Failed to load domains/nodes metadata', e)
    }
  }

  const inFlightNodeRequests: Record<string, Promise<any[]>> = {}
  const getFieldsForNode = async (nodeId: string): Promise<any[]> => {
    if (!nodeId) return []
    if (fieldSchemas.value[nodeId]) return fieldSchemas.value[nodeId]
    if (inFlightNodeRequests[nodeId]) return await inFlightNodeRequests[nodeId]

    inFlightNodeRequests[nodeId] = (async () => {
      try {
        const fields = await customFetch(`/api/nodes/${nodeId}/fields/effective`)
        fieldSchemas.value[nodeId] = fields || []
        return fieldSchemas.value[nodeId]
      } catch (e) {
        console.error(`Failed to fetch fields for node ${nodeId}`, e)
        return []
      } finally {
        delete inFlightNodeRequests[nodeId]
      }
    })()

    return await inFlightNodeRequests[nodeId]
  }
  
  const getTranslatedName = (nameObj: any): string => {
    if (!nameObj) return ''
    let obj = nameObj
    if (typeof nameObj === 'string') {
      try {
        obj = JSON.parse(nameObj)
      } catch (e) {
        return nameObj
      }
    }
    if (typeof obj !== 'object' || obj === null) return String(obj)
    const locale = localeCookie.value || 'ko'
    return obj[locale] || obj.en || obj.ko || JSON.stringify(obj)
  }

  const enrichRequest = async (req: any): Promise<EnrichedApprovalRequest> => {
    const enriched: EnrichedApprovalRequest = { ...req, domainName: '', classificationName: '', idAttribute: '', nameAttribute: '', summary: '' }
    
    // In approvals.vue, changes is used. In admin.vue, requestedData is used.
    const rawData = req.changes || req.requestedData
    
    let parsed: any = {}
    if (rawData) {
      try {
        parsed = typeof rawData === 'string' ? JSON.parse(rawData) : rawData
        if (typeof parsed === 'string') parsed = JSON.parse(parsed)
      } catch (e) {
        console.error('Failed to parse rawData for enrichment', e)
      }
    }
    
    const classificationNode = req.classificationNode || {}
    const nodeId = classificationNode.id || parsed.nodeId
    const domainId = parsed.domainId || nodeToDomainMap.value[nodeId]
    
    if (domainId && domains.value[domainId]) {
      enriched.domainName = getTranslatedName(domains.value[domainId])
    }
    if (nodeId && nodes.value[nodeId]) {
      enriched.classificationName = getTranslatedName(nodes.value[nodeId])
    }
    
    if (nodeId) {
      try {
        let recordData: Record<string, any> = {}
        let previousData: Record<string, any> = {}
        
        if (req.targetType === 'RECORD_UPDATE') {
          recordData = parsed.after || {}
          previousData = parsed.before || {}
        } else {
          recordData = parsed.data || parsed || {}
        }
        
        const fields = await getFieldsForNode(nodeId)
        
        const fullDomain = domainsFull.value[domainId] || {}
        const idFieldId = fullDomain.identifierFieldId
        const nameFieldId = fullDomain.displayNameFieldId
        
        const idField = fields.find((f: any) => f.id === idFieldId || f.isIdentifier === true)
        const nameField = fields.find((f: any) => f.id === nameFieldId || f.isDisplayName === true)
        
        if (idField && recordData[idField.key] !== undefined && recordData[idField.key] !== null && recordData[idField.key] !== '') {
          enriched.idAttribute = formatMultilingual(recordData[idField.key])
        }
        if (nameField && recordData[nameField.key] !== undefined && recordData[nameField.key] !== null && recordData[nameField.key] !== '') {
          enriched.nameAttribute = formatMultilingual(recordData[nameField.key])
        }
        
        // Summary logic
        if (req.targetType === 'RECORD_CREATE' || req.targetType === 'RECORD') {
          const parts: string[] = []
          for (const key in recordData) {
            const field = fields.find((f: any) => f.key === key)
            const fName = field ? getTranslatedName(field.name) : key
            let val = recordData[key]
            if (typeof val === 'object' && val !== null) val = getTranslatedName(val)
            parts.push(`${fName}: ${val}`)
          }
          enriched.summary = parts.join(', ')
        } else if (req.targetType === 'RECORD_UPDATE') {
          const parts: string[] = []
          for (const key in recordData) {
            if (JSON.stringify(recordData[key]) !== JSON.stringify(previousData[key])) {
              const field = fields.find((f: any) => f.key === key)
              const fName = field ? getTranslatedName(field.name) : key
              let oldVal = previousData[key]
              let newVal = recordData[key]
              if (typeof oldVal === 'object' && oldVal !== null) oldVal = getTranslatedName(oldVal)
              if (typeof newVal === 'object' && newVal !== null) newVal = getTranslatedName(newVal)
              const noneLabel = t('none')
              parts.push(`${fName}: ${oldVal || noneLabel} -> ${newVal || noneLabel}`)
            }
          }
          enriched.summary = parts.join(', ')
        } else if (req.targetType === 'RECORD_DELETE') {
          enriched.summary = t('record_delete')
        }
      } catch(e) {
        console.error('Failed to enrich summary', e)
      }
    } else if (req.targetType === 'MEMO') {
      const memoTitle = parsed.title || req.title || ''
      enriched.idAttribute = memoTitle
      enriched.nameAttribute = memoTitle
      const cleanContent = parsed.content ? String(parsed.content).replace(/<[^>]*>?/gm, '').trim() : ''
      enriched.summary = cleanContent ? (cleanContent.length > 80 ? cleanContent.substring(0, 80) + '...' : cleanContent) : memoTitle
    }
    
    return enriched
  }

  const getRequesterName = (req: any): string => {
    if (!req) return t('unknown')
    return userStore.getUserName(req.requesterId, req.requesterName || req.requesterUsername)
  }

  const getClassificationName = (node: any, field: string): string => {
    const unclassified = t('unclassified')
    if (!node || !node[field]) return unclassified
    const nameObj = node[field]
    if (typeof nameObj === 'string') return nameObj
    return nameObj[localeCookie.value || 'ko'] || nameObj['ko'] || nameObj['en'] || unclassified
  }

  const getRequestTypeLabel = (type: string): string => {
    if (!type) return t('other_request')
    
    // 1. Check CodeStore first (100% DB Common Code)
    const codeName = codeStore.getCodeName('TARGET_TYPE', type, null)
    if (codeName && codeName !== type) return codeName

    // 2. Check i18n locales
    const i18nKey = `target_type_${type}`
    const translated = t(i18nKey)
    if (translated && translated !== i18nKey) return translated

    const i18nKeyLower = `target_type_${type.toLowerCase()}`
    const translatedLower = t(i18nKeyLower)
    if (translatedLower && translatedLower !== i18nKeyLower) return translatedLower

    return type || t('other_request')
  }

  const getRequestTypeColor = (type: string): string => {
    if (type === 'RECORD_CREATE' || type === 'RECORD' || type === 'DOMAIN_RECORD_CREATE') return 'success'
    if (type === 'RECORD_UPDATE') return 'warning'
    if (type === 'RECORD_DELETE') return 'danger'
    if (type?.startsWith('SCHEMA')) return 'info'
    if (type === 'MEMO') return 'primary'
    return 'secondary'
  }

  const createTargetTypeBadgeElement = (type: string, isDarkTheme = false): HTMLElement => {
    const label = getRequestTypeLabel(type)
    const colorKey = getRequestTypeColor(type)
    
    const colorMapLight: Record<string, { bg: string; text: string; border: string }> = {
      success: { bg: '#ecfdf5', text: '#059669', border: '#a7f3d0' },
      warning: { bg: '#fffbeb', text: '#d97706', border: '#fde68a' },
      danger: { bg: '#fef2f2', text: '#dc2626', border: '#fecaca' },
      info: { bg: '#eff6ff', text: '#2563eb', border: '#bfdbfe' },
      primary: { bg: '#eef2ff', text: '#4f46e5', border: '#c7d2fe' },
      secondary: { bg: '#f3f4f6', text: '#4b5563', border: '#e5e7eb' }
    }
    
    const colorMapDark: Record<string, { bg: string; text: string; border: string }> = {
      success: { bg: 'rgba(16, 185, 129, 0.2)', text: '#34d399', border: 'rgba(16, 185, 129, 0.4)' },
      warning: { bg: 'rgba(245, 158, 11, 0.2)', text: '#fbbf24', border: 'rgba(245, 158, 11, 0.4)' },
      danger: { bg: 'rgba(239, 68, 68, 0.2)', text: '#f87171', border: 'rgba(239, 68, 68, 0.4)' },
      info: { bg: 'rgba(59, 130, 246, 0.2)', text: '#60a5fa', border: 'rgba(59, 130, 246, 0.4)' },
      primary: { bg: 'rgba(99, 102, 241, 0.2)', text: '#818cf8', border: 'rgba(99, 102, 241, 0.4)' },
      secondary: { bg: 'rgba(156, 163, 175, 0.2)', text: '#9ca3af', border: 'rgba(156, 163, 175, 0.4)' }
    }

    const themeMap = isDarkTheme ? colorMapDark : colorMapLight
    const theme = themeMap[colorKey] || themeMap.secondary

    const span = document.createElement('span')
    span.style.display = 'inline-flex'
    span.style.alignItems = 'center'
    span.style.justifyContent = 'center'
    span.style.padding = '3px 10px'
    span.style.borderRadius = '9999px'
    span.style.fontSize = '0.75rem'
    span.style.fontWeight = '600'
    span.style.backgroundColor = theme.bg
    span.style.color = theme.text
    span.style.border = `1px solid ${theme.border}`
    span.style.whiteSpace = 'nowrap'
    span.style.letterSpacing = '-0.01em'
    span.innerText = label
    return span
  }

  const formatTargetInfo = (req: any): string => {
    if (!req) return ''
    const tType = req.targetType || ''
    if (tType === 'MEMO') {
      return t('memo_approval', '메모 결재')
    }

    // 1. Direct domainName / classificationName from API or enriched object
    let dName = req.domainName ? formatMultilingual(req.domainName) : ''
    let cName = req.classificationName ? formatMultilingual(req.classificationName) : ''

    // 2. Check classificationNode structure
    if (!dName && req.classificationNode?.domain?.name) {
      dName = formatMultilingual(req.classificationNode.domain.name)
    }
    if (!cName && req.classificationNode?.name) {
      cName = formatMultilingual(req.classificationNode.name)
    }

    // 3. Check loaded metadata mappings by IDs
    const nodeId = req.classificationNode?.id || req.nodeId
    const domainId = req.domainId || req.classificationNode?.domainId || (nodeId ? nodeToDomainMap.value[nodeId] : null)
    if (!dName && domainId && domains.value[domainId]) {
      dName = getTranslatedName(domains.value[domainId])
    }
    if (!cName && nodeId && nodes.value[nodeId]) {
      cName = getTranslatedName(nodes.value[nodeId])
    }

    if (dName && cName) return `${dName} > ${cName}`
    if (dName) return dName
    if (cName) return cName
    return t('general_approval', '일반 결재')
  }

  const formatDate = (dateString: string | Date | null | undefined): string => {
    return formatWithTimezone(dateString)
  }

  return {
    loadMetadata,
    enrichRequest,
    domains,
    nodes,
    getRequesterName,
    getClassificationName,
    getRequestTypeLabel,
    getRequestTypeColor,
    createTargetTypeBadgeElement,
    formatTargetInfo,
    formatDate
  }
}
