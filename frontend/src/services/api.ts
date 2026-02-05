import axios, {AxiosInstance} from 'axios';
import {API_BASE_URL, STORAGE_KEYS} from '../util/constants';
import type {
    ApiRequest,
    ApiRequestRequest,
    Environment,
    EnvironmentRequest,
    EnvironmentVariable,
    EnvironmentVariableRequest,
    ExecutionResponse,
    Folder,
    FolderRequest,
    LoginRequest,
    LoginResponse,
    OpenApiSpec,
    RegisterRequest,
    User,
    Workspace,
    WorkspaceDetail,
    WorkspaceRequest
} from '@/types';

const api: AxiosInstance = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const rawUrl = error.config?.url ?? '';
        const pathname = rawUrl.startsWith('http')
            ? new URL(rawUrl).pathname
            : rawUrl;

        const isMeRoute = pathname === '/auth/me';

        if (error.response?.status === 401 && isMeRoute) {
            localStorage.removeItem(STORAGE_KEYS.TOKEN);
            localStorage.removeItem(STORAGE_KEYS.USER);
            window.location.href = '/login';
        }

        return Promise.reject(error);
    }
);

export const authApi = {
    register: (data: RegisterRequest) =>
        api.post<void>('/auth/register', data),
    login: (data: LoginRequest) =>
        api.post<LoginResponse>('/auth/login', data),
    me: () =>
        api.get<User>('/auth/me'),
    updatePassword: (data: { currentPassword: string; newPassword: string }) =>
        api.patch<void>('/auth/password', data)
};

export const workspaceApi = {
    getAll: () =>
        api.get<Workspace[]>('/workspaces'),
    getById: (id: number) =>
        api.get<WorkspaceDetail>(`/workspaces/${id}`),
    create: (data: WorkspaceRequest) =>
        api.post<Workspace>('/workspaces', data),
    update: (id: number, data: WorkspaceRequest) =>
        api.put<Workspace>(`/workspaces/${id}`, data),
    patch: (id: number, data: Partial<WorkspaceRequest>) =>
        api.patch<Workspace>(`/workspaces/${id}`, data),
    delete: (id: number) =>
        api.delete<void>(`/workspaces/${id}`),
    addMember: (id: number, email: string) =>
        api.post<WorkspaceDetail>(`/workspaces/${id}/members`, {email}),
    removeMember: (id: number, userId: number) =>
        api.delete<WorkspaceDetail>(`/workspaces/${id}/members/${userId}`)
};

export const folderApi = {
    getByWorkspace: (workspaceId: number) =>
        api.get<Folder[]>(`/workspaces/${workspaceId}/folders`),
    getById: (workspaceId: number, folderId: number) =>
        api.get<Folder>(`/workspaces/${workspaceId}/folders/${folderId}`),
    create: (workspaceId: number, data: FolderRequest) =>
        api.post<Folder>(`/workspaces/${workspaceId}/folders`, data),
    update: (workspaceId: number, folderId: number, data: FolderRequest) =>
        api.put<Folder>(`/workspaces/${workspaceId}/folders/${folderId}`, data),
    patch: (workspaceId: number, folderId: number, data: Partial<FolderRequest>) =>
        api.patch<Folder>(`/workspaces/${workspaceId}/folders/${folderId}`, data),
    delete: (workspaceId: number, folderId: number) =>
        api.delete<void>(`/workspaces/${workspaceId}/folders/${folderId}`)
};

export const requestApi = {
    getByFolder: (folderId: number) =>
        api.get<ApiRequest[]>(`/folders/${folderId}/requests`),
    getById: (folderId: number, requestId: number) =>
        api.get<ApiRequest>(`/folders/${folderId}/requests/${requestId}`),
    create: (folderId: number, data: ApiRequestRequest) =>
        api.post<ApiRequest>(`/folders/${folderId}/requests`, data),
    update: (folderId: number, requestId: number, data: ApiRequestRequest) =>
        api.put<ApiRequest>(`/folders/${folderId}/requests/${requestId}`, data),
    patch: (folderId: number, requestId: number, data: Partial<ApiRequestRequest>) =>
        api.patch<ApiRequest>(`/folders/${folderId}/requests/${requestId}`, data),
    delete: (folderId: number, requestId: number) =>
        api.delete<void>(`/folders/${folderId}/requests/${requestId}`),
    execute: (folderId: number, requestId: number) =>
        api.post<ExecutionResponse>(`/folders/${folderId}/requests/${requestId}/execute`),
    lock: (folderId: number, requestId: number) =>
        api.patch<ApiRequest>(`/folders/${folderId}/requests/${requestId}/lock`),
    unlock: (folderId: number, requestId: number) =>
        api.patch<ApiRequest>(`/folders/${folderId}/requests/${requestId}/unlock`)
};

export const environmentApi = {
    getByWorkspace: (workspaceId: number) =>
        api.get<Environment[]>(`/workspaces/${workspaceId}/environments`),
    getById: (workspaceId: number, environmentId: number) =>
        api.get<Environment>(`/workspaces/${workspaceId}/environments/${environmentId}`),
    create: (workspaceId: number, data: EnvironmentRequest) =>
        api.post<Environment>(`/workspaces/${workspaceId}/environments`, data),
    update: (workspaceId: number, environmentId: number, data: EnvironmentRequest) =>
        api.put<Environment>(`/workspaces/${workspaceId}/environments/${environmentId}`, data),
    patch: (workspaceId: number, environmentId: number, data: Partial<EnvironmentRequest>) =>
        api.patch<Environment>(`/workspaces/${workspaceId}/environments/${environmentId}`, data),
    delete: (workspaceId: number, environmentId: number) =>
        api.delete<void>(`/workspaces/${workspaceId}/environments/${environmentId}`),
    activate: (workspaceId: number, environmentId: number) =>
        api.patch<Environment>(`/workspaces/${workspaceId}/environments/${environmentId}/activate`)
};

export const variableApi = {
    getByEnvironment: (environmentId: number) =>
        api.get<EnvironmentVariable[]>(`/environments/${environmentId}/variables`),
    create: (environmentId: number, data: EnvironmentVariableRequest) =>
        api.post<EnvironmentVariable>(`/environments/${environmentId}/variables`, data),
    update: (environmentId: number, variableId: number, data: EnvironmentVariableRequest) =>
        api.put<EnvironmentVariable>(`/environments/${environmentId}/variables/${variableId}`, data),
    delete: (environmentId: number, variableId: number) =>
        api.delete<void>(`/environments/${environmentId}/variables/${variableId}`)
};

export const documentationApi = {
    getSpec: (workspaceId: number) =>
        api.get<OpenApiSpec>(`/workspaces/${workspaceId}/documentation`)
};

export default api;
