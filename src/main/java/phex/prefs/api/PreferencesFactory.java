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
 *  $Id: PreferencesFactory.java 4524 2011-06-27 10:04:38Z gregork $
 */
package phex.prefs.api;

import phex.util.StringUtils;

import java.util.List;
import java.util.Random;
import java.util.Set;


public final class PreferencesFactory {
    public static Setting<String> createStringSetting(String name,
                                                      String defaultValue, Preferences preferences) {
        String value = preferences!=null ? preferences.getLoadedProperty(name) : null; //preferences.getLoadedProperty(name);
        if (value == null) {
            value = defaultValue;
        }
        Setting<String> setting = new Setting<String>(name, value, defaultValue, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static Setting<Boolean> createBoolSetting(String name,
                                                     boolean defaultValue, Preferences preferences) {
        Boolean defaultBool = Boolean.valueOf(defaultValue);

        String value = preferences!=null ? preferences.getLoadedProperty(name) : null; preferences.getLoadedProperty(name);
        Boolean boolValue;
        if (value == null) {
            boolValue = defaultBool;
        } else if (value.equals("true")) {
            boolValue = Boolean.TRUE;
        } else if (value.equals("false")) {
            boolValue = Boolean.FALSE;
        } else {
            boolValue = defaultBool;
        }
        Setting<Boolean> setting = new Setting<Boolean>(name, boolValue, defaultBool, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static Setting<Float> createFloatSetting(String name,
                                                    float defaultValue, Preferences preferences) {
        Float defaultFloat = Float.valueOf(defaultValue);

        String value = preferences!=null ? preferences.getLoadedProperty(name) : null;
        Float floatValue;
        // compared to Integer number parsing, Float is not handling null as
        // NumberFormatException.
        if (StringUtils.isEmpty(value)) {
            floatValue = defaultFloat;
        } else {
            try {
                floatValue = Float.valueOf(value);
            } catch (NumberFormatException exp) {
                floatValue = defaultFloat;
            }
        }
        Setting<Float> setting = new Setting<Float>(name, floatValue, defaultFloat, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static Setting<Long> createLongSetting(String name,
                                                  long defaultValue, Preferences preferences) {
        Long defaultLong = Long.valueOf(defaultValue);

        String value = preferences!=null ? preferences.getLoadedProperty(name) : null;
        Long longValue;
        try {
            longValue = Long.valueOf(value);
        } catch (NumberFormatException exp) {
            longValue = defaultLong;
        }
        Setting<Long> setting = new Setting<Long>(name, longValue, defaultLong, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static Setting<Integer> createIntSetting(String name,
                                                    int defaultValue, Preferences preferences) {
        Integer defaultInt = Integer.valueOf(defaultValue);

        String value = preferences!=null ? preferences.getLoadedProperty(name) : null; //preferences.getLoadedProperty(name);
        Integer intValue;
        try {
            intValue = Integer.valueOf(value);
        } catch (NumberFormatException exp) {
            intValue = defaultInt;
        }
        Setting<Integer> setting = new Setting<Integer>(name, intValue, defaultInt, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static RangeSetting<Integer> createIntRangeSetting(String name,
                                                              int defaultValue, int minValue, int maxValue, Preferences preferences) {
        Integer defaultInt = Integer.valueOf(defaultValue);

        String value = preferences!=null ? preferences.getLoadedProperty(name) : null;
        Integer intValue;
        try {
            intValue = Integer.valueOf(value);
        } catch (NumberFormatException exp) {
            intValue = defaultInt;
        }
        RangeSetting<Integer> setting = new RangeSetting<Integer>(name, intValue, defaultInt,
                Integer.valueOf(minValue), Integer.valueOf(maxValue), preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static Setting<Short> createShortRangeSetting(String name,
                                                         short defaultValue, short minValue, short maxValue, Preferences preferences) {
        Short defaultShort = Short.valueOf(defaultValue);

        String value = preferences!=null ? preferences.getLoadedProperty(name) : null;
        Short shortValue;
        try {
            shortValue = Short.valueOf(value);
        } catch (NumberFormatException exp) {
            shortValue = defaultShort;
        }
        RangeSetting<Short> setting = new RangeSetting<Short>(name, shortValue, defaultShort,
                Short.valueOf(minValue), Short.valueOf(maxValue), preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static Setting<Set<String>> createSetSetting(String name,
                                                        Preferences preferences) {
        Set<String> values = PreferencesCodec.deserializeSet(name, preferences);
        Setting<Set<String>> setting = new Setting<Set<String>>(name, values,
                null, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    public static Setting<List<String>> createListSetting(String name,
                                                          Preferences preferences) {
        List<String> values = PreferencesCodec.deserializeList(name, preferences);
        Setting<List<String>> setting = new Setting<List<String>>(name, values,
                null, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }

    ///// special purpose factory methods
    public static Setting<Integer> createListeningPortSetting(String name,
                                                              Preferences preferences) {
        String value = preferences!=null ? preferences.getLoadedProperty(name) : null;// = null;
        int port;
        try {
            port = Integer.parseInt(value);
        } catch (NumberFormatException exp) {
            port = -1;
        }

        // no valid listening port is set yet. Choose a random port from 4000-49150
        if (port < 1 || port > 49150) {
            Random random = new Random(System.currentTimeMillis());
            port = random.nextInt(45150);
            port += 4000;
        }
        Setting<Integer> setting = new Setting<Integer>(name, Integer.valueOf(port),
                null, preferences);
        if (preferences!=null)
            preferences.registerSetting(name, setting);
        return setting;
    }
    ///// END special purpose factory methods
}
