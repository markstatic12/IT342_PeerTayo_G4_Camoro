import { useEffect, useRef, useState, useCallback } from 'react';
import { useNavigate, useLocation, useParams } from 'react-router-dom';
import { useAuth } from '../../auth/context/AuthContext';
import { createEvaluation, updateEvaluation, getEvaluationParticipants, getEvaluation } from './evaluationFormService';
import { searchUsers } from '../../user/search/userService';
import { useNavigationGuard } from '../../../shared/context/NavigationGuardContext';
import { Toast } from '../../../shared/components/ui';
import ExitConfirmModal from '../../../shared/components/ui/ExitConfirmModal';

import './CreateEvaluationPage.css';

/* ── Criteria data ────────────────────────────────────────────────────── */
const CRITERIA = [
  { label: 'Quality of Work',             desc: 'Produces accurate, thorough, and well-organized outputs that meet or exceed expectations.' },
  { label: 'Reliability & Dependability', desc: 'Consistently delivers on commitments and can be counted on to follow through on tasks.' },
  { label: 'Collaboration & Teamwork',    desc: 'Works effectively with others and contributes constructively to group efforts.' },
  { label: 'Communication Skills',        desc: 'Expresses ideas clearly, listens actively, and communicates updates in a timely manner.' },
  { label: 'Initiative & Proactiveness',  desc: 'Identifies and acts on opportunities without being prompted; goes beyond minimum requirements.' },
  { label: 'Problem Solving',             desc: 'Approaches challenges analytically and proposes practical, effective solutions.' },
  { label: 'Professionalism & Conduct',   desc: 'Maintains a respectful, ethical, and positive demeanor in all interactions.' },
  { label: 'Time Management',             desc: 'Prioritizes tasks effectively, meets deadlines, and manages workload without compromising quality.' },
  { label: 'Adaptability & Learning',     desc: 'Responds positively to change, accepts feedback constructively, and continuously improves.' },
  { label: 'Overall Contribution',        desc: "Holistic assessment of the individual's net positive impact on the team or group outcome." },
];

const MONTHS = ['January','February','March','April','May','June','July','August','September','October','November','December'];
const DAYS   = ['Su','Mo','Tu','We','Th','Fr','Sa'];
const HOURS12 = Array.from({ length: 12 }, (_, i) => String(i + 1).padStart(2, '0')); // 01–12
const MINS    = Array.from({ length: 60 }, (_, i) => String(i).padStart(2, '0'));

/* convert 24h hour string → { h12, ampm } */
function to12(h24) {
  const n = parseInt(h24, 10);
  const ampm = n < 12 ? 'AM' : 'PM';
  const h = n % 12 === 0 ? 12 : n % 12;
  return { h12: String(h).padStart(2, '0'), ampm };
}

/* convert 12h + ampm → 24h hour string */
function to24(h12, ampm) {
  let n = parseInt(h12, 10);
  if (ampm === 'AM') { if (n === 12) n = 0; }
  else               { if (n !== 12) n += 12; }
  return String(n).padStart(2, '0');
}

/* ── Inline SVGs ──────────────────────────────────────────────────────── */
const IcoSearch  = () => <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>;
const IcoX       = () => <svg viewBox="0 0 24 24" strokeWidth="2.5" stroke="currentColor" fill="none"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>;
const IcoBack    = () => <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none"><polyline points="15 18 9 12 15 6"/></svg>;
const IcoNext    = () => <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none"><polyline points="9 18 15 12 9 6"/></svg>;
const IcoInfo    = () => <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>;
const IcoChevL   = () => <svg viewBox="0 0 24 24" strokeWidth="2.5" stroke="currentColor" fill="none"><polyline points="15 18 9 12 15 6"/></svg>;
const IcoChevR   = () => <svg viewBox="0 0 24 24" strokeWidth="2.5" stroke="currentColor" fill="none"><polyline points="9 18 15 12 9 6"/></svg>;

/* ══════════════════════════════════════════════════════════════════════
   DeadlinePicker — tabbed: Date tab (calendar) | Time tab (scroll columns)
   value: "YYYY-MM-DDTHH:mm" string
   ══════════════════════════════════════════════════════════════════════ */
