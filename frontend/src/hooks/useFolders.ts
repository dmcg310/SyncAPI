import {useCallback, useEffect, useState} from 'react';
import {folderApi} from '../services/api';
import type {Folder, FolderRequest} from '@/types';

interface UseFoldersReturn {
    folders: Folder[];
    selectedFolder: Folder | null;
    loading: boolean;
    error: string | null;
    selectFolder: (folder: Folder | null) => void;
    reload: () => Promise<void>;
    create: (data: FolderRequest) => Promise<Folder | null>;
    update: (folderId: number, data: FolderRequest) => Promise<Folder | null>;
    remove: (folderId: number) => Promise<boolean>;
    actionLoading: boolean;
}

export function useFolders(workspaceId: number | null): UseFoldersReturn {
    const [folders, setFolders] = useState<Folder[]>([]);
    const [selectedFolder, setSelectedFolder] = useState<Folder | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [actionLoading, setActionLoading] = useState(false);

    const reload = useCallback(async () => {
        if (!workspaceId) {
            return;
        }

        setLoading(true);
        setError(null);

        try {
            const response = await folderApi.getByWorkspace(workspaceId);
            setFolders(response.data);
        } catch {
            setError('Failed to load folders');
        } finally {
            setLoading(false);
        }
    }, [workspaceId]);

    useEffect(() => {
        if (workspaceId) {
            reload();
            setSelectedFolder(null);
        }
    }, [workspaceId, reload]);

    const selectFolder = useCallback((folder: Folder | null) => {
        setSelectedFolder(folder);
    }, []);

    const create = useCallback(async (data: FolderRequest): Promise<Folder | null> => {
        if (!workspaceId) {
            return null;
        }

        setActionLoading(true);

        try {
            const response = await folderApi.create(workspaceId, data);
            await reload();

            return response.data;
        } catch {
            throw new Error('Failed to create folder');
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload]);

    const update = useCallback(async (folderId: number, data: FolderRequest): Promise<Folder | null> => {
        if (!workspaceId) {
            return null;
        }

        setActionLoading(true);

        try {
            const response = await folderApi.update(workspaceId, folderId, data);
            await reload();

            if (selectedFolder?.id === folderId) {
                setSelectedFolder(prev => prev
                    ? {...prev, ...data}
                    : null);
            }

            return response.data;
        } catch {
            throw new Error('Failed to update folder');
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload, selectedFolder?.id]);

    const remove = useCallback(async (folderId: number): Promise<boolean> => {
        if (!workspaceId) {
            return false;
        }

        setActionLoading(true);

        try {
            await folderApi.delete(workspaceId, folderId);
            await reload();

            if (selectedFolder?.id === folderId) {
                setSelectedFolder(null);
            }

            return true;
        } catch {
            throw new Error('Failed to delete folder');
        } finally {
            setActionLoading(false);
        }
    }, [workspaceId, reload, selectedFolder?.id]);

    return {
        folders,
        selectedFolder,
        loading,
        error,
        selectFolder,
        reload,
        create,
        update,
        remove,
        actionLoading
    };
}
