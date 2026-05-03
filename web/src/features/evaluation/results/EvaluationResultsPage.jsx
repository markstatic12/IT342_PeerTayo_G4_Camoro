import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getEvaluationResults } from './evaluationResultsService';
import { listCreatedEvaluations } from '../form/evaluationFormService';
import './EvaluationResultsPage.css';

/* ── Helpers ──────────────────────────────────────────────────────────── */
function initials(name) {
  if (!name) return '??';
  return name.split(' ').map((w) => w[0]).slice(0, 2).join('').toUpperCase();
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

function statusLabel(status) {
  if (!status) return 'Active';
  switch (status.toUpperCase()) {
    case 'ACTIVE':   return 'Active';
    case 'CLOSED':   return 'Closed';
    case 'EXPIRED':  return 'Expired';
    case 'ARCHIVED': return 'Archived';
    default:         return 'Needs Attention';
  }
}

function statusVariant(status) {
  if (!status) return 'active';
  switch (status.toUpperCase()) {
    case 'ACTIVE':   return 'active';
    case 'CLOSED':   return 'closed';
    case 'EXPIRED':  return 'expired';
    case 'ARCHIVED': return 'archived';
    default:         return 'attention';
  }
}

/* ── Icons ────────────────────────────────────────────────────────────── */
function IconBack() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="15 18 9 12 15 6"/>
    </svg>
  );
}
function IconEye() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
      <circle cx="12" cy="12" r="3"/>
    </svg>
  );
}
function IconAlert() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
      <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
    </svg>
  );
}

/* ══════════════════════════════════════════════════════════════════════
   EvaluationResultsPage
   ══════════════════════════════════════════════════════════════════════ */
export default function EvaluationResultsPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [results, setResults] = useState(null);
  const [meta, setMeta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

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

  return (
    <div className="er-page">
      {/* Breadcrumb */}
      <button className="er-back" type="button" onClick={() => navigate('/forms-created')}>
        <IconBack /> Forms Created
      </button>

      {loading && <div className="er-state">Loading results…</div>}
      {!loading && error && <div className="er-state er-state--error">{error}</div>}

      {!loading && !error && (
        <>
          {/* Eval header */}
          <div className="er-header">
            <h1 className="er-title">{meta?.title ?? `Evaluation #${id}`}</h1>
            <div className="er-meta">
              {meta?.deadline && <span>Due {formatDate(meta.deadline)}</span>}
              {meta?.deadline && <span className="er-meta__dot">·</span>}
              {meta?.createdAt && <span>Created {formatDate(meta.createdAt)}</span>}
              {meta?.createdAt && <span className="er-meta__dot">·</span>}
              {meta?.submissionProgress && <span>{meta.submissionProgress} submitted</span>}
              {meta?.status && (
                <>
                  <span className="er-meta__dot">·</span>
                  <span className={`er-badge er-badge--${statusVariant(meta.status)}`}>
                    {meta.status?.toUpperCase() !== 'ACTIVE' && <IconAlert />}
                    {statusLabel(meta.status)}
                  </span>
                </>
              )}
            </div>
          </div>

          {/* Section label */}
          <div className="er-section-label">
            Evaluatees
            <span className="er-section-count">
              {evaluatees.length} evaluatee{evaluatees.length !== 1 ? 's' : ''}
            </span>
          </div>

          {/* Table */}
          <div className="er-table-wrap">
            {evaluatees.length === 0 ? (
              <div className="er-state">No evaluatees assigned to this evaluation yet.</div>
            ) : (
              <table className="er-table">
                <thead>
                  <tr>
                    <th>Evaluatee</th>
                    <th>Avg Score</th>
                    <th>Progress</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {evaluatees.map((ev) => {
                    const color = avatarColor(ev.evaluateeName);
                    const submitted = ev.submittedResponses ?? 0;
                    const total = ev.totalResponses ?? 0;
                    const pct = progressPct(submitted, total);
                    const isFull = pct === 100;
                    const avg = Number(ev.overallAverage ?? 0);

                    return (
                      <tr key={ev.userId} className="er-row">
                        {/* Evaluatee name + avatar */}
                        <td className="er-row__name-cell">
                          <div className="er-row__name-wrap">
                            <div
                              className="er-avatar"
                              style={{ background: `${color}22`, color, border: `1.5px solid ${color}55` }}
                            >
                              {initials(ev.evaluateeName)}
                            </div>
                            <div>
                              <div className="er-row__name">{ev.evaluateeName}</div>
                              <div className="er-row__sub">
                                {ev.criteriaAverages?.length ?? 0} criteria rated
                              </div>
                            </div>
                          </div>
                        </td>

                        {/* Avg score */}
                        <td className="er-row__avg">
                          {avg > 0 ? (
                            <span className={`er-score${avg >= 4.0 ? ' er-score--high' : avg >= 3.0 ? ' er-score--mid' : ' er-score--low'}`}>
                              {avg.toFixed(2)}
                            </span>
                          ) : (
                            <span className="er-score er-score--none">—</span>
                          )}
                        </td>

                        {/* Progress */}
                        <td className="er-row__progress">
                          <div className="er-progress-bar">
                            <div
                              className={`er-progress-fill${isFull ? ' er-progress-fill--full' : ''}`}
                              style={{ width: `${pct}%` }}
                            />
                          </div>
                          <span className="er-progress-label">{submitted}/{total}</span>
                        </td>

                        {/* Actions */}
                        <td className="er-row__actions">
                          <button
                            className="er-view-btn"
                            type="button"
                            onClick={() => navigate(`/forms-created/${id}/evaluatee/${ev.userId}`)}
                          >
                            <IconEye /> View Results
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
}
