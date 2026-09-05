/**
 * Safe Mathematical and Logical Expression Evaluator.
 * Fully replaces `eval` and `new Function` to prevent arbitrary JavaScript execution / XSS / RCE.
 */

type TokenType =
  | 'NUMBER'
  | 'STRING'
  | 'BOOLEAN'
  | 'NULL'
  | 'UNDEFINED'
  | 'IDENTIFIER'
  | 'OPERATOR'
  | 'PAREN_OPEN'
  | 'PAREN_CLOSE'
  | 'COMMA'
  | 'EOF'

interface Token {
  type: TokenType
  value: any
}

const ALLOWED_MATH_FUNCS = new Set(['ROUND', 'ABS', 'CEIL', 'FLOOR'])

export function tokenize(input: string): Token[] {
  const tokens: Token[] = []
  let i = 0
  const len = input.length

  while (i < len) {
    const ch = input[i]

    // Skip whitespace
    if (/\s/.test(ch)) {
      i++
      continue
    }

    // Numbers (integer or float)
    if (/[0-9]/.test(ch)) {
      let numStr = ''
      while (i < len && (/[0-9]/.test(input[i]) || input[i] === '.')) {
        numStr += input[i]
        i++
      }
      tokens.push({ type: 'NUMBER', value: parseFloat(numStr) })
      continue
    }

    // Strings (single or double quotes)
    if (ch === '"' || ch === "'") {
      const quote = ch
      i++
      let strVal = ''
      while (i < len && input[i] !== quote) {
        if (input[i] === '\\' && i + 1 < len) {
          i++
          strVal += input[i]
        } else {
          strVal += input[i]
        }
        i++
      }
      if (i < len && input[i] === quote) {
        i++
      }
      tokens.push({ type: 'STRING', value: strVal })
      continue
    }

    // Parentheses and punctuation
    if (ch === '(') {
      tokens.push({ type: 'PAREN_OPEN', value: '(' })
      i++
      continue
    }
    if (ch === ')') {
      tokens.push({ type: 'PAREN_CLOSE', value: ')' })
      i++
      continue
    }
    if (ch === ',') {
      tokens.push({ type: 'COMMA', value: ',' })
      i++
      continue
    }

    // Multi-character operators: ===, !==, ==, !=, <=, >=, &&, ||
    if (input.startsWith('===', i)) {
      tokens.push({ type: 'OPERATOR', value: '===' })
      i += 3
      continue
    }
    if (input.startsWith('!==', i)) {
      tokens.push({ type: 'OPERATOR', value: '!==' })
      i += 3
      continue
    }
    if (input.startsWith('==', i)) {
      tokens.push({ type: 'OPERATOR', value: '==' })
      i += 2
      continue
    }
    if (input.startsWith('!=', i)) {
      tokens.push({ type: 'OPERATOR', value: '!=' })
      i += 2
      continue
    }
    if (input.startsWith('<=', i)) {
      tokens.push({ type: 'OPERATOR', value: '<=' })
      i += 2
      continue
    }
    if (input.startsWith('>=', i)) {
      tokens.push({ type: 'OPERATOR', value: '>=' })
      i += 2
      continue
    }
    if (input.startsWith('&&', i)) {
      tokens.push({ type: 'OPERATOR', value: '&&' })
      i += 2
      continue
    }
    if (input.startsWith('||', i)) {
      tokens.push({ type: 'OPERATOR', value: '||' })
      i += 2
      continue
    }

    // Single-character operators: +, -, *, /, %, <, >, !
    if (['+', '-', '*', '/', '%', '<', '>', '!'].includes(ch)) {
      tokens.push({ type: 'OPERATOR', value: ch })
      i++
      continue
    }

    // Identifiers: keywords / functions / forbidden identifiers
    if (/[a-zA-Z_]/.test(ch)) {
      let ident = ''
      while (i < len && /[a-zA-Z0-9_]/.test(input[i])) {
        ident += input[i]
        i++
      }
      if (ident === 'true') {
        tokens.push({ type: 'BOOLEAN', value: true })
      } else if (ident === 'false') {
        tokens.push({ type: 'BOOLEAN', value: false })
      } else if (ident === 'null') {
        tokens.push({ type: 'NULL', value: null })
      } else if (ident === 'undefined') {
        tokens.push({ type: 'UNDEFINED', value: undefined })
      } else if (ALLOWED_MATH_FUNCS.has(ident.toUpperCase())) {
        tokens.push({ type: 'IDENTIFIER', value: ident.toUpperCase() })
      } else {
        throw new Error(`Forbidden identifier in expression: ${ident}`)
      }
      continue
    }

    throw new Error(`Unexpected character in expression: ${ch}`)
  }

  tokens.push({ type: 'EOF', value: null })
  return tokens
}

