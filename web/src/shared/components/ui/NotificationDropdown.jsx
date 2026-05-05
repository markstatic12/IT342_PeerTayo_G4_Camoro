import { useEffect, useState, useRef } from 'react';
import { listNotifications } from '../../../features/notification/list/notificationService';
import { Skeleton } from '../ui';
import { BellIcon, ClockIcon, CheckCircleIcon } from '../icons/Icons';
import './NotificationDropdown.css';

export default function NotificationDropdown({ isOpen, onClose }) {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const dropdownRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      let mounted = true;
      (async () => {
        setLoading(true);
        try {
          const data = await listNotifications();
          if (mounted) setNotifications(data);
        } catch (err) {
          console.error('Failed to fetch notifications', err);
        } finally {
          if (mounted) setLoading(false);
        }
      })();
      return () => { mounted = false; };
    }
  }, [isOpen]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        onClose();
      }
    };
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="notification-dropdown" ref={dropdownRef}>
      <div className="nd-header">
        <h3 className="nd-title">Notifications</h3>
        <button className="nd-mark-all">Mark all as read</button>
      </div>

      <div className="nd-list">
        {loading ? (
          [1, 2, 3].map((i) => (
            <div key={i} className="nd-item nd-item--loading">
              <Skeleton variant="circle" width="32px" height="32px" stagger={true} />
              <div className="nd-item-content">
                <Skeleton variant="text" width="80%" style={{ marginBottom: '4px' }} stagger={true} />
                <Skeleton variant="text" width="40%" height="10px" stagger={true} />
              </div>
            </div>
          ))
        ) : notifications.length === 0 ? (
          <div className="nd-empty">
            <BellIcon size={32} style={{ opacity: 0.2, marginBottom: '12px' }} />
            <p>No new notifications</p>
          </div>
        ) : (
          notifications.map((n) => (
            <div key={n.id} className={`nd-item${!n.isRead ? ' nd-item--unread' : ''}`}>
              <div className="nd-icon-wrap">
                {n.type === 'SUCCESS' ? <CheckCircleIcon size={14} /> : <ClockIcon size={14} />}
              </div>
              <div className="nd-item-content">
                <div className="nd-message">{n.message}</div>
                <div className="nd-time">
                  {new Date(n.createdAt).toLocaleDateString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                </div>
              </div>
              {!n.isRead && <div className="nd-unread-dot" />}
            </div>
          ))
        )}
      </div>

      <div className="nd-footer">
        <button className="nd-view-all">View all activity</button>
      </div>
    </div>
  );
}
