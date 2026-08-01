<template>
  <!-- No children: Render as a single link item -->
  <va-sidebar-item v-if="!menu.children || menu.children.length === 0" 
                   :active="$route.path === menu.path" 
                   :to="menu.path"
                   class="sidebar-item"
                   :style="{ paddingLeft: `${depth * 0.8 + 0.8}rem !important` }">
    <va-sidebar-item-content>
      <va-icon :name="menu.icon || 'circle'" :size="depth > 0 ? 'small' : 'medium'" />
      <va-sidebar-item-title style="margin-left: 0.5rem;">
        {{ getMenuTitle(menu) }}
      </va-sidebar-item-title>
    </va-sidebar-item-content>
  </va-sidebar-item>

  <!-- Has children: Render as accordion/collapse -->
  <va-accordion v-else>
    <va-collapse v-model="isExpanded">
      <template #header>
        <va-sidebar-item>
          <va-sidebar-item-content style="display: flex; justify-content: space-between; align-items: center; width: 100%;"
                                   :style="{ paddingLeft: `${depth * 0.8 + 0.8}rem !important` }">
            <div style="display: flex; align-items: center;">
              <va-icon :name="menu.icon || 'folder'" :size="depth > 0 ? 'small' : 'medium'" />
              <va-sidebar-item-title style="margin-left: 0.5rem;">
                {{ getMenuTitle(menu) }}
              </va-sidebar-item-title>
            </div>
            <va-icon :name="isExpanded ? 'expand_less' : 'expand_more'" />
          </va-sidebar-item-content>
        </va-sidebar-item>
      </template>

      <!-- Recursive call for children -->
      <SidebarMenuItem v-for="child in menu.children" :key="child.id" :menu="child" :depth="depth + 1" />
    </va-collapse>
  </va-accordion>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const { t, te, locale } = useI18n()

const props = defineProps({
  menu: {
    type: Object,
    required: true
  },
  depth: {
    type: Number,
    default: 0
  }
})

const router = useRouter()
const route = useRoute()
const isExpanded = ref(false)

const currentLang = computed(() => {
  const loc = (locale?.value || useCookie('locale')?.value || 'ko').toLowerCase()
  return loc.startsWith('en') ? 'en' : 'ko'
})

const getMenuTitle = (menu) => {
  if (!menu || !menu.name) return ''

  // DB에 정식 보관된 JSON 구조 {"ko": "...", "en": "..."} 파싱
  try {
    const parsed = typeof menu.name === 'object' ? menu.name : (String(menu.name).trim().startsWith('{') ? JSON.parse(menu.name) : null)
    if (parsed && typeof parsed === 'object') {
      const title = currentLang.value === 'en' ? (parsed.en || parsed.ko) : (parsed.ko || parsed.en)
      if (title) return String(title)
    }
  } catch (e) {}

  return String(menu.name).trim()
}

const checkActiveChild = () => {
  if (props.menu.children && props.menu.children.length > 0) {
    const hasActiveChild = props.menu.children.some(child => route.path.startsWith(child.path) && child.path !== '/')
    if (hasActiveChild) {
      isExpanded.value = true
    }
  }
}

onMounted(() => {
  checkActiveChild()
})

watch(() => route.path, () => {
  checkActiveChild()
})
</script>

<style scoped>
/* Scoped styles if needed */
</style>
