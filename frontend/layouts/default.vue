<template>
  <div class="layout-wrapper" style="opacity: 0;" :style="{ opacity: isMounted ? 1 : 0, transition: 'opacity 0.3s ease' }">
    <va-layout>
      <template #top>
        <va-navbar :color="isDark ? '#1F2937' : 'primary'">
          <template #left>
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <va-icon name="menu" @click="showSidebar = !showSidebar" class="mobile-menu-btn" style="cursor: pointer; font-size: 28px;" />
              <va-navbar-item class="font-bold text-lg text-white title-text" style="padding: 0;">
                <span class="full-title">Domain Governance System</span>
                <span class="short-title">Domain System</span>
              </va-navbar-item>
            </div>
          </template>
          <template #right>
            <va-navbar-item class="text-white">
              <div class="navbar-right">
                <!-- Notification Bell -->
                <NotificationBell class="mr-2" />

                <!-- Radio DJ Control Button for Admins -->
                <va-button
                  v-if="effectiveRoles.includes('ROLE_ADMIN')"
                  preset="plain"
                  class="mr-2"
                  style="color: white !important;"
                  title="🎵 DJ 방송 제어판"
                  @click="showRadioDjModal = true"
                >
                  <va-icon name="radio" size="large" />
                </va-button>

                <!-- Theme Toggle for Desktop -->
                <va-button preset="plain" class="mr-2 hide-mobile theme-btn" @click="toggleTheme" style="color: white !important;">
                  <va-icon :name="isDark ? 'light_mode' : 'dark_mode'" size="large" />
                </va-button>

                <!-- User Profile Dropdown -->
                <va-dropdown placement="bottom-end" stick-to-edges>
                  <template #anchor>
                    <va-button preset="plain" class="profile-btn" style="color: white !important; padding: 0.35rem 0.85rem; border-radius: 20px; background: rgba(255,255,255,0.15); backdrop-filter: blur(8px); transition: all 0.2s ease;">
                      <va-avatar size="24px" color="warning" class="mr-2" style="font-weight: 800; font-size: 0.8rem;">
                        {{ (currentUser?.username || '').charAt(0).toUpperCase() }}
                      </va-avatar>
                      <span class="username-text font-bold" style="font-size: 0.92rem;">{{ currentUser?.username || '' }}</span>
                      <va-icon name="expand_more" class="ml-1" size="small" />
                    </va-button>
                  </template>
                  <va-dropdown-content :style="{ padding: '0', minWidth: '320px', borderRadius: '16px', overflow: 'hidden', boxShadow: isDark ? '0 16px 45px rgba(0,0,0,0.7), 0 0 25px rgba(99,102,241,0.25)' : '0 12px 36px rgba(0,0,0,0.18)', border: isDark ? '1px solid rgba(139,92,246,0.35)' : '1px solid var(--va-background-border)' }">
                    <!-- Premium Header Banner -->
                    <div :style="{ background: isDark ? 'linear-gradient(135deg, #0f172a 0%, #1e1b4b 55%, #312e81 100%)' : 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)', color: 'white', padding: '1.25rem 1.25rem 1.1rem 1.25rem', display: 'flex', flexDirection: 'column', gap: '0.85rem', borderBottom: isDark ? '1px solid rgba(139,92,246,0.3)' : 'none' }">
                      <div style="display: flex; align-items: center; gap: 0.85rem;">
                        <va-avatar size="large" :color="isDark ? 'warning' : 'warning'" :style="{ fontWeight: '800', fontSize: '1.2rem', border: isDark ? '2px solid #a78bfa' : '2px solid white', boxShadow: isDark ? '0 0 15px rgba(167,139,250,0.5)' : '0 4px 10px rgba(0,0,0,0.25)' }">
                          {{ (currentUser?.username || '').charAt(0).toUpperCase() }}
                        </va-avatar>
                        <div style="flex: 1; overflow: hidden;">
                          <div style="font-weight: 800; font-size: 1.15rem; letter-spacing: -0.01em; display: flex; align-items: center; gap: 0.35rem;">
                            <span :style="{ color: isDark ? '#f3f4f6' : 'white' }">{{ currentUser?.username || '' }}</span>
                            <va-icon v-if="effectiveRoles.includes('ROLE_ADMIN')" name="verified" size="small" color="warning" title="Admin User" />
                          </div>
                          <div style="font-size: 0.8rem; opacity: 0.9; margin-top: 0.15rem; display: flex; align-items: center; gap: 0.35rem; flex-wrap: wrap;">
                            <span>🏢 {{ currentOrgName || $t('belongs_to_org') || '소속 조직' }}</span>
                            <span v-if="currentUserDeptName" style="display: inline-flex; align-items: center; gap: 0.2rem;">| <va-icon :name="currentUserDeptIcon" size="small" /> {{ currentUserDeptName }}</span>
                          </div>
                        </div>
                      </div>

                      <!-- Effective Roles Section -->
                      <div :style="{ background: isDark ? 'rgba(15, 23, 42, 0.65)' : 'rgba(255,255,255,0.15)', backdropFilter: 'blur(12px)', borderRadius: '10px', padding: '0.65rem 0.85rem', display: 'flex', flexDirection: 'column', gap: '0.35rem', border: isDark ? '1px solid rgba(167, 139, 250, 0.3)' : '1px solid rgba(255,255,255,0.2)' }">
                        <div style="font-size: 0.72rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em; opacity: 0.9; display: flex; justify-content: space-between; align-items: center;">
                          <span>{{ $t('effective_roles') || '유효 통합 권한 (Effective Roles)' }}</span>
                          <span :style="{ background: isDark ? 'rgba(139,92,246,0.35)' : 'rgba(0,0,0,0.25)', color: isDark ? '#ddd6fe' : 'white' }" style="font-size: 0.65rem; padding: 1px 6px; border-radius: 10px; font-weight: 800;">UNION</span>
                        </div>
                        <div style="display: flex; flex-wrap: wrap; gap: 0.35rem; margin-top: 0.1rem;">
                          <span
                            v-for="r in displayRoles"
                            :key="r"
                            :style="getRoleBadgeStyle(r)"
                            style="padding: 3px 9px; border-radius: 12px; font-size: 0.75rem; font-weight: 800;"
                          >
                            {{ r }}
                          </span>
                        </div>
                      </div>
                    </div>

                    <!-- Dropdown Actions List -->
                    <div style="padding: 0.5rem; background: var(--va-background-secondary);">
                      <va-list style="display: flex; flex-direction: column; gap: 0.25rem;">
                        <va-list-item @click="toggleLang" class="dropdown-menu-item">
                          <va-list-item-section icon style="min-width: 32px;">
                            <div class="menu-icon-box">
                              <va-icon name="language" size="small" color="primary" />
                            </div>
                          </va-list-item-section>
                          <va-list-item-section style="font-weight: 600; font-size: 0.9rem;">
                            {{ currentLocale === 'ko' ? 'Switch to English' : '한국어로 변경' }}
                          </va-list-item-section>
                        </va-list-item>

                        <va-list-item @click="showRequestAccessModal = true" class="dropdown-menu-item">
                          <va-list-item-section icon style="min-width: 32px;">
                            <div class="menu-icon-box">
                              <va-icon name="vpn_key" size="small" color="success" />
                            </div>
                          </va-list-item-section>
                          <va-list-item-section style="font-weight: 600; font-size: 0.9rem;">
                            {{ $t('request_domain_access') || '도메인 접근 권한 신청' }}
                          </va-list-item-section>
                        </va-list-item>

                        <va-list-item @click="showSettingsModal = true" class="dropdown-menu-item">
                          <va-list-item-section icon style="min-width: 32px;">
                            <div class="menu-icon-box">
                              <va-icon name="settings" size="small" color="warning" />
                            </div>
                          </va-list-item-section>
                          <va-list-item-section style="font-weight: 600; font-size: 0.9rem;">
                            {{ $t('personal_settings') || 'Personal Settings (타임존 설정)' }}
                          </va-list-item-section>
                        </va-list-item>

                        <va-divider style="margin: 0.3rem 0;" />

                        <va-list-item @click="handleLogout" class="dropdown-menu-item text-danger">
                          <va-list-item-section icon style="min-width: 32px;">
                            <div class="menu-icon-box bg-red">
                              <va-icon name="logout" size="small" color="danger" />
                            </div>
                          </va-list-item-section>
                          <va-list-item-section style="font-weight: 700; font-size: 0.9rem; color: var(--va-danger);">
                            {{ $t('logout') || '로그아웃' }}
                          </va-list-item-section>
                        </va-list-item>
                      </va-list>
                    </div>
                  </va-dropdown-content>
                </va-dropdown>
              </div>
            </va-navbar-item>
          </template>
        </va-navbar>
      </template>
      
      <template #left>
        <va-sidebar v-model="showSidebar" :minimized="false" class="responsive-sidebar" :class="{ 'dark-theme-sidebar': isDark }">
          <SidebarMenuItem v-for="menu in filteredMenus" :key="menu.id" :menu="menu" />
        </va-sidebar>
      </template>
      
      <template #content>
        <main class="responsive-main" :style="{ backgroundColor: isDark ? 'var(--va-background-primary)' : '#f4f6f8' }">
          <slot />
        </main>

        <!-- Personal Settings Modal -->
        <va-modal v-model="showSettingsModal" :title="$t('personal_settings') || 'Personal Settings'" hide-default-actions>
          <div style="min-width: 320px; padding: 1rem 1.5rem 1.5rem 1.5rem; overflow: hidden; box-sizing: border-box;">
            <div class="mb-4" style="display: flex; flex-direction: column; gap: 0.5rem;">
              <span style="font-size: 0.85rem; color: var(--va-text-secondary); font-weight: 600;">
                {{ $t('timezone') || 'Timezone Settings' }}
              </span>
              <TimezoneSelect 
                v-model="selectedTimezone" 
                :syncWithCookie="true"
              />
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem;">
              <va-button preset="secondary" color="secondary" @click="showSettingsModal = false">Cancel</va-button>
              <va-button :loading="isSavingTimezone" @click="handleSaveTimezone">Save</va-button>
            </div>
          </div>
        </va-modal>
        <!-- Request Access Modal -->
        <DomainAccessRequestModal v-model="showRequestAccessModal" />
        <!-- Floating In-App Messenger Widget -->
        <InAppMessenger />
        <!-- Live System Radio Player Widget -->
        <SystemRadioWidget />
        <!-- Admin DJ Music Control Modal -->
        <AdminMusicControlModal v-model="showRadioDjModal" />
      </template>
    </va-layout>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useCookie, useState } from '#app'
