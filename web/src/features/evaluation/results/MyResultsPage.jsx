import { useEffect, useMemo, useState } from 'react';
import { archiveMyResult, getMyResults, unarchiveMyResult } from './evaluationResultsService';
import './MyResultsPage.css';

const CRITERIA_FALLBACK = [
  'Quality of Work','Reliability','Collaboration','Communication',
  'Initiative','Problem Solving','Professionalism','Time Management',
  'Adaptability','Contribution',
];

const CRITERIA_COLORS = [
  '#3b82f6','#a78bfa','#22c55e','#06b6d4',
  '#f97316','#3b82f6','#eab308','#a78bfa',
  '#ef4444','#22c55e',
];

const EVAL_COLOR_KEYS = ['blue','green','purple','orange'];

function formatDate(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
}

function toFixedOrDash(value, digits = 1) {
  if (!Number.isFinite(value) || value <= 0) return '—';
  return value.toFixed(digits);
}

function averagePct(value) {
  if (!Number.isFinite(value) || value <= 0) return 0;
  return Math.round((value / 5) * 100);
}

function scoreColorClass(key) {
  return { blue: 'sp-blue', green: 'sp-green', purple: 'sp-purple', orange: 'sp-orange' }[key] || 'sp-blue';
}

function evalClass(key) {
  return { blue: 'selected', green: 'ec-green', purple: 'ec-purple', orange: 'ec-orange' }[key] || 'selected';
}

function barColor(key) {
  return { blue: 'var(--blue)', green: 'var(--green)', purple: 'var(--purple)', orange: 'var(--orange)' }[key] || 'var(--blue)';
}

const SvgSearch = () => (
  <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
);

const SvgDots = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2.5" strokeLinecap="round"><circle cx="12" cy="5" r="1" fill="currentColor"/><circle cx="12" cy="12" r="1" fill="currentColor"/><circle cx="12" cy="19" r="1" fill="currentColor"/></svg>
);

const SvgArchive = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="21 8 21 21 3 21 3 8"/><rect x="1" y="3" width="22" height="5"/><line x1="10" y1="12" x2="14" y2="12"/></svg>
);

const SvgTrash = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
);

