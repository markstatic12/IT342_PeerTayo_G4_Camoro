import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/context/AuthContext';
import { listCreatedEvaluations } from '../evaluation/form/evaluationFormService';
import { listPendingEvaluations } from '../evaluation/submission/evaluationSubmissionService';
import { getMyResults } from '../evaluation/results/evaluationResultsService';
import { listNotifications } from '../notification/list/notificationService';
import './DashboardPage.css';

/* ── Carousel slides ──────────────────────────────────────────────────── */
const slides = [
  {
    eye: 'Feature Awareness',
    heading: 'Better Together',
    body: 'Did you know you can share your live form links instantly? Get your team involved and start collecting responses today.',
  },
  {
    eye: 'Productivity',
    heading: 'Stay Organized',
    body: 'Managing multiple forms is easy with our automated tracking. Keep your workflow smooth by keeping your data in one place.',
  },
  {
    eye: 'Insights',
    heading: 'Real-time Results',
    body: 'Your dashboard updates the moment a user submits a response. Experience seamless data collection with every new setup.',
  },
];

/* ── Criteria metadata ────────────────────────────────────────────────── */
const criteriaMetaById = {
  1:  { label: 'Quality of Work',          short: 'Quality',  color: '#3b82f6' },
  2:  { label: 'Reliability & Dependability', short: 'Reliab.',  color: '#a78bfa' },
  3:  { label: 'Collaboration & Teamwork', short: 'Collab.',  color: '#22c55e' },
  4:  { label: 'Communication Skills',     short: 'Comm.',    color: '#3b82f6' },
  5:  { label: 'Initiative & Proactiveness', short: 'Init.',  color: '#f97316' },
  6:  { label: 'Problem Solving',          short: 'Problem',  color: '#3b82f6' },
  7:  { label: 'Professionalism & Conduct', short: 'Prof.',   color: '#eab308' },
  8:  { label: 'Time Management',          short: 'Time',     color: '#a78bfa' },
  9:  { label: 'Adaptability & Learning',  short: 'Adapt.',   color: '#ef4444' },
  10: { label: 'Overall Contribution',     short: 'Contrib.', color: '#22c55e' },
};

/* ── Inline SVG icons for stat cards ─────────────────────────────────── */
function IconStar({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
    </svg>
  );
}
function IconClock({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" />
      <polyline points="12 6 12 12 16 14" />
    </svg>
  );
}
function IconCheck({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
      <polyline points="22 4 12 14.01 9 11.01" />
    </svg>
  );
}
function IconFile({ size = 16 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
      <polyline points="14 2 14 8 20 8" />
      <line x1="16" y1="13" x2="8" y2="13" />
      <line x1="16" y1="17" x2="8" y2="17" />
    </svg>
  );
}
function IconTrendUp({ size = 14 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="23 6 13.5 15.5 8.5 10.5 1 18" />
      <polyline points="17 6 23 6 23 12" />
    </svg>
  );
}
function IconArrowRight({ size = 13 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="5" y1="12" x2="19" y2="12" />
      <polyline points="12 5 19 12 12 19" />
    </svg>
  );
}

/* ── Helpers ──────────────────────────────────────────────────────────── */
function formatDateTime(isoString) {
  const date = new Date(isoString);
  return date.toLocaleString([], {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: 'numeric', minute: '2-digit',
  });
}

function formatDateLabel(date) {
  return date.toLocaleDateString([], {
    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric',
  });
}

