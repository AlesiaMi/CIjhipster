package com.mycompany.myapp.service.importer.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.myapp.service.importer.ImportParseException;
import com.mycompany.myapp.service.importer.model.ImportFormat;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class JsonImportParser implements ImportParser {

    private final ObjectMapper objectMapper;

    public JsonImportParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ImportFormat format() {
        return ImportFormat.JSON;
    }

    @Override
    public List<ImportRecord> parse(MultipartFile file, XmlImportDefinition xmlDefinition) {
        try {
            JsonNode root = objectMapper.readTree(file.getInputStream());
            if (root == null || !root.isArray()) {
                throw new ImportParseException("JSON root must be an array of objects");
            }
            List<ImportRecord> records = new ArrayList<>();
            int rowNumber = 1;
            for (JsonNode item : root) {
                if (!item.isObject()) {
                    throw new ImportParseException(rowNumber, "Each JSON array item must be an object");
                }
                Map<String, String> values = new LinkedHashMap<>();
                Iterator<Map.Entry<String, JsonNode>> fields = item.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    JsonNode value = field.getValue();
                    if (value != null && (value.isObject() || value.isArray())) {
                        throw new ImportParseException(rowNumber, "Nested JSON values are not supported: " + field.getKey());
                    }
                    values.put(field.getKey(), value == null || value.isNull() ? null : value.asText());
                }
                records.add(new ImportRecord(rowNumber, values));
                rowNumber++;
            }
            return records;
        } catch (ImportParseException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new ImportParseException("Invalid JSON: " + exception.getOriginalMessage(), exception);
        } catch (IOException exception) {
            throw new ImportParseException("Unable to read JSON file", exception);
        }
    }
}
