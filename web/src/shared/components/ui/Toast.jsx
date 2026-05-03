import { useState, useEffect } from 'react';
import { CheckCircleIcon, XIcon } from '../icons/Icons';
import './Toast.css';

/**
 * Auto-dismissing success toast notification.
 *
 * @param {string}   message   - Text to display
 * @param {function} onDismiss - Called when dismissed
 * @param {number}   duration  - Auto-dismiss ms (default 4000)
 */
export default function Toast({ message, onDismiss, duration = 4000 }) {
  const [leaving, setLeaving] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setLeaving(true);
      setTimeout(onDismiss, 300);
    }, duration);
    return () => clearTimeout(timer);
  }, [duration, onDismiss]);

  const handleClose = () => {
    setLeaving(true);
    setTimeout(onDismiss, 300);
  };

  return (
    <div className={`toast${leaving ? ' toast--leaving' : ''}`}>
      <span className="toast__icon">
        <CheckCircleIcon size={20} />
      </span>
      <span className="toast__message">{message}</span>
      <button className="toast__close" onClick={handleClose} aria-label="Dismiss">
        <XIcon size={16} />
      </button>
    </div>
  );
}
