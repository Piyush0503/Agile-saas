-- Regular Indexes
CREATE INDEX idx_subscriptions_org_id ON subscriptions(org_id);
CREATE INDEX idx_org_members_org_id ON org_members(org_id);
CREATE INDEX idx_org_members_user_id ON org_members(user_id);

CREATE INDEX idx_projects_org_id ON projects(org_id);
CREATE INDEX idx_epics_project_id ON epics(project_id);
CREATE INDEX idx_sprints_project_id ON sprints(project_id);

CREATE INDEX idx_issues_project_id ON issues(project_id);
CREATE INDEX idx_issues_sprint_id ON issues(sprint_id);
CREATE INDEX idx_issues_assignee_id ON issues(assignee_id);
CREATE INDEX idx_issues_status ON issues(status);
CREATE INDEX idx_issues_type ON issues(type);
CREATE INDEX idx_issues_priority ON issues(priority);

CREATE INDEX idx_issue_comments_issue_id ON issue_comments(issue_id);
CREATE INDEX idx_issue_activity_issue_id ON issue_activity(issue_id);

-- Full Text Search Index for Issues
ALTER TABLE issues ADD COLUMN fts_document tsvector GENERATED ALWAYS AS (
    setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
    setweight(to_tsvector('english', coalesce(description, '')), 'B')
) STORED;

CREATE INDEX idx_issues_fts ON issues USING GIN (fts_document);

-- Index on JSONB custom_fields for querying
CREATE INDEX idx_issues_custom_fields ON issues USING GIN (custom_fields);
