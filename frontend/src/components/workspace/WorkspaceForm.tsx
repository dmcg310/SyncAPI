import React, {useEffect, useState} from 'react';
import type {Workspace, WorkspaceRequest} from '@/types';
import {getErrorMessage} from "../../util/errors";

interface WorkspaceFormProps {
    workspace?: Workspace | null;
    onSubmit: (data: WorkspaceRequest) => Promise<void>;
    onCancel: () => void;
    loading?: boolean;
}

const WorkspaceForm: React.FC<WorkspaceFormProps> = ({workspace, onSubmit, onCancel, loading = false}) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        if (workspace) {
            setName(workspace.name);
            setDescription(workspace.description || '');
        } else {
            setName('');
            setDescription('');
        }
    }, [workspace]);

    const handleSubmit = async (e: React.SubmitEvent) => {
        e.preventDefault();

        setError('');

        if (!name.trim()) {
            setError('Name is required');
            return;
        }

        try {
            await onSubmit({
                name: name.trim(),
                description: description.trim() || undefined
            });
        } catch (err: unknown) {
            setError(getErrorMessage(err));
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
                <div className="bg-error-50 text-error-700 px-4 py-3 rounded-lg text-md">
                    {error}
                </div>
            )}

            <div>
                <label htmlFor="name" className="block text-md font-medium text-neutral-700 mb-2">
                    Name
                </label>
                <input
                    id="name"
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
                    placeholder="My API Project"
                    required
                    autoFocus
                />
            </div>

            <div>
                <label htmlFor="description" className="block text-md font-medium text-neutral-700 mb-2">
                    Description
                </label>
                <textarea
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    rows={3}
                    className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors resize-none"
                    placeholder="Optional description for this workspace..."
                />
            </div>

            <div className="flex justify-end gap-3 pt-4">
                <button
                    type="button"
                    onClick={onCancel}
                    disabled={loading}
                    className="px-4 py-2 text-md font-medium text-neutral-700 hover:bg-neutral-100 rounded-lg transition-colors disabled:opacity-50 cursor-pointer"
                >
                    Cancel
                </button>
                <button
                    type="submit"
                    disabled={loading}
                    className="px-4 py-2 text-md font-medium text-white bg-primary-600 hover:bg-primary-700 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                    {loading ? 'Saving...' : workspace ? 'Update' : 'Create'}
                </button>
            </div>
        </form>
    );
};

export default WorkspaceForm;
