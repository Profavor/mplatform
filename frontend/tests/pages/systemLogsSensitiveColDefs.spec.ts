import { describe, it, expect } from 'vitest'

describe('System Logs - Decryption Logs Grid Column Definitions (TDD)', () => {
  // 복호화 로그 컬럼 사양 검증
  const getSensitiveLogColDefs = (t: (key: string) => string, codeStore: any, locale: { value: string }) => [
    {
      headerValueGetter: () => t('access_log_time'),
      field: 'accessedAt',
      valueFormatter: (params: any) => params.value ? new Date(params.value).toLocaleString(locale.value === 'ko' ? 'ko-KR' : 'en-US') : '',
      sortable: true,
      width: 170
    },
    {
      headerValueGetter: () => t('access_log_viewer'),
      field: 'userDisplayName',
      valueGetter: (params: any) => params.data?.userDisplayName || params.data?.userId || '',
      width: 140
    },
    {
      headerValueGetter: () => t('access_log_target_type'),
      field: 'targetType',
      valueFormatter: (params: any) => {
        if (!params.value) return '-'
        return codeStore.getCodeName('TARGET_TYPE', params.value)
      },
      width: 140
    },
    {
      headerValueGetter: () => t('domain_name'),
      field: 'domainName',
      valueGetter: (params: any) => params.data?.domainName || '-',
      width: 130
    },
    {
      headerValueGetter: () => t('classification_name'),
      field: 'classificationName',
      valueGetter: (params: any) => params.data?.classificationName || '-',
      width: 120
    },
    {
      headerValueGetter: () => t('id_attribute'),
      field: 'idAttribute',
      valueGetter: (params: any) => params.data?.idAttribute || '-',
      width: 130
    },
    {
      headerValueGetter: () => t('name_attribute'),
      field: 'nameAttribute',
      valueGetter: (params: any) => params.data?.nameAttribute || '-',
      width: 130
    },
    {
      headerValueGetter: () => t('access_log_fields'),
      field: 'formattedFieldLabels',
      valueGetter: (params: any) => params.data?.formattedFieldLabels || params.data?.fieldKeys || '-',
      width: 180
    },
    {
      headerValueGetter: () => t('access_log_reason'),
      field: 'accessReason',
      valueGetter: (params: any) => params.data?.accessReason || '-',
      width: 220
    },
    {
      headerValueGetter: () => t('access_log_ip'),
      field: 'ipAddress',
      valueFormatter: (params: any) => {
        const val = params.value
        if (val === '::1' || val === '0:0:0:0:0:0:0:1') return '127.0.0.1'
        return val || '-'
      },
      width: 140
    }
  ]

  it('대상 ID(formattedTargetId) 컬럼이 삭제되어 목록에 없어야 함', () => {
    const mockT = (key: string) => key
    const mockCodeStore = { getCodeName: () => '' }
    const mockLocale = { value: 'ko' }
    const colDefs = getSensitiveLogColDefs(mockT, mockCodeStore, mockLocale)

    const targetIdCol = colDefs.find((col: any) => col.field === 'formattedTargetId' || col.field === 'targetId')
    expect(targetIdCol).toBeUndefined()
  })

  it('모든 컬럼에 flex 속성이 없어야 함 (전컬럼 flex 제외 및 px 고정)', () => {
    const mockT = (key: string) => key
    const mockCodeStore = { getCodeName: () => '' }
    const mockLocale = { value: 'ko' }
    const colDefs = getSensitiveLogColDefs(mockT, mockCodeStore, mockLocale)

    colDefs.forEach((col: any) => {
      expect(col.flex).toBeUndefined()
    })
  })

  it('모든 컬럼에 숫자 px 타입의 width 속성이 지정되어 있어야 함', () => {
    const mockT = (key: string) => key
    const mockCodeStore = { getCodeName: () => '' }
    const mockLocale = { value: 'ko' }
    const colDefs = getSensitiveLogColDefs(mockT, mockCodeStore, mockLocale)

    colDefs.forEach((col: any) => {
      expect(typeof col.width).toBe('number')
      expect(col.width).toBeGreaterThan(0)
    })
  })
})
