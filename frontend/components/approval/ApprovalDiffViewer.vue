<template>
  <div v-if="request">
    <!-- Data Changes Display (Collapsible Accordion) -->
    <div class="accordion-card" style="background-color: var(--va-background-secondary); border-left: 4px solid var(--va-primary); border-radius: 6px; padding: 0.85rem 1rem; margin-bottom: 1.5rem; transition: all 0.25s ease;">
      <div 
        class="accordion-header"
        @click="isExpanded = !isExpanded" 
        style="display: flex; justify-content: space-between; align-items: center; cursor: pointer; user-select: none; border-radius: 4px; padding: 0.25rem 0.4rem; margin: -0.25rem -0.4rem; transition: background-color 0.2s ease;"
      >
        <h4 style="margin: 0; font-size: 0.95rem; color: var(--va-text-primary); font-weight: bold; display: flex; align-items: center; gap: 0.5rem;">
          <va-icon 
            name="chevron_right" 
            size="small" 
            color="primary" 
            :style="{ transform: isExpanded ? 'rotate(90deg)' : 'rotate(0deg)', transition: 'transform 0.35s cubic-bezier(0.16, 1, 0.3, 1)' }" 
          />
          {{ t('requestedData') }}
        </h4>
        <va-chip size="small" color="primary" preset="outline" style="font-weight: 600; transition: transform 0.2s ease;">
          {{ isExpanded ? t('collapse') : t('expand') }}
        </va-chip>
      </div>

      <div v-show="isExpanded" style="padding-top: 1rem;">
        <template v-if="parsedChanges">
          <!-- Standard Record Approval Diff Section -->
          <div class="custom-scrollbar">
            <div v-for="sector in groupedChangesList" :key="sector.key" style="margin-bottom: 1rem;">
              <div style="font-weight: bold; padding: 0.5rem; background: var(--va-background-secondary); border-radius: 4px; font-size: 0.95rem; color: var(--va-primary); display: flex; align-items: center; gap: 0.5rem;">
                <va-icon name="folder" size="small" /> {{ sector.label }}
              </div>
              
              <div style="width: 100%; margin-top: 0.5rem; display: flex; flex-direction: column; gap: 0.5rem;">
                <div v-for="group in sector.groups" :key="group.key" style="border: 1px solid var(--va-background-border); border-radius: 4px; overflow: hidden; background: var(--va-background-element);">
                  <div style="background: var(--va-background-secondary); padding: 0.75rem 1rem; font-weight: bold; font-size: 0.95rem; color: var(--va-text-primary); border-bottom: 1px solid var(--va-background-border);">
                    {{ group.label }}
                  </div>
                  <div style="display: grid; grid-template-columns: repeat(12, 1fr); gap: 1rem; padding: 0.75rem;">
                    <template v-for="f in group.fields" :key="f.key">
                      <div v-if="request.targetType !== 'RECORD_UPDATE' || (f.val.isChanged || (f.val.before !== f.val.after))" :style="{ gridColumn: 'span ' + (f.gridWidth || 12), border: '1px solid var(--va-background-border)', borderRadius: '8px', overflow: 'hidden', background: 'var(--va-background-element)' }">
                        <div style="background: var(--va-background-secondary); padding: 0.75rem 1rem; border-bottom: 1px solid var(--va-background-border); font-weight: 600; font-size: 0.85rem; color: var(--va-text-primary); display: flex; justify-content: space-between; align-items: center;">
                          <span>
                            {{ f.label }}
                            <va-icon v-if="f.isEncrypted" name="lock" size="small" color="warning" style="margin-left: 4px;" :title="t('encrypted_field')" />
                          </span>
                          <va-badge v-if="request.targetType === 'RECORD_UPDATE' && f.val.isChanged" color="warning" size="small">{{ t('modified') }}</va-badge>
                        </div>
                        <div style="padding: 0;">
                          <template v-if="request.targetType === 'RECORD_UPDATE'">
                            <div v-if="f.val.isChanged" style="display: flex; flex-direction: column;">
                              <div style="background-color: rgba(229, 57, 53, 0.1); border-bottom: 1px solid rgba(229, 57, 53, 0.2); padding: 0.75rem 1rem; font-size: 0.85rem; display: flex; align-items: flex-start; gap: 0.5rem;">
                                <va-icon name="remove_circle_outline" color="danger" size="small" style="margin-top: 2px;" />
                                <div style="color: var(--va-danger); word-break: break-all; width: 100%;">
                                  <div v-if="typeof f.val.before === 'string' && (f.val.before.includes('<p>') || f.val.before.includes('<h') || f.val.before.includes('<ul>'))" v-html="f.val.before" />
                                  <span v-else>{{ formatVal(f.val.before) }}</span>
                                </div>
                              </div>
                              <div style="background-color: rgba(67, 160, 71, 0.1); padding: 0.75rem 1rem; font-size: 0.85rem; display: flex; align-items: flex-start; gap: 0.5rem;">
                                <va-icon name="add_circle_outline" color="success" size="small" style="margin-top: 2px;" />
                                <div style="color: var(--va-success); font-weight: 500; word-break: break-all; width: 100%;">
                                  <div v-if="typeof f.val.after === 'string' && (f.val.after.includes('<p>') || f.val.after.includes('<h') || f.val.after.includes('<ul>'))" v-html="f.val.after" />
                                  <span v-else>{{ formatVal(f.val.after) }}</span>
                                </div>
                              </div>
                            </div>
                            <div v-else style="padding: 0.75rem 1rem; font-size: 0.85rem; color: var(--va-text-secondary); background: var(--va-background-primary);">
                              <div v-if="typeof f.val.before === 'string' && (f.val.before.includes('<p>') || f.val.before.includes('<h') || f.val.before.includes('<ul>'))" v-html="f.val.before" />
                              <span v-else>{{ formatVal(f.val.before) }}</span>
                            </div>
                          </template>
                          <template v-else>
                            <div style="padding: 0.75rem 1rem; font-size: 0.85rem; color: var(--va-text-primary);">
                              <div v-if="typeof f.val === 'string' && (f.val.includes('<p>') || f.val.includes('<h') || f.val.includes('<ul>'))" v-html="f.val" />
                              <span v-else>{{ formatVal(f.val) }}</span>
                            </div>
                          </template>
                        </div>
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
        <div v-else style="color: var(--va-text-secondary); font-style: italic; font-size: 0.9rem;">
          {{ t('noParsable') }}
        </div>
      </div>
    </div>

    <!-- Image Lightbox Modal -->
    <ImageLightboxModal
      v-model="showLightbox"
      :images="lightboxImages"
      :initial-index="lightboxIndex"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { formatMultilingual } from '~/composables/useMultilingual'
