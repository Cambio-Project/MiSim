package desmoj.extensions.crossbar;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.exception.DelayedInterruptException;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ProcessQueue;
import desmoj.core.simulator.Reportable;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Represents a message channel which resides on a {@link MessageCrossbar}. MessageChannels can only be used in
 * conjunction with a message crossbar.<br /> <br /> On the one hand MessageChannels allow for processes to wait for
 * {@link CrossbarMessage}s on them (passivating the process) and on the other hand they allow for processes to send
 * CrossbarMessages to them. If a CrossbarMessage is received this leads to the immediate activation of all process
 * waiting on the channel.
 *
 * @param <T> Parameter restricting the types of processes which can wait on a MessageChannel.
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
 * @see CrossbarMessage
 */
public class MessageChannel<T extends SimProcess> extends Reportable {

    /**
     * The {@link MessageCrossbar} this MessageChannel resides on.
     */
    private final MessageCrossbar<T> messageCrossbar;

    /**
     * A queue of all processes which are currently waiting on this message channel.
     */
    private final ProcessQueue<T> passivatedProcessesQueue;

    /**
     * A counter keeping track of the messages which were successfully delivered to a waiting process. A message is
     * considered delivered if it was forwarded to at least one waiting process.
     */
    private long deliveredMessages;

    /**
     * A counter keeping track of the messages which could not be delivered to a waiting process. A message is
     * considered lost if it could not be forwarded to any receiver because at the time it was received there was no
     * process waiting on the MessageChannel.
     */
    private long lostMessages;

    /**
     * A counter keeping track of processes which are leaving this channel because they were activated, but were
     * activated by a message received on another channel.
     */
    private long indirectProcessActivations;

    /**
     * A counter keeping track of the processes which were interrupted and removed from this channel because of their
     * max waiting time on the MessageChannel was reached.
     */
    private long interruptedWaits;

    /**
     * Constructs a new MessageChannel.
     *
     * @param owner           The model to which this MessageChannel belongs.
     * @param name            The name of the MessageChannel.
     * @param showInTrace     Whether to show this MessageChannel in the trace or not.
     * @param messageCrossbar The {@link MessageCrossbar} this MessageChannel is associated with.
     */
    MessageChannel(Model owner, String name, boolean showInTrace, MessageCrossbar<T> messageCrossbar) {
        super(owner, name, false, showInTrace);
        this.messageCrossbar = messageCrossbar;
        passivatedProcessesQueue = new ProcessQueue<T>(owner, name + "Queue", false, false);
    }

    /**
     * Removes the given process from the queue of processes waiting for a message and increments the counter of
     * interrupted waits.
     *
     * @param process The process to remove from this channel.
     */
    void abortWaiting(T process) {
        if (passivatedProcessesQueue.get(process) != -1) {
            passivatedProcessesQueue.remove(process);
            interruptedWaits++;
        }
    }

    /**
     * Adds a process to the queue of process waiting for a message on this channel.
     *
     * @param process The process to add to this channel.
     */
    void addWaitingProcess(T process) {
        boolean processAllreadyInSet;

        processAllreadyInSet = getPassivatedProcessesQueue().insert(process);

        if (!processAllreadyInSet) {
            throw new RuntimeException("this should never happen");
            // if this exception is ever thrown this indicates a bug
        }
    }

    /**
     * @return The number of delivered messages. A message is considered delivered if it was forwarded to at least one
     *     waiting process.
     */
    public long getDeliveredMessages() {
        return deliveredMessages;
    }

    /**
     * @return The number of process which were activated by a message directly received on this channel.
     */
    public Object getDirectProcessActivations() {
        return getProcessActivations() - getIndirectProcessActivations();
    }

    /**
     * @return The number of process which were removed from this channel because they were activated (indirectly) by a
     *     message received on another channel.
     */
    public long getIndirectProcessActivations() {
        return indirectProcessActivations;
    }

    /**
     * @return The number of process which were interrupted while waiting on this channel because their max wait time
     *     was exceeded.
     */
    public long getInterruptedWaits() {
        return interruptedWaits;
    }

    /**
     * @return The number of message which were sent to this channel but couldn't be forwarded to a receiver because no
     *     process was waiting on the channel when the message was received.
     */
    public long getLostMessages() {
        return lostMessages;
    }

    /**
     * @return The number of processes which have been handled by this message channel (meaning processes which entered
     *     the channel and also left it again).
     */
    @Override
    public long getObservations() {
        return getPassivatedProcessesQueue().getObservations();
    }

