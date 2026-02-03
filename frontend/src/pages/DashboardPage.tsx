import React from 'react';
import Header from "../components/layout/Header"

const DashboardPage: React.FC = () => {
    return (
        <div className="min-h-screen bg-neutral-50">
            <Header/>
            <main className="max-w-7xl mx-auto px-4 py-8">
                <h2 className="text-2xl font-bold text-neutral-900 mb-6">Workspaces</h2>
            </main>
        </div>
    );
};

export default DashboardPage;
