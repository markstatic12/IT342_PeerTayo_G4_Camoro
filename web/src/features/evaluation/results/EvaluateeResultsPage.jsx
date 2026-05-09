import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getEvaluationResults } from './evaluationResultsService';
import { listCreatedEvaluations } from '../form/evaluationFormService';
import Skeleton from '../../../shared/components/ui/Skeleton';
import './EvaluateeResultsPage.css';

/* ── Helpers ──────────────────────────────────────────────────────────── */
function initials(name) {
  if (!name) return '??';
  return name.split(' ').filter(Boolean).map((w) => w[0]).slice(0, 2).join('').toUpperCase();
}

function avatarColor(name) {
  const palette = ['#3b82f6','#22c55e','#f97316','#a78bfa','#eab308','#ec4899','#14b8a6'];
  let hash = 0;
  for (let i = 0; i < (name?.length ?? 0); i++) hash = name.charCodeAt(i) + ((hash << 5) - hash);
  return palette[Math.abs(hash) % palette.length];
}

function toFixed(v, d = 2) {
  const n = Number(v);
  if (!Number.isFinite(n) || n <= 0) return '—';
  return n.toFixed(d);
}

const CRITERIA_COLORS = [
  '#3b82f6','#a78bfa','#22c55e','#06b6d4',
  '#f97316','#3b82f6','#eab308','#a78bfa',
  '#ef4444','#22c55e',
];

const CRITERIA_FALLBACK = [
  'Quality of Work','Reliability & Dependability','Collaboration & Teamwork',
  'Communication Skills','Initiative & Proactiveness','Problem Solving',
  'Professionalism & Conduct','Time Management','Adaptability & Learning',
  'Overall Contribution',
];

/* ── Icons ────────────────────────────────────────────────────────────── */
const IcoBack = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="15 18 9 12 15 6"/>
  </svg>
);
const IcoChart = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
    <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
  </svg>
);
const IcoEye = () => (
  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
  </svg>
);

