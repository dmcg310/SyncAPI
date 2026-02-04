import React from 'react';
import {FaLock, FaLockOpen, FaUser} from 'react-icons/fa';

interface LockStatusProps {
    isLockedByMe: boolean;
    isLockedByOther: boolean;
    onRelease: () => void;
}

const LockStatus: React.FC<LockStatusProps> = ({isLockedByMe, isLockedByOther, onRelease}) => {
    if (isLockedByMe) {
        return (
            <div className="flex items-center gap-2">
                <span
                    className="flex items-center gap-1.5 px-3 py-1.5 text-md text-success-700 bg-success-50 rounded-lg">
                    <FaLock size={14}/>
                    Locked by you
                </span>
                <button
                    onClick={onRelease}
                    className="flex items-center gap-1.5 px-3 py-1.5 text-md text-neutral-600 hover:bg-neutral-100 rounded-lg transition-colors cursor-pointer"
                    title="Release lock"
                >
                    <FaLockOpen size={14}/>
                    Release
                </button>
            </div>
        );
    }

    if (isLockedByOther) {
        return (
            <span className="flex items-center gap-1.5 px-3 py-1.5 text-md text-warning-700 bg-warning-50 rounded-lg">
                <FaLock size={14}/>
                <FaUser size={12}/>
                Locked by another user
            </span>
        );
    }

    return (
        <span className="flex items-center gap-1.5 px-3 py-1.5 text-md text-neutral-500 bg-neutral-100 rounded-lg">
            <FaLockOpen size={14}/>
            Unlocked
        </span>
    );
};

export default LockStatus;
