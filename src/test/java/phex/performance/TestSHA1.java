/*
 * Created on 24.05.2004
 *
 */
package phex.performance;

import phex.util.bitzi.Base32;
import phex.util.bitzi.SHA1;
import junit.framework.TestCase;
import phex.util.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Performance test for SHA1. Compare standard Java SHA1 calc performance
 * with bitzi performance and check for equal SHA1 results.
 */
public class TestSHA1 extends TestCase
{
    public TestSHA1(String s)
    {
        super(s);
    }
    
    public static void testSHA1()
        throws Exception
    {
        File javahome = SystemUtils.getJavaHome();
        File[] files = javahome.listFiles();
        sha1TestFiles( files, 10 );
    }
    
    public static void sha1TestFiles(File[] files, int max)
        throws Exception
    {
        for (int j = 0; j < files.length; j++)
        {
            if ( files[j].isFile() )
            {
                String result1 = calcSHA1( files[j], new SHA1());
                String result2 = calcSHA1( files[j],
                    MessageDigest.getInstance( "SHA" ) );
                assertTrue( result1.equals( result2 ) );
                if (max-- <= 0)
                    break;
            }
            /*else if ( files[j].isDirectory() )
            {
                sha1TestFiles( files[j].listFiles() );
            }*/

        }
    }
    
    public static String calcSHA1(File file, MessageDigest messageDigest)
        throws Exception
    {  
        FileInputStream inStream = new FileInputStream( file );
        

        byte[] buffer = new byte[64 * 1024];
        int length;
        long start = System.currentTimeMillis();
        while ( (length = inStream.read( buffer ) ) != -1 )
        {
            // TODO2 offer two file scan modes
            long start2 = System.currentTimeMillis();
            messageDigest.update( buffer, 0, length );
            long end2 = System.currentTimeMillis();
            Thread.sleep( (end2 - start2) * 2 );
        }
        inStream.close();
        byte[] shaDigest = messageDigest.digest();
        long end = System.currentTimeMillis();
        System.out.println("Digest: " + messageDigest.getClass()
            + " SHA1 time: " + (end - start)
            + " size: " + file.length() );

        return Base32.encode( shaDigest );
    }
}
