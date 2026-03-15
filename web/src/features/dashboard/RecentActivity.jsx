import { FormsIcon, CheckCircleIcon, AlertTriangleIcon } from '../../components/icons/Icons';
import './RecentActivity.css';

/**
 * Recent Activity section on the Dashboard (matches image 4).
 * Activities will come from the API later — showing empty state for now.
 */
export default function RecentActivity() {
  // placeholder: will be fetched from API once endpoints exist
  const activities = [];

  return (
    <section className="recent-activity">
      <div className="recent-activity__header">
        <h2 className="recent-activity__title">Recent Activity</h2>
        {activities.length > 0 && (
          <button className="recent-activity__view-all">View All</button>
        )}
      </div>

      <div className="recent-activity__grid">
        {activities.length === 0 ? (
          <div className="recent-activity__empty">
            <p>No recent activity</p>
            <span>Activity will appear here once evaluations begin</span>
          </div>
        ) : (
          activities.map((item) => (
            <div className="activity-card" key={item.id}>
              <div className={`activity-card__icon activity-card__icon--${item.type}`}>
                {item.type === 'info' && <FormsIcon size={18} />}
                {item.type === 'success' && <CheckCircleIcon size={18} />}
                {item.type === 'warning' && <AlertTriangleIcon size={18} />}
              </div>
              <div className="activity-card__body">
                <div className="activity-card__title">{item.title}</div>
                <div className="activity-card__desc">{item.description}</div>
                <div className="activity-card__time">{item.time}</div>
              </div>
            </div>
          ))
        )}
      </div>
    </section>
  );
}
