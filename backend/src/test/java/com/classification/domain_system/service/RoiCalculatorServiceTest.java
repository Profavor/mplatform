package com.classification.domain_system.service;

import com.classification.domain_system.dto.RoiCalculationRequest;
import com.classification.domain_system.dto.RoiCalculationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoiCalculatorServiceTest {

    private RoiCalculatorService roiCalculatorService;

    @BeforeEach
    void setUp() {
        roiCalculatorService = new RoiCalculatorService();
    }

    @Test
    @DisplayName("표준 시나리오: 거래처 500개, 월 세금계산서 200건, 시급 3만원일 때 정확한 ROI가 산정된다")
    void calculateRoi_standardScenario() {
        // given
        RoiCalculationRequest request = RoiCalculationRequest.builder()
                .partnerCount(500)
                .monthlyInvoiceCount(200)
                .hourlyWage(30000L)
                .manualAuditHoursMonthly(15.0)
                .build();

        // when
        RoiCalculationResponse response = roiCalculatorService.calculate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMonthlyTotalSavings()).isGreaterThan(500000L); // 50만원 이상 절감
        assertThat(response.getAnnualTotalSavings()).isEqualTo(response.getMonthlyTotalSavings() * 12);
        assertThat(response.getMonthlyHoursSaved()).isGreaterThan(10.0);
        assertThat(response.getStarterPlanMonthlyFee()).isEqualTo(110000L);
        assertThat(response.getStarterRoiMultiplier()).isGreaterThan(5.0); // 5배 이상 ROI
        assertThat(response.getPaybackDays()).isLessThanOrEqualTo(30); // 30일 이내 회수
        assertThat(response.getBreakdownItems()).hasSize(4);
    }

    @Test
    @DisplayName("기본값 방어: 요청 객체의 필드가 null인 경우 합리적인 기본값으로 대체되어 연산된다")
    void calculateRoi_defaultValuesWhenNull() {
        // given
        RoiCalculationRequest request = new RoiCalculationRequest();

        // when
        RoiCalculationResponse response = roiCalculatorService.calculate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMonthlyTotalSavings()).isPositive();
        assertThat(response.getBreakdownItems()).isNotEmpty();
    }

    @Test
    @DisplayName("경계값 방어: 음수 또는 0이 입력되더라도 최소 기준치로 보정되어 0 또는 양수가 반환된다")
    void calculateRoi_boundaryValues() {
        // given
        RoiCalculationRequest request = RoiCalculationRequest.builder()
                .partnerCount(-10)
                .monthlyInvoiceCount(-5)
                .hourlyWage(-1000L)
                .manualAuditHoursMonthly(-2.0)
                .build();

        // when
        RoiCalculationResponse response = roiCalculatorService.calculate(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMonthlyTotalSavings()).isGreaterThanOrEqualTo(0L);
    }
}
