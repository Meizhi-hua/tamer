public class ValueConversions {
    private static final Class<?> THIS_CLASS = ValueConversions.class;
    private static final int MAX_ARITY;
    static {
        final Object[] values = { 255 };
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    values[0] = Integer.getInteger(THIS_CLASS.getName()+".MAX_ARITY", 255);
                    return null;
                }
            });
        MAX_ARITY = (Integer) values[0];
    }
    private static final Lookup IMPL_LOOKUP = MethodHandles.lookup();
    private static EnumMap<Wrapper, MethodHandle>[] newWrapperCaches(int n) {
        @SuppressWarnings("unchecked")
        EnumMap<Wrapper, MethodHandle>[] caches
                = (EnumMap<Wrapper, MethodHandle>[]) new EnumMap[n];  
        for (int i = 0; i < n; i++)
            caches[i] = new EnumMap<>(Wrapper.class);
        return caches;
    }
    static int unboxInteger(Object x, boolean cast) {
        if (x instanceof Integer)
            return ((Integer) x).intValue();
        return primitiveConversion(Wrapper.INT, x, cast).intValue();
    }
    static byte unboxByte(Object x, boolean cast) {
        if (x instanceof Byte)
            return ((Byte) x).byteValue();
        return primitiveConversion(Wrapper.BYTE, x, cast).byteValue();
    }
    static short unboxShort(Object x, boolean cast) {
        if (x instanceof Short)
            return ((Short) x).shortValue();
        return primitiveConversion(Wrapper.SHORT, x, cast).shortValue();
    }
    static boolean unboxBoolean(Object x, boolean cast) {
        if (x instanceof Boolean)
            return ((Boolean) x).booleanValue();
        return (primitiveConversion(Wrapper.BOOLEAN, x, cast).intValue() & 1) != 0;
    }
    static char unboxCharacter(Object x, boolean cast) {
        if (x instanceof Character)
            return ((Character) x).charValue();
        return (char) primitiveConversion(Wrapper.CHAR, x, cast).intValue();
    }
    static long unboxLong(Object x, boolean cast) {
        if (x instanceof Long)
            return ((Long) x).longValue();
        return primitiveConversion(Wrapper.LONG, x, cast).longValue();
    }
    static float unboxFloat(Object x, boolean cast) {
        if (x instanceof Float)
            return ((Float) x).floatValue();
        return primitiveConversion(Wrapper.FLOAT, x, cast).floatValue();
    }
    static double unboxDouble(Object x, boolean cast) {
        if (x instanceof Double)
            return ((Double) x).doubleValue();
        return primitiveConversion(Wrapper.DOUBLE, x, cast).doubleValue();
    }
    static int unboxByteRaw(Object x, boolean cast) {
        return unboxByte(x, cast);
    }
    static int unboxShortRaw(Object x, boolean cast) {
        return unboxShort(x, cast);
    }
    static int unboxBooleanRaw(Object x, boolean cast) {
        return unboxBoolean(x, cast) ? 1 : 0;
    }
    static int unboxCharacterRaw(Object x, boolean cast) {
        return unboxCharacter(x, cast);
    }
    static int unboxFloatRaw(Object x, boolean cast) {
        return Float.floatToIntBits(unboxFloat(x, cast));
    }
    static long unboxDoubleRaw(Object x, boolean cast) {
        return Double.doubleToRawLongBits(unboxDouble(x, cast));
    }
    private static MethodType unboxType(Wrapper wrap, boolean raw) {
        return MethodType.methodType(rawWrapper(wrap, raw).primitiveType(), Object.class, boolean.class);
    }
    private static final EnumMap<Wrapper, MethodHandle>[]
            UNBOX_CONVERSIONS = newWrapperCaches(4);
    private static MethodHandle unbox(Wrapper wrap, boolean raw, boolean cast) {
        EnumMap<Wrapper, MethodHandle> cache = UNBOX_CONVERSIONS[(cast?1:0)+(raw?2:0)];
        MethodHandle mh = cache.get(wrap);
        if (mh != null) {
            return mh;
        }
        switch (wrap) {
            case OBJECT:
                mh = IDENTITY; break;
            case VOID:
                mh = raw ? ALWAYS_ZERO : IGNORE; break;
            case INT: case LONG:
                if (raw)  mh = unbox(wrap, false, cast);
                break;
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        String name = "unbox" + wrap.simpleName() + (raw ? "Raw" : "");
        MethodType type = unboxType(wrap, raw);
        try {
            mh = IMPL_LOOKUP.findStatic(THIS_CLASS, name, type);
        } catch (ReflectiveOperationException ex) {
            mh = null;
        }
        if (mh != null) {
            mh = MethodHandles.insertArguments(mh, 1, cast);
            cache.put(wrap, mh);
            return mh;
        }
        throw new IllegalArgumentException("cannot find unbox adapter for " + wrap
                + (cast ? " (cast)" : "") + (raw ? " (raw)" : ""));
    }
    public static MethodHandle unboxCast(Wrapper type) {
        return unbox(type, false, true);
    }
    public static MethodHandle unboxRaw(Wrapper type) {
        return unbox(type, true, false);
    }
    public static MethodHandle unbox(Class<?> type) {
        return unbox(Wrapper.forPrimitiveType(type), false, false);
    }
    public static MethodHandle unboxCast(Class<?> type) {
        return unbox(Wrapper.forPrimitiveType(type), false, true);
    }
    public static MethodHandle unboxRaw(Class<?> type) {
        return unbox(Wrapper.forPrimitiveType(type), true, false);
    }
    static private final Integer ZERO_INT = 0, ONE_INT = 1;
    public static Number primitiveConversion(Wrapper wrap, Object x, boolean cast) {
        Number res = null;
        if (x == null) {
            if (!cast)  return null;
            return ZERO_INT;
        }
        if (x instanceof Number) {
            res = (Number) x;
        } else if (x instanceof Boolean) {
            res = ((boolean)x ? ONE_INT : ZERO_INT);
        } else if (x instanceof Character) {
            res = (int)(char)x;
        } else {
            res = (Number) x;
        }
        Wrapper xwrap = Wrapper.findWrapperType(x.getClass());
        if (xwrap == null || !cast && !wrap.isConvertibleFrom(xwrap))
            return (Number) wrap.wrapperType().cast(x);
        return res;
    }
    static Integer boxInteger(int x) {
        return x;
    }
    static Byte boxByte(byte x) {
        return x;
    }
    static Short boxShort(short x) {
        return x;
    }
    static Boolean boxBoolean(boolean x) {
        return x;
    }
    static Character boxCharacter(char x) {
        return x;
    }
    static Long boxLong(long x) {
        return x;
    }
    static Float boxFloat(float x) {
        return x;
    }
    static Double boxDouble(double x) {
        return x;
    }
    static Byte boxByteRaw(int x) {
        return boxByte((byte)x);
    }
    static Short boxShortRaw(int x) {
        return boxShort((short)x);
    }
    static Boolean boxBooleanRaw(int x) {
        return boxBoolean(x != 0);
    }
    static Character boxCharacterRaw(int x) {
        return boxCharacter((char)x);
    }
    static Float boxFloatRaw(int x) {
        return boxFloat(Float.intBitsToFloat(x));
    }
    static Double boxDoubleRaw(long x) {
        return boxDouble(Double.longBitsToDouble(x));
    }
    static Void boxVoidRaw(int x) {
        return null;
    }
    private static MethodType boxType(Wrapper wrap, boolean raw) {
        Class<?> boxType = wrap.wrapperType();
        return MethodType.methodType(boxType, rawWrapper(wrap, raw).primitiveType());
    }
    private static Wrapper rawWrapper(Wrapper wrap, boolean raw) {
        if (raw)  return wrap.isDoubleWord() ? Wrapper.LONG : Wrapper.INT;
        return wrap;
    }
    private static final EnumMap<Wrapper, MethodHandle>[]
            BOX_CONVERSIONS = newWrapperCaches(4);
    private static MethodHandle box(Wrapper wrap, boolean exact, boolean raw) {
        EnumMap<Wrapper, MethodHandle> cache = BOX_CONVERSIONS[(exact?1:0)+(raw?2:0)];
        MethodHandle mh = cache.get(wrap);
        if (mh != null) {
            return mh;
        }
        switch (wrap) {
            case OBJECT:
                mh = IDENTITY; break;
            case VOID:
                if (!raw)  mh = ZERO_OBJECT;
                break;
            case INT: case LONG:
                if (raw)  mh = box(wrap, exact, false);
                break;
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        String name = "box" + wrap.simpleName() + (raw ? "Raw" : "");
        MethodType type = boxType(wrap, raw);
        if (exact) {
            try {
                mh = IMPL_LOOKUP.findStatic(THIS_CLASS, name, type);
            } catch (ReflectiveOperationException ex) {
                mh = null;
            }
        } else {
            mh = box(wrap, !exact, raw).asType(type.erase());
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        throw new IllegalArgumentException("cannot find box adapter for "
                + wrap + (exact ? " (exact)" : "") + (raw ? " (raw)" : ""));
    }
    public static MethodHandle box(Class<?> type) {
        boolean exact = false;
        return box(Wrapper.forPrimitiveType(type), exact, false);
    }
    public static MethodHandle boxRaw(Class<?> type) {
        boolean exact = false;
        return box(Wrapper.forPrimitiveType(type), exact, true);
    }
    public static MethodHandle box(Wrapper type) {
        boolean exact = false;
        return box(type, exact, false);
    }
    public static MethodHandle boxRaw(Wrapper type) {
        boolean exact = false;
        return box(type, exact, true);
    }
    static int unboxRawInteger(Object x) {
        if (x instanceof Integer)
            return (int) x;
        else
            return (int) unboxLong(x, false);
    }
    static Integer reboxRawInteger(Object x) {
        if (x instanceof Integer)
            return (Integer) x;
        else
            return (int) unboxLong(x, false);
    }
    static Byte reboxRawByte(Object x) {
        if (x instanceof Byte)  return (Byte) x;
        return boxByteRaw(unboxRawInteger(x));
    }
    static Short reboxRawShort(Object x) {
        if (x instanceof Short)  return (Short) x;
        return boxShortRaw(unboxRawInteger(x));
    }
    static Boolean reboxRawBoolean(Object x) {
        if (x instanceof Boolean)  return (Boolean) x;
        return boxBooleanRaw(unboxRawInteger(x));
    }
    static Character reboxRawCharacter(Object x) {
        if (x instanceof Character)  return (Character) x;
        return boxCharacterRaw(unboxRawInteger(x));
    }
    static Float reboxRawFloat(Object x) {
        if (x instanceof Float)  return (Float) x;
        return boxFloatRaw(unboxRawInteger(x));
    }
    static Long reboxRawLong(Object x) {
        return (Long) x;  
    }
    static Double reboxRawDouble(Object x) {
        if (x instanceof Double)  return (Double) x;
        return boxDoubleRaw(unboxLong(x, true));
    }
    private static MethodType reboxType(Wrapper wrap) {
        Class<?> boxType = wrap.wrapperType();
        return MethodType.methodType(boxType, Object.class);
    }
    private static final EnumMap<Wrapper, MethodHandle>[]
            REBOX_CONVERSIONS = newWrapperCaches(1);
    public static MethodHandle rebox(Wrapper wrap) {
        EnumMap<Wrapper, MethodHandle> cache = REBOX_CONVERSIONS[0];
        MethodHandle mh = cache.get(wrap);
        if (mh != null) {
            return mh;
        }
        switch (wrap) {
            case OBJECT:
                mh = IDENTITY; break;
            case VOID:
                throw new IllegalArgumentException("cannot rebox a void");
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        String name = "reboxRaw" + wrap.simpleName();
        MethodType type = reboxType(wrap);
        try {
            mh = IMPL_LOOKUP.findStatic(THIS_CLASS, name, type);
            mh = mh.asType(IDENTITY.type());
        } catch (ReflectiveOperationException ex) {
            mh = null;
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        throw new IllegalArgumentException("cannot find rebox adapter for " + wrap);
    }
    public static MethodHandle rebox(Class<?> type) {
        return rebox(Wrapper.forPrimitiveType(type));
    }
    static long widenInt(int x) {
        return (long) x;
    }
    static Long widenBoxedInt(Integer x) {
        return (long)(int)x;
    }
    static int narrowLong(long x) {
        return (int) x;
    }
    static Integer narrowBoxedLong(Long x) {
        return (int)(long) x;
    }
    static void ignore(Object x) {
        return;
    }
    static void empty() {
        return;
    }
    static Object zeroObject() {
        return null;
    }
    static int zeroInteger() {
        return 0;
    }
    static long zeroLong() {
        return 0;
    }
    static float zeroFloat() {
        return 0;
    }
    static double zeroDouble() {
        return 0;
    }
    private static final EnumMap<Wrapper, MethodHandle>[]
            CONSTANT_FUNCTIONS = newWrapperCaches(2);
    public static MethodHandle zeroConstantFunction(Wrapper wrap) {
        EnumMap<Wrapper, MethodHandle> cache = CONSTANT_FUNCTIONS[0];
        MethodHandle mh = cache.get(wrap);
        if (mh != null) {
            return mh;
        }
        MethodType type = MethodType.methodType(wrap.primitiveType());
        switch (wrap) {
            case VOID:
                mh = EMPTY;
                break;
            case OBJECT:
            case INT: case LONG: case FLOAT: case DOUBLE:
                try {
                    mh = IMPL_LOOKUP.findStatic(THIS_CLASS, "zero"+wrap.simpleName(), type);
                } catch (ReflectiveOperationException ex) {
                    mh = null;
                }
                break;
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        Wrapper rawWrap = wrap.rawPrimitive();
        if (mh == null && rawWrap != wrap) {
            mh = MethodHandles.explicitCastArguments(zeroConstantFunction(rawWrap), type);
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        throw new IllegalArgumentException("cannot find zero constant for " + wrap);
    }
    static Object alwaysNull(Object x) {
        return null;
    }
    static int alwaysZero(Object x) {
        return 0;
    }
    static <T> T identity(T x) {
        return x;
    }
    static int identity(int x) {
        return x;
    }
    static byte identity(byte x) {
        return x;
    }
    static short identity(short x) {
        return x;
    }
    static boolean identity(boolean x) {
        return x;
    }
    static char identity(char x) {
        return x;
    }
    static long identity(long x) {
        return x;
    }
    static float identity(float x) {
        return x;
    }
    static double identity(double x) {
        return x;
    }
    static <T,U> T castReference(Class<? extends T> t, U x) {
        return t.cast(x);
    }
    private static final MethodHandle IDENTITY, IDENTITY_I, IDENTITY_J, CAST_REFERENCE, ALWAYS_NULL, ALWAYS_ZERO, ZERO_OBJECT, IGNORE, EMPTY, NEW_ARRAY;
    static {
        try {
            MethodType idType = MethodType.genericMethodType(1);
            MethodType castType = idType.insertParameterTypes(0, Class.class);
            MethodType alwaysZeroType = idType.changeReturnType(int.class);
            MethodType ignoreType = idType.changeReturnType(void.class);
            MethodType zeroObjectType = MethodType.genericMethodType(0);
            IDENTITY = IMPL_LOOKUP.findStatic(THIS_CLASS, "identity", idType);
            IDENTITY_I = IMPL_LOOKUP.findStatic(THIS_CLASS, "identity", MethodType.methodType(int.class, int.class));
            IDENTITY_J = IMPL_LOOKUP.findStatic(THIS_CLASS, "identity", MethodType.methodType(long.class, long.class));
            CAST_REFERENCE = IMPL_LOOKUP.findStatic(THIS_CLASS, "castReference", castType);
            ALWAYS_NULL = IMPL_LOOKUP.findStatic(THIS_CLASS, "alwaysNull", idType);
            ALWAYS_ZERO = IMPL_LOOKUP.findStatic(THIS_CLASS, "alwaysZero", alwaysZeroType);
            ZERO_OBJECT = IMPL_LOOKUP.findStatic(THIS_CLASS, "zeroObject", zeroObjectType);
            IGNORE = IMPL_LOOKUP.findStatic(THIS_CLASS, "ignore", ignoreType);
            EMPTY = IMPL_LOOKUP.findStatic(THIS_CLASS, "empty", ignoreType.dropParameterTypes(0, 1));
            NEW_ARRAY = IMPL_LOOKUP.findStatic(THIS_CLASS, "newArray", MethodType.methodType(Object[].class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            Error err = new InternalError("uncaught exception");
            err.initCause(ex);
            throw err;
        }
    }
    static class LazyStatics {
        private static final MethodHandle COPY_AS_REFERENCE_ARRAY, COPY_AS_PRIMITIVE_ARRAY, MAKE_LIST;
        static {
            try {
                COPY_AS_REFERENCE_ARRAY = IMPL_LOOKUP.findStatic(THIS_CLASS, "copyAsReferenceArray", MethodType.methodType(Object[].class, Class.class, Object[].class));
                COPY_AS_PRIMITIVE_ARRAY = IMPL_LOOKUP.findStatic(THIS_CLASS, "copyAsPrimitiveArray", MethodType.methodType(Object.class, Wrapper.class, Object[].class));
                MAKE_LIST = IMPL_LOOKUP.findStatic(THIS_CLASS, "makeList", MethodType.methodType(List.class, Object[].class));
            } catch (ReflectiveOperationException ex) {
                Error err = new InternalError("uncaught exception");
                err.initCause(ex);
                throw err;
            }
        }
    }
    private static final EnumMap<Wrapper, MethodHandle>[] WRAPPER_CASTS
            = newWrapperCaches(2);
    public static MethodHandle cast(Class<?> type) {
        boolean exact = false;
        if (type.isPrimitive())  throw new IllegalArgumentException("cannot cast primitive type "+type);
        MethodHandle mh = null;
        Wrapper wrap = null;
        EnumMap<Wrapper, MethodHandle> cache = null;
        if (Wrapper.isWrapperType(type)) {
            wrap = Wrapper.forWrapperType(type);
            cache = WRAPPER_CASTS[exact?1:0];
            mh = cache.get(wrap);
            if (mh != null)  return mh;
        }
        if (VerifyType.isNullReferenceConversion(Object.class, type))
            mh = IDENTITY;
        else if (VerifyType.isNullType(type))
            mh = ALWAYS_NULL;
        else
            mh = MethodHandles.insertArguments(CAST_REFERENCE, 0, type);
        if (exact) {
            MethodType xmt = MethodType.methodType(type, Object.class);
            mh = MethodHandles.explicitCastArguments(mh, xmt);
        }
        if (cache != null)
            cache.put(wrap, mh);
        return mh;
    }
    public static MethodHandle identity() {
        return IDENTITY;
    }
    public static MethodHandle identity(Class<?> type) {
        return MethodHandles.identity(type);
    }
    public static MethodHandle identity(Wrapper wrap) {
        EnumMap<Wrapper, MethodHandle> cache = CONSTANT_FUNCTIONS[1];
        MethodHandle mh = cache.get(wrap);
        if (mh != null) {
            return mh;
        }
        MethodType type = MethodType.methodType(wrap.primitiveType());
        if (wrap != Wrapper.VOID)
            type = type.appendParameterTypes(wrap.primitiveType());
        try {
            mh = IMPL_LOOKUP.findStatic(THIS_CLASS, "identity", type);
        } catch (ReflectiveOperationException ex) {
            mh = null;
        }
        if (mh == null && wrap == Wrapper.VOID) {
            mh = EMPTY;  
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        if (mh != null) {
            cache.put(wrap, mh);
            return mh;
        }
        throw new IllegalArgumentException("cannot find identity for " + wrap);
    }
    static float doubleToFloat(double x) {
        return (float) x;
    }
    static double floatToDouble(float x) {
        return x;
    }
    static long doubleToLong(double x) {
        return (long) x;
    }
    static int doubleToInt(double x) {
        return (int) x;
    }
    static short doubleToShort(double x) {
        return (short) x;
    }
    static char doubleToChar(double x) {
        return (char) x;
    }
    static byte doubleToByte(double x) {
        return (byte) x;
    }
    static boolean doubleToBoolean(double x) {
        return toBoolean((byte) x);
    }
    static long floatToLong(float x) {
        return (long) x;
    }
    static int floatToInt(float x) {
        return (int) x;
    }
    static short floatToShort(float x) {
        return (short) x;
    }
    static char floatToChar(float x) {
        return (char) x;
    }
    static byte floatToByte(float x) {
        return (byte) x;
    }
    static boolean floatToBoolean(float x) {
        return toBoolean((byte) x);
    }
    static double longToDouble(long x) {
        return x;
    }
    static double intToDouble(int x) {
        return x;
    }
    static double shortToDouble(short x) {
        return x;
    }
    static double charToDouble(char x) {
        return x;
    }
    static double byteToDouble(byte x) {
        return x;
    }
    static double booleanToDouble(boolean x) {
        return fromBoolean(x);
    }
    static float longToFloat(long x) {
        return x;
    }
    static float intToFloat(int x) {
        return x;
    }
    static float shortToFloat(short x) {
        return x;
    }
    static float charToFloat(char x) {
        return x;
    }
    static float byteToFloat(byte x) {
        return x;
    }
    static float booleanToFloat(boolean x) {
        return fromBoolean(x);
    }
    static boolean toBoolean(byte x) {
        return ((x & 1) != 0);
    }
    static byte fromBoolean(boolean x) {
        return (x ? (byte)1 : (byte)0);
    }
    private static final EnumMap<Wrapper, MethodHandle>[]
            CONVERT_FLOAT_FUNCTIONS = newWrapperCaches(4);
    static MethodHandle convertFloatFunction(Wrapper wrap, boolean toFloat, boolean doubleSize) {
        EnumMap<Wrapper, MethodHandle> cache = CONVERT_FLOAT_FUNCTIONS[(toFloat?1:0)+(doubleSize?2:0)];
        MethodHandle mh = cache.get(wrap);
        if (mh != null) {
            return mh;
        }
        Wrapper fwrap = (doubleSize ? Wrapper.DOUBLE : Wrapper.FLOAT);
        Class<?> fix = wrap.primitiveType();
        Class<?> flt = (doubleSize ? double.class : float.class);
        Class<?> src = toFloat ? fix : flt;
        Class<?> dst = toFloat ? flt : fix;
        if (src == dst)  return identity(wrap);
        MethodType type = MethodType.methodType(dst, src);
        switch (wrap) {
            case VOID:
                mh = toFloat ? zeroConstantFunction(fwrap) : MethodHandles.dropArguments(EMPTY, 0, flt);
                break;
            case OBJECT:
                mh = toFloat ? unbox(flt) : box(flt);
                break;
            default:
                try {
                    mh = IMPL_LOOKUP.findStatic(THIS_CLASS, src.getSimpleName()+"To"+capitalize(dst.getSimpleName()), type);
                } catch (ReflectiveOperationException ex) {
                    mh = null;
                }
                break;
        }
        if (mh != null) {
            assert(mh.type() == type) : mh;
            cache.put(wrap, mh);
            return mh;
        }
        throw new IllegalArgumentException("cannot find float conversion constant for " +
                                           src.getSimpleName()+" -> "+dst.getSimpleName());
    }
    public static MethodHandle convertFromFloat(Class<?> fixType) {
        Wrapper wrap = Wrapper.forPrimitiveType(fixType);
        return convertFloatFunction(wrap, false, false);
    }
    public static MethodHandle convertFromDouble(Class<?> fixType) {
        Wrapper wrap = Wrapper.forPrimitiveType(fixType);
        return convertFloatFunction(wrap, false, true);
    }
    public static MethodHandle convertToFloat(Class<?> fixType) {
        Wrapper wrap = Wrapper.forPrimitiveType(fixType);
        return convertFloatFunction(wrap, true, false);
    }
    public static MethodHandle convertToDouble(Class<?> fixType) {
        Wrapper wrap = Wrapper.forPrimitiveType(fixType);
        return convertFloatFunction(wrap, true, true);
    }
    private static String capitalize(String x) {
        return Character.toUpperCase(x.charAt(0))+x.substring(1);
    }
    public static Object convertArrayElements(Class<?> arrayType, Object array) {
        Class<?> src = array.getClass().getComponentType();
        Class<?> dst = arrayType.getComponentType();
        if (src == null || dst == null)  throw new IllegalArgumentException("not array type");
        Wrapper sw = (src.isPrimitive() ? Wrapper.forPrimitiveType(src) : null);
        Wrapper dw = (dst.isPrimitive() ? Wrapper.forPrimitiveType(dst) : null);
        int length;
        if (sw == null) {
            Object[] a = (Object[]) array;
            length = a.length;
            if (dw == null)
                return Arrays.copyOf(a, length, arrayType.asSubclass(Object[].class));
            Object res = dw.makeArray(length);
            dw.copyArrayUnboxing(a, 0, res, 0, length);
            return res;
        }
        length = java.lang.reflect.Array.getLength(array);
        Object[] res;
        if (dw == null) {
            res = Arrays.copyOf(NO_ARGS_ARRAY, length, arrayType.asSubclass(Object[].class));
        } else {
            res = new Object[length];
        }
        sw.copyArrayBoxing(array, 0, res, 0, length);
        if (dw == null)  return res;
        Object a = dw.makeArray(length);
        dw.copyArrayUnboxing(res, 0, a, 0, length);
        return a;
    }
    private static MethodHandle findCollector(String name, int nargs, Class<?> rtype, Class<?>... ptypes) {
        MethodType type = MethodType.genericMethodType(nargs)
                .changeReturnType(rtype)
                .insertParameterTypes(0, ptypes);
        try {
            return IMPL_LOOKUP.findStatic(THIS_CLASS, name, type);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
    private static final Object[] NO_ARGS_ARRAY = {};
    private static Object[] makeArray(Object... args) { return args; }
    private static Object[] array() { return NO_ARGS_ARRAY; }
    private static Object[] array(Object a0)
                { return makeArray(a0); }
    private static Object[] array(Object a0, Object a1)
                { return makeArray(a0, a1); }
    private static Object[] array(Object a0, Object a1, Object a2)
                { return makeArray(a0, a1, a2); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3)
                { return makeArray(a0, a1, a2, a3); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4)
                { return makeArray(a0, a1, a2, a3, a4); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5)
                { return makeArray(a0, a1, a2, a3, a4, a5); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6, a7); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
    private static Object[] array(Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8, Object a9)
                { return makeArray(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
    private static MethodHandle[] makeArrays() {
        ArrayList<MethodHandle> mhs = new ArrayList<>();
        for (;;) {
            MethodHandle mh = findCollector("array", mhs.size(), Object[].class);
            if (mh == null)  break;
            mhs.add(mh);
        }
        assert(mhs.size() == 11);  
        return mhs.toArray(new MethodHandle[MAX_ARITY+1]);
    }
    private static final MethodHandle[] ARRAYS = makeArrays();
    private static Object[] newArray(int len) { return new Object[len]; }
    private static void fillWithArguments(Object[] a, int pos, Object... args) {
        System.arraycopy(args, 0, a, pos, args.length);
    }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0)
                { fillWithArguments(a, pos, a0); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1)
                { fillWithArguments(a, pos, a0, a1); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2)
                { fillWithArguments(a, pos, a0, a1, a2); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2, Object a3)
                { fillWithArguments(a, pos, a0, a1, a2, a3); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2, Object a3,
                                  Object a4)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7, a8); return a; }
    private static Object[] fillArray(Object[] a, Integer pos, Object a0, Object a1, Object a2, Object a3,
                                  Object a4, Object a5, Object a6, Object a7,
                                  Object a8, Object a9)
                { fillWithArguments(a, pos, a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); return a; }
    private static MethodHandle[] makeFillArrays() {
        ArrayList<MethodHandle> mhs = new ArrayList<>();
        mhs.add(null);  
        for (;;) {
            MethodHandle mh = findCollector("fillArray", mhs.size(), Object[].class, Object[].class, Integer.class);
            if (mh == null)  break;
            mhs.add(mh);
        }
        assert(mhs.size() == 11);  
        return mhs.toArray(new MethodHandle[0]);
    }
    private static final MethodHandle[] FILL_ARRAYS = makeFillArrays();
    private static Object[] copyAsReferenceArray(Class<? extends Object[]> arrayType, Object... a) {
        return Arrays.copyOf(a, a.length, arrayType);
    }
    private static Object copyAsPrimitiveArray(Wrapper w, Object... boxes) {
        Object a = w.makeArray(boxes.length);
        w.copyArrayUnboxing(boxes, 0, a, 0, boxes.length);
        return a;
    }
    public static MethodHandle varargsArray(int nargs) {
        MethodHandle mh = ARRAYS[nargs];
        if (mh != null)  return mh;
        mh = findCollector("array", nargs, Object[].class);
        if (mh != null)  return ARRAYS[nargs] = mh;
        MethodHandle producer = filler(0);  
        return ARRAYS[nargs] = buildVarargsArray(producer, nargs);
    }
    private static MethodHandle buildVarargsArray(MethodHandle producer, int nargs) {
        MethodHandle filler = filler(nargs);
        MethodHandle mh = producer;
        mh = MethodHandles.dropArguments(mh, 1, filler.type().parameterList());
        mh = MethodHandles.foldArguments(mh, filler);
        mh = MethodHandles.foldArguments(mh, buildNewArray(nargs));
        return mh;
    }
    private static MethodHandle buildNewArray(int nargs) {
        return MethodHandles.insertArguments(NEW_ARRAY, 0, (int) nargs);
    }
    private static final MethodHandle[] FILLERS = new MethodHandle[MAX_ARITY+1];
    private static MethodHandle filler(int nargs) {
        MethodHandle filler = FILLERS[nargs];
        if (filler != null)  return filler;
        return FILLERS[nargs] = buildFiller(nargs);
    }
    private static MethodHandle buildFiller(int nargs) {
        if (nargs == 0)
            return MethodHandles.identity(Object[].class);
        final int CHUNK = (FILL_ARRAYS.length - 1);
        int rightLen = nargs % CHUNK;
        int leftLen = nargs - rightLen;
        if (rightLen == 0) {
            leftLen = nargs - (rightLen = CHUNK);
            if (FILLERS[leftLen] == null) {
                for (int j = 0; j < leftLen; j += CHUNK)  filler(j);
            }
        }
        MethodHandle leftFill = filler(leftLen);  
        MethodHandle rightFill = FILL_ARRAYS[rightLen];
        rightFill = MethodHandles.insertArguments(rightFill, 1, (int) leftLen);  
        MethodHandle mh = filler(0);  
        mh = MethodHandles.dropArguments(mh, 1, rightFill.type().parameterList());
        mh = MethodHandles.foldArguments(mh, rightFill);
        if (leftLen > 0) {
            mh = MethodHandles.dropArguments(mh, 1, leftFill.type().parameterList());
            mh = MethodHandles.foldArguments(mh, leftFill);
        }
        return mh;
    }
    private static final ClassValue<MethodHandle[]> TYPED_COLLECTORS
        = new ClassValue<MethodHandle[]>() {
            protected MethodHandle[] computeValue(Class<?> type) {
                return new MethodHandle[256];
            }
    };
    public static MethodHandle varargsArray(Class<?> arrayType, int nargs) {
        Class<?> elemType = arrayType.getComponentType();
        if (elemType == null)  throw new IllegalArgumentException("not an array: "+arrayType);
        if (elemType == Object.class)
            return varargsArray(nargs);
        MethodHandle cache[] = TYPED_COLLECTORS.get(elemType);
        MethodHandle mh = nargs < cache.length ? cache[nargs] : null;
        if (mh != null)  return mh;
        MethodHandle producer = buildArrayProducer(arrayType);
        mh = buildVarargsArray(producer, nargs);
        mh = mh.asType(MethodType.methodType(arrayType, Collections.<Class<?>>nCopies(nargs, elemType)));
        cache[nargs] = mh;
        return mh;
    }
    private static MethodHandle buildArrayProducer(Class<?> arrayType) {
        Class<?> elemType = arrayType.getComponentType();
        if (elemType.isPrimitive())
            return LazyStatics.COPY_AS_PRIMITIVE_ARRAY.bindTo(Wrapper.forPrimitiveType(elemType));
        else
            return LazyStatics.COPY_AS_REFERENCE_ARRAY.bindTo(arrayType);
    }
    private static final List<Object> NO_ARGS_LIST = Arrays.asList(NO_ARGS_ARRAY);
    private static List<Object> makeList(Object... args) { return Arrays.asList(args); }
    private static List<Object> list() { return NO_ARGS_LIST; }
    private static List<Object> list(Object a0)
                { return makeList(a0); }
    private static List<Object> list(Object a0, Object a1)
                { return makeList(a0, a1); }
    private static List<Object> list(Object a0, Object a1, Object a2)
                { return makeList(a0, a1, a2); }
    private static List<Object> list(Object a0, Object a1, Object a2, Object a3)
                { return makeList(a0, a1, a2, a3); }
    private static List<Object> list(Object a0, Object a1, Object a2, Object a3,
                                     Object a4)
                { return makeList(a0, a1, a2, a3, a4); }
    private static List<Object> list(Object a0, Object a1, Object a2, Object a3,
                                     Object a4, Object a5)
                { return makeList(a0, a1, a2, a3, a4, a5); }
    private static List<Object> list(Object a0, Object a1, Object a2, Object a3,
                                     Object a4, Object a5, Object a6)
                { return makeList(a0, a1, a2, a3, a4, a5, a6); }
    private static List<Object> list(Object a0, Object a1, Object a2, Object a3,
                                     Object a4, Object a5, Object a6, Object a7)
                { return makeList(a0, a1, a2, a3, a4, a5, a6, a7); }
    private static List<Object> list(Object a0, Object a1, Object a2, Object a3,
                                     Object a4, Object a5, Object a6, Object a7,
                                     Object a8)
                { return makeList(a0, a1, a2, a3, a4, a5, a6, a7, a8); }
    private static List<Object> list(Object a0, Object a1, Object a2, Object a3,
                                     Object a4, Object a5, Object a6, Object a7,
                                     Object a8, Object a9)
                { return makeList(a0, a1, a2, a3, a4, a5, a6, a7, a8, a9); }
    private static MethodHandle[] makeLists() {
        ArrayList<MethodHandle> mhs = new ArrayList<>();
        for (;;) {
            MethodHandle mh = findCollector("list", mhs.size(), List.class);
            if (mh == null)  break;
            mhs.add(mh);
        }
        assert(mhs.size() == 11);  
        return mhs.toArray(new MethodHandle[MAX_ARITY+1]);
    }
    private static final MethodHandle[] LISTS = makeLists();
    public static MethodHandle varargsList(int nargs) {
        MethodHandle mh = LISTS[nargs];
        if (mh != null)  return mh;
        mh = findCollector("list", nargs, List.class);
        if (mh != null)  return LISTS[nargs] = mh;
        return LISTS[nargs] = buildVarargsList(nargs);
    }
    private static MethodHandle buildVarargsList(int nargs) {
        return MethodHandles.filterReturnValue(varargsArray(nargs), LazyStatics.MAKE_LIST);
    }
}
