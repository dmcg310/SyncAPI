import React, {useState} from 'react';
import {MdExpandMore} from 'react-icons/md';
import type {HttpMethod, OpenApiOperation} from '@/types';
import {getMethodColorClass} from "../../util/util.ts";
import ParameterTable from './ParameterTable';
import RequestBodyView from './RequestBodyView';
import ResponseView from './ResponseView';

interface EndpointCardProps {
    method: HttpMethod;
    path: string;
    operation: OpenApiOperation;
    onClick?: () => void;
}

const EndpointCard: React.FC<EndpointCardProps> = ({method, path, operation, onClick}) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const paramCount = operation.parameters?.length ?? 0;
    const hasRequestBody = !!operation.requestBody;
    const responseCount = Object.keys(operation.responses).length;

    const handleClick = () => {
        setIsExpanded(!isExpanded);
        onClick?.();
    };

    return (
        <div
            className={'bg-white rounded-xl shadow-sm border border-neutral-200 hover:shadow-md hover:border-primary-200 transition-all group'}
        >
            <div
                className="p-6 cursor-pointer"
                onClick={handleClick}
            >
                <div className="flex items-start justify-between gap-3">
                    <div className="flex items-start gap-3 flex-1 min-w-0">
                        <span
                            className={`${getMethodColorClass(method)} text-white px-3 py-1 rounded-md text-md font-semibold shrink-0`}
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
                    <button
                        className="text-neutral-400 hover:text-neutral-600 transition-colors shrink-0"
                        onClick={(e) => {
                            e.stopPropagation();
                            handleClick();
                        }}
                    >
                        <MdExpandMore
                            size={24}
                            className={`transition-transform ${isExpanded ? 'rotate-180' : ''}`}
                        />
                    </button>
                </div>

                <div className="flex gap-4 mt-3 text-md text-neutral-500">
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

            {isExpanded && (
                <div className="px-6 pb-6 pt-0 space-y-6 border-t border-neutral-100 mt-2">
                    {operation.parameters && operation.parameters.length > 0 && (
                        <div className="pt-4">
                            <ParameterTable parameters={operation.parameters}/>
                        </div>
                    )}

                    {operation.requestBody && (
                        <div className="pt-4">
                            <RequestBodyView requestBody={operation.requestBody}/>
                        </div>
                    )}

                    {operation.responses && Object.keys(operation.responses).length > 0 && (
                        <div className="pt-4">
                            <ResponseView responses={operation.responses}/>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default EndpointCard;
