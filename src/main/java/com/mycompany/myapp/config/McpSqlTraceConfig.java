package com.mycompany.myapp.config;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class McpSqlTraceConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(McpSqlTraceConfig.class);
    private static final String MCP_LOG_REQUEST_ID = McpSqlTraceConfig.class.getName() + ".requestId";
    private static final int MAX_PARAMETER_LENGTH = 200;
    private final McpSqlTrace trace;

    @Value("${CIJHIPSTER_MCP_SQL_TRACE_ENABLED:false}")
    private boolean enabled;

    public McpSqlTraceConfig(McpSqlTrace trace) {
        this.trace = trace;
    }

    @Bean
    public HibernatePropertiesCustomizer mcpSqlInspector() {
        return properties -> properties.put("hibernate.session_factory.statement_inspector", trace);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(
                new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                        String method = request.getMethod();
                        String path = request.getRequestURI();

                        boolean isGet = "GET".equals(method);

                        boolean isCreate = "POST".equals(method) && "/api/data-sources".equals(path);

                        boolean isUpdate = "PUT".equals(method) && path.matches("/api/data-sources/[1-9][0-9]*");

                        boolean isDelete = "DELETE".equals(method) && path.matches("/api/data-sources/[1-9][0-9]*");

                        if (!enabled || !(isGet || isCreate || isUpdate || isDelete)) {
                            return true;
                        }

                        if (!SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
                            return true;
                        }

                        String requestId = request.getHeader("X-MCP-Trace-ID");

                        if (requestId == null) {
                            return true;
                        }

                        try {
                            UUID uuid = UUID.fromString(requestId);

                            if (uuid.toString().equals(requestId)) {
                                trace.start(requestId);

                                request.setAttribute(MCP_LOG_REQUEST_ID, requestId);

                                log.info(
                                    "[MCP HTTP] START requestId={} method={} endpoint={} parameters={}",
                                    requestId,
                                    method,
                                    path,
                                    formatParameters(request.getParameterMap())
                                );
                            }
                        } catch (IllegalArgumentException ignored) {
                            // Игнорируем неверный идентификатор.
                        }

                        return true;
                    }

                    @Override
                    public void afterCompletion(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Object handler,
                        Exception exception
                    ) {
                        try {
                            Object requestId = request.getAttribute(MCP_LOG_REQUEST_ID);

                            if (requestId != null) {
                                log.info(
                                    "[MCP HTTP] END requestId={} method={} endpoint={} status={}",
                                    requestId,
                                    request.getMethod(),
                                    request.getRequestURI(),
                                    response.getStatus()
                                );
                            }
                        } finally {
                            trace.finish();
                        }
                    }
                }
            )
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/mcp-sql-traces/**");
    }

    private static String formatParameters(Map<String, String[]> parameters) {
        if (parameters.isEmpty()) {
            return "{}";
        }

        return parameters
            .entrySet()
            .stream()
            .filter(entry -> !isSensitiveParameter(entry.getKey()))
            .map(entry -> {
                String values = Arrays.stream(entry.getValue()).map(McpSqlTraceConfig::sanitizeValue).collect(Collectors.joining(","));

                return entry.getKey() + "=" + values;
            })
            .collect(Collectors.joining(", ", "{", "}"));
    }

    private static boolean isSensitiveParameter(String name) {
        String lower = name.toLowerCase(java.util.Locale.ROOT);

        return (
            lower.contains("token") ||
            lower.contains("password") ||
            lower.contains("secret") ||
            lower.contains("authorization") ||
            lower.contains("apikey") ||
            lower.contains("api_key")
        );
    }

    private static String sanitizeValue(String value) {
        if (value == null) {
            return "";
        }

        String sanitized = value.replaceAll("[\\r\\n\\t]", " ");

        if (sanitized.length() > MAX_PARAMETER_LENGTH) {
            return sanitized.substring(0, MAX_PARAMETER_LENGTH) + "...";
        }

        return sanitized;
    }
}
