import apiClient from './apiClient';

const unwrapData = (payload) => payload?.data ?? payload;

export const extractPage = (payload, collectionName) => {
  const data = unwrapData(payload);
  const page = data?.page ?? data;
  const records = page?.content ?? page?.records ?? page?.items ?? page?.[collectionName] ?? [];
  const totalElements = page?.totalElements ?? page?.totalRecords ?? page?.total ?? records.length;

  if (!Array.isArray(records)) throw new Error(`The ${collectionName} API did not return a valid collection.`);
  return { records, totalElements: Number(totalElements) || 0 };
};

export const extractEntity = (payload, entityName) => {
  const data = unwrapData(payload);
  const entity = data?.[entityName] ?? data;
  if (!entity || typeof entity !== 'object' || entity.id == null || !entity.uuid) {
    throw new Error(`The API did not return a valid ${entityName}.`);
  }
  return entity;
};

export const getResponseMessage = (response, fallback) => response.data?.message || fallback;

export const throwForFailureMessage = (response, fallback) => {
  const message = response.data?.message;
  if (
    response.data?.success === false
    || (typeof message === 'string' && /\b(fail(?:ed|ure)?|unable|cannot|can't)\b/i.test(message))
  ) {
    throw new Error(message || fallback);
  }
};

export const createMasterDataService = ({ endpoint, entityName, collectionName, label }) => ({
  async filter(request) {
    const response = await apiClient.post(`${endpoint}/filter`, request);
    return extractPage(response.data, collectionName);
  },
  async get(uuid) {
    const response = await apiClient.get(`${endpoint}/${uuid}`);
    return extractEntity(response.data, entityName);
  },
  async create(request) {
    const response = await apiClient.post(endpoint, request);
    return {
      entity: extractEntity(response.data, entityName),
      message: getResponseMessage(response, `${label} created successfully.`),
    };
  },
  async update(uuid, request) {
    const response = await apiClient.put(`${endpoint}/${uuid}`, request);
    return {
      entity: extractEntity(response.data, entityName),
      message: getResponseMessage(response, `${label} updated successfully.`),
    };
  },
  async delete(uuid) {
    const response = await apiClient.delete(`${endpoint}/${uuid}`);
    throwForFailureMessage(response, `Unable to delete the ${entityName}.`);
    return { uuid, message: getResponseMessage(response, `${label} deleted successfully.`) };
  },
});
