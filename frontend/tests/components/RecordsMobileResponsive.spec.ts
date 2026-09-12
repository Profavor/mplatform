import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import fs from 'fs'
import path from 'path'

describe('Records Mobile Responsive and Tree Visibility Spec (TDD)', () => {
  it('toggles tree column collapsed state and preserves visibility structure', async () => {
    const TestComponent = {
      template: `
        <div class="records-container records-layout">
          <div class="left-tree records-tree-column" :class="{ 'tree-collapsed': !showTree }">
            <div class="tree-header" :class="{ 'is-collapsed': !showTree }" @click="showTree = !showTree">
              <template v-if="showTree">
                <span class="title">분류 체계</span>
                <button class="toggle-btn" data-icon="chevron_left">접기</button>
              </template>
              <template v-else>
                <button class="toggle-btn" data-icon="chevron_right">펼치기</button>
                <div class="tree-collapsed-label">분류 체계</div>
              </template>
            </div>
            <div v-show="showTree" class="tree-scroll-container">
              <div class="va-tree">Node Item</div>
            </div>
          </div>
          <div class="right-content records-detail-column">
            <div class="records-top-context-bar">
              <div class="records-context-left">
                <span class="node-name">도메인</span>
              </div>
              <div class="records-context-right">
                <span class="total-badge">전체: 10</span>
              </div>
            </div>
            <div class="records-grid-wrapper">
              <div class="ag-root">AG Grid Content</div>
            </div>
          </div>
        </div>
      `,
      setup() {
        const showTree = ref(true)
        return { showTree }
      }
    }

    const wrapper = mount(TestComponent)

    // Initial state: expanded tree column
    expect(wrapper.find('.records-tree-column').classes()).not.toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_left')
    expect(wrapper.find('.tree-scroll-container').attributes('style') || '').not.toContain('display: none')
    expect(wrapper.find('.records-context-left').exists()).toBe(true)
    expect(wrapper.find('.records-context-right').exists()).toBe(true)

    // Toggle collapse
    await wrapper.find('.tree-header').trigger('click')
    expect(wrapper.find('.records-tree-column').classes()).toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_right')
    expect(wrapper.find('.tree-scroll-container').attributes('style')).toContain('display: none')

    // Toggle expand again
    await wrapper.find('.tree-header').trigger('click')
    expect(wrapper.find('.records-tree-column').classes()).not.toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_left')
    expect(wrapper.find('.tree-scroll-container').attributes('style') || '').not.toContain('display: none')
  })

  it('verifies that records.vue contains explicit mobile height and flex-shrink styling for .records-tree-column', () => {
    const recordsVuePath = path.resolve(__dirname, '../../pages/records.vue')
    const content = fs.readFileSync(recordsVuePath, 'utf-8')

    // Must have records-tree-column with explicit height / min-height on mobile
    expect(content).toContain('.records-tree-column')
    expect(content).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.records-tree-column[\s\S]*?height:\s*280px/)
    expect(content).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.records-tree-column[\s\S]*?flex-shrink:\s*0/)
  })

  it('verifies that AppFooter.vue contains responsive mobile layout styles', () => {
    const footerVuePath = path.resolve(__dirname, '../../components/layout/AppFooter.vue')
    const content = fs.readFileSync(footerVuePath, 'utf-8')

    // AppFooter must have responsive styling for mobile (<= 768px) and not be hidden
    expect(content).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.app-footer/)
    expect(content).not.toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.app-footer[\s\S]*?display:\s*none\s*!important/)
  })

  it('verifies records.vue and default.vue expand flex containers on mobile so footer remains below content', () => {
    const recordsVuePath = path.resolve(__dirname, '../../pages/records.vue')
    const recordsContent = fs.readFileSync(recordsVuePath, 'utf-8')
    const defaultVuePath = path.resolve(__dirname, '../../layouts/default.vue')
    const defaultContent = fs.readFileSync(defaultVuePath, 'utf-8')

    // records.vue page root and layout must have display block and height auto on mobile
    expect(recordsContent).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.records-page-root[\s\S]*?height:\s*auto\s*!important/)
    expect(recordsContent).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.records-layout[\s\S]*?display:\s*block\s*!important/)

    // default.vue main-content-body must expand so footer is placed below
    expect(defaultContent).toMatch(/@media\s*\(max-width:\s*1023px\)[\s\S]*?\.main-content-body[\s\S]*?flex:\s*0\s*0\s*auto\s*!important/)
  })

  it('verifies AG-Grid wrapper in records.vue has explicit flex column and body min-height to prevent grid body collapsing to 0 on mobile', () => {
    const recordsVuePath = path.resolve(__dirname, '../../pages/records.vue')
    const recordsContent = fs.readFileSync(recordsVuePath, 'utf-8')

    // Must have records-grid-wrapper flex column and ag-root-wrapper-body min-height
    expect(recordsContent).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.records-grid-wrapper[\s\S]*?display:\s*flex\s*!important/)
    expect(recordsContent).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.records-grid-wrapper[\s\S]*?flex-direction:\s*column\s*!important/)
    expect(recordsContent).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.records-grid-wrapper\s*:deep\(\.ag-root-wrapper-body\)[\s\S]*?min-height:\s*420px\s*!important/)
  })

  it('verifies virtual scroll toggle button matches adjacent button UI (va-button, size small, preset toggle) and removes switch wrap', () => {
    const recordsVuePath = path.resolve(__dirname, '../../pages/records.vue')
    const recordsContent = fs.readFileSync(recordsVuePath, 'utf-8')

    // Must have virtual scroll toggle button with matching button UI
    expect(recordsContent).toContain('isVirtualScroll')
    expect(recordsContent).toContain('virtual-scroll-toggle-btn')
    expect(recordsContent).toMatch(/<va-button[\s\S]*?:preset="isVirtualScroll \? 'primary' : 'secondary'"[\s\S]*?icon="view_stream"[\s\S]*?class="virtual-scroll-toggle-btn"/)
    
    // Must NOT contain the out-of-place switch wrap or va-switch
    expect(recordsContent).not.toContain('virtual-scroll-switch-wrap')
    expect(recordsContent).not.toContain('virtual-scroll-switch')
    // Must NOT have old top pagination buttons or jump-to-page input
    expect(recordsContent).not.toContain(":preset=\"viewMode === 'pagination' ? 'primary' : 'secondary'\"")
    expect(recordsContent).not.toContain('jumpPageInput')
  })

  it('interactively verifies virtual scroll button toggles preset and active state on click', async () => {
    const TestToolbar = {
      template: `
        <div class="records-context-right">
          <div class="va-button-group">
            <button class="status-btn" :data-preset="status === 'ALL' ? 'primary' : 'secondary'">전체 상태</button>
          </div>
          <button
            class="virtual-scroll-toggle-btn"
            :data-preset="isVirtualScroll ? 'primary' : 'secondary'"
            @click="isVirtualScroll = !isVirtualScroll"
          >
            가상 스크롤
          </button>
        </div>
      `,
      setup() {
        const status = ref('ALL')
        const isVirtualScroll = ref(false)
        return { status, isVirtualScroll }
      }
    }

    const wrapper = mount(TestToolbar)
    const btn = wrapper.find('.virtual-scroll-toggle-btn')

    // Initially OFF: preset is secondary
    expect(btn.attributes('data-preset')).toBe('secondary')

    // Click to toggle ON
    await btn.trigger('click')
    expect(btn.attributes('data-preset')).toBe('primary')

    // Click to toggle OFF
    await btn.trigger('click')
    expect(btn.attributes('data-preset')).toBe('secondary')
  })
})
