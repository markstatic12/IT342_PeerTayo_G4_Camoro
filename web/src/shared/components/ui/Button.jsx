import './Button.css';

/**
 * Reusable Button component.
 *
 * @param {"primary"|"outline"|"ghost"|"dark"} variant
 * @param {"sm"|"md"|"lg"} size
 * @param {boolean} block   – full-width
 */
export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  block = false,
  className = '',
  ...props
}) {
  const classes = [
    'btn',
    `btn--${variant}`,
    size !== 'md' && `btn--${size}`,
    block && 'btn--block',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <button className={classes} {...props}>
      {children}
    </button>
  );
}
