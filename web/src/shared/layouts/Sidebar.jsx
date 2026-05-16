import { useState, useMemo, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../features/auth/context/AuthContext';
import { useNavigationGuard } from '../context/NavigationGuardContext';
import { listPendingEvaluations } from '../../features/evaluation/submission/evaluationSubmissionService';
import { listCreatedEvaluations } from '../../features/evaluation/form/evaluationFormService';
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
  const location = useLocation();
  const { guardedNavigate } = useNavigationGuard();
  const [showModal, setShowModal] = useState(false);

  const isActive = (path, exact = false) =>
    exact ? location.pathname === path : location.pathname.startsWith(path);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [pendingCount, setPendingCount] = useState(0);
  const [facilitatorNoticeCount, setFacilitatorNoticeCount] = useState(0);

  const isFacilitator = useMemo(() => {
    return user?.roles?.some(
      (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR'
    );
  }, [user]);

  /* fetch counts on mount and whenever user changes */
  useEffect(() => {
    if (!user) return;
    let alive = true;

    // 1. Pending evaluations for Respondent
    listPendingEvaluations()
      .then((data) => { if (alive) setPendingCount(data?.length ?? 0); })
      .catch(() => {});

    // 2. Forms needing attention for Facilitator
    if (isFacilitator) {
      listCreatedEvaluations()
        .then((data) => {
          if (!alive) return;
          const noticeCount = data.filter(ev => {
            const status = ev.status?.toUpperCase();
            const isOverdue = ev.deadline && new Date(ev.deadline) < new Date();
            
            // Needs attention if:
            // - Status is NEEDS_ATTENTION
            // - Status is ACTIVE but deadline passed
            // - Status is CLOSED but 0 submissions and not permanently closed (needs extension/closure)
            return (status === 'NEEDS_ATTENTION') ||
                   (status === 'ACTIVE' && isOverdue) ||
                   (status === 'CLOSED' && ev.submissionCount === 0 && !ev.permanentlyClosed);
          }).length;
          setFacilitatorNoticeCount(noticeCount);
        })
        .catch(() => {});
    }

    return () => { alive = false; };
  }, [user?.id, isFacilitator]);

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

          <button
            onClick={() => guardedNavigate('/dashboard')}
            className={`sidebar__link${isActive('/dashboard', true) ? ' active' : ''}`}
          >
            <DashboardIcon size={16} />
            Dashboard
          </button>

          <button
            onClick={() => guardedNavigate('/pending-evaluations')}
            className={`sidebar__link${isActive('/pending-evaluations') ? ' active' : ''}`}
          >
            <PendingIcon size={16} />
            Pending Evaluations
            {pendingCount > 0 && <span className="sidebar__dot" />}
          </button>

          <button
            onClick={() => guardedNavigate('/my-results')}
            className={`sidebar__link${isActive('/my-results') ? ' active' : ''}`}
          >
            <MyResultsIcon size={16} />
            My Results
          </button>

          <button
            onClick={() => guardedNavigate('/completed')}
            className={`sidebar__link${isActive('/completed') ? ' active' : ''}`}
          >
            <CompletedFormsIcon size={16} />
            My Completed Forms
          </button>
        </div>

        {/* ── Manage (facilitator only) ── */}
        <div className="sidebar__section">
          <span className="sidebar__section-title">Manage</span>
          {isFacilitator && (
            <button
              onClick={() => guardedNavigate('/forms-created')}
              className={`sidebar__link${isActive('/forms-created') ? ' active' : ''}`}
            >
              <FormsIcon size={16} />
              Forms Created
              {facilitatorNoticeCount > 0 && <span className="sidebar__dot" />}
            </button>
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
      <div className="sidebar__section" style={{ marginTop: 'auto', paddingTop: 8, borderTop: '1px solid rgba(255,255,255,0.07)' }}>
        <span className="sidebar__section-title">Account</span>

        <button
          type="button"
          onClick={() => guardedNavigate('/settings')}
          className={`sidebar__link${isActive('/settings') ? ' active' : ''}`}
        >
          <SettingsIcon size={16} />
          Settings
        </button>

        <button type="button" className="sidebar__link" onClick={() => setShowModal(true)}>
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
