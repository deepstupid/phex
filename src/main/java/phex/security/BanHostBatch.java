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
 *  $Id: BanHostBatch.java 4205 2008-06-18 09:49:16Z gregork $
 */
package phex.security;

import phex.common.ExpiryDate;
import phex.common.RunnerQueueWorker;
import phex.common.ThreadTracking;
import phex.common.address.DestAddress;
import phex.common.log.NLogger;
import phex.util.Localizer;

import java.util.Stack;

/**
 * The ban host batch is responsible to ban larger amount of addresses in the
 * background.
 */
public class BanHostBatch extends RunnerQueueWorker {
    private static final Object lock = new Object();

    private static BanHostBatch instance;

    private final PhexSecurityManager securityService;
    private final Stack<BanHostHolder> rules;
    private Thread runnerThread;


    private BanHostBatch(PhexSecurityManager securityService) {
        rules = new Stack<BanHostHolder>();
        this.securityService = securityService;
    }

    private static void init(PhexSecurityManager securityService) {
        synchronized (lock) {
            if (instance == null) {
                NLogger.debug(BanHostBatch.class, "Creating Instance");
                instance = new BanHostBatch(securityService);
            }
            if (instance.runnerThread == null) {
                instance.createRunner();
            }
        }
    }

    public static void addDestAddress(DestAddress address, ExpiryDate expDate,
                                      PhexSecurityManager securityService) {
        synchronized (lock) {
            init(securityService);
            instance.rules.add(new BanHostHolder(address, expDate));
        }
        synchronized (instance) {
            instance.notify();
        }
    }

    private synchronized void createRunner() {
        NLogger.debug(BanHostBatch.class, "Creating RunnerThread");
        runnerThread = new Thread(ThreadTracking.rootThreadGroup,
                new BatchWorker());
        runnerThread.setPriority(Thread.NORM_PRIORITY);
        runnerThread.setDaemon(true);
        runnerThread.start();
    }

    private static class BanHostHolder {
        private final DestAddress address;

        private final ExpiryDate expDate;

        public BanHostHolder(DestAddress address, ExpiryDate expDate) {
            this.address = address;
            this.expDate = expDate;
        }
    }

    private class BatchWorker implements Runnable {
        public void run() {
            try {
                while (true) {
                    BanHostHolder next = rules.pop();
                    AccessType access = securityService
                            .controlHostAddressAccess(next.address);
                    // only add if not already added through earlier batch.
                    if (access == AccessType.ACCESS_GRANTED) {
                        securityService.createIPAccessRule(
                                Localizer.getString("UserBanned"),
                                next.address.getIpAddress().getHostIP(), (byte) 32,
                                false, next.expDate, true);
                    }

                    synchronized (instance) {
                        if (rules.isEmpty()) {
                            instance.wait(30 * 1000);
                        }
                        if (!rules.isEmpty()) {
                            continue;
                        }
                        // rules are still empty... stop batch process...
                        runnerThread = null;
                        NLogger.debug(BanHostBatch.class, "Releasing RunnerThread");
                        break;
                    }
                }
            } catch (Throwable th) {
                runnerThread = null;
                NLogger.error(BanHostBatch.class, th, th);
            }
            // Safety check
            synchronized (lock) {
                if (!rules.isEmpty()) {// oups... somebody is left we need to restart..
                    createRunner();
                } else {
                    instance = null;
                }
            }
        }
    }
}