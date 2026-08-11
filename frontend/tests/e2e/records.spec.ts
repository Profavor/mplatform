import { test, expect } from '@playwright/test';

test.describe('Records Management Page E2E', () => {
  test('should render classification tree and AG-Grid', async ({ page }) => {
    // Mock the session/auth cookie
    await page.context().addCookies([
      { name: 'auth_token', value: 'mock-jwt-token', url: 'http://localhost:3000' },
      { name: 'user_data', value: '{"uuid":"test-user-123","role":"SYSTEM_ADMIN"}', url: 'http://localhost:3000' }
    ]);

    // Mock API responses for domains and tree nodes
    await page.route('**/api/domains', async route => {
      await route.fulfill({ status: 200, json: [{ id: 'domain-1', name: { en: 'Product' } }] });
    });
    await page.route('**/api/nodes/tree', async route => {
      await route.fulfill({
        status: 200,
        json: [{ id: 'node-1', domainId: 'domain-1', label: { en: 'Electronics' }, children: [] }]
      });
    });
    
    // Mock records API
    await page.route('**/api/records/search', async route => {
      await route.fulfill({
        status: 200,
        json: {
          content: [
            { id: 'rec-1', recordCode: 'REC-123', data: { name: 'Smartphone', price: 999 } }
          ],
          totalElements: 1,
          totalPages: 1
        }
      });
    });

    await page.goto('/records');
    
    // Check if Classification Tree is rendered
    await expect(page.locator('h3', { hasText: /Classification Tree/i })).toBeVisible({ timeout: 10000 });
    
    // Check if AG-Grid container is rendered
    await expect(page.locator('.ag-root-wrapper')).toBeVisible();
  });
});
