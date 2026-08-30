import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref } from 'vue'

describe('Classification Tree Horizontal Collapse/Expand Spec', () => {
  it('toggles horizontal collapse state and updates chevron icons accordingly', async () => {
    const TestComponent = {
      template: `
        <div class="layout" style="display: flex;">
          <div class="tree-column" :class="{ 'tree-collapsed': !showTree }">
            <div class="tree-header" @click="showTree = !showTree">
              <template v-if="showTree">
                <span class="title">분류체계</span>
                <button class="toggle-btn" data-icon="chevron_left">접기</button>
              </template>
              <template v-else>
                <button class="toggle-btn" data-icon="chevron_right">펼치기</button>
                <div class="vertical-title">분류체계</div>
              </template>
            </div>
            <div v-show="showTree" class="tree-body">Tree Content</div>
          </div>
          <div class="content-column" style="flex: 1;">Grid Content</div>
        </div>
      `,
      setup() {
        const showTree = ref(true)
        return { showTree }
      }
    }

    const wrapper = mount(TestComponent)

    // Initial state: expanded
    expect(wrapper.find('.tree-column').classes()).not.toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_left')
    expect((wrapper.find('.tree-body').attributes('style') || '')).not.toContain('display: none')

    // Click header to collapse horizontally
    await wrapper.find('.tree-header').trigger('click')

    expect(wrapper.find('.tree-column').classes()).toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_right')
    expect((wrapper.find('.tree-body').attributes('style') || '')).toContain('display: none')

    // Click header again to expand horizontally
    await wrapper.find('.tree-header').trigger('click')

    expect(wrapper.find('.tree-column').classes()).not.toContain('tree-collapsed')
    expect(wrapper.find('.toggle-btn').attributes('data-icon')).toBe('chevron_left')
    expect((wrapper.find('.tree-body').attributes('style') || '')).not.toContain('display: none')
  })
})
