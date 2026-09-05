import { describe, it, expect } from 'vitest'
import fs from 'fs'
import path from 'path'

describe('Batch Round 7 Issue Fixes Verification', () => {
  // #192
  describe('#192: SchemaTreeNode label tooltip & aria-label', () => {
    it('SchemaTreeNode.vue should include title and aria-label bindings on .node-label', () => {
      const filePath = path.resolve(__dirname, '../../components/SchemaTreeNode.vue')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain(':title="node.label"')
      expect(content).toContain(':aria-label="node.label"')
      expect(content).toMatch(/<span[^>]*class="node-label"[^>]*:title="node\.label"[^>]*:aria-label="node\.label"/)
    })
  })

  // #193
  describe('#193: AG-Grid clipped cell tooltips (tooltipValueGetter)', () => {
    it('records.vue defaultColDef should have tooltipValueGetter', () => {
      const filePath = path.resolve(__dirname, '../../pages/records.vue')
      const content = fs.readFileSync(filePath, 'utf-8')
      expect(content).toContain('tooltipValueGetter: (params) => params.valueFormatted || params.value')
    })

    it('approvals.vue pendingGridOptions & myRequestsGridOptions should have defaultColDef with tooltipValueGetter', () => {
      const filePath = path.resolve(__dirname, '../../pages/approvals.vue')
      const content = fs.readFileSync(filePath, 'utf-8')
      expect(content).toContain('tooltipValueGetter: (params) => params.valueFormatted || params.value')
      expect(content).toMatch(/pendingGridOptions[\s\S]*?defaultColDef:\s*\{[\s\S]*?tooltipValueGetter/)
      expect(content).toMatch(/myRequestsGridOptions[\s\S]*?defaultColDef:\s*\{[\s\S]*?tooltipValueGetter/)
    })

    it('channels.vue AG-Grid defaultColDef should include tooltipValueGetter', () => {
      const filePath = path.resolve(__dirname, '../../pages/admin/integration/channels.vue')
      const content = fs.readFileSync(filePath, 'utf-8')
      expect(content).toContain('tooltipValueGetter: (params) => params.valueFormatted || params.value')
    })

    it('workflow.vue defaultColDef should include tooltipValueGetter', () => {
      const filePath = path.resolve(__dirname, '../../pages/admin/workflow.vue')
      const content = fs.readFileSync(filePath, 'utf-8')
      expect(content).toContain('tooltipValueGetter: (params) => params.valueFormatted || params.value')
    })
  })

  // #194
  describe('#194: Mobile DQ Score Trend Chart layout & tooltips', () => {
    it('DqScoreTrendCard.vue should have 56px min-width for sparkline items and tooltips on dates', () => {
      const filePath = path.resolve(__dirname, '../../components/dq/DqScoreTrendCard.vue')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain('min-width: 56px')
      expect(content).toContain(':title="`${formatDateTime(snap.recordedAt)} (${Math.round(snap.score)}점)`"')
      expect(content).toContain(':aria-label="`${formatDateTime(snap.recordedAt)}: ${Math.round(snap.score)}%`"')
      expect(content).toContain('class="dq-date-label"')
      expect(content).toContain(':title="formatDateTime(snap.recordedAt)"')
      expect(content).toContain('dq-trend-controls')
      expect(content).toContain('dq-period-group')
    })

    it('DqScoreTrendCard.vue should style scrollbar and mobile responsive media queries', () => {
      const filePath = path.resolve(__dirname, '../../components/dq/DqScoreTrendCard.vue')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain('.dq-sparkline-container {')
      expect(content).toContain('scrollbar-width: thin;')
      expect(content).toContain('@media (max-width: 640px)')
    })
  })

  // #195
  describe('#195: Mobile Record Toolbar button wrap & clipping prevention', () => {
    it('RecordToolbar.vue should have record-toolbar-actions with horizontal scroll and no wrapping', () => {
      const filePath = path.resolve(__dirname, '../../components/records/RecordToolbar.vue')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain('class="record-toolbar-container"')
      expect(content).toContain('class="record-toolbar-actions"')
      expect(content).toContain('flex-wrap: nowrap;')
      expect(content).toContain('overflow-x: auto;')
      expect(content).toContain('white-space: nowrap;')
      expect(content).toContain('flex-shrink: 0;')
      expect(content).toContain('@media (max-width: 768px)')
    })
  })

  // #179
  describe('#179: Session expired notice & return path redirect preservation', () => {
    it('auth.json should contain auth_session_expired translations', () => {
      const koAuth = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../i18n/locales/ko/auth.json'), 'utf-8'))
      const enAuth = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../i18n/locales/en/auth.json'), 'utf-8'))

      expect(koAuth.auth_session_expired).toBe('세션이 만료되었습니다. 다시 로그인해 주세요.')
      expect(enAuth.auth_session_expired).toBe('Your session has expired. Please log in again.')
    })

    it('auth.global.ts middleware should preserve redirect path and set expired flag', () => {
      const filePath = path.resolve(__dirname, '../../middleware/auth.global.ts')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain('redirect: redirectPath')
      expect(content).toContain('expired: \'1\'')
    })

    it('fetch-interceptor.client.ts should include redirectToLoginExpired with currentPath param', () => {
      const filePath = path.resolve(__dirname, '../../plugins/fetch-interceptor.client.ts')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain('redirectToLoginExpired')
      expect(content).toContain('redirect=${encodeURIComponent(currentPath)}')
    })

    it('NotificationBell.vue should preserve redirect query on session expiry', () => {
      const filePath = path.resolve(__dirname, '../../components/layout/NotificationBell.vue')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain('redirect=${encodeURIComponent(currentPath)}')
    })

    it('login.vue should support getSafeRedirectUrl, sessionStorage, and auth_session_expired toast', () => {
      const filePath = path.resolve(__dirname, '../../pages/login.vue')
      const content = fs.readFileSync(filePath, 'utf-8')

      expect(content).toContain('getSafeRedirectUrl')
      expect(content).toContain('post_login_redirect')
      expect(content).toContain('t(\'auth_session_expired\')')
      expect(content).toContain('color: isExpired ? \'warning\' : \'danger\'')
    })
  })
})
