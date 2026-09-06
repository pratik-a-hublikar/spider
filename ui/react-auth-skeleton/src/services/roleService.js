import apiClient from './apiClient';

const roleEndpoint = import.meta.env.VITE_ROLE_ENDPOINT || '/master/role';
const moduleEndpoint = import.meta.env.VITE_MENU_ENDPOINT || '/module';
const userEndpoint = import.meta.env.VITE_USER_ENDPOINT || '/master/user';

const unwrapData = (payload) => payload?.data ?? payload;

const extractPaginatedRoles = (payload) => {
  const data = unwrapData(payload);
  const page = data?.page ?? data;
  const records = page?.content ?? page?.records ?? page?.items ?? page?.roles ?? [];
  const totalElements = page?.totalElements ?? page?.totalRecords ?? page?.total ?? records.length;

  if (!Array.isArray(records)) {
    throw new Error('The role API did not return a valid collection.');
  }

  return { records, totalElements: Number(totalElements) || 0 };
};

const extractRole = (payload) => {
  const role = unwrapData(payload)?.role ?? unwrapData(payload);

  if (!role || typeof role !== 'object' || !role.uuid) {
    throw new Error('The role API did not return a valid role.');
  }

  return role;
};

const extractRoleModules = (payload) => {
  const data = unwrapData(payload);
  const modules = Array.isArray(data) ? data : data?.module ?? [];
  if (!Array.isArray(modules)) throw new Error('The role module API did not return a valid collection.');
  return modules;
};

const extractCollection = (payload, label) => {
  const data = unwrapData(payload);
  if (!Array.isArray(data)) throw new Error(`The ${label} API did not return a valid collection.`);
  return data;
};

const flattenModules = (modules, rows = []) => {
  modules.forEach((module) => {
    if (module?.id != null) {
      rows.push({
        moduleId: module.id,
        name: module.moduleUiName || module.name || `Module ${module.id}`,
        accesses: Array.isArray(module.moduleAccessStr)
          ? module.moduleAccessStr.map(String)
          : [],
      });
    }
    flattenModules(module?.subModules ?? module?.childModules ?? [], rows);
  });
  return rows;
};

export const roleService = {
  async getRoles(request) {
    const response = await apiClient.post(`${roleEndpoint}/filter`, request);
    return extractPaginatedRoles(response.data);
  },

  async getRole(uuid) {
    const response = await apiClient.get(`${roleEndpoint}/${uuid}`);
    return extractRole(response.data);
  },

  async getModulesForRoles(roleIds) {
    const responses = await Promise.all(
      roleIds.map((roleId) => apiClient.get(`${moduleEndpoint}/role/${roleId}`)),
    );
    const merged = new Map();
    responses.forEach((response) => {
      flattenModules(extractRoleModules(response.data)).forEach((module) => {
        const key = String(module.moduleId);
        const current = merged.get(key) ?? { ...module, accesses: [] };
        current.accesses = Array.from(new Set([...current.accesses, ...module.accesses]));
        merged.set(key, current);
      });
    });
    return Array.from(merged.values()).sort((left, right) => left.name.localeCompare(right.name));
  },

  async getMyAccesses() {
    const response = await apiClient.get(`${roleEndpoint}/my-accesses`);
    return flattenModules(extractRoleModules(response.data))
      .sort((left, right) => left.name.localeCompare(right.name));
  },

  async getManageableRoleIds() {
    const response = await apiClient.get(`${roleEndpoint}/manageable-uuids`);
    return extractCollection(response.data, 'manageable roles');
  },

  async getOrganizationUsers(organizationUuid, request) {
    const response = await apiClient.post(`${userEndpoint}/organization/${organizationUuid}/filter`, request);
    return extractPaginatedRoles(response.data).records;
  },

  async getRoleUsers(roleUuid) {
    const response = await apiClient.get(`${roleEndpoint}/${roleUuid}/users`);
    return extractCollection(response.data, 'role users');
  },

  async createRole(role) {
    const response = await apiClient.post(roleEndpoint, role);
    return {
      role,
      message: response.data?.message || 'Role created successfully.',
    };
  },

  async updateRole(role) {
    const response = await apiClient.put(`${roleEndpoint}/${role.uuid}`, role);
    return {
      role,
      message: response.data?.message || 'Role updated successfully.',
    };
  },

  async deleteRole(uuid) {
    const countResponse = await apiClient.get(`${roleEndpoint}/${uuid}/active-user-count`);
    const activeUserCount = Number(unwrapData(countResponse.data)) || 0;
    if (activeUserCount > 0) {
      throw new Error(`There are ${activeUserCount} members who are already assigned to this role,please remove those user's role`);
    }
    const response = await apiClient.delete(`${roleEndpoint}/${uuid}`);
    const message = response.data?.message;

    if (
      response.data?.success === false
      || (typeof message === 'string' && /\b(fail(?:ed|ure)?|unable|cannot|can't)\b/i.test(message))
    ) {
      throw new Error(message || 'Unable to delete the role.');
    }

    return { uuid, message: message || 'Role deleted successfully.' };
  },
};
