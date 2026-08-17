<template>
  <div class="global-search-container" ref="searchContainer">
    <va-input
      v-model="searchQuery"
      :placeholder="t('global_search_placeholder')"
      class="global-search-input"
      :style="{ width: isFocused ? '360px' : '220px', transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }"
      @focus="isFocused = true"
      @blur="handleBlur"
      @keyup.enter="performSearch"
      @keydown.esc="closeDropdown"
    >
      <template #prependInner>
        <va-icon name="search" color="secondary" />
      </template>
      <template #appendInner v-if="searchQuery">
        <va-icon name="close" size="small" class="cursor-pointer" @click="clearSearch" />
      </template>
    </va-input>
    
    <!-- Search Results Dropdown (Card UI) -->
    <div class="search-dropdown" v-if="isFocused && searchQuery.trim() !== ''">
      <va-inner-loading :loading="isSearching">
        <div v-if="results.length > 0" class="search-results-list">
          <div
            v-for="res in results"
            :key="res.id"
            class="search-card-item"
            @click="goToRecord(res)"
          >
            <!-- Card Header: Domain & Classification Node Badges + Status -->
            <div class="card-header-row">
              <div class="card-path-badges">
                <!-- Domain Badge -->
                <span v-if="getDomainName(res)" class="card-domain-badge">
                  <va-icon name="apartment" size="13px" class="mr-1" />
                  <span>{{ getDomainName(res) }}</span>
                </span>
                
                <span v-if="getDomainName(res) && getNodeName(res)" class="path-separator">›</span>

                <!-- Node Badge -->
                <span v-if="getNodeName(res)" class="card-node-badge">
                  <va-icon name="folder" size="13px" class="mr-1" />
                  <span>{{ getNodeName(res) }}</span>
                </span>

                <span v-if="!getDomainName(res) && !getNodeName(res)" class="card-node-badge placeholder-badge">
                  <va-icon name="description" size="13px" class="mr-1" />
                  <span>{{ t('record_item') }}</span>
                </span>
              </div>

              <va-badge
                v-if="res.status"
                :text="res.status"
                :color="getStatusColor(res.status)"
                size="small"
                class="status-badge"
              />
            </div>

            <!-- Card Body: Primary Title & Key Fields -->
            <div class="card-body-content">
              <div class="card-primary-title">
                <span class="title-field-label">{{ getPrimaryField(res).label }}:</span>
                <span class="title-field-val">{{ getPrimaryField(res).val }}</span>
              </div>
              
              <!-- Key-Value Summary Chips (Interactive for Image/File) -->
              <div class="card-field-chips" v-if="getPreviewFields(res).length > 0">
                <div
                  v-for="(field, fIdx) in getPreviewFields(res)"
                  :key="fIdx"
                  class="field-chip"
                  :class="{ 
                    'is-clickable-chip': field.type === 'image' || field.type === 'file',
                    'is-image-chip': field.type === 'image',
                    'is-file-chip': field.type === 'file'
                  }"
                  @click.stop="handleFieldAction(field)"
                  :title="field.type === 'image' ? t('preview_image') : (field.type === 'file' ? t('download_file') : '')"
                >
                  <span class="chip-key">{{ field.label }}:</span>
                  
                  <!-- Image Type Chip -->
                  <span v-if="field.type === 'image'" class="chip-val chip-media-val">
                    <va-icon name="image" size="13px" color="primary" class="mr-1" />
                    <span>{{ field.val }}</span>
                    <va-icon name="zoom_in" size="13px" class="ml-1 opacity-70" />
                  </span>

                  <!-- File Type Chip -->
                  <span v-else-if="field.type === 'file'" class="chip-val chip-media-val">
                    <va-icon name="attach_file" size="13px" color="info" class="mr-1" />
                    <span>{{ field.val }}</span>
                    <va-icon name="download" size="13px" class="ml-1 opacity-70" />
                  </span>

                  <!-- Reference Record Chip -->
                  <span v-else-if="field.type === 'reference'" class="chip-val chip-ref-val">
                    {{ field.val }}
                  </span>

                  <!-- Standard Text Chip -->
                  <span v-else class="chip-val">
                    {{ field.val }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Card Footer: Code, Date & Navigation Action -->
            <div class="card-footer-row">
              <div class="card-meta-left">
                <span class="record-code-tag">{{ formatRecordCode(res.id) }}</span>
                <span class="meta-date" v-if="res.createdAt">{{ formatWithTimezone(res.createdAt) }}</span>
              </div>
              <div class="card-action-hint">
                <va-icon name="arrow_forward" size="16px" color="primary" />
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="searchQuery.trim().length < 2" class="search-empty-state">
          <va-icon name="info" color="secondary" size="large" class="mb-2" />
          <div>{{ t('search_min_length') }}</div>
        </div>

        <div v-else-if="!isSearching" class="search-empty-state">
          <va-icon name="search_off" color="secondary" size="large" class="mb-2" />
          <div class="font-bold">{{ t('search_no_results') }}</div>
          <div class="text-xs text-secondary mt-1">"{{ searchQuery }}"</div>
        </div>
      </va-inner-loading>
    </div>

    <!-- Image Lightbox Modal for Card Image Preview -->
    <ImageLightboxModal
      v-model="isViewerModalOpen"
      :images="viewerImages"
      :initial-index="0"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { formatRecordCode } from '~/utils/formatters'
import { useTimezoneDate } from '~/composables/useTimezoneDate'
import ImageLightboxModal from '~/components/common/ImageLightboxModal.vue'

const { t, locale } = useI18n()
const { customFetch } = useCustomFetch()
const { formatWithTimezone } = useTimezoneDate()
const router = useRouter()

const searchQuery = ref('')
const isFocused = ref(false)
const isSearching = ref(false)
const results = ref([])
const searchContainer = ref(null)

// Lightbox Viewer Modal State
const isViewerModalOpen = ref(false)
const viewerImages = ref([])

// Node Field Definitions In-Memory Cache
const nodeFieldDefsCache = new Map()

let debounceTimer = null

// Close dropdown only when clicked strictly outside the search container
const onDocumentClick = (e) => {
  if (searchContainer.value && !searchContainer.value.contains(e.target)) {
    // If clicking on a modal or portal outside, also keep or close based on target
    const isModalContent = e.target.closest('.va-modal') || e.target.closest('.custom-record-modal')
    if (!isModalContent) {
      isFocused.value = false
    }
  }
}

onMounted(() => {
  if (typeof document !== 'undefined') {
    document.addEventListener('click', onDocumentClick)
  }
})

onBeforeUnmount(() => {
  if (typeof document !== 'undefined') {
    document.removeEventListener('click', onDocumentClick)
  }
})

const fetchNodeFieldDefinitions = async (nodeId) => {
  if (!nodeId || nodeFieldDefsCache.has(nodeId)) return
  try {
    const fields = await customFetch(`/api/nodes/${nodeId}/fields/effective`).catch(() => [])
    if (Array.isArray(fields)) {
      nodeFieldDefsCache.set(nodeId, fields)
    }
  } catch (e) {
    console.error('Failed to fetch field definitions for node:', nodeId, e)
  }
}

const performSearch = async () => {
  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    results.value = []
    return
  }
  
  isSearching.value = true
  try {
    const res = await customFetch(`/api/v1/search?q=${encodeURIComponent(searchQuery.value.trim())}&size=6`)
    let items = []
    if (res && res.content) {
      items = res.content
    } else if (Array.isArray(res)) {
      items = res
    }
    results.value = items

    // Prefetch field definitions for all returned nodes to ensure accurate multilingual labels
    const nodeIds = items.map(r => r.node?.id).filter(Boolean)
    const uniqueNodeIds = [...new Set(nodeIds)]
    await Promise.all(uniqueNodeIds.map(id => fetchNodeFieldDefinitions(id)))
  } catch (e) {
    console.error('Search failed', e)
    results.value = []
  } finally {
    isSearching.value = false
  }
}

