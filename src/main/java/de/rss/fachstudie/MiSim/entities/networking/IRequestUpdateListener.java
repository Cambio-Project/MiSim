package de.rss.fachstudie.MiSim.entities.networking;

public interface IRequestUpdateListener {


    /**
     * Listener for the failure of the sending process.
     * This could for example be due to the receiving service not being available, the request being canceled or timed out.
     * Provides a reference to the failed request.
     */
    void onRequestFailed(Request request);


    /**
     * Listener for the successful completion of the sending process.
     * Provides a reference to the successfully arrived request.
     */
    void onRequestArrivalAtTarget(Request request);

    /**
     * Optional Listener for the send-off of a request.
     * Provides the send request.
     */
    default void onRequestSend(Request request) {    }
}
