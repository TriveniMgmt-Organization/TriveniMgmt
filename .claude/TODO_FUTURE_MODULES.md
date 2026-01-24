# Future Modules - Clean Architecture Migration

> These modules will be implemented later using the same Clean Architecture pattern established in the existing modules.

## Pending Modules

### Sales Module
- [ ] Domain layer (aggregates, value objects, events)
- [ ] Application layer (commands, queries, handlers)
- [ ] Infrastructure layer (JPA repositories, REST controller)
- **Proposed API**: `/api/v2/sales`

### Purchasing Module
- [ ] Domain layer (aggregates, value objects, events)
- [ ] Application layer (commands, queries, handlers)
- [ ] Infrastructure layer (JPA repositories, REST controller)
- **Proposed API**: `/api/v2/purchasing`

### Reporting Module
- [ ] Domain layer (aggregates, value objects, events)
- [ ] Application layer (commands, queries, handlers)
- [ ] Infrastructure layer (JPA repositories, REST controller)
- **Proposed API**: `/api/v2/reports`
- **Note**: Currently only has Report entity - needs full design

---

## Reference Architecture Pattern

Each module follows this structure:
```
modules/{module-name}/
├── domain/
│   ├── model/        # Aggregates, entities, value objects
│   ├── event/        # Domain events
│   ├── exception/    # Domain exceptions
│   └── repository/   # Repository interfaces (ports)
├── application/
│   ├── command/      # Commands and handlers
│   ├── query/        # Queries and handlers
│   └── dto/          # Data transfer objects
└── infrastructure/
    ├── persistence/  # JPA repository implementations
    └── web/          # REST controllers
```

*Created: 2026-01-24*
