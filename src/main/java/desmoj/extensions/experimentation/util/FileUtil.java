package desmoj.extensions.experimentation.util;

/**
 * Utilities to handle files & jar-archives
 *
 * @author Gunnar Kiesel
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    /**
     * copy the file of a given URL to the given destination
     *
     * @param source
     *            URL: the URL of the source file
     * @param destination
     *            String: the filename of the destination
     */
    public static void copy(URL source, String destination) {

        InputStream in = null;
        FileOutputStream out = null;
        try {
            in = source.openStream();
            out = new FileOutputStream(destination);
            int c;

            while ((c = in.read()) != -1) {
                out.write(c);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * copy the file of a given URL to the given destination
     *
     * @param source
     *            String: the name of the source file
     * @param destination
     *            String: the filename of the destination
     */
    public static void copy(String source, String destination) {

        FileReader in = null;
        FileWriter out = null;
        try {
            File inputFile = new File(source);
            File outputFile = new File(destination);

            in = new FileReader(inputFile);
            out = new FileWriter(outputFile);
            int c;

            while ((c = in.read()) != -1) {
                out.write(c);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * deletes a file
     *
     * @param filename
     *            String: the name of the file to be deleted.
     */
    public static void deleteFile(String filename) {
        File file = new File(filename);
        boolean result = file.delete();
        if (!result) {
            System.err.println("Deleting file" + filename + " failed.");
        }
    }

    /**
     * extract a file from a jar archive
     *
     * @param jarArchive
     *            String: the filename of the source archive
     * @param fileToExtract
     *            String: the name of the file to extract (full path from
     *            archive root)
     */
    public static void extractFile(String jarArchive, String fileToExtract,
                                   String destination) {

        FileWriter writer = null;
        ZipInputStream zipStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(jarArchive);
            BufferedInputStream bufferedStream = new BufferedInputStream(
                inputStream);
            zipStream = new ZipInputStream(bufferedStream);
            writer = new FileWriter(new File(destination));
            ZipEntry zipEntry = null;
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(fileToExtract)) {
                    int size = (int) zipEntry.getSize();
                    for (int i = 0; i < size; i++) {
                        writer.write(zipStream.read());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}