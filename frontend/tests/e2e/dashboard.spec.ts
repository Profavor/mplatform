import { test, expect } from '@playwright/test';

test.describe('Dashboard Page E2E', () => {
  test('should render KPI metrics and charts on dashboard', async ({ page }) => {
    // Mock the session/auth cookie to bypass actual Keycloak login for UI testing
    await page.context().addCookies([
      { name: 'auth_token', value: 'mock-jwt-token', url: 'http://localhost:3000' },
      { name: 'user_data', value: '{"uuid":"test-user-123","role":"SYSTEM_ADMIN"}', url: 'http://localhost:3000' }
    ]);

    // Mock API responses
    await page.route('**/api/dashboard/stats', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          totalDomains: 5,
          pendingApprovals: 3,
          activeRecords: 125,
          pendingMatches: 10,
          openDqViolations: 2
        })
      });
    });

    await page.route('**/api/dashboard/trends', async route => {
      await route.fulfill({ status: 200, json: [] });
    });
    await page.route('**/api/dashboard/domain-distribution', async route => {
      await route.fulfill({ status: 200, json: [] });
    });
    await page.route('**/api/dashboard/dq-trends', async route => {
      await route.fulfill({ status: 200, json: [] });
    });
    await page.route('**/api/dashboard/dq-severity', async route => {
      await route.fulfill({ status: 200, json: [] });
    });
    await page.route('**/api/approval-requests/todos*', async route => {
      await route.fulfill({ status: 200, json: [] });
    });
    await page.route('**/api/domains', async route => {
      await route.fulfill({ status: 200, json: [] });
    });

    await page.goto('/');
    
    // Check Dashboard Header
    await expect(page.locator('h2', { hasText: 'Overview' })).toBeVisible({ timeout: 10000 });
    
    // Check KPI metric rendering
    await expect(page.locator('.kpi-card', { hasText: '125' })).toBeVisible(); // Active Records
    await expect(page.locator('.kpi-card', { hasText: '5' })).toBeVisible();   // Total Domains
  });
});
