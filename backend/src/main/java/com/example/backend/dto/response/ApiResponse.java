package com.example.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standard API response envelope used across all endpoints.
 * Matches the SDD specification:
 * { success, data, error: { code, message, details }, timestamp }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorPayload error;
    private String timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now().toString();
    }

    private ApiResponse(boolean success, T data, ErrorPayload error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = Instant.now().toString();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorPayload(code, message, null));
    }

    public static <T> ApiResponse<T> fail(String code, String message, Object details) {
        return new ApiResponse<>(false, null, new ErrorPayload(code, message, details));
    }

    // Getters
    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public ErrorPayload getError() { return error; }
    public String getTimestamp() { return timestamp; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setData(T data) { this.data = data; }
    public void setError(ErrorPayload error) { this.error = error; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorPayload {
        private String code;
        private String message;
        private Object details;

        public ErrorPayload() {}

        public ErrorPayload(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
        public Object getDetails() { return details; }

        public void setCode(String code) { this.code = code; }
        public void setMessage(String message) { this.message = message; }
        public void setDetails(Object details) { this.details = details; }
    }
}
