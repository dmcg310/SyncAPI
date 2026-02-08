import React from 'react';
import type {OpenApiResponse} from '@/types';
import {getStatusColor} from "../../util/util.ts";

interface ResponseViewProps {
    responses: Record<string, OpenApiResponse>;
}

const ResponseView: React.FC<ResponseViewProps> = ({responses}) => {
    return (
        <div className="space-y-3">
            <h4 className="text-md font-semibold text-neutral-700">Responses</h4>
            <div className="space-y-3">
                {Object.entries(responses).map(([status, response]) => (
                    <div key={status} className="border border-neutral-200 rounded-lg overflow-hidden">
                        <div className="bg-neutral-50 px-4 py-2 border-b border-neutral-200 flex items-center gap-3">
                            <span
                                className={`text-md font-bold px-2 py-1 rounded ${getStatusColor(status)}`}
                            >
                                {status}
                            </span>
                            <span className="text-md text-neutral-700">{response.description}</span>
                        </div>
                        {response.content && (
                            <div className="p-4 bg-white">
                                {Object.entries(response.content).map(([contentType, mediaType]) => (
                                    <div key={contentType}>
                                        <div className="text-sm text-neutral-500 mb-2">
                                            Content-Type: {contentType}
                                        </div>
                                        {mediaType.example && (
                                            <pre
                                                className="text-md p-3 bg-neutral-900 text-neutral-100 rounded overflow-x-auto">
                                                {JSON.stringify(mediaType.example, null, 2)}
                                            </pre>
                                        )}
                                        {mediaType.schema && !mediaType.example && (
                                            <div
                                                className="text-sm bg-neutral-50 p-3 rounded border border-neutral-200">
                                                <span className="font-mono">
                                                    {mediaType.schema.type}
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ResponseView;
