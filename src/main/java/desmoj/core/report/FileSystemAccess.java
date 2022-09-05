package desmoj.core.report;

import java.io.IOException;
import java.io.Writer;

/**
 * Strategy to create a writer for a given file path. This can be used when the experiment results shall not be written
 * to a normal file, but perhaps just to an in memory buffer or a socket.
 *
 * @author Tobias Baum
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public interface FileSystemAccess {

    /**
     * Creates a writer for the given "filename".
     */
    Writer createWriter(String filename) throws IOException;

}
