import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Input, Button, Toast } from '../../components/ui';
import PasswordInput from '../../components/ui/PasswordInput';
import { GoogleIcon } from '../../components/icons/Icons';
import { createOAuthProvider } from '../../services/auth/OAuthProviderService';
import '../../features/auth/AuthShell.css';

export default function RegisterPage() {
  const [firstName, setFirstName]             = useState('');
  const [lastName, setLastName]               = useState('');
  const [email, setEmail]                     = useState('');
  const [password, setPassword]               = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError]                     = useState('');
  const [toast, setToast]                     = useState(false);
  const { register, loading }                 = useAuth();
  const navigate                              = useNavigate();
  const googleProvider                        = createOAuthProvider('google');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (password !== confirmPassword) { setError('Passwords do not match.'); return; }
    if (password.length < 6)          { setError('Password must be at least 6 characters.'); return; }

    const result = await register(firstName, lastName, email, password);
    if (result.success) {
      setToast(true);
      setTimeout(() => navigate('/dashboard'), 1500);
    } else {
      setError(result.message);
    }
  };

  return (
    <>
      <h1 className="ash-form-heading">Create an account</h1>
      <p className="ash-form-sub">Get started with PeerTayo for free</p>

      {error && <div className="ash-error">{error}</div>}

      <form className="ash-form" onSubmit={handleSubmit}>
        <div className="ash-form__row">
          <Input
            label="First name"
            id="reg-firstName"
            type="text"
            placeholder="Juan"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            required
          />
          <Input
            label="Last name"
            id="reg-lastName"
            type="text"
            placeholder="Dela Cruz"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            required
          />
        </div>
        <Input
          label="Email address"
          id="reg-email"
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <PasswordInput
          label="Password"
          id="reg-password"
          placeholder="At least 6 characters"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <PasswordInput
          label="Confirm password"
          id="reg-confirmPassword"
          placeholder="Re-enter your password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
        />
        <Button type="submit" block disabled={loading}>
          {loading ? 'Creating account…' : 'Create Account'}
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
        Already have an account? <Link to="/login">Sign in</Link>
      </p>

      {toast && (
        <Toast
          message={`Welcome, ${firstName}! Your account has been created.`}
          onDismiss={() => setToast(false)}
          duration={1500}
        />
      )}
    </>
  );
}
