import { describe, it, expect } from 'vitest'

describe('OIDC refreshAccessToken Patch Logic (TDD)', () => {
  it('oidc.js 소스 코드에서 refreshAccessToken 내 config.tokenUrl을 internalTokenUrl로 치환해야 함', () => {
    const originalOidcContent = `
export async function refreshAccessToken(refreshToken, config) {
  const logger = useOidcLogger();
  const customFetch = await createProviderFetch(config);
  const headers = {};
  const requestBody = {
    client_id: config.clientId,
    refresh_token: refreshToken,
    grant_type: "refresh_token"
  };
  let tokenResponse;
  try {
    tokenResponse = await customFetch(config.tokenUrl, {
      method: "POST",
      headers,
      body: convertTokenRequestToType(requestBody, config.tokenRequestType)
    });
  } catch (error) {
    const fetchError = error;
    throw new Error(String(error));
  }
}
`

    const targetPattern = /tokenResponse = await customFetch\(config\.tokenUrl,/g
    const replacement = `const internalTokenUrl = (config.tokenUrl && config.tokenUrl.startsWith('/')) ? (process.env.KEYCLOAK_TOKEN_URI || 'http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token') : config.tokenUrl;\n    tokenResponse = await customFetch(internalTokenUrl,`

    const patchedContent = originalOidcContent.replace(targetPattern, replacement)

    expect(patchedContent).toContain('internalTokenUrl')
    expect(patchedContent).toContain('http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token')
    expect(patchedContent).toContain('tokenResponse = await customFetch(internalTokenUrl,')
  })
})
