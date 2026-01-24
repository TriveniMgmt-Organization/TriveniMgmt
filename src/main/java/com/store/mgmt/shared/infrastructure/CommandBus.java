package com.store.mgmt.shared.infrastructure;

import com.store.mgmt.shared.application.command.Command;
import com.store.mgmt.shared.application.command.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dispatches commands to their handlers.
 * Handlers are discovered from the Spring context.
 */
@Component
public class CommandBus {

    private static final Logger log = LoggerFactory.getLogger(CommandBus.class);

    private final ApplicationContext context;
    private final Map<Class<?>, CommandHandler<?, ?>> handlerCache = new ConcurrentHashMap<>();

    public CommandBus(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Dispatch a command to its handler.
     *
     * @param command The command to dispatch
     * @param <C>     The command type
     * @param <R>     The result type
     * @return The result from the handler
     */
    @SuppressWarnings("unchecked")
    public <C extends Command<R>, R> R dispatch(C command) {
        CommandHandler<C, R> handler = (CommandHandler<C, R>) findHandler(command.getClass());

        log.debug("Dispatching command {} to handler {}",
                command.getClass().getSimpleName(),
                handler.getClass().getSimpleName());

        return handler.handle(command);
    }

    @SuppressWarnings("rawtypes")
    private CommandHandler<?, ?> findHandler(Class<?> commandClass) {
        return handlerCache.computeIfAbsent(commandClass, clazz -> {
            Map<String, CommandHandler> handlers = context.getBeansOfType(CommandHandler.class);

            for (CommandHandler handler : handlers.values()) {
                Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(
                        handler.getClass(), CommandHandler.class);

                if (typeArgs != null && typeArgs.length > 0 && typeArgs[0].equals(commandClass)) {
                    return handler;
                }
            }

            throw new IllegalStateException("No handler found for command: " + commandClass.getName());
        });
    }
}
