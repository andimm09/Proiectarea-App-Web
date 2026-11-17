import { useNavigate } from 'react-router-dom';

/** Bara din stanga cu iconitele serverelor (initialele numelui). */
export default function ServerList({ servers, activeId, onAdd }) {
  const navigate = useNavigate();

  return (
    <nav className="server-list">
      {servers.map((s) => (
        <button
          key={s.id}
          className={`server-icon ${s.id === activeId ? 'active' : ''}`}
          title={s.name}
          onClick={() => navigate(`/servers/${s.id}`)}
        >
          {initials(s.name)}
        </button>
      ))}
      <div className="server-separator" />
      <button className="server-icon add" title="Adauga un server" onClick={onAdd}>+</button>
    </nav>
  );
}

function initials(name) {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 3)
    .map((w) => w[0].toUpperCase())
    .join('');
}
