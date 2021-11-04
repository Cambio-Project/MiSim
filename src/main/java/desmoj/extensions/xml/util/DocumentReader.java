package desmoj.extensions.xml.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Helper class for reading DOM documents from files
 *
 * @author Nicolas Knaak
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class DocumentReader {

    private static final DocumentReader instance = new DocumentReader();

    private final DocumentBuilder builder;

    private DocumentReader() {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                .newInstance();
            builderFactory.setNamespaceAware(false);
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static DocumentReader getInstance() {
        return instance;
    }

    public Document createDoc() {
        return builder.newDocument();
    }

    public Document readDoc(String filename) throws IOException, SAXException {
        FileInputStream fileIn = new FileInputStream(filename);
        Document doc = builder.parse(fileIn);
        fileIn.close();
        return doc;
    }

}
