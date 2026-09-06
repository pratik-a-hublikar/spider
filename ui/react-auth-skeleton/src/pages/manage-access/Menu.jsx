import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Snackbar,
  Stack,
  SvgIcon,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import {
  clearManagedMenuEdit,
  clearMenuDeleteNotification,
  deleteManagedMenu,
  fetchManagedMenu,
  fetchManagedMenus,
  searchParentMenus,
  updateManagedMenu,
} from '../../features/menu/menuSlice';
import { ACCESS_TYPES, FILTER_OPERATORS, MODULE_NAMES } from '../../constants/accessControl';

const emptyEditForm = {
  name: '',
  moduleUiName: '',
  parentId: null,
  moduleAccess: [],
};

const EditIcon = () => (
  <SvgIcon><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25M20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83Z" /></SvgIcon>
);

const DeleteIcon = () => (
  <SvgIcon><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12M8 9h8v10H8V9m7.5-5-1-1h-5l-1 1H5v2h14V4Z" /></SvgIcon>
);

const findModuleByName = (modules, targetName) => {
  const moduleList = Array.isArray(modules) ? modules : modules?.subModules ?? [];

  for (const module of moduleList) {
    const moduleName = String(module.name ?? module.moduleName ?? '').trim().toLowerCase();
    if (moduleName === targetName.toLowerCase()) return module;

    const nestedModule = findModuleByName(module.subModules, targetName);
    if (nestedModule) return nestedModule;
  }

  return null;
};

