<template>
  <va-modal
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    title="변경 내역 상세"
    hide-default-actions
    size="large"
    :prevent-click-outside="true"
    :no-outside-dismiss="true"
  >
    <div style="padding: 1rem; box-sizing: border-box; width: 100%; max-height: 60vh; overflow-y: auto;">
      <div v-if="!selectedDiffs || selectedDiffs.length === 0" style="color: #777; font-style: italic;">
        변경된 필드가 없습니다.
      </div>
      <div v-else style="display: flex; flex-direction: column; gap: 0.25rem;">
        <div v-for="diff in selectedDiffs" :key="diff.fieldName" style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap; background: #f9f9f9; padding: 0.35rem 0.5rem; border-radius: 4px; border: 1px solid #eee; font-size: 0.85rem;">
          <span style="font-weight: bold; color: #333; min-width: 90px;">{{ diff.fieldName }}</span>
          <div style="flex: 1; display: flex; align-items: center; gap: 0.35rem; flex-wrap: wrap;">
            <template v-if="(diff.before === undefined || diff.before === null || diff.before === '' || diff.before === 'undefined')">
              <va-badge color="info" text="NEW" size="small" />
              <template v-if="formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).isFile">
                <a href="#" @click.prevent="downloadFileWithAuth(formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).url, formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).fname)" style="color: #2563eb; text-decoration: underline; font-weight: bold; font-size: 0.85rem; display: inline-flex; align-items: center; gap: 2px;">
                  📎 {{ formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).fname }}
                </a>
              </template>
              <template v-else>
                <span style="color: #2c3e50; font-weight: bold; font-size: 0.85rem;">{{ diff.after }}</span>
              </template>
            </template>
            <template v-else-if="(diff.after === undefined || diff.after === null || diff.after === '' || diff.after === 'undefined')">
              <va-badge color="danger" text="DEL" size="small" />
              <template v-if="formatDiffDisplay(diff.before, diff.rawBefore, diff.fieldType).isFile">
                <span style="color: #666; text-decoration: line-through; font-size: 0.85rem;">📎 {{ formatDiffDisplay(diff.before, diff.rawBefore, diff.fieldType).fname }}</span>
              </template>
              <template v-else>
                <span style="color: #666; text-decoration: line-through; font-size: 0.85rem;">{{ diff.before }}</span>
              </template>
            </template>
            <template v-else>
              <va-badge color="warning" text="MOD" size="small" />
              <template v-if="formatDiffDisplay(diff.before, diff.rawBefore, diff.fieldType).isFile">
                <span style="color: #666; text-decoration: line-through; font-size: 0.85rem;">📎 {{ formatDiffDisplay(diff.before, diff.rawBefore, diff.fieldType).fname }}</span>
              </template>
              <template v-else>
                <span style="color: #666; text-decoration: line-through; font-size: 0.85rem;">{{ diff.before }}</span>
              </template>
              <span style="color: #999; font-weight: bold;">&rarr;</span>
              <template v-if="formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).isFile">
                <a href="#" @click.prevent="downloadFileWithAuth(formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).url, formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).fname)" style="color: #2563eb; text-decoration: underline; font-weight: bold; font-size: 0.85rem; display: inline-flex; align-items: center; gap: 2px;">
                  📎 {{ formatDiffDisplay(diff.after, diff.rawAfter, diff.fieldType).fname }}
                </a>
              </template>
              <template v-else>
                <span style="color: #2c3e50; font-weight: bold; font-size: 0.85rem;">{{ diff.after }}</span>
              </template>
            </template>
          </div>
        </div>
      </div>
    </div>
    <div style="display: flex; justify-content: flex-end; margin-top: 1rem;">
      <va-button @click="$emit('update:modelValue', false)">Close</va-button>
    </div>
  </va-modal>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  selectedDiffs: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue'])

const { downloadFileWithAuth } = useFileDownloader()

const extractFilename = (input) => {
  if (!input) return '';
  if (typeof input === 'object') {
    if (input.name && input.name !== 'Download') return input.name;
    if (input.originalName) return input.originalName;
    if (input.url) input = input.url;
    else return '';
  }
  let str = String(input).trim();
  if (!str || str === '-' || str === '[]' || str === '{}' || str === 'null' || str === 'undefined') return '';
  
  try {
    if (str.startsWith('{') || str.startsWith('[')) {
      const parsed = JSON.parse(str);
      if (Array.isArray(parsed) && parsed.length > 0) return extractFilename(parsed[0]);
      if (typeof parsed === 'object' && (parsed.name || parsed.originalName)) return parsed.name || parsed.originalName;
    }
  } catch (e) {}

  try {
    if (str.includes('?name=')) return decodeURIComponent(str.split('?name=')[1].split('&')[0]);
    if (str.includes('?filename=')) return decodeURIComponent(str.split('?filename=')[1].split('&')[0]);
    const fname = decodeURIComponent(str.split('/').pop().split('?')[0]);
    if (fname && fname !== '-' && fname !== 'null') return fname;
  } catch (e) {}
  
  return str;
}

const formatDiffDisplay = (val, rawVal, fieldType) => {
  if (val === undefined || val === null || val === '') return { isFile: false, text: '' };
  const str = typeof rawVal === 'string' ? rawVal : (typeof val === 'string' ? val : JSON.stringify(rawVal || val));
  if (fieldType === 'FILE' || str.includes('/api/files/download/') || str.includes('name=')) {
    const fn = extractFilename(rawVal || val);
    let url = '#';
    if (typeof rawVal === 'object' && rawVal !== null) {
      url = rawVal.url || '#';
    } else if (typeof rawVal === 'string') {
      url = rawVal;
    } else if (typeof val === 'string') {
      url = val;
    }
    // JSON 배열 형태인 경우 예외처리
    if (url.startsWith('["') && url.endsWith('"]')) {
      try {
        const arr = JSON.parse(url);
        if (arr.length > 0) url = arr[0];
      } catch (e) {}
    }
    return { isFile: true, fname: fn || '파일 다운로드', url };
  }
  return { isFile: false, text: String(val) };
}
</script>
