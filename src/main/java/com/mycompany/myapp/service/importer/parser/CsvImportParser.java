package com.mycompany.myapp.service.importer.parser;

import com.mycompany.myapp.service.importer.ImportParseException;
import com.mycompany.myapp.service.importer.model.ImportFormat;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvImportParser implements ImportParser {

    @Override
    public ImportFormat format() {
        return ImportFormat.CSV;
    }

    @Override
    public List<ImportRecord> parse(MultipartFile file, XmlImportDefinition xmlDefinition) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }

            CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setIgnoreEmptyLines(true).get();

            try (CSVParser parser = csvFormat.parse(new StringReader(content))) {
                List<String> headers = parser.getHeaderNames();
                validateHeaders(headers);
                List<ImportRecord> records = new ArrayList<>();
                for (CSVRecord csvRecord : parser) {
                    if (!csvRecord.isConsistent()) {
                        throw new ImportParseException(
                            Math.toIntExact(csvRecord.getRecordNumber() + 1),
                            "CSV column count does not match the header"
                        );
                    }
                    Map<String, String> values = new LinkedHashMap<>();

                    for (String header : headers) {
                        values.put(header, csvRecord.get(header));
                    }

                    records.add(new ImportRecord(Math.toIntExact(csvRecord.getRecordNumber() + 1), values));
                }
                return records;
            }
        } catch (ImportParseException exception) {
            throw exception;
        } catch (IOException | IllegalArgumentException exception) {
            throw new ImportParseException("Unable to parse CSV: " + exception.getMessage(), exception);
        }
    }

    private void validateHeaders(List<String> headers) {
        if (headers.isEmpty()) {
            throw new ImportParseException("CSV header is required");
        }

        Set<String> uniqueHeaders = new HashSet<>();

        for (String header : headers) {
            if (header == null || header.isBlank()) {
                throw new ImportParseException("CSV header contains an empty column name");
            }

            if (!uniqueHeaders.add(header)) {
                throw new ImportParseException("Duplicate CSV column: " + header);
            }
        }
    }
}
