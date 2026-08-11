import { configDefaults } from 'vitest/config'
import { defineVitestConfig } from '@nuxt/test-utils/config'

export default defineVitestConfig({
  test: {
    environment: 'nuxt',
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
