public abstract class InputEvent extends ComponentEvent {
    private static final long serialVersionUID = -2482525981698309786L;
    public static final int SHIFT_MASK = 1;
    public static final int CTRL_MASK = 2;
    public static final int META_MASK = 4;
    public static final int ALT_MASK = 8;
    public static final int ALT_GRAPH_MASK = 32;
    public static final int BUTTON1_MASK = 16;
    public static final int BUTTON2_MASK = 8;
    public static final int BUTTON3_MASK = 4;
    public static final int SHIFT_DOWN_MASK = 64;
    public static final int CTRL_DOWN_MASK = 128;
    public static final int META_DOWN_MASK = 256;
    public static final int ALT_DOWN_MASK = 512;
    public static final int BUTTON1_DOWN_MASK = 1024;
    public static final int BUTTON2_DOWN_MASK = 2048;
    public static final int BUTTON3_DOWN_MASK = 4096;
    public static final int ALT_GRAPH_DOWN_MASK = 8192;
    private static final int DOWN_MASKS = SHIFT_DOWN_MASK | CTRL_DOWN_MASK |
            META_DOWN_MASK | ALT_DOWN_MASK | BUTTON1_DOWN_MASK |
            BUTTON2_DOWN_MASK | BUTTON3_DOWN_MASK | ALT_GRAPH_DOWN_MASK;
    private long when;
    private int modifiersEx;
    public static String getModifiersExText(int modifiers) {
        return MouseEvent.addMouseModifiersExText(
                KeyEvent.getKeyModifiersExText(modifiers), modifiers);
    }
    static int extractExFlags(int modifiers) {
        int exFlags = modifiers & DOWN_MASKS;
        if ((modifiers & SHIFT_MASK) != 0) {
            exFlags |= SHIFT_DOWN_MASK;
        }
        if ((modifiers & CTRL_MASK) != 0) {
            exFlags |= CTRL_DOWN_MASK;
        }
        if ((modifiers & META_MASK) != 0) {
            exFlags |= META_DOWN_MASK;
        }
        if ((modifiers & ALT_MASK) != 0) {
            exFlags |= ALT_DOWN_MASK;
        }
        if ((modifiers & ALT_GRAPH_MASK) != 0) {
            exFlags |= ALT_GRAPH_DOWN_MASK;
        }
        if ((modifiers & BUTTON1_MASK) != 0) {
            exFlags |= BUTTON1_DOWN_MASK;
        }
        if ((modifiers & BUTTON2_MASK) != 0) {
            exFlags |= BUTTON2_DOWN_MASK;
        }
        if ((modifiers & BUTTON3_MASK) != 0) {
            exFlags |= BUTTON3_DOWN_MASK;
        }
        return exFlags;
    }
    InputEvent(Component source, int id, long when, int modifiers) {
        super(source, id);
        this.when = when;
        modifiersEx = extractExFlags(modifiers);
    }
    public int getModifiers() {
        int modifiers = 0;
        if ((modifiersEx & SHIFT_DOWN_MASK) != 0) {
            modifiers |= SHIFT_MASK;
        }
        if ((modifiersEx & CTRL_DOWN_MASK) != 0) {
            modifiers |= CTRL_MASK;
        }
        if ((modifiersEx & META_DOWN_MASK) != 0) {
            modifiers |= META_MASK;
        }
        if ((modifiersEx & ALT_DOWN_MASK) != 0) {
            modifiers |= ALT_MASK;
        }
        if ((modifiersEx & ALT_GRAPH_DOWN_MASK) != 0) {
            modifiers |= ALT_GRAPH_MASK;
        }
        if ((modifiersEx & BUTTON1_DOWN_MASK) != 0) {
            modifiers |= BUTTON1_MASK;
        }
        if ((modifiersEx & BUTTON2_DOWN_MASK) != 0) {
            modifiers |= BUTTON2_MASK;
        }
        if ((modifiersEx & BUTTON3_DOWN_MASK) != 0) {
            modifiers |= BUTTON3_MASK;
        }
        return modifiers;
    }
    public int getModifiersEx() {
        return modifiersEx;
    }
    void setModifiers(int modifiers) {
        modifiersEx = extractExFlags(modifiers);
    }
    public boolean isAltDown() {
        return ((modifiersEx & ALT_DOWN_MASK) != 0);
    }
    public boolean isAltGraphDown() {
        return ((modifiersEx & ALT_GRAPH_DOWN_MASK) != 0);
    }
    public boolean isControlDown() {
        return ((modifiersEx & CTRL_DOWN_MASK) != 0);
    }
    public boolean isMetaDown() {
        return ((modifiersEx & META_DOWN_MASK) != 0);
    }
    public boolean isShiftDown() {
        return ((modifiersEx & SHIFT_DOWN_MASK) != 0);
    }
    public long getWhen() {
        return when;
    }
    @Override
    public void consume() {
        super.consume();
    }
    @Override
    public boolean isConsumed() {
        return super.isConsumed();
    }
}
