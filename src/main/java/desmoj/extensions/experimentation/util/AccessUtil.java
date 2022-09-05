package desmoj.extensions.experimentation.util;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import desmoj.core.util.AccessPoint;
import desmoj.core.util.MutableAccessPoint;

/**
 * Utility class for working with access points.
 *
 * @author Nicolas Knaak
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 */

public class AccessUtil {

    /**
     * Put an access point into the given map. The access point will be the value and the access point's name will be
     * the key.
     *
     * @param accessPoints a map containing <String, AccessPoint> pairs.
     * @param apt          an access point to be added to the map.
     */
    public static void put(Map accessPoints, AccessPoint apt) {
        accessPoints.put(apt.getName(), apt);
    }

    /**
     * Retrieves the value of an access point with a given name from the map.
     *
     * @param name         name of an access point
     * @param accessPoints map containing <String,AccessPoint> pairs.
     * @return current value of the access point
     */
    public static Object getValue(String name, Map accessPoints) {
        AccessPoint ap = (AccessPoint) accessPoints.get(name);
        return ap.getValue();
    }

    /**
     * Convenience method to retrieve the String value of an access point with a given name from the map.
     *
     * @param name         name of an access point
     * @param accessPoints map containing <String,AccessPoint> pairs.
     * @return current string value of the access point
     */
    public static String getStringValue(String name, Map accessPoints) {
        return (String) getValue(name, accessPoints);
    }

    /**
     * Convenience method to retrieve the TimeUnit value of an access point with a given name from the map.
     *
     * @param name         name of an access point
     * @param accessPoints map containing <String,AccessPoint> pairs.
     * @return current string value of the access point
     */
    public static TimeUnit getTimeUnitValue(String name, Map accessPoints) {
        return (TimeUnit) getValue(name, accessPoints);
    }

    /**
     * Convenience method to retrieve the boolean value of an access point with a given name from the map.
     *
     * @param name         name of an access point
     * @param accessPoints map containing <String,AccessPoint> pairs.
     * @return current boolean value of the access point
     */
    public static boolean getBooleanValue(String name, Map accessPoints) {
        return ((Boolean) getValue(name, accessPoints)).booleanValue();
    }

    /**
     * Convenience method to retrieve the int value of an access point with a given name from the map.
     *
     * @param name         name of an access point
     * @param accessPoints map containing <String,AccessPoint> pairs.
     * @return current int value of the access point
     */
    public static int getIntValue(String name, Map accessPoints) {
        return ((Integer) getValue(name, accessPoints)).intValue();
    }

    /**
     * Convenience method to retrieve the double value of an access point with a given name from the map.
     *
     * @param name         name of an access point
     * @param accessPoints map containing <String,AccessPoint> pairs.
     * @return current double value of the access point
     */
    public static double getDoubleValue(String name, Map accessPoints) {
        return ((Double) getValue(name, accessPoints)).doubleValue();
    }

    /**
     * @return an array of all contained access point's names.
     */
    public static String[] getAccessPointNames(Map accessPoints) {
        Object[] oa = accessPoints.keySet().toArray();
        String[] na = new String[oa.length];
        System.arraycopy(oa, 0, na, 0, na.length);
        return na;
    }

    /**
     * @param searchedName an access point name.
     * @return the internal index of a given access point name. If the access point does not exist -1 is returned
     */
    public static int getIndexof(String searchedName, Map accessPoints) {
        String actualName = "";
        int index = -1;
        String[] accPtNames = getAccessPointNames(accessPoints);
        for (int i = 0; !actualName.equals(searchedName); i++) {
            actualName = accPtNames[i];
            index = i;
        }
        return index;
    }

    /**
     * @return an object array of all access points' current values.
     */
    public static Object[] getAccessPointValues(Map accessPoints) {
        Object[] values = accessPoints.values().toArray();
        for (int i = 0; i < values.length; i++) {
            values[i] = ((AccessPoint) values[i]).getValue();
        }
        return values;
    }

