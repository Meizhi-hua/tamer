public class TelephonyUtilsTest extends TestCase {
    @SmallTest
    public void testRetryManagerEmpty() throws Exception {
        RetryManager rm = new RetryManager();
        assertEquals(0, rm.getRetryCount());
        assertFalse(rm.isRetryForever());
        assertFalse(rm.isRetryNeeded());
        assertEquals(0, rm.getRetryCount());
        assertEquals(0, rm.getRetryTimer());
        rm.increaseRetryCount();
        assertFalse(rm.isRetryForever());
        assertFalse(rm.isRetryNeeded());
        assertEquals(0, rm.getRetryCount());
        assertEquals(0, rm.getRetryTimer());
        rm.setRetryCount(123);
        assertFalse(rm.isRetryForever());
        assertFalse(rm.isRetryNeeded());
        assertEquals(0, rm.getRetryCount());
        assertEquals(0, rm.getRetryTimer());
        rm.retryForeverUsingLastTimeout();
        assertTrue(rm.isRetryForever());
        assertTrue(rm.isRetryNeeded());
        assertEquals(0, rm.getRetryCount());
        assertEquals(0, rm.getRetryTimer());
        rm.setRetryCount(2);
        assertFalse(rm.isRetryForever());
        assertFalse(rm.isRetryNeeded());
        assertEquals(0, rm.getRetryCount());
        assertEquals(0, rm.getRetryTimer());
    }
    @SmallTest
    public void testRetryManagerSimplest() throws Exception {
        RetryManager rm = new RetryManager();
        assertTrue(rm.configure(1, 500, 10));
        int loops = 10;
        int count = 0;
        for (int i = 0; i < loops; i++) {
            assertTrue(rm.isRetryNeeded());
            int time = rm.getRetryTimer();
            assertTrue((time >= 500) && (time < 600));
            if (time == 500) {
                count++;
            }
        }
        assertFalse(count == loops);
        rm.increaseRetryCount();
        assertFalse(rm.isRetryNeeded());
        rm.setRetryCount(0);
        assertTrue(rm.isRetryNeeded());
    }
    @SmallTest
    public void testRetryManagerSimple() throws Exception {
        RetryManager rm = new RetryManager();
        assertTrue(rm.configure(3, 1000, 0));
        assertTrue(rm.isRetryNeeded());
        assertEquals(1000, rm.getRetryTimer());
        assertEquals(rm.getRetryTimer(), 1000);
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        assertEquals(1000, rm.getRetryTimer());
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        assertEquals(1000, rm.getRetryTimer());
        rm.increaseRetryCount();
        assertFalse(rm.isRetryNeeded());
        assertEquals(1000, rm.getRetryTimer());
    }
    @SmallTest
    public void testRetryManageSimpleString() throws Exception {
        RetryManager rm = new RetryManager();
        assertTrue(rm.configure("101"));
        assertTrue(rm.isRetryNeeded());
        assertEquals(101, rm.getRetryTimer());
        rm.increaseRetryCount();
        assertFalse(rm.isRetryNeeded());
    }
    @SmallTest
    public void testRetryManageInfinite() throws Exception {
        RetryManager rm = new RetryManager();
        assertTrue(rm.configure("1000,2000,3000,max_retries=infinite"));
        assertTrue(rm.isRetryNeeded());
        assertEquals(1000, rm.getRetryTimer());
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        assertEquals(2000, rm.getRetryTimer());
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        for (int i=0; i < 100; i++) {
            assertEquals(3000, rm.getRetryTimer());
            rm.increaseRetryCount();
            assertTrue(rm.isRetryNeeded());
        }
    }
    @SmallTest
    public void testRetryManageString() throws Exception {
        RetryManager rm = new RetryManager();
        int time;
        assertTrue(rm.configure(
                "\"max_retries=4, default_randomization=100,1000, 2000 :200 , 3000\""));
        assertTrue(rm.isRetryNeeded());
        time = rm.getRetryTimer();
        assertTrue((time >= 1000) && (time < 1100));
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        time = rm.getRetryTimer();
        assertTrue((time >= 2000) && (time < 2200));
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        time = rm.getRetryTimer();
        assertTrue((time >= 3000) && (time < 3100));
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        time = rm.getRetryTimer();
        assertTrue((time >= 3000) && (time < 3100));
        rm.increaseRetryCount();
        assertFalse(rm.isRetryNeeded());
    }
    @SmallTest
    public void testRetryManageForever() throws Exception {
        RetryManager rm = new RetryManager();
        int time;
        assertTrue(rm.configure("1000, 2000, 3000"));
        assertTrue(rm.isRetryNeeded());
        assertFalse(rm.isRetryForever());
        assertEquals(0, rm.getRetryCount());
        assertEquals(1000, rm.getRetryTimer());
        rm.retryForeverUsingLastTimeout();
        rm.increaseRetryCount();
        rm.increaseRetryCount();
        rm.increaseRetryCount();
        assertTrue(rm.isRetryNeeded());
        assertTrue(rm.isRetryForever());
        assertEquals(3, rm.getRetryCount());
        assertEquals(3000, rm.getRetryTimer());
        rm.setRetryCount(1);
        assertTrue(rm.isRetryNeeded());
        assertFalse(rm.isRetryForever());
        assertEquals(1, rm.getRetryCount());
        assertEquals(2000, rm.getRetryTimer());
        rm.retryForeverUsingLastTimeout();
        assertTrue(rm.isRetryNeeded());
        assertTrue(rm.isRetryForever());
        rm.resetRetryCount();
        assertTrue(rm.isRetryNeeded());
        assertFalse(rm.isRetryForever());
        assertEquals(0, rm.getRetryCount());
        assertEquals(1000, rm.getRetryTimer());
    }
}
