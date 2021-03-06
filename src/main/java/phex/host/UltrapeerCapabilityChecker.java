/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2011 Phex Development Group
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
 *  $Id: UltrapeerCapabilityChecker.java 4523 2011-06-22 09:27:23Z gregork $
 */
package phex.host;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.Environment;
import phex.common.LongObj;
import phex.common.bandwidth.BandwidthController;
import phex.BandwidthPrefs;
import phex.ConnectionPrefs;
import phex.peer.Peer;
import phex.statistic.StatisticProviderConstants;
import phex.statistic.StatisticsManager;
import phex.statistic.UptimeStatisticProvider;
import phex.util.DateUtils;

import java.util.TimerTask;

// TODO could be LifeCycle..
public class UltrapeerCapabilityChecker extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(
            UltrapeerCapabilityChecker.class);
    /**
     * The time period in millis to wait between checks.
     */
    private static final long TIMER_PERIOD = 10 * 1000;

    private static final long ONE_HOUR = 60 * 60 * 1000;
    private static final long HALF_HOUR = 30 * 60 * 1000;

    private final Peer peer;
    private final StatisticsManager statisticsService;
    private final boolean isUltrapeerOS;

    private volatile boolean isUltrapeerCapable;
    private volatile boolean isPerfectUltrapeer;


    public UltrapeerCapabilityChecker(Peer peer,
                                      StatisticsManager statisticsService) {
        if (peer == null) {
            throw new NullPointerException("servent missing.");
        }
        if (statisticsService == null) {
            throw new NullPointerException("statisticsService missing.");
        }
        this.peer = peer;
        this.statisticsService = statisticsService;

        Environment env = Environment.getInstance();
        isUltrapeerOS = Environment.isUltrapeerOS();

        env.scheduleTimerTask(this, 0, TIMER_PERIOD);
    }

    /**
     * Provided run implementation of TimerTask.
     */
    @Override
    public void run() {
        try {
            checkIfUltrapeerCapable();
        } catch (Throwable th) {
            logger.error(th.toString(), th);
        }
    }

    private void checkIfUltrapeerCapable() {
        UptimeStatisticProvider uptimeProvider = (UptimeStatisticProvider) statisticsService.
                getStatisticProvider(StatisticProviderConstants.UPTIME_PROVIDER);
        if (uptimeProvider == null) {// we can't measure uptime... -> we are not capable...
            isUltrapeerCapable = false;
            return;
        }

        
        boolean isCapable =
                // the first check if we are allowed to become a ultrapeer at all...
                // if not we don't need to continue checking...
                peer.connectionPrefs.AllowToBecomeUP.get() &&
                        // host should not be firewalled.
                        !peer.isFirewalled() &&
                        // host should provide a Ultrapeer capable OS
                        isUltrapeerOS &&
                        // the connection speed should be more then single ISDN
                        peer.bandwidthPrefs.NetworkSpeedKbps.get() > 64 &&
                        // also we should provide at least 10KB network bandwidth
                        peer.bandwidthPrefs.MaxNetworkBandwidth.get() > 10 * 1024 &&
                        // and at least 14KB total bandwidth (because network bandwidth might
                        // be set to unlimited)
                        peer.bandwidthPrefs.MaxTotalBandwidth.get() > 14 * 1024 &&
                        // the current uptime should be at least 60 minutes or 30 minutes in avg.
                        (((LongObj) uptimeProvider.getValue()).getValue() > ONE_HOUR ||
                                ((LongObj) uptimeProvider.getAverageValue()).getValue() > HALF_HOUR);

        if (logger.isTraceEnabled() && !isCapable) {
            logTraceUltrapeerCapable(uptimeProvider);
        }

        isUltrapeerCapable = isCapable;

        if (isUltrapeerCapable && !peer.isUltrapeer()) {
            long now = System.currentTimeMillis();

            long lastQueryTime = peer.getQueryService().getLastQueryTime();

            BandwidthController upBandCont = peer.getBandwidthService().getUploadBandwidthController();
            long upAvg = upBandCont.getLongTransferAvg().getAverage();

            boolean isPerfect =
                    // last query is 5 minutes ago
                    now - lastQueryTime > 5 * DateUtils.MILLIS_PER_MINUTE &&
                            upAvg < 2 * 1024;

            isPerfectUltrapeer = isPerfect;
            peer.upgradeToUltrapeer();
        } else {
            isPerfectUltrapeer = false;
        }
        logger.debug("UP capable: {}, perfect: {}", isUltrapeerCapable, isPerfectUltrapeer);
    }

    private void logTraceUltrapeerCapable(UptimeStatisticProvider uptimeProvider) {
        if (!peer.connectionPrefs.AllowToBecomeUP.get()) {
            logger.trace("Not allowed to become UP.");
        }
        if (peer.isFirewalled()) {
            logger.trace("Servent is firewalled.");
        }
        if (!isUltrapeerOS) {
            logger.trace("No ultrapeer OS.");
        }
        if (peer.bandwidthPrefs.NetworkSpeedKbps.get() <= 64) {
            logger.trace("Not enough network speed");
        }
        if (peer.bandwidthPrefs.MaxNetworkBandwidth.get() <= 10 * 1024) {
            logger.trace("Not enough max network bandwidth");
        }
        if (peer.bandwidthPrefs.MaxTotalBandwidth.get() <= 14 * 1024) {
            logger.trace("Not enough max total bandwidth");
        }
        if (((LongObj) uptimeProvider.getValue()).getValue() <= ONE_HOUR ||
                ((LongObj) uptimeProvider.getAverageValue()).getValue() <= HALF_HOUR) {
            logger.trace("Not enough current or avg uptime.");
        }
    }

    public boolean isUltrapeerCapable() {
        return isUltrapeerCapable;
    }

    public boolean isPerfectUltrapeer() {
        return isPerfectUltrapeer;
    }
}