public class MethodTypeTest {
    private Class<?> rtype;
    private Class<?>[] ptypes;
    private MethodType mt_viS, mt_OO, mt_OO2, mt_vv, mt_Vv, mt_Ov;
    private MethodType mt_iSI, mt_ISi, mt_ISI, mt_iSi;
    private MethodType mt_viO, mt_iO2, mt_OOi, mt_iOi;
    private MethodType mt_VIO, mt_IO2, mt_OOI, mt_IOI, mt_VIS;
    private MethodType mt_vOiSzA, mt_OO99;
    private MethodType[] GALLERY;
    private Method compareTo;
    @Before
    public void setUp() throws Exception {
        rtype = void.class;
        ptypes = new Class<?>[] { int.class, String.class };
        mt_viS = MethodType.methodType(void.class, int.class, String.class);
        mt_OO = MethodType.methodType(Object.class, Object.class);
        mt_OO2 = MethodType.methodType(Object.class, Object.class, Object.class);
        mt_vv = MethodType.methodType(void.class);
        mt_Vv = MethodType.methodType(Void.class);
        mt_Ov = MethodType.methodType(Object.class);
        mt_iSI = MethodType.methodType(int.class, String.class, Integer.class);
        mt_ISi = MethodType.methodType(Integer.class, String.class, int.class);
        mt_ISI = MethodType.methodType(Integer.class, String.class, Integer.class);
        mt_iSi = MethodType.methodType(int.class, String.class, int.class);
        compareTo = String.class.getDeclaredMethod("compareTo", String.class);
        mt_viO = MethodType.methodType(void.class, int.class, Object.class);
        mt_iO2 = MethodType.methodType(int.class, Object.class, Object.class);
        mt_OOi = MethodType.methodType(Object.class, Object.class, int.class);
        mt_iOi = MethodType.methodType(int.class, Object.class, int.class);
        mt_VIO = MethodType.methodType(Void.class, Integer.class, Object.class);
        mt_IO2 = MethodType.methodType(Integer.class, Object.class, Object.class);
        mt_OOI = MethodType.methodType(Object.class, Object.class, Integer.class);
        mt_IOI = MethodType.methodType(Integer.class, Object.class, Integer.class);
        mt_VIS = MethodType.methodType(Void.class, Integer.class, String.class);
        mt_vOiSzA = MethodType.methodType(void.class, Object.class, int.class, String.class, boolean.class, Object[].class);
        mt_OO99 = MethodType.genericMethodType(99);
        GALLERY = new MethodType[] {
            mt_viS, mt_OO, mt_OO2, mt_vv, mt_Vv, mt_Ov,
            mt_iSI, mt_ISi, mt_ISI, mt_iSi,
            mt_viO, mt_iO2, mt_OOi, mt_iOi,
            mt_VIO, mt_IO2, mt_OOI, mt_IOI,
            mt_VIS, mt_vOiSzA, mt_OO99
        };
    }
    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testDistinct() {
        List<MethodType> gallery2 = new ArrayList<>();
        for (MethodType mt : GALLERY) {
            assertFalse(mt.toString(), gallery2.contains(mt));
            gallery2.add(mt);
        }
        assertEquals(Arrays.asList(GALLERY), gallery2);
    }
    @Test
    public void testMake_Class_ClassArr() {
        System.out.println("make (from type array)");
        MethodType result = MethodType.methodType(rtype, ptypes);
        assertSame(mt_viS, result);
    }
    @Test
    public void testMake_Class_List() {
        System.out.println("make (from type list)");
        MethodType result = MethodType.methodType(rtype, Arrays.asList(ptypes));
        assertSame(mt_viS, result);
    }
    @Test
    public void testMake_3args() {
        System.out.println("make (from type with varargs)");
        MethodType result = MethodType.methodType(rtype, ptypes[0], ptypes[1]);
        assertSame(mt_viS, result);
    }
    @Test
    public void testMake_Class() {
        System.out.println("make (from single type)");
        Class<?> rt = Integer.class;
        MethodType expResult = MethodType.methodType(rt, new Class<?>[0]);
        MethodType result = MethodType.methodType(rt);
        assertSame(expResult, result);
    }
    @Test
    public void testMakeGeneric() {
        System.out.println("makeGeneric");
        int objectArgCount = 2;
        MethodType expResult = mt_OO2;
        MethodType result = MethodType.genericMethodType(objectArgCount);
        assertSame(expResult, result);
    }
    @Test
    public void testMake_MethodType() {
        System.out.println("make (from rtype, MethodType)");
        MethodType expResult = mt_iO2;
        MethodType result = MethodType.methodType(int.class, mt_IO2);
        assertSame(expResult, result);
    }
    @Test
    public void testMake_String_ClassLoader() {
        System.out.println("make (from bytecode signature)");
        ClassLoader loader = null;
        MethodType[] instances = {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        String obj = "Ljava/lang/Object;";
        assertEquals(obj, concat(Object.class));
        String[] expResults = {
            "(ILjava/lang/String;)V",
            concat("(", obj, 2, ")", Object.class),
            "()V", "()"+obj,
            concat("(", String.class, Integer.class, ")I"),
            concat("(", String.class, "I)", Integer.class),
            concat("(", String.class, Integer.class, ")", Integer.class),
            concat("(", String.class, "I)I")
        };
        for (int i = 0; i < instances.length; i++) {
            MethodType instance = instances[i];
            String result = instance.toMethodDescriptorString();
            assertEquals("#"+i, expResults[i], result);
            MethodType parsed = MethodType.fromMethodDescriptorString(result, loader);
            assertSame("--#"+i, instance, parsed);
        }
    }
    private static String concat(Object... parts) {
        StringBuilder sb = new StringBuilder();
        Object prevPart = "";
        for (Object part : parts) {
            if (part instanceof Class) {
                part = "L"+((Class)part).getName()+";";
            }
            if (part instanceof Integer) {
                for (int n = (Integer) part; n > 1; n--)
                    sb.append(prevPart);
                part = "";
            }
            sb.append(part);
            prevPart = part;
        }
        return sb.toString().replace('.', '/');
    }
    @Test
    public void testHasPrimitives() {
        System.out.println("hasPrimitives");
        MethodType[] instances = {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        boolean[] expResults =   {true,   false,  true,  false, true,   true,   false,  true};
        for (int i = 0; i < instances.length; i++) {
            boolean result = instances[i].hasPrimitives();
            assertEquals("#"+i, expResults[i], result);
        }
    }
    @Test
    public void testHasWrappers() {
        System.out.println("hasWrappers");
        MethodType[] instances = {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        boolean[] expResults =   {false,  false,  false, false, true,   true,   true,   false};
        for (int i = 0; i < instances.length; i++) {
            System.out.println("  hasWrappers "+instances[i]);
            boolean result = instances[i].hasWrappers();
            assertEquals("#"+i, expResults[i], result);
        }
    }
    @Test
    public void testErase() {
        System.out.println("erase");
        MethodType[] instances  = {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        MethodType[] expResults = {mt_viO, mt_OO2, mt_vv, mt_Ov, mt_iO2, mt_OOi, mt_OO2, mt_iOi};
        for (int i = 0; i < instances.length; i++) {
            MethodType result = instances[i].erase();
            assertSame("#"+i, expResults[i], result);
        }
    }
    @Test
    public void testGeneric() {
        System.out.println("generic");
        MethodType[] instances =  {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        MethodType[] expResults = {mt_OO2, mt_OO2, mt_Ov, mt_Ov, mt_OO2, mt_OO2, mt_OO2, mt_OO2};
        for (int i = 0; i < instances.length; i++) {
            MethodType result = instances[i].generic();
            assertSame("#"+i, expResults[i], result);
        }
    }
    @Test
    public void testWrap() {
        System.out.println("wrap");
        MethodType[] instances =  {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        MethodType[] expResults = {mt_VIS, mt_OO2, mt_Vv, mt_Ov, mt_ISI, mt_ISI, mt_ISI, mt_ISI};
        for (int i = 0; i < instances.length; i++) {
            MethodType result = instances[i].wrap();
            assertSame("#"+i, expResults[i], result);
        }
    }
    @Test
    public void testUnwrap() {
        System.out.println("unwrap");
        MethodType[] instances =  {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        MethodType[] expResults = {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSi, mt_iSi, mt_iSi, mt_iSi};
        for (int i = 0; i < instances.length; i++) {
            MethodType result = instances[i].unwrap();
            assertSame("#"+i, expResults[i], result);
        }
    }
    @Test
    public void testParameterType() {
        System.out.println("parameterType");
        for (int num = 0; num < ptypes.length; num++) {
            MethodType instance = mt_viS;
            Class<?> expResult = ptypes[num];
            Class<?> result = instance.parameterType(num);
            assertSame(expResult, result);
        }
    }
    @Test
    public void testParameterCount() {
        System.out.println("parameterCount");
        MethodType instance = mt_viS;
        int expResult = 2;
        int result = instance.parameterCount();
        assertEquals(expResult, result);
    }
    @Test
    public void testReturnType() {
        System.out.println("returnType");
        MethodType instance = mt_viS;
        Class<?> expResult = void.class;
        Class<?> result = instance.returnType();
        assertSame(expResult, result);
    }
    @Test
    public void testParameterList() {
        System.out.println("parameterList");
        MethodType instance = mt_viS;
        List<Class<?>> expResult = Arrays.asList(ptypes);
        List<Class<?>> result = instance.parameterList();
        assertEquals(expResult, result);
    }
    @Test
    public void testParameterArray() {
        System.out.println("parameterArray");
        MethodType instance = mt_viS;
        Class<?>[] expResult = ptypes;
        Class<?>[] result = instance.parameterArray();
        assertEquals(Arrays.asList(expResult), Arrays.asList(result));
    }
    @Test
    public void testEquals_Object() {
        System.out.println("equals");
        Object x = null;
        MethodType instance = mt_viS;
        boolean expResult = false;
        boolean result = instance.equals(x);
        assertEquals(expResult, result);
    }
    @Test
    public void testEquals_MethodType() {
        System.out.println("equals");
        MethodType that = mt_viS;
        MethodType instance = mt_viS;
        boolean expResult = true;
        boolean result = instance.equals(that);
        assertEquals(expResult, result);
    }
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        MethodType instance = mt_viS;
        ArrayList<Class<?>> types = new ArrayList<Class<?>>();
        types.add(instance.returnType());
        types.addAll(instance.parameterList());
        int expResult = types.hashCode();
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }
    @Test
    public void testToString() {
        System.out.println("toString");
        MethodType[] instances = {mt_viS, mt_OO2, mt_vv, mt_Ov, mt_iSI, mt_ISi, mt_ISI, mt_iSi};
        String[] expResults = {
            "(int,String)void",
            "(Object,Object)Object",
            "()void",
            "()Object",
            "(String,Integer)int",
            "(String,int)Integer",
            "(String,Integer)Integer",
            "(String,int)int"
        };
        for (int i = 0; i < instances.length; i++) {
            MethodType instance = instances[i];
            String result = instance.toString();
            System.out.println("#"+i+":"+result);
            assertEquals("#"+i, expResults[i], result);
        }
    }
    private static byte[] writeSerial(Object x) throws java.io.IOException {
        try (java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
             java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(bout)
             ) {
            out.writeObject(x);
            out.flush();
            return bout.toByteArray();
        }
    }
    private static Object readSerial(byte[] wire) throws java.io.IOException, ClassNotFoundException {
        try (java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(wire);
             java.io.ObjectInputStream in = new java.io.ObjectInputStream(bin)) {
            return in.readObject();
        }
    }
    private static void testSerializedEquality(Object x) throws java.io.IOException, ClassNotFoundException {
        if (x instanceof Object[])
            x = Arrays.asList((Object[]) x);  
        byte[] wire = writeSerial(x);
        Object y = readSerial(wire);
        assertEquals(x, y);
    }
    @Test
    public void testSerialization() throws Throwable {
        System.out.println("serialization");
        for (MethodType mt : GALLERY) {
            testSerializedEquality(mt);
        }
        testSerializedEquality(GALLERY);
        List<Object> stuff = new ArrayList<>();
        Collections.addAll(stuff, GALLERY);  
        Object[] triples = Arrays.copyOfRange(GALLERY, 0, GALLERY.length/2);
        Collections.addAll(stuff, triples);  
        for (MethodType mt : GALLERY) {
            Collections.addAll(stuff, mt.parameterArray());
        }
        Collections.shuffle(stuff, new Random(292));
        Collections.addAll(stuff, GALLERY);  
        testSerializedEquality(stuff);
    }
    @Test
    public void testPortableSerialFormat() throws Throwable {
        System.out.println("portable serial format");
        boolean generateData = false;
        Object[][] cases = {
            { mt_vv, new byte[] {  
                    (byte)0xac, (byte)0xed, (byte)0x00, (byte)0x05, (byte)0x73, (byte)0x72, (byte)0x00, (byte)0x1b,
                    (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e,
                    (byte)0x67, (byte)0x2e, (byte)0x69, (byte)0x6e, (byte)0x76, (byte)0x6f, (byte)0x6b, (byte)0x65,
                    (byte)0x2e, (byte)0x4d, (byte)0x65, (byte)0x74, (byte)0x68, (byte)0x6f, (byte)0x64, (byte)0x54,
                    (byte)0x79, (byte)0x70, (byte)0x65, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x01, (byte)0x24, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70,
                    (byte)0x76, (byte)0x72, (byte)0x00, (byte)0x04, (byte)0x76, (byte)0x6f, (byte)0x69, (byte)0x64,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70, (byte)0x75, (byte)0x72, (byte)0x00,
                    (byte)0x12, (byte)0x5b, (byte)0x4c, (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e,
                    (byte)0x6c, (byte)0x61, (byte)0x6e, (byte)0x67, (byte)0x2e, (byte)0x43, (byte)0x6c, (byte)0x61,
                    (byte)0x73, (byte)0x73, (byte)0x3b, (byte)0xab, (byte)0x16, (byte)0xd7, (byte)0xae, (byte)0xcb,
                    (byte)0xcd, (byte)0x5a, (byte)0x99, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x78,
                } },
            { mt_OO, new byte[] {  
                    (byte)0xac, (byte)0xed, (byte)0x00, (byte)0x05, (byte)0x73, (byte)0x72, (byte)0x00, (byte)0x1b,
                    (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e,
                    (byte)0x67, (byte)0x2e, (byte)0x69, (byte)0x6e, (byte)0x76, (byte)0x6f, (byte)0x6b, (byte)0x65,
                    (byte)0x2e, (byte)0x4d, (byte)0x65, (byte)0x74, (byte)0x68, (byte)0x6f, (byte)0x64, (byte)0x54,
                    (byte)0x79, (byte)0x70, (byte)0x65, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x01, (byte)0x24, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70,
                    (byte)0x76, (byte)0x72, (byte)0x00, (byte)0x10, (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61,
                    (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e, (byte)0x67, (byte)0x2e, (byte)0x4f, (byte)0x62,
                    (byte)0x6a, (byte)0x65, (byte)0x63, (byte)0x74, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x78,
                    (byte)0x70, (byte)0x75, (byte)0x72, (byte)0x00, (byte)0x12, (byte)0x5b, (byte)0x4c, (byte)0x6a,
                    (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e, (byte)0x67,
                    (byte)0x2e, (byte)0x43, (byte)0x6c, (byte)0x61, (byte)0x73, (byte)0x73, (byte)0x3b, (byte)0xab,
                    (byte)0x16, (byte)0xd7, (byte)0xae, (byte)0xcb, (byte)0xcd, (byte)0x5a, (byte)0x99, (byte)0x02,
                    (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,
                    (byte)0x71, (byte)0x00, (byte)0x7e, (byte)0x00, (byte)0x03, (byte)0x78,
                } },
            { mt_vOiSzA, new byte[] {  
                    (byte)0xac, (byte)0xed, (byte)0x00, (byte)0x05, (byte)0x73, (byte)0x72, (byte)0x00, (byte)0x1b,
                    (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e,
                    (byte)0x67, (byte)0x2e, (byte)0x69, (byte)0x6e, (byte)0x76, (byte)0x6f, (byte)0x6b, (byte)0x65,
                    (byte)0x2e, (byte)0x4d, (byte)0x65, (byte)0x74, (byte)0x68, (byte)0x6f, (byte)0x64, (byte)0x54,
                    (byte)0x79, (byte)0x70, (byte)0x65, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x01, (byte)0x24, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70,
                    (byte)0x76, (byte)0x72, (byte)0x00, (byte)0x04, (byte)0x76, (byte)0x6f, (byte)0x69, (byte)0x64,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70, (byte)0x75, (byte)0x72, (byte)0x00,
                    (byte)0x12, (byte)0x5b, (byte)0x4c, (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e,
                    (byte)0x6c, (byte)0x61, (byte)0x6e, (byte)0x67, (byte)0x2e, (byte)0x43, (byte)0x6c, (byte)0x61,
                    (byte)0x73, (byte)0x73, (byte)0x3b, (byte)0xab, (byte)0x16, (byte)0xd7, (byte)0xae, (byte)0xcb,
                    (byte)0xcd, (byte)0x5a, (byte)0x99, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x76, (byte)0x72, (byte)0x00, (byte)0x10,
                    (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e,
                    (byte)0x67, (byte)0x2e, (byte)0x4f, (byte)0x62, (byte)0x6a, (byte)0x65, (byte)0x63, (byte)0x74,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70, (byte)0x76, (byte)0x72, (byte)0x00,
                    (byte)0x03, (byte)0x69, (byte)0x6e, (byte)0x74, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x78,
                    (byte)0x70, (byte)0x76, (byte)0x72, (byte)0x00, (byte)0x10, (byte)0x6a, (byte)0x61, (byte)0x76,
                    (byte)0x61, (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e, (byte)0x67, (byte)0x2e, (byte)0x53,
                    (byte)0x74, (byte)0x72, (byte)0x69, (byte)0x6e, (byte)0x67, (byte)0xa0, (byte)0xf0, (byte)0xa4,
                    (byte)0x38, (byte)0x7a, (byte)0x3b, (byte)0xb3, (byte)0x42, (byte)0x02, (byte)0x00, (byte)0x00,
                    (byte)0x78, (byte)0x70, (byte)0x76, (byte)0x72, (byte)0x00, (byte)0x07, (byte)0x62, (byte)0x6f,
                    (byte)0x6f, (byte)0x6c, (byte)0x65, (byte)0x61, (byte)0x6e, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
                    (byte)0x78, (byte)0x70, (byte)0x76, (byte)0x72, (byte)0x00, (byte)0x13, (byte)0x5b, (byte)0x4c,
                    (byte)0x6a, (byte)0x61, (byte)0x76, (byte)0x61, (byte)0x2e, (byte)0x6c, (byte)0x61, (byte)0x6e,
                    (byte)0x67, (byte)0x2e, (byte)0x4f, (byte)0x62, (byte)0x6a, (byte)0x65, (byte)0x63, (byte)0x74,
                    (byte)0x3b, (byte)0x90, (byte)0xce, (byte)0x58, (byte)0x9f, (byte)0x10, (byte)0x73, (byte)0x29,
                    (byte)0x6c, (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x78, (byte)0x70, (byte)0x78,
                } },
        };
        for (Object[] c : cases) {
            MethodType mt = (MethodType) c[0];
            System.out.println("deserialize "+mt);
            byte[] wire = (byte[]) c[1];
            if (generateData) {
                System.out.println("<generateData>");
                wire = writeSerial(mt);
                final String INDENT = "                ";
                System.out.print("{  
                for (int i = 0; i < wire.length; i++) {
                    if (i % 8 == 0) { System.out.println(); System.out.print(INDENT+"   "); }
                    String hex = Integer.toHexString(wire[i] & 0xFF);
                    if (hex.length() == 1)  hex = "0"+hex;
                    System.out.print(" (byte)0x"+hex+",");
                }
                System.out.println();
                System.out.println(INDENT+"}");
                System.out.println("</generateData>");
                System.out.flush();
            }
            Object decode;
            try {
                decode = readSerial(wire);
            } catch (Exception ex) {
                decode = ex;  
            }
            assertEquals(mt, decode);
        }
    }
}
