/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2011 Phex Development Group
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
 *  $Id: Setting.java 4046 2007-11-19 17:13:59Z gregork $
 */
package phex.prefs.api;

/**
 * Generic change event class holding a source, the old value and the new value.
 * To indication what has changed the event topic of the event service should be
 * used.
 */
public class SettingChangedEvent<T> {
    private final Setting<T> source;
    private final T oldValue;
    private final T newValue;

    public SettingChangedEvent(Setting<T> source, T oldValue, T newValue) {
        super();
        this.source = source;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public Setting<T> getSource() {
        return source;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }
}
