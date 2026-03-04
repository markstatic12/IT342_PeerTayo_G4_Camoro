import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LogoIcon,
  DashboardIcon,
  EvaluationsIcon,
  ReportsIcon,
  FormsIcon,
  RespondentsIcon,
  SettingsIcon,
  LogoutIcon,
} from '../components/icons/Icons';
import './Sidebar.css';

const mainMenu = [
  { to: '/dashboard', label: 'Dashboard', icon: DashboardIcon },
  { to: '/evaluations', label: 'Evaluations', icon: EvaluationsIcon },
  { to: '/reports', label: 'Reports', icon: ReportsIcon },
];

const manageMenu = [
  { to: '/forms-created', label: 'Forms Created', icon: FormsIcon },
  { to: '/respondents', label: 'Respondents', icon: RespondentsIcon },
];

export default function Sidebar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      {/* Brand */}
      <div className="sidebar__brand">
        <LogoIcon size={28} />
        <span className="sidebar__brand-text">PeerTayo</span>
      </div>

      {/* Navigation */}
      <nav className="sidebar__nav">
        <div className="sidebar__section">
          <span className="sidebar__section-title">Main Menu</span>
          {mainMenu.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/dashboard'}
              className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </div>

        <div className="sidebar__section">
          <span className="sidebar__section-title">Manage</span>
          {manageMenu.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `sidebar__link${isActive ? ' active' : ''}`}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </div>
      </nav>

      {/* Footer */}
      <div className="sidebar__footer">
        <NavLink to="/settings" className="sidebar__footer-link">
          <SettingsIcon size={18} />
          Settings
        </NavLink>
        <button className="sidebar__footer-link" onClick={handleLogout}>
          <LogoutIcon size={18} />
          Sign Out
        </button>
      </div>
    </aside>
  );
}
