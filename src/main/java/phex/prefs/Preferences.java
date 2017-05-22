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
 *  $Id: java 4524 2011-06-27 10:04:38Z gregork $
 */
package phex.prefs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.collections.SortedProperties;
import phex.util.FileUtils;
import phex.util.IOUtil;
import phex.util.StringUtils;

import java.io.*;
import java.util.*;

public class Preferences {
    private static final Logger logger = LoggerFactory.getLogger(Preferences.class);
    private final Map<String, Setting<?>> settingMap;
    public final File file;
    private Properties valueProperties;
    private boolean isSaveRequired;


    public Preferences(File file) {
        this.file = file;

        settingMap = new HashMap<String, Setting<?>>();
        valueProperties = new Properties();

        load();
    }

    public Setting<String> createStringSetting(String name,
                                               String defaultValue) {
        String value = getLoadedProperty(name);
        if (value == null) {
            value = defaultValue;
        }
        Setting<String> setting = new Setting<String>(name, value, defaultValue, this);

        registerSetting(name, setting);
        return setting;
    }

    public Setting<Boolean> createBoolSetting(String name,
                                              boolean defaultValue) {
        Boolean defaultBool = Boolean.valueOf(defaultValue);

        String value = getLoadedProperty(name);
        getLoadedProperty(name);
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
        Setting<Boolean> setting = new Setting<Boolean>(name, boolValue, defaultBool, this);

        registerSetting(name, setting);
        return setting;
    }

    public Setting<Float> createFloatSetting(String name,
                                             float defaultValue) {
        Float defaultFloat = Float.valueOf(defaultValue);

        String value = getLoadedProperty(name);
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
        Setting<Float> setting = new Setting<Float>(name, floatValue, defaultFloat, this);

        registerSetting(name, setting);
        return setting;
    }

    public Setting<Long> createLongSetting(String name,
                                           long defaultValue) {
        Long defaultLong = Long.valueOf(defaultValue);

        String value = getLoadedProperty(name);
        Long longValue;
        try {
            longValue = Long.valueOf(value);
        } catch (NumberFormatException exp) {
            longValue = defaultLong;
        }
        Setting<Long> setting = new Setting<Long>(name, longValue, defaultLong, this);

        registerSetting(name, setting);
        return setting;
    }

    public Setting<Integer> createIntSetting(String name,
                                             int defaultValue) {
        Integer defaultInt = Integer.valueOf(defaultValue);

        String value = getLoadedProperty(name); //getLoadedProperty(name);
        Integer intValue;
        try {
            intValue = Integer.valueOf(value);
        } catch (NumberFormatException exp) {
            intValue = defaultInt;
        }
        Setting<Integer> setting = new Setting<Integer>(name, intValue, defaultInt, this);
        registerSetting(name, setting);
        return setting;
    }

    public RangeSetting<Integer> createIntRangeSetting(String name,
                                                              int defaultValue, int minValue, int maxValue) {
        Integer defaultInt = Integer.valueOf(defaultValue);

        String value = getLoadedProperty(name);
        Integer intValue;
        try {
            intValue = Integer.valueOf(value);
        } catch (NumberFormatException exp) {
            intValue = defaultInt;
        }
        RangeSetting<Integer> setting = new RangeSetting<Integer>(name, intValue, defaultInt,
                Integer.valueOf(minValue), Integer.valueOf(maxValue), this);
            registerSetting(name, setting);
        return setting;
    }

    public Setting<Short> createShortRangeSetting(String name,
                                                  short defaultValue, short minValue, short maxValue) {
        Short defaultShort = Short.valueOf(defaultValue);

        String value = getLoadedProperty(name);
        Short shortValue;
        try {
            shortValue = Short.valueOf(value);
        } catch (NumberFormatException exp) {
            shortValue = defaultShort;
        }
        RangeSetting<Short> setting = new RangeSetting<Short>(name, shortValue, defaultShort,
                Short.valueOf(minValue), Short.valueOf(maxValue), this);

            registerSetting(name, setting);
        return setting;
    }

