import ExcelJS from 'exceljs'

export function useRecordBulkActions() {

  const parseJwtUserId = (tStr: string): string | null => {
    if (!tStr) return null
    try {
      const base64Url = tStr.split('.')[1]
      if (!base64Url) return null
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      )
      const parsed = JSON.parse(jsonPayload)
      return parsed.userId || parsed.uuid || parsed.username || parsed.sub || null
    } catch {
      return null
    }
  }

  const handleBulkDelete = async ({
    selectedRows,
    t,
    confirm,
    customFetch,
    initToast,
    onSuccess
  }: {
    selectedRows: any[]
    t: (key: string, def?: string) => string
    confirm: (options: any) => Promise<boolean>
    customFetch: (url: string, options?: any) => Promise<any>
    initToast: (options: { message: string; color: string }) => void
    onSuccess?: () => void
  }) => {
    if (!selectedRows || selectedRows.length === 0) {
      initToast({ message: t('select_records_first', '삭제할 레코드를 먼저 선택해주세요.'), color: 'warning' })
      return
    }

    const ok = await confirm({
      title: t('bulk_delete_title', '일괄 삭제 확인'),
      message: t('bulk_delete_confirm', `선택한 ${selectedRows.length}개의 레코드를 삭제하시겠습니까?`),
      okText: t('delete', '삭제'),
      cancelText: t('cancel', '취소'),
      color: 'danger'
    })

    if (!ok) return

    try {
      for (const row of selectedRows) {
        const id = row.id || row.recordId
        if (id) {
          await customFetch(`/api/records/${id}`, { method: 'DELETE' })
        }
      }
      initToast({ message: t('bulk_delete_success', '선택한 레코드가 성공적으로 삭제되었습니다.'), color: 'success' })
      if (onSuccess) onSuccess()
    } catch (e: any) {
      initToast({ message: t('bulk_delete_failed', '일괄 삭제 처리 중 오류가 발생했습니다: ') + (e?.message || ''), color: 'danger' })
    }
  }

  return {
    parseJwtUserId,
    handleBulkDelete
  }
}
