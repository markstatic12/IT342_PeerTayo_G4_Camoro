import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Input, Button, Toast } from '../../components/ui';
import { GoogleIcon } from '../../components/icons/Icons';
import AuthLayout from './AuthLayout';
import { createOAuthProvider } from './patterns/OAuthProviderFactory';

export default function RegisterPage() {
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [toast, setToast] = useState(false);
  const { register, loading } = useAuth();
  const navigate = useNavigate();
  const googleProvider = createOAuthProvider('google');

  const handleGoogleLogin = () => {
    window.location.href = googleProvider.loginUrl;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    if (password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    const result = await register(firstName, lastName, email, password);
    if (result.success) {
      setToast(true);
      setTimeout(() => navigate('/dashboard'), 1500);
    } else {
      setError(result.message);
    }
  };

  return (
    <AuthLayout>
      <h1 className="auth-heading">Create an Account</h1>
      <p className="auth-subheading">Get started with PeerTayo</p>

      {error && <div className="auth-error">{error}</div>}

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="auth-form__row">
          <Input
            label="First Name"
            id="firstName"
            type="text"
            placeholder="Juan"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            required
          />
          <Input
            label="Last Name"
            id="lastName"
            type="text"
            placeholder="Dela Cruz"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            required
          />
        </div>

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
          placeholder="At least 6 characters"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Input
          label="Confirm Password"
          id="confirmPassword"
          type="password"
          placeholder="Re-enter your password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
        />

        <Button type="submit" block disabled={loading}>
          {loading ? 'Creating account…' : 'Create Account'}
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
          message={`Account created! Welcome to PeerTayo, ${firstName}!`}
          onDismiss={() => setToast(false)}
          duration={1500}
        />
      )}
    </AuthLayout>
  );
}
