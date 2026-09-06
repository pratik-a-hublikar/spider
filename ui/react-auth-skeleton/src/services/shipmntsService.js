import apiClient from './apiClient';

let inFlightRequest = null;

export const shipmntsService = {
  async fetchData() {
    if (!inFlightRequest) {
      inFlightRequest = apiClient.get('/shipmnts/fetch')
        .then((response) => response.data?.data ?? response.data)
        .finally(() => { inFlightRequest = null; });
    }
    return inFlightRequest;
  },

  async generateRefreshToken({ email, password, interactive } = {}) {
    const body = {};
    if (email) body.email = email;
    if (password) body.password = password;
    if (interactive) body.interactive = true;
    const res = await apiClient.post('/shipmnts/authenticate', body);
    return res.data?.data || {};
  },

  async fetchProfile() {
    const res = await apiClient.get('/shipmnts/fetch');
    return res.data?.data || {};
  },

  async getAuthorizationUrl() {
    const res = await apiClient.get('/shipmnts/oauth/authorize-url');
    return res.data?.data?.authorizeUrl || '';
  },

  async exchangeAuthorizationCode(code) {
    const res = await apiClient.post('/shipmnts/oauth/callback', { code });
    return res.data?.data || {};
  },
};

export default shipmntsService;
