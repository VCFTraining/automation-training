package com.myvcf.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Tiny helper to load JSON test data from classpath resources (src/test/resources).
 *
 * Usage:
 *   List<UnrepData> rows =
 *       JsonDataReader.readResourceList("/data/unrep.json", UnrepData[].class);
 */
public final class JsonDataReader {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // LocalDate, etc.
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonDataReader() {}

    /** Read a JSON array file (e.g., [ {...}, {...} ]) into a List. */
    public static <T> List<T> readResourceList(String resourcePath, Class<T[]> arrayType) {
        try (InputStream in = open(resourcePath)) {
            T[] arr = MAPPER.readValue(in, arrayType);
            return Arrays.asList(arr);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON array from resource: " + resourcePath, e);
        }
    }

    /** Read a single JSON object file (e.g., { ... }) into an instance. */
    public static <T> T readResource(String resourcePath, Class<T> type) {
        try (InputStream in = open(resourcePath)) {
            return MAPPER.readValue(in, type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON object from resource: " + resourcePath, e);
        }
    }

    /** Convenience: load a resource as String (handy for debug/logging). */
    public static String readResourceAsString(String resourcePath) {
        try (InputStream in = open(resourcePath)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource as String: " + resourcePath, e);
        }
    }

    private static InputStream open(String resourcePath) {
        // Expect leading "/" for classpath root, e.g., "/data/unrep.json"
        InputStream in = JsonDataReader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath +
                    " (did you place it under src/test/resources?)");
        }
        return in;
    }
}


