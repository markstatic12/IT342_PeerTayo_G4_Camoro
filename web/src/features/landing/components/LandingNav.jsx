import { IconArrowRight } from '../icons/LandingIcons';
import { LogoIcon } from '../../../shared/components/icons/Icons';
import { navItems } from '../data/landingContent';

export default function LandingNav({ activeNav, isScrolled, onGoSection, onRegister }) {
  return (
    <nav className={`landing-nav ${isScrolled ? 'scrolled' : ''}`}>
      <button type="button" className="landing-brand" onClick={() => onGoSection('home')}>
        <span className="landing-brand-icon" aria-hidden="true">
          <LogoIcon size={26} />
        </span>
        <span className="landing-brand-text">Peer<span>Tayo</span></span>
      </button>

      <div className="landing-nav-links" role="tablist" aria-label="Landing sections">
        {navItems.map((item) => (
          <button
            key={item.id}
            type="button"
            className={`landing-nav-link ${activeNav === item.id ? 'active' : ''}`}
            onClick={() => onGoSection(item.id)}
          >
            {item.label}
          </button>
        ))}
      </div>

      <div className="landing-nav-actions">
        <button type="button" className="btn-nav-primary" onClick={onRegister}>
          Get Started
          <IconArrowRight size={13} />
        </button>
      </div>
    </nav>
  );
}
