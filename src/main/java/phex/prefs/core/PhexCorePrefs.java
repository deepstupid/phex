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
 *  $Id: PhexCorePrefs.java 4435 2009-04-18 16:47:58Z gregork $
 */
package phex.prefs.core;

import phex.common.Environment;
import phex.common.EnvironmentConstants;
import phex.common.PhexVersion;
import phex.prefs.api.Preferences;

public class PhexCorePrefs {
    protected static final Preferences instance;

    static {
        instance = new Preferences(Environment.getPhexConfigFile(EnvironmentConstants.CORE_PREFERENCES_FILE_NAME));

    }


    //    public static void save(boolean force) {
//        if (force) {
//            instance.saveRequiredNotify();
//        }
//        instance.save();
//    }


}
