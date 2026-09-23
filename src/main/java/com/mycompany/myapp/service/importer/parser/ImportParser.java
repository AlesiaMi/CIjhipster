package com.mycompany.myapp.service.importer.parser;

import com.mycompany.myapp.service.importer.model.ImportFormat;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImportParser {
    ImportFormat format();
    List<ImportRecord> parse(MultipartFile file, XmlImportDefinition xmlDefinition);
}
