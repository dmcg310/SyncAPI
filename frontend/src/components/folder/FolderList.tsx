import React from 'react';
import type {Folder} from '@/types';
import {IoAdd} from "react-icons/io5";
import {FaEdit, FaFolder} from "react-icons/fa";
import {MdDeleteForever} from "react-icons/md";

interface FolderListProps {
    folders: Folder[];
    selectedFolderId: number | null;
    onSelectFolder: (folder: Folder) => void;
    onCreateFolder: () => void;
    onEditFolder: (folder: Folder) => void;
    onDeleteFolder: (folder: Folder) => void;
}

const FolderList: React.FC<FolderListProps> = ({
                                                   folders,
                                                   selectedFolderId,
                                                   onSelectFolder,
                                                   onCreateFolder,
                                                   onEditFolder,
                                                   onDeleteFolder
                                               }) => {
    return (
        <div className="h-full flex flex-col">
            <div className="flex items-center justify-between p-4 border-b border-neutral-200">
                <h3 className="font-semibold text-neutral-900">Folders</h3>
                <button
                    onClick={onCreateFolder}
                    className="p-1.5 text-neutral-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors cursor-pointer"
                    title="Create folder"
                >
                    <IoAdd size={30}/>
                </button>
            </div>

            <div className="flex-1 overflow-y-auto p-2">
                {folders.length === 0 ? (
                    <div className="text-center py-8 px-4">
                        <div
                            className="w-12 h-12 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-3">
                            <FaFolder size={30}/>
                        </div>
                        <p className="text-md text-neutral-600 mb-3">No folders yet</p>
                    </div>
                ) : (
                    <div className="space-y-1">
                        {folders.map((folder) => (
                            <div
                                key={folder.id}
                                className={`group flex items-center justify-between px-3 py-2 rounded-lg cursor-pointer transition-colors ${
                                    selectedFolderId === folder.id
                                        ? 'bg-primary-50 text-primary-700'
                                        : 'hover:bg-neutral-100 text-neutral-700'
                                }`}
                                onClick={() => onSelectFolder(folder)}
                            >
                                <div className="flex items-center gap-2 min-w-0 flex-1">
                                    <FaFolder size={15} className="shrink-0"/>
                                    <div className="flex-1 min-w-0">
                                        <div className="flex items-center gap-2">
                                            <span className="truncate text-sm font-medium">{folder.name}</span>
                                            <span className="text-xs text-neutral-400">({folder.requestCount})</span>
                                        </div>
                                        {folder.description && (
                                            <p className="text-xs text-neutral-500 truncate mt-0.5">
                                                {folder.description}
                                            </p>
                                        )}
                                    </div>
                                </div>

                                <div className="flex gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            onEditFolder(folder);
                                        }}
                                        className="p-1.5 text-neutral-400 hover:text-primary-600 hover:bg-primary-50 rounded transition-colors cursor-pointer"
                                        title="Edit folder"
                                    >
                                        <FaEdit size={15}/>
                                    </button>
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            onDeleteFolder(folder);
                                        }}
                                        className="p-1 text-neutral-400 hover:text-error-600 hover:bg-error-50 rounded transition-colors cursor-pointer"
                                        title="Delete folder"
                                    >
                                        <MdDeleteForever size={17.5}/>
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default FolderList;
