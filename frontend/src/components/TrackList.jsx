export default function TrackList({ tracks, onConnect }) {
  return (
    <div>
      <div className="track-list">
        {tracks.map((t, i) => {
          return (
            <div key={i} className="track-row">
              <div className="track-info">
                <span className="track-name">{t.track}</span>
                <span className="track-artist">{t.artist}</span>
              </div>
              <a href={t.spotifyUrl} target="_blank" rel="noopener noreferrer" className="btn-outline">
                Open
              </a>
            </div>
          );
        })}
      </div>

      {onConnect && (
        <div className="connect-prompt">
          <span>Connect Spotify to save this as a playlist</span>
          <button className="btn-primary" onClick={onConnect}>Connect</button>
        </div>
      )}
    </div>
  );
}
