import { useEffect, useRef, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSubmittedSummary, listPendingEvaluations } from './evaluationSubmissionService';
import './PendingEvaluationsPage.css';

/* ─── Helpers ─────────────────────────────────────────────── */
function getInitials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');
}

function daysLeft(deadline) {
  if (!deadline) return null;
  const diff = (new Date(deadline).getTime() - Date.now()) / (1000 * 60 * 60 * 24);
  return Math.ceil(diff);
}

function formatDeadline(deadline) {
  if (!deadline) return '—';
  return new Date(deadline).toLocaleDateString([], {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}

/**
 * The backend returns a flat list — one row per evaluatee assignment:
 *   { id, assignmentId, title, deadline, evaluateeName }
 *
 * We group them client-side by `id` (the evaluationForm id) so the UI can
 * show one card per form with a nested evaluatees list.
 */
function groupByForm(flatList) {
  const map = new Map();
  for (const item of flatList) {
    if (!map.has(item.id)) {
      map.set(item.id, {
        id:         item.id,
        title:      item.title,
        deadline:   item.deadline,
        evaluatees: [],
      });
    }
    map.get(item.id).evaluatees.push({
      assignmentId: item.assignmentId,
      name:         item.evaluateeName,
      done:         false,   // pending list = always not yet submitted
    });
  }
  return Array.from(map.values());
}

/* ─── Tiny inline SVG helpers ─────────────────────────────── */
const SvgClock = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
  </svg>
);
const SvgAlert = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="12" cy="12" r="10" />
    <line x1="12" y1="8" x2="12" y2="12" /><line x1="12" y1="16" x2="12.01" y2="16" />
  </svg>
);
const SvgCheck = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="9 11 12 14 22 4" />
  </svg>
);
const SvgSearch = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" />
  </svg>
);
const SvgArchive = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="21 8 21 21 3 21 3 8" /><rect x="1" y="3" width="22" height="5" />
    <line x1="10" y1="12" x2="14" y2="12" />
  </svg>
);
const SvgChevRight = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="9 18 15 12 9 6" />
  </svg>
);
const SvgDots = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2.5" fill="none" stroke="currentColor" strokeLinecap="round">
    <circle cx="12" cy="5"  r="1" fill="currentColor" />
    <circle cx="12" cy="12" r="1" fill="currentColor" />
    <circle cx="12" cy="19" r="1" fill="currentColor" />
  </svg>
);
const SvgTrash = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="3 6 5 6 21 6" />
    <path d="M19 6l-1 14H6L5 6" /><path d="M10 11v6M14 11v6" />
    <path d="M9 6V4h6v2" />
  </svg>
);

