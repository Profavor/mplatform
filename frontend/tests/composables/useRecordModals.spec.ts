import { describe, it, expect, beforeEach } from 'vitest'
import { useRecordModals } from '~/composables/useRecordModals'

describe('useRecordModals', () => {
  let modals: ReturnType<typeof useRecordModals>

  beforeEach(() => {
    modals = useRecordModals()
  })

  it('초기 모달 상태들이 모두 false로 닫혀있어야 한다', () => {
    expect(modals.showCreateModal.value).toBe(false)
    expect(modals.showDetailDrawer.value).toBe(false)
    expect(modals.showCompareModal.value).toBe(false)
    expect(modals.showLineageModal.value).toBe(false)
    expect(modals.showAsyncExportModal.value).toBe(false)
    expect(modals.showBulkReclassifyModal.value).toBe(false)
    expect(modals.showCdcStreamModal.value).toBe(false)
    expect(modals.showGridImageLightbox.value).toBe(false)
  })

  it('openDetailDrawer 및 closeDetailDrawer가 대상 ID와 함께 정상 작동해야 한다', () => {
    modals.openDetailDrawer('rec-123')
    expect(modals.showDetailDrawer.value).toBe(true)
    expect(modals.selectedRecordId.value).toBe('rec-123')

    modals.closeDetailDrawer()
    expect(modals.showDetailDrawer.value).toBe(false)
    expect(modals.selectedRecordId.value).toBeNull()
  })

  it('openGridImageLightbox가 이미지 목록과 인덱스를 저장하고 모달을 열어야 한다', () => {
    const images = ['img1.png', 'img2.png']
    modals.openGridImageLightbox(images, 1)

    expect(modals.showGridImageLightbox.value).toBe(true)
    expect(modals.gridLightboxImages.value).toEqual(images)
    expect(modals.gridLightboxIndex.value).toBe(1)

    modals.closeGridImageLightbox()
    expect(modals.showGridImageLightbox.value).toBe(false)
  })

  it('openCompareModal 및 openLineageModal이 정상 작동해야 한다', () => {
    modals.openCompareModal()
    expect(modals.showCompareModal.value).toBe(true)
    modals.closeCompareModal()
    expect(modals.showCompareModal.value).toBe(false)

    modals.openLineageModal()
    expect(modals.showLineageModal.value).toBe(true)
    modals.closeLineageModal()
    expect(modals.showLineageModal.value).toBe(false)
  })
})
