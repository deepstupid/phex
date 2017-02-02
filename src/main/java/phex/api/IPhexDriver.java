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

public interface IPhexDriver {
    short SEARCH_STATUS_UNDEFINED = ISearchHandle.STATUS_UNDEFINED;

    short SEARCH_STATUS_RUNNING = ISearchHandle.STATUS_RUNNING;

    short SEARCH_STATUS_FINISHED = ISearchHandle.STATUS_FINISHED;

    short SEARCH_STATUS_NON_EXISTING = ISearchHandle.STATUS_NON_EXISTING;

    short HIT_STATUS_UNDEF = ISearchHitHandle.STATUS_UNDEF;

    short HIT_STATUS_TRANSFER_NOT_RUNNING = ISearchHitHandle.STATUS_TRANSFER_NOT_RUNNING;

    short HIT_STATUS_TRANSFER_RUNNING = ISearchHitHandle.STATUS_TRANSFER_RUNNING;

    short HIT_STATUS_TRANSFER_ERROR = ISearchHitHandle.STATUS_TRANSFER_ERROR;

    short HIT_STATUS_TRANSFER_COMPLETED = ISearchHitHandle.STATUS_TRANSFER_COMPLETED;

    /**
     * Start up the local servent (start Phex in head-less mode).
     *
     * @return true on success, or false otherwise
     */
    boolean start();

    /**
     * Shutdown the local servent.
     *
     * @return true on success, or false otherwise
     */
    boolean stop();

    /**
     * Get the local servent status.
     *
     * @return the servent status
     */
    OnlineStatus getServentStatus();

    /**
     * Start a search.
     *
     * @param searchString the search string
     * @return a unique id identifying the search on success, or -1 otherwise
     */
    long startSearch(String searchString);

    /**
     * Retrieve the search string for an existing search.
     *
     * @param searchId the search id
     * @return the search string if the search id specifies an existing
     * search, or null otherwise
     */
    String getSearchString(long searchId);

    /**
     * Retrieve the search status for an existing search.
     *
     * @param searchId the search id
     * @return the search status if the search id specifies an existing
     * search, or error otherwise
     */
    short getSearchStatus(long searchId);

    /**
     * Stop an existing search.
     *
     * @param searchId the search id
     * @return true if the search exists and could be stopped, or false otherwise
     */
    boolean stopSearch(long searchId);

    /**
     * Resume an existing search.
     *
     * @param searchId the search id
     * @return true if the search exists and could be resumed, or false otherwise
     */
    boolean resumeSearch(long searchId);

    /**
     * Delete an existing search. If any hits exist, these are deleted too.
     *
     * @param searchId the search id
     * @return true if the search exists and could be stopped, or false otherwise
     * @see cancelDownload
     */
    boolean deleteSearch(long searchId);

    /**
     * Retrieve all existing searches.
     *
     * @return an array containing the search ids of all existing searches,
     * or null in case of error
     */
    long[] getAllSearches();

    /**
     * Retrieve all existing hits for a given search
     *
     * @param searchId the search id
     * @return if the search exists, an array containing the hit ids of all currently available
     * hits for the given, or null otherwise
     */
    long[] getSearchHits(long searchId);

    /**
     * Retrieve remote file name of a given hit.
     *
     * @param hitId the hit id
     * @return the file name if the hit exists, or null otherwise
     */
    String getSearchHitFilename(long hitId);

    /**
     * Retrieve the status for an existing search hit.
     *
     * @param hitId the hit id
     * @return the status if the hit id specifies an existing
     * hit, or error otherwise
     */
    short getSearchHitStatus(long hitId);

    /**
     * Start to download a hit (remote file) and save it to the specified path.
     *
     * @param hitId the hit id
     * @return true if the download could be started successfully, or false otherwise
     */
    boolean startDownload(long hitId);

    /**
     * Stop a running download.
     *
     * @param hitId the hit id
     * @return true if the download could be stopped successfully, or false otherwise
     */
    boolean stopDownload(long hitId);

    /**
     * Resume a running download.
     *
     * @param hitId the hit id
     * @return true if the download could be resumed successfully, or false otherwise
     */
    boolean resumeDownload(long hitId);

    /**
     * Cancel a running download, delete the hit from the hit list, delete the
     * partially downloaded file. If the download has been completed, the download
     * file is not deleted.
     *
     * @param hitId the hit id
     * @return true if the download could be canceled and deleted successfully, or false otherwise
     */
    boolean cancelDownload(long hitId);

    /**
     * This method is an alias for cancelDownload: it has the same functionality.
     *
     * @param hitId the hit id
     * @return true if the download could be canceled and deleted successfully, or false otherwise
     * @see cancelDownload
     */
    boolean deleteHit(long hitId);

    /**
     * Start to download a hit (remote file) and save it to the specified path.
     *
     * @param hitId         the hit id
     * @param localFilepath the path where the local file should be saved
     * @return true if the download could be started successfully, or false otherwise
     */
    boolean saveDownloadedFile(long hitId, String localFilepath);

    /**
     * Publish a local file, i.e. make it available for others to retrieve.
     *
     * @param localFilePath path to the local file
     * @return true if the file could be published correctly, or false otherwise
     */
    boolean publishFile(String localFilePath);
}