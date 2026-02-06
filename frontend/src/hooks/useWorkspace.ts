import {useCallback, useEffect, useState} from 'react';
import {workspaceApi} from '../services/api';
import {getErrorMessage} from '../util/errors';
import type {WorkspaceDetail} from '@/types';

interface UseWorkspaceReturn {
    workspace: WorkspaceDetail | null;
    loading: boolean;
    error: string | null;
    reload: () => Promise<void>;
}

export function useWorkspace(workspaceId: number | null): UseWorkspaceReturn {
    const [workspace, setWorkspace] = useState<WorkspaceDetail | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const reload = useCallback(async () => {
        if (!workspaceId) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await workspaceApi.getById(workspaceId);
            setWorkspace(response.data);
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to load workspace');
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

    return {workspace, loading, error, reload};
}
