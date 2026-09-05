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
