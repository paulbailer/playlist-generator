export default function PlaylistCard({ playlist, highlight }) {
  return (
    <div className={`playlist-card${highlight ? ' highlight' : ''}`}>
      <span className="playlist-name">{playlist.name || '—'}</span>
      <a
        href={playlist.link}
        target="_blank"
        rel="noopener noreferrer"
        className="btn-outline"
      >
        Open in Spotify
      </a>
    </div>
  );
}