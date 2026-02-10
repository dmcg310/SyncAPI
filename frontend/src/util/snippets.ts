import type {HttpMethod, OpenApiOperation, OpenApiParameter} from '@/types';

interface SnippetOptions {
    method: HttpMethod;
    path: string;
    operation: OpenApiOperation;
    baseUrl?: string;
}

const buildQueryString = (params: OpenApiParameter[]): string => {
    const queryParams = params.filter(p => p.in === 'query');
    if (queryParams.length === 0) {
        return '';
    }

    const pairs = queryParams.map(p => `${p.name}={${p.name}}`);

    return '?' + pairs.join('&');
};

const buildPathWithParams = (path: string, params?: OpenApiParameter[]): string => {
    let result = path;

    if (params) {
        const pathParams = params.filter(p => p.in === 'path');
        pathParams.forEach(p => {
            result = result.replace(`{${p.name}}`, `{${p.name}}`);
        });
    }

    return result;
};

export const generateCurlSnippet = (options: SnippetOptions): string => {
    const {method, path, operation, baseUrl = 'https://api.example.com'} = options;
    const queryString = operation.parameters ? buildQueryString(operation.parameters) : '';
    const fullPath = buildPathWithParams(path, operation.parameters);
    const url = `${baseUrl}${fullPath}${queryString}`;

    let snippet = `curl -X ${method} '${url}'`;

    const headers: Record<string, string> = {};

    if (operation.requestBody?.content?.['application/json']) {
        headers['Content-Type'] = 'application/json';
    }

    if (operation.parameters) {
        const headerParams = operation.parameters.filter(p => p.in === 'header');
        headerParams.forEach(p => {
            headers[p.name] = `{${p.name}}`;
        });
    }

    Object.entries(headers).forEach(([key, value]) => {
        snippet += ` \\\n  -H '${key}: ${value}'`;
    });

    if (operation.requestBody?.content?.['application/json']) {
        snippet += ` \\\n  -d '{}'`;
    }

    return snippet;
};
