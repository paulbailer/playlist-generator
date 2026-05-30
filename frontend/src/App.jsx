import { useState, useEffect } from 'react';
import Generator from './components/Generator';
import Privacy from './components/Privacy';
import { exchangeCodeForToken, getAccessToken, clearTokens, redirectToSpotifyLogin } from './auth/spotify';

export default function App() {
  const [accessToken, setAccessToken] = useState(getAccessToken());
  const [loading, setLoading] = useState(false);
  const [authError, setAuthError] = useState(false);
  const [showPrivacy, setShowPrivacy] = useState(() => window.location.hash === '#privacy');

  useEffect(() => {
    const onHash = () => setShowPrivacy(window.location.hash === '#privacy');
    window.addEventListener('hashchange', onHash);
    return () => window.removeEventListener('hashchange', onHash);
  }, []);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    const code = params.get('code');

    if (error) {
      setAuthError(true);
      window.history.replaceState({}, '', import.meta.env.BASE_URL);
      return;
    }
    if (!code) return;

    setLoading(true);
    exchangeCodeForToken(code)
      .then(token => {
        setAccessToken(token);
        window.history.replaceState({}, '', import.meta.env.BASE_URL);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function logout() {
    clearTokens();
    setAccessToken(null);
  }

  if (showPrivacy) return <Privacy />;
  if (loading) return <div className="center">Connecting to Spotify…</div>;

  return (
    <Generator
      accessToken={accessToken}
      onLogout={logout}
      onConnect={redirectToSpotifyLogin}
      authError={authError}
      onDismissAuthError={() => setAuthError(false)}
    />
  );
}