const parseAccess = (value) => {
  if (Array.isArray(value)) {
    return value
      .map((access) => access?.name ?? access)
      .filter((access) => access != null)
      .map((access) => String(access).toLowerCase());
  }
  return String(value ?? '')
    .replace(/[\[\]"']/g, '')
    .split(/[,|;\s]+/)
    .filter(Boolean)
    .map((access) => access.toLowerCase());
};

const getModuleLabel = (module) => (
  module?.moduleUiName || module?.moduleName || module?.name || ''
);

const createMenuListRequest = (page, pageSize, search) => ({
  page,
  pageSize,
  all: false,
  sort: null,
  filterCriteria: [],
  orCriteria: search
    ? [
        { column: 'moduleUiName', operator: FILTER_OPERATORS.CONTAINS, values: [search] },
        { column: 'url', operator: FILTER_OPERATORS.CONTAINS, values: [search] },
      ]
    : [],
});

const Menu = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const {
    items,
    status,
    records,
    totalElements,
    listStatus,
    listError,
    deleteNotification,
    deletingId,
    updatingId,
    editRecord,
    editStatus,
    editError,
    parentOptions,
    parentSearchStatus,
    parentSearchError,
  } = useSelector((state) => state.menu);

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [editingMenu, setEditingMenu] = useState(null);
  const [editForm, setEditForm] = useState(emptyEditForm);
  const [selectedParent, setSelectedParent] = useState(null);
  const [parentSelectOpen, setParentSelectOpen] = useState(false);
  const [parentSearch, setParentSearch] = useState('');
  const [parentFilterQuery, setParentFilterQuery] = useState('');

  const access = useMemo(() => {
    const menuModule = findModuleByName(items, MODULE_NAMES.MENUS);
    return new Set([
      ...parseAccess(menuModule?.moduleAccessStr),
      ...parseAccess(menuModule?.moduleAccess),
    ]);
  }, [items]);

  const canWrite = access.has(ACCESS_TYPES.WRITE);
  const canRead = access.has(ACCESS_TYPES.READ);
  const canUpdate = access.has(ACCESS_TYPES.UPDATE);
  const canDelete = access.has(ACCESS_TYPES.DELETE);
  const listRequest = useMemo(
    () => createMenuListRequest(page, pageSize, debouncedSearch),
    [debouncedSearch, page, pageSize],
  );

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedSearch(search.trim()), 400);
    return () => window.clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    if (status !== 'succeeded' || !canRead) return;
    dispatch(fetchManagedMenus(listRequest));
  }, [canRead, dispatch, listRequest, status]);

  useEffect(() => {
    if (!editingMenu || editStatus !== 'succeeded' || !editRecord) return;
    if (String(editRecord.module.id) !== String(editingMenu.id)) return;

    const module = editRecord.module;
    const parent = editRecord.parentModules.find(
      (item) => String(item.id) === String(module.parentId),
    ) ?? null;

    setEditForm({
      name: module.name,
      moduleUiName: module.moduleUiName || editingMenu.moduleUiName || '',
      parentId: module.parentId,
      moduleAccess: module.moduleAccess,
    });
    setSelectedParent(parent);
    setParentSearch(getModuleLabel(parent));
  }, [editRecord, editStatus, editingMenu]);

  useEffect(() => {
    const term = parentFilterQuery.trim();
    if (!editingMenu || editStatus !== 'succeeded' || !term) return undefined;

    const timer = window.setTimeout(() => {
      const orCriteria = [
        { column: 'moduleName', operator: FILTER_OPERATORS.CONTAINS, values: [term] },
        { column: 'moduleUiName', operator: FILTER_OPERATORS.CONTAINS, values: [term] },
      ];

      dispatch(searchParentMenus({
        page: 0,
        pageSize: 20,
        all: false,
        sort: null,
        filterCriteria: [
          { column: 'id', operator: FILTER_OPERATORS.NOT_EQUAL, values: [String(editingMenu.id)] },
        ],
        orCriteria,
      }));
    }, 350);

    return () => window.clearTimeout(timer);
  }, [dispatch, editStatus, editingMenu, parentFilterQuery]);

  const parentChoices = useMemo(() => {
    const choices = selectedParent ? [selectedParent, ...parentOptions] : parentOptions;
    const uniqueChoices = new Map();

    choices.forEach((option) => {
      if (option?.id == null || String(option.id) === String(editingMenu?.id)) return;
      uniqueChoices.set(String(option.id), option);
    });

    return Array.from(uniqueChoices.values());
  }, [editingMenu, parentOptions, selectedParent]);

  const accessRoles = useMemo(() => {
    const roles = new Map();

    editForm.moduleAccess.forEach((moduleAccess) => {
      moduleAccess.roles?.forEach((role) => {
        if (role?.id != null) roles.set(String(role.id), role);
      });
      moduleAccess.roleIds?.forEach((roleId) => {
        const key = String(roleId);
        if (!roles.has(key)) roles.set(key, { id: roleId, name: '' });
      });
    });

    return Array.from(roles.values());
  }, [editForm.moduleAccess]);

  const handleSearchChange = (event) => {
    setSearch(event.target.value);
    setPage(0);
  };

  const handleDelete = (menu) => {
    if (window.confirm(`Delete menu "${menu.moduleUiName}"?`)) {
      dispatch(deleteManagedMenu(menu.id));
    }
  };

  const handleOpenEdit = (menu) => {
    setEditingMenu(menu);
    setEditForm(emptyEditForm);
    setSelectedParent(null);
    setParentSelectOpen(false);
    setParentSearch('');
    setParentFilterQuery('');
    dispatch(clearManagedMenuEdit());
    dispatch(fetchManagedMenu(menu.id));
    dispatch(searchParentMenus({
      page: 0,
      pageSize: 20,
      all: false,
      sort: null,
      filterCriteria: [
        { column: 'id', operator: FILTER_OPERATORS.NOT_EQUAL, values: [String(menu.id)] },
      ],
      orCriteria: [],
    }));
  };

  const handleCloseEdit = () => {
    if (updatingId) return;
    setEditingMenu(null);
    dispatch(clearManagedMenuEdit());
  };

  const handleAccessToggle = (accessKey, roleId) => {
    setEditForm((current) => ({
      ...current,
      moduleAccess: current.moduleAccess.map((moduleAccess) => {
        const key = moduleAccess.id ?? moduleAccess.name;
        if (String(key) !== String(accessKey)) return moduleAccess;

        const hasRole = moduleAccess.roleIds.some((id) => String(id) === String(roleId));
        return {
          ...moduleAccess,
          roleIds: hasRole
            ? moduleAccess.roleIds.filter((id) => String(id) !== String(roleId))
            : [...moduleAccess.roleIds, roleId],
        };
      }),
    }));
  };

  const handleUpdate = async () => {
    if (!editingMenu || !editRecord || !canUpdate || !editForm.name.trim() || !editForm.moduleUiName.trim()) return;

    const result = await dispatch(updateManagedMenu({
      id: editingMenu.id,
      changes: {
        id: editingMenu.id,
        name: editForm.name.trim(),
        moduleUiName: editForm.moduleUiName.trim(),
        parentId: editForm.parentId,
        moduleAccess: editForm.moduleAccess.map((moduleAccess) => ({
          id: moduleAccess.id,
          name: moduleAccess.name,
          moduleId: editRecord.module.id,
          roleIds: [...moduleAccess.roleIds],
        })),
      },
    }));

    if (updateManagedMenu.fulfilled.match(result)) {
      setEditingMenu(null);
      dispatch(clearManagedMenuEdit());
      dispatch(fetchManagedMenus(listRequest));
    }
  };

  if (status === 'loading' || status === 'idle') {
    return <Box sx={{ display: 'grid', placeItems: 'center', minHeight: 320 }}><CircularProgress /></Box>;
  }

  if (!canRead) {
    return <Alert severity="warning">You do not have Read access to Menu Management.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Card variant="outlined" sx={{ background: 'linear-gradient(135deg, #f5f9ff 0%, #fff 70%)' }}>
        <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
          <Box>
            <Typography variant="h4" fontWeight={700}>Menu Management</Typography>
            <Typography color="text.secondary">Create and maintain application navigation menus.</Typography>
          </Box>
          {canWrite && <Button variant="contained" size="large" onClick={() => navigate('/manage-access/menu/create')}>+ Add Menu</Button>}
        </CardContent>
      </Card>

      {listError && !editingMenu && <Alert severity="error">{listError}</Alert>}

      <TextField
        value={search}
        onChange={handleSearchChange}
        label="Search menus"
        placeholder="Search by menu name or URL"
        size="small"
        sx={{ width: { xs: '100%', sm: 380 } }}
      />

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'grey.50' }}>
              <TableCell sx={{ fontWeight: 700 }}>Menu name</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>URL</TableCell>
              {(canUpdate || canDelete) && <TableCell align="right" sx={{ fontWeight: 700 }}>Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {listStatus === 'loading' && (
              <TableRow><TableCell colSpan={3} align="center" sx={{ py: 6 }}><CircularProgress size={30} /></TableCell></TableRow>
            )}
            {listStatus === 'succeeded' && records.length === 0 && (
              <TableRow><TableCell colSpan={3} align="center" sx={{ py: 6, color: 'text.secondary' }}>No menus found.</TableCell></TableRow>
            )}
            {records.map((menu) => (
              <TableRow key={menu.id ?? `${menu.moduleName}-${menu.url}`} hover>
                <TableCell><Typography fontWeight={600}>{menu.moduleUiName}</Typography></TableCell>
                <TableCell>{menu.url ? <Chip label={menu.url} size="small" variant="outlined" /> : '—'}</TableCell>
                {(canUpdate || canDelete) && (
                  <TableCell align="right">
                    {canUpdate && (
                      <Tooltip title="Edit module">
                        <IconButton color="primary" onClick={() => handleOpenEdit(menu)}><EditIcon /></IconButton>
                      </Tooltip>
                    )}
                    {canDelete && (
                      <Tooltip title="Delete module">
                        <span><IconButton color="error" disabled={deletingId === menu.id} onClick={() => handleDelete(menu)}><DeleteIcon /></IconButton></span>
                      </Tooltip>
                    )}
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={totalElements}
          page={page}
          rowsPerPage={pageSize}
          rowsPerPageOptions={[5, 10, 25, 50]}
          onPageChange={(_, nextPage) => setPage(nextPage)}
          onRowsPerPageChange={(event) => {
            setPageSize(Number(event.target.value));
            setPage(0);
          }}
        />
      </TableContainer>

      <Dialog open={Boolean(editingMenu)} onClose={handleCloseEdit} fullWidth maxWidth="lg">
        <DialogTitle>Edit module</DialogTitle>
        <DialogContent>
          {editStatus === 'loading' && (
            <Box sx={{ minHeight: 280, display: 'grid', placeItems: 'center' }}>
              <CircularProgress />
            </Box>
          )}

          {editStatus === 'failed' && (
            <Alert severity="error" sx={{ mt: 1 }}>{editError}</Alert>
          )}

          {editStatus === 'succeeded' && editRecord && (
            <Stack spacing={2.5} sx={{ pt: 1 }}>
              {listError && <Alert severity="error">{listError}</Alert>}

              <TextField
                label="Module Name"
                value={editForm.name}
                onChange={(event) => setEditForm((current) => ({ ...current, name: event.target.value }))}
                required
                autoFocus
                fullWidth
              />
              <TextField
                label="Module UI Name"
                value={editForm.moduleUiName}
                onChange={(event) => setEditForm((current) => ({ ...current, moduleUiName: event.target.value }))}
                required
                fullWidth
              />
              <TextField
                label="URL"
                value={editRecord.module.url || editingMenu?.url || ''}
                slotProps={{ input: { readOnly: true } }}
                fullWidth
              />

              <Autocomplete
                open={parentSelectOpen}
                onOpen={() => setParentSelectOpen(true)}
                onClose={() => setParentSelectOpen(false)}
                options={parentChoices}
                value={selectedParent}
                inputValue={parentSearch}
                loading={parentSearchStatus === 'loading'}
                filterOptions={(options) => options}
                isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)}
                getOptionLabel={getModuleLabel}
                onInputChange={(_, value, reason) => {
                  setParentSearch(value);
                  setParentFilterQuery(reason === 'input' ? value : '');
                }}
                onChange={(_, value) => {
                  setSelectedParent(value);
                  setEditForm((current) => ({ ...current, parentId: value?.id ?? null }));
                }}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Parent Module"
                    placeholder="Search parent modules"
                    error={parentSearchStatus === 'failed'}
                    helperText={parentSearchError}
                  />
                )}
              />

              <Box>
                <Typography variant="subtitle1" fontWeight={700} sx={{ mb: 1 }}>Role access</Typography>

                {editForm.moduleAccess.length === 0 && (
                  <Alert severity="info">No module access options were returned by the API.</Alert>
                )}

                {editForm.moduleAccess.length > 0 && accessRoles.length === 0 && (
                  <Alert severity="warning">
                    The API returned access names without role details, so role access cannot be edited.
                  </Alert>
                )}

                {editForm.moduleAccess.length > 0 && accessRoles.length > 0 && (
                  <TableContainer component={Paper} variant="outlined">
                    <Table size="small" aria-label="Role module access">
                      <TableHead>
                        <TableRow sx={{ bgcolor: 'grey.50' }}>
                          <TableCell sx={{ fontWeight: 700 }}>Role</TableCell>
                          {editForm.moduleAccess.map((moduleAccess) => (
                            <TableCell key={moduleAccess.id ?? moduleAccess.name} align="center" sx={{ fontWeight: 700 }}>
                              {moduleAccess.name}
                            </TableCell>
                          ))}
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {accessRoles.map((role) => (
                          <TableRow key={role.id} hover>
                            <TableCell sx={{ fontWeight: 600 }}>{role.name}</TableCell>
                            {editForm.moduleAccess.map((moduleAccess) => {
                              const accessKey = moduleAccess.id ?? moduleAccess.name;
                              const checked = moduleAccess.roleIds.some(
                                (roleId) => String(roleId) === String(role.id),
                              );

                              return (
                                <TableCell key={accessKey} align="center">
                                  <Checkbox
                                    checked={checked}
                                    onChange={() => handleAccessToggle(accessKey, role.id)}
                                    disabled={Boolean(updatingId)}
                                    inputProps={{ 'aria-label': `${role.name} ${moduleAccess.name} access` }}
                                  />
                                </TableCell>
                              );
                            })}
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
              </Box>
            </Stack>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3 }}>
          <Button onClick={handleCloseEdit} disabled={Boolean(updatingId)}>Cancel</Button>
          {canUpdate && (
            <Button
              variant="contained"
              onClick={handleUpdate}
              disabled={
                Boolean(updatingId)
                || editStatus !== 'succeeded'
                || !editForm.name.trim()
                || !editForm.moduleUiName.trim()
              }
            >
              {updatingId ? <CircularProgress size={22} color="inherit" /> : 'Save'}
            </Button>
          )}
        </DialogActions>
      </Dialog>

      <Snackbar
        open={Boolean(deleteNotification)}
        autoHideDuration={6000}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        onClose={(_, reason) => {
          if (reason !== 'clickaway') dispatch(clearMenuDeleteNotification());
        }}
      >
        <Alert
          severity={deleteNotification?.severity || 'error'}
          variant="filled"
          onClose={() => dispatch(clearMenuDeleteNotification())}
          sx={{ width: '100%' }}
        >
          {deleteNotification?.message}
        </Alert>
      </Snackbar>
    </Stack>
  );
};

export default Menu;
