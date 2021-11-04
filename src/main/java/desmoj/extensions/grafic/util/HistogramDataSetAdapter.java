package desmoj.extensions.grafic.util;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import desmoj.core.simulator.TimeSpan;
import desmoj.core.statistic.Histogram;
import desmoj.core.statistic.HistogramAccumulate;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * Class to convert DesmoJ histogram or histogramAccumulate data into jFreeChart format. When in DesmoJ dataset
 * getShowTimeSpansInReport() is set, the data values are interpreted as a timespan in a appropriate time unit.
 *
 * @author Christian
 */
public class HistogramDataSetAdapter extends DefaultCategoryDataset {

    Locale locale;
    TimeUnit accumulateTimeUnit;
    boolean[] timeSpanForm; // show which TimeUnit.values() are part of format
    TimeUnit mayorTimeUnit = null;
    Histogram histogramTally = null;
    HistogramAccumulate histogramAccumulate = null;

    /**
     * convert a DesmoJ histogram to a org.jfree.data.category.DefaultCategoryDataset. When
     * histogram.getShowTimeSpansInReport() is set, the cell ranges are displayed as timespans.
     *
     * @param histogram
     */
    public HistogramDataSetAdapter(Histogram histogram, Locale locale) {
        super();
        this.locale = locale;
        this.accumulateTimeUnit = null;
        this.histogramTally = histogram;
        this.histogramAccumulate = null;
        boolean showTimeSpans = histogram.getShowTimeSpansInReport();
        if (showTimeSpans) {
            this.setTimeSpanFormat(histogram);
        }
        int minCell = 0;
        for (int i = 0; i < histogram.getCells() + 2; i++) {
            if (histogram.getObservationsInCell(i) > 0.0) {
                minCell = i;
                break;
            }
        }
        int maxCell = histogram.getCells() + 1;
        for (int i = histogram.getCells() + 1; i > -1; i--) {
            if (histogram.getObservationsInCell(i) > 0.0) {
                maxCell = i;
                break;
            }
        }
        //System.out.println("minCell: "+minCell+"  maxCell: "+maxCell);

        long value;
        double from, to;
        String yKey = "", xKey;
        for (int i = minCell; i <= maxCell; i++) {
            value = Math.round(histogram.getObservationsInCell(i));
            from = histogram.getLowerLimit(i);
            to = Double.POSITIVE_INFINITY;
            if (i + 1 < histogram.getCells() + 2) {
                to = histogram.getLowerLimit(i + 1);
            }
            xKey = "[" + this.format(showTimeSpans, from) + " .. " + this.format(showTimeSpans, to) + ")";
            this.addValue(value, yKey, xKey);
            //System.out.println("added: " + value+"  "+xKey+"   "+yKey);
        }
    }

    /**
     * convert a DesmoJ histogramAccumulate to a org.jfree.data.category.DefaultCategoryDataset. When
     * histogram.getShowTimeSpansInReport() is set, the cell ranges are displayed as timespans. The observation timespan
     * is converted into a double with timeUnit.
     *
     * @param histogram
     * @param timeUnit
     */
    public HistogramDataSetAdapter(HistogramAccumulate histogram, Locale locale) {
        super();
        this.locale = locale;
        this.histogramTally = null;
        this.histogramAccumulate = histogram;
        this.accumulateTimeUnit = this.chooseTimeUnit(histogram, 3);
        boolean showTimeSpans = histogram.getShowTimeSpansInReport();
        if (showTimeSpans) {
            this.setTimeSpanFormat(histogram);
        }
        int minCell = 0;
        for (int i = 0; i < histogram.getCells() + 2; i++) {
            if (histogram.getObservationsInCell(i).isZero()) {
                minCell = i;
                break;
            }
        }
        int maxCell = histogram.getCells() + 1;
        for (int i = histogram.getCells() + 1; i > -1; i--) {
            if (!histogram.getObservationsInCell(i).isZero()) {
                maxCell = i;
                break;
            }
        }
        //System.out.println("minCell: "+minCell+"  maxCell: "+maxCell);

        double value;
        double from, to;
        String yKey = "", xKey;
        for (int i = minCell; i <= maxCell; i++) {
            value = histogram.getObservationsInCell(i).getTimeAsDouble(this.accumulateTimeUnit);
            from = histogram.getLowerLimit(i);
            to = Double.POSITIVE_INFINITY;
            if (i + 1 < histogram.getCells() + 2) {
                to = histogram.getLowerLimit(i + 1);
            }
            xKey = "[" + this.format(showTimeSpans, from) + " .. " + this.format(showTimeSpans, to) + ")";
            this.addValue(value, yKey, xKey);
            //System.out.println("added: " + value+"  "+xKey+"   "+yKey);
        }
    }

