package de.rss.fachstudie.MiSim.entities.networking;

import desmoj.core.simulator.TimeInstant;

public interface IRequestUpdateListener extends Comparable<IRequestUpdateListener> {


    /**
     * Listener for the failure of the sending process. This could for example be due to the receiving service not being
     * available, the request being canceled or timed out. Provides a reference to the failed request.
     */
    default void onRequestFailed(final Request request, final TimeInstant when, final RequestFailedReason reason) {
    }


    /**
     * Listener for the successful completion of the sending process. Provides a reference to the successfully arrived
     * request.
     */
    default void onRequestArrivalAtTarget(final Request request, final TimeInstant when) {
    }

    /**
     * Optional Listener for the send-off of a request. Provides the send request.
     */
    default void onRequestSend(final Request request, final TimeInstant when) {
    }

    /**
     * Listener for the successful receiving of the answer of a request.
     */
    default void onRequestResultArrivedAtRequester(final Request request, final TimeInstant when) {

    }


    default int getListeningPriority() {
        return 0;
    }

    @Override
    default int compareTo(IRequestUpdateListener o) {
        if (o == null) return 0;
        return o.getListeningPriority() - this.getListeningPriority();//inversed comparission so higher values have higher priority
    }
}
