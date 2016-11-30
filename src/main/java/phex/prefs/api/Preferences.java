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
 *  $Id: Preferences.java 4524 2011-06-27 10:04:38Z gregork $
 */
package phex.prefs.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.collections.SortedProperties;
import phex.event.PhexEventService;
import phex.event.PhexEventTopics;
import phex.utils.FileUtils;
import phex.utils.IOUtil;

import java.io.*;
import java.util.*;

public class Preferences
{
    private static final Logger logger = LoggerFactory.getLogger( Preferences.class );
    
    private PhexEventService eventService;
    private Map<String, Setting<?>> settingMap;
    private File prefFile;
    private Properties valueProperties;
    private boolean isSaveRequired;
    
    public Preferences( File file )
    {
        this( file, null );
    }
    
    public Preferences( File file, PhexEventService eventService )
    {
        prefFile = file;
        this.eventService = eventService;
        settingMap = new HashMap<String, Setting<?>>();
        valueProperties = new Properties();
    }
    
    public void setEventService( PhexEventService eventService )
    {
        this.eventService = eventService;
    }

    protected String getLoadedProperty( String name )
    {
        return valueProperties.getProperty( name );
    }
    
    protected List<String> getPrefixedPropertyNames( String prefix )
    {
        List<String> found = new ArrayList<String>();
        Set<Object> keys = valueProperties.keySet();
        for ( Object keyObj : keys )
        {
            String key = (String)keyObj;
            if ( key.startsWith( prefix ) )
            {
                found.add( key );
            }
        }
        return found;
    }
    
    protected void registerSetting( String name, Setting<?> setting )
    {
        settingMap.put( name, setting );
    }
    
    public synchronized void saveRequiredNotify()
    {
        isSaveRequired = true;
    }

    protected void fireSettingChanged( SettingChangedEvent<?> event )
    {
        saveRequiredNotify();
        if ( eventService != null )
        {
            eventService.publish( PhexEventTopics.Prefs_Changed, event );
        }
    }

    public synchronized void load()
    {
        Properties loadProperties = new Properties();
        InputStream inStream = null;
        try
        {
            inStream = new BufferedInputStream( new FileInputStream( prefFile ) );
            loadProperties.load( inStream );
        }
        catch ( IOException exp )
        {
            IOUtil.closeQuietly( inStream );
            if ( !(exp instanceof FileNotFoundException) )
            {
                logger.error( exp.toString(), exp );
            }
            // There was a problem loading the properties file.. try to load a
            // possible backup...
            File bakFile = new File( prefFile.getParentFile(),
                prefFile.getName() + ".bak" );
            try
            {
                inStream = new BufferedInputStream( new FileInputStream( bakFile ) );
                loadProperties.load( inStream );
            }
            catch ( FileNotFoundException exp2 )
            {/* ignore */ }
            catch ( IOException exp2 )
            {
                logger.error( exp.toString(), exp );
            }
        }
        finally
        {
            IOUtil.closeQuietly( inStream );
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
    
    public synchronized void save()
    {
        if ( !isSaveRequired )
        {
            logger.debug( "No saving of preferences required." );
            return;
        }
        logger.debug( "Saving preferences to: " + prefFile.getAbsolutePath() );
        Properties saveProperties = new SortedProperties();
        
        for ( Setting<?> setting : settingMap.values() )
        {
            if ( setting.isDefault() && !setting.isAlwaysSaved() )
            {
                continue;
            }
            PreferencesCodec.serializeSetting( setting, saveProperties );
        }
        
        File bakFile = new File( prefFile.getParentFile(), prefFile.getName() + ".bak" );
        try
        {
            // make a backup of old pref File.
            if ( prefFile.exists() )
            {
                FileUtils.copyFile( prefFile, bakFile );
            }
            
            // create a new pref file.
            OutputStream os = null;
            try
            {
                os = new BufferedOutputStream( new FileOutputStream( prefFile ) );
                saveProperties.store( os, "Phex Preferences" );
            }
            finally
            {
                IOUtil.closeQuietly( os );
            }
        }
        catch (IOException exp )
        {
            logger.error( exp.toString(), exp );
        }
    }
}