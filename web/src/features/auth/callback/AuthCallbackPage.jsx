import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Toast from '../../../shared/components/ui/Toast';

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
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    const token = searchParams.get('token');
    const error = searchParams.get('error');

    if (error) {
      const decoded = decodeURIComponent(error);
      setErrorMsg(decoded);
      setTimeout(() => navigate('/login', { replace: true }), 3000);
      return;
    }

    if (token) {
      const decoded = decodeURIComponent(token);
      loginWithToken(decoded).then((result) => {
        if (result.success) {
          const firstName = result.user?.firstName || 'there';
          setSuccessMsg(`Welcome, ${firstName}! You have signed in successfully.`);
          setTimeout(() => navigate('/dashboard', { replace: true }), 1800);
        } else {
          setErrorMsg(result.message || 'Sign-in failed. Please try again.');
          setTimeout(() => navigate('/login', { replace: true }), 3000);
        }
      });
    } else {
      navigate('/login', { replace: true });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (errorMsg) {
    return (
      <div style={{ textAlign: 'center', padding: '4rem 2rem' }}>
        <p style={{ color: '#e53e3e', fontWeight: 600 }}>Google sign-in failed</p>
        <p style={{ color: '#718096', marginTop: '0.5rem' }}>{errorMsg}</p>
        <p style={{ color: '#a0aec0', marginTop: '0.25rem', fontSize: '0.875rem' }}>
          Redirecting to login…
        </p>
      </div>
    );
  }

  return (
    <div style={{ textAlign: 'center', padding: '4rem 2rem' }}>
      <p style={{ color: '#4a5568' }}>Completing sign-in, please wait…</p>

      {successMsg && (
        <Toast
          message={successMsg}
          onDismiss={() => {}}
          duration={1800}
        />
      )}
    </div>
  );
}
