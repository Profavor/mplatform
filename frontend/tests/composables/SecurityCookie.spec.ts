import { describe, it, expect } from 'vitest'
import { readFileSync } from 'fs'
import { resolve } from 'path'

describe('Security #97, #98, #99 Regression Specs', () => {
  const rootDir = resolve(__dirname, '../../')

  it('Issue #97: k8s/40-ingress.yaml must enable ssl-redirect, force-ssl-redirect, and HSTS headers', () => {
    const ingressContent = readFileSync(resolve(rootDir, '../k8s/40-ingress.yaml'), 'utf-8')

    expect(ingressContent).toContain('nginx.ingress.kubernetes.io/ssl-redirect: "true"')
    expect(ingressContent).toContain('nginx.ingress.kubernetes.io/force-ssl-redirect: "true"')
    expect(ingressContent).toContain('nginx.ingress.kubernetes.io/hsts: "true"')
    expect(ingressContent).toContain('nginx.ingress.kubernetes.io/hsts-include-subdomains: "true"')
    expect(ingressContent).toContain('nginx.ingress.kubernetes.io/hsts-max-age: "31536000"')
  })

  it('Issue #97: nuxt.config.ts must configure secure session cookie for production/HTTPS', () => {
    const nuxtConfigContent = readFileSync(resolve(rootDir, 'nuxt.config.ts'), 'utf-8')

    expect(nuxtConfigContent).toMatch(/secure:\s*process\.env\.NODE_ENV === 'production'/)
    expect(nuxtConfigContent).not.toMatch(/cookie:\s*\{\s*secure:\s*false\s*\}/)
  })

  it('Issue #97, #99: useAuthRefresh.ts must include Secure flag when protocol is HTTPS or in production', () => {
    const authRefreshContent = readFileSync(resolve(rootDir, 'composables/useAuthRefresh.ts'), 'utf-8')

    expect(authRefreshContent).toContain('Secure')
    expect(authRefreshContent).toMatch(/secureFlag/)
    expect(authRefreshContent).toContain('sameSite: \'lax\'')
  })

  it('Issue #98: k8s/01-config.yaml must include cartbom origins in CORS_ALLOWED_ORIGINS', () => {
    const configContent = readFileSync(resolve(rootDir, '../k8s/01-config.yaml'), 'utf-8')

    expect(configContent).toContain('https://cartbom.com')
    expect(configContent).toContain('https://*.cartbom.com')
  })
})
