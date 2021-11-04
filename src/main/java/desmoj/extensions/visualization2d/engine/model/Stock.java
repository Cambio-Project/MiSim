package desmoj.extensions.visualization2d.engine.model;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Hashtable;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.StockGrafic;

/**
 * Stock with capacity
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
public class Stock implements Basic {

    private final String id;
    private String name;
    private final List consumer;
    private final Hashtable<String, Long> consumerProducts;
    private final List producer;
    private final Hashtable<String, Long> producerProducts;
    private long count;
    private final long upb;
    private final long lwb;
    private Grafic grafic;
    private final Model model;


    /**
     * @param model        used animation.model.Model
     * @param id
     * @param capacity
     * @param initialValue
     */
    public Stock(Model model, String id, long capacity, long initialValue) {
        this.model = model;
        this.id = id;
        this.lwb = 0;
        this.upb = capacity;
        this.name = null;
        this.consumer = new List(model, List.PREFIX_STOCK_CONSUMER, this.id);
        this.consumerProducts = new Hashtable<String, Long>();
        this.producer = new List(model, List.PREFIX_STOCK_PRODUCER, this.id);
        this.producerProducts = new Hashtable<String, Long>();
        this.count = initialValue;
        if (this.id != null) {
            model.getStocks().add(this);
        }
    }

    public Model getModel() {
        return this.model;
    }


    /**
     * get id of stock
     */
    public String getId() {
        return this.id;
    }

    /**
     * get name of resource
     */
    public String getName() {
        return this.name;
    }

    /**
     * set name of stock
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public long getLwb() {
        return this.lwb;
    }

    public long getUpb() {
        return this.upb;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long value, boolean aggregate) {
        if (aggregate) {
            this.count += value;
        } else {
            this.count = value;
        }
    }

    public String[][] getContentConsumer() {
        String[][] tmp = this.consumer.getContent();
        String[][] out = new String[tmp.length][3];
        for (int i = 0; i < tmp.length; i++) {
            out[i][0] = tmp[i][0];
            out[i][1] = tmp[i][1];
            out[i][2] = this.consumerProducts.get(tmp[i][0]).toString();
        }
        return out;
    }

    public String[][] getContentProducer() {
        String[][] tmp = this.producer.getContent();
        String[][] out = new String[tmp.length][3];
        for (int i = 0; i < tmp.length; i++) {
            out[i][0] = tmp[i][0];
            out[i][1] = tmp[i][1];
            out[i][2] = this.producerProducts.get(tmp[i][0]).toString();
        }
        return out;
    }

    public void reset() {
        this.setCount(0, false);
    }

    public void retrieveBegin(String entityId, int priority, long n, String priorityRule, long time) {
        this.consumerProducts.put(entityId, new Long(n));
        this.consumer.addToContainer(entityId, priority, priorityRule, time);
        ((StockGrafic) this.getGrafic()).update();
    }

    public void retrieveEnd(String entityId, long time) {
        this.consumer.removeFromContainer(entityId, time);
        long n = this.consumerProducts.remove(entityId).longValue();
        this.setCount(-n, true);
        ((StockGrafic) this.getGrafic()).update();
    }

    public void storeBegin(String entityId, int priority, long n, String priorityRule, long time) {
        this.producerProducts.put(entityId, new Long(n));
        this.producer.addToContainer(entityId, priority, priorityRule, time);
        ((StockGrafic) this.getGrafic()).update();
    }

    public void storeEnd(String entityId, long time) {
        this.producer.removeFromContainer(entityId, time);
        long n = this.producerProducts.remove(entityId).longValue();
        this.setCount(n, true);
        ((StockGrafic) this.getGrafic()).update();
    }

    /**
     * create a StockGrafic
     *
     * @param viewId              Id of view
     * @param x                   middlepoint x-coordinate
     * @param y                   middlepoint y-coordinate
     * @param defaultEntityTypeId for sizing
     * @return StockGrafic
     * @throws ModelException
     */
    public Grafic createGrafic(String viewId, int x, int y, String defaultEntityTypeId,
                               int anzVisible, boolean horizontal, Dimension deltaSize) throws ModelException {
        this.grafic =
            new StockGrafic(this, viewId, new Point(x, y), defaultEntityTypeId, anzVisible, horizontal, deltaSize);
        return this.grafic;
    }

    /**
     * get ProcessGrafic, created before
     */
    public Grafic getGrafic() {
        return grafic;
    }


}
