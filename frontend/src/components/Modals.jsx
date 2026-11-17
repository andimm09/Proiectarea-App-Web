import { useEffect, useState } from 'react';
import { api } from '../api';

function Modal({ title, onClose, children }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [onClose]);

  return (
    <div className="modal-backdrop" onMouseDown={onClose}>
      <div className="modal" onMouseDown={(e) => e.stopPropagation()}>
        <h2>{title}</h2>
        {children}
      </div>
    </div>
  );
}

/** Butonul "+" din stanga: creezi un server nou sau intri pe unul cu cod de invitatie. */
export function AddServerModal({ onClose, onDone }) {
  const [tab, setTab] = useState('create');
  const [value, setValue] = useState('');
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (tab === 'create') {
        const server = await api.createServer(value.trim());
        onDone(server.id);
      } else {
        // se poate lipi si un link intreg, luam doar ultima parte
        const code = value.trim().split('/').pop();
        await api.joinInvite(code);
        // serverele vin ordonate dupa data intrarii, deci cel nou e ultimul
        const servers = await api.myServers();
        onDone(servers[servers.length - 1].id);
      }
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <Modal title={tab === 'create' ? 'Creeaza un server' : 'Intra pe un server'} onClose={onClose}>
      <div className="tabs">
        <button className={tab === 'create' ? 'active' : ''} onClick={() => { setTab('create'); setValue(''); setError(''); }}>
          Creeaza
        </button>
        <button className={tab === 'join' ? 'active' : ''} onClick={() => { setTab('join'); setValue(''); setError(''); }}>
          Am un cod de invitatie
        </button>
      </div>
      <form onSubmit={submit}>
        <label>
          {tab === 'create' ? 'Numele serverului' : 'Cod de invitatie'}
          <input value={value} onChange={(e) => setValue(e.target.value)} autoFocus required
                 placeholder={tab === 'create' ? 'Serverul lui ...' : 'ex: hTKzmp7a'} maxLength={100} />
        </label>
        {error && <div className="error">{error}</div>}
        <div className="modal-actions">
          <button type="button" onClick={onClose}>Anuleaza</button>
          <button className="primary">{tab === 'create' ? 'Creeaza' : 'Intra'}</button>
        </div>
      </form>
    </Modal>
  );
}

export function CreateChannelModal({ serverId, onClose, onDone }) {
  const [name, setName] = useState('');
  const [type, setType] = useState('TEXT');
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      // canalele text sunt cu litere mici si cratime
      const finalName = type === 'TEXT' ? name.trim().toLowerCase().replace(/\s+/g, '-') : name.trim();
      onDone(await api.createChannel(serverId, finalName, type));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <Modal title="Creeaza canal" onClose={onClose}>
      <form onSubmit={submit}>
        <div className="radio-group">
          <label className={type === 'TEXT' ? 'selected' : ''}>
            <input type="radio" checked={type === 'TEXT'} onChange={() => setType('TEXT')} /> # Text
          </label>
          <label className={type === 'VOICE' ? 'selected' : ''}>
            <input type="radio" checked={type === 'VOICE'} onChange={() => setType('VOICE')} /> 🔊 Voice
          </label>
        </div>
        <label>
          Numele canalului
          <input value={name} onChange={(e) => setName(e.target.value)} autoFocus required maxLength={100} />
        </label>
        {error && <div className="error">{error}</div>}
        <div className="modal-actions">
          <button type="button" onClick={onClose}>Anuleaza</button>
          <button className="primary">Creeaza</button>
        </div>
      </form>
    </Modal>
  );
}

export function InviteModal({ server, onClose }) {
  const [invite, setInvite] = useState(null);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    api.createInvite(server.id, { expiresInHours: 24 })
      .then(setInvite)
      .catch((err) => setError(err.message));
  }, [server.id]);

  const copy = async () => {
    await navigator.clipboard.writeText(invite.code);
    setCopied(true);
  };

  return (
    <Modal title={`Invita prieteni pe ${server.name}`} onClose={onClose}>
      {error && <div className="error">{error}</div>}
      {invite && (
        <>
          <p className="muted">Trimite codul asta. E valabil 24 de ore.</p>
          <div className="invite-box">
            <code>{invite.code}</code>
            <button className="primary" onClick={copy}>{copied ? 'Copiat!' : 'Copiaza'}</button>
          </div>
        </>
      )}
      <div className="modal-actions">
        <button onClick={onClose}>Gata</button>
      </div>
    </Modal>
  );
}
