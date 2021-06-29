package de.rss.fachstudie.MiSim.entities.microservice;

/**
 * Represents the possible states of a {@code MicroserviceInstance}.
 *
 * @see MicroserviceInstance
 */
public enum InstanceState {
    /**
     * The instance was recently created and its existence is noted. It should be ready to start up.
     *
     * @see MicroserviceInstance
     */
    CREATED,
    /**
     * The startup process was triggered.
     *
     * @see MicroserviceInstance
     */
    STARTING,
    /**
     * The startup process was completed successfully.<br> The instance can now receive {@code Requests}
     *
     * @see MicroserviceInstance
     */
    RUNNING,
    /**
     * The shutdown process of this instance was triggered. <br> It does not accept subsequent requests, but it tries to
     * finish currently active requests.
     *
     * @see MicroserviceInstance
     */
    SHUTTING_DOWN,
    /**
     * The shutdown process was completed successfully.<br> The instance does not accept any requests.<br> The instance
     * can be restarted to continue working.
     *
     * @see MicroserviceInstance
     */
    SHUTDOWN,
    /**
     * The instance was forcibly shut down and is locked in that state. All current handling processes (and potentially
     * outgoing requests) were aborted.
     *
     * @see MicroserviceInstance
     */
    KILLED
}
