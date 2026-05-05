import LoadingSpinner from './LoadingSpinner';
import './LoadingOverlay.css';

/**
 * A full-screen or container-relative loading overlay
 * @param {boolean} visible - Whether the overlay is shown
 * @param {string} message - Optional message to display
 * @param {boolean} fullScreen - Whether it should cover the whole screen
 */
export default function LoadingOverlay({ 
  visible, 
  message = 'Processing...', 
  fullScreen = true 
}) {
  if (!visible) return null;

  return (
    <div className={`loading-overlay ${fullScreen ? 'full-screen' : ''}`}>
      <div className="loading-overlay__content">
        <LoadingSpinner size="lg" label={message} color="var(--color-primary)" />
      </div>
    </div>
  );
}
