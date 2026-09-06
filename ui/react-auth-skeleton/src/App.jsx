import { useEffect, useRef } from 'react';
import { Route, Routes } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { restoreSession } from './features/auth/authSlice';
import { routes } from './config/routesConfig';

const App = () => {
  const dispatch = useDispatch();
  const restoreRequested = useRef(false);
  const { token, initialized } = useSelector((state) => state.auth);

  useEffect(() => {
    if (token && !initialized && !restoreRequested.current) {
      restoreRequested.current = true;
      dispatch(restoreSession());
    }
  }, [dispatch, token, initialized]);

  const renderRoutes = (routesList) => {
    return routesList.map((route, index) => {
      if (route.children) {
        return (
          <Route key={route.path || index} path={route.path} element={route.element}>
            {renderRoutes(route.children)}
          </Route>
        );
      }
      return <Route key={route.path || index} path={route.path} element={route.element} />;
    });
  };

  return (
    <Routes>
      {renderRoutes(routes)}
    </Routes>
  );
};

export default App;
