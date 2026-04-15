import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopBar from './TopBar';
import './AppLayout.css';

/**
 * Main authenticated layout shell:
 *   ┌── Sidebar ──┬── TopBar ───────────────┐
 *   │             │                          │
 *   │             │  <Outlet /> (page)       │
 *   │             │                          │
 *   └─────────────┴──────────────────────────┘
 */
export default function AppLayout() {
  return (
    <div className="app-layout">
      <Sidebar />
      <TopBar />
      <main className="app-layout__content">
        <Outlet />
      </main>
    </div>
  );
}
