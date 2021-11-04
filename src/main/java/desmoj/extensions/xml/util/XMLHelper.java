package desmoj.extensions.xml.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * A utility class providing several static methods to handle DOM documents.
 *
 * @author Nicolas Knaak and Gunnar Kiesel
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class XMLHelper {

    /**
     * Writes a DOM node (and all its ancestors) to the given output stream
     *
     * @param n the node to write
     * @param o the output stream to write the node to
     */
    public static void writeNode(Node n, FileOutputStream o) {
        PrintWriter w = new PrintWriter(o);
        w.print(n.toString());
        w.close();
    }

    /**
     * Creates a DOM element node with the given name. This node must only be used in the specified document.
     *
     * @param d    the "mother" document of the created node
     * @param name the name of the node
     * @return a node with the given name
     */
    public static Element createElement(Document d, String name) {
        return d.createElement(name);
    }

    /**
     * Creates an XML comment with the given text
     *
     * @param d    the "mother" document of the created comment
     * @param text the comment text
     * @return a comment containing the given text
     */
    public static Comment createComment(Document d, String text) {
        return d.createComment(text);
    }

    /**
     * Creates a text node in an XML document
     *
     * @param d    the "mother" document of the created text node
     * @param text contents of the text node
     * @return a new text node
     */
    public static Text createText(Document d, String text) {
        return d.createTextNode(text);
    }

    /**
     * Creates a DOM element node with the given name and contents. If indicated the contents is enclosed in CDATA
     * marks.
     *
     * @param d       the "mother" document of the created Element
     * @param name    the name of the element
     * @param value   the element's value
     * @param isCDATA a flag indicating if this element contains (unformatted) CDATA.
     * @return a new DOM Element
     */
    public static Element createElement(Document d, String name, String value,
                                        boolean isCDATA) {
        Element e = createElement(d, name);
        if (isCDATA) {
            e.appendChild(createCDATA(d, value));
        } else {
            e.appendChild(createText(d, value));
        }
        return e;
    }

    /**
     * Creates a DOM element node with the given name and contents. If indicated the contents is enclosed in CDATA
     * marks. The tag ends with a new line character.
     *
     * @param d       the "mother" document of the created Element
     * @param name    the name of the element
     * @param value   the element's value
     * @param isCDATA a flag indicating if this element contains (unformatted) CDATA.
     * @return a new DOM Element
     */
    public static Element createElementLn(Document d, String name,
                                          String value, boolean isCDATA) {
        Element e = createElement(d, name);
        if (isCDATA) {
            e.appendChild(createTextLn(d));
            e.appendChild(createCDATA(d, value));
            e.appendChild(createTextLn(d));
        } else {
            e.appendChild(createTextLn(d, "\n" + value));
        }
        return e;
    }

    /**
     * Creates a text node in an XML document ended by a new line character
     *
     * @param d    the "mother" document of the created text node
     * @param text contents of the text node
     * @return a new text node
     */
    public static Text createTextLn(Document d, String text) {
        return createText(d, text + "\n");
    }

    /**
     * Creates a text node in an XML document representing a new line character
     *
     * @param d the "mother" document of the created text node
     * @return a text node containing a single newline character
     */
    public static Text createTextLn(Document d) {
        return createTextLn(d, "");
    }

    /**
     * Creates a CDATA section node in an XML document
     *
     * @param d    the "mother" document of the created text node
     * @param text the text this CDATA section contains
     * @return a new CDATA section node.
     */
    public static CDATASection createCDATA(Document d, String text) {
        return d.createCDATASection(text);
    }

    /**
     * Creates a new DOM document.
     *
     * @return a new empty DOM document
     */
    public static Document createDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
            DocumentBuilder b = factory.newDocumentBuilder();
            return b.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Saves a DOM tree as XML file
     *
     * @param document Document: The DOM-Tree to be saved
     * @param out:     Writer: The (file)writer to be used
     */
    public static void serializeDocument(Document document, Writer out) {
        try {
            XMLSerializer serializer = new XMLSerializer();
            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            serializer.setOutputFormat(format);
            serializer.setOutputCharStream(out);
            serializer.serialize(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * adds an Element with one attribut to a DOM-tree.
     *
     * @param document    Document: the DOM-tree to add to
     * @param father      Element: the new element will be inserted directly under this Element in the tree
     * @param name        String: the name of the new element
     * @param attOneName  String: the name of the attribut
     * @param attOneValue String: the value of the attribut
     */
    public static void addElement(Document document, Element father,
                                  String name, String attOneName, String attOneValue) {
        Element element = document.createElement(name);
        element.setAttribute(attOneName, attOneValue);
        father.appendChild(element);
    }

    /**
     * adds an Element with two attributs to a DOM-tree.
     *
     * @param document    Document: the DOM-tree to add to
     * @param father      Element: the new element will be inserted directly under this Element in the tree
     * @param name        String: the name of the new element
     * @param attOneName  String: the name of the 1st attribut
     * @param attOneValue String: the value of the 1st attribut
     * @param attTwoName  String: the name of the 2nd attribut
     * @param attTwoValue String: the value of the 2nd attribut
     */
    public static void addElement(Document document, Element father,
                                  String name, String attOneName, String attOneValue,
                                  String attTwoName, String attTwoValue) {
        Element element = document.createElement(name);
        element.setAttribute(attOneName, attOneValue);
        element.setAttribute(attTwoName, attTwoValue);
        father.appendChild(element);
    }

    /**
     * Transforms an xml-file (xmlFilename) using an xsl-file (xslFilename) and writes the output into file
     * (outputFilename).
     *
     * @param xmlFile        File: the xml-source-file to be transformed
     * @param xslFile        File: the xsl-file with the transformation rules
     * @param outputFilename String: the name of the file the result will be written to
     */
    public static void applyXSL(File xmlFile, File xslFile,
                                String outputFilename) {
        try {
            // DocumentBuilderFactory docBFactory = DocumentBuilderFactory
            // .newInstance();
            // DocumentBuilder docBuilder = docBFactory.newDocumentBuilder();
            StreamSource xslStream = new StreamSource(xslFile);
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer(xslStream);
            StreamSource xmlStream = new StreamSource(xmlFile);
            StreamResult result = new StreamResult(new BufferedWriter(
                new FileWriter(outputFilename)));
            transformer.transform(xmlStream, result);
            result.getWriter().close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}