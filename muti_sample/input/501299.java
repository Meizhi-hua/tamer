public class ArrayBlockingQueueTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(ArrayBlockingQueueTest.class);
    }
    private ArrayBlockingQueue populatedQueue(int n) {
        ArrayBlockingQueue q = new ArrayBlockingQueue(n);
        assertTrue(q.isEmpty());
        for(int i = 0; i < n; i++)
            assertTrue(q.offer(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(0, q.remainingCapacity());
        assertEquals(n, q.size());
        return q;
    }
    public void testConstructor1() {
        assertEquals(SIZE, new ArrayBlockingQueue(SIZE).remainingCapacity());
    }
    public void testConstructor2() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(0);
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }
    public void testConstructor3() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(1, true, null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testConstructor4() {
        try {
            Integer[] ints = new Integer[SIZE];
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE, false, Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testConstructor5() {
        try {
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new Integer(i);
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE, false, Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testConstructor6() {
        try {
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new Integer(i);
            ArrayBlockingQueue q = new ArrayBlockingQueue(1, false, Arrays.asList(ints));
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }
    public void testConstructor7() {
        try {
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new Integer(i);
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE, true, Arrays.asList(ints));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }
    public void testEmptyFull() {
        ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        assertTrue(q.isEmpty());
        assertEquals(2, q.remainingCapacity());
        q.add(one);
        assertFalse(q.isEmpty());
        q.add(two);
        assertFalse(q.isEmpty());
        assertEquals(0, q.remainingCapacity());
        assertFalse(q.offer(three));
    }
    public void testRemainingCapacity() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.remainingCapacity());
            assertEquals(SIZE-i, q.size());
            q.remove();
        }
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(SIZE-i, q.remainingCapacity());
            assertEquals(i, q.size());
            q.add(new Integer(i));
        }
    }
    public void testOfferNull() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(1);
            q.offer(null);
            shouldThrow();
        } catch (NullPointerException success) { }   
    }
    public void testAddNull() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(1);
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) { }   
    }
    public void testOffer() {
        ArrayBlockingQueue q = new ArrayBlockingQueue(1);
        assertTrue(q.offer(zero));
        assertFalse(q.offer(one));
    }
    public void testAdd() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
            for (int i = 0; i < SIZE; ++i) {
                assertTrue(q.add(new Integer(i)));
            }
            assertEquals(0, q.remainingCapacity());
            q.add(new Integer(SIZE));
        } catch (IllegalStateException success){
        }   
    }
    public void testAddAll1() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(1);
            q.addAll(null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAllSelf() {
        try {
            ArrayBlockingQueue q = populatedQueue(SIZE);
            q.addAll(q);
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }
    public void testAddAll2() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
            Integer[] ints = new Integer[SIZE];
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAll3() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAll4() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(1);
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (IllegalStateException success) {}
    }
    public void testAddAll5() {
        try {
            Integer[] empty = new Integer[0];
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new Integer(i);
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
            assertFalse(q.addAll(Arrays.asList(empty)));
            assertTrue(q.addAll(Arrays.asList(ints)));
            for (int i = 0; i < SIZE; ++i)
                assertEquals(ints[i], q.poll());
        }
        finally {}
    }
     public void testPutNull() {
        try {
            ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
            q.put(null);
            shouldThrow();
        } 
        catch (NullPointerException success){
        }   
        catch (InterruptedException ie) {
            unexpectedException();
        }
     }
     public void testPut() {
         try {
             ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
             for (int i = 0; i < SIZE; ++i) {
                 Integer I = new Integer(i);
                 q.put(I);
                 assertTrue(q.contains(I));
             }
             assertEquals(0, q.remainingCapacity());
         }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testBlockingPut() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        for (int i = 0; i < SIZE; ++i) {
                            q.put(new Integer(i));
                            ++added;
                        }
                        q.put(new Integer(SIZE));
                        threadShouldThrow();
                    } catch (InterruptedException ie){
                        threadAssertEquals(added, SIZE);
                    }   
                }});
        try { 
            t.start();
           Thread.sleep(MEDIUM_DELAY_MS); 
           t.interrupt();
           t.join();
        }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testPutWithTake() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    int added = 0;
                    try {
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        q.put(new Object());
                        ++added;
                        threadShouldThrow();
                    } catch (InterruptedException e){
                        threadAssertTrue(added >= 2);
                    }
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            q.take();
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testTimedOffer() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Object());
                        q.put(new Object());
                        threadAssertFalse(q.offer(new Object(), SHORT_DELAY_MS/2, TimeUnit.MILLISECONDS));
                        q.offer(new Object(), LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch (InterruptedException success){}
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testTake() {
        try {
            ArrayBlockingQueue q = populatedQueue(SIZE);
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(i, ((Integer)q.take()).intValue());
            }
        } catch (InterruptedException e){
            unexpectedException();
        }   
    }
    public void testTakeFromEmpty() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.take();
                        threadShouldThrow();
                    } catch (InterruptedException success){ }                
                }
            });
        try {
            t.start();
            Thread.sleep(SHORT_DELAY_MS);
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }
    public void testBlockingTake() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        ArrayBlockingQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(i, ((Integer)q.take()).intValue());
                        }
                        q.take();
                        threadShouldThrow();
                    } catch (InterruptedException success){
                    }   
                }});
        try { 
            t.start();
            Thread.sleep(SHORT_DELAY_MS); 
            t.interrupt();
            t.join();
        }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testPoll() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.poll()).intValue());
        }
        assertNull(q.poll());
    }
    public void testTimedPoll0() {
        try {
            ArrayBlockingQueue q = populatedQueue(SIZE);
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(i, ((Integer)q.poll(0, TimeUnit.MILLISECONDS)).intValue());
            }
            assertNull(q.poll(0, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
            unexpectedException();
        }   
    }
    public void testTimedPoll() {
        try {
            ArrayBlockingQueue q = populatedQueue(SIZE);
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(i, ((Integer)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
            }
            assertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException e){
            unexpectedException();
        }   
    }
    public void testInterruptedTimedPoll() {
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        ArrayBlockingQueue q = populatedQueue(SIZE);
                        for (int i = 0; i < SIZE; ++i) {
                            threadAssertEquals(i, ((Integer)q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS)).intValue());
                        }
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException success){
                    }   
                }});
        try { 
            t.start();
            Thread.sleep(SHORT_DELAY_MS); 
            t.interrupt();
            t.join();
        }
        catch (InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testTimedPollWithOffer() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        threadAssertNull(q.poll(SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        q.poll(LONG_DELAY_MS, TimeUnit.MILLISECONDS);
                        threadShouldThrow();
                    } catch (InterruptedException success) { }                
                }
            });
        try {
            t.start();
            Thread.sleep(SMALL_DELAY_MS);
            assertTrue(q.offer(zero, SHORT_DELAY_MS, TimeUnit.MILLISECONDS));
            t.interrupt();
            t.join();
        } catch (Exception e){
            unexpectedException();
        }
    }  
    public void testPeek() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.peek()).intValue());
            q.poll();
            assertTrue(q.peek() == null ||
                       i != ((Integer)q.peek()).intValue());
        }
        assertNull(q.peek());
    }
    public void testElement() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.element()).intValue());
            q.poll();
        }
        try {
            q.element();
            shouldThrow();
        }
        catch (NoSuchElementException success) {}
    }
    public void testRemove() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, ((Integer)q.remove()).intValue());
        }
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success){
        }   
    }
    public void testRemoveElement() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        for (int i = 1; i < SIZE; i+=2) {
            assertTrue(q.remove(new Integer(i)));
        }
        for (int i = 0; i < SIZE; i+=2) {
            assertTrue(q.remove(new Integer(i)));
            assertFalse(q.remove(new Integer(i+1)));
        }
        assertTrue(q.isEmpty());
    }
    public void testContains() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.contains(new Integer(i)));
            q.poll();
            assertFalse(q.contains(new Integer(i)));
        }
    }
    public void testClear() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        q.clear();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        assertEquals(SIZE, q.remainingCapacity());
        q.add(one);
        assertFalse(q.isEmpty());
        assertTrue(q.contains(one));
        q.clear();
        assertTrue(q.isEmpty());
    }
    public void testContainsAll() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        ArrayBlockingQueue p = new ArrayBlockingQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.containsAll(p));
            assertFalse(p.containsAll(q));
            p.add(new Integer(i));
        }
        assertTrue(p.containsAll(q));
    }
    public void testRetainAll() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        ArrayBlockingQueue p = populatedQueue(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            boolean changed = q.retainAll(p);
            if (i == 0)
                assertFalse(changed);
            else
                assertTrue(changed);
            assertTrue(q.containsAll(p));
            assertEquals(SIZE-i, q.size());
            p.remove();
        }
    }
    public void testRemoveAll() {
        for (int i = 1; i < SIZE; ++i) {
            ArrayBlockingQueue q = populatedQueue(SIZE);
            ArrayBlockingQueue p = populatedQueue(i);
            assertTrue(q.removeAll(p));
            assertEquals(SIZE-i, q.size());
            for (int j = 0; j < i; ++j) {
                Integer I = (Integer)(p.remove());
                assertFalse(q.contains(I));
            }
        }
    }
    public void testToArray() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        Object[] o = q.toArray();
        try {
        for(int i = 0; i < o.length; i++)
            assertEquals(o[i], q.take());
        } catch (InterruptedException e){
            unexpectedException();
        }    
    }
    public void testToArray2() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        Integer[] ints = new Integer[SIZE];
        ints = (Integer[])q.toArray(ints);
        try {
            for(int i = 0; i < ints.length; i++)
                assertEquals(ints[i], q.take());
        } catch (InterruptedException e){
            unexpectedException();
        }    
    }
    public void testToArray_BadArg() {
        try {
            ArrayBlockingQueue q = populatedQueue(SIZE);
            Object o[] = q.toArray(null);
            shouldThrow();
        } catch(NullPointerException success){}
    }
    public void testToArray1_BadArg() {
        try {
            ArrayBlockingQueue q = populatedQueue(SIZE);
            Object o[] = q.toArray(new String[10] );
            shouldThrow();
        } catch(ArrayStoreException  success){}
    }
    public void testIterator() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        Iterator it = q.iterator();
        try {
            while(it.hasNext()){
                assertEquals(it.next(), q.take());
            }
        } catch (InterruptedException e){
            unexpectedException();
        }    
    }
    public void testIteratorRemove () {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(3);
        q.add(two);
        q.add(one);
        q.add(three);
        Iterator it = q.iterator();
        it.next();
        it.remove();
        it = q.iterator();
        assertEquals(it.next(), one);
        assertEquals(it.next(), three);
        assertFalse(it.hasNext());
    }
    public void testIteratorOrdering() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(3);
        q.add(one);
        q.add(two);
        q.add(three);
        assertEquals("queue should be full", 0, q.remainingCapacity());
        int k = 0;
        for (Iterator it = q.iterator(); it.hasNext();) {
            int i = ((Integer)(it.next())).intValue();
            assertEquals(++k, i);
        }
        assertEquals(3, k);
    }
    public void testWeaklyConsistentIteration () {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(3);
        q.add(one);
        q.add(two);
        q.add(three);
        try {
            for (Iterator it = q.iterator(); it.hasNext();) {
                q.remove();
                it.next();
            }
        }
        catch (ConcurrentModificationException e) {
            unexpectedException();
        }
        assertEquals(0, q.size());
    }
    public void testToString() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        String s = q.toString();
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }        
    public void testOfferInExecutor() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        q.add(one);
        q.add(two);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new Runnable() {
            public void run() {
                threadAssertFalse(q.offer(three));
                try {
                    threadAssertTrue(q.offer(three, MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS));
                    threadAssertEquals(0, q.remainingCapacity());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SMALL_DELAY_MS);
                    threadAssertEquals(one, q.take());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        joinPool(executor);
    }
    public void testPollInExecutor() {
        final ArrayBlockingQueue q = new ArrayBlockingQueue(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new Runnable() {
            public void run() {
                threadAssertNull(q.poll());
                try {
                    threadAssertTrue(null != q.poll(MEDIUM_DELAY_MS, TimeUnit.MILLISECONDS));
                    threadAssertTrue(q.isEmpty());
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        executor.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(SMALL_DELAY_MS);
                    q.put(one);
                }
                catch (InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        });
        joinPool(executor);
    }
    public void testSerialization() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            ArrayBlockingQueue r = (ArrayBlockingQueue)in.readObject();
            assertEquals(q.size(), r.size());
            while (!q.isEmpty()) 
                assertEquals(q.remove(), r.remove());
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testDrainToNull() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(null);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }
    public void testDrainToSelf() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(q);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }
    public void testDrainTo() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        ArrayList l = new ArrayList();
        q.drainTo(l);
        assertEquals(q.size(), 0);
        assertEquals(l.size(), SIZE);
        for (int i = 0; i < SIZE; ++i) 
            assertEquals(l.get(i), new Integer(i));
        q.add(zero);
        q.add(one);
        assertFalse(q.isEmpty());
        assertTrue(q.contains(zero));
        assertTrue(q.contains(one));
        l.clear();
        q.drainTo(l);
        assertEquals(q.size(), 0);
        assertEquals(l.size(), 2);
        for (int i = 0; i < 2; ++i) 
            assertEquals(l.get(i), new Integer(i));
    }
    public void testDrainToWithActivePut() {
        final ArrayBlockingQueue q = populatedQueue(SIZE);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        q.put(new Integer(SIZE+1));
                    } catch (InterruptedException ie){ 
                        threadUnexpectedException();
                    }
                }
            });
        try {
            t.start();
            ArrayList l = new ArrayList();
            q.drainTo(l);
            assertTrue(l.size() >= SIZE);
            for (int i = 0; i < SIZE; ++i) 
                assertEquals(l.get(i), new Integer(i));
            t.join();
            assertTrue(q.size() + l.size() >= SIZE);
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testDrainToNullN() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(null, 0);
            shouldThrow();
        } catch(NullPointerException success) {
        }
    }
    public void testDrainToSelfN() {
        ArrayBlockingQueue q = populatedQueue(SIZE);
        try {
            q.drainTo(q, 0);
            shouldThrow();
        } catch(IllegalArgumentException success) {
        }
    }
    public void testDrainToN() {
        ArrayBlockingQueue q = new ArrayBlockingQueue(SIZE*2);
        for (int i = 0; i < SIZE + 2; ++i) {
            for(int j = 0; j < SIZE; j++)
                assertTrue(q.offer(new Integer(j)));
            ArrayList l = new ArrayList();
            q.drainTo(l, i);
            int k = (i < SIZE)? i : SIZE;
            assertEquals(l.size(), k);
            assertEquals(q.size(), SIZE-k);
            for (int j = 0; j < k; ++j) 
                assertEquals(l.get(j), new Integer(j));
            while (q.poll() != null) ;
        }
    }
}
