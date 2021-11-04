package desmoj.extensions.visualization2d.engine.model;

import java.awt.Image;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import desmoj.extensions.visualization2d.engine.command.Cmd;
import desmoj.extensions.visualization2d.engine.orga.ClassBasic;


/**
 * All entities of same entity-type has the same basic properties. These are: - same size in animation (width/heigth) -
 * same possible states with same icon for a state - same possible attribute-key's
 *
 * @author christian.mueller@th-wildau.de For information about subproject: desmoj.extensions.visualization2d please
 *     have a look at: http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class EntityType implements ClassBasic {

    public static final int SHOW_NAME = 1;
    public static final int SHOW_ICON = 2;

    /**
     * static hashtable with all entity-types
     */
    private final String id;
    private final int width;
    private final int height;
    private int show;
    private final TreeMap<String, String> possibleStates;
    private final TreeSet<String> possibleAttributes;
    private final Model model;

    /**
     * @param id        EntityTypeId must be unique
     * @param modell
     * @param width     pixel-size in animation
     * @param height    pixel-size in animation
     * @param posStates Attribute[] with key:state and value:iconId
     * @param posAttr   String[] with attribute-keys
     * @param show      combination of show-flags
     * @throws ModelException
     */
    public EntityType(String id, Model model, int width, int height,
                      Attribute[] posStates, String[] posAttr, int show) throws ModelException {
        this.id = id;
        this.model = model;
        this.width = width;
        this.height = height;
        this.show = show;
        //System.out.println("in EntityType Konstruktor id: "+id);

        // Erfassung der moeglichen Zustaende
        // zu jedem state(key) wird eine imageId/value) erfasst.
        this.possibleStates = new TreeMap<String, String>();
        if (posStates != null) {
            for (int i = 0; i < posStates.length; i++) {
                if (!this.model.containsImageId(posStates[i].getValue())) {
                    throw new ModelException("In EntityType id: " + id +
                        "  state: " + posStates[i].getKey() +
                        "  is represented by image id: " + posStates[i].getValue() +
                        ". This Image is unknown.");
                }
                this.possibleStates.put(posStates[i].getKey(), posStates[i].getValue());
            }
        }
        // animationError ist ein Standard Zustand
        this.possibleStates.put("animationError", "animationError");

        // Erfassung der moeglichen Attribut Key's (keine Values)
        this.possibleAttributes = new TreeSet<String>();
        if (posAttr != null) {
            for (int i = 0; i < posAttr.length; i++) {
                this.possibleAttributes.add(posAttr[i]);
            }
        }
        // name und code sind Standard Attribute
        this.possibleAttributes.add(Cmd.NAME_KEY);
        this.possibleAttributes.add(Cmd.VELOCITY_KEY);
        this.possibleAttributes.add(Cmd.PRIORITY_KEY);

        // fuegt den EntityType in die entityTypes-Liste ein
        if (id != null) {
            model.getEntityTyps().add(this);
        }
    }

    /**
     * add an attribute-key as possible
     *
     * @param attr
     * @return true, when successful
     */
    public boolean addPossibleAttribut(String attr) {
        return this.possibleAttributes.add(attr);
    }

    /**
     * check if an key exist.
     *
     * @param key
     * @return true, when key exist
     */
    public boolean existPossibleAttribut(String key) {
        return this.possibleAttributes.contains(key);
    }

    /**
     * get all possible attribute-key's
     *
     * @return array of attribute-keys
     */
    public String[] getPossibleAttributes() {
        //return (String[])this.possibleAttributes.toArray();
        Iterator<String> it = this.possibleAttributes.iterator();
        String[] out = new String[this.possibleAttributes.size()];
        int i = 0;
        while (it.hasNext()) {
            out[i++] = it.next();
        }
        return out;

    }


    public String getId() {
        return this.id;
    }


    public Model getModell() {
        return this.model;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    /**
     * get all possible states
     *
     * @return array of states
     */
    public String[] getPossibleStates() {
        //return (String[])this.possibleStates.keySet().toArray();
        Iterator<String> it = this.possibleStates.keySet().iterator();
        String[] out = new String[this.possibleStates.size()];
        int i = 0;
        while (it.hasNext()) {
            out[i++] = it.next();
        }
        return out;
    }

    /**
     * check if an state exist
     *
     * @param state
     * @return true, when state exist
     */
    public boolean existPosibleState(String state) {
        return this.possibleStates.containsKey(state);
    }

    /**
     * get image-id of state
     *
     * @param state
     * @return image-id
     */
    public String getImageId(String state) {
        String out = this.possibleStates.get("animationError");
        if (this.possibleStates.containsKey(state)) {
            out = this.possibleStates.get(state);
        }
        return out;
    }

    /**
     * get Image of state
     *
     * @param state
     * @return image
     * @throws ModelException
     */
    public Image getImage(String state) throws ModelException {
        Image out = this.model.getImage(this.getImageId(state));
        if (out == null) {
            throw new ModelException("In EntityType id: " + this.getId() +
                "  state: " + state + ", there is no associated image!");
        }
        return out;
    }

    /**
     * Show is the sum of EntityType.SHOW_ Flags
     *
     * @return
     */
    public int getShow() {
        return this.show;
    }

    /**
     * Show is the sum of EntityType.SHOW_ Flags
     *
     * @param show
     */
    public void setShow(int show) {
        this.show = show;
    }


}
