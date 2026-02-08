import React, {useState} from 'react';
import {MdExpandMore} from 'react-icons/md';
import {getMethodColorClass} from '../../util/util';
import type {HttpMethod} from '@/types';

interface EndpointItem {
    method: HttpMethod;
    path: string;
    summary: string;
}

interface EndpointGroupProps {
    groupName: string;
    endpoints: EndpointItem[];
    selectedEndpoint: string | null;
    onSelectEndpoint: (method: HttpMethod, path: string) => void;
}

const EndpointGroup: React.FC<EndpointGroupProps> = ({
                                                         groupName,
                                                         endpoints,
                                                         selectedEndpoint,
                                                         onSelectEndpoint
                                                     }) => {
    const [isExpanded, setIsExpanded] = useState(true);

    return (
        <div className="mb-2">
            <button
                onClick={() => setIsExpanded(!isExpanded)}
                className="w-full flex items-center justify-between px-3 py-2 text-left hover:bg-neutral-100 rounded-lg transition-colors cursor-pointer"
            >
                <span className="text-md font-semibold text-neutral-900">
                    {groupName}
                </span>
                <MdExpandMore
                    size={18}
                    className={`text-neutral-600 transition-transform ${
                        isExpanded ? 'rotate-0' : '-rotate-90'
                    }`}
                />
            </button>

            {isExpanded && (
                <div className="ml-2 mt-1 space-y-0.5">
                    {endpoints.map(({method, path}) => {
                        const endpointKey = `${method}-${path}`;
                        const isSelected = selectedEndpoint === endpointKey;

                        return (
                            <button
                                key={endpointKey}
                                onClick={() => onSelectEndpoint(method, path)}
                                className={`w-full flex items-center gap-2 px-3 py-2 rounded-lg text-left transition-colors cursor-pointer ${
                                    isSelected
                                        ? 'bg-primary-50 text-primary-700'
                                        : 'hover:bg-neutral-100 text-neutral-700'
                                }`}
                            >
                                <span
                                    className={`${getMethodColorClass(method)} text-white text-xs font-bold px-1.5 py-0.5 rounded shrink-0`}
                                >
                                    {method}
                                </span>
                                <span className="text-md font-mono truncate flex-1">
                                    {path}
                                </span>
                            </button>
                        );
                    })}
                </div>
            )}
        </div>
    );
};

export default EndpointGroup;
