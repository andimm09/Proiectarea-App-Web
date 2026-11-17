import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';

/** Aceeasi pagina pentru login si register. */
export default function AuthPage({ mode }) {
  const isRegister = mode === 'register';
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      if (isRegister) {
        await api.register(form.username, form.email, form.password);
      }
      await login(form.username, form.password);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={submit}>
        <div className="brand">
          <img className="brand-logo" src="/logo.svg" alt="" />
          Ecou
        </div>
        <h1>{isRegister ? 'Fa-ti cont pe Ecou' : 'Salut din nou 👋'}</h1>
        <p className="muted">
          {isRegister ? 'Un cont, toate serverele tale.' : 'Intra in cont ca sa continui conversatia.'}
        </p>

        <label>
          Username
          <input value={form.username} onChange={update('username')} autoFocus required />
        </label>
        {isRegister && (
          <label>
            Email
            <input type="email" value={form.email} onChange={update('email')} required />
          </label>
        )}
        <label>
          Parola
          <input type="password" value={form.password} onChange={update('password')} required
                 minLength={isRegister ? 8 : undefined} />
        </label>

        {error && <div className="error">{error}</div>}

        <button className="primary" disabled={busy}>
          {busy ? '...' : isRegister ? 'Creeaza contul' : 'Intra'}
        </button>

        <p className="muted small">
          {isRegister
            ? <>Ai deja cont? <Link to="/login">Login</Link></>
            : <>Esti nou pe aici? <Link to="/register">Fa-ti cont</Link></>}
        </p>
      </form>
    </div>
  );
}