import { useColors } from 'vuestic-ui'
import { useMenu } from '~/composables/useMenu'
import NotificationBell from '~/components/layout/NotificationBell.vue'
import InAppMessenger from '~/components/chat/InAppMessenger.vue'
import SystemRadioWidget from '~/components/chat/SystemRadioWidget.vue'
import AdminMusicControlModal from '~/components/chat/AdminMusicControlModal.vue'
import { useRoles } from '~/composables/useRoles'

const { t, locale, setLocale } = useI18n()
const { applyPreset, currentPresetName } = useColors()
const { getRoleBadgeStyle: getStoreRoleBadgeStyle } = useRoles()

const router = useRouter()
const tokenCookie = useCookie('auth_token')
const userCookie = useCookie('user_data')
const currentLocale = useCookie('locale', { default: () => 'ko' })
const savedTheme = useCookie('theme', { default: () => 'light' })

const showSettingsModal = ref(false)
const showRadioDjModal = ref(false)
const savedTimezone = useCookie('timezone', { default: () => 'Asia/Seoul' })
const selectedTimezone = ref('')
const isSavingTimezone = ref(false)

const showRequestAccessModal = ref(false)

const { menus, fetchMenus } = useMenu()

const userRolesArray = computed(() => {
  const r = currentUser.value?.role
  if (!r) return ['ROLE_USER']
  if (Array.isArray(r)) return r
  return String(r).split(',').map(item => item.trim()).filter(Boolean)
})

