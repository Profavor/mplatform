import { defineNuxtPlugin } from '#app'

export default defineNuxtPlugin(() => {
  if (typeof window === 'undefined') return

  let isDragging = false
  let currentDialog: HTMLElement | null = null
  let currentHandle: HTMLElement | null = null
  let startX = 0
  let startY = 0
  let initialX = 0
  let initialY = 0

  const getTransformValues = (element: HTMLElement) => {
    const style = window.getComputedStyle(element)
    const matrix = style.transform
    if (matrix === 'none' || !matrix) {
      return { x: 0, y: 0 }
    }
    const values = matrix.match(/matrix.*\((.+)\)/)?.[1].split(', ')
    if (values && values.length >= 6) {
      return { x: parseFloat(values[4]), y: parseFloat(values[5]) }
    }
    return { x: 0, y: 0 }
  }

  // Find draggable modal dialog container
  const findDialogElement = (target: HTMLElement): HTMLElement | null => {
    return target.closest('.va-modal__dialog, [role="dialog"], .integration-modal-container') as HTMLElement
  }

  // Check if click target is on header / top area
  const findHeaderHandle = (dialog: HTMLElement, target: HTMLElement): HTMLElement | null => {
    const explicitHeader = target.closest('.va-modal__header, .va-modal__title, .modal-header-banner, .modal-header, .va-card__title') as HTMLElement
    if (explicitHeader && dialog.contains(explicitHeader)) {
      return explicitHeader
    }

    // Fallback: Check if clicked on top 60px area of dialog
    const dialogRect = dialog.getBoundingClientRect()
    const relativeY = target.getBoundingClientRect().top - dialogRect.top
    if (relativeY <= 70) {
      return (dialog.firstElementChild as HTMLElement) || dialog
    }
    return null
  }

  window.addEventListener('mousedown', (e: MouseEvent) => {
    // Only handle primary left click
    if (e.button !== 0) return

    const targetNode = e.target as HTMLElement
    if (!targetNode) return

    // Prevent dragging when clicking interactive controls
    if (
      targetNode.tagName === 'BUTTON' ||
      targetNode.tagName === 'INPUT' ||
      targetNode.tagName === 'TEXTAREA' ||
      targetNode.tagName === 'SELECT' ||
      targetNode.closest('button') ||
      targetNode.closest('.va-button') ||
      targetNode.closest('.modal-close-btn') ||
      targetNode.closest('.va-input') ||
      targetNode.closest('.va-checkbox') ||
      targetNode.closest('.va-select')
    ) {
      return
    }

    const dialog = findDialogElement(targetNode)
    if (!dialog) return

    const handle = findHeaderHandle(dialog, targetNode)
    if (!handle) return

    isDragging = true
    currentDialog = dialog
    currentHandle = handle

    handle.style.cursor = 'grabbing'
    document.body.style.userSelect = 'none'

    startX = e.clientX
    startY = e.clientY

    const current = getTransformValues(dialog)
    initialX = current.x
    initialY = current.y
  })

  window.addEventListener('mousemove', (e: MouseEvent) => {
    if (!isDragging || !currentDialog) return

    const dx = e.clientX - startX
    const dy = e.clientY - startY

    const newX = initialX + dx
    const newY = initialY + dy

    currentDialog.style.transform = `translate3d(${newX}px, ${newY}px, 0px)`
  })

  const stopDragging = () => {
    if (isDragging && currentHandle) {
      currentHandle.style.cursor = 'grab'
    }
    document.body.style.userSelect = ''
    isDragging = false
    currentDialog = null
    currentHandle = null
  }

  window.addEventListener('mouseup', stopDragging)
  window.addEventListener('mouseleave', stopDragging)
})
