import apiClient from './apiClient';
import { createMasterDataService, getResponseMessage, throwForFailureMessage } from './masterDataService';

const endpoint = import.meta.env.VITE_LOCATION_ENDPOINT || '/master/location';

const masterDataService = createMasterDataService({
  endpoint,
  entityName: 'location',
  collectionName: 'locations',
  label: 'Location',
});

export const locationService = {
  ...masterDataService,
  async getUsers(locationUuid) {
    const response = await apiClient.get(`${endpoint}/users`, { params: { locationUuid } });
    const users = response.data?.data ?? response.data;
    if (!Array.isArray(users)) throw new Error('The location users API did not return a valid collection.');
    return users;
  },
  async removeUser(locationUuid, userUuid) {
    const response = await apiClient.delete(`${endpoint}/user`, { params: { locationUuid, userUuid } });
    throwForFailureMessage(response, 'Unable to remove the location from the user.');
    return { locationUuid, userUuid, message: getResponseMessage(response, 'Location removed from user successfully.') };
  },
};
