package com.syncapi.dto.execution;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * DTO for request execution response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExecutionResponse {
    private int statusCode;
    private String statusText;
    private Map<String, String> headers;
    private Object body;
    private long responseTimeMs;
    private boolean success;
    private String errorMessage;

    /**
     * Default constructor.
     */
    public ExecutionResponse() {
    }


    /**
     * Parametrized constructor.
     *
     * @param statusCode     the HTTP status code
     * @param statusText     the HTTP status text
     * @param headers        the response headers
     * @param body           the response body
     * @param responseTimeMs the response time in milliseconds
     * @param success        indicates if the request was successful
     * @param errorMessage   the error message if the request failed
     */
    public ExecutionResponse(int statusCode, String statusText, Map<String, String> headers, Object body,
                             long responseTimeMs, boolean success, String errorMessage) {
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.headers = headers;
        this.body = body;
        this.responseTimeMs = responseTimeMs;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
