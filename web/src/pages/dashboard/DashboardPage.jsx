import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listEvaluations } from '../../services/evaluations/evaluationService';
import './DashboardPage.css';

export default function DashboardPage() {
  const navigate = useNavigate();
  const [evaluations, setEvaluations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let mounted = true;
    const loadEvaluations = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await listEvaluations();
        if (mounted) {
          setEvaluations(data);
        }
      } catch {
        if (mounted) {
          setError('Unable to load evaluations right now.');
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    loadEvaluations();
    return () => {
      mounted = false;
    };
  }, []);

  const now = new Date();
  const activeCount = evaluations.filter((item) => new Date(item.deadline) >= now).length;
  const closedCount = evaluations.length - activeCount;
  const recentEvaluations = useMemo(() => evaluations.slice(0, 6), [evaluations]);

  const stats = [
    { label: 'Total Forms', value: evaluations.length, hint: 'All created evaluations' },
    { label: 'Active Evaluations', value: activeCount, hint: 'Deadline not yet reached' },
    { label: 'Closed Evaluations', value: closedCount, hint: 'Past deadline or completed' },
  ];

  const formatDateTime = (isoString) => {
    const date = new Date(isoString);
    return date.toLocaleString([], {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    });
  };

  return (
    <div className="dashboard-page">
      <section className="dashboard-header">
        <div>
          <h1 className="dashboard-title">Dashboard</h1>
          <p className="dashboard-subtitle">
            Manage your evaluation forms and track status in one place.
          </p>
        </div>
        <button
          type="button"
          className="create-eval-btn"
          onClick={() => navigate('/forms-created')}
        >
          Create Evaluation
        </button>
      </section>

      <section className="dashboard-stats">
        {stats.map((stat) => (
          <article className="stat-card" key={stat.label}>
            <p className="stat-label">{stat.label}</p>
            <p className="stat-value">{stat.value}</p>
            <p className="stat-hint">{stat.hint}</p>
          </article>
        ))}
      </section>

      <section className="dashboard-content-card">
        <div className="dashboard-content-head">
          <h2>Created Evaluations</h2>
          <button type="button" className="link-button" onClick={() => navigate('/forms-created')}>
            New Form
          </button>
        </div>

        {loading && <p className="dashboard-info">Loading evaluations...</p>}
        {!loading && error && <p className="dashboard-error">{error}</p>}

        {!loading && !error && evaluations.length === 0 && (
          <div className="empty-state">
            <h3>No evaluations yet</h3>
            <p>Create your first evaluation form to get started.</p>
            <button type="button" className="create-eval-btn" onClick={() => navigate('/forms-created')}>
              Create First Evaluation
            </button>
          </div>
        )}

        {!loading && !error && evaluations.length > 0 && (
          <div className="evaluation-grid">
            {recentEvaluations.map((evaluation) => (
              <article className="evaluation-card" key={evaluation.id}>
                <div className="evaluation-card-top">
                  <span className={`status-badge ${evaluation.status?.toLowerCase() || 'draft'}`}>
                    {evaluation.status}
                  </span>
                  <span className="evaluation-date">{formatDateTime(evaluation.createdAt)}</span>
                </div>

                <h3>{evaluation.title}</h3>
                <p>{evaluation.description}</p>

                <div className="evaluation-meta">
                  <span>Deadline: {formatDateTime(evaluation.deadline)}</span>
                  <span>Criteria: {evaluation.criteria?.length || 0}</span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
