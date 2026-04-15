import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Input, Button, Toast } from '../../components/ui';
import { GoogleIcon } from '../../components/icons/Icons';
import AuthLayout from './AuthLayout';
import { createOAuthProvider } from './patterns/OAuthProviderFactory';

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
      <h1 className="auth-heading">Welcome Back!</h1>
      <p className="auth-subheading">Sign in to continue to PeerTayo</p>

      {error && <div className="auth-error">{error}</div>}

      <form className="auth-form" onSubmit={handleSubmit}>
        <Input
          label="Email"
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
        <span>or continue with</span>
      </div>

      <button className="auth-social-btn" type="button" onClick={handleGoogleLogin}>
        <GoogleIcon size={18} />
        Continue with Google
      </button>

      {toast && (
        <Toast
          message="Welcome back! You have signed in successfully."
          onDismiss={() => setToast(false)}
          duration={1500}
        />
      )}
    </AuthLayout>
  );
}
