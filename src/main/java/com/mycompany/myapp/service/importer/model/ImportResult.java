package com.mycompany.myapp.service.importer.model;

import java.util.List;

public record ImportResult(int total, int imported, List<ImportError> errors) {
    public ImportResult {
        errors = List.copyOf(errors);
    }

    public static ImportResult failed(int total, List<ImportError> errors) {
        return new ImportResult(total, 0, errors);
    }

    public static ImportResult success(int total, int imported) {
        return new ImportResult(total, imported, List.of());
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
