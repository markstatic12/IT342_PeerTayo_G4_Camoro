/**
 * A standard loading spinner component
 * @param {string} size - 'sm', 'md', 'lg' or custom size in px
 * @param {string} color - Custom color for the spinner
 * @param {string} className - Additional CSS classes
 */
export default function LoadingSpinner({ 
  size = 'md', 
  color, 
  className = '',
  label = 'Loading...'
}) {
  const sizeMap = {
    sm: '16px',
    md: '24px',
    lg: '48px'
  };

  const actualSize = sizeMap[size] || size;

  return (
    <div className={`spinner-container ${className}`} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px' }}>
      <div 
        className="loading-spinner" 
        style={{ 
          width: actualSize, 
          height: actualSize,
          borderColor: color ? `${color}33` : undefined,
          borderTopColor: color || undefined
        }} 
      />
      {label && <span style={{ fontSize: '0.875rem', color: 'var(--color-text-muted)' }}>{label}</span>}
    </div>
  );
}
