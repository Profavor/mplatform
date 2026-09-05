import { describe, it, expect } from 'vitest'
import fs from 'fs'
import path from 'path'

describe('Batch Round 5 Issue Fixes', () => {
  // #166: nuxt.config.ts should have title, titleTemplate, and meta tags
  describe('#166 - Page title & meta tags', () => {
    const configContent = fs.readFileSync(
      path.resolve(__dirname, '../../nuxt.config.ts'),
      'utf-8'
    )

    it('should have a default title in app.head', () => {
      expect(configContent).toContain("title: 'Domain Governance System'")
    })

    it('should have a titleTemplate in app.head', () => {
      expect(configContent).toContain("titleTemplate: '%s | Domain Governance System'")
    })

    it('should have meta description tag', () => {
      expect(configContent).toContain("name: 'description'")
    })

    it('should have og:type meta tag', () => {
      expect(configContent).toContain("property: 'og:type'")
    })
  })

  // #167: isDark should use savedTheme cookie as SSOT
  describe('#167 - Theme toggle SSOT', () => {
    const layoutContent = fs.readFileSync(
      path.resolve(__dirname, '../../layouts/default.vue'),
      'utf-8'
    )

    it('isDark should reference savedTheme.value instead of currentPresetName', () => {
      expect(layoutContent).toContain("savedTheme.value === 'dark'")
      expect(layoutContent).not.toContain("currentPresetName?.value === 'dark'")
    })

    it('toggleTheme should set savedTheme before applyPreset', () => {
      const savedThemeIndex = layoutContent.indexOf('savedTheme.value = newTheme')
      const applyPresetIndex = layoutContent.indexOf(
        'applyPreset(newTheme)',
        layoutContent.indexOf('const toggleTheme')
      )
      expect(savedThemeIndex).toBeGreaterThan(-1)
      expect(applyPresetIndex).toBeGreaterThan(-1)
      expect(savedThemeIndex).toBeLessThan(applyPresetIndex)
    })
  })

  // #168: RecordDetailDrawer should have v-if conditions
  describe('#168 - RecordDetailDrawer v-if cleanup', () => {
    const recordsContent = fs.readFileSync(
      path.resolve(__dirname, '../../pages/records.vue'),
      'utf-8'
    )

    it('detail drawer should have v-if="showDetailModal"', () => {
      // The detail drawer should have v-if, not v-model:show
      const detailDrawerSection = recordsContent.substring(
        recordsContent.indexOf('<!-- Modularized Record Detail'),
        recordsContent.indexOf('<!-- Dedicated Snapshot Modal')
      )
      expect(detailDrawerSection).toContain('v-if="showDetailModal"')
      expect(detailDrawerSection).not.toContain('v-model:show="showDetailModal"')
    })

    it('snapshot drawer should have v-if="showSnapshotModal"', () => {
      const snapshotSection = recordsContent.substring(
        recordsContent.indexOf('<!-- Dedicated Snapshot Modal'),
        recordsContent.indexOf('<!-- Dedicated Record Compare Modal')
      )
      expect(snapshotSection).toContain('v-if="showSnapshotModal"')
      expect(snapshotSection).not.toContain('v-model:show="showSnapshotModal"')
    })
  })

  // #170: Header icon buttons should have aria-label
  describe('#170 - Header button aria-label', () => {
    const layoutContent = fs.readFileSync(
      path.resolve(__dirname, '../../layouts/default.vue'),
      'utf-8'
    )

    it('radio DJ button should have aria-label via $t()', () => {
      expect(layoutContent).toContain(":aria-label=\"$t('radio_dj_panel')\"")
    })

    it('theme toggle button should have dynamic aria-label', () => {
      expect(layoutContent).toContain("$t('switch_to_light_mode')")
      expect(layoutContent).toContain("$t('switch_to_dark_mode')")
    })

    it('theme toggle button should have aria-pressed attribute', () => {
      expect(layoutContent).toContain(':aria-pressed="isDark"')
    })
  })

  // #177: Footer brand spacing & i18n typo fix
  describe('#177 - Footer brand spacing & section typo', () => {
    it('brand-text should have flex layout CSS', () => {
      const footerContent = fs.readFileSync(
        path.resolve(__dirname, '../../components/layout/AppFooter.vue'),
        'utf-8'
      )
      expect(footerContent).toContain('.brand-text')
      expect(footerContent).toContain('display: flex')
      expect(footerContent).toContain('brand-separator')
    })

    it('ko common.json should have 섹션 not 섹터', () => {
      const koCommon = fs.readFileSync(
        path.resolve(__dirname, '../../i18n/locales/ko/common.json'),
        'utf-8'
      )
      expect(koCommon).toContain('섹션/그룹')
      expect(koCommon).not.toContain('섹터/그룹')
    })

    it('en common.json should have Sections not Sectors', () => {
      const enCommon = fs.readFileSync(
        path.resolve(__dirname, '../../i18n/locales/en/common.json'),
        'utf-8'
      )
      expect(enCommon).toContain('Sections/Groups')
      expect(enCommon).not.toContain('Sectors/Groups')
    })
  })

  // #170 i18n keys exist
  describe('#170 - aria-label i18n keys', () => {
    it('ko common.json should have radio_dj_panel key', () => {
      const koCommon = JSON.parse(
        fs.readFileSync(
          path.resolve(__dirname, '../../i18n/locales/ko/common.json'),
          'utf-8'
        )
      )
      expect(koCommon.radio_dj_panel).toBeDefined()
      expect(koCommon.switch_to_light_mode).toBeDefined()
      expect(koCommon.switch_to_dark_mode).toBeDefined()
    })

    it('en common.json should have radio_dj_panel key', () => {
      const enCommon = JSON.parse(
        fs.readFileSync(
          path.resolve(__dirname, '../../i18n/locales/en/common.json'),
          'utf-8'
        )
      )
      expect(enCommon.radio_dj_panel).toBeDefined()
      expect(enCommon.switch_to_light_mode).toBeDefined()
      expect(enCommon.switch_to_dark_mode).toBeDefined()
    })
  })
})
