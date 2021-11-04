package desmoj.extensions.space3D;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import desmoj.core.simulator.Model;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class loads the Layout XML file into the Model. The XML file should contain the details about the pre-defined
 * tracks and SpatialObjects.
 *
 * @author Fred Sun
 */
public abstract class LayoutLoader {

    //the model this LayoutLoader is working for.
    protected Model _model;
    //the SpatialLayoutManager
    protected SpatialLayoutManager _layoutManager;
    //the document builder for reading the xml file.
    private DocumentBuilder _builder;

    //	//saves the created SpatialObejcts for referring to its position
    //	//at the creation of the tracks
    //	private static HashMap<String,SpatialObject> _spatialObjectsMap;

    /**
     * Constructs a LayoutLoader.
     *
     * @param model The model this LayoutLoader is working for.
     */
    public LayoutLoader(Model model) {
        _model = model;
        _layoutManager = SpatialLayoutManager.getSpatialLayoutManager();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        try {
            _builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a SimpleLocation object based on the information the Track Element contains.
     *
     * @param location The element object extracted from the XML layout file.
     * @return A SimpleLocation object.
     */
    protected SimpleLocation createSimpleLocation(Element location) {
        SimpleLocation sLocation = new SimpleLocation(this._model,
            location.getAttribute("Name"),
            location.getAttribute("Type"),
            false);
        NodeList position = location.getElementsByTagName("Position");
        //get the position
        for (int i = 0; i < position.getLength(); i++) {
            Element positionElement = (Element) position.item(i);
            double x = new Double(positionElement.getAttribute("x"));
            double y = new Double(positionElement.getAttribute("y"));
            double z = new Double(positionElement.getAttribute("z"));
            sLocation.setPosition(new Length(x), new Length(y), new Length(z));
        }
        return sLocation;
    }

    /**
     * Create a SimpleTrack object based on the information the Track Element contains.
     *
     * @param track The element object extracted from the XML layout file.
     * @return A SimpleTrack object.
     */
    protected SimpleTrack createSimpleTrack(Element track) {
        //the track name
        String name = track.getAttribute("Name");
        //the start location name
        String startName = track.getAttribute("Start");
        //the name of the exit point at the start location
        String exitPointName = null;
        if (track.hasAttribute("ExitPoint")) {
            exitPointName = track.getAttribute("ExitPoint");
        }
        //the destination location name
        String destinationName = track.getAttribute("Destination");
        //the name of the entry point at the destination position
        String entryPointName = null;
        if (track.hasAttribute("EntryPoint")) {
            entryPointName = track.getAttribute("EntryPoint");
        }
        //get whether this track should be trace, default is false
        boolean showInTrace = false;
        if (track.hasAttribute("ShowInTrace")) {
            String sIT = track.getAttribute("ShowInTrace");
            if (sIT.equals("true") || sIT.equals("True") || sIT.equals("1")) {
                showInTrace = true;
            }
        }

        //get the start position
        Length[] startPosition;
        if (exitPointName == null) {
            startPosition = new Length[3];
            SpatialObject start = _layoutManager.getSpatialObject(startName);
            startPosition[0] = start.getPosX();
            startPosition[1] = start.getPosY();
            startPosition[2] = start.getPosZ();
        } else {
            startPosition = _layoutManager.getSpatialObject(startName).getExitPointGlobal(exitPointName);
        }

        //get the destination position
        Length[] destinationPosition;
        if (entryPointName == null) {
            destinationPosition = new Length[3];
            SpatialObject destination = _layoutManager.getSpatialObject(destinationName);
            destinationPosition[0] = destination.getPosX();
            destinationPosition[1] = destination.getPosY();
            destinationPosition[2] = destination.getPosZ();
        } else {
            destinationPosition = _layoutManager.getSpatialObject(destinationName).getEntryPointGlobal(entryPointName);
        }

        //get way points
        NodeList wayPoints = track.getElementsByTagName("WayPoint");

        //if the number of the wayPoints are 0
        if (wayPoints.getLength() == 0) {
            //return a new SimpleTrack without way points
            return new SimpleTrack(_model, name,
                startName, destinationName, exitPointName,
                entryPointName, startPosition, destinationPosition,
                null, showInTrace);
        } else {
            //read the way points
            ArrayList<double[]> wayPointsList = new ArrayList<double[]>();
            int numList = wayPoints.getLength();
            for (int i = 0; i < numList; i++) {
                Element wayPoint = (Element) wayPoints.item(i);
                double[] position = new double[3];
                position[0] = Double.valueOf(wayPoint.getAttribute("x"));
                position[1] = Double.valueOf(wayPoint.getAttribute("y"));
                position[2] = Double.valueOf(wayPoint.getAttribute("z"));
                wayPointsList.add(position);
            }
            //return the track with way points
            return new SimpleTrack(_model, name,
                startName, destinationName, exitPointName,
                entryPointName, startPosition, destinationPosition,
                wayPointsList, showInTrace);
        }
    }

    /**
     * Creates a SpatialObject based on the SpatialObject element specified in the layout file.
     *
     * @param spatialObject The specified SpatialObject to be added.
     * @return A SpatialObject which is specified by the element.
     */
    protected abstract SpatialObject createSpatialObject(Element spatialObject);

    /**
     * Creates a track object based on the track element specified in the layout file.
     *
     * @param track The specified track to be added.
     * @return A Track object which is specified by the element.
     */
    protected abstract Track createTrack(Element track);

    /**
     * Loads the given XML file, create the specified Tracks and SpatialObjects. The tracks will be registered into the
     * SpatialLayoutManager.
     *
     * @param filePathName A pathname string
     */
    public void loadLayout(String filePathName) {
        Document doc = null;

        //parse the xml layout file
        try {
            doc = _builder.parse(new File(filePathName));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //get the root element
        Element layout = doc.getDocumentElement();

        //get all elements of SpatialObjects
        NodeList spatialObjectsList = layout.getElementsByTagName("SpatialObjects");

        //the number of the the elements SpatialObjects
        int numSpatialObjectsList = spatialObjectsList.getLength();

        //go through all the elements SpatialObjects
        for (int i = 0; i < numSpatialObjectsList; i++) {
            //get the elements of SpatiaObject
            NodeList spatialObjectList = ((Element) spatialObjectsList.item(i)).getElementsByTagName("SpatialObject");
            int numSpatialObjectList = spatialObjectList.getLength();

            //go through every single SpatialObject in SpatialObjects
            for (int j = 0; j < numSpatialObjectList; j++) {
                //create it and save it for dealing with the tracks later
                Element spatialObject = (Element) spatialObjectList.item(j);
                SpatialObject modelSpatialObject = null;
                if (spatialObject.getAttribute("Type").equals("SimpleLocation")) {
                    modelSpatialObject = this.createSimpleLocation(spatialObject);
                } else {
                    modelSpatialObject = this.createSpatialObject(spatialObject);
                }
                _layoutManager.addSpatialObject(spatialObject.getAttribute("Name"), modelSpatialObject);
            }
        }

        //the same like for the SpatialObejct
        NodeList tracksList = layout.getElementsByTagName("Tracks");
        int numTracksList = tracksList.getLength();

        for (int i = 0; i < numTracksList; i++) {
            NodeList trackList = ((Element) tracksList.item(i)).getElementsByTagName("Track");
            int numTrackList = trackList.getLength();
            for (int j = 0; j < numTrackList; j++) {
                Element track = (Element) trackList.item(j);
                //register it into the layout manager
                if (track.getAttribute("Type").equals("SimpleTrack")) {
                    _layoutManager.addTrack(this.createSimpleTrack(track));
                } else {
                    _layoutManager.addTrack(this.createTrack(track));
                }
            }
        }
    }
}