export class SafeParser {
  private tokens: Token[]
  private pos = 0

  constructor(tokens: Token[]) {
    this.tokens = tokens
  }

  private peek(): Token {
    return this.tokens[this.pos] || { type: 'EOF', value: null }
  }

  private next(): Token {
    const t = this.peek()
    this.pos++
    return t
  }

  private match(type: TokenType, val?: any): boolean {
    const t = this.peek()
    if (t.type === type && (val === undefined || t.value === val)) {
      this.pos++
      return true
    }
    return false
  }

  public parse(): any {
    const res = this.parseLogicalOr()
    if (this.peek().type !== 'EOF') {
      throw new Error(`Unexpected token at end of expression: ${this.peek().value}`)
    }
    return res
  }

  private parseLogicalOr(): any {
    let left = this.parseLogicalAnd()
    while (this.match('OPERATOR', '||')) {
      const right = this.parseLogicalAnd()
      left = Boolean(left) || Boolean(right)
    }
    return left
  }

  private parseLogicalAnd(): any {
    let left = this.parseEquality()
    while (this.match('OPERATOR', '&&')) {
      const right = this.parseEquality()
      left = Boolean(left) && Boolean(right)
    }
    return left
  }

  private parseEquality(): any {
    let left = this.parseRelational()
    while (true) {
      if (this.match('OPERATOR', '===')) {
        const right = this.parseRelational()
        left = left === right
      } else if (this.match('OPERATOR', '!==')) {
        const right = this.parseRelational()
        left = left !== right
      } else if (this.match('OPERATOR', '==')) {
        const right = this.parseRelational()
        // eslint-disable-next-line eqeqeq
        left = left == right
      } else if (this.match('OPERATOR', '!=')) {
        const right = this.parseRelational()
        // eslint-disable-next-line eqeqeq
        left = left != right
      } else {
        break
      }
    }
    return left
  }

  private parseRelational(): any {
    let left = this.parseAdditive()
    while (true) {
      if (this.match('OPERATOR', '<=')) {
        const right = this.parseAdditive()
        left = left <= right
      } else if (this.match('OPERATOR', '>=')) {
        const right = this.parseAdditive()
        left = left >= right
      } else if (this.match('OPERATOR', '<')) {
        const right = this.parseAdditive()
        left = left < right
      } else if (this.match('OPERATOR', '>')) {
        const right = this.parseAdditive()
        left = left > right
      } else {
        break
      }
    }
    return left
  }

  private parseAdditive(): any {
    let left = this.parseMultiplicative()
    while (true) {
      if (this.match('OPERATOR', '+')) {
        const right = this.parseMultiplicative()
        left = left + right
      } else if (this.match('OPERATOR', '-')) {
        const right = this.parseMultiplicative()
        left = left - right
      } else {
        break
      }
    }
    return left
  }

  private parseMultiplicative(): any {
    let left = this.parseUnary()
    while (true) {
      if (this.match('OPERATOR', '*')) {
        const right = this.parseUnary()
        left = left * right
      } else if (this.match('OPERATOR', '/')) {
        const right = this.parseUnary()
        left = right !== 0 ? left / right : 0
      } else if (this.match('OPERATOR', '%')) {
        const right = this.parseUnary()
        left = left % right
      } else {
        break
      }
    }
    return left
  }

