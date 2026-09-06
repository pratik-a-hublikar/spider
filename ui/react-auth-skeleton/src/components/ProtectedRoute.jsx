import { Alert, Box, CircularProgress } from '@mui/material';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { useEffect, useMemo } from 'react';
import { fetchMenu } from '../features/menu/menuSlice';
import { authenticatedUserRoutes } from '../config/routeAccessConfig';

const normalizePath = (url) => {
  if (!url || typeof url !== 'string') return null;

  try {
    const pathname = new URL(url, window.location.origin).pathname;
    const normalized = `/${pathname}`.replace(/\/{2,}/g, '/').replace(/\/$/, '');
    return normalized || '/';
  } catch {
    return null;
  }
};

const collectMenuPaths = (items, paths = new Set()) => {
  if (!Array.isArray(items)) return paths;

  items.forEach((item) => {
    const path = normalizePath(item?.url);
    if (path) paths.add(path);
    collectMenuPaths(item?.subModules ?? item?.childModules, paths);
  });

  return paths;
};

const ProtectedRoute = () => {
  const dispatch = useDispatch();
  const location = useLocation();
  const { isAuthenticated, initialized } = useSelector((state) => state.auth);
  const { items, status, error } = useSelector((state) => state.menu);
  const allowedPaths = useMemo(() => collectMenuPaths(items), [items]);

  useEffect(() => {
    if (initialized && isAuthenticated && status === 'idle') {
      dispatch(fetchMenu());
    }
  }, [dispatch, initialized, isAuthenticated, status]);

  if (!initialized) {
    return (
      <Box
        sx={{
          minHeight: '100vh',
          display: 'grid',
          placeItems: 'center',
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  const currentPath = normalizePath(location.pathname);
  const isAuthenticatedUserRoute = authenticatedUserRoutes
    .map(normalizePath)
    .includes(currentPath);

  if (isAuthenticatedUserRoute) {
    return <Outlet />;
  }

  if (status === 'idle' || status === 'loading') {
    return (
      <Box sx={{ minHeight: '50vh', display: 'grid', placeItems: 'center' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (status === 'failed') {
    return <Alert severity="error">Unable to verify route access: {error}</Alert>;
  }

  if (!currentPath || !allowedPaths.has(currentPath)) {
    return <Alert severity="warning">You do not have access to this page.</Alert>;
  }

  return <Outlet />;
};

export default ProtectedRoute;
