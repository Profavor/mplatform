import fs from 'fs';
import path from 'path';

const file = path.resolve(process.cwd(), 'node_modules/nuxt-oidc-auth/dist/module.mjs');

if (fs.existsSync(file)) {
  let content = fs.readFileSync(file, 'utf8');
  content = content.replace(/sharedReferences\.push/g, 'sharedReferences?.push');
  content = content.replace(/nodeReferences\.push/g, 'nodeReferences?.push');
  fs.writeFileSync(file, content);
  console.log('Patched nuxt-oidc-auth successfully.');
} else {
  console.log('nuxt-oidc-auth module.mjs not found, skipping patch.');
}
