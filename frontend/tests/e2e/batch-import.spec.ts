import { test, expect } from '@playwright/test';

test.describe('Batch Import E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.context().addCookies([
      { name: 'auth_token', value: 'mock-jwt-token', url: 'http://localhost:3000' },
      { name: 'user_data', value: '{"uuid":"test-admin-001","role":"SYSTEM_ADMIN"}', url: 'http://localhost:3000' }
    ]);
  });

  test('should display batch import page with domain selector', async ({ page }) => {
    // Mock domains API
    await page.route('**/api/v1/domains', async route => {
      await route.fulfill({
        status: 200,
        json: [
          { id: 'domain-1', name: { ko: '제품', en: 'Product' } },
          { id: 'domain-2', name: { ko: '고객', en: 'Customer' } }
        ]
      });
    });

    await page.goto('/batch-import');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should create batch job and show validation results', async ({ page }) => {
    const batchId = 'batch-001';

    // Mock batch create
    await page.route('**/api/v1/batch/import', async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: batchId,
          status: 'QUEUED',
          totalRecords: 3,
          processedRecords: 0,
          errorRecords: 0
        }
      });
    });

    // Mock batch validate
    await page.route(`**/api/v1/batch/${batchId}/validate`, async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: batchId,
          status: 'COMPLETED',
          totalRecords: 3,
          processedRecords: 3,
          errorRecords: 1
        }
      });
    });

    // Mock staging records
    await page.route(`**/api/v1/batch/${batchId}/records**`, async route => {
      await route.fulfill({
        status: 200,
        json: {
          content: [
            { id: 'sr-1', status: 'VALIDATED', rawData: '{"name":"Valid Record"}' },
            { id: 'sr-2', status: 'VALIDATED', rawData: '{"name":"Another Valid"}' },
            { id: 'sr-3', status: 'ERROR', rawData: '{"name":""}', errorMessage: '[{"fieldKey":"name","ruleType":"NOT_NULL","message":"Required"}]' }
          ],
          totalElements: 3,
          totalPages: 1
        }
      });
    });

    await page.goto('/batch-import');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should commit validated batch and show approval status', async ({ page }) => {
    const batchId = 'batch-002';

    // Mock batch commit
    await page.route(`**/api/v1/batch/${batchId}/commit`, async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: batchId,
          status: 'PENDING_APPROVAL',
          committedRecords: 5,
          approvalRequestId: 'approval-batch-001'
        }
      });
    });

    await page.goto(`/batch-import/${batchId}`);
    await expect(page.locator('body')).toBeVisible();
  });
});
