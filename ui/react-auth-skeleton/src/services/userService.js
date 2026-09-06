import apiClient from './apiClient';

const userEndpoint = import.meta.env.VITE_USER_ENDPOINT || '/master/user';
const organizationEndpoint = import.meta.env.VITE_ORGANIZATION_ENDPOINT || '/master/organization';
const locationEndpoint = import.meta.env.VITE_LOCATION_ENDPOINT || '/master/location';
const roleEndpoint = import.meta.env.VITE_ROLE_ENDPOINT || '/master/role';

const unwrapData = (payload) => payload?.data ?? payload;

const extractPage = (payload, collectionName) => {
  const data = unwrapData(payload);
  const page = data?.page ?? data;
  const records = page?.content ?? page?.records ?? page?.items ?? page?.[collectionName] ?? [];
  const totalElements = page?.totalElements ?? page?.totalRecords ?? page?.total ?? records.length;

  if (!Array.isArray(records)) {
    throw new Error(`The ${collectionName} API did not return a valid collection.`);
  }

  return { records, totalElements: Number(totalElements) || 0 };
};

const extractEntity = (payload, entityName) => {
  const data = unwrapData(payload);
  const entity = data?.[entityName] ?? data;

  if (!entity || typeof entity !== 'object' || entity.id == null || !entity.uuid) {
    throw new Error(`The API did not return a valid ${entityName}.`);
  }

  return entity;
};

export const userService = {
  async getUsers(request) {
    const response = await apiClient.post(`${userEndpoint}/filter`, request);
    return extractPage(response.data, 'users');
  },

  async getUser(uuid) {
    const response = await apiClient.get(`${userEndpoint}/${uuid}`);
    return extractEntity(response.data, 'user');
  },

  async createUser(user) {
    const response = await apiClient.post(userEndpoint, user);
    const createdUser = unwrapData(response.data);
    return {
      user: createdUser,
      message: response.data?.message || 'User created successfully.',
    };
  },

  async updateUser(uuid, user) {
    const response = await apiClient.put(`${userEndpoint}/${uuid}`, user);
    return {
      user: unwrapData(response.data),
      message: response.data?.message || 'User updated successfully.',
    };
  },

  async deleteUser(uuid) {
    const response = await apiClient.delete(`${userEndpoint}/${uuid}`);
    return {
      uuid,
      message: response.data?.message || 'User deleted successfully.',
    };
  },

  async getOrganizations(request) {
    const response = await apiClient.post(`${organizationEndpoint}/filter`, request);
    return extractPage(response.data, 'organizations');
  },

  async getLocations(request) {
    const response = await apiClient.post(`${locationEndpoint}/filter`, request);
    return extractPage(response.data, 'locations');
  },

  async getRoles(request) {
    const response = await apiClient.post(`${roleEndpoint}/filter`, request);
    return extractPage(response.data, 'roles');
  },
};
