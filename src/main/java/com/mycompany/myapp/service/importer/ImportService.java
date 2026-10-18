package com.mycompany.myapp.service.importer;

import com.mycompany.myapp.security.SecurityUtils;
import com.mycompany.myapp.service.importer.handler.EntityImportHandler;
import com.mycompany.myapp.service.importer.model.ImportContext;
import com.mycompany.myapp.service.importer.model.ImportError;
import com.mycompany.myapp.service.importer.model.ImportFormat;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.ImportResult;
import com.mycompany.myapp.service.importer.parser.ImportParser;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    private final ImportHandlerRegistry handlerRegistry;
    private final Map<ImportFormat, ImportParser> parsers;
    private final ImportProgressService progressService;

    public ImportService(ImportHandlerRegistry handlerRegistry, List<ImportParser> parsers, ImportProgressService progressService) {
        this.handlerRegistry = handlerRegistry;
        this.progressService = progressService;

        Map<ImportFormat, ImportParser> registeredParsers = new EnumMap<>(ImportFormat.class);

        for (ImportParser parser : parsers) {
            ImportParser previous = registeredParsers.put(parser.format(), parser);

            if (previous != null) {
                throw new IllegalStateException("Duplicate parser for format: " + parser.format());
            }
        }

        this.parsers = Map.copyOf(registeredParsers);
    }

    public Set<String> supportedEntityTypes() {
        return handlerRegistry.supportedEntityTypes();
    }

    // Сохраняем старый метод для совместимости.
    public ImportResult importFile(String entityType, MultipartFile file, Long clientUserId, boolean bulk) {
        return importFile(entityType, file, clientUserId, bulk, null);
    }

    // Импорт с необязательным идентификатором WebSocket-прогресса.
    public ImportResult importFile(String entityType, MultipartFile file, Long clientUserId, boolean bulk, UUID importId) {
        String login = bulk && importId != null ? SecurityUtils.getCurrentUserLogin().orElse(null) : null;

        progressService.send(login, importId, "PARSING", 0, 0, "Чтение файла");

        if (file == null || file.isEmpty()) {
            return failedImport(login, importId, 0, List.of(new ImportError(null, null, "Import file is empty")));
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return failedImport(login, importId, 0, List.of(new ImportError(null, null, "Import file exceeds the 5 MB limit")));
        }

        final EntityImportHandler handler;
        final ImportFormat format;

        try {
            handler = handlerRegistry.get(entityType);
            format = ImportFormat.fromFilename(file.getOriginalFilename());
        } catch (IllegalArgumentException exception) {
            return failedImport(login, importId, 0, List.of(new ImportError(null, null, exception.getMessage())));
        }

        ImportParser parser = parsers.get(format);

        if (parser == null) {
            return failedImport(login, importId, 0, List.of(new ImportError(null, null, "No parser configured for format: " + format)));
        }

        final List<ImportRecord> records;
        long parseStartedAt = System.nanoTime();

        try {
            records = parser.parse(file, handler.xmlDefinition());
        } catch (ImportParseException exception) {
            return failedImport(login, importId, 0, List.of(new ImportError(exception.getRow(), null, exception.getMessage())));
        }

        long parseMs = (System.nanoTime() - parseStartedAt) / 1_000_000;

        if (records.isEmpty()) {
            return failedImport(login, importId, 0, List.of(new ImportError(null, null, "Import file contains no records")));
        }

        progressService.send(login, importId, "VALIDATING", 0, records.size(), "Проверка записей");

        ImportContext context = new ImportContext(clientUserId, bulk);

        long validateStartedAt = System.nanoTime();
        List<ImportError> errors = handler.validate(records, context);
        long validateMs = (System.nanoTime() - validateStartedAt) / 1_000_000;

        if (!errors.isEmpty()) {
            progressService.send(login, importId, "FAILED", 0, records.size(), "Ошибка проверки файла");

            LOG.info(
                "IMPORT PHASE BENCHMARK entity={} mode={} records={} parseMs={} validateMs={} importMs={} success={}",
                entityType,
                bulk ? "BATCH" : "SAVE_ALL",
                records.size(),
                parseMs,
                validateMs,
                0,
                false
            );

            return ImportResult.failed(records.size(), errors);
        }

        progressService.send(login, importId, "SAVING", 0, records.size(), "Сохранение записей");

        final int[] importedAfterCommit = { 0 };

        if (login != null && importId != null && TransactionSynchronizationManager.isSynchronizationActive()) {
            final String eventLogin = login;

            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        progressService.send(
                            eventLogin,
                            importId,
                            "COMPLETED",
                            importedAfterCommit[0],
                            records.size(),
                            "Импорт успешно завершён"
                        );
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status != STATUS_COMMITTED) {
                            progressService.send(eventLogin, importId, "FAILED", 0, records.size(), "Транзакция импорта отменена");
                        }
                    }
                }
            );
        }

        long importStartedAt = System.nanoTime();

        if (bulk && login != null) {
            progressService.start(login, importId, records.size());
        }

        final int imported;

        try {
            imported = handler.importRecords(records, context);
        } finally {
            // Очищаем ThreadLocal даже при исключении.
            progressService.clear();
        }

        importedAfterCommit[0] = imported;
        long importMs = (System.nanoTime() - importStartedAt) / 1_000_000;

        progressService.send(login, importId, "COMMITTING", imported, records.size(), "Ожидание подтверждения транзакции");

        LOG.info(
            "IMPORT PHASE BENCHMARK entity={} mode={} records={} parseMs={} validateMs={} importMs={} success={}",
            entityType,
            bulk ? "BATCH" : "SAVE_ALL",
            records.size(),
            parseMs,
            validateMs,
            importMs,
            true
        );

        return ImportResult.success(records.size(), imported);
    }

    private ImportResult failedImport(String login, UUID importId, int total, List<ImportError> errors) {
        progressService.send(login, importId, "FAILED", 0, total, "Импорт не выполнен");
        return ImportResult.failed(total, errors);
    }
}
