import { useState, useRef, useEffect } from 'react';
import { useAuth } from '../auth/context/AuthContext';
import { updateProfile, changePassword } from './settingsService';
import { getNotificationPreferences, updateNotificationPreferences } from '../notification/preferences/notificationPreferencesService';
import Skeleton from '../../shared/components/ui/Skeleton';
import './SettingsPage.css';

/* ── Tiny SVG helpers ─────────────────────────────────────────────────── */
const SvgUser    = () => <svg viewBox="0 0 24 24" strokeWidth="2"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg>;
const SvgLock    = () => <svg viewBox="0 0 24 24" strokeWidth="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>;
const SvgBell    = () => <svg viewBox="0 0 24 24" strokeWidth="2"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>;
const SvgRoles   = () => <svg viewBox="0 0 24 24" strokeWidth="2"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/></svg>;
const SvgImage   = () => <svg viewBox="0 0 24 24" strokeWidth="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>;
const SvgPerson  = () => <svg viewBox="0 0 24 24" strokeWidth="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>;
const SvgShield  = () => <svg viewBox="0 0 24 24" strokeWidth="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>;
const SvgCheck   = () => <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="9 11 12 14 22 4"/></svg>;
const SvgInfo    = () => <svg viewBox="0 0 24 24" strokeWidth="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>;
const SvgUpload  = () => <svg viewBox="0 0 24 24" strokeWidth="2"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>;
const SvgTrash   = () => <svg viewBox="0 0 24 24" strokeWidth="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/></svg>;
const SvgEdit    = () => <svg viewBox="0 0 24 24" strokeWidth="2.5"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>;
const SvgLogout  = () => <svg viewBox="0 0 24 24" strokeWidth="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>;

/* ── Password strength helper ─────────────────────────────────────────── */
function getStrength(pwd) {
  if (!pwd) return { level: 0, label: 'Enter a password to check strength' };
  let score = 0;
  if (pwd.length >= 8)  score++;
  if (/[A-Z]/.test(pwd)) score++;
  if (/[0-9]/.test(pwd)) score++;
  if (/[^A-Za-z0-9]/.test(pwd)) score++;
  if (score <= 1) return { level: 1, label: 'Weak — add uppercase, numbers, or symbols' };
  if (score === 2) return { level: 2, label: 'Fair — getting stronger' };
  return { level: 3, label: 'Strong password' };
}

