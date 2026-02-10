import React, {useRef, useState} from 'react';
import {MdRefresh} from 'react-icons/md';
import DocsSidebar from './DocsSidebar';
import EndpointCard from './EndpointCard';
import ExportButton from './ExportButton';
import Spinner from '../common/Spinner';
import type {HttpMethod, OpenApiSpec} from '@/types';
import {HTTP_METHODS_ARRAY} from "../../util/constants.ts";

interface DocumentationViewProps {
    spec: OpenApiSpec | null;
    loading: boolean;
    error: string | null;
    onRefresh: () => void;
}

const DocumentationView: React.FC<DocumentationViewProps> = ({spec, loading, error, onRefresh}) => {
    const [selectedEndpoint, setSelectedEndpoint] = useState<string | null>(null);
    const endpointRefs = useRef<Record<string, HTMLDivElement | null>>({});


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
                <p className="text-md text-neutral-500">
                    Documentation could not be loaded.
                </p>
            </div>
        );
    }

    const endpointCount = Object.values(spec.paths).reduce((total, pathItem) => {
        const ops = Object.keys(pathItem).filter(key =>
            HTTP_METHODS_ARRAY.includes(key as typeof HTTP_METHODS_ARRAY[number])
        );

        return total + ops.length;
    }, 0);

    const handleSelectEndpoint = (method: HttpMethod, path: string) => {
        const endpointKey = `${method}-${path}`;
        setSelectedEndpoint(endpointKey);

        const element = endpointRefs.current[endpointKey];
        if (element) {
            element.scrollIntoView({behavior: 'smooth', block: 'start'});
        }
    };

    const endpoints = Object.entries(spec.paths)
        .sort(([pathA], [pathB]) => pathA.localeCompare(pathB))
        .flatMap(([path, pathItem]) =>
            Object.entries(pathItem)
                .filter(([method]) => ['get', 'post', 'put', 'patch', 'delete'].includes(method))
                .sort(([methodA], [methodB]) => methodA.localeCompare(methodB))
                .map(([method, operation]) => ({
                    path,
                    method: method.toUpperCase() as HttpMethod,
                    operation,
                    servers: pathItem.servers
                }))
        );

    return (
        <div className="min-h-screen flex">
            <div className="w-80 bg-white border-r border-neutral-200 shrink-0">
                <DocsSidebar
                    paths={spec.paths}
                    selectedEndpoint={selectedEndpoint}
                    onSelectEndpoint={handleSelectEndpoint}
                />
            </div>

            <div className="flex-1 overflow-y-auto bg-neutral-50">
                <div className="max-w-4xl mx-auto p-6">
                    <div className="mb-8">
                        <div className="flex items-start justify-between gap-4 mb-3">
                        </div>
                        <div className="flex items-center justify-between gap-4 text-md text-neutral-500">
                            <p className="mt-1 text-md">
                                OpenAPI {spec.openapi} - Version {spec.info.version}{" "}
                                - {endpointCount} {endpointCount === 1 ? 'endpoint' : 'endpoints'}
                            </p>

                            <div className="flex items-center gap-3">
                                <ExportButton spec={spec}/>
                                <button
                                    onClick={onRefresh}
                                    disabled={loading}
                                    className="flex items-center gap-2 px-3 py-2 text-md font-medium text-neutral-700 bg-white border border-neutral-200 rounded-lg hover:bg-neutral-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed shrink-0 cursor-pointer"
                                    title="Refresh documentation"
                                >
                                    <MdRefresh size={18} className={loading ? 'animate-spin' : ''}/>
                                    Refresh
                                </button>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-4">
                        {endpoints.map(({path, method, operation, servers}) => {
                            const endpointKey = `${method}-${path}`;
                            return (
                                <div
                                    key={endpointKey}
                                    ref={(el) => {
                                        endpointRefs.current[endpointKey] = el;
                                    }}
                                >
                                    <EndpointCard
                                        method={method}
                                        path={path}
                                        operation={operation}
                                        servers={servers}
                                        onClick={() => handleSelectEndpoint(method, path)}
                                    />
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DocumentationView;
