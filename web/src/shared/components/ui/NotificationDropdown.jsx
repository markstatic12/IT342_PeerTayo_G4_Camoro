import { useEffect, useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { listNotifications } from '../../../features/notification/list/notificationService';
import { markNotificationAsRead } from '../../../features/notification/markread/markReadService';
import Skeleton from './Skeleton';
import { BellIcon, ClockIcon, CheckCircleIcon } from '../icons/Icons';
import './NotificationDropdown.css';

export default function NotificationDropdown({ isOpen, onClose, onCountChange }) {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();

  /* ── Fetch when opened ── */
  useEffect(() => {
    if (isOpen) {
      let mounted = true;
      (async () => {
        setLoading(true);
        try {
          const data = await listNotifications();
          if (mounted) {
            setNotifications(data);
            onCountChange?.(data.filter((n) => !n.isRead).length);
          }
        } catch {
          // silently ignore
        } finally {
          if (mounted) setLoading(false);
        }
      })();
      return () => { mounted = false; };
    }
  }, [isOpen]);

  /* ── Close on outside click ── */
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        onClose();
      }
    };
    if (isOpen) document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [isOpen, onClose]);

  /* ── Mark single notification as read ── */
  const handleMarkRead = useCallback(async (id) => {
    try {
      await markNotificationAsRead(id);
      setNotifications((prev) => {
        const updated = prev.map((n) => (n.id === id ? { ...n, isRead: true } : n));
        onCountChange?.(updated.filter((n) => !n.isRead).length);
        return updated;
      });
    } catch {
      // silently ignore
    }
  }, [onCountChange]);

  /* ── Mark all as read ── */
  const handleMarkAll = useCallback(async () => {
    const unread = notifications.filter((n) => !n.isRead);
    await Promise.allSettled(unread.map((n) => markNotificationAsRead(n.id)));
    setNotifications((prev) => {
      const updated = prev.map((n) => ({ ...n, isRead: true }));
      onCountChange?.(0);
      return updated;
    });
  }, [notifications, onCountChange]);

  /* ── Visit notification ── */
  const handleVisit = useCallback((notif) => {
    if (!notif.isRead) handleMarkRead(notif.id);
    onClose();
    navigate('/pending-evaluations');
  }, [handleMarkRead, navigate, onClose]);

  if (!isOpen) return null;

  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <div className="notification-dropdown" ref={dropdownRef}>
      <div className="nd-header">
        <h3 className="nd-title">
          Notifications
          {unreadCount > 0 && (
            <span className="nd-count">{unreadCount}</span>
          )}
        </h3>
        {unreadCount > 0 && (
          <button className="nd-mark-all" onClick={handleMarkAll}>
            Mark all as read
          </button>
        )}
      </div>

      <div className="nd-list">
        {loading ? (
          [1, 2, 3].map((i) => (
            <div key={i} className="nd-item nd-item--loading">
              <Skeleton variant="circle" width="32px" height="32px" className="skeleton-stagger" />
              <div className="nd-item-content" style={{ flex: 1 }}>
                <Skeleton variant="text" width="85%" height="12px" style={{ marginBottom: 6 }} className="skeleton-stagger" />
                <Skeleton variant="text" width="45%" height="10px" className="skeleton-stagger" />
              </div>
            </div>
          ))
        ) : notifications.length === 0 ? (
          <div className="nd-empty">
            <BellIcon size={28} style={{ opacity: 0.2, marginBottom: 10 }} />
            <p>No notifications yet</p>
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              className={`nd-item${!n.isRead ? ' nd-item--unread' : ''}`}
              onClick={() => handleVisit(n)}
            >
              <div className="nd-icon-wrap">
                {n.isRead
                  ? <CheckCircleIcon size={14} />
                  : <ClockIcon size={14} />
                }
              </div>
              <div className="nd-item-content">
                <div className="nd-message">{n.message}</div>
                <div className="nd-time">
                  {n.createdAt
                    ? new Date(n.createdAt).toLocaleDateString([], {
                        month: 'short', day: 'numeric',
                        hour: '2-digit', minute: '2-digit',
                      })
                    : 'Now'}
                </div>
              </div>
              {!n.isRead && <div className="nd-unread-dot" />}
            </div>
          ))
        )}
      </div>

      <div className="nd-footer">
        <button
          className="nd-view-all"
          onClick={() => { onClose(); navigate('/pending-evaluations'); }}
        >
          View all pending evaluations
        </button>
      </div>
    </div>
  );
}
