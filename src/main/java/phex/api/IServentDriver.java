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
 *  $Id:$
 */
package phex.api;

import phex.servent.OnlineStatus;

public interface IServentDriver
{
    /**
     * Start up the local servent (start Phex in head-less mode).
     *
     * @return true on success, or false otherwise
     */
    public boolean startServent();

    /**
     * Shutdown the local servent.
     *
     * @return true on success, or false otherwise
     */
    public boolean stopServent();

    /**
     * Get the local servent status.
     *
     * @return the servent status
     */
    public OnlineStatus getServentStatus();

    /**
     * Start a search.
     *
     * @param searchString the search string
     *
     * @return a search handle object on success, or null otherwise 
     */
    public ISearchHandle startSearch(String searchString);

    /**
     * Retrieve all existing searches.
     *
     * @return an array containing the search handles of all existing searches,
     * or null in case of error
     */
    public ISearchHandle [] getAllSearches();

    /**
     * Publish a local file, i.e. make it available for others to retrieve.
     *
     * @param localFilePath path to the local file
     *
     * @return true if the file could be published correctly, or false otherwise
     */
    public boolean publishFile(String localFilePath);
}