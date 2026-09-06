package com.classification.domain_system.service;

import com.classification.domain_system.dto.RoiCalculationRequest;
import com.classification.domain_system.dto.RoiCalculationResponse;
import com.classification.domain_system.dto.RoiCalculationResponse.SavingsBreakdownItem;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoiCalculatorService {

    private static final long STARTER_MONTHLY_FEE = 110000L;
    private static final long PRO_MONTHLY_FEE = 790000L;

    public RoiCalculationResponse calculate(RoiCalculationRequest request) {
        int partnerCount = request != null && request.getPartnerCount() != null ? Math.max(1, request.getPartnerCount()) : 500;
        int monthlyInvoiceCount = request != null && request.getMonthlyInvoiceCount() != null ? Math.max(1, request.getMonthlyInvoiceCount()) : 200;
        long hourlyWage = request != null && request.getHourlyWage() != null ? Math.max(10000L, request.getHourlyWage()) : 30000L;
        double manualAuditHours = request != null && request.getManualAuditHoursMonthly() != null ? Math.max(0.0, request.getManualAuditHoursMonthly()) : 15.0;

        List<SavingsBreakdownItem> breakdownItems = new ArrayList<>();

        // 1. 마스터 오류 정정 비용 절감: 월 인보이스 건수의 3% 오류 가정, 건당 1.0시간 정정 소요
        double errHours = monthlyInvoiceCount * 0.03 * 1.0;
        long errSavings = Math.round(errHours * hourlyWage);
        breakdownItems.add(SavingsBreakdownItem.builder()
                .categoryKey("error_correction")
                .monthlySavings(errSavings)
                .monthlyHoursSaved(Math.round(errHours * 10.0) / 10.0)
                .featureKey("dq_rules_checksum")
                .build());

        // 2. 중복 거래처 대사 및 이중지급 방지: 거래처 50개당 월 1시간 수작업 대사 소요 (최소 5시간)
        double dupHours = Math.max(5.0, partnerCount / 50.0);
        long dupSavings = Math.round(dupHours * hourlyWage);
        breakdownItems.add(SavingsBreakdownItem.builder()
                .categoryKey("duplicate_prevention")
                .monthlySavings(dupSavings)
                .monthlyHoursSaved(Math.round(dupHours * 10.0) / 10.0)
                .featureKey("match_merge_engine")
                .build());

        // 3. 수작업 정기 검수 자동화 절감: 기존 수작업 시간의 80% 자동화 단축
        double auditHours = manualAuditHours * 0.8;
        long auditSavings = Math.round(auditHours * hourlyWage);
        breakdownItems.add(SavingsBreakdownItem.builder()
                .categoryKey("manual_audit_automation")
                .monthlySavings(auditSavings)
                .monthlyHoursSaved(Math.round(auditHours * 10.0) / 10.0)
                .featureKey("scheduled_dq_scan")
                .build());

        // 4. 세무·감사 자료 준비 시간 단축: 연간 32시간 단축분의 월 환산
        double compHours = 32.0 / 12.0;
        long compSavings = Math.round(compHours * hourlyWage);
        breakdownItems.add(SavingsBreakdownItem.builder()
                .categoryKey("compliance_audit")
                .monthlySavings(compSavings)
                .monthlyHoursSaved(Math.round(compHours * 10.0) / 10.0)
                .featureKey("hash_chain_audit")
                .build());

        long monthlyTotalSavings = errSavings + dupSavings + auditSavings + compSavings;
        long annualTotalSavings = monthlyTotalSavings * 12;

        double monthlyTotalHours = errHours + dupHours + auditHours + compHours;
        double roundedMonthlyHours = Math.round(monthlyTotalHours * 10.0) / 10.0;
        double roundedAnnualHours = Math.round(roundedMonthlyHours * 12.0 * 10.0) / 10.0;

        double starterRoi = monthlyTotalSavings > 0 ? Math.round(((double) monthlyTotalSavings / STARTER_MONTHLY_FEE) * 10.0) / 10.0 : 0.0;
        double proRoi = monthlyTotalSavings > 0 ? Math.round(((double) monthlyTotalSavings / PRO_MONTHLY_FEE) * 10.0) / 10.0 : 0.0;
        int paybackDays = monthlyTotalSavings > 0 ? Math.max(1, (int) Math.round(((double) STARTER_MONTHLY_FEE / monthlyTotalSavings) * 30.0)) : 30;

        return RoiCalculationResponse.builder()
                .monthlyTotalSavings(monthlyTotalSavings)
                .annualTotalSavings(annualTotalSavings)
                .monthlyHoursSaved(roundedMonthlyHours)
                .annualHoursSaved(roundedAnnualHours)
                .starterPlanMonthlyFee(STARTER_MONTHLY_FEE)
                .starterRoiMultiplier(starterRoi)
                .proPlanMonthlyFee(PRO_MONTHLY_FEE)
                .proRoiMultiplier(proRoi)
                .paybackDays(paybackDays)
                .breakdownItems(breakdownItems)
                .build();
    }
}
