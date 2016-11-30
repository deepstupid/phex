/*
 *  PHEX - The pure-java Gnutella-servent.
 *  Copyright (C) 2001 - 2006 Phex Development Group
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
 *  --- CVS Information ---
 *  $Id: TestReadWriteLock.java 4273 2008-09-28 16:40:00Z gregork $
 */
package phex.test;

import junit.framework.Assert;
import org.junit.Test;
import phex.utils.ReadWriteLock;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

public class TestReadWriteLock implements UncaughtExceptionHandler
{
    private Throwable uncaughtExp;

    public TestReadWriteLock( )
    {
    }

    @Test
    public void testWriteReadLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    lock.writeLock();
                    lock.readLock();
                    lock.readUnlock();
                    lock.writeUnlock();
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                    Assert.fail( exp.getMessage() );
                }
            }
        };
        Thread thread = new Thread( runner );
        thread.setUncaughtExceptionHandler( this );
        thread.start();
        thread.join( 5 * 1000 );
        handleUncaughtExp();
        if ( thread.isAlive() )
        {
            Assert.fail( "Thread still in locked situation" );
        }
    }

    @Test(expected=RuntimeException.class) 
    public void testReadWriteLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();
        lock.readLock();
        lock.writeLock();
    }

    @Test
    public void testMultiReadLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    lock.readLock();
                    lock.readLock();
                    lock.readLock();
                    lock.readUnlock();
                    lock.readUnlock();
                    lock.readUnlock();
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                    Assert.fail( exp.getMessage() );
                }
            }
        };
        Thread thread = new Thread( runner );
        thread.setUncaughtExceptionHandler( this );
        thread.start();
        thread.join( 5 * 1000 );
        handleUncaughtExp();
        if ( thread.isAlive() )
        {
            Assert.fail( "Thread still in locked situation" );
        }
    }
    
    @Test
    public void testConcurrency()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();
        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    for ( int i = 0; i<20; i++ )
                    {
                        double d = Math.random();
                        if ( d < 0.33 )
                        {
                            lock.readLock();
                            Thread.sleep( 5 );
                            lock.readUnlock();
                        }
                        else if ( d < 0.66 )
                        {
                            lock.writeLock();
                            Thread.sleep( 5 );
                            lock.writeUnlock();
                        }
                        else
                        {
                            lock.writeLock();
                            lock.readLock();
                            Thread.sleep( 5 );
                            lock.readUnlock();
                            lock.writeUnlock();
                        }
                    }
                }
                catch ( Exception exp )
                {
                    throw new RuntimeException( exp );
                }
            }
        };
        List<Thread> tList = new ArrayList<Thread>();
        for ( int i = 0; i < 20; i++ )
        {
            Thread thread = new Thread( runner );
            thread.setUncaughtExceptionHandler( this );
            thread.start();
            tList.add( thread );
        }
        for ( Thread t : tList )
        {
            t.join( 3 * 60 * 1000 );
            handleUncaughtExp();
            if ( t.isAlive() )
            {
                Assert.fail( "Thread still in locked situation" );
            }
        }
    }
    
    private void handleUncaughtExp()
    {
        if ( uncaughtExp != null )
        {
            throw new RuntimeException( uncaughtExp );
        }
    }

    public void uncaughtException(Thread t, Throwable e)
    {
        uncaughtExp = e;
    }

    /*
    // Takes to long to execute in every test...
    public void testLongWaitingLock()
        throws Exception
    {
        final ReadWriteLock lock = new ReadWriteLock();

        Runnable runner = new Runnable()
        {
            public void run()
            {
                try
                {
                    lock.readLock();
                    Thread.sleep( 10 * 60 * 1000 );
                    lock.readUnlock();
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                    fail( exp.getMessage() );
                }
            }
        };
        Thread thread = new Thread( runner );
        thread.start();
        thread.join( 1000 );
        boolean hasExcepted = false;
        try
        {
            lock.writeLock();
            fail( "Expected Exception" );
        }
        catch ( RuntimeException exp )
        {
            hasExcepted = true;
        }
        assertTrue( hasExcepted );
    }
    */
}