///*
// *  PHEX - The pure-java Gnutella-servent.
// *  Copyright (C) 2001 - 2005 Phex Development Group
// *
// *  This program is free software; you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation; either version 2 of the License, or
// *  (at your option) any later version.
// *
// *  This program is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with this program; if not, write to the Free Software
// *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
// *
// *  Created on 06.10.2006
// *  --- CVS Information ---
// *  $Id: SystemToolsPrefs.java 3807 2007-05-19 17:06:46Z gregork $
// */
//package phex.prefs.core;
//
//import phex.prefs.api.PreferencesFactory;
//import phex.prefs.api.Setting;
//
//public class SystemToolsPrefs {
//    /**
//     * The command to execute for video media types on systems other then
//     * Windows and Mac OSX
//     */
//    public final Setting<String> OpenVideoCmdOtherOS;
//
//    /**
//     * The command to execute for image media types on systems other then
//     * Windows and Mac OSX (usually Unix systems).
//     */
//    public final Setting<String> OpenImageCmdOtherOS;
//
//    /**
//     * The command to execute for audio media types on systems other then
//     * Windows and Mac OSX (usually Unix systems).
//     */
//    public final Setting<String> OpenAudioCmdOtherOS;
//
//    /**
//     * The browser command to execute for none video, image, audio media types
//     * on systems other then Windows and Mac OSX (usually Unix systems).
//     */
//    public final Setting<String> OpenBrowserCmdOtherOS;
//
//    public ___(File file) { super(file);
//        OpenVideoCmdOtherOS = createStringSetting(
//                "SystemTools.OpenVideoCmdOtherOS", "xterm -e mplayer %filepath%");
//        OpenImageCmdOtherOS = createStringSetting(
//                "SystemTools.OpenImageCmdOtherOS", "ee %filepath%");
//        OpenAudioCmdOtherOS = createStringSetting(
//                "SystemTools.OpenAudioCmdOtherOS", "xterm -e mplayer %filepath%");
//        OpenBrowserCmdOtherOS = createStringSetting(
//                "SystemTools.OpenBrowserCmdOtherOS", "mozilla -remote openURL(%filepath%,new-window)");
//    }
//}
