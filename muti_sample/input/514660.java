public final class Primitives {
  private Primitives() {}
  public static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER_TYPE;
  public static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE_TYPE;
  static {
    Map<Class<?>, Class<?>> primToWrap = new HashMap<Class<?>, Class<?>>(16);
    Map<Class<?>, Class<?>> wrapToPrim = new HashMap<Class<?>, Class<?>>(16);
    add(primToWrap, wrapToPrim, boolean.class, Boolean.class);
    add(primToWrap, wrapToPrim, byte.class, Byte.class);
    add(primToWrap, wrapToPrim, char.class, Character.class);
    add(primToWrap, wrapToPrim, double.class, Double.class);
    add(primToWrap, wrapToPrim, float.class, Float.class);
    add(primToWrap, wrapToPrim, int.class, Integer.class);
    add(primToWrap, wrapToPrim, long.class, Long.class);
    add(primToWrap, wrapToPrim, short.class, Short.class);
    add(primToWrap, wrapToPrim, void.class, Void.class);
    PRIMITIVE_TO_WRAPPER_TYPE = Collections.unmodifiableMap(primToWrap);
    WRAPPER_TO_PRIMITIVE_TYPE = Collections.unmodifiableMap(wrapToPrim);
  }
  private static void add(Map<Class<?>, Class<?>> forward,
      Map<Class<?>, Class<?>> backward, Class<?> key, Class<?> value) {
    forward.put(key, value);
    backward.put(value, key);
  }
  public static final Set<Class<?>> PRIMITIVE_TYPES
      = PRIMITIVE_TO_WRAPPER_TYPE.keySet();
  public static final Set<Class<?>> WRAPPER_TYPES
      = WRAPPER_TO_PRIMITIVE_TYPE.keySet();
  public static boolean isWrapperType(Class<?> type) {
    return WRAPPER_TO_PRIMITIVE_TYPE.containsKey(checkNotNull(type));
  }
  public static <T> Class<T> wrap(Class<T> type) {
    checkNotNull(type);
    @SuppressWarnings("unchecked")
    Class<T> wrapped = (Class<T>) PRIMITIVE_TO_WRAPPER_TYPE.get(type);
    return (wrapped == null) ? type : wrapped;
  }
  public static <T> Class<T> unwrap(Class<T> type) {
    checkNotNull(type);
    @SuppressWarnings("unchecked")
    Class<T> unwrapped = (Class<T>) WRAPPER_TO_PRIMITIVE_TYPE.get(type);
    return (unwrapped == null) ? type : unwrapped;
  }
}
