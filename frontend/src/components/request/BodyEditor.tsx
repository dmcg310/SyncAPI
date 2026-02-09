import React from 'react';
import JsonEditor from '../common/JsonEditor';
import VariablePreview from '../common/VariablePreview';
import type {EnvironmentVariable} from '@/types';

interface BodyEditorProps {
    method: string;
    body: string;
    canEdit: boolean;
    onChange: (value: string) => void;
    variables?: EnvironmentVariable[];
    activeEnvironmentName?: string;
}

const BodyEditor: React.FC<BodyEditorProps> = ({
                                                   method,
                                                   body,
                                                   canEdit,
                                                   onChange,
                                                   variables = [],
                                                   activeEnvironmentName
                                               }) => {
    if (!['POST', 'PUT', 'PATCH'].includes(method)) {
        return <p className="text-md text-neutral-500">Body is not available for {method} requests</p>;
    }

    const formatJson = () => {
        try {
            onChange(JSON.stringify(JSON.parse(body), null, 2));
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
            <VariablePreview
                text={body}
                variables={variables}
                activeEnvironmentName={activeEnvironmentName}
            />
        </div>
    );
};

export default BodyEditor;
