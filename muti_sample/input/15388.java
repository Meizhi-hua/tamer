public class AWTKeyStroke implements Serializable {
    static final long serialVersionUID = -6430539691155161871L;
    private static Map cache;
    private static AWTKeyStroke cacheKey;
    private static Constructor ctor = getCtor(AWTKeyStroke.class);
    private static Map modifierKeywords;
    private static VKCollection vks;
    private char keyChar = KeyEvent.CHAR_UNDEFINED;
    private int keyCode = KeyEvent.VK_UNDEFINED;
    private int modifiers;
    private boolean onKeyRelease;
    static {
        Toolkit.loadLibraries();
    }
    protected AWTKeyStroke() {
    }
    protected AWTKeyStroke(char keyChar, int keyCode, int modifiers,
                           boolean onKeyRelease) {
        this.keyChar = keyChar;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.onKeyRelease = onKeyRelease;
    }
    protected static void registerSubclass(Class<?> subclass) {
        if (subclass == null) {
            throw new IllegalArgumentException("subclass cannot be null");
        }
        if (AWTKeyStroke.ctor.getDeclaringClass().equals(subclass)) {
            return;
        }
        if (!AWTKeyStroke.class.isAssignableFrom(subclass)) {
            throw new ClassCastException("subclass is not derived from AWTKeyStroke");
        }
        Constructor ctor = getCtor(subclass);
        String couldNotInstantiate = "subclass could not be instantiated";
        if (ctor == null) {
            throw new IllegalArgumentException(couldNotInstantiate);
        }
        try {
            AWTKeyStroke stroke = (AWTKeyStroke)ctor.newInstance((Object[]) null);
            if (stroke == null) {
                throw new IllegalArgumentException(couldNotInstantiate);
            }
        } catch (NoSuchMethodError e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (ExceptionInInitializerError e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        }
        synchronized (AWTKeyStroke.class) {
            AWTKeyStroke.ctor = ctor;
            cache = null;
            cacheKey = null;
        }
    }
    private static Constructor getCtor(final Class clazz)
    {
        Object ctor = AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    Constructor ctor = clazz.getDeclaredConstructor((Class[]) null);
                    if (ctor != null) {
                        ctor.setAccessible(true);
                    }
                    return ctor;
                } catch (SecurityException e) {
                } catch (NoSuchMethodException e) {
                }
                return null;
            }
        });
        return (Constructor)ctor;
    }
    private static synchronized AWTKeyStroke getCachedStroke
        (char keyChar, int keyCode, int modifiers, boolean onKeyRelease)
    {
        if (cache == null) {
            cache = new HashMap();
        }
        if (cacheKey == null) {
            try {
                cacheKey = (AWTKeyStroke)ctor.newInstance((Object[]) null);
            } catch (InstantiationException e) {
                assert(false);
            } catch (IllegalAccessException e) {
                assert(false);
            } catch (InvocationTargetException e) {
                assert(false);
            }
        }
        cacheKey.keyChar = keyChar;
        cacheKey.keyCode = keyCode;
        cacheKey.modifiers = mapNewModifiers(mapOldModifiers(modifiers));
        cacheKey.onKeyRelease = onKeyRelease;
        AWTKeyStroke stroke = (AWTKeyStroke)cache.get(cacheKey);
        if (stroke == null) {
            stroke = cacheKey;
            cache.put(stroke, stroke);
            cacheKey = null;
        }
        return stroke;
    }
    public static AWTKeyStroke getAWTKeyStroke(char keyChar) {
        return getCachedStroke(keyChar, KeyEvent.VK_UNDEFINED, 0, false);
    }
    public static AWTKeyStroke getAWTKeyStroke(Character keyChar, int modifiers)
    {
        if (keyChar == null) {
            throw new IllegalArgumentException("keyChar cannot be null");
        }
        return getCachedStroke(keyChar.charValue(), KeyEvent.VK_UNDEFINED,
                               modifiers, false);
    }
    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers,
                                               boolean onKeyRelease) {
        return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode, modifiers,
                               onKeyRelease);
    }
    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers) {
        return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode, modifiers,
                               false);
    }
    public static AWTKeyStroke getAWTKeyStrokeForEvent(KeyEvent anEvent) {
        int id = anEvent.getID();
        switch(id) {
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
            return getCachedStroke(KeyEvent.CHAR_UNDEFINED,
                                   anEvent.getKeyCode(),
                                   anEvent.getModifiers(),
                                   (id == KeyEvent.KEY_RELEASED));
          case KeyEvent.KEY_TYPED:
            return getCachedStroke(anEvent.getKeyChar(),
                                   KeyEvent.VK_UNDEFINED,
                                   anEvent.getModifiers(),
                                   false);
          default:
            return null;
        }
    }
    public static AWTKeyStroke getAWTKeyStroke(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        final String errmsg = "String formatted incorrectly";
        StringTokenizer st = new StringTokenizer(s, " ");
        int mask = 0;
        boolean released = false;
        boolean typed = false;
        boolean pressed = false;
        synchronized (AWTKeyStroke.class) {
            if (modifierKeywords == null) {
                Map uninitializedMap = new HashMap(8, 1.0f);
                uninitializedMap.put("shift",
                                     Integer.valueOf(InputEvent.SHIFT_DOWN_MASK
                                                     |InputEvent.SHIFT_MASK));
                uninitializedMap.put("control",
                                     Integer.valueOf(InputEvent.CTRL_DOWN_MASK
                                                     |InputEvent.CTRL_MASK));
                uninitializedMap.put("ctrl",
                                     Integer.valueOf(InputEvent.CTRL_DOWN_MASK
                                                     |InputEvent.CTRL_MASK));
                uninitializedMap.put("meta",
                                     Integer.valueOf(InputEvent.META_DOWN_MASK
                                                     |InputEvent.META_MASK));
                uninitializedMap.put("alt",
                                     Integer.valueOf(InputEvent.ALT_DOWN_MASK
                                                     |InputEvent.ALT_MASK));
                uninitializedMap.put("altGraph",
                                     Integer.valueOf(InputEvent.ALT_GRAPH_DOWN_MASK
                                                     |InputEvent.ALT_GRAPH_MASK));
                uninitializedMap.put("button1",
                                     Integer.valueOf(InputEvent.BUTTON1_DOWN_MASK));
                uninitializedMap.put("button2",
                                     Integer.valueOf(InputEvent.BUTTON2_DOWN_MASK));
                uninitializedMap.put("button3",
                                     Integer.valueOf(InputEvent.BUTTON3_DOWN_MASK));
                modifierKeywords =
                    Collections.synchronizedMap(uninitializedMap);
            }
        }
        int count = st.countTokens();
        for (int i = 1; i <= count; i++) {
            String token = st.nextToken();
            if (typed) {
                if (token.length() != 1 || i != count) {
                    throw new IllegalArgumentException(errmsg);
                }
                return getCachedStroke(token.charAt(0), KeyEvent.VK_UNDEFINED,
                                       mask, false);
            }
            if (pressed || released || i == count) {
                if (i != count) {
                    throw new IllegalArgumentException(errmsg);
                }
                String keyCodeName = "VK_" + token;
                int keyCode = getVKValue(keyCodeName);
                return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode,
                                       mask, released);
            }
            if (token.equals("released")) {
                released = true;
                continue;
            }
            if (token.equals("pressed")) {
                pressed = true;
                continue;
            }
            if (token.equals("typed")) {
                typed = true;
                continue;
            }
            Integer tokenMask = (Integer)modifierKeywords.get(token);
            if (tokenMask != null) {
                mask |= tokenMask.intValue();
            } else {
                throw new IllegalArgumentException(errmsg);
            }
        }
        throw new IllegalArgumentException(errmsg);
    }
    private static VKCollection getVKCollection() {
        if (vks == null) {
            vks = new VKCollection();
        }
        return vks;
    }
    private static int getVKValue(String key) {
        VKCollection vkCollect = getVKCollection();
        Integer value = vkCollect.findCode(key);
        if (value == null) {
            int keyCode = 0;
            final String errmsg = "String formatted incorrectly";
            try {
                keyCode = KeyEvent.class.getField(key).getInt(KeyEvent.class);
            } catch (NoSuchFieldException nsfe) {
                throw new IllegalArgumentException(errmsg);
            } catch (IllegalAccessException iae) {
                throw new IllegalArgumentException(errmsg);
            }
            value = Integer.valueOf(keyCode);
            vkCollect.put(key, value);
        }
        return value.intValue();
    }
    public final char getKeyChar() {
        return keyChar;
    }
    public final int getKeyCode() {
        return keyCode;
    }
    public final int getModifiers() {
        return modifiers;
    }
    public final boolean isOnKeyRelease() {
        return onKeyRelease;
    }
    public final int getKeyEventType() {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return KeyEvent.KEY_TYPED;
        } else {
            return (onKeyRelease)
                ? KeyEvent.KEY_RELEASED
                : KeyEvent.KEY_PRESSED;
        }
    }
    public int hashCode() {
        return (((int)keyChar) + 1) * (2 * (keyCode + 1)) * (modifiers + 1) +
            (onKeyRelease ? 1 : 2);
    }
    public final boolean equals(Object anObject) {
        if (anObject instanceof AWTKeyStroke) {
            AWTKeyStroke ks = (AWTKeyStroke)anObject;
            return (ks.keyChar == keyChar && ks.keyCode == keyCode &&
                    ks.onKeyRelease == onKeyRelease &&
                    ks.modifiers == modifiers);
        }
        return false;
    }
    public String toString() {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return getModifiersText(modifiers) + "typed " + keyChar;
        } else {
            return getModifiersText(modifiers) +
                (onKeyRelease ? "released" : "pressed") + " " +
                getVKText(keyCode);
        }
    }
    static String getModifiersText(int modifiers) {
        StringBuilder buf = new StringBuilder();
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0 ) {
            buf.append("shift ");
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0 ) {
            buf.append("ctrl ");
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0 ) {
            buf.append("meta ");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0 ) {
            buf.append("alt ");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0 ) {
            buf.append("altGraph ");
        }
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0 ) {
            buf.append("button1 ");
        }
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0 ) {
            buf.append("button2 ");
        }
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0 ) {
            buf.append("button3 ");
        }
        return buf.toString();
    }
    static String getVKText(int keyCode) {
        VKCollection vkCollect = getVKCollection();
        Integer key = Integer.valueOf(keyCode);
        String name = vkCollect.findName(key);
        if (name != null) {
            return name.substring(3);
        }
        int expected_modifiers =
            (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                if (fields[i].getModifiers() == expected_modifiers
                    && fields[i].getType() == Integer.TYPE
                    && fields[i].getName().startsWith("VK_")
                    && fields[i].getInt(KeyEvent.class) == keyCode)
                {
                    name = fields[i].getName();
                    vkCollect.put(name, key);
                    return name.substring(3);
                }
            } catch (IllegalAccessException e) {
                assert(false);
            }
        }
        return "UNKNOWN";
    }
    protected Object readResolve() throws java.io.ObjectStreamException {
        synchronized (AWTKeyStroke.class) {
            Class newClass = getClass();
            if (!newClass.equals(ctor.getDeclaringClass())) {
                registerSubclass(newClass);
            }
            return getCachedStroke(keyChar, keyCode, modifiers, onKeyRelease);
        }
    }
    private static int mapOldModifiers(int modifiers) {
        if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if ((modifiers & InputEvent.ALT_MASK) != 0) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
        }
        if ((modifiers & InputEvent.CTRL_MASK) != 0) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if ((modifiers & InputEvent.META_MASK) != 0) {
            modifiers |= InputEvent.META_DOWN_MASK;
        }
        modifiers &= InputEvent.SHIFT_DOWN_MASK
            | InputEvent.ALT_DOWN_MASK
            | InputEvent.ALT_GRAPH_DOWN_MASK
            | InputEvent.CTRL_DOWN_MASK
            | InputEvent.META_DOWN_MASK
            | InputEvent.BUTTON1_DOWN_MASK
            | InputEvent.BUTTON2_DOWN_MASK
            | InputEvent.BUTTON3_DOWN_MASK;
        return modifiers;
    }
    private static int mapNewModifiers(int modifiers) {
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            modifiers |= InputEvent.SHIFT_MASK;
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            modifiers |= InputEvent.ALT_MASK;
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            modifiers |= InputEvent.ALT_GRAPH_MASK;
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            modifiers |= InputEvent.CTRL_MASK;
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            modifiers |= InputEvent.META_MASK;
        }
        return modifiers;
    }
}
class VKCollection {
    Map code2name;
    Map name2code;
    public VKCollection() {
        code2name = new HashMap();
        name2code = new HashMap();
    }
    public synchronized void put(String name, Integer code) {
        assert((name != null) && (code != null));
        assert(findName(code) == null);
        assert(findCode(name) == null);
        code2name.put(code, name);
        name2code.put(name, code);
    }
    public synchronized Integer findCode(String name) {
        assert(name != null);
        return (Integer)name2code.get(name);
    }
    public synchronized String findName(Integer code) {
        assert(code != null);
        return (String)code2name.get(code);
    }
}
