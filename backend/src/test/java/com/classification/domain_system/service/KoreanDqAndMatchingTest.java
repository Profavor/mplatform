package com.classification.domain_system.service;

import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.service.dq.evaluators.BusinessNoChecksumEvaluator;
import com.classification.domain_system.service.dq.evaluators.CorporateNoChecksumEvaluator;
import com.classification.domain_system.utils.KoreanTextMatcher;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class KoreanDqAndMatchingTest {

    private final BusinessNoChecksumEvaluator bizEvaluator = new BusinessNoChecksumEvaluator();
    private final CorporateNoChecksumEvaluator corpEvaluator = new CorporateNoChecksumEvaluator();

    @Test
    @DisplayName("BusinessNoChecksum: 유효한 사업자등록번호는 통과하고 체크섬 불일치는 에러를 반환한다")
    void testBusinessNoChecksum() {
        FieldDefinition field = new FieldDefinition();
        field.setKey("biz_no");
        DqRule rule = new DqRule();

        // 120-81-47521 (실제 국세청 공식 가중치 알고리즘 일치 예시)
        // 1*1 + 2*3 + 0*7 + 8*1 + 1*3 + 4*7 + 7*1 + 5*3 + ((2*5)/10 + (2*5)%10) = 1+6+0+8+3+28+7+15+(1+0) = 69 -> checkDigit = (10 - 9)%10 = 1 (일치)
        Optional<String> validRes = bizEvaluator.evaluate(field, rule, new TextNode("120-81-47521"), null);
        assertThat(validRes).isEmpty();

        // 잘못된 번호 (마지막 체크섬 변조: 120-81-47529)
        Optional<String> invalidRes = bizEvaluator.evaluate(field, rule, new TextNode("120-81-47529"), null);
        assertThat(invalidRes).isPresent();
        assertThat(invalidRes.get()).contains("체크섬");

        // 길이 미달
        Optional<String> shortRes = bizEvaluator.evaluate(field, rule, new TextNode("123-45"), null);
        assertThat(shortRes).isPresent();
        assertThat(shortRes.get()).contains("10자리");
    }

    @Test
    @DisplayName("CorporateNoChecksum: 유효한 법인등록번호는 통과하고 체크섬 불일치는 에러를 반환한다")
    void testCorporateNoChecksum() {
        FieldDefinition field = new FieldDefinition();
        field.setKey("corp_no");
        DqRule rule = new DqRule();

        // 110111-0085450 (법인등록번호 가중치 [1,2,1,2,1,2,1,2,1,2,1,2])
        // 1*1 + 1*2 + 0*1 + 1*2 + 1*1 + 1*2 + 0*1 + 0*2 + 8*1 + 5*2 + 4*1 + 5*2 = 1+2+0+2+1+2+0+0+8+10+4+10 = 40 -> checkDigit = (10 - 0)%10 = 0 (일치)
        Optional<String> validRes = corpEvaluator.evaluate(field, rule, new TextNode("110111-0085450"), null);
        assertThat(validRes).isEmpty();

        // 체크섬 변조: 110111-0085459
        Optional<String> invalidRes = corpEvaluator.evaluate(field, rule, new TextNode("110111-0085459"), null);
        assertThat(invalidRes).isPresent();
        assertThat(invalidRes.get()).contains("체크섬");
    }

    @Test
    @DisplayName("KoreanTextMatcher: 법인 접두사 정규화 및 한글 자모 유사도 계산 검증")
    void testKoreanTextMatcher() {
        // 법인 표기 정규화
        assertThat(KoreanTextMatcher.normalizeCompanyName("(주) 카카오")).isEqualTo("카카오");
        assertThat(KoreanTextMatcher.normalizeCompanyName("주식회사 카카오")).isEqualTo("카카오");
        assertThat(KoreanTextMatcher.normalizeCompanyName("현대자동차㈜")).isEqualTo("현대자동차");

        // 동일 회사명 유사도 1.0
        double sim1 = KoreanTextMatcher.calculateKoreanFuzzySimilarity("(주)카카오", "주식회사 카카오");
        assertThat(sim1).isEqualTo(1.0);

        // 오타 유사도 (자모 분해 비교)
        double sim2 = KoreanTextMatcher.calculateKoreanFuzzySimilarity("삼성전자", "삼송전자");
        assertThat(sim2).isGreaterThan(0.85);

        // 초성 추출
        assertThat(KoreanTextMatcher.extractChosung("카카오")).isEqualTo("ㅋㅋㅇ");
    }
}
