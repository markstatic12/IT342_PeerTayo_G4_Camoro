import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './features/auth/context/AuthContext';
import ProtectedRoute from './shared/routes/ProtectedRoute';
import RoleProtectedRoute from './shared/routes/RoleProtectedRoute';
import AppLayout from './shared/layouts/AppLayout';
import AuthShell from './features/auth/AuthShell';
import AuthCallbackPage from './features/auth/callback/AuthCallbackPage';
import LoginPage from './features/auth/login/LoginPage';
import RegisterPage from './features/auth/register/RegisterPage';
import DashboardPage from './features/dashboard/DashboardPage';
import FormsCreatedPage from './features/evaluation/form/FormsCreatedPage';
import CreateEvaluationPage from './features/evaluation/form/CreateEvaluationPage';
import EvaluationResultsPage from './features/evaluation/results/EvaluationResultsPage';
import PendingEvaluationsPage from './features/evaluation/submission/PendingEvaluationsPage';
import EvaluateFormPage from './features/evaluation/submission/EvaluateFormPage';

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

              {/* Respondent routes */}
              <Route path="/pending-evaluations" element={<PendingEvaluationsPage />} />
              <Route path="/my-results"  element={<div style={{padding:32,color:'#94a3b8'}}>My Results — coming soon</div>} />
              <Route path="/completed"   element={<div style={{padding:32,color:'#94a3b8'}}>My Completed Forms — coming soon</div>} />
              <Route path="/settings"    element={<div style={{padding:32,color:'#94a3b8'}}>Settings — coming soon</div>} />

              {/* Facilitator-only routes */}
              <Route element={<RoleProtectedRoute role="FACILITATOR" />}>
                <Route path="/forms-created"             element={<FormsCreatedPage />} />
                <Route path="/forms-created/new"         element={<CreateEvaluationPage />} />
                <Route path="/forms-created/:id/results" element={<EvaluationResultsPage />} />
              </Route>
            </Route>

            {/* Evaluate form — full-screen, outside AppLayout (has its own top bar) */}
            <Route path="/evaluate" element={<EvaluateFormPage />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
