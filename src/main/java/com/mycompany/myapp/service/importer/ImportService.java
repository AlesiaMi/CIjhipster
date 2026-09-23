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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class ImportService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;
    private static final int MAX_RECORDS = 1000;
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

    public ImportResult importFile(String entityType, MultipartFile file, Long clientUserId) {
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

        try {
            records = parser.parse(file, handler.xmlDefinition());
        } catch (ImportParseException exception) {
            return ImportResult.failed(0, List.of(new ImportError(exception.getRow(), null, exception.getMessage())));
        }

        if (records.isEmpty()) {
            return ImportResult.failed(0, List.of(new ImportError(null, null, "Import file contains no records")));
        }

        if (records.size() > MAX_RECORDS) {
            return ImportResult.failed(
                records.size(),
                List.of(new ImportError(null, null, "Import contains more than " + MAX_RECORDS + " records"))
            );
        }

        ImportContext context = new ImportContext(clientUserId);
        List<ImportError> errors = handler.validate(records, context);

        if (!errors.isEmpty()) {
            return ImportResult.failed(records.size(), errors);
        }

        int imported = handler.importRecords(records, context);
        return ImportResult.success(records.size(), imported);
    }
}
