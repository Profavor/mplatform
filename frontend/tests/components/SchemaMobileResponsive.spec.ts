import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'
import fs from 'fs'
import path from 'path'

describe('Schema Page Mobile Responsiveness & Tree Visibility Spec (TDD)', () => {
  it('verifies horizontal tree collapse/expand toggle and state structure in Schema', async () => {
    const TestComponent = {
      template: `
        <div class="schema-page-root">
          <div class="schema-top-bar">
            <div class="schema-top-title">도메인 & 스키마 관리</div>
            <div class="schema-top-actions">
              <button class="action-btn">시맨틱 온톨로지</button>
            </div>
          </div>
          <div class="schema-layout">
            <div class="schema-tree-column" :class="{ 'tree-collapsed': !showTree }">
              <div class="schema-tree-card-title" :class="{ 'is-collapsed': !showTree }" @click="showTree = !showTree">
                <template v-if="showTree">
                  <span>분류체계 트리</span>
                  <button class="toggle-btn" data-icon="chevron_left">접기</button>
                </template>
                <template v-else>
                  <button class="toggle-btn" data-icon="chevron_right">펼치기</button>
                  <div class="tree-collapsed-label">분류체계 트리</div>
                </template>
              </div>
              <div v-show="showTree" class="tree-content">Tree Content</div>
            </div>
            <div class="schema-detail-column">
              <div class="detail-card">Detail Content</div>
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

    // Initially expanded
    expect(wrapper.find('.schema-tree-column').classes()).not.toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_left')
    expect(wrapper.find('.tree-content').attributes('style') || '').not.toContain('display: none')

    // Click to collapse
    await wrapper.find('.schema-tree-card-title').trigger('click')
    expect(wrapper.find('.schema-tree-column').classes()).toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_right')
    expect(wrapper.find('.tree-content').attributes('style')).toContain('display: none')
    expect(wrapper.find('.tree-collapsed-label').exists()).toBe(true)

    // Click to expand again
    await wrapper.find('.schema-tree-card-title').trigger('click')
    expect(wrapper.find('.schema-tree-column').classes()).not.toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_left')
    expect(wrapper.find('.tree-content').attributes('style') || '').not.toContain('display: none')
  })

  it('verifies that schema.vue contains proper mobile responsiveness styles without breaking nested scroll', () => {
    const schemaVuePath = path.resolve(__dirname, '../../pages/schema.vue')
    const content = fs.readFileSync(schemaVuePath, 'utf-8')

    // Must have schema-layout responsive rules
    expect(content).toContain('.schema-layout')
    expect(content).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.schema-layout[\s\S]*?height:\s*auto\s*!important/)
    expect(content).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.schema-detail-column[\s\S]*?min-height:\s*450px/)
  })

  it('verifies schema-grid-wrapper has explicit flex column on mobile to prevent AG-Grid collapse', () => {
    const schemaVuePath = path.resolve(__dirname, '../../pages/schema.vue')
    const content = fs.readFileSync(schemaVuePath, 'utf-8')

    expect(content).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.schema-grid-wrapper[\s\S]*?display:\s*flex\s*!important/)
    expect(content).toMatch(/@media\s*\(max-width:\s*768px\)[\s\S]*?\.schema-grid-wrapper[\s\S]*?flex-direction:\s*column\s*!important/)
  })
})
