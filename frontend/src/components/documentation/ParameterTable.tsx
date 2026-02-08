import React from 'react';
import type {OpenApiParameter} from '@/types';

interface ParameterTableProps {
    parameters: OpenApiParameter[];
}

const ParameterTable: React.FC<ParameterTableProps> = ({parameters}) => {
    if (parameters.length === 0) {
        return null;
    }

    const groupedParams = parameters.reduce((acc, param) => {
        if (!acc[param.in]) {
            acc[param.in] = [];
        }

        acc[param.in].push(param);

        return acc;
    }, {} as Record<string, OpenApiParameter[]>);

    const locationLabels: Record<string, string> = {
        path: 'Path Parameters',
        query: 'Query Parameters',
        header: 'Header Parameters',
        cookie: 'Cookie Parameters'
    };

    return (
        <div className="space-y-4">
            {Object.entries(groupedParams).map(([location, params]) => (
                <div key={location}>
                    <h4 className="text-md font-semibold text-neutral-700 mb-2">
                        {locationLabels[location] || `${location} Parameters`}
                    </h4>
                    <div className="border border-neutral-200 rounded-lg overflow-hidden">
                        <table className="w-full text-md">
                            <thead className="bg-neutral-50 border-b border-neutral-200">
                            <tr>
                                <th className="text-left px-4 py-2 font-semibold text-neutral-700">Name</th>
                                <th className="text-left px-4 py-2 font-semibold text-neutral-700">Type</th>
                                <th className="text-left px-4 py-2 font-semibold text-neutral-700">Required</th>
                                <th className="text-left px-4 py-2 font-semibold text-neutral-700">Description</th>
                            </tr>
                            </thead>
                            <tbody>
                            {params.map((param, index) => (
                                <tr
                                    key={param.name}
                                    className={index % 2 === 0 ? 'bg-white' : 'bg-neutral-50'}
                                >
                                    <td className="px-4 py-2 font-mono text-neutral-900">
                                        {param.name}
                                    </td>
                                    <td className="px-4 py-2">
                                        <span className="text-sm bg-neutral-100 text-neutral-700 px-2 py-1 rounded">
                                            {param.schema.type}
                                        </span>
                                    </td>
                                    <td className="px-4 py-2">
                                        {param.required ? (
                                            <span
                                                className="text-sm bg-error-100 text-error-700 px-2 py-1 rounded font-medium">
                                                Required
                                            </span>
                                        ) : (
                                            <span className="text-sm text-neutral-500">Optional</span>
                                        )}
                                    </td>
                                    <td className="px-4 py-2 text-neutral-600">
                                        {param.schema.type === 'array' && param.schema.items ? (
                                            <span>Array of {param.schema.items.type}</span>
                                        ) : (
                                            '-'
                                        )}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default ParameterTable;
