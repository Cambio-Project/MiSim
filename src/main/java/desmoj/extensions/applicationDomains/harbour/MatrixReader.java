package desmoj.extensions.applicationDomains.harbour;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

/**
 * A MatrixReader represents a component for the reading of a (distance) matrix from a file of characters The lines of
 * the file are the lines of the matrix. Between the elements in every line must be " " as char.
 *
 * @author Eugenia Neufeld
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class MatrixReader {

    /**
     * The name of the file that must be read and has the matrix.
     */
    private Reader reader;

    /**
     * Constructs a Matrix reader from a java.io.Reader.
     *
     * @param reader The java.io.Reader to read the Matrix from
     * @author Nicolas Knaak
     */
    public MatrixReader(Reader reader) {
        this.reader = reader;
    }

    /**
     * Constructs a MatrixReader from a file of chars.
     *
     * @param filename String : The name of the file that must be read.
     */
    public MatrixReader(String filename) {

        try {
            reader = new FileReader(filename);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    /**
     * Returns the (distance) matrix from the file of the chars.
     *
     * @return double[][] : The matrix.
     */
    public double[][] getMatrix() {

        // the distance matrix
        double[][] d_matrix = null;

        // read the distance matrix from the file
        try {

            ArrayList<String> v = new ArrayList<String>();  // Changed Vector to ArrayList (JG, 11.03.09)
            BufferedReader in = new BufferedReader(reader);
            String s = "";

            // read all the lines of the file
            while ((s = in.readLine()) != null) {
                // add every line to the vector
                v.add(s);
            }
            in.close(); // close the file

            // the line of the file
            String line;
            // make a matrix
            d_matrix = new double[v.size()][v.size()];

            // the element of the distance matrix
            double distance;   // Changed Double to double (JG, 11.03.09)

            for (int i = 0; i < v.size(); i++) {

                // get the line of the file
                line = v.get(i);

                // the position of the element after the " " in the line
                int k = 0;
                // the second index for the every element of the distance matrix
                int index = 0;
                // the position of the last element of the line
                int l = line.length() - 1;

                for (int j = 0; j < line.length(); j++) {

                    // if the end of the line
                    if (j == l) {
                        // get the new distance
                        distance = new Double(line.substring(k, j + 1));
                        d_matrix[i][index] = distance;

                    } else {
                        if (line.charAt(j) == ' ') {
                            // get the new distance
                            distance = Double.parseDouble(line.substring(k, j));
                            k = j + 1;
                            d_matrix[i][index] = distance;
                            index++;
                        }
                    }
                } // end of the inner for
            } // end of for

        } catch (IOException e) {
            System.out.println(e);
        }

        return d_matrix;
    }
}