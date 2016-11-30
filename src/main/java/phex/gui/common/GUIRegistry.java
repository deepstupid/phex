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
 *  $Id: GUIRegistry.java 4523 2011-06-22 09:27:23Z gregork $
 */
package phex.gui.common;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.Environment;
import phex.common.EnvironmentConstants;
import phex.common.Phex;
import phex.common.PhexVersion;
import phex.common.file.FileManager;
import phex.common.file.ManagedFile;
import phex.common.file.ManagedFileException;
import phex.event.UserMessageListener;
import phex.gui.actions.*;
import phex.gui.chat.ChatFrameManager;
import phex.gui.macosx.MacOsxGUIUtils;
import phex.gui.prefs.InterfacePrefs;
import phex.servent.Servent;
import phex.update.UpdateCheckRunner;
import phex.utils.Localizer;
import phex.utils.StringUtils;
import phex.utils.VersionUtils;
import phex.xml.sax.DPhex;
import phex.xml.sax.XMLBuilder;
import phex.xml.sax.gui.DGuiSettings;
import phex.xml.sax.gui.DTableList;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TimerTask;

public final class GUIRegistry implements GUIRegistryConstants
{
    private static final Logger logger = LoggerFactory.getLogger( GUIRegistry.class );
    
    /**
     * The {@link Servent} that the ui displays.
     */
    private Servent servent;
    
    /**
     * Contains the global actions of this app together with a retrieval key.
     */
    private HashMap<String, FWAction> globalActionMap;

    private LazyEventQueue lazyEventQueue;
    private GuiUpdateTimer guiUpdateTimer;
    private DesktopIndicator desktopIndicator;
    private IconPack systemIconPack;
    private IconPack plafIconPack;
    private IconPack countryIconPack;
    
    // keep strong reference to prevent GC
    @SuppressWarnings("unused")
    private GlobalGuiEventListeners globalEventListeners;
    
    private ChatFrameManager chatFrameManager;
    private MainFrame mainFrame;


    private boolean showTableHorizontalLines;
    private boolean showTableVerticalLines;
    private boolean useLogBandwidthSlider;
    private boolean showRespectCopyrightNotice;

    private GUIRegistry()
    {
        
    }
    
    private static class Holder
    {
        protected static final GUIRegistry instance = new GUIRegistry();
    }

    public static GUIRegistry getInstance()
    {
      return GUIRegistry.Holder.instance;
    }
    
