import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Input, Button, Toast } from '../../components/ui';
import { GoogleIcon } from '../../components/icons/Icons';
import AuthLayout from '../../features/auth/AuthLayout';
import { createOAuthProvider } from '../../services/auth/OAuthProviderService';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [toast, setToast] = useState(false);
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const googleProvider = createOAuthProvider('google');

  const handleGoogleLogin = () => {
    window.location.href = googleProvider.loginUrl;
  };

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
    <AuthLayout>
      <h1 className="auth-heading">Welcome back</h1>
      <p className="auth-subheading">Sign in to your PeerTayo account</p>

      {error && <div className="auth-error">{error}</div>}

      <form className="auth-form" onSubmit={handleSubmit}>
        <Input
          label="Email address"
          id="email"
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <Input
          label="Password"
          id="password"
          type="password"
          placeholder="Enter your password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Button type="submit" block disabled={loading}>
          {loading ? 'Signing in…' : 'Sign In'}
        </Button>
      </form>

      <div className="auth-divider">
        <span>or</span>
      </div>

      <button className="auth-social-btn" type="button" onClick={handleGoogleLogin}>
        <GoogleIcon size={17} />
        Continue with Google
      </button>

      <p className="auth-footer-link">
        Don't have an account? <Link to="/register">Create one</Link>
      </p>

      {toast && (
        <Toast
          message="Welcome back! Redirecting to your dashboard…"
          onDismiss={() => setToast(false)}
          duration={1500}
        />
      )}
    </AuthLayout>
  );
}
