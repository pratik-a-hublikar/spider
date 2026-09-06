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
  FormControlLabel,
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
import {
  clearMyAccesses,
  clearRoleEditor,
  clearRoleFeedback,
  clearReportingRoleModules,
  createRole,
  deleteRole,
  fetchRole,
  fetchManageableRoleIds,
  fetchMyAccesses,
  fetchRoleOrganizationUsers,
  fetchRoleUsers,
  fetchReportingRoleModules,
  fetchRoles,
  searchRoleOrganizations,
  searchReportingRoles,
  updateRole,
} from '../../features/role/roleSlice';
import { ACCESS_TYPES, FILTER_OPERATORS, MODULE_NAMES } from '../../constants/accessControl';

const EditIcon = () => (
  <SvgIcon><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25M20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83Z" /></SvgIcon>
);

const DeleteIcon = () => (
  <SvgIcon><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12M8 9h8v10H8V9m7.5-5-1-1h-5l-1 1H5v2h14V4Z" /></SvgIcon>
);

const DEFAULT_MODULE_ACCESSES = ['Read', 'Write', 'Update', 'Delete', 'Download'];
const normalizeAccessName = (value) => {
  const text = String(value ?? '').trim();
  return DEFAULT_MODULE_ACCESSES.find((access) => access.toLowerCase() === text.toLowerCase()) || text;
};

const uniqueById = (values) => {
  const unique = new Map();
  (Array.isArray(values) ? values : []).forEach((value) => {
    if (value?.id != null) unique.set(String(value.id), value);
  });
  return Array.from(unique.values());
};

