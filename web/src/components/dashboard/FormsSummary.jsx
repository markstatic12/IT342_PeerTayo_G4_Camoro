import { Button } from '../ui';
import { PlusCircleIcon, TrendUpIcon } from '../icons/Icons';
import '../../features/dashboard/FormsSummary.css';

/**
 * "Your Forms Summary" dashboard card (matches image 4).
 * All values are placeholders — will be wired to API later.
 */
export default function FormsSummary() {
  const activeForms = 0;
  const totalResponses = 0;
  const completionRate = 0;

  return (
    <div className="forms-summary">
      <div className="forms-summary__visual">
        <TrendUpIcon size={72} className="forms-summary__visual-icon" />
      </div>

      <div className="forms-summary__content">
        <div className="forms-summary__header">
          <h2 className="forms-summary__title">Your Forms Summary</h2>
          <span className="forms-summary__badge">Live Tracking</span>
        </div>

        <div className="forms-summary__stats">
          <div className="forms-summary__stat">
            <div className="forms-summary__stat-label">Active Forms</div>
            <div className="forms-summary__stat-value">{activeForms}</div>
          </div>
          <div className="forms-summary__stat">
            <div className="forms-summary__stat-label">Total Responses</div>
            <div className="forms-summary__stat-value forms-summary__stat-value--dark">
              {totalResponses}
            </div>
          </div>
        </div>

        <div className="forms-summary__progress">
          <span className="forms-summary__progress-label">Average Completion Rate</span>
          <div className="forms-summary__progress-bar">
            <div
              className="forms-summary__progress-fill"
              style={{ width: `${completionRate}%` }}
            />
          </div>
          <span className="forms-summary__progress-pct">{completionRate}%</span>
        </div>

        <div className="forms-summary__actions">
          <Button variant="primary" style={{ flex: 1 }}>
            <PlusCircleIcon size={18} />
            Create New Evaluation
          </Button>
          <Button variant="outline">View History</Button>
        </div>
      </div>
    </div>
  );
}
