import {useCallback, useEffect, useState} from 'react';
import {requestApi} from '../services/api';
import {getErrorMessage} from '../util/errors';
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
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to load requests');
            setError(message);
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
        setError(null);

        try {
            const response = await requestApi.create(folderId, data);
            await reload();
            onFoldersChanged?.();
            setSelectedRequest(response.data);

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to create request');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [folderId, reload, onFoldersChanged]);

    const update = useCallback(async (requestId: number, data: ApiRequestRequest): Promise<ApiRequest | null> => {
        if (!folderId) {
            return null;
        }

        setActionLoading(true);
        setError(null);

        try {
            const response = await requestApi.update(folderId, requestId, data);
            setSelectedRequest(response.data);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to update request');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [folderId, reload]);

    const remove = useCallback(async (requestId: number): Promise<boolean> => {
        if (!folderId) {
            return false;
        }

        setActionLoading(true);
        setError(null);

        try {
            await requestApi.delete(folderId, requestId);
            await reload();
            onFoldersChanged?.();

            if (selectedRequest?.id === requestId) {
                setSelectedRequest(null);
            }

            return true;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to delete request');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [folderId, reload, onFoldersChanged, selectedRequest?.id]);

    const execute = useCallback(async (requestId: number): Promise<ExecutionResponse> => {
        if (!folderId) {
            throw new Error('No folder selected');
        }

        setExecuting(true);
        setError(null);

        try {
            const response = await requestApi.execute(folderId, requestId);
            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to execute request');
            setError(message);
            throw new Error(message);
        } finally {
            setExecuting(false);
        }
    }, [folderId]);

    const lock = useCallback(async (requestId: number): Promise<ApiRequest | null> => {
        if (!folderId) {
            return null;
        }

        setError(null);

        try {
            const response = await requestApi.lock(folderId, requestId);
            const updated = response.data;

            setSelectedRequest(updated);
            setRequests(prev => prev.map(r => r.id === updated.id ? updated : r));

            return updated;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to lock request');
            setError(message);
            throw new Error(message);
        }
    }, [folderId]);

    const unlock = useCallback(async (requestId: number): Promise<ApiRequest | null> => {
        if (!folderId) {
            return null;
        }

        setError(null);

        try {
            const response = await requestApi.unlock(folderId, requestId);
            const updated = response.data;

            setSelectedRequest(updated);
            await reload();

            return updated;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to unlock request');
            setError(message);
            throw new Error(message);
        }
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
