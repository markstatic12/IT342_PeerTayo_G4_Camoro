import { useEffect, useState } from 'react';

const navSectionMap = {
  home: ['home'],
  features: ['features', 'how-it-works', 'roles'],
  about: ['about', 'cta'],
};

function formatCounterValue(value, suffix) {
  if (suffix === '%') return `${value}%`;
  if (suffix === '+') return `${value}+`;
  return `${value}`;
}

export default function useLandingEffects() {
  const [progress, setProgress] = useState(0);
  const [isScrolled, setIsScrolled] = useState(false);
  const [activeNav, setActiveNav] = useState('home');

  useEffect(() => {
    function onScroll() {
      const scrollTop = window.scrollY;
      const docHeight = document.documentElement.scrollHeight - window.innerHeight;
      const pct = docHeight > 0 ? (scrollTop / docHeight) * 100 : 0;

      setProgress(Math.max(0, Math.min(100, pct)));
      setIsScrolled(scrollTop > 40);

      const ids = ['home', 'features', 'how-it-works', 'roles', 'about', 'cta'];
      const scrollPos = scrollTop + 140;
      let current = 'home';

      ids.forEach((id) => {
        const element = document.getElementById(id);
        if (element && element.offsetTop <= scrollPos) current = id;
      });

      let tab = 'home';
      Object.entries(navSectionMap).forEach(([key, values]) => {
        if (values.includes(current)) tab = key;
      });

      setActiveNav(tab);
    }

    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    const nodes = document.querySelectorAll('.reveal, .reveal-left, .reveal-right, .reveal-scale');

    nodes.forEach((node) => {
      const rect = node.getBoundingClientRect();
      if (rect.top < window.innerHeight) node.classList.add('visible');
    });

    const revealObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible');
            revealObserver.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
    );

    nodes.forEach((node) => revealObserver.observe(node));

    return () => revealObserver.disconnect();
  }, []);

  useEffect(() => {
    const counters = document.querySelectorAll('[data-count]');

    function animateCount(el, target, suffix = '') {
      let start = 0;
      const duration = 1600;
      const step = target / (duration / 16);

      const timer = window.setInterval(() => {
        start += step;
        if (start >= target) {
          el.textContent = formatCounterValue(target, suffix);
          window.clearInterval(timer);
          return;
        }

        el.textContent = formatCounterValue(Math.floor(start), suffix);
      }, 16);
    }

    const counterObserver = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;

          const target = Number.parseInt(entry.target.dataset.count ?? '0', 10);
          const suffix = entry.target.dataset.suffix ?? '';
          animateCount(entry.target, target, suffix);
          counterObserver.unobserve(entry.target);
        });
      },
      { threshold: 0.5 }
    );

    counters.forEach((node) => counterObserver.observe(node));
    return () => counterObserver.disconnect();
  }, []);

  function goSection(id) {
    const section = document.getElementById(id);
    if (section) section.scrollIntoView({ behavior: 'smooth' });
  }

  return { progress, isScrolled, activeNav, goSection };
}
