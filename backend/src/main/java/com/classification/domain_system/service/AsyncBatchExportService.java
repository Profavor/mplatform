package com.classification.domain_system.service;

import com.classification.domain_system.dto.AsyncBatchDto;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.Record;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.RecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.classification.domain_system.entity.enums.RecordStatus;

@Service
@Slf4j
public class AsyncBatchExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RecordRepository recordRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;

    private final Map<String, AsyncBatchDto.BatchTaskResponse> taskMap = new ConcurrentHashMap<>();
    private final Map<String, UUID> taskDomainMap = new ConcurrentHashMap<>();
    private final Map<String, AsyncBatchDto.ExportAsyncRequest> taskGridDataMap = new ConcurrentHashMap<>();
    private final Map<String, byte[]> taskFileResultMap = new ConcurrentHashMap<>();

    public AsyncBatchExportService(RecordRepository recordRepository,
                                  FieldDefinitionRepository fieldDefinitionRepository) {
        this.recordRepository = recordRepository;
        this.fieldDefinitionRepository = fieldDefinitionRepository;
    }

    public AsyncBatchDto.BatchTaskResponse startAsyncExport(UUID domainId, String format) {
        return startAsyncExportWithData(domainId, format, null);
    }

    public AsyncBatchDto.BatchTaskResponse startAsyncExportWithData(UUID domainId, String format, AsyncBatchDto.ExportAsyncRequest request) {
        String taskId = UUID.randomUUID().toString();
        long totalCount = 0;

        if (request != null && request.getRecords() != null && !request.getRecords().isEmpty()) {
            totalCount = request.getRecords().size();
            taskGridDataMap.put(taskId, request);
        } else if (domainId != null) {
            totalCount = recordRepository.countByNodeDomainIdAndStatus(domainId, RecordStatus.ACTIVE.name());
            taskDomainMap.put(taskId, domainId);
        }

        AsyncBatchDto.BatchTaskResponse task = new AsyncBatchDto.BatchTaskResponse(
                taskId, "PROCESSING", 0, 0, totalCount
        );
        taskMap.put(taskId, task);

        processExportAsync(taskId, totalCount);

        return task;
    }

    @Async
    public void processExportAsync(String taskId, long totalCount) {
        AsyncBatchDto.BatchTaskResponse task = taskMap.get(taskId);
        if (task == null) return;

        try {
            byte[] fileBytes;
            AsyncBatchDto.ExportAsyncRequest gridData = taskGridDataMap.get(taskId);
            if (gridData != null && gridData.getRecords() != null && !gridData.getRecords().isEmpty()) {
                fileBytes = generateExcelFromGridData(gridData);
            } else {
                fileBytes = generateStreamingExcelForDomain(taskId, task, totalCount);
            }

            taskFileResultMap.put(taskId, fileBytes);
            task.setProcessedCount(totalCount);
            task.setProgressPercent(100);
            task.setStatus("COMPLETED");
            task.setDownloadUrl("/api/batch/download/" + taskId);
        } catch (Exception e) {
            log.error("Failed to generate async batch export file for task: {}", taskId, e);
            task.setStatus("FAILED");
        }
    }

    public AsyncBatchDto.BatchTaskResponse getTaskStatus(String taskId) {
        AsyncBatchDto.BatchTaskResponse task = taskMap.get(taskId);
        if (task == null) {
            return new AsyncBatchDto.BatchTaskResponse(taskId, "NOT_FOUND", 0, 0, 0);
        }
        return task;
    }

    public byte[] downloadTaskFile(String taskId) {
        byte[] cachedBytes = taskFileResultMap.get(taskId);
        if (cachedBytes != null) {
            return cachedBytes;
        }

        AsyncBatchDto.ExportAsyncRequest gridData = taskGridDataMap.get(taskId);
        if (gridData != null && gridData.getRecords() != null && !gridData.getRecords().isEmpty()) {
            byte[] bytes = generateExcelFromGridData(gridData);
            taskFileResultMap.put(taskId, bytes);
            return bytes;
        }

        UUID domainId = taskDomainMap.get(taskId);
        AsyncBatchDto.BatchTaskResponse task = taskMap.get(taskId);
        long totalCount = task != null ? task.getTotalCount() : 0;
        byte[] bytes = generateStreamingExcelForDomain(taskId, task, totalCount);
        taskFileResultMap.put(taskId, bytes);
        return bytes;
    }


    private byte[] generateStreamingExcelForDomain(String taskId, AsyncBatchDto.BatchTaskResponse task, long totalCount) {
        UUID domainId = taskDomainMap.get(taskId);
        List<Record> records = (domainId != null) ? recordRepository.findAllByDomainId(domainId) : recordRepository.findAll();
        List<FieldDefinition> fieldDefs = (domainId != null) ? fieldDefinitionRepository.findDomainFieldsWithSort(domainId) : Collections.emptyList();

        // Use SXSSFWorkbook with row window 100 for true memory-efficient streaming export
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Master Data Export");

            // Title Style
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 15);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("📊 Master Data Governance & Batch Export Report");
            titleCell.setCellStyle(titleStyle);

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Styles
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            CellStyle zebraStyle = workbook.createCellStyle();
            zebraStyle.cloneStyleFrom(dataStyle);
            zebraStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            zebraStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Dynamic Column Headers Construction
            List<String> headerTitles = new ArrayList<>();
            headerTitles.add("No");
            headerTitles.add("Record Code");

            List<String> dynamicFieldKeys = new ArrayList<>();
            for (FieldDefinition fd : fieldDefs) {
                if (fd.getKey() != null && !fd.getKey().isBlank()) {
                    dynamicFieldKeys.add(fd.getKey());
                    String label = extractFieldLabel(fd);
                    headerTitles.add(label);
                }
            }

            // Fallback default keys if domain has no explicit field definitions
            if (dynamicFieldKeys.isEmpty()) {
                dynamicFieldKeys.addAll(List.of("EMP_NO", "NAME", "JOIN_DATE", "PLANT"));
                headerTitles.addAll(List.of("사번 (Employee No)", "성명 (Name)", "입사일 (Join Date)", "공장 (Plant)"));
            }

            headerTitles.add("Status");
            headerTitles.add("Node Name");
            headerTitles.add("Updated At");

            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < headerTitles.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerTitles.get(i));
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 3;
            int sequenceIndex = 1;
            Map<String, FieldDefinition> fieldDefMap = new HashMap<>();
            for (FieldDefinition fd : fieldDefs) {
                if (fd.getKey() != null) {
                    fieldDefMap.put(fd.getKey().toLowerCase(), fd);
                }
            }

            for (Record record : records) {
                Row row = sheet.createRow(rowNum++);
                boolean isEven = rowNum % 2 == 0;
                CellStyle currentStyle = isEven ? zebraStyle : dataStyle;

                int colIdx = 0;

                Cell seqCell = row.createCell(colIdx++);
                seqCell.setCellValue(sequenceIndex++);
                seqCell.setCellStyle(currentStyle);

                Cell codeCell = row.createCell(colIdx++);
                String codeStr = (record.getId() != null) ? "REC-" + record.getId().toString().substring(0, 8) : "REC-00000000";
                codeCell.setCellValue(codeStr);
                codeCell.setCellStyle(currentStyle);

                JsonNode dataJson = parseRecordDataJson(record.getData());
                for (String fieldKey : dynamicFieldKeys) {
                    Cell dataCell = row.createCell(colIdx++);
                    JsonNode nodeVal = (dataJson != null && dataJson.has(fieldKey)) ? dataJson.get(fieldKey) : null;
                    FieldDefinition fd = fieldDefMap.get(fieldKey.toLowerCase());
                    dataCell.setCellValue(formatFieldValue(nodeVal, fd));
                    dataCell.setCellStyle(currentStyle);
                }

                Cell statusCell = row.createCell(colIdx++);
                statusCell.setCellValue(record.getStatus() != null ? record.getStatus() : RecordStatus.ACTIVE.name());
                statusCell.setCellStyle(currentStyle);

                Cell nodeCell = row.createCell(colIdx++);
                String nodeName = (record.getNode() != null && record.getNode().getName() != null)
                        ? extractNameFromObj(record.getNode().getName())
                        : "General";
                nodeCell.setCellValue(nodeName);
                nodeCell.setCellStyle(currentStyle);

                Cell updatedCell = row.createCell(colIdx++);
                updatedCell.setCellValue(record.getUpdatedAt() != null ? record.getUpdatedAt().format(DATE_FORMATTER) : "");
                updatedCell.setCellStyle(currentStyle);

                if (task != null && totalCount > 0) {
                    task.setProcessedCount(sequenceIndex - 1);
                    int pct = (int) Math.min(99, ((long) (sequenceIndex - 1) * 100) / totalCount);
                    task.setProgressPercent(pct);
                }
            }

            for (int i = 0; i < headerTitles.size(); i++) {
                try { sheet.autoSizeColumn(i); } catch (Exception ignored) {}
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1024, 4500));
            }

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate streaming Excel export file", e);
        }
    }


    private JsonNode parseRecordDataJson(String dataStr) {
        if (dataStr == null || dataStr.isBlank()) return null;
        try {
            return objectMapper.readTree(dataStr);
        } catch (Exception e) {
            return null;
        }
    }

    private static class SubColInfo {
        String label;
        Map<String, String> optionLabels = new HashMap<>();
    }

    private Map<String, SubColInfo> extractSubColumns(FieldDefinition fd) {
        Map<String, SubColInfo> map = new HashMap<>();
        if (fd == null || fd.getOptions() == null || fd.getOptions().isBlank()) return map;
        try {
            JsonNode optNode = objectMapper.readTree(fd.getOptions());
            JsonNode cols = null;
            if (optNode.has("tableSchema") && optNode.get("tableSchema").has("columns")) {
                cols = optNode.get("tableSchema").get("columns");
            } else if (optNode.has("tableColumns")) {
                cols = optNode.get("tableColumns");
            } else if (optNode.has("columns")) {
                cols = optNode.get("columns");
            } else if (optNode.isArray()) {
                cols = optNode;
            }
            if (cols != null && cols.isArray()) {
                for (JsonNode col : cols) {
                    if (col.has("key")) {
                        String key = col.get("key").asText();
                        SubColInfo info = new SubColInfo();
                        info.label = key;
                        if (col.has("name")) {
                            JsonNode nameNode = col.get("name");
                            if (nameNode.isObject()) {
                                if (nameNode.has("ko")) info.label = nameNode.get("ko").asText();
                                else if (nameNode.has("en")) info.label = nameNode.get("en").asText();
                            } else if (nameNode.isTextual()) {
                                info.label = nameNode.asText();
                            }
                        }
                        if (col.has("options")) {
                            JsonNode optsNode = col.get("options");
                            if (optsNode.isTextual()) {
                                try { optsNode = objectMapper.readTree(optsNode.asText()); } catch (Exception ignored) {}
                            }
                            if (optsNode != null && optsNode.isArray()) {
                                for (JsonNode opt : optsNode) {
                                    String optVal = opt.has("value") ? opt.get("value").asText() : (opt.has("key") ? opt.get("key").asText() : null);
                                    if (optVal != null) {
                                        String optLabel = optVal;
                                        if (opt.has("label")) {
                                            JsonNode lNode = opt.get("label");
                                            if (lNode.isObject()) {
                                                if (lNode.has("ko")) optLabel = lNode.get("ko").asText();
                                                else if (lNode.has("en")) optLabel = lNode.get("en").asText();
                                            } else if (lNode.isTextual()) {
                                                optLabel = lNode.asText();
                                            }
                                        } else if (opt.has("name")) {
                                            JsonNode nNode = opt.get("name");
                                            if (nNode.isObject()) {
                                                if (nNode.has("ko")) optLabel = nNode.get("ko").asText();
                                                else if (nNode.has("en")) optLabel = nNode.get("en").asText();
                                            } else if (nNode.isTextual()) {
                                                optLabel = nNode.asText();
                                            }
                                        }
                                        info.optionLabels.put(optVal.toLowerCase(), optLabel);
                                    }
                                }
                            }
                        }
                        map.put(key.toLowerCase(), info);
                    }
                }
            }
        } catch (Exception ignored) {}
        return map;
    }

    private String formatFieldValue(JsonNode nodeVal, FieldDefinition fd) {
        if (nodeVal == null || nodeVal.isNull()) return "";
        Map<String, SubColInfo> subCols = extractSubColumns(fd);
        if (nodeVal.isArray()) {
            List<String> items = new ArrayList<>();
            int idx = 1;
            boolean isSubTable = false;
            for (JsonNode elem : nodeVal) {
                if (elem.isObject()) {
                    isSubTable = true;
                    Iterator<Map.Entry<String, JsonNode>> fields = elem.fields();
                    List<String> fieldPairs = new ArrayList<>();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> f = fields.next();
                        SubColInfo colInfo = subCols.get(f.getKey().toLowerCase());
                        String colLabel = (colInfo != null && colInfo.label != null) ? colInfo.label : f.getKey();
                        String rawVal = formatFieldValue(f.getValue(), null);
                        if (!rawVal.isBlank()) {
                            String displayVal = rawVal;
                            if (colInfo != null && colInfo.optionLabels.containsKey(rawVal.toLowerCase())) {
                                displayVal = colInfo.optionLabels.get(rawVal.toLowerCase());
                            }
                            fieldPairs.add(colLabel + ": " + displayVal);
                        }
                    }
                    items.add(idx++ + ". " + String.join(", ", fieldPairs));
                } else {
                    String str = elem.asText();
                    items.add(extractFileNameIfFileUrl(str));
                }
            }
            return isSubTable ? String.join("\n", items) : String.join(", ", items);
        }
        if (nodeVal.isObject()) {
            if (nodeVal.has("ko") || nodeVal.has("en")) {
                String ko = nodeVal.has("ko") ? nodeVal.get("ko").asText() : "";
                String en = nodeVal.has("en") ? nodeVal.get("en").asText() : "";
                if (!ko.isEmpty() && !en.isEmpty() && !ko.equals(en)) return ko + " (" + en + ")";
                return !ko.isEmpty() ? ko : en;
            }
            List<String> pairs = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = nodeVal.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> f = fields.next();
                SubColInfo colInfo = subCols.get(f.getKey().toLowerCase());
                String colLabel = (colInfo != null && colInfo.label != null) ? colInfo.label : f.getKey();
                String rawVal = formatFieldValue(f.getValue(), null);
                String displayVal = rawVal;
                if (colInfo != null && colInfo.optionLabels.containsKey(rawVal.toLowerCase())) {
                    displayVal = colInfo.optionLabels.get(rawVal.toLowerCase());
                }
                pairs.add(colLabel + ": " + displayVal);
            }
            return String.join(", ", pairs);
        }
        String text = nodeVal.asText();
        return cleanStringValue(text);
    }

    private String formatFieldValue(JsonNode nodeVal) {
        return formatFieldValue(nodeVal, null);
    }

    private String cleanStringValue(String val) {
        if (val == null || val.isBlank()) return "";
        String trimmed = val.trim();
        if ((trimmed.startsWith("[") && trimmed.endsWith("]")) || (trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            try {
                JsonNode parsed = objectMapper.readTree(trimmed);
                return formatFieldValue(parsed, null);
            } catch (Exception ignored) {}
        }
        // If File URL
        String fileName = extractFileNameIfFileUrl(trimmed);
        if (!fileName.equals(trimmed)) return fileName;

        // If UUID
        if (trimmed.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            return "REC-" + trimmed.substring(0, 8);
        }

        return val;
    }

    private String extractFileNameIfFileUrl(String input) {
        if (input == null || input.isBlank()) return "";
        if (input.contains("/api/files/download/") || input.contains("?name=") || input.contains("?filename=")) {
            try {
                if (input.contains("?name=")) {
                    String part = input.substring(input.indexOf("?name=") + 6);
                    if (part.contains("&")) part = part.substring(0, part.indexOf("&"));
                    return java.net.URLDecoder.decode(part, java.nio.charset.StandardCharsets.UTF_8);
                }
                if (input.contains("?filename=")) {
                    String part = input.substring(input.indexOf("?filename=") + 10);
                    if (part.contains("&")) part = part.substring(0, part.indexOf("&"));
                    return java.net.URLDecoder.decode(part, java.nio.charset.StandardCharsets.UTF_8);
                }
                String path = input.contains("?") ? input.substring(0, input.indexOf("?")) : input;
                String fname = path.substring(path.lastIndexOf('/') + 1);
                if (!fname.isBlank()) {
                    return java.net.URLDecoder.decode(fname, java.nio.charset.StandardCharsets.UTF_8);
                }
            } catch (Exception ignored) {}
        }
        return input;
    }

    private String extractFieldLabel(FieldDefinition fd) {
        if (fd.getName() != null && !fd.getName().isEmpty()) {
            return extractNameFromObj(fd.getName()) + " (" + fd.getKey() + ")";
        }
        return fd.getKey();
    }

    private String extractNameFromObj(Object nameObj) {
        if (nameObj == null) return "";
        if (nameObj instanceof String) return (String) nameObj;
        if (nameObj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) nameObj;
            if (map.containsKey("ko")) return String.valueOf(map.get("ko"));
            if (map.containsKey("en")) return String.valueOf(map.get("en"));
            if (!map.isEmpty()) return String.valueOf(map.values().iterator().next());
        }
        return String.valueOf(nameObj);
    }

    private byte[] generateExcelFromGridData(AsyncBatchDto.ExportAsyncRequest request) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("AG-Grid Export");

            List<Map<String, String>> columns = request.getColumns();
            List<Map<String, Object>> records = request.getRecords();

            int totalCols = Math.max(columns.size(), 1);

            // Title Style
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 15);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("📊 Master Data Governance & Batch Export Report");
            titleCell.setCellStyle(titleStyle);

            // Merge Title Row across all columns
            if (totalCols > 1) {
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, totalCols - 1));
            }

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Style (Regular & Zebra)
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setWrapText(true);

            CellStyle zebraStyle = workbook.createCellStyle();
            zebraStyle.cloneStyleFrom(dataStyle);
            zebraStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            zebraStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            zebraStyle.setWrapText(true);

            // Render Header (Row 2) - Exactly matching AG-Grid column order
            Row headerRow = sheet.createRow(2);
            for (int i = 0; i < columns.size(); i++) {
                Map<String, String> col = columns.get(i);
                String headerText = col.getOrDefault("headerName", col.getOrDefault("field", "Col " + (i + 1)));
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headerText);
                cell.setCellStyle(headerStyle);
            }

            // Render Rows - Exactly matching AG-Grid row data
            int rowNum = 3;
            for (Map<String, Object> record : records) {
                Row row = sheet.createRow(rowNum++);
                boolean isEven = rowNum % 2 == 0;
                CellStyle currentStyle = isEven ? zebraStyle : dataStyle;

                for (int i = 0; i < columns.size(); i++) {
                    Map<String, String> col = columns.get(i);
                    String fieldKey = col.get("field");
                    Object val = extractGridRecordValue(record, fieldKey);

                    Cell cell = row.createCell(i);
                    if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                    } else {
                        cell.setCellValue(formatFieldValueObject(val));
                    }
                    cell.setCellStyle(currentStyle);
                }
            }

            // Auto-size all columns
            for (int i = 0; i < totalCols; i++) {
                try { sheet.autoSizeColumn(i); } catch (Exception ignored) {}
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1024, 4500));
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel from grid data", e);
        }
    }

    private Object extractGridRecordValue(Map<String, Object> record, String fieldKey) {
        if (record == null || fieldKey == null) return "";

        // Case 1: Direct key lookup (e.g. id, nodeName, status, updatedAt)
        if (record.containsKey(fieldKey)) {
            return record.get(fieldKey);
        }

        // Case 2: Nested data prefix lookup (e.g. data.EMP_NO -> record.data.EMP_NO or record.EMP_NO)
        if (fieldKey.startsWith("data.")) {
            String subKey = fieldKey.substring(5);
            if (record.containsKey(subKey)) {
                return record.get(subKey);
            }
            if (record.get("data") instanceof Map) {
                Map<?, ?> dataMap = (Map<?, ?>) record.get("data");
                if (dataMap.containsKey(subKey)) {
                    return dataMap.get(subKey);
                }
            }
        }

        // Case 3: Case-insensitive fallback
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(fieldKey)) {
                return entry.getValue();
            }
        }

        return "";
    }

    private String formatFieldValueObject(Object val) {
        if (val == null) return "";
        if (val instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) val;
            if (map.containsKey("ko") || map.containsKey("en")) {
                String ko = map.containsKey("ko") ? String.valueOf(map.get("ko")) : "";
                String en = map.containsKey("en") ? String.valueOf(map.get("en")) : "";
                if (!ko.isEmpty() && !en.isEmpty() && !ko.equals(en)) return ko + " (" + en + ")";
                return !ko.isEmpty() ? ko : en;
            }
            List<String> pairs = new ArrayList<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                pairs.add(entry.getKey() + ": " + formatFieldValueObject(entry.getValue()));
            }
            return String.join(", ", pairs);
        }
        if (val instanceof List) {
            List<?> list = (List<?>) val;
            List<String> items = new ArrayList<>();
            int idx = 1;
            boolean isSubTable = false;
            for (Object elem : list) {
                if (elem instanceof Map) {
                    isSubTable = true;
                    List<String> pairs = new ArrayList<>();
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) elem).entrySet()) {
                        pairs.add(entry.getKey() + ": " + formatFieldValueObject(entry.getValue()));
                    }
                    items.add(idx++ + ". " + String.join(", ", pairs));
                } else {
                    items.add(cleanStringValue(String.valueOf(elem)));
                }
            }
            return isSubTable ? String.join("\n", items) : String.join(", ", items);
        }
        return cleanStringValue(String.valueOf(val));
    }
}
