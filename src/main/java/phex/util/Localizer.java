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
 *  $Id: Localizer.java 4539 2011-08-14 17:28:33Z gregork $
 */
package phex.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * This class is intended to provide localized strings.
 * <p>
 * <b>How to store localized resource bundles</b>
 * Phex will look for resource bundles in the classpath that includes the
 * directory $PHEX/lang.<br>It will look for a file called 'language.list'
 * This file should contain a list of translated locales, one per row, in the format
 * language_COUNTRY e.g. de_DE for German Germany. Also its possible to only provide
 * a single language without country definition e.g. de as a definition for all
 * German speaking countries.<br>
 * Translation files for each local should be named e.g. Lang_de_DE.properties or
 * Lang_de.properties<br>
 * <br>
 * <b>Lookup strategy</b>
 * On startup Phex will try to use the locale defined in its configuration file. If
 * nothing is configured it will use the standard platform locale. With the defined
 * locale e.g. de_DE the $PHEX/lang directory and afterwards the classpath
 * phex.resources is searched for a file called Lang_de_DE.properties, then for
 * a file Lang_de.properties and then for a file Lang.properties. All found files
 * are chained for language key lookup in a ResourceBundle. If a locale exists but
 * doesn't have the string required, that string is looked for in the default locale
 * which is en_US.
 * <p>
 * To display all available locales in the options menu, Phex will use the file
 * $PHEX/lang/language.list and the internal resource
 * phex/resources/language.list for available locale definitions.
 *
 * @author Gregork and Tamara Civera
 */
public class Localizer {
    private static final Logger logger = LoggerFactory.getLogger(Localizer.class);
    private static final String DEFAULT_LOCALE = "en_US";
    private static final String FILES_PREFIX = "Lang";
    private static final int MAX_LINE_LENGTH = 4096;
    private static Map<String, String> langKeyMap;
    private static Map<String, String> defaultLangKeyMap;
    private static Locale usedLocale;
    private static List<Locale> availableLocales;
    private static DecimalFormatSymbols decimalFormatSymbols;
    private static NumberFormat integerNumberFormat;
    private static HashMap<String, String> countryNameCache;

    public static void initialize(String localeStr) {
        setUsedLocale(Locale.US);

        Locale locale;
        if (localeStr == null || localeStr.length() == 0 ||
                (localeStr.length() != 2 && localeStr.length() != 5 && localeStr.length() != 8)) {// default to en_US
            locale = Locale.US;
        } else {
            String lang = localeStr.substring(0, 2);
            String country = "";
            if (localeStr.length() >= 5) {
                country = localeStr.substring(3, 5);
            }
            String variant = "";
            if (localeStr.length() == 8) {
                variant = localeStr.substring(6, 8);
            }
            locale = new Locale(lang, country, variant);
        }
        setUsedLocale(locale);
    }

    public static Locale getUsedLocale() {
        return usedLocale;
    }

    public static void setUsedLocale(Locale locale) {
        usedLocale = locale;
        buildResourceBundle(locale);
        decimalFormatSymbols = new DecimalFormatSymbols(usedLocale);
        integerNumberFormat = NumberFormat.getIntegerInstance(usedLocale);
        countryNameCache = new HashMap<String, String>();
    }

    public static DecimalFormatSymbols getDecimalFormatSymbols() {
        return decimalFormatSymbols;
    }

    public static NumberFormat getIntegerNumberFormat() {
        return integerNumberFormat;
    }

    public static String getCountryName(String countryCode) {
        String countryName = countryNameCache.get(countryCode);
        if (countryName == null) {
            Locale l = new Locale("", countryCode);
            countryName = l.getDisplayCountry(usedLocale);
            countryNameCache.put(countryCode, countryName);
        }
        return countryName;
    }

    /**
     * To display all available locales in the options menu, Phex will use the file
     * $PHEX/lang/translations.list and the internal resource
     * phex/resources/translations.list for available locale definitions.
     */
    public static synchronized List<Locale> getAvailableLocales() {
        if (availableLocales != null) {
            return availableLocales;
        }
        availableLocales = new ArrayList<Locale>();
        List<Locale> list = loadLocalList("/language.list");
        availableLocales.addAll(list);
        list = loadLocalList("/phex/resources/language.list");
        availableLocales.addAll(list);
        return availableLocales;
    }

