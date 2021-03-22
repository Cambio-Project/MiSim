package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.TimeInstant;

public interface IRequestUpdateListener extends Comparable<IRequestUpdateListener> {

    /**
     * Listener for the failure of the sending process. This could for example be due to the receiving service not being
     * available, the request being canceled or timed out. Provides a reference to the failed request.
     *
     * @return true if the request was terminally handled by this instance
     */
    default boolean onRequestFailed(final Request request, final TimeInstant when, final RequestFailedReason reason) {
        return false;
    }


    /**
     * Listener for the successful completion of the sending process. Provides a reference to the successfully arrived
     * request.
     *
     * @return true if the request was terminally handled by this instance
     */
    default boolean onRequestArrivalAtTarget(final Request request, final TimeInstant when) {
        return false;
    }

    /**
     * Listener for the send-off of a request. Provides the send request.
     *
     * @return true if the request was terminally handled by this instance
     */
    default boolean onRequestSend(final Request request, final TimeInstant when) {
        return false;
    }

    /**
     * Listener for the successful receiving of the answer of a request.
     *
     * @return true if the request was terminally handled by this instance
     */
    default boolean onRequestResultArrivedAtRequester(final Request request, final TimeInstant when) {
        return false;
    }


    default int getListeningPriority() {
        return Priority.NORMAL;
    }

    @Override
    default int compareTo(IRequestUpdateListener o) {
        if (o == null) return 0;
        return o.getListeningPriority() - this.getListeningPriority();//inversed comparission so higher values have higher priority
    }
}
