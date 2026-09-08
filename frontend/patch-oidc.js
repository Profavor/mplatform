import fs from 'fs';
import path from 'path';

// 1. Patch module.mjs
const moduleFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/module.mjs');
if (fs.existsSync(moduleFile)) {
  let content = fs.readFileSync(moduleFile, 'utf8');
  content = content.replace(/sharedReferences\.push/g, 'sharedReferences?.push');
  content = content.replace(/nodeReferences\.push/g, 'nodeReferences?.push');
  fs.writeFileSync(moduleFile, content);
  console.log('Patched nuxt-oidc-auth module.mjs successfully.');
}

// 2. Patch config.js to allow relative baseUrls without forcing https:///
const configFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/runtime/server/utils/config.js');
if (fs.existsSync(configFile)) {
  let content = fs.readFileSync(configFile, 'utf8');
  if (!content.includes("baseUrl.startsWith('/')")) {
    const targetPattern = /export function generateProviderUrl\(baseUrl, relativeUrl\) \{[\s\S]*?return parsedUrl\.protocol \? withoutTrailingSlash\(cleanDoubleSlashes\(joinURL\(baseUrl, "\/", relativeUrl \|\| ""\)\)\) : withoutTrailingSlash\(cleanDoubleSlashes\(withHttps\(joinURL\(baseUrl, "\/", relativeUrl \|\| ""\)\)\)\);[\s\S]*?\}/;
    const replacement = `export function generateProviderUrl(baseUrl, relativeUrl) {
  if (baseUrl && baseUrl.startsWith('/')) {
    return withoutTrailingSlash(cleanDoubleSlashes(joinURL(baseUrl, "/", relativeUrl || "")));
  }
  const parsedUrl = parseURL(baseUrl);
  return parsedUrl.protocol ? withoutTrailingSlash(cleanDoubleSlashes(joinURL(baseUrl, "/", relativeUrl || ""))) : withoutTrailingSlash(cleanDoubleSlashes(withHttps(joinURL(baseUrl, "/", relativeUrl || ""))));
}`;
    content = content.replace(targetPattern, replacement);
    fs.writeFileSync(configFile, content);
  }
  console.log('Patched nuxt-oidc-auth config.js successfully (relative URL support).');
}

