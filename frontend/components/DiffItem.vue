<template>
  <div style="font-size: 0.85rem; margin-bottom: 0.75rem; padding-bottom: 0.75rem; border-bottom: 1px dashed #ccc;">
    <div style="font-weight: bold; margin-bottom: 0.25rem;">{{ label }}</div>
    <div v-if="targetType === 'RECORD_UPDATE'" style="display: flex; gap: 0.5rem; align-items: center; word-break: break-all;">
      <span style="color: #d32f2f; background: #ffebee; padding: 2px 4px; border-radius: 3px; text-decoration: line-through;">
        <template v-if="type === 'FILE' && getFilesList(before).length > 0">
          <span v-html="getFilesList(before).map(renderFileLink).join(', ')"></span>
        </template>
        <template v-else>{{ formatValue(before) }}</template>
      </span>
      <va-icon name="arrow_forward" size="small" style="color: #757575;" />
      <span style="color: #2e7d32; background: #e8f5e9; padding: 2px 4px; border-radius: 3px; font-weight: bold;">
        <template v-if="type === 'FILE' && getFilesList(after).length > 0">
          <span v-html="getFilesList(after).map(renderFileLink).join(', ')"></span>
        </template>
        <template v-else>{{ formatValue(after) }}</template>
      </span>
    </div>
    <div v-else-if="targetType === 'RECORD_CREATE'" style="display: flex; gap: 0.5rem; align-items: center; word-break: break-all;">
      <span style="color: #2e7d32; background: #e8f5e9; padding: 2px 4px; border-radius: 3px; font-weight: bold;">
        <template v-if="type === 'FILE' && getFilesList(after).length > 0">
          <span v-html="getFilesList(after).map(renderFileLink).join(', ')"></span>
        </template>
        <template v-else>{{ formatValue(after) }}</template>
      </span>
    </div>
    <div v-else-if="targetType === 'RECORD_DELETE'" style="display: flex; gap: 0.5rem; align-items: center; word-break: break-all;">
      <span style="color: #d32f2f; background: #ffebee; padding: 2px 4px; border-radius: 3px; text-decoration: line-through;">
        <template v-if="type === 'FILE' && getFilesList(before).length > 0">
          <span v-html="getFilesList(before).map(renderFileLink).join(', ')"></span>
        </template>
        <template v-else>{{ formatValue(before) }}</template>
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps({
  label: { type: String, required: true },
  before: { type: [String, Number, Object, Array, Boolean], default: null },
  after: { type: [String, Number, Object, Array, Boolean], default: null },
  type: { type: String, default: null },
  targetType: { type: String, default: 'RECORD_UPDATE' }
})

const getFilesList = (val) => {
  if (!val) return [];
  if (typeof val === 'string') {
    if (val.startsWith('http')) return [val];
    try {
      const parsed = JSON.parse(val);
      if (Array.isArray(parsed) && parsed.length > 0 && typeof parsed[0] === 'string' && parsed[0].startsWith('http')) {
        return parsed;
      }
    } catch(e) {}
    return [];
  }
  if (Array.isArray(val)) {
    if (val.length > 0 && typeof val[0] === 'string' && val[0].startsWith('http')) return val;
    return [];
  }
  return [];
}

const getFileName = (url) => {
  if (!url) return '';
  if (typeof url !== 'string') return 'Unknown File';
  try {
    if (url.includes('?name=')) {
      const qs = url.split('?name=')[1];
      return decodeURIComponent(qs.split('&')[0]);
    }
    const parts = url.split('/');
    let name = parts[parts.length - 1];
    if (name.includes('?')) name = name.split('?')[0];
    return decodeURIComponent(name);
  } catch (e) {
    return url;
  }
}

const renderFileLink = (url) => {
  return `<a href="${url}" target="_blank" style="text-decoration: underline; color: inherit;"><i class="material-icons" style="font-size: 14px; vertical-align: middle;">attach_file</i>${getFileName(url)}</a>`
}

const formatValue = (val) => {
  if (val === undefined || val === null || val === '') return '(?놁쓬)';
  if (typeof val === 'object') {
    if (Array.isArray(val)) return val.join(', ') || '(?놁쓬)';
    return JSON.stringify(val);
  }
  return String(val);
}
</script>
