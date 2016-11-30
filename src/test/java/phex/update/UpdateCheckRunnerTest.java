package phex.update;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phex.common.OpenPgpToolkit;
import phex.common.Phex;
import phex.gui.prefs.InterfacePrefs;
import phex.gui.prefs.PhexGuiPrefs;
import phex.prefs.core.PhexCorePrefs;
import phex.servent.Servent;
import phex.utils.Localizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
public class UpdateCheckRunnerTest extends TestCase
{
    private static final Logger logger = LoggerFactory.getLogger( UpdateCheckRunnerTest.class );
    public void setUp()
    {
        Phex.initialize();
        PhexCorePrefs.init();
        PhexGuiPrefs.init();
        Localizer.initialize( InterfacePrefs.LocaleName.get() );
        Servent.getInstance();
    }
    
    public void testPublicKeyAvailability()
    {
        OpenPgpToolkit pgpKit = new OpenPgpToolkit();
        List<String> keyServerList = pgpKit.getKeyserverList();
        Map<String, IOException> failedServers = new HashMap<String, IOException>();
        for ( String keyserver : keyServerList )
        {
            logger.debug( "Testing: " + keyserver );
            try
            {
                PGPPublicKey key = pgpKit.lookupKeyById( keyserver, 
                    UpdateCheckRunner.PUBLIC_KEY_ID );
                Assert.assertFalse( key.isRevoked() );
                logger.debug( "Good: " + keyserver );
            }
            catch ( IOException exp )
            {
                logger.error( exp.toString() );
                logger.warn( "Failed: " + keyserver );
                failedServers.put( keyserver, exp );
            }
        }
        if ( failedServers.size() >= keyServerList.size()/2.0 )
        {
            Assert.fail( "Failed to reach: " + failedServers.toString() );
        }
    }
}