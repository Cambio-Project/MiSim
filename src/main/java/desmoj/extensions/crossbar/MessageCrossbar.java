package desmoj.extensions.crossbar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.exception.DelayedInterruptException;
import desmoj.core.exception.InterruptException;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.chaining.report.FlexReporterBuilder;
import desmoj.extensions.chaining.report.FlexReporterBuilder.Row;
import desmoj.extensions.crossbar.CrossbarMessage.DistributionMode;

/**
 * The MessageCrossbar is a higher modeling construct used to synchronize processes.<br /> <br /> The MessageCrossbare
 * permits the creation of any number of MessageChannels on it. Over these channels messages can be sent from one sender
 * process to any number of receiver processes. To receive a message a process has to wait for it on a message channel.
 * Once a process starts waiting on a channel it is automatically passivated and blocked. If a process sends a message
 * to a channel all process waiting there will be reactivated and unregistered from the message channel and the
 * MessageCrossbar.<br /> <br /> It is possible for a process to wait on several message channels simultaneously. It
 * will be activated and removed from all channels if a message is received on any of those channels. In parallel it is
 * also possible for a process to send messages to several channels simultaneously.
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
 * @see MessageChannel
 * @see CrossbarMessage
 */
public class MessageCrossbar<T extends SimProcess> extends Reportable {

    /**
     * The map in which the MessageCrossbar stores all its individual {@link MessageChannel}s
     */
    private final Map<String, MessageChannel<T>> messageChannelsByName;

    /**
     * A map matching sent messages to their receivers.
     */
    private final Map<T, CrossbarMessage> messagesByProcesses;

    /**
     * A queue containing all process currently waiting on the MessageCrossbar.
     */
    private final ProcessQueue<T> passivatedProcessesQueue;

    /**
     * A counter keeping track of the processes which were interrupted and removed from this channel because of their
     * max waiting time on the MessageCrossbar was reached.
     */
    private long interruptedWaits;

    /**
     * A counter keeping track of the messages which were successfully delivered to a waiting process. A message is
     * considered delivered if it was forwarded to at least one waiting process.
     */
    private long deliveredMessages;

    /**
     * A counter keeping track of the messages which could not be delivered to a waiting process. A message is
     * considered lost if it could not be forwarded to any receiver because at the time it was received there was no
     * process waiting for messages.
     */
    private long lostMessages;

    /**
     * Creates a MessageCrossbar object with all parameters required. The MessageCrossbar registers itself at the given
     * model
     *
     * @param name         java.lang.String : The name of this reportable
     * @param owner        Model : The model this reportable is associated to
     * @param showInReport boolean : Flag for showing the report Set it to
     *                     <code>true</code> if reportable should show up in report. Set
     *                     it to <code>false</code> if reportable should not be shown in report.
     * @param showInTrace  boolean : Flag for showing this reportable in trace files. Set it to <code>true</code> if
     *                     reportable should show up in trace. Set it to <code>false</code> if reportable should not be
     *                     shown in trace.
     */
    public MessageCrossbar(Model owner, String name, boolean showInReport,
                           boolean showInTrace) {
        super(owner, name, showInReport, showInTrace);

        messageChannelsByName = new LinkedHashMap<String, MessageChannel<T>>();
        messagesByProcesses = new HashMap<T, CrossbarMessage>();
        passivatedProcessesQueue = new ProcessQueue<T>(owner, name + "Queue",
            false, false);
    }

