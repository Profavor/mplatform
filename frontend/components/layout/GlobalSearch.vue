<template>
  <div class="global-search-container" ref="searchContainer">
    <va-input
      v-model="searchQuery"
      placeholder="전역 검색 (Search any data...)"
      class="global-search-input"
      :style="{ width: isFocused ? '300px' : '200px', transition: 'width 0.3s ease' }"
      @focus="isFocused = true"
      @blur="handleBlur"
      @keyup.enter="performSearch"
    >
      <template #prependInner>
        <va-icon name="search" color="secondary" />
      </template>
    </va-input>
    
    <div class="search-dropdown" v-if="isFocused && (results.length > 0 || isSearching)">
      <va-inner-loading :loading="isSearching">
        <va-list v-if="results.length > 0">
          <va-list-item v-for="res in results" :key="res.id" class="search-result-item" @click="goToRecord(res.id)">
            <va-list-item-section>
              <div class="font-bold text-sm" style="color: var(--va-text-primary);">
                {{ formatData(res.data) }}
              </div>
              <div class="text-xs" style="color: var(--va-text-secondary); margin-top: 4px;">
                ID: {{ res.id }}
              </div>
            </va-list-item-section>
          </va-list-item>
        </va-list>
        <div v-else-if="!isSearching && searchQuery.trim() !== ''" style="padding: 1rem; text-align: center; color: var(--va-text-secondary);">
          검색 결과가 없습니다.
        </div>
      </va-inner-loading>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCookie } from '#app'

const searchQuery = ref('')
const isFocused = ref(false)
const isSearching = ref(false)
const results = ref([])
const searchContainer = ref(null)

const router = useRouter()
const tokenCookie = useCookie('auth_token')

let debounceTimer = null

const performSearch = async () => {
  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    results.value = []
    return
  }
  
  isSearching.value = true
  try {
    const res = await $fetch(`/api/v1/search?q=${encodeURIComponent(searchQuery.value)}&size=5`, {
      headers: {
        Authorization: `Bearer ${tokenCookie.value}`
      }
    })
    
    if (res && res.content) {
      results.value = res.content
    }
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
  }, 300)
})

const handleBlur = (e) => {
  // Delay blur to allow click on dropdown items
  setTimeout(() => {
    isFocused.value = false
  }, 200)
}

const formatData = (dataStr) => {
  if (!dataStr) return 'No Data'
  try {
    const obj = JSON.parse(dataStr)
    // Extract first 2 keys as preview
    const keys = Object.keys(obj).slice(0, 2)
    return keys.map(k => `${k}: ${obj[k]}`).join(', ')
  } catch(e) {
    return dataStr.substring(0, 50) + '...'
  }
}

const goToRecord = (id) => {
  // Ideally navigate to the specific record, since we don't know the domain ID easily here,
  // we might navigate to a global view or if we have a direct record detail page.
  // Assuming we can pass it or have a global view:
  // router.push(`/records/view/${id}`) 
  alert(`Navigating to record ${id} (Mocked)`)
  isFocused.value = false
  searchQuery.value = ''
}
</script>

<style scoped>
.global-search-container {
  position: relative;
  margin-right: 1.5rem;
  display: flex;
  align-items: center;
}

.search-dropdown {
  position: absolute;
  top: 110%;
  left: 0;
  width: 350px;
  background: var(--va-background-secondary);
  border: 1px solid var(--va-background-border);
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
}

.search-result-item {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid var(--va-background-border);
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-result-item:hover {
  background-color: var(--va-background-element);
}

.search-result-item:last-child {
  border-bottom: none;
}
</style>
