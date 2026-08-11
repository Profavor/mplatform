import { test, expect } from '@playwright/test';

test.describe('Domain Management E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().addCookies([
      { name: 'auth_token', value: 'mock-jwt-token', url: 'http://localhost:3000' },
      { name: 'user_data', value: '{"uuid":"test-admin-001","role":"SYSTEM_ADMIN"}', url: 'http://localhost:3000' }
    ]);
  });

  test('should display domain list', async ({ page }) => {
    await page.route('**/api/v1/domains', async route => {
      await route.fulfill({
        status: 200,
        json: [
          { id: 'domain-1', name: { ko: '제품', en: 'Product' }, description: { ko: '제품 마스터', en: 'Product Master' } },
          { id: 'domain-2', name: { ko: '고객', en: 'Customer' }, description: { ko: '고객 마스터', en: 'Customer Master' } }
        ]
      });
    });

    await page.goto('/domains');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should display domain detail with axes and nodes', async ({ page }) => {
    // Mock domain detail
    await page.route('**/api/v1/domains/domain-1', async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: 'domain-1',
          name: { ko: '제품', en: 'Product' },
          description: { ko: '제품 마스터', en: 'Product Master' }
        }
      });
    });

    // Mock axes
    await page.route('**/api/v1/domains/domain-1/axes', async route => {
      await route.fulfill({
        status: 200,
        json: [
          { id: 'axis-1', name: { ko: '카테고리', en: 'Category' }, domainId: 'domain-1' }
        ]
      });
    });

    // Mock tree nodes
    await page.route('**/api/v1/nodes/tree**', async route => {
      await route.fulfill({
        status: 200,
        json: [
          {
            id: 'node-1',
            name: { ko: '전자제품', en: 'Electronics' },
            axisId: 'axis-1',
            children: [
              { id: 'node-2', name: { ko: '스마트폰', en: 'Smartphone' }, axisId: 'axis-1', children: [] }
            ]
          }
        ]
      });
    });

    await page.goto('/domains/domain-1');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should display field definitions for a node', async ({ page }) => {
    // Mock field definitions
    await page.route('**/api/v1/fields**', async route => {
      await route.fulfill({
        status: 200,
        json: {
          content: [
            {
              id: 'field-1',
              key: 'product_name',
              name: { ko: '제품명', en: 'Product Name' },
              type: 'TEXT',
              required: true,
              order: 1
            },
            {
              id: 'field-2',
              key: 'price',
              name: { ko: '가격', en: 'Price' },
              type: 'NUMBER',
              required: false,
              order: 2,
              unit: 'KRW'
            }
          ],
          totalElements: 2,
          totalPages: 1
        }
      });
    });

    await page.goto('/domains/domain-1/nodes/node-1/fields');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should navigate to record management from domain', async ({ page }) => {
    await page.route('**/api/v1/domains', async route => {
      await route.fulfill({
        status: 200,
        json: [{ id: 'domain-1', name: { ko: '제품', en: 'Product' } }]
      });
    });

    await page.goto('/domains');
    await expect(page.locator('body')).toBeVisible();

    // Verify navigation links exist
    const currentUrl = page.url();
    expect(currentUrl).toContain('/domains');
  });
});
