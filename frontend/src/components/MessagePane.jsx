import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';

// Pana facem WebSocket, cerem mesajele noi la fiecare cateva secunde.
const POLL_MS = 3000;
const PAGE_SIZE = 50;
// mesajele aceluiasi autor la mai putin de 5 minute distanta se afiseaza grupate, fara header
const GROUP_MS = 5 * 60 * 1000;

export default function MessagePane({ channel, myRole }) {
  if (!channel) return <main className="message-pane" />;
  if (channel.type === 'VOICE') {
    return (
      <main className="message-pane">
        <ChannelTitle channel={channel} />
        <div className="empty-state">
          <h2>🔊 {channel.name}</h2>
          <p className="muted">Canalele voice vin curand (WebRTC).</p>
        </div>
      </main>
    );
  }
  // key = id-ul canalului: la schimbarea canalului se reseteaza tot state-ul
  return <TextChannel key={channel.id} channel={channel} myRole={myRole} />;
}

function ChannelTitle({ channel }) {
  return (
    <header className="message-header">
      <span className="channel-prefix">{channel.type === 'VOICE' ? '🔊' : '#'}</span>
      <strong>{channel.name}</strong>
    </header>
  );
}

function TextChannel({ channel, myRole }) {
  const { user } = useAuth();
  const [messages, setMessages] = useState(new Map()); // id -> mesaj
  const [olderPage, setOlderPage] = useState(1);
  const [hasMore, setHasMore] = useState(false);
  const [error, setError] = useState('');
  const scrollRef = useRef(null);
  const stickToBottom = useRef(true);

  const merge = useCallback((items) => {
    setMessages((prev) => {
      const next = new Map(prev);
      items.forEach((m) => next.set(m.id, m));
      return next;
    });
  }, []);

  // prima pagina + polling
  useEffect(() => {
    let cancelled = false;
    const load = async (first) => {
      try {
        const page = await api.messages(channel.id, 0, PAGE_SIZE);
        if (cancelled) return;
        merge(page.items);
        if (first) setHasMore(page.hasNext);
      } catch (err) {
        if (!cancelled) setError(err.message);
      }
    };
    load(true);
    const timer = setInterval(() => load(false), POLL_MS);
    return () => {
      cancelled = true;
      clearInterval(timer);
    };
  }, [channel.id, merge]);

  const loadOlder = async () => {
    const el = scrollRef.current;
    const heightBefore = el.scrollHeight;
    const page = await api.messages(channel.id, olderPage, PAGE_SIZE);
    stickToBottom.current = false;
    merge(page.items);
    setHasMore(page.hasNext);
    setOlderPage(olderPage + 1);
    // pastram pozitia de scroll dupa ce adaugam mesaje deasupra
    requestAnimationFrame(() => {
      el.scrollTop += el.scrollHeight - heightBefore;
    });
  };

  const sorted = [...messages.values()].sort((a, b) => a.id - b.id);

  // daca userul e jos de tot, ramanem jos cand vin mesaje noi
  useLayoutEffect(() => {
    const el = scrollRef.current;
    if (el && stickToBottom.current) el.scrollTop = el.scrollHeight;
  }, [sorted.length]);

  const onScroll = () => {
    const el = scrollRef.current;
    stickToBottom.current = el.scrollHeight - el.scrollTop - el.clientHeight < 40;
  };

  const send = async (content) => {
    stickToBottom.current = true;
    const msg = await api.sendMessage(channel.id, content);
    merge([msg]);
  };

  const canDeleteAny = myRole === 'OWNER' || myRole === 'ADMIN';

  return (
    <main className="message-pane">
      <ChannelTitle channel={channel} />

      <div className="messages" ref={scrollRef} onScroll={onScroll}>
        {hasMore && (
          <button className="load-older" onClick={loadOlder}>Incarca mesaje mai vechi</button>
        )}
        {!hasMore && (
          <div className="channel-start">
            <h2># Bine ai venit pe #{channel.name}!</h2>
            <p className="muted">Acesta e inceputul canalului.</p>
          </div>
        )}
        {sorted.map((m, i) => (
          <MessageRow
            key={m.id}
            message={m}
            grouped={isGrouped(sorted[i - 1], m)}
            canEdit={m.authorId === user.id && !m.deleted}
            canDelete={!m.deleted && (m.authorId === user.id || canDeleteAny)}
            onChanged={(updated) => merge([updated])}
          />
        ))}
      </div>

      {error && <div className="error inline">{error}</div>}
      <Composer channelName={channel.name} onSend={send} />
    </main>
  );
}

function isGrouped(prev, m) {
  return prev && prev.authorId === m.authorId && !prev.deleted
    && new Date(m.sentAt) - new Date(prev.sentAt) < GROUP_MS;
}

function MessageRow({ message: m, grouped, canEdit, canDelete, onChanged }) {
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(m.content ?? '');

  const saveEdit = async () => {
    const content = draft.trim();
    if (!content || content === m.content) return setEditing(false);
    onChanged(await api.editMessage(m.id, content));
    setEditing(false);
  };

  const remove = async () => {
    if (!window.confirm('Stergi mesajul?')) return;
    await api.deleteMessage(m.id);
    onChanged({ ...m, deleted: true, content: null });
  };

  return (
    <div className={`message ${grouped ? 'grouped' : ''}`}>
      {grouped ? <div className="avatar-spacer" /> : <div className="avatar">{m.authorUsername[0].toUpperCase()}</div>}
      <div className="message-body">
        {!grouped && (
          <div className="message-meta">
            <strong>{m.authorUsername}</strong>
            <span className="muted small">{formatTime(m.sentAt)}</span>
          </div>
        )}
        {m.deleted ? (
          <div className="muted italic">mesaj sters</div>
        ) : editing ? (
          <input
            className="edit-input"
            value={draft}
            autoFocus
            onChange={(e) => setDraft(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') saveEdit();
              if (e.key === 'Escape') setEditing(false);
            }}
          />
        ) : (
          <div className="message-content">
            {m.content}
            {m.editedAt && <span className="muted small"> (editat)</span>}
          </div>
        )}
      </div>
      {!editing && (canEdit || canDelete) && (
        <div className="message-actions">
          {canEdit && <button title="Editeaza" onClick={() => { setDraft(m.content); setEditing(true); }}>✏️</button>}
          {canDelete && <button title="Sterge" onClick={remove}>🗑️</button>}
        </div>
      )}
    </div>
  );
}

function Composer({ channelName, onSend }) {
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    const content = text.trim();
    if (!content || busy) return;
    setBusy(true);
    try {
      await onSend(content);
      setText('');
    } finally {
      setBusy(false);
    }
  };

  return (
    <form className="composer" onSubmit={submit}>
      <input
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder={`Scrie pe #${channelName}`}
        maxLength={2000}
      />
    </form>
  );
}

function formatTime(iso) {
  const d = new Date(iso);
  const today = new Date().toDateString() === d.toDateString();
  const time = d.toLocaleTimeString('ro-RO', { hour: '2-digit', minute: '2-digit' });
  return today ? `azi la ${time}` : `${d.toLocaleDateString('ro-RO')} ${time}`;
}
