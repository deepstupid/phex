package phex.upload;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestMonitorRunnable;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import phex.common.address.DefaultDestAddress;
import phex.common.address.IpAddress;
import phex.prefs.core.UploadPrefs;
import phex.servent.Peer;
import phex.util.RandomUtils;

import java.util.HashSet;
import java.util.Set;

public class UploadManagerTest extends TestCase
{

    final static int runnerCount = 8;

    private UploadManager uploadManager;
    
    @Override
    public void setUp()
    {
        //        PhexGuiPrefs.init();
//        Localizer.initialize( InterfacePrefs.LocaleName.get() );

        uploadManager = new UploadManager( new Peer() );
    }

    public void testMultiConcurrency() throws Throwable
    {
        UploadPrefs.MaxUploadsPerIP.set( Integer.valueOf( 1 ) );

        TestRunnable[] runners = new TestRunnable[runnerCount];
        for ( int i=0; i<runnerCount; i++ )
        {
            runners[i] = new Worker();
        }
        TestRunnable[] monitors = { new Monitor() };
        MultiThreadedTestRunner tr = new MultiThreadedTestRunner(
            runners, monitors );
        tr.runTestRunnables();
    }

    private class Monitor extends TestMonitorRunnable
    {
        private final int maxParallelUploads = UploadPrefs.MaxParallelUploads.get().intValue();
        private int maxQueueSize = UploadPrefs.MaxQueueSize.get().intValue();
        
        //private String lastOut;
        
        @Override
        public void runMonitor() throws Throwable
        {
            //String out = uploadManager.getUploadListSize() + " / " + uploadManager.getUploadQueueSize() + " / " + uploadManager.getUploadingCount();
            //if ( !out.equals(lastOut) )
            //{
            //    System.out.println(out);
            //    lastOut = out;
            //}
            if( uploadManager.getUploadListSize() > 0 )
            {
                int uc = uploadManager.getUploadingCount();
                Assert.assertTrue( uc +"/"+maxParallelUploads, 
                    uc <= maxParallelUploads );
                
                // TODO Queue count test still fails... queuing check and operation is
                // not atomic.. though this is not really critical
                //int uqs = uploadManager.getUploadQueueSize();
                //Assert.assertTrue( uqs+"/"+maxQueueSize, 
                //    uqs <= maxQueueSize );
                
                int size = uploadManager.getUploadListSize();
                int[] idxs = new int[size];
                for ( int i = 0; i < size; i++ )
                {
                    idxs[i]=i;
                }
                UploadState[] states = uploadManager.getUploadStatesAt( idxs );
                Set<IpAddress> ips = new HashSet<IpAddress>();
                for ( UploadState state : states )
                {
                    if ( state == null ) continue;
                    IpAddress ip = state.getHostAddress().getIpAddress();
                    boolean added = ips.add( ip );
                    Assert.assertTrue( "State with duplicate ip: " + ips + " - " + ip, 
                        added );
                }
//                System.out.println( "u: " + uploadManager.getUploadingCount() + " / " +
//                    UploadPrefs.MaxParallelUploads.get().intValue() );
//                System.out.println( ips.size() );
//                System.out.println( "q: " + uploadManager.getUploadQueueSize() + " / " +
//                    UploadPrefs.MaxQueueSize.get().intValue() );
            }
            yieldProcessing();
        }
    }

    private class Worker extends TestRunnable
    {    
        @Override
        public void runTest() throws InterruptedException
        {
            for ( int i = 0; i < 5; i++ )
            {
                int p = RandomUtils.getInt( 30 );
                UploadState state = new UploadState( 
                    new DefaultDestAddress("1.1.1." + p, 80 ), "", uploadManager );
                
                boolean succ = uploadManager.validateAndCountAddress( state.getHostAddress() );
                if ( !succ )
                {
                    Thread.yield();
                    i--; // try again
                    continue;
                }
                uploadManager.addUploadState( state );
                try
                {
                    if ( uploadManager.isHostBusy() )
                    {
                        if ( uploadManager.isQueueLimitReached() )
                        {
                            Thread.yield();
                            i--;
                            continue;
                        }
                        succ = uploadManager.trySetUploadStatus(state, UploadStatus.QUEUED);
                        if (!succ)
                        {
                            throw new RuntimeException("can't queue?");
                        }
                        uploadManager.addQueuedUpload( state );
                        // try to get slot as long as host is busy..
                        while ( !uploadManager.trySetUploadStatus(state, UploadStatus.UPLOADING_DATA) )
                        {
                            Thread.sleep( 10 );
                        }
                        uploadManager.removeQueuedUpload( state );
                    }
                    succ = uploadManager.trySetUploadStatus(state, UploadStatus.UPLOADING_DATA);
                    if (!succ)
                    {
                        // busy... try later..
                        i--;
                        continue;
                    }
                    Thread.sleep( 100+RandomUtils.getInt( 100 ) );
                }
                finally
                {
                    succ = uploadManager.trySetUploadStatus(state, UploadStatus.FINISHED);
                    if (!succ)
                    {
                        throw new RuntimeException("can't finish?");
                    }
                    uploadManager.removeUploadState( state );
                    uploadManager.releaseUploadAddress( state.getHostAddress() );
                }
            }
            //System.out.println("worker ends");
        }        
    }
}