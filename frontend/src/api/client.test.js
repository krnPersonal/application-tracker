import assert from 'node:assert/strict';
import { afterEach, test } from 'node:test';
import { api, errorMessageFromText } from './client.js';

const originalFetch = globalThis.fetch;

afterEach(() => {
  globalThis.fetch = originalFetch;
});

test('errorMessageFromText returns field validation messages', () => {
  const message = errorMessageFromText(JSON.stringify({
    message: 'Validation failed',
    fields: {
      email: 'Email is required',
      password: 'Password is too short'
    }
  }));

  assert.equal(message, 'Email is required, Password is too short');
});

test('errorMessageFromText falls back to API message', () => {
  assert.equal(errorMessageFromText('{"message":"Not found"}'), 'Not found');
});

test('errorMessageFromText returns plain text errors', () => {
  assert.equal(errorMessageFromText('Server unavailable'), 'Server unavailable');
});

test('api sends JSON requests with bearer token', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, 'http://localhost:8080/api/example');
    assert.equal(options.method, 'POST');
    assert.equal(options.headers.Authorization, 'Bearer test-token');
    assert.equal(options.headers['Content-Type'], 'application/json');
    assert.equal(options.body, '{"name":"Example"}');

    return new Response('{"ok":true}', { status: 200 });
  };

  const response = await api('/api/example', {
    method: 'POST',
    token: 'test-token',
    body: { name: 'Example' }
  });

  assert.deepEqual(response, { ok: true });
});

test('api returns null for no-content responses', async () => {
  globalThis.fetch = async () => new Response(null, { status: 204 });

  assert.equal(await api('/api/example'), null);
});

test('api throws parsed error messages for failed responses', async () => {
  globalThis.fetch = async () => new Response('{"message":"Request denied"}', { status: 403 });

  await assert.rejects(
    () => api('/api/example'),
    { message: 'Request denied' }
  );
});
