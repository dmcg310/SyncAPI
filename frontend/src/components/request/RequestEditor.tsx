import React from 'react';
import type {ApiRequest, ApiRequestRequest, ExecutionResponse, HttpMethod} from '@/types';
import {HTTP_METHODS, METHOD_COLORS} from '../../util/constants';
import {useRequestEditor} from '../../hooks';
import {TbTerminal2} from 'react-icons/tb';
import {FaLock, FaLockOpen, FaUser} from 'react-icons/fa';
import {IoClose} from 'react-icons/io5';
import JsonEditor from '../common/JsonEditor';

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
    const editor = useRequestEditor(request);

    const handleStartEditing = async () => {
        if (!request || editor.isLocked || editor.hasAutoLocked) {
            return;
        }

        editor.markAutoLocked();

        try {
            await onLock();
        } catch (err) {
            console.error('Failed to auto-lock:', err);
            editor.setError('Could not lock this request. Please try again.');
            editor.resetAutoLock();
        }
    };

    const handleFieldChange = <T, >(setter: (value: T) => void) => (value: T) => {
        handleStartEditing();
        setter(value);
    };

    const handleSave = async () => {
        const data = editor.buildRequestData();
        if (!data) {
            return;
        }

        await onSave(data);
    };

    const handleExecute = async () => {
        editor.setError('');
        editor.setResponse(null);

        try {
            const result = await onExecute();
            editor.setResponse(result);
        } catch (err: unknown) {
            if (err && typeof err === 'object' && 'response' in err) {
                const axiosError = err as { response?: { data?: ExecutionResponse } };
                if (axiosError.response?.data) {
                    editor.setResponse(axiosError.response.data);
                } else {
                    editor.setError('Request failed. Check console for details.');
                }
            } else {
                editor.setError('Request failed. Check console for details.');
            }
        }
    };

    const handleReleaseLock = async () => {
        try {
            await onUnlock();
            editor.resetAutoLock();
        } catch (err) {
            console.error('Failed to unlock:', err);
            editor.setError('Could not release lock. Please try again.');
        }
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
            <div className="flex items-center justify-between p-4 border-b border-neutral-200 shrink-0">
                <input
                    type="text"
                    value={editor.name}
                    onChange={(e) => handleFieldChange(editor.setName)(e.target.value)}
                    className="text-lg font-semibold text-neutral-900 bg-transparent border-none outline-none focus:ring-0 flex-1 mr-4"
                    placeholder="Request name"
                    disabled={!editor.canEdit}
                />
                <LockStatus
                    isLockedByMe={editor.isLockedByMe}
                    isLockedByOther={editor.isLockedByOther}
                    onRelease={handleReleaseLock}
                />
            </div>

            <div className="px-4 py-2 border-b border-neutral-200 shrink-0">
                <textarea
                    value={editor.description}
                    onChange={(e) => handleFieldChange(editor.setDescription)(e.target.value)}
                    disabled={!editor.canEdit}
                    className="w-full px-3 py-2 text-md text-neutral-600 bg-neutral-50 border border-neutral-200 rounded-lg outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500 resize-none disabled:bg-neutral-100 disabled:cursor-not-allowed"
                    placeholder="Add a description for this request..."
                    rows={2}
                />
            </div>

            {editor.error && (
                <div className="mx-4 mt-4 bg-error-50 text-error-700 px-4 py-2 rounded-lg text-md shrink-0">
                    {editor.error}
                </div>
            )}

            <div className="flex items-center gap-2 p-4 border-b border-neutral-200 shrink-0">
                <select
                    value={editor.method}
                    onChange={(e) => handleFieldChange(editor.setMethod)(e.target.value as HttpMethod)}
                    disabled={!editor.canEdit}
                    className={`${METHOD_COLORS[editor.method]} text-white font-bold text-md px-3 py-2 rounded-lg outline-none cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed`}
                >
                    {Object.values(HTTP_METHODS).map((m) => (
                        <option key={m} value={m}>{m}</option>
                    ))}
                </select>
                <input
                    type="text"
                    value={editor.url}
                    onChange={(e) => handleFieldChange(editor.setUrl)(e.target.value)}
                    disabled={!editor.canEdit}
                    className="flex-1 px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none font-mono text-md disabled:bg-neutral-100 disabled:cursor-not-allowed"
                    placeholder="https://api.example.com/endpoint"
                />
                <button
                    onClick={handleSave}
                    disabled={saving || !editor.canEdit}
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
                    onClick={() => editor.setActiveTab('headers')}
                    className={`px-4 py-2 text-md font-medium border-b-2 transition-colors cursor-pointer ${
                        editor.activeTab === 'headers'
                            ? 'border-primary-600 text-primary-600'
                            : 'border-transparent text-neutral-600 hover:text-neutral-900'
                    }`}
                >
                    Headers {editor.headers.length > 0 && `(${editor.headers.length})`}
                </button>
                <button
                    onClick={() => editor.setActiveTab('body')}
                    className={`px-4 py-2 text-md font-medium border-b-2 transition-colors cursor-pointer ${
                        editor.activeTab === 'body'
                            ? 'border-primary-600 text-primary-600'
                            : 'border-transparent text-neutral-600 hover:text-neutral-900'
                    }`}
                >
                    Body
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-4 min-h-0">
                {editor.activeTab === 'headers' && (
                    <HeadersEditor
                        headers={editor.headers}
                        canEdit={editor.canEdit}
                        onAdd={() => {
                            handleStartEditing();
                            editor.addHeader();
                        }}
                        onRemove={(i) => {
                            handleStartEditing();
                            editor.removeHeader(i);
                        }}
                        onChange={(i, f, v) => {
                            handleStartEditing();
                            editor.updateHeader(i, f, v);
                        }}
                    />
                )}

                {editor.activeTab === 'body' && (
                    <BodyEditor
                        method={editor.method}
                        body={editor.body}
                        canEdit={editor.canEdit}
                        onChange={(v) => handleFieldChange(editor.setBody)(v)}
                    />
                )}
            </div>

            {editor.response && (
                <ResponseViewer response={editor.response}/>
            )}
        </div>
    );
};

