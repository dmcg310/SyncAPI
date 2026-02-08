import React from 'react';
import EndpointList from './EndpointList';
import Spinner from '../common/Spinner';
import type {OpenApiSpec} from '@/types';

interface DocumentationViewProps {
    spec: OpenApiSpec | null;
    loading: boolean;
    error: string | null;
}

const DocumentationView: React.FC<DocumentationViewProps> = ({spec, loading, error}) => {
    if (loading) {
        return (
            <div className="flex justify-center items-center py-16">
                <Spinner/>
            </div>
        );
    }

    if (error) {
        return (
            <div className="mx-4 mt-4 bg-error-50 text-error-700 px-4 py-3 rounded-lg">
                {error}
            </div>
        );
    }

    if (!spec) {
        return (
            <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="text-neutral-400 mb-2">
                    <svg
                        className="w-16 h-16 mx-auto"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={1.5}
                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                        />
                    </svg>
                </div>
                <h3 className="text-lg font-medium text-neutral-900 mb-1">
                    No documentation available
                </h3>
                <p className="text-sm text-neutral-500">
                    Documentation could not be loaded.
                </p>
            </div>
        );
    }

    const endpointCount = Object.values(spec.paths).reduce((count, pathItem) => {
        return count + Object.keys(pathItem).length;
    }, 0);

    return (
        <div className="max-w-5xl mx-auto p-6">
            <div className="mb-6">
                <div className="flex items-center gap-4 mt-3 text-md text-neutral-500">
                    <span>OpenAPI {spec.openapi}</span>
                    <span>•</span>
                    <span>Version {spec.info.version}</span>
                    <span>•</span>
                    <span>{endpointCount} {endpointCount === 1 ? 'endpoint' : 'endpoints'}</span>
                </div>
            </div>

            <div className="border-t border-neutral-200 pt-6">
                <h2 className="text-lg font-semibold text-neutral-900 mb-4">
                    API Endpoints
                </h2>
                <EndpointList paths={spec.paths}/>
            </div>
        </div>
    );
};

export default DocumentationView;
