import React, {useEffect, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import Header from '../components/layout/Header';
import Modal from '../components/common/Modal';
import DeleteConfirmModal from '../components/common/DeleteConfirmModal';
import FolderList from '../components/folder/FolderList';
import FolderForm from '../components/folder/FolderForm';
import RequestList from '../components/request/RequestList';
import RequestForm from '../components/request/RequestForm';
import RequestEditor from '../components/request/RequestEditor';
import {folderApi, requestApi, workspaceApi} from '../services/api';
import type {ApiRequest, ApiRequestRequest, ExecutionResponse, Folder, FolderRequest, WorkspaceDetail} from '@/types';
import {MdArrowBackIos} from "react-icons/md";

const WorkspacePage: React.FC = () => {
    const {workspaceId} = useParams<{ workspaceId: string }>();
    const [workspace, setWorkspace] = useState<WorkspaceDetail | null>(null);
    const [folders, setFolders] = useState<Folder[]>([]);
    const [requests, setRequests] = useState<ApiRequest[]>([]);
    const [selectedFolder, setSelectedFolder] = useState<Folder | null>(null);
    const [selectedRequest, setSelectedRequest] = useState<ApiRequest | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [isFolderModalOpen, setIsFolderModalOpen] = useState(false);
    const [isEditFolderModalOpen, setIsEditFolderModalOpen] = useState(false);
    const [isDeleteFolderModalOpen, setIsDeleteFolderModalOpen] = useState(false);
    const [isRequestModalOpen, setIsRequestModalOpen] = useState(false);
    const [isDeleteRequestModalOpen, setIsDeleteRequestModalOpen] = useState(false);
    const [folderToEdit, setFolderToEdit] = useState<Folder | null>(null);
    const [folderToDelete, setFolderToDelete] = useState<Folder | null>(null);
    const [requestToDelete, setRequestToDelete] = useState<ApiRequest | null>(null);
    const [formLoading, setFormLoading] = useState(false);
    const [saving, setSaving] = useState(false);
    const [executing, setExecuting] = useState(false);

    useEffect(() => {
        if (workspaceId) {
            loadWorkspace();
            loadFolders();
        }
    }, [workspaceId]);

    useEffect(() => {
        if (selectedFolder) {
            setSelectedRequest(null);
            loadRequests(selectedFolder.id);
        } else {
            setRequests([]);
            setSelectedRequest(null);
        }
    }, [selectedFolder]);

    const loadWorkspace = async () => {
        try {
            const response = await workspaceApi.getById(Number(workspaceId));
            setWorkspace(response.data);
        } catch {
            setError('Failed to load workspace');
        }
    };

    const loadFolders = async () => {
        try {
            setLoading(true);
            const response = await folderApi.getByWorkspace(Number(workspaceId));
            setFolders(response.data);
            setError('');
        } catch {
            setError('Failed to load folders');
        } finally {
            setLoading(false);
        }
    };

    const loadRequests = async (folderId: number) => {
        try {
            const response = await requestApi.getByFolder(folderId);
            setRequests(response.data);
        } catch {
            setError('Failed to load requests');
        }
    };

    const handleCreateFolder = async (data: FolderRequest) => {
        setFormLoading(true);

        try {
            await folderApi.create(Number(workspaceId), data);
            await loadFolders();
            setIsFolderModalOpen(false);
        } finally {
            setFormLoading(false);
        }
    };

    const handleEditFolder = async (data: FolderRequest) => {
        if (!folderToEdit) {
            return;
        }

        setFormLoading(true);

        try {
            await folderApi.update(folderToEdit.id, data);
            await loadFolders();
            if (selectedFolder?.id === folderToEdit.id) {
                setSelectedFolder({...selectedFolder, ...data});
            }

            setIsEditFolderModalOpen(false);
            setFolderToEdit(null);
        } finally {
            setFormLoading(false);
        }
    };

    const handleDeleteFolder = async () => {
        if (!folderToDelete) {
            return;
        }

        setFormLoading(true);

        try {
            await folderApi.delete(Number(workspaceId), folderToDelete.id);
            await loadFolders();
            if (selectedFolder?.id === folderToDelete.id) {
                setSelectedFolder(null);
            }

            setIsDeleteFolderModalOpen(false);
            setFolderToDelete(null);
        } finally {
            setFormLoading(false);
        }
    };

    const handleCreateRequest = async (data: ApiRequestRequest) => {
        if (!selectedFolder) {
            return;
        }

        setFormLoading(true);

        try {
            const response = await requestApi.create(selectedFolder.id, data);
            await loadRequests(selectedFolder.id);
            await loadFolders();
            setSelectedRequest(response.data);
            setIsRequestModalOpen(false);
        } finally {
            setFormLoading(false);
        }
    };

    const handleSaveRequest = async (data: ApiRequestRequest) => {
        if (!selectedFolder || !selectedRequest) {
            return;
        }

        setSaving(true);

        try {
            const response = await requestApi.update(selectedFolder.id, selectedRequest.id, data);
            setSelectedRequest(response.data);
            await loadRequests(selectedFolder.id);
        } finally {
            setSaving(false);
        }
    };

    const handleExecuteRequest = async (): Promise<ExecutionResponse> => {
        if (!selectedFolder || !selectedRequest) {
            throw new Error('No request selected');
        }

        setExecuting(true);

        try {
            const response = await requestApi.execute(selectedFolder.id, selectedRequest.id);
            return response.data;
        } finally {
            setExecuting(false);
        }
    };

    const handleLockRequest = async () => {
        if (!selectedFolder || !selectedRequest) {
            return;
        }

        const response = await requestApi.lock(selectedFolder.id, selectedRequest.id);
        const updated = response.data;

        setSelectedRequest(updated);

        await loadRequests(selectedFolder.id);
        setRequests(prev =>
            prev.map(r => (r.id === updated.id ? updated : r))
        );
    };

    const handleUnlockRequest = async () => {
        if (!selectedFolder || !selectedRequest) {
            return;
        }

        const response = await requestApi.unlock(selectedFolder.id, selectedRequest.id);
        setSelectedRequest(response.data);
        await loadRequests(selectedFolder.id);
    };

    const handleDeleteRequest = async () => {
        if (!selectedFolder || !requestToDelete) {
            return;
        }

        setFormLoading(true);

        try {
            await requestApi.delete(selectedFolder.id, requestToDelete.id);
            await loadRequests(selectedFolder.id);
            await loadFolders();
            if (selectedRequest?.id === requestToDelete.id) {
                setSelectedRequest(null);
            }

            setIsDeleteRequestModalOpen(false);
            setRequestToDelete(null);
        } finally {
            setFormLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-neutral-50">
                <Header/>
                <div className="flex justify-center py-12">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-neutral-50 flex flex-col">
            <Header/>
            <div className="bg-white border-b border-neutral-200 px-4 py-3">
                <div className="max-w-full mx-auto flex items-center gap-4">
                    <Link
                        to="/dashboard"
                        className="text-neutral-400 hover:text-neutral-600 transition-colors"
                    >
                        <MdArrowBackIos size={30}/>
                    </Link>
                    <div>
                        <h1 className="text-lg font-semibold text-neutral-900">{workspace?.name}</h1>
                        {workspace?.description && (
                            <p className="text-sm text-neutral-500">{workspace.description}</p>
                        )}
                    </div>
                </div>
            </div>

            {error && (
                <div className="mx-4 mt-4 bg-error-50 text-error-700 px-4 py-3 rounded-lg">
                    {error}
                </div>
            )}

            <div className="flex-1 flex overflow-hidden">
                <div className="w-64 bg-white border-r border-neutral-200 shrink-0">
                    <FolderList
                        folders={folders}
                        selectedFolderId={selectedFolder?.id || null}
                        onSelectFolder={setSelectedFolder}
                        onCreateFolder={() => setIsFolderModalOpen(true)}
                        onEditFolder={(folder) => {
                            setFolderToEdit(folder);
                            setIsEditFolderModalOpen(true);
                        }}
                        onDeleteFolder={(folder) => {
                            setFolderToDelete(folder);
                            setIsDeleteFolderModalOpen(true);
                        }}
                    />
                </div>

                {selectedFolder && (
                    <div className="w-72 bg-white border-r border-neutral-200 shrink-0">
                        <RequestList
                            requests={requests}
                            selectedRequestId={selectedRequest?.id || null}
                            onSelectRequest={setSelectedRequest}
                            onCreateRequest={() => setIsRequestModalOpen(true)}
                            onDeleteRequest={(request) => {
                                setRequestToDelete(request);
                                setIsDeleteRequestModalOpen(true);
                            }}
                        />
                    </div>
                )}

                <div className="flex-1 bg-white">
                    <RequestEditor
                        request={selectedRequest}
                        onSave={handleSaveRequest}
                        onExecute={handleExecuteRequest}
                        onLock={handleLockRequest}
                        onUnlock={handleUnlockRequest}
                        saving={saving}
                        executing={executing}
                    />
                </div>
            </div>

            <Modal
                isOpen={isFolderModalOpen}
                onClose={() => setIsFolderModalOpen(false)}
                title="Create Folder"
            >
                <FolderForm
                    onSubmit={handleCreateFolder}
                    onCancel={() => setIsFolderModalOpen(false)}
                    loading={formLoading}
                />
            </Modal>

            <Modal
                isOpen={isEditFolderModalOpen}
                onClose={() => {
                    setIsEditFolderModalOpen(false);
                    setFolderToEdit(null);
                }}
                title="Edit Folder"
            >
                <FolderForm
                    folder={folderToEdit}
                    onSubmit={handleEditFolder}
                    onCancel={() => {
                        setIsEditFolderModalOpen(false);
                        setFolderToEdit(null);
                    }}
                    loading={formLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={isDeleteFolderModalOpen}
                onClose={() => {
                    setIsDeleteFolderModalOpen(false);
                    setFolderToDelete(null);
                }}
                onConfirm={handleDeleteFolder}
                title="Delete Folder"
                message={`Are you sure you want to delete "${folderToDelete?.name}"? This will permanently delete all requests in this folder.`}
                loading={formLoading}
            />

            <Modal
                isOpen={isRequestModalOpen}
                onClose={() => setIsRequestModalOpen(false)}
                title="Create Request"
            >
                <RequestForm
                    onSubmit={handleCreateRequest}
                    onCancel={() => setIsRequestModalOpen(false)}
                    loading={formLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={isDeleteRequestModalOpen}
                onClose={() => {
                    setIsDeleteRequestModalOpen(false);
                    setRequestToDelete(null);
                }}
                onConfirm={handleDeleteRequest}
                title="Delete Request"
                message={`Are you sure you want to delete "${requestToDelete?.name}"?`}
                loading={formLoading}
            />
        </div>
    );
};

export default WorkspacePage;
