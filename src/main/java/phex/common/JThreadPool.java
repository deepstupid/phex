/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2008 Phex Development Group
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  --- SVN Information ---
 *  $Id$
 */
package phex.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class JThreadPool {
    private static final Logger logger = LoggerFactory.getLogger(JThreadPool.class);
    private final ExecutorService pool;

    public JThreadPool() {
        this(ForkJoinPool.commonPool());
    }

    public JThreadPool(ExecutorService pool) {
        this.pool = pool;
        //        pool = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 30, TimeUnit.SECONDS,
//                new SynchronousQueue<>(), new DefaultThreadFactory());
        //pool = new ForkJoinPool(4);
    }

    public ExecutorService getThreadPool() {
        return pool;
    }

    public void execute(final Runnable runnable, final String name) {
        pool.execute(new NamedThreadRunnable(name, runnable));
    }

    public void shutdown() {

        pool.shutdown();
    }

    private static final class NamedThreadRunnable implements Runnable {
        private final String name;

        private final Runnable runnable;

        private NamedThreadRunnable(String name, Runnable runnable) {
            this.name = name;
            this.runnable = runnable;
        }

        public void run() {
            Thread currentThread = Thread.currentThread();
            String oldName = currentThread.getName();
            currentThread.setName(name + '-' + oldName);
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.error(t.toString(), t);
            } finally {
                currentThread.setName(oldName);
            }
        }
    }
//
//    /**
//     * The default thread factory
//     */
//    private class DefaultThreadFactory implements ThreadFactory {
//        final ThreadGroup group;
//        final AtomicInteger threadNumber = new AtomicInteger(1);
//        final String namePrefix;
//
//        DefaultThreadFactory() {
//            SecurityManager s = System.getSecurityManager();
//            group = (s != null) ? s.getThreadGroup() :
//                    Thread.currentThread().getThreadGroup();
//            namePrefix = "PhexPool-thread-";
//        }
//
//        public Thread newThread(Runnable r) {
//            logger.debug("Creating new thread for pool: {} {}", pool.getPoolSize(), pool.getActiveCount());
//            Thread t = new Thread(group, r,
//                    namePrefix + threadNumber.getAndIncrement(), 0);
//            if (t.isDaemon())
//                t.setDaemon(false);
//            if (t.getPriority() != Thread.NORM_PRIORITY)
//                t.setPriority(Thread.NORM_PRIORITY);
//            return t;
//        }
//    }
}