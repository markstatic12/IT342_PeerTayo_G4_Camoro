import { Link, useLocation } from 'react-router-dom';
import { LogoIcon, TrendUpIcon } from '../../components/icons/Icons';
import './AuthLayout.css';

/**
 * Shared layout wrapper for Login / Register pages.
 * Renders the left illustration panel and the right form panel.
 */
export default function AuthLayout({ children }) {
  const { pathname } = useLocation();
  const isLogin = pathname === '/login';

  return (
    <div className="auth-page">
      {/* ─── Left Panel ─── */}
      <div className="auth-panel-left">
        <div className="auth-panel-left__logo">
          <LogoIcon size={32} />
          <span>PeerTayo</span>
        </div>

        <div className="auth-panel-left__illustration">
          <div className="auth-illustration-box">
            <TrendUpIcon size={64} />
          </div>
        </div>

        <div className="auth-panel-left__footer">
          <p>Criteria-Based Peer Evaluation System</p>
        </div>
      </div>

      {/* ─── Right Panel ─── */}
      <div className="auth-panel-right">
        <div className="auth-form-wrapper">
          {/* Tabs */}
          <div className="auth-tabs">
            <Link to="/login" className={`auth-tab ${isLogin ? 'auth-tab--active' : ''}`}>
              Sign In
            </Link>
            <Link to="/register" className={`auth-tab ${!isLogin ? 'auth-tab--active' : ''}`}>
              Register
            </Link>
          </div>

          {children}
        </div>
      </div>
    </div>
  );
}
