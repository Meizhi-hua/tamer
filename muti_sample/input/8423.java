public class DeadlockDetector {
    public static void print(PrintStream tty) {
        print(tty, true);
    }
    public static void print(PrintStream tty, boolean concurrentLocks) {
        tty.println("Deadlock Detection:");
        tty.println();
        int globalDfn = 0, thisDfn;
        int numberOfDeadlocks = 0;
        JavaThread currentThread = null, previousThread = null;
        ObjectMonitor waitingToLockMonitor = null;
        Oop waitingToLockBlocker = null;
        threads = VM.getVM().getThreads();
        heap = VM.getVM().getObjectHeap();
        createThreadTable();
        Iterator i = threadTable.entrySet().iterator();
        while (i.hasNext()) {
            Entry e = (Entry)i.next();
            if (dfn(e) >= 0) {
                continue;
            }
            thisDfn = globalDfn;
            JavaThread thread = (JavaThread)e.getKey();
            previousThread = thread;
            try {
                waitingToLockMonitor = thread.getCurrentPendingMonitor();
            } catch (RuntimeException re) {
                tty.println("This version of HotSpot VM doesn't support deadlock detection.");
                return;
            }
            Klass abstractOwnableSyncKlass = null;
            if (concurrentLocks) {
                waitingToLockBlocker = thread.getCurrentParkBlocker();
                SystemDictionary sysDict = VM.getVM().getSystemDictionary();
                abstractOwnableSyncKlass = sysDict.getAbstractOwnableSynchronizerKlass();
            }
            while (waitingToLockMonitor != null ||
                   waitingToLockBlocker != null) {
                if (waitingToLockMonitor != null) {
                    currentThread = threads.owningThreadFromMonitor(waitingToLockMonitor);
                } else {
                    if (concurrentLocks) {
                        if (waitingToLockBlocker.isA(abstractOwnableSyncKlass)) {
                            Oop threadOop = OopUtilities.abstractOwnableSynchronizerGetOwnerThread(waitingToLockBlocker);
                            if (threadOop != null) {
                                currentThread = OopUtilities.threadOopGetJavaThread(threadOop);
                            }
                        }
                    }
                }
                if (currentThread == null) {
                    break;
                }
                if (dfn(currentThread) < 0) {
                    threadTable.put(currentThread, new Integer(globalDfn++));
                } else if (dfn(currentThread) < thisDfn) {
                    break;
                } else if (currentThread == previousThread) {
                    break;
                } else {
                    numberOfDeadlocks ++;
                    printOneDeadlock(tty, currentThread, concurrentLocks);
                    break;
                }
                previousThread = currentThread;
                waitingToLockMonitor = (ObjectMonitor)currentThread.getCurrentPendingMonitor();
                if (concurrentLocks) {
                    waitingToLockBlocker = currentThread.getCurrentParkBlocker();
                }
            }
        }
        switch (numberOfDeadlocks) {
            case 0:
                tty.println("No deadlocks found.");
                break;
            case 1:
                tty.println("Found a total of 1 deadlock.");
                break;
            default:
                tty.println("Found a total of " + numberOfDeadlocks + " deadlocks.");
                break;
        }
        tty.println();
    }
    private static Threads threads;
    private static ObjectHeap heap;
    private static HashMap threadTable;
    private static void createThreadTable() {
        threadTable = new HashMap();
        for (JavaThread cur = threads.first(); cur != null; cur = cur.next()) {
            threadTable.put(cur, new Integer(-1));
        }
    }
    private static int dfn(JavaThread thread) {
        Object obj = threadTable.get(thread);
        if (obj != null) {
            return ((Integer)obj).intValue();
        }
        return -1;
    }
    private static int dfn(Entry e) {
        return ((Integer)e.getValue()).intValue();
    }
    private static void printOneDeadlock(PrintStream tty, JavaThread thread,
                                         boolean concurrentLocks) {
        tty.println("Found one Java-level deadlock:");
        tty.println("=============================");
        ObjectMonitor waitingToLockMonitor = null;
        Oop waitingToLockBlocker = null;
        JavaThread currentThread = thread;
        do {
            tty.println();
            tty.println("\"" + currentThread.getThreadName() + "\":");
            waitingToLockMonitor = currentThread.getCurrentPendingMonitor();
            if (waitingToLockMonitor != null) {
                tty.print("  waiting to lock Monitor@" + waitingToLockMonitor.getAddress());
                OopHandle obj = waitingToLockMonitor.object();
                Oop oop = heap.newOop(obj);
                if (obj != null) {
                    tty.print(" (Object@");
                    Oop.printOopAddressOn(oop, tty);
                    tty.print(", a " + oop.getKlass().getName().asString() + ")" );
                    tty.print(",\n  which is held by");
                } else {
                    tty.print(" (raw monitor),\n  which is held by");
                }
                currentThread = threads.owningThreadFromMonitor(waitingToLockMonitor);
                tty.print(" \"" + currentThread.getThreadName() + "\"");
            } else if (concurrentLocks) {
                waitingToLockBlocker = currentThread.getCurrentParkBlocker();
                tty.print(" waiting for ownable synchronizer ");
                Oop.printOopAddressOn(waitingToLockBlocker, tty);
                tty.print(", (a " + waitingToLockBlocker.getKlass().getName().asString() + ")" );
                Oop threadOop = OopUtilities.abstractOwnableSynchronizerGetOwnerThread(waitingToLockBlocker);
                currentThread = OopUtilities.threadOopGetJavaThread(threadOop);
                tty.print(",\n which is held by");
                tty.print(" \"" + currentThread.getThreadName() + "\"");
            }
        } while (!currentThread.equals(thread));
        tty.println();
        tty.println();
    }
}
