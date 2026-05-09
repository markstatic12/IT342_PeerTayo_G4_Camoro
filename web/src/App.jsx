import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './features/auth/context/AuthContext';
import { NavigationGuardProvider } from './shared/context/NavigationGuardContext';
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
import EvaluateeResultsPage from './features/evaluation/results/EvaluateeResultsPage';
import MyResultsPage from './features/evaluation/results/MyResultsPage';
import MyCompletedFormsPage from './features/evaluation/submission/MyCompletedFormsPage';
import PendingEvaluationsPage from './features/evaluation/submission/PendingEvaluationsPage';
import EvaluateFormPage from './features/evaluation/submission/EvaluateFormPage';
import SettingsPage from './features/settings/SettingsPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <NavigationGuardProvider>
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
              <Route path="/my-results"  element={<MyResultsPage />} />
              <Route path="/completed"   element={<MyCompletedFormsPage />} />
              <Route path="/settings"    element={<SettingsPage />} />

              {/* Facilitator-only routes */}
              <Route element={<RoleProtectedRoute role="FACILITATOR" />}>
                <Route path="/forms-created"             element={<FormsCreatedPage />} />
                <Route path="/forms-created/new"         element={<CreateEvaluationPage />} />
                <Route path="/forms-created/:id/edit"    element={<CreateEvaluationPage />} />
                <Route path="/forms-created/:id/results" element={<EvaluationResultsPage />} />
                <Route path="/forms-created/:id/evaluatee/:userId" element={<EvaluateeResultsPage />} />
              </Route>
            </Route>

            {/* Evaluate form — full-screen, outside AppLayout (has its own top bar) */}
            <Route path="/evaluate" element={<EvaluateFormPage />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
        </NavigationGuardProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
