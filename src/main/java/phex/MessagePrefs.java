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
 *  Created on 17.08.2006
 *  --- CVS Information ---
 *  $Id: MessagePrefs.java 4163 2008-04-07 20:54:35Z mariosk $
 */
package phex;

import phex.prefs.Preferences;
import phex.prefs.Setting;

import java.io.File;

public class MessagePrefs extends Preferences {
    /**
     * The default value of the X-Max-TTL header for dynamic queries.
     */
    public final int DEFAULT_DYNAMIC_QUERY_MAX_TTL = 4;

    /**
     * The number of consecutive failed connection after which the servent
     * is called as offline.
     */
    public final Setting<Integer> MaxLength;

    /**
     * The TTL Phex uses for messages.
     */
    public final Setting<Integer> TTL;

    /**
     * The maximum number of hops allowed to be seen in messages otherwise a
     * message is dropped. Also the highest ttl allowed to be seen in messages
     * otherwise the ttl is limited to Cfg.maxNetworkTTL - hops.
     */
    public final Setting<Integer> MaxNetworkTTL;

    /**
     * Indicates if outdated Clip2 index queries should be dropped.
     *
     * @since 3.0.2.103
     */
    public final Setting<Boolean> DropIndexQueries;

    /**
     *
     */
    public final Setting<Boolean> UseExtendedOriginIpAddress;

    public MessagePrefs(File file) {
        super(file);
        MaxLength = createIntSetting(
                "Message.MaxLength", 65536);
        TTL = createIntSetting(
                "Message.TTL", 7);
        MaxNetworkTTL = createIntSetting(
                "Message.MaxNetworkTTL", 7);
        DropIndexQueries = createBoolSetting(
                "Message.DropIndexQueries", true);
        UseExtendedOriginIpAddress = createBoolSetting(
                "Message.UseExtendedOriginIpAddress", false);
    }
}
