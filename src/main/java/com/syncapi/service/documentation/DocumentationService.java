package com.syncapi.service.documentation;

import com.syncapi.dto.documentation.OpenApiInfo;
import com.syncapi.dto.documentation.OpenApiMediaType;
import com.syncapi.dto.documentation.OpenApiOperation;
import com.syncapi.dto.documentation.OpenApiParameter;
import com.syncapi.dto.documentation.OpenApiPathItem;
import com.syncapi.dto.documentation.OpenApiRequestBody;
import com.syncapi.dto.documentation.OpenApiResponse;
import com.syncapi.dto.documentation.OpenApiSchema;
import com.syncapi.dto.documentation.OpenApiServer;
import com.syncapi.dto.documentation.OpenApiSpec;
import com.syncapi.entity.request.Request;
import com.syncapi.entity.workspace.Workspace;
import com.syncapi.repository.folder.FolderRepository;
import com.syncapi.repository.request.RequestRepository;
import com.syncapi.util.RequestMethod;
import com.syncapi.util.Util;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for generating OpenAPI documentation from workspace requests.
 */
@Service
public class DocumentationService {
    private static final String SUCCESS_STATUS_CODE = "200";
    private static final String APPLICATION_JSON = "application/json";

    private static final Pattern PATH_PARAM_PATTERN = Pattern.compile("\\{([^}]+)}");
    private static final Pattern NUMERIC_SEGMENT_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern VARIABLE_PLACEHOLDER_PATTERN = Pattern.compile("^\\{\\{[^}]+}}(.*)$");

    private final FolderRepository folderRepository;
    private final RequestRepository requestRepository;
    private final Util util;

    /**
     * Parameterized constructor.
     *
     * @param folderRepository  the folder repository
     * @param requestRepository the request repository
     * @param util              the utility service
     */
    public DocumentationService(FolderRepository folderRepository, RequestRepository requestRepository, Util util) {
        this.folderRepository = folderRepository;
        this.requestRepository = requestRepository;
        this.util = util;
    }

    /**
     * Generates an OpenAPI specification for a workspace.
     *
     * @param workspaceId the workspace ID
     * @param email       the user's email
     * @return the OpenAPI specification
     */
    public OpenApiSpec generateSpec(Long workspaceId, String email) {
        Workspace workspace = util.getWorkspaceWithAccessCheck(workspaceId, email);
        OpenApiInfo info = new OpenApiInfo(workspace.getName(), workspace.getDescription());

        List<Request> requests = getAllRequestsInWorkspace(workspaceId);
        Map<String, OpenApiPathItem> paths = buildPaths(requests);

        return new OpenApiSpec(info, paths);
    }

    /**
     * Gets all requests in a workspace.
     *
     * @param workspaceId the workspace ID
     * @return list of all requests
     */
    private List<Request> getAllRequestsInWorkspace(Long workspaceId) {
        return folderRepository.findByWorkspaceId(workspaceId).stream()
                .flatMap(folder -> requestRepository.findByFolderId(folder.getId()).stream())
                .toList();
    }

    /**
     * Builds the servers list for a specific path from its requests.
     *
     * @param requests the list of requests for this path
     * @return the servers list for the path
     */
    private List<OpenApiServer> buildServersForPath(List<Request> requests) {
        List<OpenApiServer> servers = requests.stream()
                .map(Request::getUrl)
                .filter(url -> !hasVariablePlaceholder(url))
                .map(this::extractBaseUrl)
                .flatMap(Optional::stream)
                .distinct()
                .map(baseUrl -> new OpenApiServer(baseUrl, null))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        boolean hasVariableUrl = requests.stream()
                .anyMatch(r -> hasVariablePlaceholder(r.getUrl()));
        if (hasVariableUrl) {
            servers.addFirst(new OpenApiServer("", "Variable base URL (from environment)"));
        }

        return servers.isEmpty()
                ? null
                : servers;
    }

