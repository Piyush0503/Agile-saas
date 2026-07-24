-- Enable RLS on Tenant-specific tables (SaaS Core is global, but some tables might need RLS too, depending on use cases. For now, we will isolate PM tables).

ALTER TABLE projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE epics ENABLE ROW LEVEL SECURITY;
ALTER TABLE sprints ENABLE ROW LEVEL SECURITY;
ALTER TABLE issues ENABLE ROW LEVEL SECURITY;
ALTER TABLE issue_comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE issue_attachments ENABLE ROW LEVEL SECURITY;
ALTER TABLE issue_activity ENABLE ROW LEVEL SECURITY;
ALTER TABLE issue_labels ENABLE ROW LEVEL SECURITY;
ALTER TABLE issue_label_mapping ENABLE ROW LEVEL SECURITY;
ALTER TABLE issue_watchers ENABLE ROW LEVEL SECURITY;
ALTER TABLE sprint_velocity_snapshots ENABLE ROW LEVEL SECURITY;

-- Note: RLS policies usually depend on the current user or tenant context set by the application.
-- Spring Boot can set a session variable before executing queries. E.g. SET app.current_tenant = 'uuid';

-- Projects Policy: Users can only see projects if they belong to the organization
CREATE POLICY project_isolation_policy ON projects
    FOR ALL
    USING (org_id = current_setting('app.current_tenant')::uuid);

-- Epics Policy: Users can only see epics for projects in their organization
CREATE POLICY epic_isolation_policy ON epics
    FOR ALL
    USING (project_id IN (
        SELECT id FROM projects WHERE org_id = current_setting('app.current_tenant')::uuid
    ));

-- Sprints Policy: Users can only see sprints for projects in their organization
CREATE POLICY sprint_isolation_policy ON sprints
    FOR ALL
    USING (project_id IN (
        SELECT id FROM projects WHERE org_id = current_setting('app.current_tenant')::uuid
    ));

-- Issues Policy: Users can only see issues for projects in their organization
CREATE POLICY issue_isolation_policy ON issues
    FOR ALL
    USING (project_id IN (
        SELECT id FROM projects WHERE org_id = current_setting('app.current_tenant')::uuid
    ));

-- Issue Comments Policy
CREATE POLICY issue_comment_isolation_policy ON issue_comments
    FOR ALL
    USING (issue_id IN (
        SELECT id FROM issues WHERE project_id IN (
            SELECT id FROM projects WHERE org_id = current_setting('app.current_tenant')::uuid
        )
    ));

-- Note: These policies assume the application will always set 'app.current_tenant' before queries.
-- For super-admin queries, we might bypass RLS (e.g. BYPASSRLS on role).
