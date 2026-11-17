import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import ServerList from '../components/ServerList';
import ChannelList from '../components/ChannelList';
import MessagePane from '../components/MessagePane';
import MemberList from '../components/MemberList';
import { AddServerModal, CreateChannelModal, InviteModal } from '../components/Modals';

/**
 * Layout-ul principal: servere | canale | mesaje | membri.
 * Serverul si canalul selectat vin din URL (/servers/:serverId/:channelId).
 */
export default function ChatPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const params = useParams();
  const serverId = params.serverId ? Number(params.serverId) : null;
  const channelId = params.channelId ? Number(params.channelId) : null;

  const [servers, setServers] = useState([]);
  const [serversLoaded, setServersLoaded] = useState(false);
  const [channels, setChannels] = useState([]);
  const [members, setMembers] = useState([]);
  const [modal, setModal] = useState(null); // 'addServer' | 'createChannel' | 'invite'

  const reloadServers = useCallback(async () => {
    const list = await api.myServers();
    setServers(list);
    setServersLoaded(true);
    return list;
  }, []);

  useEffect(() => {
    reloadServers();
  }, [reloadServers]);

  // fara server in URL: mergem pe primul server al userului
  useEffect(() => {
    if (serversLoaded && serverId === null && servers.length > 0) {
      navigate(`/servers/${servers[0].id}`, { replace: true });
    }
  }, [serversLoaded, serverId, servers, navigate]);

  const reloadServerData = useCallback(async () => {
    if (serverId === null) return;
    try {
      const [ch, mem] = await Promise.all([api.channels(serverId), api.members(serverId)]);
      setChannels(ch);
      setMembers(mem);
    } catch {
      // 403/404: nu esti membru sau serverul nu mai exista
      setChannels([]);
      setMembers([]);
      navigate('/', { replace: true });
    }
  }, [serverId, navigate]);

  useEffect(() => {
    setChannels([]);
    setMembers([]);
    reloadServerData();
  }, [reloadServerData]);

  // fara canal in URL: primul canal text
  useEffect(() => {
    if (serverId !== null && channelId === null && channels.length > 0) {
      const firstText = channels.find((c) => c.type === 'TEXT') ?? channels[0];
      navigate(`/servers/${serverId}/${firstText.id}`, { replace: true });
    }
  }, [serverId, channelId, channels, navigate]);

  const server = servers.find((s) => s.id === serverId) ?? null;
  const channel = channels.find((c) => c.id === channelId) ?? null;
  const myRole = members.find((m) => m.userId === user.id)?.role;

  const onServerAdded = async (newServerId) => {
    setModal(null);
    await reloadServers();
    navigate(`/servers/${newServerId}`);
  };

  const onDeleteServer = async () => {
    if (!server || !window.confirm(`Stergi serverul "${server.name}"? Se sterg si toate canalele si mesajele.`)) return;
    await api.deleteServer(server.id);
    await reloadServers();
    navigate('/', { replace: true });
  };

  return (
    <div className="app">
      <ServerList servers={servers} activeId={serverId} onAdd={() => setModal('addServer')} />

      {server ? (
        <>
          <ChannelList
            server={server}
            channels={channels}
            activeId={channelId}
            myRole={myRole}
            onCreateChannel={() => setModal('createChannel')}
            onInvite={() => setModal('invite')}
            onDeleteServer={onDeleteServer}
          />
          <MessagePane channel={channel} myRole={myRole} />
          <MemberList members={members} />
        </>
      ) : (
        <div className="empty-state">
          {serversLoaded && (
            <>
              <h2>Nu esti pe niciun server inca</h2>
              <p className="muted">Creeaza unul sau intra pe unul cu un cod de invitatie.</p>
              <button className="primary" onClick={() => setModal('addServer')}>Adauga un server</button>
            </>
          )}
        </div>
      )}

      {modal === 'addServer' && <AddServerModal onClose={() => setModal(null)} onDone={onServerAdded} />}
      {modal === 'createChannel' && server && (
        <CreateChannelModal
          serverId={server.id}
          onClose={() => setModal(null)}
          onDone={async (created) => {
            setModal(null);
            await reloadServerData();
            if (created.type === 'TEXT') navigate(`/servers/${server.id}/${created.id}`);
          }}
        />
      )}
      {modal === 'invite' && server && <InviteModal server={server} onClose={() => setModal(null)} />}
    </div>
  );
}
