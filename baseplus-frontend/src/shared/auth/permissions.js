import { getJwtPermissions, getJwtRoles } from './jwt.js';

export const PERMISSIONS = {
  DASHBOARD_VIEW: 'DASHBOARD_VIEW',
  USERS_VIEW: 'USERS_VIEW',
  USERS_CREATE: 'USERS_CREATE',
  USERS_EDIT: 'USERS_EDIT',
  USERS_DELETE: 'USERS_DELETE',
  USERS_RESET_PASSWORD: 'USERS_RESET_PASSWORD',
  ROLES_VIEW: 'ROLES_VIEW',
  ROLES_CREATE: 'ROLES_CREATE',
  ROLES_EDIT: 'ROLES_EDIT',
  ROLES_DELETE: 'ROLES_DELETE',
  ROLES_MANAGE_PERMISSIONS: 'ROLES_MANAGE_PERMISSIONS',
  ROLES_MANAGE_USERS: 'ROLES_MANAGE_USERS',
  ROLES_MANAGE_ORGANIZATION_SCOPES: 'ROLES_MANAGE_ORGANIZATION_SCOPES',
  PERMISSIONS_VIEW: 'PERMISSIONS_VIEW',
  PERMISSIONS_CREATE: 'PERMISSIONS_CREATE',
  PERMISSIONS_EDIT: 'PERMISSIONS_EDIT',
  PERMISSIONS_DELETE: 'PERMISSIONS_DELETE',
  BRANDING_VIEW: 'BRANDING_VIEW',
  BRANDING_EDIT: 'BRANDING_EDIT',
  BRANDING_UPLOAD_ASSETS: 'BRANDING_UPLOAD_ASSETS',
  ORGANIZATION_UNITS_VIEW: 'ORGANIZATION_UNITS_VIEW',
  ORGANIZATION_UNITS_CREATE: 'ORGANIZATION_UNITS_CREATE',
  ORGANIZATION_UNITS_EDIT: 'ORGANIZATION_UNITS_EDIT',
  ORGANIZATION_UNITS_DELETE: 'ORGANIZATION_UNITS_DELETE',
  REGISTROS_VIEW: 'REGISTROS_VIEW',
  REGISTROS_DETAIL: 'REGISTROS_DETAIL',
  AUDIT_VIEW: 'AUDIT_VIEW',
  AUDIT_EXPORT: 'AUDIT_EXPORT',
};

const PERMISSION_DISPLAY = {
  REGISTROS_VIEW: {
    description: 'Permite visualizar a lista de registros do Canal de Ética.',
    group: 'Registros do Canal de Ética',
  },
  REGISTROS_DETAIL: {
    description: 'Permite visualizar os detalhes e anexos de um registro do Canal de Ética.',
    group: 'Registros do Canal de Ética',
  },
};

export function getPermissionDisplayDescription(permission) {
  return PERMISSION_DISPLAY[permission?.name]?.description ?? permission?.description ?? '';
}

export function getPermissionDisplayGroup(permissionName) {
  if (!permissionName) {
    return 'GERAL';
  }

  return PERMISSION_DISPLAY[permissionName]?.group ?? permissionName.split('_')[0] ?? 'GERAL';
}

export function getAuthRoles({ user, accessToken } = {}) {
  if (Array.isArray(user?.roles)) {
    return user.roles;
  }

  return getJwtRoles(accessToken);
}

export function getAuthPermissions({ user, accessToken } = {}) {
  if (Array.isArray(user?.permissions)) {
    return user.permissions;
  }

  return getJwtPermissions(accessToken);
}

export function hasRole(auth, role) {
  if (!role) {
    return false;
  }

  return getAuthRoles(auth).includes(role);
}

export function hasPermission(auth, permission) {
  if (!permission) {
    return false;
  }

  return getAuthPermissions(auth).includes(permission);
}

export function canAny(auth, permissions = []) {
  if (!permissions.length) {
    return true;
  }

  return permissions.some((permission) => hasPermission(auth, permission));
}
