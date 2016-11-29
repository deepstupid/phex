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
 *  $Id: NLogger.java 4266 2008-09-26 16:23:13Z gregork $
 */
package phex.common.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Proxy class for new logging.
 * @deprecated use slf4j directly
 */
@Deprecated
public class NLogger
{
    /**
     * Returns a log instance.
     * @param clazz
     * @return a log instance.
     */
    private static Logger getLogInstance( Class<?> clazz )
    {
        return LoggerFactory.getLogger( clazz );
    }

    
    public static boolean isDebugEnabled( Class<?> clazz )
    {
        return getLogInstance( clazz ).isDebugEnabled();
    }
    
    
    public static boolean isWarnEnabled( Class<?> clazz )
    {
        return getLogInstance( clazz ).isWarnEnabled();
    }
    
    public static void debug( Class<?> clazz, String message )
    {
        getLogInstance( clazz ).debug( message );
    }
    
    public static void debug( Class<?> clazz, Throwable t )
    {
        getLogInstance( clazz ).debug( t.toString(), t );
    }

    
    public static void debug(Class<?> clazz, Object message, Throwable t)
    {
        getLogInstance( clazz ).debug( message.toString(), t );
    }
    
    
    public static void info( Class<?> clazz, String message )
    {
        getLogInstance( clazz ).info( message );
    }

    public static void warn(Class<?> clazz, Object message, Throwable t)
    {
        getLogInstance( clazz ).warn( message.toString(), t );
    }
    
    
    public static void warn(Class<?> clazz, String message )
    {
        getLogInstance( clazz ).warn( message );
    }
    
    public static void warn(Class<?> clazz, Throwable t )
    {
        getLogInstance( clazz ).warn( t.toString(), t );
    }

    public static void error(Class<?> clazz, String message)
    {
        getLogInstance( clazz ).error( message );
    }
    
    public static void error(Class<?> clazz, Throwable t )
    {
        getLogInstance( clazz ).error( t.toString(), t );
    }
    
    public static void error(Class<?> clazz, Object message, Throwable t)
    {
        getLogInstance( clazz ).error( message.toString(), t );
    }
}