    private static List<Locale> loadLocalList(String name) {
        InputStream stream = Localizer.class.getResourceAsStream(name);
        if (stream == null) {
            return Collections.emptyList();
        }
        // make sure it is buffered
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream, "ISO-8859-1"));
            ArrayList<Locale> list = new ArrayList<Locale>();
            String line;
            Locale locale;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.startsWith("#")
                        || (line.length() != 2 && line.length() != 5 && line.length() != 8)) {
                    continue;
                }
                String lang = line.substring(0, 2);
                String country = "";
                if (line.length() >= 5) {
                    country = line.substring(3, 5);
                }
                String variant = "";
                if (line.length() == 8) {
                    variant = line.substring(6, 8);
                }
                locale = new Locale(lang, country, variant);
                list.add(locale);
            }
            return list;
        } catch (IOException exp) {
            logger.error(exp.toString(), exp);
        } finally {
            IOUtil.closeQuietly(stream);
        }
        return Collections.emptyList();
    }

    /**
     * Returns the actual language text out of the resource boundle.
     * If the key is not defined it returns the key itself and prints an
     * error message on system.err.
     */
    /*@NonNull*/
    public static String getString(String key) {
        if (langKeyMap == null)
            return key;

        String value = langKeyMap.get(key);
        if (value == null) {
            // If the string is not internationalized, gets the default english value
            value = defaultLangKeyMap.get(key);
            if (value == null) {
                logger.error("Missing language key: {}", key);
                value = key;
            }
        } else if (value.replace(" ", "").length() == 0) {
            // If the string is not internationalized, gets the default english value
            value = defaultLangKeyMap.get(key);
            if (value == null) {
                logger.error("Missing language key: {}", key);
                value = key;
            }
        }
        return value;
    }

    /**
     * Returns the first character of the actual language text out of the
     * resource bundle. The method can be useful for getting mnemonics.
     * If the key is not defined it returns the first char of the key itself and
     * prints an error message on system.err.
     */
    public static char getChar(String key) {
        String str = getString(key);
        if (str.length() > 0) {
            return str.charAt(0);
        } else {
            logger.error("Missing language key: {}", key);
        }
        return '#';
    }

    /**
     * Returns the actual language text out of the resource bundle and formats
     * it accordingly with the given Object array.
     * If the key is not defined it returns the key itself and print an
     * error message on system.err.
     */
    public static String getFormatedString(String key, Object... obj) {
        String value = null;

        String lookupValue = langKeyMap.get(key);
        if (lookupValue != null) {
            if (lookupValue.replace(" ", "").length() == 0) {
                // The string is not internationalized
                lookupValue = defaultLangKeyMap.get(key);
            }
            if (lookupValue == null) {
                logger.error("Missing language key: {}", key);
                value = key;
            } else {
                value = MessageFormat.format(lookupValue, obj);
            }
        } else {
            // The string is not internationalized
            lookupValue = defaultLangKeyMap.get(key);
            if (lookupValue == null) {
                logger.error("Missing language key: {}", key);
                value = key;
            } else {
                value = MessageFormat.format(lookupValue, obj);
            }
        }
        return value;
    }


    public static void buildResourceBundle(Locale locale) {
        ArrayList<String> fileList = new ArrayList<String>();
        StringBuffer buffer = new StringBuffer(FILES_PREFIX);
        fileList.add(buffer.toString());
        String language = locale.getLanguage();
        if (language.length() > 0) {
            buffer.append('_');
            buffer.append(language);
            fileList.add(buffer.toString());
            String country = locale.getCountry();
            if (country.length() > 0) {
                buffer.append('_');
                buffer.append(country);
                fileList.add(buffer.toString());
                String variant = locale.getVariant();
                if (variant.length() > 0) {
                    buffer.append('_');
                    buffer.append(variant);
                    fileList.add(buffer.toString());
                }
            }
        }
        langKeyMap = loadProperties(buffer.toString());
        defaultLangKeyMap = loadProperties(FILES_PREFIX + '_' + DEFAULT_LOCALE);
    }

    private static HashMap<String, String> loadProperties(String name) {
        HashMap<String, String> langKeyMapTmp = new HashMap<String, String>();
        String extension = ".po";
        if (name.equals(FILES_PREFIX)) {
            extension = ".pot";
        }
        String fileName = "/phex/resources/" + name + extension;
        try {
            InputStream stream = Localizer.class.getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    stream, "UTF-8"));
            boolean ok = false;
            String key;
            StringBuilder valueBuilder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                ok = false;
                key = null;
                valueBuilder.setLength(0);
                while (!ok && (line != null)) {
                    if (line.startsWith("msgid")) {
                        key = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
                        ok = true;
                    }
                    line = reader.readLine();
                }
                ok = false;
                while (!ok && (line != null)) {
                    if (line.startsWith("msgstr")) {
                        ok = true;
                        appendUnescaped(line.substring(line.indexOf('"') + 1,
                                line.lastIndexOf('"')), valueBuilder);
                        readValueLines(reader, valueBuilder);
                    } else {
                        line = reader.readLine();
                    }
                }
                if ((valueBuilder.length() > 0) && (key != null) && (key.replace(" ", "").length() > 0)) {
                    langKeyMapTmp.put(key, valueBuilder.toString());
                }
                line = reader.readLine();
            }
        } catch (Exception exp) {
            logger.error("An error reading the file {} has ocurred.", fileName);
            logger.error(exp.toString(), exp);
        }
        return langKeyMapTmp;
    }

    private static void readValueLines(BufferedReader reader, StringBuilder builder) throws IOException {
        reader.mark(MAX_LINE_LENGTH);
        String line = reader.readLine();
        while (line != null && line.trim().startsWith("\"")) {
            if (builder.length() > 0
                    && !Character.isWhitespace(builder.charAt(builder.length() - 1))) {
                builder.append(' ');
            }
            appendUnescaped(line.substring(line.indexOf('"') + 1, line.lastIndexOf('"')),
                    builder);
            reader.mark(4096);
            line = reader.readLine();
        }
        reader.reset();
    }

    private static void appendUnescaped(String line, StringBuilder builder) {
        char[] charArray = line.toCharArray();
        int offset = 0;
        int len = charArray.length;
        builder.ensureCapacity(builder.length() + len);
        char aChar;
        while (offset < len) {
            aChar = charArray[offset++];
            if (aChar == '\\') {
                aChar = charArray[offset++];
                if (aChar == 't') aChar = '\t';
                else if (aChar == 'r') aChar = '\r';
                else if (aChar == 'n') aChar = '\n';
                else if (aChar == 'f') aChar = '\f';
                builder.append(aChar);
            } else {
                builder.append(aChar);
            }
        }
    }
}