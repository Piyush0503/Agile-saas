package com.agileflow.api.security;

import com.agileflow.core.security.OrgContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OrgTenantFilter extends OncePerRequestFilter {

    private static final Pattern ORG_PATH_PATTERN = Pattern.compile("^/api/orgs/([^/]+)(/.*)?$");

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        Matcher matcher = ORG_PATH_PATTERN.matcher(path);
        
        try {
            if (matcher.matches()) {
                String orgSlug = matcher.group(1);
                OrgContextHolder.setOrgSlug(orgSlug);
                // Note: orgId mapping would require a DB lookup here, 
                // or setting it if it matches the orgId in the JWT
            }
            
            filterChain.doFilter(request, response);
        } finally {
            OrgContextHolder.clear();
        }
    }
}
