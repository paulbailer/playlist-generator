import { useState, useEffect } from 'react';
import { generatePlaylist, getSavedPlaylists, deleteUserData, suggestTracks } from '../services/api';
import PlaylistCard from './PlaylistCard';
import TrackList from './TrackList';
import SegmentedSelector from './SegmentedSelector';
import ChipSelector from './ChipSelector';

const POPULARITY_OPTIONS = [
  { value: 'underground', label: 'Underground' },
  { value: 'deep_cuts',   label: 'Deep cuts' },
  { value: 'mixed',       label: 'Mixed' },
  { value: 'popular',     label: 'Popular' },
  { value: 'mainstream',  label: 'Mainstream' },
];

const ENERGY_OPTIONS = [
  { value: 'very_chill',     label: 'Very chill' },
  { value: 'chill',          label: 'Chill' },
  { value: 'medium',         label: 'Medium' },
  { value: 'energetic',      label: 'Energetic' },
  { value: 'very_energetic', label: 'Intense' },
];

const ERA_OPTIONS = [
  { value: '60s',   label: '60s' },
  { value: '70s',   label: '70s' },
  { value: '80s',   label: '80s' },
  { value: '90s',   label: '90s' },
  { value: '2000s', label: '2000s' },
  { value: '2010s', label: '2010s' },
  { value: '2020s', label: '2020s' },
];

const MOOD_OPTIONS = [
  { value: 'Happy',       label: 'Happy' },
  { value: 'Melancholic', label: 'Melancholic' },
  { value: 'Angry',       label: 'Angry' },
  { value: 'Romantic',    label: 'Romantic' },
  { value: 'Nostalgic',   label: 'Nostalgic' },
  { value: 'Focused',     label: 'Focused' },
];

export default function Generator({ accessToken, onLogout, onConnect }) {
  const [prompt, setPrompt] = useState('');
  const [size, setSize] = useState(20);
  const [popularity, setPopularity] = useState('deep_cuts');
  const [energy, setEnergy] = useState('medium');
  const [era, setEra] = useState([]);
  const [mood, setMood] = useState([]);
  const [generating, setGenerating] = useState(false);
  const [playlist, setPlaylist] = useState(null);
  const [suggestion, setSuggestion] = useState(null);
  const [error, setError] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    if (accessToken) {
      getSavedPlaylists(accessToken).then(setHistory).catch(() => {});
    }
  }, [accessToken]);

  const criteria = {
    popularity,
    energy,
    era: era.length > 0 ? era : null,
    mood: mood.length > 0 ? mood : null,
  };

  async function handleSubmit(e) {
    e.preventDefault();
    setGenerating(true);
    setError(null);
    setPlaylist(null);
    setSuggestion(null);

    try {
      if (accessToken) {
        const result = await generatePlaylist(prompt, size, accessToken, criteria);
        setPlaylist(result);
        setHistory(prev => [result, ...prev.filter(p => p.id !== result.id)]);
      } else {
        const result = await suggestTracks(prompt, size, criteria);
        setSuggestion(result);
      }
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setGenerating(false);
    }
  }

  async function handleLogout() {
    try {
      await deleteUserData(accessToken);
    } catch {
      // proceed with logout even if deletion fails
    }
    onLogout();
  }

  return (
    <div className="app">
      <header>
        <h1>Playlist Generator</h1>
        {accessToken
          ? <button className="btn-ghost" onClick={handleLogout}>Log out</button>
          : <button className="btn-ghost" onClick={onConnect}>Connect Spotify</button>
        }
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

          <div className="criteria">
            <div className="criteria-row">
              <span className="criteria-label">Popularity</span>
              <SegmentedSelector value={popularity} onChange={setPopularity} options={POPULARITY_OPTIONS} />
            </div>
            <div className="criteria-row">
              <span className="criteria-label">Energy</span>
              <SegmentedSelector value={energy} onChange={setEnergy} options={ENERGY_OPTIONS} />
            </div>
            <div className="criteria-row">
              <span className="criteria-label">Era <span className="criteria-hint">(any if none selected)</span></span>
              <ChipSelector value={era} onChange={setEra} options={ERA_OPTIONS} />
            </div>
            <div className="criteria-row">
              <span className="criteria-label">Mood <span className="criteria-hint">(any if none selected)</span></span>
              <ChipSelector value={mood} onChange={setMood} options={MOOD_OPTIONS} />
            </div>
          </div>

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
              {generating ? 'Generating…' : accessToken ? 'Create on Spotify' : 'Generate'}
            </button>
          </div>
        </form>

        {error && <p className="error">{error}</p>}

        {playlist && (
          <div className="result">
            <p className="result-label">Playlist created</p>
            <PlaylistCard playlist={playlist} highlight />
          </div>
        )}

        {suggestion && (
          <div className="result">
            <p className="result-label">{suggestion.title}</p>
            <TrackList
              tracks={suggestion.tracks}
              onConnect={!accessToken ? onConnect : null}
            />
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

      <footer>
        <a href="#privacy" className="privacy-link">Privacy Policy</a>
      </footer>
    </div>
  );
}
