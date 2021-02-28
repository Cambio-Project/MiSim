package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.Operation;

/**
 * Wrapper class to ease responding for Requests.
 *
 * @author Lion Wagner
 */
public final class RequestAnswer extends Request {

    public RequestAnswer(Request wrappedRequest) {
        super(wrappedRequest.getModel(),
                "Request_Answer_" + wrappedRequest.getName(),
                wrappedRequest.traceIsOn(),
                wrappedRequest,
                new Operation(wrappedRequest.getModel(), "Dummy", false)); //inserting dummy operation
    }


    /**
     * Unpack the original Request that is answered by this Request
     */
    public Request unpack() {
        return getParent();
    }


}
