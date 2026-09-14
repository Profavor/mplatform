import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import RoleModal from '../../components/org/RoleModal.vue'

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string) => key,
    locale: { value: 'ko' }
  })
}))

describe('RoleModal.vue (TDD)', () => {
  it('역할 등록 모달 기본 렌더링 및 세부 권한 매트릭스 포함 확인', async () => {
    const wrapper = mount(RoleModal, {
      props: {
        modelValue: true,
        mode: 'create',
        roleForm: {
          name: '',
          displayNameKo: '',
          displayNameEn: '',
          descriptionKo: '',
          descriptionEn: '',
          permissions: ['domain:read']
        },
        groups: [
          {
            id: 'domain',
            title: '도메인 권한',
            code: 'domain',
            icon: '🌐',
            permissions: [{ label: '조회 (read)', value: 'domain:read' }]
          }
        ]
      },
      global: {
        stubs: {
          AppModal: {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-modal': {
            template: '<div class="va-modal-stub"><slot /></div>'
          },
          'va-input': true,
          'va-textarea': true,
          'va-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>'
          },
          PermissionMatrix: {
            props: ['modelValue', 'groups', 'editable'],
            template: '<div class="permission-matrix-stub">{{ groups.length }} groups</div>'
          }
        }
      }
    })

    expect(wrapper.find('.va-modal-stub').exists()).toBe(true)
    expect(wrapper.find('.permission-matrix-stub').exists()).toBe(true)
    expect(wrapper.find('.permission-matrix-stub').text()).toContain('1 groups')
  })
})
