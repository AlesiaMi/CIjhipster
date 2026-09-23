package com.mycompany.myapp.service.importer.model;

public record XmlImportDefinition(String rootElement, String recordElement, String dtdSystemId, String dtdClasspathResource) {}
