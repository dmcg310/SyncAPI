import React from 'react';
import {Link} from 'react-router-dom';
import {useAuth} from '../../context/AuthContext';
import {getInitials} from "../../util/stringUtils.ts";

interface HeaderProps {
    showAuth?: boolean;
}

const Header: React.FC<HeaderProps> = ({showAuth = true}) => {
    const {user, isAuthenticated} = useAuth();

    return (
        <header className="bg-white shadow-md border-b border-neutral-200">
            <div className="max-w-7xl mx-auto px-4 py-3 flex justify-between items-center">
                <Link to={isAuthenticated ? '/dashboard' : '/'} className="flex items-center gap-3">
                    <img src="/syncapi.png" alt="SyncAPI" className="h-20 w-auto"/>
                </Link>

                {showAuth && (
                    <div className="flex items-center gap-4">
                        {isAuthenticated ? (
                            <Link
                                to="/profile"
                                className="text-md text-neutral-600 hover:text-neutral-900 transition-colors cursor-pointer"
                            >
                                <div
                                    className="w-10 h-10 bg-primary-100 text-primary-700 rounded-full flex items-center justify-center shrink-0 font-semibold text-xl hover:bg-primary-200 transition-colors">
                                    {user?.name ? getInitials(user.name) : '?'}
                                </div>
                            </Link>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className="text-md text-neutral-600 hover:text-neutral-900 transition-colors"
                                >
                                    Sign in
                                </Link>
                                <Link
                                    to="/register"
                                    className="text-md bg-primary-600 text-white px-4 py-2 rounded-lg hover:bg-primary-700 transition-colors"
                                >
                                    Sign up
                                </Link>
                            </>
                        )}
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;