watch(searchQuery, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    performSearch()
  }, 250)
})

const handleBlur = () => {
  // Do not automatically close on blur so user can click and view cards
}

const closeDropdown = () => {
  isFocused.value = false
}

const clearSearch = () => {
  searchQuery.value = ''
  results.value = []
  isFocused.value = false
}

const parseDataObj = (dataStr) => {
  if (!dataStr) return {}
  if (typeof dataStr === 'object') return dataStr
  try {
    return JSON.parse(dataStr)
  } catch(e) {
    return {}
  }
}

const extractFileNameFromUrl = (rawUrl) => {
  if (!rawUrl || typeof rawUrl !== 'string') return ''
  const cleanUrl = rawUrl.trim().replace(/^[\\["\\s']+|[\\]"\\s']+$/g, '')
  try {
    const urlObj = new URL(cleanUrl, 'http://localhost')
    const nameParam = urlObj.searchParams.get('name')
    if (nameParam) return decodeURIComponent(nameParam).replace(/[\\["'\\]]+/g, '')
  } catch (e) {
    const match = cleanUrl.match(/[?&]name=([^&]+)/)
    if (match) return decodeURIComponent(match[1]).replace(/[\\["'\\]]+/g, '')
  }
  const parts = cleanUrl.split('/')
  const lastPart = parts[parts.length - 1].split('?')[0] || 'file'
  return lastPart.replace(/[\\["'\\]]+/g, '')
}

const isUuidString = (str) => {
  if (typeof str !== 'string') return false
  const clean = str.trim().replace(/^[\["\s']+|[\]"\s']+$/g, '')
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(clean)
}

const getFieldDefinition = (rawKey, res) => {
  const nodeId = res.node?.id
  if (!nodeId || !nodeFieldDefsCache.has(nodeId)) return null
  const defs = nodeFieldDefsCache.get(nodeId) || []
  return defs.find(d => (d.key && d.key.toUpperCase() === rawKey.toUpperCase()) || (d.code && d.code.toUpperCase() === rawKey.toUpperCase())) || null
}

const getLocalizedFieldLabel = (rawKey, res) => {
  const def = getFieldDefinition(rawKey, res)
  if (def && def.name) {
    if (typeof def.name === 'object') {
      return def.name[locale.value] || def.name.ko || def.name.en || rawKey
    }
    return def.name
  }
  return rawKey
}

const extractImageUrlsFromHtml = (htmlStr) => {
  if (!htmlStr || typeof htmlStr !== 'string') return []
  const imgRegex = /<img[^>]+src=["']([^"']+)["'][^>]*>/gi
  const urls = []
  let match
  while ((match = imgRegex.exec(htmlStr)) !== null) {
    if (match[1]) {
      const clean = match[1].trim().replace(/^[\\["\\s']+|[\\]"\\s']+$/g, '')
      if (clean) urls.push(clean)
    }
  }
  return urls
}

const parseFieldValue = (rawKey, val, res) => {
  if (val === null || val === undefined || val === '') {
    return { type: 'text', val: '-' }
  }

  // Check if val is a JSON string of an array or object
  let processedVal = val
  if (typeof val === 'string' && (val.startsWith('[') || val.startsWith('{'))) {
    try {
      processedVal = JSON.parse(val)
    } catch (e) {
      processedVal = val
    }
  }

  // 1. Array of Images / Files / Objects
  if (Array.isArray(processedVal)) {
    if (processedVal.length === 0) return { type: 'text', val: '-' }
    
    // Check if it's an array of file/image download URLs
    if (typeof processedVal[0] === 'string' && processedVal[0].includes('/api/files/download')) {
      const isImg = rawKey.toUpperCase().includes('IMG') || rawKey.toUpperCase().includes('IMAGE') || rawKey.toUpperCase().includes('PHOTO') || processedVal[0].match(/\.(png|jpe?g|gif|webp|svg)/i)
      if (isImg) {
        return {
          type: 'image',
          val: t('image_count', { count: processedVal.length }),
          imageUrls: processedVal.map(u => u.replace(/^[\\["\\s']+|[\\]"\\s']+$/g, ''))
        }
      } else {
        const firstFileName = extractFileNameFromUrl(processedVal[0])
        const displayVal = processedVal.length > 1 ? `${firstFileName} +${processedVal.length - 1}` : firstFileName
        return {
          type: 'file',
          val: displayVal,
          fileUrl: processedVal[0].replace(/^[\\["\\s']+|[\\]"\\s']+$/g, ''),
          fileName: firstFileName
        }
      }
    }

    // Array of Objects
    if (typeof processedVal[0] === 'object') {
      const first = processedVal[0]
      const sampleText = first.name || first.SCHOOL_NAME || first.label || JSON.stringify(first)
      return {
        type: 'text',
        val: processedVal.length > 1 ? `${sampleText} 외 ${processedVal.length - 1}건` : sampleText
      }
    }

    return { type: 'text', val: processedVal.join(', ') }
  }

  // 2. Multilingual Object { ko: "...", en: "..." }
  if (typeof processedVal === 'object') {
    if (processedVal.ko || processedVal.en) {
      const cur = locale.value || 'ko'
      return { type: 'text', val: processedVal[cur] || processedVal.ko || processedVal.en || JSON.stringify(processedVal) }
    }
    return { type: 'text', val: JSON.stringify(processedVal) }
  }

  // 3. String checks
  const strVal = String(processedVal).trim()

  // 3-1. Check for HTML formatted content FIRST before checking raw URL substrings
  const isHtml = strVal.startsWith('<') || strVal.includes('<p>') || strVal.includes('<img') || strVal.includes('</div>') || strVal.includes('</span>') || strVal.includes('<mark')
  if (isHtml) {
    const extractedImages = extractImageUrlsFromHtml(strVal)
    const strippedText = strVal.replace(/<[^>]*>?/gm, '').replace(/&nbsp;/g, ' ').trim()

    if (extractedImages.length > 0) {
      return {
        type: 'image',
        val: t('image_count', { count: extractedImages.length }),
        imageUrls: extractedImages
      }
    } else {
      return {
        type: 'text',
        val: strippedText || `[${t('rich_text')}]`
      }
    }
  }

  const cleanStr = strVal.replace(/^[\\["\\s']+|[\\]"\\s']+$/g, '')

  // 3-2. Single File / Image URL
  if (cleanStr.includes('/api/files/download')) {
    const isImg = rawKey.toUpperCase().includes('IMG') || rawKey.toUpperCase().includes('IMAGE') || rawKey.toUpperCase().includes('PHOTO') || cleanStr.match(/\.(png|jpe?g|gif|webp|svg)/i)
    if (isImg) {
      return {
        type: 'image',
        val: t('image_count', { count: 1 }),
        imageUrls: [cleanStr]
      }
    } else {
      const fileName = extractFileNameFromUrl(cleanStr)
      return {
        type: 'file',
        val: fileName,
        fileUrl: cleanStr,
        fileName: fileName
      }
    }
  }

  // 3-3. UUID Reference
  if (isUuidString(cleanStr)) {
    return {
      type: 'reference',
      val: formatRecordCode(cleanStr)
    }
  }

  return { type: 'text', val: strVal }
}

const getPrimaryField = (res) => {
  const obj = parseDataObj(res.data)
  const keys = Object.keys(obj).filter(k => !k.startsWith('_'))
  if (keys.length === 0) return { label: t('record_item'), val: t('search_no_data') }

  // Prioritize primary name / title keys
  const nameKeys = ['EP_NAME', 'NAME', 'TITLE', 'ITEM_NAME', 'PRODUCT_NAME', 'LABEL', 'USER_NAME']
  const matchedKey = keys.find(k => nameKeys.includes(k.toUpperCase())) || keys[0]
  
  const label = getLocalizedFieldLabel(matchedKey, res)
  const parsed = parseFieldValue(matchedKey, obj[matchedKey], res)

  return {
    rawKey: matchedKey,
    label: label,
    val: parsed.val
  }
}

const getPreviewFields = (res) => {
  const obj = parseDataObj(res.data)
  const keys = Object.keys(obj).filter(k => !k.startsWith('_'))
  if (keys.length <= 1) return []

  const nameKeys = ['EP_NAME', 'NAME', 'TITLE', 'ITEM_NAME', 'PRODUCT_NAME', 'LABEL', 'USER_NAME']
  const primaryKey = keys.find(k => nameKeys.includes(k.toUpperCase())) || keys[0]
  const otherKeys = keys.filter(k => k !== primaryKey).slice(0, 3)

  return otherKeys.map(k => {
    const label = getLocalizedFieldLabel(k, res)
    const parsed = parseFieldValue(k, obj[k], res)
    return {
      rawKey: k,
      label: label,
      ...parsed
    }
  })
}

const getDomainName = (res) => {
  if (res.node && res.node.domain) {
    const d = res.node.domain
    if (typeof d.name === 'object' && d.name !== null) {
      return d.name[locale.value] || d.name.ko || d.name.en || ''
    }
    return d.name || ''
  }
  return ''
}

const getNodeName = (res) => {
  if (res.node) {
    const n = res.node
    if (typeof n.name === 'object' && n.name !== null) {
      return n.name[locale.value] || n.name.ko || n.name.en || ''
    }
    return n.name || ''
  }
  return ''
}

const getStatusColor = (status) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'PENDING_APPROVAL': return 'warning'
    case 'REJECTED': return 'danger'
    case 'INACTIVE': return 'secondary'
    default: return 'primary'
  }
}

const handleFieldAction = async (field) => {
  if (field.type === 'image' && field.imageUrls && field.imageUrls.length > 0) {
    viewerImages.value = field.imageUrls
    isViewerModalOpen.value = true
  } else if (field.type === 'file' && field.fileUrl) {
    await downloadFile(field.fileUrl, field.fileName)
  }
}

const downloadFile = async (url, fileName) => {
  try {
    const blob = await customFetch(url, { responseType: 'blob' })
    if (blob instanceof Blob) {
      const downloadUrl = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = downloadUrl
      a.download = fileName || 'download'
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(downloadUrl)
    }
  } catch (e) {
    console.error('Failed to download file:', e)
  }
}

const goToRecord = (record) => {
  if (!record) return
  const id = typeof record === 'object' ? record.id : record
  const nodeId = typeof record === 'object' && record.node ? record.node.id : null
  const domainId = typeof record === 'object' && record.node?.domain ? record.node.domain.id : null

  // 1. Dispatch custom event for immediate response if already on /records page
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('global-search:select-record', {
      detail: { recordId: id, nodeId, domainId }
    }))
  }

  // 2. Navigate via router with timestamp to ensure route watchers trigger
  const queryObj = { recordId: id, _t: Date.now().toString() }
  if (domainId) queryObj.domainId = domainId
  if (nodeId) queryObj.nodeId = nodeId

  router.push({ path: '/records', query: queryObj })
}
</script>

<style scoped>
.global-search-container {
  position: relative;
  margin-right: 1.25rem;
  display: flex;
  align-items: center;
}

.search-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  left: 0;
  width: 440px;
  background: var(--va-background-secondary, #1e293b);
  border: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.12));
  border-radius: 10px;
  box-shadow: 0 12px 28px -4px rgba(0, 0, 0, 0.45), 0 8px 10px -6px rgba(0, 0, 0, 0.3);
  z-index: 2000;
  max-height: 520px;
  overflow-y: auto;
  backdrop-filter: blur(10px);
  padding: 8px;
}

.search-results-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* Card UI Item */
.search-card-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 11px 13px;
  background: var(--va-background-element, rgba(255, 255, 255, 0.04));
  border: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.08));
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.search-card-item:hover {
  background: rgba(37, 99, 235, 0.12);
  border-color: var(--va-primary, #2563eb);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.18);
}

.search-card-item:hover .card-action-hint {
  transform: translateX(3px);
  color: var(--va-primary, #2563eb);
}

/* Header Row: Domain & Node Badges */
.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.card-path-badges {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-wrap: wrap;
  max-width: 320px;
}

.card-domain-badge {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 700;
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.12);
  padding: 2px 7px;
  border-radius: 4px;
  white-space: nowrap;
}

.path-separator {
  font-size: 11px;
  color: var(--va-text-secondary, #94a3b8);
  font-weight: bold;
}

.card-node-badge {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 600;
  color: var(--va-primary, #60a5fa);
  background: rgba(96, 165, 250, 0.12);
  padding: 2px 7px;
  border-radius: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.placeholder-badge {
  color: var(--va-text-secondary, #94a3b8);
  background: rgba(255, 255, 255, 0.06);
}

.status-badge {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}

/* Body Content */
.card-body-content {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.card-primary-title {
  font-size: 14px;
  color: var(--va-text-primary, #f8fafc);
  line-height: 1.35;
  display: flex;
  align-items: baseline;
  gap: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.title-field-label {
  font-weight: 600;
  color: var(--va-text-secondary, #94a3b8);
  font-size: 12.5px;
}

.title-field-val {
  font-weight: 800;
  color: var(--va-text-primary, #ffffff);
}

.card-field-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 2px;
}

.field-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.06);
  padding: 2px 7px;
  border-radius: 4px;
  color: var(--va-text-secondary, #cbd5e1);
  max-width: 100%;
  border: 1px solid transparent;
}

.chip-key {
  color: var(--va-text-secondary, #94a3b8);
  font-weight: 500;
}

.chip-val {
  color: var(--va-text-primary, #e2e8f0);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.chip-ref-val {
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
  padding: 0 4px;
  border-radius: 3px;
}

/* Clickable Image & File Chips */
.is-clickable-chip {
  cursor: pointer;
  transition: all 0.15s ease;
}

.is-image-chip {
  background: rgba(37, 99, 235, 0.12);
  border-color: rgba(37, 99, 235, 0.25);
  color: #93c5fd;
}

.is-image-chip:hover {
  background: rgba(37, 99, 235, 0.25);
  border-color: var(--va-primary, #2563eb);
  transform: scale(1.02);
}

.is-file-chip {
  background: rgba(14, 165, 233, 0.12);
  border-color: rgba(14, 165, 233, 0.25);
  color: #7dd3fc;
}

.is-file-chip:hover {
  background: rgba(14, 165, 233, 0.25);
  border-color: #0284c7;
  transform: scale(1.02);
}

.chip-media-val {
  display: inline-flex;
  align-items: center;
  font-weight: 700;
}

/* Footer Row */
.card-footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 5px;
  border-top: 1px solid var(--va-background-border, rgba(255, 255, 255, 0.06));
}

.card-meta-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.record-code-tag {
  font-family: 'JetBrains Mono', 'Courier New', monospace;
  font-size: 11px;
  font-weight: 600;
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.1);
  padding: 1px 5px;
  border-radius: 3px;
}

.meta-date {
  font-size: 10.5px;
  color: var(--va-text-secondary, #64748b);
}

.card-action-hint {
  display: flex;
  align-items: center;
  transition: transform 0.15s ease;
}

/* Empty States */
.search-empty-state {
  padding: 1.5rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: var(--va-text-secondary, #94a3b8);
  font-size: 13px;
}
</style>

