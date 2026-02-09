import type {HttpMethod, OpenApiOperation, OpenApiParameter} from '@/types';

interface SnippetOptions {
    method: HttpMethod;
    path: string;
    operation: OpenApiOperation;
    baseUrl?: string;
}

const formatHeaders = (headers: Record<string, string>): string => {
    return Object.entries(headers)
        .map(([key, value]) => `  '${key}': '${value}'`)
        .join(',\n');
};

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

export const generateFetchSnippet = (options: SnippetOptions): string => {
    const {method, path, operation, baseUrl = 'https://api.example.com'} = options;
    const queryString = operation.parameters ? buildQueryString(operation.parameters) : '';
    const fullPath = buildPathWithParams(path, operation.parameters);
    const url = `${baseUrl}${fullPath}${queryString}`;

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

    let snippet = `fetch('${url}', {\n`;
    snippet += `  method: '${method}'`;

    if (Object.keys(headers).length > 0) {
        snippet += `,\n  headers: {\n${formatHeaders(headers)}\n  }`;
    }

    if (operation.requestBody?.content?.['application/json']) {
        snippet += `,\n  body: JSON.stringify({})`;
    }

    snippet += '\n})';
    snippet += '\n  .then(response => response.json())';
    snippet += '\n  .then(data => console.log(data))';
    snippet += '\n  .catch(error => console.error(error));';

    return snippet;
};

export const generateAxiosSnippet = (options: SnippetOptions): string => {
    const {method, path, operation, baseUrl = 'https://api.example.com'} = options;
    const queryString = operation.parameters ? buildQueryString(operation.parameters) : '';
    const fullPath = buildPathWithParams(path, operation.parameters);
    const url = `${baseUrl}${fullPath}${queryString}`;

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

    let snippet = `axios({\n`;
    snippet += `  method: '${method.toLowerCase()}',\n`;
    snippet += `  url: '${url}'`;

    if (Object.keys(headers).length > 0) {
        snippet += `,\n  headers: {\n${formatHeaders(headers)}\n  }`;
    }

    if (operation.requestBody?.content?.['application/json']) {
        snippet += `,\n  data: {}`;
    }

    snippet += '\n})';
    snippet += '\n  .then(response => console.log(response.data))';
    snippet += '\n  .catch(error => console.error(error));';

    return snippet;
};
