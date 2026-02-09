import React, {useState} from 'react';
import {MdExpandMore} from 'react-icons/md';
import type {HttpMethod, OpenApiOperation} from '@/types';
import {getMethodColorClass} from "../../util/util.ts";
import ParameterTable from './ParameterTable';
import RequestBodyView from './RequestBodyView';
import ResponseView from './ResponseView';
import CodeSnippet from './CodeSnippet';
import CopyButton from './CopyButton';
import {generateAxiosSnippet, generateCurlSnippet, generateFetchSnippet} from '../../util/snippets';

interface EndpointCardProps {
    method: HttpMethod;
    path: string;
    operation: OpenApiOperation;
    onClick?: () => void;
}

const EndpointCard: React.FC<EndpointCardProps> = ({method, path, operation, onClick}) => {
    const [isExpanded, setIsExpanded] = useState(false);
    const [snippetType, setSnippetType] = useState<'cURL' | 'fetch' | 'axios'>('cURL');
    const paramCount = operation.parameters?.length ?? 0;
    const hasRequestBody = !!operation.requestBody;
    const responseCount = Object.keys(operation.responses).length;

    const handleClick = () => {
        setIsExpanded(!isExpanded);
        onClick?.();
    };

    const snippetOptions = {method, path, operation};
    const snippets = {
        cURL: generateCurlSnippet(snippetOptions),
        fetch: generateFetchSnippet(snippetOptions),
        axios: generateAxiosSnippet(snippetOptions)
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
                    <div className="pt-4">
                        <h3 className="text-md font-semibold text-neutral-900 mb-3">Endpoint URL</h3>
                        <div className="pr-4 flex items-center rounded-lg bg-neutral-50 border border-neutral-200">
                            <code
                                className="flex-1 rounded-md px-3 py-2 text-md font-mono text-neutral-900">
                                https://api.example.com{path}
                            </code>
                            <CopyButton text={`https://api.example.com${path}`} label="Copy URL"/>
                        </div>
                    </div>

                    <div className="pt-2">
                        <div className="flex items-center justify-between mb-3">
                            <h3 className="text-md font-semibold text-neutral-900">Code Examples</h3>
                            <div className="flex gap-2">
                                <button
                                    onClick={() => setSnippetType('cURL')}
                                    className={`px-3 py-1 text-md font-medium rounded-md transition-colors cursor-pointer ${
                                        snippetType === 'cURL'
                                            ? 'bg-primary-100 text-primary-700'
                                            : 'text-neutral-600 hover:bg-neutral-100'
                                    }`}
                                >
                                    cURL
                                </button>
                                <button
                                    onClick={() => setSnippetType('fetch')}
                                    className={`px-3 py-1 text-md font-medium rounded-md transition-colors cursor-pointer ${
                                        snippetType === 'fetch'
                                            ? 'bg-primary-100 text-primary-700'
                                            : 'text-neutral-600 hover:bg-neutral-100'
                                    }`}
                                >
                                    Fetch
                                </button>
                                <button
                                    onClick={() => setSnippetType('axios')}
                                    className={`px-3 py-1 text-md font-medium rounded-md transition-colors cursor-pointer ${
                                        snippetType === 'axios'
                                            ? 'bg-primary-100 text-primary-700'
                                            : 'text-neutral-600 hover:bg-neutral-100'
                                    }`}
                                >
                                    Axios
                                </button>
                            </div>
                        </div>
                        <CodeSnippet code={snippets[snippetType]} language={snippetType}/>
                    </div>

                    {operation.parameters && operation.parameters.length > 0 && (
                        <div className="pt-2">
                            <ParameterTable parameters={operation.parameters}/>
                        </div>
                    )}

                    {operation.requestBody && (
                        <div className="pt-2">
                            <RequestBodyView requestBody={operation.requestBody}/>
                        </div>
                    )}

                    {operation.responses && Object.keys(operation.responses).length > 0 && (
                        <div className="pt-2">
                            <ResponseView responses={operation.responses}/>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default EndpointCard;
