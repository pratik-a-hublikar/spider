import { Navigate } from 'react-router-dom';
import AppLayout from '../layouts/AppLayout';
import ProtectedRoute from '../components/ProtectedRoute';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import AuthCallbackPage from '../pages/AuthCallbackPage';
import Menu from '../pages/manage-access/Menu';
import Role from '../pages/manage-access/Role';
import User from '../pages/manage-access/User';
import Organization from '../pages/manage-access/Organization';
import Location from '../pages/manage-access/Location';
import SalesPerformanceMirror from '../pages/SalesPerformanceMirror';

export const routes = [
  {
    path: '/auth/callback',
    element: <AuthCallbackPage />,
  },
  {
    path: '/',
    element: <AppLayout />,
    children: [
      {
        path: 'home',
        element: <ProtectedRoute />,
        children: [
          {
            path: '',
            element: <HomePage />,
          },
        ],
      },
      {
        path: 'menu-list',
        element: <ProtectedRoute />,
        children: [
          {
            path: '',
            element: <Menu />,
          },
        ],
      },
      {
        path: 'role-list',
        element: <ProtectedRoute />,
        children: [
          {
            path: '',
            element: <Role />,
          },
        ],
      },
      {
        path: 'user-list',
        element: <ProtectedRoute />,
        children: [
          {
            path: '',
            element: <User />,
          },
        ],
      },
      {
        path: 'org-location-list',
        element: <ProtectedRoute />,
        children: [
          {
            path: '',
            element: <Organization />,
          },
        ],
      },
      {
        path: 'user-location-list',
        element: <ProtectedRoute />,
        children: [
          {
            path: '',
            element: <Location />,
          },
        ],
      },
      {
        path: 'sales-performance',
        element: <ProtectedRoute />,
        children: [
          {
            path: '',
            element: <SalesPerformanceMirror />,
          },
        ],
      },
      {
        path: 'login',
        element: <LoginPage />,
      },
      {
        path: '*',
        element: <Navigate to="/login" replace />,
      },
    ],
  },
];
