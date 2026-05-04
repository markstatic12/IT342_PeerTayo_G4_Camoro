import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { LogoIcon } from '../../shared/components/icons/Icons';
import './AuthShell.css';

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
        <line x1="18" y1="20" x2="18" y2="10"/>
        <line x1="12" y1="20" x2="12" y2="4"/>
        <line x1="6"  y1="20" x2="6"  y2="14"/>
      </svg>
    ),
    label: 'Real-time performance analytics',
  },
  {
    icon: (
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
        <circle cx="9" cy="7" r="4"/>
        <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
        <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
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

export default function AuthShell() {
  const { pathname } = useLocation();

  return (
    <div className="ash-page">

      {/* ── Left panel — never unmounts ─────────────────────────── */}
      <div className="ash-left">
        <div className="ash-mesh" />
        <div className="ash-orb ash-orb--1" />
        <div className="ash-orb ash-orb--2" />
        <div className="ash-orb ash-orb--3" />

        <div className="ash-left__inner">
          {/* Brand */}
          <div className="ash-brand">
            <div className="ash-brand__icon"><LogoIcon size={26} /></div>
            <span className="ash-brand__name">PeerTayo</span>
          </div>

          {/* Hero */}
          <div className="ash-hero">
            <div className="ash-eyebrow">
              <span className="ash-eyebrow__dot" />
              Peer Evaluation Platform
            </div>
            <h1 className="ash-heading">
              Evaluate smarter,<br />
              <span className="ash-heading__accent">grow together.</span>
            </h1>
            <p className="ash-hero__sub">
              A structured, criteria-based system that helps teams give meaningful feedback and track growth over time.
            </p>
          </div>

          {/* Stats */}
          <div className="ash-stats">
            {stats.map((s) => (
              <div key={s.label} className="ash-stat">
                <div className="ash-stat__val">{s.value}</div>
                <div className="ash-stat__lbl">{s.label}</div>
              </div>
            ))}
          </div>

          {/* Features */}
          <ul className="ash-features">
            {features.map((f, i) => (
              <li
                key={f.label}
                className="ash-feature"
                style={{ animationDelay: `${i * 90}ms` }}
              >
                <span className="ash-feature__icon">{f.icon}</span>
                <span>{f.label}</span>
              </li>
            ))}
          </ul>

          {/* Badge */}
          <div className="ash-badge">
            <span className="ash-badge__dot" />
            Trusted by student teams across CIT-U
          </div>
        </div>
      </div>

      {/* ── Right panel — Outlet swaps, shell stays ─────────────── */}
      <div className="ash-right">
        <div className="ash-right__orb" />

        <div className="ash-right__inner">
          {/* Tab nav — proper NavLink routing */}
          <nav className="ash-tabs">
            <NavLink
              to="/login"
              className={({ isActive }) => `ash-tab${isActive ? ' active' : ''}`}
            >
              Sign In
            </NavLink>
            <NavLink
              to="/register"
              className={({ isActive }) => `ash-tab${isActive ? ' active' : ''}`}
            >
              Register
            </NavLink>
          </nav>

          {/* Form area — key on pathname triggers CSS enter animation */}
          <div className="ash-form-area" key={pathname}>
            <Outlet />
          </div>
        </div>
      </div>

    </div>
  );
}
