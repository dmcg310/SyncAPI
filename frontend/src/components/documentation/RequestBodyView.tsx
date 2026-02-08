import React from 'react';
import type {OpenApiRequestBody} from '@/types';

interface RequestBodyViewProps {
    requestBody: OpenApiRequestBody;
}

const RequestBodyView: React.FC<RequestBodyViewProps> = ({requestBody}) => {
    const renderSchema = (schema: any, level = 0): React.ReactNode => {
        if (!schema) {
            return null;
        }

        const indent = level * 16;

        if (schema.type === 'object' && schema.properties) {
            return (
                <div style={{marginLeft: `${indent}px`}}>
                    {Object.entries(schema.properties).map(([key, value]: [string, any]) => (
                        <div key={key} className="my-1">
                            <div className="flex items-baseline gap-2">
                                <span className="font-mono text-md text-neutral-900">{key}:</span>
                                <span className="text-sm bg-neutral-100 text-neutral-700 px-2 py-0.5 rounded">
                                    {value.type}
                                </span>
                            </div>
                            {value.type === 'object' && renderSchema(value, level + 1)}
                            {value.type === 'array' && value.items && (
                                <div className="ml-4 text-md text-neutral-600">
                                    Array of {value.items.type}
                                    {value.items.type === 'object' && renderSchema(value.items, level + 1)}
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            );
        }

        return null;
    };

    const contentTypes = Object.keys(requestBody.content);
    const primaryContentType = contentTypes[0];
    const mediaType = requestBody.content[primaryContentType];

    return (
        <div className="space-y-3">
            <div className="flex items-center gap-2">
                <h4 className="text-md font-semibold text-neutral-700">Request Body</h4>
                {requestBody.required && (
                    <span className="text-sm bg-error-100 text-error-700 px-2 py-1 rounded font-medium">
                        Required
                    </span>
                )}
                <span className="text-sm text-neutral-500">
                    ({primaryContentType})
                </span>
            </div>

            {requestBody.description && (
                <p className="text-md text-neutral-600">{requestBody.description}</p>
            )}

            {mediaType.schema && (
                <div className="border border-neutral-200 rounded-lg">
                    <div className="bg-neutral-100 px-4 py-2 border-b border-neutral-200">
                        <span className="text-md font-semibold text-neutral-700">Schema</span>
                    </div>
                    <pre className="bg-neutral-50 p-4 text-md font-mono text-neutral-800 whitespace-pre-wrap">
                        {renderSchema(mediaType.schema)}
                    </pre>
                </div>
            )}

            {mediaType.example && (
                <div className="border border-neutral-200 rounded-lg overflow-hidden">
                    <div className="bg-neutral-100 px-4 py-2 border-b border-neutral-200">
                        <span className="text-md font-semibold text-neutral-700">Example</span>
                    </div>
                    <pre className="bg-neutral-50 p-4 text-md font-mono text-neutral-800 whitespace-pre-wrap">
                        {JSON.stringify(mediaType.example, null, 2)}
                    </pre>
                </div>
            )}
        </div>
    );
};

export default RequestBodyView;
