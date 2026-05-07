import './ExitConfirmModal.css';

/**
 * Generic "are you sure you want to leave?" modal.
 *
 * Props:
 *   isOpen       — boolean
 *   onConfirm()  — user clicked "Yes, leave"
 *   onCancel()   — user clicked "Stay"
 *   title        — optional heading
 *   body         — optional description
 */
export default function ExitConfirmModal({
  isOpen,
  onConfirm,
  onCancel,
  title = 'Leave without saving?',
  body  = 'Your progress will be lost and cannot be recovered. Are you sure you want to leave?',
}) {
  if (!isOpen) return null;

  return (
    <div
      className="ecm-overlay"
      onClick={(e) => e.target === e.currentTarget && onCancel()}
    >
      <div className="ecm-modal">
        <div className="ecm-icon">
          <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none"
            width="26" height="26">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
        </div>
        <div className="ecm-title">{title}</div>
        <div className="ecm-body">{body}</div>
        <div className="ecm-actions">
          <button className="ecm-btn ecm-btn--cancel" type="button" onClick={onCancel}>
            Stay on page
          </button>
          <button className="ecm-btn ecm-btn--leave" type="button" onClick={onConfirm}>
            Yes, leave
          </button>
        </div>
      </div>
    </div>
  );
}
