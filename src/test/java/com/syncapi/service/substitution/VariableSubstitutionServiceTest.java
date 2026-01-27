package com.syncapi.service.substitution;

import com.syncapi.dto.request.SubstitutedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VariableSubstitutionServiceTest {
    private VariableSubstitutionService substitutionService;

    @BeforeEach
    void setUp() {
        substitutionService = new VariableSubstitutionService();
    }

    @Test
    void shouldSubstituteSingleVariable() {
        // given
        String input = "Hello, {{name}}!";
        Map<String, String> variables = Map.of("name", "World");

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void shouldSubstituteMultipleVariables() {
        // given
        String input = "{{greeting}}, {{name}}!";
        Map<String, String> variables = Map.of("greeting", "Hello", "name", "World");

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void shouldSubstituteSameVariableMultipleTimes() {
        // given
        String input = "{{name}} likes {{name}}";
        Map<String, String> variables = Map.of("name", "Bob");

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("Bob likes Bob");
    }

    @Test
    void shouldLeaveUnknownVariablesUnchanged() {
        // given
        String input = "Hello, {{unknown}}!";
        Map<String, String> variables = Map.of("name", "World");

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("Hello, {{unknown}}!");
    }

    @Test
    void shouldReturnNullForNullInput() {
        // given
        Map<String, String> variables = Map.of("name", "World");

        // when
        String result = substitutionService.substitute(null, variables);

        // then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnEmptyForEmptyInput() {
        // given
        Map<String, String> variables = Map.of("name", "World");

        // when
        String result = substitutionService.substitute("", variables);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnInputUnchangedWhenVariablesNull() {
        // given
        String input = "Hello, {{name}}!";

        // when
        String result = substitutionService.substitute(input, null);

        // then
        assertThat(result).isEqualTo("Hello, {{name}}!");
    }

    @Test
    void shouldReturnInputUnchangedWhenVariablesEmpty() {
        // given
        String input = "Hello, {{name}}!";
        Map<String, String> variables = Map.of();

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("Hello, {{name}}!");
    }

    @Test
    void shouldReturnInputUnchangedWhenNoVariablesInString() {
        // given
        String input = "Hello, World!";
        Map<String, String> variables = Map.of("name", "Bob");

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    void shouldHandleSpecialCharactersInValues() {
        // given
        String input = "Price: {{price}}";
        Map<String, String> variables = Map.of("price", "$100");

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("Price: $100");
    }

    @Test
    void shouldHandleUrlWithVariables() {
        // given
        String input = "https://api.example.com/users/{{userId}}/posts/{{postId}}";
        Map<String, String> variables = Map.of("userId", "123", "postId", "456");

        // when
        String result = substitutionService.substitute(input, variables);

        // then
        assertThat(result).isEqualTo("https://api.example.com/users/123/posts/456");
    }

    @Test
    void shouldSubstituteUrl() {
        // given
        String url = "https://api.example.com/{{resource}}";
        Map<String, String> variables = Map.of("resource", "users");

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, null, null, variables);

        // then
        assertThat(result.getUrl()).isEqualTo("https://api.example.com/users");
    }

    @Test
    void shouldSubstituteHeaders() {
        // given
        String url = "https://api.example.com";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer {{token}}");
        headers.put("X-Api-Key", "{{apiKey}}");
        Map<String, String> variables = Map.of("token", "abc123", "apiKey", "secret");

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, headers, null, variables);

        // then
        assertThat(result.getHeaders())
                .containsEntry("Authorization", "Bearer abc123")
                .containsEntry("X-Api-Key", "secret");
    }

    @Test
    void shouldSubstituteBodyStringValues() {
        // given
        String url = "https://api.example.com";
        Map<String, Object> body = new HashMap<>();
        body.put("username", "{{username}}");
        body.put("email", "{{email}}");
        body.put("count", 42);
        Map<String, String> variables = Map.of("username", "john", "email", "john@example.com");

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, null, body, variables);

        // then
        assertThat(result.getBody())
                .containsEntry("username", "john")
                .containsEntry("email", "john@example.com")
                .containsEntry("count", 42);
    }

    @Test
    void shouldHandleNullHeaders() {
        // given
        String url = "https://api.example.com";
        Map<String, String> variables = Map.of("key", "value");

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, null, null, variables);

        // then
        assertThat(result.getHeaders()).isNull();
    }

    @Test
    void shouldHandleNullBody() {
        // given
        String url = "https://api.example.com";
        Map<String, String> variables = Map.of("key", "value");

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, null, null, variables);

        // then
        assertThat(result.getBody()).isNull();
    }

    @Test
    void shouldHandleEmptyHeaders() {
        // given
        String url = "https://api.example.com";
        Map<String, String> headers = Map.of();
        Map<String, String> variables = Map.of("key", "value");

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, headers, null, variables);

        // then
        assertThat(result.getHeaders()).isEmpty();
    }

    @Test
    void shouldHandleEmptyBody() {
        // given
        String url = "https://api.example.com";
        Map<String, Object> body = Map.of();
        Map<String, String> variables = Map.of("key", "value");

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, null, body, variables);

        // then
        assertThat(result.getBody()).isEmpty();
    }

    @Test
    void shouldSubstituteAllPartsOfRequest() {
        // given
        String url = "https://{{host}}/api/{{version}}/users";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer {{token}}");
        Map<String, Object> body = new HashMap<>();
        body.put("name", "{{userName}}");
        body.put("active", true);

        Map<String, String> variables = Map.of(
                "host", "api.example.com",
                "version", "v1",
                "token", "xyz789",
                "userName", "Alice"
        );

        // when
        SubstitutedRequest result = substitutionService.substituteRequest(url, headers, body, variables);

        // then
        assertThat(result.getUrl()).isEqualTo("https://api.example.com/api/v1/users");
        assertThat(result.getHeaders()).containsEntry("Authorization", "Bearer xyz789");
        assertThat(result.getBody())
                .containsEntry("name", "Alice")
                .containsEntry("active", true);
    }
}
