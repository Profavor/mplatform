import { describe, it, expect } from 'vitest'

describe('Admin Form Validation Rules Spec (#82)', () => {
  // 1. Workflow Config validation helper
  const validateWorkflow = (modalData: any) => {
    const hasName = Boolean((modalData.nameKo && modalData.nameKo.trim()) || (modalData.nameEn && modalData.nameEn.trim()))
    if (!hasName) {
      return { valid: false, error: 'err_workflow_name_required' }
    }
    if (!modalData.actionType) {
      return { valid: false, error: 'err_action_type_required' }
    }
    if (!modalData.domainId) {
      return { valid: false, error: 'err_domain_required' }
    }
    if (modalData.scopeLevel === 'NODE' && !modalData.nodeId) {
      return { valid: false, error: 'err_node_required' }
    }
    return { valid: true }
  }

  // 2. Menu validation helper
  const validateMenu = (ko: string, en: string) => {
    const hasName = Boolean((ko && ko.trim()) || (en && en.trim()))
    if (!hasName) {
      return { valid: false, error: 'err_menu_name_required' }
    }
    return { valid: true }
  }

  // 3. Code Group / Detail validation helper
  const validateCodeGroup = (groupCode: string, nameKo: string, nameEn: string) => {
    if (!groupCode || !groupCode.trim()) {
      return { valid: false, error: 'err_group_code_required' }
    }
    const hasName = Boolean((nameKo && nameKo.trim()) || (nameEn && nameEn.trim()))
    if (!hasName) {
      return { valid: false, error: 'err_code_name_required' }
    }
    return { valid: true }
  }

  const validateCodeDetail = (detailCode: string, nameKo: string, nameEn: string) => {
    if (!detailCode || !detailCode.trim()) {
      return { valid: false, error: 'err_detail_code_required' }
    }
    const hasName = Boolean((nameKo && nameKo.trim()) || (nameEn && nameEn.trim()))
    if (!hasName) {
      return { valid: false, error: 'err_code_name_required' }
    }
    return { valid: true }
  }

  it('rejects empty workflow configuration and requires name and domain', () => {
    const emptyWorkflow = {
      nameKo: '',
      nameEn: '',
      actionType: '',
      domainId: '',
      scopeLevel: 'DOMAIN'
    }
    expect(validateWorkflow(emptyWorkflow).valid).toBe(false)
    expect(validateWorkflow(emptyWorkflow).error).toBe('err_workflow_name_required')

    const noDomain = {
      nameKo: '결재 서식',
      nameEn: '',
      actionType: 'CREATE',
      domainId: '',
      scopeLevel: 'DOMAIN'
    }
    expect(validateWorkflow(noDomain).valid).toBe(false)
    expect(validateWorkflow(noDomain).error).toBe('err_domain_required')

    const nodeScopeWithoutNode = {
      nameKo: '노드 서식',
      nameEn: '',
      actionType: 'CREATE',
      domainId: 'domain-1',
      scopeLevel: 'NODE',
      nodeId: null
    }
    expect(validateWorkflow(nodeScopeWithoutNode).valid).toBe(false)
    expect(validateWorkflow(nodeScopeWithoutNode).error).toBe('err_node_required')

    const validWorkflow = {
      nameKo: '정상 서식',
      nameEn: 'Valid Form',
      actionType: 'CREATE',
      domainId: 'domain-1',
      scopeLevel: 'DOMAIN'
    }
    expect(validateWorkflow(validWorkflow).valid).toBe(true)
  })

  it('rejects empty menu name', () => {
    expect(validateMenu('', '').valid).toBe(false)
    expect(validateMenu('   ', '').valid).toBe(false)
    expect(validateMenu('신규 메뉴', '').valid).toBe(true)
    expect(validateMenu('', 'New Menu').valid).toBe(true)
  })

  it('rejects empty code group and code detail', () => {
    expect(validateCodeGroup('', '', '').valid).toBe(false)
    expect(validateCodeGroup('SYS_CD', '', '').valid).toBe(false)
    expect(validateCodeGroup('SYS_CD', '시스템 코드', '').valid).toBe(true)

    expect(validateCodeDetail('', '', '').valid).toBe(false)
    expect(validateCodeDetail('D01', '', '').valid).toBe(false)
    expect(validateCodeDetail('D01', '상세 코드', '').valid).toBe(true)
  })
})
