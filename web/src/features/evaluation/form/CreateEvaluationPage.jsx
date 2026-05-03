import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/context/AuthContext';
import { createEvaluation } from '../service/evaluationService';
import { searchUsers } from '../../user/service/userService';
import './CreateEvaluationPage.css';

const criteriaOptions = [
  { label: 'Quality of Work', description: 'Produces accurate, thorough, and well-organized outputs that meet or exceed expectations.' },
  { label: 'Reliability & Dependability', description: 'Consistently delivers on commitments and can be counted on to follow through on tasks.' },
  { label: 'Collaboration & Teamwork', description: 'Works effectively with others and contributes constructively to group efforts.' },
  { label: 'Communication Skills', description: 'Expresses ideas clearly, listens actively, and communicates updates in a timely manner.' },
  { label: 'Initiative & Proactiveness', description: 'Identifies and acts on opportunities without being prompted; goes beyond minimum requirements.' },
  { label: 'Problem Solving', description: 'Approaches challenges analytically and proposes practical, effective solutions.' },
  { label: 'Professionalism & Conduct', description: 'Maintains a respectful, ethical, and positive demeanor in all interactions.' },
  { label: 'Time Management', description: 'Prioritizes tasks effectively, meets deadlines, and manages workload without compromising quality.' },
  { label: 'Adaptability & Learning', description: 'Responds positively to change, accepts feedback constructively, and continuously improves.' },
  { label: 'Overall Contribution', description: 'Holistic assessment of the individual\'s net positive impact on the team or group outcome.' },
];

