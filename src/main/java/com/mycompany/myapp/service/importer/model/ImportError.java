package com.mycompany.myapp.service.importer.model;

public record ImportError(Integer row, String field, String message) {}
