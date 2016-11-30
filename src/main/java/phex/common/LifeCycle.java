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

/**
 * A life cycle interface for classes that like to manage a defined life cycle
 * in the application.
 */
public interface LifeCycle
{
    /**
     * Starts the component.
     * 
     * @throws Exception if the component fails to start
     */
    void start() throws Exception;

    /**
     * Stops the component.
     * 
     * @exception Exception If the component fails to stop
     */
    void stop() throws Exception;

    /**
     * @return true if the component is starting or has been started.
     */
    boolean isRunning();

    /**
     * @return true if the component has been started.
     */
    boolean isStarted();

    /**
     * @return true if the component is starting.
     */
    boolean isStarting();

    /**
     * @return true if the component is stopping.
     */
    boolean isStopping();

    /**
     * @return true if the component has been stopped.
     */
    boolean isStopped();

    /**
     * @return true if the component has failed to start or has failed to stop.
     */
    boolean isFailed();
}