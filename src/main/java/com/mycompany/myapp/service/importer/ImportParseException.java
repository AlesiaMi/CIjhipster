package com.mycompany.myapp.service.importer;

public class ImportParseException extends RuntimeException {

    private final Integer row;

    public ImportParseException(String message) {
        this(null, message, null);
    }

    public ImportParseException(String message, Throwable cause) {
        this(null, message, cause);
    }

    public ImportParseException(Integer row, String message) {
        this(row, message, null);
    }

    public ImportParseException(Integer row, String message, Throwable cause) {
        super(message, cause);
        this.row = row;
    }

    public Integer getRow() {
        return row;
    }
}
