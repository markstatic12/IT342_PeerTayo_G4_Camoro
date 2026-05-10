import { featureCards, featureHighlights, howSteps } from '../data/landingContent';
import { IconArrowRight, IconCheck, IconChart, IconClock, IconDocument } from '../icons/LandingIcons';

export default function FeaturesSection() {
  return (
    <section id="features" className="landing-features">
      <div className="landing-grid-bg faint" />
      <div className="section-inner">
        <header className="section-header reveal">
          <p className="section-label">Platform Features</p>
          <h2 className="section-title">Everything your team needs to evaluate well.</h2>
          <p className="section-subtitle">
            From private submissions to real-time analytics, PeerTayo equips both respondents and facilitators
            with the right tools.
          </p>
        </header>

        <div className="features-grid">
          {featureCards.map((item, index) => {
            const Icon = item.icon;
            return (
              <article key={item.title} className={`feature-card reveal delay-${(index % 4) + 1}`}>
                <div className={`feature-icon ${item.tone}`}>
                  <Icon size={20} />
                </div>
                <h3>{item.title}</h3>
                <p>{item.desc}</p>
              </article>
            );
          })}
        </div>

        <div className="features-highlight">
          {featureHighlights.map((item, index) => (
            <article key={item.num} className={`highlight-card reveal delay-${Math.min(index + 1, 2)}`}>
              <span className="highlight-num">{item.num}</span>
              <div className="highlight-content">
                <h3>{item.title}</h3>
                <p>{item.desc}</p>
              </div>
            </article>
          ))}
        </div>

        <div className="how-block" id="how-it-works">
          <p className="section-label reveal">Process Flow</p>
          <h3 className="how-title reveal delay-1">How PeerTayo Works</h3>
          <p className="how-subtitle reveal delay-2">Four steps from setup to insight, built for speed and accountability.</p>

          <div className="how-grid with-connector">
            <div className="how-connector" />
            {howSteps.map((step, index) => (
              <article key={step.title} className={`how-card reveal delay-${Math.min(index + 1, 4)}`}>
                <span className="how-icon-wrap">
                  {index === 0 && <IconDocument size={18} />}
                  {index === 1 && <IconClock size={18} />}
                  {index === 2 && <IconChart size={18} />}
                  {index === 3 && <IconCheck size={18} />}
                </span>
                <span className="how-step">Step {String(index + 1).padStart(2, '0')}</span>
                <h4>{step.title}</h4>
                <p>{step.desc}</p>
                <div className="how-meta">
                  <span>{step.meta}</span>
                  <IconArrowRight size={11} />
                </div>
              </article>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
