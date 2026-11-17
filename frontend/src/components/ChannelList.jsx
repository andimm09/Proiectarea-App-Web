import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

export default function ChannelList({ server, channels, activeId, myRole, onCreateChannel, onInvite, onDeleteServer }) {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  // aceleasi reguli ca in backend (Role.java); backend-ul verifica oricum, aici doar ascundem butoanele
  const canManageChannels = myRole === 'OWNER' || myRole === 'ADMIN';
  const isOwner = myRole === 'OWNER';

  const text = channels.filter((c) => c.type === 'TEXT');
  const voice = channels.filter((c) => c.type === 'VOICE');

  return (
    <aside className="channel-list">
      <header className="channel-header">
        <span className="server-name">{server.name}</span>
        <button className="icon-button" title="Invita oameni" onClick={onInvite}>👤+</button>
      </header>

      <div className="channel-scroll">
        <ChannelGroup title="Canale text" onAdd={canManageChannels ? onCreateChannel : null}>
          {text.map((c) => (
            <button
              key={c.id}
              className={`channel ${c.id === activeId ? 'active' : ''}`}
              onClick={() => navigate(`/servers/${server.id}/${c.id}`)}
            >
              <span className="channel-prefix">#</span> {c.name}
            </button>
          ))}
        </ChannelGroup>

        <ChannelGroup title="Canale voice" onAdd={canManageChannels ? onCreateChannel : null}>
          {voice.map((c) => (
            <button
              key={c.id}
              className={`channel ${c.id === activeId ? 'active' : ''}`}
              onClick={() => navigate(`/servers/${server.id}/${c.id}`)}
            >
              <span className="channel-prefix">🔊</span> {c.name}
            </button>
          ))}
        </ChannelGroup>

        {isOwner && (
          <button className="danger-link" onClick={onDeleteServer}>Sterge serverul</button>
        )}
      </div>

      <footer className="user-panel">
        <div className="avatar">{user.username[0].toUpperCase()}</div>
        <div className="user-panel-name">
          <div>{user.username}</div>
          <div className="muted small">{myRole ?? ''}</div>
        </div>
        <button className="icon-button" title="Logout" onClick={logout}>⎋</button>
      </footer>
    </aside>
  );
}

function ChannelGroup({ title, onAdd, children }) {
  return (
    <div className="channel-group">
      <div className="channel-group-title">
        <span>{title}</span>
        {onAdd && <button className="icon-button small" title="Creeaza canal" onClick={onAdd}>+</button>}
      </div>
      {children}
    </div>
  );
}
