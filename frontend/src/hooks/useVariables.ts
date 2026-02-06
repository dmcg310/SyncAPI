import {useCallback, useEffect, useState} from 'react';
import {variableApi} from '../services/api';
import {getErrorMessage} from '../util/errors';
import type {EnvironmentVariable, EnvironmentVariableRequest} from '@/types';

interface UseVariablesReturn {
    variables: EnvironmentVariable[];
    loading: boolean;
    error: string | null;
    reload: () => Promise<void>;
    create: (data: EnvironmentVariableRequest) => Promise<EnvironmentVariable | null>;
    update: (variableId: number, data: EnvironmentVariableRequest) => Promise<EnvironmentVariable | null>;
    remove: (variableId: number) => Promise<boolean>;
    actionLoading: boolean;
}

export function useVariables(environmentId: number | null): UseVariablesReturn {
    const [variables, setVariables] = useState<EnvironmentVariable[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [actionLoading, setActionLoading] = useState(false);

    const reload = useCallback(async () => {
        if (!environmentId) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await variableApi.getByEnvironment(environmentId);
            setVariables(response.data);
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to load variables');
            setError(message);
        } finally {
            setLoading(false);
        }
    }, [environmentId]);

    useEffect(() => {
        if (environmentId) {
            reload();
        }
    }, [environmentId, reload]);

    const create = useCallback(async (data: EnvironmentVariableRequest): Promise<EnvironmentVariable | null> => {
        if (!environmentId) {
            return null;
        }

        setActionLoading(true);
        setError(null);

        try {
            const response = await variableApi.create(environmentId, data);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to create variable');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [environmentId, reload]);

    const update = useCallback(async (variableId: number, data: EnvironmentVariableRequest): Promise<EnvironmentVariable | null> => {
        if (!environmentId) {
            return null;
        }

        setActionLoading(true);
        setError(null);

        try {
            const response = await variableApi.update(environmentId, variableId, data);
            await reload();

            return response.data;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to update variable');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [environmentId, reload]);

    const remove = useCallback(async (variableId: number): Promise<boolean> => {
        if (!environmentId) {
            return false;
        }

        setActionLoading(true);
        setError(null);

        try {
            await variableApi.delete(environmentId, variableId);
            await reload();

            return true;
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to delete variable');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [environmentId, reload]);

    return {
        variables,
        loading,
        error,
        reload,
        create,
        update,
        remove,
        actionLoading
    };
}
