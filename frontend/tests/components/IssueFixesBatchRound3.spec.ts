import { describe, it, expect } from 'vitest'
import fs from 'fs'
import path from 'path'

describe('GitHub Issues Fixes - Batch Round 3 (TDD Tests)', () => {
  const readLocaleJson = (relPath: string) => {
    const fullPath = path.resolve(__dirname, '../../i18n/locales', relPath)
    return JSON.parse(fs.readFileSync(fullPath, 'utf-8'))
  }

  describe('#153: 신규 레코드 생성 워크플로우 i18n 검증', () => {
    it('ko/records.json 및 en/records.json에 create_record_in, edit_record_in, create_and_submit_approval 키가 정의되어 있는지 검증', () => {
      const koRecords = readLocaleJson('ko/records.json')
      const enRecords = readLocaleJson('en/records.json')

      expect(koRecords.create_record_in).toBe('{node}에 신규 레코드 생성')
      expect(enRecords.create_record_in).toBe('Create Record in {node}')

      expect(koRecords.edit_record_in).toBe('{node} 레코드 수정')
      expect(enRecords.edit_record_in).toBe('Edit Record in {node}')

      expect(koRecords.create_and_submit_approval).toBe('생성 및 결재 신청')
      expect(enRecords.create_and_submit_approval).toBe('Create & Submit for Approval')
    })

    it('RecordFormModal.vue에서 영문 하드코딩 버튼 및 타이틀이 제거되고 i18n 함수로 대체되었는지 소스 검증', () => {
      const modalVuePath = path.resolve(__dirname, '../../components/records/RecordFormModal.vue')
      const content = fs.readFileSync(modalVuePath, 'utf-8')

      // 버튼 하드코딩 제거 확인
      expect(content).not.toContain("'Create & Submit for Approval'")
      expect(content).not.toContain("Cancel</va-button>")
      expect(content).toContain("$t('records.create_and_submit_approval')")
      expect(content).toContain("$t('cancel')")

      // 다이얼로그 타이틀 동적 i18n 확인
      expect(content).not.toContain("`Create Record in ${props.nodeLabel}`")
      expect(content).toContain("t('records.create_record_in'")
    })
  })

  describe('#117: 전역 접근성(Accessible Name / aria-label) 보강 검증', () => {
    it('GlobalSearch.vue에 명시적 aria-label이 적용되었는지 검증', () => {
      const searchVuePath = path.resolve(__dirname, '../../components/layout/GlobalSearch.vue')
      const content = fs.readFileSync(searchVuePath, 'utf-8')

      expect(content).toContain(':aria-label="t(\'global_search_placeholder\')"')
    })

    it('RecordAdvancedSearch.vue의 모든 입력 및 선택 컨트롤에 formatFieldName 기반 aria-label이 바인딩되었는지 검증', () => {
      const advSearchVuePath = path.resolve(__dirname, '../../components/records/RecordAdvancedSearch.vue')
      const content = fs.readFileSync(advSearchVuePath, 'utf-8')

      expect(content).toContain(':aria-label="formatFieldName(field.name)"')
    })

    it('users.vue의 검색 인풋에 사용자 검색 aria-label이 적용되었는지 검증', () => {
      const usersVuePath = path.resolve(__dirname, '../../pages/admin/users.vue')
      const content = fs.readFileSync(usersVuePath, 'utf-8')

      expect(content).toContain(':aria-label="$t(\'users.search_user\', $t(\'search\'))"')
    })

    it('nuxt.config.ts의 vuestic 설정에 VaInput / VaInputWrapper inputAriaLabel 빈값 처리가 설정되었는지 검증', () => {
      const nuxtConfigPath = path.resolve(__dirname, '../../nuxt.config.ts')
      const content = fs.readFileSync(nuxtConfigPath, 'utf-8')

      expect(content).toContain('VaInput:')
      expect(content).toContain('inputAriaLabel:')
    })
  })

  describe('#134: 서바이버쉽 규칙 중복 방지 가드 검증', () => {
    it('ko/common.json 및 en/common.json에 duplicate_rule_warning 키가 정의되어 있는지 검증', () => {
      const koCommon = readLocaleJson('ko/common.json')
      const enCommon = readLocaleJson('en/common.json')

      expect(koCommon.duplicate_rule_warning).toBe('동일한 필드와 전략의 규칙이 이미 등록되어 있습니다.')
      expect(enCommon.duplicate_rule_warning).toBe('A rule with the same field and strategy already exists.')
    })

    it('survivorship.vue에 중복 검사 방어 로직이 적용되었는지 검증', () => {
      const survivorshipVuePath = path.resolve(__dirname, '../../pages/admin/survivorship.vue')
      const content = fs.readFileSync(survivorshipVuePath, 'utf-8')

      expect(content).toContain('duplicate_rule_warning')
    })
  })

  describe('#152: FieldDefinition.default_value 기반 빈 값 동적 렌더링 검증', () => {
    it('records.vue의 AG-Grid 컬럼 포매터에서 defaultValue를 로케일에 맞게 동적으로 파싱 및 표출하는지 검증', () => {
      const recordsVuePath = path.resolve(__dirname, '../../pages/records.vue')
      const content = fs.readFileSync(recordsVuePath, 'utf-8')

      expect(content).toContain('getLocalizedDefaultValue')
    })
  })
})
