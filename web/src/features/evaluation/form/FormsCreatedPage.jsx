import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  listCreatedEvaluations,
  updateEvaluation,
  deleteEvaluation,
} from '../service/evaluationService';
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
    case 'DRAFT':     return 'draft';
    case 'PENDING':   return 'pending';
    case 'EXPIRED':   return 'expired';
    case 'ARCHIVED':  return 'archived';
    default:          return 'attention';
  }
}

function statusLabel(status) {
  if (!status) return 'Active';
  switch (status.toUpperCase()) {
    case 'ACTIVE':    return 'Active';
    case 'CLOSED':    return 'Closed';
    case 'DRAFT':     return 'Draft';
    case 'PENDING':   return 'Pending';
    case 'EXPIRED':   return 'Expired';
    case 'ARCHIVED':  return 'Archived';
    default:          return 'Needs Attention';
  }
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

/* ── Edit Modal ───────────────────────────────────────────────────────── */
function EditModal({ evaluation, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: evaluation.title ?? '',
    description: evaluation.description ?? '',
    deadline: toDatetimeLocal(evaluation.deadline),
    status: evaluation.status ?? 'ACTIVE',
  });
  const [evaluatorSearch, setEvaluatorSearch] = useState('');
  const [evaluateeSearch, setEvaluateeSearch] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const update = (field) => (e) => setForm((p) => ({ ...p, [field]: e.target.value }));

  const handleSave = async () => {
    if (!form.title.trim() || !form.description.trim() || !form.deadline) {
      setError('Title, description, and deadline are required.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const updated = await updateEvaluation(evaluation.id, {
        title: form.title,
        description: form.description,
        deadline: form.deadline.replace('T', 'T').slice(0, 16) + ':00',
        status: form.status,
      });
      onSaved(updated ?? { ...evaluation, ...form });
    } catch (err) {
      setError(err.response?.data?.error?.message || 'Failed to update evaluation.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fc-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="fc-modal">
        <div className="fc-modal__head">
          <div>
            <div className="fc-modal__title">Edit Evaluation</div>
            <div className="fc-modal__sub">Update form details and participants</div>
          </div>
          <button className="fc-modal__close" type="button" onClick={onClose}><IconX /></button>
        </div>

        <div className="fc-modal__body">
          {error && <div className="fc-modal__error">{error}</div>}

          <div className="fc-modal__section-label">Evaluation Details</div>

          <div className="fc-form-row">
            <div className="fc-form-group">
              <label className="fc-label">Evaluation Title <span className="fc-req">*</span></label>
              <input className="fc-input" type="text" value={form.title} onChange={update('title')} />
            </div>
            <div className="fc-form-group">
              <label className="fc-label">Deadline <span className="fc-req">*</span></label>
              <input className="fc-input" type="datetime-local" value={form.deadline} onChange={update('deadline')} />
            </div>
          </div>

          <div className="fc-form-group" style={{ marginBottom: 14 }}>
            <label className="fc-label">Description <span className="fc-req">*</span></label>
            <textarea className="fc-input fc-textarea" value={form.description} onChange={update('description')} />
          </div>

          <div className="fc-rule-note">
            <IconInfo />
            <p><strong>Assignment rules:</strong> A user cannot be both evaluator and evaluatee in the same evaluation.</p>
          </div>

          <div className="fc-modal__section-label">Assign Participants</div>
          <div className="fc-people-grid">
            <div className="fc-people-box">
              <div className="fc-label">Evaluators <span className="fc-req">*</span></div>
              <div className="fc-search-wrap">
                <IconSearch />
                <input type="text" placeholder="Search by name or email…"
                  value={evaluatorSearch} onChange={(e) => setEvaluatorSearch(e.target.value)} />
              </div>
            </div>
            <div className="fc-people-box">
              <div className="fc-label">Evaluatees <span className="fc-req">*</span></div>
              <div className="fc-search-wrap">
                <IconSearch />
                <input type="text" placeholder="Search by name or email…"
                  value={evaluateeSearch} onChange={(e) => setEvaluateeSearch(e.target.value)} />
              </div>
            </div>
          </div>
        </div>

        <div className="fc-modal__foot">
          <button className="fc-btn fc-btn-ghost" type="button" onClick={onClose}>Cancel</button>
          <button className="fc-btn fc-btn-primary" type="button" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving…' : 'Update Evaluation'}
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
          This form will be permanently deleted. Submitted responses will be retained in evaluatees' results and will not be lost.
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
  const [editTarget, setEditTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

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
  const active     = evaluations.filter((e) => e.status?.toUpperCase() === 'ACTIVE').length;
  const attention  = evaluations.filter((e) => e.status?.toUpperCase() !== 'ACTIVE' && e.status?.toUpperCase() !== 'CLOSED').length;
  const closed     = evaluations.filter((e) => e.status?.toUpperCase() === 'CLOSED').length;

  /* ── Filtered list ──────────────────────────────────────────────────── */
  const filtered = evaluations.filter((e) => {
    const matchesTab =
      activeTab === 'All' ||
      (activeTab === 'Active' && e.status?.toUpperCase() === 'ACTIVE') ||
      (activeTab === 'Closed' && e.status?.toUpperCase() === 'CLOSED') ||
      (activeTab === 'Needs Attention' && e.status?.toUpperCase() !== 'ACTIVE' && e.status?.toUpperCase() !== 'CLOSED');
    const matchesSearch = !search.trim() || e.title?.toLowerCase().includes(search.toLowerCase());
    return matchesTab && matchesSearch;
  });

  /* ── Handlers ───────────────────────────────────────────────────────── */
  const handleSaved = (updated) => {
    setEvaluations((prev) => prev.map((e) => (e.id === updated.id ? { ...e, ...updated } : e)));
    setEditTarget(null);
  };

  const handleDeleted = (id) => {
    setEvaluations((prev) => prev.filter((e) => e.id !== id));
    setDeleteTarget(null);
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

      {/* ── Stats strip ─────────────────────────────────────────────── */}
      <div className="fc-stats">
        <div className="fc-stat fc-stat--total">
          <div className="fc-stat__icon"><IconFile /></div>
          <div className="fc-stat__val">{total}</div>
          <div className="fc-stat__lbl">Total Forms</div>
        </div>
        <div className="fc-stat fc-stat--active">
          <div className="fc-stat__icon"><IconCheck /></div>
          <div className="fc-stat__val">{active}</div>
          <div className="fc-stat__lbl">Active</div>
        </div>
        <div className="fc-stat fc-stat--warn">
          <div className="fc-stat__icon"><IconAlert /></div>
          <div className="fc-stat__val">{attention}</div>
          <div className="fc-stat__lbl">Needs Attention</div>
        </div>
        <div className="fc-stat fc-stat--closed">
          <div className="fc-stat__icon"><IconLock /></div>
          <div className="fc-stat__val">{closed}</div>
          <div className="fc-stat__lbl">Closed</div>
        </div>
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
        <button className="fc-archives-btn" type="button">Archives</button>
      </div>

      {/* ── Table ───────────────────────────────────────────────────── */}
      <div className="fc-table-wrap">
        {loading && <div className="fc-empty">Loading evaluations…</div>}
        {!loading && error && <div className="fc-empty fc-empty--error">{error}</div>}
        {!loading && !error && filtered.length === 0 && (
          <div className="fc-empty">
            {search ? `No forms match "${search}".` : 'No evaluations found. Create your first one.'}
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <table className="fc-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Status</th>
                <th>Progress</th>
                <th>Deadline</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((ev) => {
                const pct = progressPct(ev.submissionProgress);
                const variant = statusVariant(ev.status);
                const isFull = pct === 100;
                const isOverdue = ev.deadline && new Date(ev.deadline) < new Date();
                return (
                  <tr key={ev.id} className="fc-row">
                    <td className="fc-row__title">
                      <div className="fc-row__title-wrap">
                        <span className={`fc-row__title-dot fc-row__title-dot--${variant}`} />
                        <div>
                          <div className="fc-row__name">{ev.title}</div>
                          {ev.description && (
                            <div className="fc-row__desc">{ev.description}</div>
                          )}
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={`fc-status fc-status--${variant}`}>
                        {statusLabel(ev.status)}
                      </span>
                    </td>
                    <td className="fc-row__progress">
                      <div className="fc-progress-bar">
                        <div
                          className={`fc-progress-fill${isFull ? ' fc-progress-fill--full' : ''}`}
                          style={{ width: `${pct}%` }}
                        />
                      </div>
                      <span className="fc-progress-label">{ev.submissionProgress ?? '0/0'}</span>
                    </td>
                    <td className={`fc-row__deadline${isOverdue ? ' fc-row__deadline--overdue' : ''}`}>
                      {formatDeadline(ev.deadline)}
                    </td>
                    <td className="fc-row__actions">
                      <button
                        className="fc-icon-btn"
                        type="button"
                        title="View results"
                        onClick={() => navigate(`/forms-created/${ev.id}/results`)}
                      >
                        <IconChevronRight />
                      </button>
                      <button
                        className="fc-icon-btn fc-icon-btn--archive"
                        type="button"
                        title="Archive"
                      >
                        <IconArchive />
                      </button>
                      <button
                        className="fc-icon-btn fc-icon-btn--edit"
                        type="button"
                        title="Edit"
                        onClick={() => setEditTarget(ev)}
                      >
                        <IconEdit />
                      </button>
                      <button
                        className="fc-icon-btn fc-icon-btn--danger"
                        type="button"
                        title="Delete"
                        onClick={() => setDeleteTarget(ev)}
                      >
                        <IconTrash />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* ── Modals ──────────────────────────────────────────────────── */}
      {editTarget && (
        <EditModal
          evaluation={editTarget}
          onClose={() => setEditTarget(null)}
          onSaved={handleSaved}
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
