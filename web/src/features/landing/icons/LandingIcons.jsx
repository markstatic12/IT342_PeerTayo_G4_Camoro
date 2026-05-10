function SvgIcon({ children, size = 18, stroke = 'currentColor', fill = 'none', strokeWidth = 2 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill={fill}
      stroke={stroke}
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {children}
    </svg>
  );
}

export function IconTarget(props) {
  return (
    <SvgIcon {...props} stroke="none" fill="none">
      <defs>
        <linearGradient id="pt-logo-grad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stopColor="#60a5fa" />
          <stop offset="100%" stopColor="#3b82f6" />
        </linearGradient>
      </defs>
      <rect x="2.3" y="2.3" width="19.4" height="19.4" rx="5.3" fill="#0f2f74" stroke="url(#pt-logo-grad)" strokeWidth="1.2" />
      <rect x="6.3" y="6.3" width="11.4" height="11.4" rx="2.8" fill="none" stroke="#93c5fd" strokeWidth="1.5" />
      <polyline points="8.9 12.2 11.2 14.4 15.3 10.1" fill="none" stroke="#e2e8f0" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </SvgIcon>
  );
}

export function IconArrowRight(props) {
  return (
    <SvgIcon {...props}>
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </SvgIcon>
  );
}

export function IconPlay(props) {
  return (
    <SvgIcon {...props}>
      <polygon points="8,6 18,12 8,18" />
    </SvgIcon>
  );
}

export function IconDocument(props) {
  return (
    <SvgIcon {...props}>
      <path d="M14 2H7a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7z" />
      <polyline points="14 2 14 7 19 7" />
      <line x1="9" y1="12" x2="15" y2="12" />
      <line x1="9" y1="16" x2="15" y2="16" />
    </SvgIcon>
  );
}

export function IconLock(props) {
  return (
    <SvgIcon {...props}>
      <rect x="5" y="11" width="14" height="10" rx="2" />
      <path d="M8 11V8a4 4 0 0 1 8 0v3" />
    </SvgIcon>
  );
}

export function IconChart(props) {
  return (
    <SvgIcon {...props}>
      <line x1="4" y1="20" x2="20" y2="20" />
      <rect x="6" y="11" width="3" height="7" rx="1" />
      <rect x="11" y="8" width="3" height="10" rx="1" />
      <rect x="16" y="5" width="3" height="13" rx="1" />
    </SvgIcon>
  );
}

export function IconClock(props) {
  return (
    <SvgIcon {...props}>
      <circle cx="12" cy="12" r="9" />
      <polyline points="12 7 12 12 16 14" />
    </SvgIcon>
  );
}

export function IconRocket(props) {
  return (
    <SvgIcon {...props}>
      <path d="M15.5 8.5l-7 7" />
      <path d="M12 4c4 0 8 4 8 8-2 2-6 3-8 2-1-2 0-6 2-10z" />
      <path d="M8 12c-2 0-4 2-4 4 2 0 4-2 4-4z" />
    </SvgIcon>
  );
}

export function IconMobile(props) {
  return (
    <SvgIcon {...props}>
      <rect x="8" y="3" width="8" height="18" rx="2" />
      <line x1="11" y1="6" x2="13" y2="6" />
      <circle cx="12" cy="18" r="0.9" fill="currentColor" stroke="none" />
    </SvgIcon>
  );
}

export function IconUser(props) {
  return (
    <SvgIcon {...props}>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
    </SvgIcon>
  );
}

export function IconTools(props) {
  return (
    <SvgIcon {...props}>
      <path d="M6 5l4 4" />
      <path d="M4 7l4-4 2 2-4 4z" />
      <path d="M14 4a5 5 0 0 1 6 6l-3-1-2 2 1 3a5 5 0 0 1-6-6z" />
    </SvgIcon>
  );
}

export function IconCheck(props) {
  return (
    <SvgIcon {...props}>
      <polyline points="5 12 10 17 19 8" />
    </SvgIcon>
  );
}

export function IconShield(props) {
  return (
    <SvgIcon {...props}>
      <path d="M12 3l7 3v5c0 5-3.2 8.6-7 10-3.8-1.4-7-5-7-10V6z" />
    </SvgIcon>
  );
}

export function IconBolt(props) {
  return (
    <SvgIcon {...props}>
      <polygon points="13 2 5 13 11 13 10 22 19 10 13 10" />
    </SvgIcon>
  );
}

export function IconTrend(props) {
  return (
    <SvgIcon {...props}>
      <polyline points="3 17 9 11 13 15 21 7" />
      <polyline points="16 7 21 7 21 12" />
    </SvgIcon>
  );
}

export function IconStar(props) {
  return (
    <SvgIcon {...props}>
      <polygon points="12 2 15.2 8.4 22 9.4 17 14.1 18.2 21 12 17.7 5.8 21 7 14.1 2 9.4 8.8 8.4" />
    </SvgIcon>
  );
}

export function IconQuote(props) {
  return (
    <SvgIcon {...props} strokeWidth={1.6}>
      <path d="M10 8H6c-1 0-2 1-2 2v4h4v4H4" />
      <path d="M20 8h-4c-1 0-2 1-2 2v4h4v4h-4" />
    </SvgIcon>
  );
}

export function IconBell(props) {
  return (
    <SvgIcon {...props}>
      <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.7 21a2 2 0 0 1-3.4 0" />
    </SvgIcon>
  );
}
