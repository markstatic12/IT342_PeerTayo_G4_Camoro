import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AuthCallbackPage.css';

/**
 * Landing page for the Google OAuth2 redirect.
 *
 * Spring's OAuth2LoginSuccessHandler redirects here as:
 *   /auth/callback?token=<JWT>       – success
 *   /auth/callback?error=<message>   – failure
 */
export default function AuthCallbackPage() {
  const [searchParams] = useSearchParams();
  const { loginWithToken } = useAuth();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading'); // 'loading' | 'success' | 'error'
  const [message, setMessage] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (error) {
      const decoded = decodeURIComponent(error);
      setMessage(decoded || 'Google sign-in failed. Please try again.');
      setStatus('error');
      setTimeout(() => navigate('/login', { replace: true }), 3000);
      return;
    }

    if (token) {
      const decoded = decodeURIComponent(token);
      loginWithToken(decoded).then((result) => {
        if (result.success) {
          const firstName = result.user?.firstName || 'there';
          setMessage(`Welcome, ${firstName}! You have signed in successfully.`);
          setStatus('success');
          setTimeout(() => navigate('/dashboard', { replace: true }), 1800);
        } else {
          setMessage(result.message || 'Sign-in failed. Please try again.');
          setStatus('error');
          setTimeout(() => navigate('/login', { replace: true }), 3000);
        }
      });
    } else {
      navigate('/login', { replace: true });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="acb-page">
      {/* Background orbs — same as AuthShell */}
      <div className="acb-orb acb-orb--1" />
      <div className="acb-orb acb-orb--2" />
      <div className="acb-mesh" />

      <div className="acb-card">
        {/* Brand */}
        <div className="acb-brand">
          <div className="acb-brand__icon">
            {/* Google G mark */}
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
              <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
              <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
              <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05"/>
              <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
            </svg>
          </div>
          <span className="acb-brand__name">PeerTayo</span>
        </div>

        {/* Status area */}
        {status === 'loading' && (
          <div className="acb-status">
            <div className="acb-spinner">
              <div className="acb-spinner__ring" />
            </div>
            <p className="acb-status__title">Signing you in…</p>
            <p className="acb-status__sub">Completing Google authentication, please wait.</p>
          </div>
        )}

        {status === 'success' && (
          <div className="acb-status acb-status--success">
            <div className="acb-check">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </div>
            <p className="acb-status__title">{message}</p>
            <p className="acb-status__sub">Redirecting you to your dashboard…</p>
          </div>
        )}

        {status === 'error' && (
          <div className="acb-status acb-status--error">
            <div className="acb-error-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
            <p className="acb-status__title">Sign-in failed</p>
            <p className="acb-status__sub">{message}</p>
            <p className="acb-status__redirect">Redirecting back to login…</p>
          </div>
        )}
      </div>
    </div>
  );
}
