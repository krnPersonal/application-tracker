import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Briefcase, LogOut, Plus, Save, Trash2 } from 'lucide-react';
import { api } from './api/client';
import './styles.css';

const emptyForm = {
  company: '',
  title: '',
  status: 'SAVED',
  appliedDate: '',
  jobUrl: '',
  notes: ''
};

function App() {
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [user, setUser] = useState(null);
  const [applications, setApplications] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({ firstName: '', lastName: '', email: '', password: '' });
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const statusOptions = ['SAVED', 'APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED', 'WITHDRAWN'];

  const statusCounts = useMemo(() => dashboard?.statusCounts || {}, [dashboard]);

  useEffect(() => {
    if (!token) return;
    loadData();
  }, [token]);

  async function loadData() {
    try {
      setLoading(true);
      const [me, dash, apps] = await Promise.all([
        api('/api/me', { token }),
        api('/api/dashboard', { token }),
        api('/api/applications', { token })
      ]);
      setUser(me);
      setDashboard(dash);
      setApplications(apps);
    } catch (error) {
      setMessage(error.message);
      logout();
    } finally {
      setLoading(false);
    }
  }

  async function submitAuth(event) {
    event.preventDefault();
    setMessage('');
    const path = authMode === 'login' ? '/api/auth/login' : '/api/auth/register';
    try {
      const response = await api(path, { method: 'POST', body: authForm });
      localStorage.setItem('token', response.token);
      setToken(response.token);
      setUser(response.user);
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function submitApplication(event) {
    event.preventDefault();
    setMessage('');
    const payload = { ...form, appliedDate: form.appliedDate || null };
    const path = editingId ? `/api/applications/${editingId}` : '/api/applications';
    const method = editingId ? 'PUT' : 'POST';
    try {
      await api(path, { method, token, body: payload });
      setForm(emptyForm);
      setEditingId(null);
      await loadData();
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function deleteApplication(id) {
    await api(`/api/applications/${id}`, { method: 'DELETE', token });
    await loadData();
  }

  function editApplication(application) {
    setEditingId(application.id);
    setForm({
      company: application.company,
      title: application.title,
      status: application.status,
      appliedDate: application.appliedDate || '',
      jobUrl: application.jobUrl || '',
      notes: application.notes || ''
    });
  }

  function logout() {
    localStorage.removeItem('token');
    setToken('');
    setUser(null);
    setApplications([]);
    setDashboard(null);
  }

  if (!token) {
    return (
      <main className="auth-shell">
        <section className="auth-panel">
          <div className="brand">
            <Briefcase size={34} />
            <div>
              <h1>Application Tracker</h1>
              <p>Track job applications from saved role to offer.</p>
            </div>
          </div>
          <div className="tabs">
            <button className={authMode === 'login' ? 'active' : ''} onClick={() => setAuthMode('login')}>Login</button>
            <button className={authMode === 'register' ? 'active' : ''} onClick={() => setAuthMode('register')}>Register</button>
          </div>
          <form onSubmit={submitAuth} className="stack">
            {authMode === 'register' && (
              <div className="grid two">
                <label>First name<input value={authForm.firstName} onChange={event => setAuthForm({ ...authForm, firstName: event.target.value })} required /></label>
                <label>Last name<input value={authForm.lastName} onChange={event => setAuthForm({ ...authForm, lastName: event.target.value })} required /></label>
              </div>
            )}
            <label>Email<input type="email" value={authForm.email} onChange={event => setAuthForm({ ...authForm, email: event.target.value })} required /></label>
            <label>Password<input type="password" minLength="8" value={authForm.password} onChange={event => setAuthForm({ ...authForm, password: event.target.value })} required /></label>
            {message && <p className="error">{message}</p>}
            <button className="primary" type="submit">{authMode === 'login' ? 'Login' : 'Create account'}</button>
          </form>
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <div className="brand compact"><Briefcase /><strong>Application Tracker</strong></div>
        <div className="userbar">
          <span>{user?.firstName} {user?.lastName}</span>
          <button className="icon-button" onClick={logout} title="Logout"><LogOut size={18} /></button>
        </div>
      </header>

      <section className="hero">
        <h1>Job Applications</h1>
        <p>Organize every role, status, link, and note in one focused workflow.</p>
      </section>

      <section className="metrics">
        <article><span>Total</span><strong>{dashboard?.totalApplications || 0}</strong></article>
        {statusOptions.map(status => <article key={status}><span>{status}</span><strong>{statusCounts[status] || 0}</strong></article>)}
      </section>

      <section className="workspace">
        <form className="editor" onSubmit={submitApplication}>
          <h2>{editingId ? 'Edit Application' : 'New Application'}</h2>
          <div className="grid two">
            <label>Company<input value={form.company} onChange={event => setForm({ ...form, company: event.target.value })} required /></label>
            <label>Title<input value={form.title} onChange={event => setForm({ ...form, title: event.target.value })} required /></label>
          </div>
          <div className="grid two">
            <label>Status<select value={form.status} onChange={event => setForm({ ...form, status: event.target.value })}>{statusOptions.map(status => <option key={status}>{status}</option>)}</select></label>
            <label>Applied date<input type="date" value={form.appliedDate} onChange={event => setForm({ ...form, appliedDate: event.target.value })} /></label>
          </div>
          <label>URL<input value={form.jobUrl} onChange={event => setForm({ ...form, jobUrl: event.target.value })} /></label>
          <label>Notes<textarea rows="4" value={form.notes} onChange={event => setForm({ ...form, notes: event.target.value })} /></label>
          {message && <p className="error">{message}</p>}
          <button className="primary" type="submit"><Save size={17} />{editingId ? 'Save Changes' : 'Add Application'}</button>
        </form>

        <section className="list">
          <div className="list-title">
            <h2>Applications</h2>
            <button className="secondary" onClick={() => { setForm(emptyForm); setEditingId(null); }}><Plus size={16} />New</button>
          </div>
          {loading && <p className="muted">Loading...</p>}
          {applications.map(application => (
            <article className="application-card" key={application.id} onClick={() => editApplication(application)}>
              <div>
                <strong>{application.company}</strong>
                <span>{application.title}</span>
              </div>
              <div className="card-actions">
                <mark>{application.status}</mark>
                <button className="icon-button danger" onClick={event => { event.stopPropagation(); deleteApplication(application.id); }} title="Delete"><Trash2 size={17} /></button>
              </div>
            </article>
          ))}
        </section>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
