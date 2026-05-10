import { IconArrowRight } from '../icons/LandingIcons';

export default function CtaSection({ onRegister }) {
  return (
    <section id="cta" className="landing-cta">
      <div className="section-inner">
        <div className="cta-box reveal-scale">
          <div className="cta-glow" />
          <div className="cta-glow warm" />
          <div className="hero-badge">
            <span className="hero-badge-dot" />
            Free to get started · No credit card required
          </div>
          <h2>Ready to evaluate smarter?</h2>
          <p>
            Join teams already using PeerTayo to run fairer, clearer, and more transparent peer evaluations.
          </p>
          <div className="cta-actions">
            <button type="button" className="btn-hero-primary" onClick={onRegister}>
              Create Your Account
              <IconArrowRight size={14} />
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}
