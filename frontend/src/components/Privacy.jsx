export default function Privacy() {
  return (
    <div className="app">
      <header>
        <h1>Playlist Generator</h1>
        <a href="#" className="btn-ghost">← Back</a>
      </header>

      <main className="privacy">
        <h2>Privacy Policy</h2>
        <p className="privacy-date">Last updated: May 2026</p>

        <section>
          <h3>What this app does</h3>
          <p>
            Playlist Generator lets you describe a playlist in plain language and generates it
            directly on your Spotify account using the Spotify Web API and the Claude AI model
            by Anthropic.
          </p>
        </section>

        <section>
          <h3>Data we collect</h3>
          <ul>
            <li>
              <strong>Spotify access token</strong> — obtained via Spotify's OAuth 2.0 PKCE flow.
              Stored only in your browser's session storage and cleared when you log out or close
              the tab. Never sent to or stored on our servers.
            </li>
            <li>
              <strong>Spotify user ID</strong> — used to scope your playlist history so you only
              see your own playlists. Stored in our database alongside each playlist you generate.
            </li>
            <li>
              <strong>Generated playlists</strong> — the title and Spotify link of each playlist
              you generate are stored in our database so you can access your history.
            </li>
          </ul>
        </section>

        <section>
          <h3>What we don't collect</h3>
          <ul>
            <li>Your name or email address</li>
            <li>Your Spotify listening history or library</li>
            <li>Any payment or financial information</li>
            <li>Any data beyond what is listed above</li>
          </ul>
        </section>

        <section>
          <h3>Third-party services</h3>
          <ul>
            <li>
              <strong>Spotify</strong> — authentication and playlist creation.
              See <a href="https://www.spotify.com/legal/privacy-policy/" target="_blank" rel="noopener noreferrer">Spotify's Privacy Policy</a>.
            </li>
            <li>
              <strong>Anthropic</strong> — your playlist prompt is sent to the Claude API to
              generate track suggestions. No personal data is included in this request.
              See <a href="https://www.anthropic.com/legal/privacy" target="_blank" rel="noopener noreferrer">Anthropic's Privacy Policy</a>.
            </li>
          </ul>
        </section>

        <section>
          <h3>Data retention and deletion</h3>
          <p>
            Generated playlist records (Spotify user ID, playlist title, Spotify link) are stored
            for the duration of your session. <strong>All data is automatically and permanently
            deleted from our servers when you log out</strong> using the logout button in the app.
            No manual deletion request is needed.
          </p>
        </section>

        <section>
          <h3>Contact</h3>
          <p>
            Questions or deletion requests: <a href="mailto:page.bailer@gmx.de">page.bailer@gmx.de</a>
          </p>
        </section>
      </main>
    </div>
  );
}
