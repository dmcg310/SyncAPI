import React from 'react';
import {useParams} from 'react-router-dom';

const WorkspacePage: React.FC = () => {
    const {workspaceId} = useParams<{ workspaceId: string }>();

    return (
        <div>
        </div>
    );
};

export default WorkspacePage;
