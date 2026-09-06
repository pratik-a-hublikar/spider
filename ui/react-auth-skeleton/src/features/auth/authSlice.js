import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { authService } from '../../services/authService';

const storedToken = localStorage.getItem('authToken');

const getErrorMessage = (error) =>
  error?.response?.data?.message ||
  error?.response?.data?.error ||
  error?.message ||
  'Something went wrong.';

export const loginUser = createAsyncThunk(
  'auth/loginUser',
  async (credentials, { rejectWithValue }) => {
    try {
      return await authService.login(credentials);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const restoreSession = createAsyncThunk(
  'auth/restoreSession',
  async (_, { rejectWithValue }) => {
    try {
      const user = await authService.getCurrentUser();
      return user;
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    token: storedToken,
    user: null,
    isAuthenticated: false,
    initialized: !storedToken,
    loading: false,
    error: null,
  },
  reducers: {
    logout: (state) => {
      localStorage.removeItem('authToken');
      state.token = null;
      state.user = null;
      state.isAuthenticated = false;
      state.initialized = true;
      state.loading = false;
      state.error = null;
    },
    clearAuthError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        const { token, user } = action.payload;
        localStorage.setItem('authToken', token);
        state.token = token;
        state.user = user;
        state.isAuthenticated = true;
        state.initialized = true;
        state.loading = false;
      })
      .addCase(loginUser.rejected, (state, action) => {
        localStorage.removeItem('authToken');
        state.token = null;
        state.user = null;
        state.isAuthenticated = false;
        state.initialized = true;
        state.loading = false;
        state.error = action.payload || 'Login failed.';
      })
      .addCase(restoreSession.pending, (state) => {
        state.initialized = false;
      })
      .addCase(restoreSession.fulfilled, (state, action) => {
        state.user = action.payload;
        state.isAuthenticated = true;
        state.initialized = true;
        state.loading = false;
        state.error = null;
      })
      .addCase(restoreSession.rejected, (state) => {
        localStorage.removeItem('authToken');
        state.token = null;
        state.user = null;
        state.isAuthenticated = false;
        state.initialized = true;
        state.loading = false;
      });
  },
});

export const { logout, clearAuthError } = authSlice.actions;
export default authSlice.reducer;
