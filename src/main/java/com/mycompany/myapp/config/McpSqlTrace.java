package com.mycompany.myapp.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

@Component
public class McpSqlTrace implements StatementInspector {

    private static final long serialVersionUID = 1L;

    private static final int MAX_SQL = 40;
    private static final int MAX_SQL_LENGTH = 4000;
    private static final int MAX_TRACES = 100;
    private static final long TTL_MS = 60_000;

    private final transient ThreadLocal<Capture> current = new ThreadLocal<>();

    private final Map<String, StoredTrace> completed = new ConcurrentHashMap<>();

    private static class Capture {

        final String requestId;
        final List<String> sql = new ArrayList<>();
        boolean truncated = false;

        Capture(String requestId) {
            this.requestId = requestId;
        }
    }

    private record StoredTrace(TraceResult result, long expiresAt) {}

    public record TraceResult(String requestId, List<String> sqlQueries, boolean truncated) {}

    public void start(String requestId) {
        current.set(new Capture(requestId));
    }

    @Override
    public String inspect(String sql) {
        Capture capture = current.get();

        if (capture != null) {
            if (capture.sql.size() < MAX_SQL) {
                if (sql.length() > MAX_SQL_LENGTH) {
                    capture.sql.add(sql.substring(0, MAX_SQL_LENGTH));
                    capture.truncated = true;
                } else {
                    capture.sql.add(sql);
                }
            } else {
                capture.truncated = true;
            }
        }

        // Не меняем SQL, передаваемый PostgreSQL.
        return sql;
    }

    public synchronized void finish() {
        Capture capture = current.get();
        current.remove();

        if (capture == null) {
            return;
        }

        cleanup();

        if (completed.size() >= MAX_TRACES) {
            completed
                .entrySet()
                .stream()
                .min((a, b) -> Long.compare(a.getValue().expiresAt(), b.getValue().expiresAt()))
                .ifPresent(entry -> completed.remove(entry.getKey()));
        }

        TraceResult result = new TraceResult(capture.requestId, List.copyOf(capture.sql), capture.truncated);

        completed.put(capture.requestId, new StoredTrace(result, System.currentTimeMillis() + TTL_MS));
    }

    public synchronized Optional<TraceResult> take(String requestId) {
        cleanup();

        StoredTrace stored = completed.remove(requestId);

        return stored == null ? Optional.empty() : Optional.of(stored.result());
    }

    private void cleanup() {
        long now = System.currentTimeMillis();

        completed.entrySet().removeIf(entry -> entry.getValue().expiresAt() < now);
    }
}
