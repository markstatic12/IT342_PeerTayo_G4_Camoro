import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../../features/auth/context/AuthContext';

/**
 * Route guard — redirects to /login if not authenticated.
 */
export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Outlet />;
}
