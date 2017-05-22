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
 *  $Id: ConnectionPrefs.java 4367 2009-01-18 18:52:03Z gregork $
 */
package phex;

import phex.prefs.Preferences;
import phex.prefs.RangeSetting;
import phex.prefs.Setting;

import java.io.File;

public class ConnectionPrefs extends Preferences {
    public final Setting<Boolean> AutoConnectOnStartup;

    /**
     * Indicates if this node is allowed to become a Ultrapeer.
     */
    public final Setting<Boolean> AllowToBecomeUP;

    /**
     * Indicates if this node force to be a Ultrapeer.
     * This value must always be checked together with allowToBecomeUP. If
     * allowToBecomeUP is false, a forceToBeUltrapeer value of true must be
     * ignored.
     */
    public final Setting<Boolean> ForceToBeUltrapeer;

    /**
     * The number of ultrapeer to ultrapeer connections the nodes is allowed to
     * have open.
     * This value is used for the X-Degree header. To reach high out degree
     * for dynamic query the value must be above 15.
     */
    public final Setting<Integer> Up2UpConnections;

    /**
     * The number of ultrapeer to leaf connections the nodes is allowed to
     * have open.
     */
    public final Setting<Integer> Up2LeafConnections;

    /**
     * The number of leaf to ultrapeer connections the nodes is allowed to
     * have open. The max should be 5.
     */
    public final RangeSetting<Integer> Leaf2UpConnections;

    /**
     * Indicates if the peers has connected incoming the last time it was
     * shutdown. The value is only updated in case of a server shutdown, but
     * the Server maintains and holds the state changes during runtime.
     */
    public final Setting<Boolean> HasConnectedIncomming;

    /**
     * The number of consecutive failed connection after which the servent
     * is called as offline.
     */
    public final Setting<Integer> OfflineConnectionFailureCount;

    /**
     * Enables QueryHit Snooping.
     */
    public final Setting<Boolean> EnableQueryHitSnooping;

    /**
     * Indicates if we accept deflated connections.
     */
    public final Setting<Boolean> AcceptDeflateConnection;

    public ConnectionPrefs(File file) { super(file);
        AutoConnectOnStartup = createBoolSetting(
                "Connection.AutoConnectOnStartup", true);
        AllowToBecomeUP = createBoolSetting(
                "Connection.AllowToBecomeUP", true);
        ForceToBeUltrapeer = createBoolSetting(
                "Connection.ForceToBeUltrapeer",
                PrivateNetworkConstants.DEFAULT_FORCE_TOBE_ULTRAPEER);
        Up2UpConnections = createIntRangeSetting(
                "Connection.Up2UpConnections", 32, 16, 999);
        Up2LeafConnections = createIntRangeSetting(
                "Connection.Up2LeafConnections", 30, 0, 999);
        Leaf2UpConnections = createIntRangeSetting(
                "Connection.Leaf2UpConnections", 5, 1, 5);
        HasConnectedIncomming = createBoolSetting(
                "Connection.HasConnectedIncomming", false);
        OfflineConnectionFailureCount = createIntSetting(
                "Connection.OfflineConnectionFailureCount", 61);
        EnableQueryHitSnooping = createBoolSetting(
                "Connection.EnableQueryHitSnooping", true);
        AcceptDeflateConnection = createBoolSetting(
                "Connection.AcceptDeflateConnection", true);
    }
}
