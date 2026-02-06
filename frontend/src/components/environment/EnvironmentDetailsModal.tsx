import React, {useState} from 'react';
import Modal from '../common/Modal';
import DeleteConfirmModal from '../common/DeleteConfirmModal';
import VariableList from './VariableList';
import {useVariables} from '../../hooks/useVariables';
import {useModal} from '../../hooks';
import type {Environment, EnvironmentVariable} from '@/types';
import {MdEdit} from 'react-icons/md';

interface EnvironmentDetailsModalProps {
    isOpen: boolean;
    onClose: () => void;
    environment: Environment | null;
    onEditEnvironment?: (environment: Environment) => void;
}

const EnvironmentDetailsModal: React.FC<EnvironmentDetailsModalProps> = ({
                                                                             isOpen,
                                                                             onClose,
                                                                             environment,
                                                                             onEditEnvironment
                                                                         }) => {
    const variables = useVariables(environment?.id ?? null);
    const [error, setError] = useState<string | null>(null);
    const deleteVariableModal = useModal<EnvironmentVariable>();

    const handleAddVariable = async (key: string, value: string) => {
        try {
            setError(null);
            await variables.create({key, value});
        } catch (err) {
            setError('Failed to add variable');
            throw err;
        }
    };

    const handleUpdateVariable = async (variableId: number, key: string, value: string) => {
        try {
            setError(null);
            await variables.update(variableId, {key, value});
        } catch (err) {
            setError('Failed to update variable');
            throw err;
        }
    };

    const handleDeleteClick = (variable: EnvironmentVariable) => {
        deleteVariableModal.open(variable);
    };

    const handleDeleteVariable = async () => {
        if (!deleteVariableModal.data) {
            return;
        }

        try {
            setError(null);
            await variables.remove(deleteVariableModal.data.id);
            deleteVariableModal.close();
        } catch (err) {
            setError('Failed to delete variable');
        }
    };

    if (!environment) {
        return null;
    }

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={environment.name}
            size="lg"
        >
            <div className="space-y-4">
                <div className="flex items-start justify-between pb-4 border-b border-neutral-200">
                    <div className="flex-1">
                        {environment.description && (
                            <p className="text-md text-neutral-600">{environment.description}</p>
                        )}
                        <p className="text-sm text-neutral-400 mt-1">
                            {environment.isActive ? (
                                <span className="text-primary-600 font-medium">Active Environment</span>
                            ) : (
                                <span>Inactive</span>
                            )}
                        </p>
                    </div>
                    {onEditEnvironment && (
                        <button
                            onClick={() => onEditEnvironment(environment)}
                            className="p-2 text-neutral-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors cursor-pointer"
                            title="Edit environment details"
                        >
                            <MdEdit size={18}/>
                        </button>
                    )}
                </div>

                {error && (
                    <div className="bg-error-50 text-error-700 px-3 py-2 rounded-lg text-md">
                        {error}
                    </div>
                )}

                <div className="min-h-96 max-h-96">
                    <VariableList
                        variables={variables.variables}
                        onAdd={handleAddVariable}
                        onUpdate={handleUpdateVariable}
                        onDeleteClick={handleDeleteClick}
                        loading={variables.loading}
                    />
                </div>
            </div>

            <DeleteConfirmModal
                isOpen={deleteVariableModal.isOpen}
                onClose={deleteVariableModal.close}
                onConfirm={handleDeleteVariable}
                title="Delete Variable"
                message={`Are you sure you want to delete variable "${deleteVariableModal.data?.key}"?`}
                loading={variables.actionLoading}
            />
        </Modal>
    );
};

export default EnvironmentDetailsModal;
