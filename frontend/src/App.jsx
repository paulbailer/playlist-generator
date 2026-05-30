import { useState, useEffect } from 'react';
import Login from './components/Login';
import Generator from './components/Generator';
import Privacy from './components/Privacy';
import { exchangeCodeForToken, getAccessToken, clearTokens } from './auth/spotify';

export default function App() {
  const [accessToken, setAccessToken] = useState(getAccessToken());
  const [loading, setLoading] = useState(false);
  const [showPrivacy, setShowPrivacy] = useState(() => window.location.hash === '#privacy');

  useEffect(() => {
    const onHash = () => setShowPrivacy(window.location.hash === '#privacy');
    window.addEventListener('hashchange', onHash);
    return () => window.removeEventListener('hashchange', onHash);
  }, []);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const code = params.get('code');
    if (!code) return;

    setLoading(true);
    exchangeCodeForToken(code)
      .then(token => {
        setAccessToken(token);
        window.history.replaceState({}, '', import.meta.env.BASE_URL);
      })
      .catch(() => setLoading(false))
      .finally(() => setLoading(false));
  }, []);

  function logout() {
    clearTokens();
    setAccessToken(null);
  }

  if (showPrivacy) return <Privacy />;
  if (loading) return <div className="center">Connecting to Spotify…</div>;
  if (!accessToken) return <Login />;
  return <Generator accessToken={accessToken} onLogout={logout} />;
}
