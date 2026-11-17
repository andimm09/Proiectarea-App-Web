import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth';
import AuthPage from './pages/AuthPage';
import ChatPage from './pages/ChatPage';

export default function App() {
  const { user, loading } = useAuth();

  if (loading) return <div className="fullscreen-center">Se incarca...</div>;

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" /> : <AuthPage mode="login" />} />
      <Route path="/register" element={user ? <Navigate to="/" /> : <AuthPage mode="register" />} />
      {/* rutele de mai jos sunt doar pentru useri logati */}
      <Route path="/servers/:serverId/:channelId?" element={user ? <ChatPage /> : <Navigate to="/login" />} />
      <Route path="*" element={user ? <ChatPage /> : <Navigate to="/login" />} />
    </Routes>
  );
}
