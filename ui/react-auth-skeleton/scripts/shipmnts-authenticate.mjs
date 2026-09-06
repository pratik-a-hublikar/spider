import { chromium } from 'playwright-core';
import { readFile, rename, writeFile } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDirectory = dirname(fileURLToPath(import.meta.url));
const projectRoot = resolve(scriptDirectory, '..', '..', '..');
const envPath = resolve(projectRoot, '.env');
const authenticationEndpoint = 'https://auth.api.shipmnts.com/user_management/authenticate';
const bookingUrl = 'https://booking.shipmnts.com/';

function parseEnv(contents) {
  const values = {};
  for (const rawLine of contents.split(/\r?\n/)) {
    const line = rawLine.trim();
    if (!line || line.startsWith('#')) continue;

    const separator = line.indexOf('=');
    if (separator < 1) continue;

    const key = line.slice(0, separator).trim();
    let value = line.slice(separator + 1).trim();
    if ((value.startsWith('"') && value.endsWith('"'))
        || (value.startsWith("'") && value.endsWith("'"))) {
      value = value.slice(1, -1);
    }
    values[key] = value;
  }
  return values;
}

function requireValue(values, key) {
  const value = values[key];
  if (!value) {
    throw new Error(`${key} is missing from ${envPath}`);
  }
  return value;
}

function replaceEnvValue(contents, key, value) {
  if (/\r|\n/.test(value)) {
    throw new Error(`Refusing to store an invalid ${key} value.`);
  }

  const escapedKey = key.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const linePattern = new RegExp(`^${escapedKey}=.*$`, 'm');
  if (linePattern.test(contents)) {
    return contents.replace(linePattern, `${key}=${value}`);
  }
  const separator = contents.endsWith('\n') ? '' : '\n';
  return `${contents}${separator}${key}=${value}\n`;
}

async function persistTokens(envContents, authentication) {
  let updated = replaceEnvValue(envContents, 'SHIPMNTS_REFRESH_TOKEN', authentication.refreshToken);
  if (authentication.organizationId) {
    updated = replaceEnvValue(updated, 'SHIPMNTS_ORGANIZATION_ID', authentication.organizationId);
  }

  const temporaryPath = `${envPath}.tmp`;
  await writeFile(temporaryPath, updated, { encoding: 'utf8', mode: 0o600 });
  await rename(temporaryPath, envPath);
}

function captureAuthentication(page) {
  return new Promise((resolveAuthentication) => {
    page.on('response', async (response) => {
      if (response.url() !== authenticationEndpoint || response.request().method() !== 'POST') return;

      try {
        const body = await response.json();
        if (response.ok() && typeof body.refresh_token === 'string' && body.refresh_token) {
          resolveAuthentication({
            refreshToken: body.refresh_token,
            organizationId: typeof body.organization_id === 'string' ? body.organization_id : '',
          });
        }
      } catch {
        // Ignore non-JSON and intermediate authentication responses.
      }
    });
  });
}

async function waitForAuthentication(authenticationPromise) {
  const timeoutPromise = new Promise((_, rejectAuthentication) => {
    setTimeout(() => rejectAuthentication(new Error(
      'Timed out waiting for Shipmnts authentication. Check the opened Chrome window for an error, CAPTCHA, or MFA prompt.',
    )), 5 * 60 * 1000);
  });
  return Promise.race([authenticationPromise, timeoutPromise]);
}

async function submitCredentials(page, email, password) {
  const emailInput = page.locator('input[type="email"], input[name*="email" i]').first();
  await emailInput.waitFor({ state: 'visible', timeout: 30_000 });
  await emailInput.fill(email);
  await page.locator('button[type="submit"]').first().click();

  const passwordInput = page.locator('input[type="password"]').first();
  await passwordInput.waitFor({ state: 'visible', timeout: 30_000 });
  await passwordInput.fill(password);
  await page.locator('button[type="submit"]').first().click();

  console.log('Credentials submitted. Complete any CAPTCHA or MFA prompt in Chrome.');
}

async function main() {
  const envContents = await readFile(envPath, 'utf8');
  const env = parseEnv(envContents);
  const clientId = requireValue(env, 'SHIPMNTS_CLIENT_ID');
  const email = requireValue(env, 'SHIPMNTS_EMAIL');
  const password = requireValue(env, 'SHIPMNTS_PASSWORD');

  if (process.argv.includes('--check')) {
    console.log('Shipmnts authentication configuration is present.');
    return;
  }

  const authorizationUrl = new URL('https://auth.shipmnts.com/');
  authorizationUrl.searchParams.set('client_id', clientId);
  authorizationUrl.searchParams.set('redirect_uri', bookingUrl.slice(0, -1));
  authorizationUrl.searchParams.set('state', JSON.stringify({ path: '/', search: '' }));

  const browser = await chromium.launch({
    channel: 'chrome',
    headless: process.env.SHIPMNTS_AUTH_HEADLESS === 'true',
  });

  try {
    const context = await browser.newContext();
    const page = await context.newPage();
    const authenticationPromise = captureAuthentication(page);

    await page.goto(authorizationUrl.toString(), { waitUntil: 'domcontentloaded' });
    await submitCredentials(page, email, password);

    const authentication = await waitForAuthentication(authenticationPromise);
    await persistTokens(envContents, authentication);
    console.log('Shipmnts authentication succeeded. The refresh token was saved to .env without being printed.');
    console.log('Restart the identity backend so it reads the new token.');
  } finally {
    await browser.close();
  }
}

main().catch((error) => {
  console.error(`Shipmnts authentication failed: ${error.message}`);
  process.exitCode = 1;
});
