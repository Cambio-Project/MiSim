package desmoj.extensions.visualization3d;

import org.scijava.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.scijava.java3d.BranchGroup;
import org.scijava.java3d.Link;
import org.scijava.java3d.SharedGroup;
import org.scijava.java3d.Transform3D;
import org.scijava.java3d.TransformGroup;
import org.scijava.java3d.loaders.IncorrectFormatException;
import org.scijava.java3d.loaders.ParsingErrorException;
import org.scijava.java3d.loaders.Scene;
import org.scijava.java3d.loaders.objectfile.ObjectFile;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The ModelManager loads the Model-XML file where the SpatialObject and its associated 3D-Model is listed. The
 * decoration models will also be listed in the file and loaded by this class.
 * </br></br>
 * The associated model will be load and stored within this class.
 *
 * @author Fred Sun
 */
public class ModelManager {

    //stores the active 3D-models
    private final HashMap<String, BranchGroup> _modelMap;

    //a list contains activ objects
    private final NodeList _modelList;

    //a list contains the decoration items
    private final NodeList _decorList;

    //a wavefront file loader
    private final ObjectFile _objLoader;

    /**
     * Constructs a ModelManager and load the XML file where the 3D-models are specified.
     *
     * @param filePath The path to the XML file where the 3D-models are specified.
     */
    public ModelManager(String filePath) {
        _objLoader = new ObjectFile();
        _modelMap = new HashMap<String, BranchGroup>();

        //create new DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        //create a new document object
        Document doc = null;

        //parse the xml layout file
        try {
            doc = builder.parse(new File(filePath));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get the root element
        Element layout = doc.getDocumentElement();

        //get all the decorations
        _decorList = layout.getElementsByTagName("Decorations");

        //get all the dynamic objects
        _modelList = layout.getElementsByTagName("ActiveObjects");
    }

    /**
     * Creates a BranchGroup contains all the decoration models specified in the XML file. Every model will only be
     * loaded once. The loaded model will be linked to a TransformGroup which is responsible for the transformation. The
     * nodePath is:</br></br>
     * <p>
     * BranchGroup(decoration root)->TransformGroup->Link->SharedGroup-> BranchGroup(loaded model)
     *
     * @return A BranchGroup contains all the decoration models specified in the XML file.
     */
    BranchGroup loadDecoration() {
        BranchGroup decor = new BranchGroup();
        HashMap<String, SharedGroup> decorMap = new HashMap<String, SharedGroup>();

        //the number of all Decorations
        int numDecorations = _decorList.getLength();

        //go through all the Decorations
        for (int i = 0; i < numDecorations; i++) {
            //get decorations element
            NodeList decorItemList = ((Element) _decorList.item(i)).getElementsByTagName("DecorItem");
            //the number of all decorItems
            int numDecorItems = decorItemList.getLength();

            //go through every single decorItem in decorations
            for (int j = 0; j < numDecorItems; j++) {
                //create it
                Element decorItem = (Element) decorItemList.item(j);

                //check whether the model has been loaded already
                //load it and save it, if not
                String filePath = decorItem.getAttribute("FileName");
                if (!decorMap.containsKey(filePath)) {
                    Scene modelScene = null;
                    try {
                        modelScene = _objLoader.load(filePath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IncorrectFormatException e) {
                        e.printStackTrace();
                    } catch (ParsingErrorException e) {
                        e.printStackTrace();
                    }
                    //add the loaded model into a SharedGroup
                    SharedGroup sGroup = new SharedGroup();
                    sGroup.addChild(modelScene.getSceneGroup());
                    sGroup.compile();
                    //save it for later
                    decorMap.put(filePath, sGroup);
                }

                //create the TransformGroup for the DecorItem
                TransformGroup tGroup = new TransformGroup();
                tGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
                //link it to the associated model
                Link link = new Link(decorMap.get(filePath));
                decor.addChild(tGroup);
                tGroup.addChild(link);

                //set the transform
                //get all transform elements
                NodeList transformList = decorItem.getElementsByTagName("*");
                //get the number of the transform elements
                int numTransforms = transformList.getLength();
                //the transform3d to be transformed
                Transform3D t3d = new Transform3D();
                //go through every element
                for (int k = 0; k < numTransforms; k++) {
                    Element transformElement = (Element) transformList.item(k);
                    //if it is a rotate transform
                    if (transformElement.getTagName().equals("Rotate")) {
                        Attr x = transformElement.getAttributeNode("x");
                        Attr y = transformElement.getAttributeNode("y");
                        Attr z = transformElement.getAttributeNode("z");
                        if (x != null) {
                            t3d.rotX(new Double(x.getValue()));
                        }
                        if (y != null) {
                            t3d.rotY(new Double(y.getValue()));
                        }
                        if (z != null) {
                            t3d.rotZ(new Double(z.getValue()));
                        }
                    }
                    //if it is scale transform
                    if (transformElement.getTagName().equals("Scale")) {
                        double x = new Double(transformElement.getAttribute("x"));
                        double y = new Double(transformElement.getAttribute("y"));
                        double z = new Double(transformElement.getAttribute("z"));
                        Vector3d scaleVec = new Vector3d(x, y, z);
                        t3d.setScale(scaleVec);
                    }
                    //if it is a translate transform
                    if (transformElement.getTagName().equals("Position")) {
                        double x = new Double(transformElement.getAttribute("x"));
                        double y = new Double(transformElement.getAttribute("y"));
                        double z = new Double(transformElement.getAttribute("z"));
                        Vector3d transVec = new Vector3d(x, y, z);
                        t3d.setTranslation(transVec);
                    }
                }
                //set the transformation to the TransformGroup
                tGroup.setTransform(t3d);
            }
        }

        return decor;
    }

    /**
     * Gets the model associated to the given type.
     *
     * @param type The type of the model.
     * @return A BranchGroup contains the loaded model.
     */
    BranchGroup getModel(String type) {
        if (_modelMap.containsKey(type)) {
            //if the model has been loaded already, return a clone.
            return (BranchGroup) _modelMap.get(type).cloneTree(true);
        } else {
            //if the model hans't been loaded. Load it from the file.
            //go through all ActiveObject elements
            for (int i = 0; i < _modelList.getLength(); i++) {
                NodeList modelList = ((Element) _modelList.item(i)).getElementsByTagName("Model");
                //get all model elements
                for (int j = 0; j < modelList.getLength(); j++) {
                    //see if it matches the type
                    Element modelElement = (Element) modelList.item(j);
                    String modelType = modelElement.getAttribute("VisualModel");
                    //if matches, load it
                    if (modelType.equals(type)) {
                        String filePath = modelElement.getAttribute("FileName");
                        Scene modelScene = null;
                        try {
                            modelScene = _objLoader.load(filePath);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IncorrectFormatException e) {
                            e.printStackTrace();
                        } catch (ParsingErrorException e) {
                            e.printStackTrace();
                        }

                        BranchGroup modelBranch = modelScene.getSceneGroup();

                        //ROUTINE FOR EXAMINE THE LOADED SCENE
                        //						System.out.println(modelBranch.numChildren());
                        //						for(int in=0;in<modelBranch.numChildren();in++){
                        //							Shape3D s = (Shape3D)modelBranch.getChild(in);
                        //							Appearance a = s.getAppearance();
                        //							Material m = a.getMaterial();
                        //							System.out.println(m);
                        //						}
                        _modelMap.put(type, modelBranch);
                        return (BranchGroup) modelBranch.cloneTree(true);
                    }
                }
            }
        }
        //if no model can be found, return null
        return null;
    }
}
