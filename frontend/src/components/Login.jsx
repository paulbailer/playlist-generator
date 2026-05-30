import { redirectToSpotifyLogin } from '../auth/spotify';

export default function Login() {
  return (
    <div className="login">
      <div className="login-card">
        <h1>Playlist Generator</h1>
        <p>Describe a vibe, get a playlist. Powered by AI.</p>
        <button className="btn-primary" onClick={redirectToSpotifyLogin}>
          Connect with Spotify
        </button>
      </div>
    </div>
  );
}