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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KeyAllocator<T> {
    /**
     * Given a hash map, allocate a certain number of positive long keys starting at
     * a start value.
     *
     * @param map          the map in which we want to allocate the keys
     * @param numberOfKeys number of keys to allocate
     * @param startValue   the start value
     * @return a list containing the allocated keys on success, null on failure
     */
    public List<Long> allocateKeys(HashMap<Long, T> map, int numberOfKeys, long startValue) {
        if (map == null) {
            return null;
        }

        List<Long> keys = new ArrayList<Long>(numberOfKeys);
        int count = 0;

        long currentValue = startValue;
        while (count < numberOfKeys) {
            Long key = currentValue;
            if (!map.containsKey(key)) {
                count++;

                keys.add(key);
            }

            currentValue++;
            if (currentValue >= Long.MAX_VALUE) {
                currentValue = 0;
            } else if (currentValue == startValue) {
                break;
            }
        }

        if (count == numberOfKeys) {
            return keys;
        } else {
            return null;
        }
    }
}