package com.agileflow.core.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Aspect
@Component
public class RequiresOrgRoleAspect {

    @Before("@annotation(requiresOrgRole)")
    public void checkOrgRole(JoinPoint joinPoint, RequiresOrgRole requiresOrgRole) {
        // Current user
        UserPrincipal user = SecurityUtils.getCurrentUser()
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));

        String currentOrgSlug = OrgContextHolder.getOrgSlug();
        if (currentOrgSlug == null) {
            throw new AccessDeniedException("No organization context found for this request");
        }

        List<String> allowedRoles = Arrays.asList(requiresOrgRole.value());
        
        // Check if user has the role
        boolean hasRole = user.getAuthorities().stream()
                .anyMatch(auth -> allowedRoles.contains(auth.getAuthority().replace("ROLE_", "")));

        if (!hasRole) {
            throw new AccessDeniedException("User does not have the required role(s): " + allowedRoles);
        }
    }
}