/* ── Canvas line chart ────────────────────────────────────────────────── */
function drawLineChart(canvas, criteriaData) {
  if (!canvas) return;
  const area = canvas.parentElement;
  if (!area) return;
  const W = area.clientWidth;
  const H = area.clientHeight;
  if (!W || !H) return;

  const ctx = canvas.getContext('2d');
  const ratio = window.devicePixelRatio || 1;
  canvas.width = W * ratio;
  canvas.height = H * ratio;
  ctx.setTransform(ratio, 0, 0, ratio, 0, 0);
  ctx.clearRect(0, 0, W, H);

  const PAD_L = 32, PAD_R = 12, PAD_T = 14, PAD_B = 8;
  const gW = W - PAD_L - PAD_R;
  const gH = H - PAD_T - PAD_B;
  const n = criteriaData.length;
  if (n < 2) return;

  const scores = criteriaData.map((d) => d.cur);
  const yOf = (v) => PAD_T + gH - ((v - 1) / 4) * gH;
  const xOf = (i) => PAD_L + (i / (n - 1)) * gW;

  ctx.font = '8px Inter, system-ui, sans-serif';
  ctx.textAlign = 'right';
  for (let v = 1; v <= 5; v++) {
    const y = yOf(v);
    ctx.beginPath();
    ctx.moveTo(PAD_L, y);
    ctx.lineTo(PAD_L + gW, y);
    ctx.strokeStyle = v === 5 ? 'rgba(255,255,255,0.06)' : 'rgba(255,255,255,0.04)';
    ctx.lineWidth = 1;
    ctx.setLineDash(v === 4 ? [3, 3] : []);
    ctx.stroke();
    ctx.setLineDash([]);
    ctx.fillStyle = 'rgba(139,153,181,0.7)';
    ctx.fillText(v.toFixed(0), PAD_L - 5, y + 3);
  }

  const pts = criteriaData.map((_, i) => ({ x: xOf(i), y: yOf(scores[i]) }));

  function drawCurve() {
    ctx.beginPath();
    ctx.moveTo(pts[0].x, pts[0].y);
    for (let i = 0; i < pts.length - 1; i++) {
      const cpx = (pts[i + 1].x - pts[i].x) * 0.45;
      ctx.bezierCurveTo(pts[i].x + cpx, pts[i].y, pts[i + 1].x - cpx, pts[i + 1].y, pts[i + 1].x, pts[i + 1].y);
    }
  }

  drawCurve();
  ctx.lineTo(pts[pts.length - 1].x, PAD_T + gH);
  ctx.lineTo(pts[0].x, PAD_T + gH);
  ctx.closePath();
  const grad = ctx.createLinearGradient(0, PAD_T, 0, PAD_T + gH);
  grad.addColorStop(0, 'rgba(59,130,246,0.22)');
  grad.addColorStop(0.6, 'rgba(59,130,246,0.06)');
  grad.addColorStop(1, 'rgba(59,130,246,0)');
  ctx.fillStyle = grad;
  ctx.fill();

  ctx.save();
  drawCurve();
  ctx.strokeStyle = '#3b82f6';
  ctx.lineWidth = 2.2;
  ctx.lineJoin = 'round';
  ctx.shadowColor = '#3b82f6';
  ctx.shadowBlur = 8;
  ctx.stroke();
  ctx.restore();

  const highest = Math.max(...scores);
  const lowest = Math.min(...scores);
  pts.forEach((pt, i) => {
    const v = scores[i];
    const isHigh = v === highest, isLow = v === lowest;
    const dotColor = isHigh ? '#3b82f6' : isLow ? '#ef4444' : '#3b82f6';
    const dotR = isHigh || isLow ? 4.5 : 3.5;

    ctx.beginPath();
    ctx.arc(pt.x, pt.y, dotR + 3, 0, Math.PI * 2);
    ctx.fillStyle = isHigh ? 'rgba(59,130,246,0.18)' : isLow ? 'rgba(239,68,68,0.18)' : 'rgba(59,130,246,0.1)';
    ctx.fill();

    ctx.beginPath();
    ctx.arc(pt.x, pt.y, dotR, 0, Math.PI * 2);
    ctx.fillStyle = dotColor;
    ctx.shadowColor = dotColor;
    ctx.shadowBlur = 6;
    ctx.fill();
    ctx.shadowBlur = 0;

    ctx.beginPath();
    ctx.arc(pt.x, pt.y, dotR, 0, Math.PI * 2);
    ctx.strokeStyle = 'rgba(12,15,24,0.9)';
    ctx.lineWidth = 1;
    ctx.stroke();

    if (isHigh || isLow) {
      ctx.font = '700 8.5px Inter, system-ui, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillStyle = isHigh ? '#3b82f6' : '#ef4444';
      ctx.fillText(v.toFixed(1), pt.x, isHigh ? pt.y - dotR - 5 : pt.y + dotR + 11);
    }
  });
}

