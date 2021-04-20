package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;

/**
 * Wrapper class to ease responding to {@code Request}s.
 * <p>
 * Represents the answering of a {@code Request}.
 *
 * @author Lion Wagner
 */
public final class RequestAnswer extends Request {

    public RequestAnswer(Request wrappedRequest, MicroserviceInstance answerSender) {
        super(wrappedRequest.getModel(),
                "Request_Answer_" + wrappedRequest.getName(),
                wrappedRequest.traceIsOn(),
                wrappedRequest,
                new Operation(wrappedRequest.getModel(), "Dummy", false, null, 0),
                answerSender); //inserting dummy operation
        this.getUpdateListeners().addAll(wrappedRequest.getUpdateListeners());
    }

    public MicroserviceInstance getAnswerSender() {
        return getRequester();
    }


    /**
     * Unpack the original Request that is answered by this Request
     */
    public Request unpack() {
        return getParent();
    }


}
