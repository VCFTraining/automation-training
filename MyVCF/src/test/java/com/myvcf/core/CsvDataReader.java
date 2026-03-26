package com.myvcf.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvDataReader {

	private CsvDataReader() {

	}

	private static InputStream open(String resourcePath) {
		InputStream in = CsvDataReader.class.getResourceAsStream(resourcePath);
		if (in == null) {
			throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath
					+ " (make sure the file is under src/test/resources)");
		}
		return in;
	}

	public static <T> List<T> readResourceList(String resourcePath, Class<T> type) {
		try (InputStream in = open(resourcePath);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			String headerLine = reader.readLine();
			if (headerLine == null) {
				return List.of();
			}

			List<String> headers = parseCsvLine(headerLine);

			return reader.lines().filter(line -> line != null && !line.trim().isEmpty())
					.map(CsvDataReader::parseCsvLine).map(values -> mapRow(headers, values, type))
					.collect(Collectors.toList());

		} catch (Exception e) {
			throw new RuntimeException("Failed to load CSV from resource: " + resourcePath, e);
		}
	}

	private static List<String> parseCsvLine(String line) {
		if (line == null || line.isBlank()) {
			return List.of();
		}

		return Arrays.stream(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1)).map(field -> {
			String cleaned = field.trim().replaceAll("^\"|\"$", "");
			return cleaned.isBlank() ? null : cleaned;
		}).collect(Collectors.toList());
	}

	private static Object convertValue(String value, Class<?> targetType) {
		if (value == null) {
			if (targetType.isPrimitive()) {
				return targetType == boolean.class ? false : 0; // default for other primitives
			}
			return null;
		}

		if (targetType == String.class) {
			return value;
		}
		if (targetType == boolean.class || targetType == Boolean.class) {
			return Boolean.valueOf(value); // case-insensitive "true"/"false"
		}

		return value;
	}

	private static <T> T mapRow(List<String> headers, List<String> values, Class<T> type) {
		try {
			T instance = type.getDeclaredConstructor().newInstance();

			for (int i = 0; i < headers.size(); i++) {
				String header = headers.get(i);
				if (header == null || header.isBlank())
					continue;

				String valueStr = (i < values.size()) ? values.get(i) : null;

				Class<?> targetType = String.class; // default
				Field field = null;
				try {
					field = type.getDeclaredField(header);
					targetType = field.getType();
				} catch (NoSuchFieldException ignored) {
				}

				Object converted = convertValue(valueStr, targetType);

				String setterName = "set" + Character.toUpperCase(header.charAt(0)) + header.substring(1);
				try {
					Method setter = type.getMethod(setterName, targetType);
					setter.invoke(instance, converted);
					continue;
				} catch (NoSuchMethodException ignored) {
				}

				if (field != null) {
					field.setAccessible(true);
					field.set(instance, converted);
				}
			}

			return instance;

		} catch (Exception e) {
			throw new RuntimeException("Failed to map CSV row to " + type.getSimpleName(), e);
		}
	}
}
