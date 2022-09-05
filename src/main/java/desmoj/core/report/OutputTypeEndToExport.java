package desmoj.core.report;

/**
 * Interface for Outputs that cannot be written line by line, e.g. Excel.
 *
 * @author Xiufeng Li
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @date 30.03.2011
 */

public interface OutputTypeEndToExport extends OutputType {
    /**
     * Export a new few file for the writting output.
     *
     * @param pathname String: path to write in
     * @param filename String: name of the file
     */
	void export(String pathname, String filename);
}
