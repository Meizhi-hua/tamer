class ReferrersFiller {
    static int FILLER_COUNT = 20000;
    static ReferrersFiller[] lotsAndLots = new ReferrersFiller[
                                               ReferrersFiller.FILLER_COUNT];
    int xx;
    ReferrersFiller(int p1) {
        xx = p1;
    }
}
class ReferrersTarg {
    static int TARG_COUNT = 10;
    static ReferrersTarg theReferrersTarg;
    static ReferrersTarg[] allReferrersTargs;
    ReferrersTarg oneReferrersTarg;
    public static void bkpt() {
    }
    public static void main(String[] args) {
        System.out.println("Howdy!");
        for (int ii = 0; ii < ReferrersFiller.lotsAndLots.length; ii++) {
            ReferrersFiller.lotsAndLots[ii] = new ReferrersFiller(ii);
        }
        theReferrersTarg = new ReferrersTarg();
        allReferrersTargs = new ReferrersTarg[ReferrersTarg.TARG_COUNT];
        for (int ii = 0; ii < ReferrersTarg.TARG_COUNT; ii++) {
            allReferrersTargs[ii] = new ReferrersTarg();
            allReferrersTargs[ii].oneReferrersTarg = theReferrersTarg;
        }
        bkpt();
        System.out.println("Goodbye from ReferrersTarg!");
    }
}
public class ReferrersTest extends TestScaffold {
    static String targetName = "ReferrersTarg";
    ReferenceType targetClass;
    ThreadReference mainThread;
    ReferrersTest(String args[]) {
        super(args);
    }
    public static void main(String[] args) throws Exception {
        for (int ii = 0; ii < args.length; ii ++) {
            if (args[ii].startsWith("@@")) {
                targetName = args[ii] = args[ii].substring(2);
                break;
            }
        }
        new ReferrersTest(args).startTests();
    }
    class ToSort implements Comparable<ToSort> {
        long count;
        ReferenceType rt;
        public ToSort(long count, ReferenceType rt) {
            this.count = count;
            this.rt = rt;
        }
        public int compareTo(ToSort obj) {
            if (count < obj.count) return -1;
            if (count == obj.count) return 0;
            return 1;
        }
    }
    protected void runTests() throws Exception {
        int CUT_OFF = 1000;
        BreakpointEvent bpe;
        bpe = startToMain(targetName);
        targetClass = bpe.location().declaringType();
        mainThread = bpe.thread();
        if (targetName.equals("ReferrersTarg")) {
            resumeTo("ReferrersTarg", "bkpt", "()V");
        } else {
            vm().resume();
            try {
                System.err.println("Press <enter> to continue");
                System.in.read();
                System.err.println("running...");
            } catch(Exception e) {
            }
            vm().suspend();
        }
        long start = System.currentTimeMillis();
        List<ReferenceType> allClasses = vm().allClasses();
        long end = System.currentTimeMillis();
        System.out.println( allClasses.size() +
                            " classes from vm.allClasses() took " +
                            (end - start) + " ms");
        long[] counts;
        {
            boolean pass = false;
            try {
                counts = vm().instanceCounts(null);
            } catch (NullPointerException ee) {
                pass = true;
            }
            if (!pass) {
                failure("failure: NullPointerException not thrown on instanceCounts(null)");
            }
        }
        {
            List<ReferenceType>someClasses = new ArrayList(2);
            counts = vm().instanceCounts(someClasses);
            if (counts.length != 0) {
                failure("failure: instanceCounts with a zero length array fails: " +
                        counts.length);
            }
        }
        if (targetClass.name().equals("ReferrersTarg")) {
            List<ObjectReference> noInstances = targetClass.instances(0);
            if (noInstances.size() != ReferrersTarg.TARG_COUNT + 1) {
                failure("failure: instances(0): " + noInstances.size() + ", for " + targetClass);
            }
            noInstances = targetClass.instances(1);
            if (noInstances.size() != 1) {
                failure("failure: instances(1): " + noInstances.size() + ", for " + targetClass);
            }
            boolean pass = false;
            try {
                noInstances = targetClass.instances(-1);
            } catch (IllegalArgumentException ee) {
                pass = true;
            }
            if (!pass) {
                failure("failure: instances(-1) did not get an exception");
            }
        }
        start = System.currentTimeMillis();
        counts = vm().instanceCounts(allClasses);
        end = System.currentTimeMillis();
        if (counts.length == 0) {
            System.out.println("failure: No instances found");
            throw new Exception("ReferrersTest: failed");
        }
        int size = 0;
        List<ToSort> sorted = new ArrayList(allClasses.size());
        for (int ii = 0; ii < allClasses.size(); ii++) {
            size += counts[ii];
            ToSort tos = new ToSort(counts[ii], allClasses.get(ii));
            sorted.add(tos);
        }
        System.out.println("instance counts for " + counts.length +
                           " classes got " + size + " instances and took " +
                            (end - start) + " ms");
        boolean gotReferrersFiller = false;
        boolean gotReferrersTarg = false;
        Collections.sort(sorted);
        for (int ii = sorted.size() - 1; ii >= 0 ; ii--) {
            ToSort xxx = sorted.get(ii);
            if (xxx.rt.name().equals("ReferrersFiller") &&
                xxx.count == ReferrersFiller.FILLER_COUNT) {
                gotReferrersFiller = true;
            }
            if (xxx.rt.name().equals("ReferrersTarg") &&
                xxx.count == ReferrersTarg.TARG_COUNT + 1) {
                gotReferrersTarg = true;
            }
        }
        if (!gotReferrersFiller) {
            failure("failure: Expected " + ReferrersFiller.FILLER_COUNT +
                        " instances of ReferrersFiller");
        }
        if (!gotReferrersTarg) {
            failure("failure: Expected " + (ReferrersTarg.TARG_COUNT + 1) +
                    " instances of ReferrersTarg");
        }
        List<List<ObjectReference>> allInstances = new ArrayList(10);
        if (true) {
            System.out.println("\nGetting instances for one class " +
                               "at a time (limited) in sorted order");
            List<ReferenceType> rtList = new ArrayList(1);
            rtList.add(null);
            long start1 = System.currentTimeMillis();
            size = 0;
            long count = 0;
            for (int ii = sorted.size() - 1; ii >= 0 ; ii--) {
                ToSort xxx = sorted.get(ii);
                if (xxx.count <= CUT_OFF) {
                    break;
                }
                rtList.set(0, xxx.rt);
                start = System.currentTimeMillis();
                List<ObjectReference> oneInstances = xxx.rt.instances(19999999);
                end = System.currentTimeMillis();
                size += oneInstances.size();
                count++;
                System.out.println("Expected " + xxx.count + " instances, got " +
                                   oneInstances.size() +
                                   " instances for " + sorted.get(ii).rt +
                                   " in " + (end - start) + " ms");
                if (xxx.rt.name().equals("ReferrersFiller") &&
                    oneInstances.size() != ReferrersFiller.FILLER_COUNT) {
                    failure("failure: Expected " + ReferrersFiller.FILLER_COUNT +
                            " instances of ReferrersFiller");
                }
                if (xxx.rt.name().equals("ReferrersTarg") &&
                    oneInstances.size() != ReferrersTarg.TARG_COUNT + 1) {
                    failure("failure: Expected " + (ReferrersTarg.TARG_COUNT + 1) +
                            " instances of ReferrersTarg");
                }
                allInstances.add(oneInstances);
            }
            end = System.currentTimeMillis();
            System.out.println(size + " instances via making one vm.instances" +
                               " call for each of " + count +
                               " classes took " + (end - start1) + " ms");
            System.out.println("Per class = " +
                               (end - start) / allClasses.size() + " ms");
        }
        if (targetClass.name().equals("ReferrersTarg")) {
            Field field1 = targetClass.fieldByName("theReferrersTarg");
            ObjectReference anInstance = (ObjectReference)targetClass.getValue(field1);
            List<ObjectReference> noReferrers = anInstance.referringObjects(0);
            if (noReferrers.size() != ReferrersTarg.TARG_COUNT + 1 ) {
                failure("failure: referringObjects(0) got " + noReferrers.size() +
                        ", for " + anInstance);
            }
            noReferrers = anInstance.referringObjects(1);
            if (noReferrers.size() != 1 ) {
                failure("failure: referringObjects(1) got " + noReferrers.size() +
                        ", for " + anInstance);
            }
            boolean pass = false;
            try {
                noReferrers = anInstance.referringObjects(-1);
            } catch (IllegalArgumentException ee) {
                pass = true;
            }
            if (!pass) {
                failure("failure: referringObjects(-1) did not get an exception");
            }
        }
        List<ObjectReference> allReferrers = null;
        List<ObjectReference> someInstances = new ArrayList();
        if (targetName.equals("ReferrersTarg")) {
            Field field1 = targetClass.fieldByName("theReferrersTarg");
            ObjectReference val = (ObjectReference)targetClass.getValue(field1);
            someInstances.add(val);
            allReferrers = val.referringObjects(99999);  
            if (allReferrers.size() != ReferrersTarg.TARG_COUNT + 1) {
                failure("failure: expected " + (ReferrersTarg.TARG_COUNT + 1) +
                        "referrers, but got " + allReferrers.size() +
                        " referrers for " + val);
            }
        } else {
            for (int ii = 0; ii < allClasses.size(); ii++) {
                List<ObjectReference> objRefList = allInstances.get(ii);
                if (objRefList != null) {
                    int asize = objRefList.size();
                    if (false) {
                        System.out.println(asize + ", " + allClasses.get(ii));
                    }
                    if (asize > 0) {
                        someInstances.add(objRefList.get(0));
                    }
                }
            }
        }
        for (ObjectReference objRef: someInstances) {
            start = System.currentTimeMillis();
            if ( true) {
                showReferrers(objRef, 0, 0, 0);
            } else {
                allReferrers = objRef.referringObjects(99999);  
                end = System.currentTimeMillis();
                if (true || allReferrers.size() > 1) {
                    System.out.println( allReferrers.size() + " referrers for " + objRef + " took " + (end - start) + " ms");
                }
            }
        }
        if (!testFailed) {
            println("ReferrersTest: passed");
        } else {
            throw new Exception("ReferrersTest: failed");
        }
    }
    void indent(int level) {
        for (int ii = 0; ii < level; ii++) {
            System.out.print("    ");
        }
    }
    Map<ObjectReference, Object> visited = new HashMap(100);
    void showReferrers(ObjectReference objRef, int level, int total, int which) {
        if (level == 0) {
            visited.clear();
        } else {
            if (visited.containsKey(objRef)) {
                indent(level);
                System.out.println("(" + which + ")" + ":<pruned> " + objRef);
                return;
            }
            visited.put(objRef, null);
            indent(level);
        }
        List<ObjectReference> allReferrers = null;
        long start, end;
        start = System.currentTimeMillis();
        allReferrers = objRef.referringObjects(99999);  
        end = System.currentTimeMillis();
        if (which == 0) {
            System.out.println(allReferrers.size() + " referrers for " + objRef + " took " + (end - start) + " ms");
        } else {
            System.out.println("(" + which + ") "  + objRef);
            indent(level);
            System.out.println("    " + allReferrers.size() + " referrers for " + objRef + " took " + (end - start) + " ms");
        }
        Type rt = objRef.type();
        if (rt instanceof ClassType) {
            ClassType ct = (ClassType)rt;
            String name = ct.name();
            if (name.equals("sun.misc.SoftCache$ValueCell")) {
                return;
            }
            if (name.equals("java.lang.ref.Finalizer")) {
                return;
            }
            if (name.equals("java.lang.ref.SoftReference")) {
                return;
            }
            if (name.indexOf("ClassLoader") >= 0) {
                return;
            }
        }
        int itemNumber = 1;
        int allSize = allReferrers.size();
        for (ObjectReference objx: allReferrers) {
            showReferrers(objx, level + 1, allSize, itemNumber++);
        }
    }
}
