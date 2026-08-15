package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
@Slf4j
public class CalculatedFieldEvaluator {

    private final FieldDefinitionService fieldDefinitionService;
    private final ObjectMapper mapper = new ObjectMapper();

    public String recomputeCalculatedFields(java.util.UUID nodeId, String dataJson) {
        if (dataJson == null || dataJson.isBlank()) return dataJson;
        try {
            Map<String, Object> data = mapper.readValue(dataJson, new TypeReference<Map<String, Object>>() {});
            List<FieldDefinition> fields = fieldDefinitionService.getEffectiveFields(nodeId);

            // 1. CALCULATED 필드 필터링 및 의존성 분석
            Map<String, FieldDefinition> calcFieldMap = new HashMap<>();
            Map<String, Set<String>> dependencies = new HashMap<>(); // key -> set of keys it depends on

            for (FieldDefinition field : fields) {
                if ("CALCULATED".equals(field.getType()) && field.getOptions() != null && field.getKey() != null) {
                    calcFieldMap.put(field.getKey(), field);
                    Set<String> referenced = extractReferencedKeys(field);
                    dependencies.put(field.getKey(), referenced);
                }
            }

            if (calcFieldMap.isEmpty()) {
                return dataJson;
            }

            // 2. DAG 위상 정렬 (Kahn's Algorithm)
            List<String> evaluationOrder = resolveTopologicalOrder(calcFieldMap.keySet(), dependencies);

            // 3. 위상 정렬 순서대로 순차 평가
            for (String fieldKey : evaluationOrder) {
                FieldDefinition field = calcFieldMap.get(fieldKey);
                if (field == null || field.getOptions() == null) continue;

                try {
                    JsonNode opts = mapper.readTree(field.getOptions());
                    if (opts.has("formula")) {
                        String formula = opts.get("formula").asText();
                        Double result = evaluateFormula(formula, data);
                        if (result != null && !result.isNaN() && !result.isInfinite()) {
                            data.put(field.getKey(), result);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed to calculate formula for field: {}", field.getKey(), e);
                }
            }

            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            log.error("Failed to recompute calculated fields for node: {}", nodeId, e);
            return dataJson;
        }
    }

    private Set<String> extractReferencedKeys(FieldDefinition field) {
        Set<String> refs = new HashSet<>();
        try {
            JsonNode opts = mapper.readTree(field.getOptions());
            if (opts.has("formula")) {
                String formula = opts.get("formula").asText();
                Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
                Matcher matcher = pattern.matcher(formula);
                while (matcher.find()) {
                    refs.add(matcher.group(1));
                }
            }
        } catch (Exception ignored) {}
        return refs;
    }

    private List<String> resolveTopologicalOrder(Set<String> allCalcKeys, Map<String, Set<String>> dependencies) {
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, List<String>> adjList = new HashMap<>();

        for (String key : allCalcKeys) {
            inDegree.put(key, 0);
            adjList.put(key, new ArrayList<>());
        }

        // 그래프 엣지 생성: dep -> key (dep가 계산된 후 key가 계산되어야 함)
        for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
            String targetKey = entry.getKey();
            for (String dep : entry.getValue()) {
                if (allCalcKeys.contains(dep)) {
                    adjList.get(dep).add(targetKey);
                    inDegree.put(targetKey, inDegree.get(targetKey) + 1);
                }
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (String key : allCalcKeys) {
            if (inDegree.get(key) == 0) {
                queue.add(key);
            }
        }

        List<String> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            String u = queue.poll();
            order.add(u);

            for (String v : adjList.get(u)) {
                inDegree.put(v, inDegree.get(v) - 1);
                if (inDegree.get(v) == 0) {
                    queue.add(v);
                }
            }
        }

        // 순환 참조(Cycle) 감지 시
        if (order.size() < allCalcKeys.size()) {
            log.warn("Circular dependency detected among calculated fields. Processed: {}, Total: {}", order.size(), allCalcKeys.size());
            // 순환에 빠지지 않은 노드 우선 처리 후 나머지 추가
            for (String key : allCalcKeys) {
                if (!order.contains(key)) {
                    order.add(key);
                }
            }
        }

        return order;
    }


    public Double evaluateFormula(String formula, Map<String, Object> data) {
        try {
            Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");
            Matcher matcher = pattern.matcher(formula);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                String key = matcher.group(1);
                if (!data.containsKey(key)) {
                    log.warn("Referenced key '{}' in formula '{}' not found in record data", key, formula);
                }
                Object val = data.get(key);
                double numVal = 0;
                if (val instanceof Number) {
                    numVal = ((Number) val).doubleValue();
                } else if (val != null) {
                    try { numVal = Double.parseDouble(val.toString()); } catch (Exception e) { numVal = 0; }
                }
                matcher.appendReplacement(sb, String.valueOf(numVal));
            }
            matcher.appendTail(sb);

            return evalExpr(sb.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    public double evalExpr(String expr) {
        return new Object() {
            int pos = 0;

            double parse() {
                return parseAddSub();
            }

            double parseAddSub() {
                skipSpaces();
                double left = parseMulDiv();
                skipSpaces();
                while (pos < expr.length()) {
                    char op = expr.charAt(pos);
                    if (op == '+' || op == '-') {
                        pos++;
                        skipSpaces();
                        double right = parseMulDiv();
                        left = op == '+' ? left + right : left - right;
                        skipSpaces();
                    } else break;
                }
                return left;
            }

            double parseMulDiv() {
                skipSpaces();
                double left = parseUnary();
                skipSpaces();
                while (pos < expr.length()) {
                    char op = expr.charAt(pos);
                    if (op == '*' || op == '/') {
                        pos++;
                        skipSpaces();
                        double right = parseUnary();
                        left = op == '*' ? left * right : left / right;
                        skipSpaces();
                    } else break;
                }
                return left;
            }

            double parseUnary() {
                skipSpaces();
                if (pos < expr.length() && expr.charAt(pos) == '-') {
                    pos++;
                    return -parseUnary();
                }
                return parsePrimary();
            }

            double parsePrimary() {
                skipSpaces();
                for (String fn : new String[]{"CEIL", "FLOOR", "ROUND", "ABS"}) {
                    if (pos + fn.length() <= expr.length() && expr.substring(pos, pos + fn.length()).equals(fn)) {
                        pos += fn.length();
                        skipSpaces();
                        if (pos < expr.length() && expr.charAt(pos) == '(') {
                            pos++;
                            double val = parseAddSub();
                            double decimals = 0;
                            skipSpaces();
                            if (pos < expr.length() && expr.charAt(pos) == ',') {
                                pos++;
                                decimals = parseAddSub();
                            }
                            skipSpaces();
                            if (pos < expr.length() && expr.charAt(pos) == ')') pos++;
                            switch (fn) {
                                case "CEIL": return Math.ceil(val);
                                case "FLOOR": return Math.floor(val);
                                case "ABS": return Math.abs(val);
                                case "ROUND": {
                                    double factor = Math.pow(10, decimals);
                                    return Math.round(val * factor) / factor;
                                }
                                default: return val;
                            }
                        }
                    }
                }
                if (pos < expr.length() && expr.charAt(pos) == '(') {
                    pos++;
                    double val = parseAddSub();
                    skipSpaces();
                    if (pos < expr.length() && expr.charAt(pos) == ')') pos++;
                    return val;
                }
                int start = pos;
                while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
                    pos++;
                }
                if (start == pos) return 0;
                return Double.parseDouble(expr.substring(start, pos));
            }

            void skipSpaces() {
                while (pos < expr.length() && expr.charAt(pos) == ' ') pos++;
            }
        }.parse();
    }
}
