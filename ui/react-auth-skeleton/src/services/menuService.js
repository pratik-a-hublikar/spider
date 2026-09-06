import apiClient from './apiClient';

const menuEndpoint = import.meta.env.VITE_MENU_ENDPOINT + '/accessible';
const menuListEndpoint = import.meta.env.VITE_MENU_ENDPOINT+'/filter';
const menuDeleteEndpoint = import.meta.env.VITE_MENU_ENDPOINT ;
const menuUpdateEndpoint = import.meta.env.VITE_MENU_ENDPOINT ;

const extractMenuItems = (payload) => {
  const data = payload?.data ?? payload;
  const menu = data?.menus ?? data?.menu ?? data?.items ?? data;

  if (!Array.isArray(menu)) {
    throw new Error('The menu API did not return an array of menu items.');
  }

  return menu;
};

const extractPaginatedMenus = (payload) => {
  const data = payload?.data ?? payload;
  const page = data?.page ?? data;
  const records = page?.content ?? page?.records ?? page?.items ?? page?.menus ?? [];
  const totalElements = page?.totalElements ?? page?.totalRecords ?? page?.total ?? records.length;

  if (!Array.isArray(records)) {
    throw new Error('The menu list API did not return a valid collection.');
  }

  return { records, totalElements: Number(totalElements) || 0 };
};

const normalizeModule = (module) => ({
  ...module,
  id: module?.id ?? null,
  name: module?.name ?? module?.moduleName ?? '',
  moduleUiName: module?.moduleUiName ?? '',
  parentId: module?.parentId ?? null,
  url: module?.url ?? '',
});

const extractModuleDetail = (payload) => {
  const data = payload?.data ?? payload;
  const detail = data?.moduleDetailDTO ?? data?.moduleDetail ?? data?.module ?? data;

  if (!detail || typeof detail !== 'object' || detail.id == null) {
    throw new Error('The module detail API did not return a valid module.');
  }

  const detailAccess = Array.isArray(detail.moduleAccess) ? detail.moduleAccess : null;
  const rootAccess = Array.isArray(data?.moduleAccess) ? data.moduleAccess : null;
  const moduleAccess = detailAccess ?? rootAccess ?? [];
  const parentModules = data?.parentModule ?? data?.parentModules ?? [];

  return {
    module: {
      ...normalizeModule(detail),
      moduleAccess: moduleAccess.map((access) => {
        const normalizedAccess = typeof access === 'string' ? { name: access } : access;
        const roles = Array.isArray(normalizedAccess?.roles)
          ? normalizedAccess.roles
          : [];

        return {
          id: normalizedAccess?.id ?? null,
          name: normalizedAccess?.name ?? '',
          moduleId: normalizedAccess?.moduleId ?? detail.id,
          roles,
          roleIds: Array.from(new Set([
            ...(Array.isArray(normalizedAccess?.roleIds) ? normalizedAccess.roleIds : []),
            ...roles.map((role) => role?.id),
          ].filter((id) => id != null))),
        };
      }),
    },
    parentModules: Array.isArray(parentModules)
      ? parentModules.map(normalizeModule)
      : [],
  };
};

const corsConfig = {
  headers: {
    'Content-Type': 'application/json',
  },
};

export const menuService = {
  async getMenu() {
    const response = await apiClient.get(menuEndpoint, corsConfig);
    return extractMenuItems(response.data);
  },

  async getManagedMenus(request) {
    const response = await apiClient.post(menuListEndpoint, request, corsConfig);
    return extractPaginatedMenus(response.data);
  },

  async getManagedMenu(id) {
    const response = await apiClient.get(`${menuUpdateEndpoint}/${id}`, corsConfig);
    return extractModuleDetail(response.data);
  },

  async searchParentMenus(request) {
    const response = await apiClient.post(menuListEndpoint, request, corsConfig);
    return extractPaginatedMenus(response.data);
  },

  async deleteMenu(id) {
    const response = await apiClient.delete(`${menuDeleteEndpoint}/${id}`, corsConfig);
    const message = response.data?.message;

    // Some backend delete failures are returned with HTTP 200 and a failure message.
    if (
      response.data?.success === false
      || (typeof message === 'string' && /\b(fail(?:ed|ure)?|unable|cannot|can't)\b/i.test(message))
    ) {
      throw new Error(message || 'Unable to delete the menu.');
    }

    return {
      id,
      message: message || 'Menu deleted successfully.',
    };
  },

  async updateMenu(id, changes) {
    const response = await apiClient.put(`${menuUpdateEndpoint}/${id}`, changes, corsConfig);
    const data = response.data?.data ?? response.data;
    const updatedMenu = data?.menu ?? data;
    return {
      id,
      ...changes,
      ...(updatedMenu && typeof updatedMenu === 'object' ? updatedMenu : {}),
    };
  },
};
