package cambio.simulator.entities.networking;

import cambio.simulator.misc.Priority;
import desmoj.core.simulator.TimeInstant;

/**
 * Interface for listening for request updates. <br> Each implementing class can be registered at a {@code
 * RequestSender} to receive updates of all its send messages.
 *
 * <p>
 * This interface only contains optional Methods.
 *
 * @see RequestSender
 */
public interface IRequestUpdateListener extends Comparable<IRequestUpdateListener> {

    /**
     * Listener for the failure of the sending process. This could for example be due to the receiving service not being
     * available, the request being canceled or timed out. Provides a reference to the failed request.
     *
     * @param when    time of this event
     * @param request request that triggered this event
     * @param reason  reason why the request failed
     * @return true if the request was terminally handled (consumed) by this instance
     */
    default boolean onRequestFailed(final Request request, final TimeInstant when, final RequestFailedReason reason) {
        return false;
    }


    /**
     * Listener for the successful completion of the sending process. Provides a reference to the successfully arrived
     * request.
     *
     * @param when    time of this event
     * @param request request that triggered this event
     * @return true if the request was terminally handled (consumed) by this instance
     */
    default boolean onRequestArrivalAtTarget(final Request request, final TimeInstant when) {
        return false;
    }

    /**
     * Listener for the send-off of a request. Provides the send request.
     *
     * @param when    time of this event
     * @param request request that triggered this event
     * @return true if the request was terminally handled (consumed) by this instance
     */
    default boolean onRequestSend(final Request request, final TimeInstant when) {
        return false;
    }

    /**
     * Listener for the successful receiving of the answer of a request.
     *
     * @param when    time of this event
     * @param request request that triggered this event
     * @return true if the request was terminally handled (consumed) by this instance
     */
    default boolean onRequestResultArrivedAtRequester(final Request request, final TimeInstant when) {
        return false;
    }


    /**
     * Gets the priority of this listener. Listeners with higher priority will be notified first about the status of a
     * request. The interaction for listeners with equal priority is undefined.
     *
     * @return the Priority of this Listener. Defaults to {@code Priority#NORMAL}
     * @see Priority
     */
    default int getListeningPriority() {
        return Priority.NORMAL;
    }

    /**
     * Natural ordering is done by {@code IRequestUpdateListener#getListeningPriority}.
     */
    @Override
    default int compareTo(IRequestUpdateListener other) {
        if (other == null) {
            return 0;
        }
        //inverse comparison so higher values have higher priority
        return other.getListeningPriority() - this.getListeningPriority();
    }
}
