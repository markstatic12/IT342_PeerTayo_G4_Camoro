import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Input, Button, Toast } from '../../../shared/components/ui';
import PasswordInput from '../../../shared/components/ui/PasswordInput';
import { GoogleIcon } from '../../../shared/components/icons/Icons';
import { createOAuthProvider } from '../oauth2/OAuthProviderService';
import '../AuthShell.css';

export default function LoginPage() {
  const [email, setEmail]       = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [toast, setToast]       = useState(false);
  const { login, loading }      = useAuth();
  const navigate                = useNavigate();
  const googleProvider          = createOAuthProvider('google');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const result = await login(email, password);
    if (result.success) {
      setToast(true);
      setTimeout(() => navigate('/dashboard'), 1500);
    } else {
      setError(result.message);
    }
  };

  return (
    <>
      <h1 className="ash-form-heading">Welcome back</h1>
      <p className="ash-form-sub">Sign in to your PeerTayo account</p>

      {error && <div className="ash-error">{error}</div>}

      <form className="ash-form" onSubmit={handleSubmit}>
        <Input
          label="Email address"
          id="login-email"
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <PasswordInput
          label="Password"
          id="login-password"
          placeholder="Enter your password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Button type="submit" block disabled={loading}>
          {loading ? 'Signing in…' : 'Sign In'}
        </Button>
      </form>

      <div className="ash-divider"><span>or</span></div>

      <button
        className="ash-social-btn"
        type="button"
        onClick={() => { window.location.href = googleProvider.loginUrl; }}
      >
        <GoogleIcon size={17} />
        Continue with Google
      </button>

      <p className="ash-footer">
        Don't have an account? <Link to="/register">Create one</Link>
      </p>

      {toast && (
        <Toast
          message="Welcome back! Redirecting to your dashboard…"
          onDismiss={() => setToast(false)}
          duration={1500}
        />
      )}
    </>
  );
}
