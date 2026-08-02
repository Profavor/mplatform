package com.classification.domain_system.dto;

public class AsyncBatchDto {

    public static class BatchTaskResponse {
        private String taskId;
        private String status; // PROCESSING, COMPLETED, FAILED
        private int progressPercent;
        private long processedCount;
        private long totalCount;
        private String downloadUrl;

        public BatchTaskResponse() {}

        public BatchTaskResponse(String taskId, String status, int progressPercent, long processedCount, long totalCount) {
            this.taskId = taskId;
            this.status = status;
            this.progressPercent = progressPercent;
            this.processedCount = processedCount;
            this.totalCount = totalCount;
        }

        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getProgressPercent() { return progressPercent; }
        public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

        public long getProcessedCount() { return processedCount; }
        public void setProcessedCount(long processedCount) { this.processedCount = processedCount; }

        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    }

    public static class ExportAsyncRequest {
        private java.util.UUID domainId;
        private String format;
        private java.util.List<java.util.Map<String, String>> columns = new java.util.ArrayList<>();
        private java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();

        public java.util.UUID getDomainId() { return domainId; }
        public void setDomainId(java.util.UUID domainId) { this.domainId = domainId; }

        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }

        public java.util.List<java.util.Map<String, String>> getColumns() { return columns; }
        public void setColumns(java.util.List<java.util.Map<String, String>> columns) { this.columns = columns; }

        public java.util.List<java.util.Map<String, Object>> getRecords() { return records; }
        public void setRecords(java.util.List<java.util.Map<String, Object>> records) { this.records = records; }
    }
}
