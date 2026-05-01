import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './routes/ProtectedRoute';
import RoleProtectedRoute from './routes/RoleProtectedRoute';
import AppLayout from './layouts/AppLayout';
import AuthShell from './features/auth/AuthShell';
import AuthCallbackPage from './features/auth/AuthCallbackPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import DashboardPage from './pages/dashboard/DashboardPage';
import FormsCreatedPage from './pages/forms/FormsCreatedPage';
import CreateEvaluationPage from './pages/forms/CreateEvaluationPage';
import EvaluationResultsPage from './pages/forms/EvaluationResultsPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Auth routes — AuthShell stays mounted, only Outlet swaps */}
          <Route element={<AuthShell />}>
            <Route path="/login"    element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Route>
          <Route path="/auth/callback" element={<AuthCallbackPage />} />

          {/* Protected routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/dashboard" element={<DashboardPage />} />

              {/* Facilitator-only routes */}
              <Route element={<RoleProtectedRoute role="FACILITATOR" />}>
                <Route path="/forms-created"             element={<FormsCreatedPage />} />
                <Route path="/forms-created/new"         element={<CreateEvaluationPage />} />
                <Route path="/forms-created/:id/results" element={<EvaluationResultsPage />} />
              </Route>
            </Route>
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
