import React, {useEffect, useRef, useState} from 'react';
import {Link, useParams} from 'react-router-dom';
import Header from '../components/layout/Header';
import Modal from '../components/common/Modal';
import DeleteConfirmModal from '../components/common/DeleteConfirmModal';
import FolderList from '../components/folder/FolderList';
import FolderForm from '../components/folder/FolderForm';
import RequestList from '../components/request/RequestList';
import RequestForm from '../components/request/RequestForm';
import RequestEditor from '../components/request/RequestEditor';
import EnvironmentList from '../components/environment/EnvironmentList';
import {useEnvironments, useFolders, useModal, useRequests, useWorkspace, useWorkspaceMembers} from '../hooks';
import type {
    ApiRequest,
    ApiRequestRequest,
    Environment,
    EnvironmentRequest,
    ExecutionResponse,
    Folder,
    FolderRequest,
    Member
} from '@/types';
import {MdAdd, MdArrowBackIos, MdCheck, MdExpandMore, MdPeople, MdSettings} from 'react-icons/md';
import {useAuth} from '../context/AuthContext';
import Spinner from "../components/common/Spinner.tsx";
import EnvironmentForm from "../components/environment/EnvironmentForm.tsx";
import MemberList from "../components/workspace/MemberList.tsx";

const WorkspacePage: React.FC = () => {
    const {workspaceId} = useParams<{ workspaceId: string }>();
    const numericWorkspaceId = workspaceId ? Number(workspaceId) : null;

    const {user} = useAuth();
    const {workspace, reload: reloadWorkspace} = useWorkspace(numericWorkspaceId);
    const environments = useEnvironments(numericWorkspaceId);
    const folders = useFolders(numericWorkspaceId);
    const requests = useRequests(folders.selectedFolder?.id ?? null, folders.reload);
    const members = useWorkspaceMembers(numericWorkspaceId, reloadWorkspace);

    const [showEnvironmentDropdown, setShowEnvironmentDropdown] = useState(false);
    const [showManageModal, setShowManageModal] = useState(false);
    const [showMembersModal, setShowMembersModal] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);

    const createEnvironmentModal = useModal();
    const editEnvironmentModal = useModal<Environment>();
    const deleteEnvironmentModal = useModal<Environment>();

    const createFolderModal = useModal();
    const editFolderModal = useModal<Folder>();
    const deleteFolderModal = useModal<Folder>();
    const createRequestModal = useModal();
    const deleteRequestModal = useModal<ApiRequest>();
    const deleteMemberModal = useModal<Member>();

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
                setShowEnvironmentDropdown(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

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

    const handleCreateEnvironment = async (data: EnvironmentRequest) => {
        await environments.create(data);
        createEnvironmentModal.close();
    };

    const handleEditEnvironment = async (data: EnvironmentRequest) => {
        if (!editEnvironmentModal.data) {
            return;
        }

        await environments.update(editEnvironmentModal.data.id, data);
        editEnvironmentModal.close();
    };

    const handleActivateEnvironment = async (environment: Environment) => {
        if (environment.id === environments.activeEnvironment?.id) {
            return;
        }

        await environments.activate(environment.id);
    };

    const handleDeleteEnvironment = async () => {
        if (!deleteEnvironmentModal.data) {
            return;
        }

        await environments.remove(deleteEnvironmentModal.data.id);
        deleteEnvironmentModal.close();
    };

    const handleDeleteMember = async () => {
        if (!deleteMemberModal.data) {
            return;
        }

        await members.removeMember(deleteMemberModal.data.userId);
        deleteMemberModal.close();
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
                    <div className="flex-1">
                        <h1 className="text-lg font-semibold text-neutral-900">{workspace?.name}</h1>
                        {workspace?.description && (
                            <p className="text-sm text-neutral-500">{workspace.description}</p>
                        )}
                    </div>
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => setShowMembersModal(true)}
                            className="flex items-center gap-2 px-3 py-1.5 bg-neutral-100 hover:bg-neutral-200 rounded-lg transition-colors cursor-pointer"
                            title="Manage Members"
                        >
                            <MdPeople size={18}/>
                            <span className="text-lg font-semibold text-neutral-900">Members</span>
                        </button>
                        {environments.environments.length === 0 ? (
                            <button
                                onClick={() => createEnvironmentModal.open()}
                                className="flex items-center gap-2 px-3 py-1.5 bg-primary-600 hover:bg-primary-700 text-white rounded-lg transition-colors cursor-pointer"
                            >
                                <MdAdd size={18}/>
                                <span className="text-lg font-semibold">Create Environment</span>
                            </button>
                        ) : (
                            <div className="relative" ref={dropdownRef}>
                                <button
                                    onClick={() => setShowEnvironmentDropdown(!showEnvironmentDropdown)}
                                    className="flex items-center gap-2 px-3 py-1.5 bg-neutral-100 hover:bg-neutral-200 rounded-lg transition-colors min-w-45 justify-between cursor-pointer"
                                >
                                    <span className="text-lg font-semibold text-neutral-900">
                                        {environments.activeEnvironment?.name || 'Select Environment'}
                                    </span>
                                    <MdExpandMore
                                        size={20}
                                        className={`text-neutral-600 transition-transform ${
                                            showEnvironmentDropdown ? 'rotate-180' : ''
                                        }`}
                                    />
                                </button>

                                {showEnvironmentDropdown && (
                                    <div
                                        className="absolute right-0 mt-2 w-72 bg-white rounded-lg shadow-lg border border-neutral-200 z-50 max-h-96 overflow-hidden flex flex-col">
                                        <div className="overflow-y-auto flex-1">
                                            {environments.environments.map((env) => (
                                                <button
                                                    key={env.id}
                                                    onClick={() => {
                                                        handleActivateEnvironment(env);
                                                        setShowEnvironmentDropdown(false);
                                                    }}
                                                    className={`w-full px-4 py-2.5 text-left hover:bg-neutral-50 transition-colors flex items-center justify-between cursor-pointer ${
                                                        env.id === environments.activeEnvironment?.id ? 'bg-primary-50' : ''
                                                    }`}
                                                >
                                                    <div className="flex-1 min-w-0">
                                                        <div className="text-md font-medium text-neutral-900 truncate">
                                                            {env.name}
                                                        </div>
                                                        {env.description && (
                                                            <div className="text-sm text-neutral-500 truncate mt-0.5">
                                                                {env.description}
                                                            </div>
                                                        )}
                                                        <div className="text-sm text-neutral-400 mt-0.5">
                                                            {env.variableCount} {env.variableCount === 1 ? 'variable' : 'variables'}
                                                        </div>
                                                    </div>
                                                    {env.id === environments.activeEnvironment?.id && (
                                                        <MdCheck size={18} className="text-primary-600 ml-2"/>
                                                    )}
                                                </button>
                                            ))}
                                        </div>
                                        <div className="p-2 border-t border-neutral-200">
                                            <button
                                                onClick={() => {
                                                    setShowManageModal(true);
                                                    setShowEnvironmentDropdown(false);
                                                }}
                                                className="w-full px-3 py-2 text-md font-medium text-neutral-700 hover:bg-neutral-100 rounded-lg transition-colors flex items-center gap-2 cursor-pointer"
                                            >
                                                <MdSettings size={18}/>
                                                Manage Environments
                                            </button>
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {(environments.error || folders.error || requests.error) && (
                <div className="mx-4 mt-4 bg-error-50 text-error-700 px-4 py-3 rounded-lg">
                    {environments.error || folders.error || requests.error}
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

            <Modal isOpen={createEnvironmentModal.isOpen} onClose={createEnvironmentModal.close}
                   title="Create Environment">
                <EnvironmentForm
                    onSubmit={handleCreateEnvironment}
                    onCancel={createEnvironmentModal.close}
                    loading={environments.actionLoading}
                />
            </Modal>

            <Modal isOpen={editEnvironmentModal.isOpen} onClose={editEnvironmentModal.close} title="Edit Environment">
                <EnvironmentForm
                    environment={editEnvironmentModal.data}
                    onSubmit={handleEditEnvironment}
                    onCancel={editEnvironmentModal.close}
                    loading={environments.actionLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={deleteEnvironmentModal.isOpen}
                onClose={deleteEnvironmentModal.close}
                onConfirm={handleDeleteEnvironment}
                title="Delete Environment"
                message={`Are you sure you want to delete "${deleteEnvironmentModal.data?.name}"?
                This will permanently delete all variables in this environment.`}
                loading={environments.actionLoading}
            />

            <Modal
                isOpen={showManageModal}
                onClose={() => setShowManageModal(false)}
                title="Manage Environments"
            >
                <div className="h-125">
                    <EnvironmentList
                        environments={environments.environments}
                        activeEnvironmentId={environments.activeEnvironment?.id || null}
                        onActivate={handleActivateEnvironment}
                        onCreate={() => {
                            setShowManageModal(false);
                            createEnvironmentModal.open();
                        }}
                        onEdit={(environment) => {
                            setShowManageModal(false);
                            editEnvironmentModal.open(environment);
                        }}
                        onDelete={(environment) => {
                            setShowManageModal(false);
                            deleteEnvironmentModal.open(environment);
                        }}
                    />
                </div>
            </Modal>

            <Modal
                isOpen={showMembersModal}
                onClose={() => setShowMembersModal(false)}
                title="Workspace Members"
            >
                <MemberList
                    members={workspace?.members || []}
                    currentUserId={user?.id || 0}
                    onAddMember={members.addMember}
                    onRemoveMember={(member) => deleteMemberModal.open(member)}
                    loading={members.actionLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={deleteMemberModal.isOpen}
                onClose={deleteMemberModal.close}
                onConfirm={handleDeleteMember}
                title="Remove Member"
                message={`Are you sure you want to remove "${deleteMemberModal.data?.name}" from this workspace?`}
                loading={members.actionLoading}
            />
        </div>
    );
};

export default WorkspacePage;
