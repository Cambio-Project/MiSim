package desmoj.extensions.visualization2d.animation.internalTools;

import java.util.LinkedList;
import java.util.List;

import desmoj.extensions.visualization2d.animation.core.simulator.EntityBasicAnimation;


/**
 * used in: ProcessStationNonAbstrResAnimation ProcessStationAbstrResAnimation ProcessStationNoResAnimation
 *
 * @param <Proc>
 * @param <Res>
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
public class EntryAnimationVector
    <Proc extends EntityBasicAnimation, Res extends EntityBasicAnimation> {

    private final List<EntryAnimation<Proc, Res>> entries;
    private int totalNoRes;

    public EntryAnimationVector() {
        this.entries = new LinkedList<EntryAnimation<Proc, Res>>();
        this.totalNoRes = 0;
    }

    public void add(EntryAnimation<Proc, Res> e) {
        this.entries.add(e);
        if (e.getNeedNoRes() != null) {
            this.totalNoRes += e.getNeedNoRes();
        }
    }

    public EntryAnimation<Proc, Res> remove(EntityBasicAnimation entity) {
        EntryAnimation<Proc, Res> out = null;
        for (int i = 0; i < this.entries.size(); i++) {
            EntryAnimation<Proc, Res> entry = this.entries.get(i);
            if ((entry).contains(entity)) {
                out = entry;
                this.entries.remove(i);
            }
        }
        if (out != null && out.getNeedNoRes() != null) {
            this.totalNoRes -= out.getNeedNoRes();
        }
        return out;
    }

    public boolean contains(EntityBasicAnimation entity) {
        for (int i = 0; i < this.entries.size(); i++) {
            if (this.entries.get(i).contains(entity)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt die Anzahl der in dem Vector gebundenen abstrakten Resourcen wieder
     *
     * @return
     */
    public int getTotalNoRes() {
        return this.totalNoRes;
    }

    /**
     * Gives nr of entries.
     *
     * @return
     */
    public int length() {
        return this.entries.size();
    }

    /**
     * Gives names of Proc entities in entry i.
     *
     * @param i
     * @return
     */
    public List<String> getProcNames(int i) {
        List<String> out = new LinkedList<String>();
        if (i >= 0 && i < this.entries.size()) {
            List<Proc> procs = this.entries.get(i).getProc();
            for (Proc proc : procs) {
                out.add(proc.getName());
            }
        }
        return out;
    }

    /**
     * Gives names of Res entities in entry i. For each abstract entity an empty string is given.
     *
     * @param i
     * @return
     */
    public List<String> getResourceNames(int i) {
        List<String> out = new LinkedList<String>();
        if (i >= 0 && i < this.entries.size()) {
            List<Res> ress = this.entries.get(i).getRes();
            Integer nrRess = this.entries.get(i).getNeedNoRes();
            if (ress != null) {
                for (Res res : ress) {
                    out.add(res.getName());
                }
            } else if (nrRess != null) {
                for (int j = 0; j < nrRess; j++) {
                    out.add("");
                }
            }
        }
        return out;
    }
}
