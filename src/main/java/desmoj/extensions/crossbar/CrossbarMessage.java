package desmoj.extensions.crossbar;

import desmoj.core.report.Message;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;

/**
 * A message which a process can send to a {@link MessageCrossbar} or a {@link MessageChannel} to activate processes
 * waiting on the them. The CrossbarMessage contains the {@link TimeInstant} on which it was sent and its sender (the
 * process that send the message to the crossbar).<br /> <br /> Subclasses of the CrossbarMessage can be used to
 * transport information between senders and receivers of messages.
 *
 * @author Malte Unkrig
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 * @see MessageCrossbar
 * @see MessageChannel
 */
public class CrossbarMessage extends Message implements Cloneable {

    /**
     * The mode defining how the message will be distributed to receiver processes.
     */
    private final DistributionMode distributionMode;
    /**
     * The sender process of the CrossbarMessage. The sender is automatically set on send time by the message crossbar.
     */
    private SimProcess sender;

    /**
     * Constructs a CrossbarMessage with the given model and description. This message will be distributed to receivers
     * by reference. To change that specify a {@link DistributionMode} using the constructor {@link
     * CrossbarMessage#CrossbarMessage(Model, String, DistributionMode)}
     *
     * @param origin      The model the CrossbarMessage belongs to.
     * @param description The description for the message.
     */
    public CrossbarMessage(Model origin, String description) {
        this(origin, description, DistributionMode.DISTRIBUTE_BY_REFERENCE);
    }

    /**
     * Constructs a CrossbarMessage with the given model, description and {@link DistributionMode}.
     *
     * @param origin           The model the CrossbarMessage belongs to.
     * @param description      The description for the message.
     * @param distributionMode The {@link DistributionMode} for this message defining whether it will be distributed to
     *                         receivers by reference or as a copy.
     */
    public CrossbarMessage(Model origin, String description, DistributionMode distributionMode) {
        super(origin, description, origin.presentTime());

        if (distributionMode == null) {
            distributionMode = DistributionMode.DISTRIBUTE_BY_REFERENCE;
            origin.sendWarning(
                "Missing parameter: distributionMode.",
                "CrossbarMessage# CrossbarMessage(Model origin, String description, DistributionMode distributionMode)",
                "The parameter distributionMode cannot be null. The distribution mode DISTRIBUTE_BY_REFERENCE is used as a fallback.",
                "Do not pass null as a parameter.");
        }

        this.distributionMode = distributionMode;
    }

    @Override
    protected CrossbarMessage clone() throws CloneNotSupportedException {
        return (CrossbarMessage) super.clone();
    }

    /**
     * @return The DistributionMode of this message defining whether it will be passed to receiver processes by
     *     reference or as a copy.
     */
    public DistributionMode getDistributionMode() {
        return distributionMode;
    }

    /**
     * Returns the process which sent this message to the {@link MessageCrossbar}.
     *
     * @return The sender of the CrossbarMessage.
     */
    public SimProcess getSender() {
        return sender;
    }

    /**
     * Setter for the sender of this CrossbarMessage.
     *
     * @param sender
     */
    // setter only visible inside the framework.
    void setSender(SimProcess sender) {
        this.sender = sender;
    }

    /**
     * The DistributionMode defines how a CrossbarMessage is distributed to receiver processes. It can either be
     * distributed by reference, meaning each receiver gets the exact same message object, or as a copy so that each
     * receiver only gets a (shallow) copy of the original message object.
     *
     * @author Malte Unkrig
     */
    public enum DistributionMode {
        DISTRIBUTE_BY_REFERENCE, DISTRIBUTE_AS_COPY
    }

}
