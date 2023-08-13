public class BadConstraintTest {
    private static String failure;
    public static void main(String[] args) throws Exception {
        genericTests();
        descriptorTests();
        if (failure == null)
            System.out.println("Test passed");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void genericTests() throws Exception {
        for (Object[] test : tests) {
            if (test.length != 5) {
                throw new Exception("Test element has wrong length: " +
                                    toString(test));
            }
            OpenType<?> openType = (OpenType<?>) test[0];
            Object defaultValue = test[1];
            Comparable<?> minValue = (Comparable<?>) test[2];
            Comparable<?> maxValue = (Comparable<?>) test[3];
            Object[] legalValues = (Object[]) test[4];
            System.out.println("test: openType=" + openType +
                               "; defaultValue=" + defaultValue +
                               "; minValue=" + minValue +
                               "; maxValue=" + maxValue +
                               "; legalValues=" + toString(legalValues));
            genericTest(openType, defaultValue, minValue, maxValue,
                        legalValues);
        }
    }
    private static void descriptorTests() throws Exception {
        for (Object[][] test : descriptorTests) {
            if (test.length != 2) {
                throw new Exception("Test element has wrong length: " +
                                    toString(test));
            }
            if (test[0].length != 1) {
                throw new Exception("Test element should have one OpenType: " +
                                    toString(test[0]));
            }
            OpenType<?> openType = (OpenType<?>) test[0][0];
            Descriptor d = descriptor(test[1]);
            System.out.println("test: openType=" + openType +
                               "; descriptor=" + d);
            descriptorTest(openType, d);
        }
    }
    private static void genericTest(OpenType<?> openType,
                                    Object defaultValue,
                                    Comparable<?> minValue,
                                    Comparable<?> maxValue,
                                    Object[] legalValues)
            throws Exception {
        if (minValue == null && maxValue == null && legalValues == null) {
            if (defaultValue == null)
                throw new Exception("What am I testing?");
            Class[] params1 = new Class[] {
                String.class, String.class, OpenType.class,
                boolean.class, boolean.class, boolean.class,
                Object.class
            };
            Constructor<OpenMBeanAttributeInfoSupport> c1 =
                OpenMBeanAttributeInfoSupport.class.getConstructor(params1);
            Class[] params2 = new Class[] {
                String.class, String.class, OpenType.class,
                Object.class
            };
            Constructor<OpenMBeanParameterInfoSupport> c2 =
                OpenMBeanParameterInfoSupport.class.getConstructor(params2);
            ode(c1, "name", "descr", openType, true, true, false, defaultValue);
            ode(c2, "name", "descr", openType, defaultValue);
            descriptorTest(openType,
                           descriptor("defaultValue", defaultValue));
            descriptorTest(openType,
                           descriptor("defaultValue", string(defaultValue)));
        }
        if (legalValues == null) {
            Class[] params1 = new Class[] {
                String.class, String.class, OpenType.class,
                boolean.class, boolean.class, boolean.class,
                Object.class, Comparable.class, Comparable.class
            };
            Constructor<OpenMBeanAttributeInfoSupport> c1 =
                OpenMBeanAttributeInfoSupport.class.getConstructor(params1);
            Class[] params2 = new Class[] {
                String.class, String.class, OpenType.class,
                Object.class, Comparable.class, Comparable.class
            };
            Constructor<OpenMBeanParameterInfoSupport> c2 =
                OpenMBeanParameterInfoSupport.class.getConstructor(params2);
            ode(c1, "name", "descr", openType, true, true, false, defaultValue,
                minValue, maxValue);
            ode(c2, "name", "descr", openType, defaultValue,
                minValue, maxValue);
            descriptorTest(openType,
                           descriptor("defaultValue", defaultValue,
                                      "minValue", minValue,
                                      "maxValue", maxValue));
            descriptorTest(openType,
                           descriptor("defaultValue", string(defaultValue),
                                      "minValue", string(minValue),
                                      "maxValue", string(maxValue)));
        }
        if (legalValues != null) {
            Class[] params1 = new Class[] {
                String.class, String.class, OpenType.class,
                boolean.class, boolean.class, boolean.class,
                Object.class, Object[].class
            };
            Constructor<OpenMBeanAttributeInfoSupport> c1 =
                OpenMBeanAttributeInfoSupport.class.getConstructor(params1);
            Class[] params2 = new Class[] {
                String.class, String.class, OpenType.class,
                Object.class, Object[].class
            };
            Constructor<OpenMBeanParameterInfoSupport> c2 =
                OpenMBeanParameterInfoSupport.class.getConstructor(params2);
            ode(c1, "name", "descr", openType, true, true, false, defaultValue,
                legalValues);
            ode(c2, "name", "descr", openType, defaultValue,
                legalValues);
            descriptorTest(openType,
                           descriptor("defaultValue", defaultValue,
                                      "legalValues", legalValues));
            descriptorTest(openType,
                           descriptor("defaultValue", defaultValue,
                                      "legalValues", arraySet(legalValues)));
            Set<String> strings = new HashSet<String>();
            for (Object x : legalValues)
                strings.add(x.toString());
            descriptorTest(openType,
                           descriptor("defaultValue", defaultValue,
                                      "legalValues", strings));
            descriptorTest(openType,
                           descriptor("defaultValue", defaultValue,
                                      "legalValues",
                                      strings.toArray(new String[0])));
        }
    }
    private static void descriptorTest(OpenType<?> openType, Descriptor d)
            throws Exception {
        Class[] params1 = new Class[] {
            String.class, String.class, OpenType.class,
            boolean.class, boolean.class, boolean.class,
            Descriptor.class
        };
        Constructor<OpenMBeanAttributeInfoSupport> c1 =
            OpenMBeanAttributeInfoSupport.class.getConstructor(params1);
        Class[] params2 = new Class[] {
            String.class, String.class, OpenType.class,
            Descriptor.class
        };
        Constructor<OpenMBeanParameterInfoSupport> c2 =
            OpenMBeanParameterInfoSupport.class.getConstructor(params2);
        iae(c1, "name", "descr", openType, true, true, false, d);
        iae(c2, "name", "descr", openType, d);
    }
    private static void iae(Constructor<?> con, Object... params) {
        checkException(IllegalArgumentException.class, con, params);
    }
    private static void ode(Constructor<?> con, Object... params) {
        checkException(OpenDataException.class, con, params);
    }
    private static void checkException(Class<? extends Exception> exc,
                                       Constructor<?> con, Object[] params) {
        try {
            con.newInstance(params);
            fail("Constructor succeeded but should have got " + exc.getName() +
                 " with params " + Arrays.deepToString(params));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (exc.isInstance(cause))
                return;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            cause.printStackTrace(pw);
            pw.close();
            fail("Constructor should have got " + exc.getName() +
                 " with params " + Arrays.deepToString(params) + ": " + sw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Reflection failed", e);
        }
    }
    private static void fail(String why) {
        System.out.println("FAILED: " + why);
        failure = why;
    }
    private static Descriptor descriptor(Object... entries) {
        if (entries.length % 2 != 0)
            throw new RuntimeException("Odd length descriptor entries");
        String[] names = new String[entries.length / 2];
        Object[] values = new Object[entries.length / 2];
        for (int i = 0; i < entries.length; i += 2) {
            names[i / 2] = (String) entries[i];
            values[i / 2] = entries[i + 1];
        }
        return new ImmutableDescriptor(names, values);
    }
    private static <T> Set<T> arraySet(T[] array) {
        return new HashSet<T>(Arrays.asList(array));
    }
    private static String toString(Object x) {
        if (x == null)
            return "null";
        else if (x.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            int len = Array.getLength(x);
            for (int i = 0; i < len; i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(toString(Array.get(x, i)));
            }
            sb.append("]");
            return sb.toString();
        } else
            return x.toString();
    }
    private static String string(Object x) {
        if (x == null)
            return null;
        return toString(x);
    }
    private static final OpenType<?>
        ostring = SimpleType.STRING,
        oint = SimpleType.INTEGER,
        obool = SimpleType.BOOLEAN,
        olong = SimpleType.LONG,
        obyte = SimpleType.BYTE,
        ofloat = SimpleType.FLOAT,
        odouble = SimpleType.DOUBLE,
        ostringarray, ostringarray2;
    private static final CompositeType ocomposite;
    private static final CompositeData compositeData, compositeData2;
    static {
        try {
            ostringarray = new ArrayType<String[]>(1, ostring);
            ostringarray2 = new ArrayType<String[][]>(2, ostring);
            ocomposite =
                new CompositeType("name", "descr",
                                  new String[] {"s", "i"},
                                  new String[] {"sdesc", "idesc"},
                                  new OpenType[] {ostring, oint});
            compositeData =
                new CompositeDataSupport(ocomposite,
                                         new String[] {"s", "i"},
                                         new Object[] {"foo", 23});
            compositeData2 =
                new CompositeDataSupport(ocomposite,
                                         new String[] {"s", "i"},
                                         new Object[] {"bar", -23});
        } catch (OpenDataException e) { 
            throw new IllegalArgumentException(e.toString(), e);
        }
    }
    private static final Descriptor
        nullD = null,
        emptyD = ImmutableDescriptor.EMPTY_DESCRIPTOR;
    private static final Object[][] tests = {
        {oint, "oops", null, null, null},
        {oint, Long.MAX_VALUE, null, null, null},
        {oint, null, "oops", null, null},
        {oint, "oops", 3, null, null},
        {oint, 3, "oops", null, null},
        {oint, null, null, "oops", null},
        {oint, null, 3, "oops", null},
        {oint, null, 3, false, null},
        {oint, null, null, null, new String[] {"x"}},
        {oint, null, null, null, new Object[] {"x"}},
        {oint, null, null, null, new Object[] {3, "x"}},
        {oint, 3, 4, null, null},
        {oint, 3, null, 2, null},
        {oint, 3, null, null, new Integer[] {2, 4}},
        {ostring, null, "z", "a", null},
        {oint, null, 3, 2, null},
        {ostringarray, new String[] {"x"}, null, null, null},
        {ostringarray, null, null, null, new String[][]{new String[] {"x"}}},
    };
    private static final Object[][][] descriptorTests = {
        {{oint},
         {"minValue", 25L}},
        {{oint},
         {"minValue", new Object()}}, 
        {{oint},
         {"maxValue", new Object()}}, 
        {{oint},
         {"defaultValue", 3,
          "minValue", new Object()}},
        {{oint},
         {"defaultValue", 3,
          "maxValue", new Object()}},
        {{oint},
         {"legalValues", new int[] {3}}}, 
        {{oint},
         {"legalValues", 3}},
        {{oint},
         {"minValue", 3, "legalValues", new Integer[] {3, 4}}},
        {{oint},
         {"maxValue", 3, "legalValues", new Integer[] {2, 3}}},
        {{oint},
         {"defaultValue", 3, "minValue", 3, "legalValues", new Integer[] {3}}},
        {{ostringarray},
         {"minValue", new String[] {"x"}}},
        {{ostringarray},
         {"maxValue", new String[] {"x"}}},
    };
}
