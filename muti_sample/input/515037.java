@TestTargetClass(Parcel.class)
public class ParcelTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "obtain",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "recycle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "This method is protected final, which can't be invoked in test case.",
            method = "obtain",
            args = {int.class}
        )
    })
    public void testObtain() {
        Parcel p1 = Parcel.obtain();
        assertNotNull(p1);
        Parcel p2 = Parcel.obtain();
        assertNotNull(p2);
        Parcel p3 = Parcel.obtain();
        assertNotNull(p3);
        Parcel p4 = Parcel.obtain();
        assertNotNull(p4);
        Parcel p5 = Parcel.obtain();
        assertNotNull(p5);
        Parcel p6 = Parcel.obtain();
        assertNotNull(p6);
        Parcel p7 = Parcel.obtain();
        assertNotNull(p7);
        p1.recycle();
        p2.recycle();
        p3.recycle();
        p4.recycle();
        p5.recycle();
        p6.recycle();
        p7.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "appendFrom",
            args = {Parcel.class, int.class, int.class}
        )
    })
    public void testAppendFrom() {
        Parcel p;
        Parcel p2;
        int d1;
        int d2;
        p = Parcel.obtain();
        d1 = p.dataPosition();
        p.writeInt(7);
        p.writeInt(5);
        d2 = p.dataPosition();
        p2 = Parcel.obtain();
        p2.appendFrom(p, d1, d2 - d1);
        p2.setDataPosition(0);
        assertEquals(7, p2.readInt());
        assertEquals(5, p2.readInt());
        p2.recycle();
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: dataAvail ",
            method = "dataAvail",
            args = {}
        )
    })
    public void testDataAvail() {
        Parcel p;
        p = Parcel.obtain();
        p.writeInt(7); 
        p.writeInt(5); 
        p.writeLong(7L); 
        p.writeString("7L"); 
        p.setDataPosition(0);
        assertEquals(p.dataSize(), p.dataAvail());
        p.readInt();
        assertEquals(p.dataSize() - p.dataPosition(), p.dataAvail());
        p.readInt();
        assertEquals(p.dataSize() - p.dataPosition(), p.dataAvail());
        p.readLong();
        assertEquals(p.dataSize() - p.dataPosition(), p.dataAvail());
        p.readString();
        assertEquals(p.dataSize() - p.dataPosition(), p.dataAvail());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dataCapacity",
            args = {}
        )
    })
    public void testDataCapacity() {
        Parcel p;
        p = Parcel.obtain();
        assertEquals(0, p.dataCapacity());
        p.writeInt(7); 
        int dC1 = p.dataCapacity();
        p.writeDouble(2.19);
        int dC2 = p.dataCapacity();
        assertTrue(dC2 > dC1);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataCapacity",
            args = {int.class}
        )
    })
    public void testSetDataCapacity() {
        Parcel p;
        p = Parcel.obtain();
        assertEquals(0, p.dataCapacity());
        p.setDataCapacity(2);
        assertEquals(2, p.dataCapacity());
        p.setDataCapacity(1);
        assertEquals(2, p.dataCapacity());
        p.setDataCapacity(3);
        assertEquals(3, p.dataCapacity());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dataPosition",
            args = {}
        )
    })
    public void testDataPosition() {
        Parcel p;
        p = Parcel.obtain();
        assertEquals(0, p.dataPosition());
        p.writeInt(7); 
        int dP1 = p.dataPosition();
        p.writeLong(7L); 
        int dP2 = p.dataPosition();
        assertTrue(dP2 > dP1);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataPosition",
            args = {int.class}
        )
    })
    public void testSetDataPosition() {
        Parcel p;
        p = Parcel.obtain();
        assertEquals(0, p.dataSize());
        assertEquals(0, p.dataPosition());
        p.setDataPosition(4);
        assertEquals(4, p.dataPosition());
        p.setDataPosition(7);
        assertEquals(7, p.dataPosition());
        p.setDataPosition(0);
        p.writeInt(7);
        assertEquals(4, p.dataSize());
        p.setDataPosition(4);
        assertEquals(4, p.dataPosition());
        p.setDataPosition(7);
        assertEquals(7, p.dataPosition());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "dataSize",
            args = {}
        )
    })
    public void testDataSize() {
        Parcel p;
        p = Parcel.obtain();
        assertEquals(0, p.dataSize());
        p.writeInt(7); 
        assertEquals(4, p.dataSize());
        p.writeInt(5); 
        assertEquals(8, p.dataSize());
        p.writeLong(7L); 
        assertEquals(16, p.dataSize());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDataSize",
            args = {int.class}
        )
    })
    public void testSetDataSize() {
        Parcel p;
        p = Parcel.obtain();
        assertEquals(0, p.dataSize());
        p.setDataSize(5);
        assertEquals(5, p.dataSize());
        p.setDataSize(3);
        assertEquals(3, p.dataSize());
        p.writeInt(3);
        assertEquals(4, p.dataSize());
        p.setDataSize(5);
        assertEquals(5, p.dataSize());
        p.setDataSize(3);
        assertEquals(3, p.dataSize());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "enforceInterface",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeInterfaceToken",
            args = {String.class}
        )
    })
    public void testEnforceInterface() {
        Parcel p;
        String s = "IBinder interface token";
        p = Parcel.obtain();
        p.writeInterfaceToken(s);
        p.setDataPosition(0);
        try {
            p.enforceInterface("");
            fail("Should throw an SecurityException");
        } catch (SecurityException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeInterfaceToken(s);
        p.setDataPosition(0);
        p.enforceInterface(s);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "marshall",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "unmarshall",
            args = {byte[].class, int.class, int.class}
        )
    })
    public void testMarshall() {
        final byte[] c = {Byte.MAX_VALUE, (byte) 111, (byte) 11, (byte) 1, (byte) 0,
                    (byte) -1, (byte) -11, (byte) -111, Byte.MIN_VALUE};
        Parcel p1 = Parcel.obtain();
        p1.writeByteArray(c);
        p1.setDataPosition(0);
        byte[] d1 = p1.marshall();
        Parcel p2 = Parcel.obtain();
        p2.unmarshall(d1, 0, d1.length);
        p2.setDataPosition(0);
        byte[] d2 = new byte[c.length];
        p2.readByteArray(d2);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d2[i]);
        }
        p1.recycle();
        p2.recycle();
    }
    @SuppressWarnings("unchecked")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readValue",
            args = {ClassLoader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeValue",
            args = {Object.class}
        )
    })
    public void testReadValue() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        p = Parcel.obtain();
        p.writeValue(null);
        p.setDataPosition(0);
        assertNull(p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue("String");
        p.setDataPosition(0);
        assertEquals("String", p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue(Integer.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Integer.MAX_VALUE, p.readValue(mcl));
        p.recycle();
        HashMap map = new HashMap();
        HashMap map2;
        map.put("string", "String");
        map.put("int", Integer.MAX_VALUE);
        map.put("boolean", true);
        p = Parcel.obtain();
        p.writeValue(map);
        p.setDataPosition(0);
        map2 = (HashMap) p.readValue(mcl);
        assertNotNull(map2);
        assertEquals(map.size(), map2.size());
        assertEquals("String", map.get("string"));
        assertEquals(Integer.MAX_VALUE, map.get("int"));
        assertEquals(true, map.get("boolean"));
        p.recycle();
        Bundle bundle = new Bundle();
        bundle.putBoolean("boolean", true);
        bundle.putInt("int", Integer.MAX_VALUE);
        bundle.putString("string", "String");
        Bundle bundle2;
        p = Parcel.obtain();
        p.writeValue(bundle);
        p.setDataPosition(0);
        bundle2 = (Bundle) p.readValue(mcl);
        assertNotNull(bundle2);
        assertEquals(true, bundle2.getBoolean("boolean"));
        assertEquals(Integer.MAX_VALUE, bundle2.getInt("int"));
        assertEquals("String", bundle2.getString("string"));
        p.recycle();
        final String signatureString  = "1234567890abcdef";
        Signature s = new Signature(signatureString);
        p = Parcel.obtain();
        p.writeValue(s);
        p.setDataPosition(0);
        assertEquals(s, p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue(Short.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Short.MAX_VALUE, p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue(Long.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Long.MAX_VALUE, p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue(Float.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Float.MAX_VALUE, p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue(Double.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Double.MAX_VALUE, p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue(true);
        p.writeValue(false);
        p.setDataPosition(0);
        assertTrue((Boolean) p.readValue(mcl));
        assertFalse((Boolean) p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue((CharSequence) "CharSequence");
        p.setDataPosition(0);
        assertEquals("CharSequence", p.readValue(mcl));
        p.recycle();
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(Integer.MAX_VALUE);
        arrayList2.add(true);
        arrayList2.add(Long.MAX_VALUE);
        ArrayList arrayList = new ArrayList();
        p = Parcel.obtain();
        p.writeValue(arrayList2);
        p.setDataPosition(0);
        assertEquals(0, arrayList.size());
        arrayList = (ArrayList) p.readValue(mcl);
        assertEquals(3, arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            assertEquals(arrayList.get(i), arrayList2.get(i));
        }
        p.recycle();
        SparseArray<Object> sparseArray = new SparseArray<Object>();
        sparseArray.put(3, "String");
        sparseArray.put(2, Long.MAX_VALUE);
        sparseArray.put(4, Float.MAX_VALUE);
        sparseArray.put(0, Integer.MAX_VALUE);
        sparseArray.put(1, true);
        sparseArray.put(10, true);
        SparseArray<Object> sparseArray2;
        p = Parcel.obtain();
        p.writeValue(sparseArray);
        p.setDataPosition(0);
        sparseArray2 = (SparseArray<Object>) p.readValue(mcl);
        assertNotNull(sparseArray2);
        assertEquals(sparseArray.size(), sparseArray2.size());
        assertEquals(sparseArray.get(0), sparseArray2.get(0));
        assertEquals(sparseArray.get(1), sparseArray2.get(1));
        assertEquals(sparseArray.get(2), sparseArray2.get(2));
        assertEquals(sparseArray.get(3), sparseArray2.get(3));
        assertEquals(sparseArray.get(4), sparseArray2.get(4));
        assertEquals(sparseArray.get(10), sparseArray2.get(10));
        p.recycle();
        boolean[] booleanArray  = {true, false, true, false};
        boolean[] booleanArray2 = new boolean[booleanArray.length];
        p = Parcel.obtain();
        p.writeValue(booleanArray);
        p.setDataPosition(0);
        booleanArray2 = (boolean[]) p.readValue(mcl);
        for (int i = 0; i < booleanArray.length; i++) {
            assertEquals(booleanArray[i], booleanArray2[i]);
        }
        p.recycle();
        byte[] byteArray = {Byte.MAX_VALUE, (byte) 111, (byte) 11, (byte) 1, (byte) 0,
                (byte) -1, (byte) -11, (byte) -111, Byte.MIN_VALUE};
        byte[] byteArray2 = new byte[byteArray.length];
        p = Parcel.obtain();
        p.writeValue(byteArray);
        p.setDataPosition(0);
        byteArray2 = (byte[]) p.readValue(mcl);
        for (int i = 0; i < byteArray.length; i++) {
            assertEquals(byteArray[i], byteArray2[i]);
        }
        p.recycle();
        String[] stringArray = {"",
                "a",
                "Hello, Android!",
                "A long string that is used to test the api readStringArray(),"};
        String[] stringArray2 = new String[stringArray.length];
        p = Parcel.obtain();
        p.writeValue(stringArray);
        p.setDataPosition(0);
        stringArray2 = (String[]) p.readValue(mcl);
        for (int i = 0; i < stringArray.length; i++) {
            assertEquals(stringArray[i], stringArray2[i]);
        }
        p.recycle();
        Binder binder;
        Binder binder2 = new Binder();
        p = Parcel.obtain();
        p.writeValue(binder2);
        p.setDataPosition(0);
        binder = (Binder) p.readValue(mcl);
        assertEquals(binder2, binder);
        p.recycle();
        Signature[] signatures = {new Signature("123"),
                new Signature("ABC"),
                new Signature("abc")};
        Parcelable[] signatures2;
        p = Parcel.obtain();
        p.writeValue(signatures);
        p.setDataPosition(0);
        signatures2 = (Parcelable[]) p.readValue(mcl);
        for (int i = 0; i < signatures.length; i++) {
            assertEquals(signatures[i], signatures2[i]);
        }
        p.recycle();
        Object[] objects = new Object[5];
        objects[0] = Integer.MAX_VALUE;
        objects[1] = true;
        objects[2] = Long.MAX_VALUE;
        objects[3] = "String";
        objects[4] = Float.MAX_VALUE;
        Object[] objects2;
        p = Parcel.obtain();
        p.writeValue(objects);
        p.setDataPosition(0);
        objects2 = (Object[]) p.readValue(mcl);
        assertNotNull(objects2);
        for (int i = 0; i < objects2.length; i++) {
            assertEquals(objects[i], objects2[i]);
        }
        p.recycle();
        int[] intArray = {111, 11, 1, 0, -1, -11, -111};
        int[] intArray2 = new int[intArray.length];
        p = Parcel.obtain();
        p.writeValue(intArray);
        p.setDataPosition(0);
        intArray2= (int[]) p.readValue(mcl);
        assertNotNull(intArray2);
        for (int i = 0; i < intArray2.length; i++) {
            assertEquals(intArray[i], intArray2[i]);
        }
        p.recycle();
        long[] longArray = {111L, 11L, 1L, 0L, -1L, -11L, -111L};
        long[] longArray2 = new long[longArray.length];
        p = Parcel.obtain();
        p.writeValue(longArray);
        p.setDataPosition(0);
        longArray2= (long[]) p.readValue(mcl);
        assertNotNull(longArray2);
        for (int i = 0; i < longArray2.length; i++) {
            assertEquals(longArray[i], longArray2[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeValue(Byte.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Byte.MAX_VALUE, p.readValue(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeValue((Serializable) "Serializable");
        p.setDataPosition(0);
        assertEquals("Serializable", p.readValue(mcl));
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readByte",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeByte",
            args = {byte.class}
        )
    })
    public void testReadByte() {
        Parcel p;
        p = Parcel.obtain();
        p.writeByte((byte) 0);
        p.setDataPosition(0);
        assertEquals((byte) 0, p.readByte());
        p.recycle();
        p = Parcel.obtain();
        p.writeByte((byte) 1);
        p.setDataPosition(0);
        assertEquals((byte) 1, p.readByte());
        p.recycle();
        p = Parcel.obtain();
        p.writeByte((byte) -1);
        p.setDataPosition(0);
        assertEquals((byte) -1, p.readByte());
        p.recycle();
        p = Parcel.obtain();
        p.writeByte(Byte.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Byte.MAX_VALUE, p.readByte());
        p.recycle();
        p = Parcel.obtain();
        p.writeByte(Byte.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Byte.MIN_VALUE, p.readByte());
        p.recycle();
        p = Parcel.obtain();
        p.writeByte(Byte.MAX_VALUE);
        p.writeByte((byte) 11);
        p.writeByte((byte) 1);
        p.writeByte((byte) 0);
        p.writeByte((byte) -1);
        p.writeByte((byte) -11);
        p.writeByte(Byte.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Byte.MAX_VALUE, p.readByte());
        assertEquals((byte) 11, p.readByte());
        assertEquals((byte) 1, p.readByte());
        assertEquals((byte) 0, p.readByte());
        assertEquals((byte) -1, p.readByte());
        assertEquals((byte) -11, p.readByte());
        assertEquals(Byte.MIN_VALUE, p.readByte());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readByteArray",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeByteArray",
            args = {byte[].class}
        )
    })
    public void testReadByteArray() {
        Parcel p;
        byte[] a = {(byte) 21};
        byte[] b = new byte[a.length];
        byte[] c = {Byte.MAX_VALUE, (byte) 111, (byte) 11, (byte) 1, (byte) 0,
                    (byte) -1, (byte) -11, (byte) -111, Byte.MIN_VALUE};
        byte[] d = new byte[c.length];
        p = Parcel.obtain();
        p.writeByteArray(null);
        p.setDataPosition(0);
        try {
            p.readByteArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readByteArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeByteArray(a);
        p.setDataPosition(0);
        try {
            p.readByteArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readByteArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeByteArray(c);
        p.setDataPosition(0);
        try {
            p.readByteArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readByteArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeByteArray",
            args = {byte[].class, int.class, int.class}
        )
    })
    public void testWriteByteArray() {
        Parcel p;
        byte[] a = {(byte) 21};
        byte[] b = new byte[a.length];
        byte[] c = {Byte.MAX_VALUE, (byte) 111, (byte) 11, (byte) 1, (byte) 0,
                    (byte) -1, (byte) -11, (byte) -111, Byte.MIN_VALUE};
        byte[] d = new byte[c.length - 2];
        p = Parcel.obtain();
        p.writeByteArray(null, 0, 2);
        p.setDataPosition(0);
        try {
            p.readByteArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readByteArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        try {
            p.writeByteArray(a, 0, 2);
            fail("Should throw a ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        try {
            p.writeByteArray(a, -1, 1);
            fail("Should throw a ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        try {
            p.writeByteArray(a, 0, -1);
            fail("Should throw a ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeByteArray(a, 0 , 1);
        p.setDataPosition(0);
        try {
            p.readByteArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readByteArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeByteArray(c, 1, 7);
        p.setDataPosition(0);
        try {
            p.readByteArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        d = new byte[c.length - 2];
        p.setDataPosition(0);
        p.readByteArray(d);
        assertEquals(7, d.length);;
        for (int i = 0; i < d.length; i++) {
            Log.d("Trace", "i=" + i + " d[i]=" + d[i]);
        }
        for (int i = 0; i < 7; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createByteArray",
            args = {}
        )
    })
    public void testCreateByteArray() {
        Parcel p;
        byte[] a = {(byte) 21};
        byte[] b;
        byte[] c = {Byte.MAX_VALUE, (byte) 111, (byte) 11, (byte) 1, (byte) 0,
                    (byte) -1, (byte) -11, (byte) -111, Byte.MIN_VALUE};
        byte[] d;
        byte[] e = {};
        byte[] f;
        p = Parcel.obtain();
        p.writeByteArray(null);
        p.setDataPosition(0);
        b = p.createByteArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeByteArray(e);
        p.setDataPosition(0);
        f = p.createByteArray();
        assertNotNull(f);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeByteArray(a);
        p.setDataPosition(0);
        b = p.createByteArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeByteArray(c);
        p.setDataPosition(0);
        d = p.createByteArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readCharArray",
            args = {char[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeCharArray",
            args = {char[].class}
        )
    })
    public void testReadCharArray() {
        Parcel p;
        char[] a = {'a'};
        char[] b = new char[a.length];
        char[] c = {'a', Character.MAX_VALUE, Character.MIN_VALUE, Character.MAX_SURROGATE, Character.MIN_SURROGATE,
                    Character.MAX_HIGH_SURROGATE, Character.MAX_LOW_SURROGATE,
                    Character.MIN_HIGH_SURROGATE, Character.MIN_LOW_SURROGATE};
        char[] d = new char[c.length];
        p = Parcel.obtain();
        p.writeCharArray(null);
        p.setDataPosition(0);
        try {
            p.readCharArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readCharArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeCharArray(a);
        p.setDataPosition(0);
        try {
            p.readCharArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readCharArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeCharArray(c);
        p.setDataPosition(0);
        try {
            p.readCharArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readCharArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createCharArray",
            args = {}
        )
    })
    public void testCreateCharArray() {
        Parcel p;
        char[] a = {'a'};
        char[] b;
        char[] c = {'a', Character.MAX_VALUE, Character.MIN_VALUE, Character.MAX_SURROGATE, Character.MIN_SURROGATE,
                    Character.MAX_HIGH_SURROGATE, Character.MAX_LOW_SURROGATE,
                    Character.MIN_HIGH_SURROGATE, Character.MIN_LOW_SURROGATE};
        char[] d;
        char[] e = {};
        char[] f;
        p = Parcel.obtain();
        p.writeCharArray(null);
        p.setDataPosition(0);
        b = p.createCharArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeCharArray(e);
        p.setDataPosition(0);
        f = p.createCharArray();
        assertNotNull(e);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeCharArray(a);
        p.setDataPosition(0);
        b = p.createCharArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeCharArray(c);
        p.setDataPosition(0);
        d = p.createCharArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readInt",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeInt",
            args = {int.class}
        )
    })
    public void testReadInt() {
        Parcel p;
        p = Parcel.obtain();
        p.writeInt(0);
        p.setDataPosition(0);
        assertEquals(0, p.readInt());
        p.recycle();
        p = Parcel.obtain();
        p.writeInt(1);
        p.setDataPosition(0);
        assertEquals(1, p.readInt());
        p.recycle();
        p = Parcel.obtain();
        p.writeInt(-1);
        p.setDataPosition(0);
        assertEquals(-1, p.readInt());
        p.recycle();
        p = Parcel.obtain();
        p.writeInt(Integer.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Integer.MAX_VALUE, p.readInt());
        p.recycle();
        p = Parcel.obtain();
        p.writeInt(Integer.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Integer.MIN_VALUE, p.readInt());
        p.recycle();
        p = Parcel.obtain();
        p.writeInt(Integer.MAX_VALUE);
        p.writeInt(11);
        p.writeInt(1);
        p.writeInt(0);
        p.writeInt(-1);
        p.writeInt(-11);
        p.writeInt(Integer.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Integer.MAX_VALUE, p.readInt());
        assertEquals(11, p.readInt());
        assertEquals(1, p.readInt());
        assertEquals(0, p.readInt());
        assertEquals(-1, p.readInt());
        assertEquals(-11, p.readInt());
        assertEquals(Integer.MIN_VALUE, p.readInt());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readIntArray",
            args = {int[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeIntArray",
            args = {int[].class}
        )
    })
    public void testReadIntArray() {
        Parcel p;
        int[] a = {21};
        int[] b = new int[a.length];
        int[] c = {Integer.MAX_VALUE, 111, 11, 1, 0, -1, -11, -111, Integer.MIN_VALUE};
        int[] d = new int[c.length];
        p = Parcel.obtain();
        p.writeIntArray(null);
        p.setDataPosition(0);
        try {
            p.readIntArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readIntArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeIntArray(a);
        p.setDataPosition(0);
        try {
            p.readIntArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readIntArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeIntArray(c);
        p.setDataPosition(0);
        try {
            p.readIntArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readIntArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createIntArray",
            args = {}
        )
    })
    public void testCreateIntArray() {
        Parcel p;
        int[] a = {21};
        int[] b;
        int[] c = {Integer.MAX_VALUE, 111, 11, 1, 0, -1, -11, -111, Integer.MIN_VALUE};
        int[] d;
        int[] e = {};
        int[] f;
        p = Parcel.obtain();
        p.writeIntArray(null);
        p.setDataPosition(0);
        b = p.createIntArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeIntArray(e);
        p.setDataPosition(0);
        f = p.createIntArray();
        assertNotNull(e);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeIntArray(a);
        p.setDataPosition(0);
        b = p.createIntArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeIntArray(c);
        p.setDataPosition(0);
        d = p.createIntArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readLong",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeLong",
            args = {long.class}
        )
    })
    public void testReadLong() {
        Parcel p;
        p = Parcel.obtain();
        p.writeLong(0L);
        p.setDataPosition(0);
        assertEquals(0, p.readLong());
        p.recycle();
        p = Parcel.obtain();
        p.writeLong(1L);
        p.setDataPosition(0);
        assertEquals(1, p.readLong());
        p.recycle();
        p = Parcel.obtain();
        p.writeLong(-1L);
        p.setDataPosition(0);
        assertEquals(-1L, p.readLong());
        p.recycle();
        p = Parcel.obtain();
        p.writeLong(Long.MAX_VALUE);
        p.writeLong(11L);
        p.writeLong(1L);
        p.writeLong(0L);
        p.writeLong(-1L);
        p.writeLong(-11L);
        p.writeLong(Long.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Long.MAX_VALUE, p.readLong());
        assertEquals(11L, p.readLong());
        assertEquals(1L, p.readLong());
        assertEquals(0L, p.readLong());
        assertEquals(-1L, p.readLong());
        assertEquals(-11L, p.readLong());
        assertEquals(Long.MIN_VALUE, p.readLong());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readLongArray",
            args = {long[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeLongArray",
            args = {long[].class}
        )
    })
    public void testReadLongArray() {
        Parcel p;
        long[] a = {21L};
        long[] b = new long[a.length];
        long[] c = {Long.MAX_VALUE, 111L, 11L, 1L, 0L, -1L, -11L, -111L, Long.MIN_VALUE};
        long[] d = new long[c.length];
        p = Parcel.obtain();
        p.writeLongArray(null);
        p.setDataPosition(0);
        try {
            p.readLongArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readLongArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeLongArray(a);
        p.setDataPosition(0);
        try {
            p.readLongArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readLongArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeLongArray(c);
        p.setDataPosition(0);
        try {
            p.readLongArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readLongArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createLongArray",
            args = {}
        )
    })
    public void testCreateLongArray() {
        Parcel p;
        long[] a = {21L};
        long[] b;
        long[] c = {Long.MAX_VALUE, 111L, 11L, 1L, 0L, -1L, -11L, -111L, Long.MIN_VALUE};
        long[] d;
        long[] e = {};
        long[] f;
        p = Parcel.obtain();
        p.writeLongArray(null);
        p.setDataPosition(0);
        b = p.createLongArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeLongArray(e);
        p.setDataPosition(0);
        f = p.createLongArray();
        assertNotNull(e);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeLongArray(a);
        p.setDataPosition(0);
        b = p.createLongArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeLongArray(c);
        p.setDataPosition(0);
        d = p.createLongArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFloat",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeFloat",
            args = {float.class}
        )
    })
    public void testReadFloat() {
        Parcel p;
        p = Parcel.obtain();
        p.writeFloat(.0f);
        p.setDataPosition(0);
        assertEquals(.0f, p.readFloat());
        p.recycle();
        p = Parcel.obtain();
        p.writeFloat(0.1f);
        p.setDataPosition(0);
        assertEquals(0.1f, p.readFloat());
        p.recycle();
        p = Parcel.obtain();
        p.writeFloat(-1.1f);
        p.setDataPosition(0);
        assertEquals(-1.1f, p.readFloat());
        p.recycle();
        p = Parcel.obtain();
        p.writeFloat(Float.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Float.MAX_VALUE, p.readFloat());
        p.recycle();
        p = Parcel.obtain();
        p.writeFloat(Float.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Float.MIN_VALUE, p.readFloat());
        p.recycle();
        p = Parcel.obtain();
        p.writeFloat(Float.MAX_VALUE);
        p.writeFloat(1.1f);
        p.writeFloat(0.1f);
        p.writeFloat(.0f);
        p.writeFloat(-0.1f);
        p.writeFloat(-1.1f);
        p.writeFloat(Float.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Float.MAX_VALUE, p.readFloat());
        assertEquals(1.1f, p.readFloat());
        assertEquals(0.1f, p.readFloat());
        assertEquals(.0f, p.readFloat());
        assertEquals(-0.1f, p.readFloat());
        assertEquals(-1.1f, p.readFloat());
        assertEquals(Float.MIN_VALUE, p.readFloat());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFloatArray",
            args = {float[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeFloatArray",
            args = {float[].class}
        )
    })
    public void testReadFloatArray() {
        Parcel p;
        float[] a = {2.1f};
        float[] b = new float[a.length];
        float[] c = {Float.MAX_VALUE, 11.1f, 1.1f, 0.1f, .0f, -0.1f, -1.1f, -11.1f, Float.MIN_VALUE};
        float[] d = new float[c.length];
        p = Parcel.obtain();
        p.writeFloatArray(null);
        p.setDataPosition(0);
        try {
            p.readFloatArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readFloatArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeFloatArray(a);
        p.setDataPosition(0);
        try {
            p.readFloatArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readFloatArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeFloatArray(c);
        p.setDataPosition(0);
        try {
            p.readFloatArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readFloatArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createFloatArray",
            args = {}
        )
    })
    public void testCreateFloatArray() {
        Parcel p;
        float[] a = {2.1f};
        float[] b;
        float[] c = {Float.MAX_VALUE, 11.1f, 1.1f, 0.1f, .0f, -0.1f, -1.1f, -11.1f, Float.MIN_VALUE};
        float[] d;
        float[] e = {};
        float[] f;
        p = Parcel.obtain();
        p.writeFloatArray(null);
        p.setDataPosition(0);
        b = p.createFloatArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeFloatArray(e);
        p.setDataPosition(0);
        f = p.createFloatArray();
        assertNotNull(f);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeFloatArray(a);
        p.setDataPosition(0);
        b = p.createFloatArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeFloatArray(c);
        p.setDataPosition(0);
        d = p.createFloatArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readDouble",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeDouble",
            args = {double.class}
        )
    })
    public void testReadDouble() {
        Parcel p;
        p = Parcel.obtain();
        p.writeDouble(.0d);
        p.setDataPosition(0);
        assertEquals(.0d, p.readDouble());
        p.recycle();
        p = Parcel.obtain();
        p.writeDouble(0.1d);
        p.setDataPosition(0);
        assertEquals(0.1d, p.readDouble());
        p.recycle();
        p = Parcel.obtain();
        p.writeDouble(-1.1d);
        p.setDataPosition(0);
        assertEquals(-1.1d, p.readDouble());
        p.recycle();
        p = Parcel.obtain();
        p.writeDouble(Double.MAX_VALUE);
        p.setDataPosition(0);
        assertEquals(Double.MAX_VALUE, p.readDouble());
        p.recycle();
        p = Parcel.obtain();
        p.writeDouble(Double.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Double.MIN_VALUE, p.readDouble());
        p.recycle();
        p = Parcel.obtain();
        p.writeDouble(Double.MAX_VALUE);
        p.writeDouble(1.1d);
        p.writeDouble(0.1d);
        p.writeDouble(.0d);
        p.writeDouble(-0.1d);
        p.writeDouble(-1.1d);
        p.writeDouble(Double.MIN_VALUE);
        p.setDataPosition(0);
        assertEquals(Double.MAX_VALUE, p.readDouble());
        assertEquals(1.1d, p.readDouble());
        assertEquals(0.1d, p.readDouble());
        assertEquals(.0d, p.readDouble());
        assertEquals(-0.1d, p.readDouble());
        assertEquals(-1.1d, p.readDouble());
        assertEquals(Double.MIN_VALUE, p.readDouble());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readDoubleArray",
            args = {double[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeDoubleArray",
            args = {double[].class}
        )
    })
    public void testReadDoubleArray() {
        Parcel p;
        double[] a = {2.1d};
        double[] b = new double[a.length];
        double[] c = {Double.MAX_VALUE, 11.1d, 1.1d, 0.1d, .0d, -0.1d, -1.1d, -11.1d, Double.MIN_VALUE};
        double[] d = new double[c.length];
        p = Parcel.obtain();
        p.writeDoubleArray(null);
        p.setDataPosition(0);
        try {
            p.readDoubleArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readDoubleArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeDoubleArray(a);
        p.setDataPosition(0);
        try {
            p.readDoubleArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readDoubleArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeDoubleArray(c);
        p.setDataPosition(0);
        try {
            p.readDoubleArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readDoubleArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createDoubleArray",
            args = {}
        )
    })
    public void testCreateDoubleArray() {
        Parcel p;
        double[] a = {2.1d};
        double[] b;
        double[] c = {
                Double.MAX_VALUE, 11.1d, 1.1d, 0.1d, .0d, -0.1d, -1.1d, -11.1d, Double.MIN_VALUE
        };
        double[] d;
        double[] e = {};
        double[] f;
        p = Parcel.obtain();
        p.writeDoubleArray(null);
        p.setDataPosition(0);
        b = p.createDoubleArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeDoubleArray(e);
        p.setDataPosition(0);
        f = p.createDoubleArray();
        assertNotNull(f);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeDoubleArray(a);
        p.setDataPosition(0);
        b = p.createDoubleArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeDoubleArray(c);
        p.setDataPosition(0);
        d = p.createDoubleArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readBooleanArray",
            args = {boolean[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeBooleanArray",
            args = {boolean[].class}
        )
    })
    public void testReadBooleanArray() {
        Parcel p;
        boolean[] a = {true};
        boolean[] b = new boolean[a.length];
        boolean[] c = {true, false, true, false};
        boolean[] d = new boolean[c.length];
        p = Parcel.obtain();
        p.writeBooleanArray(null);
        p.setDataPosition(0);
        try {
            p.readIntArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readBooleanArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeBooleanArray(a);
        p.setDataPosition(0);
        try {
            p.readBooleanArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readBooleanArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeBooleanArray(c);
        p.setDataPosition(0);
        try {
            p.readBooleanArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readBooleanArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createBooleanArray",
            args = {}
        )
    })
    public void testCreateBooleanArray() {
        Parcel p;
        boolean[] a = {true};
        boolean[] b;
        boolean[] c = {true, false, true, false};
        boolean[] d;
        boolean[] e = {};
        boolean[] f;
        p = Parcel.obtain();
        p.writeBooleanArray(null);
        p.setDataPosition(0);
        b = p.createBooleanArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeBooleanArray(e);
        p.setDataPosition(0);
        f = p.createBooleanArray();
        assertNotNull(f);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeBooleanArray(a);
        p.setDataPosition(0);
        b = p.createBooleanArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeBooleanArray(c);
        p.setDataPosition(0);
        d = p.createBooleanArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeString",
            args = {String.class}
        )
    })
    public void testReadString() {
        Parcel p;
        final String string = "Hello, Android!";
        p = Parcel.obtain();
        p.writeString(null);
        p.setDataPosition(0);
        assertNull(p.readString());
        p.recycle();
        p = Parcel.obtain();
        p.writeString("");
        p.setDataPosition(0);
        assertEquals("", p.readString());
        p.recycle();
        p = Parcel.obtain();
        p.writeString("a");
        p.setDataPosition(0);
        assertEquals("a", p.readString());
        p.recycle();
        p = Parcel.obtain();
        p.writeString(string);
        p.setDataPosition(0);
        assertEquals(string, p.readString());
        p.recycle();
        p = Parcel.obtain();
        p.writeString(string);
        p.writeString("a");
        p.writeString("");
        p.setDataPosition(0);
        assertEquals(string, p.readString());
        assertEquals("a", p.readString());
        assertEquals("", p.readString());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readStringArray",
            args = {String[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeStringArray",
            args = {String[].class}
        )
    })
    public void testReadStringArray() {
        Parcel p;
        String[] a = {"21"};
        String[] b = new String[a.length];
        String[] c = {"",
                "a",
                "Hello, Android!",
                "A long string that is used to test the api readStringArray(),"};
        String[] d = new String[c.length];
        p = Parcel.obtain();
        p.writeStringArray(null);
        p.setDataPosition(0);
        try {
            p.readStringArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readStringArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeStringArray(a);
        p.setDataPosition(0);
        try {
            p.readStringArray(d);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readStringArray(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeStringArray(c);
        p.setDataPosition(0);
        try {
            p.readStringArray(b);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readStringArray(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createStringArray",
            args = {}
        )
    })
    public void testCreateStringArray() {
        Parcel p;
        String[] a = {"21"};
        String[] b;
        String[] c = {"",
                "a",
                "Hello, Android!",
                "A long string that is used to test the api readStringArray(),"};
        String[] d;
        String[] e = {};
        String[] f;
        p = Parcel.obtain();
        p.writeStringArray(null);
        p.setDataPosition(0);
        b = p.createStringArray();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeStringArray(e);
        p.setDataPosition(0);
        f = p.createStringArray();
        assertNotNull(e);
        assertEquals(0, f.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeStringArray(a);
        p.setDataPosition(0);
        b = p.createStringArray();
        assertNotNull(b);
        for (int i = 0; i < a.length; i++) {
            assertEquals(a[i], b[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeStringArray(c);
        p.setDataPosition(0);
        d = p.createStringArray();
        assertNotNull(d);
        for (int i = 0; i < c.length; i++) {
            assertEquals(c[i], d[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readStringList",
            args = {List.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeStringList",
            args = {List.class}
        )
    })
    public void testReadStringList() {
        Parcel p;
        ArrayList<String> a = new ArrayList<String>();
        a.add("21");
        ArrayList<String> b = new ArrayList<String>();
        ArrayList<String> c = new ArrayList<String>();
        c.add("");
        c.add("a");
        c.add("Hello, Android!");
        c.add("A long string that is used to test the api readStringList(),");
        ArrayList<String> d = new ArrayList<String>();
        p = Parcel.obtain();
        p.writeStringList(null);
        p.setDataPosition(0);
        try {
            p.readStringList(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readStringList(b);
        assertTrue(0 == b.size());
        p.recycle();
        p = Parcel.obtain();
        p.writeStringList(a);
        p.setDataPosition(0);
        assertTrue(c.size() > a.size());
        p.readStringList(c);
        assertTrue(c.size() == a.size());
        assertEquals(a, c);
        p.setDataPosition(0);
        assertTrue(0 == b.size() && 0 != a.size());
        p.readStringList(b);
        assertEquals(a, b);
        p.recycle();
        c = new ArrayList<String>();
        c.add("");
        c.add("a");
        c.add("Hello, Android!");
        c.add("A long string that is used to test the api readStringList(),");
        p = Parcel.obtain();
        p.writeStringList(c);
        p.setDataPosition(0);
        assertTrue(b.size() < c.size());
        p.readStringList(b);
        assertTrue(b.size() == c.size());
        assertEquals(c, b);
        p.setDataPosition(0);
        assertTrue(d.size() < c.size());
        p.readStringList(d);
        assertEquals(c, d);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createStringArrayList",
            args = {}
        )
    })
    public void testCreateStringArrayList() {
        Parcel p;
        ArrayList<String> a = new ArrayList<String>();
        a.add("21");
        ArrayList<String> b;
        ArrayList<String> c = new ArrayList<String>();
        c.add("");
        c.add("a");
        c.add("Hello, Android!");
        c.add("A long string that is used to test the api readStringList(),");
        ArrayList<String> d;
        ArrayList<String> e = new ArrayList<String>();
        ArrayList<String> f = null;
        p = Parcel.obtain();
        p.writeStringList(null);
        p.setDataPosition(0);
        b = p.createStringArrayList();
        assertNull(b);
        p.recycle();
        p = Parcel.obtain();
        p.writeStringList(e);
        p.setDataPosition(0);
        assertNull(f);
        f = p.createStringArrayList();
        assertNotNull(f);
        p.recycle();
        p = Parcel.obtain();
        p.writeStringList(a);
        p.setDataPosition(0);
        b = p.createStringArrayList();
        assertEquals(a, b);
        p.recycle();
        p = Parcel.obtain();
        p.writeStringList(c);
        p.setDataPosition(0);
        d = p.createStringArrayList();
        assertEquals(c, d);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readSerializable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeSerializable",
            args = {Serializable.class}
        )
    })
    public void testReadSerializable() {
        Parcel p;
        p = Parcel.obtain();
        p.writeSerializable(null);
        p.setDataPosition(0);
        assertNull(p.readSerializable());
        p.recycle();
        p = Parcel.obtain();
        p.writeSerializable("Hello, Android!");
        p.setDataPosition(0);
        assertEquals("Hello, Android!", p.readSerializable());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readParcelable",
            args = {ClassLoader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeParcelable",
            args = {Parcelable.class, int.class}
        )
    })
    public void testReadParcelable() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        final String signatureString  = "1234567890abcdef";
        Signature s = new Signature(signatureString);
        p = Parcel.obtain();
        p.writeParcelable(null, 0);
        p.setDataPosition(0);
        assertNull(p.readParcelable(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeParcelable(s, 0);
        p.setDataPosition(0);
        assertEquals(s, p.readParcelable(mcl));
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readParcelableArray",
            args = {ClassLoader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeParcelableArray",
            args = {Parcelable[].class, int.class}
        )
    })
    public void testReadParcelableArray() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        Signature[] s = {new Signature("123"),
                new Signature("ABC"),
                new Signature("abc")};
        Signature[] s2 = {new Signature("123"),
                null,
                new Signature("abc")};
        Parcelable[] s3;
        p = Parcel.obtain();
        p.writeParcelableArray(null, 0);
        p.setDataPosition(0);
        assertNull(p.readParcelableArray(mcl));
        p.recycle();
        p = Parcel.obtain();
        p.writeParcelableArray(s, 0);
        p.setDataPosition(0);
        s3 = p.readParcelableArray(mcl);
        for (int i = 0; i < s.length; i++) {
            assertEquals(s[i], s3[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeParcelableArray(s2, 0);
        p.setDataPosition(0);
        s3 = p.readParcelableArray(mcl);
        for (int i = 0; i < s2.length; i++) {
            assertEquals(s2[i], s3[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readTypedArray",
            args = {Object[].class, android.os.Parcelable.Creator.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeTypedArray",
            args = {android.os.Parcelable[].class, int.class}
        )
    })
    public void testReadTypedArray() {
        Parcel p;
        Signature[] s = {new Signature("123"),
                new Signature("ABC"),
                new Signature("abc")};
        Signature[] s2 = {new Signature("123"),
                null,
                new Signature("abc")};
        Signature[] s3 = new Signature[3];
        Signature[] s4 = new Signature[4];
        p = Parcel.obtain();
        p.writeTypedArray(null, 0);
        p.setDataPosition(0);
        try {
            p.readTypedArray(s3, Signature.CREATOR);
            fail("should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readTypedArray(null, Signature.CREATOR);
            fail("should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeTypedArray(s, 0);
        p.setDataPosition(0);
        p.readTypedArray(s3, Signature.CREATOR);
        for (int i = 0; i < s.length; i++) {
            assertEquals(s[i], s3[i]);
        }
        p.setDataPosition(0);
        try {
            p.readTypedArray(null, Signature.CREATOR);
            fail("should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readTypedArray(s4, Signature.CREATOR);
            fail("should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        s3 = new Signature[s2.length];
        p = Parcel.obtain();
        p.writeTypedArray(s2, 0);
        p.setDataPosition(0);
        p.readTypedArray(s3, Signature.CREATOR);
        for (int i = 0; i < s.length; i++) {
            assertEquals(s2[i], s3[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeTypedArray",
            args = {Parcelable[].class, int.class}
        )
    })
    public void testReadTypedArray2() {
        Parcel p;
        Signature[] s = {
                new Signature("123"), new Signature("ABC"), new Signature("abc")
        };
        Signature[] s2 = {
                new Signature("123"), null, new Signature("abc")
        };
        Signature[] s3 = {
                null, null, null
        };
        p = Parcel.obtain();
        p.writeTypedArray(null, 0);
        p.setDataPosition(0);
        p.recycle();
        p = Parcel.obtain();
        p.writeTypedArray(s, 0);
        p.setDataPosition(0);
        p.readTypedArray(s3, Signature.CREATOR);
        for (int i = 0; i < s.length; i++) {
            assertEquals(s[i], s3[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeTypedArray(s2, 0);
        p.setDataPosition(0);
        p.readTypedArray(s3, Signature.CREATOR);
        for (int i = 0; i < s.length; i++) {
            assertEquals(s2[i], s3[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createTypedArray",
            args = {Parcelable.Creator.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeTypedArray",
            args = {Parcelable[].class, int.class}
        )
    })
    public void testCreateTypedArray() {
        Parcel p;
        Signature[] s = {new Signature("123"),
                new Signature("ABC"),
                new Signature("abc")};
        Signature[] s2 = {new Signature("123"),
                null,
                new Signature("abc")};
        Signature[] s3;
        p = Parcel.obtain();
        p.writeTypedArray(null, 0);
        p.setDataPosition(0);
        assertNull(p.createTypedArray(Signature.CREATOR));
        p.recycle();
        p = Parcel.obtain();
        p.writeTypedArray(s, 0);
        p.setDataPosition(0);
        s3 = p.createTypedArray(Signature.CREATOR);
        for (int i = 0; i < s.length; i++) {
            assertEquals(s[i], s3[i]);
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeTypedArray(s2, 0);
        p.setDataPosition(0);
        s3 = p.createTypedArray(Signature.CREATOR);
        for (int i = 0; i < s.length; i++) {
            assertEquals(s2[i], s3[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readTypedList",
            args = {List.class, Parcelable.Creator.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeTypedList",
            args = {List.class}
        )
    })
    public void testReadTypedList() {
        Parcel p;
        ArrayList<Signature> s = new ArrayList<Signature>();
        s.add(new Signature("123"));
        s.add(new Signature("ABC"));
        s.add(new Signature("abc"));
        ArrayList<Signature> s2 = new ArrayList<Signature>();
        s2.add(new Signature("123"));
        s2.add(null);
        ArrayList<Signature> s3 = new ArrayList<Signature>();
        p = Parcel.obtain();
        p.writeTypedList(null);
        p.setDataPosition(0);
        p.readTypedList(s3, Signature.CREATOR);
        assertEquals(0, s3.size());
        p.setDataPosition(0);
        try {
            p.readTypedList(null, Signature.CREATOR);
            fail("should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeTypedList(s);
        p.setDataPosition(0);
        p.readTypedList(s3, Signature.CREATOR);
        for (int i = 0; i < s.size(); i++) {
            assertEquals(s.get(i), s3.get(i));
        }
        p.setDataPosition(0);
        try {
            p.readTypedList(null, Signature.CREATOR);
            fail("should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readTypedList(s2, Signature.CREATOR);
        assertEquals(s.size(), s2.size());
        for (int i = 0; i < s.size(); i++) {
            assertEquals(s.get(i), s2.get(i));
        }
        p.recycle();
        s2 = new ArrayList<Signature>();
        s2.add(new Signature("123"));
        s2.add(null);
        p = Parcel.obtain();
        p.writeTypedList(s2);
        p.setDataPosition(0);
        p.readTypedList(s3, Signature.CREATOR);
        assertEquals(s3.size(), s2.size());
        for (int i = 0; i < s2.size(); i++) {
            assertEquals(s2.get(i), s3.get(i));
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createTypedArrayList",
            args = {Parcelable.Creator.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeTypedList",
            args = {List.class}
        )
    })
    public void testCreateTypedArrayList() {
        Parcel p;
        ArrayList<Signature> s = new ArrayList<Signature>();
        s.add(new Signature("123"));
        s.add(new Signature("ABC"));
        s.add(new Signature("abc"));
        ArrayList<Signature> s2 = new ArrayList<Signature>();
        s2.add(new Signature("123"));
        s2.add(null);
        ArrayList<Signature> s3;
        p = Parcel.obtain();
        p.writeTypedList(null);
        p.setDataPosition(0);
        assertNull(p.createTypedArrayList(Signature.CREATOR));
        p.recycle();
        p = Parcel.obtain();
        p.writeTypedList(s);
        p.setDataPosition(0);
        s3 = p.createTypedArrayList(Signature.CREATOR);
        for (int i = 0; i < s.size(); i++) {
            assertEquals(s.get(i), s3.get(i));
        }
        p = Parcel.obtain();
        p.writeTypedList(s2);
        p.setDataPosition(0);
        s3 = p.createTypedArrayList(Signature.CREATOR);
        assertEquals(s3.size(), s2.size());
        for (int i = 0; i < s2.size(); i++) {
            assertEquals(s2.get(i), s3.get(i));
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "readException",
            args = {int.class, String.class}
        )
    })
    @ToBeFixed(bug = "1668892", explanation = "The javadoc for readException(int, String) "
                                               + "is not clear at all")
    public void testReadException() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeException",
            args = {Exception.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readException",
            args = {}
        )
    })
    public void testReadException2() {
        Parcel p = Parcel.obtain();
        String msg = "testReadException2";
        p.writeException(new SecurityException(msg));
        p.setDataPosition(0);
        try {
            p.readException();
            fail("Should throw a SecurityException");
        } catch (SecurityException e) {
            assertEquals(msg, e.getMessage());
        }
        p.setDataPosition(0);
        p.writeException(new BadParcelableException(msg));
        p.setDataPosition(0);
        try {
            p.readException();
            fail("Should throw a BadParcelableException");
        } catch (BadParcelableException e) {
            assertEquals(msg, e.getMessage());
        }
        p.setDataPosition(0);
        p.writeException(new IllegalArgumentException(msg));
        p.setDataPosition(0);
        try {
            p.readException();
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(msg, e.getMessage());
        }
        p.setDataPosition(0);
        p.writeException(new NullPointerException(msg));
        p.setDataPosition(0);
        try {
            p.readException();
            fail("Should throw a NullPointerException");
        } catch (NullPointerException e) {
            assertEquals(msg, e.getMessage());
        }
        p.setDataPosition(0);
        p.writeException(new IllegalStateException(msg));
        p.setDataPosition(0);
        try {
            p.readException();
            fail("Should throw an IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals(msg, e.getMessage());
        }
        p.setDataPosition(0);
        try {
            p.writeException(new RuntimeException());
            fail("Should throw an IllegalStateException");
        } catch (RuntimeException e) {
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeNoException",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readException",
            args = {}
        )
    })
    public void testWriteNoException() {
        Parcel p = Parcel.obtain();
        p.writeNoException();
        p.setDataPosition(0);
        p.readException();
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeFileDescriptor",
            args = {FileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readFileDescriptor",
            args = {}
        )
    })
    public void testWriteFileDescriptor() {
        Parcel p;
        FileDescriptor fIn = FileDescriptor.in;
        ParcelFileDescriptor pfd;
        p = Parcel.obtain();
        pfd = p.readFileDescriptor();
        assertNull(pfd);
        p.recycle();
        p = Parcel.obtain();
        p.writeFileDescriptor(fIn);
        p.setDataPosition(0);
        pfd = p.readFileDescriptor();
        assertNotNull(pfd);
        assertNotNull(pfd.getFileDescriptor());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasFileDescriptors",
            args = {}
        )
    })
    public void testHasFileDescriptor() {
        Parcel p;
        FileDescriptor fIn = FileDescriptor.in;
        p = Parcel.obtain();
        p.writeFileDescriptor(fIn);
        p.setDataPosition(0);
        assertTrue(p.hasFileDescriptors());
        p.recycle();
        p = Parcel.obtain();
        p.writeInt(111);
        p.setDataPosition(0);
        assertFalse(p.hasFileDescriptors());
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readBundle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeBundle",
            args = {Bundle.class}
        )
    })
    public void testReadBundle() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("boolean", true);
        bundle.putInt("int", Integer.MAX_VALUE);
        bundle.putString("string", "String");
        Bundle bundle2;
        Parcel p;
        p = Parcel.obtain();
        p.writeBundle(null);
        p.setDataPosition(0);
        bundle2 = p.readBundle();
        assertNull(bundle2);
        p.recycle();
        bundle2 = null;
        p = Parcel.obtain();
        p.writeBundle(bundle);
        p.setDataPosition(0);
        bundle2 = p.readBundle();
        assertNotNull(bundle2);
        assertEquals(true, bundle2.getBoolean("boolean"));
        assertEquals(Integer.MAX_VALUE, bundle2.getInt("int"));
        assertEquals("String", bundle2.getString("string"));
        p.recycle();
        bundle2 = null;
        Parcel a = Parcel.obtain();
        bundle2 = new Bundle();
        bundle2.putString("foo", "test");
        a.writeBundle(bundle2);
        a.setDataPosition(0);
        bundle.readFromParcel(a);
        p = Parcel.obtain();
        p.setDataPosition(0);
        p.writeBundle(bundle);
        p.setDataPosition(0);
        bundle2 = p.readBundle();
        assertNotNull(bundle2);
        assertFalse(true == bundle2.getBoolean("boolean"));
        assertFalse(Integer.MAX_VALUE == bundle2.getInt("int"));
        assertFalse("String".equals( bundle2.getString("string")));
        a.recycle();
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: readBundle ",
            method = "readBundle",
            args = {ClassLoader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test method: writeBundle ",
            method = "writeBundle",
            args = {Bundle.class}
        )
    })
    public void testReadBundle2() {
        Bundle b = new Bundle();
        b.putBoolean("boolean", true);
        b.putInt("int", Integer.MAX_VALUE);
        b.putString("string", "String");
        Bundle u;
        Parcel p;
        MockClassLoader m = new MockClassLoader();
        p = Parcel.obtain();
        p.writeBundle(null);
        p.setDataPosition(0);
        u = p.readBundle(m);
        assertNull(u);
        p.recycle();
        u = null;
        p = Parcel.obtain();
        p.writeBundle(b);
        p.setDataPosition(0);
        u = p.readBundle(m);
        assertNotNull(u);
        assertEquals(true, b.getBoolean("boolean"));
        assertEquals(Integer.MAX_VALUE, b.getInt("int"));
        assertEquals("String", b.getString("string"));
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeArray",
            args = {Object[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readArray",
            args = {ClassLoader.class}
        )
    })
    public void testWriteArray() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        p = Parcel.obtain();
        p.writeArray(null);
        p.setDataPosition(0);
        assertNull(p.readArray(mcl));
        p.recycle();
        Object[] objects = new Object[5];
        objects[0] = Integer.MAX_VALUE;
        objects[1] = true;
        objects[2] = Long.MAX_VALUE;
        objects[3] = "String";
        objects[4] = Float.MAX_VALUE;
        Object[] objects2;
        p = Parcel.obtain();
        p.writeArray(objects);
        p.setDataPosition(0);
        objects2 = p.readArray(mcl);
        assertNotNull(objects2);
        for (int i = 0; i < objects2.length; i++) {
            assertEquals(objects[i], objects2[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readArrayList",
            args = {ClassLoader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeArray",
            args = {Object[].class}
        )
    })
    public void testReadArrayList() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        p = Parcel.obtain();
        p.writeArray(null);
        p.setDataPosition(0);
        assertNull(p.readArrayList(mcl));
        p.recycle();
        Object[] objects = new Object[5];
        objects[0] = Integer.MAX_VALUE;
        objects[1] = true;
        objects[2] = Long.MAX_VALUE;
        objects[3] = "String";
        objects[4] = Float.MAX_VALUE;
        ArrayList<?> objects2;
        p = Parcel.obtain();
        p.writeArray(objects);
        p.setDataPosition(0);
        objects2 = p.readArrayList(mcl);
        assertNotNull(objects2);
        for (int i = 0; i < objects2.size(); i++) {
            assertEquals(objects[i], objects2.get(i));
        }
        p.recycle();
    }
    @SuppressWarnings("unchecked")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeSparseArray",
            args = {SparseArray.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readSparseArray",
            args = {ClassLoader.class}
        )
    })
    public void testWriteSparseArray() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        p = Parcel.obtain();
        p.writeSparseArray(null);
        p.setDataPosition(0);
        assertNull(p.readSparseArray(mcl));
        p.recycle();
        SparseArray<Object> sparseArray = new SparseArray<Object>();
        sparseArray.put(3, "String");
        sparseArray.put(2, Long.MAX_VALUE);
        sparseArray.put(4, Float.MAX_VALUE);
        sparseArray.put(0, Integer.MAX_VALUE);
        sparseArray.put(1, true);
        sparseArray.put(10, true);
        SparseArray<Object> sparseArray2;
        p = Parcel.obtain();
        p.writeSparseArray(sparseArray);
        p.setDataPosition(0);
        sparseArray2 = p.readSparseArray(mcl);
        assertNotNull(sparseArray2);
        assertEquals(sparseArray.size(), sparseArray2.size());
        assertEquals(sparseArray.get(0), sparseArray2.get(0));
        assertEquals(sparseArray.get(1), sparseArray2.get(1));
        assertEquals(sparseArray.get(2), sparseArray2.get(2));
        assertEquals(sparseArray.get(3), sparseArray2.get(3));
        assertEquals(sparseArray.get(4), sparseArray2.get(4));
        assertEquals(sparseArray.get(10), sparseArray2.get(10));
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeSparseBooleanArray",
            args = {SparseBooleanArray.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readSparseBooleanArray",
            args = {}
        )
    })
    public void testWriteSparseBooleanArray() {
        Parcel p;
        p = Parcel.obtain();
        p.writeSparseArray(null);
        p.setDataPosition(0);
        assertNull(p.readSparseBooleanArray());
        p.recycle();
        SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();
        sparseBooleanArray.put(3, true);
        sparseBooleanArray.put(2, false);
        sparseBooleanArray.put(4, false);
        sparseBooleanArray.put(0, true);
        sparseBooleanArray.put(1, true);
        sparseBooleanArray.put(10, true);
        SparseBooleanArray sparseBoolanArray2;
        p = Parcel.obtain();
        p.writeSparseBooleanArray(sparseBooleanArray);
        p.setDataPosition(0);
        sparseBoolanArray2 = p.readSparseBooleanArray();
        assertNotNull(sparseBoolanArray2);
        assertEquals(sparseBooleanArray.size(), sparseBoolanArray2.size());
        assertEquals(sparseBooleanArray.get(0), sparseBoolanArray2.get(0));
        assertEquals(sparseBooleanArray.get(1), sparseBoolanArray2.get(1));
        assertEquals(sparseBooleanArray.get(2), sparseBoolanArray2.get(2));
        assertEquals(sparseBooleanArray.get(3), sparseBoolanArray2.get(3));
        assertEquals(sparseBooleanArray.get(4), sparseBoolanArray2.get(4));
        assertEquals(sparseBooleanArray.get(10), sparseBoolanArray2.get(10));
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeStrongBinder",
            args = {IBinder.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readStrongBinder",
            args = {}
        )
    })
    public void testWriteStrongBinder() {
        Parcel p;
        Binder binder;
        Binder binder2 = new Binder();
        p = Parcel.obtain();
        p.writeStrongBinder(null);
        p.setDataPosition(0);
        assertNull(p.readStrongBinder());
        p.recycle();
        p = Parcel.obtain();
        p.writeStrongBinder(binder2);
        p.setDataPosition(0);
        binder = (Binder) p.readStrongBinder();
        assertEquals(binder2, binder);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeStrongInterface",
            args = {IInterface.class}
        )
    })
    public void testWriteStrongInterface() {
        Parcel p;
        MockIInterface mockInterface = new MockIInterface();
        MockIInterface mockIInterface2 = new MockIInterface();
        p = Parcel.obtain();
        p.writeStrongInterface(null);
        p.setDataPosition(0);
        assertNull(p.readStrongBinder());
        p.recycle();
        p = Parcel.obtain();
        p.writeStrongInterface(mockInterface);
        p.setDataPosition(0);
        mockIInterface2.binder = (Binder) p.readStrongBinder();
        assertEquals(mockInterface.binder, mockIInterface2.binder);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeBinderArray",
            args = {IBinder[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readBinderArray",
            args = {IBinder[].class}
        )
    })
    public void testWriteBinderArray() {
        Parcel p;
        IBinder[] ibinder2 = {new Binder(), new Binder()};
        IBinder[] ibinder3 = new IBinder[2];
        IBinder[] ibinder4 = new IBinder[3];
        p = Parcel.obtain();
        p.writeBinderArray(null);
        p.setDataPosition(0);
        try {
            p.readBinderArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readBinderArray(ibinder3);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readBinderArray(ibinder2);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.recycle();
        p = Parcel.obtain();
        p.writeBinderArray(ibinder2);
        p.setDataPosition(0);
        try {
            p.readBinderArray(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        try {
            p.readBinderArray(ibinder4);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        p.readBinderArray(ibinder3);
        assertNotNull(ibinder3);
        for (int i = 0; i < ibinder3.length; i++) {
            assertNotNull(ibinder3[i]);
            assertEquals(ibinder2[i], ibinder3[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createBinderArray",
            args = {}
        )
    })
    public void testCreateBinderArray() {
        Parcel p;
        IBinder[] ibinder  = {};
        IBinder[] ibinder2 = {new Binder(), new Binder()};
        IBinder[] ibinder3;
        IBinder[] ibinder4;
        p = Parcel.obtain();
        p.writeBinderArray(null);
        p.setDataPosition(0);
        ibinder3 = p.createBinderArray();
        assertNull(ibinder3);
        p.recycle();
        p = Parcel.obtain();
        p.writeBinderArray(ibinder);
        p.setDataPosition(0);
        ibinder4 = p.createBinderArray();
        assertNotNull(ibinder4);
        assertEquals(0, ibinder4.length);
        p.recycle();
        p = Parcel.obtain();
        p.writeBinderArray(ibinder2);
        p.setDataPosition(0);
        ibinder3 = p.createBinderArray();
        assertNotNull(ibinder3);
        for (int i = 0; i < ibinder3.length; i++) {
            assertNotNull(ibinder3[i]);
            assertEquals(ibinder2[i], ibinder3[i]);
        }
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeBinderList",
            args = {List.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readBinderList",
            args = {List.class}
        )
    })
    public void testWriteBinderList() {
        Parcel p;
        ArrayList<IBinder> arrayList = new ArrayList<IBinder>();
        ArrayList<IBinder> arrayList2 = new ArrayList<IBinder>();
        arrayList2.add(new Binder());
        arrayList2.add(new Binder());
        ArrayList<IBinder> arrayList3 = new ArrayList<IBinder>();
        arrayList3.add(new Binder());
        arrayList3.add(new Binder());
        arrayList3.add(new Binder());
        p = Parcel.obtain();
        p.writeBinderList(null);
        p.setDataPosition(0);
        try {
            p.readBinderList(null);
            fail("Should throw a RuntimeException");
        } catch (RuntimeException e) {
        }
        p.setDataPosition(0);
        assertEquals(0, arrayList.size());
        p.readBinderList(arrayList);
        assertEquals(0, arrayList.size());
        p.recycle();
        p = Parcel.obtain();
        p.writeBinderList(arrayList2);
        p.setDataPosition(0);
        assertEquals(0, arrayList.size());
        p.readBinderList(arrayList);
        assertEquals(2, arrayList.size());
        assertEquals(arrayList2, arrayList);
        p.recycle();
        p = Parcel.obtain();
        p.writeBinderList(arrayList2);
        p.setDataPosition(0);
        assertEquals(3, arrayList3.size());
        p.readBinderList(arrayList3);
        assertEquals(2, arrayList3.size());
        assertEquals(arrayList2, arrayList3);
        p.recycle();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createBinderArrayList",
            args = {}
        )
    })
    public void testCreateBinderArrayList() {
        Parcel p;
        ArrayList<IBinder> arrayList = new ArrayList<IBinder>();
        ArrayList<IBinder> arrayList2 = new ArrayList<IBinder>();
        arrayList2.add(new Binder());
        arrayList2.add(new Binder());
        ArrayList<IBinder> arrayList3;
        ArrayList<IBinder> arrayList4;
        p = Parcel.obtain();
        p.writeBinderList(null);
        p.setDataPosition(0);
        arrayList3 = p.createBinderArrayList();
        assertNull(arrayList3);
        p.recycle();
        p = Parcel.obtain();
        p.writeBinderList(arrayList);
        p.setDataPosition(0);
        arrayList3 = p.createBinderArrayList();
        assertNotNull(arrayList3);
        assertEquals(0, arrayList3.size());
        p.recycle();
        p = Parcel.obtain();
        p.writeBinderList(arrayList2);
        p.setDataPosition(0);
        arrayList4 = p.createBinderArrayList();
        assertNotNull(arrayList4);
        assertEquals(arrayList2, arrayList4);
        p.recycle();
    }
    @SuppressWarnings("unchecked")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeMap",
            args = {Map.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readMap",
            args = {Map.class, ClassLoader.class}
        )
    })
    public void testWriteMap() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        HashMap map = new HashMap();
        HashMap map2 = new HashMap();
        p = Parcel.obtain();
        p.writeMap(null);
        p.setDataPosition(0);
        assertEquals(0, map2.size());
        p.readMap(map2, mcl);
        assertEquals(0, map2.size());
        p.recycle();
        map.put("string", "String");
        map.put("int", Integer.MAX_VALUE);
        map.put("boolean", true);
        p = Parcel.obtain();
        p.writeMap(map);
        p.setDataPosition(0);
        assertEquals(0, map2.size());
        p.readMap(map2, mcl);
        assertEquals(3, map2.size());
        assertEquals("String", map.get("string"));
        assertEquals(Integer.MAX_VALUE, map.get("int"));
        assertEquals(true, map.get("boolean"));
        p.recycle();
    }
    @SuppressWarnings("unchecked")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readHashMap",
            args = {ClassLoader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeMap",
            args = {Map.class}
        )
    })
    public void testReadHashMap() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        HashMap map = new HashMap();
        HashMap map2;
        p = Parcel.obtain();
        p.writeMap(null);
        p.setDataPosition(0);
        map2 = p.readHashMap(mcl);
        assertNull(map2);
        p.recycle();
        map.put("string", "String");
        map.put("int", Integer.MAX_VALUE);
        map.put("boolean", true);
        map2 = null;
        p = Parcel.obtain();
        p.writeMap(map);
        p.setDataPosition(0);
        map2 = p.readHashMap(mcl);
        assertNotNull(map2);
        assertEquals(3, map2.size());
        assertEquals("String", map.get("string"));
        assertEquals(Integer.MAX_VALUE, map.get("int"));
        assertEquals(true, map.get("boolean"));
        p.recycle();
    }
    @SuppressWarnings("unchecked")
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "readList",
            args = {List.class, ClassLoader.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeList",
            args = {List.class}
        )
    })
    public void testReadList() {
        Parcel p;
        MockClassLoader mcl = new MockClassLoader();
        ArrayList arrayList = new ArrayList();
        p = Parcel.obtain();
        p.writeList(null);
        p.setDataPosition(0);
        assertEquals(0, arrayList.size());
        p.readList(arrayList, mcl);
        assertEquals(0, arrayList.size());
        p.recycle();
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(Integer.MAX_VALUE);
        arrayList2.add(true);
        arrayList2.add(Long.MAX_VALUE);
        arrayList2.add("String");
        arrayList2.add(Float.MAX_VALUE);
        p = Parcel.obtain();
        p.writeList(arrayList2);
        p.setDataPosition(0);
        assertEquals(0, arrayList.size());
        p.readList(arrayList, mcl);
        assertEquals(5, arrayList.size());
        for (int i = 0; i < arrayList.size(); i++) {
            assertEquals(arrayList.get(i), arrayList2.get(i));
        }
        p.recycle();
    }
    private class MockClassLoader extends ClassLoader {
        public MockClassLoader() {
            super();
        }
    }
    private class MockIInterface implements IInterface {
        public Binder binder;
        public MockIInterface() {
            binder = new Binder();
        }
        public IBinder asBinder() {
            return binder;
        }
    }
}
