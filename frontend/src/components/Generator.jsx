import { useState, useEffect } from 'react';
import { generatePlaylist, getSavedPlaylists } from '../services/api';
import PlaylistCard from './PlaylistCard';

export default function Generator({ accessToken, onLogout }) {
  const [prompt, setPrompt] = useState('');
  const [size, setSize] = useState(20);
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    getSavedPlaylists().then(setHistory).catch(() => {});
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setGenerating(true);
    setError(null);
    setResult(null);
    try {
      const playlist = await generatePlaylist(prompt, size, accessToken);
      setResult(playlist);
      setHistory(prev => [playlist, ...prev.filter(p => p.id !== playlist.id)]);
    } catch {
      setError('Something went wrong. Make sure the backend is running and try again.');
    } finally {
      setGenerating(false);
    }
  }

  return (
    <div className="app">
      <header>
        <h1>Playlist Generator</h1>
        <button className="btn-ghost" onClick={onLogout}>Log out</button>
      </header>

      <main>
        <form onSubmit={handleSubmit} className="generator-form">
          <textarea
            value={prompt}
            onChange={e => setPrompt(e.target.value)}
            placeholder="Describe your playlist… e.g. 'upbeat 90s rock for a road trip'"
            rows={3}
            required
          />
          <div className="form-row">
            <label>
              Tracks
              <input
                type="number"
                min={1}
                max={50}
                value={size}
                onChange={e => setSize(Number(e.target.value))}
              />
            </label>
            <button type="submit" className="btn-primary" disabled={generating}>
              {generating ? 'Generating…' : 'Generate'}
            </button>
          </div>
        </form>

        {error && <p className="error">{error}</p>}

        {result && (
          <div className="result">
            <p className="result-label">Playlist created</p>
            <PlaylistCard playlist={result} highlight />
          </div>
        )}

        {history.length > 0 && (
          <section className="history">
            <p className="section-label">Previously generated</p>
            {history.map(p => (
              <PlaylistCard key={p.id} playlist={p} />
            ))}
          </section>
        )}
      </main>
    </div>
  );
}
