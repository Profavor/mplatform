import { ref } from 'vue'
import { useCookie } from '#app'
import { formatMultilingual } from './useMultilingual'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '~/stores/useUserStore'
import { useCodeStore } from '~/stores/useCodeStore'

export const useApprovalEnricher = () => {
  const { t } = useI18n()
  const userStore = useUserStore()
  const codeStore = useCodeStore()
  const token = useCookie('auth_token')
  const localeCookie = useCookie('locale', { default: () => 'ko' })
  const domains = ref({})
  const domainsFull = ref({})
  const nodes = ref({})
  const nodeToDomainMap = ref({})
  const fieldSchemas = ref({})

  const loadMetadata = async () => {
    try {
      // Preload DD codes
      codeStore.loadGroup('TARGET_TYPE').catch(console.error)
      
      const domRes = await $fetch('/api/domains', { headers: { Authorization: `Bearer ${token.value}` } })
      
      const dMap = {}
      const dFullMap = {}
      domRes.forEach(d => {
        dMap[d.id] = d.name
        dFullMap[d.id] = d
      })
      domains.value = dMap
      domainsFull.value = dFullMap
      
      const nMap = {}
      const dMapping = {}
      
      const flatten = (list, currentDomainId) => {
        list.forEach(n => {
          nMap[n.id] = n.name
          dMapping[n.id] = currentDomainId
          if (n.children) flatten(n.children, currentDomainId)
        })
      }
      
      const treePromises = domRes.map(d => 
        $fetch(`/api/domains/${d.id}/nodes/tree`, { headers: { Authorization: `Bearer ${token.value}` } })
          .catch(e => {
            console.error(`Failed to load tree for domain ${d.id}`, e)
            return []
          })
      )
      
      const treeResults = await Promise.all(treePromises)
      
      treeResults.forEach((treeRes, idx) => {
        flatten(treeRes, domRes[idx].id)
      })
      
      nodes.value = nMap
      nodeToDomainMap.value = dMapping
    } catch (e) {
      console.error('Failed to load domains/nodes metadata', e)
    }
  }

  const inFlightNodeRequests = {}
  const getFieldsForNode = async (nodeId) => {
    if (!nodeId) return []
    if (fieldSchemas.value[nodeId]) return fieldSchemas.value[nodeId]
    if (inFlightNodeRequests[nodeId]) return await inFlightNodeRequests[nodeId]

    inFlightNodeRequests[nodeId] = (async () => {
      try {
        const fields = await $fetch(`/api/nodes/${nodeId}/fields/effective`, {
          headers: { Authorization: `Bearer ${token.value}` }
        })
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
  
  const getTranslatedName = (nameObj) => {
    if (!nameObj) return ''
    let obj = nameObj
    if (typeof nameObj === 'string') {
      try {
        obj = JSON.parse(nameObj)
      } catch (e) {
        return nameObj
      }
    }
    if (typeof obj !== 'object' || obj === null) return obj
    const locale = localeCookie.value || 'ko'
    return obj[locale] || obj.en || obj.ko || JSON.stringify(obj)
  }

  const enrichRequest = async (req) => {
    const enriched = { ...req, domainName: '', classificationName: '', idAttribute: '', nameAttribute: '', summary: '' }
    
    // In approvals.vue, changes is used. In admin.vue, requestedData is used.
    const rawData = req.changes || req.requestedData
    
    let parsed = {}
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
        let recordData = {}
        let previousData = {}
        
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
        
        const idField = fields.find(f => f.id === idFieldId)
        const nameField = fields.find(f => f.id === nameFieldId)
        
        if (idField && recordData[idField.key] !== undefined) {
          enriched.idAttribute = formatMultilingual(recordData[idField.key])
        }
        if (nameField && recordData[nameField.key] !== undefined) {
          enriched.nameAttribute = formatMultilingual(recordData[nameField.key])
        }
        
        // Summary logic
        if (req.targetType === 'RECORD_CREATE' || req.targetType === 'RECORD') {
          const parts = []
          for (const key in recordData) {
            const field = fields.find(f => f.key === key)
            const fName = field ? getTranslatedName(field.name) : key
            let val = recordData[key]
            if (typeof val === 'object' && val !== null) val = getTranslatedName(val)
            parts.push(`${fName}: ${val}`)
          }
          enriched.summary = parts.join(', ')
        } else if (req.targetType === 'RECORD_UPDATE') {
          const parts = []
          for (const key in recordData) {
            if (JSON.stringify(recordData[key]) !== JSON.stringify(previousData[key])) {
              const field = fields.find(f => f.key === key)
              const fName = field ? getTranslatedName(field.name) : key
              let oldVal = previousData[key]
              let newVal = recordData[key]
              if (typeof oldVal === 'object' && oldVal !== null) oldVal = getTranslatedName(oldVal)
              if (typeof newVal === 'object' && newVal !== null) newVal = getTranslatedName(newVal)
              const noneLabel = t('none') || 'None'
              parts.push(`${fName}: ${oldVal || noneLabel} -> ${newVal || noneLabel}`)
            }
          }
          enriched.summary = parts.join(', ')
        } else if (req.targetType === 'RECORD_DELETE') {
          enriched.summary = t('record_delete') || 'Data Delete'
        }

        // Fallback: If ID or Name is still empty, infer from record data keys (Summary Data)
        if (!enriched.idAttribute || !enriched.nameAttribute) {
          // 1st pass: try to find by common keywords
          for (const key in recordData) {
            const field = fields.find(f => f.key === key)
            const fName = field ? getTranslatedName(field.name) : key
            const fNameLower = String(fName).toLowerCase()
            const keyLower = String(key).toLowerCase()
            
            if (!enriched.idAttribute && (
                fNameLower.includes('코드') || fNameLower.includes('번호') || fNameLower.includes('사번') || 
                fNameLower.includes('id') || fNameLower.includes('code') || fNameLower.includes('ticker') || fNameLower.includes('no') ||
                keyLower.includes('id') || keyLower.includes('code') || keyLower.includes('ticker') || keyLower.includes('no')
            )) {
              enriched.idAttribute = recordData[key]
            }
            if (!enriched.nameAttribute && (
                fNameLower.includes('명') || fNameLower.includes('이름') || fNameLower.includes('name') || fNameLower.includes('title') ||
                keyLower.includes('name') || keyLower.includes('title')
            )) {
              enriched.nameAttribute = formatMultilingual(recordData[key])
            }
          }
          
          // 2nd pass: if still empty, pick the first available non-object value for ID and second for Name
          if (!enriched.idAttribute || !enriched.nameAttribute) {
            const keys = Object.keys(recordData)
            if (!enriched.idAttribute && keys.length > 0) enriched.idAttribute = formatMultilingual(recordData[keys[0]])
            if (!enriched.nameAttribute && keys.length > 1) enriched.nameAttribute = formatMultilingual(recordData[keys[1]])
          }
        }
      } catch(e) {
        console.error('Failed to enrich summary', e)
      }
    }
    
    return enriched
  }

  const getRequesterName = (req) => {
    if (!req) return t('unknown') || 'Unknown';
    return userStore.getUserName(req.requesterId, req.requesterName || req.requesterUsername);
  }

  const getClassificationName = (node, field) => {
    const unclassified = t('unclassified') || 'Unclassified'
    if (!node || !node[field]) return unclassified;
    const nameObj = node[field];
    if (typeof nameObj === 'string') return nameObj;
    return nameObj[localeCookie.value || 'ko'] || nameObj['ko'] || nameObj['en'] || unclassified;
  }

  const getRequestTypeLabel = (type) => {
    if (!type) return t('other_request') || 'Other Request';
    const i18nKey = `target_type_${type}`;
    const translated = t(i18nKey);
    if (translated && translated !== i18nKey) return translated;

    const codeName = codeStore.getCodeName('TARGET_TYPE', type, null)
    if (codeName && codeName !== type) return codeName

    if (type === 'RECORD_CREATE') return t('record_create') || 'New Record';
    if (type === 'RECORD_UPDATE') return t('record_update') || 'Data Update';
    if (type === 'RECORD_DELETE') return t('record_delete') || 'Data Delete';
    if (type === 'DOMAIN_RECORD_CREATE') return t('domain_record_create') || 'Domain Record Create';
    return type || t('other_request') || 'Other Request';
  }

  const getRequestTypeColor = (type) => {
    if (type === 'RECORD_CREATE' || type === 'DOMAIN_RECORD_CREATE') return 'success';
    if (type === 'RECORD_UPDATE') return 'warning';
    if (type === 'RECORD_DELETE') return 'danger';
    return 'primary';
  }

  const parseDate = (dateString) => {
    if (!dateString) return null
    let str = String(dateString).trim()
    if (/^\d+$/.test(str)) {
      return new Date(parseInt(str, 10))
    }
    if (!str.endsWith('Z') && !str.includes('+') && !/[-+]\d{2}:\d{2}$/.test(str)) {
      if (str.includes(' ') && !str.includes('T')) {
        str = str.replace(' ', 'T')
      }
      const serverOffset = useCookie('server_offset', { default: () => '+09:00' }).value
      str += serverOffset
    }
    const d = new Date(str)
    return isNaN(d.getTime()) ? new Date(dateString) : d
  }

  const formatDate = (dateString) => {
    if (!dateString) return ''
    const date = parseDate(dateString)
    if (!date) return ''
    const tz = useCookie('timezone', { default: () => 'Asia/Seoul' }).value
    const formatted = date.toLocaleString(undefined, { timeZone: tz })
    return formatted.replace(/\s*(GMT|UTC|KST|PST|EST|CET)[-+0-9:]*/gi, '').trim()
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
    formatDate
  }
}
