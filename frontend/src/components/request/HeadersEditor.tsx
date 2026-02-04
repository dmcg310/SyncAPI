import React from 'react';
import {IoClose} from 'react-icons/io5';

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

export default HeadersEditor;