/* ══════════════════════════════════════════════════════════════════════ */
export default function SettingsPage() {
  const { user, setUser, logout, refreshCurrentUser } = useAuth();
  const [activePanel, setActivePanel] = useState('profile');
  const [pageLoading, setPageLoading] = useState(!user);

  /* ── Profile state ── */
  const [firstName, setFirstName] = useState('');
  const [lastName,  setLastName]  = useState('');
  const [email,     setEmail]     = useState('');
  const [profileDirty,  setProfileDirty]  = useState(false);
  const [profileSaving, setProfileSaving] = useState(false);
  const [profileAlert,  setProfileAlert]  = useState(null); // { type, msg }

  /* ── Password state ── */
  const [currentPwd, setCurrentPwd] = useState('');
  const [newPwd,     setNewPwd]     = useState('');
  const [confirmPwd, setConfirmPwd] = useState('');
  const [pwdSaving,  setPwdSaving]  = useState(false);
  const [pwdAlert,   setPwdAlert]   = useState(null);
  const strength = getStrength(newPwd);

  /* ── Notification prefs — loaded from and saved to backend ── */
  const [notifPrefs, setNotifPrefs] = useState({
    evaluationAssigned: true,
    deadlineReminder: true,
    resultsPublished: true,
    formCreated: true,
    submissionReceived: true,
    systemAnnouncements: true,
  });
  const [notifLoading, setNotifLoading] = useState(false);
  const [notifSaving, setNotifSaving] = useState(false);
  const [notifAlert, setNotifAlert] = useState(null);

  // Load preferences when the notifications panel is opened
  useEffect(() => {
    if (activePanel !== 'notifications') return;
    let alive = true;
    (async () => {
      setNotifLoading(true);
      try {
        const data = await getNotificationPreferences();
        if (alive && data) setNotifPrefs(data);
      } catch {
        // silently ignore — defaults remain
      } finally {
        if (alive) setNotifLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [activePanel]);

  /* ── Sync user into form on mount / user change ── */
  useEffect(() => {
    if (user) {
      setFirstName(user.firstName ?? '');
      setLastName(user.lastName ?? '');
      setEmail(user.email ?? '');
      setProfileDirty(false);
      setPageLoading(false);
    }
  }, [user]);

  const markDirty = () => setProfileDirty(true);

  /* ── Initials for avatar ── */
  const initials = ((user?.firstName?.[0] ?? '') + (user?.lastName?.[0] ?? '')).toUpperCase() || '?';

  /* ── Profile save ── */
  const handleProfileSave = async () => {
    if (!firstName.trim() || !lastName.trim() || !email.trim()) {
      setProfileAlert({ type: 'error', msg: 'All fields are required.' });
      return;
    }
    setProfileSaving(true);
    setProfileAlert(null);
    try {
      const result = await updateProfile({ firstName: firstName.trim(), lastName: lastName.trim(), email: email.trim() });
      if (result?.user) {
        // Update context with new user data and fresh token
        await refreshCurrentUser();
      }
      setProfileDirty(false);
      setProfileAlert({ type: 'success', msg: 'Profile updated successfully.' });
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Failed to update profile.';
      setProfileAlert({ type: 'error', msg });
    } finally {
      setProfileSaving(false);
    }
  };

  const handleProfileDiscard = () => {
    setFirstName(user?.firstName ?? '');
    setLastName(user?.lastName ?? '');
    setEmail(user?.email ?? '');
    setProfileDirty(false);
    setProfileAlert(null);
  };

  /* ── Password save ── */
  const handlePasswordSave = async () => {
    if (!currentPwd || !newPwd || !confirmPwd) {
      setPwdAlert({ type: 'error', msg: 'All password fields are required.' });
      return;
    }
    if (newPwd !== confirmPwd) {
      setPwdAlert({ type: 'error', msg: 'New passwords do not match.' });
      return;
    }
    if (newPwd.length < 6) {
      setPwdAlert({ type: 'error', msg: 'New password must be at least 6 characters.' });
      return;
    }
    setPwdSaving(true);
    setPwdAlert(null);
    try {
      await changePassword({ currentPassword: currentPwd, newPassword: newPwd });
      setCurrentPwd('');
      setNewPwd('');
      setConfirmPwd('');
      setPwdAlert({ type: 'success', msg: 'Password updated successfully.' });
    } catch (err) {
      const msg = err.response?.data?.error?.message || 'Failed to update password.';
      setPwdAlert({ type: 'error', msg });
    } finally {
      setPwdSaving(false);
    }
  };

  const isFacilitator = user?.roles?.some(
    (r) => (typeof r === 'string' ? r : r?.name)?.toUpperCase() === 'FACILITATOR'
  );

  /* ── Render ── */
  return (
    <div className="settings-page animate-page">

      {/* ── Settings sidebar nav ── */}
      <nav className="settings-nav">
        <span className="sn-group-label">Account</span>
        <div className={`sn-item${activePanel === 'profile' ? ' active' : ''}`} onClick={() => setActivePanel('profile')}>
          <SvgUser /> Profile
        </div>
        <div className={`sn-item${activePanel === 'password' ? ' active' : ''}`} onClick={() => setActivePanel('password')}>
          <SvgLock /> Password &amp; Security
        </div>

        <span className="sn-group-label">Preferences</span>
        <div className={`sn-item${activePanel === 'notifications' ? ' active' : ''}`} onClick={() => setActivePanel('notifications')}>
          <SvgBell /> Notifications
        </div>

        <span className="sn-group-label">System</span>
        <div className={`sn-item${activePanel === 'roles' ? ' active' : ''}`} onClick={() => setActivePanel('roles')}>
          <SvgRoles /> Roles &amp; Access
        </div>
      </nav>

      {/* ── Settings content ── */}
      <div className="settings-content">

        {/* ── PAGE-LEVEL SKELETON (while user loads) ── */}
        {pageLoading && (
          <div className="settings-panel">
            <div className="panel-header">
              <Skeleton variant="title" width="140px" height="22px" />
              <Skeleton variant="text" width="260px" height="12px" style={{ marginTop: 8 }} />
            </div>
            {/* Skeleton card 1 — avatar row + 3 field rows */}
            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <Skeleton variant="circle" width="34px" height="34px" />
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
                    <Skeleton variant="text" width="120px" height="13px" />
                    <Skeleton variant="text" width="180px" height="10px" />
                  </div>
                </div>
              </div>
              <div className="field-row" style={{ alignItems: 'center', gap: 24, padding: '20px' }}>
                <Skeleton variant="rect" width="80px" height="80px" style={{ borderRadius: 16, flexShrink: 0 }} />
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 8 }}>
                  <Skeleton variant="text" width="120px" height="13px" />
                  <Skeleton variant="text" width="240px" height="10px" />
                  <Skeleton variant="text" width="200px" height="10px" />
                  <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
                    <Skeleton variant="rect" width="110px" height="32px" style={{ borderRadius: 8 }} />
                    <Skeleton variant="rect" width="90px" height="32px" style={{ borderRadius: 8 }} />
                  </div>
                </div>
              </div>
            </div>
            {/* Skeleton card 2 — 3 field rows */}
            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <Skeleton variant="circle" width="34px" height="34px" />
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 5 }}>
                    <Skeleton variant="text" width="140px" height="13px" />
                    <Skeleton variant="text" width="160px" height="10px" />
                  </div>
                </div>
              </div>
              {[1, 2, 3].map((i) => (
                <div className="field-row" key={i}>
                  <div className="field-info">
                    <Skeleton variant="text" width="90px" height="13px" />
                    <Skeleton variant="text" width="220px" height="10px" style={{ marginTop: 5 }} />
                  </div>
                  <Skeleton variant="rect" width="220px" height="36px" style={{ borderRadius: 8 }} />
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ══ PROFILE ══ */}
        {!pageLoading && activePanel === 'profile' && (
          <div className="settings-panel">
            <div className="panel-header">
              <div className="panel-title">Profile</div>
              <div className="panel-sub">Manage your personal information and identity within PeerTayo.</div>
            </div>

            {/* Profile Picture */}
            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <div className="card-head-icon chi-purple"><SvgImage /></div>
                  <div>
                    <div className="card-head-title">Profile Picture</div>
                    <div className="card-head-sub">Shown on your evaluations and results</div>
                  </div>
                </div>
              </div>
              <div className="field-row" style={{ alignItems: 'center', gap: 24, padding: '20px' }}>
                <div className="avatar-wrap">
                  <div className="avatar-box">{initials}</div>
                  <div className="avatar-edit-btn"><SvgEdit /></div>
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 13.5, fontWeight: 600, color: '#f1f5f9', marginBottom: 4 }}>Upload a photo</div>
                  <div style={{ fontSize: 11.5, color: '#94a3b8', lineHeight: 1.6, marginBottom: 12 }}>
                    JPG, PNG or GIF · Max 2 MB · Recommended 200×200px or larger
                  </div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button className="s-btn s-btn-ghost s-btn-sm" type="button">
                      <SvgUpload /> Upload Photo
                    </button>
                    <button className="s-btn s-btn-ghost s-btn-sm" type="button" style={{ color: '#ef4444', borderColor: 'rgba(239,68,68,0.2)' }}>
                      <SvgTrash /> Remove
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* Personal Info */}
            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <div className="card-head-icon chi-blue"><SvgPerson /></div>
                  <div>
                    <div className="card-head-title">Personal Information</div>
                    <div className="card-head-sub">Your name and contact details</div>
                  </div>
                </div>
              </div>
              <div className="field-row">
                <div className="field-info">
                  <div className="field-label">First Name</div>
                  <div className="field-desc">As it appears on evaluations and results</div>
                </div>
                <div className="field-control">
                  <input className="field-input" type="text" value={firstName}
                    onChange={(e) => { setFirstName(e.target.value); markDirty(); }} />
                </div>
              </div>
              <div className="field-row">
                <div className="field-info">
                  <div className="field-label">Last Name</div>
                  <div className="field-desc">As it appears on evaluations and results</div>
                </div>
                <div className="field-control">
                  <input className="field-input" type="text" value={lastName}
                    onChange={(e) => { setLastName(e.target.value); markDirty(); }} />
                </div>
              </div>
              <div className="field-row">
                <div className="field-info">
                  <div className="field-label">Email Address</div>
                  <div className="field-desc">Used for login and system notifications. Must be unique.</div>
                </div>
                <div className="field-control">
                  <input className="field-input" type="email" value={email}
                    onChange={(e) => { setEmail(e.target.value); markDirty(); }} />
                </div>
              </div>
            </div>

            {profileAlert && (
              <div className={`s-alert s-alert-${profileAlert.type}`}>
                {profileAlert.type === 'success' ? <SvgCheck /> : <SvgInfo />}
                {profileAlert.msg}
              </div>
            )}

            <div className="save-bar">
              <div className="save-bar-hint">
                <SvgInfo /> Changes are saved immediately after clicking Save.
              </div>
              <div className="save-bar-actions">
                <button className="s-btn s-btn-ghost" type="button" onClick={handleProfileDiscard} disabled={!profileDirty}>
                  Discard
                </button>
                <button className="s-btn s-btn-primary" type="button" onClick={handleProfileSave} disabled={!profileDirty || profileSaving}>
                  <SvgCheck /> {profileSaving ? 'Saving…' : 'Save Changes'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ══ PASSWORD & SECURITY ══ */}
        {!pageLoading && activePanel === 'password' && (
          <div className="settings-panel">
            <div className="panel-header">
              <div className="panel-title">Password &amp; Security</div>
              <div className="panel-sub">Update your login password. Must be at least 6 characters.</div>
            </div>

            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <div className="card-head-icon chi-orange"><SvgLock /></div>
                  <div>
                    <div className="card-head-title">Change Password</div>
                    <div className="card-head-sub">Use a strong, unique password of at least 6 characters</div>
                  </div>
                </div>
              </div>

              {user?.provider === 'GOOGLE' ? (
                <div className="field-row">
                  <div className="field-info">
                    <div className="field-label">Google Account</div>
                    <div className="field-desc">
                      Your account uses Google sign-in. Password management is handled by Google.
                    </div>
                  </div>
                </div>
              ) : (
                <>
                  <div className="field-row">
                    <div className="field-info"><div className="field-label">Current Password</div></div>
                    <div className="field-control">
                      <input className="field-input" type="password" placeholder="Enter current password"
                        value={currentPwd} onChange={(e) => setCurrentPwd(e.target.value)} />
                    </div>
                  </div>
                  <div className="field-row top">
                    <div className="field-info">
                      <div className="field-label">New Password</div>
                      <div className="field-desc">Must be at least 6 characters</div>
                    </div>
                    <div className="field-control">
                      <input className="field-input" type="password" placeholder="Enter new password"
                        value={newPwd} onChange={(e) => setNewPwd(e.target.value)} />
                      <div className="pwd-strength">
                        <div className={`pwd-bar${strength.level >= 1 ? ' s1' : ''}`} />
                        <div className={`pwd-bar${strength.level >= 2 ? ' s2' : ''}`} />
                        <div className={`pwd-bar${strength.level >= 3 ? ' s3' : ''}`} />
                      </div>
                      <div className="pwd-label">{strength.label}</div>
                    </div>
                  </div>
                  <div className="field-row">
                    <div className="field-info"><div className="field-label">Confirm New Password</div></div>
                    <div className="field-control">
                      <input className="field-input" type="password" placeholder="Repeat new password"
                        value={confirmPwd} onChange={(e) => setConfirmPwd(e.target.value)} />
                    </div>
                  </div>
                </>
              )}
            </div>

            {pwdAlert && (
              <div className={`s-alert s-alert-${pwdAlert.type}`}>
                {pwdAlert.type === 'success' ? <SvgCheck /> : <SvgInfo />}
                {pwdAlert.msg}
              </div>
            )}

            {user?.provider !== 'GOOGLE' && (
              <div className="save-bar">
                <div className="save-bar-hint">
                  <SvgShield /> Security changes take effect immediately on all devices.
                </div>
                <div className="save-bar-actions">
                  <button className="s-btn s-btn-ghost" type="button"
                    onClick={() => { setCurrentPwd(''); setNewPwd(''); setConfirmPwd(''); setPwdAlert(null); }}>
                    Cancel
                  </button>
                  <button className="s-btn s-btn-primary" type="button" onClick={handlePasswordSave} disabled={pwdSaving}>
                    <SvgCheck /> {pwdSaving ? 'Updating…' : 'Update Password'}
                  </button>
                </div>
              </div>
            )}
          </div>
        )}

        {/* ══ NOTIFICATIONS ══ */}
        {!pageLoading && activePanel === 'notifications' && (
          <div className="settings-panel">
            <div className="panel-header">
              <div className="panel-title">Notifications</div>
              <div className="panel-sub">Control which in-app alerts PeerTayo sends you.</div>
            </div>

            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <div className="card-head-icon chi-blue"><SvgBell /></div>
                  <div>
                    <div className="card-head-title">In-App Notifications</div>
                    <div className="card-head-sub">Choose which alerts appear inside PeerTayo</div>
                  </div>
                </div>
              </div>

              {notifLoading ? (
                /* ── Notification skeleton rows ── */
                [1, 2, 3, 4, 5, 6].map((i) => (
                  <div className="notif-row" key={i}>
                    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 6 }}>
                      <Skeleton variant="text" width="180px" height="13px" />
                      <Skeleton variant="text" width="280px" height="10px" />
                    </div>
                    <Skeleton variant="rect" width="42px" height="24px" style={{ borderRadius: 24, flexShrink: 0 }} />
                  </div>
                ))
              ) : (
                [
                  { key: 'evaluationAssigned', label: 'New Evaluation Assigned',   desc: 'When a facilitator assigns you to an evaluation form' },
                  { key: 'deadlineReminder',   label: 'Deadline Reminders',         desc: '48-hour and 24-hour reminders before an evaluation closes' },
                  { key: 'resultsPublished',   label: 'Results Published',          desc: 'When your evaluation results become available to view' },
                  { key: 'formCreated',        label: 'Form Created',               desc: 'Confirmation when you successfully create or update an evaluation form', facilitatorOnly: true },
                  { key: 'submissionReceived', label: 'Submission Received',        desc: 'When an evaluator submits a response for your form', facilitatorOnly: true },
                  { key: 'systemAnnouncements', label: 'System Announcements',     desc: 'Important updates about PeerTayo features and maintenance' },
                ].filter(n => !n.facilitatorOnly || isFacilitator).map((n) => (
                  <div className="notif-row" key={n.key}>
                    <div>
                      <div className="notif-label">
                        {n.label}
                        {n.facilitatorOnly && (
                          <span style={{ fontSize: 10, fontWeight: 600, color: '#fb923c', background: 'rgba(249,115,22,0.12)', padding: '1px 6px', borderRadius: 4, marginLeft: 6 }}>
                            Facilitator
                          </span>
                        )}
                      </div>
                      <div className="notif-desc">{n.desc}</div>
                    </div>
                    <div className="notif-toggles">
                      <label className="toggle">
                        <input
                          type="checkbox"
                          checked={notifPrefs[n.key] ?? true}
                          onChange={(e) => setNotifPrefs(p => ({ ...p, [n.key]: e.target.checked }))}
                        />
                        <span className="toggle-slider" />
                      </label>
                    </div>
                  </div>
                ))
              )}
            </div>

            {notifAlert && (
              <div className={`s-alert s-alert-${notifAlert.type}`}>
                {notifAlert.type === 'success' ? <SvgCheck /> : <SvgInfo />}
                {notifAlert.msg}
              </div>
            )}

            <div className="save-bar">
              <div className="save-bar-hint"><SvgInfo /> Preferences apply immediately to new notifications.</div>
              <div className="save-bar-actions">
                <button
                  className="s-btn s-btn-primary"
                  type="button"
                  disabled={notifSaving || notifLoading}
                  onClick={async () => {
                    setNotifSaving(true);
                    setNotifAlert(null);
                    try {
                      await updateNotificationPreferences(notifPrefs);
                      setNotifAlert({ type: 'success', msg: 'Notification preferences saved.' });
                    } catch {
                      setNotifAlert({ type: 'error', msg: 'Failed to save preferences.' });
                    } finally {
                      setNotifSaving(false);
                    }
                  }}
                >
                  <SvgCheck /> {notifSaving ? 'Saving…' : 'Save Preferences'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ══ ROLES & ACCESS ══ */}
        {!pageLoading && activePanel === 'roles' && (
          <div className="settings-panel">
            <div className="panel-header">
              <div className="panel-title">Roles &amp; Access</div>
              <div className="panel-sub">
                Your current system roles and what they allow you to do. Roles are assigned automatically based on your activity.
              </div>
            </div>

            {/* Active Roles */}
            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <div className="card-head-icon chi-green"><SvgRoles /></div>
                  <div>
                    <div className="card-head-title">Your Active Roles</div>
                    <div className="card-head-sub">Roles determine what you can create and access in PeerTayo</div>
                  </div>
                </div>
              </div>

              {isFacilitator && (
                <div className="field-row">
                  <div className="field-info">
                    <div className="field-label">Facilitator</div>
                    <div className="field-desc">Can create evaluation forms, assign evaluators and evaluatees, and view aggregated results.</div>
                  </div>
                  <div className="field-control">
                    <span className="role-badge rb-facilitator">● Active</span>
                  </div>
                </div>
              )}

              <div className="field-row">
                <div className="field-info">
                  <div className="field-label">Respondent</div>
                  <div className="field-desc">Can view and submit assigned evaluations, and view personal performance results.</div>
                </div>
                <div className="field-control">
                  <span className="role-badge rb-respondent">● Active</span>
                </div>
              </div>
            </div>

            {/* Permissions Summary */}
            <div className="settings-card">
              <div className="card-head">
                <div className="card-head-left">
                  <div className="card-head-icon chi-warn"><SvgShield /></div>
                  <div>
                    <div className="card-head-title">Permissions Summary</div>
                    <div className="card-head-sub">What you can do with your current roles</div>
                  </div>
                </div>
              </div>

              {[
                { label: 'Submit peer evaluations',          allowed: true },
                { label: 'View personal performance results', allowed: true },
                { label: 'Receive evaluation notifications',  allowed: true },
                { label: 'Create evaluation forms',           allowed: isFacilitator },
                { label: 'Assign evaluators and evaluatees',  allowed: isFacilitator },
                { label: 'View aggregated team results',      allowed: isFacilitator },
              ].map((p) => (
                <div className="field-row" key={p.label}>
                  <div className="field-info">
                    <div className="field-label" style={{ color: p.allowed ? '#f1f5f9' : '#4a5568' }}>{p.label}</div>
                  </div>
                  <div className="field-control">
                    {p.allowed
                      ? <span className="perm-check" style={{ fontSize: 13, fontWeight: 700 }}>✓ Allowed</span>
                      : <span className="perm-cross" style={{ fontSize: 13, fontWeight: 600 }}>— Not available</span>
                    }
                  </div>
                </div>
              ))}

              <div style={{ padding: '14px 20px', background: 'rgba(59,130,246,0.04)', borderTop: '1px solid rgba(255,255,255,0.07)' }}>
                <p style={{ fontSize: 12, color: '#60a5fa', lineHeight: 1.6 }}>
                  Roles are assigned automatically by the system.{' '}
                  <strong style={{ color: '#f1f5f9' }}>Respondent</strong> is granted on registration.{' '}
                  <strong style={{ color: '#f1f5f9' }}>Facilitator</strong> is granted when you create your first evaluation.
                  You cannot manually request or revoke roles.
                </p>
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
