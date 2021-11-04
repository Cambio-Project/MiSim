package desmoj.extensions.visualization2d.engine.modelGrafic;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * defines some constants used in this package
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
public interface Grafic {

    int layer0 = JLayeredPane.DEFAULT_LAYER.intValue();
    Integer LAYER_BACKGROUND = new Integer(layer0 + 0);
    Integer LAYER_BackGroundLine = new Integer(layer0 + 1);
    Integer LAYER_BackGroundElement = new Integer(layer0 + 2);
    Integer LAYER_PROCESS_LINE_LIST = new Integer(layer0 + 3);
    Integer LAYER_ROUTE_STATIC = new Integer(layer0 + 4);
    Integer LAYER_STATION = new Integer(layer0 + 5);
    Integer LAYER_LIST = new Integer(layer0 + 6);
    Integer LAYER_PROCESS = new Integer(layer0 + 7);
    Integer LAYER_RESOURCE = new Integer(layer0 + 8);
    Integer LAYER_STOCK = new Integer(layer0 + 9);
    Integer LAYER_Bin = new Integer(layer0 + 10);
    Integer LAYER_WAITING_QUEUE = new Integer(layer0 + 11);
    Integer LAYER_ENTITY = new Integer(layer0 + 12);
    Integer LAYER_STATISTIC = new Integer(layer0 + 13);
    Integer LAYER_ROUTE_DYNAMIC = new Integer(layer0 + 19);
    Integer LAYER_MARKER = new Integer(layer0 + 20);
    Color COLOR_BACKGROUND = Color.white;
    Color COLOR_FOREGROUND = Color.black;
    Color COLOR_BORDER = Color.black;
    Color COLOR_ZOOM_MARKER = Color.gray;
    Color COLOR_INFOPANE_MARKED = Color.lightGray;
    Border Border_Default = BorderFactory.createEtchedBorder();
    Color[] COLOR_SWITCH_BACKGROUND = {new Color(255, 230, 230), new Color(230, 255, 255)};
    Color[] COLOR_SWITCH_STOCK_BOUND =
        {COLOR_FOREGROUND, new Color(200, 0, 0), new Color(0, 200, 0)};
    int BOUNDARY_WIDTH = 200; //Pixel
    Font FONT_DEFAULT = new Font("SansSerif", Font.PLAIN, 10);
    Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 8);
    Font FONT_BIG = new Font("SansSerif", Font.PLAIN, 20);
    Dimension STATION_DEFAULT_DIMENSION = new Dimension(20, 5);

}
