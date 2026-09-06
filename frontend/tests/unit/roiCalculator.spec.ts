import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import RoiCalculatorPage from '../../pages/roi-calculator.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn()
  }),
  useRoute: () => ({ query: {} })
}))

vi.mock('#app', () => ({
  useRoute: () => ({ query: {} }),
  useCookie: () => ({ value: null }),
  navigateTo: vi.fn(),
  useHead: vi.fn(),
  definePageMeta: vi.fn()
}))

vi.mock('vue-i18n', () => ({
  useI18n: () => ({
    t: (key: string, params?: Record<string, any>) => {
      if (params) {
        let result = key
        Object.keys(params).forEach(p => {
          result = result.replace(`{${p}}`, String(params[p]))
        })
        return result
      }
      return key
    },
    locale: { value: 'ko' }
  })
}))

vi.mock('vuestic-ui', async (importOriginal) => {
  const actual = await importOriginal<any>()
  return {
    ...actual,
    useToast: () => ({ init: vi.fn() }),
    useColors: () => ({
      currentPresetName: { value: 'light' }
    })
  }
})

describe('roi-calculator.vue (MDM ROI Calculator Unit Test)', () => {
  let wrapper: any

  const tMock = (key: string, params?: Record<string, any>) => {
    if (params) {
      let result = key
      Object.keys(params).forEach(p => {
        result = result.replace(`{${p}}`, String(params[p]))
      })
      return result
    }
    return key
  }

  const createWrapper = () => {
    return mount(RoiCalculatorPage, {
      global: {
        mocks: {
          $t: tMock
        },
        stubs: {
          'va-slider': {
            props: ['modelValue', 'min', 'max', 'step', 'label'],
            template: '<div class="va-slider-stub"><input type="range" :value="modelValue" @input="$emit(\'update:modelValue\', Number($event.target.value))" /></div>'
          },
          'va-input': {
            props: ['modelValue', 'type', 'label'],
            template: '<div class="va-input-stub"><input :value="modelValue" @input="$emit(\'update:modelValue\', Number($event.target.value))" /></div>'
          },
          'va-card': {
            template: '<div class="va-card-stub"><slot /></div>'
          },
          'va-card-title': {
            template: '<div class="va-card-title-stub"><slot /></div>'
          },
          'va-card-content': {
            template: '<div class="va-card-content-stub"><slot /></div>'
          },
          'va-button': {
            props: ['to', 'color', 'size', 'preset'],
            template: '<button class="va-btn-stub" :data-to="to" @click="$emit(\'click\')"><slot /></button>'
          },
          'va-badge': {
            props: ['text', 'color'],
            template: '<span class="va-badge-stub">{{ text }}</span>'
          },
          'va-icon': true,
          'NuxtLink': {
            props: ['to'],
            template: '<a :href="to"><slot /></a>'
          }
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('초기 폼 상태가 권장 기본값(거래처 1,000, 전표 3,000, 시급 25,000, 검수 40h)으로 올바르게 초기화된다', () => {
    wrapper = createWrapper()
    const vm = wrapper.vm as any

    expect(vm.form).toBeDefined()
    expect(vm.form.partnerCount).toBe(1000)
    expect(vm.form.monthlyInvoiceCount).toBe(3000)
    expect(vm.form.hourlyWage).toBe(25000)
    expect(vm.form.manualAuditHoursMonthly).toBe(40)
  })

  it('클라이언트 사이드 실시간 연산 로직이 4대 핵심 영역 절감액 및 ROI 지표를 정확하게 산출한다', async () => {
    wrapper = createWrapper()
    const vm = wrapper.vm as any

    // 기본값 상태에서의 계산 결과 검증
    // 1) 오류 정정: 3,000 * 0.03 * 1.0h * 25,000 = 2,250,000원
    // 2) 중복 대사: max(5, round(1000 / 50)) = 20h * 25,000 = 500,000원
    // 3) 수작업 자동화: 40h * 0.8 * 25,000 = 800,000원
    // 4) 세무·감사 증빙: round(32 / 12) = 3h * 25,000 = 75,000원
    // 월간 총 절감: 3,625,000원, 연간 총 절감: 43,500,000원
    // 월간 절감 시간: 90 + 20 + 32 + 3 = 145h, 연간: 1,740h
    // Starter ROI: round(3,625,000 / 110,000 * 10) / 10 = 33.0x
    // Payback days: round(110,000 / (3,625,000 / 30)) = 1일
    expect(vm.calculatedRoi).toBeDefined()
    expect(vm.calculatedRoi.monthlyTotalSavings).toBe(3625000)
    expect(vm.calculatedRoi.annualTotalSavings).toBe(43500000)
    expect(vm.calculatedRoi.monthlyHoursSaved).toBe(145)
    expect(vm.calculatedRoi.annualHoursSaved).toBe(1740)
    expect(vm.calculatedRoi.starterRoiMultiplier).toBeGreaterThan(0)
    expect(vm.calculatedRoi.paybackDays).toBeGreaterThanOrEqual(1)
  })

  it('슬라이더 입력 값 변경 시 실시간으로 계산 결과가 즉각 반영된다', async () => {
    wrapper = createWrapper()
    const vm = wrapper.vm as any

    // 거래처 수를 2,000, 세금계산서를 5,000건으로 증가
    vm.form.partnerCount = 2000
    vm.form.monthlyInvoiceCount = 5000
    await wrapper.vm.$nextTick()

    // 1) 오류 정정: 5,000 * 0.03 * 1.0 * 25,000 = 3,750,000원
    // 2) 중복 대사: max(5, round(2000 / 50)) = 40h * 25,000 = 1,000,000원
    // 3) 수작업: 800,000원
    // 4) 감사: 75,000원
    // 월 총합: 5,625,000원
    expect(vm.calculatedRoi.monthlyTotalSavings).toBe(5625000)
    expect(vm.calculatedRoi.annualTotalSavings).toBe(67500000)
  })

  it('14일 무료 체험 CTA 버튼이 /register 경로로 연결되어 있는지 검증', () => {
    wrapper = createWrapper()
    const ctaBtn = wrapper.find('[data-to="/register"]')
    expect(ctaBtn.exists()).toBe(true)
  })
})