/* ══════════════════════════════════════════════════════════════════════ */
export default function DashboardPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, promoteToFacilitator } = useAuth();
  const [createdEvaluations, setCreatedEvaluations] = useState([]);
  const [pendingEvaluations, setPendingEvaluations] = useState([]);
  const [myResults, setMyResults] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [slideIndex, setSlideIndex] = useState(0);
  const chartRef = useRef(null);
  const [showUpgradeModal, setShowUpgradeModal] = useState(
    location.state?.upgradeRequired === 'FACILITATOR'
  );
  const [promoting, setPromoting] = useState(false);
  const [promoteError, setPromoteError] = useState('');

  /* ── Derived chart data ─────────────────────────────────────────────── */
  const chartData = useMemo(() => {
    const averages = myResults?.questionAverages;
    if (!Array.isArray(averages) || averages.length === 0) return [];
    return averages.map((item) => {
      const meta = criteriaMetaById[item.criteriaId] || {
        label: `Criteria ${item.criteriaId}`, short: `C${item.criteriaId}`, color: '#3b82f6',
      };
      return { ...meta, cur: Number(item.average ?? 0) };
    });
  }, [myResults]);

  /* ── Data loading ───────────────────────────────────────────────────── */
  useEffect(() => {
    let mounted = true;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const promises = [
          listPendingEvaluations(),
          getMyResults(),
          listNotifications(),
        ];
        // Only fetch created evaluations for facilitators
        const isFac = user?.roles?.some(
          (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR'
        );
        if (isFac) promises.unshift(listCreatedEvaluations());
        else promises.unshift(Promise.resolve([]));

        const [created, pending, results, notices] = await Promise.all(promises);
        if (mounted) {
          setCreatedEvaluations(created);
          setPendingEvaluations(pending);
          setMyResults(results ?? null);
          setNotifications(notices);
        }
      } catch {
        if (mounted) setError('Unable to load dashboard data right now.');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, [user?.id, user?.roles]);

  /* ── Carousel auto-advance ──────────────────────────────────────────── */
  useEffect(() => {
    const id = window.setInterval(() => setSlideIndex((c) => (c + 1) % slides.length), 4200);
    return () => window.clearInterval(id);
  }, []);

  /* ── Chart draw / resize ────────────────────────────────────────────── */
  useEffect(() => {
    if (chartRef.current) drawLineChart(chartRef.current, chartData);
  }, [chartData]);

  useEffect(() => {
    const fn = () => { if (chartRef.current) drawLineChart(chartRef.current, chartData); };
    window.addEventListener('resize', fn);
    fn();
    return () => window.removeEventListener('resize', fn);
  }, [chartData]);

  /* ── Computed values ────────────────────────────────────────────────── */
  const now = new Date();
  const todayLabel = formatDateLabel(now);

  const pendingDue = pendingEvaluations.filter((ev) => {
    const dl = ev.deadline ? new Date(ev.deadline) : null;
    if (!dl) return false;
    const diff = (dl.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
    return diff >= 0 && diff <= 2;
  }).length;

  const closedCount = createdEvaluations.reduce((sum, item) => {
    const [submitted] = (item.submissionProgress || '0/0').split('/').map(Number);
    return sum + (Number.isFinite(submitted) ? submitted : 0);
  }, 0);

  const overallScore = useMemo(() => {
    if (typeof myResults?.overallAverage === 'number') return myResults.overallAverage;
    if (chartData.length === 0) return 0;
    return chartData.reduce((s, d) => s + d.cur, 0) / chartData.length;
  }, [chartData, myResults]);

  const highestCriterion = useMemo(() => {
    if (!chartData.length) return null;
    return chartData.reduce((b, d) => (d.cur > b.cur ? d : b), chartData[0]);
  }, [chartData]);

  const lowestCriterion = useMemo(() => {
    if (!chartData.length) return null;
    return chartData.reduce((w, d) => (d.cur < w.cur ? d : w), chartData[0]);
  }, [chartData]);

  const isFacilitator = useMemo(() => {
    return user?.roles?.some(
      (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR'
    );
  }, [user]);

  /* ── Stat cards config ──────────────────────────────────────────────── */
  const allStats = [
    {
      label: 'Overall Avg Score',
      value: overallScore.toFixed(1),
      variant: 'blue',
      icon: <IconStar size={15} />,
      delta: overallScore > 0 ? '↑ +0.5 from last eval' : null,
      deltaClass: 'd-up',
      action: 'View My Results',
      route: '/my-results',
      showAlways: true,
    },
    {
      label: 'Pending Evaluations',
      value: pendingEvaluations.length,
      variant: 'orange',
      icon: <IconClock size={15} />,
      delta: pendingDue > 0 ? `⚠ ${pendingDue} due in 2 days` : null,
      deltaClass: 'd-warn',
      action: 'View Pending',
      route: '/evaluations',
      showAlways: true,
    },
    {
      label: 'Evaluations Submitted',
      value: closedCount,
      variant: 'green',
      icon: <IconCheck size={15} />,
      delta: closedCount > 0 ? '↑ 100% completion rate' : null,
      deltaClass: 'd-up',
      action: 'My Completed Forms',
      route: '/completed',
      showAlways: true,
    },
    {
      label: 'Forms Created',
      value: createdEvaluations.length,
      variant: 'warn',
      icon: <IconFile size={15} />,
      delta: createdEvaluations.length > 0 ? `↑ ${createdEvaluations.filter(e => e.status !== 'CLOSED').length} still active` : null,
      deltaClass: 'd-up',
      action: 'Manage Forms',
      route: '/forms-created',
      showAlways: false, // facilitator only
    },
  ];

  const stats = allStats.filter((s) => s.showAlways || isFacilitator);

  /* ── Activity items ─────────────────────────────────────────────────── */
  const dotColors = ['dot-green', 'dot-blue', 'dot-orange', 'dot-purple', 'dot-red'];
  const tagMap = { Done: 'done', New: 'new', Pending: 'pending', Updated: 'update', Assigned: 'update', Score: 'done', Closed: 'late' };

  const activityItems = notifications.length
    ? notifications.slice(0, 7).map((item, i) => ({
        color: dotColors[i % 5],
        title: item.message,
        message: '',
        time: item.createdAt ? formatDateTime(item.createdAt) : 'Now',
        tag: item.isRead ? 'Done' : 'New',
      }))
    : pendingEvaluations.length
    ? pendingEvaluations.slice(0, 7).map((ev, i) => ({
        color: dotColors[i % 5],
        title: `Pending: ${ev.title}`,
        message: ev.evaluateeName ? `Evaluatee: ${ev.evaluateeName}` : '',
        time: ev.deadline ? formatDateTime(ev.deadline) : 'No deadline',
        tag: 'Pending',
      }))
    : [];

  const activeSlide = slides[slideIndex];

  const handlePromote = async () => {
    setPromoting(true);
    setPromoteError('');
    const result = await promoteToFacilitator();
    setPromoting(false);
    if (result.success) {
      setShowUpgradeModal(false);
      navigate('/forms-created/new');
    } else {
      setPromoteError(result.message);
    }
  };

  /* ════════════════════════════════════════════════════════════════════ */
  return (
    <div className="db-page">
      <div className="db-shell">

        {/* ── LEFT COLUMN ─────────────────────────────────────────────── */}
        <div className="db-left">

          {/* Greeting banner */}
          <div className="gh-banner">
            <div className="gh-left">
              <div className="gh-hello">
                Hello, <span>{user?.firstName ?? 'there'}</span> 👋
              </div>
              <div className="gh-date">{todayLabel}</div>
              <div className="gh-alert">
                <span className="gh-alert-dot" />
                {pendingDue > 0
                  ? `${pendingDue} evaluation${pendingDue !== 1 ? 's' : ''} due in 2 days`
                  : 'No upcoming deadlines'}
              </div>
            </div>
            <div className="gh-carousel">
              <div className="ghc-content">
                <div className="ghc-eye">{activeSlide.eye}</div>
                <div className="ghc-heading">{activeSlide.heading}</div>
                <div className="ghc-body">{activeSlide.body}</div>
              </div>
              <div className="ghc-right">
                <button
                  className="ghc-btn"
                  type="button"
                  onClick={() => isFacilitator ? navigate('/forms-created/new') : setShowUpgradeModal(true)}
                >
                  {isFacilitator ? 'Create Evaluation →' : 'Create Now →'}
                </button>
                <div className="ghc-dots">
                  {slides.map((_, idx) => (
                    <button
                      key={idx}
                      type="button"
                      className={`cdot${idx === slideIndex ? ' active' : ''}`}
                      onClick={() => setSlideIndex(idx)}
                    />
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Stat cards */}
          <div className="stat-row">
            {stats.map((s) => (
              <div key={s.label} className={`sc c-${s.variant}`}>
                <div className={`sc-icon-wrap ic-${s.variant}`}>{s.icon}</div>
                <div className="sc-val">{s.value}</div>
                <div className="sc-label">{s.label}</div>
                {s.delta && (
                  <div className={`sc-delta ${s.deltaClass}`}>
                    <IconTrendUp size={10} />
                    {s.delta}
                  </div>
                )}
                <button className="sc-action" type="button" onClick={() => navigate(s.route)}>
                  {s.action}
                  <IconArrowRight size={11} />
                </button>
              </div>
            ))}
          </div>

          {/* Analytics card */}
          <div className="analytics-card">
            <div className="ac-head">
              <div>
                <div className="ac-title">My Performance Analytics</div>
                <div className="ac-sub">
                  Per-criterion avg · {myResults?.totalResponses ?? 0} peer responses · rated 1–5
                </div>
              </div>
              {chartData.length > 0 && overallScore >= 4.0 && (
                <span className="ac-badge">✓ Above 4.0 goal</span>
              )}
            </div>
            <div className="ac-body">
              <div className="ac-chart-col">
                <div className="lg-wrap">
                  {chartData.length > 0 ? (
                    <canvas ref={chartRef} />
                  ) : (
                    <div className="ac-empty">No performance data yet.</div>
                  )}
                </div>
                {chartData.length > 0 && (
                  <div className="lg-x-labels">
                    {chartData.map((d) => (
                      <div key={d.label} className="lg-x-label">{d.short}</div>
                    ))}
                  </div>
                )}
              </div>
              <div className="ac-indicators">
                <div className="ac-ind-box high">
                  <div className="ac-ind-label">▲ Highest</div>
                  <div className="ac-ind-val">
                    {chartData.length > 0 ? Math.max(...chartData.map((d) => d.cur)).toFixed(1) : '—'}
                  </div>
                  <div className="ac-ind-crit">{highestCriterion?.label ?? '—'}</div>
                </div>
                <div className="ac-ind-box low">
                  <div className="ac-ind-label">▼ Lowest</div>
                  <div className="ac-ind-val">
                    {chartData.length > 0 ? Math.min(...chartData.map((d) => d.cur)).toFixed(1) : '—'}
                  </div>
                  <div className="ac-ind-crit">{lowestCriterion?.label ?? '—'}</div>
                </div>
                <div className="ac-ind-avg-box">
                  <div className="ac-ind-avg-label">Avg Score</div>
                  <div className="ac-ind-avg-val">
                    {chartData.length > 0 ? overallScore.toFixed(1) : '—'}
                  </div>
                  <div className="ac-ind-avg-sub">across {chartData.length} criteria</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ── RIGHT COLUMN ────────────────────────────────────────────── */}
        <div className="db-right">
          <div className="act-card">
            <div className="act-head">
              <div>
                <div className="act-title">Recent Activity</div>
                <div className="act-sub">Last 7 days</div>
              </div>
              <button className="act-view-all" type="button" onClick={() => navigate('/evaluations')}>
                View all <IconArrowRight size={11} />
              </button>
            </div>

            <div className="act-list">
              {loading && <div className="act-empty">Loading activity…</div>}
              {!loading && error && <div className="act-empty">{error}</div>}
              {!loading && !error && activityItems.length === 0 && (
                <div className="act-empty">No recent activity yet.</div>
              )}
              {activityItems.map((item, i) => (
                <div key={`${item.title}-${i}`} className="act-item">
                  <div className="act-timeline">
                    <div className={`act-dot ${item.color}`} />
                    <div className="act-line" />
                  </div>
                  <div className="act-body">
                    <div className="act-msg">{item.title}</div>
                    {item.message && <div className="act-msg-sub">{item.message}</div>}
                    <div className="act-meta">
                      <span className="act-time">{item.time}</span>
                      <span className={`act-tag tag-${(tagMap[item.tag] ?? item.tag).toLowerCase()}`}>
                        {item.tag}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

      </div>

      {/* ── Facilitator Upgrade Modal ──────────────────────────────────── */}
      {showUpgradeModal && (
        <div className="db-overlay" onClick={(e) => e.target === e.currentTarget && setShowUpgradeModal(false)}>
          <div className="db-upgrade-modal">
            <div className="db-upgrade-icon">✦</div>
            <div className="db-upgrade-title">Become a Facilitator</div>
            <div className="db-upgrade-body">
              Creating evaluation forms requires the <strong>Facilitator</strong> role.
              As a Facilitator you can create forms, assign evaluators and evaluatees,
              and view aggregated results.
            </div>
            {promoteError && <div className="db-upgrade-error">{promoteError}</div>}
            <div className="db-upgrade-actions">
              <button
                className="db-upgrade-btn db-upgrade-btn--ghost"
                type="button"
                onClick={() => setShowUpgradeModal(false)}
                disabled={promoting}
              >
                Cancel
              </button>
              <button
                className="db-upgrade-btn db-upgrade-btn--primary"
                type="button"
                onClick={handlePromote}
                disabled={promoting}
              >
                {promoting ? 'Upgrading…' : 'Yes, make me a Facilitator'}
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