import ImageLightboxModal from '~/components/common/ImageLightboxModal.vue'

const props = defineProps({
  request: { type: Object, required: true }
})

const { t } = useI18n()
const isExpanded = ref(true)

const parsedChanges = computed(() => {
  const ch = props.request?.changes
  if (!ch) return null
  if (typeof ch === 'object') return ch
  try {
    let p = JSON.parse(ch)
    if (typeof p === 'string') p = JSON.parse(p)
    return p
  } catch (e) {
    return null
  }
})

const formatVal = (val) => {
  if (val === null || val === undefined) return t('none')
  if (typeof val === 'object') return formatMultilingual(val)
  return String(val)
}

const groupedChangesList = computed(() => {
  const parsed = parsedChanges.value
  if (!parsed) return []
  
  const isUpdate = props.request?.targetType === 'RECORD_UPDATE'
  const before = parsed.before || {}
  const after = parsed.after || parsed.data || parsed || {}
  const changedFields = parsed.changedFields

  const allKeys = Array.from(new Set([
    ...(Array.isArray(changedFields) ? changedFields : []),
    ...Object.keys(before),
    ...Object.keys(after)
  ])).filter(k => k && !k.startsWith('_'))

  const fields = allKeys
    .map(k => {
      const isExplicit = Array.isArray(changedFields) && changedFields.includes(k)
      const isChanged = isExplicit || (JSON.stringify(before[k] ?? '') !== JSON.stringify(after[k] ?? ''))
      return {
        key: k,
        label: k,
        val: isUpdate ? {
          before: before[k],
          after: after[k],
          isChanged
        } : after[k],
        gridWidth: 12
      }
    })
    .filter(f => !isUpdate || (f.val && f.val.isChanged))

  return [{
    key: 'default',
    label: t('default_group'),
    groups: [{
      key: 'general',
      label: t('general_fields'),
      fields
    }]
  }]
})

// Lightbox states & handlers
const showLightbox = ref(false)
const lightboxImages = ref([])
const lightboxIndex = ref(0)

const parseImagesList = (val) => {
  if (!val) return []
  if (Array.isArray(val)) {
    return val.map((item, idx) => {
      if (typeof item === 'string') return { url: item, name: `Image ${idx + 1}` }
      if (typeof item === 'object' && item?.url) return { url: item.url, name: item.name || `Image ${idx + 1}` }
      return { url: String(item), name: `Image ${idx + 1}` }
    })
  }
  if (typeof val === 'object' && val !== null) {
    if (val.url) return [{ url: val.url, name: val.name || 'Image' }]
    return []
  }
  if (typeof val === 'string') {
    const trimmed = val.trim()
    if (trimmed.startsWith('[') || trimmed.startsWith('{')) {
      try {
        const parsed = JSON.parse(trimmed)
        return parseImagesList(parsed)
      } catch (e) {}
    }
    return [{ url: val, name: 'Image' }]
  }
  return []
}

const openImageLightbox = (images, initialIdx = 0) => {
  if (!images || images.length === 0) return
  lightboxImages.value = images
  lightboxIndex.value = initialIdx
  showLightbox.value = true
}

const handleHtmlImageClick = (e) => {
  const target = e.target
  if (target && target.tagName === 'IMG' && target.src) {
    e.preventDefault()
    e.stopPropagation()
    openImageLightbox([{ url: target.src, name: target.alt || 'Image' }], 0)
  }
}
</script>

<style scoped>
.custom-scrollbar {
  overflow-x: hidden;
}

:deep(img) {
  max-width: 100% !important;
  max-height: 180px !important;
  object-fit: contain !important;
  cursor: zoom-in !important;
  border-radius: 4px;
}
</style>
