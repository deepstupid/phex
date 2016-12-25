//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.sourceforge.groboutils.junit.v1;

import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MultiThreadedTestRunner {
    private static final Class THIS_CLASS;
    private static final String THIS_CLASS_NAME;
    private static final Logger LOG;
    private static final long DEFAULT_MAX_FINAL_JOIN_TIME = 30000L;
    private static final long DEFAULT_MAX_WAIT_TIME = 86400000L;
    private static final long MIN_WAIT_TIME = 10L;
    private Object synch;
    private boolean threadsFinished;
    private ThreadGroup threadGroup;
    private Thread coreThread;
    private Throwable exception;
    private TestRunnable[] runners;
    private TestRunnable[] monitors;
    private long maxFinalJoinTime;
    private long maxWaitTime;
    private boolean performKills;

    public MultiThreadedTestRunner(TestRunnable[] tr) {
        this(tr, (TestRunnable[])null);
    }

    public MultiThreadedTestRunner(TestRunnable[] runners, TestRunnable[] monitors) {
        this.synch = new Object();
        this.threadsFinished = false;
        this.maxFinalJoinTime = 30000L;
        this.maxWaitTime = 86400000L;
        this.performKills = true;
        if(runners == null) {
            throw new IllegalArgumentException("no null runners");
        } else {
            int len = runners.length;
            if(len <= 0) {
                throw new IllegalArgumentException("must have at least one runnable");
            } else {
                this.runners = new TestRunnable[len];
                System.arraycopy(runners, 0, this.runners, 0, len);
                if(monitors != null) {
                    len = monitors.length;
                    this.monitors = new TestRunnable[len];
                    System.arraycopy(monitors, 0, this.monitors, 0, len);
                } else {
                    this.monitors = new TestRunnable[0];
                }

            }
        }
    }

    public void runTestRunnables() throws Throwable {
        this.runTestRunnables(-1L);
    }

    public void runTestRunnables(long maxTime) throws Throwable {
        Thread.interrupted();
        this.exception = null;
        this.coreThread = Thread.currentThread();
        this.threadGroup = new ThreadGroup(THIS_CLASS_NAME);
        this.threadsFinished = false;
        Thread[] monitorThreads = this.setupThreads(this.threadGroup, this.monitors);
        Thread[] runnerThreads = this.setupThreads(this.threadGroup, this.runners);

        boolean threadsStillRunning;
        try {
            threadsStillRunning = this.joinThreads(runnerThreads, maxTime);
        } catch (InterruptedException var21) {
            threadsStillRunning = true;
        } finally {
            Object var9 = this.synch;
            synchronized(var9) {
                if(!this.threadsFinished) {
                    this.interruptThreads();
                } else {
                    LOG.debug("All threads finished within timeframe.");
                }

            }
        }

        if(threadsStillRunning) {
            LOG.debug("Halting the test threads.");
            this.setTimeoutError(maxTime);

            try {
                this.joinThreads(runnerThreads, this.maxFinalJoinTime);
            } catch (InterruptedException var20) {
                ;
            }

            int ex = this.killThreads(runnerThreads);
            if(ex > 0) {
                LOG.error(ex + " thread(s) did not stop themselves.");
                this.setTimeoutError(this.maxFinalJoinTime);
            }
        }

        LOG.debug("Halting the monitor threads.");

        try {
            this.joinThreads(monitorThreads, this.maxFinalJoinTime);
        } catch (InterruptedException var19) {
            ;
        }

        this.killThreads(monitorThreads);
        if(this.exception != null) {
            LOG.debug("Exception occurred during testing.", this.exception);
            throw this.exception;
        } else {
            LOG.debug("No exceptions caused during execution.");
        }
    }

    void handleException(Throwable t) {
        LOG.warn("A test thread caused an exception.", t);
        Object var2 = this.synch;
        synchronized(var2) {
            if(this.exception == null) {
                LOG.debug("Setting the exception to:", t);
                this.exception = t;
            }

            if(!this.threadsFinished) {
                this.interruptThreads();
            }
        }

        if(t instanceof ThreadDeath) {
            throw (ThreadDeath)t;
        }
    }

    void interruptThreads() {
        LOG.debug("Forcing all test threads to stop.");
        Object var1 = this.synch;
        synchronized(var1) {
            if(Thread.currentThread() != this.coreThread) {
                this.coreThread.interrupt();
            }

            this.threadsFinished = true;
            int count = this.threadGroup.activeCount();
            Thread[] t = new Thread[count];
            this.threadGroup.enumerate(t);
            int i = t.length;

            while(true) {
                --i;
                if(i < 0) {
                    return;
                }

                if(t[i] != null && t[i].isAlive()) {
                    t[i].interrupt();
                }
            }
        }
    }

    boolean areThreadsFinished() {
        return this.threadsFinished;
    }

    private Thread[] setupThreads(ThreadGroup tg, TestRunnable[] tr) {
        int len = tr.length;
        Thread[] threads = new Thread[len];

        for(int i = 0; i < len; ++i) {
            tr[i].setTestRunner(this);
            threads[i] = new Thread(tg, tr[i]);
            threads[i].setDaemon(true);
        }

        for(int i1 = 0; i1 < len; ++i1) {
            threads[i1].start();

            int count;
            for(count = 0; !threads[i1].isAlive() && count < 10; ++count) {
                LOG.debug("Waiting for thread at index " + i1 + " to start.");
                Thread.yield();
            }

            if(count >= 10) {
                LOG.debug("Assuming thread at index " + i1 + " already finished.");
            }
        }

        return threads;
    }

    private boolean joinThreads(Thread[] t, long waitTime) throws InterruptedException {
        if(t == null) {
            return false;
        } else {
            int len = t.length;
            if(len <= 0) {
                return false;
            } else {
                if(waitTime < 0L || waitTime > this.maxWaitTime) {
                    waitTime = 86400000L;
                }

                boolean threadsRunning = true;
                InterruptedException iex = null;

                boolean enteredLoop;
                for(long finalTime = System.currentTimeMillis() + waitTime; threadsRunning && System.currentTimeMillis() < finalTime && iex == null; threadsRunning = threadsRunning || !enteredLoop) {
                    LOG.debug("Time = " + System.currentTimeMillis() + "; final = " + finalTime);
                    threadsRunning = false;
                    enteredLoop = false;

                    for(int i = 0; i < len && System.currentTimeMillis() < finalTime; ++i) {
                        enteredLoop = true;
                        if(t[i] != null) {
                            try {
                                t[i].join(10L);
                            } catch (InterruptedException var12) {
                                LOG.debug("Join for thread at index " + i + " was interrupted.");
                                iex = var12;
                            }

                            if(!t[i].isAlive()) {
                                LOG.debug("Joined thread at index " + i);
                                t[i] = null;
                            } else {
                                LOG.debug("Thread at index " + i + " still running.");
                                threadsRunning = true;
                            }
                        }
                    }
                }

                if(iex != null) {
                    throw iex;
                } else {
                    return threadsRunning;
                }
            }
        }
    }

    private int killThreads(Thread[] t) {
        int killCount = 0;

        for(int i = 0; i < t.length; ++i) {
            if(t[i] != null && t[i].isAlive()) {
                LOG.debug("Stopping thread at index " + i);
                ++killCount;
                if(!this.performKills) {
                    LOG.error("Did not stop thread " + t[i]);
                } else {
                    int count = 0;

                    for(boolean isAlive = t[i].isAlive(); isAlive && count < 10; ++count) {
                        t[i].stop(new MultiThreadedTestRunner.TestDeathException("Thread " + i + " did not die on its own"));
                        LOG.debug("Waiting for thread at index " + i + " to stop.");
                        Thread.yield();
                        isAlive = t[i].isAlive();
                        if(isAlive) {
                            t[i].interrupt();
                        }
                    }

                    if(count >= 10) {
                        LOG.error("Thread at index " + i + " did not stop!");
                    }

                    t[i] = null;
                }
            }
        }

        return killCount;
    }

    private void setTimeoutError(long maxTime) {
        Throwable t = this.createTimeoutError(maxTime);
        Object var4 = this.synch;
        synchronized(var4) {
            if(this.exception == null) {
                LOG.debug("Setting the exception to a timeout exception.");
                this.exception = t;
            }

        }
    }

    private Throwable createTimeoutError(long maxTime) {
        Throwable ret = null;

        try {
            Assert.fail("Threads did not finish within " + maxTime + " milliseconds.");
        } catch (ThreadDeath var6) {
            throw var6;
        } catch (Throwable var7) {
            var7.fillInStackTrace();
            ret = var7;
        }

        return ret;
    }

    static {
        THIS_CLASS = MultiThreadedTestRunner.class; //class$net$sourceforge$groboutils$junit$v1$MultiThreadedTestRunner == null?(class$net$sourceforge$groboutils$junit$v1$MultiThreadedTestRunner = class$("net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner")):class$net$sourceforge$groboutils$junit$v1$MultiThreadedTestRunner;
        THIS_CLASS_NAME = THIS_CLASS.getName();
        LOG = LoggerFactory.getLogger(THIS_CLASS);
    }

    public static final class TestDeathException extends RuntimeException {
        private TestDeathException(String msg) {
            super(msg);
        }
    }
}