/* ── Dot track (5 dots, filled by score) ─────────────────────────────── */
function DotTrack({ score, color }) {
  const filled = Math.round(Math.max(0, Math.min(5, Number(score) || 0)));
  return (
    <div className="rr-dot-track">
      {Array.from({ length: 5 }, (_, i) => (
        <div
          key={i}
          className={`rr-dot${i < filled ? ' rr-dot-filled' : ' rr-dot-empty'}`}
          style={i < filled ? { background: color } : undefined}
        />
      ))}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════════════ */
export default function EvaluateeResultsPage() {
  const { id, userId } = useParams();
  const navigate = useNavigate();
  const [allResults, setAllResults] = useState(null);
  const [meta, setMeta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedEv, setSelectedEv] = useState(null);

  useEffect(() => {
    let alive = true;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const [res, list] = await Promise.all([
          getEvaluationResults(id),
          listCreatedEvaluations(),
        ]);
        if (!alive) return;
        setAllResults(res);
        setMeta(list.find((e) => String(e.id) === String(id)) ?? null);
      } catch {
        if (alive) setError('Unable to load results right now.');
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [id]);

  const evaluatee = allResults?.evaluatees?.find(
    (ev) => String(ev.userId ?? ev.evaluateeId ?? ev.id) === String(userId)
  ) ?? null;

  const evaluators = evaluatee?.submissions
    ?? evaluatee?.responses
    ?? evaluatee?.evaluatorResponses
    ?? evaluatee?.evaluators
    ?? [];

  // auto-select first submitted evaluator
  useEffect(() => {
    if (evaluators.length > 0) {
      const first = evaluators.find((e) => e.submittedAt || e.submitted) ?? evaluators[0];
      setSelectedEv(first ?? null);
    } else {
      setSelectedEv(null);
    }
  }, [allResults, userId]); // eslint-disable-line react-hooks/exhaustive-deps

  const evaluateeName = evaluatee?.evaluateeName ?? evaluatee?.name ?? 'Unknown';
  const submittedCount = evaluatee?.submittedResponses ?? 0;
  const totalCount = evaluatee?.totalResponses ?? 0;

  /* ── Render ── */
  return (
    <div className="erp-page animate-page">

      {/* Back */}
      <button className="erp-back" type="button" onClick={() => navigate(`/forms-created/${id}/results`)}>
        <IcoBack /> Back to Form Details
      </button>

      {/* Loading */}
      {loading && (
        <>
          <div className="erp-header">
            <Skeleton variant="title" width="55%" height="22px" />
            <div style={{ display:'flex', gap:8, marginTop:8 }}>
              <Skeleton variant="text" width="100px" height="10px" />
              <Skeleton variant="text" width="80px" height="10px" />
            </div>
          </div>
          <div className="erp-layout">
            <div className="erp-left">
              {[1,2,3].map((i) => (
                <div key={i} className="erp-ev-card">
                  <Skeleton variant="circle" width="36px" height="36px" />
                  <div style={{ flex:1, display:'flex', flexDirection:'column', gap:5 }}>
                    <Skeleton variant="text" width="70%" height="12px" />
                    <Skeleton variant="text" width="50%" height="10px" />
                  </div>
                  <Skeleton variant="text" width="32px" height="18px" />
                </div>
              ))}
            </div>
            <div className="erp-right">
              <div className="erp-right-empty">
                <Skeleton variant="circle" width="48px" height="48px" style={{ margin:'0 auto 12px' }} />
                <Skeleton variant="text" width="50%" height="12px" style={{ margin:'0 auto' }} />
              </div>
            </div>
          </div>
        </>
      )}

      {!loading && error && <div className="erp-state erp-state--error">{error}</div>}
      {!loading && !error && !evaluatee && (
        <div className="erp-state">Evaluatee not found for this evaluation.</div>
      )}

      {!loading && !error && evaluatee && (
        <>
          {/* Header */}
          <div className="erp-header">
            <h1 className="erp-title">{meta?.title ?? `Evaluation #${id}`}</h1>
            <div className="erp-meta">
              <span>{evaluateeName}</span>
              <span className="erp-meta-sep">·</span>
              <span>{submittedCount} response{submittedCount !== 1 ? 's' : ''}</span>
            </div>
          </div>

          {/* Two-column layout */}
          <div className="erp-layout">

            {/* ── LEFT: evaluator list ── */}
            <div className="erp-left">
              <div className="erp-left-label">Evaluators</div>
              {evaluators.length === 0 ? (
                <div className="erp-state" style={{ padding:'24px 0' }}>No evaluators assigned.</div>
              ) : (
                evaluators.map((ev) => {
                  const name = ev.evaluatorName ?? ev.name ?? ev.displayName ?? 'Unknown';
                  const key = ev.evaluatorId ?? ev.userId ?? ev.id ?? name;
                  const submitted = Boolean(ev.submittedAt || ev.submitted);
                  const avg = Number(ev.overallAverage ?? ev.average ?? 0);
                  const color = avatarColor(name);
                  const isSelected = selectedEv && (
                    selectedEv === ev ||
                    (selectedEv.evaluatorId ?? selectedEv.userId) === (ev.evaluatorId ?? ev.userId)
                  );

                  return (
                    <div
                      key={key}
                      className={`erp-ev-card${submitted ? ' erp-ev-submitted' : ' erp-ev-pending'}${isSelected ? ' erp-ev-selected' : ''}`}
                      onClick={() => submitted && setSelectedEv(ev)}
                    >
                      {/* Avatar */}
                      <div
                        className="erp-ev-avatar"
                        style={{ background: `${color}22`, color, border: `1.5px solid ${color}44` }}
                      >
                        {initials(name)}
                      </div>

                      {/* Info */}
                      <div className="erp-ev-info">
                        <div className="erp-ev-name">{name}</div>
                        <div className={`erp-ev-tag${submitted ? ' erp-ev-tag--done' : ''}`}>
                          {submitted ? '✓ Submitted' : 'Pending'}
                        </div>
                      </div>

                      {/* Score */}
                      {submitted && avg > 0 && (
                        <div className="erp-ev-score" style={{ color }}>
                          {avg.toFixed(1)}
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </div>

            {/* ── RIGHT: result panel ── */}
            <div className="erp-right">
              {!selectedEv ? (
                /* Empty state — no evaluator selected */
                <div className="erp-right-empty">
                  <div className="erp-right-empty-icon"><IcoChart /></div>
                  <div className="erp-right-empty-txt">Select an evaluator to view their submission</div>
                </div>
              ) : (
                /* Selected evaluator detail */
                <div className="erp-detail">

                  {/* Summary boxes */}
                  <div className="erp-summary-row">
                    <div className="erp-summary-box">
                      <div className="erp-summary-label">Overall Score</div>
                      <div className="erp-summary-val erp-summary-val--blue">
                        {toFixed(selectedEv.overallAverage ?? selectedEv.average, 2)}
                      </div>
                      <div className="erp-summary-sub">out of 5.0</div>
                    </div>
                    <div className="erp-summary-box">
                      <div className="erp-summary-label">Evaluation Progress</div>
                      <div className="erp-summary-val erp-summary-val--blue">
                        {submittedCount}
                      </div>
                      <div className="erp-summary-sub">of {totalCount} submitted</div>
                    </div>
                  </div>

                  {/* Criteria breakdown */}
                  <div className="erp-crit-section">
                    <div className="erp-crit-label">Criteria Breakdown</div>
                    <div className="erp-crit-list">
                      {(selectedEv.criteriaRatings ?? selectedEv.ratings ?? evaluatee.criteriaAverages ?? []).map((r, i) => {
                        const score = Number(r.score ?? r.average ?? r ?? 0);
                        const label = r.criteriaName ?? CRITERIA_FALLBACK[(r.criteriaId ?? i + 1) - 1] ?? `Criteria ${i + 1}`;
                        const color = CRITERIA_COLORS[i % CRITERIA_COLORS.length];
                        return (
                          <div className="erp-crit-row" key={i}>
                            <div className="erp-crit-name">{label}</div>
                            <DotTrack score={score} color={color} />
                            <div className="erp-crit-score" style={{ color }}>
                              {score > 0 ? score.toFixed(1) : '—'}
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  </div>

                  {/* Comment */}
                  {selectedEv.comment && (
                    <div className="erp-comment">
                      <div className="erp-comment-label">Peer Comment</div>
                      <div className="erp-comment-text">"{selectedEv.comment}"</div>
                    </div>
                  )}

                </div>
              )}
            </div>

          </div>
        </>
      )}
    </div>
  );
}
