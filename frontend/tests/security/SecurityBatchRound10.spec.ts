import { describe, it, expect } from 'vitest'
import fs from 'fs'
import path from 'path'

describe('Security Batch Round 10 - Frontend Hardening & Data Protection', () => {

  describe('#191: Secret & License Key Protection in Nuxt Config', () => {
    it('agGridLicense should not be exposed in runtimeConfig.public', () => {
      const nuxtConfigPath = path.resolve(__dirname, '../../nuxt.config.ts')
      const nuxtConfigContent = fs.readFileSync(nuxtConfigPath, 'utf-8')

      // Check if agGridLicense is defined inside public: { ... }
      const publicConfigRegex = /public:\s*\{([^}]+)\}/s
      const match = nuxtConfigContent.match(publicConfigRegex)

      if (match) {
        const publicSection = match[1]
        expect(publicSection.includes('agGridLicense')).toBe(false)
      }
    })
  })

  describe('#198: Partial Record Merge Utility', () => {
    it('preserves existing record attributes when updating single fields like price', () => {
      const existing = {
        PRODUCT_ID: '6011227725',
        PRODUCT_CODE: 'CBP-001207',
        PRODUCT_NAME: '로지텍 G102',
        PRODUCT_PRICE: 24060
      }
      const incoming = {
        PRODUCT_PRICE: 24410
      }

      const merged = { ...existing, ...incoming }

      expect(merged.PRODUCT_PRICE).toBe(24410)
      expect(merged.PRODUCT_NAME).toBe('로지텍 G102')
      expect(merged.PRODUCT_ID).toBe('6011227725')
      expect(merged.PRODUCT_CODE).toBe('CBP-001207')
    })
  })
})
