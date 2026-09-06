import { useEffect, useState } from 'react';
import { Box, CircularProgress, Alert, Typography } from '@mui/material';

/**
 * OAuth Callback Handler
 * This page is opened by the Shipmnts OAuth flow redirect.
 * It captures the authorization code from the URL and sends it to the backend to exchange for tokens.
 */
const AuthCallbackPage = () => {
  const [status, setStatus] = useState('processing');
  const [message, setMessage] = useState('Processing authentication...');

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const params = new URLSearchParams(window.location.search);
        const code = params.get('code');
        const error = params.get('error');

        // Handle OAuth error
        if (error) {
          setStatus('error');
          setMessage(`Authentication failed: ${error} - ${params.get('error_description') || ''}`);
          return;
        }

        // Handle missing code
        if (!code) {
          setStatus('error');
          setMessage('No authorization code received from Shipmnts');
          return;
        }

        // Exchange code for token with backend
        const response = await fetch('http://localhost:8080/api/v1/identity/shipmnts/oauth/callback', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ code }),
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          const errorMsg = errorData?.message || `HTTP ${response.status}`;
          throw new Error(errorMsg);
        }

        const data = await response.json();

        // Success - notify parent window and close
        setStatus('success');
        setMessage('Authentication successful! Closing window...');

        // Notify the parent window (the app) that authentication succeeded
        if (window.opener) {
          window.opener.postMessage({
            type: 'SHIPMNTS_AUTH_SUCCESS',
            refreshToken: data?.data?.refreshToken,
          }, window.location.origin);

          // Close this popup window after a short delay
          setTimeout(() => {
            window.close();
          }, 1500);
        } else {
          // If no opener (direct access), redirect to home
          setTimeout(() => {
            window.location.href = '/';
          }, 2000);
        }
      } catch (error) {
        setStatus('error');
        setMessage(`Error: ${error.message}`);
      }
    };

    handleCallback();
  }, []);

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        bgcolor: 'background.default',
        padding: 2,
      }}
    >
      <Box sx={{ maxWidth: 400, width: '100%' }}>
        <Typography variant="h5" gutterBottom align="center" sx={{ mb: 3 }}>
          Shipmnts Authentication
        </Typography>

        {status === 'processing' && (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
            <CircularProgress />
            <Alert severity="info">{message}</Alert>
          </Box>
        )}

        {status === 'success' && (
          <Alert severity="success">{message}</Alert>
        )}

        {status === 'error' && (
          <Alert severity="error">{message}</Alert>
        )}
      </Box>
    </Box>
  );
};

export default AuthCallbackPage;
