package com.mycompany.myapp.web.rest;

import com.mycompany.myapp.config.McpSqlTrace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/mcp-sql-traces")
public class McpSqlTraceResource {

    private final McpSqlTrace trace;

    @Value("${CIJHIPSTER_MCP_SQL_TRACE_ENABLED:false}")
    private boolean enabled;

    public McpSqlTraceResource(McpSqlTrace trace) {
        this.trace = trace;
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public McpSqlTrace.TraceResult getTrace(@PathVariable String requestId) {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SQL trace disabled");
        }

        return trace.take(requestId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SQL trace not found or expired"));
    }
}
