public class Event implements Serializable {
    private static final long serialVersionUID = 5488922509400504703L;
    public static final int SHIFT_MASK = 1;
    public static final int CTRL_MASK = 2;
    public static final int META_MASK = 4;
    public static final int ALT_MASK = 8;
    public static final int HOME = 1000;
    public static final int END = 1001;
    public static final int PGUP = 1002;
    public static final int PGDN = 1003;
    public static final int UP = 1004;
    public static final int DOWN = 1005;
    public static final int LEFT = 1006;
    public static final int RIGHT = 1007;
    public static final int F1 = 1008;
    public static final int F2 = 1009;
    public static final int F3 = 1010;
    public static final int F4 = 1011;
    public static final int F5 = 1012;
    public static final int F6 = 1013;
    public static final int F7 = 1014;
    public static final int F8 = 1015;
    public static final int F9 = 1016;
    public static final int F10 = 1017;
    public static final int F11 = 1018;
    public static final int F12 = 1019;
    public static final int PRINT_SCREEN = 1020;
    public static final int SCROLL_LOCK = 1021;
    public static final int CAPS_LOCK = 1022;
    public static final int NUM_LOCK = 1023;
    public static final int PAUSE = 1024;
    public static final int INSERT = 1025;
    public static final int ENTER = 10;
    public static final int BACK_SPACE = 8;
    public static final int TAB = 9;
    public static final int ESCAPE = 27;
    public static final int DELETE = 127;
    public static final int WINDOW_DESTROY = 201;
    public static final int WINDOW_EXPOSE = 202;
    public static final int WINDOW_ICONIFY = 203;
    public static final int WINDOW_DEICONIFY = 204;
    public static final int WINDOW_MOVED = 205;
    public static final int KEY_PRESS = 401;
    public static final int KEY_RELEASE = 402;
    public static final int KEY_ACTION = 403;
    public static final int KEY_ACTION_RELEASE = 404;
    public static final int MOUSE_DOWN = 501;
    public static final int MOUSE_UP = 502;
    public static final int MOUSE_MOVE = 503;
    public static final int MOUSE_ENTER = 504;
    public static final int MOUSE_EXIT = 505;
    public static final int MOUSE_DRAG = 506;
    public static final int SCROLL_LINE_UP = 601;
    public static final int SCROLL_LINE_DOWN = 602;
    public static final int SCROLL_PAGE_UP = 603;
    public static final int SCROLL_PAGE_DOWN = 604;
    public static final int SCROLL_ABSOLUTE = 605;
    public static final int SCROLL_BEGIN = 606;
    public static final int SCROLL_END = 607;
    public static final int LIST_SELECT = 701;
    public static final int LIST_DESELECT = 702;
    public static final int ACTION_EVENT = 1001;
    public static final int LOAD_FILE = 1002;
    public static final int SAVE_FILE = 1003;
    public static final int GOT_FOCUS = 1004;
    public static final int LOST_FOCUS = 1005;
    public Object target;
    public long when;
    public int id;
    public int x;
    public int y;
    public int key;
    public int modifiers;
    public int clickCount;
    public Object arg;
    public Event evt;
    public Event(Object target, int id, Object arg) {
        this(target, 0l, id, 0, 0, 0, 0, arg);
    }
    public Event(Object target, long when, int id, int x, int y, int key, int modifiers) {
        this(target, when, id, x, y, key, modifiers, null);
    }
    public Event(Object target, long when, int id, int x, int y, int key, int modifiers, Object arg) {
        this.target = target;
        this.when = when;
        this.id = id;
        this.x = x;
        this.y = y;
        this.key = key;
        this.modifiers = modifiers;
        this.arg = arg;
    }
    @Override
    public String toString() {
        return getClass().getName() + "[" + paramString() + "]"; 
    }
    protected String paramString() {
        return "id=" + id + ",x=" + x + ",y=" + y + 
                (key != 0 ? ",key=" + key + getModifiersString() : "") + 
                ",target=" + target + 
                (arg != null ? ",arg=" + arg : ""); 
    }
    private String getModifiersString() {
        String strMod = ""; 
        if (shiftDown()) {
            strMod += ",shift"; 
        }
        if (controlDown()) {
            strMod += ",control"; 
        }
        if (metaDown()) {
            strMod += ",meta"; 
        }
        return strMod;
    }
    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }
    public boolean controlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }
    public boolean metaDown() {
        return (modifiers & META_MASK) != 0;
    }
    public boolean shiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }
}