    /**
     * Checks if a URL contains a variable placeholder (e.g., {{BASE_URL}}).
     *
     * @param url the URL to check
     * @return true if the URL contains a variable placeholder
     */
    private boolean hasVariablePlaceholder(String url) {
        return VARIABLE_PLACEHOLDER_PATTERN.matcher(url).matches();
    }

    /**
     * Extracts the base URL from a full URL.
     *
     * @param url the full URL
     * @return the base URL (scheme + host + port), or empty if invalid
     */
    private Optional<String> extractBaseUrl(String url) {
        try {
            URI uri = URI.create(url);
            if (uri.getScheme() != null && uri.getHost() != null) {
                String baseUrl = uri.getScheme() + "://" + uri.getHost();
                if (uri.getPort() != -1) {
                    baseUrl += ":" + uri.getPort();
                }

                return Optional.of(baseUrl);
            }
        } catch (Exception e) {
        }

        return Optional.empty();
    }

    /**
     * Builds the paths map from requests.
     *
     * @param requests the list of requests
     * @return the paths map
     */
    private Map<String, OpenApiPathItem> buildPaths(List<Request> requests) {
        Map<String, OpenApiPathItem> paths = new LinkedHashMap<>();
        Map<String, List<Request>> pathToRequests = new LinkedHashMap<>();

        for (Request request : requests) {
            String normalizedPath = normalizePath(extractPath(request.getUrl()));
            OpenApiPathItem pathItem = paths.computeIfAbsent(normalizedPath, k -> new OpenApiPathItem());

            pathToRequests.computeIfAbsent(normalizedPath, k -> new ArrayList<>()).add(request);

            OpenApiOperation operation = buildOperation(request, normalizedPath);

            setOperationByMethod(pathItem, request.getMethod(), operation);
        }

        for (Map.Entry<String, OpenApiPathItem> entry : paths.entrySet()) {
            String path = entry.getKey();
            OpenApiPathItem pathItem = entry.getValue();
            List<Request> pathRequests = pathToRequests.get(path);

            List<OpenApiServer> pathServers = buildServersForPath(pathRequests);
            pathItem.setServers(pathServers);
        }

        return paths;
    }

    /**
     * Extracts the path from a URL.
     *
     * @param url the full URL
     * @return the path portion
     */
    private String extractPath(String url) {
        try {
            Matcher matcher = VARIABLE_PLACEHOLDER_PATTERN.matcher(url);
            if (matcher.matches()) {
                String pathAfterPlaceholder = matcher.group(1);

                return pathAfterPlaceholder.isEmpty() ? "/" : pathAfterPlaceholder;
            }

            String path = URI.create(url).getPath();

            return (path == null || path.isEmpty())
                    ? "/"
                    : path;
        } catch (Exception e) {
            return "/";
        }
    }

    /**
     * Normalizes a path by converting numeric segments to path parameters.
     * e.g., /users/123 becomes /users/{id}
     *
     * @param path the original path
     * @return the normalized path
     */
    private String normalizePath(String path) {
        StringBuilder normalized = new StringBuilder();

        String[] segments = path.split("/");
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            normalized.append("/");

            if (NUMERIC_SEGMENT_PATTERN.matcher(segment).matches()) {
                normalized.append("{id}");
            } else {
                normalized.append(segment);
            }
        }

