package desmoj.extensions.applicationDomains.production;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;

/**
 * A PartsList is a listing of all the different kinds of parts and the number (quantity) of them needed to produce a
 * new part (or product). It is used in conjunction with a <code>WorkStation</code> in order to tell the work station
 * how many and of which kind of parts should be assembled there. The user can instantiate a PartsList either by
 * providing to arrays of the same length, one with all the different kinds of parts and one with the respective number
 * of each different part, or by instantiating a PartsList with the number of different kinds of parts and then adding
 * pairs of [kindOfPart, number of this kind of parts] using the <code>addPart</code> method.
 *
 * @author Soenke Claassen
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class PartsList extends ModelComponent {

    /**
     * The number of different kinds of parts, needed for the assembly.
     */
    private int numberOfDiffParts;

    /**
     * An array of the different kinds of the parts, needed for the assembly.
     */
    private Class[] kindOfParts;

    /**
     * An array of the number of each different kind of part, needed for the assembly.
     */
    private int[] numberOfParts;

    /**
     * Constructs a PartsList with the given arrays of different kinds of parts and the quantities of that kind of part.
     * Both arrays must be of the same size, of course. The PartsList leaves no messages in the trace.
     *
     * @param ownerModel   desmoj.Model : The model this PartsList is associated to.
     * @param name         java.lang.String : The name of this PartsList.
     * @param kindsOfParts java.lang.Class[] : The array containing the different kinds of parts.
     * @param numOfParts   int[] : The array constaining the number of each kind of part.
     */
    public PartsList(Model ownerModel, String name, Class[] kindsOfParts,
                     int[] numOfParts) {

        super(ownerModel, name, false); // make a ModelComponent with no trace
        // output

        // check if the given arrays have the same length
        if (kindsOfParts.length != numOfParts.length) {
            sendWarning(
                "The given number of different kinds of parts is not the  "
                    + "same like the number of quantities of each kind. No PartsList will "
                    + "be created!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: "
                    + "PartsList(Model ownerModel, String name, Class[] kindsOfParts, "
                    + "int[] numOfParts)",
                "Both arrays (kindsOfParts, numOfParts) must have the same length.",
                "Make sure to provide two arrays with the same length to construct a "
                    + "valid PartsList.");

            return; // ignore that rubbish and just return
        } else {
            this.numberOfDiffParts = kindsOfParts.length;
        }

        // save both arrays
        this.kindOfParts = kindsOfParts;
        this.numberOfParts = numOfParts;

    }

    /**
     * Constructs a PartsList with enough space to store a number of different kinds of parts with the respective
     * quantity needed of each kind. Use the method <code>addPart</code> to add a pair of [kind of part, quantity of
     * that kind of part]. The PartsList leaves no messages in the trace.
     *
     * @param ownerModel             desmoj.Model : The model this PartsList is associated to.
     * @param name                   java.lang.String : The name of this PartsList.
     * @param numberOfDifferentParts int : The number of different kinds of parts. Should not be zero or negative!
     */
    public PartsList(Model ownerModel, String name, int numberOfDifferentParts) {

        super(ownerModel, name, false); // make a ModelComponent with no trace
        // output

        // check the number of different kinds of parts
        if (numberOfDifferentParts < 1) {
            sendWarning(
                "The given number of different kinds of parts is zero or "
                    + "negative. No PartsList will be created!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Constructor: "
                    + "PartsList(Model ownerModel, String name, int numberOfDifferentParts)",
                "A PartsList with no parts does not make sense.",
                "Make sure to provide a positive valid number of parts to create a "
                    + "PartsList.");

            return; // ignore that rubbish and just return
        } else {
            this.numberOfDiffParts = numberOfDifferentParts;
        }

        // create the arrays of the given size
        kindOfParts = new Class[numberOfDiffParts];
        numberOfParts = new int[numberOfDiffParts];
    }

    /**
     * Adds a pair of [kind of part, quantity of that kind of part] to the PartsList. There can not be added more pairs
     * than the PartsList can hold. To check how many pairs a PartList can hold use the method
     * <code>getNumberOfDiffParts()<code>.
     *
     * @param kindOfPart    java.lang.Class : The kind of part as a Class object.
     * @param numberOfParts int : The number of that kind of part.
     */
    public void addPart(Class kindOfPart, int numberOfParts) {

        // check the number of parts
        if (numberOfParts < 1) {
            sendWarning(
                "The number of parts is zero or negative. The attempted "
                    + "insertion of a new pair into the PartsList will be ignored!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "void addPart(Class kindOfPart, int numberOfParts)",
                "A negative or zero quantity of parts does not make sense.",
                "Make sure to provide a positive valid quantity of parts when adding "
                    + "it to the PartsList.");

            return; // do nothing, just return
        }

        // flag, if the insertion of the pair was successful
        boolean successful = false;

        // loop through the arrays
        for (int i = 0; i < numberOfDiffParts; i++) {
            // check for a free space
            if (kindOfParts[i] == null) {
                this.kindOfParts[i] = kindOfPart;
                this.numberOfParts[i] = numberOfParts;

                successful = true; // sucessfully added to the arrays
                break; // leave the loop
            }
        }

        // if no place was found
        if (!successful) {
            sendWarning(
                "The PartsList is full already. No more elements can be added "
                    + "to the list. The attempted insertion of a new pair will be ignored!",
                getClass().getName()
                    + ": "
                    + getQuotedName()
                    + ", Method: "
                    + "void addPart(Class kindOfPart, int numberOfParts)",
                "The size of this PartsList is not big enough to hold another pair.",
                "Make sure that the PartList is not full already before trying to add "
                    + "another pair.");

            // do nothing
        }
    }

    /**
     * Returns the index of the given kind of part (
     * <code>java.lang.Class</code>). The index is indicating the position in
     * the array of kindOfParts. If the given kind of part is not found in this PartsList, undefinded (-1) will be
     * returned.
     *
     * @param kind java.lang.Class : The kind of part given as an object of
     *             <code>java.lang.Class</code>.
     * @return int : The index of the given kind of part (
     *     <code>java.lang.Class</code>). That is the position in the
     *     array of kindOfParts. Or undefined (-1) if no such kind of part can be found in this PartsList.
     */
    public int getIndexOfKind(Class kind) {

        // check kind
        if (kind == null) {
            sendWarning("The given kind of the part is only a null pointer. "
                    + "No index can be returned for that kind of part!",
                getClass().getName() + ": " + getQuotedName()
                    + ", Method: " + "int getIndexOfKind(Class kind)",
                "The given parameter is only a null pointer.",
                "Make sure to provide a valid Class variable for the kind of part you "
                    + "are looking for.");

            return -1; // ignore that rubbish and just return (-1) undefined
        }

        // search the whole array
        for (int i = 0; i < numberOfDiffParts; i++) {
            if (kind == kindOfParts[i]) {
                return i;
            }
        }

        // nothing found -> return undefined
        return -1;
    }

    /**
     * Returns the kind of the part at index i as a Class object.
     *
     * @param index int : The index in the PartsList for which the kind of the part will be returned. The index runs
     *              from zero to (numberOfDiffParts - 1).
     * @return java.lang.Class : The Class object identifying the kind of the part. If there is no entry at this index
     *     position in the PartsList <code>null</code> will be returned.
     */
    public Class getKindOfPart(int index) {

        // check the index
        if (index >= numberOfDiffParts || index < 0) {
            sendWarning(
                "The given index is negative or greater than the number of "
                    + "different parts in the PartsList. No kind of part can be returned for "
                    + "that index!", getClass().getName() + ": "
                    + getQuotedName() + ", Method: "
                    + "Class getkindOfPart(int index)",
                "The given index is out of bounds.",
                "Make sure to provide a positive valid index when trying to retrieve "
                    + "the kind of part for that index.");
        }

        return kindOfParts[index];
    }

    /**
     * Returns the whole array of all kinds of parts listed in this PartsList.
     *
     * @return java.lang.Class[] : the whole array of all kinds of parts listed in this PartsList.
     */
    public Class[] getKindOfParts() {

        return kindOfParts.clone();
    }

    /**
     * Returns the number of different parts (that is the number of entries) in this PartsList.
     *
     * @return int : The number of different parts (that is the number of entries) in this PartsList.
     */
    public int getNumberOfDiffParts() {

        return numberOfDiffParts;
    }

    /**
     * Returns the whole array of all the different quantities of all different kinds of parts listed in this
     * PartsList.
     *
     * @return int[] : The whole array of all the different quantities of all different kinds of parts listed in this
     *     PartsList.
     */
    public int[] getNumberOfParts() {

        return numberOfParts.clone();
    }

    /**
     * Returns the quantity of the part at the index i of this PartsList.
     *
     * @param index int : The index in the PartsList for which the quantity of the part will be returned. The index runs
     *              from zero to (numberOfDiffParts - 1).
     * @return int : The quantity of the kind of part at index i of this PartsList.
     */
    public int getQuantityOfPart(int index) {

        // check the index
        if (index >= numberOfDiffParts || index < 0) {
            sendWarning(
                "The given index is negative or greater than the number of "
                    + "different parts in the PartsList. No kind of part can be returned for "
                    + "that index!", getClass().getName() + ": "
                    + getQuotedName() + ", Method: "
                    + "int getQuantityOfPart(int index)",
                "The given index is out of bounds.",
                "Make sure to provide a positive valid index when trying to retrieve "
                    + "the quantity of part for that index.");
        }

        return numberOfParts[index];
    }
}