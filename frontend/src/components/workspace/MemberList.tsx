import React, {useState} from 'react';
import {IoAdd} from 'react-icons/io5';
import {MdPeople} from 'react-icons/md';
import MemberCard from './MemberCard';
import type {Member} from '@/types';

interface MemberListProps {
    members: Member[];
    currentUserId: number;
    onAddMember: (email: string) => Promise<void>;
    onRemoveMember: (member: Member) => void;
    loading: boolean;
}

const MemberList: React.FC<MemberListProps> = ({
                                                   members,
                                                   currentUserId,
                                                   onAddMember,
                                                   onRemoveMember,
                                                   loading
                                               }) => {
    const [email, setEmail] = useState('');
    const [showAddForm, setShowAddForm] = useState(false);
    const [addError, setAddError] = useState<string | null>(null);

    const handleAddMember = async (e: React.SubmitEvent) => {
        e.preventDefault();
        setAddError(null);

        if (!email.trim()) {
            setAddError('Email is required');

            return;
        }

        try {
            await onAddMember(email.trim());
            setEmail('');
            setShowAddForm(false);
        } catch (error) {
            setAddError(error instanceof Error ? error.message : 'Failed to add member');
        }
    };

    return (
        <div className="h-125 flex flex-col">
            <div className="flex items-center justify-between pb-4 border-b border-neutral-200">
                <h3 className="font-semibold text-neutral-900">
                    {members.length} {members.length === 1 ? 'member' : 'members'}
                </h3>
                <button
                    onClick={() => setShowAddForm(!showAddForm)}
                    className="p-1.5 text-neutral-500 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors cursor-pointer"
                    title="Add Member"
                >
                    <IoAdd size={30}/>
                </button>
            </div>

            {showAddForm && (
                <div className="p-4 bg-neutral-50 border-b border-neutral-200">
                    <form onSubmit={handleAddMember}>
                        <div className="flex gap-2">
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="Enter email address"
                                className="flex-1 px-3 py-2 border border-neutral-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                                disabled={loading}
                            />
                            <button
                                type="submit"
                                disabled={loading}
                                className="px-4 py-2 bg-primary-600 hover:bg-primary-700 text-white rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                            >
                                {loading ? 'Adding...' : 'Add'}
                            </button>
                        </div>
                        {addError && (
                            <p className="mt-2 text-sm text-error-600">{addError}</p>
                        )}
                    </form>
                </div>
            )}

            <div className="flex-1 overflow-y-auto p-2">
                {members.length === 0 ? (
                    <div className="text-center py-8 px-4">
                        <div
                            className="w-12 h-12 bg-neutral-100 rounded-full flex items-center justify-center mx-auto mb-3">
                            <MdPeople size={30}/>
                        </div>
                        <p className="text-md text-neutral-600 mb-3">No members yet</p>
                    </div>
                ) : (
                    <div className="space-y-1">
                        {members.map((member) => (
                            <MemberCard
                                key={member.userId}
                                member={member}
                                onRemove={onRemoveMember}
                                isCurrentUser={member.userId === currentUserId}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default MemberList;
