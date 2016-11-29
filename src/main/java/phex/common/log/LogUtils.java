package phex.common.log;

import java.io.File;

import org.slf4j.LoggerFactory;

import phex.utils.StringUtils;
import phex.utils.SystemProperties;

public class LogUtils
{
    public static void initializeLogging()
    {
        File file = SystemProperties.getPhexConfigRoot();
        
        // Unfortunately we must convert backslash to slash for logback to understand
        // paths correctly, bug??
        String absPath = file.getAbsolutePath();
        absPath = StringUtils.replace( absPath, "\\", "/", -1 );
        System.setProperty( "phex.log.path", absPath );
        
        LoggerFactory.getLogger( LogUtils.class ).debug( "Logging initialized" );
    }
}