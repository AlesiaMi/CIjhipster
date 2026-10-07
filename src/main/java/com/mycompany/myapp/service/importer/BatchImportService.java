package com.mycompany.myapp.service.importer;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BatchImportService {

    private final EntityManager entityManager;
    private final int batchSize;

    public BatchImportService(EntityManager entityManager, @Value("${import-settings.batch-size:50}") int batchSize) {
        this.entityManager = entityManager;
        this.batchSize = batchSize;
    }

    public <T> int saveBatch(List<T> entities) {
        if (batchSize <= 0) {
            throw new IllegalStateException("import-settings.batch-size must be greater than 0");
        }

        Session session = entityManager.unwrap(Session.class);
        Integer previousBatchSize = session.getJdbcBatchSize();
        session.setJdbcBatchSize(batchSize);

        try {
            for (int i = 0; i < entities.size(); i++) {
                entityManager.persist(entities.get(i));

                if ((i + 1) % batchSize == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }

            if (entities.size() % batchSize != 0) {
                entityManager.flush();
                entityManager.clear();
            }

            return entities.size();
        } finally {
            session.setJdbcBatchSize(previousBatchSize);
        }
    }
}
