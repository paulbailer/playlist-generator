const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function generatePlaylist(prompt, size, accessToken) {
  const response = await fetch(`${BASE_URL}/generate-playlist`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ prompt, size }),
  });
  if (!response.ok) throw new Error('Generation failed');
  return response.json();
}

export async function getSavedPlaylists() {
  const response = await fetch(`${BASE_URL}/playlists`);
  if (!response.ok) throw new Error('Failed to load playlists');
  return response.json();
}