import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { logout } from '../auth/authSlice';
import { userService } from '../../services/userService';

const getErrorMessage = (error) => (
  error?.response?.data?.message
  || error?.response?.data?.error
  || error?.message
  || 'Unable to process the user request.'
);

const createThunk = (type, operation) => createAsyncThunk(
  type,
  async (argument, { rejectWithValue }) => {
    try {
      return await operation(argument);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const fetchUsers = createThunk('user/fetchUsers', (request) => userService.getUsers(request));
export const fetchUser = createThunk('user/fetchUser', (uuid) => userService.getUser(uuid));
export const createUser = createThunk('user/createUser', (user) => userService.createUser(user));
export const updateUser = createThunk('user/updateUser', ({ uuid, user }) => userService.updateUser(uuid, user));
export const deleteUser = createThunk('user/deleteUser', (uuid) => userService.deleteUser(uuid));
export const fetchOrganizations = createThunk(
  'user/fetchOrganizations',
  (request) => userService.getOrganizations(request),
);
export const fetchLocations = createThunk(
  'user/fetchLocations',
  (request) => userService.getLocations(request),
);
export const fetchUserRoles = createThunk(
  'user/fetchRoles',
  (request) => userService.getRoles(request),
);
export const fetchReportingUsers = createThunk(
  'user/fetchReportingUsers',
  (request) => userService.getUsers(request),
);

const initialState = {
  records: [],
  totalElements: 0,
  listStatus: 'idle',
  listError: null,
  activeListRequestId: null,
  selectedUser: null,
  detailStatus: 'idle',
  detailError: null,
  activeDetailRequestId: null,
  createStatus: 'idle',
  updateStatus: 'idle',
  deletingId: null,
  mutationError: null,
  notification: null,
  organizations: [],
  organizationStatus: 'idle',
  organizationError: null,
  activeOrganizationRequestId: null,
  locations: [],
  locationStatus: 'idle',
  locationError: null,
  activeLocationRequestId: null,
  roles: [],
  roleStatus: 'idle',
  roleError: null,
  activeRoleRequestId: null,
  reportingUsers: [],
  reportingUserStatus: 'idle',
  reportingUserError: null,
  activeReportingUserRequestId: null,
};

const userSlice = createSlice({
  name: 'userManagement',
  initialState,
  reducers: {
    clearUserEditor: (state) => {
      state.selectedUser = null;
      state.detailStatus = 'idle';
      state.detailError = null;
      state.activeDetailRequestId = null;
      state.createStatus = 'idle';
      state.updateStatus = 'idle';
      state.mutationError = null;
      state.roles = [];
      state.roleStatus = 'idle';
      state.roleError = null;
      state.activeRoleRequestId = null;
      state.reportingUsers = [];
      state.reportingUserStatus = 'idle';
      state.reportingUserError = null;
      state.activeReportingUserRequestId = null;
    },
    clearUserNotification: (state) => {
      state.notification = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUsers.pending, (state, action) => {
        state.listStatus = 'loading';
        state.listError = null;
        state.activeListRequestId = action.meta.requestId;
      })
      .addCase(fetchUsers.fulfilled, (state, action) => {
        if (state.activeListRequestId !== action.meta.requestId) return;
        state.records = action.payload.records;
        state.totalElements = action.payload.totalElements;
        state.listStatus = 'succeeded';
        state.activeListRequestId = null;
      })
      .addCase(fetchUsers.rejected, (state, action) => {
        if (state.activeListRequestId !== action.meta.requestId) return;
        state.listStatus = 'failed';
        state.listError = action.payload || 'Unable to load users.';
        state.activeListRequestId = null;
      })
      .addCase(fetchUser.pending, (state, action) => {
        state.selectedUser = null;
        state.detailStatus = 'loading';
        state.detailError = null;
        state.mutationError = null;
        state.activeDetailRequestId = action.meta.requestId;
      })
      .addCase(fetchUser.fulfilled, (state, action) => {
        if (state.activeDetailRequestId !== action.meta.requestId) return;
        state.selectedUser = action.payload;
        state.detailStatus = 'succeeded';
        state.activeDetailRequestId = null;
      })
      .addCase(fetchUser.rejected, (state, action) => {
        if (state.activeDetailRequestId !== action.meta.requestId) return;
        state.detailStatus = 'failed';
        state.detailError = action.payload || 'Unable to load the user.';
        state.activeDetailRequestId = null;
      })
      .addCase(createUser.pending, (state) => {
        state.createStatus = 'loading';
        state.mutationError = null;
      })
      .addCase(createUser.fulfilled, (state, action) => {
        state.createStatus = 'succeeded';
        state.notification = { message: action.payload.message, severity: 'success' };
      })
      .addCase(createUser.rejected, (state, action) => {
        state.createStatus = 'failed';
        state.mutationError = action.payload || 'Unable to create the user.';
      })
      .addCase(updateUser.pending, (state) => {
        state.updateStatus = 'loading';
        state.mutationError = null;
      })
      .addCase(updateUser.fulfilled, (state, action) => {
        const user = action.payload.user;
        const index = state.records.findIndex((item) => String(item.id) === String(user?.id));
        if (index !== -1) state.records[index] = { ...state.records[index], ...user };
        state.updateStatus = 'succeeded';
        state.notification = { message: action.payload.message, severity: 'success' };
      })
      .addCase(updateUser.rejected, (state, action) => {
        state.updateStatus = 'failed';
        state.mutationError = action.payload || 'Unable to update the user.';
      })
      .addCase(deleteUser.pending, (state, action) => {
        state.deletingId = action.meta.arg;
        state.notification = null;
      })
      .addCase(deleteUser.fulfilled, (state, action) => {
        state.records = state.records.filter((item) => item.uuid !== action.payload.uuid);
        state.totalElements = Math.max(0, state.totalElements - 1);
        state.deletingId = null;
        state.notification = { message: action.payload.message, severity: 'success' };
      })
      .addCase(deleteUser.rejected, (state, action) => {
        state.deletingId = null;
        state.notification = {
          message: action.payload || 'Unable to delete the user.',
          severity: 'error',
        };
      })
      .addCase(fetchOrganizations.pending, (state, action) => {
        state.organizationStatus = 'loading';
        state.organizationError = null;
        state.activeOrganizationRequestId = action.meta.requestId;
      })
      .addCase(fetchOrganizations.fulfilled, (state, action) => {
        if (state.activeOrganizationRequestId !== action.meta.requestId) return;
        state.organizations = action.payload.records;
        state.organizationStatus = 'succeeded';
        state.activeOrganizationRequestId = null;
      })
      .addCase(fetchOrganizations.rejected, (state, action) => {
        if (state.activeOrganizationRequestId !== action.meta.requestId) return;
        state.organizationStatus = 'failed';
        state.organizationError = action.payload || 'Unable to load organizations.';
        state.activeOrganizationRequestId = null;
      })
      .addCase(fetchLocations.pending, (state, action) => {
        state.locationStatus = 'loading';
        state.locationError = null;
        state.activeLocationRequestId = action.meta.requestId;
      })
      .addCase(fetchLocations.fulfilled, (state, action) => {
        if (state.activeLocationRequestId !== action.meta.requestId) return;
        state.locations = action.payload.records;
        state.locationStatus = 'succeeded';
        state.activeLocationRequestId = null;
      })
      .addCase(fetchLocations.rejected, (state, action) => {
        if (state.activeLocationRequestId !== action.meta.requestId) return;
        state.locationStatus = 'failed';
        state.locationError = action.payload || 'Unable to load locations.';
        state.activeLocationRequestId = null;
      })
      .addCase(fetchUserRoles.pending, (state, action) => {
        state.roleStatus = 'loading';
        state.roleError = null;
        state.activeRoleRequestId = action.meta.requestId;
      })
      .addCase(fetchUserRoles.fulfilled, (state, action) => {
        if (state.activeRoleRequestId !== action.meta.requestId) return;
        state.roles = action.payload.records;
        state.roleStatus = 'succeeded';
        state.activeRoleRequestId = null;
      })
      .addCase(fetchUserRoles.rejected, (state, action) => {
        if (state.activeRoleRequestId !== action.meta.requestId) return;
        state.roleStatus = 'failed';
        state.roleError = action.payload || 'Unable to load roles.';
        state.activeRoleRequestId = null;
      })
      .addCase(fetchReportingUsers.pending, (state, action) => {
        state.reportingUserStatus = 'loading';
        state.reportingUserError = null;
        state.activeReportingUserRequestId = action.meta.requestId;
      })
      .addCase(fetchReportingUsers.fulfilled, (state, action) => {
        if (state.activeReportingUserRequestId !== action.meta.requestId) return;
        state.reportingUsers = action.payload.records;
        state.reportingUserStatus = 'succeeded';
        state.activeReportingUserRequestId = null;
      })
      .addCase(fetchReportingUsers.rejected, (state, action) => {
        if (state.activeReportingUserRequestId !== action.meta.requestId) return;
        state.reportingUserStatus = 'failed';
        state.reportingUserError = action.payload || 'Unable to load reporting users.';
        state.activeReportingUserRequestId = null;
      })
      .addCase(logout, () => initialState);
  },
});

export const { clearUserEditor, clearUserNotification } = userSlice.actions;
export default userSlice.reducer;
