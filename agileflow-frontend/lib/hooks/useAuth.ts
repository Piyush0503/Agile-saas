'use client';

import { useSession } from 'next-auth/react';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

/**
 * Hook to get the current authenticated user and their session data.
 */
export function useAuth() {
  const { data: session, status } = useSession();
  
  return {
    user: session?.user,
    isAuthenticated: status === 'authenticated',
    isLoading: status === 'loading',
    roles: session?.user?.roles || [],
    orgSlug: session?.user?.orgSlug,
    orgId: session?.user?.orgId,
    accessToken: session?.user?.accessToken,
  };
}

/**
 * Hook that redirects unauthenticated users to the login page.
 */
export function useRequireAuth(redirectTo: string = '/login') {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push(redirectTo);
    }
  }, [isLoading, isAuthenticated, router, redirectTo]);

  return { isAuthenticated, isLoading };
}

/**
 * Hook to check if the current user has a specific role/permission 
 * within the current organization context.
 */
export function useOrgPermission(requiredRoles: string | string[]) {
  const { roles, isAuthenticated, isLoading } = useAuth();

  if (isLoading || !isAuthenticated) {
    return { hasPermission: false, isLoading };
  }

  const rolesToCheck = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
  
  // Example checking if any of the user's roles match the required roles
  // In a real scenario, this might check against specific permission strings like 'project:create'
  const hasPermission = rolesToCheck.some(role => roles.includes(role));

  return { hasPermission, isLoading };
}
