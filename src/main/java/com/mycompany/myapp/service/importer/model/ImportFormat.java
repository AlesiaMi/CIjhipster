package com.mycompany.myapp.service.importer.model;

import java.util.Locale;

public enum ImportFormat {
    CSV,
    JSON,
    XML;

    public static ImportFormat fromFilename(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            throw new IllegalArgumentException("File extension is required");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1).toUpperCase(Locale.ROOT);
        try {
            return ImportFormat.valueOf(extension);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Supported file formats: CSV, JSON, XML");
        }
    }
}
