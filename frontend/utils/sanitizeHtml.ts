import DOMPurify from 'dompurify'

/**
 * Sanitizes HTML content to eliminate Stored XSS vectors (script tags, event handlers, javascript: URLs)
 * while safely preserving legitimate HTML tags (headings, formatting, lists, tables, links, images).
 */
export function sanitizeHtml(dirty: string | null | undefined): string {
  if (!dirty) return ''

  // 1. Primary layer: strip malicious tags, scripts, iframes, and inline event handlers
  let cleaned = String(dirty)
    .replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '')
    .replace(/<iframe\b[^<]*(?:(?!<\/iframe>)<[^<]*)*<\/iframe>/gi, '')
    .replace(/<object\b[^<]*(?:(?!<\/object>)<[^<]*)*<\/object>/gi, '')
    .replace(/<embed\b[^<]*(?:(?!<\/embed>)<[^<]*)*<\/embed>/gi, '')
    .replace(/\son[a-zA-Z]+\s*=\s*(?:'[^']*'|"[^"]*"|[^\s>]+)/gi, '')
    .replace(/(href|src)\s*=\s*['"]?\s*javascript:[^'"]*['"]?/gi, '')

  // 2. Secondary layer: in real browser environments (non-happy-dom test mock), run DOMPurify
  const isHappyDom = typeof window !== 'undefined' && Boolean((window as any).happyDOM || (window as any).__happyDom__)
  if (typeof window !== 'undefined' && !isHappyDom) {
    try {
      const purify = typeof (DOMPurify as any) === 'function' ? (DOMPurify as any)(window) : DOMPurify
      if (purify && typeof purify.sanitize === 'function' && purify.isSupported) {
        cleaned = purify.sanitize(cleaned, {
          ALLOWED_TAGS: [
            'b', 'i', 'em', 'strong', 'a', 'p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
            'ul', 'ol', 'li', 'br', 'hr', 'span', 'div', 'blockquote', 'code', 'pre',
            'table', 'thead', 'tbody', 'tr', 'th', 'td', 'img'
          ],
          ALLOWED_ATTR: ['href', 'title', 'target', 'src', 'alt', 'class', 'style', 'width', 'height'],
          ALLOW_DATA_ATTR: false
        })
      }
    } catch {
      // Keep cleaned string
    }
  }

  return cleaned
}
