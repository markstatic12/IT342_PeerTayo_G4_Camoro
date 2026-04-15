import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createEvaluation } from '../../services/evaluations/evaluationService';
import './CreateEvaluationPage.css';

const criteria = [
  'Quality of Work',
  'Reliability & Dependability',
  'Collaboration & Teamwork',
  'Communication Skills',
  'Initiative & Proactiveness',
  'Problem Solving',
  'Professionalism & Conduct',
  'Time Management',
  'Adaptability & Learning',
  'Overall Contribution',
];

export default function CreateEvaluationPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    title: '',
    description: '',
    deadline: '',
    status: 'DRAFT',
    criteria: criteria.join('\n'),
    questions: '',
    ratingFields: '1\n2\n3\n4\n5',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const updateField = (field) => (event) => {
    setForm((prev) => ({ ...prev, [field]: event.target.value }));
  };

  const toList = (value) =>
    value
      .split('\n')
      .map((item) => item.trim())
      .filter(Boolean);

  const handleSubmit = async () => {
    if (!form.title.trim() || !form.description.trim() || !form.deadline) {
      setError('Title, description, and deadline are required.');
      return;
    }

    setSaving(true);
    setError('');
    try {
      await createEvaluation({
        title: form.title,
        description: form.description,
        deadline: form.deadline,
        status: form.status,
        criteria: toList(form.criteria),
        questions: toList(form.questions),
        ratingFields: toList(form.ratingFields),
      });
      navigate('/dashboard');
    } catch (err) {
      const message = err.response?.data?.error?.message || 'Failed to create evaluation.';
      setError(message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="create-eval-v7">
      <div className="ce-header">
        <div className="ce-breadcrumb">Dashboard / Forms Created / Create Evaluation</div>
        <h1>Create Your Evaluation Form</h1>
        <p>Set up a peer evaluation by reviewing criteria and assigning participants.</p>
      </div>

      <div className="ce-stepper">
        <div className={`ce-step ${step >= 1 ? 'on' : ''}`}>1. Overview of Criteria</div>
        <div className="ce-line" />
        <div className={`ce-step ${step >= 2 ? 'on' : ''}`}>2. Details & People</div>
      </div>

      {step === 1 ? (
        <section className="ce-card">
          <div className="ce-card-head">
            <div>
              <h2>Evaluation Criteria</h2>
              <p>10 standardized criteria used across all evaluations.</p>
            </div>
            <span className="ce-pill">Read-only</span>
          </div>
          <div className="ce-criteria-grid">
            {criteria.map((name, idx) => (
              <article className="ce-criteria-item" key={name}>
                <span className="ce-num">{String(idx + 1).padStart(2, '0')}</span>
                <div>
                  <h3>{name}</h3>
                  <p>Standardized criterion definition from your design specification.</p>
                </div>
              </article>
            ))}
          </div>
          <div className="ce-actions">
            <span>All 10 criteria are fixed and apply to every evaluation in the system.</span>
            <button type="button" onClick={() => setStep(2)}>Next Step</button>
          </div>
        </section>
      ) : (
        <section className="ce-card">
          <h2 className="ce-form-title">Evaluation Details</h2>
          {error && <p className="ce-error">{error}</p>}
          <div className="ce-grid-2">
            <label>
              Evaluation Title
              <input
                type="text"
                placeholder="e.g. Q1 2026 Team Performance Review"
                value={form.title}
                onChange={updateField('title')}
              />
            </label>
            <label>
              Deadline
              <input type="datetime-local" value={form.deadline} onChange={updateField('deadline')} />
            </label>
          </div>

          <div className="ce-grid-2">
            <label>
              Status
              <select value={form.status} onChange={updateField('status')}>
                <option value="DRAFT">Draft</option>
                <option value="ACTIVE">Active</option>
                <option value="CLOSED">Closed</option>
              </select>
            </label>
            <label>
              Questions (one per line)
              <textarea
                value={form.questions}
                onChange={updateField('questions')}
                placeholder="How well does this member collaborate?"
              />
            </label>
          </div>

          <label>
            Description
            <textarea
              value={form.description}
              onChange={updateField('description')}
              placeholder="Briefly describe the purpose or context of this evaluation..."
            />
          </label>

          <div className="ce-grid-2">
            <label>
              Criteria (one per line)
              <textarea value={form.criteria} onChange={updateField('criteria')} />
            </label>
            <label>
              Rating Fields (one per line)
              <textarea value={form.ratingFields} onChange={updateField('ratingFields')} />
            </label>
          </div>

          <div className="ce-note">
            Assignment rules: A user cannot be both evaluator and evaluatee in the same evaluation.
          </div>

          <div className="ce-actions">
            <button type="button" className="ghost" onClick={() => setStep(1)}>Back</button>
            <button type="button" onClick={handleSubmit} disabled={saving}>
              {saving ? 'Creating...' : 'Create Evaluation'}
            </button>
          </div>
        </section>
      )}
    </div>
  );
}
