import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  listCreatedEvaluations,
  archiveEvaluation,
  deleteEvaluation,
} from './evaluationFormService';
import Skeleton from '../../../shared/components/ui/Skeleton';
import ExitConfirmModal from '../../../shared/components/ui/ExitConfirmModal';
import './FormsCreatedPage.css';

/* ── Helpers ──────────────────────────────────────────────────────────── */
function formatDeadline(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString([], {
    month: 'short', day: 'numeric', year: 'numeric',
  });
}

function toDatetimeLocal(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function statusVariant(status) {
  if (!status) return 'active';
  switch (status.toUpperCase()) {
    case 'ACTIVE':    return 'active';
    case 'CLOSED':    return 'closed';
    default:          return 'attention';
  }
}

function isOverdueEvaluation(evaluation) {
  if (!evaluation?.deadline) return false;
  if (evaluation.status?.toUpperCase() !== 'ACTIVE') return false;
  return new Date(evaluation.deadline) < new Date();
}

function normalizedStatus(evaluation) {
  const status = evaluation?.status?.toUpperCase() ?? 'ACTIVE';
  if (status === 'CLOSED' || status === 'ARCHIVED') return 'CLOSED';
  if (status === 'ACTIVE' && isOverdueEvaluation(evaluation)) return 'NEEDS_ATTENTION';
  if (status === 'ACTIVE') return 'ACTIVE';
  return 'NEEDS_ATTENTION';
}

function statusLabel(status, evaluation) {
  if (isOverdueEvaluation(evaluation)) return 'Needs Attention';
  if (!status) return 'Active';
  switch (status.toUpperCase()) {
    case 'ACTIVE':    return 'Active';
    case 'CLOSED':    return 'Closed';
    default:          return 'Needs Attention';
  }
}

function getFormInitials(title) {
  if (!title) return '??';
  const words = title.trim().split(/\s+/).filter(Boolean);
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
  return (words[0][0] + words[1][0]).toUpperCase();
}

function progressPct(progress) {
  if (!progress) return 0;
  const [a, b] = progress.split('/').map(Number);
  if (!b) return 0;
  return Math.round((a / b) * 100);
}

/* ── Icons ────────────────────────────────────────────────────────────── */
function IconPlus() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
      <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
    </svg>
  );
}
function IconEdit() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
    </svg>
  );
}
function IconTrash() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
      <path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
    </svg>
  );
}
function IconSearch() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
      <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
    </svg>
  );
}
function IconX() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
      <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
    </svg>
  );
}
function IconChevronRight() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round">
      <polyline points="9 18 15 12 9 6"/>
    </svg>
  );
}
function IconInfo() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
    </svg>
  );
}
function IconFile() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
      <polyline points="14 2 14 8 20 8"/>
    </svg>
  );
}
function IconCheck() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>
    </svg>
  );
}
function IconAlert() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
      <line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
    </svg>
  );
}
function IconLock() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
      <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
    </svg>
  );
}

function IconArchive() {
  return (
    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="21 8 21 21 3 21 3 8"/>
      <rect x="1" y="3" width="22" height="5"/>
      <line x1="10" y1="12" x2="14" y2="12"/>
    </svg>
  );
}

