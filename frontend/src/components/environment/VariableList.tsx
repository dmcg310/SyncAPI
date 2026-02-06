import React, {useState} from 'react';
import {IoAdd} from 'react-icons/io5';
import {MdCheck, MdClose} from 'react-icons/md';
import VariableRow from './VariableRow';
import type {EnvironmentVariable} from '@/types';
import {VscSymbolVariable} from "react-icons/vsc";
import Spinner from "../common/Spinner.tsx";

interface VariableListProps {
    variables: EnvironmentVariable[];
    onAdd: (key: string, value: string) => Promise<void>;
    onUpdate: (variableId: number, key: string, value: string) => Promise<void>;
    onDelete: (variableId: number) => Promise<void>;
    loading?: boolean;
}

const VariableList: React.FC<VariableListProps> = ({
                                                       variables,
                                                       onAdd,
                                                       onUpdate,
                                                       onDelete,
                                                       loading = false
                                                   }) => {
    const [isAdding, setIsAdding] = useState(false);
    const [newKey, setNewKey] = useState('');
    const [newValue, setNewValue] = useState('');
    const [isSaving, setIsSaving] = useState(false);

    const handleAdd = async () => {
        if (!newKey.trim()) {
            return;
        }

        setIsSaving(true);
        try {
            await onAdd(newKey.trim(), newValue.trim());
            setNewKey('');
            setNewValue('');
            setIsAdding(false);
        } catch (error) {
            console.error('Failed to add variable:', error);
        } finally {
            setIsSaving(false);
        }
    };

    const handleCancel = () => {
        setNewKey('');
        setNewValue('');
        setIsAdding(false);
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            handleAdd();
        } else if (e.key === 'Escape') {
            handleCancel();
        }
    };

    return (
        <div className="flex flex-col h-full">
            <div className="flex items-center justify-between pb-4 border-b border-neutral-200">
                <h3 className="font-semibold text-neutral-900">Variables</h3>
                {!isAdding && (
                    <button
                        onClick={() => setIsAdding(true)}
                        disabled={loading}
                        className="p-1.5 text-neutral-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors cursor-pointer disabled:opacity-50"
                        title="Add Variable"
                    >
                        <IoAdd size={24}/>
                    </button>
                )}
            </div>

            <div className="flex-1 overflow-y-auto">
                {loading ? (
                    <div className="flex items-center justify-center py-8">
                        <Spinner/>
                    </div>
                ) : variables.length === 0 && !isAdding ? (
                    <div className="text-center py-8 px-4">
                        <div
                            className="w-12 h-12 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-3">
                            <VscSymbolVariable size={24} className="text-neutral-400"/>
                        </div>
                        <p className="text-md text-neutral-600 mb-1">No variables yet</p>
                        <p className="text-md text-neutral-400">Add variables to use in your requests</p>
                    </div>
                ) : (
                    <table className="w-full mt-4">
                        <thead>
                        <tr className="border-b border-neutral-300">
                            <th className="px-4 py-2 text-left text-md font-semibold text-neutral-700">
                                Key
                            </th>
                            <th className="px-4 py-2 text-left text-md font-semibold text-neutral-700">
                                Value
                            </th>
                            <th className="px-4 py-2 text-left text-md font-semibold text-neutral-700">
                                Actions
                            </th>
                        </tr>
                        </thead>
                        <tbody>
                        {isAdding && (
                            <tr className="border-b border-neutral-200 bg-primary-50/30">
                                <td className="px-4 py-2">
                                    <input
                                        type="text"
                                        value={newKey}
                                        onChange={(e) => setNewKey(e.target.value)}
                                        onKeyDown={handleKeyDown}
                                        disabled={isSaving}
                                        autoFocus
                                        className="w-full px-2 py-1 border border-neutral-300 rounded focus:outline-none focus:border-primary-500 text-md"
                                        placeholder="e.g., api_key, base_url"
                                    />
                                </td>
                                <td className="px-4 py-2">
                                    <input
                                        type="text"
                                        value={newValue}
                                        onChange={(e) => setNewValue(e.target.value)}
                                        onKeyDown={handleKeyDown}
                                        disabled={isSaving}
                                        className="w-full px-2 py-1 border border-neutral-300 rounded focus:outline-none focus:border-primary-500 text-md"
                                        placeholder="e.g., abc123, https://api.example.com"
                                    />
                                </td>
                                <td className="px-4 py-2">
                                    <div className="flex gap-1">
                                        <button
                                            onClick={handleAdd}
                                            disabled={isSaving || !newKey.trim()}
                                            className="p-1.5 text-success-600 hover:bg-success-50 rounded transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                                            title="Add"
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
                        )}
                        {variables.map((variable) => (
                            <VariableRow
                                key={variable.id}
                                variable={variable}
                                onUpdate={onUpdate}
                                onDelete={onDelete}
                                disabled={loading}
                            />
                        ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default VariableList;
