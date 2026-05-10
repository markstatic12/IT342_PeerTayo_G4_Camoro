import { aboutStack, metrics, socialProofStars, testimonialCards, values } from '../data/landingContent';
import { IconQuote } from '../icons/LandingIcons';

export default function AboutSection() {
  return (
    <section id="about" className="landing-about">
      <div className="section-inner">
        <div className="about-layout">
          <div className="about-left reveal-left">
            <p className="section-label">About PeerTayo</p>
            <h2 className="section-title">Built for groups that want honest, structured feedback.</h2>
            <p className="section-subtitle">
              PeerTayo solves a common challenge in team environments: collecting feedback that is fair, measurable,
              and actionable.
            </p>
            <div className="tag-row">
              {aboutStack.map((item, index) => (
                <span key={item} className={`tag ${index < 4 ? 'active' : ''}`}>{item}</span>
              ))}
            </div>
          </div>

          <article className="about-card reveal-right delay-1">
            <h3>Our Mission</h3>
            <p>
              Every team member deserves meaningful feedback, not vague impressions. PeerTayo creates a workflow
              where groups evaluate each other fairly, privately, and consistently.
            </p>
            <div className="value-grid">
              {values.map((value) => {
                const Icon = value.icon;
                return (
                  <div key={value.title} className="value-item">
                    <span className="value-icon"><Icon size={18} /></span>
                    <h4>{value.title}</h4>
                    <p>{value.desc}</p>
                  </div>
                );
              })}
            </div>
          </article>
        </div>

        <div className="proof-block">
          <p className="section-label center reveal">By the Numbers</p>
          <div className="proof-grid">
            {metrics.map((metric, index) => (
              <article key={metric.label} className={`proof-card reveal delay-${Math.min(index + 1, 4)}`}>
                <strong data-count={metric.value} data-suffix={metric.suffix}>0</strong>
                <h4>{metric.label}</h4>
                <p>{metric.sub}</p>
              </article>
            ))}
          </div>
        </div>

        <div className="testimonials-block">
          <p className="section-label center reveal">What People Say</p>
          <div className="testimonials-grid">
            {testimonialCards.map((item, index) => (
              <article key={item.name} className={`testimonial-card reveal delay-${Math.min(index + 1, 3)}`}>
                <span className="quote-icon"><IconQuote size={18} /></span>
                <div className="stars-row">
                  {socialProofStars.map((star) => {
                    const StarIcon = star.icon;
                    return <StarIcon key={star.id} size={12} />;
                  })}
                </div>
                <p>{item.text}</p>
                <div className="author">
                  <span className={`avatar ${item.tone}`}>{item.initials}</span>
                  <div>
                    <h5>{item.name}</h5>
                    <small>{item.role}</small>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