    /**
     * an appropriate rangeTime Unit of given HistogramAccumulate. is determined in constructor in HistogramAccumulate
     * case.
     *
     * @return
     */
    public TimeUnit getAccumulateTimeUnit() {
        return this.accumulateTimeUnit;
    }

    /**
     * get category axis label
     *
     * @return
     */
    public String getCategoryAxisLabel() {
        NumberFormat formatter = NumberFormat.getInstance(locale);
        String out = "Unit: ";
        if (this.histogramTally != null) {
            boolean showTimeSpans = this.histogramTally.getShowTimeSpansInReport();
            if (showTimeSpans) {
                out += "TimeSpan [" + this.format(null) + "]";
            } else if (this.histogramTally.getUnit() != null) {
                out += "[" + this.histogramTally.getUnit() + "]";
            } else {
                out += "unknown";
            }
            if (showTimeSpans) {
                double mean = new TimeSpan(this.histogramTally.getMean()).getTimeAsDouble(mayorTimeUnit);
                double stdDev = new TimeSpan(this.histogramTally.getStdDev()).getTimeAsDouble(mayorTimeUnit);
                double min = new TimeSpan(this.histogramTally.getMinimum()).getTimeAsDouble(mayorTimeUnit);
                double max = new TimeSpan(this.histogramTally.getMaximum()).getTimeAsDouble(mayorTimeUnit);
                out += "   (  Mean = " + formatter.format(mean);
                out += "    Std.Dev = " + formatter.format(stdDev);
                out += "    Min = " + formatter.format(min);
                out += "    Max = " + formatter.format(max);
                out += " [" + this.mayorTimeUnit.name() + "]   )";
            } else {
                out += "   (  Mean = " + formatter.format(this.histogramTally.getMean());
                out += "    Std.Dev = " + formatter.format(this.histogramTally.getStdDev());
                out += "    Min = " + formatter.format(this.histogramTally.getMinimum());
                out += "    Max = " + formatter.format(this.histogramTally.getMaximum()) + "   )";
            }
        } else if (this.histogramAccumulate != null) {
            boolean showTimeSpans = this.histogramAccumulate.getShowTimeSpansInReport();
            if (showTimeSpans) {
                out += "TimeSpan [" + this.format(null) + "]";
            } else if (this.histogramAccumulate.getUnit() != null) {
                out += "[" + this.histogramAccumulate.getUnit() + "]";
            } else {
                out += "unknown";
            }
            if (showTimeSpans) {
                double mean = new TimeSpan(this.histogramAccumulate.getMean()).getTimeAsDouble(mayorTimeUnit);
                double stdDev = new TimeSpan(this.histogramAccumulate.getStdDev()).getTimeAsDouble(mayorTimeUnit);
                double min = new TimeSpan(this.histogramAccumulate.getMinimum()).getTimeAsDouble(mayorTimeUnit);
                double max = new TimeSpan(this.histogramAccumulate.getMaximum()).getTimeAsDouble(mayorTimeUnit);
                out += "   (  Mean = " + formatter.format(mean);
                out += "    Std.Dev = " + formatter.format(stdDev);
                out += "    Min = " + formatter.format(min);
                out += "    Max = " + formatter.format(max);
                out += " [" + this.mayorTimeUnit.name() + "]   )";
            } else {
                out += "   (  Mean = " + formatter.format(this.histogramAccumulate.getMean());
                out += "    Std.Dev = " + formatter.format(this.histogramAccumulate.getStdDev());
                out += "    Min = " + formatter.format(this.histogramAccumulate.getMinimum());
                out += "    Max = " + formatter.format(this.histogramAccumulate.getMaximum()) + "   )";
            }
        }
        return out;
    }

