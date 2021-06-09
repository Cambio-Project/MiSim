package de.unistuttgart.sqa.orcas.misim.export;

import java.lang.reflect.Field;

/**
 * Provides methods for direct conversion to the csv format. It is best used in pure dataclasses since all fields of an
 * implementing object will be extracted to the csv string.
 *
 * <p>
 * Default implementation can be overwritten if needed. For consistency reasons this is not recommended.
 *
 * @author Lion Wagner
 */
public interface CSVData {
    /**
     * Converts this object to csv data. Uses a comma for separation.
     * @return a string, containing this objects data.
     */
    default String toCSVData() {
        return this.toCSVData(',');
    }

    /**
     * Converts this object to csv data. Uses the provided separator.
     * @param sep the char that should be used as separator
     * @return a string, containing this objects data.
     */
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
        if (result.length() > 0) {
            result.deleteCharAt(result.lastIndexOf(String.valueOf(sep)));
        }
        return result.toString();
    }

    /**
     * Gets the csv header for this object. Values are separated by a comma.
     *
     * @return a comma-separated string, containing the names of all fields of this object
     */
    default String toCSVHeader() {
        return this.toCSVHeader(',');
    }

    /**
     * Gets the csv header for this object. Values are separated by the provided separator.
     *
     * @return a character-separated string, containing the names of all fields of this object
     */
    default String toCSVHeader(char sep) {
        StringBuilder result = new StringBuilder();
        for (Field field : this.getClass().getDeclaredFields()) {
            result.append(field.getName()).append(sep);
        }
        if (result.length() > 0) {
            result.deleteCharAt(result.lastIndexOf(String.valueOf(sep)));
        }
        return result.toString();
    }
}
