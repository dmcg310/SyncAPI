import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import Header from '../components/layout/Header';
import Modal from '../components/common/Modal';
import DeleteConfirmModal from '../components/common/DeleteConfirmModal';
import WorkspaceCard from '../components/workspace/WorkspaceCard';
import WorkspaceForm from '../components/workspace/WorkspaceForm';
import {workspaceApi} from '../services/api';
import type {Workspace, WorkspaceRequest} from '@/types';
import {IoAdd} from "react-icons/io5";
import {BsFiles} from "react-icons/bs";

const DashboardPage: React.FC = () => {
    const navigate = useNavigate();
    const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [selectedWorkspace, setSelectedWorkspace] = useState<Workspace | null>(null);
    const [formLoading, setFormLoading] = useState(false);

    useEffect(() => {
        loadWorkspaces();
    }, []);

    const loadWorkspaces = async () => {
        try {
            setLoading(true);

            const response = await workspaceApi.getAll();
            setWorkspaces(response.data);
            setError('');
        } catch {
            setError('Failed to load workspaces');
        } finally {
            setLoading(false);
        }
    };

    const handleCreate = async (data: WorkspaceRequest) => {
        setFormLoading(true);

        try {
            await workspaceApi.create(data);
            await loadWorkspaces();
            setIsCreateModalOpen(false);
        } finally {
            setFormLoading(false);
        }
    };

    const handleEdit = async (data: WorkspaceRequest) => {
        if (!selectedWorkspace) {
            return;
        }

        setFormLoading(true);

        try {
            await workspaceApi.update(selectedWorkspace.id, data);
            await loadWorkspaces();
            setIsEditModalOpen(false);
            setSelectedWorkspace(null);
        } finally {
            setFormLoading(false);
        }
    };

    const handleDelete = async () => {
        if (!selectedWorkspace) {
            return;
        }

        setFormLoading(true);

        try {
            await workspaceApi.delete(selectedWorkspace.id);
            await loadWorkspaces();
            setIsDeleteModalOpen(false);
            setSelectedWorkspace(null);
        } finally {
            setFormLoading(false);
        }
    };

    const openEditModal = (workspace: Workspace) => {
        setSelectedWorkspace(workspace);
        setIsEditModalOpen(true);
    };

    const openDeleteModal = (workspace: Workspace) => {
        setSelectedWorkspace(workspace);
        setIsDeleteModalOpen(true);
    };

    const handleWorkspaceClick = (workspace: Workspace) => {
        navigate(`/workspace/${workspace.id}`);
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
                        onClick={() => setIsCreateModalOpen(true)}
                        className="flex items-center gap-2 bg-primary-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-primary-700 transition-colors cursor-pointer"
                    >
                        <IoAdd size={30}/>
                        New Workspace
                    </button>
                </div>

                {loading ? (
                    <div className="flex justify-center py-12">
                        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
                    </div>
                ) : error ? (
                    <div className="bg-error-50 text-error-700 px-4 py-3 rounded-lg">
                        {error}
                        <button
                            onClick={loadWorkspaces}
                            className="ml-2 underline hover:no-underline cursor-pointer"
                        >
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
                                onClick={() => handleWorkspaceClick(workspace)}
                                onEdit={() => openEditModal(workspace)}
                                onDelete={() => openDeleteModal(workspace)}
                            />
                        ))}
                    </div>
                )}
            </main>

            <Modal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
                title="Create Workspace"
            >
                <WorkspaceForm
                    onSubmit={handleCreate}
                    onCancel={() => setIsCreateModalOpen(false)}
                    loading={formLoading}
                />
            </Modal>

            <Modal
                isOpen={isEditModalOpen}
                onClose={() => {
                    setIsEditModalOpen(false);
                    setSelectedWorkspace(null);
                }}
                title="Edit Workspace"
            >
                <WorkspaceForm
                    workspace={selectedWorkspace}
                    onSubmit={handleEdit}
                    onCancel={() => {
                        setIsEditModalOpen(false);
                        setSelectedWorkspace(null);
                    }}
                    loading={formLoading}
                />
            </Modal>

            <DeleteConfirmModal
                isOpen={isDeleteModalOpen}
                onClose={() => {
                    setIsDeleteModalOpen(false);
                    setSelectedWorkspace(null);
                }}
                onConfirm={handleDelete}
                title="Delete Workspace"
                message={`Are you sure you want to delete "${selectedWorkspace?.name}"? 
                This will permanently delete all folders, requests, and environments within this workspace.`}
                loading={formLoading}
            />
        </div>
    );
};

export default DashboardPage;
