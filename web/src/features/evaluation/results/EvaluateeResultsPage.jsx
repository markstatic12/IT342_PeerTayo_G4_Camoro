import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getEvaluateeSubmissions } from './evaluationResultsService';
import { listCreatedEvaluations as listFormsCreated } from '../form/evaluationFormService';
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

/* ── 5-dot track ─────────────────────────────────────────────────────── */
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
  const [data, setData] = useState(null);
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
        const [subs, forms] = await Promise.all([
          getEvaluateeSubmissions(id, userId),
          listFormsCreated(),
        ]);
        if (!alive) return;
        setData(subs);
        setMeta(forms.find((e) => String(e.id) === String(id)) ?? null);
        // auto-select first submitted evaluator
        const first = subs?.evaluators?.find((e) => e.submitted) ?? subs?.evaluators?.[0] ?? null;
        setSelectedEv(first);
      } catch {
        if (alive) setError('Unable to load results right now.');
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [id, userId]);

  const evaluators = data?.evaluators ?? [];
  const evaluateeName = data?.evaluateeName ?? 'Unknown';
  const submittedCount = data?.submittedCount ?? 0;
  const totalCount = data?.totalCount ?? 0;

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
              <Skeleton variant="text" width="120px" height="10px" />
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

      {!loading && !error && data && (
        <>
          {/* Header */}
          <div className="erp-header">
            <h1 className="erp-title">{meta?.title ?? `Evaluation #${id}`}</h1>
            <div className="erp-meta">
              <span>Evaluatee: {evaluateeName}</span>
              <span className="erp-meta-sep">·</span>
              <span>{submittedCount} of {totalCount} evaluations submitted</span>
            </div>
          </div>

          {/* Two-column layout */}
          <div className="erp-layout">

            {/* ── LEFT: evaluator list ── */}
            <div className="erp-left">
              <div className="erp-left-label">Evaluators</div>
              {evaluators.length === 0 ? (
                <div className="erp-state" style={{ padding:'24px 0', fontSize:12 }}>No evaluators assigned.</div>
              ) : (
                evaluators.map((ev) => {
                  const color = avatarColor(ev.evaluatorName);
                  const isSelected = selectedEv?.evaluatorId === ev.evaluatorId;
                  return (
                    <div
                      key={ev.evaluatorId}
                      className={`erp-ev-card${ev.submitted ? ' erp-ev-submitted' : ' erp-ev-pending'}${isSelected ? ' erp-ev-selected' : ''}`}
                      onClick={() => ev.submitted && setSelectedEv(ev)}
                    >
                      <div
                        className="erp-ev-avatar"
                        style={{ background: `${color}22`, color, border: `1.5px solid ${color}44` }}
                      >
                        {initials(ev.evaluatorName)}
                      </div>
                      <div className="erp-ev-info">
                        <div className="erp-ev-name">{ev.evaluatorName}</div>
                        <div className={`erp-ev-tag${ev.submitted ? ' erp-ev-tag--done' : ''}`}>
                          {ev.submitted ? '✓ Submitted' : 'Pending'}
                        </div>
                      </div>
                      {ev.submitted && ev.overallAverage > 0 && (
                        <div className="erp-ev-score" style={{ color }}>
                          {ev.overallAverage.toFixed(1)}
                        </div>
                      )}
                    </div>
                  );
                })
              )}
            </div>

            {/* ── RIGHT: detail panel ── */}
            <div className="erp-right">
              {!selectedEv || !selectedEv.submitted ? (
                <div className="erp-right-empty">
                  <div className="erp-right-empty-icon"><IcoChart /></div>
                  <div className="erp-right-empty-txt">Select an evaluator to view their submission</div>
                </div>
              ) : (
                <div className="erp-detail">

                  {/* Evaluator header */}
                  <div className="erp-detail-head">
                    <div
                      className="erp-detail-avatar"
                      style={{
                        background: `${avatarColor(selectedEv.evaluatorName)}22`,
                        color: avatarColor(selectedEv.evaluatorName),
                        border: `1.5px solid ${avatarColor(selectedEv.evaluatorName)}44`,
                      }}
                    >
                      {initials(selectedEv.evaluatorName)}
                    </div>
                    <div className="erp-detail-info">
                      <div className="erp-detail-name">{selectedEv.evaluatorName}</div>
                      <div className="erp-detail-sub">
                        Submitted evaluation
                        {selectedEv.submittedAt && (
                          <> · Evaluating {evaluateeName}</>
                        )}
                      </div>
                    </div>
                    <div className="erp-detail-score-hero">
                      <div className="erp-detail-score-num">
                        {toFixed(selectedEv.overallAverage, 1)}
                      </div>
                      <div className="erp-detail-score-denom">out of 5.0</div>
                    </div>
                  </div>

                  {/* Criteria scores — 2-column grid */}
                  {selectedEv.criteriaRatings?.length > 0 && (
                    <div className="erp-crit-section">
                      <div className="erp-crit-label">Criteria Scores</div>
                      <div className="erp-crit-grid">
                        {selectedEv.criteriaRatings.map((r, i) => {
                          const color = CRITERIA_COLORS[i % CRITERIA_COLORS.length];
                          return (
                            <div className="erp-crit-row" key={r.criteriaId ?? i}>
                              <div className="erp-crit-num"
                                style={{ background: `${color}22`, color }}>
                                {String(r.criteriaId ?? i + 1).padStart(2, '0')}
                              </div>
                              <div className="erp-crit-name">{r.criteriaName}</div>
                              <DotTrack score={r.score} color={color} />
                              <div className="erp-crit-score" style={{ color }}>
                                {r.score ?? '—'}
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}

                  {/* Peer comment */}
                  {selectedEv.comment && (
                    <div className="erp-comment-section">
                      <div className="erp-crit-label">Peer Comment</div>
                      <div className="erp-comment">
                        <div className="erp-comment-text">"{selectedEv.comment}"</div>
                      </div>
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
