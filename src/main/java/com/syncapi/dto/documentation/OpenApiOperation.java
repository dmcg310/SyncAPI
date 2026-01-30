package com.syncapi.dto.documentation;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * DTO representing an OpenAPI operation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenApiOperation {
    private String summary;
    private String description;
    private List<OpenApiParameter> parameters;
    private OpenApiRequestBody requestBody;
    private Map<String, OpenApiResponse> responses;

    /**
     * Default constructor.
     */
    public OpenApiOperation() {
    }

    /**
     * Parameterized constructor.
     *
     * @param summary     the summary of the operation
     * @param description the description of the operation
     * @param parameters  the list of parameters
     * @param requestBody the request body
     * @param responses   the map of responses
     */
    public OpenApiOperation(String summary, String description, List<OpenApiParameter> parameters,
                            OpenApiRequestBody requestBody, Map<String, OpenApiResponse> responses) {
        this.summary = summary;
        this.description = description;
        this.parameters = parameters;
        this.requestBody = requestBody;
        this.responses = responses;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<OpenApiParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<OpenApiParameter> parameters) {
        this.parameters = parameters;
    }

    public OpenApiRequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(OpenApiRequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, OpenApiResponse> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, OpenApiResponse> responses) {
        this.responses = responses;
    }
}
