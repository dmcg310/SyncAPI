import React, {useState} from 'react';
import type {ApiRequestRequest, HttpMethod} from '@/types';
import {HTTP_METHODS} from '../../util/constants';

interface RequestFormProps {
    onSubmit: (data: ApiRequestRequest) => Promise<void>;
    onCancel: () => void;
    loading?: boolean;
}

const RequestForm: React.FC<RequestFormProps> = ({onSubmit, onCancel, loading = false}) => {
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [method, setMethod] = useState<HttpMethod>('GET');
    const [url, setUrl] = useState('');
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.SubmitEvent) => {
        e.preventDefault();

        setError('');

        if (!name.trim()) {
            setError('Name is required');
            return;
        }

        if (!url.trim()) {
            setError('URL is required');
            return;
        }

        try {
            await onSubmit({
                name: name.trim(),
                description: description.trim() || undefined,
                method,
                url: url.trim()
            });
        } catch (err: unknown) {
            if (err && typeof err === 'object' && 'response' in err) {
                const axiosError = err as { response?: { data?: { message?: string } } };
                setError(axiosError.response?.data?.message || 'An error occurred');
            } else {
                setError('An error occurred');
            }
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
                <label htmlFor="requestName" className="block text-md font-medium text-neutral-700 mb-2">
                    Name
                </label>
                <input
                    id="requestName"
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
                    placeholder="Get All Users"
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
                    placeholder="Optional description for this request..."
                />
            </div>

            <div>
                <label htmlFor="requestMethod" className="block text-md font-medium text-neutral-700 mb-2">
                    Method
                </label>
                <select
                    id="requestMethod"
                    value={method}
                    onChange={(e) => setMethod(e.target.value as HttpMethod)}
                    className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors"
                >
                    {Object.values(HTTP_METHODS).map((m) => (
                        <option key={m} value={m}>{m}</option>
                    ))}
                </select>
            </div>

            <div>
                <label htmlFor="requestUrl" className="block text-md font-medium text-neutral-700 mb-2">
                    URL
                </label>
                <input
                    id="requestUrl"
                    type="text"
                    value={url}
                    onChange={(e) => setUrl(e.target.value)}
                    className="w-full px-4 py-2 border border-neutral-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500 outline-none transition-colors font-mono text-md"
                    placeholder="https://api.example.com/users"
                    required
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
                    {loading ? 'Creating...' : 'Create'}
                </button>
            </div>
        </form>
    );
};

export default RequestForm;
