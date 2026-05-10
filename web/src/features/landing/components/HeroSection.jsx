import { heroStats } from '../data/landingContent';
import {
  IconArrowRight,
  IconBell,
  IconChart,
  IconCheck,
  IconClock,
  IconDocument,
  IconPlay,
  IconTrend,
  IconUser,
} from '../icons/LandingIcons';
import { LogoIcon } from '../../../shared/components/icons/Icons';

export default function HeroSection({ onGoSection, onRegister }) {
  return (
    <section id="home" className="landing-hero">
      <div className="landing-grid-bg" />
      <div className="landing-hero-glow" />
      <div className="landing-hero-glow-secondary" />

      <div className="landing-hero-inner">
        <div className="hero-badge reveal">
          <span className="hero-badge-dot" />
          Structured · Private · Insightful
        </div>

        <h1 className="hero-title reveal delay-1">
          Peer Evaluation.
          <br />
          <span className="accent">Reimagined</span> for
          <br />
          Real <span className="accent-warm">Teams.</span>
        </h1>

        <p className="hero-subtitle reveal delay-2">
          PeerTayo transforms how teams assess each other with structured, private, and actionable feedback.
          Built for classrooms, organizations, and collaborative groups.
        </p>

        <div className="hero-actions reveal delay-3">
          <button type="button" className="btn-hero-primary" onClick={onRegister}>
            Get Started Free
            <IconArrowRight size={14} />
          </button>
          <button type="button" className="btn-hero-ghost" onClick={() => onGoSection('features')}>
            <IconPlay size={13} />
            See How It Works
          </button>
        </div>

        <div className="hero-stats reveal delay-4">
          {heroStats.map((stat) => (
            <div key={stat.label} className="hero-stat">
              <span className="hero-stat-value" data-count={stat.value} data-suffix={stat.suffix}>0</span>
              <span className="hero-stat-label">{stat.label}</span>
            </div>
          ))}
        </div>

        <div className="hero-preview reveal delay-5">
          <div className="hero-preview-header">
            <div className="preview-brand">
              <span className="preview-brand-icon"><LogoIcon size={16} /></span>
              <span>Peer<span>Tayo</span></span>
            </div>
            <div className="preview-header-actions">
              <span className="preview-bell">
                <IconBell size={12} />
                <em>3</em>
              </span>
              <span className="preview-user-chip">
                <strong>MA</strong>
                <small>Facilitator</small>
              </span>
            </div>
          </div>
          <div className="hero-preview-body">
            <aside className="preview-sidebar">
              <div className="preview-panel-title">My Activity</div>
              <div className="preview-item active"><IconChart size={11} />Dashboard</div>
              <div className="preview-item"><IconClock size={11} />Pending Evaluations</div>
              <div className="preview-item"><IconTrend size={11} />My Results</div>
              <div className="preview-item"><IconCheck size={11} />My Completed Forms</div>
              <div className="preview-divider" />
              <div className="preview-panel-title compact">Manage</div>
              <div className="preview-item"><IconDocument size={11} />Forms Created</div>
              <div className="preview-facilitator-banner">
                <p>You are a Facilitator.</p>
                <small>Create and manage evaluations.</small>
              </div>
              <div className="preview-item"><IconUser size={11} />Settings</div>
            </aside>
            <main className="preview-main">
              <div className="preview-banner">
                <div>
                  <h3>Hello, Mark Anton</h3>
                  <p>Saturday, April 4, 2026</p>
                </div>
                <span>2 evaluations due in 2 days</span>
              </div>
              <div className="preview-kpis">
                <article>
                  <strong>4.6</strong>
                  <span>Overall Avg Score</span>
                  <small>+0.5 from last eval</small>
                </article>
                <article>
                  <strong>5</strong>
                  <span>Pending Evaluations</span>
                  <small>2 due soon</small>
                </article>
                <article>
                  <strong>8</strong>
                  <span>Evaluations Submitted</span>
                  <small>100% completion rate</small>
                </article>
                <article>
                  <strong>3</strong>
                  <span>Forms Created</span>
                  <small>2 still active</small>
                </article>
              </div>
              <div className="preview-graph">
                <div className="graph-header">
                  <h4>My Performance Analytics</h4>
                  <span>Above 4.0 goal</span>
                </div>
                <svg viewBox="0 0 560 90" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Performance chart">
                  <path d="M28,14 C70,20 100,38 140,34 C180,30 210,16 252,20 C294,24 322,14 364,26 C406,38 434,46 476,38 C504,32 532,50 532,62" fill="none" stroke="#3b82f6" strokeWidth="2" strokeLinecap="round" />
                </svg>
              </div>
            </main>

            <aside className="preview-activity">
              <div className="activity-head">
                <h4>Recent Activity</h4>
                <span>Last 7 days</span>
              </div>
              <div className="activity-list">
                <article>
                  <span className="dot green" />
                  <div>
                    <p>Submitted evaluation for Mark Anton Camoro</p>
                    <small>Today, 9:14 AM</small>
                  </div>
                </article>
                <article>
                  <span className="dot blue" />
                  <div>
                    <p>Assigned in SIA G4 Sprint 3 by Jay Lord Bayonas</p>
                    <small>Today, 8:02 AM</small>
                  </div>
                </article>
                <article>
                  <span className="dot orange" />
                  <div>
                    <p>Deadline extended for Q1 Team Review to Apr 6</p>
                    <small>Yesterday, 4:30 PM</small>
                  </div>
                </article>
                <article>
                  <span className="dot red" />
                  <div>
                    <p>SIA G4 Sprint 2 eval closed, results available</p>
                    <small>Mar 28, 11:59 PM</small>
                  </div>
                </article>
              </div>
            </aside>
          </div>
        </div>
      </div>
    </section>
  );
}
