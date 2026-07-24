package com.agileflow.core.security;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresOrgRole {
    String[] value(); // Array of roles allowed, e.g. {"ADMIN", "OWNER"}
}