        return normalized.isEmpty()
                ? "/"
                : normalized.toString();
    }

    /**
     * Builds an OpenAPI operation from a request.
     *
     * @param request        the request
     * @param normalizedPath the normalized path
     * @return the operation
     */
    private OpenApiOperation buildOperation(Request request, String normalizedPath) {
        List<OpenApiParameter> parameters = extractPathParameters(normalizedPath);
        parameters.addAll(extractQueryParameters(request.getUrl()));

        return new OpenApiOperation(
                request.getName(),
                request.getDescription(),
                parameters.isEmpty() ? null : parameters,
                buildRequestBody(request),
                buildResponses()
        );
    }

    /**
     * Extracts path parameters from a normalized path.
     *
     * @param path the normalized path
     * @return list of path parameters
     */
    private List<OpenApiParameter> extractPathParameters(String path) {
        List<OpenApiParameter> parameters = new ArrayList<>();

        Matcher matcher = PATH_PARAM_PATTERN.matcher(path);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            parameters.add(new OpenApiParameter(paramName, "path", true, new OpenApiSchema("string")));
        }

        return parameters;
    }

    /**
     * Extracts query parameters from a URL.
     *
     * @param url the full URL
     * @return list of query parameters
     */
    private List<OpenApiParameter> extractQueryParameters(String url) {
        List<OpenApiParameter> parameters = new ArrayList<>();

        try {
            String query = URI.create(url).getQuery();
            if (query != null && !query.isEmpty()) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    String paramName = keyValue[0];

                    parameters.add(new OpenApiParameter(paramName, "query", false, new OpenApiSchema("string")));
                }
            }
        } catch (Exception e) {
            // ignore malformed URLs
        }

        return parameters;
    }

    /**
     * Builds a request body from a request.
     *
     * @param request the request
     * @return the request body, or null if not applicable
     */
    private OpenApiRequestBody buildRequestBody(Request request) {
        if (request.getBody() == null || request.getBody().isEmpty()) {
            return null;
        }

        if (request.getMethod() == RequestMethod.GET || request.getMethod() == RequestMethod.DELETE) {
            return null;
        }

        OpenApiMediaType mediaType = new OpenApiMediaType(inferSchema(request.getBody()), request.getBody());

        return new OpenApiRequestBody(
                null,
                true,
                Map.of(APPLICATION_JSON, mediaType)
        );
    }

    /**
     * Builds default responses.
     *
     * @return the responses map
     */
    private Map<String, OpenApiResponse> buildResponses() {
        return Map.of(SUCCESS_STATUS_CODE, new OpenApiResponse("Successful response", null));
    }

    /**
     * Infers an OpenAPI schema from a value.
     *
     * @param value the value to infer from
     * @return the inferred schema
     */
    private OpenApiSchema inferSchema(Object value) {
        if (value == null) {
            return new OpenApiSchema("string");
        }

        if (value instanceof String) {
            return new OpenApiSchema("string");
        }

        if (value instanceof Integer || value instanceof Long) {
            return new OpenApiSchema("integer");
        }

        if (value instanceof Double || value instanceof Float || value instanceof Number) {
            return new OpenApiSchema("number");
        }

        if (value instanceof Boolean) {
            return new OpenApiSchema("boolean");
        }

        if (value instanceof List<?> list) {
            OpenApiSchema schema = new OpenApiSchema("array");
            if (!list.isEmpty()) {
                schema.setItems(inferSchema(list.getFirst()));
            }

            return schema;
        }

        if (value instanceof Map<?, ?> map) {
            OpenApiSchema schema = new OpenApiSchema("object");

            Map<String, OpenApiSchema> properties = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                properties.put(entry.getKey().toString(), inferSchema(entry.getValue()));
            }

            schema.setProperties(properties);

            return schema;
        }

        return new OpenApiSchema("string");
    }

    /**
     * Sets the operation on the path item based on the HTTP method.
     *
     * @param pathItem  the path item
     * @param method    the HTTP method
     * @param operation the operation
     */
    private void setOperationByMethod(OpenApiPathItem pathItem, RequestMethod method, OpenApiOperation operation) {
        switch (method) {
            case GET -> pathItem.setGet(operation);
            case POST -> pathItem.setPost(operation);
            case PUT -> pathItem.setPut(operation);
            case PATCH -> pathItem.setPatch(operation);
            case DELETE -> pathItem.setDelete(operation);
        }
    }
}
