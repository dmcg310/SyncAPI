import {useCallback, useState} from 'react';
import {workspaceApi} from '../services/api';
import {getErrorMessage} from "../util/errors.ts";

interface UseWorkspaceMembersReturn {
    addMember: (email: string) => Promise<void>;
    removeMember: (userId: number) => Promise<void>;
    actionLoading: boolean;
    error: string | null;
}

export function useWorkspaceMembers(
    workspaceId: number | null,
    onMembersChange: () => Promise<void>
): UseWorkspaceMembersReturn {
    const [actionLoading, setActionLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const addMember = useCallback(async (email: string): Promise<void> => {
        if (!workspaceId) {
            return;
        }

        setActionLoading(true);
        setError(null);

        try {
            await workspaceApi.addMember(workspaceId, email);
            await onMembersChange();
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to add member');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, onMembersChange]);

    const removeMember = useCallback(async (userId: number): Promise<void> => {
        if (!workspaceId) {
            return;
        }

        setActionLoading(true);
        setError(null);

        try {
            await workspaceApi.removeMember(workspaceId, userId);
            await onMembersChange();
        } catch (err: unknown) {
            const message = getErrorMessage(err, 'Failed to remove member');
            setError(message);
            throw new Error(message);
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, onMembersChange]);

    return {
        addMember,
        removeMember,
        actionLoading,
        error
    };
}
