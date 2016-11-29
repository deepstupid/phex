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
 *  $Id: UpdateCheckRunner.java 4435 2009-04-18 16:47:58Z gregork $
 */
package phex.update;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import phex.common.Environment;
import phex.common.LongObj;
import phex.common.OpenPgpToolkit;
import phex.common.Phex;
import phex.common.PhexVersion;
import phex.event.UpdateNotificationListener;
import phex.gui.common.GUIRegistry;
import phex.prefs.core.ConnectionPrefs;
import phex.prefs.core.PhexCorePrefs;
import phex.prefs.core.StatisticPrefs;
import phex.prefs.core.UpdatePrefs;
import phex.servent.Servent;
import phex.share.SharedFilesService;
import phex.statistic.StatisticProvider;
import phex.statistic.StatisticProviderConstants;
import phex.statistic.StatisticsManager;
import phex.utils.IOUtil;
import phex.utils.Localizer;
import phex.utils.VersionUtils;
import phex.xml.sax.DPhex;
import phex.xml.sax.DUpdateRequest;
import phex.xml.sax.DUpdateResponse;
import phex.xml.sax.XMLBuilder;
import phex.xml.sax.DUpdateResponse.VersionType;

/**
 * The UpdateCheckRunner handles regular update check against the phex website.
 * It is also used to collect Phex statistics to identify possible problem areas
 * with new versions.<br/>
 * Phex informations are transmitted in an XML structure, the update information
 * response is also returned in an XML structure.
 */
