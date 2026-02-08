import {useCallback, useEffect, useState} from 'react';
import {documentationApi} from '../services/api';
import {getErrorMessage} from '../util/errors';
import type {OpenApiSpec} from '@/types';

interface UseDocumentationReturn {
    spec: OpenApiSpec | null;
    loading: boolean;
    error: string | null;
    reload: () => Promise<void>;
}

export function useDocumentation(workspaceId: number | null): UseDocumentationReturn {
    const [spec, setSpec] = useState<OpenApiSpec | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const reload = useCallback(async () => {
        if (!workspaceId) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await documentationApi.getSpec(workspaceId);
            setSpec(response.data);
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to load documentation');
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

    return {spec, loading, error, reload};
}