interface LockStatusProps {
    isLockedByMe: boolean;
    isLockedByOther: boolean;
    onRelease: () => void;
}

const LockStatus: React.FC<LockStatusProps> = ({isLockedByMe, isLockedByOther, onRelease}) => {
    if (isLockedByMe) {
        return (
            <div className="flex items-center gap-2">
                <span
                    className="flex items-center gap-1.5 px-3 py-1.5 text-md text-success-700 bg-success-50 rounded-lg">
                    <FaLock size={14}/>
                    Locked by you
                </span>
                <button
                    onClick={onRelease}
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
            <span className="flex items-center gap-1.5 px-3 py-1.5 text-md text-warning-700 bg-warning-50 rounded-lg">
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

interface HeadersEditorProps {
    headers: Array<{ key: string; value: string }>;
    canEdit: boolean;
    onAdd: () => void;
    onRemove: (index: number) => void;
    onChange: (index: number, field: 'key' | 'value', value: string) => void;
}

const HeadersEditor: React.FC<HeadersEditorProps> = ({headers, canEdit, onAdd, onRemove, onChange}) => (
    <div className="space-y-2">
        {headers.map((header, index) => (
            <div key={index} className="flex items-center gap-2">
                <input
                    type="text"
                    value={header.key}
                    onChange={(e) => onChange(index, 'key', e.target.value)}
                    disabled={!canEdit}
                    className="flex-1 px-3 py-2 border border-neutral-300 rounded-lg text-md focus:ring-2 focus:ring-primary-500 outline-none disabled:bg-neutral-100 disabled:cursor-not-allowed"
                    placeholder="Header name"
                />
                <input
                    type="text"
                    value={header.value}
                    onChange={(e) => onChange(index, 'value', e.target.value)}
                    disabled={!canEdit}
                    className="flex-1 px-3 py-2 border border-neutral-300 rounded-lg text-md focus:ring-2 focus:ring-primary-500 outline-none disabled:bg-neutral-100 disabled:cursor-not-allowed"
                    placeholder="Header value"
                />
                <button
                    onClick={() => onRemove(index)}
                    disabled={!canEdit}
                    className="p-2 text-neutral-400 hover:text-error-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                    <IoClose size={20}/>
                </button>
            </div>
        ))}
        <button
            onClick={onAdd}
            disabled={!canEdit}
            className="text-md text-primary-600 hover:text-primary-700 font-medium disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
        >
            + Add Header
        </button>
    </div>
);

interface BodyEditorProps {
    method: string;
    body: string;
    canEdit: boolean;
    onChange: (value: string) => void;
}

const BodyEditor: React.FC<BodyEditorProps> = ({method, body, canEdit, onChange}) => {
    if (!['POST', 'PUT', 'PATCH'].includes(method)) {
        return <p className="text-sm text-neutral-500">Body is not available for {method} requests</p>;
    }

    const formatJson = () => {
        try {
            const formatted = JSON.stringify(JSON.parse(body), null, 2);
            onChange(formatted);
        } catch {
        }
    };

    return (
        <div className="space-y-2">
            <div className="flex items-center justify-between">
                <span className="text-sm text-neutral-600">JSON</span>
                <button
                    type="button"
                    onClick={formatJson}
                    disabled={!canEdit}
                    className="text-xs text-primary-600 hover:text-primary-700 font-medium disabled:opacity-50 cursor-pointer"
                >
                    FORMAT
                </button>
            </div>
            <JsonEditor value={body} onChange={onChange} disabled={!canEdit} height="250px"/>
        </div>
    );
};

interface ResponseViewerProps {
    response: ExecutionResponse;
}

const ResponseViewer: React.FC<ResponseViewerProps> = ({response}) => {
    const getStatusColor = (status: number) => {
        if (status >= 200 && status < 300) {
            return 'text-success-600 bg-success-50';
        }

        if (status >= 300 && status < 400) {
            return 'text-warning-600 bg-warning-50';
        }

        return 'text-error-600 bg-error-50';
    };

    return (
        <div className="border-t border-neutral-200 shrink-0 max-h-[60vh] flex flex-col">
            <div className="flex items-center gap-4 px-4 py-3 bg-neutral-50 border-b border-neutral-100">
                <span className="text-md font-medium text-neutral-700">Response</span>
                <span className={`px-2 py-0.5 rounded text-md font-bold ${getStatusColor(response.statusCode)}`}>
                    {response.statusText}
                </span>
                <span className="text-md text-neutral-500">{response.responseTimeMs}ms</span>
                {response.errorMessage && (
                    <span className="text-md text-error-600">{response.errorMessage}</span>
                )}
            </div>
            <div className="flex-1 overflow-auto p-4 bg-neutral-50">
                <pre className="text-md font-mono text-neutral-800 whitespace-pre-wrap">
                    {typeof response.body === 'string' ? response.body : JSON.stringify(response.body, null, 2)}
                </pre>
            </div>
        </div>
    );
};

export default RequestEditor;
