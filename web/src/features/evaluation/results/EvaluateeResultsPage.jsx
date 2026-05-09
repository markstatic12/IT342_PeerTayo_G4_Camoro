import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getEvaluationResults } from './evaluationResultsService';
import { listCreatedEvaluations } from '../form/evaluationFormService';
import Skeleton from '../../../shared/components/ui/Skeleton';
import './EvaluateeResultsPage.css';

/* ── Helpers ──────────────────────────────────────────────────────────── */
function initials(name) {
  if (!name) return '??';
  return name.split(' ').map((w) => w[0]).slice(0, 2).join('').toUpperCase();
}

function toFixedOrDash(value, digits = 1) {
  if (!Number.isFinite(value) || value <= 0) return '—';
  return value.toFixed(digits);
}

const CRITERIA_COLORS = [
  '#3b82f6','#a78bfa','#22c55e','#06b6d4',
  '#f97316','#3b82f6','#eab308','#a78bfa',
  '#ef4444','#22c55e',
];

const CRITERIA_FALLBACK = [
  'Quality of Work','Reliability','Collaboration','Communication',
  'Initiative','Problem Solving','Professionalism','Time Management',
  'Adaptability','Contribution',
];

/* ── Icons ────────────────────────────────────────────────────────────── */
function IconBack() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="15 18 9 12 15 6"/>
    </svg>
  );
}

function IconChart() {
  return (
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round">
      <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
    </svg>
  );
}

/* ══════════════════════════════════════════════════════════════════════
   EvaluateeResultsPage
   Show detailed results for a specific evaluatee within an evaluation
   ══════════════════════════════════════════════════════════════════════ */
