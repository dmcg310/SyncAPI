import React from 'react';
import {useNavigate} from 'react-router-dom';
import {useAuth} from '../context/AuthContext';
import Header from '../components/layout/Header';
import ChangePasswordForm from '../components/profile/ChangePasswordForm';
import {FaSignOutAlt} from "react-icons/fa";

const ProfilePage: React.FC = () => {
    const {user, logout} = useAuth();
    const navigate = useNavigate();

    if (!user) {
        return null;
    }

    const handleSignOut = () => {
        logout();
        navigate('/login');
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    return (
        <div className="min-h-screen bg-neutral-50">
            <Header/>
            <div className="max-w-xl mx-auto py-8 px-6">
                <div className="bg-white rounded-lg shadow-sm border border-neutral-200">
                    <div className="px-6 py-5 border-b border-neutral-200">
                        <h1 className="text-2xl font-semibold text-neutral-900">Profile</h1>
                    </div>

                    <div className="p-6">
                        <div className="mb-8">
                            <h2 className="text-lg font-medium text-neutral-900 mb-4">Account Information</h2>
                            <div className="space-y-4">
                                <div>
                                    <label className="block text-md font-medium text-neutral-700 mb-1">
                                        Name
                                    </label>
                                    <p className="text-md text-neutral-900">
                                        {user.name}
                                    </p>
                                </div>

                                <div>
                                    <label className="block text-md font-medium text-neutral-700 mb-1">
                                        Email
                                    </label>
                                    <p className="text-md text-neutral-900">
                                        {user.email}
                                    </p>
                                </div>

                                <div>
                                    <label className="block text-md font-medium text-neutral-700 mb-1">
                                        Member since
                                    </label>
                                    <p className="text-md text-neutral-900">
                                        {formatDate(user.createdAt)}
                                    </p>
                                </div>
                            </div>
                        </div>

                        <ChangePasswordForm/>

                        <div className="pt-6 border-t border-neutral-200">
                            <button
                                onClick={handleSignOut}
                                className="flex items-center gap-2 px-4 py-2 bg-error-600 text-white rounded-lg hover:bg-error-700 transition-colors font-medium cursor-pointer"
                            >
                                Sign out <FaSignOutAlt size={20}/>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ProfilePage;
