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

import phex.Main;
import phex.common.Phex;
import phex.common.ThreadTracking;
import phex.common.log.NLogger;
import phex.connection.LoopbackDispatcher;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.util.SystemProperties;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A class to run Phex from the Phex API.
 * This class is derived from phex.Main.
 *
 * @author Giorgio Busatto - 2011
 */
public class PhexRunner {

    private static PhexRunner phex = null;

    private PhexRunner() {
    }

    public static PhexRunner getInstance() {
        if (phex == null) {
            phex = new PhexRunner();
        }

        return phex;
    }

    /**
     * Don't use NLogger before arguments have been read ( -c )
     *
     * @param args
     */
    public static void main(String args[]) {
        PhexRunner runner = getInstance();

        if (runner.runPhex(args)) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

    /**
     * @param iterator
     * @return
     */
    private static String readArgument(Iterator<String> iterator) {
        if (!iterator.hasNext()) {
            return null;
        }

        String value = iterator.next();
//      if ( value.startsWith( "\"" ))
//      {
//          while (iterator.hasNext())
//          {
//              String additional = (String)iterator.next();
//              value += additional;
//              if ( additional.endsWith("\""))
//              {
//                  break;
//              }
//          }
//          if ( !value.endsWith("\"") )
//          {
//              throw new IllegalArgumentException( "Unterminated argument" );
//          }
//          // cut of starting and ending "
//          value = value.substring( 1, value.length() - 1 );
//      }
        return value;
    }

    public boolean startPhex() {
        return runPhex(new String[0]);
    }

//    private static void showSplash()
//    {
//        try
//        {
//            _splashScreen = new SplashScreen();
//            _splashScreen.showSplash();
//        }
//        catch (java.awt.HeadlessException ex)
//        {
//            // Running in head-less mode so of course the splash
//            // doesn't work.
//        }
//    }

    public boolean runPhex(String args[]) {
        long start = System.currentTimeMillis();
        long end;

//        // If there are no args to evaluate, show splash asap.
//        if (args == null || args.length == 0)
//        {
//            showSplash();
//            //end = System.currentTimeMillis();
//            //System.out.println("Splash time: " + (end-start));
//        }


        // Parse args...
        Iterator<String> iterator = Arrays.asList(args).iterator();

        String loopbackUri = null;
        String magmaFile = null;
        String rssFile = null;
        String argument;

        while ((argument = readArgument(iterator)) != null) {
            if (argument.equalsIgnoreCase("-c")) {
                String path = readArgument(iterator);
                if (path != null) {
                    System.setProperty(SystemProperties.PHEX_CONFIG_PATH_SYSPROP, path);
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
//          System.exit(0);

            return true;
        }

        if (magmaFile != null && LoopbackDispatcher.dispatchMagmaFile(magmaFile)) {// correctly dispatched uri
//          System.exit(0);

            return true;
        }

        if (rssFile != null && LoopbackDispatcher.dispatchRSSFile(rssFile)) {// correctly dispatched uri
//          System.exit(0);

            return true;
        }

        try {
            // Might be the case when arguments are used to start Phex,
//            // but there is no Phex running yet.
//            if (_splashScreen == null)
//            {
//                showSplash();
//
//                //end = System.currentTimeMillis();
//                //System.out.println("Splash time: " + (end-start));
//            }


//            PhexGuiPrefs.init();
//            Localizer.initialize(InterfacePrefs.LocaleName.get());

            ThreadTracking.initialize();

            Phex.initialize();
            Servent.getInstance();
            Servent.getInstance().start();

            end = System.currentTimeMillis();
            NLogger.debug(Main.class, "Pre GUI startup time: " + (end - start));

//            try
//            {
//                GUIRegistry.getInstance().initialize(Servent.getInstance());
//            }
//            catch (ExceptionInInitializerError ex)
//            {
//                // Running in head-less mode so of course this
//                // doesn't work.
//            }

//            MainFrame mainFrame = null;
//            mainFrame = GUIRegistry.getInstance().getMainFrame();
//            if (mainFrame != null)
//            {
//                mainFrame.setVisible(true);
//            }

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
            // Unhandled application exception... exit,
//          System.exit(1);

            return false;
        }

//        /**
//         * Trying to use a jython interpreter as powerful command line interface.
//         * This blocks, so it has to be at the end.
//         */
//        if (startConsole)
//        {
//            JythonInterpreter jython;
//            jython = new JythonInterpreter();
//            jython.startConsole();
//        }

        return true;
    }

//    private static void validateJavaVersion()
//    {
//        if (SystemUtils.isJavaVersionAtLeast(1.5f))
//        {
//            return;
//        }
//
//        JFrame frame = new JFrame("Wrong Java Version");
//        frame.setSize(new Dimension(0, 0));
//        frame.setVisible(true);
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        Dimension winSize = frame.getSize();
//        Rectangle rect = new Rectangle(
//        (screenSize.width - winSize.width) / 2,
//        (screenSize.height - winSize.height) / 2,
//        winSize.width, winSize.height );
//        frame.setBounds(rect);
//        JOptionPane.showMessageDialog( frame,
//                "Please use a newer Java VM.\n" +
//                "Phex requires at least Java 1.5.0. You are using Java " + SystemUtils.JAVA_VERSION + "\n" +
//                "To get the latest Java release go to http://java.com.",
//                "Wrong Java Version", JOptionPane.WARNING_MESSAGE );
//
//        System.exit(1);
//    }
}