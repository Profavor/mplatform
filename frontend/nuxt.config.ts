import { defineNuxtConfig } from 'nuxt/config'
import dns from 'dns'

// Force IPv4 for localhost to fix Node 18+ Docker connectivity issues
dns.setDefaultResultOrder('ipv4first')

// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2026-08-11',
  experimental: {
    scanPageMeta: true
  },
  devtools: { enabled: false },
  unhead: { legacy: true },
  modules: ['@pinia/nuxt', '@vuestic/nuxt', 'nuxt-oidc-auth', '@nuxtjs/i18n'],
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
          closeButton: false,
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
  oidc: {
    defaultProvider: 'keycloak',
    providers: {
      keycloak: {
        baseUrl: process.env.OAUTH2_ISSUER_URI || 'http://localhost:8081/realms/mplatform',
        clientId: 'mdm-frontend',
        exposeAccessToken: true,
        scope: ['openid', 'profile', 'email'],
        clientSecret: 'secret',
        authenticationScheme: 'body',
        pkce: false,
        nonce: false,
        redirectUri: process.env.KEYCLOAK_REDIRECT_URI || 'http://localhost:3000/auth/keycloak/callback',
        logoutRedirectUri: process.env.KEYCLOAK_LOGOUT_REDIRECT_URI || 'http://localhost:3000/login'
      }
    },
    cookie: {
      secure: false
    },
    session: {
      expirationCheck: true,
      automaticRefresh: true,
      cookie: {
        secure: false
      }
    },

    middleware: {
      globalMiddlewareEnabled: false,
      customLoginPage: true,
      customLogoutPage: true
    }
  },
  build: {
    transpile: []
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
    const targetUrl = rawUrl.replace(/\/$/, '')
    return {
      '/ws-stomp/**': { proxy: `${targetUrl}/ws-stomp/**` }
    }
  })(),
  vite: {
    server: {
      allowedHosts: true,
      proxy: {
        '/ws-stomp': {
          target: process.env.API_BASE_URL || process.env.NUXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
          ws: true,
          changeOrigin: true
        }
      }
    }
  }
})