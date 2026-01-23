# TriveniMgmt - Claude Code Instructions

## Project Type
Multi-tenant SaaS Inventory Management System

## Quick Start Context

**Read `.claude/CONTEXT.md` first** - It contains the essential project overview.

## Repository Structure

This is the **backend** repository. The frontend is a sibling repo at `../frontend/`.

```
parent-folder/
├── backend/           # This repo (Spring Boot)
│   ├── .claude/       # Documentation
│   ├── CLAUDE.md      # This file
│   └── src/
└── frontend/          # Sibling repo (Next.js)
    └── src/
```

## Documentation Files
| File | When to Read |
|------|--------------|
| `.claude/CONTEXT.md` | Always (entry point) |
| `.claude/CHANGELOG.md` | Check recent changes |
| `.claude/docs/BACKEND.md` | Backend work |
| `.claude/docs/FRONTEND.md` | Frontend work |
| `.claude/docs/API.md` | API changes |
| `.claude/docs/DATABASE.md` | Schema changes |
| `.claude/docs/PERMISSIONS.md` | RBAC changes |

## Key Commands

### Backend (this repo)
```bash
./gradlew bootRun          # Run backend
./gradlew test             # Run tests
./gradlew build            # Build JAR
```

### Frontend (sibling repo)
```bash
cd ../frontend
npm run dev                # Run dev server (port 3000)
npm run build              # Production build
npm run apigen             # Regenerate API client from OpenAPI
npm run lint               # Lint code
```

## After Making Changes

Update `.claude/CHANGELOG.md` with:
- Features added
- Bugs fixed
- Decisions made

This ensures context continuity for future sessions.

## Tech Stack Summary
- **Backend**: Spring Boot 3.5.3, Java 21, PostgreSQL 16, JWT auth
- **Frontend**: Next.js 15, React 19, MUI 5, React Query, TypeScript
- **API**: REST, OpenAPI spec at `/v3/api-docs`

## Default Users (Development)
| User | Password | Role |
|------|----------|------|
| admin@store.com | admin123 | SUPER_ADMIN |
| manager@store.com | manager123 | STORE_MANAGER |

## GitHub Repositories
- Backend: `TriveniMgmt-Organization/TriveniMgmt`
- Frontend: `TriveniMgmt-Organization/triveni-mgmt-client`
