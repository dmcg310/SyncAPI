export interface User {
    id: number;
    email: string;
    name: string;
    createdAt: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
}

export interface LoginResponse {
    token: string;
}

export interface Workspace {
    id: number;
    name: string;
    description?: string;
    memberCount: number;
    folderCount: number;
    environmentCount: number;
    createdAt: string;
    updatedAt: string;
}

export interface WorkspaceDetail extends Workspace {
    members: User[];
}

export interface WorkspaceRequest {
    name: string;
    description?: string;
}

export interface Folder {
    id: number;
    name: string;
    description?: string;
    workspaceId: number;
    requestCount: number;
    createdAt: string;
    updatedAt: string;
}

export interface FolderRequest {
    name: string;
    description?: string;
}

export interface ApiRequest {
    id: number;
    name: string;
    description?: string;
    method: HttpMethod;
    url: string;
    headers?: Record<string, string>;
    body?: unknown;
    authType?: string;
    authConfig?: unknown;
    lockedBy: number | null;
    lockedAt: string | null;
    createdAt: string;
    folderId: number;
}

export interface ApiRequestRequest {
    name: string;
    description?: string;
    method: HttpMethod;
    url: string;
    headers?: Record<string, string>;
    body?: Record<string, unknown>;
}

export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface ExecutionResponse {
    statusCode: number;
    statusText: string;
    headers: Record<string, string>;
    body: unknown;
    responseTimeMs: number;
    success: boolean;
    errorMessage?: string;
}

export interface Environment {
    id: number;
    name: string;
    description?: string;
    workspaceId: number;
    isActive: boolean;
    variableCount: number;
    createdAt: string;
    updatedAt: string;
}

export interface EnvironmentRequest {
    name: string;
    description?: string;
}

export interface EnvironmentVariable {
    id: number;
    key: string;
    value: string;
    environmentId: number;
    createdAt: string;
    updatedAt: string;
}

export interface EnvironmentVariableRequest {
    key: string;
    value: string;
}

export interface OpenApiSpec {
    openapi: string;
    info: OpenApiInfo;
    paths: Record<string, OpenApiPathItem>;
}

export interface OpenApiInfo {
    title: string;
    description?: string;
    version: string;
}

export interface OpenApiPathItem {
    get?: OpenApiOperation;
    post?: OpenApiOperation;
    put?: OpenApiOperation;
    patch?: OpenApiOperation;
    delete?: OpenApiOperation;
}

export interface OpenApiOperation {
    summary: string;
    description?: string;
    parameters?: OpenApiParameter[];
    requestBody?: OpenApiRequestBody;
    responses: Record<string, OpenApiResponse>;
}

export interface OpenApiParameter {
    name: string;
    in: string;
    required: boolean;
    schema: OpenApiSchema;
}

export interface OpenApiRequestBody {
    description?: string;
    required: boolean;
    content: Record<string, OpenApiMediaType>;
}

export interface OpenApiMediaType {
    schema: OpenApiSchema;
    example?: unknown;
}

export interface OpenApiResponse {
    description: string;
    content?: Record<string, OpenApiMediaType>;
}

export interface OpenApiSchema {
    type: string;
    properties?: Record<string, OpenApiSchema>;
    items?: OpenApiSchema;
}
