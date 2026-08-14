import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import CandidateDetailSidebar from '../../components/admin/CandidateDetailSidebar.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key
  })
}))

describe('CandidateDetailSidebar.vue (TDD Component Test)', () => {
  const mockCandidate = {
    id: 'cand-123',
    score: 0.92,
    status: 'PENDING_REVIEW',
    existingRecord: {
      id: 'rec-1',
      name: '홍길동',
      phone: '010-1234-5678'
    },
    incomingData: {
      name: '홍길동',
      phone: '010-1234-5678',
      email: 'hong@example.com'
    }
  }

  it('사이드바 컴포넌트 렌더링 및 후보 점수 표시 검증', async () => {
    const wrapper = mount(CandidateDetailSidebar, {
      props: {
        candidate: mockCandidate,
        hasWritePermission: true
      },
      global: {
        stubs: {
          'va-card': {
            template: '<div class="va-card-stub"><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    expect(wrapper.find('.va-card-stub').exists()).toBe(true)
    expect(wrapper.text()).toContain('홍길동')
    expect(wrapper.text()).toContain('010-1234-5678')
  })

  it('거절 버튼 클릭 시 reject 이벤트 방출', async () => {
    const wrapper = mount(CandidateDetailSidebar, {
      props: {
        candidate: mockCandidate,
        hasWritePermission: true
      },
      global: {
        stubs: {
          'va-card': {
            template: '<div class="va-card-stub"><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    const buttons = wrapper.findAll('.va-btn-stub')
    expect(buttons.length).toBe(2)
    // 거절 버튼
    await buttons[0].trigger('click')

    expect(wrapper.emitted('reject')).toBeTruthy()
    expect(wrapper.emitted('reject')![0]).toEqual([mockCandidate])
  })

  it('병합 승인 버튼 클릭 시 merge 이벤트 방출', async () => {
    const wrapper = mount(CandidateDetailSidebar, {
      props: {
        candidate: mockCandidate,
        hasWritePermission: true
      },
      global: {
        stubs: {
          'va-card': {
            template: '<div class="va-card-stub"><slot /></div>'
          },
          'va-badge': true,
          'va-icon': true,
          'va-button': {
            template: '<button class="va-btn-stub" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })

    const buttons = wrapper.findAll('.va-btn-stub')
    expect(buttons.length).toBe(2)
    // 병합 승인 버튼
    await buttons[1].trigger('click')

    expect(wrapper.emitted('merge')).toBeTruthy()
    expect(wrapper.emitted('merge')![0]).toEqual([mockCandidate])
  })
})
