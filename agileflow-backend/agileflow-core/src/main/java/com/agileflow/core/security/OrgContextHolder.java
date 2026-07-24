package com.agileflow.core.security;

import java.util.UUID;

public class OrgContextHolder {

    private static final ThreadLocal<String> currentOrgSlug = new ThreadLocal<>();
    private static final ThreadLocal<UUID> currentOrgId = new ThreadLocal<>();

    public static void setOrgSlug(String orgSlug) {
        currentOrgSlug.set(orgSlug);
    }

    public static String getOrgSlug() {
        return currentOrgSlug.get();
    }

    public static void setOrgId(UUID orgId) {
        currentOrgId.set(orgId);
    }

    public static UUID getOrgId() {
        return currentOrgId.get();
    }

    public static void clear() {
        currentOrgSlug.remove();
        currentOrgId.remove();
    }
}
