import { roleCards, IconCheck } from '../data/landingContent';

export default function RolesSection() {
  return (
    <section id="roles" className="landing-roles">
      <div className="section-inner">
        <header className="section-header reveal">
          <p className="section-label">User Roles</p>
          <h2 className="section-title">Two roles. One platform.</h2>
          <p className="section-subtitle">
            PeerTayo supports everyone in the group, whether you submit evaluations or manage the process.
          </p>
        </header>

        <div className="roles-grid">
          {roleCards.map((card, index) => {
            const Icon = card.icon;
            return (
              <article key={card.role} className={`role-card ${card.tone} reveal ${index === 0 ? 'reveal-left' : 'reveal-right'}`}>
                <div className="role-icon">
                  <Icon size={22} />
                </div>
                <h3>{card.role}</h3>
                <p>{card.subtitle}</p>
                <ul>
                  {card.perks.map((perk) => (
                    <li key={perk}>
                      <span className="perk-check"><IconCheck size={12} /></span>
                      <span>{perk}</span>
                    </li>
                  ))}
                </ul>
              </article>
            );
          })}
        </div>
      </div>
    </section>
  );
}
