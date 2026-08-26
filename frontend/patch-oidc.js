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

// 3. Patch callback.js to use internal cluster token endpoint when tokenUrl is relative
const callbackFile = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/runtime/server/handler/callback.js');
if (fs.existsSync(callbackFile)) {
  let content = fs.readFileSync(callbackFile, 'utf8');
  if (!content.includes('internalTokenUrl')) {
    const targetPattern = /tokenResponse = await customFetch\(config\.tokenUrl,/g;
    const replacement = `const internalTokenUrl = (config.tokenUrl && config.tokenUrl.startsWith('/')) ? (process.env.KEYCLOAK_TOKEN_URI || 'http://keycloak:8080/auth/realms/mplatform/protocol/openid-connect/token') : config.tokenUrl;\n    tokenResponse = await customFetch(internalTokenUrl,`;
    content = content.replace(targetPattern, replacement);
    fs.writeFileSync(callbackFile, content);
  }
  console.log('Patched nuxt-oidc-auth callback.js successfully (internal tokenUrl routing).');
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
