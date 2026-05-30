import { useState, useEffect } from 'react';
import { generatePlaylist, getSavedPlaylists, deleteUserData } from '../services/api';
import PlaylistCard from './PlaylistCard';
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

export default function Generator({ accessToken, onLogout }) {
  const [prompt, setPrompt] = useState('');
  const [size, setSize] = useState(20);
  const [popularity, setPopularity] = useState('deep_cuts');
  const [energy, setEnergy] = useState('medium');
  const [era, setEra] = useState([]);
  const [mood, setMood] = useState([]);
  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [history, setHistory] = useState([]);

  useEffect(() => {
    getSavedPlaylists(accessToken).then(setHistory).catch(() => {});
  }, []);

  async function handleLogout() {
    try {
      await deleteUserData(accessToken);
    } catch {
      // proceed with logout even if the server call fails
    }
    onLogout();
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setGenerating(true);
    setError(null);
    setResult(null);
    try {
      const playlist = await generatePlaylist(prompt, size, accessToken, {
        popularity,
        energy,
        era: era.length > 0 ? era : null,
        mood: mood.length > 0 ? mood : null,
      });
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
        <button className="btn-ghost" onClick={handleLogout}>Log out</button>
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
      <footer>
        <a href="#privacy" className="privacy-link">Privacy Policy</a>
      </footer>
    </div>
  );
}
