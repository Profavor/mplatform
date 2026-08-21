import { ref } from 'vue'

export function useRecordModals() {
  const showCreateModal = ref(false)
  const showDetailDrawer = ref(false)
  const showCompareModal = ref(false)
  const showLineageModal = ref(false)
  const showAsyncExportModal = ref(false)
  const showBulkReclassifyModal = ref(false)
  const showCdcStreamModal = ref(false)
  const selectedRecordId = ref<string | null>(null)

  // Image Lightbox
  const showGridImageLightbox = ref(false)
  const gridLightboxImages = ref<string[]>([])
  const gridLightboxIndex = ref(0)

  const openCreateModal = () => {
    showCreateModal.value = true
  }

  const closeCreateModal = () => {
    showCreateModal.value = false
  }

  const openDetailDrawer = (recordId: string) => {
    selectedRecordId.value = recordId
    showDetailDrawer.value = true
  }

  const closeDetailDrawer = () => {
    showDetailDrawer.value = false
    selectedRecordId.value = null
  }

  const openCompareModal = () => {
    showCompareModal.value = true
  }

  const closeCompareModal = () => {
    showCompareModal.value = false
  }

  const openLineageModal = () => {
    showLineageModal.value = true
  }

  const closeLineageModal = () => {
    showLineageModal.value = false
  }

  const openAsyncExportModal = () => {
    showAsyncExportModal.value = true
  }

  const closeAsyncExportModal = () => {
    showAsyncExportModal.value = false
  }

  const openBulkReclassifyModal = () => {
    showBulkReclassifyModal.value = true
  }

  const closeBulkReclassifyModal = () => {
    showBulkReclassifyModal.value = false
  }

  const openCdcStreamModal = () => {
    showCdcStreamModal.value = true
  }

  const closeCdcStreamModal = () => {
    showCdcStreamModal.value = false
  }

  const openGridImageLightbox = (images: string[], startIndex = 0) => {
    gridLightboxImages.value = images || []
    gridLightboxIndex.value = startIndex
    showGridImageLightbox.value = true
  }

  const closeGridImageLightbox = () => {
    showGridImageLightbox.value = false
    gridLightboxImages.value = []
    gridLightboxIndex.value = 0
  }

  return {
    showCreateModal,
    showDetailDrawer,
    showCompareModal,
    showLineageModal,
    showAsyncExportModal,
    showBulkReclassifyModal,
    showCdcStreamModal,
    selectedRecordId,
    showGridImageLightbox,
    gridLightboxImages,
    gridLightboxIndex,
    openCreateModal,
    closeCreateModal,
    openDetailDrawer,
    closeDetailDrawer,
    openCompareModal,
    closeCompareModal,
    openLineageModal,
    closeLineageModal,
    openAsyncExportModal,
    closeAsyncExportModal,
    openBulkReclassifyModal,
    closeBulkReclassifyModal,
    openCdcStreamModal,
    closeCdcStreamModal,
    openGridImageLightbox,
    closeGridImageLightbox
  }
}
