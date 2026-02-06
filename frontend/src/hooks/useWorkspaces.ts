import {useCallback, useEffect, useState} from 'react';
import {workspaceApi} from '../services/api';
import {getErrorMessage} from '../util/errors';
import type {Workspace, WorkspaceRequest} from '@/types';

interface UseWorkspacesReturn {
    workspaces: Workspace[];
    loading: boolean;
    error: string | null;
    reload: () => Promise<void>;
    create: (data: WorkspaceRequest) => Promise<Workspace | null>;
    update: (id: number, data: WorkspaceRequest) => Promise<Workspace | null>;
    remove: (id: number) => Promise<boolean>;
    actionLoading: boolean;
}

export function useWorkspaces(): UseWorkspacesReturn {
    const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [actionLoading, setActionLoading] = useState(false);

    const reload = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await workspaceApi.getAll();
            setWorkspaces(response.data);
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to load workspaces');
            setError(message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        reload();
    }, [reload]);

    const create = useCallback(async (data: WorkspaceRequest): Promise<Workspace | null> => {
        setActionLoading(true);
        setError(null);

        try {
            const response = await workspaceApi.create(data);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to create workspace');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [reload]);

    const update = useCallback(async (id: number, data: WorkspaceRequest): Promise<Workspace | null> => {
        setActionLoading(true);
        setError(null);

        try {
            const response = await workspaceApi.update(id, data);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to update workspace');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [reload]);

    const remove = useCallback(async (id: number): Promise<boolean> => {
        setActionLoading(true);
        setError(null);

        try {
            await workspaceApi.delete(id);
            await reload();

            return true;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to delete workspace');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [reload]);

    return {
        workspaces,
        loading,
        error,
        reload,
        create,
        update,
        remove,
        actionLoading
    };
}
