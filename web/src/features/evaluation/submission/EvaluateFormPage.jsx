import { useState, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { submitEvaluation } from './evaluationSubmissionService';
import './EvaluateFormPage.css';

/* ─── Criteria definition (mirrors HTML prototype) ────────── */
const CRITERIA = [
  { name: 'Quality of Work',            desc: 'Produces accurate, thorough, and well-organized outputs that meet or exceed expectations.' },
  { name: 'Reliability & Dependability',desc: 'Consistently delivers on commitments and can be counted on to follow through on tasks.' },
  { name: 'Collaboration & Teamwork',   desc: 'Works effectively with others and contributes constructively to group efforts.' },
  { name: 'Communication Skills',       desc: 'Expresses ideas clearly, listens actively, and communicates updates in a timely manner.' },
  { name: 'Initiative & Proactiveness', desc: 'Identifies and acts on opportunities without being prompted; goes beyond minimum requirements.' },
  { name: 'Problem Solving',            desc: 'Approaches challenges analytically and proposes practical, effective solutions.' },
  { name: 'Professionalism & Conduct',  desc: 'Maintains a respectful, ethical, and positive demeanor in all interactions.' },
  { name: 'Time Management',            desc: 'Prioritizes tasks effectively, meets deadlines, and manages workload without compromising quality.' },
  { name: 'Adaptability & Learning',    desc: 'Responds positively to change, accepts feedback constructively, and continuously improves.' },
  { name: 'Overall Contribution',       desc: "Holistic assessment of the individual's net positive impact on the team or group outcome." },
];

/* ─── Helpers ──────────────────────────────────────────────── */
function getInitials(name = '') {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');
}

/* ─── Tiny inline SVGs ─────────────────────────────────────── */
const SvgChevLeft = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor">
    <polyline points="15 18 9 12 15 6" />
  </svg>
);
const SvgAlert = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor">
    <circle cx="12" cy="12" r="10" />
    <line x1="12" y1="8" x2="12" y2="12" />
    <line x1="12" y1="16" x2="12.01" y2="16" />
  </svg>
);
const SvgCheckFull = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);
const SvgCheckCircle = () => (
  <svg viewBox="0 0 24 24" strokeWidth="2" fill="none" stroke="currentColor">
    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
    <polyline points="22 4 12 14.01 9 11.01" />
  </svg>
);

/* ─── Scale strip ─────────────────────────────────────────── */
const SCALE = [
  { n: 1, label: 'Poor',                    cls: 'sn-1' },
  { n: 2, label: 'Below Average',           cls: 'sn-2' },
  { n: 3, label: 'Fair / Meets Expectations', cls: 'sn-3' },
  { n: 4, label: 'Good',                    cls: 'sn-4' },
  { n: 5, label: 'Excellent',               cls: 'sn-5' },
];

