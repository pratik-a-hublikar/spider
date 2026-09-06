import { createMasterDataService } from './masterDataService';

const endpoint = import.meta.env.VITE_ORGANIZATION_ENDPOINT || '/master/organization';

export const organizationService = createMasterDataService({
  endpoint,
  entityName: 'organization',
  collectionName: 'organizations',
  label: 'Organization',
});
