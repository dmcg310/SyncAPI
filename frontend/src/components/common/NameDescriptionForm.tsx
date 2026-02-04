import React, {useEffect, useState} from 'react';
import {getErrorMessage} from '../../util/errors';

interface NameDescriptionFormProps {
    initialName?: string;
    initialDescription?: string;
    onSubmit: (data: { name: string; description?: string }) => Promise<void>;
    onCancel: () => void;
    loading?: boolean;
    submitLabel?: string;
    namePlaceholder?: string;
    descriptionPlaceholder?: string;
}

const NameDescriptionForm: React.FC<NameDescriptionFormProps> = ({
                                                                     initialName = '',
                                                                     initialDescription = '',
                                                                     onSubmit,
                                                                     onCancel,
                                                                     loading = false,
                                                                     submitLabel = 'Save',
                                                                     namePlaceholder = 'Name',
                                                                     descriptionPlaceholder = 'Optional description...'
                                                                 }) => {
    const [name, setName] = useState(initialName);
    const [description, setDescription] = useState(initialDescription);
    const [error, setError] = useState('');

    useEffect(() => {
        setName(initialName);
        setDescription(initialDescription);
    }, [initialName, initialDescription]);

    const handleSubmit = async (e: React.FormEvent) => {
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
        } catch (err) {
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
                    placeholder={namePlaceholder}
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
                    placeholder={descriptionPlaceholder}
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
                    {loading ? 'Saving...' : submitLabel}
                </button>
            </div>
        </form>
    );
};

export default NameDescriptionForm;
