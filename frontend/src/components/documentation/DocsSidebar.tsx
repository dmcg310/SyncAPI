import React from 'react';
import EndpointGroup from './EndpointGroup';
import type {HttpMethod, OpenApiPathItem} from '@/types';

interface DocsSidebarProps {
    paths: Record<string, OpenApiPathItem>;
    selectedEndpoint: string | null;
    onSelectEndpoint: (method: HttpMethod, path: string) => void;
}

const DocsSidebar: React.FC<DocsSidebarProps> = ({paths, selectedEndpoint, onSelectEndpoint}) => {
    const groupedEndpoints = React.useMemo(() => {
        const groups: Record<string, Array<{
            method: HttpMethod;
            path: string;
            summary: string;
        }>> = {};

        Object.entries(paths).forEach(([path, pathItem]) => {
            const firstSegment = path.split('/').filter(Boolean)[0] || 'Root';
            const groupName = firstSegment.charAt(0).toUpperCase() + firstSegment.slice(1);

            if (!groups[groupName]) {
                groups[groupName] = [];
            }

            Object.entries(pathItem).forEach(([method, operation]) => {
                if (['get', 'post', 'put', 'patch', 'delete'].includes(method)) {
                    groups[groupName].push({
                        method: method.toUpperCase() as HttpMethod,
                        path,
                        summary: operation.summary || ''
                    });
                }
            });
        });

        return groups;
    }, [paths]);

    const sortedGroupNames = Object.keys(groupedEndpoints).sort();
    if (sortedGroupNames.length === 0) {
        return (
            <div className="p-4 text-center">
                <p className="text-md text-neutral-500">No endpoints</p>
            </div>
        );
    }

    return (
        <div className="h-full overflow-y-auto">
            <div className="flex items-center justify-between p-4 border-b border-neutral-200">
                <div>
                    <h3 className="font-semibold text-neutral-900 uppercase tracking-wider">
                        Endpoints
                    </h3>
                </div>
            </div>
            {sortedGroupNames.map((groupName) => (
                <EndpointGroup
                    key={groupName}
                    groupName={groupName}
                    endpoints={groupedEndpoints[groupName]}
                    selectedEndpoint={selectedEndpoint}
                    onSelectEndpoint={onSelectEndpoint}
                />
            ))}
        </div>
    );
};

export default DocsSidebar;
