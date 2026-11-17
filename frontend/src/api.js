// Toate apelurile catre backend trec pe aici: adauga token-ul si transforma erorile in mesaje.

const TOKEN_KEY = 'token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

// AuthProvider seteaza aici ce se intampla cand token-ul nu mai e valid (401)
let onUnauthorized = () => {};
export function setUnauthorizedHandler(handler) {
  onUnauthorized = handler;
}

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

async function request(method, url, body) {
  const headers = {};
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  const res = await fetch(url, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);

  if (!res.ok) {
    // la login gresit tot 401 primim, dar acolo nu vrem logout automat
    if (res.status === 401 && token) onUnauthorized();
    const message = data?.detail || defaultMessage(res.status);
    throw new ApiError(res.status, message);
  }
  return data;
}

function defaultMessage(status) {
  if (status === 401) return 'Nu esti logat';
  if (status === 403) return 'Nu ai acces';
  if (status === 404) return 'Nu exista';
  return `Eroare ${status}`;
}

export const api = {
  register: (username, email, password) => request('POST', '/api/auth/register', { username, email, password }),
  login: (username, password) => request('POST', '/api/auth/login', { username, password }),
  me: () => request('GET', '/api/auth/me'),

  myServers: () => request('GET', '/api/users/me/servers'),
  createServer: (name) => request('POST', '/api/servers', { name }),
  deleteServer: (serverId) => request('DELETE', `/api/servers/${serverId}`),
  members: (serverId) => request('GET', `/api/servers/${serverId}/members`),
  channels: (serverId) => request('GET', `/api/servers/${serverId}/channels`),
  createChannel: (serverId, name, type) => request('POST', `/api/servers/${serverId}/channels`, { name, type }),
  createInvite: (serverId, options = {}) => request('POST', `/api/servers/${serverId}/invites`, options),
  joinInvite: (code) => request('POST', `/api/invites/${encodeURIComponent(code)}/join`),

  messages: (channelId, page = 0, size = 50) =>
    request('GET', `/api/channels/${channelId}/messages?page=${page}&size=${size}`),
  sendMessage: (channelId, content) => request('POST', `/api/channels/${channelId}/messages`, { content }),
  editMessage: (messageId, content) => request('PATCH', `/api/messages/${messageId}`, { content }),
  deleteMessage: (messageId) => request('DELETE', `/api/messages/${messageId}`),
};
