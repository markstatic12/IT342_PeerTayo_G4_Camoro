import './Skeleton.css';

/**
 * Skeleton component for loading states
 * @param {string} className - Additional CSS classes
 * @param {string} variant - 'text', 'title', 'circle', or 'rect' (default)
 * @param {string|number} width - Custom width
 * @param {string|number} height - Custom height
 */
export default function Skeleton({ 
  variant = 'text', 
  width, 
  height, 
  style, 
  className = '',
  stagger = false
}) {
  const classes = [
    'skeleton',
    `skeleton-${variant}`,
    stagger ? 'skeleton-stagger' : '',
    className
  ].filter(Boolean).join(' ');
  
  const customStyle = {
    ...style,
    width: width,
    height: height
  };

  return <div className={classes} style={customStyle} />;
}
