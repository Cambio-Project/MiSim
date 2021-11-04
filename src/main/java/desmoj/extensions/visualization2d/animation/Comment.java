package desmoj.extensions.visualization2d.animation;

import java.awt.Color;

/**
 * A comment to paint in a animation element, used in process and queue elements.
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
public class Comment {

    public static final int TEXT_Style_Plain = 0;
    public static final int TEXT_Style_Bold = 1;
    public static final int TEXT_Style_Italic = 2;

    public static final int TEXT_Size_Normal = 10;
    public static final int TEXT_Size_Small = 8;
    public static final int TEXT_Size_Big = 20;

    private String text = null;
    private int style = TEXT_Style_Plain;
    private int size = TEXT_Size_Small;
    private Color color = Color.BLACK;
    private boolean sizeExt = false;

    /**
     * A comment to paint in a animation element
     *
     * @param text    Text of comment
     * @param style   Style of comment, look at Comment.TEXT_Style_
     * @param size    Size of comment, look at Comment.TEXT_Size_
     * @param color   Color of comment
     * @param sizeExt Size of animation element will be extended by the size need for comment
     */
    public Comment(String text, int style, int size, Color color, boolean sizeExt) {
        this.text = text;
        if (this.text == null) {
            this.text = "comment";
        }
        this.style = style;
        this.size = size;
        this.color = color;
        if (this.color == null) {
            this.color = Color.BLACK;
        }
        this.sizeExt = sizeExt;
    }

    public String getText() {
        return this.text;
    }

    public int getTextStyle() {
        return this.style;
    }

    public int getTextSize() {
        return this.size;
    }

    public Color getTextColor() {
        return this.color;
    }

    public boolean getSizeExt() {
        return this.sizeExt;
    }

    public String[] getProperties() {
        String[] out = new String[7];
        out[0] = this.text;
        out[1] = Integer.toString(this.style);
        out[2] = Integer.toString(this.size);
        out[3] = Integer.toString(this.color.getRed());
        out[4] = Integer.toString(this.color.getGreen());
        out[5] = Integer.toString(this.color.getBlue());
        out[6] = Boolean.toString(this.sizeExt);
        return out;
    }


}
