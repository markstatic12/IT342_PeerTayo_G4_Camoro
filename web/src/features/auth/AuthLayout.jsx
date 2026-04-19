import { Link, useLocation } from 'react-router-dom';
import { LogoIcon } from '../../components/icons/Icons';
import './AuthLayout.css';

const features = [
  {
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
      </svg>
    ),
    text: 'Criteria-based peer evaluation',
  },
  {
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
      </svg>
    ),
    text: 'Real-time performance analytics',
  },
  {
    icon: (
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>
      </svg>
    ),
    text: 'Team-wide evaluation management',
  },
];

export default function AuthLayout({ children }) {
  const { pathname } = useLocation();
  const isLogin = pathname === '/login';

  return (
    <div className="auth-page">

      {/* ── Left panel ─────────────────────────────────────────────── */}
      <div className="auth-left">
        <div className="auth-left__inner">
          {/* Brand */}
          <div className="auth-brand">
            <LogoIcon size={30} />
            <span className="auth-brand__name">PeerTayo</span>
          </div>

          {/* Hero text */}
          <div className="auth-hero">
            <div className="auth-hero__eyebrow">Peer Evaluation Platform</div>
            <h1 className="auth-hero__heading">
              Evaluate smarter,<br />grow together.
            </h1>
            <p className="auth-hero__sub">
              A structured, criteria-based system that helps teams give meaningful feedback and track growth over time.
            </p>
          </div>

          {/* Feature list */}
          <ul className="auth-features">
            {features.map((f) => (
              <li key={f.text} className="auth-feature-item">
                <span className="auth-feature-icon">{f.icon}</span>
                <span>{f.text}</span>
              </li>
            ))}
          </ul>

          {/* Bottom badge */}
          <div className="auth-left__badge">
            Trusted by student teams across CIT-U
          </div>
        </div>

        {/* Decorative glow */}
        <div className="auth-left__glow" />
      </div>

      {/* ── Right panel ────────────────────────────────────────────── */}
      <div className="auth-right">
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
