package com.mycompany.myapp.service.importer;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private final ImportHandlerRegistry handlerRegistry;
    private final Map<ImportFormat, ImportParser> parsers;

    public ImportService(ImportHandlerRegistry handlerRegistry, List<ImportParser> parsers) {
        this.handlerRegistry = handlerRegistry;
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

    public ImportResult importFile(String entityType, MultipartFile file, Long clientUserId, boolean bulk) {
        if (file == null || file.isEmpty()) {
            return ImportResult.failed(0, List.of(new ImportError(null, null, "Import file is empty")));
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            return ImportResult.failed(0, List.of(new ImportError(null, null, "Import file exceeds the 5 MB limit")));
        }

        final EntityImportHandler handler;
        final ImportFormat format;

        try {
            handler = handlerRegistry.get(entityType);
            format = ImportFormat.fromFilename(file.getOriginalFilename());
        } catch (IllegalArgumentException exception) {
            return ImportResult.failed(0, List.of(new ImportError(null, null, exception.getMessage())));
        }

        ImportParser parser = parsers.get(format);
        if (parser == null) {
            return ImportResult.failed(0, List.of(new ImportError(null, null, "No parser configured for format: " + format)));
        }

        final List<ImportRecord> records;
        long parseStartedAt = System.nanoTime();
        try {
            records = parser.parse(file, handler.xmlDefinition());
        } catch (ImportParseException exception) {
            return ImportResult.failed(0, List.of(new ImportError(exception.getRow(), null, exception.getMessage())));
        }

        long parseMs = (System.nanoTime() - parseStartedAt) / 1_000_000;
        if (records.isEmpty()) {
            return ImportResult.failed(0, List.of(new ImportError(null, null, "Import file contains no records")));
        }

        ImportContext context = new ImportContext(clientUserId, bulk);
        long validateStartedAt = System.nanoTime();
        List<ImportError> errors = handler.validate(records, context);
        long validateMs = (System.nanoTime() - validateStartedAt) / 1_000_000;

        if (!errors.isEmpty()) {
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

        long importStartedAt = System.nanoTime();
        int imported = handler.importRecords(records, context);
        long importMs = (System.nanoTime() - importStartedAt) / 1_000_000;

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
}
