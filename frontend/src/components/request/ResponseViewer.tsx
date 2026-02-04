import React from 'react';
import type {ExecutionResponse} from '@/types';

interface ResponseViewerProps {
    response: ExecutionResponse;
}

const ResponseViewer: React.FC<ResponseViewerProps> = ({response}) => {
    const getStatusColor = (status: number) => {
        if (status >= 200 && status < 300) {
            return 'text-success-600 bg-success-50';
        }

        if (status >= 300 && status < 400) {
            return 'text-warning-600 bg-warning-50';
        }

        return 'text-error-600 bg-error-50';
    };

    return (
        <div className="border-t border-neutral-200 shrink-0 max-h-[60vh] flex flex-col">
            <div className="flex items-center gap-4 px-4 py-3 bg-neutral-50 border-b border-neutral-100">
                <span className="text-md font-medium text-neutral-700">Response</span>
                <span className={`px-2 py-0.5 rounded text-md font-bold ${getStatusColor(response.statusCode)}`}>
                    {response.statusText}
                </span>
                <span className="text-md text-neutral-500">{response.responseTimeMs}ms</span>
                {response.errorMessage && (
                    <span className="text-md text-error-600">{response.errorMessage}</span>
                )}
            </div>
            <div className="flex-1 overflow-auto p-4 bg-neutral-50">
                <pre className="text-md font-mono text-neutral-800 whitespace-pre-wrap">
                    {typeof response.body === 'string' ? response.body : JSON.stringify(response.body, null, 2)}
                </pre>
            </div>
        </div>
    );
};

export default ResponseViewer;
