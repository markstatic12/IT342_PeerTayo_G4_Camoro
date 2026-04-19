import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Input, Button, Toast } from '../../components/ui';
import { LogoIcon, GoogleIcon } from '../../components/icons/Icons';
import { createOAuthProvider } from '../../services/auth/OAuthProviderService';
import './AuthPage.css';

const features = [
  {
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
    ),
    label: 'Criteria-based peer evaluation',
  },
  {
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
      </svg>
    ),
    label: 'Real-time performance analytics',
  },
  {
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
    ),
    label: 'Team-wide evaluation management',
  },
];

const stats = [
  { value: '10',   label: 'Criteria'    },
  { value: '360°', label: 'Feedback'    },
  { value: '100%', label: 'Transparent' },
];

/* ── Login form ─────────────────────────────────────────────────────── */
function LoginForm({ onSwitch }) {
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
    <div className="af-panel">
      <h1 className="af-heading">Welcome back</h1>
      <p className="af-sub">Sign in to your PeerTayo account</p>

      {error && <div className="af-error">{error}</div>}

      <form className="af-form" onSubmit={handleSubmit}>
        <Input
          label="Email address"
          id="login-email"
          type="email"
          placeholder="you@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <Input
          label="Password"
          id="login-password"
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

      <div className="af-divider"><span>or</span></div>

      <button
        className="af-social-btn"
        type="button"
        onClick={() => { window.location.href = googleProvider.loginUrl; }}
      >
        <GoogleIcon size={17} />
        Continue with Google
      </button>

      <p className="af-footer">
        Don't have an account?{' '}
        <button type="button" className="af-switch-btn" onClick={onSwitch}>
          Create one
        </button>
      </p>

      {toast && (
        <Toast
          message="Welcome back! Redirecting to your dashboard…"
          onDismiss={() => setToast(false)}
          duration={1500}
        />
      )}
    </div>
  );
}

/* ── Register form ──────────────────────────────────────────────────── */
function RegisterForm({ onSwitch }) {
  const [firstName, setFirstName]           = useState('');
  const [lastName, setLastName]             = useState('');
  const [email, setEmail]                   = useState('');
  const [password, setPassword]             = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError]                   = useState('');
  const [toast, setToast]                   = useState(false);
  const { register, loading }               = useAuth();
  const navigate                            = useNavigate();
  const googleProvider                      = createOAuthProvider('google');

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
    <div className="af-panel">
      <h1 className="af-heading">Create an account</h1>
      <p className="af-sub">Get started with PeerTayo for free</p>

      {error && <div className="af-error">{error}</div>}

      <form className="af-form" onSubmit={handleSubmit}>
        <div className="af-form__row">
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
        <Input
          label="Password"
          id="reg-password"
          type="password"
          placeholder="At least 6 characters"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <Input
          label="Confirm password"
          id="reg-confirmPassword"
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

      <div className="af-divider"><span>or</span></div>

      <button
        className="af-social-btn"
        type="button"
        onClick={() => { window.location.href = googleProvider.loginUrl; }}
      >
        <GoogleIcon size={17} />
        Continue with Google
      </button>

      <p className="af-footer">
        Already have an account?{' '}
        <button type="button" className="af-switch-btn" onClick={onSwitch}>
          Sign in
        </button>
      </p>

      {toast && (
        <Toast
          message={`Welcome, ${firstName}! Your account has been created.`}
          onDismiss={() => setToast(false)}
          duration={1500}
        />
      )}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════════════
   AuthPage — single mount, slides between login and register
   ══════════════════════════════════════════════════════════════════════ */
export default function AuthPage({ defaultTab = 'login' }) {
  const [tab, setTab] = useState(defaultTab);

  return (
    <div className="auth-page">

      {/* ── Left panel ─────────────────────────────────────────────── */}
      <div className="auth-left">
        <div className="auth-left__mesh" />
        <div className="auth-left__orb auth-left__orb--1" />
        <div className="auth-left__orb auth-left__orb--2" />
        <div className="auth-left__orb auth-left__orb--3" />

        <div className="auth-left__inner">
          <div className="auth-brand">
            <div className="auth-brand__logo-wrap"><LogoIcon size={26} /></div>
            <span className="auth-brand__name">PeerTayo</span>
          </div>

          <div className="auth-hero">
            <div className="auth-hero__eyebrow">
              <span className="auth-hero__eyebrow-dot" />
              Peer Evaluation Platform
            </div>
            <h1 className="auth-hero__heading">
              Evaluate smarter,<br />
              <span className="auth-hero__heading-accent">grow together.</span>
            </h1>
            <p className="auth-hero__sub">
              A structured, criteria-based system that helps teams give meaningful feedback and track growth over time.
            </p>
          </div>

          <div className="auth-stats">
            {stats.map((s) => (
              <div key={s.label} className="auth-stat">
                <div className="auth-stat__value">{s.value}</div>
                <div className="auth-stat__label">{s.label}</div>
              </div>
            ))}
          </div>

          <ul className="auth-features">
            {features.map((f, i) => (
              <li key={f.label} className="auth-feature-item" style={{ animationDelay: `${i * 80}ms` }}>
                <span className="auth-feature-icon">{f.icon}</span>
                <span>{f.label}</span>
              </li>
            ))}
          </ul>

          <div className="auth-left__badge">
            <span className="auth-badge__pulse" />
            Trusted by student teams across CIT-U
          </div>
        </div>
      </div>

      {/* ── Right panel ────────────────────────────────────────────── */}
      <div className="auth-right">
        <div className="auth-right__orb" />

        <div className="auth-right__inner">
          {/* Tab switcher */}
          <div className="auth-tabs">
            <button
              type="button"
              className={`auth-tab${tab === 'login' ? ' active' : ''}`}
              onClick={() => setTab('login')}
            >
              Sign In
            </button>
            <button
              type="button"
              className={`auth-tab${tab === 'register' ? ' active' : ''}`}
              onClick={() => setTab('register')}
            >
              Register
            </button>
          </div>

          {/* Sliding viewport */}
          <div className="auth-slider">
            <div className={`auth-slider__track${tab === 'register' ? ' shifted' : ''}`}>
              <div className="auth-slider__pane">
                <LoginForm onSwitch={() => setTab('register')} />
              </div>
              <div className="auth-slider__pane">
                <RegisterForm onSwitch={() => setTab('login')} />
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  );
}
