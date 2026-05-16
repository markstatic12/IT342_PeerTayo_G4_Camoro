import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getEvaluationResults, extendDeadline, closePermanently } from './evaluationResultsService';
import { listCreatedEvaluations } from '../form/evaluationFormService';
import Skeleton from '../../../shared/components/ui/Skeleton';
import './EvaluationResultsPage.css';

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

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
}

function progressPct(submitted, total) {
  if (!total) return 0;
  return Math.round((submitted / total) * 100);
}

function isOverdue(meta) {
  if (!meta?.deadline) return false;
  if (meta.status?.toUpperCase() !== 'ACTIVE') return false;
  return new Date(meta.deadline) < new Date();
}

function normalizedStatus(meta) {
  const s = meta?.status?.toUpperCase() ?? 'ACTIVE';
  if (s === 'CLOSED' || s === 'ARCHIVED') return 'closed';
  if (s === 'ACTIVE' && isOverdue(meta)) return 'attention';
  return 'active';
}

function statusLabel(meta) {
  const s = normalizedStatus(meta);
  if (s === 'closed') return 'Closed';
  if (s === 'attention') return 'Needs Attention';
  return 'Active';
}

/* ── Icons ────────────────────────────────────────────────────────────── */
const IcoBack = () => (
  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="15 18 9 12 15 6"/>
  </svg>
);
const IcoEye = () => (
  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>
  </svg>
);
const IcoAlert = () => (
  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
    <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
  </svg>
);

/* ══════════════════════════════════════════════════════════════════════
   EvaluationResultsPage — Screen 2: evaluatee list with score boxes
   ══════════════════════════════════════════════════════════════════════ */
