import { useNavigate } from 'react-router-dom';
import LandingNav from './components/LandingNav';
import HeroSection from './components/HeroSection';
import FeaturesSection from './components/FeaturesSection';
import RolesSection from './components/RolesSection';
import AboutSection from './components/AboutSection';
import CtaSection from './components/CtaSection';
import LandingFooter from './components/LandingFooter';
import useLandingEffects from './hooks/useLandingEffects';
import './LandingPage.css';

export default function LandingPage() {
  const navigate = useNavigate();
  const { activeNav, goSection, isScrolled, progress } = useLandingEffects();

  return (
    <div className="landing-page">
      <div className="scroll-progress" style={{ width: `${progress}%` }} />

      <LandingNav
        activeNav={activeNav}
        isScrolled={isScrolled}
        onGoSection={goSection}
        onRegister={() => navigate('/register')}
      />

      <HeroSection onGoSection={goSection} onRegister={() => navigate('/register')} />
      <FeaturesSection />
      <RolesSection />
      <AboutSection />
      <CtaSection onRegister={() => navigate('/register')} />
      <LandingFooter />
    </div>
  );
}
