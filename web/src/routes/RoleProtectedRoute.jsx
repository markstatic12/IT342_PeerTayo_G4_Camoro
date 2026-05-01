import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

/**
 * Route guard that requires the authenticated user to have a specific role.
 * Redirects to /dashboard with a state flag if the role requirement is not met.
 *
 * Usage: <RoleProtectedRoute role="FACILITATOR" />
 */
export default function RoleProtectedRoute({ role }) {
  const { user } = useAuth();

  const hasRole = user?.roles?.some(
    (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === role.toUpperCase()
  );

  if (!hasRole) {
    return <Navigate to="/dashboard" state={{ upgradeRequired: role }} replace />;
  }

  return <Outlet />;
}
