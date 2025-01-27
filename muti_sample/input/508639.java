public class CyclicBarrierTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(CyclicBarrierTest.class);
    }
    private volatile int countAction;
    private class MyAction implements Runnable {
        public void run() { ++countAction; }
    }
    public void testConstructor1() {
        try {
            new CyclicBarrier(-1, (Runnable)null);
            shouldThrow();
        } catch(IllegalArgumentException e){}
    }
    public void testConstructor2() {
        try {
            new CyclicBarrier(-1);
            shouldThrow();
        } catch(IllegalArgumentException e){}
    }
    public void testGetParties() {
        CyclicBarrier b = new CyclicBarrier(2);
        assertEquals(2, b.getParties());
        assertEquals(0, b.getNumberWaiting());
    }
    public void testSingleParty() {
        try {
            CyclicBarrier b = new CyclicBarrier(1);
            assertEquals(1, b.getParties());
            assertEquals(0, b.getNumberWaiting());
            b.await();
            b.await();
            assertEquals(0, b.getNumberWaiting());
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testBarrierAction() {
        try {
            countAction = 0;
            CyclicBarrier b = new CyclicBarrier(1, new MyAction());
            assertEquals(1, b.getParties());
            assertEquals(0, b.getNumberWaiting());
            b.await();
            b.await();
            assertEquals(0, b.getNumberWaiting());
            assertEquals(countAction, 2);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testTwoParties() {
        final CyclicBarrier b = new CyclicBarrier(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        b.await();
                        b.await();
                        b.await();
                        b.await();
                    } catch(Exception e){
                        threadUnexpectedException();
                    }}});
        try {
            t.start();
            b.await();
            b.await();
            b.await();
            b.await();
            t.join();
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testAwait1_Interrupted_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();
                    } catch(InterruptedException success){}                
                    catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            t1.interrupt();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    public void testAwait2_Interrupted_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(InterruptedException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            t1.interrupt();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    public void testAwait3_TimeOutException() {
        final CyclicBarrier c = new CyclicBarrier(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            t.join(); 
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    public void testAwait4_Timeout_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    public void testAwait5_Timeout_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await(SHORT_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch(TimeoutException success){
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    public void testReset_BrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();
                    } catch(BrokenBarrierException success){}                
                    catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                        threadShouldThrow();                        
                    } catch(BrokenBarrierException success){
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t1.start();
            t2.start();
            Thread.sleep(SHORT_DELAY_MS);
            c.reset();
            t1.join(); 
            t2.join();
        } catch(InterruptedException e){
            unexpectedException();
        }
    }
    public void testReset_NoBrokenBarrier() {
        final CyclicBarrier c = new CyclicBarrier(3);
        Thread t1 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                    } catch(Exception b){
                        threadUnexpectedException();
                    }
                }
            });
        Thread t2 = new Thread(new Runnable() {
                public void run() {
                    try {
                        c.await();
                    } catch(Exception i){
                        threadUnexpectedException();
                    }
                }
            });
        try {
            c.reset();
            t1.start();
            t2.start();
            c.await();
            t1.join(); 
            t2.join();
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testReset_Leakage() {
        try {
            final CyclicBarrier c = new CyclicBarrier(2);
            final AtomicBoolean done = new AtomicBoolean();
            Thread t = new Thread() {
                    public void run() {
                        while (!done.get()) {
                            try {
                                while (c.isBroken())
                                    c.reset();
                                c.await();
                                threadFail("await should not return");
                            }
                            catch (BrokenBarrierException e) {
                            }
                            catch (InterruptedException ie) {
                            }
                        }
                    }
                };
            t.start();
            for( int i = 0; i < 4; i++) {
                Thread.sleep(SHORT_DELAY_MS);
                t.interrupt();
            }
            done.set(true);
            t.interrupt();
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testResetWithoutBreakage() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = new CyclicBarrier(3);
            for (int i = 0; i < 3; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                barrier.await();
                t1.join();
                t2.join();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                if (i == 1) barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testResetAfterInterrupt() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = new CyclicBarrier(3);
            for (int i = 0; i < 2; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(InterruptedException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                t1.interrupt();
                t1.join();
                t2.join();
                assertTrue(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testResetAfterTimeout() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = new CyclicBarrier(3);
            for (int i = 0; i < 2; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS); }
                            catch(TimeoutException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                t1.join();
                t2.join();
                assertTrue(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testResetAfterCommandException() {
        try {
            final CyclicBarrier start = new CyclicBarrier(3);
            final CyclicBarrier barrier = 
                new CyclicBarrier(3, new Runnable() {
                        public void run() { 
                            throw new NullPointerException(); }});
            for (int i = 0; i < 2; i++) {
                Thread t1 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                Thread t2 = new Thread(new Runnable() {
                        public void run() {
                            try { start.await(); }
                            catch (Exception ie) { 
                                threadFail("start barrier"); 
                            }
                            try { barrier.await(); }
                            catch(BrokenBarrierException ok) {}
                            catch (Throwable thrown) { 
                                unexpectedException(); 
                            }}});
                t1.start();
                t2.start();
                try { start.await(); }
                catch (Exception ie) { threadFail("start barrier"); }
                while (barrier.getNumberWaiting() < 2) { Thread.yield(); }
                try { barrier.await(); }
                catch (Exception ok) { }
                t1.join();
                t2.join();
                assertTrue(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
                barrier.reset();
                assertFalse(barrier.isBroken());
                assertEquals(0, barrier.getNumberWaiting());
            }
        }
        catch (Exception ex) {
            unexpectedException();
        }
    }
}
