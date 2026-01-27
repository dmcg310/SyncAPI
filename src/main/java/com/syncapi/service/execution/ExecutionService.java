package com.syncapi.service.execution;

import com.syncapi.dto.execution.ExecutionResponse;
import com.syncapi.dto.request.SubstitutedRequest;
import com.syncapi.entity.request.Request;
import com.syncapi.service.substitution.VariableSubstitutionService;
import com.syncapi.util.RequestMethod;
import com.syncapi.util.Util;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for executing requests.
 */
@Service
public class ExecutionService {
    private final VariableSubstitutionService variableSubstitutionService;
    private final RestTemplate restTemplate;
    private final Util util;

    /**
     * Parameterized constructor.
     *
     * @param variableSubstitutionService the variable substitution service
     * @param restTemplate                the REST template
     * @param util                        the utility service
     */
    public ExecutionService(VariableSubstitutionService variableSubstitutionService, RestTemplate restTemplate,
                            Util util) {
        this.variableSubstitutionService = variableSubstitutionService;
        this.restTemplate = restTemplate;
        this.util = util;
    }

    /**
     * Executes a request by ID.
     *
     * @param requestId the request ID
     * @param email     the user's email
     * @return the execution response
     */
    public ExecutionResponse execute(Long requestId, String email) {
        Map<String, String> variables = util.getActiveEnvironmentByRequestId(requestId, email)
                .map(util::getVariablesFromEnvironment)
                .orElse(Map.of());

        return executeInternal(util.getRequestWithAccessCheck(requestId, email), variables);
    }

    /**
     * Executes the request with variable substitution.
     *
     * @param request   the request entity
     * @param variables the variables to substitute
     * @return the execution response
     */
    private ExecutionResponse executeInternal(Request request, Map<String, String> variables) {
        SubstitutedRequest substitutedRequest = variableSubstitutionService.substituteRequest(request.getUrl(),
                request.getHeaders(), request.getBody(), variables);

        HttpEntity<Object> httpEntity = new HttpEntity<>(substitutedRequest.getBody(),
                buildHeaders(substitutedRequest.getHeaders()));

        long startTime = System.currentTimeMillis();

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    substitutedRequest.getUrl(),
                    mapMethod(request.getMethod()),
                    httpEntity,
                    Object.class
            );

            long responseTimeMs = System.currentTimeMillis() - startTime;

            return buildSuccessResponse(response, responseTimeMs);
        } catch (HttpStatusCodeException e) {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            return buildErrorStatusResponse(e, responseTimeMs);
        } catch (RestClientException e) {
            long responseTimeMs = System.currentTimeMillis() - startTime;
            return buildFailureResponse(e, responseTimeMs);
        }
    }

    /**
     * Builds HttpHeaders from a map.
     *
     * @param headersMap the headers map
     * @return the HttpHeaders
     */
    private HttpHeaders buildHeaders(Map<String, String> headersMap) {
        HttpHeaders headers = new HttpHeaders();
        if (headersMap != null) {
            headersMap.forEach(headers::set);
        }

        return headers;
    }

    /**
     * Maps RequestMethod enum to Spring HttpMethod.
     *
     * @param method the request method
     * @return the HTTP method
     */
    private HttpMethod mapMethod(RequestMethod method) {
        return switch (method) {
            case GET -> HttpMethod.GET;
            case POST -> HttpMethod.POST;
            case PUT -> HttpMethod.PUT;
            case PATCH -> HttpMethod.PATCH;
            case DELETE -> HttpMethod.DELETE;
        };
    }

    /**
     * Extracts response headers as a simple map.
     *
     * @param headers the HTTP headers
     * @return the headers map
     */
    private Map<String, String> extractHeaders(HttpHeaders headers) {
        Map<String, String> headerMap = new HashMap<>();

        headers.forEach((key, values) -> {
            if (!values.isEmpty()) {
                headerMap.put(key, values.getFirst());
            }
        });

        return headerMap;
    }

    /**
     * Builds a successful execution response.
     *
     * @param response       the response entity
     * @param responseTimeMs the response time
     * @return the execution response
     */
    private ExecutionResponse buildSuccessResponse(ResponseEntity<Object> response, long responseTimeMs) {
        return new ExecutionResponse(
                response.getStatusCode().value(),
                response.getStatusCode().toString(),
                extractHeaders(response.getHeaders()),
                response.getBody(),
                responseTimeMs,
                true,
                null
        );
    }

    /**
     * Builds a response for HTTP error status codes (4xx, 5xx).
     *
     * @param e              the exception
     * @param responseTimeMs the response time
     * @return the execution response
     */
    private ExecutionResponse buildErrorStatusResponse(HttpStatusCodeException e, long responseTimeMs) {
        return new ExecutionResponse(
                e.getStatusCode().value(),
                e.getStatusCode().toString(),
                extractHeaders(e.getResponseHeaders()),
                e.getResponseBodyAsString(),
                responseTimeMs,
                true,
                null
        );
    }

    /**
     * Builds a response for connection failures.
     *
     * @param e              the exception
     * @param responseTimeMs the response time
     * @return the execution response
     */
    private ExecutionResponse buildFailureResponse(RestClientException e, long responseTimeMs) {
        return new ExecutionResponse(
                0,
                null,
                null,
                null,
                responseTimeMs,
                false,
                e.getMessage()
        );
    }
}
