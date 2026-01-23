# Changelog

> **Purpose**: Track features, bug fixes, and decisions for context continuity.
> **Format**: Newest entries at top. Update after each significant change.

---

## 2026-01-23

### Frontend: Compatibility Updates for Backend Changes

**Added 409 CONFLICT handling:**
- `light-client.ts` - Added 409 status code handling
- `error-messages.ts` - Added 409 status code handling
- Added `isConflictError()` helper function
- Added `isValidationError()` helper function
- Updated `getErrorTitle()` to return appropriate titles for conflict/validation errors

**Added X-Correlation-ID support:**
- `light-client.ts` - Generates and sends correlation ID header
- `api-client.ts` - Generates and sends correlation ID header
- Enables end-to-end request tracing with backend logs

**Updated error handling documentation:**
- `ERROR_HANDLING_GUIDE.md` - Added new error type helpers

**Files Modified (Frontend):**
- `src/lib/client/light-client.ts`
- `src/lib/server/api-client.ts`
- `src/utils/error-messages.ts`
- `ERROR_HANDLING_GUIDE.md`

---

### Cleanup: Phase 6 Code Cleanup Complete

**Removed dead commented code:**
- `Organization.java` - Removed old equals/hashCode (now handled by Lombok)
- `UserOrganizationRole.java` - Removed old equals/hashCode (now handled by Lombok)
- `OrganizationRepository.java` - Removed commented User queries
- `UserAssignmentRepository.java` - Removed commented User queries
- `PosServiceImpl.java` - Cleaned up unused imports and placeholder code
- `RateLimitConfig.java` - Cleaned up commented code, added TODO note
- `SecurityConfig.java` - Removed stale TODO comment

**Removed unused imports:**
- `Organization.java` - Removed HibernateProxy import
- `UserOrganizationRole.java` - Removed Objects import

**Files Modified:**
- `src/main/java/com/store/mgmt/organization/model/entity/Organization.java`
- `src/main/java/com/store/mgmt/organization/model/entity/UserOrganizationRole.java`
- `src/main/java/com/store/mgmt/organization/repository/OrganizationRepository.java`
- `src/main/java/com/store/mgmt/organization/repository/UserAssignmentRepository.java`
- `src/main/java/com/store/mgmt/pos/service/PosServiceImpl.java`
- `src/main/java/com/store/mgmt/config/security/RateLimitConfig.java`
- `src/main/java/com/store/mgmt/config/security/SecurityConfig.java`

---

### Infrastructure: Phase 5 Production Infrastructure Complete

**Added Correlation ID Filter:**
- New `CorrelationIdFilter` for distributed request tracing
- Generates UUID if `X-Correlation-ID` header not provided
- Adds correlation ID to MDC for logging
- Returns correlation ID in response header

**Added Request Logging:**
- New `RequestLoggingConfig` with `CommonsRequestLoggingFilter`
- Enabled via `logging.request.enabled=true`
- Logs request details (query string, payload, client info)
- Headers excluded to prevent auth token exposure

**Added Startup Validation:**
- New `ApplicationStartupValidator` component
- Validates JWT secret configuration
- Validates FRONTEND_URL for CORS
- Tests database connectivity on startup
- Fails startup in production if critical configs missing
- Warns about dev secrets used in non-dev profiles

**Added Logback Configuration:**
- New `logback-spring.xml` with correlation ID in log pattern
- Profile-specific logging levels (dev=DEBUG, prod=WARN)
- Rolling file appender for production (100MB files, 30 days retention)

**Added Graceful Shutdown:**
- `server.shutdown=graceful` for clean shutdown
- 30 second timeout for in-flight requests

**Added Response Compression:**
- Enabled GZIP compression for JSON/XML/HTML
- Minimum response size 1024 bytes

**Updated CORS Configuration:**
- Added `X-Correlation-ID` to allowed/exposed headers

**Updated Actuator:**
- Added `/actuator/health/**` and `/actuator/info` to public endpoints
- Enabled Kubernetes-style health probes

**Files Created:**
- `src/main/java/com/store/mgmt/config/CorrelationIdFilter.java`
- `src/main/java/com/store/mgmt/config/RequestLoggingConfig.java`
- `src/main/java/com/store/mgmt/config/ApplicationStartupValidator.java`
- `src/main/resources/logback-spring.xml`

