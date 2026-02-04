import React from 'react';
import NameDescriptionForm from '../common/NameDescriptionForm';
import type {Folder, FolderRequest} from '@/types';

interface FolderFormProps {
    folder?: Folder | null;
    onSubmit: (data: FolderRequest) => Promise<void>;
    onCancel: () => void;
    loading?: boolean;
}

const FolderForm: React.FC<FolderFormProps> = ({folder, onSubmit, onCancel, loading}) => (
    <NameDescriptionForm
        initialName={folder?.name}
        initialDescription={folder?.description}
        onSubmit={onSubmit}
        onCancel={onCancel}
        loading={loading}
        submitLabel={folder ? 'Update' : 'Create'}
        namePlaceholder="User Endpoints"
        descriptionPlaceholder="Optional description for this folder..."
    />
);

export default FolderForm;