    /**
     * get observation axis label
     *
     * @return
     */
    public String getObservationAxisLabel() {
        String out = "";
        if (this.histogramTally != null) {
            NumberFormat formatter = NumberFormat.getInstance(locale);
            out = "Observations (Total = " +
                formatter.format(this.histogramTally.getObservations()) + ")";
        } else if (this.histogramAccumulate != null) {
            TimeUnit tUnit = this.getAccumulateTimeUnit();
            String tu = "[" + tUnit.name() + "]";
            double total = this.histogramAccumulate.getPeriodMeasured().getTimeAsDouble(tUnit);
            out = "Observation Time " + tu + " (Total = " + total + " " + tu + ") ";
        }
        return out;
    }

    /**
     * format values in category label. used in both constructors
     *
     * @param showTimeSpans
     * @param value
     * @return
     */
    private String format(boolean showTimeSpans, double value) {
        NumberFormat numberForm = NumberFormat.getInstance(locale);
        String out = numberForm.format(value);
        if (showTimeSpans && value < 0.0) {
            out += "";
        } else if (showTimeSpans && value >= Long.MAX_VALUE) {
            out += "";
        } else if (showTimeSpans) {
            out = this.format(new TimeSpan(value));
        }
        return out;
    }

    /**
     * choose shortest possible format of timespan values in category labels Method for Histogram (identical with method
     * for HistogramAccumulate) The determined format this.timeSpanForm is used in format(TimeSpan value).
     *
     * @param histogram
     */
    private void setTimeSpanFormat(Histogram histogram) {
        if (histogram.getShowTimeSpansInReport()) {
            TimeUnit maxUnit = null, minUnit = null;
            this.timeSpanForm = new boolean[TimeUnit.values().length];
            for (TimeUnit tu : TimeUnit.values()) {
                TimeSpan t;
                long tV = -1;
                double tN;
                boolean tNisZerro = true;
                for (int i = 0; i < histogram.getCells() + 2; i++) {
                    if (histogram.getLowerLimit(i) >= 0.0 &&
                        histogram.getLowerLimit(i) < Long.MAX_VALUE) {
                        t = new TimeSpan(histogram.getLowerLimit(i));
                        tV = t.getTimeTruncated(tu);                // Vorkomma Stellen
                        tN = Math.abs(t.getTimeAsDouble(tu) - tV);    // Nachkomma Stellen
                        //if(tN != 0.0) tNisZerro = false;
                        if (!(new TimeSpan(tN, tu).isZero())) {
                            tNisZerro = false;
                        }
                        //System.out.println(t);
                    }
                }
                if (tV == 0 && maxUnit == null) {
                    maxUnit = tu;
                }
                if (tNisZerro) {
                    minUnit = tu;
                }
            }
            //System.out.println("maxUnit: "+this.maxUnit.name()+"  minUnit: "+this.minUnit.name());
            boolean s = false;
            for (int j = 0; j < this.timeSpanForm.length; j++) {
                if (minUnit.equals(TimeUnit.values()[j])) {
                    s = true;
                }
                if (maxUnit != null && maxUnit.equals(TimeUnit.values()[j])) {
                    s = false;
                    this.mayorTimeUnit = TimeUnit.values()[j - 1];
                }
                this.timeSpanForm[j] = s;
            }
            if (maxUnit == null) {
                this.mayorTimeUnit = TimeUnit.DAYS;
            }

            // manage seconds, milliseconds, ...
            for (int i = 3; i >= 0; i--) {
                if (this.timeSpanForm[i]) {
                    for (int j = i - 1; j >= 0; j--) {
                        this.timeSpanForm[j] = false;
                    }
                    break;
                }
            }

			/*
			for(int j=0; j<this.timeSpanForm.length; j++){
				System.out.println(TimeUnit.values()[j]+"   "+this.timeSpanForm[j]);
			}
			*/

        }
    }

