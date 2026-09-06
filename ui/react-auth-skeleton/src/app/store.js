import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import menuReducer from '../features/menu/menuSlice';
import roleReducer from '../features/role/roleSlice';
import userReducer from '../features/user/userSlice';
import organizationReducer from '../features/organization/organizationSlice';
import locationReducer from '../features/location/locationSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    menu: menuReducer,
    role: roleReducer,
    userManagement: userReducer,
    organization: organizationReducer,
    location: locationReducer,
  },
});
