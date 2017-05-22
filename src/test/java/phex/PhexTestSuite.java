/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2007 Phex Development Group
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
 *  $Id: PhexTestSuite.java 4273 2008-09-28 16:40:00Z gregork $
 */
package phex;

import junit.framework.Test;
import junit.framework.TestSuite;
import phex.common.AltLocContainerTest;
import phex.util.IOUtilTest;
import phex.util.Localizer;
import phex.util.SystemProperties;
import phex.xml.thex.TestThexHashTreeSaxHandler;
import phex.xml.thex.ThexHashTreeCodecTest;

import java.io.File;

import static phex.performance.PhexPerformanceSuite.tempPath;


public class PhexTestSuite extends TestSuite
{

    public PhexTestSuite(String s)
    {
        super(s);
    }

    protected static void setUp()
        throws Exception
    {
        StringBuffer path = tempPath();

        //phex config files are hidden on all UNIX systems (also MacOSX. Since
        //there are many UNIX like operation systems with Java support out there,
        //we can not recognize the OS through it's name. Thus we check if the
        //root of the filesystem starts with "/" since only UNIX uses such
        //filesystem conventions
        if ( File.separatorChar == '/' )
        {
            path.append ('.');
        }
        path.append ("phex");
        path.append( File.separator );
        path.append( "testSuite" );
        Localizer.initialize( "en_US" );
        System.setProperty( SystemProperties.PHEX_CONFIG_PATH_SYSPROP, 
            path.toString() );

    }

    public static Test suite()
        throws Exception
    {
        PhexTestSuite suite = new PhexTestSuite("PhexTestSuite");
        PhexTestSuite.setUp();
        suite.addTestSuite(IOUtilTest.class);
        suite.addTestSuite(ThexHashTreeCodecTest.class);
        suite.addTestSuite(TestThexHashTreeSaxHandler.class);
        suite.addTestSuite(TestHTTPRangeSet.class);
        suite.addTestSuite(TestConnection.class);
        suite.addTestSuite(TestDownloadScopeList.class);
        suite.addTestSuite(TestRatedDownloadScopeList.class);
        suite.addTestSuite(TestLogBuffer.class);
        suite.addTestSuite(TestMagmaParser.class);
        suite.addTestSuite(TestStringUtils.class);
        suite.addTestSuite(TestIp2CountryManager.class);
        suite.addTestSuite(TestFileUtils.class);
        suite.addTestSuite(TestAlternateLocation.class);
        suite.addTestSuite(AltLocContainerTest.class);
        suite.addTestSuite(TestSWDownloadCandidate.class);
        suite.addTestSuite(TestCatchedHostCache.class);
        suite.addTestSuite(TestGGEPBlock.class);
        suite.addTestSuite(TestURN.class);
        suite.addTestSuite(phex.msg.PongMsgTest.class);
        suite.addTestSuite(TestHTTPProcessor.class);
        suite.addTestSuite(TestHostAddress.class);
        suite.addTestSuite(TestXQueueParameters.class);
        suite.addTestSuite(phex.msghandling.MessageRoutingTest.class);
        suite.addTestSuite(TestGUID.class);
        suite.addTestSuite(TestQueryRoutingTable.class);
        suite.addTestSuite(phex.msg.QueryResponseMsgTest.class);
        suite.addTestSuite(phex.msg.QueryMsgTest.class);
        suite.addTestSuite(TestCircularQueue.class);
        suite.addTestSuite(TestDownload.class);

        
        //suite.addTestSuite(phex.test.TestThrottleController.class);
        //suite.addTestSuite(phex.test.TestUpdateChecker.class);

        return suite;
    }
}
