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
 */
package phex.download.swarming;

public interface SWDownloadConstants {
    /**
     * Indicator for a unknown file size.
     */
    int UNKNOWN_FILE_SIZE = -1;

    /**
     * The number of failed connection tries after which a candidate is marked
     * as bad.
     */
    int BAD_CANDIDATE_CONNECTION_TRIES = 3;

    /**
     * This value is given as a rating to a segment which is not restricted.
     * ie: these segments are the last ones you want to download, other things being equal
     */
    int WORST_RATING = 1000000;

    /**
     * The number of failed connection tries after which a candidate is marked
     * as ignored.
     */
    int IGNORE_CANDIDATE_CONNECTION_TRIES = 12;

    /**
     * The in bytes that is used for determine a merge or split of a segment.
     * A segment will tried to be merged when less then RESIZE_BOUNDRY_SIZE
     * is available in the segment.
     * A segment will be split if the segment to be split has at least 4 times
     * RESIZE_BOUNDRY_SIZE left ( 2 times for each resulting segment )
     */
    //public static final int RESIZE_BOUNDARY_SIZE = 32 * 1024;

    /**
     * The time to sleep when the host is busy in millis.
     */
    long HOST_BUSY_SLEEP_TIME = 60 * 1000;

    /**
     * The time step to sleep when a host range is unavailable in millis.
     */
    long RANGE_UNAVAILABLE_STEP_TIME = 60 * 1000;

    /**
     * The time step to sleep when a host connection failed in millis.
     */
    long CONNECTION_FAILED_STEP_TIME = 2 * 60 * 1000;

    /**
     * The timeout after which collected available range set information times out.
     */
    long AVAILABLE_RANGE_SET_TIMEOUT = 10 * 60 * 1000;

    /**
     * The timeout after which the bad candidates status times out.
     */
    long BAD_CANDIDATE_STATUS_TIMEOUT = 3 * 60 * 60 * 1000;

    /**
     * The time after which the rated scope list information times out and
     * should be recalculated.
     */
    long RATED_SCOPE_LIST_TIMEOUT = 2 * 60 * 1000;
    
    /*----------------------------------------------------------------
     * Status Constants - Be aware to only add new status to the end of the integer
     *     list. The status is stored as value in the XML output. DON'T assign
     *     a different status to a already used value!!
     */

    /**
     * Used to clear a status. Like a error status.
     */
    short STATUS_CLEARED = 0;

    /////////////////// SWDownloadFile Constants
    /**
     * The status of a download file indicating that it is queued and waiting
     * to be downloaded.
     */
    short STATUS_FILE_WAITING = 1;

    /**
     * The status of a download file indicating that a download is running.
     */
    short STATUS_FILE_DOWNLOADING = 2;

    /**
     * The status of a download file indicating that a download is completed.
     */
    short STATUS_FILE_COMPLETED = 3;

    /**
     * The status of a download file indicating that a download is stopped.
     */
    short STATUS_FILE_STOPPED = 4;

    /**
     * The status of a download file indicating that a download is queued.
     */
    short STATUS_FILE_QUEUED = 5;

    /**
     * The status of a download file indicating that a download is completed
     * and moved to destination.
     */
    short STATUS_FILE_COMPLETED_MOVED = 6;

    /*----------------------------------------------------------------
     *Status Key Constants used to store localized status values
     */

    /**
     * The status key for the localized status string indicating that a download
     * file is queued and waiting to be downloaded.
     */
    String STATUS_FILE_WAITING_KEY =
            "WaitingForDownload";

    /**
     * The status key for the localized status string indicating that a download
     * file is downloading.
     */
    String STATUS_FILE_DOWNLOADING_KEY =
            "Downloading";

    /**
     * The status key for the localized status string indicating that a download
     * file is downloading.
     */
    String STATUS_FILE_COMPLETED_KEY =
            "Completed";

    /**
     * The status key for the localized status string indicating that a download
     * file is stopped.
     */
    String STATUS_FILE_STOPPED_KEY =
            "Stopped";

    /**
     * The status key for the localized status string indicating that a download
     * file is queued.
     */
    String STATUS_FILE_QUEUED_KEY =
            "FileQueued";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is ignored.
     */
    String STATUS_CANDIDATE_IGNORED_KEY =
            "CandidateIgnored";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is bad.
     */
    String STATUS_CANDIDATE_BAD_KEY =
            "CandidateOffline";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is queued and waiting to be downloaded.
     */
    String STATUS_CANDIDATE_WAITING_KEY =
            "WaitingForDownload";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is busy.
     */
    String STATUS_CANDIDATE_BUSY_KEY =
            "HostBusy";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is connecting.
     */
    String STATUS_CANDIDATE_CONNECTING_KEY =
            "Connecting";

    String STATUS_CANDIDATE_ALLOCATING_SEGMENT_KEY =
            "AllocatingSegment";

    /**
     * The status key for the localized status string indicating that a connection
     * to a download candidate failed.
     */
    String STATUS_CANDIDATE_CONNECTION_FAILED_KEY =
            "ConnectionFailed";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is busy.
     */
    String STATUS_CANDIDATE_DOWNLOADING_KEY =
            "Downloading";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is requesting a segment.
     */
    String STATUS_CANDIDATE_REQUESTING_KEY =
            "Requesting";

    /**
     * The status key for the localized status string indicating that a download
     * candidate is busy.
     */
    String STATUS_CANDIDATE_PUSH_REQUEST_KEY =
            "PushRequest";

    /**
     * The status key for the localized status string indicating that a download
     * range is unavailable.
     */
    String STATUS_CANDIDATE_RANGE_UNAVAILABLE_KEY =
            "RangeUnavailable";

    String STATUS_CANDIDATE_REMOTLY_QUEUED_KEY =
            "RemotlyQueued";

    /**
     * The status key for the localized status string indicating that a download
     * file has an unrecognized status.
     */
    String STATUS_UNRECOGNIZED_KEY = "UnrecognizedStatus";
}