const deptRolesMap = ref({})

const deptNameMap = ref({})
const deptIconMap = ref({})

const fetchDepartmentRoles = async () => {
  if (!tokenCookie.value || !currentUser.value?.organizationId) return
  try {
    const depts = await $fetch(`/api/organizations/${currentUser.value.organizationId}/departments`, {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    if (depts && Array.isArray(depts)) {
      const map = {}
      const nameMap = {}
      const iconMap = {}
      depts.forEach(d => {
        if (d.role) map[d.id] = d.role
        nameMap[d.id] = d.name
        if (d.icon) iconMap[d.id] = d.icon
      })
      deptRolesMap.value = map
      deptNameMap.value = nameMap
      deptIconMap.value = iconMap
    }
  } catch (e) {
    console.error('Failed to fetch dept roles for header:', e)
  }
}

const parseMultilingualText = (text) => {
  if (!text) return ''
  const currentLang = (locale?.value || 'ko').toLowerCase().startsWith('en') ? 'en' : 'ko'

  if (typeof text === 'object' && text !== null) {
    return text[currentLang] || text.ko || text.en || ''
  }

  const str = String(text).trim()
  if (str.startsWith('{')) {
    try {
      const parsed = JSON.parse(str)
      if (parsed && typeof parsed === 'object') {
        const val = currentLang === 'en' ? (parsed.en || parsed.ko) : (parsed.ko || parsed.en)
        if (val) return String(val)
      }
    } catch (e) {}
  }

  return str
}

const currentUserDeptName = computed(() => {
  const deptId = currentUser.value?.departmentId
  const rawName = deptId ? (deptNameMap.value[deptId] || null) : null
  return parseMultilingualText(rawName)
})

const currentUserDeptIcon = computed(() => {
  const deptId = currentUser.value?.departmentId
  return deptId ? (deptIconMap.value[deptId] || 'folder') : 'folder'
})

const expandRoleNames = (roles) => {
  const set = new Set()
  roles.forEach(r => {
    if (!r) return
    const str = String(r).trim()
    if (!str) return
    const clean = str.replace(/^ROLE_/, '')
    set.add(clean)
    set.add('ROLE_' + clean)
  })
  return Array.from(set)
}

const effectiveRoles = computed(() => {
  const directRoles = userRolesArray.value
  const userDeptId = currentUser.value?.departmentId
  const deptRoleStr = userDeptId ? deptRolesMap.value[userDeptId] : null
  const deptRoles = deptRoleStr ? String(deptRoleStr).split(',').map(r => r.trim()).filter(Boolean) : []
  return expandRoleNames([...directRoles, ...deptRoles])
})

const displayRoles = computed(() => {
  const roles = effectiveRoles.value
  const set = new Set(roles.map(r => String(r).replace(/^ROLE_/, '')))
  return Array.from(set)
})

const effectiveRolesDisplay = computed(() => {
  return displayRoles.value.join(', ') || 'GUEST'
})

const getRoleBadgeStyle = (role) => {
  return getStoreRoleBadgeStyle(role, isDark.value)
}

const filteredMenus = computed(() => {
  const myRoles = effectiveRoles.value

  const hasAccess = (node) => {
    // requiredRoles 배열 (API 응답 표준)
    const roles = node.requiredRoles || []
    // requiredRole 문자열 (레거시 호환)
    if (node.requiredRole) {
      node.requiredRole.split(',').map(r => r.trim()).filter(Boolean).forEach(r => roles.push(r))
    }
    if (roles.length === 0) return true
    return myRoles.some(myRole => roles.includes(myRole))
  }

  const filterTree = (nodes) => {
    return nodes.filter(node => {
      if (!hasAccess(node)) return false
      if (node.children && node.children.length > 0) {
        node.children = filterTree(node.children)
        // 부모 메뉴는 접근 가능한 자식이 하나라도 있어야 표시
        if (node.children.length === 0) return false
      }
      return true
    })
  }

  if (!menus.value) return []
  const menusCopy = JSON.parse(JSON.stringify(menus.value))
  return filterTree(menusCopy)
})

watch(showSettingsModal, (isOpen) => {
  if (isOpen) {
    selectedTimezone.value = savedTimezone.value || 'Asia/Seoul'
  }
})

const handleSaveTimezone = async () => {
  isSavingTimezone.value = true
  try {
    savedTimezone.value = selectedTimezone.value
    
    if (userCookie.value) {
      const usr = typeof userCookie.value === 'string' ? JSON.parse(userCookie.value) : userCookie.value
      usr.timezone = selectedTimezone.value
      userCookie.value = JSON.stringify(usr)
    }
    
    await $fetch('/api/users/timezone', {
      method: 'POST',
      headers: { Authorization: `Bearer ${tokenCookie.value}` },
      body: { timezone: selectedTimezone.value }
    })
    
    showSettingsModal.value = false
    window.location.reload()
  } catch (e) {
    console.error('Failed to save timezone:', e)
  } finally {
    isSavingTimezone.value = false
  }
}

const isDark = computed(() => currentPresetName.value === 'dark')

if (setLocale) setLocale(currentLocale.value || 'ko')
else locale.value = currentLocale.value || 'ko'

watch(currentLocale, (newVal) => {
  if (setLocale) setLocale(newVal || 'ko')
  else locale.value = newVal || 'ko'
})

const toggleTheme = () => {
  const newTheme = isDark.value ? 'light' : 'dark'
  applyPreset(newTheme)
  savedTheme.value = newTheme
}

const toggleLang = () => {
  const newLang = currentLocale.value === 'ko' ? 'en' : 'ko'
  currentLocale.value = newLang
  if (setLocale) setLocale(newLang)
  else locale.value = newLang
  if (typeof document !== 'undefined') {
    document.documentElement.lang = newLang === 'en' ? 'en-US' : 'ko-KR'
  }
}

watch(currentLocale, (newLang) => {
  if (typeof document !== 'undefined') {
    document.documentElement.lang = newLang === 'en' ? 'en-US' : 'ko-KR'
  }
}, { immediate: true })
const showSidebar = ref(true)
const isMobile = ref(false)

const route = useRoute()

const isMounted = ref(false)
const userPermissionsCookie = useCookie('user_permissions')
const { currentUser, fetchCurrentUser } = useAuthUser()

const syncCurrentUserInfo = async () => {
  if (!tokenCookie.value) return
  const me = await fetchCurrentUser(true)
  if (me && Array.isArray(me.permissions)) {
    userPermissionsCookie.value = me.permissions
  }
}

onMounted(async () => {
  if (savedTheme.value) {
    applyPreset(savedTheme.value)
  }
  
  await syncCurrentUserInfo()
  await fetchMenus(true)
  await fetchUserOrganizationName()
  await fetchDepartmentRoles()

  if (window.innerWidth < 768) {
    isMobile.value = true
    showSidebar.value = false
  }
  window.addEventListener('resize', () => {
    const isNowMobile = window.innerWidth < 768
    if (isMobile.value && !isNowMobile) {
      showSidebar.value = true // Restore on PC
    }
    isMobile.value = isNowMobile
  })
  isMounted.value = true
})

watch(route, () => {
  if (isMobile.value) {
    showSidebar.value = false
  }
})

const userOrgNameMap = ref({})

const fetchUserOrganizationName = async () => {
  if (!tokenCookie.value) return
  try {
    const orgs = await $fetch('/api/organizations', {
      headers: { Authorization: `Bearer ${tokenCookie.value}` }
    })
    if (orgs && Array.isArray(orgs)) {
      const map = {}
      orgs.forEach(o => {
        map[o.id] = o.displayName || o.name
      })
      userOrgNameMap.value = map
    }
  } catch (e) {
    console.error('Failed to fetch orgs for header navbar:', e)
  }
}

const currentOrgName = computed(() => {
  const orgId = currentUser.value?.organizationId
  const raw = (orgId && userOrgNameMap.value[orgId]) ? userOrgNameMap.value[orgId] : null
  return parseMultilingualText(raw)
})

const handleLogout = () => {
  tokenCookie.value = null
  userCookie.value = null
  if (process.client) {
    document.cookie = 'auth_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
    document.cookie = 'user_data=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
  }
  router.push('/login')
}
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Inter', sans-serif;
  background-color: var(--va-background-primary);
  color: var(--va-text-primary);
  height: 100vh;
  width: 100%;
  overflow: hidden; /* Prevent double scrollbars, let va-layout handle it */
}
.layout-wrapper {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
.navbar-right {
  display: flex; 
  align-items: center; 
  white-space: nowrap; 
  padding-right: 1.5rem;
}
.profile-btn {
  text-transform: none !important;
}
.profile-dropdown-content {
  min-width: 240px;
  padding: 0.5rem 0;
}
.dropdown-item {
  padding: 0.75rem 1rem;
  transition: background-color 0.2s;
}
.dropdown-item:hover {
  background-color: var(--va-background-element);
}
.hide-mobile {
  display: block;
}
.show-mobile {
  display: none !important;
}
.mobile-menu-btn {
  display: inline-flex !important;
}
.responsive-main {
  padding: 1rem;
  height: calc(100vh - 64px);
  box-sizing: border-box;
  overflow-y: auto;
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}
.short-title { display: none; }

/* Minimalist Flat Sidebar Navigation UI */
.va-sidebar {
  border-right: 1px solid var(--va-background-border);
}
.va-sidebar-item {
  margin: 0 !important;
  padding: 0 !important;
  border-radius: 0 !important;
  transition: background-color 0.15s ease !important;
  color: #111827 !important;
  background-color: transparent !important;
  min-height: 40px !important; /* Slightly increased height */
}
.va-sidebar-item-content {
  padding: 8px 1.2rem !important; /* Slightly increased padding */
  min-height: 40px !important;
  display: flex !important;
  align-items: center !important;
}
.va-sidebar__menu, .va-sidebar__body, .va-accordion, .va-collapse, .va-collapse__body, .va-collapse__content, .va-collapse__content-wrapper {
  padding: 0 !important;
  margin: 0 !important;
  gap: 0 !important;
}
.va-sidebar-item .va-icon {
  color: #6b7280 !important;
}
.va-sidebar-item:hover {
  background-color: rgba(0, 0, 0, 0.04) !important;
}

/* Active State */
.va-sidebar-item--active {
  background-color: #f0f4f8 !important; 
  border-left: 3px solid var(--va-primary) !important;
}
.va-sidebar-item--active .va-sidebar-item-title,
.va-sidebar-item--active .va-icon {
  color: var(--va-primary) !important;
  font-weight: 600;
}

/* Dark Theme Sidebar Overrides */
.dark-theme-sidebar {
  border-right: 1px solid rgba(255, 255, 255, 0.1);
}
.dark-theme-sidebar .va-sidebar-item {
  color: #e5e7eb !important;
}
.dark-theme-sidebar .va-sidebar-item .va-icon {
  color: #9ca3af !important;
}
.dark-theme-sidebar .va-sidebar-item:hover {
  background-color: rgba(255, 255, 255, 0.05) !important;
}
.dark-theme-sidebar .va-sidebar-item--active {
  background-color: rgba(59, 130, 246, 0.1) !important;
}
.dark-theme-sidebar .va-sidebar-item--active .va-sidebar-item-title,
.dark-theme-sidebar .va-sidebar-item--active .va-icon {
  color: #60A5FA !important;
}

/* Premium Dropdown Styling */
.profile-btn:hover {
  background: rgba(255, 255, 255, 0.25) !important;
  transform: translateY(-1px);
}

.dropdown-menu-item {
  cursor: pointer;
  border-radius: 8px;
  padding: 0.55rem 0.75rem !important;
  transition: all 0.2s ease !important;
}
.dropdown-menu-item:hover {
  background: var(--va-background-element) !important;
  transform: translateX(4px);
}

.menu-icon-box {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--va-background-element);
}

@media (max-width: 768px) {
  body {
    overflow-x: hidden;
  }
  .responsive-main {
    padding: 0.25rem !important;
  }
  .username-text {
    display: none;
  }
  .navbar-right {
    padding-right: 0.75rem;
  }
  .responsive-sidebar {
    position: absolute;
    z-index: 1000;
    height: 100%;
  }
  .full-title { display: none; }
  .short-title { display: inline; font-size: 1.1rem; }
  .hide-mobile { display: none !important; }
  .show-mobile { display: flex !important; }
  .mobile-menu-btn { display: inline-flex !important; }
}
</style>
