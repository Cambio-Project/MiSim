package desmoj.extensions.visualization2d.engine.model;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import desmoj.extensions.visualization2d.engine.command.Cmd;
import desmoj.extensions.visualization2d.engine.command.Parameter;
import desmoj.extensions.visualization2d.engine.modelGrafic.EntityGrafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.EntityPosition;
import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;


/**
 * An entity is a basic-object of animation. Every entity has an entity-type. Basic-properties of an entity e.g.
 * possible attributes or states are definied in entity-type. The history of all values are stored also. All grafical
 * properies are stored in EntityGrafic
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
public class Entity implements Basic {

    private final String id;
    private final EntityType entityTyp;
    /**
     * includes all grafical properties
     */
    private EntityGrafic grafic;
    private final Model model;

    /**
     * Hashtable of all attributes with it's historical values
     */
    private final Hashtable<String, List<Attribute>> attribute;
    /**
     * Historical vector of attributeId's of name-attribute
     */
    private final List<Attribute> name;
    /**
     * Historical vector of attributeId's of priority-attribute
     */
    private final List<Attribute> priority;
    /**
     * Historical vector of attributeId's of velocity-attribute
     */
    private final List<Attribute> velocity;
    /**
     * When an entity changes a container or is created or destroyed, this is stored here
     */
    private final List<Attribute> containerHistory;
    /**
     * Historical vector of states
     */
    private final List<Attribute> state;

    /**
     * An new entity will indexed automaticly in classContent hashtable
     *
     * @param model        used animation.model.Model
     * @param id           entityId must be unique
     * @param entityTypeId
     * @param state        initial state
     * @param time         creation-time
     * @throws ModelException
     */
    public Entity(Model model, String id, String entityTypeId, String state, long time) throws ModelException {
        // TODO Auto-generated constructor stub
        this.model = model;
        this.id = id;
        this.name = new LinkedList<Attribute>();
        this.name.add(new Attribute("name", Cmd.NAME_KEY, time));
        this.entityTyp = model.getEntityTyps().get(entityTypeId);
        if (this.entityTyp == null) {
            throw new ModelException("entityType is unknown! entityType: " + entityTypeId);
        }
        this.attribute = new Hashtable<String, List<Attribute>>();
        this.velocity = new LinkedList<Attribute>();
        this.velocity.add(new Attribute("velovity", Cmd.VELOCITY_KEY, time));
        this.priority = new LinkedList<Attribute>();
        this.priority.add(new Attribute("priority", Cmd.PRIORITY_KEY, time));
        this.state = new LinkedList<Attribute>();
        this.state.add(new Attribute("state", state, time));
        this.containerHistory = new LinkedList<Attribute>();
        this.changeContainer("Entity", id, "created", time);
        this.grafic = null;
        // Entity wird in Entity-Liste aufgenommen
        if (this.id != null) {
            model.getEntities().add(this);
        }
    }

    public Model getModel() {
        return this.model;
    }

    public String getId() {
        return this.id;
    }

    /**
     * actual value of name-attribute
     */
    public String getName() {
        return this.getAttribute(this.name.get(this.name.size() - 1).getValue());
    }

    /**
     * set a  new key of name-attribute This seted attribute is used as name attribute.
     *
     * @param name new key of name-attribute
     * @param time time of setting
     * @throws ModelException
     */
    public void setNameAttribute(String name, long time) throws ModelException {
        if (this.entityTyp.existPossibleAttribut(name)) {
            Attribute last = this.name.get(this.name.size() - 1);
            if (last != null && !last.getValue().equals(name.trim())) {
                this.name.add(new Attribute("name", name.trim(), time));
            }
        } else {
            throw new ModelException("Entity.setNameAttribute: " + name + " isn't a possibleAttribute");
        }
    }

    /**
     * get vector with all historical name-keys
     *
     * @return vector with all historical name-keys
     */
    public List<Attribute> getNameAttribute() {
        return this.name;
    }

    /**
     * get actual value of priority-attribute
     *
     * @return actual value of priority-attribute
     */
    public int getPriority() {
        int out = 0;
        String p = this.getAttribute(this.priority.get(this.priority.size() - 1).getValue());
        if (p != null) {
            try {
                out = Integer.parseInt(p);
            } catch (NumberFormatException e) {
                out = 1;
            }
        }
        return out;
    }

    /**
     * sets new value of priority-attribute
     *
     * @param p    new priority-value
     * @param time time of setting
     * @throws ModelException
     */
    public void setPriorityAttribute(String p, long time) throws ModelException {
        if (this.entityTyp.existPossibleAttribut(p)) {
            Attribute last = this.priority.get(this.priority.size() - 1);
            if (last != null && !last.getValue().equals(p.trim())) {
                this.priority.add(new Attribute("priority", p, time));
            }
            this.priority.add(new Attribute(Cmd.PRIORITY_KEY, p, time));
        } else {
            throw new ModelException("Entity.setPriorityAttribute: " + p + " isn't a possibleAttribute");
        }
    }

    /**
     * get historical vector of priorities
     *
     * @return historical vector of priorities
     */
    public List<Attribute> getPriorityAttribute() {
        return this.priority;
    }

    /**
     * get actual velocity
     *
     * @return actual velocity
     */
    public double getVelocity() {
        double out = 1.0;
        String v = this.getAttribute(this.velocity.get(this.velocity.size() - 1).getValue());
        if (v != null) {
            try {
                out = Double.parseDouble(v);
            } catch (NumberFormatException e) {
                out = 1.0;
            }
        }
        return out;
    }

    /**
     * set new value of velocity-attribute
     *
     * @param v    new value of velocity
     * @param time time of setting
     * @throws ModelException
     */
    public void setVelocityAttribute(String v, long time) throws ModelException {
        if (this.entityTyp.existPossibleAttribut(v)) {
            Attribute last = this.velocity.get(this.velocity.size() - 1);
            if (last != null && !last.getValue().equals(v.trim())) {
                this.velocity.add(new Attribute("velovity", v, time));
            }
        } else {
            throw new ModelException("Entity.setVelocityAttribute: " + v + " isn't a possibleAttribute");
        }
    }

    /**
     * get historical vector of velocity
     *
     * @return historical vector of velocity
     */
    public List<Attribute> getVelocityAttribute() {
        return this.velocity;
    }

    public String getEntityTypeId() {
        return this.entityTyp.getId();
    }

    /**
     * set new state-value
     *
     * @param state new state-value
     * @param time  time of setting
     * @throws ModelException
     */
    public void setState(String state, long time) throws ModelException {
        if (this.entityTyp.existPosibleState(state)) {
            Attribute last = null;
            try {
                last = this.state.get(this.state.size() - 1);
            } catch (NoSuchElementException e) {
            }
            if (last != null && !last.getValue().equals(state)) {
                this.state.add(new Attribute("state", state, time));
            }
            if (this.grafic != null) {
                this.grafic.setImage();
            }
        }
    }

    /**
     * get actual value of state
     *
     * @return actual value of state
     */
    public String getState() {
        return this.state.get(this.state.size() - 1).getValue();
    }

    /**
     * get vector of state history
     *
     * @return vector of state history
     */
    public List<Attribute> getStateHistory() {
        return this.state;
    }

    /**
     * An entity will be created, add or remove a container. This history is stored in containerHistory
     *
     * @param type container-typ (route, list, ..)
     * @param id   container-id or "free" or "static"
     * @param op   add or remove or created or destroyed
     * @param time time of operation
     * @throws ModelException, when type or id contains a Cmd.VALUE_SEPARATOR
     */
    public void changeContainer(String type, String id, String op, long time) throws ModelException {
        String[] p = new String[2];
        p[0] = type;
        p[1] = id;
        Attribute attr = null;
        try {
            attr = new Attribute(Parameter.cat(p), op, time);
            this.containerHistory.add(attr);
        } catch (desmoj.extensions.visualization2d.engine.command.CommandException e) {
            throw new ModelException(e.getMessage());
        }
    }

    /**
     * get vector of containerHistory
     *
     * @return vector of containerHistory
     */
    public List<Attribute> getContainerHistory() {
        return this.containerHistory;
    }

    /**
     * An entity is static or it is in a container (list, route, process) or it is free Only a free entity can put in to
     * a container
     *
     * @return true, when non-static entity is in no container
     */
    public boolean isFree() {
        String key = this.containerHistory.get(this.containerHistory.size() - 1).getKey();
        String[] p = Parameter.split(key);
        return p[1].equals("free");
    }

    /**
     * An entity has a fixed location
     *
     * @return true, when entity has a fixed location
     */
    public boolean isStatic() {
        String key = this.containerHistory.get(this.containerHistory.size() - 1).getKey();
        String[] p = Parameter.split(key);
        return p[1].equals("static");
    }

    /**
     * set an attribute-value
     *
     * @param key   attribute-key
     * @param value attribute-value
     * @param since time of setting
     */
    public void setAttribute(String key, String value, long since) {
        if (this.entityTyp.existPossibleAttribut(key)) {
            if (this.attribute.containsKey(key)) {
                Attribute last = this.attribute.get(key).get(this.attribute.get(key).size() - 1);
                if (last != null && !last.getValue().equals(value.trim())) {
                    this.attribute.get(key).add(new Attribute(key, value, since));
                }
            } else {
                List<Attribute> v = new LinkedList<Attribute>();
                v.add(new Attribute(key, value, since));
                this.attribute.put(key, v);
            }
        }
    }

    /**
     * get actual value of attribute
     *
     * @param key attribute-key
     * @return actual value of attribute
     */
    public String getAttribute(String key) {
        String out = null;
        List<Attribute> v = this.attribute.get(key);
        if (v != null) {
            out = v.get(v.size() - 1).getValue();
        }
        return out;
    }

    /**
     * get history vector of attribute
     *
     * @param key attribute-key
     * @return history vector of attribute
     */
    public List<Attribute> getAttributeHistory(String key) {
        return this.attribute.get(key);
    }

    /**
     * check if attribute-key is valid
     *
     * @param key
     * @return true, when attribute-key is valid
     */
    public boolean existAttribute(String key) {
        return this.attribute.containsKey(key);
    }

    /**
     * Dispose this entity
     */
    public void dispose() {
        //System.out.println("Entity.dispose  id: "+id);
        this.model.getEntities().remove(getId());
        this.grafic = null;
    }

    /**
     * creates an EntityGrafic instance for an free entity
     *
     * @param time time of creation
     * @return EntityGrafic
     * @throws ModelException
     */
    public Grafic createGraficFree(long time) throws ModelException {
        this.changeContainer("", "free", "", time);
        this.grafic = new EntityGrafic(this);
        return this.grafic;
    }

    /**
     * creates an EntityGrafic instance for an static entity with fixed position
     *
     * @param viewId    Id of view
     * @param positionY y-coordinate
     * @param angle     Drehwinkel
     * @param direction icon ist an y-achse gespiegelt
     * @param time      time of creation
     * @return EntityGrafic
     * @throws ModelException
     */
    public Grafic createGraficStatic(String viewId, double positionX, double positionY, double angle, boolean direction,
                                     long time) throws ModelException {
        this.changeContainer("", "static", "", time);
        EntityPosition pos = new EntityPosition(positionX, positionY, angle, direction);
        this.grafic = new EntityGrafic(this, viewId, pos);
        //this.grafic.addMouseListener(this.grafic);
        return this.grafic;
    }

    public Grafic getGrafic() {
        return this.grafic;
    }
}
