import {createContext, ReactNode, useContext, useEffect, useState} from 'react';
import {authApi} from '../services/api';
import {STORAGE_KEYS} from '../util/constants';
import type {User} from '@/types';

interface AuthContextType {
    user: User | null;
    loading: boolean;
    login: (email: string, password: string) => Promise<User>;
    register: (name: string, email: string, password: string) => Promise<User>;
    logout: () => void;
    isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({children}: AuthProviderProps) {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
        if (!token) {
            setLoading(false);
            return;
        }

        authApi.me()
            .then((response) => setUser(response.data))
            .catch(() => {
                localStorage.removeItem(STORAGE_KEYS.TOKEN);
                localStorage.removeItem(STORAGE_KEYS.USER);
            })
            .finally(() => setLoading(false));
    }, []);

    const login = async (email: string, password: string): Promise<User> => {
        const response = await authApi.login({email, password});
        const {token} = response.data;
        localStorage.setItem(STORAGE_KEYS.TOKEN, token);

        const userResponse = await authApi.me();
        setUser(userResponse.data);
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(userResponse.data));

        return userResponse.data;
    };

    const register = async (name: string, email: string, password: string): Promise<User> => {
        await authApi.register({name, email, password});

        return await login(email, password);
    };

    const logout = () => {
        localStorage.removeItem(STORAGE_KEYS.TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER);
        setUser(null);
    };

    const value: AuthContextType = {
        user,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextType {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }

    return context;
}
