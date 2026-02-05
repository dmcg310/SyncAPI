import {useCallback, useEffect, useState} from 'react';
import {environmentApi} from '../services/api';
import type {Environment, EnvironmentRequest} from '@/types';

interface UseEnvironmentsReturn {
    environments: Environment[];
    activeEnvironment: Environment | null;
    loading: boolean;
    error: string | null;
    reload: () => Promise<void>;
    create: (data: EnvironmentRequest) => Promise<Environment | null>;
    update: (environmentId: number, data: EnvironmentRequest) => Promise<Environment | null>;
    remove: (environmentId: number) => Promise<boolean>;
    activate: (environmentId: number) => Promise<Environment | null>;
    actionLoading: boolean;
}

export function useEnvironments(workspaceId: number | null): UseEnvironmentsReturn {
    const [environments, setEnvironments] = useState<Environment[]>([]);
    const [activeEnvironment, setActiveEnvironment] = useState<Environment | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [actionLoading, setActionLoading] = useState(false);

    const reload = useCallback(async () => {
        if (!workspaceId) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await environmentApi.getByWorkspace(workspaceId);
            setEnvironments(response.data);

            const active = response.data.find(env => env.isActive) ?? null;
            setActiveEnvironment(active);
        } catch {
            setError('Failed to load environments');
        } finally {
            setLoading(false);
        }
    }, [workspaceId]);

    useEffect(() => {
        if (workspaceId) {
            reload();
        }
    }, [workspaceId, reload]);

    const create = useCallback(async (data: EnvironmentRequest): Promise<Environment | null> => {
        if (!workspaceId) {
            return null;
        }

        setActionLoading(true);

        try {
            const response = await environmentApi.create(workspaceId, data);
            await reload();

            return response.data;
        } catch {
            throw new Error('Failed to create environment');
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    const update = useCallback(async (environmentId: number, data: EnvironmentRequest): Promise<Environment | null> => {
        if (!workspaceId) {
            return null;
        }

        setActionLoading(true);

        try {
            const response = await environmentApi.update(workspaceId, environmentId, data);
            await reload();

            return response.data;
        } catch {
            throw new Error('Failed to update environment');
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    const remove = useCallback(async (environmentId: number): Promise<boolean> => {
        if (!workspaceId) {
            return false;
        }

        setActionLoading(true);

        try {
            await environmentApi.delete(workspaceId, environmentId);
            await reload();

            return true;
        } catch {
            throw new Error('Failed to delete environment');
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    const activate = useCallback(async (environmentId: number): Promise<Environment | null> => {
        if (!workspaceId) {
            return null;
        }

        setActionLoading(true);

        try {
            const response = await environmentApi.activate(workspaceId, environmentId);
            await reload();

            return response.data;
        } catch {
            throw new Error('Failed to activate environment');
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    return {
        environments,
        activeEnvironment,
        loading,
        error,
        reload,
        create,
        update,
        remove,
        activate,
        actionLoading
    };
}
