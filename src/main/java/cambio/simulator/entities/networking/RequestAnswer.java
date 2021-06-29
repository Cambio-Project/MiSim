package cambio.simulator.entities.networking;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.Operation;

/**
 * Wrapper class to ease responding to {@code Request}s.
 *
 * <p>
 * Represents the answering of a {@code Request}.
 *
 * @author Lion Wagner
 */
public final class RequestAnswer extends Request {

    /**
     * Constructs a new {@link RequestAnswer}.
     *
     * @param wrappedRequest {@link Request} to which this is an answer to
     * @param answerSender   sender of the answer
     */
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
     * Unpack the original Request that is answered by this Request.
     *
     * @return the request, that is wrapped by this answer
     */
    public Request unpack() {
        return getParent();
    }


}
