export function getErrorMessage(err: unknown, fallback = 'An error occurred'): string {
    if (err && typeof err === 'object' && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        return axiosError.response?.data?.message || fallback;
    }
    return fallback;
}