  private parseUnary(): any {
    if (this.match('OPERATOR', '!')) {
      return !this.parseUnary()
    }
    if (this.match('OPERATOR', '-')) {
      return -this.parseUnary()
    }
    if (this.match('OPERATOR', '+')) {
      return +this.parseUnary()
    }
    return this.parsePrimary()
  }

  private parsePrimary(): any {
    const t = this.peek()

    if (t.type === 'NUMBER' || t.type === 'STRING' || t.type === 'BOOLEAN' || t.type === 'NULL' || t.type === 'UNDEFINED') {
      this.next()
      return t.value
    }

    if (this.match('PAREN_OPEN')) {
      const expr = this.parseLogicalOr()
      if (!this.match('PAREN_CLOSE')) {
        throw new Error('Missing closing parenthesis')
      }
      return expr
    }

    if (t.type === 'IDENTIFIER') {
      const fnName = t.value
      this.next()
      if (!this.match('PAREN_OPEN')) {
        throw new Error(`Expected '(' after function ${fnName}`)
      }
      const args: any[] = []
      if (!this.match('PAREN_CLOSE')) {
        args.push(this.parseLogicalOr())
        while (this.match('COMMA')) {
          args.push(this.parseLogicalOr())
        }
        if (!this.match('PAREN_CLOSE')) {
          throw new Error(`Missing closing parenthesis in call to ${fnName}`)
        }
      }

      if (fnName === 'ROUND') {
        const val = Number(args[0] || 0)
        const dec = Number(args[1] || 0)
        return Number(Math.round(Number(val + 'e' + dec)) + 'e-' + dec)
      }
      if (fnName === 'ABS') {
        return Math.abs(Number(args[0] || 0))
      }
      if (fnName === 'CEIL') {
        return Math.ceil(Number(args[0] || 0))
      }
      if (fnName === 'FLOOR') {
        return Math.floor(Number(args[0] || 0))
      }
      throw new Error(`Unknown function: ${fnName}`)
    }

    throw new Error(`Unexpected token: ${JSON.stringify(t)}`)
  }
}

/**
 * Safely evaluates a condition expression against form data.
 * @param expr e.g. "#{status} === 'APPROVED' && #{amount} > 100"
 * @param formData Key-value map of record data
 * @returns boolean result
 */
export function safeEvaluateCondition(expr: string | null | undefined, formData: Record<string, any> | null | undefined): boolean {
  if (!expr || !expr.trim() || !formData) return false
  try {
    const replaced = expr.replace(/#{([a-zA-Z0-9_]+)}/g, (_, key) => {
      const val = formData[key]
      if (val === undefined || val === null) return 'null'
      if (typeof val === 'number' || typeof val === 'boolean') return String(val)
      if (typeof val === 'object') return JSON.stringify(JSON.stringify(val))
      return JSON.stringify(String(val))
    })
    const tokens = tokenize(replaced)
    const parser = new SafeParser(tokens)
    return Boolean(parser.parse())
  } catch (e) {
    return false
  }
}

/**
 * Safely evaluates a mathematical formula against form data.
 * @param formula e.g. "${price} * ${qty} * (1 - ${discount})" or "ROUND(${subtotal} * 1.1, 2)"
 * @param data Key-value map of record data
 * @returns calculated number or null
 */
export function safeEvaluateFormula(formula: string | null | undefined, data: Record<string, any> | null | undefined): number | null {
  if (!formula || !formula.trim() || !data) return null
  try {
    const replaced = formula.replace(/\${([^}]+)}/g, (_, key) => {
      const val = data[key]
      return val != null && val !== '' ? String(val) : '0'
    })
    const tokens = tokenize(replaced)
    const parser = new SafeParser(tokens)
    const res = parser.parse()
    return typeof res === 'number' && !isNaN(res) ? res : null
  } catch (e) {
    return null
  }
}

/**
 * Validates syntax of a formula. Throws or returns error if invalid.
 */
export function validateFormulaSyntax(formula: string | null | undefined): void {
  if (!formula || !formula.trim()) {
    throw new Error('Formula cannot be empty')
  }
  const testFormula = formula.replace(/\${[^}]+}/g, '1')
  const tokens = tokenize(testFormula)
  const parser = new SafeParser(tokens)
  parser.parse()
}
