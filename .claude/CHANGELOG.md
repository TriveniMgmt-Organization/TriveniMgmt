# Changelog

> **Purpose**: Track features, bug fixes, and decisions for context continuity.
> **Format**: Newest entries at top. Update after each significant change.

---

## 2026-01-23

### Documentation Moved to Backend Repo
- Moved `.claude/` and `CLAUDE.md` from parent folder into `backend/` repo
- Documentation is now version-controlled with the backend
- Frontend is a sibling repo at `../frontend/`

### Project Restructure
- Renamed `TriveniMgmt/` → `backend/`
- Renamed `triveni-mgmt-client/` → `frontend/`
- Updated all documentation to reflect new paths
- Git remotes unchanged (still point to original GitHub repo names)

**Note**: GitHub repos still named `TriveniMgmt` and `triveni-mgmt-client`. Local folders renamed for clarity.

### Documentation Created
- Created `.claude/` documentation structure for efficient context loading
- Generated comprehensive documentation for backend, frontend, API, database, and permissions
- Established changelog for tracking future changes

### Project State
- **Backend**: Spring Boot 3.5.3, Java 21, PostgreSQL 16
- **Frontend**: Next.js 15, React 19, MUI 5
- **Auth**: JWT with HttpOnly cookies
- **Multi-tenancy**: Organization → Store hierarchy
- **Seeded Users**: admin (SUPER_ADMIN), manager (STORE_MANAGER)

### Known Issues
- None documented yet

### Architecture Decisions
- Using Orval for API client generation from OpenAPI spec
- MapStruct for entity-DTO mapping in backend
- React Query for server state management
- Context API for client-side state (Auth, Navigation, Notifications)

---

## Template for New Entries

```markdown
## YYYY-MM-DD

### Feature: [Feature Name]
- Description of what was added
- Key files modified:
  - `path/to/file.java`
  - `path/to/component.tsx`
- Notes or decisions made

### Bug Fix: [Issue Description]
- Root cause
- Solution implemented
- Files changed

### Refactor: [Area]
- What was refactored and why
- Breaking changes (if any)

### Decision: [Topic]
- Context
- Options considered
- Decision made and rationale
```

---

## How to Use This Changelog

1. **After completing a feature**: Add an entry describing what was built
2. **After fixing a bug**: Document the issue and solution
3. **After making decisions**: Record the context and rationale
4. **Before starting work**: Read recent entries for context

This helps Claude understand recent changes without re-reading the entire codebase.
