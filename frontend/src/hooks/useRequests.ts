import {useCallback, useEffect, useState} from 'react';
import {requestApi} from '../services/api';
import type {ApiRequest, ApiRequestRequest, ExecutionResponse} from '@/types';

interface UseRequestsReturn {
    requests: ApiRequest[];
    selectedRequest: ApiRequest | null;
    loading: boolean;
    error: string | null;
    selectRequest: (request: ApiRequest | null) => void;
    reload: () => Promise<void>;
    create: (data: ApiRequestRequest) => Promise<ApiRequest | null>;
    update: (requestId: number, data: ApiRequestRequest) => Promise<ApiRequest | null>;
    remove: (requestId: number) => Promise<boolean>;
    execute: (requestId: number) => Promise<ExecutionResponse>;
    lock: (requestId: number) => Promise<ApiRequest | null>;
    unlock: (requestId: number) => Promise<ApiRequest | null>;
    actionLoading: boolean;
    executing: boolean;
}

export function useRequests(folderId: number | null, onFoldersChanged?: () => void): UseRequestsReturn {
    const [requests, setRequests] = useState<ApiRequest[]>([]);
    const [selectedRequest, setSelectedRequest] = useState<ApiRequest | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [actionLoading, setActionLoading] = useState(false);
    const [executing, setExecuting] = useState(false);

    const reload = useCallback(async () => {
        if (!folderId) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await requestApi.getByFolder(folderId);
            setRequests(response.data);
        } catch {
            setError('Failed to load requests');
        } finally {
            setLoading(false);
        }
    }, [folderId]);

    useEffect(() => {
        if (folderId) {
            reload();
            setSelectedRequest(null);
        } else {
            setRequests([]);
            setSelectedRequest(null);
        }
    }, [folderId, reload]);

    const selectRequest = useCallback((request: ApiRequest | null) => {
        setSelectedRequest(request);
    }, []);

    const create = useCallback(async (data: ApiRequestRequest): Promise<ApiRequest | null> => {
        if (!folderId) {
            return null;
        }

        setActionLoading(true);

        try {
            const response = await requestApi.create(folderId, data);
            await reload();
            onFoldersChanged?.();
            setSelectedRequest(response.data);

            return response.data;
        } catch {
            throw new Error('Failed to create request');
        } finally {
            setActionLoading(false);
        }
    }, [folderId, reload, onFoldersChanged]);

    const update = useCallback(async (requestId: number, data: ApiRequestRequest): Promise<ApiRequest | null> => {
        if (!folderId) {
            return null;
        }

        setActionLoading(true);

        try {
            const response = await requestApi.update(folderId, requestId, data);
            setSelectedRequest(response.data);
            await reload();

            return response.data;
        } catch {
            throw new Error('Failed to update request');
        } finally {
            setActionLoading(false);
        }
    }, [folderId, reload]);

    const remove = useCallback(async (requestId: number): Promise<boolean> => {
        if (!folderId) {
            return false;
        }

        setActionLoading(true);

        try {
            await requestApi.delete(folderId, requestId);
            await reload();
            onFoldersChanged?.();

            if (selectedRequest?.id === requestId) {
                setSelectedRequest(null);
            }

            return true;
        } catch {
            throw new Error('Failed to delete request');
        } finally {
            setActionLoading(false);
        }
    }, [folderId, reload, onFoldersChanged, selectedRequest?.id]);

    const execute = useCallback(async (requestId: number): Promise<ExecutionResponse> => {
        if (!folderId) {
            throw new Error('No folder selected');
        }

        setExecuting(true);

        try {
            const response = await requestApi.execute(folderId, requestId);
            return response.data;
        } finally {
            setExecuting(false);
        }
    }, [folderId]);

    const lock = useCallback(async (requestId: number): Promise<ApiRequest | null> => {
        if (!folderId) {
            return null;
        }

        const response = await requestApi.lock(folderId, requestId);
        const updated = response.data;

        setSelectedRequest(updated);
        setRequests(prev => prev.map(r => r.id === updated.id ? updated : r));

        return updated;
    }, [folderId]);

    const unlock = useCallback(async (requestId: number): Promise<ApiRequest | null> => {
        if (!folderId) {
            return null;
        }

        const response = await requestApi.unlock(folderId, requestId);
        const updated = response.data;

        setSelectedRequest(updated);
        await reload();

        return updated;
    }, [folderId, reload]);

    return {
        requests,
        selectedRequest,
        loading,
        error,
        selectRequest,
        reload,
        create,
        update,
        remove,
        execute,
        lock,
        unlock,
        actionLoading,
        executing
    };
}
