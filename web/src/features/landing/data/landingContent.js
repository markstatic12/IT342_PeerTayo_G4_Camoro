import {
  IconBolt,
  IconChart,
  IconCheck,
  IconClock,
  IconDocument,
  IconLock,
  IconMobile,
  IconRocket,
  IconShield,
  IconStar,
  IconTarget,
  IconTools,
  IconTrend,
  IconUser,
} from '../icons/LandingIcons';

export const navItems = [
  { id: 'home', label: 'Home' },
  { id: 'features', label: 'Features' },
  { id: 'about', label: 'About' },
];

export const heroStats = [
  { label: 'Evaluations Submitted', value: 500, suffix: '+' },
  { label: 'Active Groups', value: 120, suffix: '+' },
  { label: 'Satisfaction Rate', value: 98, suffix: '%' },
  { label: 'Roles Supported', value: 2, suffix: '' },
];

export const featureCards = [
  {
    title: 'Structured Evaluation Forms',
    desc: 'Facilitators build multi-criteria forms with custom questions, scoring rubrics, and open-ended feedback.',
    icon: IconDocument,
    tone: 'blue',
  },
  {
    title: 'Private Submissions',
    desc: 'Respondents submit evaluations confidentially, enabling honest feedback with identity protection.',
    icon: IconLock,
    tone: 'green',
  },
  {
    title: 'Real-Time Analytics',
    desc: 'Monitor response rates, score averages, and form health in one visual dashboard.',
    icon: IconChart,
    tone: 'orange',
  },
  {
    title: 'Deadline Management',
    desc: 'Set deadlines with urgency indicators, then extend or close forms without friction.',
    icon: IconClock,
    tone: 'yellow',
  },
  {
    title: 'Role Upgrade System',
    desc: 'Upgrade from respondent to facilitator to unlock advanced creation and oversight tools.',
    icon: IconRocket,
    tone: 'purple',
  },
  {
    title: 'Mobile-Ready Interface',
    desc: 'Touch-friendly workflows let users submit and review evaluations on any device.',
    icon: IconMobile,
    tone: 'red',
  },
];

export const roleCards = [
  {
    role: 'Respondent',
    tone: 'respondent',
    icon: IconUser,
    subtitle:
      'The default role for all users, focused on completing assigned evaluations and viewing personal results.',
    perks: [
      'Dashboard overview with pending tasks and score summaries',
      'Private submissions for impartial peer feedback',
      'My Results page with per-criterion breakdowns',
      'Completed forms history with timestamps',
      'Deadline-aware urgency indicators',
    ],
  },
  {
    role: 'Facilitator',
    tone: 'facilitator',
    icon: IconTools,
    subtitle:
      'An upgrade from Respondent that unlocks form creation, assignment controls, and evaluation oversight.',
    perks: [
      'Everything in Respondent with elevated permissions',
      'Create and publish forms with custom criteria',
      'Assign respondents and manage groups',
      'Monitor completion and zero-submission alerts',
      'Extend, close, or archive forms anytime',
    ],
  },
];

export const values = [
  {
    icon: IconTarget,
    title: 'Accuracy First',
    desc: 'Structured criteria reduce subjectivity and improve consistency across teams.',
  },
  {
    icon: IconShield,
    title: 'Privacy by Design',
    desc: 'Privacy is built into the core workflow, not added as an afterthought.',
  },
  {
    icon: IconBolt,
    title: 'Fast Setup',
    desc: 'Publish and assign an evaluation flow in minutes.',
  },
  {
    icon: IconTrend,
    title: 'Growth-Oriented',
    desc: 'Criterion-level feedback makes strengths and gaps immediately visible.',
  },
];

export const testimonialCards = [
  {
    text: 'PeerTayo gave our team objective proof of contribution. It made sprint retrospectives far more productive.',
    name: 'Jose Lorenzo',
    role: 'Project Facilitator',
    initials: 'JL',
    tone: 'orange',
  },
  {
    text: 'Seeing scores per criterion helped me improve specific skills instead of guessing what I lacked.',
    name: 'Anna Reyes',
    role: 'Respondent',
    initials: 'AR',
    tone: 'blue',
  },
  {
    text: 'Deadline monitoring and zero-submission alerts saved us from closing incomplete evaluations.',
    name: 'Maria Cruz',
    role: 'Class Facilitator',
    initials: 'MC',
    tone: 'green',
  },
];

export const footerSections = [
  {
    title: 'Platform',
    links: ['Dashboard', 'Pending Evaluations', 'My Results', 'Create Form', 'Mobile App'],
  },
  {
    title: 'Roles',
    links: ['Respondent Guide', 'Facilitator Guide', 'Role Upgrade', 'Permissions'],
  },
  {
    title: 'Company',
    links: ['About', 'Privacy Policy', 'Terms of Use', 'Contact'],
  },
];

export const metrics = [
  { value: 500, suffix: '+', label: 'Evaluations Completed', sub: 'Across all groups' },
  { value: 120, suffix: '+', label: 'Active Groups', sub: 'In classrooms and organizations' },
  { value: 10, suffix: '', label: 'Evaluation Criteria', sub: 'Per form, per evaluatee' },
  { value: 98, suffix: '%', label: 'Satisfaction Rate', sub: 'Facilitator NPS score' },
];

export const howSteps = [
  {
    title: 'Create an Evaluation Form',
    desc: 'Facilitators define criteria, timelines, and assignees in one setup flow.',
    meta: 'Facilitator role required',
  },
  {
    title: 'Respondents Submit Ratings',
    desc: 'Assigned respondents evaluate peers with private submissions and structured scoring.',
    meta: 'Private by default',
  },
  {
    title: 'Track Completion Live',
    desc: 'Facilitators monitor response rates and deadline risk in real time.',
    meta: 'Zero-submission alerts',
  },
  {
    title: 'Review Aggregated Results',
    desc: 'Teams receive criterion-level insights that guide improvement.',
    meta: 'Per-criterion breakdown',
  },
];

export const featureHighlights = [
  {
    num: '01',
    title: '10-Criteria Scoring Engine',
    desc: 'Evaluate peers across Communication, Teamwork, Leadership, Reliability, Problem Solving, Adaptability, Work Ethic, Professionalism, Creativity, and Overall Contribution with granular 1 to 5 scoring.',
  },
  {
    num: '02',
    title: 'Dual Dashboard Intelligence',
    desc: 'Respondents see personal scores and pending tasks while facilitators track form health, response rates, and group completion in real time.',
  },
];

export const proofBadges = ['Spring Boot', 'React', 'Kotlin', 'MySQL', 'REST API', 'JWT Auth'];

export const aboutStack = ['Spring Boot', 'React', 'Kotlin', 'MySQL', 'REST API', 'JWT Auth', 'Mobile-First'];

export const socialProofStars = [1, 2, 3, 4, 5].map((id) => ({ id, icon: IconStar }));

export { IconCheck };
