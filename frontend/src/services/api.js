const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function generatePlaylist(prompt, size, accessToken, criteria = {}) {
  const response = await fetch(`${BASE_URL}/generate-playlist`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${accessToken}`,
    },
    body: JSON.stringify({ prompt, size, ...criteria }),
  });
  if (!response.ok) throw new Error('Generation failed');
  return response.json();
}

export async function suggestTracks(prompt, size, criteria = {}) {
  const response = await fetch(`${BASE_URL}/suggest`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ prompt, size, ...criteria }),
  });
  if (!response.ok) throw new Error('Suggestion failed');
  return response.json();
}

export async function deleteUserData(accessToken) {
  const response = await fetch(`${BASE_URL}/user-data`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${accessToken}` },
  });
  if (!response.ok) throw new Error('Failed to delete user data');
}

export async function getSavedPlaylists(accessToken) {
  const response = await fetch(`${BASE_URL}/playlists`, {
    headers: { 'Authorization': `Bearer ${accessToken}` },
  });
  if (!response.ok) throw new Error('Failed to load playlists');
  return response.json();
}