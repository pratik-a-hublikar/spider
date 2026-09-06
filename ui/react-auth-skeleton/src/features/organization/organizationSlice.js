import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { logout } from '../auth/authSlice';
import { organizationService } from '../../services/organizationService';

const errorMessage = (error) => error?.response?.data?.message || error?.response?.data?.error || error?.message || 'Organization request failed.';
const thunk = (type, operation) => createAsyncThunk(type, async (arg, { rejectWithValue }) => {
  try { return await operation(arg); } catch (error) { return rejectWithValue(errorMessage(error)); }
});

export const fetchOrganizations = thunk('organization/fetchAll', (request) => organizationService.filter(request));
export const fetchOrganization = thunk('organization/fetchOne', (uuid) => organizationService.get(uuid));
export const searchParentOrganizations = thunk('organization/searchParents', (request) => organizationService.filter(request));
export const createOrganization = thunk('organization/create', (request) => organizationService.create(request));
export const updateOrganization = thunk('organization/update', ({ uuid, request }) => organizationService.update(uuid, request));
export const deleteOrganization = thunk('organization/delete', (uuid) => organizationService.delete(uuid));

const initialState = {
  records: [], totalElements: 0, listStatus: 'idle', listError: null, listRequestId: null,
  selected: null, detailStatus: 'idle', detailError: null, detailRequestId: null,
  parentOptions: [], parentStatus: 'idle', parentError: null, parentRequestId: null,
  createStatus: 'idle', updateStatus: 'idle', deletingId: null, mutationError: null, notification: null,
};

const slice = createSlice({
  name: 'organization',
  initialState,
  reducers: {
    clearOrganizationEditor: (state) => {
      state.selected = null; state.detailStatus = 'idle'; state.detailError = null;
      state.createStatus = 'idle'; state.updateStatus = 'idle'; state.mutationError = null;
    },
    clearOrganizationNotification: (state) => { state.notification = null; },
  },
  extraReducers: (builder) => builder
    .addCase(fetchOrganizations.pending, (state, action) => { state.listStatus = 'loading'; state.listError = null; state.listRequestId = action.meta.requestId; })
    .addCase(fetchOrganizations.fulfilled, (state, action) => { if (state.listRequestId !== action.meta.requestId) return; state.records = action.payload.records; state.totalElements = action.payload.totalElements; state.listStatus = 'succeeded'; state.listRequestId = null; })
    .addCase(fetchOrganizations.rejected, (state, action) => { if (state.listRequestId !== action.meta.requestId) return; state.listStatus = 'failed'; state.listError = action.payload; state.listRequestId = null; })
    .addCase(fetchOrganization.pending, (state, action) => { state.selected = null; state.detailStatus = 'loading'; state.detailError = null; state.detailRequestId = action.meta.requestId; })
    .addCase(fetchOrganization.fulfilled, (state, action) => { if (state.detailRequestId !== action.meta.requestId) return; state.selected = action.payload; state.detailStatus = 'succeeded'; state.detailRequestId = null; })
    .addCase(fetchOrganization.rejected, (state, action) => { if (state.detailRequestId !== action.meta.requestId) return; state.detailStatus = 'failed'; state.detailError = action.payload; state.detailRequestId = null; })
    .addCase(searchParentOrganizations.pending, (state, action) => { state.parentStatus = 'loading'; state.parentError = null; state.parentRequestId = action.meta.requestId; })
    .addCase(searchParentOrganizations.fulfilled, (state, action) => { if (state.parentRequestId !== action.meta.requestId) return; state.parentOptions = action.payload.records; state.parentStatus = 'succeeded'; state.parentRequestId = null; })
    .addCase(searchParentOrganizations.rejected, (state, action) => { if (state.parentRequestId !== action.meta.requestId) return; state.parentStatus = 'failed'; state.parentError = action.payload; state.parentRequestId = null; })
    .addCase(createOrganization.pending, (state) => { state.createStatus = 'loading'; state.mutationError = null; })
    .addCase(createOrganization.fulfilled, (state, action) => { state.createStatus = 'succeeded'; state.notification = { message: action.payload.message, severity: 'success' }; })
    .addCase(createOrganization.rejected, (state, action) => { state.createStatus = 'failed'; state.mutationError = action.payload; })
    .addCase(updateOrganization.pending, (state) => { state.updateStatus = 'loading'; state.mutationError = null; })
    .addCase(updateOrganization.fulfilled, (state, action) => { state.updateStatus = 'succeeded'; state.notification = { message: action.payload.message, severity: 'success' }; })
    .addCase(updateOrganization.rejected, (state, action) => { state.updateStatus = 'failed'; state.mutationError = action.payload; })
    .addCase(deleteOrganization.pending, (state, action) => { state.deletingId = action.meta.arg; state.notification = null; })
    .addCase(deleteOrganization.fulfilled, (state, action) => { state.deletingId = null; state.notification = { message: action.payload.message, severity: 'success' }; })
    .addCase(deleteOrganization.rejected, (state, action) => { state.deletingId = null; state.notification = { message: action.payload, severity: 'error' }; })
    .addCase(logout, () => initialState),
});

export const { clearOrganizationEditor, clearOrganizationNotification } = slice.actions;
export default slice.reducer;
