import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, getToken, setToken, setUnauthorizedHandler } from './api';

const AuthContext = createContext(null);

/** Tine minte userul logat; token-ul sta in localStorage ca sa ramai logat dupa refresh. */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(getToken()));

  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
    if (!getToken()) return;
    // avem token de la o sesiune anterioara: verificam ca inca e valid
    api.me()
      .then(setUser)
      .catch(logout)
      .finally(() => setLoading(false));
  }, [logout]);

  const login = async (username, password) => {
    const res = await api.login(username, password);
    setToken(res.accessToken);
    setUser(res.user);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
