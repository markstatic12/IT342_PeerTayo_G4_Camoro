import { useState, useMemo } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/context/AuthContext';
import {
  LogoIcon,
  DashboardIcon,
  PendingIcon,
  MyResultsIcon,
  CompletedFormsIcon,
  FormsIcon,
  SettingsIcon,
  LogoutIcon,
} from '../components/icons/Icons';
import Toast from '../components/ui/Toast';
import './Sidebar.css';

export default function Sidebar() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showToast, setShowToast] = useState(false);

  const isFacilitator = useMemo(() => {
    return user?.roles?.some(
      (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR'
    );
  }, [user]);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    await logout();
    setIsLoggingOut(false);
    setShowModal(false);
    setShowToast(true);
    setTimeout(() => navigate('/login', { replace: true }), 1800);
  };

  return (
    <aside className="sidebar">
      <div className="sidebar__brand">
        <LogoIcon size={28} />
        <span className="sidebar__brand-text">PeerTayo</span>
      </div>

      <nav className="sidebar__nav">
        {/* ── My Activity ── */}
        <div className="sidebar__section">
          <span className="sidebar__section-title">My Activity</span>

          <NavLink
            to="/dashboard"
            end
            className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
          >
            <DashboardIcon size={16} />
            Dashboard
          </NavLink>

          <NavLink
            to="/pending-evaluations"
            className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
          >
            <PendingIcon size={16} />
            Pending Evaluations
            {/* orange dot badge if there are any pending */}
            <span className="sidebar__dot" />
          </NavLink>

          <NavLink
            to="/my-results"
            className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
          >
            <MyResultsIcon size={16} />
            My Results
          </NavLink>

          <NavLink
            to="/completed"
            className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
          >
            <CompletedFormsIcon size={16} />
            My Completed Forms
          </NavLink>
        </div>

        {/* ── Manage (facilitator only) ── */}
        <div className="sidebar__section">
          <span className="sidebar__section-title">Manage</span>
          {isFacilitator && (
            <NavLink
              to="/forms-created"
              className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
            >
              <FormsIcon size={16} />
              Forms Created
            </NavLink>
          )}
          {!isFacilitator && (
            <span className="sidebar__link sidebar__link--disabled" style={{ opacity: 0.4, cursor: 'default', pointerEvents: 'none' }}>
              <FormsIcon size={16} />
              Forms Created
            </span>
          )}
        </div>
      </nav>

      {/* Facilitator badge */}
      {isFacilitator && (
        <div className="sidebar__role-badge">
          <span className="sidebar__role-icon">
            {/* Star icon matching the HTML prototype */}
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
            </svg>
          </span>
          <div>
            <div className="sidebar__role-title">You&apos;re a <span>Facilitator.</span></div>
            <div className="sidebar__role-sub">Create &amp; manage evals.</div>
          </div>
        </div>
      )}

      {/* ── Account ── */}
      <div className="sidebar__footer">
        <span className="sidebar__section-title" style={{ paddingLeft: '0.3rem', marginBottom: '0.5rem', display: 'block' }}>Account</span>
        <NavLink to="/settings" className="sidebar__footer-link">
          <SettingsIcon size={16} />
          Settings
        </NavLink>

        <button className="sidebar__footer-link" onClick={() => setShowModal(true)}>
          <LogoutIcon size={16} />
          Logout
        </button>
      </div>

      {/* Logout modal */}
      {showModal && (
        <div
          className="sidebar__logout-overlay"
          onClick={(e) => e.target === e.currentTarget && !isLoggingOut && setShowModal(false)}
        >
          <div className="sidebar__logout-modal">
            <div className="sidebar__logout-icon">
              <LogoutIcon size={22} />
            </div>
            <div className="sidebar__logout-title">Sign out of PeerTayo?</div>
            <div className="sidebar__logout-body">
              You&apos;ll need to sign back in to access your evaluations and forms.
            </div>
            <div className="sidebar__logout-actions">
              <button
                className="sidebar__logout-btn sidebar__logout-btn--cancel"
                onClick={() => setShowModal(false)}
                disabled={isLoggingOut}
              >
                Cancel
              </button>
              <button
                className="sidebar__logout-btn sidebar__logout-btn--danger"
                onClick={handleLogout}
                disabled={isLoggingOut}
              >
                {isLoggingOut ? 'Signing out…' : 'Yes, sign out'}
              </button>
            </div>
          </div>
        </div>
      )}

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
