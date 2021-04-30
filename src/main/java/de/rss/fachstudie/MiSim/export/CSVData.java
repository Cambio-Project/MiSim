package de.rss.fachstudie.MiSim.export;

import java.lang.reflect.Field;

/**
 * Provides methods for direct conversion to the csv format.
 * It is best used in pure dataclasses since all fields of an implementing object will be extracted to the csv string.
 *
 * Default implementation can be overwritten if needed. For consistency reasons this is not recommended.
 *
 * @author Lion Wagner
 */
public interface CSVData {
    default String toCSVData() {
        return this.toCSVData(',');
    }

    default String toCSVData(char sep) {
        StringBuilder result = new StringBuilder();
        try {
            for (Field field : this.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                result.append(field.get(this)).append(sep);
            }
        } catch (IllegalAccessException ignored) {
            ignored.printStackTrace();
        }
        if (result.length() > 0) result.deleteCharAt(result.lastIndexOf(String.valueOf(sep)));
        return result.toString();
    }

    default String toCSVHeader() {
        return this.toCSVHeader(',');
    }

    default String toCSVHeader(char sep) {
        StringBuilder result = new StringBuilder();
        for (Field field : this.getClass().getDeclaredFields()) {
            result.append(field.getName()).append(sep);
        }
        if (result.length() > 0) result.deleteCharAt(result.lastIndexOf(String.valueOf(sep)));
        return result.toString();
    }
}
