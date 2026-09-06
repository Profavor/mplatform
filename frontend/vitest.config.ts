import { configDefaults } from 'vitest/config'
import { defineVitestConfig } from '@nuxt/test-utils/config'

export default defineVitestConfig({
  test: {
    environment: 'nuxt',
    environmentMatchGlobs: [
      ['tests/unit/**', 'happy-dom'],
      ['tests/utils/**', 'happy-dom'],
      ['tests/timezone.spec.ts', 'happy-dom'],
    ],
    globals: true,
    pool: 'forks',
    poolOptions: {
      forks: {
        singleFork: true,
      },
    },
    exclude: [...configDefaults.exclude, 'tests/e2e/**'],
  },
})
