/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2005 Phex Development Group
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
 *  Created on 18.09.2006
 *  --- CVS Information ---
 *  $Id: StatisticPrefs.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex;

import phex.prefs.Preferences;
import phex.prefs.Setting;

import java.io.File;

public class StatisticPrefs extends Preferences  {
    /**
     * The total number of completed downloads tracked.
     */
    public final Setting<Integer> TotalDownloadCount;

    /**
     * The total number of uploads.
     */
    public final Setting<Integer> TotalUploadCount;

    /**
     * The last fractional uptime calculated. Needed for avg. daily uptime
     * calculation.
     */
    public final Setting<Float> FractionalUptime;

//    /**
//     * Counts the total number of Phex startups.
//     */
//    public final Setting<Integer> TotalStartupCounter;

    /**
     * The total uptime of the last movingTotalUptimeCount starts.
     */
    public final Setting<Long> MovingTotalUptime;

    /**
     * The number of times the uptime was added to movingTotalUptime.
     */
    public final Setting<Integer> MovingTotalUptimeCount;

    /**
     * The maximal uptime ever seen.
     */
    public final Setting<Long> MaximalUptime;

    /**
     * Last time Phex was shutdown. Needed for avg. daily uptime calculation.
     */
    public final Setting<Long> LastShutdownTime;

    /**
     * The file into which searches should be monitored.
     */
    public final Setting<String> QueryHistoryLogFile;

    /**
     * The number of searches to track in monitor.
     */
    public final Setting<Integer> QueryHistoryEntries;

    public StatisticPrefs(File file) { super(file);
        Preferences instance = null;

        TotalDownloadCount = createIntSetting(
                "Statistic.TotalDownloadCount", 0);
        TotalUploadCount = createIntSetting(
                "Statistic.TotalUploadCount", 0);
        FractionalUptime = createFloatSetting(
                "Statistic.FractionalUptime", 0);
//        TotalStartupCounter = createIntSetting(
//                "Statistic.TotalStartupCounter", 0);
        MovingTotalUptime = createLongSetting(
                "Statistic.MovingTotalUptime", 0);
        MovingTotalUptimeCount = createIntSetting(
                "Statistic.MovingTotalUptimeCount", 0);
        MaximalUptime = createLongSetting(
                "Statistic.MaximalUptime", 0);
        LastShutdownTime = createLongSetting(
                "Statistic.LastShutdownTime", 0);
        QueryHistoryLogFile = createStringSetting(
                "Statistic.SearchMonitorFile", "");
        QueryHistoryEntries = createIntSetting(
                "Statistic.SearchHistoryLength", 10);
    }
}
