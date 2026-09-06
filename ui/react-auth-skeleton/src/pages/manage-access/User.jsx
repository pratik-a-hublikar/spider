import { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Autocomplete,
  Box,
  Button,
  Card,
  CardContent,
  Checkbox,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
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
  MenuItem,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import {
  clearUserEditor,
  clearUserNotification,
  createUser,
  deleteUser,
  fetchLocations,
  fetchOrganizations,
  fetchUserRoles,
  fetchReportingUsers,
  fetchUser,
  fetchUsers,
  updateUser,
} from '../../features/user/userSlice';
import { ACCESS_TYPES, FILTER_OPERATORS, MODULE_NAMES } from '../../constants/accessControl';
import PasswordField from '../../components/PasswordField';

const emptyForm = {
  email: '',
  password: '',
  fname: '',
  lname: '',
  username: '',
  statusId: '1',
};

const EditIcon = () => (
  <SvgIcon><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25M20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83Z" /></SvgIcon>
);

const DeleteIcon = () => (
  <SvgIcon><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12M8 9h8v10H8V9m7.5-5-1-1h-5l-1 1H5v2h14V4Z" /></SvgIcon>
);

const findModuleByName = (modules, targetName) => {
  const list = Array.isArray(modules) ? modules : [];

  for (const module of list) {
    const name = String(module.name ?? module.moduleName ?? '').trim().toLowerCase();
    if (name === targetName.toLowerCase()) return module;
    const nested = findModuleByName(module.subModules ?? module.childModules ?? [], targetName);
    if (nested) return nested;
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

const createUserListRequest = (page, pageSize, search) => ({
  page,
  pageSize,
  all: false,
  sort: null,
  filterCriteria: [],
  orCriteria: search
    ? ['email', 'username', 'fullName'].map((column) => ({
        column,
        operator: FILTER_OPERATORS.CONTAINS,
        values: [search],
      }))
    : [],
});

const createLookupRequest = (column, search, filterCriteria = []) => ({
  page: 0,
  pageSize: 10,
  all: false,
  sort: null,
  filterCriteria,
  orCriteria: search
    ? [{ column, operator: FILTER_OPERATORS.CONTAINS, values: [search] }]
    : [],
});

const createReportingUserLookupRequest = (search, filterCriteria = []) => ({
  page: 0,
  pageSize: 25,
  all: false,
  sort: null,
  filterCriteria,
  orCriteria: search
    ? ['fullName', 'username', 'email'].map((column) => ({
        column,
        operator: FILTER_OPERATORS.CONTAINS,
        values: [search],
      }))
    : [],
});

const mergeOptions = (selected, options) => {
  const merged = new Map();
  [...selected, ...options].forEach((option) => {
    if (option?.id != null) merged.set(String(option.id), option);
  });
  return Array.from(merged.values());
};

const User = () => {
  const dispatch = useDispatch();
  const { items, status: menuStatus } = useSelector((state) => state.menu);
  const { user: currentUser } = useSelector((state) => state.auth);
  const {
    records,
    totalElements,
    listStatus,
    listError,
    selectedUser,
    detailStatus,
    detailError,
    createStatus,
    updateStatus,
    deletingId,
    mutationError,
    notification,
    organizations,
    organizationStatus,
    organizationError,
    locations,
    locationStatus,
    locationError,
    roles,
    roleStatus,
    roleError,
    reportingUsers,
    reportingUserStatus,
    reportingUserError,
  } = useSelector((state) => state.userManagement);

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [dialogMode, setDialogMode] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [selectedOrganization, setSelectedOrganization] = useState(null);
  const [selectedLocations, setSelectedLocations] = useState([]);
  const [organizationOpen, setOrganizationOpen] = useState(false);
  const [locationOpen, setLocationOpen] = useState(false);
  const [organizationSearch, setOrganizationSearch] = useState('');
  const [locationSearch, setLocationSearch] = useState('');
  const [organizationFilterQuery, setOrganizationFilterQuery] = useState('');
  const [locationFilterQuery, setLocationFilterQuery] = useState('');
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [roleOpen, setRoleOpen] = useState(false);
  const [roleSearch, setRoleSearch] = useState('');
  const [roleFilterQuery, setRoleFilterQuery] = useState('');
  const [selectedReportingUsers, setSelectedReportingUsers] = useState([]);
  const [reportingUserOpen, setReportingUserOpen] = useState(false);
  const [reportingUserSearch, setReportingUserSearch] = useState('');
  const [reportingUserFilterQuery, setReportingUserFilterQuery] = useState('');

  const access = useMemo(() => {
    const userModule = findModuleByName(items, MODULE_NAMES.USERS);
    return new Set([
      ...parseAccess(userModule?.moduleAccessStr),
      ...parseAccess(userModule?.moduleAccess),
    ]);
  }, [items]);

  const canWrite = access.has(ACCESS_TYPES.WRITE);
  const canRead = access.has(ACCESS_TYPES.READ);
  const canUpdate = access.has(ACCESS_TYPES.UPDATE);
  const canDelete = access.has(ACCESS_TYPES.DELETE);
  const listRequest = useMemo(
    () => createUserListRequest(page, pageSize, debouncedSearch),
    [debouncedSearch, page, pageSize],
  );

  const organizationOptions = useMemo(
    () => mergeOptions(selectedOrganization ? [selectedOrganization] : [], organizations),
    [organizations, selectedOrganization],
  );
  const locationOptions = useMemo(
    () => mergeOptions(
      selectedLocations,
      locations.filter((location) => (
        selectedOrganization?.id != null
        && String(location.organizationId) === String(selectedOrganization.id)
      )),
    ),
    [locations, selectedLocations, selectedOrganization?.id],
  );
  const roleOptions = useMemo(
    () => mergeOptions(
      selectedRoles || [],
      roles.filter((role) => (
        selectedOrganization?.id != null
        && String(role.orgId) === String(selectedOrganization.id)
        && (
          currentUser?.superAdmin === true
          || role.canManage === true
          || (currentUser?.roleIdList ?? []).some((id) => String(id) === String(role.id))
        )
      )),
    ),
    [currentUser, roles, selectedOrganization?.id, selectedRoles],
  );
  const reportingUserOptions = useMemo(
    () => mergeOptions(
      selectedReportingUsers,
      [
        ...(dialogMode === 'create' && currentUser ? [currentUser] : []),
        ...reportingUsers,
      ].filter((candidate) => (
        candidate?.id != null
        && String(candidate.uuid) !== String(selectedUser?.uuid ?? '')
        && selectedOrganization?.id != null
        && (
          (dialogMode === 'create' && candidate.uuid === currentUser?.uuid)
          || (candidate.orgIdList ?? []).some((id) => String(id) === String(selectedOrganization.id))
        )
      )),
    ),
    [currentUser, dialogMode, reportingUsers, selectedOrganization?.id, selectedReportingUsers, selectedUser?.uuid],
  );

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedSearch(search.trim()), 400);
    return () => window.clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    if (menuStatus === 'succeeded' && canRead) dispatch(fetchUsers(listRequest));
  }, [canRead, dispatch, listRequest, menuStatus]);

  useEffect(() => {
    const term = organizationFilterQuery.trim();
    if (!dialogMode || !term) return undefined;
    const timer = window.setTimeout(() => {
      dispatch(fetchOrganizations(createLookupRequest('appName', term)));
    }, 350);
    return () => window.clearTimeout(timer);
  }, [dialogMode, dispatch, organizationFilterQuery]);

  useEffect(() => {
    const term = locationFilterQuery.trim();
    if (!dialogMode || !selectedOrganization || !term) return undefined;
    const timer = window.setTimeout(() => {
      const filters = [{ column: 'orgId', operator: FILTER_OPERATORS.EQUAL, values: [String(selectedOrganization.id)] }];
      dispatch(fetchLocations(createLookupRequest('locationName', term, filters)));
    }, 350);
    return () => window.clearTimeout(timer);
  }, [dialogMode, dispatch, locationFilterQuery, selectedOrganization]);

  useEffect(() => {
    const term = roleFilterQuery.trim();
    if (!dialogMode || !selectedOrganization || !term) return undefined;
    const timer = window.setTimeout(() => {
      const filters = [{ column: 'orgId', operator: FILTER_OPERATORS.EQUAL, values: [String(selectedOrganization.id)] }];
      dispatch(fetchUserRoles(createLookupRequest('name', term, filters)));
    }, 350);
    return () => window.clearTimeout(timer);
  }, [dialogMode, dispatch, roleFilterQuery, selectedOrganization]);

  useEffect(() => {
    const term = reportingUserFilterQuery.trim();
    if (!dialogMode || !selectedOrganization || !term) return undefined;
    const timer = window.setTimeout(() => {
      dispatch(fetchReportingUsers(createReportingUserLookupRequest(term)));
    }, 350);
    return () => window.clearTimeout(timer);
  }, [dialogMode, dispatch, reportingUserFilterQuery, selectedOrganization]);

  useEffect(() => {
    if (dialogMode !== 'edit' || detailStatus !== 'succeeded' || !selectedUser) return;

    const organizationId = selectedUser.orgIdList?.[0] ?? null;
    const organization = organizationId == null
      ? null
      : { id: organizationId, name: '' };
    const userLocations = (selectedUser.locationIdList ?? []).map((id) => ({ id, name: '' }));
    const roleIds = (selectedUser.roleIdList && selectedUser.roleIdList.length)
      ? selectedUser.roleIdList
      : (selectedUser.roleIds ?? []);
    const userRoles = (roleIds ?? []).map((id) => ({ id, name: '', orgId: organizationId }));
    const reportingUserIds = selectedUser.reportsToUserIdList ?? [];
    const userManagers = reportingUserIds.map((id) => ({ id, fullName: '', username: '', orgIdList: [organizationId] }));

    setForm({
      email: selectedUser.email ?? '',
      password: '',
      fname: selectedUser.fname ?? '',
      lname: selectedUser.lname ?? '',
      username: selectedUser.username ?? '',
      statusId: selectedUser.statusId == null ? '1' : String(selectedUser.statusId),
    });
    setSelectedOrganization(organization);
    setSelectedLocations(userLocations);
    setSelectedRoles(userRoles);
    setSelectedReportingUsers(userManagers);
    setOrganizationSearch(organization?.name ?? '');
    setLocationSearch('');
    setRoleSearch('');
    setReportingUserSearch('');

    if (organizationId != null) {
      dispatch(fetchOrganizations(createLookupRequest('appName', '', [
        { column: 'id', operator: FILTER_OPERATORS.IN, values: [String(organizationId)] },
      ])));
    }

    const locationIds = selectedUser.locationIdList ?? [];
    if (locationIds.length) {
      const filters = [
        { column: 'id', operator: FILTER_OPERATORS.IN, values: locationIds.map(String) },
        ...(organizationId == null
          ? []
          : [{ column: 'orgId', operator: FILTER_OPERATORS.EQUAL, values: [String(organizationId)] }]),
      ];
      dispatch(fetchLocations(createLookupRequest('locationName', '', filters)));
    }
    if (organizationId != null) {
      const filters = [
        { column: 'orgId', operator: FILTER_OPERATORS.EQUAL, values: [String(organizationId)] },
        ...(roleIds && roleIds.length
          ? [{ column: 'id', operator: FILTER_OPERATORS.IN, values: roleIds.map(String) }]
          : []),
      ];
      dispatch(fetchUserRoles(createLookupRequest('name', '', filters)));
      dispatch(fetchReportingUsers(createReportingUserLookupRequest('', reportingUserIds.length
        ? [{ column: 'id', operator: FILTER_OPERATORS.IN, values: reportingUserIds.map(String) }]
        : [])));
    }
  }, [detailStatus, dialogMode, dispatch, selectedUser]);

  useEffect(() => {
    if (!selectedOrganization) return;
    const resolved = organizations.find(
      (organization) => String(organization.id) === String(selectedOrganization.id),
    );
    if (resolved && resolved.name !== selectedOrganization.name) {
      setSelectedOrganization(resolved);
      setOrganizationSearch(resolved.name ?? '');
    }
  }, [organizations, selectedOrganization]);

  useEffect(() => {
    if (!selectedLocations.length || !locations.length) return;
    const locationMap = new Map(locations.map((location) => [String(location.id), location]));
    let changed = false;
    const resolved = selectedLocations.map((location) => {
      const match = locationMap.get(String(location.id));
      if (match && match.name !== location.name) {
        changed = true;
        return match;
      }
      return location;
    });
    if (changed) setSelectedLocations(resolved);
  }, [locations, selectedLocations]);

  useEffect(() => {
    if (!selectedRoles || !selectedRoles.length) return;
    const roleMap = new Map(roles.map((r) => [String(r.id), r]));
    let changed = false;
    const resolved = selectedRoles.map((role) => {
      const match = roleMap.get(String(role.id));
      if (match && match.name !== role.name) {
        changed = true;
        return match;
      }
      return role;
    });
    if (changed) setSelectedRoles(resolved);
  }, [roles, selectedRoles]);

  useEffect(() => {
    if (!selectedReportingUsers.length || !reportingUsers.length) return;
    const userMap = new Map(reportingUsers.map((user) => [String(user.id), user]));
    let changed = false;
    const resolved = selectedReportingUsers.map((user) => {
      const match = userMap.get(String(user.id));
      if (match && match.username !== user.username) {
        changed = true;
        return match;
      }
      return user;
    });
    if (changed) setSelectedReportingUsers(resolved);
  }, [reportingUsers, selectedReportingUsers]);

  useEffect(() => () => {
    dispatch(clearUserEditor());
    dispatch(clearUserNotification());
  }, [dispatch]);

  const resetEditor = () => {
    setForm(emptyForm);
    setSelectedOrganization(null);
    setSelectedLocations([]);
    setOrganizationOpen(false);
    setLocationOpen(false);
    setOrganizationSearch('');
    setLocationSearch('');
    setOrganizationFilterQuery('');
    setLocationFilterQuery('');
    setSelectedRoles([]);
    setRoleOpen(false);
    setRoleSearch('');
    setRoleFilterQuery('');
    setSelectedReportingUsers([]);
    setReportingUserOpen(false);
    setReportingUserSearch('');
    setReportingUserFilterQuery('');
  };

  const openCreate = () => {
    dispatch(clearUserEditor());
    dispatch(clearUserNotification());
    resetEditor();
    setDialogMode('create');
    dispatch(fetchOrganizations(createLookupRequest('appName', '')));
  };

  const openEdit = (user) => {
    if (!user.canManage) return;
    dispatch(clearUserEditor());
    dispatch(clearUserNotification());
    resetEditor();
    setDialogMode('edit');
    dispatch(fetchUser(user.uuid));
  };

  const isSubmitting = createStatus === 'loading' || updateStatus === 'loading';
  const passwordIsValid = dialogMode === 'edit'
    ? !form.password || form.password.length >= 8
    : form.password.length >= 8;
  const isFormValid = Boolean(
    form.email.trim()
    && /^\S+@\S+\.\S+$/.test(form.email)
    && form.fname.trim()
    && form.username.trim()
    && passwordIsValid
    && ['0', '1'].includes(form.statusId)
    && selectedOrganization
    && selectedRoles.length > 0,
  );

  const closeDialog = () => {
    if (isSubmitting) return;
    setDialogMode(null);
    resetEditor();
    dispatch(clearUserEditor());
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!isFormValid) return;

    const payload = {
      email: form.email.trim(),
      fname: form.fname.trim(),
      lname: form.lname.trim(),
      username: form.username.trim(),
      statusId: Number(form.statusId),
      orgIdList: [selectedOrganization.id],
      locationIdList: selectedLocations.map((location) => location.id),
    };

    if (form.password) payload.password = form.password;
    payload.roleIdList = selectedRoles.map((r) => r.id);
    payload.reportsToUserIdList = selectedReportingUsers.map((user) => user.id);

    const action = dialogMode === 'edit'
      ? updateUser({ uuid: selectedUser.uuid, user: payload })
      : createUser(payload);
    const result = await dispatch(action);
    const succeeded = dialogMode === 'edit'
      ? updateUser.fulfilled.match(result)
      : createUser.fulfilled.match(result);

    if (succeeded) {
      setDialogMode(null);
      resetEditor();
      dispatch(clearUserEditor());
      dispatch(fetchUsers(listRequest));
    }
  };

  const handleDelete = async (user) => {
    if (!window.confirm(`Delete user "${user.username || user.email}"?`)) return;
    const result = await dispatch(deleteUser(user.uuid));
    if (deleteUser.fulfilled.match(result)) dispatch(fetchUsers(listRequest));
  };

  if (menuStatus === 'idle' || menuStatus === 'loading') {
    return <Box sx={{ minHeight: 320, display: 'grid', placeItems: 'center' }}><CircularProgress /></Box>;
  }

  if (!canRead) {
    return <Alert severity="warning">You do not have Read access to User Management.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Card variant="outlined" sx={{ background: 'linear-gradient(135deg, #f5f9ff 0%, #fff 70%)' }}>
        <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
          <Box>
            <Typography variant="h4" fontWeight={700}>User Management</Typography>
            <Typography color="text.secondary">Create and maintain application users.</Typography>
          </Box>
          {canWrite && <Button variant="contained" size="large" onClick={openCreate}>+ Add User</Button>}
        </CardContent>
      </Card>

      {listError && <Alert severity="error">{listError}</Alert>}

      <TextField
        value={search}
        onChange={(event) => {
          setSearch(event.target.value);
          setPage(0);
        }}
        label="Search users"
        placeholder="Search by name, username, or email"
        size="small"
        sx={{ width: { xs: '100%', sm: 420 } }}
      />

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'grey.50' }}>
              <TableCell sx={{ fontWeight: 700 }}>Name</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Username</TableCell>
              <TableCell sx={{ fontWeight: 700 }}>Email</TableCell>
              {(canUpdate || canDelete) && <TableCell align="right" sx={{ fontWeight: 700 }}>Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {listStatus === 'loading' && (
              <TableRow><TableCell colSpan={4} align="center" sx={{ py: 6 }}><CircularProgress size={30} /></TableCell></TableRow>
            )}
            {listStatus === 'succeeded' && records.length === 0 && (
              <TableRow><TableCell colSpan={4} align="center" sx={{ py: 6, color: 'text.secondary' }}>No users found.</TableCell></TableRow>
            )}
            {listStatus !== 'loading' && records.map((user) => (
              <TableRow key={user.uuid} hover>
                <TableCell><Typography fontWeight={600}>{user.fullName || '—'}</Typography></TableCell>
                <TableCell>{user.username}</TableCell>
                <TableCell>{user.email}</TableCell>
                {(canUpdate || canDelete) && (
                  <TableCell align="right">
                    {canUpdate && (
                      <Tooltip title={user.canManage ? 'Edit user' : 'You can only edit users below you in the reporting hierarchy'}>
                        <span><IconButton color="primary" disabled={!user.canManage} onClick={() => openEdit(user)}><EditIcon /></IconButton></span>
                      </Tooltip>
                    )}
                    {canDelete && (
                      <Tooltip title={user.canManage ? 'Delete user' : 'You can only delete users below you in the reporting hierarchy'}>
                        <span>
                          <IconButton color="error" disabled={!user.canManage || deletingId === user.uuid} onClick={() => handleDelete(user)}>
                            <DeleteIcon />
                          </IconButton>
                        </span>
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

      <Dialog open={Boolean(dialogMode)} onClose={closeDialog} fullWidth maxWidth="md">
        <Box component="form" onSubmit={handleSubmit}>
          <DialogTitle>{dialogMode === 'edit' ? 'Edit User' : 'Create User'}</DialogTitle>
          <DialogContent>
            {dialogMode === 'edit' && detailStatus === 'loading' && (
              <Box sx={{ minHeight: 280, display: 'grid', placeItems: 'center' }}><CircularProgress /></Box>
            )}
            {dialogMode === 'edit' && detailStatus === 'failed' && (
              <Alert severity="error" sx={{ mt: 1 }}>{detailError}</Alert>
            )}
            {(dialogMode === 'create' || detailStatus === 'succeeded') && (
              <Stack spacing={2.5} sx={{ pt: 1 }}>
                {mutationError && <Alert severity="error">{mutationError}</Alert>}

                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
                  <TextField label="First Name" value={form.fname} onChange={(event) => setForm((current) => ({ ...current, fname: event.target.value }))} required autoFocus />
                  <TextField label="Last Name" value={form.lname} onChange={(event) => setForm((current) => ({ ...current, lname: event.target.value }))} />
                  <TextField label="Username" value={form.username} onChange={(event) => setForm((current) => ({ ...current, username: event.target.value }))} required />
                  <TextField label="Email" type="email" value={form.email} onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))} required />
                  <PasswordField
                    label={dialogMode === 'edit' ? 'New Password' : 'Password'}
                    value={form.password}
                    onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
                    helperText={dialogMode === 'edit' ? 'Leave blank to keep the existing password.' : 'Minimum 8 characters.'}
                    required={dialogMode === 'create'}
                  />
                  <TextField select label="Status" value={form.statusId} onChange={(event) => setForm((current) => ({ ...current, statusId: event.target.value }))} required>
                    <MenuItem value="1">Active</MenuItem>
                    <MenuItem value="0">Inactive</MenuItem>
                  </TextField>
                </Box>

                <Autocomplete
                  open={organizationOpen}
                  onOpen={() => setOrganizationOpen(true)}
                  onClose={() => setOrganizationOpen(false)}
                  options={organizationOptions}
                  value={selectedOrganization}
                  inputValue={organizationSearch}
                  loading={organizationStatus === 'loading'}
                  filterOptions={(options) => options}
                  isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)}
                  getOptionLabel={(option) => option?.name ?? ''}
                  onInputChange={(_, value, reason) => {
                    setOrganizationSearch(value);
                    setOrganizationFilterQuery(reason === 'input' ? value : '');
                  }}
                  onChange={(_, value) => {
                    setSelectedOrganization(value);
                    setSelectedLocations([]);
                    setLocationSearch('');
                    setLocationFilterQuery('');
                    setSelectedRoles([]);
                    setRoleSearch('');
                    setRoleFilterQuery('');
                    setSelectedReportingUsers([]);
                    setReportingUserSearch('');
                    setReportingUserFilterQuery('');
                    if (value) {
                      const filters = [{ column: 'orgId', operator: FILTER_OPERATORS.EQUAL, values: [String(value.id)] }];
                      dispatch(fetchLocations(createLookupRequest('locationName', '', filters)));
                      dispatch(fetchUserRoles(createLookupRequest('name', '', filters)));
                      dispatch(fetchReportingUsers(createReportingUserLookupRequest('')));
                    }
                  }}
                  renderInput={(params) => (
                    <TextField {...params} label="Organization" required error={organizationStatus === 'failed'} helperText={organizationError} />
                  )}
                />

                <Autocomplete
                    multiple
                    disabled={!selectedOrganization}
                    open={roleOpen}
                    onOpen={() => setRoleOpen(true)}
                    onClose={() => setRoleOpen(false)}
                    options={roleOptions}
                    value={selectedRoles}
                    inputValue={roleSearch}
                    loading={roleStatus === 'loading'}
                    filterOptions={(options) => options}
                    isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)}
                    getOptionLabel={(option) => option?.name ?? ''}
                    onInputChange={(_, value, reason) => {
                      setRoleSearch(value);
                      setRoleFilterQuery(reason === 'input' ? value : '');
                    }}
                    onChange={(_, value) => setSelectedRoles(value)}
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        label="Role"
                        required={selectedRoles.length === 0}
                        placeholder="Search and select roles"
                        error={roleStatus === 'failed'}
                        helperText={roleError || (!selectedOrganization ? 'Select an organization first.' : '')}
                      />
                    )}
                  />

                <Autocomplete
                  multiple
                  disabled={!selectedOrganization}
                  open={reportingUserOpen}
                  onOpen={() => setReportingUserOpen(true)}
                  onClose={() => setReportingUserOpen(false)}
                  options={reportingUserOptions}
                  value={selectedReportingUsers}
                  inputValue={reportingUserSearch}
                  loading={reportingUserStatus === 'loading'}
                  filterOptions={(options) => options}
                  isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)}
                  getOptionLabel={(option) => (
                    option?.fullName
                    || option?.username
                    || option?.email
                    || (option?.id == null ? '' : `User ${option.id}`)
                  )}
                  onInputChange={(_, value, reason) => {
                    setReportingUserSearch(value);
                    setReportingUserFilterQuery(reason === 'input' ? value : '');
                  }}
                  onChange={(_, value) => setSelectedReportingUsers(value)}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Reports to"
                      placeholder="Search and select managers"
                      error={reportingUserStatus === 'failed'}
                      helperText={reportingUserError || (!selectedOrganization ? 'Select an organization first.' : '')}
                    />
                  )}
                />

                <Autocomplete
                  multiple
                  disabled={!selectedOrganization}
                  open={locationOpen}
                  onOpen={() => setLocationOpen(true)}
                  onClose={() => setLocationOpen(false)}
                  options={locationOptions}
                  value={selectedLocations}
                  inputValue={locationSearch}
                  loading={locationStatus === 'loading'}
                  filterOptions={(options) => options}
                  isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)}
                  getOptionLabel={(option) => option?.name ?? ''}
                  onInputChange={(_, value, reason) => {
                    setLocationSearch(value);
                    setLocationFilterQuery(reason === 'input' ? value : '');
                  }}
                  onChange={(_, value) => setSelectedLocations(value)}
                  renderInput={(params) => (
                    <TextField {...params} label="Locations" placeholder="Search and select locations" error={locationStatus === 'failed'} helperText={locationError || (!selectedOrganization ? 'Select an organization first.' : '')} />
                  )}
                />

               
              </Stack>
            )}
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button onClick={closeDialog} disabled={isSubmitting}>Cancel</Button>
            <Button
              type="submit"
              variant="contained"
              disabled={!isFormValid || isSubmitting || (dialogMode === 'edit' && detailStatus !== 'succeeded')}
            >
              {isSubmitting ? <CircularProgress size={22} color="inherit" /> : dialogMode === 'edit' ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      <Snackbar
        open={Boolean(notification)}
        autoHideDuration={6000}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        onClose={(_, reason) => {
          if (reason !== 'clickaway') dispatch(clearUserNotification());
        }}
      >
        <Alert severity={notification?.severity || 'success'} variant="filled" onClose={() => dispatch(clearUserNotification())} sx={{ width: '100%' }}>
          {notification?.message}
        </Alert>
      </Snackbar>
    </Stack>
  );
};

export default User;
