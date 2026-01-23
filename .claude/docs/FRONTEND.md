# Frontend Architecture

> **Path**: `../frontend/src/` (sibling repo)

## Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Next.js | 15.3.5 | Framework (App Router) |
| React | 19.0.0 | UI Library |
| MUI | 5.15.20 | Component Library |
| TanStack Query | 5.83.0 | Server State |
| React Hook Form | 7.60.0 | Form State |
| Axios | 1.10.0 | HTTP Client |
| Orval | 7.10.0 | API Code Generation |
| Yup | 1.6.1 | Validation |

## Directory Structure

```
src/
├── app/                    # Next.js App Router
│   ├── (auth)/            # Auth pages (login, register)
│   ├── (dashboard)/       # Protected pages
│   └── api/               # API routes (login, logout, me)
├── components/
│   ├── ui/                # Reusable UI components
│   ├── layouts/           # Header, Sidebar, MainLayout
│   ├── auth/              # Gatekeeper (permission wrapper)
│   ├── forms/             # Domain-specific forms
│   └── common/            # NavBar, Footer, ErrorDisplay
├── contexts/
│   ├── AuthContext.tsx    # Auth state & methods
│   ├── NavigationContext.tsx  # Scope switching
│   └── NotificationContext.tsx  # Toast notifications
├── api/generated/         # Orval-generated API client
│   ├── client/            # React Query hooks
│   ├── server/            # Axios functions
│   └── models/            # TypeScript types
├── lib/
│   ├── server/api-client.ts   # Axios with interceptors
│   ├── client/light-client.ts # Client-side mutator
│   ├── theme/             # MUI theme config
│   └── hooks/             # Custom hooks
├── constants/
│   ├── routes/            # Route path constants
│   └── navigation.tsx     # Nav menu items
└── utils/                 # Helpers, validators
```

## Key Components

### UI Components (`components/ui/`)

| Component | Purpose |
|-----------|---------|
| `AppButton` | Styled MUI Button |
| `AppCard` | Card container |
| `AppTextField` | Form text input with validation |
| `AppSelect` | Dropdown with async support |
| `AppTable` | Data grid with pagination |
| `AppFormDialog` | Modal form wrapper |
| `AppConfirmDialog` | Confirmation modal |
| `AppFormProvider` | React Hook Form wrapper |

### Layout Components (`components/layouts/`)

| Component | Purpose |
|-----------|---------|
| `MainLayout` | Header + Sidebar + Content wrapper |
| `Header` | Top nav with user menu |
| `Sidebar` | Left nav with menu items |
| `ResponsiveDrawer` | Mobile/tablet drawer |

### Auth Components (`components/auth/`)

```tsx
// Gatekeeper - Permission-based rendering
<Gatekeeper permission="PRODUCT_WRITE">
  <EditButton />
</Gatekeeper>

<Gatekeeper role={["ORG_ADMIN", "SUPER_ADMIN"]}>
  <AdminPanel />
</Gatekeeper>
```

## Context Providers

### AuthContext (`contexts/AuthContext.tsx`)

```tsx
const {
  user,              // Current user object
  isAuthenticated,   // Boolean
  isAuthLoading,     // Loading state
  login,             // (credentials) => Promise
  logout,            // () => Promise
  selectTenant,      // (orgId) => Promise
  hasPermission,     // (permission) => boolean
  hasRole,           // (role) => boolean
} = useAuth();
```

### NavigationContext (`contexts/NavigationContext.tsx`)

```tsx
const {
  currentScope,      // 'organization' | 'store'
  activeStoreId,     // Current store UUID
  activeStore,       // Full store object
  availableStores,   // User's accessible stores
  navItems,          // Filtered nav items
  switchToOrganizationLevel,
  switchToStoreLevel,
  prefetchRoute,     // Prefetch route on hover for faster navigation
} = useNavigationContext();
```

### NotificationContext

```tsx
const { showNotification } = useNotification();
showNotification('Success!', 'success');
showNotification('Error occurred', 'error');
```

## API Integration

### Generated API Client

Located in `api/generated/`:
- **Server functions**: `api/generated/server/` - For SSR
- **Client hooks**: `api/generated/client/` - React Query hooks
- **Models**: `api/generated/models/` - TypeScript types

### API Client (`lib/server/api-client.ts`)

Features:
- Auto-retry with exponential backoff
- Token refresh on 401
- `X-Store-Id` header injection
- Request caching for GET requests

### Using Generated Hooks

```tsx
// Query
const { data: stores } = useGetStores();

// Mutation
const createStore = useCreateStore();
createStore.mutate({ name: 'New Store' });
```

### Regenerating API Client

```bash
cd triveni-mgmt-client
npm run apigen
```

Reads OpenAPI spec from `OPENAPI_URL` env var.

## Routing

### Route Groups
- `(auth)/` - Public auth pages
- `(dashboard)/` - Protected pages with layout

### Key Routes

| Route | Purpose | Access |
|-------|---------|--------|
| `/auth/login` | Login page | Public |
| `/organization` | Org dashboard | ORG_ADMIN+ |
| `/organization/stores` | Store list | ORG_ADMIN+ |
| `/store/[storeId]/dashboard` | Store dashboard | STORE_MANAGER+ |
| `/store/[storeId]/inventory` | Store inventory | STORE_MANAGER+ |
| `/inventory` | Global inventory | All auth users |
| `/sales` | Sales list | SALE_READ |
| `/purchase-orders` | PO list | PO_READ |

