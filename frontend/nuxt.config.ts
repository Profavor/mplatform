// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2024-11-01',
  devtools: { enabled: false },
  unhead: { legacy: true },
  modules: ['@vuestic/nuxt', 'nuxt-auth-utils', '@nuxtjs/i18n'],
  css: ['~/assets/main.css'],
  vuestic: {
    css: ['typography', 'grid', 'reset'],
    config: {
      colors: {
        presets: {
          light: {
            divider: '#e0e0e0'
          },
          dark: {
            divider: '#333333'
          }
        },
        variables: {
          divider: '#e0e0e0'
        }
      },
      components: {
        VaModal: {
          closeButton: true,
          noOutsideDismiss: true,
          preventClickOutside: true,
          blurToCancel: false
        }
      },

    }
  },
  i18n: {
    locales: [
      {
        code: 'ko',
        files: [
          'ko/admin.json',
          'ko/approval.json',
          'ko/auth.json',
          'ko/common.json',
          'ko/dq.json',
          'ko/records.json',
          'ko/schema.json'
        ]
      },
      {
        code: 'en',
        files: [
          'en/admin.json',
          'en/approval.json',
          'en/auth.json',
          'en/common.json',
          'en/dq.json',
          'en/records.json',
          'en/schema.json'
        ]
      }
    ],
    defaultLocale: 'ko',
    strategy: 'no_prefix',
    lazy: true,
    langDir: 'locales',
    bundle: {
      optimizeTranslationDirective: false
    }
  },
  build: {
  },
  app: {
    head: {
      link: [
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Source+Sans+Pro:ital,wght@0,400;1,700&display=swap' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/icon?family=Material+Icons' }
      ]
    }
  },
  runtimeConfig: {
    public: {
      apiBaseUrl: process.env.API_BASE_URL || process.env.NUXT_PUBLIC_API_BASE_URL || process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080',
      agGridLicense: process.env.AG_GRID_LICENSE,
      accessTokenExpirationSec: Number(process.env.JWT_ACCESS_EXPIRATION_SEC || 1800),
      refreshTokenExpirationSec: Number(process.env.JWT_REFRESH_EXPIRATION_SEC || 172800)
    }
  },
  routeRules: (() => {
    let rawUrl = process.env.API_BASE_URL || process.env.NUXT_PUBLIC_API_BASE || process.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'
    if (rawUrl && !rawUrl.startsWith('http://') && !rawUrl.startsWith('https://')) {
      rawUrl = `https://${rawUrl}`
    }
    const targetUrl = rawUrl
    const wsUrl = targetUrl.replace(/^http/, 'ws')
    return {
      '/api/**': { proxy: `${targetUrl.replace(/\/$/, '')}/api/**` },
      '/ws-stomp/**': { proxy: `${wsUrl.replace(/\/$/, '')}/ws-stomp/**` }
    }
  })(),
  vite: {
    server: {
      allowedHosts: true
    }
  }
})