const findModuleByName = (modules, targetNames) => {
  const names = targetNames.map((name) => name.toLowerCase());
  const moduleList = Array.isArray(modules) ? modules : [];

  for (const module of moduleList) {
    const moduleName = String(module.name ?? module.moduleName ?? '').trim().toLowerCase();
    if (names.includes(moduleName)) return module;

    const nestedModule = findModuleByName(
      module.subModules ?? module.childModules ?? [],
      targetNames,
    );
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

const createListRequest = (page, pageSize, search) => ({
  page,
  pageSize,
  all: false,
  sort: null,
  filterCriteria: [],
  orCriteria: search
    ? [{ column: 'name', operator: FILTER_OPERATORS.CONTAINS, values: [search] }]
    : [],
});

const createOrganizationRequest = (search) => ({
  page: 0,
  pageSize: 10,
  all: false,
  sort: null,
  filterCriteria: [],
  orCriteria: search
    ? [{ column: 'appName', operator: FILTER_OPERATORS.CONTAINS, values: [search] }]
    : [],
});

const createReportingRoleRequest = (organizationId, search, editingRoleId = null) => ({
  page: 0,
  pageSize: 10,
  all: false,
  sort: null,
  filterCriteria: [
    { column: 'orgId', operator: FILTER_OPERATORS.EQUAL, values: [String(organizationId)] },
    ...(editingRoleId == null
      ? []
      : [{ column: 'id', operator: FILTER_OPERATORS.NOT_EQUAL, values: [String(editingRoleId)] }]),
  ],
  orCriteria: search
    ? [{ column: 'name', operator: FILTER_OPERATORS.CONTAINS, values: [search] }]
    : [],
});

const createOrganizationUserRequest = (search) => ({
  page: 0,
  pageSize: 5,
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

const Role = () => {
  const dispatch = useDispatch();
  const { items, status: menuStatus } = useSelector((state) => state.menu);
  const { user } = useSelector((state) => state.auth);
  const {
    records,
    totalElements,
    listStatus,
    listError,
    selectedRole,
    detailStatus,
    detailError,
    createStatus,
    updateStatus,
    mutationError,
    successMessage,
    organizations,
    organizationStatus,
    organizationError,
    deletingId,
    deleteNotification,
    reportingRoles,
    reportingRoleStatus,
    reportingRoleError,
    reportingRoleModules,
    reportingRoleModuleStatus,
    reportingRoleModuleError,
    myAccessModules,
    myAccessStatus,
    myAccessError,
    manageableRoleIds,
    manageableRoleError,
    organizationUsers,
    organizationUserStatus,
    organizationUserError,
    roleUsers,
    roleUserStatus,
    roleUserError,
  } = useSelector((state) => state.role);

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [dialogMode, setDialogMode] = useState(null);
  const [form, setForm] = useState({ name: '' });
  const [selectedOrganization, setSelectedOrganization] = useState(null);
  const [organizationOpen, setOrganizationOpen] = useState(false);
  const [organizationSearch, setOrganizationSearch] = useState('');
  const [organizationFilterQuery, setOrganizationFilterQuery] = useState('');
  const [selectedReportingRoles, setSelectedReportingRoles] = useState([]);
  const [reportingRoleOpen, setReportingRoleOpen] = useState(false);
  const [reportingRoleSearch, setReportingRoleSearch] = useState('');
  const [reportingRoleFilterQuery, setReportingRoleFilterQuery] = useState('');
  const [moduleAccessRows, setModuleAccessRows] = useState([]);
  const [showMyAccesses, setShowMyAccesses] = useState(false);
  const [suppressParentAccessReload, setSuppressParentAccessReload] = useState(false);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [wholeCompany, setWholeCompany] = useState(false);
  const [showUsers, setShowUsers] = useState(false);
  const [membershipInitializedRoleId, setMembershipInitializedRoleId] = useState(null);
  const [hierarchyError, setHierarchyError] = useState(null);
  const [userSearch, setUserSearch] = useState('');
  const [userFilterQuery, setUserFilterQuery] = useState('');
  const [viewUsersRole, setViewUsersRole] = useState(null);

  const access = useMemo(() => {
    const roleModule = findModuleByName(items, [MODULE_NAMES.ROLES, 'Role']);
    return new Set([
      ...parseAccess(roleModule?.moduleAccessStr),
      ...parseAccess(roleModule?.moduleAccess),
    ]);
  }, [items]);

  const canCreate = access.has(ACCESS_TYPES.CREATE) || access.has(ACCESS_TYPES.WRITE);
  const canRead = access.has(ACCESS_TYPES.READ);
  const canUpdate = access.has(ACCESS_TYPES.UPDATE);
  const canDelete = access.has(ACCESS_TYPES.DELETE);
  const isSuperAdmin = user?.superAdmin === true;
  const manageableRoleIdSet = useMemo(
    () => new Set((manageableRoleIds ?? []).map(String)),
    [manageableRoleIds],
  );
  const canManageRole = (role) => role?.canManage === true
    || manageableRoleIdSet.has(String(role?.uuid));
  const listRequest = useMemo(
    () => createListRequest(page, pageSize, debouncedSearch),
    [debouncedSearch, page, pageSize],
  );
  const organizationOptions = useMemo(() => {
    const merged = new Map();
    [...(selectedOrganization ? [selectedOrganization] : []), ...organizations].forEach((organization) => {
      if (organization?.id != null) merged.set(String(organization.id), organization);
    });
    return Array.from(merged.values());
  }, [organizations, selectedOrganization]);
  const reportingRoleOptions = useMemo(() => {
    const merged = new Map();
    const matchingRoles = reportingRoles.filter((role) => (
      selectedOrganization?.id != null
      && String(role.orgId) === String(selectedOrganization.id)
      && (dialogMode !== 'edit' || String(role.id) !== String(selectedRole?.id))
    ));
    [...selectedReportingRoles, ...matchingRoles].forEach((role) => {
      if (role?.id != null) merged.set(String(role.id), role);
    });
    return Array.from(merged.values());
  }, [dialogMode, reportingRoles, selectedOrganization?.id, selectedReportingRoles, selectedRole?.id]);
  const reportingRoleIds = useMemo(
    () => Array.from(new Set(selectedReportingRoles
      .map((role) => role.id)
      .filter((id) => id != null)))
      .sort((a, b) => Number(a) - Number(b)),
    [selectedReportingRoles],
  );
  const organizationUserOptions = useMemo(
    () => uniqueById([...selectedUsers, ...(organizationUsers ?? [])]),
    [organizationUsers, selectedUsers],
  );
  const getUserLabel = (option) => {
    const name = option?.fullName?.trim();
    return name || option?.username || option?.email || (option?.id == null ? '' : `User ${option.id}`);
  };
  const moduleSourceRoleIds = useMemo(() => Array.from(new Set([
    ...reportingRoleIds,
    ...(dialogMode === 'edit' && selectedRole?.id != null ? [selectedRole.id] : []),
  ])).sort((a, b) => Number(a) - Number(b)), [dialogMode, reportingRoleIds, selectedRole?.id]);
  const moduleSourceRoleIdsKey = moduleSourceRoleIds.join(',');
  const moduleAccessColumns = useMemo(() => {
    const available = new Set(DEFAULT_MODULE_ACCESSES);
    moduleAccessRows.forEach((module) => module.accesses.forEach((item) => available.add(normalizeAccessName(item))));
    return Array.from(available);
  }, [moduleAccessRows]);

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedSearch(search.trim()), 400);
    return () => window.clearTimeout(timer);
  }, [search]);

  useEffect(() => {
    if (menuStatus === 'succeeded' && canRead) {
      dispatch(fetchRoles(listRequest));
      dispatch(fetchManageableRoleIds());
    }
  }, [canRead, dispatch, listRequest, menuStatus]);

  useEffect(() => {
    const term = organizationFilterQuery.trim();
    if (!dialogMode || !term) return undefined;
    const timer = window.setTimeout(() => {
      dispatch(searchRoleOrganizations(createOrganizationRequest(term)));
    }, 350);
    return () => window.clearTimeout(timer);
  }, [dialogMode, dispatch, organizationFilterQuery]);

  useEffect(() => {
    const term = reportingRoleFilterQuery.trim();
    if (!dialogMode || selectedOrganization?.id == null || !term) return undefined;
    const timer = window.setTimeout(() => {
      dispatch(searchReportingRoles(createReportingRoleRequest(
        selectedOrganization.id,
        term,
        dialogMode === 'edit' ? selectedRole?.id : null,
      )));
    }, 350);
    return () => window.clearTimeout(timer);
  }, [dialogMode, dispatch, reportingRoleFilterQuery, selectedOrganization?.id, selectedRole?.id]);

  useEffect(() => {
    if (!dialogMode || !selectedOrganization?.uuid) return undefined;
    const timer = window.setTimeout(() => {
      dispatch(fetchRoleOrganizationUsers({
        organizationUuid: selectedOrganization.uuid,
        request: createOrganizationUserRequest(userFilterQuery.trim()),
      }));
    }, 350);
    return () => window.clearTimeout(timer);
  }, [dialogMode, dispatch, selectedOrganization?.uuid, userFilterQuery]);

  useEffect(() => {
    if (dialogMode !== 'edit' || detailStatus !== 'succeeded' || !selectedRole) return;
    setForm({ name: selectedRole.name ?? '' });
    const organization = selectedRole.orgId == null
      ? null
      : { id: selectedRole.orgId, name: '' };
    setSelectedOrganization(organization);
    setOrganizationSearch(organization?.name ?? '');
    const selectedReportsToRoles = (selectedRole.reportsToRoleIds ?? []).map((id) => ({ id, name: '' }));
    setSelectedReportingRoles(uniqueById(selectedReportsToRoles));
    setSuppressParentAccessReload(false);
    setReportingRoleSearch('');
    dispatch(searchRoleOrganizations(createOrganizationRequest('')));
    if (organization?.id != null) {
      dispatch(searchReportingRoles(createReportingRoleRequest(
        organization.id,
        '',
        selectedRole.id,
      )));
    }
  }, [detailStatus, dialogMode, dispatch, selectedRole]);

  useEffect(() => {
    if (dialogMode !== 'edit' || roleUserStatus !== 'succeeded' || selectedRole?.id == null) return;
    if (String(membershipInitializedRoleId) === String(selectedRole.id)) return;
    setSelectedUsers(uniqueById(roleUsers));
    setMembershipInitializedRoleId(selectedRole.id);
  }, [dialogMode, membershipInitializedRoleId, roleUserStatus, roleUsers, selectedRole?.id]);

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
    if (!selectedReportingRoles.length || !reportingRoles.length) return;
    const roleMap = new Map(reportingRoles.map((role) => [String(role.id), role]));
    let changed = false;
    const resolved = selectedReportingRoles.map((role) => {
      const match = roleMap.get(String(role.id));
      if (match && match.name !== role.name) {
        changed = true;
        return match;
      }
      return role;
    });
    if (changed) setSelectedReportingRoles(resolved);
  }, [reportingRoles, selectedReportingRoles]);

  useEffect(() => {
    if (!dialogMode) return;
    if (dialogMode === 'create' && (showMyAccesses || suppressParentAccessReload)) {
      return;
    }

    if (!moduleSourceRoleIdsKey) {
      setModuleAccessRows([]);
      dispatch(clearReportingRoleModules());
      return;
    }
    setModuleAccessRows([]);
    dispatch(fetchReportingRoleModules(moduleSourceRoleIds));
  }, [dialogMode, dispatch, moduleSourceRoleIds, moduleSourceRoleIdsKey, showMyAccesses, suppressParentAccessReload]);

  useEffect(() => {
    if (reportingRoleModuleStatus !== 'succeeded') return;
    const savedAccessByModule = new Map(
      (dialogMode === 'edit' && Array.isArray(selectedRole?.moduleAccess)
        ? selectedRole.moduleAccess
        : []).map((module) => [String(module.moduleId), module.accesses ?? []]),
    );
    if (dialogMode === 'create' && showMyAccesses) return;
    setModuleAccessRows(reportingRoleModules.map((module) => ({
      moduleId: module.moduleId,
      name: module.name,
      accesses: (dialogMode === 'edit'
        ? savedAccessByModule.get(String(module.moduleId)) ?? []
        : module.accesses)
        .map(normalizeAccessName)
        .filter(Boolean),
    })));
  }, [dialogMode, reportingRoleModuleStatus, reportingRoleModules, selectedRole, showMyAccesses]);

  useEffect(() => {
    if (dialogMode !== 'create' || !showMyAccesses || myAccessStatus !== 'succeeded') return;
    setModuleAccessRows(myAccessModules.map((module) => ({
      ...module,
      accesses: module.accesses.map(normalizeAccessName).filter(Boolean),
    })));
  }, [dialogMode, myAccessModules, myAccessStatus, showMyAccesses]);

  useEffect(() => {
    if (isSuperAdmin || !showMyAccesses) return;
    setShowMyAccesses(false);
    setSuppressParentAccessReload(true);
    setModuleAccessRows([]);
    dispatch(clearMyAccesses());
  }, [dispatch, isSuperAdmin, showMyAccesses]);

  useEffect(() => () => {
    dispatch(clearRoleEditor());
    dispatch(clearRoleFeedback());
  }, [dispatch]);

  const resetRoleLookups = () => {
    setSelectedOrganization(null);
    setOrganizationOpen(false);
    setOrganizationSearch('');
    setOrganizationFilterQuery('');
    setSelectedReportingRoles([]);
    setReportingRoleOpen(false);
    setReportingRoleSearch('');
    setReportingRoleFilterQuery('');
    setModuleAccessRows([]);
    setShowMyAccesses(false);
    setSuppressParentAccessReload(false);
    setSelectedUsers([]);
    setWholeCompany(false);
    setShowUsers(false);
    setMembershipInitializedRoleId(null);
    setUserSearch('');
    setUserFilterQuery('');
    dispatch(clearMyAccesses());
  };

  const openCreate = () => {
    dispatch(clearRoleEditor());
    dispatch(clearRoleFeedback());
    setForm({ name: '' });
    const organization = user?.orgId == null
      ? null
      : { id: user.orgId, name: '' };
    setSelectedOrganization(organization);
    setOrganizationSearch(organization?.name ?? '');
    setOrganizationFilterQuery('');
    setSelectedReportingRoles([]);
    setReportingRoleOpen(false);
    setReportingRoleSearch('');
    setReportingRoleFilterQuery('');
    setModuleAccessRows([]);
    setShowMyAccesses(false);
    setSuppressParentAccessReload(false);
    setSelectedUsers([]);
    setWholeCompany(false);
    setShowUsers(false);
    setMembershipInitializedRoleId(null);
    setHierarchyError(null);
    setDialogMode('create');
    dispatch(searchRoleOrganizations(createOrganizationRequest('')));
    if (organization?.id != null) {
      dispatch(searchReportingRoles(createReportingRoleRequest(organization.id, '')));
    }
  };

  const openEdit = (role) => {
    if (!canManageRole(role)) {
      setHierarchyError('You can only edit roles below your assigned roles in the role hierarchy.');
      return;
    }
    dispatch(clearRoleEditor());
    dispatch(clearRoleFeedback());
    setForm({ name: '' });
    resetRoleLookups();
    setModuleAccessRows([]);
    setShowMyAccesses(false);
    setSuppressParentAccessReload(false);
    setSelectedUsers([]);
    setWholeCompany(false);
    setShowUsers(false);
    setMembershipInitializedRoleId(null);
    setHierarchyError(null);
    setDialogMode('edit');
    dispatch(fetchRole(role.uuid));
    dispatch(fetchRoleUsers(role.uuid));
  };

  const isSubmitting = createStatus === 'loading' || updateStatus === 'loading';
  const isFormValid = form.name.trim() && selectedOrganization?.id != null;

  const closeDialog = () => {
    if (isSubmitting) return;
    setDialogMode(null);
    resetRoleLookups();
    setModuleAccessRows([]);
    setShowMyAccesses(false);
    setSuppressParentAccessReload(false);
    dispatch(clearRoleEditor());
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!isFormValid) return;
    if (dialogMode === 'edit' && !canManageRole(selectedRole)) {
      setHierarchyError('You can only edit roles below your assigned roles in the role hierarchy.');
      return;
    }

    const role = {
      name: form.name.trim(),
      orgId: selectedOrganization.id,
      reportsToRoleIds: selectedReportingRoles.map((role) => role.id),
      moduleAccess: moduleAccessRows.map((module) => ({
        moduleId: module.moduleId,
        accesses: module.accesses,
      })),
      useMyAccesses: dialogMode === 'create' && isSuperAdmin && showMyAccesses,
      userIds: wholeCompany ? [] : selectedUsers.map((selectedUser) => selectedUser.id),
      wholeCompany,
    };

    const action = dialogMode === 'edit'
      ? updateRole({ ...role, id: selectedRole.id, uuid: selectedRole.uuid })
      : createRole(role);
    const result = await dispatch(action);
    const succeeded = dialogMode === 'edit'
      ? updateRole.fulfilled.match(result)
      : createRole.fulfilled.match(result);

    if (succeeded) {
      setDialogMode(null);
      resetRoleLookups();
      dispatch(clearRoleEditor());
      dispatch(fetchRoles(listRequest));
      dispatch(fetchManageableRoleIds());
    }
  };

  const handleDelete = async (role) => {
    if (!canManageRole(role)) return;
    if (!window.confirm(`Delete role "${role.name}"?`)) return;
    const result = await dispatch(deleteRole(role.uuid));
    if (deleteRole.fulfilled.match(result)) {
      dispatch(fetchRoles(listRequest));
      dispatch(fetchManageableRoleIds());
    }
  };

  const openRoleUsers = (role) => {
    setViewUsersRole(role);
    dispatch(fetchRoleUsers(role.uuid));
  };

  const closeRoleUsers = () => setViewUsersRole(null);

  const handleModuleAccessToggle = (moduleId, access) => {
    setModuleAccessRows((current) => current.map((module) => {
      if (String(module.moduleId) !== String(moduleId)) return module;
      const checked = module.accesses.includes(access);
      return {
        ...module,
        accesses: checked
          ? module.accesses.filter((item) => item !== access)
          : [...module.accesses, access],
      };
    }));
  };

  const handleShowMyAccessesChange = (event) => {
    const checked = Boolean(event.target.checked);
    setShowMyAccesses(checked);
    setModuleAccessRows([]);
    dispatch(clearReportingRoleModules());
    dispatch(clearMyAccesses());
    if (checked) {
      setSuppressParentAccessReload(false);
      dispatch(fetchMyAccesses());
    } else {
      // Do not silently restore stale parent data; the next parent-role change may load it again.
      setSuppressParentAccessReload(true);
    }
  };

  if (menuStatus === 'idle' || menuStatus === 'loading') {
    return <Box sx={{ minHeight: 320, display: 'grid', placeItems: 'center' }}><CircularProgress /></Box>;
  }

  if (!canRead) {
    return <Alert severity="warning">You do not have Read access to Role Management.</Alert>;
  }

  return (
    <Stack spacing={3}>
      <Card variant="outlined" sx={{ background: 'linear-gradient(135deg, #f5f9ff 0%, #fff 70%)' }}>
        <CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
          <Box>
            <Typography variant="h4" fontWeight={700}>Role Management</Typography>
            <Typography color="text.secondary">Create and maintain application roles.</Typography>
          </Box>
          {canCreate && <Button variant="contained" size="large" onClick={openCreate}>+ Add Role</Button>}
        </CardContent>
      </Card>

      {successMessage && <Alert severity="success" onClose={() => dispatch(clearRoleFeedback())}>{successMessage}</Alert>}
      {listError && <Alert severity="error">{listError}</Alert>}
      {manageableRoleError && <Alert severity="error">{manageableRoleError}</Alert>}
      {hierarchyError && <Alert severity="error" onClose={() => setHierarchyError(null)}>{hierarchyError}</Alert>}

      <TextField
        value={search}
        onChange={(event) => {
          setSearch(event.target.value);
          setPage(0);
        }}
        label="Search roles"
        placeholder="Search by role name"
        size="small"
        sx={{ width: { xs: '100%', sm: 380 } }}
      />

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow sx={{ bgcolor: 'grey.50' }}>
              <TableCell sx={{ fontWeight: 700 }}>Role Name</TableCell>
              <TableCell align="center" sx={{ fontWeight: 700 }}>Users</TableCell>
              {(canUpdate || canDelete) && <TableCell align="right" sx={{ fontWeight: 700 }}>Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {listStatus === 'loading' && (
              <TableRow><TableCell colSpan={canUpdate || canDelete ? 3 : 2} align="center" sx={{ py: 6 }}><CircularProgress size={30} /></TableCell></TableRow>
            )}
            {listStatus === 'succeeded' && records.length === 0 && (
              <TableRow><TableCell colSpan={canUpdate || canDelete ? 3 : 2} align="center" sx={{ py: 6, color: 'text.secondary' }}>No roles found.</TableCell></TableRow>
            )}
            {listStatus !== 'loading' && records.map((role) => (
              <TableRow key={role.uuid} hover>
                <TableCell><Typography fontWeight={600}>{role.name}</Typography></TableCell>
                <TableCell align="center">
                  <Button variant="text" size="small" onClick={() => openRoleUsers(role)} sx={{ minWidth: 36, textDecoration: 'underline' }}>
                    {Number(role.userCount || 0)}
                  </Button>
                </TableCell>
                {(canUpdate || canDelete) && (
                  <TableCell align="right">
                    {canUpdate && (
                      <Tooltip title={canManageRole(role) ? 'Edit role' : 'Only lower roles in your hierarchy can be edited'}>
                        <span><IconButton color="primary" disabled={!canManageRole(role)} onClick={() => openEdit(role)}><EditIcon /></IconButton></span>
                      </Tooltip>
                    )}
                    {canDelete && (
                      <Tooltip title={canManageRole(role) ? 'Delete role' : 'Only lower roles in your hierarchy can be deleted'}>
                        <span>
                          <IconButton color="error" disabled={!canManageRole(role) || deletingId === role.uuid} onClick={() => handleDelete(role)}>
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
          <DialogTitle>{dialogMode === 'edit' ? 'Edit Role' : 'Create Role'}</DialogTitle>
          <DialogContent>
            {dialogMode === 'edit' && detailStatus === 'loading' && (
              <Box sx={{ minHeight: 180, display: 'grid', placeItems: 'center' }}><CircularProgress /></Box>
            )}
            {dialogMode === 'edit' && detailStatus === 'failed' && (
              <Alert severity="error" sx={{ mt: 1 }}>{detailError}</Alert>
            )}
            {(dialogMode === 'create' || detailStatus === 'succeeded') && (
              <Stack spacing={2.5} sx={{ pt: 1 }}>
                {mutationError && <Alert severity="error">{mutationError}</Alert>}
                <TextField
                  label="Role Name"
                  value={form.name}
                  onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
                  required
                  autoFocus
                  fullWidth
                />
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
                    setSelectedUsers([]);
                    setWholeCompany(false);
                    setShowUsers(false);
                    setMembershipInitializedRoleId(null);
                    setUserSearch('');
                    setUserFilterQuery('');
                    setSelectedReportingRoles([]);
                    setShowMyAccesses(false);
                    setSuppressParentAccessReload(false);
                    setModuleAccessRows([]);
                    dispatch(clearMyAccesses());
                    dispatch(clearReportingRoleModules());
                    setReportingRoleSearch('');
                    setReportingRoleFilterQuery('');
                    if (value?.id != null) {
                      dispatch(searchReportingRoles(createReportingRoleRequest(
                        value.id,
                        '',
                        dialogMode === 'edit' ? selectedRole?.id : null,
                      )));
                    }
                  }}
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Organization"
                      required
                      error={organizationStatus === 'failed'}
                      helperText={organizationError || 'Search and select an organization.'}
                    />
                  )}
                />
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
                  <Autocomplete
                    multiple
                    disabled={selectedOrganization?.id == null}
                    open={reportingRoleOpen}
                    onOpen={() => setReportingRoleOpen(true)}
                    onClose={() => setReportingRoleOpen(false)}
                    options={reportingRoleOptions}
                    value={selectedReportingRoles}
                    inputValue={reportingRoleSearch}
                    loading={reportingRoleStatus === 'loading'}
                    filterOptions={(options) => options}
                    isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)}
                    getOptionLabel={(option) => option?.name ?? ''}
                    onInputChange={(_, value, reason) => {
                      setReportingRoleSearch(value);
                      setReportingRoleFilterQuery(reason === 'input' ? value : '');
                    }}
                    onChange={(_, value) => {
                      setSelectedReportingRoles(uniqueById(value));
                      setSuppressParentAccessReload(false);
                    }}
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        label="Parent Role"
                        placeholder={selectedReportingRoles.length ? '' : 'Search and select roles'}
                        error={reportingRoleStatus === 'failed'}
                        helperText={reportingRoleError || (selectedOrganization ? 'Select one or more roles.' : 'Select an organization first.')}
                      />
                    )}
                    sx={{ flex: 1, width: '100%' }}
                  />

                  {dialogMode === 'create' && isSuperAdmin && (
                    <FormControlLabel
                      sx={{ mt: { xs: 0, sm: 1 }, mr: 0, whiteSpace: 'nowrap' }}
                      control={(
                        <Checkbox
                          checked={showMyAccesses}
                          onChange={handleShowMyAccessesChange}
                        />
                      )}
                      label="Show my accesses"
                    />
                  )}
                </Box>
                {dialogMode === 'create' && !selectedReportingRoles.length && !showMyAccesses && (
                  <Alert severity="info">Select at least one parent role to load its module accesses.</Alert>
                )}
                {showMyAccesses && myAccessStatus === 'loading' && (
                  <Box sx={{ py: 4, display: 'grid', placeItems: 'center' }}><CircularProgress size={28} /></Box>
                )}
                {showMyAccesses && myAccessStatus === 'failed' && (
                  <Alert severity="error">{myAccessError}</Alert>
                )}
                {!showMyAccesses && reportingRoleModuleStatus === 'loading' && (
                  <Box sx={{ py: 4, display: 'grid', placeItems: 'center' }}><CircularProgress size={28} /></Box>
                )}
                {!showMyAccesses && reportingRoleModuleStatus === 'failed' && (
                  <Alert severity="error">{reportingRoleModuleError}</Alert>
                )}
                {((showMyAccesses && myAccessStatus === 'succeeded')
                  || (!showMyAccesses && reportingRoleModuleStatus === 'succeeded'))
                  && moduleAccessRows.length === 0 && (
                  <Alert severity="info">The selected roles do not have any module access.</Alert>
                )}
                {moduleAccessRows.length > 0 && (
                  <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 360 }}>
                    <Table stickyHeader size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell sx={{ fontWeight: 700, minWidth: 180 }}>Module Name</TableCell>
                          {moduleAccessColumns.map((access) => (
                            <TableCell key={access} align="center" sx={{ fontWeight: 700 }}>{access}</TableCell>
                          ))}
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {moduleAccessRows.map((module) => (
                          <TableRow key={module.moduleId} hover>
                            <TableCell><Typography fontWeight={600}>{module.name}</Typography></TableCell>
                            {moduleAccessColumns.map((access) => (
                              <TableCell key={access} align="center">
                                <Checkbox
                                  size="small"
                                  checked={module.accesses.includes(access)}
                                  onChange={() => handleModuleAccessToggle(module.moduleId, access)}
                                  inputProps={{ 'aria-label': `${module.name} ${access}` }}
                                />
                              </TableCell>
                            ))}
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
                {dialogMode === 'edit' && (
                  <Box>
                    <Button
                      variant="text"
                      size="small"
                      onClick={() => setShowUsers((current) => !current)}
                      disabled={roleUserStatus === 'loading'}
                      sx={{ px: 0 }}
                    >
                      {roleUserStatus === 'loading' ? 'Loading users…' : showUsers ? 'Hide Users' : 'Show Users'}
                    </Button>
                    {roleUserError && <Alert severity="error" sx={{ mt: 1 }}>{roleUserError}</Alert>}
                  </Box>
                )}
                {showUsers && (
                  <TableContainer component={Paper} variant="outlined" sx={{ maxHeight: 280 }}>
                    <Table stickyHeader size="small">
                      <TableHead>
                        <TableRow>
                          <TableCell sx={{ fontWeight: 700 }}>User</TableCell>
                          <TableCell sx={{ fontWeight: 700 }}>Email</TableCell>
                          <TableCell align="center" sx={{ fontWeight: 700 }}>Assigned</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {(wholeCompany ? organizationUsers : selectedUsers).map((assignedUser) => (
                          <TableRow key={assignedUser.id} hover>
                            <TableCell>{getUserLabel(assignedUser)}</TableCell>
                            <TableCell>{assignedUser.email || '—'}</TableCell>
                            <TableCell align="center">
                              <Checkbox
                                checked
                                disabled={wholeCompany}
                                onChange={() => setSelectedUsers((current) => (
                                  current.filter((item) => String(item.id) !== String(assignedUser.id))
                                ))}
                                inputProps={{ 'aria-label': `Remove ${getUserLabel(assignedUser)} from role` }}
                              />
                            </TableCell>
                          </TableRow>
                        ))}
                        {(wholeCompany ? organizationUsers : selectedUsers).length === 0 && (
                          <TableRow>
                            <TableCell colSpan={3} align="center" sx={{ py: 3, color: 'text.secondary' }}>
                              No users are assigned to this role.
                            </TableCell>
                          </TableRow>
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>
                )}
                <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, flexDirection: { xs: 'column', sm: 'row' } }}>
                  <Autocomplete
                    multiple
                    disabled={selectedOrganization?.id == null || wholeCompany}
                    options={organizationUserOptions}
                    value={selectedUsers}
                    inputValue={userSearch}
                    loading={organizationUserStatus === 'loading'}
                    isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)}
                    getOptionLabel={getUserLabel}
                    filterOptions={(options) => options}
                    onInputChange={(_, value, reason) => {
                      setUserSearch(value);
                      setUserFilterQuery(reason === 'input' ? value : '');
                    }}
                    onChange={(_, value) => setSelectedUsers(uniqueById(value))}
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        label="Add Users"
                        placeholder={selectedUsers.length ? '' : 'Select users for this role'}
                        error={organizationUserStatus === 'failed'}
                        helperText={organizationUserError || (selectedOrganization ? 'Select one or more users.' : 'Select an organization first.')}
                      />
                    )}
                    sx={{ flex: 1, width: '100%' }}
                  />
                  <FormControlLabel
                    sx={{ mt: { xs: 0, sm: 1 }, mr: 0, whiteSpace: 'nowrap' }}
                    control={(
                      <Checkbox
                        checked={wholeCompany}
                        disabled={selectedOrganization?.id == null}
                        onChange={(event) => setWholeCompany(Boolean(event.target.checked))}
                      />
                    )}
                    label="For whole Company"
                  />
                </Box>
              </Stack>
            )}
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button onClick={closeDialog} disabled={isSubmitting}>Cancel</Button>
            <Button
              type="submit"
              variant="contained"
              disabled={
                !isFormValid
                || isSubmitting
                || (dialogMode === 'edit' && detailStatus !== 'succeeded')
                || (dialogMode === 'edit' && roleUserStatus !== 'succeeded')
                || (showMyAccesses && myAccessStatus !== 'succeeded')
                || (!showMyAccesses
                  && !suppressParentAccessReload
                  && moduleSourceRoleIds.length > 0
                  && reportingRoleModuleStatus !== 'succeeded')
              }
            >
              {isSubmitting ? <CircularProgress size={22} color="inherit" /> : dialogMode === 'edit' ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
      <Dialog open={Boolean(viewUsersRole)} onClose={closeRoleUsers} fullWidth maxWidth="md">
        <DialogTitle>Users in {viewUsersRole?.name}</DialogTitle>
        <DialogContent>
          {roleUserError && <Alert severity="error" sx={{ mb: 2 }}>{roleUserError}</Alert>}
          {roleUserStatus === 'loading' && (
            <Box sx={{ minHeight: 180, display: 'grid', placeItems: 'center' }}><CircularProgress /></Box>
          )}
          {roleUserStatus !== 'loading' && (
            <TableContainer component={Paper} variant="outlined">
              <Table size="small">
                <TableHead><TableRow sx={{ bgcolor: 'grey.50' }}><TableCell sx={{ fontWeight: 700 }}>User</TableCell><TableCell sx={{ fontWeight: 700 }}>Email</TableCell></TableRow></TableHead>
                <TableBody>
                  {roleUserStatus === 'succeeded' && roleUsers.length === 0 && <TableRow><TableCell colSpan={2} align="center" sx={{ py: 4 }}>No users are assigned to this role.</TableCell></TableRow>}
                  {roleUsers.map((assignedUser) => <TableRow key={assignedUser.uuid}><TableCell>{getUserLabel(assignedUser)}</TableCell><TableCell>{assignedUser.email || '—'}</TableCell></TableRow>)}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>
        <DialogActions><Button onClick={closeRoleUsers}>Close</Button></DialogActions>
      </Dialog>
      <Snackbar
        open={Boolean(deleteNotification)}
        autoHideDuration={6000}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        onClose={(_, reason) => { if (reason !== 'clickaway') dispatch(clearRoleFeedback()); }}
      >
        <Alert
          variant="filled"
          severity={deleteNotification?.severity || 'error'}
          onClose={() => dispatch(clearRoleFeedback())}
        >
          {deleteNotification?.message}
        </Alert>
      </Snackbar>
    </Stack>
  );
};

export default Role;