function DeadlinePicker({ value, onChange, hasError }) {
  const parsed = value ? new Date(value) : null;

  const initYear  = parsed ? parsed.getFullYear()  : new Date().getFullYear();
  const initMonth = parsed ? parsed.getMonth()      : new Date().getMonth();
  const { h12: initH12, ampm: initAmpm } = to12(
    parsed ? String(parsed.getHours()).padStart(2,'0') : '23'
  );
  const initMin = parsed ? String(parsed.getMinutes()).padStart(2,'0') : '59';

  const [dpTab,     setDpTab]     = useState('date');
  const [viewYear,  setViewYear]  = useState(initYear);
  const [viewMonth, setViewMonth] = useState(initMonth);
  const [selDate,   setSelDate]   = useState(parsed);
  const [selH12,    setSelH12]    = useState(initH12);
  const [selAmpm,   setSelAmpm]   = useState(initAmpm);
  const [selMin,    setSelMin]    = useState(initMin);

  const hourRef = useRef(null);
  const minRef  = useRef(null);

  /* scroll time lists to selected item when Time tab opens */
  useEffect(() => {
    if (dpTab !== 'time') return;
    const scrollTo = (ref, val, arr) => {
      if (!ref.current) return;
      const idx = arr.indexOf(val);
      if (idx < 0) return;
      ref.current.scrollTop = idx * 36 - 36 * 2;
    };
    setTimeout(() => {
      scrollTo(hourRef, selH12, HOURS12);
      scrollTo(minRef,  selMin, MINS);
    }, 30);
  }, [dpTab]); // eslint-disable-line react-hooks/exhaustive-deps

  const emit = useCallback((date, h12, ampm, m) => {
    if (!date) return;
    const y   = date.getFullYear();
    const mo  = String(date.getMonth() + 1).padStart(2, '0');
    const d   = String(date.getDate()).padStart(2, '0');
    const h24 = to24(h12, ampm);
    onChange(`${y}-${mo}-${d}T${h24}:${m}`);
  }, [onChange]);

  const selectDay = (day) => {
    const d = new Date(viewYear, viewMonth, day);
    setSelDate(d);
    emit(d, selH12, selAmpm, selMin);
  };

  const pickHour = (h) => { setSelH12(h);   emit(selDate, h,      selAmpm, selMin); };
  const pickMin  = (m) => { setSelMin(m);   emit(selDate, selH12, selAmpm, m); };
  const pickAmpm = (a) => { setSelAmpm(a);  emit(selDate, selH12, a,       selMin); };

  const prevMonth = () => {
    if (viewMonth === 0) { setViewMonth(11); setViewYear(y => y - 1); }
    else setViewMonth(m => m - 1);
  };
  const nextMonth = () => {
    if (viewMonth === 11) { setViewMonth(0); setViewYear(y => y + 1); }
    else setViewMonth(m => m + 1);
  };

  /* calendar grid */
  const firstDow    = new Date(viewYear, viewMonth, 1).getDay();
  const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();
  const cells = [
    ...Array(firstDow).fill(null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];

  const isSelected = (d) =>
    selDate &&
    selDate.getFullYear() === viewYear &&
    selDate.getMonth()    === viewMonth &&
    selDate.getDate()     === d;

  const isPast = (d) => {
    const t = new Date(viewYear, viewMonth, d);
    t.setHours(0, 0, 0, 0);
    const now = new Date(); now.setHours(0, 0, 0, 0);
    return t < now;
  };

  const dateLabel = selDate
    ? `${MONTHS[selDate.getMonth()]} ${selDate.getDate()}, ${selDate.getFullYear()}`
    : 'Pick a date';
  const timeLabel = `${selH12} : ${selMin} ${selAmpm}`;

  return (
    <div className={`dp-wrap${hasError ? ' dp-wrap--error' : ''}`}>

      {/* ── Summary display bar ── */}
      <div className="dp-display">
        <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none" width="13" height="13">
          <rect x="3" y="4" width="18" height="18" rx="2"/>
          <line x1="16" y1="2" x2="16" y2="6"/>
          <line x1="8"  y1="2" x2="8"  y2="6"/>
          <line x1="3"  y1="10" x2="21" y2="10"/>
        </svg>
        {selDate
          ? <span className="dp-display-val">{dateLabel}&nbsp;&nbsp;·&nbsp;&nbsp;{timeLabel}</span>
          : <span className="dp-display-placeholder">Select a date and time</span>
        }
      </div>

      {/* ── Picker body ── */}
      <div className="dp-body">

        {/* inner tab bar */}
        <div className="dp-inner-tabs">
          <button
            type="button"
            className={`dp-inner-tab${dpTab === 'date' ? ' dp-inner-tab--active' : ''}`}
            onClick={() => setDpTab('date')}
          >
            <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none" width="11" height="11">
              <rect x="3" y="4" width="18" height="18" rx="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8"  y1="2" x2="8"  y2="6"/>
              <line x1="3"  y1="10" x2="21" y2="10"/>
            </svg>
            Date
          </button>
          <button
            type="button"
            className={`dp-inner-tab${dpTab === 'time' ? ' dp-inner-tab--active' : ''}`}
            onClick={() => setDpTab('time')}
          >
            <svg viewBox="0 0 24 24" strokeWidth="2" stroke="currentColor" fill="none" width="11" height="11">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12 6 12 12 16 14"/>
            </svg>
            Time
          </button>
        </div>

        {/* ── DATE tab ── */}
        {dpTab === 'date' && (
          <div className="dp-cal">
            <div className="dp-cal-nav">
              <button type="button" className="dp-nav-btn" onClick={prevMonth}><IcoChevL /></button>
              <span className="dp-cal-month">{MONTHS[viewMonth]} {viewYear}</span>
              <button type="button" className="dp-nav-btn" onClick={nextMonth}><IcoChevR /></button>
            </div>
            <div className="dp-cal-grid">
              {DAYS.map(d => <div key={d} className="dp-cal-dow">{d}</div>)}
              {cells.map((day, i) => (
                <button
                  key={i}
                  type="button"
                  className={[
                    'dp-cal-day',
                    !day                       ? 'dp-cal-day--empty' : '',
                    day && isSelected(day)     ? 'dp-cal-day--sel'   : '',
                    day && isPast(day)         ? 'dp-cal-day--past'  : '',
                  ].filter(Boolean).join(' ')}
                  disabled={!day || isPast(day)}
                  onClick={() => day && !isPast(day) && selectDay(day)}
                >
                  {day ?? ''}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* ── TIME tab ── */}
        {dpTab === 'time' && (
          <div className="dp-time">
            <div className="dp-time-cols">

              <div className="dp-time-col">
                <div className="dp-time-col-head">Hour</div>
                <div className="dp-scroll" ref={hourRef}>
                  {HOURS12.map(h => (
                    <button key={h} type="button"
                      className={`dp-time-item${h === selH12 ? ' dp-time-item--sel' : ''}`}
                      onClick={() => pickHour(h)}>
                      {h}
                    </button>
                  ))}
                </div>
              </div>

              <div className="dp-time-sep">:</div>

              <div className="dp-time-col">
                <div className="dp-time-col-head">Min</div>
                <div className="dp-scroll" ref={minRef}>
                  {MINS.map(m => (
                    <button key={m} type="button"
                      className={`dp-time-item${m === selMin ? ' dp-time-item--sel' : ''}`}
                      onClick={() => pickMin(m)}>
                      {m}
                    </button>
                  ))}
                </div>
              </div>

              <div className="dp-ampm-col">
                <div className="dp-time-col-head">—</div>
                {['AM', 'PM'].map(a => (
                  <button key={a} type="button"
                    className={`dp-ampm-item${a === selAmpm ? ' dp-ampm-item--sel' : ''}`}
                    onClick={() => pickAmpm(a)}>
                    {a}
                  </button>
                ))}
              </div>

            </div>
          </div>
        )}

      </div>
    </div>
  );
}

/* ── Participant search box — fixed search + inline results + chips ───── */
function ParticipantBox({ label, color, selectedIds, options, search, onSearch, onAdd, onRemove, usersById, error, loading }) {
  const inputRef = useRef(null);

  const q = search.trim().toLowerCase();

  const filtered = q
    ? options
        .filter((u) => !selectedIds.includes(u.id))
        .filter((u) =>
          u.firstName?.toLowerCase().startsWith(q) ||
          u.lastName?.toLowerCase().startsWith(q) ||
          u.email?.toLowerCase().startsWith(q)
        )
        .slice(0, 20)
    : [];

  const showSkeleton = loading && q.length > 0;
  const showResults  = !showSkeleton && filtered.length > 0;
  const showEmpty    = !showSkeleton && !showResults && q.length > 0;

  return (
    <div className="pb-wrap">
      <div className="pb-label">{label} <span className="req">*</span></div>

      {/* search bar with dropdown anchored directly to it */}
      <div className="pb-search-wrap">
        <div className={`pb-search-bar${error ? ' pb-search-bar--error' : ''}`}
          onClick={() => inputRef.current?.focus()}>
          <IcoSearch />
          <input
            ref={inputRef}
            type="text"
            placeholder="Search by name or email…"
            value={search}
            onChange={(e) => onSearch(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') {
                e.preventDefault();
                const first = filtered[0];
                if (first) { onAdd(first.id); onSearch(''); }
              }
            }}
          />
          {search && (
            <button type="button" className="pb-clear" onClick={() => onSearch('')}>
              <IcoX />
            </button>
          )}
        </div>

        {/* floating dropdown — anchored to search bar */}
        {(showSkeleton || showResults || showEmpty) && (
          <div className="pb-dropdown">
            {showSkeleton ? (
              [1, 2, 3, 4].map((i) => (
                <div key={i} className="pb-option pb-option--skeleton">
                  <div className="pb-skel-avatar" />
                  <div className="pb-skel-text">
                    <div className="pb-skel-name" />
                    <div className="pb-skel-email" />
                  </div>
                </div>
              ))
            ) : showResults ? filtered.map((u) => (
              <button key={u.id} type="button" className="pb-option"
                onMouseDown={(e) => { e.preventDefault(); onAdd(u.id); onSearch(''); }}>
                <span className="pb-option-avatar">{u.firstName?.[0]}{u.lastName?.[0]}</span>
                <span className="pb-option-name">{u.firstName} {u.lastName}</span>
                <span className="pb-option-email">{u.email}</span>
              </button>
            )) : (
              <div className="pb-results-empty">No users found</div>
            )}
          </div>
        )}
      </div>

      {/* selected users — card that fills remaining space, one per row */}
      <div className="pb-chips">
        {selectedIds.length === 0 ? (
          <div className="pb-chips-empty">No participants selected yet</div>
        ) : (
          selectedIds.map((id, idx) => {
            const u = usersById[id];
            if (!u) return null;
            return (
              <div key={id} className="pb-chip-row">
                <span className={`pb-chip-num pb-chip-num--${color}`}>{idx + 1}</span>
                <span className="pb-chip-name">{u.firstName} {u.lastName}</span>
                <span className="pb-chip-email">{u.email}</span>
                <button type="button" className="pb-chip-x" onClick={() => onRemove(id)}>
                  <IcoX />
                </button>
              </div>
            );
          })
        )}
      </div>

      {error && <div className="field-error">{error}</div>}
    </div>
  );
}

/* ══════════════════════════════════════════════════════════════════════ */
export default function CreateEvaluationPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { id: editId } = useParams();
  const { user: currentUser } = useAuth();
  const { registerGuard, clearGuard } = useNavigationGuard();

  // Edit mode: evaluation may be passed via route state or loaded by id
  const routeEvaluation = location.state?.evaluation ?? null;
  const isEditMode = !!editId;
  const [editEvaluation, setEditEvaluation] = useState(routeEvaluation);

  const [tab, setTab] = useState(1);
  const [form, setForm] = useState({
    title: editEvaluation?.title ?? '',
    deadline: editEvaluation?.deadline
      ? (() => {
          const d = new Date(editEvaluation.deadline);
          const pad = (n) => String(n).padStart(2, '0');
          return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
        })()
      : '',
    description: editEvaluation?.description ?? '',
  });
  const [errors, setErrors] = useState({});
  const [exitModal, setExitModal] = useState({ open: false, path: '' });

  const [usersById,        setUsersById]        = useState({});
  const [allUsers,         setAllUsers]         = useState([]);
  const [evaluatorIds,     setEvaluatorIds]     = useState([]);
  const [evaluateeIds,     setEvaluateeIds]     = useState([]);
  const [evaluatorSearch,  setEvaluatorSearch]  = useState('');
  const [evaluateeSearch,  setEvaluateeSearch]  = useState('');
  const [loadingUsers,     setLoadingUsers]     = useState(false);
  const [saving,           setSaving]           = useState(false);
  const [apiError,         setApiError]         = useState('');
  const [submitted,        setSubmitted]        = useState(false);
  const [showRoleToast,    setShowRoleToast]    = useState(false);


  /* dirty = user has entered anything */
  const isDirty = !!(form.title || form.deadline || form.description ||
    evaluatorIds.length > 0 || evaluateeIds.length > 0);

  /* Register/clear the global navigation guard based on dirty state */
  useEffect(() => {
    if (isDirty) {
      registerGuard({
        title: 'Discard this evaluation?',
        body: "You've started filling in details. Leaving now will discard everything and you'll need to start over.",
      });
    } else {
      clearGuard();
    }
    return () => clearGuard();
  }, [isDirty, registerGuard, clearGuard]);

  /* Block browser back/forward */
  useEffect(() => {
    if (!isDirty) return;
    const handlePop = (e) => {
      window.history.pushState(null, '', window.location.href);
      setExitModal({ open: true, path: -1 });
    };
    window.history.pushState(null, '', window.location.href);
    window.addEventListener('popstate', handlePop);
    return () => window.removeEventListener('popstate', handlePop);
  }, [isDirty]);

  /* Block tab close / refresh */
  useEffect(() => {
    if (!isDirty) return;
    const handler = (e) => { e.preventDefault(); e.returnValue = ''; };
    window.addEventListener('beforeunload', handler);
    return () => window.removeEventListener('beforeunload', handler);
  }, [isDirty]);

  const guardedNavigate = useCallback((path) => {
    if (isDirty) {
      setExitModal({ open: true, path });
    } else {
      navigate(path);
    }
  }, [isDirty, navigate]);

  /* load all users once on mount */
  useEffect(() => {
    let alive = true;
    (async () => {
      setLoadingUsers(true);
      try {
        const data = await searchUsers('');
        if (!alive) return;
        setUsersById(Object.fromEntries(data.map((u) => [u.id, u])));
        setAllUsers(data);
      } catch { /* ignore */ }
      finally { if (alive) setLoadingUsers(false); }
    })();
    return () => { alive = false; };
  }, [currentUser?.id]);

  /* in edit mode — pre-populate evaluator/evaluatee chips from backend */
  useEffect(() => {
    if (!isEditMode) return;
    let alive = true;
    (async () => {
      try {
        // If evaluation data wasn't provided via route state, fetch it
        if (!editEvaluation) {
          const ev = await getEvaluation(editId);
          if (!alive) return;
          setEditEvaluation(ev);
          // populate main fields from fetched evaluation
          setForm((p) => ({
            ...p,
            title: ev?.title ?? p.title,
            description: ev?.description ?? p.description,
            deadline: ev?.deadline ? (() => {
              const d = new Date(ev.deadline);
              const pad = (n) => String(n).padStart(2, '0');
              return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
            })() : p.deadline,
          }));
        }

        const participants = await getEvaluationParticipants(editId);
        if (!alive) return;
        const evrs = participants?.evaluators ?? [];
        const eves = participants?.evaluatees ?? [];
        // merge into usersById so chips can resolve names
        setUsersById((prev) => {
          const merged = { ...prev };
          [...evrs, ...eves].forEach((u) => { merged[u.id] = u; });
          return merged;
        });
        setEvaluatorIds(evrs.map((u) => u.id));
        setEvaluateeIds(eves.map((u) => u.id));
      } catch { /* ignore — chips stay empty */ }
    })();
    return () => { alive = false; };
  }, [isEditMode, editId]); // eslint-disable-line react-hooks/exhaustive-deps

  const updateField = (field) => (e) => {
    setForm((p) => ({ ...p, [field]: e.target.value }));
    setErrors((p) => ({ ...p, [field]: '' }));
  };

  const handleNext = () => {
    const errs = {};
    if (!form.title.trim())       errs.title       = 'Evaluation title is required.';
    if (!form.description.trim()) errs.description = 'Description is required.';
    if (!form.deadline)           errs.deadline    = 'Please select a deadline date and time.';
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }
    setErrors({});
    setTab(2);
  };

  const handleSubmit = async () => {
    const errs = {};
    if (evaluatorIds.length === 0) errs.evaluators = 'Add at least one evaluator.';
    if (evaluateeIds.length === 0) errs.evaluatees = 'Add at least one evaluatee.';
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }
    setSaving(true);
    setApiError('');
    try {
      if (isEditMode) {
        await updateEvaluation(editId, {
          title: form.title,
          description: form.description,
          deadline: form.deadline,
          evaluatorIds,
          evaluateeIds,
        });
      } else {
        const res = await createEvaluation({
          title: form.title,
          description: form.description,
          deadline: form.deadline,
          evaluatorIds,
          evaluateeIds,
        });
        if (res?.roleUpgraded) {
          setShowRoleToast(true);
        }
      }
      clearGuard();
      setSubmitted(true);
      setTimeout(() => navigate('/forms-created'), 2000);
    } catch (err) {
      const msg = err.response?.data?.error?.message
        || (err.response?.status === 500 ? 'Server error — please restart the backend and try again.' : isEditMode ? 'Failed to update evaluation.' : 'Failed to create evaluation.');
      setApiError(msg);
    } finally {
      setSaving(false);
    }
  };

  const tab1HasError = !!(errors.title || errors.description || errors.deadline);
  const tab2HasError = !!(errors.evaluators || errors.evaluatees);

  /* ── Success screen ── */
  if (submitted) {
    return (
      <div className="ce-success-screen">
        <div className="ce-success-card">
          <div className="ce-success-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"
              strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div className="ce-success-title">{isEditMode ? 'Evaluation Updated!' : 'Evaluation Created!'}</div>
          <div className="ce-success-sub">{isEditMode ? 'Your changes have been saved successfully.' : 'Your evaluation form has been saved and participants have been notified.'}</div>
        </div>
        {showRoleToast && (
          <Toast 
            message="Account upgraded! You are now a FACILITATOR." 
            onDismiss={() => setShowRoleToast(false)}
            duration={5000}
          />
        )}
      </div>
    );
  }

  return (
    <div className="ce-page animate-page">

      <div className="ce-header">
        <div className="ce-breadcrumb">
          Dashboard / Forms Created / <span>{isEditMode ? 'Edit Evaluation' : 'Create Evaluation'}</span>
        </div>
        <div className="ce-title">{isEditMode ? 'Edit Evaluation Form' : 'Create Evaluation Form'}</div>
        <div className="ce-sub">{isEditMode ? 'Update the details and participants for this evaluation.' : 'Fill in the details and assign participants to launch a peer evaluation.'}</div>
      </div>

      <div className="ce-shell">

        {/* ══ LEFT — tabbed form ══ */}
        <div className="ce-main">

          <div className="ce-tabs">
            <button
              type="button"
              className={`ce-tab${tab === 1 ? ' ce-tab--active' : ''}`}
              onClick={() => setTab(1)}
            >
              <span className={`ce-tab-num${tab === 1 ? ' ce-tab-num--active' : tab > 1 ? ' ce-tab-num--done' : ''}`}>
                {tab > 1 ? '✓' : '1'}
              </span>
              Evaluation Details
              {tab1HasError && <span className="ce-tab-dot" />}
            </button>
            <button
              type="button"
              className={`ce-tab${tab === 2 ? ' ce-tab--active' : ''}`}
              onClick={() => tab === 1 ? handleNext() : setTab(2)}
            >
              <span className={`ce-tab-num${tab === 2 ? ' ce-tab-num--active' : ''}`}>2</span>
              Assign Participants
              {tab2HasError && <span className="ce-tab-dot" />}
            </button>
          </div>

          <div className="ce-tab-body">

            {/* ── TAB 1 ── */}
            {tab === 1 && (
              <div className="ce-tab-panel ce-tab-panel--details">

                {/* Left sub-column: title + description */}
                <div className="ce-details-left">
                  <div className="ce-field">
                    <label className="ce-label">Evaluation Title <span className="req">*</span></label>
                    <input
                      className={`ce-input${errors.title ? ' ce-input--error' : ''}`}
                      type="text"
                      placeholder="e.g. Q1 2026 Team Performance Review"
                      value={form.title}
                      onChange={updateField('title')}
                    />
                    {errors.title && <div className="field-error">{errors.title}</div>}
                  </div>

                  <div className="ce-field ce-field--grow">
                    <label className="ce-label">Description <span className="req">*</span></label>
                    <textarea
                      className={`ce-input ce-textarea${errors.description ? ' ce-input--error' : ''}`}
                      placeholder="Briefly describe the purpose or context of this evaluation…"
                      value={form.description}
                      onChange={updateField('description')}
                    />
                    {errors.description && <div className="field-error">{errors.description}</div>}
                  </div>
                </div>

                {/* Right sub-column: deadline picker */}
                <div className="ce-details-right">
                  <div className="ce-field ce-field--grow">
                    <label className="ce-label">Deadline <span className="req">*</span></label>
                    <DeadlinePicker
                      value={form.deadline}
                      onChange={(v) => {
                        setForm((p) => ({ ...p, deadline: v }));
                        setErrors((p) => ({ ...p, deadline: '' }));
                      }}
                      hasError={!!errors.deadline}
                    />
                    {errors.deadline && <div className="field-error">{errors.deadline}</div>}
                  </div>
                </div>

              </div>
            )}

            {/* ── TAB 2 ── */}
            {tab === 2 && (
              <div className="ce-tab-panel">
                <div className="ce-rule-note">
                  <IcoInfo />
                  <p>
                    <strong>Self-Evaluation Restriction:</strong> A user can be both an Evaluator and an Evaluatee, 
                    but the system will automatically prevent them from evaluating themselves.
                  </p>
                </div>

                {loadingUsers && <div className="ce-loading">Loading users…</div>}

                <div className="ce-people-grid">
                  <ParticipantBox
                    label="Evaluators"
                    color="blue"
                    selectedIds={evaluatorIds}
                    options={allUsers}
                    search={evaluatorSearch}
                    onSearch={setEvaluatorSearch}
                    onAdd={(id) => {
                      setEvaluatorIds((p) => (p.includes(id) ? p : [...p, id]));
                    }}

                    onRemove={(id) => setEvaluatorIds((p) => p.filter((x) => x !== id))}
                    usersById={usersById}
                    error={errors.evaluators}
                    loading={loadingUsers}
                  />
                  <ParticipantBox
                    label="Evaluatees"
                    color="green"
                    selectedIds={evaluateeIds}
                    options={allUsers}
                    search={evaluateeSearch}
                    onSearch={setEvaluateeSearch}
                    onAdd={(id) => {
                      setEvaluateeIds((p) => (p.includes(id) ? p : [...p, id]));
                    }}

                    onRemove={(id) => setEvaluateeIds((p) => p.filter((x) => x !== id))}
                    usersById={usersById}
                    error={errors.evaluatees}
                    loading={loadingUsers}
                  />
                </div>

                {apiError && (
                  <div className="ce-api-error"><IcoInfo /> {apiError}</div>
                )}
              </div>
            )}

          </div>
        </div>

        {/* ══ RIGHT — criteria panel ══ */}
        <aside className="ce-criteria-panel">
          <div className="ce-criteria-panel-head">
            <div className="ce-criteria-panel-title">Evaluation Criteria</div>
            <span className="ce-readonly-pill">Read-only</span>
          </div>
          <div className="ce-criteria-panel-sub">
            10 standardized criteria applied to every evaluation. Cannot be modified.
          </div>
          <div className="ce-criteria-list">
            {CRITERIA.map((c, i) => (
              <div className="ce-criteria-row" key={c.label}>
                <div className="ce-criteria-num">{String(i + 1).padStart(2, '0')}</div>
                <div>
                  <div className="ce-criteria-name">{c.label}</div>
                  <div className="ce-criteria-desc">{c.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </aside>

      </div>

      <div className="ce-footer-bar">
        <button
          className="ce-btn ce-btn-ghost"
          type="button"
          onClick={() => tab === 1 ? guardedNavigate('/forms-created') : setTab(1)}
        >
          <IcoBack /> {tab === 1 ? 'Cancel' : 'Back'}
        </button>
        {tab === 1 ? (
          <button className="ce-btn ce-btn-primary" type="button" onClick={handleNext}>
            Next <IcoNext />
          </button>
        ) : (
          <button className="ce-btn ce-btn-primary" type="button" onClick={handleSubmit} disabled={saving}>
            {saving ? (isEditMode ? 'Saving…' : 'Creating…') : (isEditMode ? 'Save Changes' : 'Create Evaluation')}
          </button>
        )}
      </div>

      <ExitConfirmModal
        isOpen={exitModal.open}
        onCancel={() => setExitModal({ open: false, path: '' })}
        onConfirm={() => {
          const path = exitModal.path;
          setExitModal({ open: false, path: '' });
          navigate(path);
        }}
        title="Discard this evaluation?"
        body="You've started filling in details. Leaving now will discard everything and you'll need to start over."
      />

    </div>
  );
}
