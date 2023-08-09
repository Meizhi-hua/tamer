public class CopyMethods {
    static int muzzle;  
    static int maxLen = 40;  
    static int shortStepsNear = 4;  
    static int downShift = 3;
    static int testCasesRun = 0;
    static long consing = 0;
    static void simpleTests() {
        int[] a = (int[]) makeArray(3, int.class);
        if (muzzle == 0)
            System.out.println("int[] a = "+Arrays.toString(a));
        check(a.length == 3);
        check(a[0] == testValues[0]);
        check(a[1] == testValues[1]);
        check(a[2] == testValues[2]);
        checkArray(a, int.class, 3, 0, 3);
        for (int bad = -2; bad < a.length; bad++) {
            try {
                int[] aa = a.clone();
                if (bad < 0)  aa = new int[4];
                else          aa[bad] = 0;
                ++muzzle;
                if (bad == -2)
                    checkArray(new String[3], int.class, 0, 0, a.length);
                else
                    checkArray(aa, int.class, 0, 0, a.length);
                throw new Error("Should Not Reach Here");
            } catch (RuntimeException ee) {
                --muzzle;
                if (muzzle == 0)
                    System.out.println("Expected: "+ee);
            }
        }
        checkArray(Arrays.copyOf(a, 0), int.class, 0, 0, 3);
        checkArray(Arrays.copyOf(a, 1), int.class, 1, 0, 3);
        checkArray(Arrays.copyOf(a, 2), int.class, 2, 0, 3);
        checkArray(Arrays.copyOf(a, 3), int.class, 3, 0, 3);
        checkArray(Arrays.copyOf(a, 4), int.class, 4, 0, 3);
        int[] ar = Arrays.copyOfRange(a, 1, 3);
        check(ar.length == 2);
        check(ar[0] == a[1]);
        check(ar[1] == a[2]);
        checkArray(ar, int.class, 2, 1, 2);
        ar = Arrays.copyOfRange(a, 2, 4);
        check(ar.length == 2);
        check(ar[0] == a[2]);
        check(ar[1] == 0);
        checkArray(ar, int.class, 2, 2, 1);
        ar = Arrays.copyOfRange(a, 3, 5);
        check(ar.length == 2);
        check(ar[0] == 0);
        check(ar[1] == 0);
        checkArray(ar, int.class, 2, 3, 0);
        byte[] ba = (byte[]) makeArray(3, byte.class);
        if (muzzle == 0)
            System.out.println("byte[] ba = "+Arrays.toString(ba));
        for (int j = 0; j <= ba.length+2; j++) {
            byte[] bb = Arrays.copyOf(ba, j);
            if (muzzle == 0)
                System.out.println("copyOf(ba,"+j+") = "+
                                   Arrays.toString(bb));
            checkArray(bb, byte.class, j, 0, ba.length);
            byte[] bbr = Arrays.copyOfRange(ba, 0, j);
            check(Arrays.equals(bb, bbr));
        }
        for (int i = 0; i <= a.length; i++) {
            for (int j = i; j <= a.length+2; j++) {
                byte[] br = Arrays.copyOfRange(ba, i, j);
                if (muzzle == 0)
                    System.out.println("copyOfRange(ba,"+i+","+j+") = "+
                                       Arrays.toString(br));
                checkArray(br, byte.class, j-i, i, ba.length-i);
            }
        }
        String[] sa = (String[]) makeArray(3, String.class);
        if (muzzle == 0)
            System.out.println("String[] sa = "+Arrays.toString(sa));
        check(sa[0].equals(Integer.toHexString(testValues[0])));
        check(sa[1].equals(Integer.toHexString(testValues[1])));
        check(sa[2].equals(Integer.toHexString(testValues[2])));
        checkArray(sa, String.class, sa.length, 0, sa.length);
        String[] sa4 = Arrays.copyOf(sa, sa.length+1);
        check(sa4[0] == sa[0]);
        check(sa4[1] == sa[1]);
        check(sa4[2] == sa[2]);
        check(sa4[sa.length] == null);
        checkArray(sa4, String.class, sa4.length, 0, sa.length);
        String[] sr4 = Arrays.copyOfRange(sa, 1, 5);
        check(sr4[0] == sa[1]);
        check(sr4[1] == sa[2]);
        check(sr4[2] == null);
        check(sr4[3] == null);
        checkArray(sr4, String.class, 4, 1, sa.length-1);
        if (muzzle == 0)
            System.out.println("simpleTests done");
    }
    static final int[] testValues;
    static {
        testValues = new int[1000];
        Random r = new Random();
        for (int i = 0; i < testValues.length; i++) {
            testValues[i] = r.nextInt();
        }
    }
    static Object testValue(int i, Class<?> c) {
        int tv = testValues[i % testValues.length];
        if (i >= testValues.length)  tv ^= i;
        return invoke(coercers.get(c), tv);
    }
    static Object makeArray(int len, Class<?> c) {
        Object a = Array.newInstance(c, len);
        for (int i = 0; i < len; i++) {
            Array.set(a, i, testValue(i, c));
        }
        return a;
    }
    static void checkArray(Object a, Class<?> c, int requiredLen, int offset, int firstNull) {
        check(c == a.getClass().getComponentType());
        Object nullValue = nullValues.get(c);
        assert(nullValues.containsKey(c));
        int misses = 0;
        int firstMiss = -1;
        int length = Array.getLength(a);
        if (length != requiredLen && requiredLen != -1) {
            if (muzzle == 0)
                System.out.println("*** a.length = "+length+" != "+requiredLen);
            ++misses;
        }
        for (int i = 0; i < length; i++) {
            Object tv = (i >= firstNull) ? nullValue : testValue(i+offset, c);
            Object ai = Array.get(a, i);
            if (!eq(ai, tv)) {
                if (muzzle == 0)
                    System.out.println("*** a["+i+"] = "+ai+" != "+tv);
                if (misses == 0)  firstMiss = i;
                if (++misses > 10)  break;
            }
        }
        if (misses != 0) {
            Method toString = toStrings.get(c);
            if (toString == null)  toString = toStrings.get(Object.class);
            throw new RuntimeException("checkArray failed at "+firstMiss
                                       +" "+c+"[]"
                                       +" : "+invoke(toString, a));
        }
    }
    static boolean eq(Object x, Object y) {
        return x == null? y == null: x.equals(y);
    }
    static Object invoke(Method m, Object... args) {
        Exception ex;
        try {
            return m.invoke(null, args);
        } catch (InvocationTargetException ee) {
            ex = ee;
        } catch (IllegalAccessException ee) {
            ex = ee;
        } catch (IllegalArgumentException ee) {
            ex = ee;
        }
        ArrayList<Object> call = new ArrayList<Object>();
        call.add(m); Collections.addAll(call, args);
        throw new RuntimeException(call+" : "+ex);
    }
    static void check(boolean z) {
        if (!z)  throw new RuntimeException("check failed");
    }
    static void fullTests(int maxLen, Class<?> c) {
        Method cloner      = cloners.get(c);
        assert(cloner != null) : c;
        Method cloneRanger = cloneRangers.get(c);
        assert(cloneRanger != null) : c;
        for (int src = 0; src <= maxLen; src = inc(src, 0, maxLen)) {
            Object a = makeArray(src, c);
            for (int x : new ArrayList<Integer>()) {}
            for (int j = 0; j <= maxLen; j = inc(j, src, maxLen)) {
                Object b = invoke(cloner, a, j);
                checkArray(b, c, j, 0, src);
                testCasesRun++;
                consing += j;
                int maxI = Math.min(src, j);
                for (int i = 0; i <= maxI; i = inc(i, src, maxI)) {
                    Object r = invoke(cloneRanger, a, i, j);
                    checkArray(r, c, j-i, i, src-i);
                    testCasesRun++;
                    consing += j-i;
                }
            }
        }
    }
    static int inc(int x, int crit1, int crit2) {
        int D = shortStepsNear;
        if (crit1 > crit2) { int t = crit1; crit1 = crit2; crit2 = t; }
        assert(crit1 <= crit2);
        assert(x <= crit2);  
        x += 1;
        if (x > D) {
            if (x < crit1-D) {
                x += (x << 1) >> downShift;  
                if (x > crit1-D)  x = crit1-D;
            } else if (x >= crit1+D && x < crit2-D) {
                x += (x << 1) >> downShift;  
                if (x > crit2-D)  x = crit2-D;
            }
        }
        return x;
    }
    public static void main(String[] av) {
        boolean verbose = (av.length != 0);
        muzzle = (verbose? 0: 1);
        if (muzzle == 0)
            System.out.println("test values: "+Arrays.toString(Arrays.copyOf(testValues, 5))+"...");
        simpleTests();
        muzzle = 0;  
        fullTests();
        if (verbose)
            System.out.println("ran "+testCasesRun+" tests, avg len="
                               +(float)consing/testCasesRun);
        maxLen = 500;
        shortStepsNear = 2;
        downShift = 0;
        testCasesRun = 0;
        consing = 0;
        fullTests();
        if (verbose)
            System.out.println("ran "+testCasesRun+" tests, avg len="
                               +(float)consing/testCasesRun);
    }
    static void fullTests() {
        for (Class<?> c : allTypes) {
            fullTests(maxLen, c);
        }
    }
    static Object  coerceToObject(int x) { return (x & 0xF) == 0? null: new Integer(x); }
    static String  coerceToString(int x) { return (x == 0)? null: Integer.toHexString(x); }
    static Integer coerceToInteger(int x) { return (x == 0)? null: x; }
    static byte    coerceToByte(int x) { return (byte)x; }
    static short   coerceToShort(int x) { return (short)x; }
    static int     coerceToInt(int x) { return x; }
    static long    coerceToLong(int x) { return x; }
    static char    coerceToChar(int x) { return (char)x; }
    static float   coerceToFloat(int x) { return x; }
    static double  coerceToDouble(int x) { return x; }
    static boolean coerceToBoolean(int x) { return (x&1) != 0; }
    static Integer[] copyOfIntegerArray(Object[] a, int len) {
        return Arrays.copyOf(a, len, Integer[].class);
    }
    static Integer[] copyOfIntegerArrayRange(Object[] a, int m, int n) {
        return Arrays.copyOfRange(a, m, n, Integer[].class);
    }
    static final List<Class<?>> allTypes
        = Arrays.asList(new Class<?>[]
                        {   Object.class, String.class, Integer.class,
                            byte.class, short.class, int.class, long.class,
                            char.class, float.class, double.class,
                            boolean.class
                        });
    static final HashMap<Class<?>,Method> coercers;
    static final HashMap<Class<?>,Method> cloners;
    static final HashMap<Class<?>,Method> cloneRangers;
    static final HashMap<Class<?>,Method> toStrings;
    static final HashMap<Class<?>,Object> nullValues;
    static {
        coercers = new HashMap<Class<?>,Method>();
        Method[] testMethods = CopyMethods.class.getDeclaredMethods();
        Method cia = null, ciar = null;
        for (int i = 0; i < testMethods.length; i++) {
            Method m = testMethods[i];
            if (!Modifier.isStatic(m.getModifiers()))  continue;
            Class<?> rt = m.getReturnType();
            if (m.getName().startsWith("coerceTo") && allTypes.contains(rt))
                coercers.put(m.getReturnType(), m);
            if (m.getName().equals("copyOfIntegerArray"))
                cia = m;
            if (m.getName().equals("copyOfIntegerArrayRange"))
                ciar = m;
        }
        Method[] arrayMethods = Arrays.class.getDeclaredMethods();
        cloners      = new HashMap<Class<?>,Method>();
        cloneRangers = new HashMap<Class<?>,Method>();
        toStrings    = new HashMap<Class<?>,Method>();
        for (int i = 0; i < arrayMethods.length; i++) {
            Method m = arrayMethods[i];
            if (!Modifier.isStatic(m.getModifiers()))  continue;
            Class<?> rt = m.getReturnType();
            if (m.getName().equals("copyOf")
                && m.getParameterTypes().length == 2)
                cloners.put(rt.getComponentType(), m);
            if (m.getName().equals("copyOfRange")
                && m.getParameterTypes().length == 3)
                cloneRangers.put(rt.getComponentType(), m);
            if (m.getName().equals("toString")) {
                Class<?> pt = m.getParameterTypes()[0];
                toStrings.put(pt.getComponentType(), m);
            }
        }
        cloners.put(String.class, cloners.get(Object.class));
        cloneRangers.put(String.class, cloneRangers.get(Object.class));
        assert(cia != null);
        cloners.put(Integer.class, cia);
        assert(ciar != null);
        cloneRangers.put(Integer.class, ciar);
        nullValues = new HashMap<Class<?>,Object>();
        for (Class<?> c : allTypes) {
            nullValues.put(c, invoke(coercers.get(c), 0));
        }
    }
}
