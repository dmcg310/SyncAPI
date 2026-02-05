import React from 'react';
import {Link, useParams} from 'react-router-dom';
import Header from '../components/layout/Header';
import Modal from '../components/common/Modal';
import DeleteConfirmModal from '../components/common/DeleteConfirmModal';
import FolderList from '../components/folder/FolderList';
import FolderForm from '../components/folder/FolderForm';
import RequestList from '../components/request/RequestList';
import RequestForm from '../components/request/RequestForm';
import RequestEditor from '../components/request/RequestEditor';
import {useFolders, useModal, useRequests, useWorkspace} from '../hooks';
import type {ApiRequest, ApiRequestRequest, ExecutionResponse, Folder, FolderRequest} from '@/types';
import {MdArrowBackIos} from 'react-icons/md';
import Spinner from "../components/common/Spinner.tsx";

const WorkspacePage: React.FC = () => {
    const {workspaceId} = useParams<{ workspaceId: string }>();
    const numericWorkspaceId = workspaceId ? Number(workspaceId) : null;

    const {workspace} = useWorkspace(numericWorkspaceId);
    const folders = useFolders(numericWorkspaceId);
    const requests = useRequests(folders.selectedFolder?.id ?? null, folders.reload);

    const createFolderModal = useModal();
    const editFolderModal = useModal<Folder>();
    const deleteFolderModal = useModal<Folder>();
    const createRequestModal = useModal();
    const deleteRequestModal = useModal<ApiRequest>();

    const handleCreateFolder = async (data: FolderRequest) => {
        await folders.create(data);
        createFolderModal.close();
    };

    const handleEditFolder = async (data: FolderRequest) => {
        if (!editFolderModal.data) {
            return;
        }

        await folders.update(editFolderModal.data.id, data);
        editFolderModal.close();
    };

    const handleDeleteFolder = async () => {
        if (!deleteFolderModal.data) {
            return;
        }

        await folders.remove(deleteFolderModal.data.id);
        deleteFolderModal.close();
    };

    const handleCreateRequest = async (data: ApiRequestRequest) => {
        await requests.create(data);
        createRequestModal.close();
    };

    const handleSaveRequest = async (data: ApiRequestRequest) => {
        if (!requests.selectedRequest) {
            return;
        }

        await requests.update(requests.selectedRequest.id, data);
    };

    const handleExecuteRequest = async (): Promise<ExecutionResponse> => {
        if (!requests.selectedRequest) {
            throw new Error('No request selected');
        }

        return await requests.execute(requests.selectedRequest.id);
    };

    const handleLockRequest = async () => {
        if (!requests.selectedRequest) {
            return;
        }

        await requests.lock(requests.selectedRequest.id);
    };

    const handleUnlockRequest = async () => {
        if (!requests.selectedRequest) {
            return;
        }

        await requests.unlock(requests.selectedRequest.id);
    };

    const handleDeleteRequest = async () => {
        if (!deleteRequestModal.data) {
            return;
        }

        await requests.remove(deleteRequestModal.data.id);
        deleteRequestModal.close();
    };

    if (folders.loading) {
        return (
            <div className="min-h-screen bg-neutral-50">
                <Header/>
                <div className="flex justify-center py-12">
                    <Spinner/>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-neutral-50 flex flex-col">
            <Header/>

            <div className="bg-white border-b border-neutral-200 px-4 py-3">
                <div className="max-w-full mx-auto flex items-center gap-4">
                    <Link to="/dashboard" className="text-neutral-400 hover:text-neutral-600 transition-colors">
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

            {(folders.error || requests.error) && (
                <div className="mx-4 mt-4 bg-error-50 text-error-700 px-4 py-3 rounded-lg">
                    {folders.error || requests.error}
                </div>
            )}

            <div className="flex-1 flex overflow-hidden">
                <div className="w-64 bg-white border-r border-neutral-200 shrink-0">
                    <FolderList
                        folders={folders.folders}
                        selectedFolderId={folders.selectedFolder?.id || null}
                        onSelectFolder={folders.selectFolder}
                        onCreateFolder={() => createFolderModal.open()}
                        onEditFolder={(folder) => editFolderModal.open(folder)}
                        onDeleteFolder={(folder) => deleteFolderModal.open(folder)}
                    />
                </div>

                {folders.selectedFolder && (
                    <div className="w-72 bg-white border-r border-neutral-200 shrink-0">
                        <RequestList
                            requests={requests.requests}
                            selectedRequestId={requests.selectedRequest?.id || null}
                            onSelectRequest={requests.selectRequest}
                            onCreateRequest={() => createRequestModal.open()}
                            onDeleteRequest={(request) => deleteRequestModal.open(request)}
                        />
                    </div>
                )}

                <div className="flex-1 bg-white">
                    <RequestEditor
                        request={requests.selectedRequest}
                        onSave={handleSaveRequest}
                        onExecute={handleExecuteRequest}
                        onLock={handleLockRequest}
                        onUnlock={handleUnlockRequest}
                        saving={requests.actionLoading}
                        executing={requests.executing}
                    />
                </div>
            </div>

            <Modal isOpen={createFolderModal.isOpen} onClose={createFolderModal.close} title="Create Folder">
                <FolderForm
                    onSubmit={handleCreateFolder}
                    onCancel={createFolderModal.close}
                    loading={folders.actionLoading}
                />
            </Modal>

            <Modal isOpen={editFolderModal.isOpen} onClose={editFolderModal.close} title="Edit Folder">
                <FolderForm
                    folder={editFolderModal.data}
                    onSubmit={handleEditFolder}
                    onCancel={editFolderModal.close}
                    loading={folders.actionLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={deleteFolderModal.isOpen}
                onClose={deleteFolderModal.close}
                onConfirm={handleDeleteFolder}
                title="Delete Folder"
                message={`Are you sure you want to delete "${deleteFolderModal.data?.name}"? 
                This will permanently delete all requests in this folder.`}
                loading={folders.actionLoading}
            />

            <Modal isOpen={createRequestModal.isOpen} onClose={createRequestModal.close} title="Create Request">
                <RequestForm
                    onSubmit={handleCreateRequest}
                    onCancel={createRequestModal.close}
                    loading={requests.actionLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={deleteRequestModal.isOpen}
                onClose={deleteRequestModal.close}
                onConfirm={handleDeleteRequest}
                title="Delete Request"
                message={`Are you sure you want to delete "${deleteRequestModal.data?.name}"?`}
                loading={requests.actionLoading}
            />
        </div>
    );
};

export default WorkspacePage;
