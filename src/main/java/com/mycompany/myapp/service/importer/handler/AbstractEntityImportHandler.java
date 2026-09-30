package com.mycompany.myapp.service.importer.handler;

import com.mycompany.myapp.domain.annotation.Importable;
import com.mycompany.myapp.service.importer.model.ImportContext;
import com.mycompany.myapp.service.importer.model.ImportError;
import com.mycompany.myapp.service.importer.model.ImportRecord;
import com.mycompany.myapp.service.importer.model.XmlImportDefinition;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractEntityImportHandler<DOMAIN, DTO> implements EntityImportHandler {

    private final String entityType;
    private final XmlImportDefinition xmlDefinition;

    protected abstract Class<DOMAIN> getDomainClass();

    protected abstract void validateSpecificBusinessRules(List<ImportRecord> records, ImportContext context, List<ImportError> errors);

    protected abstract DTO mapSpecificRecord(ImportRecord record, Instant batchTimestamp);

    protected abstract List<DTO> saveSpecificRecords(List<DTO> records, ImportContext context);

    protected AbstractEntityImportHandler(String entityType, XmlImportDefinition xmlDefinition) {
        this.entityType = entityType;
        this.xmlDefinition = xmlDefinition;
    }

    @Override
    public final String entityType() {
        return entityType;
    }

    @Override
    public final XmlImportDefinition xmlDefinition() {
        return xmlDefinition;
    }

    @Override
    public final int importRecords(List<ImportRecord> records, ImportContext context) {
        Instant batchTimestamp = Instant.now();
        List<DTO> mappedRecords = records
            .stream()
            .map(record -> mapRecord(record, batchTimestamp))
            .toList();
        return saveAll(mappedRecords, context);
    }

    @Override
    public final List<ImportError> validate(List<ImportRecord> records, ImportContext context) {
        List<ImportError> errors = new ArrayList<>();
        validateImportableFields(records, errors);
        validateDomainConstraints(records, errors);
        errors.addAll(validateBusinessRules(records, context));
        return List.copyOf(errors);
    }

    protected final List<ImportError> validateBusinessRules(List<ImportRecord> records, ImportContext context) {
        List<ImportError> errors = new ArrayList<>();
        validateSpecificBusinessRules(records, context, errors);
        return List.copyOf(errors);
    }

    protected final DTO mapRecord(ImportRecord record, Instant batchTimestamp) {
        return mapSpecificRecord(record, batchTimestamp);
    }

    protected final int saveAll(List<DTO> records, ImportContext context) {
        return saveSpecificRecords(records, context).size();
    }

    protected final boolean validateHttpUrl(ImportRecord record, String field, List<ImportError> errors) {
        String value = stringValue(record, field);
        if (value == null) {
            return true;
        }

        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new URISyntaxException(value, "HTTP/HTTPS URL required");
            }
            return true;
        } catch (URISyntaxException exception) {
            errors.add(new ImportError(record.rowNumber(), field, "Invalid HTTP/HTTPS URL"));
            return false;
        }
    }

    protected final String normalizeKey(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    protected final boolean addUniqueKey(
        Set<String> keys,
        String key,
        ImportRecord record,
        String field,
        String message,
        List<ImportError> errors
    ) {
        if (keys.add(key)) {
            return true;
        }
        errors.add(new ImportError(record.rowNumber(), field, message));
        return false;
    }

    protected final String stringValue(ImportRecord record, String field) {
        return normalizeValue(record.values().get(field));
    }

    protected final Boolean booleanValue(ImportRecord record, String field) {
        String value = stringValue(record, field);
        if (value == null) {
            return null;
        }
        return Boolean.valueOf(value);
    }

    protected final Instant instantValue(ImportRecord record, String field) {
        String value = stringValue(record, field);
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    protected final Long longValue(ImportRecord record, String field) {
        String value = stringValue(record, field);

        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    protected final <E extends Enum<E>> E enumValue(ImportRecord record, String field, Class<E> enumClass) {
        String value = stringValue(record, field);
        if (value == null) {
            return null;
        }
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        return null;
    }

    private Map<String, Field> getImportableFields() {
        Map<String, Field> result = new LinkedHashMap<>();
        for (Field field : getAllFields(getDomainClass())) {
            Importable importable = field.getAnnotation(Importable.class);
            if (importable == null) {
                continue;
            }
            String importFieldName = getImportFieldName(field, importable);
            Field previous = result.put(importFieldName, field);
            if (previous != null) {
                throw new IllegalStateException("Duplicate import field name '" + importFieldName + "' in " + getDomainClass().getName());
            }
        }
        return Map.copyOf(result);
    }

    private void validateImportableFields(List<ImportRecord> records, List<ImportError> errors) {
        Map<String, Field> importableFields = getImportableFields();
        for (ImportRecord record : records) {
            for (String importFieldName : record.values().keySet()) {
                if (!importableFields.containsKey(importFieldName)) {
                    errors.add(new ImportError(record.rowNumber(), importFieldName, "Unknown import field"));
                }
            }
        }
    }

    private void validateDomainConstraints(List<ImportRecord> records, List<ImportError> errors) {
        Map<String, Field> importableFields = getImportableFields();
        for (ImportRecord record : records) {
            for (Map.Entry<String, Field> entry : importableFields.entrySet()) {
                String importFieldName = entry.getKey();
                Field domainField = entry.getValue();
                String value = record.values().get(importFieldName);
                validateRequiredConstraint(record, importFieldName, domainField, value, errors);
                validateSizeConstraint(record, importFieldName, domainField, value, errors);
                validateTypeConstraint(record, importFieldName, domainField, value, errors);
            }
        }
    }

    private void validateRequiredConstraint(
        ImportRecord record,
        String importFieldName,
        Field domainField,
        String value,
        List<ImportError> errors
    ) {
        boolean required =
            domainField.isAnnotationPresent(NotNull.class) ||
            domainField.isAnnotationPresent(NotBlank.class) ||
            domainField.isAnnotationPresent(NotEmpty.class);
        if (!required) {
            return;
        }
        if (normalizeValue(value) == null) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Field is required"));
        }
    }

    private void validateSizeConstraint(
        ImportRecord record,
        String importFieldName,
        Field domainField,
        String value,
        List<ImportError> errors
    ) {
        String normalizedValue = normalizeValue(value);
        if (normalizedValue == null) {
            return;
        }
        Size size = domainField.getAnnotation(Size.class);
        if (size == null) {
            return;
        }
        int length = normalizedValue.length();
        if (length < size.min()) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Minimum length is " + size.min()));
        }

        if (length > size.max()) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Maximum length is " + size.max()));
        }
    }

    private void validateTypeConstraint(
        ImportRecord record,
        String importFieldName,
        Field domainField,
        String value,
        List<ImportError> errors
    ) {
        String normalizedValue = normalizeValue(value);
        if (normalizedValue == null) {
            return;
        }
        if (isRelationField(domainField)) {
            validateRelationId(record, importFieldName, normalizedValue, errors);
            return;
        }

        Class<?> fieldType = domainField.getType();
        if (fieldType == Boolean.class || fieldType == boolean.class) {
            validateBooleanValue(record, importFieldName, normalizedValue, errors);
            return;
        }

        if (fieldType.isEnum()) {
            validateEnumValue(record, importFieldName, fieldType, normalizedValue, errors);
            return;
        }

        if (fieldType == Instant.class) {
            validateInstantValue(record, importFieldName, normalizedValue, errors);
            return;
        }

        if (fieldType == Long.class || fieldType == long.class) {
            validateLongValue(record, importFieldName, normalizedValue, errors);
            return;
        }

        if (fieldType == Integer.class || fieldType == int.class) {
            validateIntegerValue(record, importFieldName, normalizedValue, errors);
        }
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }

    private void validateBooleanValue(ImportRecord record, String importFieldName, String value, List<ImportError> errors) {
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Allowed values: true, false"));
        }
    }

    private void validateEnumValue(ImportRecord record, String importFieldName, Class<?> enumType, String value, List<ImportError> errors) {
        Object[] constants = enumType.getEnumConstants();
        boolean valid = Arrays.stream(constants)
            .map(constant -> ((Enum<?>) constant).name())
            .anyMatch(name -> name.equalsIgnoreCase(value));
        if (valid) {
            return;
        }

        String allowedValues = Arrays.stream(constants)
            .map(constant -> ((Enum<?>) constant).name())
            .collect(Collectors.joining(", "));
        errors.add(new ImportError(record.rowNumber(), importFieldName, "Allowed values: " + allowedValues));
    }

    private void validateInstantValue(ImportRecord record, String importFieldName, String value, List<ImportError> errors) {
        try {
            Instant.parse(value);
        } catch (DateTimeParseException exception) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Expected ISO-8601 instant"));
        }
    }

    private void validateLongValue(ImportRecord record, String importFieldName, String value, List<ImportError> errors) {
        try {
            Long.parseLong(value);
        } catch (NumberFormatException exception) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Expected integer number"));
        }
    }

    private void validateIntegerValue(ImportRecord record, String importFieldName, String value, List<ImportError> errors) {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Expected integer number"));
        }
    }

    private boolean isRelationField(Field domainField) {
        return domainField.isAnnotationPresent(ManyToOne.class) || domainField.isAnnotationPresent(OneToOne.class);
    }

    private void validateRelationId(ImportRecord record, String importFieldName, String value, List<ImportError> errors) {
        try {
            long id = Long.parseLong(value);
            if (id <= 0) {
                errors.add(new ImportError(record.rowNumber(), importFieldName, "ID must be positive"));
            }
        } catch (NumberFormatException exception) {
            errors.add(new ImportError(record.rowNumber(), importFieldName, "Expected positive integer ID"));
        }
    }

    private String getImportFieldName(Field field, Importable importable) {
        if (!importable.name().isBlank()) {
            return importable.name();
        }
        return field.getName();
    }

    private List<Field> getAllFields(Class<?> domainClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = domainClass;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                fields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }
}
