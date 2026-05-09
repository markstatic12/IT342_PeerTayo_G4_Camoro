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
  const [activeTab, setActiveTab] = useState('unread');
  const [clearedItems, setClearedItems] = useState([]);
  const [showUndo, setShowUndo] = useState(false);
  const undoTimerRef = useRef(null);
  const prevIdsRef = useRef(new Set());
  const [newIds, setNewIds] = useState([]);
  const dropdownRef = useRef(null);
  const tabsContainerRef = useRef(null);
  const tabsRef = useRef({});
  const [indicatorStyle, setIndicatorStyle] = useState({ left: 0, width: 0 });
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
            // defer count update to avoid setState-during-render warning
            setTimeout(() => onCountChange?.(data.filter((n) => !n.isRead).length), 0);
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

  // Detect newly arrived notifications (client-side) and mark temporarily
  useEffect(() => {
    const prev = prevIdsRef.current;
    const curr = new Set(notifications.map((n) => n.id));
    const added = notifications.filter((n) => !prev.has(n.id)).map((n) => n.id);
    if (added.length > 0) {
      setNewIds(added);
      // clear highlight after 3s
      const t = setTimeout(() => setNewIds([]), 3000);
      return () => clearTimeout(t);
    }
    prevIdsRef.current = curr;
    return undefined;
  }, [notifications]);

  // Position the sliding underline indicator under the active tab
  useEffect(() => {
    const setPos = () => {
      const container = tabsContainerRef.current;
      const activeEl = tabsRef.current[activeTab];
      if (!container || !activeEl) return;
      const cRect = container.getBoundingClientRect();
      const aRect = activeEl.getBoundingClientRect();
      setIndicatorStyle({ left: aRect.left - cRect.left, width: aRect.width });
    };
    const id = requestAnimationFrame(setPos);
    window.addEventListener('resize', setPos);
    return () => { cancelAnimationFrame(id); window.removeEventListener('resize', setPos); };
  }, [activeTab, notifications]);

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

  const handleClearRead = () => {
    const read = notifications.filter((n) => n.isRead);
    if (read.length === 0) return;
    setClearedItems(read);
    setNotifications((prev) => prev.filter((n) => !n.isRead));
    onCountChange?.(notifications.filter((n) => !n.isRead).length);
    setShowUndo(true);
    if (undoTimerRef.current) clearTimeout(undoTimerRef.current);
    undoTimerRef.current = setTimeout(() => {
      setShowUndo(false);
      setClearedItems([]);
    }, 5000);
  };

  const handleUndoClear = () => {
    if (undoTimerRef.current) clearTimeout(undoTimerRef.current);
    setNotifications((prev) => [...clearedItems, ...prev]);
    setClearedItems([]);
    setShowUndo(false);
    onCountChange?.(notifications.filter((n) => !n.isRead).length);
  };

  if (!isOpen) return null;

  const unreadCount = notifications.filter((n) => !n.isRead).length;
  const displayed = notifications.filter((n) => (activeTab === 'unread' ? !n.isRead : n.isRead));

  return (
    <div className="notification-dropdown" ref={dropdownRef}>
      <div className="nd-header">
        <div className="nd-title-row">
          <h3 className="nd-title">Notifications</h3>
          {unreadCount > 0 && <span className="nd-count">{unreadCount}</span>}
        </div>
        <div className="nd-header-actions">
          {activeTab === 'unread' && unreadCount > 0 && (
            <button className="nd-mark-all" onClick={handleMarkAll}>
              Mark all as read
            </button>
          )}
          {activeTab === 'read' && (
            <button className="nd-clear-read" onClick={handleClearRead}>
              Clear
            </button>
          )}
        </div>
      </div>

      <div className="nd-tab-row">
        <div className="nd-tabs" ref={tabsContainerRef}>
          <button
            ref={(el) => (tabsRef.current['unread'] = el)}
            type="button"
            className={`nd-tab${activeTab === 'unread' ? ' active' : ''}`}
            onClick={() => setActiveTab('unread')}
          >
            Unread
          </button>
          <button
            ref={(el) => (tabsRef.current['read'] = el)}
            type="button"
            className={`nd-tab${activeTab === 'read' ? ' active' : ''}`}
            onClick={() => setActiveTab('read')}
          >
            Read
          </button>
          <div
            className="nd-tab-indicator"
            style={{ left: indicatorStyle.left, width: indicatorStyle.width }}
          />
        </div>
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
        ) : displayed.length === 0 ? (
          <div className="nd-empty">
            <BellIcon size={28} style={{ opacity: 0.2, marginBottom: 10 }} />
            <p>{activeTab === 'unread' ? 'No unread notifications' : 'No read notifications'}</p>
          </div>
        ) : (
          displayed.map((n) => (
            <div
              key={n.id}
              className={`nd-item${!n.isRead ? ' nd-item--unread' : ''}${newIds.includes(n.id) ? ' nd-item--new' : ''}`}
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
      {showUndo && (
        <div className="nd-undo">
          <div style={{ color: '#cbd5e1' }}>{`Cleared ${clearedItems.length} notification${clearedItems.length !== 1 ? 's' : ''}`}</div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button onClick={handleUndoClear}>Undo</button>
          </div>
        </div>
      )}
    </div>
  );
}
