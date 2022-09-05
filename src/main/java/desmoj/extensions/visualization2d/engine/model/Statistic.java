package desmoj.extensions.visualization2d.engine.model;


import java.awt.Dimension;
import java.awt.Point;
import java.util.Date;

import desmoj.extensions.visualization2d.engine.modelGrafic.Grafic;
import desmoj.extensions.visualization2d.engine.modelGrafic.StatisticGrafic;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.XIntervalDataItem;
import org.jfree.data.xy.XIntervalSeries;
import org.jfree.data.xy.XYSeries;


/**
 * Class to store a time series(typeData = DATA_TimeSeries) or a observation series (typeData = DATA_Observations). In a
 * time series, for every observed time, you have exactly one value. In a observation series, you can have more than one
 * value at one time. E.g. At one time, you have 2 events with different values. On the fly, the indexes: INDEX_None,
 * INDEX_Min_Max, INDEX_Mean_StdDev are supported. For visualization you must use the class StatisticGrafic. The data
 * are stored in data structures of the jFreeChart Library. For histogram support the number of histogram-cells
 * specified by constructor must greater than 0.
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
public class Statistic implements Basic {

    public static final int DATA_TimeSeries = 0;
    public static final int DATA_Observations = 1;
    public static final int INDEX_None = 0;
    public static final int INDEX_Min_Max = 1;
    public static final int INDEX_Mean_StdDev = 2;

    //private static final double	Factor_Interval_Inceasing		= 1.5;


    private final String id;
    private String name;
    private final boolean aggregate;
    private double valueLow;
    private double valueHigh;
    private long timeLow;
    private long timeHigh;
    private final int typeData;
    private final int typeIndex;
    private int observationCount;
    private double lastValue;
    private long lastTime;
    private long firstTime;
    private double min;
    private double max;
    private double summe;
    private double summeQuadrat;
    private TimeSeries timeSerie;
    private XYSeries observationSerie;
    private XIntervalSeries histogram;
    private double histogramLow;
    private double histogramHigh;
    private final double histogramCells;
    private double histogramIntervalLength;
    private ValuesAtTime vat;

    /**
     * includes all grafical properties
     */
    private StatisticGrafic grafic;
    private StatisticGrafic grafic_LastValue;
    private StatisticGrafic grafic_TimeValueDiagram;
    private StatisticGrafic grafic_Histogram;

    private final Model model;

    /**
     * Constructor
     *
     * @param model          used animation.model.Model
     * @param id             must be unique
     * @param typeData       one of the following values: Statistic.DATA_Observations Statistic.DATA_TimeSeries
     * @param typeIndex      one of the following values: Statistic.INDEX_None Statistic.INDEX_Min_Max
     *                       Statistic.INDEX_Mean_StdDev
     * @param aggregate      true:  the values will be aggregated false: the original values will be used
     * @param timeLow        lowest expected time, will be changed when a lower time is observed. Used to avoid axis
     *                       changes.
     * @param timeHigh       highest expected time, will be changed when a higher time is observed. Used to avoid axis
     *                       changes.
     * @param valueLow       lowest expected value, will be changed when a lower value is observed. Used to avoid axis
     *                       changes.
     * @param valueHigh      highest expected value, will be changed when a higher value is observed. Used to avoid axis
     *                       changes.
     * @param histogramCells No. of histogram cells between valueLow and valueHigh. Every cell has the same length. For
     *                       values outside of [valueLow, valueHigh] additional cells will be created. When <= 0, no
     *                       histogram data support.
     */
    public Statistic(Model model, String id, int typeData, int typeIndex, boolean aggregate,
                     long timeLow, long timeHigh, double valueLow, double valueHigh, int histogramCells) {

        //System.out.println("Statistic-Konstructor   id: "+id);
        this.model = model;
        this.id = id;
        this.name = null;
        this.aggregate = aggregate;
        this.valueLow = valueLow;
        this.valueHigh = valueHigh;
        this.timeLow = timeLow;
        this.timeHigh = timeHigh;
        this.histogramCells = histogramCells;
        this.histogramHigh = valueHigh;
        this.histogramLow = valueLow;
        this.typeData = typeData;
        this.typeIndex = typeIndex;
        this.observationSerie = null;
        this.timeSerie = null;
        this.histogram = null;
        this.init(true);
        this.grafic = null;
        this.grafic_LastValue = null;
        this.grafic_TimeValueDiagram = null;
        this.grafic_Histogram = null;

        // Statistic wird in Statistic-Liste aufgenommen
        if (this.id != null) {
            model.getStatistics().add(this);
        }
        //System.out.println("Anz. StatisticObj: "+Statistic.classContent.getAllIds().length);

    }

    /**
     * reset statistic object, used by warnup called from Model.resetStatistic(...)
     */
    public void reset() {
        //System.out.println("Statistic.reset   id:"+this.getId());
        this.init(false);
        //System.out.println("Statistik.update time: "+time+"  "+(this.grafic_Histogram!= null));
        if (this.grafic_LastValue != null) {
            this.grafic_LastValue.update();
        }
        if (this.grafic_Histogram != null) {
            this.grafic_Histogram.update();
        }
        if (this.grafic_TimeValueDiagram != null) {
            this.grafic_TimeValueDiagram.update();
        }
        //if(this.getGrafic()!= null)((StatisticGrafic)this.getGrafic()).update();
    }


    /**
     * init / reset Statistic Object called by Constructor and reset
     *
     * @param init true = init / false = reset
     */
    private void init(boolean init) {
        this.observationCount = 0;
        this.vat = null;
        switch (this.typeIndex) {
            case Statistic.INDEX_Min_Max:
                this.min = Double.POSITIVE_INFINITY;
                this.max = Double.NEGATIVE_INFINITY;
                break;
            case Statistic.INDEX_Mean_StdDev:
                this.summe = 0.0;
                this.summeQuadrat = 0.0;
                break;
        }
        switch (this.typeData) {
            case Statistic.DATA_Observations:
                if (init) {
                    this.observationSerie = new XYSeries(this.id);
                } else {
                    this.observationSerie.clear();
                }
                break;
            case Statistic.DATA_TimeSeries:
                if (init) {
                    this.timeSerie = new TimeSeries(this.id);
                } else {
                    this.timeSerie.clear();
                }
                break;
        }
        if (this.histogramCells > 0) {
            if (init) {
                if (Math.abs(this.histogramHigh - this.histogramLow) < 0.0001) {
                    this.histogramLow = 0.0;
                    this.histogramHigh = 1.0;
                } else if ((this.histogramHigh - this.histogramLow) < 0.0) {
                    double a = this.histogramLow;
                    this.histogramLow = this.histogramHigh;
                    this.histogramHigh = a;
                }
                this.histogramIntervalLength =
                    (this.histogramHigh - this.histogramLow) / this.histogramCells;
                this.histogram = new XIntervalSeries(this.id, true, false);
            } else {
                this.histogram.clear();
            }
        }
    }

    public Model getModel() {
        return this.model;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Possibility to specify a name of a instance. This name is shown in StatisticGrafic
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
        if (this.histogram != null) {
            this.histogram.setKey(name);
        }
        if (this.observationSerie != null) {
            this.observationSerie.setKey(name);
        }
        if (this.timeSerie != null) {
            this.timeSerie.setKey(name);
        }
    }

    /**
     * add a new point to the time/observation series. For time series the times must strictly increasing. For
     * observation series the times must (not strictly)increasing.
     *
     * @param value
     * @param time
     * @throws ModelException when times are not (strictly) increasing.
     */
    public void update(double value, long time) throws ModelException {

        //System.out.println("Statistic.update   id: "+this.id+" observationCount: "+this.observationCount+" value: "+value+" time: "+time+"  lastTime: "+lastTime);
        double histogramAddValue = 1.0;
        if (this.observationCount > 0) {
            if (lastTime > time) {
                throw new ModelException(
                    "Time observation are not increasing! " + lastTime + " .. " + time);
            }

            if (aggregate) {
                value += lastValue;
            }
        } else {
            firstTime = time;
        }

        switch (this.typeData) {
            case Statistic.DATA_Observations:
                this.observationSerie.add(time, value);
                histogramAddValue = 1.0;
                break;
            case Statistic.DATA_TimeSeries:
                if (observationCount > 0 && lastTime == time) {
                    vat.addValue(value);
                } else {
                    vat = new ValuesAtTime(time, value);
                }
                this.timeSerie.addOrUpdate(new Millisecond(new Date(vat.getTime())), vat.getMean());
                histogramAddValue = (this.observationCount > 0) ? (time - lastTime) : 0.0;
                break;
        }
        if (this.histogram != null) {
            // changed at 04.11.2011 by Chr. Mueller
            int i = (int) Math.floor((value - this.histogramLow) / this.histogramIntervalLength);
            double xLow = this.histogramLow + (i) * this.histogramIntervalLength;
            double xHigh = xLow + this.histogramIntervalLength;
            double key = (xLow + xHigh) / 2.0;
            if (this.histogram.indexOf(key) > -1) {
                // ist vorhanden
                double y = ((XIntervalDataItem) this.histogram.remove(key)).getYValue();
                this.histogram.add(key, xLow, xHigh, y + histogramAddValue);
                //System.out.println("value:  "+value+"   xLow: "+xLow+"   xHigh: "+xHigh+"  addValue: "+histogramAddValue+"   y: "+y);
            } else {
                // neu hinzufuegen
                this.histogram.add(key, xLow, xHigh, histogramAddValue);
                //System.out.println("value:  "+value+"   xLow: "+xLow+"   xHigh: "+xHigh+"  addValue: "+histogramAddValue);
            }
        }

        switch (this.typeIndex) {
            case Statistic.INDEX_Min_Max:
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
                break;
            case Statistic.INDEX_Mean_StdDev:
                switch (this.typeData) {
                    case Statistic.DATA_Observations:
                        summe += value;
                        summeQuadrat += value * value;
                        break;
                    case Statistic.DATA_TimeSeries:
                        if (this.observationCount > 0) {
                            this.summe += (time - lastTime) * lastValue;
                            this.summeQuadrat += (time - lastTime) * (lastValue * lastValue);
                        }
                        break;
                }
                break;
        }
        if (value < this.valueLow) {
            this.valueLow = value;
        }
        if (value > this.valueHigh) {
            this.valueHigh = value;
        }
        if (time < this.timeLow) {
            this.timeLow = time;
        }
        if (time > this.timeHigh) {
            this.timeHigh = time;
        }

        lastTime = time;
        lastValue = value;
        this.observationCount++;
        //System.out.println("Statistik.update time: "+time+"  "+(this.grafic_Histogram!= null));
        if (this.grafic_LastValue != null) {
            this.grafic_LastValue.update();
        }
        if (this.grafic_Histogram != null) {
            this.grafic_Histogram.update();
        }
        if (this.grafic_TimeValueDiagram != null) {
            this.grafic_TimeValueDiagram.update();
        }
        //if(this.getGrafic()!= null)((StatisticGrafic)this.getGrafic()).update();
    }


    /**
     * true when one or more points stored.
     *
     * @return
     */
    public boolean hasValue() {
        //return (this.observationCount > 0)|| this.accumulate;
        return (this.observationCount > 0);
    }

    /**
     * gets TypeData. Possible values: Statistic.DATA_Observations or Statistic.DATA_TimeSeries
     *
     * @return
     */
    public int getTypeData() {
        return this.typeData;
    }

    /**
     * gets TypeData. Possible values: Statistic.INDEX_None, Statistic.INDEX_Min_Max or Statistic.INDEX_Mean_StdDev
     *
     * @return
     */
    public int getTypeIndex() {
        return this.typeIndex;
    }

    /**
     * when no. of histogram cells greater zero
     *
     * @return
     */
    public boolean hasHistogramSupport() {
        return this.histogram != null;
    }

    /**
     * get low value of value axis
     *
     * @return
     */
    public double getValueLow() {
        return this.valueLow;
    }

    /**
     * get high value of value axis
     *
     * @return
     */
    public double getValueHigh() {
        return this.valueHigh;
    }

    /**
     * get low value of time axis
     *
     * @return
     */
    public long getTimeLow() {
        return this.timeLow;
    }

    /**
     * get high value of time axis
     *
     * @return
     */
    public long getTimeHigh() {
        return this.timeHigh;
    }


    /**
     * get last stored value
     *
     * @return
     * @throws ModelException, when no value stored
     */
    public double getLastValue() throws ModelException {
        if (!(this.hasValue())) {
            throw new ModelException("No Observations stored.");
        }
        return this.lastValue;
    }

    /**
     * get min value
     *
     * @return
     * @throws ModelException, when typeIndex != Statistic.INDEX_Min_Max
     */
    public double getMin() throws ModelException {
        if (this.typeIndex != Statistic.INDEX_Min_Max) {
            throw new ModelException("Min_Max Index not updated.");
        }
        return this.min;
    }

    /**
     * get max value
     *
     * @return
     * @throws ModelException, when typeIndex != Statistic.INDEX_Min_Max
     */
    public double getMax() throws ModelException {
        if (this.typeIndex != Statistic.INDEX_Min_Max) {
            throw new ModelException("Min_Max Index not updated.");
        }
        return this.max;
    }

    /**
     * get mean for typeData = Statistic.DATA_Observations, all values have the same weight. for typeData =
     * Statistic.DATA_TimeSeries, the time depended mean is computed.
     *
     * @return
     * @throws ModelException, when typeIndex != Statistic.INDEX_Mean_StdDev
     */
    public double getMean() throws ModelException {
        double out = Double.NaN;
        if (this.typeIndex != Statistic.INDEX_Mean_StdDev) {
            throw new ModelException("Mean_StdDev Index not updated.");
        }
        switch (this.typeData) {
            case Statistic.DATA_Observations:
                out = this.summe / this.observationCount;
                break;
            case Statistic.DATA_TimeSeries:
                out = this.summe / this.getObservationLength();
                break;
        }
        return out;
    }

    /**
     * get standard deviation for typeData = Statistic.DATA_Observations, all values have the same weight. for typeData
     * = Statistic.DATA_TimeSeries, the time depended standard deviation is computed.
     *
     * @return
     * @throws ModelException, when typeIndex != Statistic.INDEX_Mean_StdDev
     */
    public double getStdDev() throws ModelException {
        double out = Double.NaN;
        if (this.typeIndex != Statistic.INDEX_Mean_StdDev) {
            throw new ModelException("Mean_StdDev Index not updated.");
        }
        double mean = getMean();
        switch (this.typeData) {
            case Statistic.DATA_Observations:
                out = Math.sqrt((this.summeQuadrat / this.observationCount) - (mean * mean));
                break;
            case Statistic.DATA_TimeSeries:
                out = Math.sqrt((this.summeQuadrat / this.getObservationLength()) - (mean * mean));
                break;
        }
        return out;
    }

    /**
     * get first observation time
     *
     * @return
     * @throws ModelException, when no observation is stored.
     */
    public long getFirstTime() throws ModelException {
        if (!this.hasValue()) {
            throw new ModelException("No Observations stored.");
        }
        return this.firstTime;
    }

    /**
     * get last observation time
     *
     * @return
     * @throws ModelException, when no observation is stored.
     */
    public long getLastTime() throws ModelException {
        if (!this.hasValue()) {
            throw new ModelException("No Observations stored.");
        }
        return this.lastTime;
    }

    /**
     * get length of observation time
     *
     * @return
     * @throws ModelException, when no observation is stored.
     */
    public long getObservationLength() throws ModelException {
        return this.getLastTime() - this.getFirstTime();
    }

    /**
     * get time series in jFreeChart data structure.
     *
     * @return
     * @throws ModelException, when typeData != Statistic.DATA_TimeSeries
     */
    public TimeSeries getTimeSerie() throws ModelException {
        if (this.timeSerie == null) {
            throw new ModelException("TimeSerie not updated.");
        }
        return this.timeSerie;
    }

    /**
     * get observation series in jFreeChart data structure.
     *
     * @return
     * @throws ModelException, when typeData != Statistic.DATA_Observations
     */
    public XYSeries getObservationSerie() throws ModelException {
        if (this.observationSerie == null) {
            throw new ModelException("ObservationSerie not updated.");
        }
        return this.observationSerie;
    }

    /**
     * get histogram in jFreeChart data structure.
     *
     * @return
     * @throws ModelException, when histogramCells > 0 (in constructor)
     */
    public XIntervalSeries getHistogram() throws ModelException {
        if (this.histogram == null) {
            throw new ModelException("Histogram not updated.");
        }
        return this.histogram;
    }

    /**
     * get length of one histogram cell
     *
     * @return
     */
    public double getHistogramIntervalLength() {
        return this.histogramIntervalLength;
    }

    /**
     * get lowest border of lowest histogram cell
     *
     * @return
     */
    public double getHistogramLow() {
        int i = (int) Math.floor((this.valueLow - this.histogramLow) / this.histogramIntervalLength);
        return this.histogramLow + (i) * this.histogramIntervalLength;
    }

    /**
     * get highest border of highest histogram cell
     *
     * @return
     */
    public double getHistogramHigh() {
        int i = (int) Math.floor((this.valueHigh - this.histogramLow) / this.histogramIntervalLength);
        return this.histogramLow + (i + 1) * this.histogramIntervalLength;
    }

    /**
     * build an instance of StatisticGrafic
     *
     * @param viewId        Id of view
     * @param x             x Coordinate of Middle point
     * @param y             y Coordinate of Middle point
     * @param animationType Possible values: StatisticGrafic.ANIMATION_LastValue, StatisticGrafic.ANIMATION_TimeValueDiagram
     *                      StatisticGrafic.ANIMATION_Histogram
     * @param isIntValue    In typeAnimation == StatisticGrafic.ANIMATION_LastValue value is shown as integer.
     * @param deltaSize     Each animation type has a default size. This size can be incremented/decremented by
     *                      deltaSize. Null means no change.
     * @return
     * @throws ModelException
     */
    public Grafic createGrafic(String viewId, int x, int y, int animationType, boolean isIntValue, Dimension deltaSize,
                               boolean infopane) throws ModelException {
        Grafic out = null;

        switch (animationType) {
            case (StatisticGrafic.ANIMATION_LastValue):
                this.grafic_LastValue = new StatisticGrafic(this, viewId, new Point(x, y),
                    animationType, isIntValue, deltaSize, infopane);
                if (this.grafic == null) {
                    this.grafic = this.grafic_LastValue;
                }
                out = this.grafic_LastValue;
                break;
            case (StatisticGrafic.ANIMATION_Histogram):
                this.grafic_Histogram = new StatisticGrafic(this, viewId, new Point(x, y),
                    animationType, isIntValue, deltaSize, infopane);
                if (this.grafic == null) {
                    this.grafic = this.grafic_Histogram;
                }
                out = this.grafic_Histogram;
                break;
            case (StatisticGrafic.ANIMATION_TimeValueDiagram):
                this.grafic_TimeValueDiagram = new StatisticGrafic(this, viewId, new Point(x, y),
                    animationType, isIntValue, deltaSize, infopane);
                if (this.grafic == null) {
                    this.grafic = this.grafic_TimeValueDiagram;
                }
                out = this.grafic_TimeValueDiagram;
                break;
        }
        //System.out.println("Statistic  createGrafic: typ:"+animationType+"  "+(this.grafic_Histogram != null));
        //this.grafic.update();
        return out;
    }

    /**
     * get an existing StatisticGrafic instance.
     */
    public Grafic getGrafic() {
        return this.grafic;
    }

    class ValuesAtTime {

        private final long time;
        private double summe;
        private int count;

        public ValuesAtTime(long time, double value) {
            this.time = time;
            this.summe = value;
            this.count = 1;
        }

        public void addValue(double value) {
            this.summe += value;
            this.count += 1;
        }

        public int getCount() {
            return this.count;
        }

        public double getMean() {
            return this.summe / this.count;
        }

        public long getTime() {
            return this.time;
        }
    }
}
