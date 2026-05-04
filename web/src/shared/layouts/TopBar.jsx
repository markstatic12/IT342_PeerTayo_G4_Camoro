import { useAuth } from '../../features/auth/context/AuthContext';
import { SearchIcon, BellIcon } from '../components/icons/Icons';
import './TopBar.css';

export default function TopBar() {
  const { user } = useAuth();

  const initials =
    (user?.firstName?.charAt(0) || '') + (user?.lastName?.charAt(0) || '');

  const roleName =
    user?.roles?.some((r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR')
      ? 'Facilitator'
      : 'Respondent';

  return (
    <header className="topbar">
      <div className="topbar__search">
        <span className="topbar__search-icon">
          <SearchIcon size={16} />
        </span>
        <input
          className="topbar__search-input"
          type="text"
          placeholder="Search evaluations, forms, or users…"
        />
      </div>

      <div className="topbar__right">
        <button className="topbar__icon-btn" title="Notifications">
          <BellIcon size={20} />
        </button>

        <div className="topbar__profile">
          <div className="topbar__user-info">
            <span className="topbar__user-name">
              {user?.firstName} {user?.lastName}
            </span>
            <span className="topbar__user-role">{roleName}</span>
          </div>
          <div className="topbar__avatar">{initials}</div>
        </div>
      </div>
    </header>
  );
}
