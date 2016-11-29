package phex.common;

import junit.framework.TestCase;
import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestMonitorRunnable;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import phex.common.address.DefaultDestAddress;
import phex.utils.AccessUtils;
import phex.utils.RandomUtils;

import java.util.Map;

public class AddressCounterTest extends TestCase
{    
    public void testSingleEntry() throws Throwable
    {
        AddressCounter ac = new AddressCounter( 1, false );
        int i1 = 10;
        TestRunnable[] runners = new TestRunnable[i1];
        for (int i = 0; i< i1; i++ )
        {
            runners[i] = new Worker( ac );
        }
        TestRunnable[] monitors = { new Monitor(ac) };
        MultiThreadedTestRunner tr = new MultiThreadedTestRunner(
            runners, monitors );
        tr.runTestRunnables();
    }
    
    public void testDoubleEntry() throws Throwable
    {
        AddressCounter ac = new AddressCounter( 2, false );
        int i1 = 10;
        TestRunnable[] runners = new TestRunnable[i1];
        for (int i = 0; i< i1; i++ )
        {
            runners[i] = new Worker( ac );
        }
        TestRunnable[] monitors = { new Monitor(ac) };
        MultiThreadedTestRunner tr = new MultiThreadedTestRunner(
            runners, monitors );
        tr.runTestRunnables();
    }
    
    private class Worker extends TestRunnable
    {
        private AddressCounter ac;
        
        Worker( AddressCounter ac )
        {
            this.ac = ac;
        }
        
        @Override
        public void runTest()
        {
            int good = 0;
            int bad = 0;
            int tries = 10000;
            while( good < 1000 && bad < 1000 && tries > 0 )
            {
                tries --;
                int p = RandomUtils.getInt( 10 );
                DefaultDestAddress address = new DefaultDestAddress("1.1.1." + p, 80 );
                boolean succ = ac.validateAndCountAddress( address );
                if ( !succ )
                {
                    bad ++;
                    continue;
                }
                good ++;
                try
                {
                    Thread.sleep( 10+RandomUtils.getInt( 100 ) );
                }
                catch ( InterruptedException exp )
                {
                    exp.printStackTrace();
                }
                ac.relaseAddress( address );
            }
        }        
    }
    
    private class Monitor extends TestMonitorRunnable 
    {
        private final AddressCounter ac;
        private final Map<Object, Integer> map;
        
        @SuppressWarnings("unchecked")
        Monitor( AddressCounter ac )
        {
            this.ac = ac;
            map = (Map<Object, Integer>) AccessUtils.getFieldValue( 
                ac, "addressCountMap" );
        }

        @Override
        public void runMonitor() throws Throwable {
            synchronized ( ac ) {
                for ( Integer val : map.values() ) {
                    assertTrue( val != 0 && val <= ac.getMaxCount() );                       
                }
            }
            yieldProcessing();
        }
        
    }
}
