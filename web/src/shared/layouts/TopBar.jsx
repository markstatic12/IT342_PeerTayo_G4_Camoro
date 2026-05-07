import { useState, useCallback, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../features/auth/context/AuthContext';
import { getUnreadCount } from '../../features/notification/list/notificationService';
import NotificationDropdown from '../components/ui/NotificationDropdown';
import './TopBar.css';

/* ── Filled blue bell SVG ─────────────────────────────────────────────── */
function BellFilled({ size = 20, hasUnread = false }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      className={hasUnread ? 'bell-ring' : ''}
    >
      {/* Bell body — filled blue, no separate stroke */}
      <path
        d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"
        fill="#3b82f6"
      />
      {/* Clapper — same blue so it blends as one shape */}
      <path
        d="M13.73 21a2 2 0 0 1-3.46 0"
        stroke="#3b82f6"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        fill="none"
      />
    </svg>
  );
}

export default function TopBar() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [notifOpen, setNotifOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const bellRef = useRef(null);

  /* ── Poll unread count every 30 s ── */
  useEffect(() => {
    let mounted = true;
    const fetchCount = async () => {
      try {
        const count = await getUnreadCount();
        if (mounted) setUnreadCount(count);
      } catch { /* silently ignore */ }
    };
    fetchCount();
    /* re-check every 15 s so new assignments show up without a page refresh */
    const id = setInterval(fetchCount, 15_000);
    return () => { mounted = false; clearInterval(id); };
  }, []);

  const initials =
    (user?.firstName?.charAt(0) || '') + (user?.lastName?.charAt(0) || '');

  const roles = user?.roles ?? [];
  const isFacilitator = roles.some(
    (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR'
  );
  const isRespondent = roles.some(
    (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'RESPONDENT'
  );
  // Build role label as JSX so each role gets its own colour
  const roleLabel = isFacilitator && isRespondent ? (
    <>
      <span style={{ color: '#3b82f6' }}>Respondent</span>
      <span style={{ color: '#64748b' }}> · </span>
      <span style={{ color: '#fb923c' }}>Facilitator</span>
    </>
  ) : isFacilitator ? (
    <span style={{ color: '#fb923c' }}>Facilitator</span>
  ) : (
    <span style={{ color: '#3b82f6' }}>Respondent</span>
  );

  const handleClose = useCallback(() => setNotifOpen(false), []);

  /* When dropdown closes after marking all read, reset count */
  const handleDropdownClose = useCallback(() => {
    setNotifOpen(false);
    // Re-fetch count after a short delay so mark-as-read has settled
    setTimeout(async () => {
      try {
        const count = await getUnreadCount();
        setUnreadCount(count);
      } catch { /* ignore */ }
    }, 400);
  }, []);

  return (
    <header className="topbar">
      <div className="topbar__right" style={{ marginLeft: 'auto' }}>
        {/* Bell notification button */}
        <div className="topbar__notif-wrap" ref={bellRef}>
          <button
            className={`topbar__icon-btn topbar__bell-btn${unreadCount > 0 ? ' topbar__bell-btn--active' : ''}`}
            title="Notifications"
            onClick={() => setNotifOpen((o) => !o)}
          >
            <BellFilled size={20} hasUnread={unreadCount > 0} />
            {unreadCount > 0 && (
              <span className="topbar__bell-badge">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>
          <NotificationDropdown
            isOpen={notifOpen}
            onClose={handleDropdownClose}
            onCountChange={setUnreadCount}
          />
        </div>

        <div className="topbar__profile">
          <div className="topbar__user-info">
            <span className="topbar__user-name">
              {user?.firstName} {user?.lastName}
            </span>
            <span className="topbar__user-role">{roleLabel}</span>
          </div>
          <div className="topbar__avatar">{initials}</div>
        </div>
      </div>
    </header>
  );
}
