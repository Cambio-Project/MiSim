package desmoj.extensions.visualization2d.engine.viewer;


/**
 * SimulationThread runs run-part of cmds-file and is started from Viewer.fileReset() after run init-part of cmds-file.
 * This thread will be interrupted from Viewer.fileReset or Viewer.fileClose.
 *
 * @author christian.mueller@th-wildau.de For information about subproject: desmoj.extensions.visualization2d please
 *     have a look at: http://www.th-wildau.de/cmueller/Desmo-J/Visualization2d/
 *     <p>
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 *     with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *     <p>
 *     Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 *     the specific language governing permissions and limitations under the License.
 * @version DESMO-J, Ver. 2.5.1e copyright (c) 2017
 */
public class SimulationThread extends Thread {

    private static boolean debugOutput = false;
    ViewerPanel viewer;
    long s0;    // simulation time now
    long s1;    // next simulation time

    public SimulationThread(ViewerPanel viewer, long firstStepTime) {
        this.viewer = viewer;
        this.s0 = viewer.getSimulationTimeInstance().getSimulationStart();
        this.s1 = firstStepTime;
        this.viewer.getSimulationTimeInstance().setSimulationTime(s0);
        this.viewer.updateSimulationTime(true);
    }

    /**
     * Permits switching the debug output of the viewer's time steps to system out.
     *
     * @param debug Switch output on (<code>true</code>) or off (<code>false</code>, default)
     */
    public static void debugToSystemOut(boolean debug) {
        debugOutput = debug;
    }

    public void run() {
        long delay;    // additional delay between 2 animation steps
        double speed;    // animation speed
        long t0 = System.currentTimeMillis();    // system time now
        long t1;        // system time at s1 with given animation speed

        while (!this.isInterrupted() && s1 < Long.MAX_VALUE) {

            if (this.isWorking()) {
                //System.out.println("SimulationThread no pause");
                speed = viewer.getSimulationTimeInstance().getSpeed();

                switch (viewer.getTimeFlowMode()) {
                    case ViewerPanel.TimeFlowMode_CONTINIUM:
                        t1 = Math.round((this.s1 - this.s0) / speed) + t0;
                        // mindest delay  10 millisec.
                        delay = Math.max(10, t1 - System.currentTimeMillis());
                        this.myPause(delay, s0, s1);
                        this.viewer.getSimulationTimeInstance().setSimulationTime(s1);
                        this.viewer.updateSimulationTime(true);
                        double realSpeed = (double) (s1 - s0) / (double) (System.currentTimeMillis() - t0);
                        if (debugOutput) {
                            System.out.println("continuous SimTime from: " + s0 + " until: " + s1 +
                                "  ViewerZoom: " + viewer.getSimulationZoom() +
                                "  ViewerSpeed: " + speed + "  RealSpeed/ViewerSpeed: " + (realSpeed / speed));
                        }
                        break;
                    case ViewerPanel.TimeFlowMode_STEP_FLOW:
                        // mindest delay  10 millisec.
                        delay = Math.max(10, Math.round(1000 / speed));
                        this.myPause(delay, s0, s1);
                        this.viewer.getSimulationTimeInstance().setSimulationTime(s1);
                        this.viewer.updateSimulationTime(true);
                        if (debugOutput) {
                            System.out.println("stepFlow SimTime from: " + s0 + " until: " + s1 +
                                "  ViewerZoom: " + viewer.getSimulationZoom() + "  ViewerSpeed: " + speed);
                        }
                        break;
                    case ViewerPanel.TimeFlowMode_STEP_SINGLE:
                        this.viewer.getSimulationTimeInstance().pause();
                        this.viewer.getSimulationTimeInstance().setSimulationTime(s1);
                        this.viewer.updateSimulationTime(true);
                        if (debugOutput) {
                            System.out.println("single Step SimTime: " + s0 + " nextCmd: " + s1 +
                                "  ViewerZoom: " + viewer.getSimulationZoom() + "  ViewerSpeed: " + speed);
                        }
                        break;
                }
                // update
                t0 = System.currentTimeMillis();
                this.s0 = this.s1;
                this.s1 = viewer.executeRunCommands(s0);
            } else {
                // not running or pause
                //System.out.println("SimulationThread not running or pause");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    this.interrupt();
                }
                t0 = System.currentTimeMillis();
            }
            this.viewer.writeStatusMsg();
        }
        // Animation abgelaufen
        viewer.setStatusMessage("Animation has finished");
    }

    /**
     * Animation is working when simulation time is running and has no pause
     *
     * @return
     */
    public boolean isWorking() {
        boolean running = viewer.getSimulationTimeInstance().isRunning();
        boolean pause = viewer.getSimulationTimeInstance().isPause();
        return running && !pause;
    }

    /**
     * Make a delay of delay millisec. and shows simulation time in viewer from s0 until s1. The delay is finished when
     * isWorking switch to false. Every 100 millisec the simulation time is updated.
     *
     * @param delay Delay in system time (millisec)
     * @param s0    Begin of delay in simulation time
     * @param s1    End od delay in simulation time
     */
    private void myPause(long delay, long s0, long s1) {
        //System.out.println("myPause begin  delay: "+delay+"   s1-s0: "+(s1-s0));
        long t_temp = System.currentTimeMillis();
        while (this.isWorking() && System.currentTimeMillis() - t_temp < delay - 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                this.interrupt();
            }
            double delta = (double) (System.currentTimeMillis() - t_temp) / (double) delay;
            long s_delta = s0 + Math.round(delta * (s1 - s0));
            this.viewer.getSimulationTimeInstance().setSimulationTime(s_delta);
            this.viewer.updateSimulationTime(true);
            //System.out.println("myPause delta: "+delta+"    s_delta: "+s_delta);
            this.viewer.updateDynamic(s_delta);
        }
        if (this.isWorking()) {
            try {
                Thread.sleep(Math.max(0, delay + t_temp - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                this.interrupt();
            }
        }
        //System.out.println("myPause end");
    }

}
