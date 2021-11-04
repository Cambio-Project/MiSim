package desmoj.extensions.experimentation.ui;

import java.awt.Graphics;

/**
 * An interface which asks for a method drawChart(). This is needed for the class Chart. Within the paint() method of
 * Chart the drawChart() method of the given ChartOwner is called. The drawChart() method should implement the algorithm
 * which draws the data (e.g. a histogram or a mathematical function).
 * <p>
 * In Desmo-J the HistogramPlotter and the TimerSeriesPlotter are inherited from this interface.
 *
 * @author Philip Joschko
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public interface ChartOwner {

    /**
     * This method implements the drawing of the data for a Chart class.
     *
     * @param g The specified Graphics object.
     */
    void drawChart(Graphics g);

}
