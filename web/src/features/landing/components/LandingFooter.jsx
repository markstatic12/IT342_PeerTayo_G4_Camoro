import { footerSections, proofBadges } from '../data/landingContent';
import { IconLock } from '../icons/LandingIcons';
import { LogoIcon } from '../../../shared/components/icons/Icons';

export default function LandingFooter() {
  return (
    <footer className="landing-footer">
      <div className="footer-inner">
        <div className="footer-brand">
          <div className="footer-logo">
            <span className="footer-logo-icon"><LogoIcon size={20} /></span>
            <span className="footer-logo-text">Peer<span>Tayo</span></span>
          </div>
          <p>
            A peer evaluation platform for collaborative groups, classrooms, and organizations that care about
            meaningful feedback.
          </p>
          <div className="footer-badges-stack">
            {proofBadges.slice(0, 4).map((item) => (
              <span key={item}>{item}</span>
            ))}
          </div>
        </div>

        {footerSections.map((section) => (
          <div key={section.title}>
            <h4>{section.title}</h4>
            <ul>
              {section.links.map((link) => (
                <li key={link}><a href="#">{link}</a></li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      <div className="footer-bottom">
        <p>© 2026 PeerTayo. Built with Spring Boot, React, and Kotlin.</p>
        <div className="footer-status">
          <span>v1.0.0</span>
          <span className="with-icon"><IconLock size={11} />Private</span>
          <span className="live">Live</span>
        </div>
      </div>
    </footer>
  );
}
