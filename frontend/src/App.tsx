import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import {AuthProvider, useAuth} from './context/AuthContext.tsx';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import {ReactNode} from 'react';

interface RouteGuardProps {
    children: ReactNode;
}

function ProtectedRoute({children}: RouteGuardProps) {
    const {isAuthenticated, loading} = useAuth();
    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            </div>
        );
    }

    return isAuthenticated
        ? <>{children}</>
        : <Navigate to="/login"/>;
}

function PublicRoute({children}: RouteGuardProps) {
    const {isAuthenticated, loading} = useAuth();
    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
            </div>
        );
    }

    return isAuthenticated
        ? <Navigate to="/dashboard"/>
        : <>{children}</>;
}

function AppRoutes() {
    return (
        <Routes>
            <Route path="/login" element={<PublicRoute><LoginPage/></PublicRoute>}/>
            <Route path="/register" element={<PublicRoute><RegisterPage/></PublicRoute>}/>
            <Route path="/dashboard" element={<ProtectedRoute><DashboardPage/></ProtectedRoute>}/>
            <Route path="/" element={<Navigate to="/dashboard"/>}/>
        </Routes>
    );
}

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <AppRoutes/>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
