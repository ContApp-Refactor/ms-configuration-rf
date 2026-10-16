package co.unicauca.edu.co.contables.audit.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import co.unicauca.edu.co.contables.audit.domain.exceptions.InvalidSnapshotDataException;

import lombok.Getter;

@Getter
public class DocumentData {

    private final Map<String, Object> header;
    private final List<Map<String, Object>> details;
    private final Map<String, Object> totals;
    private final Map<String, Object> metadata;

    private DocumentData(Map<String, Object> header, List<Map<String, Object>> details, Map<String, Object> totals,
            Map<String, Object> metadata) {
        this.header = sanitizeMap(header);
        this.details = details != null ? details.stream()
                .map(DocumentData::sanitizeMap)
                .toList()
                : Collections.emptyList();
        this.totals = sanitizeMap(totals);
        this.metadata = sanitizeMap(metadata);
    }

    public static DocumentData of(Map<String, Object> header, List<Map<String, Object>> details,
            Map<String, Object> totals,
            Map<String, Object> metadata) {

        validateContent(header, details, totals, metadata);

        validateKeys(header);
        validateKeys(totals);
        validateKeys(metadata);

        validateDetails(details);

        if (details != null) {
            details.forEach(DocumentData::validateKeys);
        }

        return new DocumentData(header, details, totals, metadata);
    }

    private static void validateContent(Map<String, Object> header, List<Map<String, Object>> details,
            Map<String, Object> totals, Map<String, Object> metadata) {

        boolean emptyHeader = header == null || header.isEmpty();
        boolean emptyDetails = details == null || details.isEmpty();
        boolean emptyTotals = totals == null || totals.isEmpty();
        boolean emptyMetadata = metadata == null || metadata.isEmpty();

        if (emptyHeader && emptyDetails && emptyTotals && emptyMetadata) {
            throw new InvalidSnapshotDataException(
                    "Document snapshot cannot be completely empty");
        }
    }

    private static void validateDetails(List<Map<String, Object>> details) {
        if (details == null)
            return;
        for (Map<String, Object> detail : details) {
            if (detail == null || detail.isEmpty()) {
                throw new InvalidSnapshotDataException(
                        "Document detail row cannot be null or empty");
            }
        }
    }

    private static void validateKeys(Map<String, Object> map) {
        if (map == null)
            return;

        for (String key : map.keySet()) {
            if (key == null || key.isBlank()) {
                throw new InvalidSnapshotDataException(
                        "Document field name cannot be null or empty");
            }
        }
    }

    private static Map<String, Object> sanitizeMap(Map<String, Object> map) {
        if (map == null) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream()
                .filter(e -> e.getKey() != null)
                .filter(e -> e.getValue() != null)
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }
}