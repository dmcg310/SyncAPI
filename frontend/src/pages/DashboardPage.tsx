import React from 'react';
import {useNavigate} from 'react-router-dom';
import Header from '../components/layout/Header';
import Modal from '../components/common/Modal';
import DeleteConfirmModal from '../components/common/DeleteConfirmModal';
import WorkspaceCard from '../components/workspace/WorkspaceCard';
import WorkspaceForm from '../components/workspace/WorkspaceForm';
import {useModal, useWorkspaces} from '../hooks';
import type {Workspace, WorkspaceRequest} from '@/types';
import {IoAdd} from 'react-icons/io5';
import {BsFiles} from 'react-icons/bs';
import Spinner from "../components/common/Spinner.tsx";

const DashboardPage: React.FC = () => {
    const navigate = useNavigate();
    const {workspaces, loading, error, reload, create, update, remove, actionLoading} = useWorkspaces();

    const createModal = useModal();
    const editModal = useModal<Workspace>();
    const deleteModal = useModal<Workspace>();

    const handleCreate = async (data: WorkspaceRequest) => {
        await create(data);
        createModal.close();
    };

    const handleEdit = async (data: WorkspaceRequest) => {
        if (!editModal.data) {
            return;
        }

        await update(editModal.data.id, data);
        editModal.close();
    };

    const handleDelete = async () => {
        if (!deleteModal.data) {
            return;
        }

        await remove(deleteModal.data.id);
        deleteModal.close();
    };

    return (
        <div className="min-h-screen bg-neutral-50">
            <Header/>

            <main className="max-w-7xl mx-auto px-4 py-8">
                <div className="flex justify-between items-center mb-8">
                    <div>
                        <h2 className="text-2xl font-bold text-neutral-900">Workspaces</h2>
                        <p className="text-neutral-600 mt-1">Manage your API projects</p>
                    </div>
                    <button
                        onClick={() => createModal.open()}
                        className="flex items-center gap-2 bg-primary-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-primary-700 transition-colors cursor-pointer"
                    >
                        <IoAdd size={30}/>
                        New Workspace
                    </button>
                </div>

                {loading ? (
                    <div className="flex justify-center py-12">
                        <Spinner/>
                    </div>
                ) : error ? (
                    <div className="bg-error-50 text-error-700 px-4 py-3 rounded-lg">
                        {error}
                        <button onClick={reload} className="ml-2 underline hover:no-underline cursor-pointer">
                            Retry
                        </button>
                    </div>
                ) : workspaces.length === 0 ? (
                    <div className="text-center py-12">
                        <div
                            className="w-16 h-16 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-4">
                            <BsFiles size={30}/>
                        </div>
                        <h3 className="text-lg font-medium text-neutral-900 mb-2">No workspaces yet</h3>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                        {workspaces.map((workspace) => (
                            <WorkspaceCard
                                key={workspace.id}
                                workspace={workspace}
                                onClick={() => navigate(`/workspace/${workspace.id}`)}
                                onEdit={() => editModal.open(workspace)}
                                onDelete={() => deleteModal.open(workspace)}
                            />
                        ))}
                    </div>
                )}
            </main>

            <Modal isOpen={createModal.isOpen} onClose={createModal.close} title="Create Workspace">
                <WorkspaceForm onSubmit={handleCreate} onCancel={createModal.close} loading={actionLoading}/>
            </Modal>

            <Modal isOpen={editModal.isOpen} onClose={editModal.close} title="Edit Workspace">
                <WorkspaceForm
                    workspace={editModal.data}
                    onSubmit={handleEdit}
                    onCancel={editModal.close}
                    loading={actionLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={deleteModal.isOpen}
                onClose={deleteModal.close}
                onConfirm={handleDelete}
                title="Delete Workspace"
                message={`Are you sure you want to delete "${deleteModal.data?.name}"? 
                This will permanently delete all folders, requests, and environments within this workspace.`}
                loading={actionLoading}
            />
        </div>
    );
};

export default DashboardPage;
