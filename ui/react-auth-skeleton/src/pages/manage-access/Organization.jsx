import { useEffect, useMemo, useState } from 'react';
import { Alert, Autocomplete, Box, Button, Card, CardContent, Checkbox, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, IconButton, Paper, Snackbar, Stack, SvgIcon, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination, TableRow, TextField, Tooltip, Typography } from '@mui/material';
import { useDispatch, useSelector } from 'react-redux';
import { clearOrganizationEditor, clearOrganizationNotification, createOrganization, deleteOrganization, fetchOrganization, fetchOrganizations, searchParentOrganizations, updateOrganization } from '../../features/organization/organizationSlice';
import { ACCESS_TYPES, FILTER_OPERATORS, MODULE_NAMES } from '../../constants/accessControl';

const EditIcon = () => <SvgIcon><path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25M20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83Z" /></SvgIcon>;
const DeleteIcon = () => <SvgIcon><path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12M8 9h8v10H8V9m7.5-5-1-1h-5l-1 1H5v2h14V4Z" /></SvgIcon>;

const findModule = (modules, names) => {
  for (const module of Array.isArray(modules) ? modules : []) {
    const name = String(module.name ?? module.moduleName ?? '').toLowerCase();
    if (names.some((value) => value.toLowerCase() === name)) return module;
    const nested = findModule(module.subModules ?? module.childModules, names);
    if (nested) return nested;
  }
  return null;
};
const parseAccess = (value) => Array.isArray(value)
  ? value.map((item) => String(item?.name ?? item).toLowerCase())
  : String(value ?? '').replace(/[\[\]"']/g, '').split(/[,|;\s]+/).filter(Boolean).map((item) => item.toLowerCase());
const listRequest = (page, pageSize, search) => ({ page, pageSize, all: false, sort: null, filterCriteria: [], orCriteria: search ? [{ column: 'appName', operator: FILTER_OPERATORS.CONTAINS, values: [search] }] : [] });
const parentRequest = (search, editingId) => ({ page: 0, pageSize: 10, all: false, sort: null, filterCriteria: editingId ? [{ column: 'id', operator: FILTER_OPERATORS.NOT_EQUAL, values: [String(editingId)] }] : [], orCriteria: search ? [{ column: 'appName', operator: FILTER_OPERATORS.CONTAINS, values: [search] }] : [] });

const Organization = () => {
  const dispatch = useDispatch();
  const { items, status: menuStatus } = useSelector((state) => state.menu);
  const state = useSelector((store) => store.organization);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [mode, setMode] = useState(null);
  const [form, setForm] = useState({ name: '', superOrganization: false });
  const [parent, setParent] = useState(null);
  const [parentOpen, setParentOpen] = useState(false);
  const [parentSearch, setParentSearch] = useState('');
  const [parentFilterQuery, setParentFilterQuery] = useState('');

  const access = useMemo(() => {
    const module = findModule(items, [MODULE_NAMES.ORGANIZATIONS, 'Organizations', 'Organisation', 'Organisations']);
    return new Set([...parseAccess(module?.moduleAccessStr), ...parseAccess(module?.moduleAccess)]);
  }, [items]);
  const canRead = access.has(ACCESS_TYPES.READ);
  const canWrite = access.has(ACCESS_TYPES.WRITE);
  const canUpdate = access.has(ACCESS_TYPES.UPDATE);
  const canDelete = access.has(ACCESS_TYPES.DELETE);
  const currentRequest = useMemo(() => listRequest(page, pageSize, debouncedSearch), [debouncedSearch, page, pageSize]);
  const parentOptions = useMemo(() => {
    const map = new Map();
    [...(parent ? [parent] : []), ...state.parentOptions].forEach((item) => { if (item?.id != null) map.set(String(item.id), item); });
    return Array.from(map.values());
  }, [parent, state.parentOptions]);

  useEffect(() => { const timer = window.setTimeout(() => setDebouncedSearch(search.trim()), 400); return () => window.clearTimeout(timer); }, [search]);
  useEffect(() => { if (menuStatus === 'succeeded' && canRead) dispatch(fetchOrganizations(currentRequest)); }, [canRead, currentRequest, dispatch, menuStatus]);
  useEffect(() => {
    const term = parentFilterQuery.trim();
    if (!mode || !term) return undefined;
    const timer = window.setTimeout(() => dispatch(searchParentOrganizations(parentRequest(term, mode === 'edit' ? state.selected?.id : null))), 350);
    return () => window.clearTimeout(timer);
  }, [dispatch, mode, parentFilterQuery, state.selected?.id]);
  useEffect(() => {
    if (mode !== 'edit' || state.detailStatus !== 'succeeded' || !state.selected) return;
    setForm({ name: state.selected.name ?? '', superOrganization: Boolean(state.selected.superOrganization) });
    const selectedParent = state.selected.parentOrganizationId == null ? null : { id: state.selected.parentOrganizationId, name: '' };
    setParent(selectedParent); setParentSearch(selectedParent?.name ?? '');
    dispatch(searchParentOrganizations(parentRequest('', state.selected.id)));
  }, [dispatch, mode, state.detailStatus, state.selected]);
  useEffect(() => {
    if (!parent) return;
    const resolved = state.parentOptions.find((item) => String(item.id) === String(parent.id));
    if (resolved && resolved.name !== parent.name) { setParent(resolved); setParentSearch(resolved.name ?? ''); }
  }, [parent, state.parentOptions]);

  const reset = () => { setForm({ name: '', superOrganization: false }); setParent(null); setParentOpen(false); setParentSearch(''); setParentFilterQuery(''); };
  const openCreate = () => { dispatch(clearOrganizationEditor()); dispatch(clearOrganizationNotification()); reset(); setMode('create'); dispatch(searchParentOrganizations(parentRequest('', null))); };
  const openEdit = (item) => { dispatch(clearOrganizationEditor()); dispatch(clearOrganizationNotification()); reset(); setMode('edit'); dispatch(fetchOrganization(item.uuid)); };
  const busy = state.createStatus === 'loading' || state.updateStatus === 'loading';
  const close = () => { if (busy) return; setMode(null); reset(); dispatch(clearOrganizationEditor()); };
  const submit = async (event) => {
    event.preventDefault(); if (!form.name.trim()) return;
    const request = { name: form.name.trim(), superOrganization: form.superOrganization, parentOrganizationId: form.superOrganization ? null : parent?.id ?? null };
    const action = mode === 'edit' ? updateOrganization({ uuid: state.selected.uuid, request }) : createOrganization(request);
    const result = await dispatch(action);
    if ((mode === 'edit' ? updateOrganization.fulfilled : createOrganization.fulfilled).match(result)) { setMode(null); reset(); dispatch(clearOrganizationEditor()); dispatch(fetchOrganizations(currentRequest)); }
  };
  const remove = async (item) => { if (!window.confirm(`Delete organization "${item.name}"?`)) return; const result = await dispatch(deleteOrganization(item.uuid)); if (deleteOrganization.fulfilled.match(result)) dispatch(fetchOrganizations(currentRequest)); };

  if (menuStatus === 'idle' || menuStatus === 'loading') return <Box sx={{ minHeight: 320, display: 'grid', placeItems: 'center' }}><CircularProgress /></Box>;
  if (!canRead) return <Alert severity="warning">You do not have Read access to Organization Management.</Alert>;

  return <Stack spacing={3}>
    <Card variant="outlined" sx={{ background: 'linear-gradient(135deg, #f5f9ff 0%, #fff 70%)' }}><CardContent sx={{ display: 'flex', justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2 }}><Box><Typography variant="h4" fontWeight={700}>Organization Management</Typography><Typography color="text.secondary">Create and maintain organizations.</Typography></Box>{canWrite && <Button variant="contained" size="large" onClick={openCreate}>+ Add Organization</Button>}</CardContent></Card>
    {state.listError && <Alert severity="error">{state.listError}</Alert>}
    <TextField value={search} onChange={(event) => { setSearch(event.target.value); setPage(0); }} label="Search organizations" size="small" sx={{ width: { xs: '100%', sm: 400 } }} />
    <TableContainer component={Paper} variant="outlined"><Table><TableHead><TableRow sx={{ bgcolor: 'grey.50' }}><TableCell sx={{ fontWeight: 700 }}>Organization</TableCell><TableCell sx={{ fontWeight: 700 }}>Type</TableCell><TableCell sx={{ fontWeight: 700 }}>Parent Organization</TableCell>{(canUpdate || canDelete) && <TableCell align="right" sx={{ fontWeight: 700 }}>Actions</TableCell>}</TableRow></TableHead><TableBody>
      {state.listStatus === 'loading' && <TableRow><TableCell colSpan={4} align="center" sx={{ py: 6 }}><CircularProgress size={30} /></TableCell></TableRow>}
      {state.listStatus === 'succeeded' && state.records.length === 0 && <TableRow><TableCell colSpan={4} align="center" sx={{ py: 6 }}>No organizations found.</TableCell></TableRow>}
      {state.listStatus !== 'loading' && state.records.map((item) => <TableRow key={item.uuid} hover><TableCell><Typography fontWeight={600}>{item.name}</Typography></TableCell><TableCell><Chip size="small" label={item.superOrganization ? 'Super' : 'Standard'} color={item.superOrganization ? 'primary' : 'default'} /></TableCell><TableCell>{item.parentOrganizationName ?? '—'}</TableCell>{(canUpdate || canDelete) && <TableCell align="right">{canUpdate && <Tooltip title="Edit organization"><IconButton color="primary" onClick={() => openEdit(item)}><EditIcon /></IconButton></Tooltip>}{canDelete && <Tooltip title="Delete organization"><span><IconButton color="error" disabled={state.deletingId === item.uuid} onClick={() => remove(item)}><DeleteIcon /></IconButton></span></Tooltip>}</TableCell>}</TableRow>)}
    </TableBody></Table><TablePagination component="div" count={state.totalElements} page={page} rowsPerPage={pageSize} rowsPerPageOptions={[5, 10, 25, 50]} onPageChange={(_, value) => setPage(value)} onRowsPerPageChange={(event) => { setPageSize(Number(event.target.value)); setPage(0); }} /></TableContainer>
    <Dialog open={Boolean(mode)} onClose={close} fullWidth maxWidth="sm"><Box component="form" onSubmit={submit}><DialogTitle>{mode === 'edit' ? 'Edit Organization' : 'Create Organization'}</DialogTitle><DialogContent>
      {mode === 'edit' && state.detailStatus === 'loading' && <Box sx={{ minHeight: 200, display: 'grid', placeItems: 'center' }}><CircularProgress /></Box>}
      {mode === 'edit' && state.detailStatus === 'failed' && <Alert severity="error">{state.detailError}</Alert>}
      {(mode === 'create' || state.detailStatus === 'succeeded') && <Stack spacing={2.5} sx={{ pt: 1 }}>{state.mutationError && <Alert severity="error">{state.mutationError}</Alert>}<TextField label="Organization Name" value={form.name} onChange={(event) => setForm((old) => ({ ...old, name: event.target.value }))} required autoFocus fullWidth />{mode === 'edit' && <FormControlLabel control={<Checkbox checked={form.superOrganization} disabled />} label="Super organization" />}<Autocomplete disabled={form.superOrganization} open={parentOpen} onOpen={() => setParentOpen(true)} onClose={() => setParentOpen(false)} options={parentOptions} value={parent} inputValue={parentSearch} loading={state.parentStatus === 'loading'} filterOptions={(options) => options} isOptionEqualToValue={(option, value) => String(option.id) === String(value.id)} getOptionLabel={(option) => option?.name ?? ''} onInputChange={(_, value, reason) => { setParentSearch(value); setParentFilterQuery(reason === 'input' ? value : ''); }} onChange={(_, value) => setParent(value)} renderInput={(params) => <TextField {...params} label="Parent Organization" error={state.parentStatus === 'failed'} helperText={state.parentError} />} /></Stack>}
    </DialogContent><DialogActions sx={{ px: 3, pb: 3 }}><Button onClick={close} disabled={busy}>Cancel</Button><Button type="submit" variant="contained" disabled={busy || !form.name.trim() || (mode === 'edit' && state.detailStatus !== 'succeeded')}>{busy ? <CircularProgress size={22} color="inherit" /> : mode === 'edit' ? 'Update' : 'Create'}</Button></DialogActions></Box></Dialog>
    <Snackbar open={Boolean(state.notification)} autoHideDuration={6000} anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }} onClose={(_, reason) => { if (reason !== 'clickaway') dispatch(clearOrganizationNotification()); }}><Alert variant="filled" severity={state.notification?.severity || 'success'} onClose={() => dispatch(clearOrganizationNotification())}>{state.notification?.message}</Alert></Snackbar>
  </Stack>;
};

export default Organization;
