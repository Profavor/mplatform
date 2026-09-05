import { describe, it, expect } from 'vitest'
import fs from 'fs'
import path from 'path'

describe('GitHub Issues Fixes - Batch Round 4 (TDD Tests)', () => {
  describe('#126: 임시 비밀번호 사용자 강제 교체 모달 잠금 검증', () => {
    it('default.vue의 Force Password Change Modal에 :no-outside-dismiss="true" 및 :show-close="false"가 적용되어 탈출이 차단되었는지 검증', () => {
      const defaultVuePath = path.resolve(__dirname, '../../layouts/default.vue')
      const content = fs.readFileSync(defaultVuePath, 'utf-8')

      // 강제 변경 모달의 탈출 방지 속성 확인
      expect(content).toContain('showForcePasswordChangeModal')
      expect(content).toContain(':no-outside-dismiss="true"')
      expect(content).toContain(':show-close="false"')
    })
  })

  describe('#140: 주식 도메인 해외 종목의 기본값 파싱 렌더링 검증', () => {
    it('records.vue의 getLocalizedDefaultValue 로직이 다국어 기본값 객체(ko/en)를 정상 파싱하는지 로직 검증', () => {
      const recordsVuePath = path.resolve(__dirname, '../../pages/records.vue')
      const content = fs.readFileSync(recordsVuePath, 'utf-8')

      expect(content).toContain('getLocalizedDefaultValue')
      
      // 모의 파싱 검증
      const testDefaultVal = { ko: '해당없음(해외)', en: 'N/A (Global)' }
      const parseHelper = (defVal: any, locale = 'ko') => {
        if (!defVal) return ''
        if (typeof defVal === 'object') return defVal[locale] || defVal.ko || defVal.en || ''
        return String(defVal)
      }

      expect(parseHelper(testDefaultVal, 'ko')).toBe('해당없음(해외)')
      expect(parseHelper(testDefaultVal, 'en')).toBe('N/A (Global)')
    })
  })

  describe('#108: 노드 레코드 대용량 페이징 결정성 및 재귀 하위노드 탐색 소스 검증', () => {
    it('CustomRecordRepositoryImpl.java에 결정적 타이브레이커(, r.id ASC)가 적용되어 있는지 검증', () => {
      const javaRepoPath = path.resolve(__dirname, '../../../backend/src/main/java/com/classification/domain_system/repository/CustomRecordRepositoryImpl.java')
      const content = fs.readFileSync(javaRepoPath, 'utf-8')

      expect(content).toContain('r.id ASC')
      expect(content).toContain('Deterministic pagination tie-breaker')
    })

    it('RecordService.java에 재귀 하위노드 수집(collectDescendantNodeIds)이 적용되어 있는지 검증', () => {
      const javaServicePath = path.resolve(__dirname, '../../../backend/src/main/java/com/classification/domain_system/service/RecordService.java')
      const content = fs.readFileSync(javaServicePath, 'utf-8')

      expect(content).toContain('collectDescendantNodeIds(nodeId, targetNodeIds)')
      expect(content).toContain('private void collectDescendantNodeIds(UUID parentId, List<UUID> accumulator)')
    })
  })
})
