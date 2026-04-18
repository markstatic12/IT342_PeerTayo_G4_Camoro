import { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  getMyResults,
  listCreatedEvaluations,
  listPendingEvaluations,
} from '../../services/evaluations/evaluationService';
import { listNotifications } from '../../services/notifications/notificationService';
import './DashboardPage.css';

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

const chartCriteria = [
  { label: 'Quality of Work', short: 'Quality', cur: 5.0, color: '#3b82f6' },
  { label: 'Reliability', short: 'Reliab.', cur: 4.5, color: '#a78bfa' },
  { label: 'Collaboration', short: 'Collab.', cur: 4.8, color: '#22c55e' },
  { label: 'Communication', short: 'Comm.', cur: 4.2, color: '#06b6d4' },
  { label: 'Initiative', short: 'Init.', cur: 5.0, color: '#f97316' },
  { label: 'Problem Solving', short: 'Problem', cur: 4.4, color: '#3b82f6' },
  { label: 'Professionalism', short: 'Prof.', cur: 4.6, color: '#eab308' },
  { label: 'Time Management', short: 'Time', cur: 4.3, color: '#a78bfa' },
  { label: 'Adaptability', short: 'Adapt.', cur: 3.6, color: '#ef4444' },
  { label: 'Contribution', short: 'Contrib.', cur: 4.8, color: '#22c55e' },
];

const criteriaMetaById = {
  1: { label: 'Quality of Work', short: 'Quality', color: '#3b82f6' },
  2: { label: 'Reliability & Dependability', short: 'Reliab.', color: '#a78bfa' },
  3: { label: 'Collaboration & Teamwork', short: 'Collab.', color: '#22c55e' },
  4: { label: 'Communication Skills', short: 'Comm.', color: '#06b6d4' },
  5: { label: 'Initiative & Proactiveness', short: 'Init.', color: '#f97316' },
  6: { label: 'Problem Solving', short: 'Problem', color: '#3b82f6' },
  7: { label: 'Professionalism & Conduct', short: 'Prof.', color: '#eab308' },
  8: { label: 'Time Management', short: 'Time', color: '#a78bfa' },
  9: { label: 'Adaptability & Learning', short: 'Adapt.', color: '#ef4444' },
  10: { label: 'Overall Contribution', short: 'Contrib.', color: '#22c55e' },
};

const activityFeed = [
  {
    color: 'dot-green',
    title: 'Evaluation submitted',
    message: 'Lara Santos completed her response for the review form.',
    time: '2 min ago',
    tag: 'Done',
  },
  {
    color: 'dot-blue',
    title: 'Pending review',
    message: 'Ana Cruz has a review due in 1 day.',
    time: '12 min ago',
    tag: 'Pending',
  },
  {
    color: 'dot-orange',
    title: 'Form created',
    message: 'Q1 team performance evaluation is ready for assignment.',
    time: '1 hr ago',
    tag: 'New',
  },
  {
    color: 'dot-purple',
    title: 'Reminder sent',
    message: 'Reminder emails were sent to all evaluators.',
    time: '3 hr ago',
    tag: 'Alert',
  },
  {
    color: 'dot-red',
    title: 'Response late',
    message: 'Ben Reyes has missed a submission deadline.',
    time: '6 hr ago',
    tag: 'Late',
  },
  {
    color: 'dot-blue',
    title: 'New respondent',
    message: 'Cris Tan joined the evaluation group.',
    time: 'Yesterday',
    tag: 'Update',
  },
];

