import {useCallback, useEffect, useRef, useState} from 'react';
import type {ApiRequest, ApiRequestRequest, ExecutionResponse, HttpMethod} from '@/types';
import {useAuth} from '../context/AuthContext';

interface HeaderEntry {
    key: string;
    value: string;
}

interface UseRequestEditorReturn {
    name: string;
    setName: (value: string) => void;
    description: string;
    setDescription: (value: string) => void;
    method: HttpMethod;
    setMethod: (value: HttpMethod) => void;
    url: string;
    setUrl: (value: string) => void;
    headers: HeaderEntry[];
    body: string;
    setBody: (value: string) => void;
    activeTab: 'headers' | 'body';
    setActiveTab: (tab: 'headers' | 'body') => void;

    addHeader: () => void;
    removeHeader: (index: number) => void;
    updateHeader: (index: number, field: 'key' | 'value', value: string) => void;

    isLocked: boolean;
    isLockedByMe: boolean;
    isLockedByOther: boolean;
    canEdit: boolean;

    response: ExecutionResponse | null;
    setResponse: (response: ExecutionResponse | null) => void;

    error: string;
    setError: (error: string) => void;

    buildRequestData: () => ApiRequestRequest | null;
    resetAutoLock: () => void;
    markAutoLocked: () => void;
    hasAutoLocked: boolean;
}

export function useRequestEditor(request: ApiRequest | null): UseRequestEditorReturn {
    const {user} = useAuth();

    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [method, setMethod] = useState<HttpMethod>('GET');
    const [url, setUrl] = useState('');
    const [headers, setHeaders] = useState<HeaderEntry[]>([]);
    const [body, setBody] = useState('');
    const [activeTab, setActiveTab] = useState<'headers' | 'body'>('headers');

    const [response, setResponse] = useState<ExecutionResponse | null>(null);
    const [error, setError] = useState('');
    const hasAutoLockedRef = useRef(false);

    const isLocked = request?.lockedBy != null;
    const isLockedByMe = !!request && isLocked && request.lockedBy === user?.id;
    const isLockedByOther = !!request && isLocked && request.lockedBy !== user?.id;
    const canEdit = !isLocked || isLockedByMe;

    useEffect(() => {
        if (request) {
            setName(request.name);
            setDescription(request.description || '');
            setMethod(request.method);
            setUrl(request.url);
            setHeaders(request.headers
                ? Object.entries(request.headers).map(([key, value]) => ({key, value}))
                : []
            );
            setBody(request.body ? JSON.stringify(request.body, null, 2) : '');
            setResponse(null);
            setError('');
            hasAutoLockedRef.current = false;
        }
    }, [request?.id]);

    const addHeader = useCallback(() => {
        setHeaders(prev => [...prev, {key: '', value: ''}]);
    }, []);

    const removeHeader = useCallback((index: number) => {
        setHeaders(prev => prev.filter((_, i) => i !== index));
    }, []);

    const updateHeader = useCallback((index: number, field: 'key' | 'value', value: string) => {
        setHeaders(prev => {
            const newHeaders = [...prev];
            newHeaders[index][field] = value;
            return newHeaders;
        });
    }, []);

    const buildRequestData = useCallback((): ApiRequestRequest | null => {
        if (!name.trim() || !url.trim()) {
            setError('Name and URL are required');

            return null;
        }

        const headersObject: Record<string, string> = {};
        headers.forEach(h => {
            if (h.key.trim()) {
                headersObject[h.key.trim()] = h.value;
            }
        });

        let bodyObject: Record<string, unknown> | undefined;
        if (body.trim() && ['POST', 'PUT', 'PATCH'].includes(method)) {
            try {
                bodyObject = JSON.parse(body);
            } catch {
                setError('Invalid JSON in body');

                return null;
            }
        }

        setError('');

        return {
            name: name.trim(),
            description: description.trim() || undefined,
            method,
            url: url.trim(),
            headers: Object.keys(headersObject).length > 0 ? headersObject : undefined,
            body: bodyObject
        };
    }, [name, url, headers, body, method, description]);

    const resetAutoLock = useCallback(() => {
        hasAutoLockedRef.current = false;
    }, []);

    const markAutoLocked = useCallback(() => {
        hasAutoLockedRef.current = true;
    }, []);

    return {
        name,
        setName,
        description,
        setDescription,
        method,
        setMethod,
        url,
        setUrl,
        headers,
        body,
        setBody,
        activeTab,
        setActiveTab,
        addHeader,
        removeHeader,
        updateHeader,
        isLocked,
        isLockedByMe,
        isLockedByOther,
        canEdit,
        response,
        setResponse,
        error,
        setError,
        buildRequestData,
        resetAutoLock,
        markAutoLocked,
        hasAutoLocked: hasAutoLockedRef.current
    };
}
