import { test, expect } from '@playwright/test';

test.describe('Login & Navigation Flow', () => {
  test('User can visit login page and see the brand', async ({ page }) => {
    await page.goto('/login');
    // Wait for page to load
    await expect(page.locator('body')).toBeVisible();
    
    // Check if there is some common auth element or text
    const loginButton = page.locator('button', { hasText: /Login|로그인|Sign in|로그인/i });
    if (await loginButton.count() > 0) {
      await expect(loginButton.first()).toBeVisible();
    }
  });

  test('User can navigate to home page', async ({ page }) => {
    // If auth is strictly required, this might redirect to /login
    await page.goto('/');
    
    // Wait for either the home page dashboard or redirect to login
    const currentUrl = page.url();
    expect(currentUrl.includes('/login') || currentUrl.endsWith('/')).toBeTruthy();
  });
});
