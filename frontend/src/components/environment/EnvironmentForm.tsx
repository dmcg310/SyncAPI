import React from 'react';
import NameDescriptionForm from '../common/NameDescriptionForm';
import type {Environment, EnvironmentRequest} from '@/types';

interface EnvironmentFormProps {
    environment?: Environment | null;
    onSubmit: (data: EnvironmentRequest) => Promise<void>;
    onCancel: () => void;
    loading?: boolean;
}

const EnvironmentForm: React.FC<EnvironmentFormProps> = ({
                                                             environment,
                                                             onSubmit,
                                                             onCancel,
                                                             loading = false
                                                         }) => {
    return (
        <NameDescriptionForm
            initialName={environment?.name}
            initialDescription={environment?.description}
            onSubmit={onSubmit}
            onCancel={onCancel}
            loading={loading}
            submitLabel={environment ? 'Update' : 'Create'}
            namePlaceholder="Environment name (e.g., Production, Development)"
            descriptionPlaceholder="Optional description..."
        />
    );
};

export default EnvironmentForm;
