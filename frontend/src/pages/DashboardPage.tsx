import React from 'react';
import {useAuth} from '../context/AuthContext';

const DashboardPage: React.FC = () => {
    const {user, logout} = useAuth();

    return (
        <div className="min-h-screen bg-neutral-50">
            <header className="bg-white shadow-sm border-b border-neutral-200">
                <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
                    <h1 className="text-xl font-bold text-neutral-900">SyncAPI</h1>
                    <div className="flex items-center gap-4">
                        <span className="text-sm text-neutral-600">
                            {user?.name}
                        </span>
                        <button
                            onClick={logout}
                            className="text-sm text-neutral-600 hover:text-neutral-900"
                        >
                            Sign out
                        </button>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-4 py-8">
                <h2 className="text-2xl font-bold text-neutral-900 mb-6">Workspaces</h2>
            </main>
        </div>
    );
};

export default DashboardPage;