    /**
     * @return the access points as an array.
     */
    public static AccessPoint[] getAccessPoints(Map accessPoints) {
        Object[] pa = accessPoints.values().toArray();
        AccessPoint[] pa2 = new AccessPoint[pa.length];
        System.arraycopy(pa, 0, pa2, 0, pa2.length);
        return pa2;
    }

    /**
     * tests if this map contains an access point with the given name.
     *
     * @param name an access point's name
     * @param map  a map containing access points
     * @return true iff the map contains an access point with the given name
     */
    public static boolean contains(String name, Map accessPoints) {
        return accessPoints.containsKey(name);
    }

    /**
     * tests if the access point with the given name is mutable.
     *
     * @param name         an access point's name
     * @param accessPoints a map containing access points
     * @return true iff the access point with the given name is a MutableAccessPoint
     */
    public static boolean isMutable(String name, Map accessPoints) {
        AccessPoint p = (AccessPoint) accessPoints.get(name);
        if (p == null) {
            return false;
        } else {
            return (p instanceof MutableAccessPoint);
        }
    }

    /**
     * sets the specified access point to the given value. Returns true if this was successful (i.e. if the access point
     * is mutable) or false otherwise.
     *
     * @param accName      the name of the access point to change
     * @param value        the value to set the accessed attribute to.
     * @param accessPoints a map containing access points
     * @return status of "set" operation
     */
    public static boolean setValue(Map accessPoints, String accName,
                                   Object value) {
        AccessPoint p = (AccessPoint) accessPoints.get(accName);
        if (p instanceof MutableAccessPoint) {
            ((MutableAccessPoint) p).setValue(value);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Inits the values of all access points in the given map from a map of values. This map must contain key/value
     * pairs where the key is a string corresponding to an access point's name and the value is an object fitting the
     * accessed attributes type.
     *
     * @param accessPoints a map containing access points
     * @param m            a java.util.Map to initialize the given map of access points from.
     */
    public static void init(Map accessPoints, Map m) {
        for (Iterator i = m.keySet().iterator(); i.hasNext(); ) {
            String name = (String) i.next();
            String value = (String) m.get(name);
            AccessPoint p = (AccessPoint) accessPoints.get(name);
            if (p instanceof MutableAccessPoint) {
                MutableAccessPoint mp = (MutableAccessPoint) p;
                Object oldVal = mp.getValue();
                Class type = oldVal.getClass();
                Object o = parseString(value, type, oldVal);
                if (o == null) {
                    throw new RuntimeException("** Error: Cannot init parameter "
                        + name + ". Unsupported type " + type);
                } else {
                    mp.setValue(o);
                }
            } else {
                throw new RuntimeException("** Error: Cannot init parameter " + name
                    + ". AccessPoint is not mutable.");
            }
        }
    }

    /**
     * Helper method to parse a string and return an object of the given type.
     *
     * @param value the string to be parsed
     * @param type  the type of the resulting object
     * @return Object object initialized from the given string
     */
    private static Object parseString(String value, Class type, Object oldVal) {
        Object o = null;
        if (type.equals(Boolean.class)) {
            o = Boolean.valueOf(value);
        } else if (type.equals(Byte.class)) {
            o = Byte.valueOf(value);
        } else if (type.equals(Short.class)) {
            o = Short.valueOf(value);
        } else if (type.equals(Integer.class)) {
            o = Integer.valueOf(value);
        } else if (type.equals(Long.class)) {
            o = Long.valueOf(value);
        } else if (type.equals(Float.class)) {
            o = Float.valueOf(value);
        } else if (type.equals(Double.class)) {
            o = Double.valueOf(value);
        } else if (type
            .equals(Filename.class)) {
            boolean isDir = oldVal != null && ((Filename) oldVal).isDirectory();
            o = new Filename(value, isDir);
        } else {
            try {
                Constructor c = type
                    .getConstructor(String.class);
                if (c != null) {
                    o = c.newInstance(value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return o;
    }

}
