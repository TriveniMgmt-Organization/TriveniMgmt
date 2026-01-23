# TriveniMgmt - Project Context for Claude

> **Auto-generated**: This file serves as the primary context entry point.
> **Last Updated**: 2026-01-23

## Quick Reference

| Component | Technology | Port |
|-----------|------------|------|
| Backend | Spring Boot 3.5.3, Java 21 | 8080 |
| Frontend | Next.js 15, React 19, MUI 5 | 3000 |
| Database | PostgreSQL 16 | 5432 |
| Auth | JWT (HttpOnly cookies) | - |

## Project Overview

Multi-tenant SaaS Inventory Management System where:
- **Organizations** are the top-level tenants
- **Stores** belong to Organizations
- **Users** are assigned to Organizations/Stores with specific Roles

## Directory Structure

**Note**: Docs live in backend repo. Frontend is a sibling repo.

```
parent-folder/
├── backend/                        # THIS REPO (Spring Boot)
│   ├── .claude/                    # Documentation (this folder)
│   │   ├── CONTEXT.md              # This file
│   │   ├── CHANGELOG.md            # Feature/bug changelog
│   │   └── docs/                   # Detailed documentation
│   ├── CLAUDE.md                   # Entry point for Claude
│   └── src/main/java/com/store/mgmt/
│       ├── auth/                   # Authentication (JWT, login, register)
│       ├── users/                  # User, Role, Permission management
│       ├── organization/           # Organization, Store, UserOrgRole
│       ├── inventory/              # Products, Stock, PO, Sales
│       ├── pos/                    # Point of Sale
│       ├── config/                 # Security, DataSeeder, Audit
│       └── common/                 # BaseEntity, Exceptions
└── frontend/                       # SIBLING REPO (Next.js) at ../frontend
    └── src/
        ├── app/                    # Next.js App Router pages
        ├── components/             # UI components
        ├── contexts/               # Auth, Navigation, Notification
        ├── api/generated/          # Orval-generated API client
        └── lib/                    # Utilities, theme, hooks
```

**GitHub Repos**:
- Backend: `TriveniMgmt-Organization/TriveniMgmt`
- Frontend: `TriveniMgmt-Organization/triveni-mgmt-client`

## Key Files to Know

### Backend Critical Files
| File | Purpose |
|------|---------|
| `config/DataSeeder.java` | Seeds permissions, roles, default users |
| `config/security/SecurityConfig.java` | JWT, CORS, endpoint security |
| `auth/service/JWTService.java` | Token generation/validation |
| `common/model/BaseEntity.java` | Audit fields (createdAt, updatedAt, etc.) |
| `organization/model/entity/UserOrganizationRole.java` | User-Org-Role mapping |

### Frontend Critical Files
| File | Purpose |
|------|---------|
| `contexts/AuthContext.tsx` | Auth state, login/logout, permissions |
| `contexts/NavigationContext.tsx` | Scope switching (org/store level) |
| `lib/server/api-client.ts` | Axios with interceptors, token refresh |
| `components/auth/Gatekeeper.tsx` | Permission-based rendering |

## Multi-Tenancy Model

```
Organization (Tenant)
├── Users (with ORG_ADMIN or SUPER_ADMIN role)
├── Stores
│   └── Users (with STORE_MANAGER role)
├── Product Catalog (Categories, Brands, Templates)
└── Policies (Discounts, Pricing)
```

### Scope-Based Access
- **Organization Level**: SUPER_ADMIN, ORG_ADMIN can access org settings, all stores
- **Store Level**: STORE_MANAGER can only access assigned store(s)

## Authentication Flow

1. User logs in → Backend returns JWT in HttpOnly cookies
2. Frontend calls `/api/me` to get user info
3. User selects tenant (if multiple orgs) → `/api/select-tenant`
4. `X-Store-Id` header sent with store-scoped requests

## Common Patterns

### Backend
- All entities extend `BaseEntity` (UUID id, audit fields, soft delete)
- MapStruct for entity↔DTO mapping
- `@PreAuthorize` for permission checks
- Repositories use JPA with custom JPQL queries

### Frontend
- App Router with route groups: `(auth)`, `(dashboard)`
- React Query for server state, Context for client state
- Form handling with React Hook Form + Yup validation
- `AppFormDialog` pattern for CRUD modals

## Related Documentation

- [Backend Details](./docs/BACKEND.md) - Services, repositories, DTOs
- [Frontend Details](./docs/FRONTEND.md) - Components, hooks, state
- [API Reference](./docs/API.md) - All endpoints with auth requirements
- [Database Schema](./docs/DATABASE.md) - Entity relationships
- [Permission System](./docs/PERMISSIONS.md) - Roles and permissions
- [Changelog](./CHANGELOG.md) - Recent changes

---

## Instructions for Claude

When working on this project:

1. **Read this file first** - It provides essential context
2. **Check CHANGELOG.md** - For recent changes and decisions
3. **Reference specific docs** - Based on the task area
4. **Update CHANGELOG.md** - After completing features/fixes

### Token Efficiency Tips
- This CONTEXT.md is the minimal context needed for most tasks
- Only read detailed docs when working in that specific area
- The generated API types in `api/generated/models/` are the source of truth