**Files Modified:**
- `src/main/java/com/store/mgmt/config/security/SecurityConfig.java`
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`

---

### Performance: Phase 4 Database & Performance Complete

**Fixed N+1 query potential:**
- Changed `User.organizationRoles` from `FetchType.EAGER` to `FetchType.LAZY`
- Repositories already have optimized JOIN FETCH queries (`findByUsernameWithAllRelatedData`, etc.)

**Added database indexes:**
- `stores` table:
  - `idx_store_organization` on `organization_id`
  - `idx_store_status` on `status`
- `user_organization_roles` table:
  - `idx_uor_user` on `user_id`
  - `idx_uor_organization` on `organization_id`
  - `idx_uor_store` on `store_id`
  - `idx_uor_role` on `role_id`
  - `idx_uor_user_org` on `user_id, organization_id` (composite)

**Verified existing optimizations:**
- `@Version` in BaseEntity for optimistic locking ✓
- `open-in-view=false` to prevent lazy loading in views ✓
- HikariCP connection pool configured ✓
- Inventory entities already have comprehensive indexes ✓

**Files Modified:**
- `src/main/java/com/store/mgmt/users/model/entity/User.java`
- `src/main/java/com/store/mgmt/organization/model/entity/Store.java`
- `src/main/java/com/store/mgmt/organization/model/entity/UserOrganizationRole.java`

---

### Review: Phase 1-3 Gap Fixes

**Added `@Slf4j` to controllers missing logging:**
- `UserController`
- `RoleController`
- `InventoryController`
- `GlobalTemplateController`
- `PosController`

**Added `@Slf4j` to services missing logging:**
- `RoleServiceImpl`

**Fixed RuntimeException throws:**
- `AuditLogServiceImpl._persistAuditLog()` - Now logs error but doesn't throw
- `AuthServiceImpl.logAuditEntry()` - Now logs error but doesn't throw
- `OrganizationServiceImpl.logAuditEntry()` - Now logs error but doesn't throw
- `EmailService.sendInvitationEmail()` - Changed to `ResponseStatusException`

**Fixed EmailService bugs:**
- Fixed `@Value("${FRONTEND_URL")` → `@Value("${FRONTEND_URL}")`
- Added default for `spring.mail.from`

**Fixed HTTP status codes:**
- `RoleServiceImpl.createRole()` - Changed "already exists" from BAD_REQUEST to CONFLICT
- `RoleServiceImpl.updateRole()` - Changed "already exists" from BAD_REQUEST to CONFLICT

**Removed redundant validation:**
- `RoleServiceImpl.createRole()` - Removed null check (handled by @Valid)

**Files Modified:**
- `src/main/java/com/store/mgmt/users/controller/UserController.java`
- `src/main/java/com/store/mgmt/users/controller/RoleController.java`
- `src/main/java/com/store/mgmt/inventory/controller/InventoryController.java`
- `src/main/java/com/store/mgmt/globaltemplates/controller/GlobalTemplateController.java`
- `src/main/java/com/store/mgmt/pos/controller/PosController.java`
- `src/main/java/com/store/mgmt/users/service/RoleServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/AuditLogServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/EmailService.java`
- `src/main/java/com/store/mgmt/auth/service/AuthServiceImpl.java`
- `src/main/java/com/store/mgmt/organization/service/OrganizationServiceImpl.java`

---

### Validation: Phase 3 Input Validation Complete

**Added validation annotations to DTOs:**
- `CreateUserDTO` - @NotBlank, @Email, @Size for email and name fields
- `CreateOrganizationDTO` - @NotBlank, @Size for name, description, contactInfo
- `CreateStoreDTO` - @NotNull for organizationId, @NotBlank for name/location, @Size limits
- `InviteUserDTO` - @NotBlank, @Email for email, @NotNull for organizationId, @NotBlank for roleName
- `CreateUserAssignmentDTO` - @NotNull for organizationId, userId, roleId
- `RemoveUserAssignmentDTO` - @NotNull for organizationId, userId
- `CreateTenantDTO` - @NotNull for organizationId
- `RoleDTO` - @NotBlank, @Size for name and description

**Cleaned up redundant validation:**
- Removed manual null checks from `UserServiceImpl.createUser()` (handled by @NotBlank)
- Removed manual null checks from `OrganizationServiceImpl.createOrganization()` (handled by @NotBlank)
- Changed duplicate check errors from BAD_REQUEST to CONFLICT (proper HTTP semantics)

**Files Modified:**
- `src/main/java/com/store/mgmt/users/model/dto/CreateUserDTO.java`
- `src/main/java/com/store/mgmt/users/model/dto/RoleDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateOrganizationDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateStoreDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/InviteUserDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateUserAssignmentDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/RemoveUserAssignmentDTO.java`
- `src/main/java/com/store/mgmt/organization/model/dto/CreateTenantDTO.java`
- `src/main/java/com/store/mgmt/users/service/UserServiceImpl.java`
- `src/main/java/com/store/mgmt/organization/service/OrganizationServiceImpl.java`

---

### Code Quality: Phase 2 Architecture Improvements Complete

**Added missing `@Valid` annotations:**
- `OrganizationController.createOrganization()`
- `UserController.createUser()`, `updateUser()`, `inviteUser()`
- `UserController.assignUserToOrganization()`, `assignUserToStore()`
- `UserController.removeUserFromOrganization()`, `removeUserFromStore()`
- `RoleController.createRole()`, `updateRole()`

**Enhanced GlobalExceptionHandler:**
- Added `SecurityException` handler for authorization failures
- Added `IllegalStateException` handler for internal state errors

**Created shared AuthorizationService:**
- New `com.store.mgmt.common.service.AuthorizationService`
- Centralized authorization logic: `hasRole()`, `hasRoleInOrganization()`, `lacksRole()`, `lacksRoleInOrganization()`
- Added `requireRole()` and `requireRoleInOrganization()` for cleaner authorization checks
- Fixed bug in `StoreServiceImpl.hasRole()` that used `noneMatch` instead of `anyMatch`

**Refactored services to use AuthorizationService:**
- `StoreServiceImpl` - Removed private authorization methods, uses shared service
- `UserServiceImpl` - Uses shared service for authorization checks

**Files Modified:**
- `src/main/java/com/store/mgmt/common/service/AuthorizationService.java` (new)
- `src/main/java/com/store/mgmt/common/exception/GlobalExceptionHandler.java`
- `src/main/java/com/store/mgmt/organization/controller/OrganizationController.java`
- `src/main/java/com/store/mgmt/organization/service/StoreServiceImpl.java`
- `src/main/java/com/store/mgmt/users/controller/UserController.java`
- `src/main/java/com/store/mgmt/users/controller/RoleController.java`
- `src/main/java/com/store/mgmt/users/service/UserServiceImpl.java`

---

### Security: Phase 1 Critical Fixes Complete
- **Removed all `System.out.println` and `printStackTrace` calls** from application code
  - `SecurityConfig.java` - Replaced with SLF4J logger
  - `AuthController.java` - Replaced debug output with proper logging
  - `OrganizationController.java` - Removed debug code that read raw request body
  - `StoreController.java` - Removed debug code, added proper logging
  - `OrganizationServiceImpl.java` - Added @Slf4j, proper logging
  - `StoreServiceImpl.java` - Replaced with logger
  - `UserServiceImpl.java` - Replaced with logger
  - `InventoryServiceImpl.java` - Replaced with logger
  - `AuditLogServiceImpl.java` - Replaced with logger
  - Note: `Seeder.java` retains System.out (CLI utility, acceptable)

- **Removed hardcoded credentials** from `SecurityConfig.java`
  - Removed hardcoded email credentials (prasubd@gmail.com)
  - Mail configuration now loaded from environment variables
  - Added validation to ensure mail credentials are set when needed

- **Environment variable requirements enforced**
  - `application.properties` - Removed default credentials, requires env vars
  - `application-dev.properties` - Safe defaults for local development only
  - `application-prod.properties` - No defaults, all sensitive values via env vars

- **Fixed CORS configuration**
  - Replaced wildcard `*` in allowed-headers with explicit list
  - Headers: `Authorization,Content-Type,Accept,X-Requested-With,X-Store-Id,X-Organization-Id`

- **Created shared SecretKey bean** in SecurityConfig
  - Eliminates duplicate key creation for JWT encoder/decoder
  - Single source of truth for signing key

**Files Modified:**
- `src/main/java/com/store/mgmt/config/security/SecurityConfig.java`
- `src/main/java/com/store/mgmt/auth/controller/AuthController.java`
- `src/main/java/com/store/mgmt/organization/controller/OrganizationController.java`
- `src/main/java/com/store/mgmt/organization/controller/StoreController.java`
- `src/main/java/com/store/mgmt/organization/service/OrganizationServiceImpl.java`
- `src/main/java/com/store/mgmt/organization/service/StoreServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/UserServiceImpl.java`
- `src/main/java/com/store/mgmt/inventory/service/InventoryServiceImpl.java`
- `src/main/java/com/store/mgmt/users/service/AuditLogServiceImpl.java`
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`

---

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