    /**
     * choose shortest possible format of timespan values in category labels Method for HistogramAccumulate (identical
     * with method for Histogram) The determined format this.timeSpanForm is used in format(TimeSpan value).
     *
     * @param histogram
     */
    private void setTimeSpanFormat(HistogramAccumulate histogram) {
        if (histogram.getShowTimeSpansInReport()) {
            TimeUnit maxUnit = null, minUnit = null;
            this.timeSpanForm = new boolean[TimeUnit.values().length];
            for (TimeUnit tu : TimeUnit.values()) {
                TimeSpan t;
                long tV = -1;
                double tN;
                boolean tNisZerro = true;
                for (int i = 0; i < histogram.getCells() + 2; i++) {
                    if (histogram.getLowerLimit(i) >= 0.0 &&
                        histogram.getLowerLimit(i) < Long.MAX_VALUE) {
                        t = new TimeSpan(histogram.getLowerLimit(i));
                        tV = t.getTimeTruncated(tu);                // Vorkomma Stellen
                        tN = Math.abs(t.getTimeAsDouble(tu) - tV);    // Nachkomma Stellen
                        //if(tN != 0.0) tNisZerro = false;
                        if (!(new TimeSpan(tN, tu).isZero())) {
                            tNisZerro = false;
                        }
                        //System.out.println(t);
                    }
                }
                if (tV == 0 && maxUnit == null) {
                    maxUnit = tu;
                }
                if (tNisZerro) {
                    minUnit = tu;
                }
            }
            //System.out.println("maxUnit: "+this.maxUnit.name()+"  minUnit: "+this.minUnit.name());
            boolean s = false;
            for (int j = 0; j < this.timeSpanForm.length; j++) {
                if (minUnit.equals(TimeUnit.values()[j])) {
                    s = true;
                }
                if (maxUnit != null && maxUnit.equals(TimeUnit.values()[j])) {
                    s = false;
                    this.mayorTimeUnit = TimeUnit.values()[j - 1];
                }
                this.timeSpanForm[j] = s;
            }
            if (maxUnit == null) {
                this.mayorTimeUnit = TimeUnit.DAYS;
            }

            // manage seconds, milliseconds, ...
            for (int i = 3; i >= 0; i--) {
                if (this.timeSpanForm[i]) {
                    for (int j = i - 1; j >= 0; j--) {
                        this.timeSpanForm[j] = false;
                    }
                    break;
                }
            }
			
			/*
			for(int j=0; j<this.timeSpanForm.length; j++){
				System.out.println(TimeUnit.values()[j]+"   "+this.timeSpanForm[j]);
			}
			*/
        }
    }

    /**
     * format timespan values in category label in showTimeSpans case. Used in format(boolean showTimeSpans, double
     * value)
     *
     * @param value
     * @return
     */
    private String format(TimeSpan value) {
        String out = "";
        if (value == null) {
            for (int j = this.timeSpanForm.length - 1; j >= 0; j--) {
                if (this.timeSpanForm[j]) {
                    out += TimeUnit.values()[j] + ":";
                }
            }
            out = out.substring(0, out.length() - 2);
        } else {
            for (int j = this.timeSpanForm.length - 1; j >= 0; j--) {
                if (this.timeSpanForm[j]) {
                    if (this.timeSpanForm[j - 1]) {
                        out += value.getTimeTruncated(TimeUnit.values()[j]) + ":";
                    } else {
                        out += value.getTimeAsDouble(TimeUnit.values()[j]);
                    }
                }
            }
        }
        return out;
    }

    /**
     * Choose a appropriate rangeTime Unit of histogram values. Used in constructor in HistogramAccumulate case. This is
     * the mayor unit, where the maximum histogram entry has more than threshold units. Example: threshold = 3 when max
     * entry = 4 minutes then time unit = minutes and when max entry = 2 minutes then time unit = seconds
     *
     * @param histogram
     * @param threshold
     * @return
     */
    private TimeUnit chooseTimeUnit(HistogramAccumulate histogram, int threshold) {
        TimeUnit out = TimeUnit.MINUTES;
        // determine max observation
        TimeSpan max = new TimeSpan(0);
        for (int i = 0; i < histogram.getCells(); i++) {
            if (TimeSpan.isLonger(histogram.getObservationsInCell(i), max)) {
                max = histogram.getObservationsInCell(i);
            }
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