    public void initialize( Servent serv )
    {
        this.servent = serv;
        
        // make sure you never need to keep a reference of DGuiSettings
        // by a class attributes...
        DGuiSettings guiSettings = loadGUISettings();
        initializeGUISettings( guiSettings );
        
        systemIconPack = new IconPack( SYSTEM_ICON_PACK_RESOURCE );
        countryIconPack = new CountryFlagIconPack();
        
        // only systray support on windows...
        if ( SystemUtils.IS_OS_WINDOWS )
        {
            try
            {
                desktopIndicator = new DesktopIndicator();
            }
            catch(UnsupportedOperationException x)
            {
                desktopIndicator = null;
            }
        }
        
        if ( SystemUtils.IS_OS_MAC_OSX )
        {
            MacOsxGUIUtils.installEventHandlers();
        }
        
        guiUpdateTimer = new GuiUpdateTimer( InterfacePrefs.GuiUpdateInterval );
        
        initializeGlobalActions();
        chatFrameManager = new ChatFrameManager();
        try 
        {
            mainFrame = new MainFrame( guiSettings );
            logger.debug( "GUIRegistry initialized." );
        } 
        catch ( java.awt.HeadlessException ex ) 
        {
            // ignore GUI will fail.. :(
        }
        
        globalEventListeners = new GlobalGuiEventListeners( 
            Phex.getEventService() );

        Environment environment = Environment.getInstance();

        // before we set of the update check we will wait 1 minute...
        // this will help manager to initialize (file scan) and reduce
        // update checks on short Phex session lifetimes...
        environment.scheduleTimerTask( new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    UpdateCheckRunner.triggerAutoBackgroundCheck( 
                        new GUIUpdateNotificationListener(),
                        InterfacePrefs.ShowBetaUpdateNotification.get().booleanValue() );
                }
                catch ( Throwable th )
                {
                    logger.error( th.toString(), th );
                }
            }
        }, DateUtils.MILLIS_PER_MINUTE );
        
        environment.setUserMessageListener( new GUIUserMessageListener() );
    }
    
    public Servent getServent()
    {
        return servent;
    }

    /**
     * Returns the DesktopIndicator responsible for systray support.
     * Method might return null if no systray is supported.
     */
    public DesktopIndicator getDesktopIndicator()
    {
        return desktopIndicator;
    }

    public MainFrame getMainFrame()
    {
        return mainFrame;
    }

    public IconPack getPlafIconPack()
    {
        return plafIconPack;
    }
    
    public void setPlafIconPack( IconPack pack )
    {
        plafIconPack = pack;
    }
    
    public IconPack getCountryIconPack()
    {
        return countryIconPack;
    }
    
    public IconPack getSystemIconPack()
    {
        return systemIconPack;
    }

    public LazyEventQueue getLazyEventQueue()
    {
        if ( lazyEventQueue == null )
        {
            lazyEventQueue = new LazyEventQueue();
        }
        return lazyEventQueue;
    }

    public GuiUpdateTimer getGuiUpdateTimer()
    {
        return guiUpdateTimer;
    }

    public FWAction getGlobalAction( String actionKey )
    {
        return globalActionMap.get( actionKey );
    }
    
    public String getUsedLAFClass()
    {
        return UIManager.getLookAndFeel().getClass().getName();
    }

    /**
     * Returns true if the tables draw horizontal lines between cells, false if
     * they don't. The default is false for MacOSX and Windows, true for others.
     * @return true if the tables draw horizontal lines between cells, false
     * if they don't.
     */
    public boolean getShowTableHorizontalLines()
    {
        return showTableHorizontalLines;
    }

    /**
     * Sets whether the tables draw horizontal lines between cells. If
     * showHorizontalLines is true it does; if it is false it doesn't.
     * @param showHorizontalLines
     */
    public void setShowTableHorizontalLines( boolean showHorizontalLines )
    {
        showTableHorizontalLines = showHorizontalLines;
    }

    /**
     * Returns true if the tables draw vertical lines between cells, false if
     * they don't. The default is false for MacOSX and Windows, true for others.
     * @return true if the tables draw vertical lines between cells, false
     * if they don't.
     */
    public boolean getShowTableVerticalLines()
    {
        return showTableVerticalLines;
    }

    /**
     * Sets whether the tables draw vertical lines between cells. If
     * showVerticalLines is true it does; if it is false it doesn't.
     * @param showVerticalLines
     */
    public void setShowTableVerticalLines( boolean showVerticalLines )
    {
        showTableVerticalLines = showVerticalLines;
    }


    /**
     * @return Returns the isLogarithmicBandwidthSliderUsed.
     */
    public boolean useLogBandwidthSlider()
    {
        return useLogBandwidthSlider;
    }
    
    /**
     * @param useLogBandwidthSlider The useLogBandwidthSlider to set.
     */
    public void setLogBandwidthSliderUsed(
        boolean useLogBandwidthSlider)
    {
        this.useLogBandwidthSlider = useLogBandwidthSlider;
    }
    
    /**
     * @return Returns the showRespectCopyrightNotice.
     */
    public boolean isRespectCopyrightNoticeShown()
    {
        return showRespectCopyrightNotice;
    }
    
    /**
     * @param showRespectCopyrightNotice The showRespectCopyrightNotice to set.
     */
    public void setRespectCopyrightNoticeShown(
        boolean showRespectCopyrightNotice)
    {
        this.showRespectCopyrightNotice = showRespectCopyrightNotice;
    }
    
    /**
     * Loads the DGuiSettings object or null if its not existing or a parsing
     * error occurs.
     * @return the DGuiSettings object.
     */
    private synchronized DGuiSettings loadGUISettings()
    {
        logger.debug( "Loading gui settings file" );
        
        File inputFile = Environment.getInstance().getPhexConfigFile(
            EnvironmentConstants.XML_GUI_SETTINGS_FILE_NAME );
        
        DPhex dPhex;
        try
        {
            if ( !inputFile.exists() )
            {
                logger.debug( "No gui settings configuration file found." );
                return null;
            }
            
            FileManager fileMgr = Phex.getFileManager();
            ManagedFile managedFile = fileMgr.getReadWriteManagedFile( inputFile );
            dPhex = XMLBuilder.loadDPhexFromFile( managedFile );
            if ( dPhex == null )
            {
                logger.debug( "No DPhex found." );
                return null;
            }
            updateGUISettings( dPhex );
            return dPhex.getGuiSettings();
        }
        catch ( IOException exp )
        {
            logger.error( exp.toString(), exp );
            Environment.getInstance().fireDisplayUserMessage( 
                UserMessageListener.GuiSettingsLoadFailed, 
                new String[]{ exp.toString() } );
            return null;
        }
        catch ( ManagedFileException exp )
        {
            logger.error( exp.toString(), exp );
            Environment.getInstance().fireDisplayUserMessage( 
                UserMessageListener.GuiSettingsLoadFailed, 
                new String[]{ exp.toString() } );
            return null;
        }
    }

    /**
     * Saves the DGuiSettings object or null if its not existing or a parsing
     * error occurs.
     */
    public synchronized void saveGUISettings()
    {
        logger.debug( "Saving gui settings." );
        try
        {
            DPhex phex = new DPhex();
            phex.setPhexVersion( PhexVersion.getFullVersion() );

            DGuiSettings dSettings = new DGuiSettings();
            dSettings.setLogBandwidthSliderUsed( useLogBandwidthSlider );
            dSettings.setShowRespectCopyrightNotice( showRespectCopyrightNotice );
            phex.setGuiSettings( dSettings );

            DTableList dTableList = new DTableList();
            dTableList.setShowHorizontalLines( showTableHorizontalLines );
            dTableList.setShowVerticalLines( showTableVerticalLines );
            dSettings.setTableList( dTableList );
            
            dSettings.setLookAndFeelClass( getUsedLAFClass() );
            dSettings.setIconPackName( plafIconPack.getName() );

            // could be null when headless
            if ( mainFrame != null )
            {
                mainFrame.saveGUISettings( dSettings );
            }

            File outputFile = Environment.getInstance().getPhexConfigFile(
                EnvironmentConstants.XML_GUI_SETTINGS_FILE_NAME );
            ManagedFile managedFile = Phex.getFileManager().getReadWriteManagedFile( outputFile );
            XMLBuilder.saveToFile( managedFile, phex );
        }
        catch ( IOException exp )
        {
            // TODO during close this message is never displayed since application
            // will exit too fast. A solution to delay exit process in case 
            // SlideInWindows are open needs to be found.
            logger.error( exp.toString(), exp );
            Environment.getInstance().fireDisplayUserMessage( 
                UserMessageListener.GuiSettingsSaveFailed, 
                new String[]{ exp.toString() } );
        }
        catch ( ManagedFileException exp )
        {
            // TODO during close this message is never displayed since application
            // will exit too fast. A solution to delay exit process in case 
            // SlideInWindows are open needs to be found.
            logger.error( exp.toString(), exp );
            Environment.getInstance().fireDisplayUserMessage( 
                UserMessageListener.GuiSettingsSaveFailed, 
                new String[]{ exp.toString() } );
        }
    }

    /**
     * Initializes global actions that need or can be available before the main
     * frame is initialized.
     */
    private void initializeGlobalActions()
    {
        // required capacity is calculated by
        // Math.ceil( actionCount * 1 / 0.75 )
        // -> actionCount = 10 -> capacity =  14
        globalActionMap = new HashMap<String, FWAction>( 2 );

        FWAction action = new ExitPhexAction();
        globalActionMap.put( EXIT_PHEX_ACTION, action );
        
        action = new NewDownloadAction();
        globalActionMap.put( NEW_DOWNLOAD_ACTION, action );

        action = new ConnectNetworkAction();
        globalActionMap.put( CONNECT_NETWORK_ACTION, action );

        action = new DisconnectNetworkAction();
        globalActionMap.put( DISCONNECT_NETWORK_ACTION, action );

        action = new SwitchNetworkAction();
        globalActionMap.put( SWITCH_NETWORK_ACTION, action );
    }

    private void initializeGUISettings( DGuiSettings guiSettings )
    {
        // set default values...
        if ( SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_WINDOWS  )
        {
            showTableHorizontalLines = false;
            showTableVerticalLines = false;
        }
        else
        {
            showTableHorizontalLines = true;
            showTableVerticalLines = true;
        }
        useLogBandwidthSlider = false;
        showRespectCopyrightNotice = true;

        // sets old values from old cfg...
        ToolTipManager.sharedInstance().setEnabled(
            InterfacePrefs.DisplayTooltip.get().booleanValue() );
            
        String userLafClass;
        String iconPackName;
        // load values from gui new settings if available.
        if ( guiSettings != null )
        {
            if ( guiSettings.isSetLogBandwidthSliderUsed() )
            {
                useLogBandwidthSlider = guiSettings.isLogBandwidthSliderUsed();
            }
            if ( guiSettings.isSetShowRespectCopyrightNotice() )
            {
                showRespectCopyrightNotice = guiSettings.isShowRespectCopyrightNotice();
            }
            
            DTableList tableList = guiSettings.getTableList();
            if ( tableList != null && tableList.isSetShowHorizontalLines() )
            {
                showTableHorizontalLines = tableList.isShowHorizontalLines();
            }
            if ( tableList != null && tableList.isSetShowVerticalLines() )
            {
                showTableVerticalLines = tableList.isShowVerticalLines();
            }
            iconPackName = guiSettings.getIconPackName();
            userLafClass = guiSettings.getLookAndFeelClass();
        }
        else
        {
            userLafClass = null;
            iconPackName = null;
        }
        
        if ( iconPackName != null )
        {
            plafIconPack = IconPack.createIconPack( iconPackName );
        }
        if ( plafIconPack == null )
        {
            plafIconPack = IconPack.createDefaultIconPack();
        }
        
        LookAndFeel laf = LookAndFeelUtils.determineLAF(userLafClass);
        String phexLafClass = laf.getClass().getName();
        if ( userLafClass != null && !phexLafClass.equals( userLafClass ) )
        {// in case we had to switch LAF show error.
            JOptionPane.showMessageDialog( 
                GUIRegistry.getInstance().getMainFrame(),
                Localizer.getString("LAF_ErrorLoadingSwitchToDefault"),
                Localizer.getString("Error"), 
                JOptionPane.ERROR_MESSAGE );
        }
        
        if ( phexLafClass.equals( UIManager.getLookAndFeel().getClass().getName()))
        {
            // in case correct laf is already set just update UI!
            // this must be done to get colors correctly initialized!
            GUIUtils.updateComponentsUI();
        }
        else
        {
            try 
            {
                LookAndFeelUtils.setLookAndFeel( laf );
            }
            catch ( ExceptionInInitializerError ex ) 
            {
                // headless mode
            }
            catch (LookAndFeelFailedException e)
            {// this is supposed to never happen.. since the LAF
             // should already be tested to function.
                assert( false );
            }
        }
    }
    
    ///////////////// Settings updates /////////////////////////
    
    private void updateGUISettings( DPhex dPhex )
    {
        if ( dPhex == null )
        {
            return;
        }
        String version = dPhex.getPhexVersion();
        if ( VersionUtils.compare( PhexVersion.getFullVersion(), version) != 0 )
        {
            // a Phex version change... reactivate respect copyright dialog
            reactivateRespectCopyright( dPhex );
        }
        if ( VersionUtils.compare( "2.1.6.82", version) > 0 )
        {
            updatesForBuild82( dPhex );
        }
    }
    
    private void updatesForBuild82( DPhex dPhex )
    {
        DGuiSettings guiSettings = dPhex.getGuiSettings();
        if ( guiSettings == null )
        {
            return;
        }
        String userLafClass = guiSettings.getLookAndFeelClass();
        if ( userLafClass == null )
        {
            return;
        }
        if ( userLafClass.startsWith( "com.jgoodies.plaf" ) )
        {
            userLafClass = StringUtils.replace(userLafClass, 
                "com.jgoodies.plaf.", "com.jgoodies.looks.", 1 );
            guiSettings.setLookAndFeelClass(userLafClass);
        }
    }
    
    /**
     * Reactivate respect copyright dialog on a Phex version change.
     * @param dPhex
     */
    private void reactivateRespectCopyright( DPhex dPhex )
    {
        DGuiSettings guiSettings = dPhex.getGuiSettings();
        if ( guiSettings == null )
        {
            return;
        }
        guiSettings.setShowRespectCopyrightNotice(true);
    }
}