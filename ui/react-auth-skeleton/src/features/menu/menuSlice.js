import { createAsyncThunk, createSlice } from '@reduxjs/toolkit';
import { logout } from '../auth/authSlice';
import { menuService } from '../../services/menuService';

const getErrorMessage = (error) =>
  error?.response?.data?.message || error?.response?.data?.error || error?.message || 'Unable to load the menu.';

export const fetchMenu = createAsyncThunk(
  'menu/fetchMenu',
  async (_, { rejectWithValue }) => {
    try {
      return await menuService.getMenu();
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
  {
    condition: (_, { getState }) => getState().menu.status === 'idle',
  },
);

export const fetchManagedMenus = createAsyncThunk(
  'menu/fetchManagedMenus',
  async (request, { rejectWithValue }) => {
    try {
      return await menuService.getManagedMenus(request);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
  {
    condition: (request, { getState }) => {
      const menu = getState().menu;
      return !(menu.listStatus === 'loading' && menu.listRequestKey === JSON.stringify(request));
    },
  },
);

export const fetchManagedMenu = createAsyncThunk(
  'menu/fetchManagedMenu',
  async (id, { rejectWithValue }) => {
    try {
      return await menuService.getManagedMenu(id);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const searchParentMenus = createAsyncThunk(
  'menu/searchParentMenus',
  async (request, { rejectWithValue }) => {
    try {
      return await menuService.searchParentMenus(request);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const deleteManagedMenu = createAsyncThunk(
  'menu/deleteManagedMenu',
  async (id, { rejectWithValue }) => {
    try {
      return await menuService.deleteMenu(id);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

export const updateManagedMenu = createAsyncThunk(
  'menu/updateManagedMenu',
  async ({ id, changes }, { rejectWithValue }) => {
    try {
      return await menuService.updateMenu(id, changes);
    } catch (error) {
      return rejectWithValue(getErrorMessage(error));
    }
  },
);

const initialState = {
  items: [],
  status: 'idle',
  error: null,
  records: [],
  totalElements: 0,
  listStatus: 'idle',
  listError: null,
  deleteNotification: null,
  deletingId: null,
  updatingId: null,
  listRequestKey: null,
  activeListRequestId: null,
  editRecord: null,
  editStatus: 'idle',
  editError: null,
  activeEditRequestId: null,
  parentOptions: [],
  parentSearchStatus: 'idle',
  parentSearchError: null,
  activeParentSearchRequestId: null,
};

const menuSlice = createSlice({
  name: 'menu',
  initialState,
  reducers: {
    resetMenu: () => initialState,
    clearMenuDeleteNotification: (state) => {
      state.deleteNotification = null;
    },
    clearManagedMenuEdit: (state) => {
      state.editRecord = null;
      state.editStatus = 'idle';
      state.editError = null;
      state.activeEditRequestId = null;
      state.parentOptions = [];
      state.parentSearchStatus = 'idle';
      state.parentSearchError = null;
      state.activeParentSearchRequestId = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMenu.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchMenu.fulfilled, (state, action) => {
        state.items = action.payload;
        state.status = 'succeeded';
        state.error = null;
      })
      .addCase(fetchMenu.rejected, (state, action) => {
        state.items = [];
        state.status = 'failed';
        state.error = action.payload || 'Unable to load the menu.';
      })
      .addCase(fetchManagedMenus.pending, (state, action) => {
        state.listStatus = 'loading';
        state.listError = null;
        state.listRequestKey = JSON.stringify(action.meta.arg);
        state.activeListRequestId = action.meta.requestId;
      })
      .addCase(fetchManagedMenus.fulfilled, (state, action) => {
        if (state.activeListRequestId !== action.meta.requestId) return;
        state.records = action.payload.records;
        state.totalElements = action.payload.totalElements;
        state.listStatus = 'succeeded';
        state.activeListRequestId = null;
      })
      .addCase(fetchManagedMenus.rejected, (state, action) => {
        if (state.activeListRequestId !== action.meta.requestId) return;
        state.listStatus = 'failed';
        state.listError = action.payload || 'Unable to load menus.';
        state.activeListRequestId = null;
      })
      .addCase(fetchManagedMenu.pending, (state, action) => {
        state.editRecord = null;
        state.editStatus = 'loading';
        state.editError = null;
        state.activeEditRequestId = action.meta.requestId;
      })
      .addCase(fetchManagedMenu.fulfilled, (state, action) => {
        if (state.activeEditRequestId !== action.meta.requestId) return;
        state.editRecord = action.payload;
        state.editStatus = 'succeeded';
        state.activeEditRequestId = null;
      })
      .addCase(fetchManagedMenu.rejected, (state, action) => {
        if (state.activeEditRequestId !== action.meta.requestId) return;
        state.editStatus = 'failed';
        state.editError = action.payload || 'Unable to load the module.';
        state.activeEditRequestId = null;
      })
      .addCase(searchParentMenus.pending, (state, action) => {
        state.parentSearchStatus = 'loading';
        state.parentSearchError = null;
        state.activeParentSearchRequestId = action.meta.requestId;
      })
      .addCase(searchParentMenus.fulfilled, (state, action) => {
        if (state.activeParentSearchRequestId !== action.meta.requestId) return;
        state.parentOptions = action.payload.records;
        state.parentSearchStatus = 'succeeded';
        state.activeParentSearchRequestId = null;
      })
      .addCase(searchParentMenus.rejected, (state, action) => {
        if (state.activeParentSearchRequestId !== action.meta.requestId) return;
        state.parentSearchStatus = 'failed';
        state.parentSearchError = action.payload || 'Unable to search parent modules.';
        state.activeParentSearchRequestId = null;
      })
      .addCase(deleteManagedMenu.pending, (state, action) => {
        state.deletingId = action.meta.arg;
        state.deleteNotification = null;
      })
      .addCase(deleteManagedMenu.fulfilled, (state, action) => {
        state.records = state.records.filter((item) => item.id !== action.payload.id);
        state.totalElements = Math.max(0, state.totalElements - 1);
        state.deletingId = null;
        state.deleteNotification = {
          message: action.payload.message,
          severity: 'success',
        };
      })
      .addCase(deleteManagedMenu.rejected, (state, action) => {
        state.deletingId = null;
        state.deleteNotification = {
          message: action.payload || 'Unable to delete the menu.',
          severity: 'error',
        };
      })
      .addCase(updateManagedMenu.pending, (state, action) => {
        state.updatingId = action.meta.arg.id;
        state.listError = null;
      })
      .addCase(updateManagedMenu.fulfilled, (state, action) => {
        const index = state.records.findIndex((item) => item.id === action.payload.id);
        if (index !== -1) {
          state.records[index] = {
            ...state.records[index],
            ...action.payload,
            moduleName: action.payload.name ?? action.payload.moduleName ?? state.records[index].moduleName,
          };
        }
        state.updatingId = null;
      })
      .addCase(updateManagedMenu.rejected, (state, action) => {
        state.updatingId = null;
        state.listError = action.payload || 'Unable to update the menu.';
      })
      .addCase(logout, () => initialState);
  },
});

export const { clearManagedMenuEdit, clearMenuDeleteNotification, resetMenu } = menuSlice.actions;
export default menuSlice.reducer;
