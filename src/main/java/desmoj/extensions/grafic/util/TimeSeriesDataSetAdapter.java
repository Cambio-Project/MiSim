package desmoj.extensions.grafic.util;

import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.TimeSeries;
import org.jfree.data.*;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * Class to convert DesmoJ timeseries data into jFreeChart format When in DesmoJ dataset getShowTimeSpansInReport() is
 * set, the data values are interpreted as a timespan in a appropriate time unit.
 *
 * @author christian.mueller@th-wildau.de and goebel@informatik.uni-hamburg.de modified at 4.12.2012 by
 *     christian.mueller@th-wildau.de
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class TimeSeriesDataSetAdapter extends AbstractXYDataset
    implements XYDataset, DomainInfo, RangeInfo {
    private static final long serialVersionUID = 1L;
    private final TimeSeries timeSeries;
    private TimeUnit rangeTimeUnit;
    private boolean isValid;
    private final Double[][] xValues;
    private final Double[][] yValues;
    private final int seriesCount;
    private int itemCount;
    private Number domainMin;
    private Number domainMax;
    private Number rangeMin;
    private Number rangeMax;
    private Range domainRange;
    private Range range;

    /**
     * convert a DesmoJ timeserie to an JFREE.CHART XYDataset. When allowMultipleValues is set, multiple range values of
     * a time value are allowed. In the opposite case only the last range value of a time value is accepted. In the case
     * timeSeries.getShowTimeSpansInReport() the data values are interpreted as a timespan in a appropriate time unit.
     *
     * @param timeSeries          DesmoJ timeSerie
     * @param allowMultipleValues
     */
    public TimeSeriesDataSetAdapter(TimeSeries timeSeries, boolean allowMultipleValues) {

        this.timeSeries = timeSeries;
        this.rangeTimeUnit = null;
        this.isValid = true;
        if (timeSeries.getShowTimeSpansInReport()) {
            this.rangeTimeUnit = this.chooseRangeTimeUnit(3);
            this.isValid = (this.rangeTimeUnit != null); // series has negative value
        }
        if (isValid) {
            itemCount = (int) timeSeries.getObservations();
        } else {
            itemCount = 0;
        }

        seriesCount = 1;
        xValues = new Double[seriesCount][itemCount];
        yValues = new Double[seriesCount][itemCount];
        if (itemCount > 0) {
            TimeInstant time = null;
            if (allowMultipleValues) {
                // allow multiple range values of one domain point
                for (int j = 0; j < itemCount; j++) {
                    time = new TimeInstant(timeSeries.getTimeValues().get(j));
                    xValues[0][j] = time.getTimeAsDouble(TimeUnit.MILLISECONDS);
                    yValues[0][j] = timeSeries.getDataValues().get(j);
                    if (timeSeries.getShowTimeSpansInReport() && isValid) {
                        yValues[0][j] = new TimeSpan(yValues[0][j]).getTimeAsDouble(rangeTimeUnit);
                    }

                }
            } else {
                // accept only the last range value of one domain point
                TimeInstant oldTime = new TimeInstant(timeSeries.getTimeValues().get(0));
                int count = 0;
                for (int j = 0; j < itemCount; j++) {
                    time = new TimeInstant(timeSeries.getTimeValues().get(j));
                    if (TimeInstant.isAfter(time, oldTime)) {
                        oldTime = time;
                        count++;
                    }
                    xValues[0][count] = time.getTimeAsDouble(TimeUnit.MILLISECONDS);
                    yValues[0][count] = timeSeries.getDataValues().get(j);
                    if (timeSeries.getShowTimeSpansInReport() && isValid) {
                        yValues[0][j] = new TimeSpan(yValues[0][j]).getTimeAsDouble(rangeTimeUnit);
                    }
                }
                this.itemCount = count + 1;
            }

            // determine min, max of Range
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (int j = 0; j < this.itemCount; j++) {
                if (yValues[0][j] < min) {
                    min = yValues[0][j];
                }
                if (yValues[0][j] > max) {
                    max = yValues[0][j];
                }
            }

            time = new TimeInstant(timeSeries.getTimeValues().get(0));
            domainMin = time.getTimeAsDouble(TimeUnit.MILLISECONDS);
            time = new TimeInstant(timeSeries.getTimeValues().get(itemCount - 1));
            domainMax = time.getTimeAsDouble(TimeUnit.MILLISECONDS);
            domainRange = new Range(domainMin.doubleValue(), domainMax.doubleValue());
            //System.out.println(new Date(domainMin.longValue())+".."+new Date(domainMax.longValue()));
            rangeMin = new Double(min);
            rangeMax = new Double(max);
            range = new Range(min, max);
        }
    }

    public Number getX(int i, int j) {
        return xValues[i][j];
    }

    public Number getY(int i, int j) {
        return yValues[i][j];
    }

    public int getSeriesCount() {
        return seriesCount;
    }

    public Comparable getSeriesKey(int i) {
        return "Sample " + i;
    }

    public int getItemCount(int i) {
        return itemCount;
    }

    public double getDomainLowerBound() {
        return domainMin.doubleValue();
    }

    public double getDomainLowerBound(boolean flag) {
        return domainMin.doubleValue();
    }

    public double getDomainUpperBound() {
        return domainMax.doubleValue();
    }

    public double getDomainUpperBound(boolean flag) {
        return domainMax.doubleValue();
    }

    public Range getDomainBounds() {
        return domainRange;
    }

    public Range getDomainBounds(boolean flag) {
        return domainRange;
    }

    public Range getDomainRange() {
        return domainRange;
    }

    public double getRangeLowerBound() {
        return rangeMin.doubleValue();
    }

    public double getRangeLowerBound(boolean flag) {
        return rangeMin.doubleValue();
    }

    public double getRangeUpperBound() {
        return rangeMax.doubleValue();
    }

    public double getRangeUpperBound(boolean flag) {
        return rangeMax.doubleValue();
    }

    public Range getRangeBounds(boolean flag) {
        return range;
    }

    public Range getValueRange() {
        return range;
    }

    public Number getMinimumDomainValue() {
        return domainMin;
    }

    public Number getMaximumDomainValue() {
        return domainMax;
    }

    public Number getMinimumRangeValue() {
        return domainMin;
    }

    public Number getMaximumRangeValue() {
        return domainMax;
    }

    /**
     * is invalid when timeSeries.getShowTimeSpansInReport() and timsSeries has negative values
     *
     * @return
     */
    public boolean isValid() {
        return this.isValid;
    }

    /**
     * in the case timeSeries.getShowTimeSpansInReport() a rangeTimeUnit is automatic determined
     *
     * @return
     */
    public TimeUnit getRangeTimeUnit() {
        return this.rangeTimeUnit;
    }

    /**
     * in the case timeSeries.getShowTimeSpansInReport() a rangeTimeUnit is automatic determined This is the mayor unit,
     * where the maximum time-series data entry has more than threshold units. Example: threshold = 3 when max entry = 4
     * minutes then time unit = minutes and when max entry = 2 minutes then time unit = seconds
     *
     * @param threshold
     * @return
     */
    private TimeUnit chooseRangeTimeUnit(int threshold) {
        TimeUnit out = null;
        boolean negativeTime = false;
        // determine max observation
        TimeSpan max = new TimeSpan(0);
        for (int i = 0; i < timeSeries.getObservations(); i++) {
            double value = timeSeries.getDataValues().get(i);
            if (value < 0.0) {
                negativeTime = true;
                break;
            }
            TimeSpan time = new TimeSpan(value);
            if (TimeSpan.isLonger(time, max)) {
                max = time;
            }
        }
        if (negativeTime) {
            return out;
        }

        TimeUnit[] timeUnits = {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES,
            TimeUnit.SECONDS, TimeUnit.MILLISECONDS,
            TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS};
        for (TimeUnit tu : timeUnits) {
            //System.out.println(tu+"  "+max.getTimeAsDouble(tu));
            if (max.getTimeAsDouble(tu) > threshold) {
                out = tu;
                break;
            }
        }
        return out;
    }

}
