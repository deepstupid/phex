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
 *  $Id: DownloadPrefs.java 4499 2010-11-15 20:33:07Z ArneBab $
 */
package phex;

import phex.prefs.Preferences;
import phex.prefs.Setting;
import phex.util.SystemProperties;

import java.io.File;

public class DownloadPrefs extends Preferences {
    /**
     * The destination directory of finished downloads.
     */
    public final Setting<String> DestinationDirectory;

    /**
     * The directory where incomplete files are stored during download.
     */
    public final Setting<String> IncompleteDirectory;

    /**
     * The max number of parallel workers per download file.
     */
    public final Setting<Integer> MaxWorkerPerDownload;

    /**
     * The max number of total parallel workers for all download files.
     */
    public final Setting<Integer> MaxTotalDownloadWorker;

    /**
     * The max number of bytes to buffer per download file. When the buffer is
     * exceeded the complete buffered data is written to disk.
     * <p>
     * NOTE: Changing this value at runtime can cause problems in buffer handling!
     */
    public final Setting<Integer> MaxWriteBufferPerDownload;

    /**
     * The max number of bytes to buffer for all download files. When the buffer
     * is exceeded the complete buffered data is written to disk.
     * <p>
     * NOTE: Changing this value at runtime can cause problems in buffer handling!
     */
    public final Setting<Integer> MaxTotalDownloadWriteBuffer;

    /**
     * The maximum number of downloads that are allowed per IP.
     * Changing this value is not recommended since most, if not all servents
     * only accept one upload per IP.
     */
    public final Setting<Integer> MaxDownloadsPerIP;

    /**
     * The segment size initially requested on a download connection. It's used
     * for the first transfer attempt when no transfer speed is yet known.
     * Afterwards the segment size is adjusted for each individual candidate to
     * approximatly meet the 'SegmentTransferTargetTime'.
     */
    public final Setting<Integer> SegmentInitialSize;

    /**
     * Used to adjust the segment size for an individual candidate to
     * approximatly meet the SegmentTransferTargetTime depending on the
     * candidates transfer speed.
     * This is done after the first segment is transfered, sized according to
     * the SegmentInitialSize configuration.
     */
    public final Setting<Integer> SegmentTransferTargetTime;

    /**
     * Do not allow segments to be larger than this value, regardless of the
     * previous segment's download rate.
     */
    public final Setting<Integer> SegmentMaximumSize;

    public final Setting<Integer> SegmentMultiple;

    /**
     * If a segment is transferred at less than this speed (b/sec), refuse further downloads
     * from this candidate
     */
    public final Setting<Integer> CandidateMinAllowedTransferRate;

    /**
     * The LogBuffer size used for download candidates logging.
     */
    public final Setting<Integer> CandidateLogBufferSize;

    /**
     * The timeout of a push request in millis.
     */
    public final Setting<Integer> PushRequestTimeout;

    /**
     * Indicates if completed download should be automatically removed.
     */
    public final Setting<Boolean> AutoRemoveCompleted;

    /**
     * Indicates if downloaded magma files should be parsed and its referenced
     * sources further downloaded.
     */
    public final Setting<Boolean> AutoReadoutMagmaFiles;

    /**
     * Indicates if downloaded metalink files should be parsed and its
     * reference sources further downloaded.
     */
    public final Setting<Boolean> AutoReadoutMetalinkFiles;

    /**
     * Indicates if downloaded rss files should be parsed and its referenced
     * sources further downloaded.
     */
    public final Setting<Boolean> AutoReadoutRSSFiles;

    public DownloadPrefs(File file) { super(file);
        File defaultIncDir = new File(SystemProperties.getPhexDownloadsRoot(), "incomplete");
        File defaultDownDir = new File(SystemProperties.getPhexDownloadsRoot(), "download");

        DestinationDirectory = createStringSetting(
                "Download.DestinationDirectory", defaultDownDir.getAbsolutePath());
        IncompleteDirectory = createStringSetting(
                "Download.IncompleteDirectory", defaultIncDir.getAbsolutePath());
        MaxWorkerPerDownload = createIntRangeSetting(
                "Download.MaxWorkerPerDownload", 12, 1, 99);
        MaxTotalDownloadWorker = createIntRangeSetting(
                "Download.MaxTotalDownloadWorker", 30, 1, 99);
        MaxWriteBufferPerDownload = createIntRangeSetting(
                "Download.MaxWriteBufferPerDownload", 256 * 1024, 0, Integer.MAX_VALUE);
        MaxTotalDownloadWriteBuffer = createIntRangeSetting(
                "Download.MaxTotalDownloadWriteBuffer", 1024 * 1024, 0, Integer.MAX_VALUE);
        MaxDownloadsPerIP = createIntRangeSetting(
                "Download.MaxDownloadsPerIP", 1, 1, 99);
        SegmentInitialSize = createIntRangeSetting(
                "Download.SegmentInitialSize", 16 * 1024, 1024, 10 * 1024 * 1024);
        SegmentTransferTargetTime = createIntRangeSetting(
                "Download.SegmentTransferTargetTime", 90, 15, 999);
        SegmentMaximumSize = createIntSetting(
                "Download.SegmentMaximumSize", 10 * 1024 * 1024);
        SegmentMultiple = createIntSetting(
                "Download.SegmentMultiple", 4096);
        CandidateMinAllowedTransferRate = createIntRangeSetting(
                "Download.CandidateMinAllowedTransferRate", 1, 1, 100 * 1024);
        CandidateLogBufferSize = createIntSetting(
                "Download.CandidateLogBufferSize", 0);
        PushRequestTimeout = createIntSetting(
                "Download.PushRequestTimeout", 30 * 1000);
        AutoRemoveCompleted = createBoolSetting(
                "Download.AutoRemoveCompleted", false);
        AutoReadoutMagmaFiles = createBoolSetting(
                "Download.AutoReadoutMagmaFiles", true);
        AutoReadoutMetalinkFiles = createBoolSetting(
                "Download.AutoReadoutMetalinkFiles", true);
        AutoReadoutRSSFiles = createBoolSetting(
                "Download.AutoReadoutRSSFiles", true);
    }
}
