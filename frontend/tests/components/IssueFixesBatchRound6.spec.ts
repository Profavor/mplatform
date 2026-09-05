import { describe, it, expect } from 'vitest'
import fs from 'fs'
import path from 'path'
import { getAgGridLocaleText, AG_GRID_LOCALE_KO, AG_GRID_LOCALE_EN } from '../../utils/agGridLocale'

describe('Batch Round 6 Issue Fixes', () => {
  // #176: Brand name consistency in login.vue
  describe('#176 - Brand name unified to Domain Governance System', () => {
    const loginContent = fs.readFileSync(
      path.resolve(__dirname, '../../pages/login.vue'),
      'utf-8'
    )

    it('login.vue title should use $t("footer.system_name")', () => {
      expect(loginContent).toContain("{{ $t('footer.system_name') }}")
      expect(loginContent).not.toContain('<h1 class="title">Domain System</h1>')
    })

    it('login.vue footer should use $t("footer.copyright")', () => {
      expect(loginContent).toContain("{{ $t('footer.copyright'")
      expect(loginContent).not.toContain('&copy; 2026 Domain System. All rights reserved.')
    })
  })

  // #174: Mobile sidebar initial closed state & auto close on resize
  describe('#174 - Mobile sidebar default closed and resize handling', () => {
    const defaultLayoutContent = fs.readFileSync(
      path.resolve(__dirname, '../../layouts/default.vue'),
      'utf-8'
    )

    it('showSidebar should be conditionally initialized based on viewport width', () => {
      expect(defaultLayoutContent).toContain("showSidebar = ref(typeof window !== 'undefined' ? window.innerWidth >= 768 : false)")
    })

    it('resize listener should auto-close sidebar when switching to mobile', () => {
      expect(defaultLayoutContent).toContain("showSidebar.value = false // Auto close on mobile")
    })
  })

  // #178: RecordToolbar reset buttons separation
  describe('#178 - Record toolbar reset button separation', () => {
    const toolbarContent = fs.readFileSync(
      path.resolve(__dirname, '../../components/records/RecordToolbar.vue'),
      'utf-8'
    )

    it('domain records reset should use delete_sweep icon', () => {
      expect(toolbarContent).toContain('name="delete_sweep"')
    })

    it('filter reset should use filter_alt_off icon and reset_filters label', () => {
      expect(toolbarContent).toContain('icon="filter_alt_off"')
      expect(toolbarContent).toContain("t('reset_filters')")
    })

    it('toolbar should include vertical divider between actions and view controls', () => {
      expect(toolbarContent).toContain('<va-divider vertical class="mx-1" />')
    })
  })

  // #169: Single click row selection, hint, and enter key detail open
  describe('#169 - Record grid single-click selection and double-click hint', () => {
    const recordsContent = fs.readFileSync(
      path.resolve(__dirname, '../../pages/records.vue'),
      'utf-8'
    )

    it('ag-grid rowSelection should have enableClickSelection: true', () => {
      expect(recordsContent).toContain("enableClickSelection: true")
    })

    it('should include double-click hint banner with row_double_click_hint i18n key', () => {
      expect(recordsContent).toContain("$t('row_double_click_hint')")
    })

    it('should have onRowClicked and onCellKeyDown event listeners', () => {
      expect(recordsContent).toContain('@row-clicked="onRowClicked"')
      expect(recordsContent).toContain('@cell-key-down="onCellKeyDown"')
      expect(recordsContent).toContain('const onRowClicked =')
      expect(recordsContent).toContain('const onCellKeyDown =')
    })

    it('records grid wrapper should set cursor: pointer on ag-row', () => {
      expect(recordsContent).toContain('.records-grid-wrapper :deep(.ag-row)')
      expect(recordsContent).toContain('cursor: pointer;')
    })
  })

  // #175: i18n hardcoded text removal & AG-Grid localeText
  describe('#175 - i18n completeness and AG-Grid localeText', () => {
    it('agGridLocale helper should export Korean and English mappings', () => {
      expect(AG_GRID_LOCALE_KO.page).toBe('페이지')
      expect(AG_GRID_LOCALE_KO.loadingOoo).toContain('데이터')
      expect(AG_GRID_LOCALE_EN.page).toBe('Page')
      expect(getAgGridLocaleText('ko')).toEqual(AG_GRID_LOCALE_KO)
      expect(getAgGridLocaleText('en')).toEqual(AG_GRID_LOCALE_EN)
    })

    it('records.vue should bind localeText to ag-grid-vue', () => {
      const recordsContent = fs.readFileSync(
        path.resolve(__dirname, '../../pages/records.vue'),
        'utf-8'
      )
      expect(recordsContent).toContain(':localeText="gridLocaleText"')
      expect(recordsContent).toContain('getAgGridLocaleText')
    })

    it('AiDqRecommendations.vue should use no_new_rules_recommended i18n key', () => {
      const aiDqContent = fs.readFileSync(
        path.resolve(__dirname, '../../components/dq/AiDqRecommendations.vue'),
        'utf-8'
      )
      expect(aiDqContent).toContain("$t('no_new_rules_recommended')")
      expect(aiDqContent).not.toContain('현재 추천할 새로운 규칙이 없습니다.')
    })

    it('match-candidates.vue should use no_match_candidates_found i18n key', () => {
      const matchContent = fs.readFileSync(
        path.resolve(__dirname, '../../pages/match-candidates.vue'),
        'utf-8'
      )
      expect(matchContent).toContain("$t('no_match_candidates_found')")
      expect(matchContent).not.toContain('현재 상태 조건에 해당하는 중복 레코드 검토 후보가 없습니다.')
    })

    it('DqKpiCards.vue should use grade_a_excellent i18n key in getGradeLabel', () => {
      const dqKpiContent = fs.readFileSync(
        path.resolve(__dirname, '../../components/dq/DqKpiCards.vue'),
        'utf-8'
      )
      expect(dqKpiContent).toContain("t('grade_a_excellent'")
    })

    it('ko and en common.json should contain all required keys for Batch 6', () => {
      const koJson = JSON.parse(
        fs.readFileSync(
          path.resolve(__dirname, '../../i18n/locales/ko/common.json'),
          'utf-8'
        )
      )
      const enJson = JSON.parse(
        fs.readFileSync(
          path.resolve(__dirname, '../../i18n/locales/en/common.json'),
          'utf-8'
        )
      )

      const requiredKeys = [
        'reset_filters',
        'row_double_click_hint',
        'no_new_rules_recommended',
        'no_match_candidates_found',
        'grade_a_excellent',
        'grade_b_good',
        'grade_c_normal',
        'grade_d_warning'
      ]

      requiredKeys.forEach((key) => {
        expect(koJson[key], `ko/common.json missing ${key}`).toBeDefined()
        expect(enJson[key], `en/common.json missing ${key}`).toBeDefined()
      })
    })
  })
})
