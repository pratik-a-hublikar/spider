import { useState } from 'react';
import {
  AppBar,
  Box,
  Button,
  Container,
  IconButton,
  Toolbar,
  Typography,
} from '@mui/material';
import { Outlet, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../features/auth/authSlice';
import MenuDrawer from '../components/MenuDrawer';
import ShipmntsAuthDialog from '../components/ShipmntsAuthDialog';

const FourLineMenuIcon = () => (
  <Box sx={{ width: 22 }}>
    {[0, 1, 2, 3].map((line) => (
      <Box key={line} sx={{ height: 2, bgcolor: 'currentColor', my: '3px', borderRadius: 1 }} />
    ))}
  </Box>
);

const AppLayout = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useSelector((state) => state.auth);
  const [menuOpen, setMenuOpen] = useState(false);
  const [authOpen, setAuthOpen] = useState(false);

  const handleLogout = () => {
    dispatch(logout());
    setMenuOpen(false);
    navigate('/login', { replace: true });
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="static">
        <Toolbar>
          {isAuthenticated && (
            <IconButton color="inherit" edge="start" aria-label="toggle navigation menu" onClick={() => setMenuOpen(true)} sx={{ mr: 2 }}>
              <FourLineMenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            React Application
          </Typography>
          {isAuthenticated && (
            <>
              <Typography variant="body2" sx={{ mr: 2, display: { xs: 'none', sm: 'block' } }}>
                {user?.name || user?.username || user?.email || 'Authenticated user'}
              </Typography>
              <Button color="inherit" variant="outlined" onClick={() => setAuthOpen(true)} sx={{ mr: 1 }}>
                Fetch
              </Button>
              <Button color="inherit" variant="outlined" onClick={handleLogout}>
                Logout
              </Button>
              <ShipmntsAuthDialog open={authOpen} onClose={() => setAuthOpen(false)} />
            </>
          )}
        </Toolbar>
      </AppBar>

      {isAuthenticated && <MenuDrawer open={menuOpen} onClose={() => setMenuOpen(false)} />}

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Outlet />
      </Container>
    </Box>
  );
};

export default AppLayout;
