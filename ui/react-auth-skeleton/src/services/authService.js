import apiClient from './apiClient';

const loginEndpoint = import.meta.env.VITE_LOGIN_ENDPOINT || '/auth/login';
const meEndpoint = import.meta.env.VITE_ME_ENDPOINT || '/auth/me';

const getAuthorizationToken = (headers) => {
  const authorization = headers?.get?.('authorization')
    ?? headers?.authorization
    ?? headers?.Authorization;

  if (typeof authorization !== 'string') return null;

  const token = authorization.replace(/^Bearer\s+/i, '').trim();
  return token || null;
};

const normalizeUser = (user) => {
  if (!user || typeof user !== 'object') return null;
  return {
    ...user,
    name: user.name || user.fullName || [user.fname, user.lname].filter(Boolean).join(' '),
    orgId: user.orgId ?? user.orgIdList?.[0] ?? null,
  };
};

const normalizeLoginResponse = (payload, headers) => {
  const data = payload?.data ?? payload;
  const token = getAuthorizationToken(headers) ?? data?.accessToken ?? data?.token;
  const user = normalizeUser(data?.user);

  if (!token) {
    throw new Error('Authentication succeeded but no Authorization token was returned by the backend.');
  }

  return { token, user };
};

export const authService = {
  async login(credentials) {
    const response = await apiClient.post(loginEndpoint, credentials);
    return normalizeLoginResponse(response.data, response.headers);
  },

  async getCurrentUser() {
    const response = await apiClient.get(meEndpoint);
    const data = response.data?.data ?? response.data;
    return normalizeUser(data?.user ?? data);
  },
};
