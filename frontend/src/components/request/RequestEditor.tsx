import React, {useEffect, useRef, useState} from 'react';
import type {ApiRequest, ApiRequestRequest, ExecutionResponse, HttpMethod} from '@/types';
import {HTTP_METHODS, METHOD_COLORS} from '../../util/constants';
import {TbTerminal2} from "react-icons/tb";
import {FaLock, FaLockOpen, FaUser} from "react-icons/fa";
import {useAuth} from '../../context/AuthContext';
import {IoClose} from "react-icons/io5";
import JsonEditor from "../common/JsonEditor";

interface RequestEditorProps {
    request: ApiRequest | null;
    onSave: (data: ApiRequestRequest) => Promise<void>;
    onExecute: () => Promise<ExecutionResponse>;
    onLock: () => Promise<void>;
    onUnlock: () => Promise<void>;
    saving: boolean;
    executing: boolean;
}

const RequestEditor: React.FC<RequestEditorProps> = ({
                                                         request,
                                                         onSave,
                                                         onExecute,
                                                         onLock,
                                                         onUnlock,
                                                         saving,
                                                         executing
                                                     }) => {
    const {user} = useAuth();
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [method, setMethod] = useState<HttpMethod>('GET');
    const [url, setUrl] = useState('');
    const [headers, setHeaders] = useState<Array<{ key: string; value: string }>>([]);
    const [body, setBody] = useState('');
    const [response, setResponse] = useState<ExecutionResponse | null>(null);
    const [activeTab, setActiveTab] = useState<'headers' | 'body'>('headers');
    const [error, setError] = useState('');
    const hasAutoLocked = useRef(false);

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
            setHeaders(
                request.headers
                    ? Object.entries(request.headers).map(([key, value]) => ({key, value}))
                    : []
            );
            setBody(request.body ? JSON.stringify(request.body, null, 2) : '');
            setResponse(null);
            setError('');
            hasAutoLocked.current = false;
        }
    }, [request?.id]);

    const handleStartEditing = async () => {
        if (!request) {
            return;
        }

        if (isLocked) {
            return;
        }

        if (hasAutoLocked.current) {
            return;
        }

        hasAutoLocked.current = true;

        try {
            await onLock();
        } catch (err) {
            console.error('Failed to auto-lock:', err);
            setError('Could not lock this request. Please try again.');
            hasAutoLocked.current = false;
        }
    };

    const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        handleStartEditing();
        setName(e.target.value);
    };

    const handleDescriptionChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
        handleStartEditing();
        setDescription(e.target.value);
    };

    const handleMethodChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        handleStartEditing();
        setMethod(e.target.value as HttpMethod);
    };

    const handleUrlChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        handleStartEditing();
        setUrl(e.target.value);
    };

    const handleHeaderChange = (index: number, field: 'key' | 'value', value: string) => {
        handleStartEditing();
        const newHeaders = [...headers];
        newHeaders[index][field] = value;
        setHeaders(newHeaders);
    };

    const handleSave = async () => {
        if (!name.trim() || !url.trim()) {
            setError('Name and URL are required');
            return;
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
                return;
            }
        }

        setError('');

        await onSave({
            name: name.trim(),
            description: description.trim() || undefined,
            method,
            url: url.trim(),
            headers: Object.keys(headersObject).length > 0 ? headersObject : undefined,
            body: bodyObject
        });
    };

    const handleExecute = async () => {
        setError('');
        setResponse(null);

        try {
            const result = await onExecute();
            setResponse(result);
        } catch (err: unknown) {
            if (err && typeof err === 'object' && 'response' in err) {
                const axiosError = err as { response?: { data?: ExecutionResponse } };
                if (axiosError.response?.data) {
                    setResponse(axiosError.response.data);
                } else {
                    setError('Request failed. Check console for details.');
                }
            } else {
                setError('Request failed. Check console for details.');
            }
        }
    };

    const addHeader = () => {
        handleStartEditing();
        setHeaders([...headers, {key: '', value: ''}]);
    };

    const removeHeader = (index: number) => {
        handleStartEditing();
        setHeaders(headers.filter((_, i) => i !== index));
    };

    const handleReleaseLock = async () => {
        try {
            await onUnlock();
            hasAutoLocked.current = false;
        } catch (err) {
            console.error('Failed to unlock:', err);
            setError('Could not release lock. Please try again.');
        }
    };

    const getStatusColor = (status: number) => {
        if (status >= 200 && status < 300) {
            return 'text-success-600 bg-success-50';
        }

        if (status >= 300 && status < 400) {
            return 'text-warning-600 bg-warning-50';
        }

        return 'text-error-600 bg-error-50';
    };

    const renderLockStatus = () => {
        if (!request) {
            return null;
        }

        if (isLockedByMe) {
            return (
                <div className="flex items-center gap-2">
                    <span
                        className="flex items-center gap-1.5 px-3 py-1.5 text-md text-success-700 bg-success-50 rounded-lg">
                        <FaLock size={14}/>
                        Locked by you
                    </span>
                    <button
                        onClick={handleReleaseLock}
                        className="flex items-center gap-1.5 px-3 py-1.5 text-md text-neutral-600 hover:bg-neutral-100 rounded-lg transition-colors cursor-pointer"
                        title="Release lock"
                    >
                        <FaLockOpen size={14}/>
                        Release
                    </button>
                </div>
            );
        }

        if (isLockedByOther) {
            return (
                <span
                    className="flex items-center gap-1.5 px-3 py-1.5 text-md text-warning-700 bg-warning-50 rounded-lg">
                    <FaLock size={14}/>
                    <FaUser size={12}/>
                    Locked by another user
                </span>
            );
        }

        return (
            <span className="flex items-center gap-1.5 px-3 py-1.5 text-md text-neutral-500 bg-neutral-100 rounded-lg">
                <FaLockOpen size={14}/>
                Unlocked
            </span>
        );
    };

    if (!request) {
        return (
            <div className="h-full flex items-center justify-center text-neutral-500">
                <div className="flex flex-col items-center gap-4">
                    <TbTerminal2 size={150}/>
                    <p>Select a request to edit</p>
                </div>
            </div>
        );
    }

    return (
        <div className="h-full flex flex-col overflow-hidden">
            <div className="flex items-center justify-between p-4 border-b border-neutral-200 shrink-0-0">
                <input
                    type="text"
                    value={name}
                    onChange={handleNameChange}
                    className="text-lg font-semibold text-neutral-900 bg-transparent border-none outline-none focus:ring-0 flex-1 mr-4"
                    placeholder="Request name"
                    disabled={!canEdit}
                />
                {renderLockStatus()}
            </div>

            <div className="px-4 py-2 border-b border-neutral-200 shrink-0-0">
                <textarea
                    value={description}
                    onChange={handleDescriptionChange}
                    disabled={!canEdit}
                    className="w-full px-3 py-2 text-md text-neutral-600 bg-neutral-50 border border-neutral-200 rounded-lg outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none disabled:bg-neutral-100 disabled:cursor-not-allowed"
                    placeholder="Add a description for this request..."
                    rows={2}
                />
            </div>

            {error && (
                <div className="mx-4 mt-4 bg-error-50 text-error-700 px-4 py-2 rounded-lg text-md shrink-0-0">
                    {error}
                </div>
            )}

            <div className="flex items-center gap-2 p-4 border-b border-neutral-200 shrink-0">
                <select
                    value={method}
                    onChange={handleMethodChange}
                    disabled={!canEdit}
                    className={`${METHOD_COLORS[method]} text-white font-bold text-md px-3 py-2 rounded-lg outline-none cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer`}
                >
                    {Object.values(HTTP_METHODS).map((m) => (
                        <option key={m} value={m}>{m}</option>
                    ))}
                </select>
                <input
                    type="text"
                    value={url}
                    onChange={handleUrlChange}
                    disabled={!canEdit}
                    className="flex-1 px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none font-mono text-md disabled:bg-neutral-100 disabled:cursor-not-allowed"
                    placeholder="https://api.example.com/endpoint"
                />
                <button
                    onClick={handleSave}
                    disabled={saving || !canEdit}
                    className="px-4 py-2 text-md font-medium text-neutral-700 border border-neutral-300 hover:bg-neutral-50 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                    {saving ? 'Saving...' : 'Save'}
                </button>
                <button
                    onClick={handleExecute}
                    disabled={executing}
                    className="px-4 py-2 text-md font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
                >
                    {executing ? 'Sending...' : 'Send'}
                </button>
            </div>

            <div className="flex border-b border-neutral-200 shrink-0">
                <button
                    onClick={() => setActiveTab('headers')}
                    className={`px-4 py-2 text-md font-medium border-b-2 transition-colors ${
                        activeTab === 'headers'
                            ? 'border-primary-600 text-primary-600'
                            : 'border-transparent text-neutral-600 hover:text-neutral-900'
                    } cursor-pointer`}
                >
                    Headers {headers.length > 0 && `(${headers.length})`}
                </button>
                <button
                    onClick={() => setActiveTab('body')}
                    className={`px-4 py-2 text-md font-medium border-b-2 transition-colors ${
                        activeTab === 'body'
                            ? 'border-primary-600 text-primary-600'
                            : 'border-transparent text-neutral-600 hover:text-neutral-900'
                    } cursor-pointer`}
                >
                    Body
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-4 min-h-0">
                {activeTab === 'headers' && (
                    <div className="space-y-2">
                        {headers.map((header, index) => (
                            <div key={index} className="flex items-center gap-2">
                                <input
                                    type="text"
                                    value={header.key}
                                    onChange={(e) => handleHeaderChange(index, 'key', e.target.value)}
                                    disabled={!canEdit}
                                    className="flex-1 px-3 py-2 border border-neutral-300 rounded-lg text-md focus:ring-2 focus:ring-primary-500 outline-none disabled:bg-neutral-100 disabled:cursor-not-allowed"
                                    placeholder="Header name"
                                />
                                <input
                                    type="text"
                                    value={header.value}
                                    onChange={(e) => handleHeaderChange(index, 'value', e.target.value)}
                                    disabled={!canEdit}
                                    className="flex-1 px-3 py-2 border border-neutral-300 rounded-lg text-md focus:ring-2 focus:ring-primary-500 outline-none disabled:bg-neutral-100 disabled:cursor-not-allowed"
                                    placeholder="Header value"
                                />
                                <button
                                    onClick={() => removeHeader(index)}
                                    disabled={!canEdit}
                                    className="p-2 text-neutral-400 hover:text-error-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                                >
                                    <IoClose size={20}/>
                                </button>
                            </div>
                        ))}
                        <button
                            onClick={addHeader}
                            disabled={!canEdit}
                            className="text-md text-primary-600 hover:text-primary-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                        >
                            + Add Header
                        </button>
                    </div>
                )}

                {activeTab === 'body' && (
                    <div>
                        {['POST', 'PUT', 'PATCH'].includes(method) ? (
                            <div className="space-y-2">
                                <div className="flex items-center justify-between">
                                    <span className="text-sm text-neutral-600">JSON</span>
                                    <button
                                        type="button"
                                        onClick={() => {
                                            try {
                                                const formatted = JSON.stringify(JSON.parse(body), null, 2);
                                                setBody(formatted);
                                            } catch {
                                            }
                                        }}
                                        disabled={!canEdit}
                                        className="text-xs text-primary-600 hover:text-primary-700 font-medium disabled:opacity-50 cursor-pointer"
                                    >
                                        FORMAT
                                    </button>
                                </div>
                                <JsonEditor
                                    value={body}
                                    onChange={setBody}
                                    disabled={!canEdit}
                                    height="250px"
                                />
                            </div>
                        ) : (
                            <p className="text-sm text-neutral-500">
                                Body is not available for {method} requests
                            </p>
                        )}
                    </div>
                )}
            </div>

            {response && (
                <div className="border-t border-neutral-200 shrink-0 max-h-[60vh] flex flex-col">
                    <div className="flex items-center gap-4 px-4 py-3 bg-neutral-50 border-b border-neutral-100">
                        <span className="text-md font-medium text-neutral-700">Response</span>
                        <span
                            className={`px-2 py-0.5 rounded text-md font-bold ${getStatusColor(response.statusCode)}`}>
                            {response.statusText}
                        </span>
                        <span className="text-md text-neutral-500">{response.responseTimeMs}ms</span>
                        {response.errorMessage && (
                            <span className="text-md text-error-600">{response.errorMessage}</span>
                        )}
                    </div>
                    <div className="flex-1 overflow-auto p-4 bg-neutral-50">
                        <pre className="text-md font-mono text-neutral-800 whitespace-pre-wrap">
                            {typeof response.body === 'string'
                                ? response.body
                                : JSON.stringify(response.body, null, 2)}
                        </pre>
                    </div>
                </div>
            )}
        </div>
    );
};

export default RequestEditor;