/* ─── Main page ────────────────────────────────────────────── */
export default function EvaluateFormPage() {
  const navigate = useNavigate();
  const location = useLocation();

  /* State passed from PendingEvaluationsPage via navigate() */
  const { form, evaluatee } = location.state ?? {};

  const [ratings, setRatings]   = useState({});   // { criteriaIndex: 1–5 }
  const [comment, setComment]   = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted]   = useState(false);
  const [error, setError]       = useState('');

  const ratedCount = Object.keys(ratings).length;
  const total      = CRITERIA.length;
  const pct        = Math.round((ratedCount / total) * 100);
  const allRated   = ratedCount === total;

  const evaluateeName = evaluatee?.name ?? evaluatee?.evaluateeName ?? 'Unknown';
  const formTitle     = form?.title ?? '—';
  const formDue       = form?.due ?? form?.deadline ?? '—';
  const formDaysLeft  = form?.daysLeft ?? null;
  const urgent        = form?.urgent || (formDaysLeft !== null && formDaysLeft <= 3);

  const handleRate = useCallback((criteriaIdx, value) => {
    setRatings((prev) => ({ ...prev, [criteriaIdx]: value }));
  }, []);

  const handleSubmit = async () => {
    if (!allRated) return;
    setSubmitting(true);
    setError('');
    try {
      // Backend expects: POST /evaluations/{evaluationFormId}/submit
      // Body: { responses: [{ criteriaId, rating }], comment }
      const payload = {
        responses: CRITERIA.map((_, i) => ({
          criteriaId: i + 1,   // criteria IDs are 1-indexed on the backend
          rating:     ratings[i],
        })),
        comment: comment.trim() || undefined,
      };
      // form.id = evaluationForm id; evaluatee.assignmentId is stored on the backend
      if (form?.id) {
        await submitEvaluation(form.id, payload);
      }
      setSubmitted(true);
    } catch (err) {
      const msg = err?.response?.data?.message ?? 'Submission failed. Please try again.';
      setError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  /* ── Success state ── */
  if (submitted) {
    return (
      <div className="eval-screen animate-page">
        <div className="ef-success">
          <div className="ef-success-icon" style={{ width: 60, height: 60 }}>
            <SvgCheckCircle />
          </div>
          <div className="ef-success-title">Evaluation Submitted!</div>
          <div className="ef-success-sub">Your response for {evaluateeName} has been recorded.</div>
          <button
            className="ef-btn ef-btn-primary"
            onClick={() => navigate('/pending-evaluations')}
          >
            Back to Pending Evaluations
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="eval-screen animate-page">

      {/* ── Top bar ── */}
      <div className="eval-topbar">
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <button className="esh-back" onClick={() => navigate('/pending-evaluations')}>
            <SvgChevLeft />
            Pending Evaluations
          </button>
          <span className="esh-crumb">/ {formTitle} / {evaluateeName}</span>
        </div>
        {urgent && formDaysLeft !== null && (
          <div className="esh-urgent">
            <SvgAlert />
            Urgent — {formDaysLeft} day{formDaysLeft !== 1 ? 's' : ''} left
          </div>
        )}
      </div>

      {/* ── Who banner ── */}
      <div className="eval-who-banner">
        <div className="ewb-avatar">
          {getInitials(evaluateeName)}
        </div>
        <div>
          <div className="ewb-name">{evaluateeName}</div>
          <div className="ewb-sub">
            <span>{formTitle}</span>
            <span className="ewb-divider" />
            <span>Due {formDue}</span>
          </div>
        </div>
        <div className="ewb-right">
          <div className="ewb-stat">
            <div className="ewb-stat-val">{ratedCount}</div>
            <div className="ewb-stat-lbl">of {total} rated</div>
          </div>
          <div className="ewb-track-wrap">
            <div className="ewb-track-label">{pct}%</div>
            <div className="ewb-track">
              <div className="ewb-fill" style={{ width: `${pct}%` }} />
            </div>
          </div>
        </div>
      </div>

      {/* ── Scale legend ── */}
      <div className="scale-strip">
        {SCALE.map((s) => (
          <div className="scale-item" key={s.n}>
            <div className={`scale-num ${s.cls}`}>{s.n}</div>
            <div className="scale-lbl">{s.label}</div>
          </div>
        ))}
      </div>

      {/* ── Scrollable body ── */}
      <div className="eval-body">
        <div className="eval-content-wrap">

          {/* Criteria grid */}
          <div className="criteria-rating-grid">
            {CRITERIA.map((c, i) => {
              const selected = ratings[i];
              return (
                <div
                  key={c.name}
                  className={`cr-card${selected ? ` rated-${selected}` : ''}`}
                >
                  <div className="cr-top">
                    <div className="cr-num">{String(i + 1).padStart(2, '0')}</div>
                    <div>
                      <div className="cr-name">{c.name}</div>
                      <div className="cr-desc">{c.desc}</div>
                    </div>
                  </div>
                  <div className="cr-rating">
                    {[1, 2, 3, 4, 5].map((n) => (
                      <div
                        key={n}
                        className={`cr-rb${selected === n ? ` sel-${n}` : ''}`}
                        onClick={() => handleRate(i, n)}
                      >
                        {n}
                      </div>
                    ))}
                  </div>
                </div>
              );
            })}
          </div>

          {/* Comment card */}
          <div className="comment-card">
            <div className="comment-label">
              Final Feedback Comment
              <span className="opt-tag">OPTIONAL</span>
            </div>
            <textarea
              className="comment-ta"
              placeholder="Share any additional observations about this person's performance…"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
            />
          </div>

          {/* Submit row */}
          <div className="submit-row">
            <div className={`ans-note${allRated ? ' all' : ' partial'}`}>
              {allRated ? <SvgCheckFull /> : <SvgAlert />}
              <span>
                {allRated
                  ? `All ${total} criteria answered — ready to submit`
                  : `${ratedCount} of ${total} criteria rated`}
              </span>
            </div>
            <div className="ef-actions">
              <button
                className="ef-btn ef-btn-ghost"
                onClick={() => navigate('/pending-evaluations')}
              >
                Cancel
              </button>
              <button
                className="ef-btn ef-btn-primary"
                disabled={!allRated || submitting}
                onClick={handleSubmit}
              >
                {submitting ? 'Submitting…' : 'Submit Evaluation'}
                {!submitting && <SvgCheckFull />}
              </button>
            </div>
          </div>

          {error && (
            <div style={{ color: 'var(--ef-red)', fontSize: 12, textAlign: 'center' }}>
              {error}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
