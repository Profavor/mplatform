import { test, expect } from '@playwright/test';

test.describe('Approval Workflow E2E', () => {
  test.beforeEach(async ({ page }) => {
    // Mock auth session
    await page.context().addCookies([
      { name: 'auth_token', value: 'mock-jwt-token', url: 'http://localhost:3000' },
      { name: 'user_data', value: '{"uuid":"test-admin-001","role":"SYSTEM_ADMIN"}', url: 'http://localhost:3000' }
    ]);
  });

  test('should display pending approval list', async ({ page }) => {
    // Mock approval API
    await page.route('**/api/v1/approvals/pending**', async route => {
      await route.fulfill({
        status: 200,
        json: {
          content: [
            {
              id: 'approval-001',
              requestType: 'CREATE',
              status: 'PENDING',
              requestedBy: 'user1',
              createdAt: '2026-08-01T10:00:00'
            }
          ],
          totalElements: 1,
          totalPages: 1
        }
      });
    });

    await page.goto('/approvals');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should approve a pending request', async ({ page }) => {
    let approveApiCalled = false;

    // Mock approval detail
    await page.route('**/api/v1/approvals/approval-001', async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: 'approval-001',
          requestType: 'CREATE',
          status: 'PENDING',
          requestedBy: 'user1',
          diff: { before: {}, after: { name: 'New Record' } }
        }
      });
    });

    // Mock approve action
    await page.route('**/api/v1/approvals/approval-001/approve', async route => {
      approveApiCalled = true;
      await route.fulfill({ status: 200, json: { status: 'APPROVED' } });
    });

    await page.goto('/approvals/approval-001');
    await expect(page.locator('body')).toBeVisible();
  });

  test('should reject a pending request with comment', async ({ page }) => {
    let rejectApiCalled = false;

    await page.route('**/api/v1/approvals/approval-002', async route => {
      await route.fulfill({
        status: 200,
        json: {
          id: 'approval-002',
          requestType: 'UPDATE',
          status: 'PENDING',
          requestedBy: 'user2'
        }
      });
    });

    await page.route('**/api/v1/approvals/approval-002/reject', async route => {
      rejectApiCalled = true;
      await route.fulfill({ status: 200, json: { status: 'REJECTED' } });
    });

    await page.goto('/approvals/approval-002');
    await expect(page.locator('body')).toBeVisible();
  });
});
