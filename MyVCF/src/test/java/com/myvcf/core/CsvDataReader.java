package com.myvcf.core;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvDataReader {

	private CsvDataReader() {
	}

	public static <T> List<T> readResourceList(String resourcePath, Class<T> type) {
		try (InputStream in = open(resourcePath);
				InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			CSVReader csvReader = new CSVReaderBuilder(reader).withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
					.build();

			return new CsvToBeanBuilder<T>(csvReader).withType(type).withIgnoreLeadingWhiteSpace(true).build().parse();

		} catch (Exception e) {
			throw new RuntimeException("Failed to load CSV from resource: " + resourcePath, e);
		}
	}

	private static InputStream open(String resourcePath) {

		InputStream input = CsvDataReader.class.getResourceAsStream(resourcePath);
		if (input == null) {
			throw new IllegalArgumentException("resource not found on classPath: " + resourcePath);
		}
		return input;
	}
}
