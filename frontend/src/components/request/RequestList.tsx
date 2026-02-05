import React from 'react';
import type {ApiRequest} from '@/types';
import {METHOD_COLORS} from '../../util/constants';
import {IoAdd} from "react-icons/io5";
import {IoIosCodeWorking} from "react-icons/io";
import {MdDeleteForever} from "react-icons/md";
import {FaLock} from "react-icons/fa";

interface RequestListProps {
    requests: ApiRequest[];
    selectedRequestId: number | null;
    onSelectRequest: (request: ApiRequest) => void;
    onCreateRequest: () => void;
    onDeleteRequest: (request: ApiRequest) => void;
}

const RequestList: React.FC<RequestListProps> = ({
                                                     requests,
                                                     selectedRequestId,
                                                     onSelectRequest,
                                                     onCreateRequest,
                                                     onDeleteRequest
                                                 }) => {
    const getMethodColorClass = (method: string) => {
        return METHOD_COLORS[method as keyof typeof METHOD_COLORS] || 'bg-neutral-500';
    };

    return (
        <div className="h-full flex flex-col">
            <div className="flex items-center justify-between p-4 border-b border-neutral-200">
                <div>
                    <h3 className="font-semibold text-neutral-900">Requests</h3>
                </div>
                <button
                    onClick={onCreateRequest}
                    className="p-1.5 text-neutral-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors cursor-pointer"
                    title="Create request"
                >
                    <IoAdd size={30}/>
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-2">
                {requests.length === 0 ? (
                    <div className="text-center py-8 px-4">
                        <div
                            className="w-12 h-12 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-3">
                            <IoIosCodeWorking size={30}/>
                        </div>
                        <p className="text-md text-neutral-600 mb-3">No requests yet</p>
                    </div>
                ) : (
                    <div className="space-y-1">
                        {requests.map((request) => (
                            <div
                                key={request.id}
                                className={`group flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer transition-colors ${
                                    selectedRequestId === request.id
                                        ? 'bg-primary-50'
                                        : 'hover:bg-neutral-100'
                                }`}
                                onClick={() => onSelectRequest(request)}
                            >
                                <div className="flex items-center gap-3 min-w-0">
                                    <span
                                        className={`${getMethodColorClass(request.method)} text-white text-xs font-bold px-2 py-0.5 rounded`}>
                                        {request.method}
                                    </span>
                                    <div className="min-w-0">
                                        <p className={`text-sm font-medium truncate ${
                                            selectedRequestId === request.id ? 'text-primary-700' : 'text-neutral-700'
                                        }`}>
                                            {request.name}
                                        </p>
                                    </div>
                                    {request.lockedBy && (
                                        <span
                                            className="flex items-center gap-1.5 px-3 py-1.5 text-md text-warning-700 bg-warning-50 rounded-lg">
                                            <FaLock size={14}/>
                                        </span>
                                    )}
                                </div>

                                <button
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        onDeleteRequest(request);
                                    }}
                                    className="p-1 text-neutral-400 hover:text-error-600 hover:bg-error-50 rounded opacity-0 group-hover:opacity-100 transition-all cursor-pointer"
                                    title="Delete request"
                                >
                                    <MdDeleteForever size={17.5}/>
                                </button>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default RequestList;
