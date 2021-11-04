package desmoj.extensions.visualization2d.engine;


/**
 * These constants are used in 2d animation engine
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
public interface Constants {
    /**
     * File extension of cmds-file
     */
    String FILE_EXTENSION_CMD = ".cmds";

    /**
     * File extension from log-file generated from viewer while animation
     */
    String FILE_EXTENSION_LOG = ".log";

    /**
     * File extension from log-file generated while cmd-file generation.
     */
    String FILE_EXTENSION_LOG_0 = ".log0";

    /**
     * File path to viewer.file directory
     */
    String FILE_PATH = "/desmoj/extensions/visualization2d/engine/viewer/files/";

    /**
     * Package path to viewer.file package
     */
    String PACKAGE_PATH = "desmoj.extensions.visualization2d.engine.viewer.files.";

    /**
     * Resource Bundle in package_path
     */
    String BUNDLE_NAME = "bundleViewerLanguage";

}
