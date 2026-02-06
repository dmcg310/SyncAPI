import React from 'react';
import type {ApiRequest, ApiRequestRequest, EnvironmentVariable, ExecutionResponse, HttpMethod} from '@/types';
import {HTTP_METHODS, METHOD_COLORS} from '../../util/constants';
import {useRequestEditor} from '../../hooks';
import {TbTerminal2} from 'react-icons/tb';
import LockStatus from './LockStatus';
import HeadersEditor from './HeadersEditor';
import BodyEditor from './BodyEditor';
import ResponseViewer from './ResponseViewer';
import VariablePreview from '../common/VariablePreview';
import {getErrorMessage} from "../../util/errors.ts";

interface RequestEditorProps {
    request: ApiRequest | null;
    onSave: (data: ApiRequestRequest) => Promise<void>;
    onExecute: () => Promise<ExecutionResponse>;
    onLock: () => Promise<void>;
    onUnlock: () => Promise<void>;
    saving: boolean;
    executing: boolean;
    variables?: EnvironmentVariable[];
    activeEnvironmentName?: string;
}

const RequestEditor: React.FC<RequestEditorProps> = ({
                                                         request,
                                                         onSave,
                                                         onExecute,
                                                         onLock,
                                                         onUnlock,
                                                         saving,
                                                         executing,
                                                         variables = [],
                                                         activeEnvironmentName
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
            editor.setResponse(await onExecute());
        } catch (err: unknown) {
            if (err && typeof err === 'object' && 'response' in err) {
                const axiosError = err as { response?: { data?: ExecutionResponse } };
                if (axiosError.response?.data) {
                    editor.setResponse(axiosError.response.data);
                    return;
                }
            }

            editor.setError(getErrorMessage(err, 'Request failed. Check console for details.'));
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

            <div className="p-4 border-b border-neutral-200 shrink-0 space-y-2">
                <div className="flex items-center gap-2">
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
                <VariablePreview
                    text={editor.url}
                    variables={variables}
                    activeEnvironmentName={activeEnvironmentName}
                />
            </div>

            <div className="flex border-b border-neutral-200 shrink-0">
                {(['headers', 'body'] as const).map((tab) => (
                    <button
                        key={tab}
                        onClick={() => editor.setActiveTab(tab)}
                        className={`px-4 py-2 text-md font-medium border-b-2 transition-colors cursor-pointer ${
                            editor.activeTab === tab
                                ? 'border-primary-600 text-primary-600'
                                : 'border-transparent text-neutral-600 hover:text-neutral-900'
                        }`}
                    >
                        {tab === 'headers' ? `Headers ${editor.headers.length > 0 ? `(${editor.headers.length})` : ''}` : 'Body'}
                    </button>
                ))}
            </div>

            <div className="flex-1 overflow-y-auto p-4 min-h-0">
                {editor.activeTab === 'headers' ? (
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
                ) : (
                    <BodyEditor
                        method={editor.method}
                        body={editor.body}
                        canEdit={editor.canEdit}
                        onChange={(v) => handleFieldChange(editor.setBody)(v)}
                        variables={variables}
                        activeEnvironmentName={activeEnvironmentName}
                    />
                )}
            </div>

            {editor.response && <ResponseViewer response={editor.response}/>}
        </div>
    );
};

export default RequestEditor;
