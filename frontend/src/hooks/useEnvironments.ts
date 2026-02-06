import {useCallback, useEffect, useState} from 'react';
import {environmentApi} from '../services/api';
import {getErrorMessage} from '../util/errors';
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
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to load environments');
            setError(message);
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
        setError(null);

        try {
            const response = await environmentApi.create(workspaceId, data);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to create environment');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    const update = useCallback(async (environmentId: number, data: EnvironmentRequest): Promise<Environment | null> => {
        if (!workspaceId) {
            return null;
        }

        setActionLoading(true);
        setError(null);

        try {
            const response = await environmentApi.update(workspaceId, environmentId, data);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to update environment');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    const remove = useCallback(async (environmentId: number): Promise<boolean> => {
        if (!workspaceId) {
            return false;
        }

        setActionLoading(true);
        setError(null);

        try {
            await environmentApi.delete(workspaceId, environmentId);
            await reload();

            return true;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to delete environment');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    const activate = useCallback(async (environmentId: number): Promise<Environment | null> => {
        if (!workspaceId) {
            return null;
        }

        setActionLoading(true);
        setError(null);

        try {
            const response = await environmentApi.activate(workspaceId, environmentId);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to activate environment');
            setError(message);
            throw new Error(message);
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
