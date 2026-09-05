import { describe, it, expect } from 'vitest'
import { sanitizeHtml } from '~/utils/sanitizeHtml'
import {
  safeEvaluateCondition,
  safeEvaluateFormula,
  validateFormulaSyntax
} from '~/utils/safeEvaluator'

describe('Security Batch Round 8 - Frontend Security Tests', () => {
  describe('#183 Stored XSS Prevention: sanitizeHtml', () => {
    it('removes script tags and inline execution vectors', () => {
      const maliciousHtml = '<p>Safe Text</p><script>alert("XSS")</script>'
      const sanitized = sanitizeHtml(maliciousHtml)
      expect(sanitized).not.toContain('<script>')
      expect(sanitized).not.toContain('alert("XSS")')
      expect(sanitized).toContain('<p>Safe Text</p>')
    })

    it('strips onerror and onclick event handlers from elements', () => {
      const maliciousImg = '<img src="invalid.jpg" onerror="alert(document.cookie)" alt="Test" />'
      const sanitized = sanitizeHtml(maliciousImg)
      expect(sanitized).not.toContain('onerror')
      expect(sanitized).not.toContain('alert')
      expect(sanitized).toContain('<img')
      expect(sanitized).toContain('alt="Test"')
    })

    it('strips javascript: pseudo-protocol URIs in links and iframes', () => {
      const maliciousLink = '<a href="javascript:alert(1)">Click Me</a>'
      const sanitized = sanitizeHtml(maliciousLink)
      expect(sanitized).not.toContain('javascript:')
    })

    it('preserves legitimate formatting and layout elements', () => {
      const validHtml = '<h1>Title</h1><p>Description with <b>bold</b> and <i>italic</i></p><ul><li>List Item</li></ul>'
      const sanitized = sanitizeHtml(validHtml)
      expect(sanitized).toContain('<h1>Title</h1>')
      expect(sanitized).toContain('<b>bold</b>')
      expect(sanitized).toContain('<li>List Item</li>')
    })
  })

  describe('#183 Safe Evaluator: safeEvaluateCondition', () => {
    it('evaluates equality and boolean logic correctly', () => {
      const formData = { status: 'APPROVED', priority: 'HIGH', count: 5 }

      expect(safeEvaluateCondition("#{status} === 'APPROVED'", formData)).toBe(true)
      expect(safeEvaluateCondition("#{status} === 'REJECTED'", formData)).toBe(false)
      expect(safeEvaluateCondition("#{status} === 'APPROVED' && #{count} >= 5", formData)).toBe(true)
      expect(safeEvaluateCondition("#{status} === 'APPROVED' && #{count} > 10", formData)).toBe(false)
      expect(safeEvaluateCondition("#{priority} === 'HIGH' || #{count} === 0", formData)).toBe(true)
    })

    it('handles numeric comparisons properly', () => {
      const formData = { amount: 1500, threshold: 1000 }
      expect(safeEvaluateCondition('#{amount} > #{threshold}', formData)).toBe(true)
      expect(safeEvaluateCondition('#{amount} < 500', formData)).toBe(false)
      expect(safeEvaluateCondition('#{amount} <= 1500', formData)).toBe(true)
    })

    it('blocks arbitrary JavaScript and global property execution safely', () => {
      const formData = { val: 'test' }
      // Attempts to access globals or call functions should return false and not execute
      expect(safeEvaluateCondition("window.alert('XSS')", formData)).toBe(false)
      expect(safeEvaluateCondition('document.cookie', formData)).toBe(false)
      expect(safeEvaluateCondition('(function(){ return true; })()', formData)).toBe(false)
      expect(safeEvaluateCondition('constructor.constructor("alert(1)")()', formData)).toBe(false)
    })
  })

  describe('#183 Safe Evaluator: safeEvaluateFormula', () => {
    it('evaluates basic arithmetic correctly', () => {
      const data = { price: 100, qty: 5, discount: 0.1 }
      const res = safeEvaluateFormula('${price} * ${qty} * (1 - ${discount})', data)
      expect(res).toBeCloseTo(450)
    })

    it('supports ROUND, ABS, CEIL, FLOOR math functions', () => {
      const data = { subtotal: 100.556, diff: -25.7 }
      expect(safeEvaluateFormula('ROUND(${subtotal}, 2)', data)).toBe(100.56)
      expect(safeEvaluateFormula('ABS(${diff})', data)).toBe(25.7)
      expect(safeEvaluateFormula('CEIL(${subtotal})', data)).toBe(101)
      expect(safeEvaluateFormula('FLOOR(${subtotal})', data)).toBe(100)
    })

    it('safely rejects malicious formula injection without code execution', () => {
      const data = { a: 10 }
      expect(safeEvaluateFormula('${a} + (function(){ return 999; })()', data)).toBeNull()
      expect(safeEvaluateFormula('alert(1)', data)).toBeNull()
    })
  })

  describe('#183 Formula Syntax Validation: validateFormulaSyntax', () => {
    it('accepts valid formulas', () => {
      expect(() => validateFormulaSyntax('${a} * ${b} + 10')).not.toThrow()
      expect(() => validateFormulaSyntax('ROUND(${total} * 1.1, 2)')).not.toThrow()
    })

    it('rejects invalid or dangerous formulas', () => {
      expect(() => validateFormulaSyntax('')).toThrow()
      expect(() => validateFormulaSyntax('${a} + window.location')).toThrow()
      expect(() => validateFormulaSyntax('${a} + eval("1")')).toThrow()
    })
  })
})
