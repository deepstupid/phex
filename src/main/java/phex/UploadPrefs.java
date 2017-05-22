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
 *  $Id: UploadPrefs.java 3807 2007-05-19 17:06:46Z gregork $
 */
package phex;

import phex.prefs.Preferences;
import phex.prefs.Setting;

import java.io.File;

public class UploadPrefs extends Preferences {
    public final Setting<Integer> MaxParallelUploads;
    public final Setting<Integer> MaxUploadsPerIP;
    public final Setting<Boolean> AutoRemoveCompleted;

    /**
     * Indicates whether partial downloaded files are offered to others for download.
     */
    public final Setting<Boolean> SharePartialFiles;

    /**
     * Indicates whether upload queuing is allowed or not.
     */
    public final Setting<Boolean> AllowQueuing;

    /**
     * The maximal number of upload queue slots available.
     */
    public final Setting<Integer> MaxQueueSize;

    /**
     * The minimum poll time for queued uploads.
     */
    public final Setting<Integer> MinQueuePollTime;

    /**
     * The maximum poll time for queued uploads.
     */
    public final Setting<Integer> MaxQueuePollTime;

    /**
     * The LogBuffer size used for upload state.
     */
    public final Setting<Integer> UploadStateLogBufferSize;

    public UploadPrefs(File file) {
        super(file);
        MaxParallelUploads = createIntRangeSetting(
                "Upload.MaxParallelUploads", 4, 1, 99);

        MaxUploadsPerIP = createIntRangeSetting(
                "Upload.MaxUploadsPerIP", 1, 1, 99);

        AutoRemoveCompleted = createBoolSetting(
                "Upload.AutoRemoveCompleted", false);

        SharePartialFiles = createBoolSetting(
                "Upload.SharePartialFiles", true);

        AllowQueuing = createBoolSetting(
                "Upload.AllowQueuing", true);

        MaxQueueSize = createIntRangeSetting(
                "Upload.MaxQueueSize", 10, 1, 99);

        MinQueuePollTime = createIntRangeSetting(
                "Upload.MinQueuePollTime", 45, 30, 120);

        MaxQueuePollTime = createIntRangeSetting(
                "Upload.MaxQueuePollTime", 120, 90, 180);

        UploadStateLogBufferSize = createIntSetting(
                "Upload.UploadStateLogBufferSize", 0);
    }

}
