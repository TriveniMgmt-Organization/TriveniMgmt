package com.store.mgmt.shared.application.query;

/**
 * Handler for a specific query type.
 * Each query should have exactly one handler.
 *
 * @param <Q> The query type
 * @param <R> The result type
 */
public interface QueryHandler<Q extends Query<R>, R> {

    /**
     * Handle the query and return a result.
     *
     * @param query The query to handle
     * @return The result of handling the query
     */
    R handle(Q query);
}
