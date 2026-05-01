import { useState, useMemo } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LogoIcon,
  DashboardIcon,
  EvaluationsIcon,
  ReportsIcon,
  FormsIcon,
  SettingsIcon,
  LogoutIcon,
} from '../components/icons/Icons';
import Toast from '../components/ui/Toast';
import './Sidebar.css';

const mainMenu = [
  { to: '/dashboard', label: 'Dashboard', icon: DashboardIcon },
  { to: '/evaluations', label: 'Evaluations', icon: EvaluationsIcon },
  { to: '/reports', label: 'Reports', icon: ReportsIcon },
];

export default function Sidebar() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();
  const [showConfirm, setShowConfirm] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showToast, setShowToast] = useState(false);

  // Memoize isFacilitator to ensure it updates when user changes
  const isFacilitator = useMemo(() => {
    return user?.roles?.some(
      (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR'
    );
  }, [user]);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    await logout();
    setIsLoggingOut(false);
    setShowConfirm(false);
    setShowToast(true);
    setTimeout(() => navigate('/login', { replace: true }), 1800);
  };

  return (
    <aside className="sidebar">
      {/* Brand */}
      <div className="sidebar__brand">
        <LogoIcon size={28} />
        <span className="sidebar__brand-text">PeerTayo</span>
      </div>

      {/* Navigation */}
      <nav className="sidebar__nav">
        <div className="sidebar__section">
          <span className="sidebar__section-title">Main Menu</span>
          {/* eslint-disable-next-line no-unused-vars */}
          {mainMenu.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/dashboard'}
              className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </div>

        <div className="sidebar__section">
          <span className="sidebar__section-title">Manage</span>
          {isFacilitator && (
            <NavLink
              to="/forms-created"
              className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
            >
              <FormsIcon size={18} />
              Forms Created
            </NavLink>
          )}
        </div>
      </nav>

      {/* Role badge */}
      {isFacilitator && (
        <div className="sidebar__role-badge">
          <span className="sidebar__role-icon">
            <FormsIcon size={13} />
          </span>
          <div>
            <div className="sidebar__role-title">You're a <span>Facilitator.</span></div>
            <div className="sidebar__role-sub">Create &amp; manage evals.</div>
          </div>
        </div>
      )}

      {/* Footer */}
      <div className="sidebar__footer">
        <NavLink to="/settings" className="sidebar__footer-link">
          <SettingsIcon size={18} />
          Settings
        </NavLink>

        {showConfirm ? (
          <div className="sidebar__confirm">
            <p className="sidebar__confirm-text">Sign out of PeerTayo?</p>
            <div className="sidebar__confirm-actions">
              <button
                className="sidebar__confirm-btn sidebar__confirm-btn--cancel"
                onClick={() => setShowConfirm(false)}
                disabled={isLoggingOut}
              >
                Cancel
              </button>
              <button
                className="sidebar__confirm-btn sidebar__confirm-btn--danger"
                onClick={handleLogout}
                disabled={isLoggingOut}
              >
                {isLoggingOut ? 'Signing out…' : 'Yes, sign out'}
              </button>
            </div>
          </div>
        ) : (
          <button className="sidebar__footer-link" onClick={() => setShowConfirm(true)}>
            <LogoutIcon size={18} />
            Sign Out
          </button>
        )}
      </div>

      {showToast && (
        <Toast
          message="You have been signed out successfully."
          onDismiss={() => setShowToast(false)}
          duration={1800}
        />
      )}
    </aside>
  );
}
