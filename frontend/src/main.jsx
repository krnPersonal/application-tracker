import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { BookOpen, Briefcase, Download, LogOut, Plus, Save, Trash2, Upload, UserRound } from 'lucide-react';
import { API_BASE_URL, api } from './api/client';
import './styles.css';

const emptyForm = {
  company: '',
  title: '',
  status: 'SAVED',
  appliedDate: '',
  jobUrl: '',
  notes: ''
};

const emptyCourseForm = {
  name: '',
  description: ''
};

const emptyTopicForm = {
  courseId: '',
  title: '',
  status: 'NOT_STARTED',
  notes: ''
};

function App() {
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [user, setUser] = useState(null);
  const [applications, setApplications] = useState([]);
  const [courses, setCourses] = useState([]);
  const [topics, setTopics] = useState([]);
  const [dashboard, setDashboard] = useState(null);
  const [dashboardFilters, setDashboardFilters] = useState({ from: '', to: '' });
  const [applicationFilters, setApplicationFilters] = useState({ q: '', status: '', sort: 'updatedAt', direction: 'desc' });
  const [authMode, setAuthMode] = useState('login');
  const [authForm, setAuthForm] = useState({ firstName: '', lastName: '', email: '', password: '' });
  const [form, setForm] = useState(emptyForm);
  const [courseForm, setCourseForm] = useState(emptyCourseForm);
  const [topicForm, setTopicForm] = useState(emptyTopicForm);
  const [profileForm, setProfileForm] = useState({ firstName: '', lastName: '', email: '' });
  const [passwordForm, setPasswordForm] = useState({ currentPassword: '', newPassword: '' });
  const [editingId, setEditingId] = useState(null);
  const [editingCourseId, setEditingCourseId] = useState(null);
  const [editingTopicId, setEditingTopicId] = useState(null);
  const [message, setMessage] = useState('');
  const [courseMessage, setCourseMessage] = useState('');
  const [topicMessage, setTopicMessage] = useState('');
  const [profileMessage, setProfileMessage] = useState('');
  const [passwordMessage, setPasswordMessage] = useState('');
  const [resumeMessage, setResumeMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const statusOptions = ['SAVED', 'APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED', 'WITHDRAWN'];
  const topicStatusOptions = ['NOT_STARTED', 'IN_PROGRESS', 'DONE'];

  const statusCounts = useMemo(() => dashboard?.statusCounts || {}, [dashboard]);

  useEffect(() => {
    if (!token) return;
    loadData();
  }, [token]);

  async function loadData(filters = dashboardFilters, appFilters = applicationFilters) {
    try {
      setLoading(true);
      const dashboardQuery = new URLSearchParams();
      if (filters.from) dashboardQuery.set('from', filters.from);
      if (filters.to) dashboardQuery.set('to', filters.to);
      const dashboardPath = `/api/dashboard${dashboardQuery.toString() ? `?${dashboardQuery}` : ''}`;
      const applicationsQuery = new URLSearchParams();
      if (appFilters.q) applicationsQuery.set('q', appFilters.q);
      if (appFilters.status) applicationsQuery.set('status', appFilters.status);
      if (appFilters.sort) applicationsQuery.set('sort', appFilters.sort);
      if (appFilters.direction) applicationsQuery.set('direction', appFilters.direction);
      const applicationsPath = `/api/applications${applicationsQuery.toString() ? `?${applicationsQuery}` : ''}`;
      const [me, dash, apps, studyCourses, studyTopics] = await Promise.all([
        api('/api/me', { token }),
        api(dashboardPath, { token }),
        api(applicationsPath, { token }),
        api('/api/study/courses', { token }),
        api('/api/study/topics', { token })
      ]);
      setUser(me);
      setProfileForm({ firstName: me.firstName, lastName: me.lastName, email: me.email });
      setDashboard(dash);
      setApplications(apps);
      setCourses(studyCourses);
      setTopics(studyTopics);
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

  async function submitCourse(event) {
    event.preventDefault();
    setCourseMessage('');
    const path = editingCourseId ? `/api/study/courses/${editingCourseId}` : '/api/study/courses';
    const method = editingCourseId ? 'PUT' : 'POST';
    try {
      await api(path, { method, token, body: courseForm });
      setCourseForm(emptyCourseForm);
      setEditingCourseId(null);
      await loadData();
      setCourseMessage(editingCourseId ? 'Course updated' : 'Course added');
    } catch (error) {
      setCourseMessage(error.message);
    }
  }

  async function deleteCourse(id) {
    setCourseMessage('');
    try {
      await api(`/api/study/courses/${id}`, { method: 'DELETE', token });
      await loadData();
      setCourseMessage('Course deleted');
    } catch (error) {
      setCourseMessage(error.message);
    }
  }

  async function submitTopic(event) {
    event.preventDefault();
    setTopicMessage('');
    const path = editingTopicId ? `/api/study/topics/${editingTopicId}` : '/api/study/topics';
    const method = editingTopicId ? 'PUT' : 'POST';
    try {
      await api(path, {
        method,
        token,
        body: { ...topicForm, courseId: Number(topicForm.courseId) }
      });
      setTopicForm(emptyTopicForm);
      setEditingTopicId(null);
      await loadData();
      setTopicMessage(editingTopicId ? 'Topic updated' : 'Topic added');
    } catch (error) {
      setTopicMessage(error.message);
    }
  }

  async function deleteTopic(id) {
    setTopicMessage('');
    try {
      await api(`/api/study/topics/${id}`, { method: 'DELETE', token });
      await loadData();
      setTopicMessage('Topic deleted');
    } catch (error) {
      setTopicMessage(error.message);
    }
  }

  async function submitProfile(event) {
    event.preventDefault();
    setProfileMessage('');
    try {
      const updated = await api('/api/profile', { method: 'PUT', token, body: profileForm });
      setUser(updated);
      setProfileForm({ firstName: updated.firstName, lastName: updated.lastName, email: updated.email });
      setProfileMessage('Profile updated');
    } catch (error) {
      setProfileMessage(error.message);
    }
  }

  async function submitPassword(event) {
    event.preventDefault();
    setPasswordMessage('');
    try {
      await api('/api/profile/password', { method: 'PUT', token, body: passwordForm });
      setPasswordForm({ currentPassword: '', newPassword: '' });
      setPasswordMessage('Password updated');
    } catch (error) {
      setPasswordMessage(error.message);
    }
  }

  async function submitDashboardFilters(event) {
    event.preventDefault();
    await loadData(dashboardFilters, applicationFilters);
  }

  function clearDashboardFilters() {
    const emptyFilters = { from: '', to: '' };
    setDashboardFilters(emptyFilters);
    loadData(emptyFilters, applicationFilters);
  }

  async function submitApplicationFilters(event) {
    event.preventDefault();
    await loadData(dashboardFilters, applicationFilters);
  }

  function clearApplicationFilters() {
    const emptyFilters = { q: '', status: '', sort: 'updatedAt', direction: 'desc' };
    setApplicationFilters(emptyFilters);
    loadData(dashboardFilters, emptyFilters);
  }

  async function submitResume(event) {
    event.preventDefault();
    setResumeMessage('');
    const file = event.currentTarget.elements.resume.files[0];
    if (!file) {
      setResumeMessage('Resume file is required');
      return;
    }
    const data = new FormData();
    data.append('file', file);
    try {
      const response = await fetch(`${API_BASE_URL}/api/profile/resume`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}` },
        body: data
      });
      const updated = await response.json();
      if (!response.ok) {
        throw new Error(updated?.message || 'Resume upload failed');
      }
      setUser(updated);
      setProfileForm({ firstName: updated.firstName, lastName: updated.lastName, email: updated.email });
      setResumeMessage('Resume uploaded');
      event.currentTarget.reset();
    } catch (error) {
      setResumeMessage(error.message);
    }
  }

  async function downloadResume() {
    setResumeMessage('');
    try {
      const response = await fetch(`${API_BASE_URL}/api/profile/resume`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!response.ok) {
        const text = await response.text();
        const error = text ? JSON.parse(text) : null;
        throw new Error(error?.message || 'Resume download failed');
      }
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = user?.resumeFileName || 'resume';
      link.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      setResumeMessage(error.message);
    }
  }

  async function downloadApplicationsPdf() {
    setMessage('');
    try {
      const response = await fetch(`${API_BASE_URL}/api/applications/report.pdf`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!response.ok) {
        const text = await response.text();
        const error = text ? JSON.parse(text) : null;
        throw new Error(error?.message || 'PDF download failed');
      }
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'job-applications.pdf';
      link.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      setMessage(error.message);
    }
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

  function editCourse(course) {
    setEditingCourseId(course.id);
    setCourseForm({
      name: course.name,
      description: course.description || ''
    });
  }

  function editTopic(topic) {
    setEditingTopicId(topic.id);
    setTopicForm({
      courseId: String(topic.courseId),
      title: topic.title,
      status: topic.status,
      notes: topic.notes || ''
    });
  }

  function logout() {
    localStorage.removeItem('token');
    setToken('');
    setUser(null);
    setApplications([]);
    setCourses([]);
    setTopics([]);
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

      <form className="filter-bar" onSubmit={submitDashboardFilters}>
        <label>From<input type="date" value={dashboardFilters.from} onChange={event => setDashboardFilters({ ...dashboardFilters, from: event.target.value })} /></label>
        <label>To<input type="date" value={dashboardFilters.to} onChange={event => setDashboardFilters({ ...dashboardFilters, to: event.target.value })} /></label>
        <button className="primary fit" type="submit">Apply</button>
        <button className="secondary fit" type="button" onClick={clearDashboardFilters}>Clear</button>
      </form>

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
            <div className="title-actions">
              <button className="secondary" onClick={downloadApplicationsPdf}><Download size={16} />PDF</button>
              <button className="secondary" onClick={() => { setForm(emptyForm); setEditingId(null); }}><Plus size={16} />New</button>
            </div>
          </div>
          <form className="list-filters" onSubmit={submitApplicationFilters}>
            <label>Search<input value={applicationFilters.q} onChange={event => setApplicationFilters({ ...applicationFilters, q: event.target.value })} /></label>
            <label>Status<select value={applicationFilters.status} onChange={event => setApplicationFilters({ ...applicationFilters, status: event.target.value })}><option value="">All</option>{statusOptions.map(status => <option key={status}>{status}</option>)}</select></label>
            <label>Sort<select value={applicationFilters.sort} onChange={event => setApplicationFilters({ ...applicationFilters, sort: event.target.value })}><option value="updatedAt">Updated</option><option value="appliedDate">Applied date</option><option value="company">Company</option><option value="title">Title</option><option value="status">Status</option></select></label>
            <label>Direction<select value={applicationFilters.direction} onChange={event => setApplicationFilters({ ...applicationFilters, direction: event.target.value })}><option value="desc">Desc</option><option value="asc">Asc</option></select></label>
            <button className="primary fit" type="submit">Apply</button>
            <button className="secondary fit" type="button" onClick={clearApplicationFilters}>Clear</button>
          </form>
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

      <section className="study-band">
        <form className="study-editor" onSubmit={submitCourse}>
          <div className="section-heading">
            <BookOpen size={22} />
            <h2>{editingCourseId ? 'Edit Course' : 'New Course'}</h2>
          </div>
          <label>Course name<input value={courseForm.name} onChange={event => setCourseForm({ ...courseForm, name: event.target.value })} required /></label>
          <label>Description<textarea rows="4" value={courseForm.description} onChange={event => setCourseForm({ ...courseForm, description: event.target.value })} /></label>
          {courseMessage && <p className={['Course added', 'Course updated', 'Course deleted'].includes(courseMessage) ? 'success' : 'error'}>{courseMessage}</p>}
          <div className="title-actions">
            <button className="primary" type="submit"><Save size={17} />{editingCourseId ? 'Save Course' : 'Add Course'}</button>
            {editingCourseId && <button className="secondary" type="button" onClick={() => { setCourseForm(emptyCourseForm); setEditingCourseId(null); }}>Cancel</button>}
          </div>
        </form>
        <section className="study-list">
          <div className="list-title">
            <div className="section-heading">
              <BookOpen size={22} />
              <h2>Study Courses</h2>
            </div>
            <button className="secondary" onClick={() => { setCourseForm(emptyCourseForm); setEditingCourseId(null); }}><Plus size={16} />New</button>
          </div>
          {courses.length === 0 && <p className="muted">No courses yet.</p>}
          {courses.map(course => (
            <article className="application-card" key={course.id} onClick={() => editCourse(course)}>
              <div>
                <strong>{course.name}</strong>
                <span>{course.description || 'No description'}</span>
              </div>
              <div className="card-actions">
                <button className="icon-button danger" onClick={event => { event.stopPropagation(); deleteCourse(course.id); }} title="Delete course"><Trash2 size={17} /></button>
              </div>
            </article>
          ))}
        </section>
      </section>

      <section className="topic-band">
        <form className="study-editor" onSubmit={submitTopic}>
          <div className="section-heading">
            <BookOpen size={22} />
            <h2>{editingTopicId ? 'Edit Topic' : 'New Topic'}</h2>
          </div>
          <label>Course<select value={topicForm.courseId} onChange={event => setTopicForm({ ...topicForm, courseId: event.target.value })} required><option value="">Select course</option>{courses.map(course => <option key={course.id} value={course.id}>{course.name}</option>)}</select></label>
          <label>Topic title<input value={topicForm.title} onChange={event => setTopicForm({ ...topicForm, title: event.target.value })} required /></label>
          <label>Status<select value={topicForm.status} onChange={event => setTopicForm({ ...topicForm, status: event.target.value })}>{topicStatusOptions.map(status => <option key={status}>{status}</option>)}</select></label>
          <label>Notes<textarea rows="4" value={topicForm.notes} onChange={event => setTopicForm({ ...topicForm, notes: event.target.value })} /></label>
          {topicMessage && <p className={['Topic added', 'Topic updated', 'Topic deleted'].includes(topicMessage) ? 'success' : 'error'}>{topicMessage}</p>}
          <div className="title-actions">
            <button className="primary" type="submit" disabled={courses.length === 0}><Save size={17} />{editingTopicId ? 'Save Topic' : 'Add Topic'}</button>
            {editingTopicId && <button className="secondary" type="button" onClick={() => { setTopicForm(emptyTopicForm); setEditingTopicId(null); }}>Cancel</button>}
          </div>
        </form>
        <section className="study-list">
          <div className="list-title">
            <div className="section-heading">
              <BookOpen size={22} />
              <h2>Study Topics</h2>
            </div>
            <button className="secondary" onClick={() => { setTopicForm(emptyTopicForm); setEditingTopicId(null); }} disabled={courses.length === 0}><Plus size={16} />New</button>
          </div>
          {courses.length === 0 && <p className="muted">Add a course before creating topics.</p>}
          {courses.length > 0 && topics.length === 0 && <p className="muted">No topics yet.</p>}
          {topics.map(topic => (
            <article className="application-card" key={topic.id} onClick={() => editTopic(topic)}>
              <div>
                <strong>{topic.title}</strong>
                <span>{topic.courseName} - {topic.notes || 'No notes'}</span>
              </div>
              <div className="card-actions">
                <mark>{topic.status}</mark>
                <button className="icon-button danger" onClick={event => { event.stopPropagation(); deleteTopic(topic.id); }} title="Delete topic"><Trash2 size={17} /></button>
              </div>
            </article>
          ))}
        </section>
      </section>

      <section className="profile-band">
        <form className="profile-panel" onSubmit={submitProfile}>
          <div className="section-heading">
            <UserRound size={22} />
            <h2>Profile</h2>
          </div>
          <div className="grid three">
            <label>First name<input value={profileForm.firstName} onChange={event => setProfileForm({ ...profileForm, firstName: event.target.value })} required /></label>
            <label>Last name<input value={profileForm.lastName} onChange={event => setProfileForm({ ...profileForm, lastName: event.target.value })} required /></label>
            <label>Email<input type="email" value={profileForm.email} onChange={event => setProfileForm({ ...profileForm, email: event.target.value })} required /></label>
          </div>
          {profileMessage && <p className={profileMessage === 'Profile updated' ? 'success' : 'error'}>{profileMessage}</p>}
          <button className="primary fit" type="submit"><Save size={17} />Save Profile</button>
        </form>
        <form className="profile-panel" onSubmit={submitPassword}>
          <div className="section-heading">
            <UserRound size={22} />
            <h2>Password</h2>
          </div>
          <div className="grid two">
            <label>Current password<input type="password" value={passwordForm.currentPassword} onChange={event => setPasswordForm({ ...passwordForm, currentPassword: event.target.value })} required /></label>
            <label>New password<input type="password" minLength="8" value={passwordForm.newPassword} onChange={event => setPasswordForm({ ...passwordForm, newPassword: event.target.value })} required /></label>
          </div>
          {passwordMessage && <p className={passwordMessage === 'Password updated' ? 'success' : 'error'}>{passwordMessage}</p>}
          <button className="primary fit" type="submit"><Save size={17} />Change Password</button>
        </form>
        <form className="profile-panel" onSubmit={submitResume}>
          <div className="section-heading">
            <Upload size={22} />
            <h2>Resume</h2>
          </div>
          <div className="resume-row">
            <label>Resume file<input name="resume" type="file" accept=".pdf,.doc,.docx" required /></label>
            <div className="resume-actions">
              <span className="muted">{user?.resumeFileName || 'No resume uploaded'}</span>
              <button className="secondary" type="button" onClick={downloadResume} disabled={!user?.resumeFileName}><Download size={17} />Download</button>
            </div>
          </div>
          {resumeMessage && <p className={resumeMessage === 'Resume uploaded' ? 'success' : 'error'}>{resumeMessage}</p>}
          <button className="primary fit" type="submit"><Upload size={17} />Upload Resume</button>
        </form>
      </section>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
