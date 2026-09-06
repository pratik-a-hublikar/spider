import { useState, useEffect } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Stack,
  CircularProgress,
  Alert,
  FormControlLabel,
  Checkbox,
  Typography,
} from '@mui/material';
import shipmntsService from '../services/shipmntsService';

const ShipmntsAuthDialog = ({ open, onClose, oauthMode = true }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [interactive, setInteractive] = useState(false);

  // Handle OAuth popup notifications
  useEffect(() => {
    const handleMessage = (event) => {
      // Verify origin for security
      if (event.origin !== window.location.origin) return;

      if (event.data?.type === 'SHIPMNTS_AUTH_SUCCESS') {
        setSuccess('Authentication successful! Token received.');
        setTimeout(() => {
          onClose();
          // Optionally reload to update user data with new token
          window.location.reload();
        }, 1500);
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [onClose]);

  const handleOAuthFlow = async () => {
    setError(null);
    setSuccess(null);
    setLoading(true);
    try {
      const authUrl = await shipmntsService.getAuthorizationUrl();
      if (!authUrl) {
        throw new Error('Failed to get authorization URL');
      }

      // Open popup window for OAuth flow
      const width = 500;
      const height = 600;
      const left = window.screenX + (window.outerWidth - width) / 2;
      const top = window.screenY + (window.outerHeight - height) / 2;

      window.open(authUrl, 'shipmnts_auth', `width=${width},height=${height},left=${left},top=${top}`);
      setLoading(false);
    } catch (e) {
      setError(e?.message || 'Failed to start OAuth flow');
      setLoading(false);
    }
  };

  const handleScriptFlow = async () => {
    setError(null);
    setSuccess(null);
    setLoading(true);
    try {
      const token = await shipmntsService.generateRefreshToken({ email, password, interactive });
      if (token && token.refreshToken) {
        setSuccess('Refresh token obtained successfully');
      } else {
        setSuccess('Authentication completed');
      }
      // Optionally fetch profile immediately
      try {
        const profile = await shipmntsService.fetchProfile();
        if (profile && profile.user_profile && profile.user_profile.email) {
          setSuccess(`Authenticated; user: ${profile.user_profile.email}`);
        }
      } catch (e) {
        // ignore fetch profile error
      }
      onClose();
    } catch (e) {
      setError(e?.response?.data?.message || e?.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Shipmnts Authentication</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}
          {success && <Alert severity="success">{success}</Alert>}

          {oauthMode ? (
            <Stack spacing={2}>
              <Alert severity="info">
                Click below to open the Shipmnts login page. After authentication, the window will close automatically.
              </Alert>
              <Typography variant="body2" color="text.secondary">
                You can also use the traditional script-based flow with email/password below.
              </Typography>
            </Stack>
          ) : null}

          <TextField
            label="Shipmnts Email (Optional)"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            fullWidth
            autoComplete="username"
            disabled={oauthMode && !interactive}
          />
          <TextField
            label="Shipmnts Password (Optional)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            type="password"
            fullWidth
            autoComplete="current-password"
            disabled={oauthMode && !interactive}
          />
          <FormControlLabel
            control={<Checkbox checked={interactive} onChange={(e) => setInteractive(e.target.checked)} />}
            label="Open visible browser for manual CAPTCHA/MFA completion"
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
        {oauthMode ? (
          <Button onClick={handleOAuthFlow} variant="contained" disabled={loading}>
            {loading ? <CircularProgress size={20} /> : 'Open Shipmnts Login'}
          </Button>
        ) : (
          <Button onClick={handleScriptFlow} variant="contained" disabled={loading}>
            {loading ? <CircularProgress size={20} /> : 'Authenticate'}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default ShipmntsAuthDialog;