export default function EvaluationResultsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [results, setResults] = useState(null);
  const [meta, setMeta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showExtendModal, setShowExtendModal] = useState(false);
  const [newDeadline, setNewDeadline] = useState('');
  const [processing, setProcessing] = useState(false);

  const refreshData = async () => {
    setLoading(true);
    try {
      const [res, list] = await Promise.all([
        getEvaluationResults(id),
        listCreatedEvaluations(),
      ]);
      setResults(res);
      setMeta(list.find((e) => String(e.id) === String(id)) ?? null);
    } catch {
      setError('Unable to load results right now.');
    } finally {
      setLoading(false);
    }
  };

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
        setResults(res);
        setMeta(list.find((e) => String(e.id) === String(id)) ?? null);
      } catch {
        if (alive) setError('Unable to load results right now.');
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [id]);

  const evaluatees = results?.evaluatees ?? [];
  const statusKey = normalizedStatus(meta);

  return (
    <div className="er-page animate-page">

      {/* Back */}
      <button className="er-back" type="button" onClick={() => navigate('/forms-created')}>
        <IcoBack /> Forms Created
      </button>

      {/* Loading */}
      {loading && (
        <>
          <div className="er-header">
            <Skeleton variant="title" width="50%" height="22px" />
            <div style={{ display:'flex', gap:8, marginTop:8 }}>
              <Skeleton variant="text" width="80px" height="10px" />
              <Skeleton variant="text" width="80px" height="10px" />
            </div>
          </div>
          <div className="er-list">
            {[1,2,3].map((i) => (
              <div key={i} className="er-ev-card">
                <Skeleton variant="circle" width="42px" height="42px" />
                <div style={{ flex:1, display:'flex', flexDirection:'column', gap:6 }}>
                  <Skeleton variant="text" width="40%" height="13px" />
                  <Skeleton variant="text" width="25%" height="10px" />
                </div>
                <div style={{ display:'flex', gap:5 }}>
                  {[1,2,3,4,5].map((j) => <Skeleton key={j} variant="rect" width="28px" height="28px" style={{ borderRadius:6 }} />)}
                </div>
                <Skeleton variant="rect" width="90px" height="32px" style={{ borderRadius:8 }} />
              </div>
            ))}
          </div>
        </>
      )}

      {!loading && error && <div className="er-state er-state--error">{error}</div>}

      {!loading && !error && (
        <>
          {/* Header */}
          <div className="er-header">
            <h1 className="er-title">{meta?.title ?? `Evaluation #${id}`}</h1>
            <div className="er-meta">
              {meta?.deadline && <span>Due {formatDate(meta.deadline)}</span>}
              {meta?.deadline && <span className="er-meta-dot">·</span>}
              {meta?.createdAt && <span>Created {formatDate(meta.createdAt)}</span>}
              {meta?.createdAt && <span className="er-meta-dot">·</span>}
              {meta?.submissionProgress && <span>Based on {meta.submissionCount ?? 0} response{meta.submissionCount !== 1 ? 's' : ''}</span>}
              {meta?.status && (
                <>
                  <span className="er-meta-dot">·</span>
                  <span className={`er-badge er-badge--${statusKey}`}>
                    {statusKey !== 'active' && <IcoAlert />}
                    {statusLabel(meta)}
                  </span>
                </>
              )}
            </div>
          </div>

          {/* BR-004: Zero Submissions Alert */}
          {meta?.status === 'CLOSED' && meta?.submissionCount === 0 && !meta?.permanentlyClosed && (
            <div className="er-zero-alert animate-slide-down">
              <div className="er-zero-alert__icon">
                <IcoAlert />
              </div>
              <div className="er-zero-alert__content">
                <div className="er-zero-alert__title">No responses received</div>
                <div className="er-zero-alert__text">
                  The deadline has passed with zero submissions. You can extend the deadline to give evaluators more time or close it permanently.
                </div>
              </div>
              <div className="er-zero-alert__actions">
                <button className="er-btn er-btn-outline" onClick={() => setShowExtendModal(true)}>Extend Deadline</button>
                <button className="er-btn er-btn-danger" onClick={async () => {
                  if (window.confirm('Are you sure you want to close this evaluation permanently?')) {
                    await closePermanently(id);
                    refreshData();
                  }
                }}>Close Permanently</button>
              </div>
            </div>
          )}

          {/* Section label */}
          <div className="er-section-label">
            Evaluatees
            <span className="er-section-count">
              {evaluatees.length} evaluatee{evaluatees.length !== 1 ? 's' : ''}
            </span>
          </div>

          {/* Evaluatee card list */}
          <div className="er-list">
            {evaluatees.length === 0 ? (
              <div className="er-state">No evaluatees assigned to this evaluation yet.</div>
            ) : (
              evaluatees.map((ev) => {
                const name = ev.evaluateeName ?? ev.name ?? ev.displayName ?? 'Unknown';
                const userIdKey = ev.userId ?? ev.id ?? ev.evaluateeId ?? name;
                const color = avatarColor(name);
                const submitted = Number(ev.submittedResponses ?? ev.responsesReceived ?? 0);
                const total = Number(ev.totalResponses ?? ev.expectedResponses ?? 0);
                const pct = progressPct(submitted, total);
                const isFull = pct === 100;
                const avg = Number(ev.overallAverage ?? ev.average ?? 0);

                // Score boxes from individual evaluator ratings
                const scoreBoxes = (ev.submissions ?? ev.responses ?? ev.evaluators ?? [])
                  .filter((s) => s.submittedAt || s.submitted)
                  .map((s) => Number(s.overallAverage ?? s.average ?? 0));

                return (
                  <div
                    key={userIdKey}
                    className={`er-ev-card${isFull ? ' er-ev-card--done' : ''}`}
                    onClick={() => navigate(`/forms-created/${id}/evaluatee/${userIdKey}`)}
                  >
                    {/* Avatar */}
                    <div
                      className="er-ev-avatar"
                      style={{ background: `${color}22`, color, border: `1.5px solid ${color}44` }}
                    >
                      {initials(name)}
                    </div>

                    {/* Name + sub */}
                    <div className="er-ev-info">
                      <div className="er-ev-name">{name}</div>
                      <div className="er-ev-sub">Based on {submitted} response{submitted !== 1 ? 's' : ''}</div>
                    </div>

                    {/* Score boxes — one per submitted evaluator */}
                    <div className="er-score-boxes">
                      {scoreBoxes.length > 0 ? (
                        scoreBoxes.map((s, i) => {
                          const rounded = Math.max(1, Math.min(5, Math.round(s)));
                          return (
                            <div key={i} className={`er-score-box er-score-box--${rounded}`}>
                              {s > 0 ? s.toFixed(1) : '—'}
                            </div>
                          );
                        })
                      ) : (
                        Array.from({ length: total || 1 }, (_, i) => (
                          <div key={i} className="er-score-box er-score-box--pending">—</div>
                        ))
                      )}
                    </div>

                    {/* Avg + progress */}
                    <div className="er-ev-avg">
                      <div className="er-ev-avg-val" style={{ color: avg >= 4 ? '#22c55e' : avg >= 3 ? '#eab308' : avg > 0 ? '#f87171' : '#4a5568' }}>
                        {avg > 0 ? Math.round((avg / 5) * 100) + '%' : '—'}
                      </div>
                      <div className="er-ev-avg-lbl">avg score</div>
                    </div>

                    {/* View button */}
                    <button
                      className="er-view-btn"
                      type="button"
                      onClick={(e) => { e.stopPropagation(); navigate(`/forms-created/${id}/evaluatee/${userIdKey}`); }}
                    >
                      <IcoEye /> View Results
                    </button>
                  </div>
                );
              })
            )}
          </div>
        </>
      )}

      {/* Extend Deadline Modal */}
      {showExtendModal && (
        <div className="er-modal-overlay" onClick={() => !processing && setShowExtendModal(false)}>
          <div className="er-modal" onClick={e => e.stopPropagation()}>
            <div className="er-modal-title">Extend Deadline</div>
            <div className="er-modal-text">Select a new future deadline for this evaluation. All assigned evaluators will be re-notified.</div>
            <input 
              type="datetime-local" 
              className="er-input" 
              value={newDeadline}
              onChange={e => setNewDeadline(e.target.value)}
              min={new Date().toISOString().slice(0, 16)}
            />
            <div className="er-modal-actions">
              <button className="er-btn er-btn-ghost" onClick={() => setShowExtendModal(false)} disabled={processing}>Cancel</button>
              <button className="er-btn er-btn-primary" onClick={async () => {
                if (!newDeadline) return;
                setProcessing(true);
                try {
                  await extendDeadline(id, new Date(newDeadline).toISOString());
                  setShowExtendModal(false);
                  refreshData();
                } catch (err) {
                  alert(err.response?.data?.message || 'Failed to extend deadline');
                } finally {
                  setProcessing(false);
                }
              }} disabled={processing || !newDeadline}>
                {processing ? 'Extending...' : 'Extend Deadline'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