/* ─── EvalCard (list item with 3-dot menu) ────────────────── */
function EvalCard({ form, isSelected, onSelect, onArchive, isArchived }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const dotsRef = useRef(null);

  const total  = form.evaluatees.length;
  const done   = form.evaluatees.filter((e) => e.done).length;
  const pct    = total ? ((done / total) * 100).toFixed(0) : 0;
  const dl     = daysLeft(form.deadline);
  const urgent = dl !== null && dl <= 3;

  useEffect(() => {
    const handler = (e) => {
      if (!dotsRef.current?.contains(e.target)) setMenuOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  return (
    <div
      className={`eval-card ${urgent ? 'ec-warn' : 'ec-blue'}${isSelected ? ' selected' : ''}`}
      onClick={() => onSelect(form.id)}
    >
      <div className="ec-top">
        <div className="ec-title">{form.title}</div>
        <div className="ec-top-right">
          <span className={`ec-pill ${urgent ? 'ep-warn' : 'ep-blue'}`}>
            {urgent ? 'Urgent' : 'Pending'}
          </span>
          <div
            ref={dotsRef}
            className="ec-dots"
            title="Options"
            onClick={(e) => { e.stopPropagation(); setMenuOpen((o) => !o); }}
          >
            <SvgDots />
            <div className={`ec-menu${menuOpen ? ' open' : ''}`}>
              <div
                className="ec-menu-item mi-archive"
                onClick={(e) => { e.stopPropagation(); setMenuOpen(false); onArchive(form.id); }}
              >
                <SvgArchive /> {isArchived ? 'Unarchive' : 'Archive'}
              </div>
              <div
                className="ec-menu-item mi-delete"
                onClick={(e) => {
                  e.stopPropagation(); setMenuOpen(false);
                  if (window.confirm('Delete this evaluation? This cannot be undone.')) alert('Evaluation deleted.');
                }}
              >
                <SvgTrash /> Delete
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="ec-meta">
        <span>{total} evaluatee{total !== 1 ? 's' : ''}</span>
        <span className="ec-sep" />
        <span>Due {formatDeadline(form.deadline)}</span>
        {urgent && dl !== null && (
          <>
            <span className="ec-sep" />
            <span style={{ color: 'var(--pe-warn)', fontWeight: 700 }}>{dl}d left</span>
          </>
        )}
      </div>

      <div className="ec-progress-row">
        <div className="ec-bar-track">
          <div
            className="ec-bar-fill"
            style={{ width: `${pct}%`, background: urgent ? 'var(--pe-warn)' : 'var(--pe-blue)' }}
          />
        </div>
        <span className="ec-prog-txt">{done}/{total}</span>
      </div>
    </div>
  );
}

/* ─── Detail panel ────────────────────────────────────────── */
function DetailPanel({ form, onStartEvaluate }) {
  if (!form) {
    return (
      <div className="detail-panel">
        <div className="detail-empty">
          <div className="detail-empty-icon"><SvgClock /></div>
          <div className="detail-empty-txt">Select an evaluation to view details</div>
        </div>
      </div>
    );
  }

  const evaluatees = form.evaluatees;
  const done       = evaluatees.filter((e) => e.done).length;
  const dl         = daysLeft(form.deadline);
  const urgent     = dl !== null && dl <= 3;

  return (
    <div className="detail-panel">
      <div className="dh">
        <div>
          <div className="dh-title">{form.title}</div>
          <div className="dh-meta">
            <span>Due {formatDeadline(form.deadline)}</span>
            <span className="dh-sep" />
            <span>{evaluatees.length} evaluatee{evaluatees.length !== 1 ? 's' : ''}</span>
          </div>
        </div>
        <div className="dh-right">
          <div className="dh-progress-hero">
            <div className="dh-prog-num">{done}/{evaluatees.length}</div>
            <div className="dh-prog-lbl">evaluated</div>
          </div>
          {urgent && dl !== null && (
            <div className="dh-urgent-hero">
              <div className="dh-urgent-num">{dl}d</div>
              <div className="dh-urgent-lbl">days left</div>
            </div>
          )}
        </div>
      </div>

      <div className="detail-body">
        <div>
          <div className="dp-section-title">Evaluatees</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
            {evaluatees.map((ev) => (
              <div className="ev-row" key={ev.assignmentId}>
                <div className={`ev-av${ev.done ? ' done' : ''}`}>
                  {getInitials(ev.name)}
                </div>
                <div>
                  <div className="ev-name">{ev.name}</div>
                  <div className="ev-sub">Not yet submitted</div>
                </div>
                <div className="ev-right">
                  <button
                    className="btn btn-primary"
                    onClick={() => onStartEvaluate(form, ev)}
                  >
                    Evaluate <SvgChevRight />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="detail-footer">
        <span className="detail-footer-note">
          Evaluations are independent — submit in any order.
        </span>
        {/* Start with first pending evaluatee */}
        {evaluatees.filter((e) => !e.done).length > 0 && (
          <button
            className="btn btn-primary"
            onClick={() => onStartEvaluate(form, evaluatees.find((e) => !e.done))}
          >
            Start Next <SvgChevRight />
          </button>
        )}
      </div>
    </div>
  );
}

/* ─── Main Page ───────────────────────────────────────────── */
export default function PendingEvaluationsPage() {
  const navigate = useNavigate();
  // Raw flat list from API, grouped into forms
  const [rawList, setRawList]     = useState([]);
  const [loading, setLoading]     = useState(true);
  const [error, setError]         = useState('');
  const [selectedId, setSelectedId] = useState(null);
  const [filter, setFilter]       = useState('all');
  const [search, setSearch]       = useState('');
  const [archivedIds, setArchivedIds] = useState([]);
  const [submittedThisMonth, setSubmittedThisMonth] = useState(0);

  useEffect(() => {
    let mounted = true;
    (async () => {
      setLoading(true);
      setError('');
      try {
        // Returns: [{ id, assignmentId, title, deadline, evaluateeName }, ...]
        const [flat, summary] = await Promise.all([
          listPendingEvaluations(),
          getSubmittedSummary(),
        ]);
        if (mounted) {
          setRawList(flat);
          setSubmittedThisMonth(summary?.submittedThisMonth ?? 0);
        }
      } catch {
        if (mounted) setError('Unable to load pending evaluations right now.');
        if (mounted) setSubmittedThisMonth(0);
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, []);

  // Group flat API list into forms with evaluatees arrays
  const forms = useMemo(() => groupByForm(rawList), [rawList]);

  // Derived totals
  const urgentCount = forms.filter((f) => {
    const dl = daysLeft(f.deadline);
    return dl !== null && dl <= 3;
  }).length;

  // Total pending evaluatee assignments
  const totalPending = rawList.length;

  // Filtered forms
  const visible = useMemo(() => forms.filter((f) => {
    const isArchived = archivedIds.includes(f.id);
    
    // If viewing archives, only show archived items
    if (filter === 'archived') {
      if (!isArchived) return false;
    } else {
      // If viewing active lists, hide archived items
      if (isArchived) return false;
      
      const dl = daysLeft(f.deadline);
      if (filter === 'urgent' && !(dl !== null && dl <= 3)) return false;
      if (filter === 'missed' && !(dl !== null && dl < 0)) return false;
    }

    if (search) {
      const q = search.toLowerCase();
      if (!(f.title ?? '').toLowerCase().includes(q)) return false;
    }
    return true;
  }), [forms, filter, search, archivedIds]);

  const selectedForm = forms.find((f) => f.id === selectedId) ?? null;

  const handleSelect = useCallback((id) => {
    setSelectedId((prev) => (prev === id ? null : id));
  }, []);

  const handleStartEvaluate = useCallback((form, ev) => {
    navigate('/evaluate', { state: { form, evaluatee: ev } });
  }, [navigate]);

  const handleArchive = useCallback((id) => {
    setArchivedIds(prev => 
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
    if (selectedId === id) setSelectedId(null);
  }, [selectedId]);

  return (
    <div className="pe-page animate-page">
      {loading && <div className="pe-loading">Loading evaluations…</div>}
      {!loading && error && <div className="pe-error">{error}</div>}
      {!loading && !error && (
        <div className="page-col">

          {/* Header */}
          <div className="page-header">
            <div>
              <div className="page-title">Pending Evaluations</div>
              <div className="page-sub">Assigned to you — select a form to begin evaluating</div>
            </div>
            <div className="search-box">
              <SvgSearch />
              <input
                type="text"
                placeholder="Search evaluations…"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>

          {/* Summary strip */}
          <div className="summary-strip">
            <div className="sum-card s-blue">
              <div className="sum-icon si-blue"><SvgClock /></div>
              <div>
                <div className="sum-val">{totalPending}</div>
                <div className="sum-lbl">Pending Count</div>
                <div className="sum-delta sd-neutral">
                  Across {forms.length} form{forms.length !== 1 ? 's' : ''}
                </div>
              </div>
            </div>
            <div className="sum-card s-orange">
              <div className="sum-icon si-orange"><SvgAlert /></div>
              <div>
                <div className="sum-val">{urgentCount}</div>
                <div className="sum-lbl">Urgent</div>
                <div className="sum-delta sd-warn">Due within 3 days</div>
              </div>
            </div>
            <div className="sum-card s-green">
              <div className="sum-icon si-green"><SvgCheck /></div>
              <div>
                <div className="sum-val">{submittedThisMonth}</div>
                <div className="sum-lbl">Submitted This Month</div>
                <div className="sum-delta sd-green">Keep it up!</div>
              </div>
            </div>
          </div>

          {/* Content area */}
          <div className="content-area">

            {/* LEFT: list column */}
            <div className="list-col">
              <div className="filter-row">
                <div className="filter-tabs">
                  {[
                    { id: 'all',    label: 'All' },
                    { id: 'urgent', label: 'Urgent' },
                    { id: 'missed', label: 'Missed' },
                  ].map((t) => (
                    <div
                      key={t.id}
                      className={`ftab${filter === t.id ? ' active' : ''}`}
                      onClick={() => setFilter(t.id)}
                    >
                      {t.label}
                    </div>
                  ))}
                </div>
                <button 
                  className={`btn-archive${filter === 'archived' ? ' active' : ''}`} 
                  title="View archived evaluations"
                  onClick={() => setFilter(prev => prev === 'archived' ? 'all' : 'archived')}
                >
                  <SvgArchive /> Archives
                </button>
              </div>

              <div className="eval-scroll">
                {visible.length === 0 ? (
                  <div className="pe-empty-list">
                    {search ? 'No matching evaluations.' : 'No pending evaluations.'}
                  </div>
                ) : (
                  visible.map((f) => (
                    <EvalCard
                      key={f.id}
                      form={f}
                      isSelected={selectedId === f.id}
                      onSelect={handleSelect}
                      onArchive={handleArchive}
                      isArchived={archivedIds.includes(f.id)}
                    />
                  ))
                )}
              </div>
            </div>

            {/* RIGHT: detail panel */}
            <DetailPanel
              form={selectedForm}
              onStartEvaluate={handleStartEvaluate}
            />
          </div>
        </div>
      )}
    </div>
  );
}
