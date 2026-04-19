import { Link, useLocation } from 'react-router-dom';
import { LogoIcon } from '../../components/icons/Icons';
import './AuthLayout.css';

const features = [
  {
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
    ),
    label: 'Criteria-based peer evaluation',
    delay: '0ms',
  },
  {
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
      </svg>
    ),
    label: 'Real-time performance analytics',
    delay: '80ms',
  },
  {
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
    ),
    label: 'Team-wide evaluation management',
    delay: '160ms',
  },
];

const stats = [
  { value: '10', label: 'Criteria' },
  { value: '360°', label: 'Feedback' },
  { value: '100%', label: 'Transparent' },
];

export default function AuthLayout({ children }) {
  const { pathname } = useLocation();
  const isLogin = pathname === '/login';

  return (
    <div className="auth-page">

      {/* ── Left panel ─────────────────────────────────────────────── */}
      <div className="auth-left">
        {/* Animated background layers */}
        <div className="auth-left__mesh" />
        <div className="auth-left__orb auth-left__orb--1" />
        <div className="auth-left__orb auth-left__orb--2" />
        <div className="auth-left__orb auth-left__orb--3" />

        <div className="auth-left__inner">
          {/* Brand */}
          <div className="auth-brand">
            <div className="auth-brand__logo-wrap">
              <LogoIcon size={28} />
            </div>
            <span className="auth-brand__name">PeerTayo</span>
          </div>

          {/* Hero */}
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

          {/* Stats strip */}
          <div className="auth-stats">
            {stats.map((s) => (
              <div key={s.label} className="auth-stat">
                <div className="auth-stat__value">{s.value}</div>
                <div className="auth-stat__label">{s.label}</div>
              </div>
            ))}
          </div>

          {/* Feature list */}
          <ul className="auth-features">
            {features.map((f) => (
              <li
                key={f.label}
                className="auth-feature-item"
                style={{ animationDelay: f.delay }}
              >
                <span className="auth-feature-icon">{f.icon}</span>
                <span>{f.label}</span>
              </li>
            ))}
          </ul>

          {/* Bottom badge */}
          <div className="auth-left__badge">
            <span className="auth-badge__pulse" />
            Trusted by student teams across CIT-U
          </div>
        </div>
      </div>

      {/* ── Right panel ────────────────────────────────────────────── */}
      <div className="auth-right">
        {/* Subtle right-side background orb */}
        <div className="auth-right__orb" />

        <div className="auth-form-shell">
          {/* Tab switcher */}
          <div className="auth-tabs">
            <Link to="/login"    className={`auth-tab${isLogin ? ' active' : ''}`}>Sign In</Link>
            <Link to="/register" className={`auth-tab${!isLogin ? ' active' : ''}`}>Register</Link>
          </div>

          {children}
        </div>
      </div>

    </div>
  );
}
