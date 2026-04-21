export const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080';

export function errorMessageFromText(text, fallback = 'Request failed') {
  if (!text) {
    return fallback;
  }
  try {
    const data = JSON.parse(text);
    const fieldMessage = data?.fields ? Object.values(data.fields).join(', ') : '';
    return fieldMessage || data?.message || fallback;
  } catch {
    return text || fallback;
  }
}

export async function api(path, { method = 'GET', token, body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();

  if (!response.ok) {
    throw new Error(errorMessageFromText(text));
  }

  return text ? JSON.parse(text) : null;
}
