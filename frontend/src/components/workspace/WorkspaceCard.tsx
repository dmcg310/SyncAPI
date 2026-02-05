import React from 'react';
import type {Workspace} from '@/types';
import {FaEdit, FaFolder, FaUser} from "react-icons/fa";
import {MdDeleteForever} from "react-icons/md";
import {IoIosBarcode} from "react-icons/io";

interface WorkspaceCardProps {
    workspace: Workspace;
    onClick: () => void;
    onEdit: () => void;
    onDelete: () => void;
}

const WorkspaceCard: React.FC<WorkspaceCardProps> = ({workspace, onClick, onEdit, onDelete}) => {
    return (
        <div
            className="bg-white rounded-xl shadow-sm border border-neutral-200 p-6 hover:shadow-md hover:border-primary-200 transition-all cursor-pointer group"
            onClick={onClick}
        >
            <div className="flex justify-between items-start mb-3">
                <h3 className="text-lg font-semibold text-neutral-900 group-hover:text-primary-600 transition-colors">
                    {workspace.name}
                </h3>
                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onEdit();
                        }}
                        className="p-1.5 text-neutral-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors cursor-pointer"
                        title="Edit workspace"
                    >
                        <FaEdit size={20}/>
                    </button>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onDelete();
                        }}
                        className="p-1.5 text-neutral-400 hover:text-error-600 hover:bg-error-50 rounded-lg transition-colors cursor-pointer"
                        title="Delete workspace"
                    >
                        <MdDeleteForever size={20}/>
                    </button>
                </div>
            </div>

            {workspace.description && (
                <p className="text-md text-neutral-600 mb-4 line-clamp-2">
                    {workspace.description}
                </p>
            )}

            <div className="flex items-center gap-4 text-sm text-neutral-500">
                <div className="flex items-center gap-1.5">
                    <FaFolder size={15}/>
                    <span>{workspace.folderCount} {workspace.folderCount === 1 ? 'folder' : 'folders'}</span>
                </div>
                <div className="flex items-center gap-1.5">
                    <FaUser size={15}/>
                    <span>{workspace.memberCount} {workspace.memberCount === 1 ? 'member' : 'members'}</span>
                </div>
                <div className="flex items-center gap-1.5">
                    <IoIosBarcode size={22.5}/>
                    <span>{workspace.environmentCount} {workspace.environmentCount === 1 ? 'environment' : 'environments'}</span>
                </div>
            </div>
        </div>
    );
};

export default WorkspaceCard;
