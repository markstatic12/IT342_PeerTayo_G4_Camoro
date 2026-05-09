import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  archiveCompletedForm,
  deleteCompletedForm,
  getCompletedForms,
  unarchiveCompletedForm,
} from './evaluationSubmissionService';
import Skeleton from '../../../shared/components/ui/Skeleton';
import './MyCompletedFormsPage.css';

function getInitials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');
}

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString([], {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}

function averageScore(values) {
  if (!values.length) return 0;
  return values.reduce((sum, v) => sum + v, 0) / values.length;
}

function scoreColor(score) {
  if (score === 5) return 'var(--blue)';
  if (score === 4) return '#22c55e';
  if (score === 3) return '#eab308';
  if (score === 2) return '#f97316';
  return '#ef4444';
}

function scoreLabel(score) {
  return ['', 'Poor', 'Below Avg', 'Fair', 'Good', 'Excellent'][score] ?? '';
}

const SvgSearch = () => (
  <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
);

const SvgArchive = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg>
);

const SvgCheck = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor"><polyline points="9 11 12 14 22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
);

const SvgCalendar = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
);

const SvgStar = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
);

const SvgDots = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2.5" strokeLinecap="round"><circle cx="12" cy="5" r="1" fill="currentColor"/><circle cx="12" cy="12" r="1" fill="currentColor"/><circle cx="12" cy="19" r="1" fill="currentColor"/></svg>
);

const SvgEye = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
);

const SvgChat = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 0 2 2z"/></svg>
);

const SvgTrash = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
);

const SvgClose = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2.5" fill="none" stroke="currentColor"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
);

