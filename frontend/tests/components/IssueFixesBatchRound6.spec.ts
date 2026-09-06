import { describe, it, expect } from 'vitest'
import fs from 'fs'
import path from 'path'

describe('Batch Round 6 Issue Fixes (#199 ~ #207)', () => {
  const rootDir = path.resolve(__dirname, '../../')

  // #202: userPermissionsCookie is defined in default.vue
  describe('#202 - userPermissionsCookie definition in default.vue', () => {
    const layoutContent = fs.readFileSync(path.resolve(rootDir, 'layouts/default.vue'), 'utf-8')

    it('should declare userPermissionsCookie using useCookie', () => {
      expect(layoutContent).toMatch(/const\s+userPermissionsCookie\s*=\s*useCookie/)
    })

    it('syncCurrentUserInfo should safely update userPermissionsCookie', () => {
      expect(layoutContent).toContain('userPermissionsCookie.value = me.permissions')
    })
  })

  // #206: Sidebar resize 375px -> 768px should not overlap main content
  describe('#206 - Responsive sidebar & main layout', () => {
    const layoutContent = fs.readFileSync(path.resolve(rootDir, 'layouts/default.vue'), 'utf-8')

    it('should align mobile/tablet breakpoint between JS and CSS', () => {
      // 768px should not force open sidebar without desktop width
      expect(layoutContent).not.toContain('if (isMobile.value && !isNowMobile) {\n      showSidebar.value = true')
    })
  })

  // #199: OIDC Keycloak clientSecret fallback in nuxt.config.ts
  describe('#199 - OIDC Keycloak configuration', () => {
    const configContent = fs.readFileSync(path.resolve(rootDir, 'nuxt.config.ts'), 'utf-8')

    it('should provide clientSecret fallback to avoid empty string validation error', () => {
      expect(configContent).toMatch(/clientSecret:\s*process\.env\.KEYCLOAK_CLIENT_SECRET\s*\|\|\s*['"]secret['"]/)
    })
  })

  // #200: Login a11y (lang, main landmark, footer contrast)
  describe('#200 - Login page a11y enhancements', () => {
    const loginContent = fs.readFileSync(path.resolve(rootDir, 'pages/login.vue'), 'utf-8')

    it('should contain a semantic <main> tag with role="main"', () => {
      expect(loginContent).toMatch(/<main[^>]*class="auth-box"[^>]*role="main"/)
    })

    it('should set html lang attribute via useHead', () => {
      expect(loginContent).toContain('htmlAttrs')
      expect(loginContent).toContain('lang')
    })

    it('should use high-contrast color for light theme footer', () => {
      expect(loginContent).toMatch(/\.theme-light\s+\.auth-footer\s*\{[^}]*color:\s*#475569/)
    })
  })

  // #201: titleTemplate prevents duplicate service name
  describe('#201 - Title template duplicate prevention', () => {
    const configContent = fs.readFileSync(path.resolve(rootDir, 'nuxt.config.ts'), 'utf-8')

    it('should use a function for titleTemplate to prevent duplication', () => {
      expect(configContent).toContain('titleTemplate:')
      expect(configContent).toMatch(/titleTemplate:\s*\(titleChunk[^)]*\)\s*=>/)
    })
  })

  // #205: va-switch aria-label $t:switch prevention
  describe('#205 - VaSwitch aria-label $t:switch fix', () => {
    const patchContent = fs.readFileSync(path.resolve(rootDir, 'patch-oidc.js'), 'utf-8')
    const addMenuContent = fs.readFileSync(path.resolve(rootDir, 'components/admin/AddMenuModal.vue'), 'utf-8')
    const codeGroupContent = fs.readFileSync(path.resolve(rootDir, 'components/admin/CodeGroupModal.vue'), 'utf-8')

    it('patch-oidc.js should include patch for $t:switch in VaSwitch', () => {
      expect(patchContent).toContain('$t:switch')
    })

    it('AddMenuModal should provide an explicit aria-label for the switch', () => {
      expect(addMenuContent).toMatch(/<va-switch[^>]*aria-label=/)
    })

    it('CodeGroupModal should provide an explicit aria-label for the switch', () => {
      expect(codeGroupContent).toMatch(/<va-switch[^>]*aria-label=/)
    })
  })

  // #203: Mobile responsive layouts for DQ, menus, and workflow
  describe('#203 - Mobile responsive headers and controls', () => {
    const dqContent = fs.readFileSync(path.resolve(rootDir, 'pages/dq-dashboard.vue'), 'utf-8')
    const menuContent = fs.readFileSync(path.resolve(rootDir, 'pages/admin/menus.vue'), 'utf-8')
    const workflowContent = fs.readFileSync(path.resolve(rootDir, 'pages/admin/workflow.vue'), 'utf-8')

    it('dq-dashboard should have responsive top action bar classes', () => {
      expect(dqContent).toContain('top-action-bar')
      expect(dqContent).toMatch(/@media\s*\(max-width:\s*768px\)/)
    })

    it('admin/menus should have responsive action bar and tree-form layout', () => {
      expect(menuContent).toContain('top-action-bar')
      expect(menuContent).toMatch(/@media\s*\(max-width:\s*768px\)/)
    })

    it('admin/workflow should have responsive header and filter bar', () => {
      expect(workflowContent).toContain('top-action-bar')
      expect(workflowContent).toMatch(/@media\s*\(max-width:\s*768px\)/)
    })
  })

  // #204: Mobile system logs chart xAxis label rotation
  describe('#204 - System logs chart xAxis label formatting', () => {
    const sysLogsContent = fs.readFileSync(path.resolve(rootDir, 'pages/admin/system-logs.vue'), 'utf-8')

    it('chartOption xAxis should have rotate or overflow handling', () => {
      expect(sysLogsContent).toMatch(/rotate:\s*4[05]/)
    })
  })

  // #207: Approval monitor AG-Grid pagination & chat FAB safe spacing
  describe('#207 - Approval monitor pagination & messenger FAB', () => {
    const approvalContent = fs.readFileSync(path.resolve(rootDir, 'pages/admin/approval-monitor.vue'), 'utf-8')
    const messengerContent = fs.readFileSync(path.resolve(rootDir, 'components/chat/InAppMessenger.vue'), 'utf-8')

    it('approval-monitor should have mobile ag-paging-panel styling', () => {
      expect(approvalContent).toContain('ag-paging-panel')
    })

    it('InAppMessenger toggle button should have mobile safe offset', () => {
      expect(messengerContent).toMatch(/bottom:\s*90px/)
    })
  })
})
