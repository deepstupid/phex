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
 *  $Id: TimeFormatUtils.java 4526 2011-06-28 17:25:36Z gregork $
 */
package phex.common.format;

import phex.util.Localizer;

public final class TimeFormatUtils
{
    // Don't allow instances
    private TimeFormatUtils() {}

    /**
     * Print only the most significant portion of the time. This is
     * the two most significant units of time. Form will be something
     * like "3h 26m" indicating 3 hours 26 minutes and some insignificant
     * number of seconds.
     */
    public static String formatSignificantElapsedTime( long seconds )
    {
        final long days = seconds / 86400;
        if ( days > 0 ) // Display days and hours
        {
            int hours = (int)((seconds / 3600) % 24); // hours
            return String.valueOf(days) + Localizer.getString("TimeFormatDays") + ' '
                 + String.valueOf(hours) + Localizer.getString("TimeFormatHours");
        }
    
        final int hours = (int)((seconds / 3600) % 24);
        if( hours > 0 ) // Display hours and minutes
        {
            int minutes = (int)((seconds / 60) % 60); // minutes
            return String.valueOf(hours) + Localizer.getString("TimeFormatHours") + ' '
                 + String.valueOf(minutes) + Localizer.getString("TimeFormatMinutes");
        }
    
        final int minutes =  (int)((seconds / 60) % 60);
        if( minutes > 0 ) // Display minutes and seconds
        {
            return String.valueOf(minutes) + Localizer.getString("TimeFormatMinutes") + ' '
                 + String.valueOf(seconds%60) + Localizer.getString("TimeFormatSeconds");
        }
    
        final int secs = (int)(seconds % 60);
        return String.valueOf(secs) + Localizer.getString("TimeFormatSeconds");
    }

    /**
     * Converts the given seconds to a time format with following format:
     * days:hours:minutes:seconds. When days &lt; 0 the format will be 
     * hours:minutes:seconds. When hours &lt; 0 the format will be minutes:seconds.
     * Values &lt; 10 will be padded with a 0. 
     */
    public static String convertSecondsToTime( int seconds )
    {
        StringBuffer buffer = new StringBuffer();
        int days = seconds / 86400;
        int hours = (seconds / 3600) % 24;
        int minutes = (seconds / 60) % 60;
        int secs = seconds % 60;
        
        if ( days > 0 ) // Display days and hours
        {
            buffer.append( Integer.toString( days ) );
            buffer.append(':');
            if ( hours < 10 )
            {
                buffer.append('0');
            }
        }
        if ( days > 0 || hours > 0 )
        {
            buffer.append( Integer.toString( hours ) );
            buffer.append(':');
            if ( minutes < 10 )
            {
                buffer.append('0');
            }
        }
        
        buffer.append( Integer.toString( minutes ) );
        buffer.append(':');
        if ( secs < 10 )
        {
            buffer.append('0');
        }
        buffer.append( Integer.toString( secs ) );
        return buffer.toString();
    }
}