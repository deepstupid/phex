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
 *  $Id: UptimeStatisticProvider.java 4368 2009-01-29 14:05:54Z gregork $
 */
package phex.statistic;

import phex.common.LongObj;
import phex.common.format.TimeFormatUtils;
import phex.StatisticPrefs;
import phex.peer.Peer;

public class UptimeStatisticProvider implements StatisticProvider {
    private final LongObj valueObj;
    private final LongObj avgObj;
    private final LongObj maxObj;
    private final Peer peer;
    private long startTime;


    public UptimeStatisticProvider(Peer peer) {
        this.peer = peer;
        valueObj = new LongObj();
        avgObj = new LongObj();
        maxObj = new LongObj(peer.statPrefs.MaximalUptime.get().longValue());
        startUptimeMeasurement();
    }

    private void startUptimeMeasurement() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Returns the current value this provider presents.
     * The return value can be null in case no value is provided.
     *
     * @return the current value or null.
     */
    public Object getValue() {
        long value = System.currentTimeMillis() - startTime;
        valueObj.setValue(value);
        return valueObj;
    }

    /**
     * Returns the average value this provider presents.
     * The return value can be null in case no value is provided.
     *
     * @return the average value or null.
     */
    public Object getAverageValue() {
        LongObj currentUptimeObj = (LongObj) getValue();
        long currentUptime = currentUptimeObj.longValue();
        // current uptime might be negative...
        currentUptime = Math.max(currentUptime, 0);
        long avgUptime = (currentUptime +
                peer.statPrefs.MovingTotalUptime.get().longValue())
                / (peer.statPrefs.MovingTotalUptimeCount.get().intValue() + 1);
        avgObj.setValue(avgUptime);
        return avgObj;
    }

    /**
     * Returns the max value this provider presents.
     * The return value can be null in case no value is provided.
     *
     * @return the max value or null.
     */
    public Object getMaxValue() {
        long uptime = System.currentTimeMillis() - startTime;
        if (uptime > maxObj.getValue()) {
            maxObj.setValue(uptime);
        }
        return maxObj;
    }

    /**
     * Returns the presentation string that should be displayed for the corresponding
     * value.
     *
     * @param value the value returned from getValue(), getAverageValue() or
     *              getMaxValue()
     * @return the statistic presentation string.
     */
    public String toStatisticString(Object value) {
        return TimeFormatUtils.formatSignificantElapsedTime(
                ((LongObj) value).longValue() / 1000);
    }

    public void saveUptimeStats() {
        LongObj obj = (LongObj) getMaxValue();
        peer.statPrefs.MaximalUptime.set(Long.valueOf(obj.getValue()));

        long mtu = peer.statPrefs.MovingTotalUptime.get().intValue();
        int mtuCount = peer.statPrefs.MovingTotalUptimeCount.get().intValue();
        if (mtuCount >= 25) {
            // substract one average uptime...
            mtu -= (mtu / mtuCount);
            peer.statPrefs.MovingTotalUptime.set(Long.valueOf(mtu));
            mtuCount--;
            peer.statPrefs.MovingTotalUptimeCount.set(Integer.valueOf(mtuCount));
        }

        obj = (LongObj) getValue();
        // sometimes time might be negative since clocks can go backwards
        // due to DST adjustments. In this case ignore the uptime value.
        if (obj.longValue() > 0) {
            mtu += obj.longValue();
            peer.statPrefs.MovingTotalUptime.set(Long.valueOf(mtu));
            mtuCount++;
            peer.statPrefs.MovingTotalUptimeCount.set(Integer.valueOf(mtuCount));
        }
    }
}