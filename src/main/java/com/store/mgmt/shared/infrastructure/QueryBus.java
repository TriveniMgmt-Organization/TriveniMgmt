package com.store.mgmt.shared.infrastructure;

import com.store.mgmt.shared.application.query.Query;
import com.store.mgmt.shared.application.query.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatches queries to their handlers.
 * Handlers are discovered from the Spring context.
 */
@Component
public class QueryBus {

    private static final Logger log = LoggerFactory.getLogger(QueryBus.class);

    private final ApplicationContext context;
    private final Map<Class<?>, QueryHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();

    public QueryBus(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Dispatch a query to its handler.
     *
     * @param query The query to dispatch
     * @param <Q>   The query type
     * @param <R>   The result type
     * @return The result from the handler
     */
    @SuppressWarnings("unchecked")
    public <Q extends Query<R>, R> R dispatch(Q query) {
        QueryHandler<Q, R> handler = (QueryHandler<Q, R>) findHandler(query.getClass());

        log.debug("Dispatching query {} to handler {}",
                query.getClass().getSimpleName(),
                handler.getClass().getSimpleName());

        return handler.handle(query);
    }

    @SuppressWarnings("rawtypes")
    private QueryHandler<?, ?> findHandler(Class<?> queryClass) {
        return handlerCache.computeIfAbsent(queryClass, clazz -> {
            Map<String, QueryHandler> handlers = context.getBeansOfType(QueryHandler.class);

            for (QueryHandler handler : handlers.values()) {
                Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(
                        handler.getClass(), QueryHandler.class);

                if (typeArgs != null && typeArgs.length > 0 && typeArgs[0].equals(queryClass)) {
                    return handler;
                }
            }

            throw new IllegalStateException("No handler found for query: " + queryClass.getName());
        });
    }
}
