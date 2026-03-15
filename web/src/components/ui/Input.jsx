import './Input.css';

/**
 * Reusable labelled text input.
 */
export default function Input({
  label,
  id,
  error = false,
  className = '',
  ...props
}) {
  return (
    <div className={`form-field ${className}`}>
      {label && (
        <label className="form-field__label" htmlFor={id}>
          {label}
        </label>
      )}
      <input
        id={id}
        className={`form-field__input${error ? ' form-field__input--error' : ''}`}
        {...props}
      />
    </div>
  );
}