function formatDateTime(isoString) {
  const date = new Date(isoString);
  return date.toLocaleString([], {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

function formatDateLabel(date) {
  return date.toLocaleDateString([], {
    weekday: 'long',
    month: 'long',
    day: 'numeric',
    year: 'numeric',
  });
}

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

  const PAD_L = 32;
  const PAD_R = 12;
  const PAD_T = 14;
  const PAD_B = 8;
  const gW = W - PAD_L - PAD_R;
  const gH = H - PAD_T - PAD_B;
  const n = criteriaData.length;
  if (n === 0) return;

  const scores = criteriaData.map((item) => item.cur);

  const yOf = (value) => PAD_T + gH - ((value - 1) / 4) * gH;
  const xOf = (index) => PAD_L + (index / (n - 1)) * gW;

  ctx.font = '8px Inter, system-ui, sans-serif';
  ctx.textAlign = 'right';

  for (let v = 1; v <= 5; v += 1) {
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

  const points = criteriaData.map((_, index) => ({ x: xOf(index), y: yOf(scores[index]) }));

  function drawCurve() {
    ctx.beginPath();
    ctx.moveTo(points[0].x, points[0].y);
    for (let i = 0; i < points.length - 1; i += 1) {
      const cp1x = points[i].x + (points[i + 1].x - points[i].x) * 0.45;
      const cp1y = points[i].y;
      const cp2x = points[i + 1].x - (points[i + 1].x - points[i].x) * 0.45;
      const cp2y = points[i + 1].y;
      ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, points[i + 1].x, points[i + 1].y);
    }
  }

  drawCurve();
  ctx.lineTo(points[points.length - 1].x, PAD_T + gH);
  ctx.lineTo(points[0].x, PAD_T + gH);
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

  points.forEach((point, index) => {
    const value = scores[index];
    const isHigh = value === highest;
    const isLow = value === lowest;
    const dotColor = isHigh ? '#60a5fa' : isLow ? '#ef4444' : '#3b82f6';
    const dotR = isHigh || isLow ? 4.5 : 3.5;

    ctx.beginPath();
    ctx.arc(point.x, point.y, dotR + 3, 0, Math.PI * 2);
    ctx.fillStyle = isHigh
      ? 'rgba(59,130,246,0.18)'
      : isLow
      ? 'rgba(239,68,68,0.18)'
      : 'rgba(59,130,246,0.1)';
    ctx.fill();

    ctx.beginPath();
    ctx.arc(point.x, point.y, dotR, 0, Math.PI * 2);
    ctx.fillStyle = dotColor;
    ctx.shadowColor = dotColor;
    ctx.shadowBlur = 6;
    ctx.fill();
    ctx.shadowBlur = 0;

    ctx.beginPath();
    ctx.arc(point.x, point.y, dotR, 0, Math.PI * 2);
    ctx.strokeStyle = 'rgba(12,15,24,0.9)';
    ctx.lineWidth = 1;
    ctx.stroke();

    if (isHigh || isLow) {
      ctx.font = '700 8.5px Inter, system-ui, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillStyle = isHigh ? '#60a5fa' : '#ef4444';
      const labelY = isHigh ? point.y - dotR - 5 : point.y + dotR + 11;
      ctx.fillText(value.toFixed(1), point.x, labelY);
    }
  });
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [createdEvaluations, setCreatedEvaluations] = useState([]);
  const [pendingEvaluations, setPendingEvaluations] = useState([]);
  const [myResults, setMyResults] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [slideIndex, setSlideIndex] = useState(0);
  const chartRef = useRef(null);

  const chartData = useMemo(() => {
    const averages = myResults?.questionAverages;
    if (!Array.isArray(averages) || averages.length === 0) return chartCriteria;

    return averages.map((item) => {
      const meta = criteriaMetaById[item.criteriaId] || {
        label: `Criteria ${item.criteriaId}`,
        short: `C${item.criteriaId}`,
        color: '#3b82f6',
      };

      return {
        ...meta,
        cur: Number(item.average ?? 0),
      };
    });
  }, [myResults]);

  useEffect(() => {
    let mounted = true;
    const loadEvaluations = async () => {
      setLoading(true);
      setError('');
      try {
        const [created, pending, results, notices] = await Promise.all([
          listCreatedEvaluations(),
          listPendingEvaluations(),
          getMyResults(),
          listNotifications(),
        ]);

        if (mounted) {
          setCreatedEvaluations(created);
          setPendingEvaluations(pending);
          setMyResults(results ?? null);
          setNotifications(notices);
        }
      } catch {
        if (mounted) setError('Unable to load evaluations right now.');
      } finally {
        if (mounted) setLoading(false);
      }
    };

    loadEvaluations();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    const interval = window.setInterval(() => {
      setSlideIndex((current) => (current + 1) % slides.length);
    }, 4200);
    return () => window.clearInterval(interval);
  }, []);

  useEffect(() => {
    if (chartRef.current) drawLineChart(chartRef.current, chartData);
  }, [chartData]);

  useEffect(() => {
    const listener = () => {
      if (chartRef.current) drawLineChart(chartRef.current, chartData);
    };
    window.addEventListener('resize', listener);
    listener();
    return () => window.removeEventListener('resize', listener);
  }, [chartData]);

  const now = new Date();
  const todayLabel = formatDateLabel(now);
  const pendingDue = pendingEvaluations.filter((evaluation) => {
    const deadline = evaluation.deadline ? new Date(evaluation.deadline) : null;
    if (!deadline) return false;
    const diff = (deadline.getTime() - now.getTime()) / (1000 * 60 * 60 * 24);
    return diff >= 0 && diff <= 2;
  }).length;
  const closedCount = createdEvaluations.reduce((sum, item) => {
    const progress = item.submissionProgress || '0/0';
    const [submitted] = progress.split('/').map(Number);
    return sum + (Number.isFinite(submitted) ? submitted : 0);
  }, 0);
  const overallScore = useMemo(() => {
    if (typeof myResults?.overallAverage === 'number') {
      return myResults.overallAverage;
    }
    const total = chartData.reduce((sum, item) => sum + item.cur, 0);
    return total / chartData.length;
  }, [chartData, myResults]);

  const highestCriterion = useMemo(() => {
    return chartData.reduce((best, item) => (item.cur > best.cur ? item : best), chartData[0]);
  }, [chartData]);

  const lowestCriterion = useMemo(() => {
    return chartData.reduce((worst, item) => (item.cur < worst.cur ? item : worst), chartData[0]);
  }, [chartData]);

  const stats = [
    { label: 'Overall Avg Score', value: overallScore.toFixed(1), variant: 'blue' },
    { label: 'Pending Evaluations', value: pendingDue, variant: 'orange' },
    { label: 'Evaluations Submitted', value: closedCount, variant: 'green' },
    { label: 'Forms Created', value: createdEvaluations.length, variant: 'warn' },
  ];

  const activityItems = notifications.length
    ? notifications.slice(0, 6).map((item, index) => ({
        color: ['dot-green', 'dot-blue', 'dot-orange', 'dot-purple', 'dot-red'][index % 5],
        title: item.message,
        message: '',
        time: item.createdAt ? formatDateTime(item.createdAt) : 'Now',
        tag: item.isRead ? 'Done' : 'New',
      }))
    : pendingEvaluations.length
    ? pendingEvaluations.slice(0, 6).map((evaluation, index) => ({
        color: ['dot-green', 'dot-blue', 'dot-orange', 'dot-purple', 'dot-red'][index % 5],
        title: `Pending: ${evaluation.title}`,
        message: `Evaluatee ${evaluation.evaluateeName}`,
        time: evaluation.deadline ? formatDateTime(evaluation.deadline) : 'No deadline',
        tag: 'Pending',
      }))
    : activityFeed;

  const activeSlide = slides[slideIndex];

  return (
    <div className="dashboard-page">
      <div className="shell">
        <div className="left-col">
          <div className="greeting-header">
            <div className="gh-left">
              <div className="gh-hello">
                Hello, <span>{user?.firstName ? `${user.firstName}` : 'Mark Anton'}</span> 👋
              </div>
              <div className="gh-sub">{todayLabel}</div>
              <div className="gh-alert">
                <span className="gh-alert-dot" />
                {pendingDue} evaluations due in 2 days
              </div>
            </div>
            <div className="gh-carousel">
              <div className="ghc-content">
                <div className="ghc-eye">{activeSlide.eye}</div>
                <div className="ghc-heading">{activeSlide.heading}</div>
                <div className="ghc-body">{activeSlide.body}</div>
              </div>
              <div className="ghc-right">
                <button className="ghc-btn" type="button" onClick={() => navigate('/forms-created')}>
                  Create Evaluation →
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

          <div className="stat-row">
            {stats.map((stat) => (
              <div key={stat.label} className={`sc c-${stat.variant}`}>
                <div className={`sc-icon ic-${stat.variant}`}>
                  <span />
                </div>
                <div className="sc-body">
                  <div className="sc-val">{stat.value}</div>
                  <div className="sc-label">{stat.label}</div>
                  <div className={`sc-delta ${stat.variant === 'green' ? 'd-up' : stat.variant === 'orange' ? 'd-warn' : 'd-up'}`}>
                    {stat.variant === 'warn'
                      ? '↑ 2 still active'
                      : stat.variant === 'green'
                      ? '↑ 100% completion rate'
                      : stat.variant === 'orange'
                      ? '⚠ 2 due in 2 days'
                      : '↑ +0.5 from last eval'}
                  </div>
                </div>
              </div>
            ))}
          </div>

          <div className="analytics-card">
            <div className="ac-head">
              <div>
                <div className="ac-title">Team performance overview</div>
                <div className="ac-sub">Track average scores across evaluation criteria.</div>
              </div>
              <span className="ac-badge">✓ Above 4.0 goal</span>
            </div>
            <div className="ac-body">
              <div className="lg-wrap">
                <canvas id="lineChart" ref={chartRef} />
              </div>
              <div className="lg-x-labels">
                {chartData.map((item) => (
                  <div key={item.label} className="lg-x-label">
                    {item.short}
                  </div>
                ))}
              </div>
              <div className="ac-indicators">
                <div className="ac-ind-box high">
                  <div className="ac-ind-label">▲ Highest</div>
                  <div className="ac-ind-val">{Math.max(...chartData.map((item) => item.cur)).toFixed(1)}</div>
                  <div className="ac-ind-crit">{highestCriterion.label}</div>
                </div>
                <div className="ac-ind-box low">
                  <div className="ac-ind-label">▼ Lowest</div>
                  <div className="ac-ind-val">{Math.min(...chartData.map((item) => item.cur)).toFixed(1)}</div>
                  <div className="ac-ind-crit">{lowestCriterion.label}</div>
                </div>
                <div className="ac-ind-avg-box">
                  <div className="ac-ind-avg-label">Avg Score</div>
                  <div className="ac-ind-avg-val">{overallScore.toFixed(1)}</div>
                  <div className="ac-ind-avg-sub">across {chartData.length} criteria</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="right-col">
          <div className="act-card">
            <div className="act-head">
              <div className="act-head-left">
                <div className="ac-title">Recent activity</div>
                <div className="ac-sub">Live update stream of evaluation events.</div>
              </div>
              <span className="act-see-all">See all</span>
            </div>
            <div className="act-list">
              {loading && <div className="ac-sub">Loading dashboard activity...</div>}
              {!loading && error && <div className="ac-sub">{error}</div>}
              {activityItems.map((item) => (
                <div key={`${item.title}-${item.time}`} className="act-item">
                  <div className="act-timeline">
                    <div className={`act-dot ${item.color}`} />
                    <div className="act-line" />
                  </div>
                  <div className="act-body">
                    <div className="act-msg">
                      <strong>{item.title}</strong> {item.message}
                    </div>
                    <div className="act-meta">
                      <span className="act-time">{item.time}</span>
                      <span className={`act-tag tag-${item.tag.toLowerCase()}`}>{item.tag}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
