package com.mycompany.myapp.service.importer.handler;

import com.mycompany.myapp.service.importer.model.ImportContext;
import com.mycompany.myapp.service.importer.model.ImportError;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.util.List;

public interface EntityImportHandler {
    String entityType();
    XmlImportDefinition xmlDefinition();
    List<ImportError> validate(List<ImportRecord> records, ImportContext context);
    int importRecords(List<ImportRecord> records, ImportContext context);
}
