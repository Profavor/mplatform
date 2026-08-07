const fs = require('fs');
const path = require('path');

const srcDir = 'c:/dev/ai/frontend/i18n/locales';
const destDir = 'c:/dev/ai/mobile/lib/core/l10n';
fs.mkdirSync(destDir, { recursive: true });

function toCamelCase(str) {
  let cleaned = str
    .replace(/[^a-zA-Z0-9_]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_|_$/g, '');
  
  if (!cleaned) cleaned = 'key';
  if (/^[0-9]/.test(cleaned)) cleaned = 'k_' + cleaned;

  const parts = cleaned.split('_').filter(Boolean);
  if (parts.length === 0) return 'key';
  
  return parts[0].toLowerCase() + parts.slice(1).map(p => 
    p.charAt(0).toUpperCase() + p.slice(1)
  ).join('');
}

const dartKeywords = [
  'default', 'new', 'assert', 'null', 'true', 'false', 'return', 'if', 'else', 'case', 
  'continue', 'break', 'switch', 'for', 'while', 'do', 'in', 'is', 'as', 'try', 'catch', 
  'finally', 'throw', 'void', 'var', 'final', 'const', 'late', 'class', 'enum', 'extends', 
  'implements', 'with', 'super', 'this', 'get', 'set', 'dynamic', 'int', 'double', 'String', 'bool'
];

function processLocale(locale) {
  const dir = path.join(srcDir, locale);
  const files = fs.readdirSync(dir).filter(f => f.endsWith('.json'));
  const merged = { "@@locale": locale };

  for (const file of files) {
    const content = JSON.parse(fs.readFileSync(path.join(dir, file), 'utf8'));
    for (let [rawKey, val] of Object.entries(content)) {
      if (typeof val !== 'string') continue;
      
      val = val.replace(/\{['"]?@['"]?\}/g, '@');

      let camelKey = toCamelCase(rawKey);
      if (dartKeywords.includes(camelKey)) {
        camelKey = camelKey + 'Key';
      }

      merged[camelKey] = val;

      const matches = val.match(/\{([a-zA-Z0-9_]+)\}/g);
      if (matches) {
        const placeholders = {};
        for (const m of matches) {
          const p = m.slice(1, -1);
          if (!/^[a-zA-Z]/.test(p)) continue;
          placeholders[p] = { type: 'Object' };
        }
        if (Object.keys(placeholders).length > 0) {
          merged['@' + camelKey] = {
            description: `Translated from ${rawKey}`,
            placeholders
          };
        }
      }
    }
  }

  const outPath = path.join(destDir, `app_${locale}.arb`);
  fs.writeFileSync(outPath, JSON.stringify(merged, null, 2), 'utf8');
  console.log(`Generated ${outPath} (${Object.keys(merged).length} entries)`);
}

processLocale('en');
processLocale('ko');