export default function MyCompletedFormsPage() {
  const [completed, setCompleted] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('all');
  const [showArchived, setShowArchived] = useState(false);
  const [selectedFormId, setSelectedFormId] = useState(null);
  const [selectedEvaluateeId, setSelectedEvaluateeId] = useState(null);
  const [menuOpenId, setMenuOpenId] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [modalEvaluatee, setModalEvaluatee] = useState(null);

  const refreshCompleted = useCallback(async (message = 'Unable to load completed forms right now.') => {
    try {
      const data = await getCompletedForms();
      setCompleted(data ?? null);
    } catch {
      setError(message);
    }
  }, []);

  const loadCompleted = useCallback(async () => {
    setLoading(true);
    setError('');
    await refreshCompleted();
    setLoading(false);
  }, [refreshCompleted]);

  useEffect(() => {
    const handler = (e) => {
      if (!e.target.closest('.fc-dots')) setMenuOpenId(null);
    };
    document.addEventListener('click', handler);
    return () => document.removeEventListener('click', handler);
  }, []);

  useEffect(() => {
    loadCompleted();
  }, [loadCompleted]);

  const forms = useMemo(() => completed?.forms ?? [], [completed]);

  const filteredForms = useMemo(() => {
    const now = Date.now();
    const q = search.trim().toLowerCase();
    let list = forms.filter((f) => {
      if (showArchived) {
        if (!f.archived) return false;
      } else if (f.archived) {
        return false;
      }
      if (q && !(f.title ?? '').toLowerCase().includes(q)) return false;
      if (filter === 'week') {
        const d = f.submittedAt ? new Date(f.submittedAt) : null;
        if (!d) return false;
        const diff = (now - d.getTime()) / (1000 * 60 * 60 * 24);
        return diff <= 7;
      }
      return true;
    });
    return list;
  }, [forms, search, filter, showArchived]);

  const selectedForm = useMemo(
    () => filteredForms.find((f) => String(f.evaluationId) === String(selectedFormId)) || null,
    [filteredForms, selectedFormId]
  );

  const selectedEvaluatee = useMemo(() => {
    if (!selectedForm || !selectedEvaluateeId) return null;
    return selectedForm.evaluatees.find((e) => String(e.assignmentId) === String(selectedEvaluateeId)) || null;
  }, [selectedForm, selectedEvaluateeId]);

  const summaryAvg = Number.isFinite(completed?.avgScoreGiven)
    ? completed.avgScoreGiven.toFixed(1)
    : '—';

  const handleSelectForm = (id) => {
    setSelectedFormId(id);
    setSelectedEvaluateeId(null);
  };

  const handleSelectEvaluatee = (assignmentId) => {
    setSelectedEvaluateeId(assignmentId);
  };

  const handleArchive = async (evaluationId, isArchived) => {
    try {
      if (isArchived) {
        await unarchiveCompletedForm(evaluationId);
      } else {
        await archiveCompletedForm(evaluationId);
      }
      setMenuOpenId(null);
      setCompleted((prev) => {
        if (!prev?.forms) return prev;
        return {
          ...prev,
          forms: prev.forms.map((form) =>
            String(form.evaluationId) === String(evaluationId)
              ? { ...form, archived: !isArchived }
              : form
          ),
        };
      });
      const hidesFromView = (!isArchived && !showArchived) || (isArchived && showArchived);
      if (hidesFromView && String(selectedFormId) === String(evaluationId)) {
        setSelectedFormId(null);
        setSelectedEvaluateeId(null);
      }
      await refreshCompleted('Unable to refresh completed forms right now.');
    } catch {
      setError('Unable to update archive status right now.');
    }
  };

  const handleDelete = async (evaluationId) => {
    const confirmed = window.confirm('Delete this completed form permanently? This cannot be undone.');
    if (!confirmed) return;
    try {
      await deleteCompletedForm(evaluationId);
      setMenuOpenId(null);
      if (String(selectedFormId) === String(evaluationId)) {
        setSelectedFormId(null);
        setSelectedEvaluateeId(null);
      }
      await refreshCompleted('Unable to refresh completed forms right now.');
    } catch {
      setError('Unable to delete completed form right now.');
    }
  };

  const handleOpenModal = (evaluatee) => {
    setModalEvaluatee(evaluatee);
    setModalOpen(true);
  };

  const handleCloseModal = () => {
    setModalOpen(false);
    setModalEvaluatee(null);
  };

  const formsEmpty = filteredForms.length === 0;

  return (
    <div className="completed-forms-page page-col animate-page">
      <div className="page-header">
        <div>
          <div className="page-title">My Completed Forms</div>
          <div className="page-sub">All evaluations you have submitted — select a form to review your answers</div>
        </div>
        <div className="search-box">
          <SvgSearch />
          <input
            type="text"
            placeholder="Search completed forms..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      {loading && (
        <>
          <div className="summary-strip">
            {[1,2,3].map((i) => (
              <div key={i} className="sum-card">
                <Skeleton variant="circle" width="36px" height="36px" className="skeleton-stagger" />
                <div style={{ display:'flex', flexDirection:'column', gap:5 }}>
                  <Skeleton variant="title" width="40px" height="22px" className="skeleton-stagger" />
                  <Skeleton variant="text" width="100px" height="10px" className="skeleton-stagger" />
                  <Skeleton variant="text" width="80px" height="9px" className="skeleton-stagger" />
                </div>
              </div>
            ))}
          </div>
          <div className="content-area">
            <div className="list-col">
              <div className="filter-row">
                <div style={{ display:'flex', gap:6 }}>
                  {[1,2].map((i) => <Skeleton key={i} variant="rect" width="70px" height="28px" style={{ borderRadius:6 }} className="skeleton-stagger" />)}
                </div>
              </div>
              <div className="form-scroll">
                {[1,2,3].map((i) => (
                  <div key={i} className="form-card" style={{ pointerEvents:'none' }}>
                    <div className="fc-top">
                      <Skeleton variant="text" width="65%" height="13px" className="skeleton-stagger" />
                      <Skeleton variant="rect" width="50px" height="20px" style={{ borderRadius:10 }} className="skeleton-stagger" />
                    </div>
                    <Skeleton variant="text" width="50%" height="10px" style={{ marginTop:6 }} className="skeleton-stagger" />
                    <div className="fc-score-row" style={{ marginTop:8 }}>
                      <Skeleton variant="rect" width="60px" height="22px" style={{ borderRadius:6 }} className="skeleton-stagger" />
                      <Skeleton variant="text" width="80px" height="10px" className="skeleton-stagger" />
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <div className="detail-panel">
              <div className="detail-empty">
                <Skeleton variant="circle" width="40px" height="40px" style={{ margin:'0 auto 12px' }} className="skeleton-stagger" />
                <Skeleton variant="text" width="60%" height="11px" style={{ margin:'0 auto' }} className="skeleton-stagger" />
              </div>
            </div>
          </div>
        </>
      )}
      {!loading && error && <div className="cf-state cf-state--error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="summary-strip">
            <div className="sum-card s-green">
              <div className="sum-icon si-green"><SvgCheck /></div>
              <div>
                <div className="sum-val" style={{ color: 'var(--green)' }}>{completed?.totalSubmitted ?? 0}</div>
                <div className="sum-lbl">Total Submitted</div>
                <div className="sum-delta" style={{ color: 'var(--muted)' }}>All time</div>
              </div>
            </div>
            <div className="sum-card s-blue">
              <div className="sum-icon si-blue"><SvgCalendar /></div>
              <div>
                <div className="sum-val" style={{ color: 'var(--blue-light)' }}>{completed?.submittedThisMonth ?? 0}</div>
                <div className="sum-lbl">Submitted This Month</div>
                <div className="sum-delta" style={{ color: 'var(--green)' }}>↑ +2 from last month</div>
              </div>
            </div>
            <div className="sum-card s-purple">
              <div className="sum-icon si-purple"><SvgStar /></div>
              <div>
                <div className="sum-val" style={{ color: 'var(--purple)' }}>{summaryAvg}</div>
                <div className="sum-lbl">Avg Score Given</div>
                <div className="sum-delta" style={{ color: 'var(--muted)' }}>Across all criteria</div>
              </div>
            </div>
          </div>

          <div className="content-area">
            <div className="list-col">
              <div className="filter-row">
                <div className="filter-tabs">
                  <div className={`ftab${filter === 'all' && !showArchived ? ' active' : ''}`} onClick={() => setFilter('all')}>All</div>
                  <div className={`ftab${filter === 'week' && !showArchived ? ' active' : ''}`} onClick={() => setFilter('week')}>This Week</div>
                </div>
                <button
                  className={`btn-archive${showArchived ? ' active' : ''}`}
                  title="View archived forms"
                  type="button"
                  onClick={() => setShowArchived((prev) => !prev)}
                >
                  <SvgArchive />
                  Archives
                </button>
              </div>
              <div className="form-scroll" id="formList">
                {formsEmpty ? (
                  <div className="cf-empty">No forms match your filter.</div>
                ) : (
                  filteredForms.map((form) => {
                    const allRatings = form.evaluatees.flatMap((e) => e.criteria.map((c) => c.rating));
                    const avg = averageScore(allRatings);
                    const menuId = `fc-menu-${form.evaluationId}`;
                    const isSelected = String(selectedFormId) === String(form.evaluationId);
                    return (
                      <div
                        key={form.evaluationId}
                        className={`form-card${isSelected ? ' selected' : ''}`}
                        onClick={() => handleSelectForm(form.evaluationId)}
                      >
                        <div className="fc-top">
                          <div className="fc-title">{form.title}</div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 5, flexShrink: 0 }}>
                            <span className="fc-badge"><SvgCheck />Done</span>
                            <div
                              className="fc-dots"
                              onClick={(e) => { e.stopPropagation(); setMenuOpenId(menuOpenId === form.evaluationId ? null : form.evaluationId); }}
                              title="Options"
                            >
                              <SvgDots />
                              <div className={`fc-menu${menuOpenId === form.evaluationId ? ' open' : ''}`} id={menuId}>
                                <div
                                  className="fc-menu-item mi-archive"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleArchive(form.evaluationId, form.archived);
                                  }}
                                >
                                  <SvgArchive />
                                  {form.archived ? 'Unarchive' : 'Archive'}
                                </div>
                                <div
                                  className="fc-menu-item mi-delete"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleDelete(form.evaluationId);
                                  }}
                                >
                                  <SvgTrash />
                                  Delete
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                        <div className="fc-meta">
                          <span>{form.totalEvaluatees} evaluatees</span>
                          <span className="fc-sep" />
                          <span>By {form.creatorName}</span>
                        </div>
                        <div className="fc-score-row">
                          <div className="fc-score-pill">
                            <span className="fc-score-num">{avg.toFixed(1)}</span>
                            <span className="fc-score-lbl">avg score</span>
                          </div>
                          <span className="fc-submitted">Submitted {formatDate(form.submittedAt)}</span>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div className="detail-panel" id="detailPanel">
              {!selectedForm ? (
                <div className="detail-empty" id="detailEmpty">
                  <div className="detail-empty-icon"><SvgCheck /></div>
                  <div className="detail-empty-txt">Select a form to review your submission</div>
                </div>
              ) : (
                <div id="detailContent" style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
                  <div className="dh">
                    <div className="dh-left">
                      <div className="dh-title">{selectedForm.title}</div>
                      <div className="dh-meta">
                        <span>By {selectedForm.creatorName}</span>
                        <span className="dh-sep" />
                        <span>Submitted {formatDate(selectedForm.submittedAt)}</span>
                        <span className="dh-sep" />
                        <span>{selectedForm.totalEvaluatees} evaluatees</span>
                      </div>
                    </div>
                    <div className="dh-right">
                      <div className="dh-stat-box green">
                        <div className="dh-stat-num" style={{ color: 'var(--green)' }}>
                          {selectedForm.totalEvaluatees}/{selectedForm.totalEvaluatees}
                        </div>
                        <div className="dh-stat-lbl">submitted</div>
                      </div>
                      <div className="dh-stat-box blue">
                        <div className="dh-stat-num" style={{ color: 'var(--blue-light)' }}>
                          {averageScore(selectedForm.evaluatees.flatMap((e) => e.criteria.map((c) => c.rating))).toFixed(1)}
                        </div>
                        <div className="dh-stat-lbl">avg score</div>
                      </div>
                    </div>
                  </div>

                  <div className="detail-body">
                    <div>
                      <div className="dp-section-label">Evaluatees You Submitted For</div>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }} id="evaluateeList">
                        {selectedForm.evaluatees.map((ev) => {
                          const ratings = ev.criteria.map((c) => c.rating);
                          const avg = averageScore(ratings);
                          const pct = Math.round((avg / 5) * 100);
                          const col = scoreColor(Math.round(avg));
                          return (
                            <div
                              key={ev.assignmentId}
                              className="ev-row"
                              onClick={() => handleSelectEvaluatee(ev.assignmentId)}
                            >
                              <div className="ev-av">{getInitials(ev.evaluateeName)}</div>
                              <div style={{ flex: 1, minWidth: 0 }}>
                                <div className="ev-name">{ev.evaluateeName}</div>
                                <div className="ev-sub">Submitted {formatDate(ev.submittedAt)}</div>
                              </div>
                              <div className="ev-right">
                                <div style={{ textAlign: 'right' }}>
                                  <div className="ev-score" style={{ color: col }}>{avg.toFixed(1)}</div>
                                  <div className="ev-score-lbl">/ 5.0</div>
                                </div>
                                <div className="ev-bar-wrap">
                                  <div style={{ fontSize: '8.5px', color: 'var(--dim)', textAlign: 'right', marginBottom: 2 }}>{pct}%</div>
                                  <div className="ev-bar-track"><div className="ev-bar-fill" style={{ width: `${pct}%`, background: col }} /></div>
                                </div>
                                <button
                                  className="btn btn-edit"
                                  onClick={(e) => { e.stopPropagation(); handleOpenModal(ev); }}
                                >
                                  <SvgEye />
                                  View
                                </button>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>

                    {selectedEvaluatee && (
                      <>
                        <div id="criteriaSummarySection">
                          <div className="dp-section-label">
                            Criteria Breakdown — <span style={{ color: 'var(--blue-light)', textTransform: 'none', fontSize: 10 }}>{selectedEvaluatee.evaluateeName}</span>
                          </div>
                          <div className="criteria-table" id="criteriaTable">
                            {selectedEvaluatee.criteria.map((c, idx) => {
                              const col = scoreColor(c.rating);
                              const pct = Math.round((c.rating / 5) * 100);
                              return (
                                <div className="ct-row" key={`${selectedEvaluatee.assignmentId}-${c.criteriaId}`}>
                                  <div className="ct-num" style={{ background: `${col}22`, color: col }}>{String(idx + 1).padStart(2, '0')}</div>
                                  <div className="ct-name">{c.criteriaName}</div>
                                  <div className="ct-bar-wrap">
                                    <div className="ct-bar-track"><div className="ct-bar-fill" style={{ width: `${pct}%`, background: col }} /></div>
                                  </div>
                                  <div className="ct-val" style={{ color: col }}>{c.rating}</div>
                                </div>
                              );
                            })}
                          </div>
                        </div>

                        {selectedEvaluatee.comment && (
                          <div id="commentSection">
                            <div className="comment-preview">
                              <div className="cp-label"><SvgChat />Your Comment</div>
                              <div className="cp-text" id="commentText">{selectedEvaluatee.comment}</div>
                            </div>
                          </div>
                        )}
                      </>
                    )}
                  </div>

                  <div className="detail-footer">
                    <span className="detail-footer-note">Read-only — evaluations cannot be changed after submission.</span>
                  </div>
                </div>
              )}
            </div>
          </div>
        </>
      )}

      <div
        className={`modal-overlay${modalOpen ? ' open' : ''}`}
        id="submissionModal"
        onClick={(e) => { if (e.target.id === 'submissionModal') handleCloseModal(); }}
      >
        {modalEvaluatee && (
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div className="modal-who">
                <div className="modal-avatar">{getInitials(modalEvaluatee.evaluateeName)}</div>
                <div>
                  <div className="modal-name">{modalEvaluatee.evaluateeName}</div>
                  <div className="modal-sub">
                    <span>{selectedForm?.title ?? '—'}</span>
                    <span className="modal-sub-dot" />
                    <span>Submitted {formatDate(modalEvaluatee.submittedAt)}</span>
                  </div>
                </div>
              </div>
              <div className="modal-header-right">
                <div className="modal-avg-badge">
                  <div className="modal-avg-num">
                    {averageScore(modalEvaluatee.criteria.map((c) => c.rating)).toFixed(1)}
                  </div>
                  <div className="modal-avg-lbl">avg score given</div>
                </div>
                <div className="modal-close" onClick={handleCloseModal} title="Close"><SvgClose /></div>
              </div>
            </div>

            <div className="modal-scale">
              <div className="modal-scale-item"><div className="modal-scale-num sn-1">1</div><div className="modal-scale-lbl">Poor</div></div>
              <div className="modal-scale-item"><div className="modal-scale-num sn-2">2</div><div className="modal-scale-lbl">Below Average</div></div>
              <div className="modal-scale-item"><div className="modal-scale-num sn-3">3</div><div className="modal-scale-lbl">Fair / Meets Expectations</div></div>
              <div className="modal-scale-item"><div className="modal-scale-num sn-4">4</div><div className="modal-scale-lbl">Good</div></div>
              <div className="modal-scale-item"><div className="modal-scale-num sn-5">5</div><div className="modal-scale-lbl">Excellent</div></div>
            </div>

            <div className="modal-body">
              <div className="modal-criteria-grid" id="modalCriteriaGrid">
                {modalEvaluatee.criteria.map((c, idx) => {
                  const col = scoreColor(c.rating);
                  return (
                    <div className={`mcv-card rv-${c.rating}`} key={`${modalEvaluatee.assignmentId}-${c.criteriaId}`}>
                      <div className="mcv-top">
                        <div className="mcv-num">{String(idx + 1).padStart(2, '0')}</div>
                        <div>
                          <div className="mcv-name">{c.criteriaName}</div>
                          <div className="mcv-desc">{c.criteriaDescription}</div>
                        </div>
                      </div>
                      <div className="mcv-bar-row">
                        {[1, 2, 3, 4, 5].map((n) => (
                          <div
                            key={n}
                            className={`mcv-dot${n <= c.rating ? ` filled-${c.rating}` : ''}`}
                          />
                        ))}
                        <span className="mcv-score-lbl" style={{ color: col }}>
                          {c.rating} — {scoreLabel(c.rating)}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
              <div className="modal-comment-card">
                <div className="modal-comment-label"><SvgChat />Final Feedback Comment</div>
                <div className="modal-comment-text" id="modalCommentText">
                  {modalEvaluatee.comment || 'No comment was provided.'}
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