    /**
     * Checks whether the SimProcess (in it's current state) can wait at the message crossbar.
     *
     * @param process The SimProcess to check
     * @param where   The Method from which this method is called
     * @return Whether the SimProcess is valid or not.
     */
    private boolean canProcessWaitAtMessageCrossbar(SimProcess process,
                                                    String where) {

        if (process.isBlocked()) {
            sendWarning(
                "Can't wait at MessageCrossbar! Command ignored.",
                "SimProcess : " + getName() + " Method: " + where,
                "SimProcess is blocked. Blocked SimProcesses cannot wait at a MessageCrossbar.",
                "You can check if a SimProcess is blocked using the method "
                    + "isBlocked().");
            return false;
        }

        if (process.isTerminated()) {
            sendWarning(
                "Can't wait at MessageCrossbar! Command ignored.",
                "SimProcess : " + getName() + " Method: " + where,
                "SimProcess is terminated. Terminated SimProcesses cannot be interrupted.",
                "You can check if a SimProcess is terminated using the method "
                    + "isTerminated().");
            return false;

        }

        if (process.isScheduled()) {
            sendWarning(
                "The activation which is scheduled for a process is canceled because it begins waiting at a message crossbar.",
                "SimProcess : " + getName() + " Method: " + where,
                "The SimProcess which begins to wait at the message crossbar is already scheduled. This may indicate a flaw in your model.",
                "Make sure the SimProcess is not sheduled before trying to wait at the MessageCrossbar.");
            // The scheduled activation of the Process will be canceled in the
            // wait method of the crossbar

        }

        return true;
    }

    /**
     * Creates a new MessageChannel with is automatically associated with the MessageCrossbar. If a channel with the
     * specified name already exists it will be returned instead of a new channel.
     *
     * @param channelName String : The name of the new channel
     * @return The new MessageChannel instance
     */
    public MessageChannel<T> createMessageChannel(String channelName) {
        MessageChannel<T> messageChannel;

        if (channelName == null || channelName.isEmpty()) {
            sendWarning(
                "Invalid MessageChannel name",
                "MessageCrossbar : "
                    + getName()
                    +
                    " Method: MessageChannel<T>  createMessageChannel(String channelName, boolean showInReport, boolean showInTrace)",
                "The name of the message channel may not be null or \"\". It was: \""
                    + channelName + "\".",
                "Please use a valid channel name");
            return null;
        }

        if (!messageChannelsByName.containsKey(channelName)) {
            messageChannel = new MessageChannel<T>(getModel(), channelName,
                traceIsOn(), this);
            messageChannelsByName.put(channelName, messageChannel);
        }

        return messageChannelsByName.get(channelName);
    }

