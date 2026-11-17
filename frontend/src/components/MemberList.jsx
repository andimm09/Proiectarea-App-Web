const ROLE_TITLES = { OWNER: 'Owner', ADMIN: 'Admini', MEMBER: 'Membri' };

/** Lista din dreapta, grupata pe roluri. */
export default function MemberList({ members }) {
  return (
    <aside className="member-list">
      {['OWNER', 'ADMIN', 'MEMBER'].map((role) => {
        const group = members.filter((m) => m.role === role);
        if (group.length === 0) return null;
        return (
          <div key={role}>
            <div className="channel-group-title">{ROLE_TITLES[role]} — {group.length}</div>
            {group.map((m) => (
              <div key={m.userId} className="member">
                <div className="avatar">{m.username[0].toUpperCase()}</div>
                <span>{m.nickname || m.username}</span>
                {role === 'OWNER' && <span title="Owner">👑</span>}
              </div>
            ))}
          </div>
        );
      })}
    </aside>
  );
}
