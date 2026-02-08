import React from 'react';
import EndpointCard from './EndpointCard';
import type {HttpMethod, OpenApiPathItem} from '@/types';

interface EndpointListProps {
    paths: Record<string, OpenApiPathItem>;
}

const EndpointList: React.FC<EndpointListProps> = ({paths}) => {
    const endpoints = Object.entries(paths).flatMap(([path, pathItem]) =>
        Object.entries(pathItem)
            .filter(([method]) => ['get', 'post', 'put', 'patch', 'delete'].includes(method))
            .map(([method, operation]) => ({
                path,
                method: method.toUpperCase() as HttpMethod,
                operation
            }))
    );

    if (endpoints.length === 0) {
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
                    No API endpoints
                </h3>
                <p className="text-md text-neutral-500 max-w-sm">
                    Create some requests in your workspace to see them documented here.
                </p>
            </div>
        );
    }

    return (
        <div className="space-y-3">
            {endpoints.map(({path, method, operation}, index) => (
                <EndpointCard
                    key={`${method}-${path}-${index}`}
                    method={method}
                    path={path}
                    operation={operation}
                />
            ))}
        </div>
    );
};

export default EndpointList;