    public Setting<Set<String>> createSetSetting(String name) {
        Set<String> values = PreferencesCodec.deserializeSet(name, this);
        Setting<Set<String>> setting = new Setting<Set<String>>(name, values,
                null, this);
        registerSetting(name, setting);
        return setting;
    }

    public Setting<List<String>> createListSetting(String name) {
        List<String> values = PreferencesCodec.deserializeList(name, this);
        Setting<List<String>> setting = new Setting<List<String>>(name, values,
                null, this);
        registerSetting(name, setting);
        return setting;
    }

    ///// special purpose factory methods
    public Setting<Integer> createListeningPortSetting(String name) {
        String value = getLoadedProperty(name);// = null;
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
                null, this);

        registerSetting(name, setting);
        return setting;
    }
    ///// END special purpose factory methods


    protected String getLoadedProperty(String name) {
        return valueProperties.getProperty(name);
    }

    protected List<String> getPrefixedPropertyNames(String prefix) {
        List<String> found = new ArrayList<String>();
        Set<Object> keys = valueProperties.keySet();
        for (Object keyObj : keys) {
            String key = (String) keyObj;
            if (key.startsWith(prefix)) {
                found.add(key);
            }
        }
        return found;
    }

    protected void registerSetting(String name, Setting<?> setting) {
        settingMap.put(name, setting);
    }

    public synchronized void saveRequiredNotify() {
        isSaveRequired = true;
    }

    protected void fireSettingChanged(SettingChangedEvent<?> event) {
        saveRequiredNotify();

    }

    public synchronized void load() {
        Properties loadProperties = new Properties();
        InputStream inStream = null;
        try {
            inStream = new BufferedInputStream(new FileInputStream(file));
            loadProperties.load(inStream);
        } catch (IOException exp) {
            IOUtil.closeQuietly(inStream);
            if (!(exp instanceof FileNotFoundException)) {
                logger.error(exp.toString(), exp);
            }
            // There was a problem loading the properties file.. try to load a
            // possible backup...
            File bakFile = new File(file.getParentFile(),
                    file.getName() + ".bak");
            try {
                inStream = new BufferedInputStream(new FileInputStream(bakFile));
                loadProperties.load(inStream);
            } catch (FileNotFoundException exp2) {/* ignore */ } catch (IOException exp2) {
                logger.error(exp.toString(), exp);
            }
        } finally {
            IOUtil.closeQuietly(inStream);
        }
        valueProperties = loadProperties;


        /*

        deserializeSimpleFields();
        deserializeComplexFields();

        handlePhexVersionAdjustments();

        // no listening port is set yet. Choose a random port from 4000-63999
        if (mListeningPort == -1 || mListeningPort > 65500 )
        {
            Random random = new Random(System.currentTimeMillis());
            mListeningPort = random.nextInt( 60000 );
            mListeningPort += 4000;
        }
        updateSystemSettings();

        // count startup...
        totalStartupCounter ++;

        // make sure directories exists...
        File dir = new File( mDownloadDir );
        dir.mkdirs();
        dir = new File( incompleteDir );
        dir.mkdirs();

        */
    }

    public synchronized void save() {
        if (!isSaveRequired) {
            logger.debug("No saving of preferences required.");
            return;
        }
        logger.debug("Saving preferences to: {}", file.getAbsolutePath());
        Properties saveProperties = new SortedProperties();

        for (Setting<?> setting : settingMap.values()) {
            if (setting.isDefault() && !setting.isAlwaysSaved()) {
                continue;
            }
            PreferencesCodec.serializeSetting(setting, saveProperties);
        }

        File bakFile = new File(file.getParentFile(), file.getName() + ".bak");
        try {
            // make a backup of old pref File.
            if (file.exists()) {
                FileUtils.copyFile(file, bakFile);
            }

            // create a new pref file.
            OutputStream os = null;
            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
                saveProperties.store(os, "Phex Preferences");
            } finally {
                IOUtil.closeQuietly(os);
            }
        } catch (IOException exp) {
            logger.error(exp.toString(), exp);
        }
    }
}