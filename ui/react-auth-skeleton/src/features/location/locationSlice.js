import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { logout } from '../auth/authSlice';
import { locationService } from '../../services/locationService';
import { organizationService } from '../../services/organizationService';

const errorMessage = (error) => error?.response?.data?.message || error?.response?.data?.error || error?.message || 'Location request failed.';
const thunk = (type, operation) => createAsyncThunk(type, async (arg, { rejectWithValue }) => {
  try { return await operation(arg); } catch (error) { return rejectWithValue(errorMessage(error)); }
});

export const fetchLocations = thunk('location/fetchAll', (request) => locationService.filter(request));
export const fetchLocation = thunk('location/fetchOne', (uuid) => locationService.get(uuid));
export const searchLocationOrganizations = thunk('location/searchOrganizations', (request) => organizationService.filter(request));
export const createLocation = thunk('location/create', (request) => locationService.create(request));
export const updateLocation = thunk('location/update', ({ uuid, request }) => locationService.update(uuid, request));
export const deleteLocation = thunk('location/delete', (uuid) => locationService.delete(uuid));
export const fetchLocationUsers = thunk('location/fetchUsers', (locationUuid) => locationService.getUsers(locationUuid));
export const removeLocationUser = thunk('location/removeUser', ({ locationUuid, userUuid }) => locationService.removeUser(locationUuid, userUuid));

const initialState = {
  records: [], totalElements: 0, listStatus: 'idle', listError: null, listRequestId: null,
  selected: null, detailStatus: 'idle', detailError: null, detailRequestId: null,
  organizationOptions: [], organizationStatus: 'idle', organizationError: null, organizationRequestId: null,
  createStatus: 'idle', updateStatus: 'idle', deletingId: null, mutationError: null, notification: null,
  locationUsers: [], usersStatus: 'idle', usersError: null, usersLocationId: null, usersRequestId: null, removingUserId: null,
};

const slice = createSlice({
  name: 'location',
  initialState,
  reducers: {
    clearLocationEditor: (state) => { state.selected = null; state.detailStatus = 'idle'; state.detailError = null; state.createStatus = 'idle'; state.updateStatus = 'idle'; state.mutationError = null; },
    clearLocationNotification: (state) => { state.notification = null; },
    clearLocationUsers: (state) => { state.locationUsers = []; state.usersStatus = 'idle'; state.usersError = null; state.usersLocationId = null; state.usersRequestId = null; state.removingUserId = null; },
  },
  extraReducers: (builder) => builder
    .addCase(fetchLocations.pending, (state, action) => { state.listStatus = 'loading'; state.listError = null; state.listRequestId = action.meta.requestId; })
    .addCase(fetchLocations.fulfilled, (state, action) => { if (state.listRequestId !== action.meta.requestId) return; state.records = action.payload.records; state.totalElements = action.payload.totalElements; state.listStatus = 'succeeded'; state.listRequestId = null; })
    .addCase(fetchLocations.rejected, (state, action) => { if (state.listRequestId !== action.meta.requestId) return; state.listStatus = 'failed'; state.listError = action.payload; state.listRequestId = null; })
    .addCase(fetchLocation.pending, (state, action) => { state.selected = null; state.detailStatus = 'loading'; state.detailError = null; state.detailRequestId = action.meta.requestId; })
    .addCase(fetchLocation.fulfilled, (state, action) => { if (state.detailRequestId !== action.meta.requestId) return; state.selected = action.payload; state.detailStatus = 'succeeded'; state.detailRequestId = null; })
    .addCase(fetchLocation.rejected, (state, action) => { if (state.detailRequestId !== action.meta.requestId) return; state.detailStatus = 'failed'; state.detailError = action.payload; state.detailRequestId = null; })
    .addCase(searchLocationOrganizations.pending, (state, action) => { state.organizationStatus = 'loading'; state.organizationError = null; state.organizationRequestId = action.meta.requestId; })
    .addCase(searchLocationOrganizations.fulfilled, (state, action) => { if (state.organizationRequestId !== action.meta.requestId) return; state.organizationOptions = action.payload.records; state.organizationStatus = 'succeeded'; state.organizationRequestId = null; })
    .addCase(searchLocationOrganizations.rejected, (state, action) => { if (state.organizationRequestId !== action.meta.requestId) return; state.organizationStatus = 'failed'; state.organizationError = action.payload; state.organizationRequestId = null; })
    .addCase(createLocation.pending, (state) => { state.createStatus = 'loading'; state.mutationError = null; })
    .addCase(createLocation.fulfilled, (state, action) => { state.createStatus = 'succeeded'; state.notification = { message: action.payload.message, severity: 'success' }; })
    .addCase(createLocation.rejected, (state, action) => { state.createStatus = 'failed'; state.mutationError = action.payload; })
    .addCase(updateLocation.pending, (state) => { state.updateStatus = 'loading'; state.mutationError = null; })
    .addCase(updateLocation.fulfilled, (state, action) => { state.updateStatus = 'succeeded'; state.notification = { message: action.payload.message, severity: 'success' }; })
    .addCase(updateLocation.rejected, (state, action) => { state.updateStatus = 'failed'; state.mutationError = action.payload; })
    .addCase(deleteLocation.pending, (state, action) => { state.deletingId = action.meta.arg; state.notification = null; })
    .addCase(deleteLocation.fulfilled, (state, action) => { state.deletingId = null; state.notification = { message: action.payload.message, severity: 'success' }; })
    .addCase(deleteLocation.rejected, (state, action) => { state.deletingId = null; state.notification = { message: action.payload, severity: 'error' }; })
    .addCase(fetchLocationUsers.pending, (state, action) => { state.locationUsers = []; state.usersStatus = 'loading'; state.usersError = null; state.usersLocationId = action.meta.arg; state.usersRequestId = action.meta.requestId; })
    .addCase(fetchLocationUsers.fulfilled, (state, action) => { if (state.usersRequestId !== action.meta.requestId) return; state.locationUsers = action.payload; state.usersStatus = 'succeeded'; state.usersRequestId = null; })
    .addCase(fetchLocationUsers.rejected, (state, action) => { if (state.usersRequestId !== action.meta.requestId) return; state.usersStatus = 'failed'; state.usersError = action.payload; state.usersRequestId = null; })
    .addCase(removeLocationUser.pending, (state, action) => { state.removingUserId = action.meta.arg.userUuid; state.usersError = null; state.notification = null; })
    .addCase(removeLocationUser.fulfilled, (state, action) => { const { locationUuid, userUuid, message } = action.payload; state.removingUserId = null; state.locationUsers = state.locationUsers.filter((user) => user.uuid !== userUuid); const location = state.records.find((item) => item.uuid === locationUuid); if (location) location.userCount = Math.max(0, Number(location.userCount || 0) - 1); state.notification = { message, severity: 'success' }; })
    .addCase(removeLocationUser.rejected, (state, action) => { state.removingUserId = null; state.usersError = action.payload; state.notification = { message: action.payload, severity: 'error' }; })
    .addCase(logout, () => initialState),
});

export const { clearLocationEditor, clearLocationNotification, clearLocationUsers } = slice.actions;
export default slice.reducer;
