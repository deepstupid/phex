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

import phex.download.swarming.SWDownloadFile;

public interface ISearchHitHandle
{
    public static final short STATUS_UNDEF = -1;

    public static final short STATUS_TRANSFER_NOT_RUNNING = SWDownloadFile.TRANSFER_NOT_RUNNING;

    public static final short STATUS_TRANSFER_RUNNING = SWDownloadFile.TRANSFER_RUNNING;

    public static final short STATUS_TRANSFER_ERROR = SWDownloadFile.TRANSFER_ERROR;

    public static final short STATUS_TRANSFER_COMPLETED = SWDownloadFile.TRANSFER_COMPLETED;

    /**
     * Retrieve remote file name of a given hit.
     *
     * @return the file name if the hit exists, or null otherwise
     */
    public String getRemoteFilename();

    /**
     * Retrieve the status for an existing search hit.
     *
     * @return the status if the hit handle specifies an existing
     * hit, or error otherwise
     */
    public short getStatus();

    /**
     * Start to download a hit (remote file) and save it to the specified path.
     *
     * @return true if the download could be started successfully, or false otherwise
     */
    public boolean startDownload();

    /**
     * Start to download a hit (remote file) and save it to the specified path.
     *
     * @param localFilepath the path where the local file should be saved
     *
     * @return true if the download could be started successfully, or false otherwise
     */
    public boolean saveDownloadedFile(String localFilepath);

    /**
     * Stop a running download.
     *
     * @return true if the download could be stopped successfully, or false otherwise
     */
    public boolean stopDownload();

    /**
     * Resume a running download.
     *
     * @return true if the download could be resumed successfully, or false otherwise
     */
    public boolean resumeDownload();

    /**
     * Cancel a running download, delete the hit from the hit list (will not be
     * returned by ISearchHit.getSearchHits() any more), delete the
     * partially downloaded file. If the download has been completed, the download
     * file is not deleted.
     *
     * @return true if the download could be canceled and deleted successfully, or false otherwise
     */
    public boolean cancelDownload();

    /**
     * This method is an alias for cancelDownload: it has the same functionality.
     * @see cancelDownload
     *
     * @return true if the download could be canceled and deleted successfully, or false otherwise
     */
    public boolean deleteHit();
}