public class UpdateCheckRunner implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger( UpdateCheckRunner.class );
    
    private static final String UPDATE_CHECK_URL = "http://www.phex.org/update/update34.php";
    
    // Generate key: 
    // gpg --gen-key
    //     RSA ; does not expire ; "The Phex Team <upd1@phex.org>"
    //
    // Export key for keyserver:
    // gpg -a --export upd1@phex.org
    //
    // Generate revocation cert:
    // gpg --gen-revoke upd1@phex.org
    //
    // Sign:
    // gpg -asb -u upd1@phex.org
    public static final String PUBLIC_KEY_ID = "0x0EE3F089E5CC3925";
    
    private Throwable updateCheckError;
    private UpdateNotificationListener listener;
    private String releaseVersion;
    private String betaVersion;
    private boolean isBetaInfoShown;
    
    public UpdateCheckRunner( UpdateNotificationListener updateListener, boolean showBetaInfo )
    {
        listener = updateListener;
        this.isBetaInfoShown = showBetaInfo;
    }
    
    /**
     * Trigger a automated background update check. This is the standard Phex
     * check done every week to display the update dialog or just collect Phex
     * statistics.
     * The call is not blocking.
     */
    public static void triggerAutoBackgroundCheck(
        final UpdateNotificationListener updateListener, 
        final boolean showBetaInfo )
    {
        if ( UpdatePrefs.LastUpdateCheckTime.get().longValue() >
             System.currentTimeMillis() - DateUtils.MILLIS_PER_DAY * 7 )
        {
            logger.debug( "No update check necessary." );
            return;
        }
        logger.debug( "Triggering update check." );
        UpdateCheckRunner runner = new UpdateCheckRunner( updateListener, showBetaInfo );
        Environment.getInstance().executeOnThreadPool( runner, "UpdateCheckRunner" );
    }
    
    public String getReleaseVersion()
    {
        return releaseVersion;
    }

    public String getBetaVersion()
    {
        return betaVersion;
    }

    /**
     * Returns a possible Throwable that could be thrown during the update check
     * or null if no error was caught.
     * @return a possible Throwable that could be thrown during the update check
     * or null if no error was caught.
     */
    public Throwable getUpdateCheckError()
    {
        return updateCheckError;
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        try
        {            
            performUpdateCheck();
        }
        catch ( Throwable exp )
        {
            updateCheckError = exp;
            logger.warn( exp.toString(), exp );
        }
    }
    
    private void performUpdateCheck()
    {
        URL url;
        DPhex dPhex;
        try
        {
            byte[] data = buildXMLUpdateRequest();
            if ( data == null )
            {
                throw new IOException( "Missing XML update data" );
            }
            if ( logger.isDebugEnabled( ) )
            {
                logger.debug( new String(data) );
            }
            
            url = new URL( UPDATE_CHECK_URL );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setUseCaches( false );
            connection.setRequestProperty( "User-Agent", Phex.getFullPhexVendor() );
            connection.setRequestMethod( "POST" );
            connection.setDoOutput( true );
            connection.setRequestProperty( "Content-Type",
                "text/xml; charset=UTF-8" );
                        
            OutputStream outStream = connection.getOutputStream();
            outStream.write( data );
            
            UpdatePrefs.LastUpdateCheckTime.set( Long.valueOf(System.currentTimeMillis()) );
            
            InputStream inStream = connection.getInputStream();
            UpdateResponseParts parts = createResponseParts( inStream );
            verifySignature( parts );
            dPhex = XMLBuilder.readDPhexFromStream( new ByteArrayInputStream( 
                parts.xml.getBytes( "UTF-8" )) );
        }
        catch ( MalformedURLException exp )
        {
            updateCheckError = exp;
            logger.error( exp.toString(), exp );
            assert false;
            throw new RuntimeException( );
        }
        catch ( UnknownHostException exp )
        {
            // can't find way to host
            // this maybe means we have no internet connection
            updateCheckError = exp;
            logger.warn( exp.toString(), exp );
            return;
        }
        catch ( SocketException exp )
        {
            // can't connect... maybe a proxy is in the way...
            updateCheckError = exp;
            logger.warn( exp.toString(), exp );
            return;
        }
        catch ( IOException exp )
        {
            updateCheckError = exp;
            logger.warn( exp.toString(), exp );
            return;
        }
        finally
        {
            PhexCorePrefs.save( false );
        }
        
        DUpdateResponse response = dPhex.getUpdateResponse();
        List<VersionType> versionList = response.getVersionList();
        VersionType latestReleaseVersion = null;
        VersionType latestBetaVersion = null;
        
        for ( VersionType currentVersion : versionList )
        {
            if ( currentVersion.isBeta() )
            {
                if ( latestBetaVersion == null || VersionUtils.compare(
                    currentVersion.getId(), latestBetaVersion.getId() ) > 0 )
                {
                    latestBetaVersion = currentVersion;
                }
            }
            else
            {
                if ( latestReleaseVersion == null || VersionUtils.compare(
                    currentVersion.getId(), latestReleaseVersion.getId() ) > 0 )
                {
                    latestReleaseVersion = currentVersion;
                }
            }
        }
        
        
        betaVersion = "0";
        releaseVersion = "0";
        if ( latestBetaVersion != null )
        {
            betaVersion = latestBetaVersion.getId();
        }
        if ( latestReleaseVersion != null )
        {
            releaseVersion = latestReleaseVersion.getId();
        }
        
        int releaseCompare = 0;
        int betaCompare = 0;
        betaCompare = VersionUtils.compare( betaVersion,
            PhexVersion.getFullVersion() );
        releaseCompare = VersionUtils.compare( releaseVersion,
            PhexVersion.getFullVersion() );
        
        if ( releaseCompare <= 0 && betaCompare <= 0 )
        {
            return;
        }
        
        betaCompare = VersionUtils.compare( betaVersion, 
            UpdatePrefs.LastBetaUpdateCheckVersion.get() );        
        releaseCompare = VersionUtils.compare( releaseVersion,
            UpdatePrefs.LastUpdateCheckVersion.get() );

        int verDiff = VersionUtils.compare( betaVersion,
            releaseVersion );

        boolean triggerUpdateNotification = false;
        if ( releaseCompare > 0 )
        {
            UpdatePrefs.LastUpdateCheckVersion.set( releaseVersion );
            triggerUpdateNotification = true;
        }
        if ( betaCompare > 0 )
        {
            UpdatePrefs.LastBetaUpdateCheckVersion.set( betaVersion );
            triggerUpdateNotification = true;
        }

        if ( verDiff > 0 )
        {
            // reset release version since beta is more up-to-date
            releaseVersion = null;
        }
        else
        {
            // reset beta version since release is the one to go.
            betaVersion = null;
        }

        if ( triggerUpdateNotification )
        {
            PhexCorePrefs.save( false );
            fireUpdateNotification();
        }
    }
    
    private void verifySignature( UpdateResponseParts parts ) throws IOException
    {
        OpenPgpToolkit pgpKit = new OpenPgpToolkit();
        String keyServer = pgpKit.getRandomKeyserver();
        PGPPublicKey pubKey = pgpKit.lookupKeyById( keyServer, PUBLIC_KEY_ID );
        if ( pubKey.isRevoked() )
        {
            throw new IOException( "Public key revoked" );
        }
        
        PGPObjectFactory pgpFact = new PGPObjectFactory( 
            new ArmoredInputStream( new ByteArrayInputStream( parts.sig.getBytes("US-ASCII") ) ) );
        PGPSignatureList list = (PGPSignatureList) pgpFact.nextObject();
        PGPSignature sig = list.get( 0 );
        try
        {
            sig.initVerify( pubKey, "BC" );
            sig.update( parts.xml.getBytes("UTF-8") );
            if ( !sig.verify() )
            {
                throw new IOException( "Invalid signature." );
            }
        }
        catch (PGPException exp) 
        {
            logger.error( exp.toString(), exp );
            throw new IOException( "PGPException: " + exp.getMessage() );
        }
        catch ( GeneralSecurityException exp )
        {
            logger.error( exp.toString(), exp );
            throw new IOException( exp.toString() );
        }
    }
    
    private void fireUpdateNotification()
    {
        listener.updateNotification( this );
    }
    
    private byte[] buildXMLUpdateRequest()
    {
        Servent servent = Servent.getInstance();
        try
        {
            DPhex dPhex = new DPhex();
            DUpdateRequest dRequest = new DUpdateRequest();
            dPhex.setUpdateRequest( dRequest );
            
            dRequest.setCurrentVersion( PhexVersion.getFullVersion() );
            dRequest.setStartupCount( StatisticPrefs.TotalStartupCounter.get().intValue() );
            dRequest.setLafUsed( GUIRegistry.getInstance().getUsedLAFClass() );
            dRequest.setLanguage( Localizer.getUsedLocale().toString() );
            dRequest.setJavaVersion( System.getProperty( "java.version" ) );
            dRequest.setOperatingSystem( SystemUtils.OS_NAME );
            
            dRequest.setHostid( servent.getServentGuid().toHexString() );
            dRequest.setShowBetaInfo( isBetaInfoShown );
            dRequest.setLastInfoId( UpdatePrefs.LastShownUpdateInfoId.get().intValue() );
            
            String lastCheckVersion;
            if ( VersionUtils.compare( UpdatePrefs.LastUpdateCheckVersion.get(),
                UpdatePrefs.LastBetaUpdateCheckVersion.get() ) > 0 )
            {
                lastCheckVersion = UpdatePrefs.LastUpdateCheckVersion.get();
            }
            else
            {
                lastCheckVersion = UpdatePrefs.LastBetaUpdateCheckVersion.get();
            }
            dRequest.setLastCheckVersion( lastCheckVersion );
            
            // unknown       = 0
            // leaf - forced = 1
            // leaf          = 2 (old)
            // leaf - noFW   = 20
            // leaf - FW     = 21
            // up            = 3
            // up - forced   = 4
            int lupStatus = 0;
            if ( servent.isShieldedLeafNode() )
            {
                if ( ConnectionPrefs.AllowToBecomeUP.get().booleanValue() )
                {
                    lupStatus = servent.isFirewalled() ? 21 : 20;
                }
                else
                {
                    lupStatus = 1;
                }
            }
            else if ( servent.isUltrapeer() )
            {
                if ( ConnectionPrefs.ForceToBeUltrapeer.get().booleanValue() )
                {
                    lupStatus = 4;
                }
                else
                {
                    lupStatus = 3;
                }
            }
            dRequest.setLeafUltrapeerStatus( lupStatus );
            
            StatisticsManager statMgr = servent.getStatisticsService();
            
            StatisticProvider uptimeProvider = statMgr.getStatisticProvider(
                StatisticsManager.UPTIME_PROVIDER );
            dRequest.setAvgUptime(
                ((LongObj)uptimeProvider.getAverageValue()).value );
            
            StatisticProvider dailyUptimeProvider = statMgr.getStatisticProvider(
                StatisticsManager.DAILY_UPTIME_PROVIDER );
            dRequest.setDailyAvgUptime(
                ((Integer)dailyUptimeProvider.getValue()).intValue() );
                
            StatisticProvider downloadProvider = statMgr.getStatisticProvider(
                StatisticProviderConstants.TOTAL_DOWNLOAD_COUNT_PROVIDER );
            dRequest.setDownloadCount(
                (int)((LongObj)downloadProvider.getValue()).value );
                
            StatisticProvider uploadProvider = statMgr.getStatisticProvider(
                StatisticProviderConstants.TOTAL_UPLOAD_COUNT_PROVIDER );
            dRequest.setUploadCount(
                (int)((LongObj)uploadProvider.getValue()).value );
                
            SharedFilesService sharedFilesService = Servent.getInstance().getSharedFilesService();
            dRequest.setSharedFiles( sharedFilesService.getFileCount() );
            dRequest.setSharedSize( sharedFilesService.getTotalFileSizeInKb() );
            
            dRequest.setErrorLog( getErrorLogFileTail() );
            
            return phex.xml.sax.XMLBuilder.serializeToBytes( dPhex );
        }
        catch ( IOException exp )
        {
            logger.error( exp.toString(), exp );
            return null;
        }
    }
    
    private String getErrorLogFileTail()
    {
        try
        {
            File logFile = Environment.getInstance().getPhexConfigFile( "phex.error.log" );
            if ( !logFile.exists() )
            {
                return null;
            }
            RandomAccessFile raf = new RandomAccessFile( logFile, "r" );
            long pos = Math.max( raf.length() - 10 * 1024, 0 );
            raf.seek(pos);
            byte[] buffer = new byte[ (int)Math.min( 10*1024, raf.length() ) ];
            int lenRead = raf.read( buffer );
            return new String( buffer, 0, lenRead );
        }
        catch ( IOException exp )
        {
            logger.error( exp.toString(), exp );
            return exp.toString();
        }
    }
    
    private UpdateResponseParts createResponseParts( InputStream stream ) 
        throws IOException
    {
        InputStreamReader reader = new InputStreamReader( stream, "UTF-8" );
        String dataStr = IOUtil.toString( reader );
        int pgpIdx = dataStr.indexOf( "-BEGIN PGP" );
        while ( dataStr.charAt( pgpIdx ) == '-' && pgpIdx > 0 )
        {
            pgpIdx--;
        }
        int xmlIdx = pgpIdx;
        while ( dataStr.charAt( xmlIdx ) != '>' && pgpIdx > 0 )
        {
            xmlIdx--;
        }
        UpdateResponseParts parts = new UpdateResponseParts();
        parts.xml = dataStr.substring( 0, xmlIdx+1 );
        parts.sig = dataStr.substring( pgpIdx );
        
        logger.debug( parts.xml );
        logger.debug( parts.sig );
        
        return parts;
    }
    
    private class UpdateResponseParts
    {
        String xml;
        String sig;
    }
}