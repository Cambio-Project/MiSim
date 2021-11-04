package desmoj.extensions.visualization2d.animation.internalTools;

import java.util.List;

import desmoj.extensions.visualization2d.animation.core.simulator.EntityBasicAnimation;


/**
 * Class to manage return values of: ProcessStationNonAbstrResAnimation.remove(EntityBasicAnimation e)
 * ProcessStationAbstrResAnimation.remove(EntityBasicAnimation e) ProcessStationNoResAnimation.remove(EntityBasicAnimation
 * e)
 * <p>
 * This methods remove the entry which contains e. The return value contains the data of the removed entry.
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
public class EntryAnimation<Proc extends EntityBasicAnimation, Res extends EntityBasicAnimation> {

    private final List<Proc> proc;
    private final List<Res> res;
    private final Integer needNoRes;

    public EntryAnimation(List<Proc> proc, List<Res> res, Integer needNoRes) {
        this.proc = proc;
        this.res = res;
        this.needNoRes = needNoRes;
    }

    public boolean contains(EntityBasicAnimation entity) {
        boolean out = false;
        out |= this.proc.contains(entity);
        out |= this.res != null && this.res.contains(entity);
        return out;
    }

    public List<Proc> getProc() {
        return this.proc;
    }

    public List<Res> getRes() {
        return this.res;
    }

    public Integer getNeedNoRes() {
        return this.needNoRes;
    }

    public EntityBasicAnimation getOneEntity() {
        EntityBasicAnimation out = null;
        if (!this.proc.isEmpty()) {
            out = this.proc.get(0);
        }
        if (!this.res.isEmpty()) {
            out = this.res.get(0);
        }
        return out;
    }

    public boolean isEmpty() {
        return (this.getOneEntity() == null);
    }
}