    /**
     * Creates a new MessageChannel with is automatically associated with the MessageCrossbar. If a channel with the
     * specified name already exists it will be returned instead of a new channel.<br /> <br /> By specifying the
     * generic type of the {@link MessageChannel} this method allows the creation of MessageChannels which are more
     * specifically typed than the MessageCrossbar. In this way on a generally typed MessageCrossbar< SimProcess> a
     * MessageChannel< Truck> (where Truck extends SimProcess) can be created.
     *
     * @param channelName        String : The name of the new channel
     * @param desiredGenericType Class<ST> : The generic typ of the channel.
     * @param showInReport       boolean : Flag for showing the report Set it to
     *                           <code>true</code> if reportable should show up in report. Set
     *                           it to <code>false</code> if reportable should not be shown in report.
     * @param showInTrace        boolean : Flag for showing this reportable in trace files. Set it to <code>true</code>
     *                           if reportable should show up in trace. Set it to <code>false</code> if reportable
     *                           should not be shown in trace.
     * @return The new MessageChannel instance
     */
    @SuppressWarnings("unchecked")
    public <ST extends T> MessageChannel<ST> createMessageChannel(
        String channelName, Class<ST> desiredGenericType,
        boolean showInReport, boolean showInTrace) {
        return (MessageChannel<ST>) createMessageChannel(channelName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter createDefaultReporter() {
        FlexReporterBuilder builder;
        Row row;

        builder = new FlexReporterBuilder("MessageCrossbars", 6100);
        row = builder.openRow();
        row.addHeadingCell("Title");
        row.addHeadingCell("");
        row.addHeadingCell("(Re)set");
        row.addHeadingCell("MsgsRec");
        row.addHeadingCell("MsgsDeli");
        row.addHeadingCell("MsgsLost");
        row.addHeadingCell("Activated directly");
        row.addHeadingCell("Activated indirectly");
        row.addHeadingCell("Aborted");
        row.addHeadingCell("Obs");
        row.addHeadingCell("Qmax");
        row.addHeadingCell("Qnow");
        row.addHeadingCell("Qavg");
        row.addHeadingCell("max.Wait");
        row.addHeadingCell("avg.Wait");
        row.closeRow();

        row = builder.openRow();
        row.addCell(getName());
        row.addCell("Global:");
        row.addCell(resetAt());
        row.addCell(getReceivedMessages());
        row.addCell(getDeliveredMessages());
        row.addCell(getLostMessages());
        row.addCell(getProcessActivations());
        row.addCell("n/a");
        row.addCell(getInterruptedWaits());
        row.addCell(getObservations());
        row.addCell(passivatedProcessesQueue.maxLength());
        row.addCell(passivatedProcessesQueue.length());
        row.addCell(passivatedProcessesQueue.averageLength());
        row.addCell(passivatedProcessesQueue.maxWaitTime());
        row.addCell(passivatedProcessesQueue.averageWaitTime());
        row.closeRow();

        row = builder.openRow();
        row.addCell("").addHeadingCell("Message channels").addCell("")
            .addCell("").addCell("").addCell("").addCell("").addCell("")
            .addCell("").addCell("").addCell("").addCell("").addCell("")
            .addCell("").addCell("").closeRow();

        for (MessageChannel<T> ch : messageChannelsByName.values()) {
            row = builder.openRow();
            row.addCell("");
            row.addCell(ch.getName());
            row.addCell(ch.resetAt());
            row.addCell(ch.getReceivedMessages());
            row.addCell(ch.getDeliveredMessages());
            row.addCell(ch.getLostMessages());
            row.addCell(ch.getDirectProcessActivations());
            row.addCell(ch.getIndirectProcessActivations());
            row.addCell(ch.getInterruptedWaits());
            row.addCell(ch.getObservations());
            row.addCell(ch.getPassivatedProcessesQueue().maxLength());
            row.addCell(ch.getPassivatedProcessesQueue().length());
            row.addCell(ch.getPassivatedProcessesQueue().averageLength());
            row.addCell(ch.getPassivatedProcessesQueue().maxWaitTime());
            row.addCell(ch.getPassivatedProcessesQueue().averageWaitTime())
                .closeRow();
        }

        return builder.build();
    }

    /**
     * @return The number of delivered messages. A message is considered delivered if it was forwarded to at least one
     *     waiting process.
     */
    public long getDeliveredMessages() {
        return deliveredMessages;
    }

    /**
     * @return The number of process which were interrupted while waiting on the MessageCrossbar because their max wait
     *     time was exceeded.
     */
    public long getInterruptedWaits() {
        return interruptedWaits;
    }

    /**
     * @return The number of message which were sent to this MessageCrossbar but couldn't be forwarded to a receiver
     *     because no process was waiting on the MessageCrossbar at the time the message was received.
     */
    public long getLostMessages() {
        return lostMessages;
    }

    /**
     * Returns the {@link MessageChannel} identified by the given name.
     *
     * @param channelName The name of the channel
     * @return The {@link MessageChannel} identified by the given name, or
     *     <code>null</code> if no channel with that name is managed by the
     *     MessageCrossbar.
     */
    public MessageChannel<T> getMessageChannel(String channelName) {
        return messageChannelsByName.get(channelName);
    }

    /**
     * Method that returns the message which is to be distributed to the receiver. This can either be the original
     * message itself if the {@link DistributionMode} of the message is {@link DistributionMode#DISTRIBUTE_BY_REFERENCE}
     * or a copy if the DistributionMode is {@link DistributionMode#DISTRIBUTE_AS_COPY}
     *
     * @param message The original message
     * @return The message object to distribute
     */
    private CrossbarMessage getMessageToDistributeToReceiverProcess(
        CrossbarMessage message) {
        CrossbarMessage messageToDistribute;

        switch (message.getDistributionMode()) {
            case DISTRIBUTE_AS_COPY:
                try {
                    messageToDistribute = message.clone();
                } catch (CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case DISTRIBUTE_BY_REFERENCE:
                messageToDistribute = message;
                break;
            default:
                throw new RuntimeException("Unhandled DistributionMode: "
                    + message.getDistributionMode());
        }

        return messageToDistribute;
    }

    /**
     * @return The number of processes which have been handled by this message MessageCrossbar (meaning processes which
     *     entered the channel and also left it again).
     */
    @Override
    public long getObservations() {
        return getPassivatedProcessesQueue().getObservations();
    }

    /**
     * @return The Queue in which all passivated processes are stored.
     */
    ProcessQueue<T> getPassivatedProcessesQueue() {
        return passivatedProcessesQueue;
    }

    /**
     * @return The number of process that were successfully activated by a delivered message.
     */
    public long getProcessActivations() {
        return getObservations() - interruptedWaits;
    }

    /**
     * @return The number of messages which were sent to this channel.
     */
    public long getReceivedMessages() {
        return deliveredMessages + lostMessages;
    }

    /**
     * Checks whether the given list of message channels is valid.
     *
     * @param channels The list of channels to check
     * @param where    The caller method
     * @return True if the list of channels is valid, false otherwise
     */
    private boolean isChannelListValid(List<MessageChannel<T>> channels,
                                       String where) {
        String channelsNullOrEmpty = null;

        if (channels == null) {
            channelsNullOrEmpty = "null";
        } else if (channels.isEmpty()) {
            channelsNullOrEmpty = "empty";
        }

        if (channelsNullOrEmpty != null) {
            sendWarning("Invalid parameter. The attempted action is ignored!",
                "MessageCrossbar: " + getName() + " Method: " + where,
                "The list of channels given as a parameter is "
                    + channelsNullOrEmpty + ".",
                "Make sure you pass a valid list of channels to the method.");
            return false;
        }

        for (MessageChannel<T> channel : channels) {
            if (!messageChannelsByName.containsValue(channel)) {
                sendWarning(
                    "Invalid parameter. The attempted action is ignored!",
                    "MessageCrossbar: " + getName() + " Method: " + where,
                    "The channel named \""
                        + channel.getName()
                        + "\" does not belong to the MessageCrossbar \""
                        + getName() + "\"",
                    "Make sure to only pass channels to this method which belong to the MessageCrossbar.");
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the given CrossbarMessage is valid.
     *
     * @param message The message to check
     * @param where   The caller method
     * @return True if the message is valid, false otherwise
     */
    private boolean isMessageValid(CrossbarMessage message, String where) {
        if (message == null) {
            sendWarning("Invalid parameter. The attempted action is ignored!",
                "MessageCrossbar: " + getName() + " Method: " + where,
                "The message is only a null pointer.",
                "Make sure that only valid CrossbarMessages are passed to this method.");
            return false;
        }

        return true;
    }

    /**
     * Checks whether the SimProcess using the MessageCrossbar is a valid process.
     *
     * @param process The SimProcess to check
     * @param where   The Method from which this method is called
     * @return Whether the SimProcess can currently wait at the crossbar.
     */
    private boolean isProcessValid(SimProcess process, String where) {
        if (process == null) {
            sendWarning(
                "A non existing process is trying to use the MessageCrossbar. "
                    + "The attempted action is ignored!",
                "MessageCrossbar: " + getName() + " Method: " + where,
                "The process is only a null pointer.",
                "Make sure that only real SimProcesses are using Stocks.");
            return false;
        }

        if (!isModelCompatible(process)) {
            sendWarning(
                "The process trying to use the MessageCrossbar does not "
                    + "belong to this model. The attempted action is ignored!",
                "Stock: " + getName() + " Method: " + where,
                "The process is not modelcompatible.",
                "Make sure that processes are using only MessageCrossbars within their model.");
            return false;
        }

        return true;
    }

    /**
     * Checks whether the given TimeInstant is valid
     *
     * @param waitUntil The TimeInstant to check
     * @param where     The caller method
     * @return True if the TimeInstant is valid, false otherwise
     */
    private boolean isWaitUntilValid(TimeInstant waitUntil, String where) {
        if (waitUntil != null && TimeInstant.isBefore(waitUntil, presentTime())) {
            sendWarning(
                "Cannot wait at the MessageCrossbar! Command Ignored.",
                "MessageCrossbar: " + getName() + " Method " + where,
                "The parameter waitUntil is before the current simulation time.",
                "Please pass a valid TimeInstant to this method.");
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        getPassivatedProcessesQueue().reset();
        for (MessageChannel<?> messageChannel : messageChannelsByName.values()) {
            messageChannel.reset();
        }
        deliveredMessages = 0;
        lostMessages = 0;
        interruptedWaits = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeInstant resetAt() {
        return passivatedProcessesQueue.resetAt();
    }

    /**
     * Sends the given {@link CrossbarMessage} to the specified message channels, thereby activating all processes that
     * are waiting on these channel.
     *
     * @param message          The message to send to the specified channels
     * @param channelsToSendTo The channels to which the message will be sent
     */
    public void sendMessage(CrossbarMessage message,
                            List<MessageChannel<T>> channelsToSendTo) {
        String where = "void sendMessage(CrossbarMessage message, List<MessageChannel<T>> channelsToSendTo)";
        Set<T> processesToActivate;
        SimProcess currentSimProcess;

        currentSimProcess = currentSimProcess();

        // Check if the process is valid
        if (!isProcessValid(currentSimProcess, where)) {
            return;
        }
        // Check if the channelsToSendTo are valid
        if (!isChannelListValid(channelsToSendTo, where)) {
            return;
        }
        // Check if the message is valid
        if (!isMessageValid(message, where)) {
            return;
        }

        sendTraceNote("SimProcess " + currentSimProcess.getName()
            + " sends a message to the MessageCrossbar " + getName());

        // Set the sender of the message
        message.setSender(currentSimProcess);

        // Determine all process that are to be activated...
        processesToActivate = new HashSet<T>();
        for (MessageChannel<T> channel : channelsToSendTo) {
            // ..and in doing so also remove those processes from the channels
            // they are waiting on
            processesToActivate.addAll(channel
                .receiveMessageAndRemoveWaitingProcesses());
        }

        // check whether processes are to be activated
        if (!processesToActivate.isEmpty()) {
            // This message activates at least on process so it counts as
            // delivered
            deliveredMessages++;

            // Allthough we have removed all processes from the channels the
            // message was sent to, the processes might wait on other channels
            // ion this
            // crossbar to which the message wasnt sent to. Wen need to remove
            // the
            // processes from these channels too.
            for (MessageChannel<T> channel : messageChannelsByName.values()) {
                channel.removePassivatedProcessesThatWereActivatedByAMessageOnAnotherChannel(processesToActivate);
            }

            // for each process which is to be activated
            for (T process : processesToActivate) {
                // accociate the proccess with the message which activated it
                messagesByProcesses.put(process,
                    getMessageToDistributeToReceiverProcess(message));
                getPassivatedProcessesQueue().remove(process);
                process.setBlocked(false);// unblock it
                process.activateAfter(current());// activate it
            }
        } else {
            // the list of processes to be activated is empty. Since the message
            // doesn't lead to any process activations it is considered as
            // "lost". Therefore the lostMessages counter needs to be
            // incremented
            lostMessages++;
        }
    }

    /**
     * Wait for a {@link CrossbarMessage} on the specified message channels, thereby passivating the current process.
     * The process is not reactivated until a message is received.
     *
     * @param channelsToWaitOn The channels on which to wait for a message
     * @return The CorssbarMessage received after waiting
     */
    public CrossbarMessage waitForMessage(
        List<MessageChannel<T>> channelsToWaitOn) throws SuspendExecution {
        return waitForMessage(channelsToWaitOn, (TimeInstant) null);
    }

    /**
     * Wait for a {@link CrossbarMessage} on the specified message channels, thereby passivating the current process.
     * The process is not reactivated until a message is received. The parameter waitUntil specifies the point in time
     * to which the current process will wait for a message. If no message is received before that time, the process
     * will be interrupted and a {@link DelayedInterruptException} will be thrown.
     *
     * @param channelsToWaitOn The channels on which to wait for a message
     * @param waitUntil        The point in time at which the waiting will be interrupted
     * @return The CorssbarMessage received after waiting
     */
    @SuppressWarnings("unchecked")
    public CrossbarMessage waitForMessage(
        List<MessageChannel<T>> channelsToWaitOn, TimeInstant waitUntil)
        throws InterruptException, SuspendExecution {
        final T current;
        String where =
            "CrossbarMessage waitForMessage(List<MessageChannel<T>> channelsToWaitOn, TimeInstant waitUntil)";
        ExternalEvent delayedInterruptEvent;
        ExternalEvent processUnblockingEvent = null;

        current = (T) currentSimProcess();// get the current SimProcess

        // perform all necessary checks
        if (!isProcessValid(current, where)) {
            return null;
        }
        if (!isChannelListValid(channelsToWaitOn, where)) {
            return null;
        }
        if (!isWaitUntilValid(waitUntil, where)) {
            return null;
        }
        if (!canProcessWaitAtMessageCrossbar(current, where)) {
            return null;
        }

        // If a wait time limit is set (waitUntil!=null) schedule an delayed
        // interrupt of this process, so it can leave the
        // crossbar after the specified wait time
        // Since blocked processes cannot be interrupted schedule an
        // ExternalEvent that unblocks the process just before it is
        // interrupted.
        if (waitUntil != null) {
            delayedInterruptEvent = current.interruptDelayed(waitUntil);
            processUnblockingEvent = new ExternalEvent(getModel(),
                "MessageCrossbarProcessUnblockingEvent", true) {

                @Override
                public void eventRoutine() {
                    current.setBlocked(false);
                }
            };
            skipTraceNote();
            processUnblockingEvent.scheduleBefore(delayedInterruptEvent);
        }

        current.setBlocked(true); // block the process
        if (current.isScheduled()) {
            current.cancel();
        }
        for (MessageChannel<T> channel : channelsToWaitOn) {
            // Let the channels keep track of the processes which are waiting on
            // them
            channel.addWaitingProcess(current);
        }

        // let the crossbar track all passivated process
        getPassivatedProcessesQueue().insert(current);

        skipTraceNote(); // dont tell the user that...
        try {
            sendTraceNote("SimProcess " + current.getName()
                + " starts waiting at MessageCrossbar " + getName());
            current.passivate();// ...the process is passivated here.
            sendTraceNote("SimProcess "
                + current.getName()
                + " receives a message and finishes waiting at MessageCrossbar "
                + getName());
        } catch (DelayedInterruptException ex) {
            // If the process is interrupted by the delayed interupt, remove it
            // from the crossbar queue and from all channels it might still be
            // waiting on
            sendTraceNote("SimProcess " + current.getName()
                + " interrupts waiting at MessageCrossbar " + getName());
            getPassivatedProcessesQueue().remove(current);
            interruptedWaits++; // increment the number of interrupted waits
            for (MessageChannel<T> channel : messageChannelsByName.values()) {
                channel.abortWaiting(current);
            }

            // Finally re-throw the DelayedInterruptException so a process using
            // the message crossbar can handle the interrupt.
            throw ex;
        }

        // The process has been activated normally. Therefore the delayed
        // interrupt and the processUnblockingEvent have to be unscheduled. Of
        // course this only applies if a wait time limit has been set and a
        // delayed interrupt has been scheduled.
        if (waitUntil != null) {
            current.cancelInterruptDelayed();
            processUnblockingEvent.cancel();
        }

        // Now after the process is active again get the message that activated
        // the process and return it. Additionally remove the process from the
        // list of passivated processes.
        return messagesByProcesses.remove(current);
    }

    /**
     * Wait for a {@link CrossbarMessage} on the specified message channels, thereby passivating the current process.
     * The process is not reactivated until a message is received. The parameter maxWaitTime specifies maximum time the
     * current process will wait for a message. If no message is received before this amount of time has elapsed, the
     * process will be interrupted and a {@link DelayedInterruptException} will be thrown.
     *
     * @param channelsToWaitOn The channels on which to wait for a message
     * @param maxWaitTime      The maximum amount of time until the waiting will be interrupted
     * @return The received message
     * @throws SuspendExecution
     * @throws InterruptException
     */
    public CrossbarMessage waitForMessage(
        List<MessageChannel<T>> channelsToWaitOn, TimeSpan maxWaitTime)
        throws InterruptException, SuspendExecution {
        TimeInstant waitUntil;

        if (maxWaitTime != null) {
            waitUntil = TimeOperations.add(presentTime(), maxWaitTime);
        } else {
            waitUntil = null;
        }

        return waitForMessage(channelsToWaitOn, waitUntil);
    }
}
