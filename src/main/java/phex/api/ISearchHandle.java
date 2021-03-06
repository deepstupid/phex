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

public interface ISearchHandle {
    short STATUS_UNDEFINED = -1;

    short STATUS_RUNNING = 0;

    short STATUS_FINISHED = 1;

    short STATUS_NON_EXISTING = 2;

    /**
     * Retrieve the search string for an existing search.
     *
     * @return the search string if the search handle specifies an existing
     * search, or null otherwise
     */
    String getSearchString();

    /**
     * Retrieve the search status for an existing search.
     *
     * @return the search status if the search handle specifies an existing
     * search, or error otherwise
     */
    short getSearchStatus();

    /**
     * Stop an existing search.
     *
     * @return true if the search exists and could be stopped, or false otherwise
     */
    boolean stopSearch();

    /**
     * Resume an existing search.
     *
     * @return true if the search exists and could be resumed, or false otherwise
     */
    boolean resumeSearch();

    /**
     * Delete an existing search. If any hits exist, these are deleted too.
     *
     * @return true if the search exists and could be stopped, or false otherwise
     * @see cancelDownload
     * After executing this method successfully, this search will no longer
     * be returned by
     */
    boolean deleteSearch();

    /**
     * Retrieve all existing hits for a given search
     *
     * @return if the search exists, an array containing the hit handles of all currently available
     * hits for the given, or null otherwise
     */
    ISearchHitHandle[] getSearchHits();
}