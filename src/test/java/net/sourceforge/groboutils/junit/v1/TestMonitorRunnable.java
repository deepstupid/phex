//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.sourceforge.groboutils.junit.v1;

public abstract class TestMonitorRunnable extends TestRunnable {
    public TestMonitorRunnable() {
        super(true);
    }

    public abstract void runMonitor() throws Throwable;

    public void runTest() throws Throwable {
        while(!this.isDone() && !Thread.interrupted()) {
            this.runMonitor();
            TestMonitorRunnable.yieldProcessing();
        }

        this.runMonitor();
    }

    protected static void yieldProcessing() throws InterruptedException {
        Thread.yield();
    }
}
