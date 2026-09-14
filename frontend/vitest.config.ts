import { configDefaults } from 'vitest/config'
import { defineVitestConfig } from '@nuxt/test-utils/config'

export default defineVitestConfig({
  test: {
    environment: 'nuxt',
    hookTimeout: 60000,
    testTimeout: 30000,
    globals: true,
    pool: 'forks',
    forks: {
      singleFork: true,
    },
    exclude: [...configDefaults.exclude, 'tests/e2e/**'],
  } as any,
})
