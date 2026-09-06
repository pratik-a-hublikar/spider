export const MODULE_NAMES = Object.freeze({
  MENUS: 'Menus',
  ROLES: 'Roles',
  USERS: 'User',
  ORGANIZATIONS: 'Organization Location',
  LOCATIONS: 'User Location',
});

export const ACCESS_TYPES = Object.freeze({
  CREATE: 'create',
  WRITE: 'write',
  READ: 'read',
  UPDATE: 'update',
  DELETE: 'delete',
});

export const FILTER_OPERATORS = Object.freeze({
  CONTAINS: 'like',
  EQUAL: '=',
  NOT_EQUAL: '!=',
  IN: 'in',
  GREATER_THAN: '>',
  LESS_THAN: '<',
  GREATER_THAN_EQUAL: '>=',
  LESS_THAN_EQUAL: '<=',
  TRUE: 'is_true',
  FALSE: 'is_false',
  NULL: 'is_null',
  NOT_NULL: 'is_not_null',
  CONTAINS_OR_EQUAL: 'likeOrEqual',
  BETWEEN: 'between',
});
