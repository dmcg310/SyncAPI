import {BrowserRouter, Navigate, Route, Routes} from 'react-router-dom';
import {AuthProvider, useAuth} from './context/AuthContext.tsx';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import WorkspacePage from "./pages/WorkspacePage.tsx";
import ProfilePage from "./pages/ProfilePage.tsx";
import {ReactNode} from 'react';
import Spinner from "./components/common/Spinner.tsx";

interface RouteGuardProps {
    children: ReactNode;
}

function ProtectedRoute({children}: RouteGuardProps) {
    const {isAuthenticated, loading} = useAuth();
    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <Spinner/>
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
                <Spinner/>
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
            <Route path="/workspace/:workspaceId" element={<ProtectedRoute><WorkspacePage/></ProtectedRoute>}/>
            <Route path="/profile" element={<ProtectedRoute><ProfilePage/></ProtectedRoute>}/>
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
