package com.mycompany.myapp.service.importer.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ImportRecord(int rowNumber, Map<String, String> values) {
    public ImportRecord {
        values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }
}