export default function MyResultsPage() {
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('all');
  const [showArchived, setShowArchived] = useState(false);
  const [selectedId, setSelectedId] = useState(null);
  const [menuOpenId, setMenuOpenId] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    let alive = true;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const data = await getMyResults();
        if (!alive) return;
        setResults(data ?? null);
      } catch {
        if (alive) setError('Unable to load results right now.');
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, []);

  useEffect(() => {
    const handler = (e) => {
      if (!e.target.closest('.ec-dots')) setMenuOpenId(null);
    };
    document.addEventListener('click', handler);
    return () => document.removeEventListener('click', handler);
  }, []);

  const evaluations = useMemo(() => {
    const list = results?.evaluations ?? [];
    return list.map((ev, idx) => ({
      ...ev,
      colorKey: EVAL_COLOR_KEYS[idx % EVAL_COLOR_KEYS.length],
    }));
  }, [results]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    let list = evaluations.filter((ev) => {
      if (showArchived) {
        if (!ev.archived) return false;
      } else if (ev.archived) {
        return false;
      }
      if (!q) return true;
      return (ev.title ?? '').toLowerCase().includes(q)
        || (ev.createdByName ?? '').toLowerCase().includes(q);
    });

    if (filter === 'highest') {
      list = [...list].sort((a, b) => (b.overallAverage ?? 0) - (a.overallAverage ?? 0));
    }
    if (filter === 'recent') {
      list = [...list].sort((a, b) => new Date(b.submittedAt ?? 0) - new Date(a.submittedAt ?? 0));
    }
    return list;
  }, [evaluations, search, filter]);

  const selected = useMemo(
    () => evaluations.find((ev) => String(ev.evaluationId) === String(selectedId)) || null,
    [evaluations, selectedId]
  );

  const summaryHighest = useMemo(() => {
    const list = results?.questionAverages ?? [];
    if (!list.length) return null;
    return list.reduce((best, item) => (item.average > best.average ? item : best), list[0]);
  }, [results]);

  const handleSelect = (id) => {
    setSelectedId(id);
  };

  const handleArchive = async (evaluationId, isArchived) => {
    try {
      if (isArchived) {
        await unarchiveMyResult(evaluationId);
      } else {
        await archiveMyResult(evaluationId);
      }
      setResults((prev) => {
        if (!prev?.evaluations) return prev;
        return {
          ...prev,
          evaluations: prev.evaluations.map((ev) =>
            String(ev.evaluationId) === String(evaluationId)
              ? { ...ev, archived: !isArchived }
              : ev
          ),
        };
      });
      if (!isArchived) setSelectedId(null);
    } catch {
      setError('Unable to update archive status right now.');
    }
  };

  const handleOpenModal = () => {
    if (!selected) return;
    setIsModalOpen(true);
  };

  const handleCloseModal = () => setIsModalOpen(false);

  return (
    <div className="my-results-page page-col animate-page">
      <div className="page-header">
        <div className="page-title-wrap">
          <div className="page-title">My Results</div>
          <div className="page-sub">Evaluations you&apos;ve received as a respondent</div>
        </div>
        <div className="page-header-right">
          <div className="search-box">
            <SvgSearch />
            <input
              type="text"
              placeholder="Search evaluations..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
        </div>
      </div>

      {loading && <div className="mr-state">Loading results…</div>}
      {!loading && error && <div className="mr-state mr-state--error">{error}</div>}

      {!loading && !error && (
        <>
          <div className="summary-strip">
            <div className="sum-card s-blue">
              <div className="sum-icon si-blue">
                <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
              </div>
              <div>
                <div className="sum-val" style={{ color: 'var(--blue-light)' }}>{toFixedOrDash(results?.overallAverage, 1)}</div>
                <div className="sum-lbl">Overall Avg Score</div>
                <div className="sum-delta" style={{ color: 'var(--green)' }}>↑ +0.5 improvement</div>
              </div>
            </div>
            <div className="sum-card s-green">
              <div className="sum-icon si-green">
                <svg viewBox="0 0 24 24" strokeWidth="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </div>
              <div>
                <div className="sum-val" style={{ color: 'var(--green)' }}>{evaluations.length}</div>
                <div className="sum-lbl">Evaluations Received</div>
                <div className="sum-delta" style={{ color: 'var(--muted)' }}>Across {evaluations.length} forms</div>
              </div>
            </div>
            <div className="sum-card s-purple">
              <div className="sum-icon si-purple">
                <svg viewBox="0 0 24 24" strokeWidth="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
              </div>
              <div>
                <div className="sum-val" style={{ color: 'var(--purple)' }}>{results?.totalResponses ?? 0}</div>
                <div className="sum-lbl">Total Responses</div>
                <div className="sum-delta" style={{ color: 'var(--muted)' }}>From peers</div>
              </div>
            </div>
            <div className="sum-card s-orange">
              <div className="sum-icon si-orange">
                <svg viewBox="0 0 24 24" strokeWidth="2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>
              </div>
              <div>
                <div className="sum-val" style={{ color: 'var(--orange-lt)' }}>
                  {toFixedOrDash(summaryHighest?.average, 1)}
                </div>
                <div className="sum-lbl">Highest Criterion</div>
                <div className="sum-delta" style={{ color: 'var(--orange-lt)' }}>
                  {summaryHighest?.criteriaName ?? (summaryHighest ? CRITERIA_FALLBACK[(summaryHighest.criteriaId ?? 1) - 1] : '—')}
                </div>
              </div>
            </div>
          </div>

          <div className="content-area">
            <div className="eval-list-col">
              <div className="filter-row">
                <div className="filter-tabs">
                  <div className={`ftab${filter === 'all' ? ' active' : ''}`} onClick={() => setFilter('all')}>All</div>
                  <div className={`ftab${filter === 'highest' ? ' active' : ''}`} onClick={() => setFilter('highest')}>Highest</div>
                  <div className={`ftab${filter === 'recent' ? ' active' : ''}`} onClick={() => setFilter('recent')}>Most Recent</div>
                </div>
                <button
                  className="btn-archive"
                  type="button"
                  title="View archived evaluations"
                  onClick={() => setShowArchived((prev) => !prev)}
                >
                  <SvgArchive />
                  Archives
                </button>
              </div>

              <div className="eval-scroll" id="evalList">
                {filtered.length === 0 ? (
                  <div className="mr-empty">No evaluation results found.</div>
                ) : (
                  filtered.map((ev) => {
                    const pct = averagePct(ev.overallAverage ?? 0);
                    const menuId = `menu-${ev.evaluationId}`;
                    const isSelected = String(selectedId) === String(ev.evaluationId);
                    return (
                      <div
                        key={ev.evaluationId}
                        className={`eval-card ${evalClass(ev.colorKey)}${isSelected ? ' selected' : ''}`}
                        onClick={() => handleSelect(ev.evaluationId)}
                      >
                        <div className="ec-top">
                          <div className="ec-title">{ev.title}</div>
                          <div className="ec-top-right">
                            <span className={`ec-score-pill ${scoreColorClass(ev.colorKey)}`}>
                              {toFixedOrDash(ev.overallAverage, 1)}
                            </span>
                            <div
                              className="ec-dots"
                              onClick={(e) => { e.stopPropagation(); setMenuOpenId(menuOpenId === ev.evaluationId ? null : ev.evaluationId); }}
                              title="Options"
                            >
                              <SvgDots />
                              <div className={`ec-menu${menuOpenId === ev.evaluationId ? ' open' : ''}`} id={menuId}>
                                <div
                                  className="ec-menu-item mi-archive"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    setMenuOpenId(null);
                                    handleArchive(ev.evaluationId, ev.archived);
                                  }}
                                >
                                  <SvgArchive />
                                  {ev.archived ? 'Unarchive' : 'Archive'}
                                </div>
                                <div className="ec-menu-item mi-delete">
                                  <SvgTrash />
                                  Delete
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                        <div className="ec-meta">
                          <span>{ev.responses ?? 0} responses</span><span className="ec-sep"></span><span>{formatDate(ev.submittedAt)}</span>
                        </div>
                        <div className="ec-bar-wrap">
                          <div className="ec-bar-track"><div className="ec-bar-fill" style={{ width: `${pct}%`, background: barColor(ev.colorKey) }} /></div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div className="detail-panel" id="detailPanel">
              {!selected ? (
                <div className="detail-empty" id="detailEmpty">
                  <div className="detail-empty-icon"><svg viewBox="0 0 24 24" strokeWidth="1.5"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg></div>
                  <div className="detail-empty-txt">Select an evaluation to view details</div>
                </div>
              ) : (
                <div id="detailContent" style={{ display: 'flex', flexDirection: 'column', flex: 1, overflow: 'hidden' }}>
                  <div className="detail-head">
                    <div className="dh-left">
                      <div className="dh-title">{selected.title}</div>
                      <div className="dh-meta">
                        <span>{selected.responses ?? 0} peer responses</span>
                        <span className="dh-sep" />
                        <span>{formatDate(selected.submittedAt)}</span>
                        <span className="dh-sep" />
                        <span>By {selected.createdByName ?? '—'}</span>
                      </div>
                    </div>
                    <div className="dh-right">
                      <div className="dh-score-hero">
                        <div className="dh-score-num">{toFixedOrDash(selected.overallAverage, 1)}</div>
                        <div className="dh-score-denom">out of 5.0</div>
                      </div>
                      <button
                        type="button"
                        onClick={handleOpenModal}
                        style={{ background: 'var(--blue)', color: '#fff', border: 'none', borderRadius: 8, padding: '8px 16px', fontFamily: 'Plus Jakarta Sans, sans-serif', fontSize: 12, fontWeight: 700, cursor: 'pointer', boxShadow: '0 4px 14px var(--blue-glow)' }}
                      >
                        View Full Details
                      </button>
                    </div>
                  </div>
                  <div className="detail-body">
                    <div className="criteria-section">
                      <div className="cs-title">Criteria Breakdown</div>
                      <div className="criteria-grid" id="criteriaGrid">
                        {(selected.criteriaAverages ?? []).map((s, i) => {
                          const filled = Math.round(s.average ?? 0);
                          const label = s.criteriaName ?? CRITERIA_FALLBACK[(s.criteriaId ?? 1) - 1] ?? `Criteria ${s.criteriaId}`;
                          const color = CRITERIA_COLORS[i % CRITERIA_COLORS.length];
                          return (
                            <div className="crit-row" key={`${selected.evaluationId}-${s.criteriaId}`}>
                              <div className="crit-name">{label}</div>
                              <div className="crit-dots">
                                {Array.from({ length: 5 }, (_, j) => (
                                  <div
                                    key={j}
                                    className={`crit-dot ${j < filled ? 'filled' : 'empty'}`}
                                    style={j < filled ? { background: color } : undefined}
                                  />
                                ))}
                              </div>
                              <div className="crit-score" style={{ color }}>{toFixedOrDash(s.average, 1)}</div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                    <div className="comments-section">
                      <div className="cs-title">Peer Comments</div>
                      <div id="commentsList">
                        {selected.comments?.length ? (
                          <div className="comment-card" style={{ borderLeftColor: barColor(selected.colorKey) }}>
                            <div className="comment-text">&quot;{selected.comments[0].comment}&quot;</div>
                            <div className="comment-meta">Anonymous · {formatDate(selected.comments[0].submittedAt)}</div>
                          </div>
                        ) : (
                          <div className="no-comment">No comments were provided for this evaluation.</div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </>
      )}

      <div className={`modal-overlay${isModalOpen ? ' open' : ''}`} onClick={(e) => e.target === e.currentTarget && handleCloseModal()}>
        {selected && (
          <div className="modal">
            <div className="modal-head">
              <div>
                <div className="modal-title">{selected.title}</div>
                <div className="modal-sub">{selected.responses ?? 0} responses · {formatDate(selected.submittedAt)} · Created by {selected.createdByName ?? '—'}</div>
              </div>
              <div className="modal-close" onClick={handleCloseModal}>×</div>
            </div>
            <div className="modal-body">
              <div className="modal-left">
                <div className="modal-score-hero">
                  <div className="msh-label">Overall Score</div>
                  <div><span className="msh-score">{toFixedOrDash(selected.overallAverage, 1)}</span><span className="msh-denom"> / 5.0</span></div>
                  <div className="msh-badge">{selected.overallAverage >= 4.5 ? '✓ Excellent' : selected.overallAverage >= 4.0 ? '✓ Above Goal' : 'Needs Improvement'}</div>
                </div>
                <div className="modal-info-box">
                  <div className="mib-row"><span className="mib-lbl">Responses</span><span className="mib-val">{selected.responses ?? 0} peers</span></div>
                  <div className="mib-row"><span className="mib-lbl">Submitted</span><span className="mib-val">{formatDate(selected.submittedAt)}</span></div>
                  <div className="mib-row"><span className="mib-lbl">Created by</span><span className="mib-val">{selected.createdByName ?? '—'}</span></div>
                  <div className="mib-row"><span className="mib-lbl">Criteria</span><span className="mib-val">10 / 10</span></div>
                </div>
              </div>
              <div className="modal-right">
                <div>
                  <div className="mr-section-title">Criteria Breakdown</div>
                  <div className="mc-crit-list">
                    {(selected.criteriaAverages ?? []).map((s, i) => {
                      const pct = averagePct(s.average ?? 0);
                      const color = CRITERIA_COLORS[i % CRITERIA_COLORS.length];
                      const label = s.criteriaName ?? CRITERIA_FALLBACK[(s.criteriaId ?? 1) - 1] ?? `Criteria ${s.criteriaId}`;
                      return (
                        <div className="mc-crit-row" key={`modal-${selected.evaluationId}-${s.criteriaId}`}>
                          <div className="mc-crit-num">{String(i + 1).padStart(2, '0')}</div>
                          <div className="mc-crit-name">{label}</div>
                          <div className="mc-bar-track"><div className="mc-bar-fill" style={{ width: `${pct}%`, background: color }} /></div>
                          <div className="mc-crit-val" style={{ color }}>{toFixedOrDash(s.average, 1)}</div>
                        </div>
                      );
                    })}
                  </div>
                </div>
                <div>
                  <div className="mr-section-title" style={{ marginTop: 4 }}>Peer Comment</div>
                  <div className="mc-comment" id="modal-comment">
                    {selected.comments?.length ? (
                      <>
                        <div className="mc-comment-text">&quot;{selected.comments[0].comment}&quot;</div>
                        <div style={{ fontSize: '9.5px', color: 'var(--dim)', marginTop: 6 }}>Anonymous · {formatDate(selected.comments[0].submittedAt)}</div>
                      </>
                    ) : (
                      <div className="mc-comment-none">No comments were provided for this evaluation.</div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
