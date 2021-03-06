/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
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
 *  --- CVS Information ---
 *  $Id: CatchedHostCache.java 4154 2008-03-21 14:26:32Z gregork $
 */
package phex.host;

import phex.common.address.DestAddress;
import phex.peer.Peer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * The CatchedHostCache provides a container with a limited size.
 * The container stores CaughtHosts ordered by successful connection
 * probability. When the container is full, the element with the
 * lowest priority is dropped.
 */
public class CaughtHosts {
    private final TreeSet<CaughtHost> sortedHosts;
    private final Map<DestAddress, CaughtHost> addressHostMapping;
    private final Peer peer;

    public CaughtHosts(Peer peer) {
        this.peer = peer;
        sortedHosts = new TreeSet<>(new CaughtHostComparator());
        addressHostMapping = new HashMap<>();
    }

    /**
     * Returns the cached CaughtHost associated by this HostAddress.
     *
     * @param address the HostAddress to look up the CaughtHost for.
     * @return the CaughtHost of the given HostAddress or null if not available.
     */
    public synchronized CaughtHost getCaughHost(DestAddress address) {
        return addressHostMapping.get(address);
    }

    /**
     * Adds the given CaughtHost to the host cache if no already present. If the
     * cache is full the element with the lowest successful connection
     * probability is dropped.
     *
     * @param host the CaughtHost to add.
     */
    public synchronized void add(CaughtHost host) {
        if (addressHostMapping.putIfAbsent(host.getHostAddress(), host) != null) {
            return;
        }
        sortedHosts.add(host);

        if (sortedHosts.size() >= peer.netPrefs.MaxHostInHostCache.get()) {
            addressHostMapping.put(host.getHostAddress(), host);
            sortedHosts.add(host);
            if (sortedHosts.size() >= peer.netPrefs.MaxHostInHostCache.get()) {
                CaughtHost dropObject = sortedHosts.first();
                remove(dropObject);
            }
        }
        assert addressHostMapping.size() == sortedHosts.size() :
                "CatchedHostCache out of sync. s: " + sortedHosts.toString() + " - m: " + addressHostMapping.toString();
    }

    /**
     * Removes the CaughtHost from the host cache.
     *
     * @param host the CaughtHost to remove.
     */
    public synchronized void remove(CaughtHost host) {
        CaughtHost value = addressHostMapping.remove(
                host.getHostAddress());
        if (value != null) {
            sortedHosts.remove(value);
        }
        assert addressHostMapping.size() == sortedHosts.size() :
                "CatchedHostCache out of sync. s: " + sortedHosts.toString() + " - m: " + addressHostMapping.toString();
    }

    /**
     * Clears the complete host cache.
     */
    public synchronized void clear() {
        sortedHosts.clear();
        addressHostMapping.clear();
    }

    /**
     * Returns a iterator of all CaughtHost reverse ordered by the successful
     * connection probability.
     *
     * @return a reverse ordered iterator of all CaughtHost.
     */
    public synchronized Iterator<CaughtHost> iterator() {
        return sortedHosts.iterator();
    }
}