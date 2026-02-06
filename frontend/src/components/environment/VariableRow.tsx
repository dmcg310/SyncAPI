import React, {useEffect, useRef, useState} from 'react';
import {MdCheck, MdClose, MdDeleteForever, MdEdit} from 'react-icons/md';
import type {EnvironmentVariable} from '@/types';

interface VariableRowProps {
    variable: EnvironmentVariable;
    onUpdate: (variableId: number, key: string, value: string) => Promise<void>;
    onDeleteClick: (variable: EnvironmentVariable) => void;
    disabled?: boolean;
}

const VariableRow: React.FC<VariableRowProps> = ({
                                                     variable,
                                                     onUpdate,
                                                     onDeleteClick,
                                                     disabled = false
                                                 }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [editKey, setEditKey] = useState(variable.key);
    const [editValue, setEditValue] = useState(variable.value);
    const [isSaving, setIsSaving] = useState(false);
    const keyInputRef = useRef<HTMLInputElement>(null);

    useEffect(() => {
        if (isEditing && keyInputRef.current) {
            keyInputRef.current.focus();
        }
    }, [isEditing]);

    const handleSave = async () => {
        if (!editKey.trim()) {
            return;
        }

        setIsSaving(true);
        try {
            await onUpdate(variable.id, editKey.trim(), editValue.trim());
            setIsEditing(false);
        } catch (error) {
            console.error('Failed to update variable:', error);
        } finally {
            setIsSaving(false);
        }
    };

    const handleCancel = () => {
        setEditKey(variable.key);
        setEditValue(variable.value);
        setIsEditing(false);
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleSave();
        } else if (e.key === 'Escape') {
            handleCancel();
        }
    };

    const handleDelete = () => {
        onDeleteClick(variable);
    };

    if (isEditing) {
        return (
            <tr className="border-b border-neutral-200 hover:bg-neutral-50">
                <td className="px-4 py-2">
                    <input
                        ref={keyInputRef}
                        type="text"
                        value={editKey}
                        onChange={(e) => setEditKey(e.target.value)}
                        onKeyDown={handleKeyDown}
                        disabled={isSaving}
                        className="w-full px-2 py-1 border border-neutral-300 rounded focus:outline-none focus:border-primary-500 text-md"
                        placeholder="Key"
                    />
                </td>
                <td className="px-4 py-2">
                    <input
                        type="text"
                        value={editValue}
                        onChange={(e) => setEditValue(e.target.value)}
                        onKeyDown={handleKeyDown}
                        disabled={isSaving}
                        className="w-full px-2 py-1 border border-neutral-300 rounded focus:outline-none focus:border-primary-500 text-md"
                        placeholder="Value"
                    />
                </td>
                <td className="px-4 py-2">
                    <div className="flex gap-1">
                        <button
                            onClick={handleSave}
                            disabled={isSaving || !editKey.trim()}
                            className="p-1.5 text-success-600 hover:bg-success-50 rounded transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                            title="Save"
                        >
                            <MdCheck size={18}/>
                        </button>
                        <button
                            onClick={handleCancel}
                            disabled={isSaving}
                            className="p-1.5 text-neutral-600 hover:bg-neutral-100 rounded transition-colors cursor-pointer disabled:opacity-50"
                            title="Cancel"
                        >
                            <MdClose size={18}/>
                        </button>
                    </div>
                </td>
            </tr>
        );
    }

    return (
        <tr className="group border-b border-neutral-200 hover:bg-neutral-50">
            <td className="px-4 py-2 font-mono text-md text-neutral-900">
                {variable.key}
            </td>
            <td className="px-4 py-2 font-mono text-md text-neutral-600">
                {variable.value}
            </td>
            <td className="px-4 py-2">
                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                        onClick={() => setIsEditing(true)}
                        disabled={disabled}
                        className="p-1.5 text-neutral-400 hover:text-primary-600 hover:bg-primary-50 rounded transition-colors cursor-pointer disabled:opacity-50"
                        title="Edit variable"
                    >
                        <MdEdit size={18}/>
                    </button>
                    <button
                        onClick={handleDelete}
                        disabled={disabled}
                        className="p-1.5 text-neutral-400 hover:text-error-600 hover:bg-error-50 rounded transition-colors cursor-pointer disabled:opacity-50"
                        title="Delete variable"
                    >
                        <MdDeleteForever size={20}/>
                    </button>
                </div>
            </td>
        </tr>
    );
};

export default VariableRow;
