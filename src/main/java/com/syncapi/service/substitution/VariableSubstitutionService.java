package com.syncapi.service.substitution;

import com.syncapi.dto.request.SubstitutedRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for variable substitution in strings and requests.
 */
@Service
public class VariableSubstitutionService {
    /**
     * Substitutes variables in the input string based on the provided variables.
     * <p>
     * For example, substitute("Hello, {{name}}!", Map.of("name", "World")) returns "Hello, World!"
     * <p>
     * Variables not found in the map are left unchanged.
     *
     * @param input     the input string containing {{key}} patterns
     * @param variables the map of variable keys and their corresponding values
     * @return the input string with variables substituted
     */
    public String substitute(String input, Map<String, String> variables) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (variables == null || variables.isEmpty()) {
            return input;
        }

        String result = input;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String token = "{{" + entry.getKey() + "}}";

            String value = entry.getValue();
            if (value != null) {
                result = result.replace(token, value);
            }
        }

        return result;
    }

    /**
     * Substitutes variables in the Request's URL, headers, and body.
     * <p>
     * Returns a new SubstitutedRequest DTO to avoid mutating the JPA entity.
     *
     * @param url       the request URL
     * @param headers   the request headers
     * @param body      the request body
     * @param variables the map of variable keys and their corresponding values
     * @return a new SubstitutedRequest with variables substituted
     */
    public SubstitutedRequest substituteRequest(String url, Map<String, String> headers, Map<String, Object> body,
                                                Map<String, String> variables) {
        String substitutedUrl = substitute(url, variables);
        Map<String, String> substitutedHeaders = substituteHeaders(headers, variables);
        Map<String, Object> substitutedBody = substituteBody(body, variables);

        return new SubstitutedRequest(substitutedUrl, substitutedHeaders, substitutedBody);
    }

    /**
     * Substitutes variables in header values.
     *
     * @param headers   the request headers
     * @param variables the map of variable keys and their corresponding values
     * @return a new map with substituted header values
     */
    private Map<String, String> substituteHeaders(Map<String, String> headers, Map<String, String> variables) {
        if (headers == null || headers.isEmpty()) {
            return headers;
        }

        Map<String, String> substitutedHeaders = new HashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String substitutedValue = substitute(entry.getValue(), variables);
            substitutedHeaders.put(entry.getKey(), substitutedValue);
        }

        return substitutedHeaders;
    }

    /**
     * Substitutes variables in body values (only for String values).
     *
     * @param body      the request body
     * @param variables the map of variable keys and their corresponding values
     * @return a new map with substituted body values
     */
    private Map<String, Object> substituteBody(Map<String, Object> body, Map<String, String> variables) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        Map<String, Object> substitutedBody = new HashMap<>();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                substitutedBody.put(entry.getKey(), substitute((String) value, variables));
            } else {
                substitutedBody.put(entry.getKey(), value);
            }
        }

        return substitutedBody;
    }
}