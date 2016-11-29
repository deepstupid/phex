/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2012 Phex Development Group
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
 *  $Id: UploadStatus.java 4553 2012-01-20 17:37:24Z gregork $
 */
package phex.upload;

public enum UploadStatus
{
    /**
     * The status of a just incomming upload request before it is 
     * processed further.
     */
    ACCEPTING_REQUEST(false),
    
    /**
     * The status of a request in the handshake phase.
     */
    HANDSHAKE(true),
    
    /**
     * The status of a upload indicating that a upload is queued.
     */
    QUEUED(false),
    
    /**
     * The status of a upload indicating that a thex upload is in progress.
     */
    UPLOADING_THEX(true),
    
    /**
     * The status of a upload indicating that a thex upload is in progress.
     */
    UPLOADING_DATA(true),
    
    /**
     * The status of a upload indicating that a upload is completed.
     */
    COMPLETED(true),
    
    /**
     * The status of a upload indicating that a upload is completed.
     */
    FINISHED(false),
    
    /**
     * The status of a upload indicating that a upload is aborted.
     */
    ABORTED(false);
    
    private boolean isRunningStatus;
    
    UploadStatus(boolean isRunningStatus)
    {
        this.isRunningStatus = isRunningStatus;
    }
    
    public boolean isRunningStatus()
    {
        return isRunningStatus;
    }
}
