import { useState, useEffect } from 'react';
import Login from './components/Login';
import Generator from './components/Generator';
import { exchangeCodeForToken, getAccessToken, clearTokens } from './auth/spotify';

export default function App() {
  const [accessToken, setAccessToken] = useState(getAccessToken());
  const [loading, setLoading] = useState(false);

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

  if (loading) return <div className="center">Connecting to Spotify…</div>;
  if (!accessToken) return <Login />;
  return <Generator accessToken={accessToken} onLogout={logout} />;
}