### Route Constants (`constants/routes/`)

```tsx
import { LogIn, Inventory, EditProduct } from '@/constants/routes';
// LogIn = '/auth/login'
// EditProduct('123') = '/inventory/products/123/edit'
```

## State Management

### Server State (React Query)
- All API data managed via generated hooks
- 5-minute stale time (data considered fresh)
- 30-minute garbage collection time (cache retention)
- Auto-refetch disabled on window focus
- Auto-refetch disabled on mount (if data exists)
- Single retry on failure (faster error handling)

### Client State (Context)
- Auth state in `AuthContext`
- Navigation state in `NavigationContext`
- UI notifications in `NotificationContext`

### Form State (React Hook Form)
- All forms use `AppFormProvider`
- Yup schemas for validation
- `@hookform/resolvers` for integration

## Styling

### Theme (`lib/theme/`)

```tsx
// Color palette
primary: blue-sky shades
success: green shades
warning: amber shades
error: red shades

// Typography
fontFamily: 'Inter, Roboto, ...'
h1-h6: fontWeight 600-700

// Shape
borderRadius: 8px
```

### Styling Patterns

```tsx
// Inline sx (preferred)
<Box sx={{ mt: 2, p: { xs: 2, md: 4 } }} />

// Styled components
const StyledCard = styled(AppCard)(({ theme }) => ({
  padding: theme.spacing(4),
}));
```

## Custom Hooks

| Hook | Purpose |
|------|---------|
| `useAuth()` | Auth context access |
| `useNotification()` | Toast notifications |
| `useNavigationContext()` | Navigation state |
| `useDashboardData()` | Dashboard stats |
| `useErrorHandler()` | Error handling |
| `useDebounce()` | Input debouncing |

## Environment Variables

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_API_URL=http://localhost:8080
OPENAPI_URL=http://localhost:8080/v3/api-docs
NEXT_PUBLIC_DEMO_MODE=false
```

## Common Patterns

### Page with Data Fetching

```tsx
export default function ProductsPage() {
  const { data, isLoading } = useGetProducts();

  if (isLoading) return <CircularProgress />;

  return <AppTable data={data} columns={columns} />;
}
```

### Form Dialog

```tsx
<AppFormDialog
  open={open}
  title="Create Product"
  onSubmit={handleSubmit}
  onClose={() => setOpen(false)}
>
  <AppTextField name="name" label="Name" />
  <AppSelect name="category" options={categories} />
</AppFormDialog>
```

### Permission Check

```tsx
const { hasPermission } = useAuth();

{hasPermission('PRODUCT_WRITE') && (
  <AppButton onClick={handleEdit}>Edit</AppButton>
)}
```

## Performance Optimizations

### Navigation Performance
- **Route Prefetching**: Routes are prefetched on hover (`prefetchRoute` in NavigationContext)
- **React Query Caching**: 5-minute stale time prevents unnecessary refetches during navigation
- **Skeleton Loaders**: Dashboard pages show skeleton UI during loading (`loading.tsx`)

### Authentication Architecture (Industry Best Practice)

Authentication follows the standard Next.js pattern with separation of concerns:

**Middleware (Quick Guard)**
- Cookie existence check only (no API calls)
- Redirects unauthenticated users to `/login`
- Redirects authenticated users away from `/login`, `/register`
- No secrets, no validation logic - just routing

**AuthContext (Full Validation)**
- Calls `/api/me` to validate session with backend
- Handles 401/403 responses by clearing session and redirecting
- Manages user state, permissions, roles
- React Query for caching (5-minute stale time)

**Why this pattern?**
- Middleware runs on Edge runtime with limitations
- API calls in middleware cause latency on every navigation
- AuthContext handles complex auth logic properly
- Expired tokens are caught on first page load, not every navigation

**Security:**
- Backend is the ONLY source of truth for token validation
- No JWT secret in frontend
- HttpOnly cookies prevent XSS token theft
- AuthContext redirects to login on any auth error

### Loading States

Loading.tsx files provide skeleton UI during navigation:

| File | Coverage |
|------|----------|
| `src/app/loading.tsx` | Root level |
| `src/app/(dashboard)/loading.tsx` | All dashboard routes |
| `src/app/(dashboard)/organization/loading.tsx` | Organization routes |
| `src/app/(dashboard)/store/[storeId]/loading.tsx` | Store routes |

### Key Performance Files
| File | Purpose |
|------|---------|
| `src/components/Providers/ReactQueryProvider.tsx` | Query caching config |
| `src/middleware.ts` | Security-balanced auth |
| `src/contexts/NavigationContext.tsx` | Route prefetching |

### Performance Tips
1. Keep `staleTime` at 5 minutes to prevent refetch on navigation
2. Use `prefetchRoute` on hover for instant page loads
3. Don't set `refetchOnMount: true` - causes unnecessary fetches
4. Use skeleton loaders in `loading.tsx` for perceived performance
5. Never remove backend validation - it catches revoked tokens
