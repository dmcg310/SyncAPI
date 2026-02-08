import React from 'react';
import type {HttpMethod, OpenApiOperation} from '@/types';
import {getMethodColorClass} from "../../util/util.ts";

interface EndpointCardProps {
    method: HttpMethod;
    path: string;
    operation: OpenApiOperation;
    onClick?: () => void;
}

const EndpointCard: React.FC<EndpointCardProps> = ({method, path, operation, onClick}) => {
    const paramCount = operation.parameters?.length ?? 0;
    const hasRequestBody = !!operation.requestBody;
    const responseCount = Object.keys(operation.responses).length;

    return (
        <div
            className={'bg-white rounded-xl shadow-sm border border-neutral-200 p-6 hover:shadow-md hover:border-primary-200 transition-all cursor-pointer group'}
            onClick={onClick}
        >
            <div className="flex items-start gap-3">
                <span
                    className={`${getMethodColorClass(method)} text-white px-3 py-1 rounded-md text-md font-semibold`}
                >
                    {method}
                </span>
                <div className="flex-1 min-w-0">
                    <div className="font-mono text-md text-neutral-900 font-medium break-all">
                        {path}
                    </div>
                    {operation.summary && (
                        <div className="text-md text-neutral-600 mt-1">
                            {operation.summary} {operation.description ? `- ${operation.description}` : ''}
                        </div>
                    )}
                </div>
            </div>

            <div className="flex gap-4 mt-3 text-sm text-neutral-500">
                {responseCount > 0 && (
                    <span>{responseCount} {responseCount === 1 ? 'response' : 'responses'}</span>
                )}
                {paramCount > 0 && (
                    <span>{paramCount} {paramCount === 1 ? 'parameter' : 'parameters'}</span>
                )}
                {hasRequestBody && (
                    <span>Request body</span>
                )}
            </div>
        </div>
    );
};

export default EndpointCard;