    /**
     * @return The queue of process waiting for messages on this channel.
     */
    ProcessQueue<T> getPassivatedProcessesQueue() {
        return passivatedProcessesQueue;
    }

    /**
     * @return The number of process which were removed from this channel because they were activated either by a
     *     message received directly on this channel or by a message received (indirectly) on another channel.
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
     * Receive a message which leads to the updating of several internal counters and the removal of all waiting
     * processes from this channel.
     *
     * @return A Set of all removed processes.
     */
    Set<T> receiveMessageAndRemoveWaitingProcesses() {
        Set<T> temp;

        // increment the counter of received messages

        temp = new HashSet<T>();
        while (!getPassivatedProcessesQueue().isEmpty()) {
            T process;

            process = getPassivatedProcessesQueue().first();
            temp.add(process);
            getPassivatedProcessesQueue().remove(process);
        }

        if (temp.isEmpty()) {
            lostMessages++; // no processes is activated. As no process receives
            // the message it is considered lost.
        } else {
            deliveredMessages++; // At least one process is activated, so its
            // considered delivered
        }

        return temp;
    }

    /**
     * Removes all processes from this channel which have been activated by a message received on another channel (on
     * which the given processes were also waiting).
     *
     * @param processesToRemove The removed processes.
     */
    void removePassivatedProcessesThatWereActivatedByAMessageOnAnotherChannel(Collection<T> processesToRemove) {
        for (T process : processesToRemove) {
            if (passivatedProcessesQueue.get(process) != -1) {
                passivatedProcessesQueue.remove(process);
                // The process has been activated by a message which was
                // received on another channel. For statistical purposes each of
                // these indirect activations has to be counted.
                indirectProcessActivations++;
            }
        }
    }

    @Override
    public void reset() {
        getPassivatedProcessesQueue().reset();
        deliveredMessages = 0;
        lostMessages = 0;
        indirectProcessActivations = 0;
        interruptedWaits = 0;
    }

    @Override
    public TimeInstant resetAt() {
        return getPassivatedProcessesQueue().resetAt();
    }

    /**
     * Sends the given {@link CrossbarMessage} to the channel, thereby activating all processes that are waiting on the
     * channel.
     *
     * @param message The message to send to the channel.
     */
    @SuppressWarnings("unchecked")
    public void send(CrossbarMessage message) {
        messageCrossbar.sendMessage(message, Arrays.asList(this));
    }

    /**
     * Wait for a {@link CrossbarMessage} on this channel, thereby passivating the current process. The process is not
     * reactivated until a message is received.
     *
     * @return The message that was received.
     */
    @SuppressWarnings("unchecked")
    public CrossbarMessage waitForMessage() throws SuspendExecution {
        return messageCrossbar.waitForMessage(Arrays.asList(this));
    }

    /**
     * Wait for a {@link CrossbarMessage} on this channel, thereby passivating the current process. The process is not
     * reactivated until a message is received. The parameter waitUntil specifies the point in time to which the current
     * process will wait for a message. If no message is received before that time the process will be interrupted and a
     * {@link DelayedInterruptException} will be thrown.
     *
     * @param waitUntil The point in time up to which the process will wait for a message before the waiting is
     *                  aborted.
     * @return The message that was received.
     * @throws DelayedInterruptException The exception that indicates the process has reached its max wait time and has
     *                                   been interrupted.
     */
    @SuppressWarnings("unchecked")
    public CrossbarMessage waitForMessage(TimeInstant waitUntil) throws DelayedInterruptException, SuspendExecution {
        return messageCrossbar.waitForMessage(Arrays.asList(this), waitUntil);
    }

    /**
     * Wait for a {@link CrossbarMessage} on this channel, thereby passivating the current process. The process is not
     * reactivated until a message is received. The parameter maxWaitTime specifies the time the current process will
     * wait for a message. If no message is received before the wait time has passed the process will be interrupted and
     * a {@link DelayedInterruptException} will be thrown.
     *
     * @param maxWaitTime The time the process will wait for a message before the waiting is aborted.
     * @return The message that was received.
     * @throws DelayedInterruptException The exception that indicates the process has reached its max wait time and has
     *                                   been interrupted.
     */
    @SuppressWarnings("unchecked")
    public CrossbarMessage waitForMessage(TimeSpan maxWaitTime) throws DelayedInterruptException, SuspendExecution {
        return messageCrossbar.waitForMessage(Arrays.asList(this), maxWaitTime);
    }

}