export default function CreateEvaluationPage() {
  const navigate = useNavigate();
  const { user: currentUser } = useAuth();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    title: '',
    deadline: '',
    description: '',
  });
  const [usersById, setUsersById] = useState({});
  const [evaluatorOptions, setEvaluatorOptions] = useState([]);
  const [evaluateeOptions, setEvaluateeOptions] = useState([]);
  const [evaluatorIds, setEvaluatorIds] = useState([]);
  const [evaluateeIds, setEvaluateeIds] = useState([]);
  const [evaluatorSearch, setEvaluatorSearch] = useState('');
  const [evaluateeSearch, setEvaluateeSearch] = useState('');
  const [saving, setSaving] = useState(false);
  const [loadingUsers, setLoadingUsers] = useState(false);
  const [error, setError] = useState('');

  // Scroll to top of the layout content area whenever the step changes
  useEffect(() => {
    const container = document.querySelector('.app-layout__content');
    if (container) {
      container.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }, [step]);

  const updateField = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  useEffect(() => {
    let alive = true;

    const loadInitialUsers = async () => {
      setLoadingUsers(true);
      setError('');

      try {
        const data = await searchUsers('');
        if (!alive) return;

        const byId = Object.fromEntries(data.map((item) => [item.id, item]));
        setUsersById((prev) => ({ ...prev, ...byId }));
        setEvaluateeOptions(data);
        setEvaluatorOptions(
          data.filter((item) => item.id !== currentUser?.id)
        );
      } catch (err) {
        if (!alive) return;
        const message = err.response?.data?.error?.message || 'Failed to load users for assignment.';
        setError(message);
      } finally {
        if (alive) setLoadingUsers(false);
      }
    };

    loadInitialUsers();

    return () => {
      alive = false;
    };
  }, [currentUser?.id]);

  useEffect(() => {
    let alive = true;

    const timer = window.setTimeout(async () => {
      try {
        const data = await searchUsers(evaluatorSearch);
        if (!alive) return;
        setUsersById((prev) => ({
          ...prev,
          ...Object.fromEntries(data.map((item) => [item.id, item])),
        }));
        setEvaluatorOptions(data.filter((item) => item.id !== currentUser?.id));
      } catch {
        if (!alive) return;
        setEvaluatorOptions([]);
      }
    }, 250);

    return () => {
      alive = false;
      window.clearTimeout(timer);
    };
  }, [evaluatorSearch, currentUser?.id]);

  useEffect(() => {
    let alive = true;

    const timer = window.setTimeout(async () => {
      try {
        const data = await searchUsers(evaluateeSearch);
        if (!alive) return;
        setUsersById((prev) => ({
          ...prev,
          ...Object.fromEntries(data.map((item) => [item.id, item])),
        }));
        setEvaluateeOptions(data);
      } catch {
        if (!alive) return;
        setEvaluateeOptions([]);
      }
    }, 250);

    return () => {
      alive = false;
      window.clearTimeout(timer);
    };
  }, [evaluateeSearch]);

  const evaluatorUsers = evaluatorIds.map((id) => usersById[id]).filter(Boolean);
  const evaluateeUsers = evaluateeIds.map((id) => usersById[id]).filter(Boolean);

  const addEvaluatorById = (id) => {
    setEvaluatorIds((prev) => (prev.includes(id) ? prev : [...prev, id]));
    setEvaluatorSearch('');
  };

  const addEvaluateeById = (id) => {
    setEvaluateeIds((prev) => (prev.includes(id) ? prev : [...prev, id]));
    setEvaluateeSearch('');
  };

  const removeEvaluator = (id) => {
    setEvaluatorIds((prev) => prev.filter((item) => item !== id));
  };

  const removeEvaluatee = (id) => {
    setEvaluateeIds((prev) => prev.filter((item) => item !== id));
  };

  const handleSubmit = async () => {
    if (!form.title.trim() || !form.description.trim() || !form.deadline) {
      setError('Title, description, and deadline are required.');
      return;
    }

    if (evaluatorIds.length === 0 || evaluateeIds.length === 0) {
      setError('At least one evaluator and one evaluatee are required.');
      return;
    }

    setSaving(true);
    setError('');
    try {
      await createEvaluation({
        title: form.title,
        description: form.description,
        deadline: form.deadline,
        evaluateeIds,
        evaluatorIds,
      });
      navigate('/forms-created');
    } catch (err) {
      const message = err.response?.data?.error?.message || 'Failed to create evaluation.';
      setError(message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="create-eval-v7">
      {/* Top-left page header */}
      <div className="page-header-block">
        <div className="breadcrumb">
          Dashboard / Forms Created / <span>Create Evaluation</span>
        </div>
        <div className="page-title">Create Your Evaluation Form</div>
        <div className="page-sub">Set up a peer evaluation by reviewing the criteria and assigning participants.</div>
      </div>

      {/* Centered stepper */}
      <div className="stepper" id="stepper">
        <div className="step">
          <div className={`step-circle ${step >= 1 ? 'done' : 'inactive'}`}>1</div>
          <span className={`step-label${step < 1 ? ' inactive' : ''}`}>Overview of Criteria</span>
        </div>
        <div className={`step-connector${step > 1 ? ' done' : ''}`} />
        <div className="step">
          <div className={`step-circle ${step === 2 ? 'active' : step > 2 ? 'done' : 'inactive'}`}>2</div>
          <span className={`step-label${step < 2 ? ' inactive' : ''}`}>Details &amp; People</span>
        </div>
      </div>

      {/* Centered card */}
      <div className="card-center-wrap">
        <div className="content-card">
          <div className="card-body">
            {step === 1 ? (
              <>
                <div className="criteria-header">
                  <div>
                    <div className="criteria-title">Evaluation Criteria</div>
                    <div className="criteria-sub">10 standardized criteria used across all evaluations. They cannot be modified.</div>
                  </div>
                  <span className="readonly-pill">⊘ Read-only</span>
                </div>
                <div className="criteria-grid">
                  {criteriaOptions.map((item, index) => (
                    <div className="criteria-item" key={item.label}>
                      <div className="criteria-num">{String(index + 1).padStart(2, '0')}</div>
                      <div>
                        <div className="criteria-name">{item.label}</div>
                        <div className="criteria-desc">{item.description}</div>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <>
                <div className="form-section-title">Evaluation Details</div>
                {loadingUsers && <div className="ce-error">Loading users...</div>}
                {error && <div className="ce-error">{error}</div>}
                <div className="form-grid-2">
                  <div className="form-group">
                    <label className="form-label">
                      Evaluation Title <span className="req">*</span>
                    </label>
                    <input
                      className="form-input"
                      type="text"
                      placeholder="e.g. Q1 2026 Team Performance Review"
                      value={form.title}
                      onChange={updateField('title')}
                    />
                  </div>
                  <div className="form-group">
                    <label className="form-label">
                      Deadline <span className="req">*</span>
                    </label>
                    <input
                      className="form-input"
                      type="datetime-local"
                      value={form.deadline}
                      onChange={updateField('deadline')}
                    />
                  </div>
                </div>

                <div className="form-group" style={{ marginBottom: '16px' }}>
                  <label className="form-label">
                    Description <span className="req">*</span>
                  </label>
                  <textarea
                    className="form-input"
                    placeholder="Briefly describe the purpose or context of this evaluation…"
                    value={form.description}
                    onChange={updateField('description')}
                  />
                </div>

                <div className="rule-note">
                  <svg viewBox="0 0 24 24" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  <p>
                    <strong>Assignment rules:</strong> A user cannot be both evaluator and evaluatee in the same evaluation. As the form creator, you are excluded from the evaluator list but may add yourself as an evaluatee.
                  </p>
                </div>

                <span className="form-section-title">Assign Participants</span>
                <div className="people-grid">
                  <div className="people-box">
                    <div className="people-label">
                      Select Evaluators <span className="req">*</span>
                    </div>
                    <div className="search-input-wrap">
                      <svg viewBox="0 0 24 24" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                      <input
                        type="text"
                        placeholder="Search by name or email…"
                        value={evaluatorSearch}
                        onChange={(event) => setEvaluatorSearch(event.target.value)}
                        onKeyDown={(event) => {
                          if (event.key === 'Enter') {
                            event.preventDefault();
                            const first = evaluatorOptions.find((item) => !evaluatorIds.includes(item.id));
                            if (first) addEvaluatorById(first.id);
                          }
                        }}
                      />
                    </div>
                    {evaluatorSearch.trim() && (
                      <div className="chips-area" style={{ minHeight: 'auto', maxHeight: '130px', overflowY: 'auto' }}>
                        {evaluatorOptions
                          .filter((item) => !evaluatorIds.includes(item.id))
                          .slice(0, 10)
                          .map((item) => (
                            <button key={item.id} type="button" className="chip chip-blue" onClick={() => addEvaluatorById(item.id)}>
                              {item.firstName} {item.lastName} ({item.email})
                            </button>
                          ))}
                      </div>
                    )}
                    <div className="chips-area">
                      {evaluatorUsers.map((user) => (
                        <div className="chip chip-blue" key={user.id}>
                          {`${user.firstName} ${user.lastName}`}
                          <svg className="chip-remove" viewBox="0 0 24 24" strokeWidth="2.5" onClick={() => removeEvaluator(user.id)}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                        </div>
                      ))}
                    </div>
                  </div>
                  <div className="people-box">
                    <div className="people-label">
                      Select Evaluatees <span className="req">*</span>
                    </div>
                    <div className="search-input-wrap">
                      <svg viewBox="0 0 24 24" strokeWidth="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                      <input
                        type="text"
                        placeholder="Search by name or email…"
                        value={evaluateeSearch}
                        onChange={(event) => setEvaluateeSearch(event.target.value)}
                        onKeyDown={(event) => {
                          if (event.key === 'Enter') {
                            event.preventDefault();
                            const first = evaluateeOptions.find((item) => !evaluateeIds.includes(item.id));
                            if (first) addEvaluateeById(first.id);
                          }
                        }}
                      />
                    </div>
                    {evaluateeSearch.trim() && (
                      <div className="chips-area" style={{ minHeight: 'auto', maxHeight: '130px', overflowY: 'auto' }}>
                        {evaluateeOptions
                          .filter((item) => !evaluateeIds.includes(item.id))
                          .slice(0, 10)
                          .map((item) => (
                            <button key={item.id} type="button" className="chip chip-green" onClick={() => addEvaluateeById(item.id)}>
                              {item.firstName} {item.lastName} ({item.email})
                            </button>
                          ))}
                      </div>
                    )}
                    <div className="chips-area">
                      {evaluateeUsers.map((user) => (
                        <div className="chip chip-green" key={user.id}>
                          {`${user.firstName} ${user.lastName}`}
                          <svg className="chip-remove" viewBox="0 0 24 24" strokeWidth="2.5" onClick={() => removeEvaluatee(user.id)}><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>

          {step === 1 && (
            <div className="card-footer-note">
              All 10 criteria are fixed and apply to every evaluation in the system.
            </div>
          )}

          <div className="card-body-actions">
            <button
              className="btn btn-ghost"
              type="button"
              onClick={() => {
                if (step === 1) { navigate('/forms-created'); return; }
                setStep(1);
              }}
            >
              <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="15 18 9 12 15 6"/></svg>
              Back
            </button>
            <button
              className="btn btn-primary"
              type="button"
              onClick={() => {
                if (step === 1) { setStep(2); return; }
                handleSubmit();
              }}
              disabled={saving}
            >
              {step === 1 ? 'Next Step' : saving ? 'Creating...' : 'Create Evaluation'}
              {step === 1 && (
                <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="9 18 15 12 9 6"/></svg>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
