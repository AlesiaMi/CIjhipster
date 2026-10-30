package com.mycompany.myapp.config;

import com.mycompany.myapp.security.AuthoritiesConstants;
import com.mycompany.myapp.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class McpSqlTraceConfig implements WebMvcConfigurer {

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
                        trace.finish();
                    }
                }
            )
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/mcp-sql-traces/**");
    }
}
