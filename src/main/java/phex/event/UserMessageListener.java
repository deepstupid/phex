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
 *  Created on 13.06.2005
 *  --- CVS Information ---
 *  $Id: UserMessageListener.java 3362 2006-03-30 22:27:26Z gregork $
 */
package phex.event;

import java.util.EventListener;

/**
 *
 */
public interface UserMessageListener extends EventListener {
    String SegmentCreateIncompleteFileFailed = "SegmentCreateIncompleteFileFailed";

    String GuiSettingsSaveFailed = "GuiSettingsSaveFailed";
    String DownloadSettingsSaveFailed = "DownloadSettingsSaveFailed";
    String FavoritesSettingsSaveFailed = "FavoritesSettingsSaveFailed";
    String SecuritySettingsSaveFailed = "SecuritySettingsSaveFailed";
    String SharedFilesSaveFailed = "SharedFilesSaveFailed";

    String GuiSettingsLoadFailed = "GuiSettingsLoadFailed";
    String DownloadSettingsLoadFailed = "DownloadSettingsLoadFailed";
    String FavoritesSettingsLoadFailed = "FavoritesSettingsLoadFailed";
    String SecuritySettingsLoadFailed = "SecuritySettingsLoadFailed";
    String SharedFilesLoadFailed = "SharedFilesLoadFailed";

    void displayUserMessage(String userMessageId, String[] args);
}
