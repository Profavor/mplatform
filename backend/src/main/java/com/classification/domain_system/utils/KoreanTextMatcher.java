package com.classification.domain_system.utils;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import java.util.regex.Pattern;

public class KoreanTextMatcher {

    private static final char[] CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private static final char[] JUNGSUNG = {
            'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ',
            'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ'
    };

    private static final char[] JONGSUNG = {
            '\0', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ',
            'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ',
            'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private static final Pattern COMPANY_AFFIXES = Pattern.compile(
            "(\\([주유합합자사]\\)|\\(주식회사\\)|\\(유한회사\\)|\\(합자회사\\)|\\(합명회사\\)|\\(사단법인\\)|\\(재단법인\\)|" +
            "㈜|㈲|㈳|㈵|주식회사|유한회사|합자회사|합명회사|사단법인|재단법인|\\(유\\)|\\(합\\)|\\(재\\)|\\(사\\))",
            Pattern.CASE_INSENSITIVE
    );

    private static final JaroWinklerSimilarity JARO_WINKLER = new JaroWinklerSimilarity();

    /**
     * 기업/상호명 법인 형태 접두/접미사 정규화 (예: "(주) 카카오" -> "카카오")
     */
    public static String normalizeCompanyName(String text) {
        if (text == null) return "";
        String normalized = COMPANY_AFFIXES.matcher(text).replaceAll("");
        return normalized.replaceAll("[\\s_\\-\\.]+", "").trim().toLowerCase();
    }

    /**
     * 한글 문자열을 자모(초성/중성/종성) 단위로 완전 분해 (예: "카카오" -> "ㅋㅏㅋㅏㅇㅗ")
     */
    public static String decomposeKorean(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int base = c - 0xAC00;
                int cho = base / (21 * 28);
                int jung = (base % (21 * 28)) / 28;
                int jong = base % 28;

                sb.append(CHOSUNG[cho]);
                sb.append(JUNGSUNG[jung]);
                if (jong > 0) {
                    sb.append(JONGSUNG[jong]);
                }
            } else if (!Character.isWhitespace(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 한글 초성 추출 (예: "카카오" -> "ㅋㅋㅇ")
     */
    public static String extractChosung(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0xAC00 && c <= 0xD7A3) {
                int base = c - 0xAC00;
                int cho = base / (21 * 28);
                sb.append(CHOSUNG[cho]);
            } else if (!Character.isWhitespace(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    /**
     * 한글 자모 분해 및 상호명 정규화를 거친 퍼지 유사도 계산 (0.0 ~ 1.0)
     */
    public static double calculateKoreanFuzzySimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;

        String norm1 = normalizeCompanyName(s1);
        String norm2 = normalizeCompanyName(s2);

        if (norm1.equals(norm2)) return 1.0;
        if (norm1.isEmpty() || norm2.isEmpty()) return 0.0;

        // 1. 자모 분해 유사도
        String jamo1 = decomposeKorean(norm1);
        String jamo2 = decomposeKorean(norm2);
        double jamoSim = JARO_WINKLER.apply(jamo1, jamo2);

        // 2. 초성 일치 보너스 (초성이 완전히 같거나 포함되는 경우)
        String cho1 = extractChosung(norm1);
        String cho2 = extractChosung(norm2);
        if (cho1.equals(cho2) && !cho1.isEmpty()) {
            jamoSim = Math.max(jamoSim, 0.95);
        }

        return jamoSim;
    }
}
