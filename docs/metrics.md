## Output Metrics

MiSim supports the following output metrics.
A csv table for each mertric is generated that contains data pairs of simulation time (column SimulationTime) and the assotiated value (column Value) .

| #   | Metric                            | File                                                                  | Desciption                                                                                      |
| --- | --------------------------------- | --------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------- |
| 1   | System Load                       | G[\<LoadGeneratorName>]\_[\<TargetOperation>]\_Load.csv               | Load accumulated over 1 STU                                                                     |
| 2   | Failed Requests                   | G[\<LoadGeneratorName>]\_[\<TargetOperation>]\_FailedRequests.csv     | Failed Requests accumulated over 1 STU                                                          |
| 3   | Successful Requests               | G[\<LoadGeneratorName>]\_[\<TargetOperation>]\_SuccessfulRequests.csv | Successful Requests accumulated over 1 STU                                                      |
| 4   | Failed Requests Global            | GEN_ALL_FailedRequests.csv                                            | Accumulation of failed requests over all load generators binned by 1 STU.                       |
| 5   | Successful Requests Global        | GEN_ALL_SuccessfulRequests.csv                                        | Accumulation of successful requests over all Load generators binned by 1 STU.                   |
| 6   | Global Response Times             | R[All]ResponseTimes.csv                                               | All measured responsetimes, independent of service.                                             |
| 7   | Response Times                    | R[\<EndpointName>]\_ResponseTimes.csv                                 | Response times simualted for the specific endpoint.                                             |
| 8   | Instance Count                    | S[\<ServiceName>]\_InstanceCount.csv                                  | Number of Instances of the respective service.                                                  |
| 9   | Load Distribution                 | S[\<ServiceName>]\_Load_Distribution.csv                              | List of instances that were chosen as load balancing target.                                    |
| 10  | Autoscaling Decision              | AS[\<servicename>]\_Decision.csv                                      | Holds the autoscaler decision as "UP","HOLD" and "DOWN"                                         |
| 11  | Instance Changes                  | AS[\<servicename>]\_InstanceChange.csv                                | Number of started/stopped instances for a scaling decision                                      |
| 12  | Measured Utilization              | AS[\<servicename>]\_MeasuredUtilization.csv                           | Periodically measured Utilization by the autoscaler                                             |
| 13  | Requests in System                | I[\<InstanceName>]\_Requests_InSystem.csv                             | Number of Requests currently handled by an Instance.                                            |
| 14  | Not Computed Requests             | I[\<InstanceName>]\_Requests_NotComputed.csv                          | Number of Requests that still have a remaining computational demand for the instances' CPU.     |
| 15  | Requests Waiting For Dependencies | I[\<InstanceName>]\_Requests_WaitingForDependencies.csv               | Number of Requests still waiting for dependenies (data collection) to complete.                 |
| 16  | Internal Request Sends            | I[\<InstanceName>]\_SendOff_Internal_Requests.csv                     | Number of open Dependencies. (One request may produces multiple dependencie requests.)          |
| 17  | Instance State                    | I[\<InstanceName>]\_State.csv                                         | State of the instance (Running, Shutting Down, Shut Down, ...)                                  |
| 18  | Active Processes                  | C[<InstanceName>_CPU]\_ActiveProcesses.csv                            | The count of requests that are currently using active CPU time. Essentially active Threads.     |
| 19  | Relative Utilization              | C[<InstanceName>_CPU]\_RelativeUtiliization                           | A measure of how long the CPU will take to complete its active work and clear its queue.        |
| 20  | Total Processes                   | C[<InstanceName>_CPU]\_TotalProcesses.csv                             | Count of requests currently assigend to be handled by the CPU. (Queuelenght + Active Processes) |
| 21  | Utilization                       | C[<InstanceName>_CPU]\_Utilization.csv                                | Percentage utilization of the CPU changes in _n_/_CoreCount_ sized steps.                       |
| 22  | Binned/Avg. Utilization           | C[<InstanceName>_CPU]\_UtilizationBinned.csv                          | Running Average over the utilization with window size of 0.5 STU (not modifyable yet).          |
| 23  | Network Latency                   | NL_latency.csv                                                        | List of calculated Network latencies. **Disabled by default**.                                  |
| 24  | Circuit Breaker State             | CB[\<CircuitBreakerName>]\_[\<CB_TargetService>].csv                  | Tupel of current (State, #SuccessFullRequests,#FailedRequests, FailureRate)                     |
