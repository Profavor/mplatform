import { describe, it, expect, beforeAll } from 'vitest'
import { generateKeyPair, SignJWT } from 'jose'

describe('OIDC Token Validation with Reverse Proxy & Multi-Issuer Support (TDD)', () => {
  let privateKey: any
  let publicKey: any
  let keyId = 'test-key-id'

  beforeAll(async () => {
    const keyPair = await generateKeyPair('RS256')
    privateKey = keyPair.privateKey
    publicKey = keyPair.publicKey
  })

  // validateToken의 핵심 검증 로직 시뮬레이션 함수
  async function simulateValidateToken(
    token: string,
    options: { issuer?: string | string[]; audience?: string | string[] },
    key: any
  ) {
    const { jwtVerify } = await import('jose')
    
    // JWT 토큰 파싱 (비서명 디코딩)
    const [headerB64, payloadB64] = token.split('.')
    const payload = JSON.parse(Buffer.from(payloadB64, 'base64').toString('utf8'))
    const parsedTokenIss = payload?.iss

    let allowedIssuers: string[] = []
    if (options.issuer) {
      allowedIssuers = Array.isArray(options.issuer) ? [...options.issuer] : [options.issuer]
    }
    if (parsedTokenIss && !allowedIssuers.includes(parsedTokenIss)) {
      const realmMatch = parsedTokenIss.includes('/realms/mplatform')
      if (realmMatch || allowedIssuers.length === 0) {
        allowedIssuers.push(parsedTokenIss)
      }
    }

    const verifyOptions: any = {
      issuer: allowedIssuers.length > 0 ? (allowedIssuers.length === 1 ? allowedIssuers[0] : allowedIssuers) : undefined
    }
    if (options.audience) {
      verifyOptions.audience = options.audience
    }

    const { payload: verifiedPayload } = await jwtVerify(token, key, verifyOptions)
    return verifiedPayload
  }

  it('Keycloak 내부 URL(http://keycloak:8080)과 브라우저 인그레스 URL(https://mplatform.local) 간의 iss 차이가 있어도 동일 렐름이면 서명 및 검증이 정상 성공해야 한다', async () => {
    // 1. 브라우저 인그레스 도메인(mplatform.local)으로 발행된 Keycloak 토큰
    const token = await new SignJWT({ sub: 'admin-user', preferred_username: 'admin' })
      .setProtectedHeader({ alg: 'RS256', kid: keyId })
      .setIssuer('https://mplatform.local/auth/realms/mplatform')
      .setAudience('mdm-frontend')
      .setExpirationTime('1h')
      .setIssuedAt()
      .sign(privateKey)

    // 2. 프론트엔드가 Keycloak OIDC 구성으로부터 취득한 내부 클러스터 issuer
    const internalIssuer = 'http://keycloak:8080/auth/realms/mplatform'

    // 3. 검증 실행
    const verified = await simulateValidateToken(
      token,
      { issuer: internalIssuer, audience: 'mdm-frontend' },
      publicKey
    )

    expect(verified).toBeDefined()
    expect(verified.sub).toBe('admin-user')
    expect(verified.iss).toBe('https://mplatform.local/auth/realms/mplatform')
  })

  it('Cloudflare 터널이나 외부 서브도메인(https://mdm.mplat.store)으로 발행된 토큰도 정상 검증되어야 한다', async () => {
    const token = await new SignJWT({ sub: 'user-123' })
      .setProtectedHeader({ alg: 'RS256', kid: keyId })
      .setIssuer('https://mdm.mplat.store/auth/realms/mplatform')
      .setAudience('mdm-frontend')
      .setExpirationTime('1h')
      .setIssuedAt()
      .sign(privateKey)

    const internalIssuer = 'http://keycloak:8080/auth/realms/mplatform'

    const verified = await simulateValidateToken(
      token,
      { issuer: internalIssuer, audience: 'mdm-frontend' },
      publicKey
    )

    expect(verified).toBeDefined()
    expect(verified.sub).toBe('user-123')
  })

  it('인가되지 않은 다른 렐름(https://evil.com/auth/realms/malicious)의 토큰인 경우 검증이 거부되어야 한다', async () => {
    const evilToken = await new SignJWT({ sub: 'attacker' })
      .setProtectedHeader({ alg: 'RS256', kid: keyId })
      .setIssuer('https://evil.com/auth/realms/malicious')
      .setAudience('mdm-frontend')
      .setExpirationTime('1h')
      .setIssuedAt()
      .sign(privateKey)

    const internalIssuer = 'http://keycloak:8080/auth/realms/mplatform'

    await expect(
      simulateValidateToken(
        evilToken,
        { issuer: internalIssuer, audience: 'mdm-frontend' },
        publicKey
      )
    ).rejects.toThrow(/unexpected "iss" claim value/)
  })

  it('토큰의 디지털 서명이 변조된 경우 JWSSignatureVerificationFailed 에러로 차단되어야 한다', async () => {
    // 다른 개인키로 서명된 토큰
    const anotherKeyPair = await generateKeyPair('RS256')
    const forgedToken = await new SignJWT({ sub: 'imposter' })
      .setProtectedHeader({ alg: 'RS256', kid: keyId })
      .setIssuer('https://mplatform.local/auth/realms/mplatform')
      .setAudience('mdm-frontend')
      .setExpirationTime('1h')
      .setIssuedAt()
      .sign(anotherKeyPair.privateKey)

    const internalIssuer = 'http://keycloak:8080/auth/realms/mplatform'

    await expect(
      simulateValidateToken(
        forgedToken,
        { issuer: internalIssuer, audience: 'mdm-frontend' },
        publicKey
      )
    ).rejects.toThrow(/signature verification failed/)
  })
})
