import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { logout } from '../auth/authSlice';
import { roleService } from '../../services/roleService';
import { organizationService } from '../../services/organizationService';

const getErrorMessage = (error) => (
  error?.response?.data?.message
  || error?.response?.data?.error
  || error?.message
  || 'Unable to process the role request.'
);

export const fetchRoles = createAsyncThunk(
  'role/fetchRoles',
  async (request, { rejectWithValue }) => {
    try {
      return await roleService.getRoles(request);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const fetchRole = createAsyncThunk(
  'role/fetchRole',
  async (id, { rejectWithValue }) => {
    try {
      return await roleService.getRole(id);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const createRole = createAsyncThunk(
  'role/createRole',
  async (role, { rejectWithValue }) => {
    try {
      return await roleService.createRole(role);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const updateRole = createAsyncThunk(
  'role/updateRole',
  async (role, { rejectWithValue }) => {
    try {
      return await roleService.updateRole(role);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const deleteRole = createAsyncThunk(
  'role/deleteRole',
  async (id, { rejectWithValue }) => {
    try {
      return await roleService.deleteRole(id);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const searchRoleOrganizations = createAsyncThunk(
  'role/searchOrganizations',
  async (request, { rejectWithValue }) => {
    try {
      return await organizationService.filter(request);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const searchReportingRoles = createAsyncThunk(
  'role/searchReportingRoles',
  async (request, { rejectWithValue }) => {
    try {
      return await roleService.getRoles(request);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const fetchReportingRoleModules = createAsyncThunk(
  'role/fetchReportingRoleModules',
  async (roleIds, { rejectWithValue }) => {
    try {
      return await roleService.getModulesForRoles(roleIds);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const fetchMyAccesses = createAsyncThunk(
  'role/fetchMyAccesses',
  async (_, { rejectWithValue }) => {
    try {
      return await roleService.getMyAccesses();
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const fetchManageableRoleIds = createAsyncThunk(
  'role/fetchManageableRoleIds',
  async (_, { rejectWithValue }) => {
    try {
      return await roleService.getManageableRoleIds();
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const fetchRoleOrganizationUsers = createAsyncThunk(
  'role/fetchOrganizationUsers',
  async ({ organizationUuid, request }, { rejectWithValue }) => {
    try {
      return await roleService.getOrganizationUsers(organizationUuid, request);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const fetchRoleUsers = createAsyncThunk(
  'role/fetchRoleUsers',
  async (roleId, { rejectWithValue }) => {
    try {
      return await roleService.getRoleUsers(roleId);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

const initialState = {
  records: [],
  totalElements: 0,
  listStatus: 'idle',
  listError: null,
  activeListRequestId: null,
  selectedRole: null,
  detailStatus: 'idle',
  detailError: null,
  activeDetailRequestId: null,
  createStatus: 'idle',
  updateStatus: 'idle',
  mutationError: null,
  successMessage: null,
  deletingId: null,
  deleteNotification: null,
  organizations: [],
  organizationStatus: 'idle',
  organizationError: null,
  activeOrganizationRequestId: null,
  reportingRoles: [],
  reportingRoleStatus: 'idle',
  reportingRoleError: null,
  activeReportingRoleRequestId: null,
  reportingRoleModules: [],
  reportingRoleModuleStatus: 'idle',
  reportingRoleModuleError: null,
  activeReportingRoleModuleRequestId: null,
  myAccessModules: [],
  myAccessStatus: 'idle',
  myAccessError: null,
  activeMyAccessRequestId: null,
  manageableRoleIds: [],
  manageableRoleStatus: 'idle',
  manageableRoleError: null,
  organizationUsers: [],
  organizationUserStatus: 'idle',
  organizationUserError: null,
  activeOrganizationUserRequestId: null,
  roleUsers: [],
  roleUserStatus: 'idle',
  roleUserError: null,
};

const roleSlice = createSlice({
  name: 'role',
  initialState,
  reducers: {
    clearRoleEditor: (state) => {
      state.selectedRole = null;
      state.detailStatus = 'idle';
      state.detailError = null;
      state.activeDetailRequestId = null;
      state.createStatus = 'idle';
      state.updateStatus = 'idle';
      state.mutationError = null;
      state.reportingRoles = [];
      state.reportingRoleStatus = 'idle';
      state.reportingRoleError = null;
      state.activeReportingRoleRequestId = null;
      state.reportingRoleModules = [];
      state.reportingRoleModuleStatus = 'idle';
      state.reportingRoleModuleError = null;
      state.activeReportingRoleModuleRequestId = null;
      state.myAccessModules = [];
      state.myAccessStatus = 'idle';
      state.myAccessError = null;
      state.activeMyAccessRequestId = null;
      state.organizationUsers = [];
      state.organizationUserStatus = 'idle';
      state.organizationUserError = null;
      state.activeOrganizationUserRequestId = null;
      state.roleUsers = [];
      state.roleUserStatus = 'idle';
      state.roleUserError = null;
    },
    clearRoleFeedback: (state) => {
      state.mutationError = null;
      state.successMessage = null;
      state.deleteNotification = null;
    },
    clearReportingRoleModules: (state) => {
      state.reportingRoleModules = [];
      state.reportingRoleModuleStatus = 'idle';
      state.reportingRoleModuleError = null;
      state.activeReportingRoleModuleRequestId = null;
    },
    clearMyAccesses: (state) => {
      state.myAccessModules = [];
      state.myAccessStatus = 'idle';
      state.myAccessError = null;
      state.activeMyAccessRequestId = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchRoles.pending, (state, action) => {
        state.listStatus = 'loading';
        state.listError = null;
        state.activeListRequestId = action.meta.requestId;
      })
      .addCase(fetchRoles.fulfilled, (state, action) => {
        if (state.activeListRequestId !== action.meta.requestId) return;
        state.records = action.payload.records;
        state.totalElements = action.payload.totalElements;
        state.listStatus = 'succeeded';
        state.activeListRequestId = null;
      })
      .addCase(fetchRoles.rejected, (state, action) => {
        if (state.activeListRequestId !== action.meta.requestId) return;
        state.listStatus = 'failed';
        state.listError = action.payload || 'Unable to load roles.';
        state.activeListRequestId = null;
      })
      .addCase(fetchRole.pending, (state, action) => {
        state.selectedRole = null;
        state.detailStatus = 'loading';
        state.detailError = null;
        state.mutationError = null;
        state.activeDetailRequestId = action.meta.requestId;
      })
      .addCase(fetchRole.fulfilled, (state, action) => {
        if (state.activeDetailRequestId !== action.meta.requestId) return;
        state.selectedRole = action.payload;
        state.detailStatus = 'succeeded';
        state.activeDetailRequestId = null;
      })
      .addCase(fetchRole.rejected, (state, action) => {
        if (state.activeDetailRequestId !== action.meta.requestId) return;
        state.detailStatus = 'failed';
        state.detailError = action.payload || 'Unable to load the role.';
        state.activeDetailRequestId = null;
      })
      .addCase(createRole.pending, (state) => {
        state.createStatus = 'loading';
        state.mutationError = null;
      })
      .addCase(createRole.fulfilled, (state, action) => {
        state.createStatus = 'succeeded';
        state.successMessage = action.payload.message;
      })
      .addCase(createRole.rejected, (state, action) => {
        state.createStatus = 'failed';
        state.mutationError = action.payload || 'Unable to create the role.';
      })
      .addCase(updateRole.pending, (state) => {
        state.updateStatus = 'loading';
        state.mutationError = null;
      })
      .addCase(updateRole.fulfilled, (state, action) => {
        const role = action.payload.role;
        const index = state.records.findIndex((item) => String(item.id) === String(role.id));
        if (index !== -1) state.records[index] = { ...state.records[index], ...role };
        state.selectedRole = role;
        state.updateStatus = 'succeeded';
        state.successMessage = action.payload.message;
      })
      .addCase(updateRole.rejected, (state, action) => {
        state.updateStatus = 'failed';
        state.mutationError = action.payload || 'Unable to update the role.';
      })
      .addCase(deleteRole.pending, (state, action) => {
        state.deletingId = action.meta.arg;
        state.deleteNotification = null;
      })
      .addCase(deleteRole.fulfilled, (state, action) => {
        state.deletingId = null;
        state.deleteNotification = {
          message: action.payload.message,
          severity: 'success',
        };
      })
      .addCase(deleteRole.rejected, (state, action) => {
        state.deletingId = null;
        state.deleteNotification = {
          message: action.payload || 'Unable to delete the role.',
          severity: 'error',
        };
      })
      .addCase(searchRoleOrganizations.pending, (state, action) => {
        state.organizationStatus = 'loading';
        state.organizationError = null;
        state.activeOrganizationRequestId = action.meta.requestId;
      })
      .addCase(searchRoleOrganizations.fulfilled, (state, action) => {
        if (state.activeOrganizationRequestId !== action.meta.requestId) return;
        state.organizations = action.payload.records;
        state.organizationStatus = 'succeeded';
        state.activeOrganizationRequestId = null;
      })
      .addCase(searchRoleOrganizations.rejected, (state, action) => {
        if (state.activeOrganizationRequestId !== action.meta.requestId) return;
        state.organizationStatus = 'failed';
        state.organizationError = action.payload || 'Unable to load organizations.';
        state.activeOrganizationRequestId = null;
      })
      .addCase(searchReportingRoles.pending, (state, action) => {
        state.reportingRoleStatus = 'loading';
        state.reportingRoleError = null;
        state.activeReportingRoleRequestId = action.meta.requestId;
      })
      .addCase(searchReportingRoles.fulfilled, (state, action) => {
        if (state.activeReportingRoleRequestId !== action.meta.requestId) return;
        state.reportingRoles = action.payload.records;
        state.reportingRoleStatus = 'succeeded';
        state.activeReportingRoleRequestId = null;
      })
      .addCase(searchReportingRoles.rejected, (state, action) => {
        if (state.activeReportingRoleRequestId !== action.meta.requestId) return;
        state.reportingRoleStatus = 'failed';
        state.reportingRoleError = action.payload || 'Unable to load reporting roles.';
        state.activeReportingRoleRequestId = null;
      })
      .addCase(fetchReportingRoleModules.pending, (state, action) => {
        state.reportingRoleModules = [];
        state.reportingRoleModuleStatus = 'loading';
        state.reportingRoleModuleError = null;
        state.activeReportingRoleModuleRequestId = action.meta.requestId;
      })
      .addCase(fetchReportingRoleModules.fulfilled, (state, action) => {
        if (state.activeReportingRoleModuleRequestId !== action.meta.requestId) return;
        state.reportingRoleModules = action.payload;
        state.reportingRoleModuleStatus = 'succeeded';
        state.activeReportingRoleModuleRequestId = null;
      })
      .addCase(fetchReportingRoleModules.rejected, (state, action) => {
        if (state.activeReportingRoleModuleRequestId !== action.meta.requestId) return;
        state.reportingRoleModuleStatus = 'failed';
        state.reportingRoleModuleError = action.payload || 'Unable to load role module accesses.';
        state.activeReportingRoleModuleRequestId = null;
      })
      .addCase(fetchMyAccesses.pending, (state, action) => {
        state.myAccessModules = [];
        state.myAccessStatus = 'loading';
        state.myAccessError = null;
        state.activeMyAccessRequestId = action.meta.requestId;
      })
      .addCase(fetchMyAccesses.fulfilled, (state, action) => {
        if (state.activeMyAccessRequestId !== action.meta.requestId) return;
        state.myAccessModules = action.payload;
        state.myAccessStatus = 'succeeded';
        state.activeMyAccessRequestId = null;
      })
      .addCase(fetchMyAccesses.rejected, (state, action) => {
        if (state.activeMyAccessRequestId !== action.meta.requestId) return;
        state.myAccessModules = [];
        state.myAccessStatus = 'failed';
        state.myAccessError = action.payload || 'Unable to load your module accesses.';
        state.activeMyAccessRequestId = null;
      })
      .addCase(fetchManageableRoleIds.pending, (state) => {
        state.manageableRoleStatus = 'loading';
        state.manageableRoleError = null;
      })
      .addCase(fetchManageableRoleIds.fulfilled, (state, action) => {
        state.manageableRoleIds = action.payload;
        state.manageableRoleStatus = 'succeeded';
      })
      .addCase(fetchManageableRoleIds.rejected, (state, action) => {
        state.manageableRoleIds = [];
        state.manageableRoleStatus = 'failed';
        state.manageableRoleError = action.payload || 'Unable to load manageable roles.';
      })
      .addCase(fetchRoleOrganizationUsers.pending, (state, action) => {
        state.organizationUsers = [];
        state.organizationUserStatus = 'loading';
        state.organizationUserError = null;
        state.activeOrganizationUserRequestId = action.meta.requestId;
      })
      .addCase(fetchRoleOrganizationUsers.fulfilled, (state, action) => {
        if (state.activeOrganizationUserRequestId !== action.meta.requestId) return;
        state.organizationUsers = action.payload;
        state.organizationUserStatus = 'succeeded';
        state.activeOrganizationUserRequestId = null;
      })
      .addCase(fetchRoleOrganizationUsers.rejected, (state, action) => {
        if (state.activeOrganizationUserRequestId !== action.meta.requestId) return;
        state.organizationUsers = [];
        state.organizationUserStatus = 'failed';
        state.organizationUserError = action.payload || 'Unable to load organization users.';
        state.activeOrganizationUserRequestId = null;
      })
      .addCase(fetchRoleUsers.pending, (state) => {
        state.roleUsers = [];
        state.roleUserStatus = 'loading';
        state.roleUserError = null;
      })
      .addCase(fetchRoleUsers.fulfilled, (state, action) => {
        state.roleUsers = action.payload;
        state.roleUserStatus = 'succeeded';
      })
      .addCase(fetchRoleUsers.rejected, (state, action) => {
        state.roleUsers = [];
        state.roleUserStatus = 'failed';
        state.roleUserError = action.payload || 'Unable to load role users.';
      })
      .addCase(logout, () => initialState);
  },
});

export const {
  clearMyAccesses,
  clearRoleEditor,
  clearRoleFeedback,
  clearReportingRoleModules,
} = roleSlice.actions;
export default roleSlice.reducer;
