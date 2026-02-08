import React from 'react';
import {MdDeleteForever} from 'react-icons/md';
import type {Member} from '@/types';
import {getInitials} from '../../util/stringUtils';

interface MemberCardProps {
    member: Member;
    onRemove: (member: Member) => void;
    isCurrentUser: boolean;
}

const MemberCard: React.FC<MemberCardProps> = ({member, onRemove, isCurrentUser}) => {
    return (
        <div
            className="group flex items-center justify-between px-3 py-2.5 rounded-lg hover:bg-neutral-50 transition-colors">
            <div className="flex items-center gap-3 flex-1 min-w-0">
                <div
                    className="w-10 h-10 bg-primary-100 text-primary-700 rounded-full flex items-center justify-center shrink-0 font-semibold">
                    {getInitials(member.name)}
                </div>
                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                        <span className="text-md font-medium text-neutral-900 truncate">
                            {member.name}
                        </span>
                        {isCurrentUser && (
                            <span className="text-sm px-2 py-0.5 bg-primary-100 text-primary-700 rounded-full">
                                You
                            </span>
                        )}
                    </div>
                    <span className="text-md text-neutral-500 truncate block">
                        {member.email}
                    </span>
                </div>
            </div>
            {!isCurrentUser && (
                <button
                    onClick={() => onRemove(member)}
                    className="p-1 text-neutral-400 hover:text-error-600 hover:bg-error-50 rounded transition-colors opacity-0 group-hover:opacity-100 cursor-pointer"
                    title="Remove member"
                >
                    <MdDeleteForever size={22.5}/>
                </button>
            )}
        </div>
    );
};

export default MemberCard;
