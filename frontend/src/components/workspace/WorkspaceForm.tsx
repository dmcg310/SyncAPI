import React from 'react';
import NameDescriptionForm from '../common/NameDescriptionForm';
import type {Workspace, WorkspaceRequest} from '@/types';

interface WorkspaceFormProps {
    workspace?: Workspace | null;
    onSubmit: (data: WorkspaceRequest) => Promise<void>;
    onCancel: () => void;
    loading?: boolean;
}

const WorkspaceForm: React.FC<WorkspaceFormProps> = ({workspace, onSubmit, onCancel, loading}) => (
    <NameDescriptionForm
        initialName={workspace?.name}
        initialDescription={workspace?.description}
        onSubmit={onSubmit}
        onCancel={onCancel}
        loading={loading}
        submitLabel={workspace ? 'Update' : 'Create'}
        namePlaceholder="My API Project"
        descriptionPlaceholder="Optional description for this workspace..."
    />
);

export default WorkspaceForm;
