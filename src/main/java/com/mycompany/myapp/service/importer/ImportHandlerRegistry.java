package com.mycompany.myapp.service.importer;

import com.mycompany.myapp.service.importer.handler.EntityImportHandler;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ImportHandlerRegistry {

    private final Map<String, EntityImportHandler> handlers;

    public ImportHandlerRegistry(List<EntityImportHandler> handlers) {
        Map<String, EntityImportHandler> registeredHandlers = new LinkedHashMap<>();
        for (EntityImportHandler handler : handlers) {
            EntityImportHandler previous = registeredHandlers.put(handler.entityType(), handler);
            if (previous != null) {
                throw new IllegalStateException("Duplicate import handler for entity type: " + handler.entityType());
            }
        }
        this.handlers = Map.copyOf(registeredHandlers);
    }

    public EntityImportHandler get(String entityType) {
        EntityImportHandler handler = handlers.get(entityType);

        if (handler == null) {
            throw new IllegalArgumentException("Import is not supported for entity: " + entityType);
        }
        return handler;
    }

    public Set<String> supportedEntityTypes() {
        return handlers.keySet();
    }
}
