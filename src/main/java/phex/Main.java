/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2009 Phex Development Group
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
 *  $Id: Main.java 4483 2009-09-20 16:43:17Z ArneBab $
 */
package phex;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;
import phex.common.Phex;
import phex.common.ThreadTracking;
import phex.common.log.NLogger;
import phex.connection.LoopbackDispatcher;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.util.SystemProperties;

import java.util.Arrays;
import java.util.Iterator;


public class Main {

    static {
        boolean DEBUG = true;
        if (!DEBUG) {
            ((ch.qos.logback.classic.Logger) (LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)))
                    .setLevel(Level.INFO);
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String args[]) {
        long start = System.currentTimeMillis();
        long end;

        // if there are no args to evaluate, show splash asap.
        if (args == null || args.length == 0) {
            //showSplash();
            //end = System.currentTimeMillis();
            //System.out.println("Splash time: " + (end-start));
        }

        //validateJavaVersion();

        // parse args...
        Iterator<String> iterator = Arrays.asList(args).iterator();

        String loopbackUri = null;
        String magmaFile = null;
        String rssFile = null;
        String argument;

        while ((argument = readArgument(iterator)) != null) {
            if (argument.equalsIgnoreCase("-c")) {
                String path = readArgument(iterator);
                if (path != null) {
                    System.setProperty(SystemProperties.PHEX_CONFIG_PATH_SYSPROP,
                            path);
                }
            } else if (argument.equalsIgnoreCase("-uri")) {
                loopbackUri = readArgument(iterator);
            } else if (argument.equalsIgnoreCase("-magma")) {
                magmaFile = readArgument(iterator);
            } else if (argument.equalsIgnoreCase("-rss")) {
                rssFile = readArgument(iterator);
            }
        }


        PhexCorePrefs.init();

        if (loopbackUri != null && LoopbackDispatcher.dispatchUri(loopbackUri)) {// correctly dispatched uri
            System.exit(0);
        }
        if (magmaFile != null && LoopbackDispatcher.dispatchMagmaFile(magmaFile)) {// correctly dispatched uri
            System.exit(0);
        }
        if (rssFile != null && LoopbackDispatcher.dispatchRSSFile(rssFile)) {// correctly dispatched uri
            System.exit(0);
        }

        try {
            // might be the case when arguments are used to start Phex,
            // but there is no Phex running yet.
//            if (splashScreen == null) {
//                showSplash();
//                //end = System.currentTimeMillis();
//                //System.out.println("Splash time: " + (end-start));
//            }


//            PhexGuiPrefs.init();
//            Localizer.initialize(InterfacePrefs.LocaleName.get());

            ThreadTracking.initialize();


            Phex.initialize();
            Servent.getInstance().start();

            end = System.currentTimeMillis();
            NLogger.debug(Main.class, "Pre GUI startup time: " + (end - start));

//            try {
//                GUIRegistry.getInstance().initialize(Servent.getInstance());
//            } catch (ExceptionInInitializerError ex) {
//                // running in headless mode so of course this
//                // doesn't work
//            }
//            if (splashScreen != null) {
//                splashScreen.closeSplash();
//                splashScreen = null;
//            }

//            MainFrame mainFrame = null;
//            mainFrame = GUIRegistry.getInstance().getMainFrame();
//            if (mainFrame != null)
//                mainFrame.setVisible(true);

            end = System.currentTimeMillis();
            NLogger.debug(Main.class, "Full startup time: " + (end - start));


            if (loopbackUri != null) {// correctly dispatched uri

            }
            if (magmaFile != null) {// correctly dispatched uri

            }
            if (rssFile != null) {// correctly dispatched uri

            }
        } catch (Throwable th) {
            th.printStackTrace();
            NLogger.error(Main.class, th, th);
            // unhandled application exception... exit
            System.exit(1);
        }



    }

//    private static void showSplash() {
//        try {
//            splashScreen = new SplashScreen();
//            splashScreen.showSplash();
//        } catch (java.awt.HeadlessException ex) {
//            // running in headless mode so of course the splash
//            // doesn't work
//        }
//    }


    /**
     * @param iterator
     * @return
     */
    private static String readArgument(Iterator<String> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }
        String value = iterator.next();
//        if ( value.startsWith( "\"" ))
//        {
//            while (iterator.hasNext())
//            {
//                String additional = (String)iterator.next();
//                value += additional;
//                if ( additional.endsWith("\""))
//                {
//                    break;
//                }
//            }
//            if ( !value.endsWith("\"") )
//            {
//                throw new IllegalArgumentException( "Unterminated argument" );
//            }
//            // cut of starting and ending "
//            value = value.substring( 1, value.length() - 1 );
//        }
        return value;
    }

//    /**
//     *
//     */
//    private static void validateJavaVersion()
//    {
//        if ( SystemUtils.isJavaVersionAtLeast( 1.5f ) )
//        {
//            return;
//        }
//
//        JFrame frame = new JFrame( "Wrong Java Version" );
//        frame.setSize( new Dimension( 0, 0 ) );
//        frame.setVisible(true);
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension winSize = frame.getSize();
//        Rectangle rect = new Rectangle(
//            (screenSize.width - winSize.width) / 2,
//            (screenSize.height - winSize.height) / 2,
//            winSize.width, winSize.height );
//        frame.setBounds(rect);
//        JOptionPane.showMessageDialog( frame,
//            "Please use a newer Java VM.\n" +
//            "Phex requires at least Java 1.5.0. You are using Java " + SystemUtils.JAVA_VERSION + "\n" +
//            "To get the latest Java release go to http://java.com.",
//            "Wrong Java Version", JOptionPane.WARNING_MESSAGE );
//        System.exit( 1 );
//    }
}
