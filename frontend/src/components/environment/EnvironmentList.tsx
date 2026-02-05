import React from 'react';
import {MdCheck, MdDeleteForever, MdEdit} from 'react-icons/md';
import type {Environment} from '@/types';
import {IoAdd} from "react-icons/io5";
import {IoIosBarcode} from "react-icons/io";

interface EnvironmentListProps {
    environments: Environment[];
    activeEnvironmentId: number | null;
    onActivate: (environment: Environment) => void;
    onCreate: () => void;
    onEdit: (environment: Environment) => void;
    onDelete: (environment: Environment) => void;
}

const EnvironmentList: React.FC<EnvironmentListProps> = ({
                                                             environments,
                                                             activeEnvironmentId,
                                                             onActivate,
                                                             onCreate,
                                                             onEdit,
                                                             onDelete
                                                         }) => {
    return (
        <div className="h-full flex flex-col">
            <div className="flex items-center justify-between pb-4 border-b border-neutral-200">
                <h3 className="font-semibold text-neutral-900">Create new environment</h3>
                <button
                    onClick={onCreate}
                    className="p-1.5 text-neutral-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors cursor-pointer"
                    title="Create Environment"
                >
                    <IoAdd size={30}/>
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-2">
                {environments.length === 0 ? (
                    <div className="text-center py-8 px-4">
                        <div
                            className="w-12 h-12 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-3">
                            <IoIosBarcode size={30}/>
                        </div>
                        <p className="text-md text-neutral-600 mb-3">No environments yet</p>
                    </div>
                ) : (
                    <div className="space-y-1">
                        {environments.map((environment) => (
                            <div
                                key={environment.id}
                                className={`group flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer transition-colors ${
                                    environment.id === activeEnvironmentId
                                        ? 'bg-primary-50 hover:bg-neutral-50'
                                        : 'hover:bg-neutral-100 text-neutral-700'
                                }`}
                                onClick={() => onActivate(environment)}
                            >
                                <div className="flex flex-col gap-0.5">
                                    <span
                                        className="text-neutral-900 truncate text-md font-medium">{environment.name}</span>
                                    <span className="text-sm text-neutral-500">
                                        {environment.variableCount} {environment.variableCount === 1 ? 'variable' : 'variables'}
                                    </span>
                                    {environment.description && (
                                        <span className="text-sm text-neutral-400 block mt-1 truncate">
                                            {environment.description}
                                        </span>
                                    )}
                                </div>
                                {environment.id === activeEnvironmentId && (
                                    <MdCheck
                                        size={16}
                                        className="text-primary-600 shrink-0"
                                        title="Active"
                                    />
                                )}

                                <div className="flex gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <div
                                        className="flex gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                                        <button
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                onEdit(environment);
                                            }}
                                            className="p-1.5 text-neutral-400 hover:text-primary-600 hover:bg-primary-50 rounded transition-colors cursor-pointer"
                                            title="Edit environment"
                                        >
                                            <MdEdit size={20}/>
                                        </button>
                                        <button
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                onDelete(environment);
                                            }}
                                            className="p-1 text-neutral-400 hover:text-error-600 hover:bg-error-50 rounded transition-colors cursor-pointer"
                                            title="Delete environment"
                                        >
                                            <MdDeleteForever size={22.5}/>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default EnvironmentList;
