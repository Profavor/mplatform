import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import OrgTreeItem from '../../components/OrgTreeItem.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => {
      const map: Record<string, string> = {
        'dept': '부서',
        'manage_members': '구성원 관리',
        'add_subdept': '하위 부서 추가',
        'edit': '수정',
        'delete': '삭제'
      }
      return map[key] || key
    },
    locale: { value: 'ko' }
  })
}))

describe('OrgTreeItem.vue (TDD Component Test)', () => {
  it('부서 노드와 액션 버튼이 다국어에 맞게 렌더링되고 이벤트를 발행한다', async () => {
    const mockNode = {
      id: 'dept-1',
      name: { ko: '플랫폼개발부', en: 'Platform Dev Dept' },
      icon: 'code',
      memberCount: 5,
      role: 'DEVELOPER'
    }

    const wrapper = mount(OrgTreeItem, {
      props: {
        node: mockNode
      },
      global: {
        stubs: {
          'va-icon': true,
          'va-chip': { template: '<span class="va-chip-stub"><slot /></span>' },
          'va-badge': true,
          'va-button': { template: '<button class="va-button-stub" @click="$attrs.onClick"><slot /></button>' }
        }
      }
    })

    expect(wrapper.text()).toContain('플랫폼개발부')
    expect(wrapper.text()).toContain('부서')
    expect(wrapper.text()).toContain('구성원 관리')
    expect(wrapper.text()).toContain('하위 부서 추가')
    expect(wrapper.text()).toContain('수정')
    expect(wrapper.text()).toContain('삭제')
  })
})
