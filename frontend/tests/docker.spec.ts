import { describe, it, expect } from 'vitest'
import * as fs from 'fs'
import * as path from 'path'

describe('Dockerfile 무결성 및 하드코딩 제거 검증 (TDD)', () => {
  it('Dockerfile 내에 개인 배포 서버 도메인(onrender.com)이 하드코딩되어 있지 않아야 함 (Red)', () => {
    const dockerfilePath = path.resolve(__dirname, '../Dockerfile')
    const content = fs.readFileSync(dockerfilePath, 'utf-8')

    // 개인 호스팅 도메인 하드코딩 방지
    expect(content).not.toContain('onrender.com')
    // 환경 변수 주입을 위한 기본 fallback은 중립적인 localhost:8080 이어야 함
    expect(content).toContain('ARG API_BASE_URL=http://localhost:8080')
  })
})