// 3. Patch callback.js to use internal cluster token endpoint and forward client host/proto
const callbackFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/runtime/server/handler/callback.js');
if (fs.existsSync(callbackFile)) {
  let content = fs.readFileSync(callbackFile, 'utf8');
  const targetPattern = /let tokenResponse;[\s\S]*?tokenResponse = await customFetch\(.*?, \{/g;
  const replacement = `let tokenResponse;
    try {
      const forwardedHost = event.node?.req?.headers?.['x-forwarded-host'] || event.node?.req?.headers?.host || process.env.KEYCLOAK_PUBLIC_HOST || 'mplatform.local';
      const forwardedProto = event.node?.req?.headers?.['x-forwarded-proto'] || (event.node?.req?.connection?.encrypted ? 'https' : (process.env.KEYCLOAK_PUBLIC_PROTO || 'http'));
      headers['X-Forwarded-Host'] = forwardedHost;
      headers['X-Forwarded-Proto'] = forwardedProto;
      const internalTokenUrl = (config.tokenUrl && config.tokenUrl.startsWith('/')) ? (process.env.KEYCLOAK_TOKEN_URI || 'http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token') : config.tokenUrl;
      tokenResponse = await customFetch(internalTokenUrl, {`;
  if (content.match(targetPattern)) {
    content = content.replace(targetPattern, replacement);
    fs.writeFileSync(callbackFile, content);
    console.log('Patched nuxt-oidc-auth callback.js successfully (internal tokenUrl routing & forwarded headers).');
  }

  if (!content.includes('/api/auth/record-login')) {
    const successPattern = /export default callbackEventHandler\(\{[\s\S]*?async onSuccess\(event, \{ user, callbackRedirectUrl \}\) \{[\s\S]*?await setUserSession\(event, user\);[\s\S]*?return sendRedirect\(event, callbackRedirectUrl \|\| "\/"\);[\s\S]*?\}[\s\S]*?\}\);/;
    const successReplacement = `export default callbackEventHandler({
  async onSuccess(event, { user, callbackRedirectUrl }) {
    await setUserSession(event, user);
    try {
      const backendUrl = process.env.API_BASE_URL || 'http://backend:8080';
      const clientIp = event.node?.req?.headers?.['x-forwarded-for'] || event.node?.req?.headers?.['x-real-ip'] || event.node?.req?.socket?.remoteAddress || '';
      const userAgent = event.node?.req?.headers?.['user-agent'] || '';
      const username = user?.userName || user?.claims?.preferred_username || user?.claims?.sub;
      if (username) {
        fetch(\`\${backendUrl.replace(/\\/$/, '')}/api/auth/record-login\`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Forwarded-For': clientIp,
            'User-Agent': userAgent
          },
          body: JSON.stringify({
            username: username,
            clientIp: clientIp,
            userAgent: userAgent
          })
        }).catch((err) => console.warn('[OIDC Callback] Record login error:', err));
      }
    } catch (e) {}
    return sendRedirect(event, callbackRedirectUrl || "/");
  }
});`;
    if (content.match(successPattern)) {
      content = content.replace(successPattern, successReplacement);
      fs.writeFileSync(callbackFile, content);
      console.log('Patched nuxt-oidc-auth callback.js successfully (login log recording).');
    }
  }
}

// 4. Patch keycloak.js provider to resolve internal openIdConfiguration
const keycloakFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/runtime/providers/keycloak.js');
if (fs.existsSync(keycloakFile)) {
  let content = fs.readFileSync(keycloakFile, 'utf8');
  if (!content.includes('internalServer')) {
    const targetPattern = /async openIdConfiguration\(config\) \{[\s\S]*?const configUrl = generateProviderUrl\(config\.baseUrl, "\.well-known\/openid-configuration"\);/g;
    const replacement = `async openIdConfiguration(config) {
    const internalServer = process.env.KEYCLOAK_SERVER_URL || 'http://keycloak:8080/auth';
    const base = (config.baseUrl && config.baseUrl.startsWith('/')) ? (internalServer.replace(/\\/$/, '') + config.baseUrl.replace(/^\\/auth/, '')) : config.baseUrl;
    const configUrl = generateProviderUrl(base, ".well-known/openid-configuration");`;
    content = content.replace(targetPattern, replacement);
    fs.writeFileSync(keycloakFile, content);
  }
  console.log('Patched nuxt-oidc-auth keycloak.js successfully (internal openIdConfiguration routing).');
}

// 5. Patch oidc.js to use internal cluster token endpoint and forward dynamic issuer host/proto in refreshAccessToken
const oidcFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/runtime/server/utils/oidc.js');
if (fs.existsSync(oidcFile)) {
  let content = fs.readFileSync(oidcFile, 'utf8');
  const targetPattern = /let tokenResponse;[\s\S]*?tokenResponse = await customFetch\(.*?, \{/g;
  const replacement = `let tokenResponse;
  try {
    let forwardedHost = '';
    let forwardedProto = 'http';
    try {
      if (refreshToken && typeof refreshToken === 'string' && refreshToken.includes('.')) {
        const payload = JSON.parse(Buffer.from(refreshToken.split('.')[1], 'base64').toString());
        if (payload && payload.iss) {
          const issUrl = new URL(payload.iss);
          forwardedHost = issUrl.host;
          forwardedProto = issUrl.protocol.replace(':', '');
        }
      }
    } catch (e) {}
    if (!forwardedHost) {
      forwardedHost = process.env.KEYCLOAK_PUBLIC_HOST || 'mplatform.local';
      forwardedProto = process.env.KEYCLOAK_PUBLIC_PROTO || 'http';
    }
    headers['X-Forwarded-Host'] = forwardedHost;
    headers['X-Forwarded-Proto'] = forwardedProto;
    const internalTokenUrl = (config.tokenUrl && config.tokenUrl.startsWith('/')) ? (process.env.KEYCLOAK_TOKEN_URI || 'http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token') : config.tokenUrl;
    tokenResponse = await customFetch(internalTokenUrl, {`;
  if (content.match(targetPattern)) {
    content = content.replace(targetPattern, replacement);
    fs.writeFileSync(oidcFile, content);
    console.log('Patched nuxt-oidc-auth oidc.js successfully (dynamic issuer host forwarding for refreshAccessToken).');
  }
}

// 6. Patch oidcAuth.js composable to prevent aggressive automatic redirect to /login on refresh failure
const oidcAuthFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/runtime/composables/oidcAuth.js');
if (fs.existsSync(oidcAuthFile)) {
  let content = fs.readFileSync(oidcAuthFile, 'utf8');
  if (content.includes('catch(() => login())')) {
    content = content.replace(
      /sessionState\.value = await useRequestFetch\(\)\("\/api\/_auth\/refresh", \{[\s\S]*?\}\)\.catch\(\(\) => login\(\)\);[\s\S]*?if \(!loggedIn\.value\) \{[\s\S]*?await logout\(currentProvider2\);[\s\S]*?\}/,
      `sessionState.value = await useRequestFetch()("/api/_auth/refresh", {
      headers: {
        Accept: "text/json"
      },
      method: "POST"
    }).catch((err) => {
      console.warn('[OIDC Auth] Refresh request failed, deferring to fallback:', err);
      return void 0;
    });`
    );
    fs.writeFileSync(oidcAuthFile, content);
    console.log('Patched nuxt-oidc-auth oidcAuth.js successfully (graceful fallback without forced redirect).');
  }
}

// 7. Patch vuestic-ui useInputFieldAria to prevent $t:inputField aria-label leak (#117)
const vuesticAriaPaths = [
  'node_modules/vuestic-ui/dist/es/src/components/va-input-wrapper/hooks/useInputFieldAria.js',
  'node_modules/vuestic-ui/dist/esm-node/src/components/va-input-wrapper/hooks/useInputFieldAria.mjs',
  'node_modules/vuestic-ui/dist/web-components/src/components/va-input-wrapper/hooks/useInputFieldAria.js'
];
vuesticAriaPaths.forEach((rel) => {
  const fPath = path.resolve(process.cwd(), rel);
  if (fs.existsSync(fPath)) {
    let content = fs.readFileSync(fPath, 'utf8');
    if (content.includes("useTranslationProp('$t:inputField')") || content.includes('useTranslationProp("$t:inputField")')) {
      content = content.replace(/useTranslationProp\(["']\$t:inputField["']\)/g, "useTranslationProp('')");
      fs.writeFileSync(fPath, content);
      console.log(`Patched vuestic-ui ${rel} successfully (prevent $t:inputField leak).`);
    }
  }
});

// 8. Patch vuestic-ui VaSwitch to prevent $t:switch aria-label leak (#205)
const vuesticSwitchPaths = [
  'node_modules/vuestic-ui/dist/es/src/components/va-switch/VaSwitch.vue_vue_type_script_setup_true_lang.js',
  'node_modules/vuestic-ui/dist/esm-node/src/components/va-switch/VaSwitch.vue_vue_type_script_setup_true_lang.mjs',
  'node_modules/vuestic-ui/dist/web-components/src/components/va-switch/VaSwitch.vue_vue_type_script_setup_true_lang.js'
];
vuesticSwitchPaths.forEach((rel) => {
  const fPath = path.resolve(process.cwd(), rel);
  if (fs.existsSync(fPath)) {
    let content = fs.readFileSync(fPath, 'utf8');
    if (content.includes("useTranslationProp('$t:switch')") || content.includes('useTranslationProp("$t:switch")')) {
      content = content.replace(/useTranslationProp\(["']\$t:switch["']\)/g, "useTranslationProp('')");
      fs.writeFileSync(fPath, content);
      console.log(`Patched vuestic-ui ${rel} successfully (prevent $t:switch leak).`);
    }
  }
});

// 9. Patch security.js validateToken to support multi-issuer / reverse proxy / dynamic host validation (#208)
const securityFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/runtime/server/utils/security.js');
if (fs.existsSync(securityFile)) {
  let content = fs.readFileSync(securityFile, 'utf8');
  if (!content.includes('parsedTokenIss')) {
    const targetPattern = /export async function validateToken\(token, options\) \{[\s\S]*?const \{ payload \} = await jwtVerify\(token, jwks, \{[\s\S]*?\}\);[\s\S]*?return payload;[\s\S]*?\}/;
    const replacement = `export async function validateToken(token, options) {
  const jwksUri = (process.env.JWK_SET_URI && !options.jwksUri.startsWith('http://keycloak'))
    ? process.env.JWK_SET_URI
    : options.jwksUri;
  const jwks = createRemoteJWKSet(new URL(jwksUri));
  let parsedTokenIss;
  try {
    const parsed = parseJwtToken(token, false);
    parsedTokenIss = parsed?.iss;
  } catch (e) {}

  let allowedIssuers = [];
  if (options.issuer) {
    allowedIssuers = Array.isArray(options.issuer) ? [...options.issuer] : [options.issuer];
  }
  if (parsedTokenIss && !allowedIssuers.includes(parsedTokenIss)) {
    const realmMatch = parsedTokenIss.includes('/realms/mplatform');
    if (realmMatch || allowedIssuers.length === 0) {
      allowedIssuers.push(parsedTokenIss);
    }
  }

  const { payload } = await jwtVerify(token, jwks, {
    issuer: allowedIssuers.length > 0 ? (allowedIssuers.length === 1 ? allowedIssuers[0] : allowedIssuers) : undefined,
    audience: options.audience
  });
  return payload;
}`;
    content = content.replace(targetPattern, replacement);
    fs.writeFileSync(securityFile, content);
    console.log('Patched nuxt-oidc-auth security.js successfully (dynamic issuer & reverse proxy support).');
  }
}
