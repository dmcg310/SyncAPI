import {useCallback, useState} from 'react';

interface UseAsyncReturn<T, Args extends unknown[]> {
    data: T | null;
    loading: boolean;
    error: string | null;
    execute: (...args: Args) => Promise<T | null>;
    reset: () => void;
    setData: (data: T | null) => void;
}

export function useAsync<T, Args extends unknown[] = []>(
    asyncFn: (...args: Args) => Promise<T>,
    options?: {
        onSuccess?: (data: T) => void;
        onError?: (error: string) => void;
    }
): UseAsyncReturn<T, Args> {
    const [data, setData] = useState<T | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const execute = useCallback(async (...args: Args): Promise<T | null> => {
        setLoading(true);
        setError(null);

        try {
            const result = await asyncFn(...args);
            setData(result);
            options?.onSuccess?.(result);

            return result;
        } catch (err: unknown) {
            const message = extractErrorMessage(err);
            setError(message);
            options?.onError?.(message);

            return null;
        } finally {
            setLoading(false);
        }
    }, [asyncFn, options]);

    const reset = useCallback(() => {
        setData(null);
        setError(null);
        setLoading(false);
    }, []);

    return {data, loading, error, execute, reset, setData};
}

function extractErrorMessage(err: unknown): string {
    if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        return axiosError.response?.data?.message || 'An error occurred';
    }

    return 'An error occurred';
}
