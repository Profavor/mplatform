import { describe, it, expect } from 'vitest'
import fs from 'node:fs'
import path from 'node:path'

const koAdmin = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../i18n/locales/ko/admin.json'), 'utf-8'))
const enAdmin = JSON.parse(fs.readFileSync(path.resolve(__dirname, '../../i18n/locales/en/admin.json'), 'utf-8'))

describe('i18n locale messages - admin.json', () => {
  it('접근 로그 다국어 키 (access_log_reason)가 ko 및 en 메시지에 존재해야 한다', () => {
    expect(koAdmin).toHaveProperty('access_log_reason')
    expect(enAdmin).toHaveProperty('access_log_reason')
    expect(koAdmin.access_log_reason).toBe('열람 사유')
    expect(enAdmin.access_log_reason).toBe('Access Reason')
  })
})

