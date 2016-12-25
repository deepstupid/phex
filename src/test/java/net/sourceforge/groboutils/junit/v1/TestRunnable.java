//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.sourceforge.groboutils.junit.v1;

import junit.framework.Assert;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner.TestDeathException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class TestRunnable extends Assert implements Runnable {
    private static final Class THIS_CLASS;
    protected static final Logger LOG;
    private static int testCount;
    private MultiThreadedTestRunner mttr;
    private int testIndex;
    private boolean ignoreStopErrors;

    public TestRunnable() {
        this.ignoreStopErrors = false;
        Class var1 = THIS_CLASS;
        synchronized(var1) {
            this.testIndex = testCount++;
        }
    }

    TestRunnable(boolean ignoreStopErrors) {
        this();
        this.ignoreStopErrors = ignoreStopErrors;
    }

    public abstract void runTest() throws Throwable;

    public void delay(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    public void run() {
        if(this.mttr == null) {
            throw new IllegalStateException("Owning runner never defined.  The runnables should only be started through the MultiThreadedTestRunner instance.");
        } else {
            LOG.info("Starting test thread " + this.testIndex);

            try {
                this.runTest();
            } catch (InterruptedException var4) {
                ;
            } catch (TestDeathException var5) {
                if(!this.ignoreStopErrors) {
                    LOG.info("Aborted test thread " + this.testIndex);
                    throw var5;
                }
            } catch (Throwable var6) {
                this.mttr.handleException(var6);
            }

            LOG.info("Ended test thread " + this.testIndex);
        }
    }

    public boolean isDone() {
        return this.mttr.areThreadsFinished();
    }

    void setTestRunner(MultiThreadedTestRunner mttr) {
        this.mttr = mttr;
    }

    static {
        THIS_CLASS = MultiThreadedTestRunner.class;
        LOG = LoggerFactory.getLogger(THIS_CLASS);
        testCount = 0;
    }
}