export default function EvaluateeResultsPage() {
  const { id, userId } = useParams();
  const navigate = useNavigate();
  const [allResults, setAllResults] = useState(null);
  const [meta, setMeta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedEvaluator, setSelectedEvaluator] = useState(null);

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

  // Find the specific evaluatee from all results
  const evaluatee = allResults?.evaluatees?.find((ev) => String(ev.userId) === String(userId)) ?? null;
  const hasSubmittedResponses = evaluatee && (evaluatee.submittedResponses ?? 0) > 0;
  const totalResponses = evaluatee?.totalResponses ?? 0;

  // Try to find evaluator-level submissions in several possible keys.
  const evaluatorSubmissions = evaluatee?.submissions || evaluatee?.responses || evaluatee?.evaluatorResponses || evaluatee?.evaluators || [];

  useEffect(() => {
    // auto-select the first submitted evaluator when data loads
    if (evaluatorSubmissions && evaluatorSubmissions.length > 0) {
      const first = evaluatorSubmissions.find((s) => s.submittedAt || s.submitted) || evaluatorSubmissions[0];
      setSelectedEvaluator(first ?? null);
    } else {
      setSelectedEvaluator(null);
    }
  }, [allResults, userId]);

  return (
    <div className="result-col">
      {/* Back Button */}
      <button className="back-btn" onClick={() => navigate(`/forms-created/${id}/results`)}>
        <IconBack /> Back to Form Details
      </button>

      {loading && (
        <div style={{ padding: '40px 20px', textAlign: 'center' }}>
          <Skeleton variant="title" width="40%" height="22px" className="skeleton-stagger" style={{ margin: '0 auto 20px' }} />
          <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:12, marginTop:16 }}>
            <Skeleton variant="rect" width="100%" height="140px" style={{ borderRadius:11 }} className="skeleton-stagger" />
            <Skeleton variant="rect" width="100%" height="140px" style={{ borderRadius:11 }} className="skeleton-stagger" />
          </div>
        </div>
      )}

      {!loading && error && <div className="er-state er-state--error">{error}</div>}

      {!loading && !error && !evaluatee && (
        <div className="er-state">Evaluatee results not found for this evaluation.</div>
      )}

      {!loading && !error && evaluatee && (
        <>
          {/* Result Header */}
          <div className="result-header">
            <div className="result-form-title">{meta?.title ?? `Evaluation #${id}`}</div>
            <div className="result-meta">
              <span>{evaluatee.evaluateeName}</span>
              <span className="rm-sep"></span>
              <span>{evaluatee.submittedResponses ?? 0} responses</span>
            </div>
          </div>

          {/* Empty State or Results */}
          {!hasSubmittedResponses ? (
            <div className="result-layout">
              <div className="result-left">
                {evaluatorSubmissions.length === 0 ? (
                  <div className="er-state">No evaluators have submitted yet.</div>
                ) : (
                  evaluatorSubmissions.map((ev) => {
                    const name = ev.evaluatorName || ev.name || ev.displayName || ev.userName || 'Unknown';
                    const idKey = ev.evaluatorId ?? ev.userId ?? ev.id ?? name;
                    const submitted = Boolean(ev.submittedAt || ev.submitted);
                    const avg = Number(ev.overallAverage ?? ev.average ?? 0);
                    return (
                      <div
                        key={idKey}
                        className={`ev-row-card ${submitted ? 'ev-done' : 'ev-partial'} ${selectedEvaluator && (selectedEvaluator === ev || (selectedEvaluator.evaluatorId ?? selectedEvaluator.userId) === (ev.evaluatorId ?? ev.userId)) ? 'selected' : ''}`}
                        onClick={() => submitted && setSelectedEvaluator(ev)}
                      >
                        <div className={`eva-circle ${submitted ? 'eva-s' : 'eva-p'}`}>
                          {initials(name)}
                        </div>
                        <div className="ev-row-info">
                          <div className="ev-row-name">{name}</div>
                          <div className="ev-row-sub">{submitted ? 'Submitted evaluation' : 'Pending'}</div>
                        </div>
                        <div className="ev-score-boxes">
                          {(ev.criteriaRatings || ev.ratings || []).slice(0,5).map((r, idx) => (
                            <div key={idx} className={`ev-score-box esb-${Math.max(1, Math.min(5, Math.round(Number((r.score ?? r) || 0))))}`}>{(r.score ?? r) || '—'}</div>
                          ))}
                        </div>
                        <div className="ev-avg-block">
                          <div className="ev-avg-pct">{avg > 0 ? Math.round((avg / 5) * 100) + '%' : '—'}</div>
                          <div className="ev-avg-lbl">avg score</div>
                        </div>
                        <button className="ev-view-btn" type="button" onClick={() => submitted && setSelectedEvaluator(ev)}>
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                          View
                        </button>
                      </div>
                    );
                  })
                )}
              </div>

              <div className="result-right">
                <div className="rr-empty">
                  <div className="rr-empty-icon"><IconChart /></div>
                  <div className="rr-empty-txt">No Results Available Yet</div>
                  <div className="rr-empty-desc">This evaluatee hasn't received any evaluation submissions yet. Results will appear here once evaluators submit their responses.</div>
                </div>
              </div>
            </div>
          ) : (
            <div className="result-layout">
              <div className="result-left">
                {evaluatorSubmissions.length === 0 ? (
                  <div className="er-state">No evaluators assigned.</div>
                ) : (
                  evaluatorSubmissions.map((ev) => {
                    const name = ev.evaluatorName || ev.name || ev.displayName || ev.userName || 'Unknown';
                    const idKey = ev.evaluatorId ?? ev.userId ?? ev.id ?? name;
                    const submitted = Boolean(ev.submittedAt || ev.submitted);
                    const avg = Number(ev.overallAverage ?? ev.average ?? 0);
                    return (
                      <div
                        key={idKey}
                        className={`ev-row-card ${submitted ? 'ev-done' : 'ev-partial'} ${selectedEvaluator && (selectedEvaluator === ev || (selectedEvaluator.evaluatorId ?? selectedEvaluator.userId) === (ev.evaluatorId ?? ev.userId)) ? 'selected' : ''}`}
                        onClick={() => submitted && setSelectedEvaluator(ev)}
                      >
                        <div className={`eva-circle ${submitted ? 'eva-s' : 'eva-p'}`}>
                          {initials(name)}
                        </div>
                        <div className="ev-row-info">
                          <div className="ev-row-name">{name}</div>
                          <div className="ev-row-sub">{submitted ? `${ev.submittedAt ? new Date(ev.submittedAt).toLocaleString() : 'Submitted'}` : 'Pending'}</div>
                        </div>
                        <div className="ev-score-boxes">
                          {(ev.criteriaRatings || ev.ratings || []).slice(0,5).map((r, idx) => (
                            <div key={idx} className={`ev-score-box esb-${Math.max(1, Math.min(5, Math.round(Number((r.score ?? r) || 0))))}`}>{(r.score ?? r) || '—'}</div>
                          ))}
                        </div>
                        <div className="ev-avg-block">
                          <div className="ev-avg-pct">{avg > 0 ? Math.round((avg / 5) * 100) + '%' : '—'}</div>
                          <div className="ev-avg-lbl">avg score</div>
                        </div>
                        <button className="ev-view-btn" type="button" onClick={() => submitted && setSelectedEvaluator(ev)}>
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                          View Results
                        </button>
                      </div>
                    );
                  })
                )}
              </div>

              <div className="result-right">
                {selectedEvaluator ? (
                  <div className="rr-content" style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                      <div style={{ width:48, height:48, borderRadius:10, background:'#0f1724', display:'flex',alignItems:'center',justifyContent:'center',fontWeight:800 }}>{initials(selectedEvaluator.evaluatorName || selectedEvaluator.name)}</div>
                      <div>
                        <div style={{ fontSize:15, fontWeight:800 }}>{selectedEvaluator.evaluatorName || selectedEvaluator.name}</div>
                        <div style={{ color:'#94a3b8', fontSize:12 }}>{selectedEvaluator.submittedAt ? new Date(selectedEvaluator.submittedAt).toLocaleString() : 'Submitted'}</div>
                      </div>
                      <div style={{ marginLeft:'auto', textAlign:'right' }}>
                        <div style={{ fontFamily:'DM Mono, monospace', fontSize:20, fontWeight:900 }}>{toFixedOrDash(Number(selectedEvaluator.overallAverage ?? selectedEvaluator.average ?? 0), 2)}</div>
                        <div style={{ color:'#64748b', fontSize:11 }}>out of 5.0</div>
                      </div>
                    </div>

                    <div className="result-criteria" style={{ flex:1 }}>
                      <div className="result-section-title">Evaluator Scores</div>
                      <div className="result-criteria-list">
                        {(selectedEvaluator.criteriaRatings || selectedEvaluator.ratings || []).map((r, i) => (
                          <div className="result-criteria-row" key={i}>
                            <div className="result-crit-name">{r.criteriaName ?? `Criteria ${r.criteriaId ?? i+1}`}</div>
                            <div className="result-crit-dots">
                              {Array.from({ length: 5 }).map((_, j) => (
                                <div key={j} className={`result-crit-dot ${j < Math.round((r.score ?? r) || 0) ? 'filled' : 'empty'}`} style={j < Math.round((r.score ?? r) || 0) ? { background: CRITERIA_COLORS[i % CRITERIA_COLORS.length] } : undefined} />
                              ))}
                            </div>
                            <div className="result-crit-score">{toFixedOrDash(Number((r.score ?? r) || 0), 1)}</div>
                          </div>
                        ))}
                      </div>
                    </div>

                    {selectedEvaluator.comment && (
                      <div style={{ background:'#0b1220', border:'1px solid rgba(255,255,255,0.04)', borderRadius:10, padding:12 }}>
                        <div style={{ fontSize:11, color:'#94a3b8', fontWeight:700, marginBottom:6 }}>Peer Comment</div>
                        <div style={{ color:'#cbd5e1' }}>&quot;{selectedEvaluator.comment}&quot;</div>
                      </div>
                    )}
                  </div>
                ) : (
                  <div style={{ padding: 12 }}>
                    <div className="result-summary-cards">
                      <div className="result-card">
                        <div className="result-card-label">Overall Score</div>
                        <div className="result-card-value">{toFixedOrDash(evaluatee.overallAverage, 2)}</div>
                        <div className="result-card-sub">out of 5.0</div>
                      </div>
                      <div className="result-card">
                        <div className="result-card-label">Evaluation Progress</div>
                        <div className="result-card-value">{evaluatee.submittedResponses ?? 0}</div>
                        <div className="result-card-sub">of {totalResponses} submitted</div>
                      </div>
                    </div>

                    <div className="result-criteria" style={{ marginTop: 12 }}>
                      <div className="result-section-title">Criteria Breakdown</div>
                      <div className="result-criteria-list">
                        {(evaluatee.criteriaAverages ?? []).map((crit, i) => {
                          const filled = Math.round(crit.average ?? 0);
                          const label = crit.criteriaName ?? CRITERIA_FALLBACK[(crit.criteriaId ?? 1) - 1] ?? `Criteria ${crit.criteriaId}`;
                          const color = CRITERIA_COLORS[i % CRITERIA_COLORS.length];
                          return (
                            <div className="result-criteria-row" key={`${evaluatee.userId}-${crit.criteriaId}`}>
                              <div className="result-crit-name">{label}</div>
                              <div className="result-crit-dots">
                                {Array.from({ length: 5 }, (_, j) => (
                                  <div
                                    key={j}
                                    className={`result-crit-dot ${j < filled ? 'filled' : 'empty'}`}
                                    style={j < filled ? { background: color } : undefined}
                                  />
                                ))}
                              </div>
                              <div className="result-crit-score" style={{ color }}>{toFixedOrDash(crit.average, 1)}</div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