/* ── Archive Confirm Modal ────────────────────────────────────────────── */
function ArchiveModal({ evaluation, onClose, onArchived }) {
  const [archiving, setArchiving] = useState(false);
  const [error, setError] = useState('');

  const handleArchive = async () => {
    setArchiving(true);
    setError('');
    try {
      await archiveEvaluation(evaluation);
      onArchived(evaluation.id);
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Failed to archive evaluation.');
      setArchiving(false);
    }
  };

  return (
    <div className="fc-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="fc-modal fc-modal--sm">
        <div className="fc-delete-icon" style={{ background:'rgba(59,130,246,0.1)', border:'1px solid rgba(59,130,246,0.2)', color:'#60a5fa' }}>
          <IconArchive />
        </div>
        <div className="fc-delete-title">Archive this evaluation?</div>
        <div className="fc-delete-body">
          The form will be marked as Archived and hidden from the active list. Submitted responses are preserved.
        </div>
        {error && <div className="fc-modal__error">{error}</div>}
        <div className="fc-modal__foot fc-modal__foot--center">
          <button className="fc-btn fc-btn-ghost" type="button" onClick={onClose}>Cancel</button>
          <button className="fc-btn fc-btn-primary" type="button" onClick={handleArchive} disabled={archiving}>
            {archiving ? 'Archiving…' : 'Yes, Archive'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Delete Modal ─────────────────────────────────────────────────────── */
function DeleteModal({ evaluation, onClose, onDeleted }) {
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  const handleDelete = async () => {
    setDeleting(true);
    setError('');
    try {
      await deleteEvaluation(evaluation.id);
      onDeleted(evaluation.id);
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Failed to delete evaluation.');
      setDeleting(false);
    }
  };

  return (
    <div className="fc-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="fc-modal fc-modal--sm">
        <div className="fc-delete-icon">
          <IconTrash />
        </div>
        <div className="fc-delete-title">Delete this evaluation form?</div>
        <div className="fc-delete-body">
          This evaluation will be deleted. Submitted responses will be retained in evaluatees' results.
        </div>
        {error && <div className="fc-modal__error">{error}</div>}
        <div className="fc-modal__foot fc-modal__foot--center">
          <button className="fc-btn fc-btn-ghost" type="button" onClick={onClose}>Cancel</button>
          <button className="fc-btn fc-btn-danger" type="button" onClick={handleDelete} disabled={deleting}>
            {deleting ? 'Deleting…' : 'Yes, Delete'}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════════════
   FormsCreatedPage
   ══════════════════════════════════════════════════════════════════════ */
const TABS = ['All', 'Active', 'Needs Attention', 'Closed'];

export default function FormsCreatedPage() {
  const navigate = useNavigate();
  const [evaluations, setEvaluations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [activeTab, setActiveTab] = useState('All');
  const [archiveTarget, setArchiveTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [notice, setNotice] = useState('');
  const [showArchived, setShowArchived] = useState(false);

  useEffect(() => {
    let alive = true;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const data = await listCreatedEvaluations();
        if (alive) setEvaluations(data);
      } catch {
        if (alive) setError('Unable to load evaluations right now.');
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, []);

  /* ── Derived stats ──────────────────────────────────────────────────── */
  const total      = evaluations.length;
  const active     = evaluations.filter((e) => normalizedStatus(e) === 'ACTIVE').length;
  const attention  = evaluations.filter((e) => normalizedStatus(e) === 'NEEDS_ATTENTION').length;
  const closed     = evaluations.filter((e) => normalizedStatus(e) === 'CLOSED').length;
  const archived   = evaluations.filter((e) => e.status?.toUpperCase() === 'ARCHIVED').length;

  /* ── Filtered list ──────────────────────────────────────────────────── */
  const filtered = evaluations.filter((e) => {
    const status = normalizedStatus(e);
    const matchesTab =
      activeTab === 'All' ||
      (activeTab === 'Active' && status === 'ACTIVE') ||
      (activeTab === 'Closed' && status === 'CLOSED') ||
      (activeTab === 'Needs Attention' && status === 'NEEDS_ATTENTION');
    const matchesSearch = !search.trim() || e.title?.toLowerCase().includes(search.toLowerCase());
    const matchesArchive = showArchived ? (e.status?.toUpperCase() === 'ARCHIVED') : (e.status?.toUpperCase() !== 'ARCHIVED');
    return matchesTab && matchesSearch && matchesArchive;
  });

  /* ── Handlers ───────────────────────────────────────────────────────── */
  const handleArchived = (id) => {
    setEvaluations((prev) => prev.map((e) =>
      e.id === id ? { ...e, status: 'ARCHIVED' } : e
    ));
    setArchiveTarget(null);
  };

  const handleDeleted = (id) => {
    setEvaluations((prev) => prev.filter((e) => e.id !== id));
    setDeleteTarget(null);
  };

  const showNotice = (msg) => {
    setNotice(msg);
    setTimeout(() => setNotice(''), 3000);
  };

  const handleEditClick = (ev, variant) => {
    if (variant === 'closed') {
      showNotice('This evaluation is closed and cannot be edited.');
      return;
    }
    navigate(`/forms-created/${ev.id}/edit`, { state: { evaluation: ev } });
  };

  return (
    <div className="fc-page">
      {/* ── Page header ─────────────────────────────────────────────── */}
      <div className="fc-header">
        <div className="fc-header__left">
          <h1 className="fc-page-title">Forms Created</h1>
          <p className="fc-page-sub">Manage your evaluation forms as a Facilitator</p>
        </div>
        <div className="fc-header__right">
          <div className="fc-search-bar">
            <IconSearch />
            <input
              type="text"
              placeholder="Search forms…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <button
            className="fc-btn fc-btn-primary"
            type="button"
            onClick={() => navigate('/forms-created/new')}
          >
            <IconPlus /> New Form
          </button>
        </div>
      </div>
      {notice && (
        <div style={{ margin: '8px 0', padding: '8px 12px', background: '#fef3c7', color: '#92400e', borderRadius: 8 }}>
          {notice}
        </div>
      )}

      {/* ── Stats strip ─────────────────────────────────────────────── */}
      <div className="fc-stats">
        {loading ? (
          [1,2,3,4].map((i) => (
            <div key={i} className="fc-stat" style={{ pointerEvents:'none' }}>
              <Skeleton variant="rect" width="34px" height="34px" style={{ borderRadius:8, flexShrink:0 }} />
              <div style={{ display:'flex', flexDirection:'column', gap:4, flex:1 }}>
                <Skeleton variant="title" width="36px" height="22px" />
                <Skeleton variant="text" width="80px" height="10px" />
                <Skeleton variant="text" width="60px" height="9px" />
              </div>
            </div>
          ))
        ) : (
          <>
            <div className="fc-stat fc-stat--total">
              <div className="fc-stat__icon"><IconFile /></div>
              <div className="fc-stat__body">
                <div className="fc-stat__val">{total}</div>
                <div className="fc-stat__lbl">Total Forms</div>
                <div className="fc-stat__delta">All forms</div>
              </div>
            </div>
            <div className="fc-stat fc-stat--active">
              <div className="fc-stat__icon"><IconCheck /></div>
              <div className="fc-stat__body">
                <div className="fc-stat__val">{active}</div>
                <div className="fc-stat__lbl">Active</div>
                <div className="fc-stat__delta">Currently open</div>
              </div>
            </div>
            <div className="fc-stat fc-stat--warn">
              <div className="fc-stat__icon"><IconAlert /></div>
              <div className="fc-stat__body">
                <div className="fc-stat__val">{attention}</div>
                <div className="fc-stat__lbl">Needs Attention</div>
                <div className="fc-stat__delta">Review required</div>
              </div>
            </div>
            <div className="fc-stat fc-stat--closed">
              <div className="fc-stat__icon"><IconLock /></div>
              <div className="fc-stat__body">
                <div className="fc-stat__val">{closed}</div>
                <div className="fc-stat__lbl">Closed</div>
                <div className="fc-stat__delta">No longer active</div>
              </div>
            </div>
          </>
        )}
      </div>

      {/* ── Filter tabs ─────────────────────────────────────────────── */}
      <div className="fc-tabs-bar">
        <div className="fc-tabs">
          {TABS.map((tab) => (
            <button
              key={tab}
              type="button"
              className={`fc-tab${activeTab === tab ? ' active' : ''}`}
              onClick={() => setActiveTab(tab)}
            >
              {tab}
            </button>
          ))}
        </div>
        <button
          className={`fc-archives-btn${showArchived ? ' active' : ''}`}
          type="button"
          title="View archived forms"
          onClick={() => setShowArchived((s) => !s)}
        >
          Archives <span className="fc-archives-count">{archived}</span>
        </button>
      </div>

      {/* ── Card list ───────────────────────────────────────────────── */}
      <div className="fc-list">
        {loading && [1,2,3,4,5].map((i) => (
          <div key={i} className="fc-card fc-card--active" style={{ pointerEvents:'none' }}>
            <Skeleton variant="rect" width="38px" height="38px" style={{ borderRadius:9, flexShrink:0 }} />
            <div style={{ flex:1, display:'flex', flexDirection:'column', gap:6 }}>
              <Skeleton variant="text" width="45%" height="13px" />
              <Skeleton variant="text" width="65%" height="10px" />
              <div style={{ display:'flex', gap:10 }}>
                <Skeleton variant="text" width="80px" height="9px" />
              </div>
            </div>
            <div style={{ display:'flex', flexDirection:'column', gap:4, width:120 }}>
              <Skeleton variant="rect" width="100%" height="5px" style={{ borderRadius:3 }} />
              <Skeleton variant="text" width="40px" height="9px" style={{ marginLeft:'auto' }} />
            </div>
            <div style={{ display:'flex', gap:4 }}>
              {[1,2,3,4].map((j) => <Skeleton key={j} variant="circle" width="30px" height="30px" />)}
            </div>
          </div>
        ))}

        {!loading && error && <div className="fc-empty fc-empty--error">{error}</div>}

        {!loading && !error && filtered.length === 0 && (
          <div className="fc-empty">
            {search ? `No forms match "${search}".` : 'No evaluations found. Create your first one.'}
          </div>
        )}

        {!loading && !error && filtered.map((ev) => {
          const pct = progressPct(ev.submissionProgress);
          const statusKey = normalizedStatus(ev);
          const variant = statusVariant(statusKey);
          const isFull = pct === 100;
          const isOverdue = isOverdueEvaluation(ev);
          return (
            <div key={ev.id} className={`fc-card fc-card--${variant}`}>
              <div className="fc-card__initials">{getFormInitials(ev.title)}</div>
              <div className="fc-card__body">
                <div className="fc-card__top">
                  <span className="fc-card__title">{ev.title}</span>
                  <span className={`fc-status fc-status--${variant}`}>{statusLabel(ev.status, ev)}</span>
                </div>
                {ev.description && <div className="fc-card__desc">{ev.description}</div>}
                <div className="fc-card__meta">
                  <span className={`fc-card__meta-item${isOverdue ? ' fc-card__meta-item--overdue' : ''}`}>
                    <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                    {isOverdue ? 'Overdue · ' : ''}{formatDeadline(ev.deadline)}
                  </span>
                </div>
              </div>
              <div className="fc-card__progress">
                <div className="fc-card__progress-bar">
                  <div
                    className={`fc-card__progress-fill${isFull ? ' fc-card__progress-fill--full' : ''}`}
                    style={{ width: `${pct}%` }}
                  />
                </div>
                <span className="fc-card__progress-label">{ev.submissionProgress ?? '0/0'}</span>
              </div>
              <div className="fc-card__actions">
                <button className="fc-icon-btn" type="button" title="View results"
                  onClick={() => navigate(`/forms-created/${ev.id}/results`)}>
                  <IconChevronRight />
                </button>
                <button className="fc-icon-btn fc-icon-btn--archive" type="button" title="Archive"
                  onClick={() => setArchiveTarget(ev)}>
                  <IconArchive />
                </button>
                <button
                  className="fc-icon-btn fc-icon-btn--edit"
                  type="button"
                  title={variant === 'closed' ? 'Cannot edit closed evaluation' : 'Edit'}
                  onClick={() => handleEditClick(ev, variant)}
                  disabled={variant === 'closed'}
                >
                  <IconEdit />
                </button>
                <button className="fc-icon-btn fc-icon-btn--danger" type="button" title="Delete"
                  onClick={() => setDeleteTarget(ev)}>
                  <IconTrash />
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* ── Modals ──────────────────────────────────────────────────── */}
      {archiveTarget && (
        <ArchiveModal
          evaluation={archiveTarget}
          onClose={() => setArchiveTarget(null)}
          onArchived={handleArchived}
        />
      )}
      {deleteTarget && (
        <DeleteModal
          evaluation={deleteTarget}
          onClose={() => setDeleteTarget(null)}
          onDeleted={handleDeleted}
        />
      )}
    </div>
  );
}
