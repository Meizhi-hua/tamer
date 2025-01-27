public class XWindow extends XBaseWindow implements X11ComponentPeer {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XWindow");
    private static PlatformLogger insLog = PlatformLogger.getLogger("sun.awt.X11.insets.XWindow");
    private static PlatformLogger eventLog = PlatformLogger.getLogger("sun.awt.X11.event.XWindow");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.X11.focus.XWindow");
    private static PlatformLogger keyEventLog = PlatformLogger.getLogger("sun.awt.X11.kye.XWindow");
    private final static int AWT_MULTICLICK_SMUDGE = 4;
    static int rbutton = 0;
    static int lastX = 0, lastY = 0;
    static long lastTime = 0;
    static long lastButton = 0;
    static WeakReference lastWindowRef = null;
    static int clickCount = 0;
    int oldWidth = -1;
    int oldHeight = -1;
    protected PropMwmHints mwm_hints;
    protected static XAtom wm_protocols;
    protected static XAtom wm_delete_window;
    protected static XAtom wm_take_focus;
    private boolean stateChanged; 
    private int savedState; 
    XWindowAttributesData winAttr;
    protected X11GraphicsConfig graphicsConfig;
    protected AwtGraphicsConfigData graphicsConfigData;
    private boolean reparented;
    XWindow parent;
    Component target;
    private static int JAWT_LOCK_ERROR=0x00000001;
    private static int JAWT_LOCK_CLIP_CHANGED=0x00000002;
    private static int JAWT_LOCK_BOUNDS_CHANGED=0x00000004;
    private static int JAWT_LOCK_SURFACE_CHANGED=0x00000008;
    private int drawState = JAWT_LOCK_CLIP_CHANGED |
    JAWT_LOCK_BOUNDS_CHANGED |
    JAWT_LOCK_SURFACE_CHANGED;
    public static final String TARGET = "target",
        REPARENTED = "reparented"; 
    SurfaceData surfaceData;
    XRepaintArea paintArea;
    private static Font defaultFont;
    static synchronized Font getDefaultFont() {
        if (null == defaultFont) {
            defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
        }
        return defaultFont;
    }
    private int mouseButtonClickAllowed = 0;
    native int getNativeColor(Color clr, GraphicsConfiguration gc);
    native void getWMInsets(long window, long left, long top, long right, long bottom, long border);
    native long getTopWindow(long window, long rootWin);
    native void getWindowBounds(long window, long x, long y, long width, long height);
    private native static void initIDs();
    private static Field isPostedField;
    private static Field rawCodeField;
    private static Field primaryLevelUnicodeField;
    private static Field extendedKeyCodeField;
    static {
        initIDs();
    }
    XWindow(XCreateWindowParams params) {
        super(params);
    }
    XWindow() {
    }
    XWindow(long parentWindow, Rectangle bounds) {
        super(new XCreateWindowParams(new Object[] {
            BOUNDS, bounds,
            PARENT_WINDOW, Long.valueOf(parentWindow)}));
    }
    XWindow(Component target, long parentWindow, Rectangle bounds) {
        super(new XCreateWindowParams(new Object[] {
            BOUNDS, bounds,
            PARENT_WINDOW, Long.valueOf(parentWindow),
            TARGET, target}));
    }
    XWindow(Component target, long parentWindow) {
        this(target, parentWindow, new Rectangle(target.getBounds()));
    }
    XWindow(Component target) {
        this(target, (target.getParent() == null) ? 0 : getParentWindowID(target), new Rectangle(target.getBounds()));
    }
    XWindow(Object target) {
        this(null, 0, null);
    }
    XWindow(long parentWindow) {
        super(new XCreateWindowParams(new Object[] {
            PARENT_WINDOW, Long.valueOf(parentWindow),
            REPARENTED, Boolean.TRUE,
            EMBEDDED, Boolean.TRUE}));
    }
    protected void initGraphicsConfiguration() {
        graphicsConfig = (X11GraphicsConfig) target.getGraphicsConfiguration();
        graphicsConfigData = new AwtGraphicsConfigData(graphicsConfig.getAData());
    }
    void preInit(XCreateWindowParams params) {
        super.preInit(params);
        reparented = Boolean.TRUE.equals(params.get(REPARENTED));
        target = (Component)params.get(TARGET);
        initGraphicsConfiguration();
        AwtGraphicsConfigData gData = getGraphicsConfigurationData();
        X11GraphicsConfig config = (X11GraphicsConfig) getGraphicsConfiguration();
        XVisualInfo visInfo = gData.get_awt_visInfo();
        params.putIfNull(EVENT_MASK, XConstants.KeyPressMask | XConstants.KeyReleaseMask
            | XConstants.FocusChangeMask | XConstants.ButtonPressMask | XConstants.ButtonReleaseMask
            | XConstants.EnterWindowMask | XConstants.LeaveWindowMask | XConstants.PointerMotionMask
            | XConstants.ButtonMotionMask | XConstants.ExposureMask | XConstants.StructureNotifyMask);
        if (target != null) {
            params.putIfNull(BOUNDS, new Rectangle(target.getBounds()));
        } else {
            params.putIfNull(BOUNDS, new Rectangle(0, 0, MIN_SIZE, MIN_SIZE));
        }
        params.putIfNull(BORDER_PIXEL, Long.valueOf(0));
        getColorModel(); 
        params.putIfNull(COLORMAP, gData.get_awt_cmap());
        params.putIfNull(DEPTH, gData.get_awt_depth());
        params.putIfNull(VISUAL_CLASS, Integer.valueOf((int)XConstants.InputOutput));
        params.putIfNull(VISUAL, visInfo.get_visual());
        params.putIfNull(VALUE_MASK, XConstants.CWBorderPixel | XConstants.CWEventMask | XConstants.CWColormap);
        Long parentWindow = (Long)params.get(PARENT_WINDOW);
        if (parentWindow == null || parentWindow.longValue() == 0) {
            XToolkit.awtLock();
            try {
                int screen = visInfo.get_screen();
                if (screen != -1) {
                    params.add(PARENT_WINDOW, XlibWrapper.RootWindow(XToolkit.getDisplay(), screen));
                } else {
                    params.add(PARENT_WINDOW, XToolkit.getDefaultRootWindow());
                }
            } finally {
                XToolkit.awtUnlock();
            }
        }
        paintArea = new XRepaintArea();
        if (target != null) {
            this.parent = getParentXWindowObject(target.getParent());
        }
        params.putIfNull(BACKING_STORE, XToolkit.getBackingStoreType());
        XToolkit.awtLock();
        try {
            if (wm_protocols == null) {
                wm_protocols = XAtom.get("WM_PROTOCOLS");
                wm_delete_window = XAtom.get("WM_DELETE_WINDOW");
                wm_take_focus = XAtom.get("WM_TAKE_FOCUS");
            }
        }
        finally {
            XToolkit.awtUnlock();
        }
        winAttr = new XWindowAttributesData();
        savedState = XUtilConstants.WithdrawnState;
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
        setWMClass(getWMClass());
        surfaceData = graphicsConfig.createSurfaceData(this);
        Color c;
        if (target != null && (c = target.getBackground()) != null) {
            xSetBackground(c);
        }
    }
    public GraphicsConfiguration getGraphicsConfiguration() {
        if (graphicsConfig == null) {
            initGraphicsConfiguration();
        }
        return graphicsConfig;
    }
    public AwtGraphicsConfigData getGraphicsConfigurationData() {
        if (graphicsConfigData == null) {
            initGraphicsConfiguration();
        }
        return graphicsConfigData;
    }
    protected String[] getWMClass() {
        return new String[] {XToolkit.getCorrectXIDString(getClass().getName()), XToolkit.getAWTAppClassName()};
    }
    void setReparented(boolean newValue) {
        reparented = newValue;
    }
    boolean isReparented() {
        return reparented;
    }
    static long getParentWindowID(Component target) {
        ComponentPeer peer = target.getParent().getPeer();
        Component temp = target.getParent();
        while (!(peer instanceof XWindow))
        {
            temp = temp.getParent();
            peer = temp.getPeer();
        }
        if (peer != null && peer instanceof XWindow)
            return ((XWindow)peer).getContentWindow();
        else return 0;
    }
    static XWindow getParentXWindowObject(Component target) {
        if (target == null) return null;
        Component temp = target.getParent();
        if (temp == null) return null;
        ComponentPeer peer = temp.getPeer();
        if (peer == null) return null;
        while ((peer != null) && !(peer instanceof XWindow))
        {
            temp = temp.getParent();
            peer = temp.getPeer();
        }
        if (peer != null && peer instanceof XWindow)
            return (XWindow) peer;
        else return null;
    }
    boolean isParentOf(XWindow win) {
        if (!(target instanceof Container) || win == null || win.getTarget() == null) {
            return false;
        }
        Container parent = AWTAccessor.getComponentAccessor().getParent(win.target);
        while (parent != null && parent != target) {
            parent = AWTAccessor.getComponentAccessor().getParent(parent);
        }
        return (parent == target);
    }
    public Object getTarget() {
        return target;
    }
    public Component getEventSource() {
        return target;
    }
    public ColorModel getColorModel(int transparency) {
        return graphicsConfig.getColorModel (transparency);
    }
    public ColorModel getColorModel() {
        if (graphicsConfig != null) {
            return graphicsConfig.getColorModel ();
        }
        else {
            return XToolkit.getStaticColorModel();
        }
    }
    Graphics getGraphics(SurfaceData surfData, Color afore, Color aback, Font afont) {
        if (surfData == null) return null;
        Component target = (Component) this.target;
        Color bgColor = aback;
        if (bgColor == null) {
            bgColor = SystemColor.window;
        }
        Color fgColor = afore;
        if (fgColor == null) {
            fgColor = SystemColor.windowText;
        }
        Font font = afont;
        if (font == null) {
            font = XWindow.getDefaultFont();
        }
        return new SunGraphics2D(surfData, fgColor, bgColor, font);
    }
    public Graphics getGraphics() {
        return getGraphics(surfaceData,
                           target.getForeground(),
                           target.getBackground(),
                           target.getFont());
    }
    public FontMetrics getFontMetrics(Font font) {
        return Toolkit.getDefaultToolkit().getFontMetrics(font);
    }
    public Rectangle getTargetBounds() {
        return target.getBounds();
    }
    boolean prePostEvent(AWTEvent e) {
        return false;
    }
    static Method m_sendMessage;
    static void sendEvent(final AWTEvent e) {
        if (isPostedField == null) {
            isPostedField = SunToolkit.getField(AWTEvent.class, "isPosted");
        }
        SunToolkit.setSystemGenerated(e);
        PeerEvent pe = new PeerEvent(Toolkit.getDefaultToolkit(), new Runnable() {
                public void run() {
                    try {
                        isPostedField.setBoolean(e, true);
                    } catch (IllegalArgumentException e) {
                        assert(false);
                    } catch (IllegalAccessException e) {
                        assert(false);
                    }
                    ((Component)e.getSource()).dispatchEvent(e);
                }
            }, PeerEvent.ULTIMATE_PRIORITY_EVENT);
        if (focusLog.isLoggable(PlatformLogger.FINER) && (e instanceof FocusEvent)) focusLog.finer("Sending " + e);
        XToolkit.postEvent(XToolkit.targetToAppContext(e.getSource()), pe);
    }
    void postEvent(AWTEvent event) {
        XToolkit.postEvent(XToolkit.targetToAppContext(event.getSource()), event);
    }
    static void postEventStatic(AWTEvent event) {
        XToolkit.postEvent(XToolkit.targetToAppContext(event.getSource()), event);
    }
    public void postEventToEventQueue(final AWTEvent event) {
        if (!prePostEvent(event)) {
            postEvent(event);
        }
    }
    protected boolean doEraseBackground() {
        return true;
    }
    final public void xSetBackground(Color c) {
        XToolkit.awtLock();
        try {
            winBackground(c);
            if (!doEraseBackground()) {
                return;
            }
            ColorModel cm = getColorModel();
            int pixel = PixelConverter.instance.rgbToPixel(c.getRGB(), cm);
            XlibWrapper.XSetWindowBackground(XToolkit.getDisplay(), getContentWindow(), pixel);
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    public void setBackground(Color c) {
        xSetBackground(c);
    }
    Color backgroundColor;
    void winBackground(Color c) {
        backgroundColor = c;
    }
    public Color getWinBackground() {
        Color c = null;
        if (backgroundColor != null) {
            c = backgroundColor;
        } else if (parent != null) {
            c = parent.getWinBackground();
        }
        if (c instanceof SystemColor) {
            c = new Color(c.getRGB());
        }
        return c;
    }
    public boolean isEmbedded() {
        return embedded;
    }
    public  void repaint(int x,int y, int width, int height) {
        if (!isVisible()) {
            return;
        }
        Graphics g = getGraphics();
        if (g != null) {
            try {
                g.setClip(x,y,width,height);
                paint(g);
            } finally {
                g.dispose();
            }
        }
    }
    public  void repaint() {
        if (!isVisible()) {
            return;
        }
        Graphics g = getGraphics();
        if (g != null) {
            try {
                paint(g);
            } finally {
                g.dispose();
            }
        }
    }
    void paint(Graphics g) {
    }
    protected void flush(){
        XToolkit.awtLock();
        try {
            XlibWrapper.XFlush(XToolkit.getDisplay());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void popup(int x, int y, int width, int height) {
        xSetBounds(x, y, width, height);
    }
    public void handleExposeEvent(XEvent xev) {
        super.handleExposeEvent(xev);
        XExposeEvent xe = xev.get_xexpose();
        if (isEventDisabled(xev)) {
            return;
        }
        int x = xe.get_x();
        int y = xe.get_y();
        int w = xe.get_width();
        int h = xe.get_height();
        Component target = (Component)getEventSource();
        AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
        if (!compAccessor.getIgnoreRepaint(target)
            && compAccessor.getWidth(target) != 0
            && compAccessor.getHeight(target) != 0)
        {
            handleExposeEvent(target, x, y, w, h);
        }
    }
    public void handleExposeEvent(Component target, int x, int y, int w, int h) {
        PaintEvent event = PaintEventDispatcher.getPaintEventDispatcher().
            createPaintEvent(target, x, y, w, h);
        if (event != null) {
            postEventToEventQueue(event);
        }
    }
    static int getModifiers(int state, int button, int keyCode) {
        return getModifiers(state, button, keyCode, 0,  false);
    }
    static int getModifiers(int state, int button, int keyCode, int type, boolean wheel_mouse) {
        int modifiers = 0;
        if (((state & XConstants.ShiftMask) != 0) ^ (keyCode == KeyEvent.VK_SHIFT)) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if (((state & XConstants.ControlMask) != 0) ^ (keyCode == KeyEvent.VK_CONTROL)) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if (((state & XToolkit.metaMask) != 0) ^ (keyCode == KeyEvent.VK_META)) {
            modifiers |= InputEvent.META_DOWN_MASK;
        }
        if (((state & XToolkit.altMask) != 0) ^ (keyCode == KeyEvent.VK_ALT)) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        if (((state & XToolkit.modeSwitchMask) != 0) ^ (keyCode == KeyEvent.VK_ALT_GRAPH)) {
            modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
        }
        for (int i = 0; i < XConstants.buttonsMask.length; i ++){
            if (((state & XConstants.buttonsMask[i]) != 0) != (button == XConstants.buttons[i])){
                if (!wheel_mouse) {
                    modifiers |= InputEvent.getMaskForButton(i+1);
                }
            }
        }
        return modifiers;
    }
    static int getXModifiers(AWTKeyStroke stroke) {
        int mods = stroke.getModifiers();
        int res = 0;
        if ((mods & (InputEvent.SHIFT_DOWN_MASK | InputEvent.SHIFT_MASK)) != 0) {
            res |= XConstants.ShiftMask;
        }
        if ((mods & (InputEvent.CTRL_DOWN_MASK | InputEvent.CTRL_MASK)) != 0) {
            res |= XConstants.ControlMask;
        }
        if ((mods & (InputEvent.ALT_DOWN_MASK | InputEvent.ALT_MASK)) != 0) {
            res |= XToolkit.altMask;
        }
        if ((mods & (InputEvent.META_DOWN_MASK | InputEvent.META_MASK)) != 0) {
            res |= XToolkit.metaMask;
        }
        if ((mods & (InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.ALT_GRAPH_MASK)) != 0) {
            res |= XToolkit.modeSwitchMask;
        }
        return res;
    }
    static int getRightButtonNumber() {
        if (rbutton == 0) { 
            XToolkit.awtLock();
            try {
                rbutton = XlibWrapper.XGetPointerMapping(XToolkit.getDisplay(), XlibWrapper.ibuffer, 3);
            }
            finally {
                XToolkit.awtUnlock();
            }
        }
        return rbutton;
    }
    static int getMouseMovementSmudge() {
        return AWT_MULTICLICK_SMUDGE;
    }
    public void handleButtonPressRelease(XEvent xev) {
        super.handleButtonPressRelease(xev);
        XButtonEvent xbe = xev.get_xbutton();
        if (isEventDisabled(xev)) {
            return;
        }
        if (eventLog.isLoggable(PlatformLogger.FINE)) eventLog.fine(xbe.toString());
        long when;
        int modifiers;
        boolean popupTrigger = false;
        int button=0;
        boolean wheel_mouse = false;
        int lbutton = xbe.get_button();
        if (lbutton > SunToolkit.MAX_BUTTONS_SUPPORTED) {
            return;
        }
        int type = xev.get_type();
        when = xbe.get_time();
        long jWhen = XToolkit.nowMillisUTC_offset(when);
        int x = xbe.get_x();
        int y = xbe.get_y();
        if (xev.get_xany().get_window() != window) {
            Point localXY = toLocal(xbe.get_x_root(), xbe.get_y_root());
            x = localXY.x;
            y = localXY.y;
        }
        if (type == XConstants.ButtonPress) {
            mouseButtonClickAllowed |= XConstants.buttonsMask[lbutton];
            XWindow lastWindow = (lastWindowRef != null) ? ((XWindow)lastWindowRef.get()):(null);
            if (eventLog.isLoggable(PlatformLogger.FINEST)) eventLog.finest("lastWindow = " + lastWindow + ", lastButton "
                                                                   + lastButton + ", lastTime " + lastTime + ", multiClickTime "
                                                                   + XToolkit.getMultiClickTime());
            if (lastWindow == this && lastButton == lbutton && (when - lastTime) < XToolkit.getMultiClickTime()) {
                clickCount++;
            } else {
                clickCount = 1;
                lastWindowRef = new WeakReference(this);
                lastButton = lbutton;
                lastX = x;
                lastY = y;
            }
            lastTime = when;
            if (lbutton == getRightButtonNumber() || lbutton > 2) {
                popupTrigger = true;
            } else {
                popupTrigger = false;
            }
        }
        button = XConstants.buttons[lbutton - 1];
        if (lbutton == XConstants.buttons[3] ||
            lbutton == XConstants.buttons[4]) {
            wheel_mouse = true;
        }
        if ((button > XConstants.buttons[4]) && (!Toolkit.getDefaultToolkit().areExtraMouseButtonsEnabled())){
            return;
        }
        if (button > XConstants.buttons[4]){
            button -= 2;
        }
        modifiers = getModifiers(xbe.get_state(),button,0, type, wheel_mouse);
        if (!wheel_mouse) {
            MouseEvent me = new MouseEvent((Component)getEventSource(),
                                           type == XConstants.ButtonPress ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED,
                                           jWhen,modifiers, x, y,
                                           xbe.get_x_root(),
                                           xbe.get_y_root(),
                                           clickCount,popupTrigger,button);
            postEventToEventQueue(me);
            if ((type == XConstants.ButtonRelease) &&
                ((mouseButtonClickAllowed & XConstants.buttonsMask[lbutton]) != 0) ) 
            {
                postEventToEventQueue(me = new MouseEvent((Component)getEventSource(),
                                                     MouseEvent.MOUSE_CLICKED,
                                                     jWhen,
                                                     modifiers,
                                                     x, y,
                                                     xbe.get_x_root(),
                                                     xbe.get_y_root(),
                                                     clickCount,
                                                     false, button));
            }
        }
        else {
            if (xev.get_type() == XConstants.ButtonPress) {
                MouseWheelEvent mwe = new MouseWheelEvent((Component)getEventSource(),MouseEvent.MOUSE_WHEEL, jWhen,
                                                          modifiers,
                                                          x, y,
                                                          xbe.get_x_root(),
                                                          xbe.get_y_root(),
                                                          1,false,MouseWheelEvent.WHEEL_UNIT_SCROLL,
                                                          3,button==4 ?  -1 : 1);
                postEventToEventQueue(mwe);
            }
        }
        if (type == XConstants.ButtonRelease) {
            mouseButtonClickAllowed &= ~XConstants.buttonsMask[lbutton];
        }
    }
    public void handleMotionNotify(XEvent xev) {
        super.handleMotionNotify(xev);
        XMotionEvent xme = xev.get_xmotion();
        if (isEventDisabled(xev)) {
            return;
        }
        int mouseKeyState = 0; 
        final int buttonsNumber = ((SunToolkit)(Toolkit.getDefaultToolkit())).getNumberOfButtons();
        for (int i = 0; i < buttonsNumber; i++){
            if ((i != 4) && (i != 5)) {
                mouseKeyState = mouseKeyState | (xme.get_state() & XConstants.buttonsMask[i]);
            }
        }
        boolean isDragging = (mouseKeyState != 0);
        int mouseEventType = 0;
        if (isDragging) {
            mouseEventType = MouseEvent.MOUSE_DRAGGED;
        } else {
            mouseEventType = MouseEvent.MOUSE_MOVED;
        }
        int x = xme.get_x();
        int y = xme.get_y();
        XWindow lastWindow = (lastWindowRef != null) ? ((XWindow)lastWindowRef.get()):(null);
        if (!(lastWindow == this &&
              (xme.get_time() - lastTime) < XToolkit.getMultiClickTime()  &&
              (Math.abs(lastX - x) < AWT_MULTICLICK_SMUDGE &&
               Math.abs(lastY - y) < AWT_MULTICLICK_SMUDGE))) {
          clickCount = 0;
          lastWindowRef = null;
          mouseButtonClickAllowed = 0;
          lastTime = 0;
          lastX = 0;
          lastY = 0;
        }
        long jWhen = XToolkit.nowMillisUTC_offset(xme.get_time());
        int modifiers = getModifiers(xme.get_state(), 0, 0);
        boolean popupTrigger = false;
        Component source = (Component)getEventSource();
        if (xme.get_window() != window) {
            Point localXY = toLocal(xme.get_x_root(), xme.get_y_root());
            x = localXY.x;
            y = localXY.y;
        }
        if ((isDragging && clickCount == 0) || !isDragging) {
            MouseEvent mme = new MouseEvent(source, mouseEventType, jWhen,
                                            modifiers, x, y, xme.get_x_root(), xme.get_y_root(),
                                            clickCount, popupTrigger, MouseEvent.NOBUTTON);
            postEventToEventQueue(mme);
        }
    }
    public native boolean x11inputMethodLookupString(long event, long [] keysymArray);
    native boolean haveCurrentX11InputMethodInstance();
    private boolean mouseAboveMe;
    public boolean isMouseAbove() {
        synchronized (getStateLock()) {
            return mouseAboveMe;
        }
    }
    protected void setMouseAbove(boolean above) {
        synchronized (getStateLock()) {
            mouseAboveMe = above;
        }
    }
    protected void enterNotify(long window) {
        if (window == getWindow()) {
            setMouseAbove(true);
        }
    }
    protected void leaveNotify(long window) {
        if (window == getWindow()) {
            setMouseAbove(false);
        }
    }
    public void handleXCrossingEvent(XEvent xev) {
        super.handleXCrossingEvent(xev);
        XCrossingEvent xce = xev.get_xcrossing();
        if (eventLog.isLoggable(PlatformLogger.FINEST)) eventLog.finest(xce.toString());
        if (xce.get_type() == XConstants.EnterNotify) {
            enterNotify(xce.get_window());
        } else { 
            leaveNotify(xce.get_window());
        }
        XWindowPeer toplevel = getToplevelXWindow();
        if (toplevel != null && !toplevel.isModalBlocked()){
            if (xce.get_mode() != XConstants.NotifyNormal) {
                if (xce.get_type() == XConstants.EnterNotify) {
                    XAwtState.setComponentMouseEntered(getEventSource());
                    XGlobalCursorManager.nativeUpdateCursor(getEventSource());
                } else { 
                    XAwtState.setComponentMouseEntered(null);
                }
                return;
            }
        }
        long childWnd = xce.get_subwindow();
        if (childWnd != XConstants.None) {
            XBaseWindow child = XToolkit.windowToXWindow(childWnd);
            if (child != null && child instanceof XWindow &&
                !child.isEventDisabled(xev))
            {
                return;
            }
        }
        final Component compWithMouse = XAwtState.getComponentMouseEntered();
        if (toplevel != null) {
            if(!toplevel.isModalBlocked()){
                if (xce.get_type() == XConstants.EnterNotify) {
                    XAwtState.setComponentMouseEntered(getEventSource());
                    XGlobalCursorManager.nativeUpdateCursor(getEventSource());
                } else { 
                    XAwtState.setComponentMouseEntered(null);
                }
            } else {
                ((XComponentPeer) AWTAccessor.getComponentAccessor().getPeer(target))
                    .pSetCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
        if (isEventDisabled(xev)) {
            return;
        }
        long jWhen = XToolkit.nowMillisUTC_offset(xce.get_time());
        int modifiers = getModifiers(xce.get_state(),0,0);
        int clickCount = 0;
        boolean popupTrigger = false;
        int x = xce.get_x();
        int y = xce.get_y();
        if (xce.get_window() != window) {
            Point localXY = toLocal(xce.get_x_root(), xce.get_y_root());
            x = localXY.x;
            y = localXY.y;
        }
        if (compWithMouse != null) {
            MouseEvent me = new MouseEvent(compWithMouse,
                MouseEvent.MOUSE_EXITED, jWhen, modifiers, xce.get_x(),
                xce.get_y(), xce.get_x_root(), xce.get_y_root(), clickCount, popupTrigger,
                MouseEvent.NOBUTTON);
            postEventToEventQueue(me);
            eventLog.finest("Clearing last window ref");
            lastWindowRef = null;
        }
        if (xce.get_type() == XConstants.EnterNotify) {
            MouseEvent me = new MouseEvent(getEventSource(), MouseEvent.MOUSE_ENTERED,
                jWhen, modifiers, xce.get_x(), xce.get_y(), xce.get_x_root(), xce.get_y_root(), clickCount,
                popupTrigger, MouseEvent.NOBUTTON);
            postEventToEventQueue(me);
        }
    }
    public void doLayout(int x, int y, int width, int height) {}
    public void handleConfigureNotifyEvent(XEvent xev) {
        Rectangle oldBounds = getBounds();
        super.handleConfigureNotifyEvent(xev);
        insLog.finer("Configure, {0}, event disabled: {1}",
                     xev.get_xconfigure(), isEventDisabled(xev));
        if (isEventDisabled(xev)) {
            return;
        }
        Rectangle bounds = getBounds();
        if (!bounds.getSize().equals(oldBounds.getSize())) {
            postEventToEventQueue(new ComponentEvent(getEventSource(), ComponentEvent.COMPONENT_RESIZED));
        }
        if (!bounds.getLocation().equals(oldBounds.getLocation())) {
            postEventToEventQueue(new ComponentEvent(getEventSource(), ComponentEvent.COMPONENT_MOVED));
        }
    }
    public void handleMapNotifyEvent(XEvent xev) {
        super.handleMapNotifyEvent(xev);
        log.fine("Mapped {0}", this);
        if (isEventDisabled(xev)) {
            return;
        }
        ComponentEvent ce;
        ce = new ComponentEvent(getEventSource(), ComponentEvent.COMPONENT_SHOWN);
        postEventToEventQueue(ce);
    }
    public void handleUnmapNotifyEvent(XEvent xev) {
        super.handleUnmapNotifyEvent(xev);
        if (isEventDisabled(xev)) {
            return;
        }
        ComponentEvent ce;
        ce = new ComponentEvent(target, ComponentEvent.COMPONENT_HIDDEN);
        postEventToEventQueue(ce);
    }
    private void dumpKeysymArray(XKeyEvent ev) {
        keyEventLog.fine("  "+Long.toHexString(XlibWrapper.XKeycodeToKeysym(XToolkit.getDisplay(), ev.get_keycode(), 0))+
                         "\n        "+Long.toHexString(XlibWrapper.XKeycodeToKeysym(XToolkit.getDisplay(), ev.get_keycode(), 1))+
                         "\n        "+Long.toHexString(XlibWrapper.XKeycodeToKeysym(XToolkit.getDisplay(), ev.get_keycode(), 2))+
                         "\n        "+Long.toHexString(XlibWrapper.XKeycodeToKeysym(XToolkit.getDisplay(), ev.get_keycode(), 3)));
    }
    int keysymToUnicode( long keysym, int state ) {
        return XKeysym.convertKeysym( keysym, state );
    }
    int keyEventType2Id( int xEventType ) {
        return xEventType == XConstants.KeyPress ? java.awt.event.KeyEvent.KEY_PRESSED :
               xEventType == XConstants.KeyRelease ? java.awt.event.KeyEvent.KEY_RELEASED : 0;
    }
    static private long xkeycodeToKeysym(XKeyEvent ev) {
        return XKeysym.getKeysym( ev );
    }
    private long xkeycodeToPrimaryKeysym(XKeyEvent ev) {
        return XKeysym.xkeycode2primary_keysym( ev );
    }
    static private int primaryUnicode2JavaKeycode(int uni) {
        return (uni > 0? sun.awt.ExtendedKeyCodes.getExtendedKeyCodeForChar(uni) : 0);
    }
    void logIncomingKeyEvent(XKeyEvent ev) {
        keyEventLog.fine("--XWindow.java:handleKeyEvent:"+ev);
        dumpKeysymArray(ev);
        keyEventLog.fine("XXXXXXXXXXXXXX javakeycode will be most probably:0x"+ Integer.toHexString(XKeysym.getJavaKeycodeOnly(ev)));
    }
    public void handleKeyPress(XEvent xev) {
        super.handleKeyPress(xev);
        XKeyEvent ev = xev.get_xkey();
        if (eventLog.isLoggable(PlatformLogger.FINE)) eventLog.fine(ev.toString());
        if (isEventDisabled(xev)) {
            return;
        }
        handleKeyPress(ev);
    }
    final void handleKeyPress(XKeyEvent ev) {
        long keysym[] = new long[2];
        int unicodeKey = 0;
        keysym[0] = XConstants.NoSymbol;
        if (keyEventLog.isLoggable(PlatformLogger.FINE)) {
            logIncomingKeyEvent( ev );
        }
        if ( 
            haveCurrentX11InputMethodInstance()) {
            if (x11inputMethodLookupString(ev.pData, keysym)) {
                if (keyEventLog.isLoggable(PlatformLogger.FINE)) {
                    keyEventLog.fine("--XWindow.java XIM did process event; return; dec keysym processed:"+(keysym[0])+
                                   "; hex keysym processed:"+Long.toHexString(keysym[0])
                                   );
                }
                return;
            }else {
                unicodeKey = keysymToUnicode( keysym[0], ev.get_state() );
                if (keyEventLog.isLoggable(PlatformLogger.FINE)) {
                    keyEventLog.fine("--XWindow.java XIM did NOT process event, hex keysym:"+Long.toHexString(keysym[0])+"\n"+
                                     "                                         unicode key:"+Integer.toHexString((int)unicodeKey));
                }
            }
        }else  {
            keysym[0] = xkeycodeToKeysym(ev);
            unicodeKey = keysymToUnicode( keysym[0], ev.get_state() );
            if (keyEventLog.isLoggable(PlatformLogger.FINE)) {
                keyEventLog.fine("--XWindow.java XIM is absent;             hex keysym:"+Long.toHexString(keysym[0])+"\n"+
                                 "                                         unicode key:"+Integer.toHexString((int)unicodeKey));
            }
        }
        XKeysym.Keysym2JavaKeycode jkc = XKeysym.getJavaKeycode(ev);
        if( jkc == null ) {
            jkc = new XKeysym.Keysym2JavaKeycode(java.awt.event.KeyEvent.VK_UNDEFINED, java.awt.event.KeyEvent.KEY_LOCATION_UNKNOWN);
        }
        int unicodeFromPrimaryKeysym = keysymToUnicode( xkeycodeToPrimaryKeysym(ev) ,0);
        if (keyEventLog.isLoggable(PlatformLogger.FINE)) {
            keyEventLog.fine(">>>Fire Event:"+
               (ev.get_type() == XConstants.KeyPress ? "KEY_PRESSED; " : "KEY_RELEASED; ")+
               "jkeycode:decimal="+jkc.getJavaKeycode()+
               ", hex=0x"+Integer.toHexString(jkc.getJavaKeycode())+"; "+
               " legacy jkeycode: decimal="+XKeysym.getLegacyJavaKeycodeOnly(ev)+
               ", hex=0x"+Integer.toHexString(XKeysym.getLegacyJavaKeycodeOnly(ev))+"; "
            );
        }
        int jkeyToReturn = XKeysym.getLegacyJavaKeycodeOnly(ev); 
        int jkeyExtended = jkc.getJavaKeycode() == java.awt.event.KeyEvent.VK_UNDEFINED ?
                           primaryUnicode2JavaKeycode( unicodeFromPrimaryKeysym ) :
                             jkc.getJavaKeycode();
        postKeyEvent( java.awt.event.KeyEvent.KEY_PRESSED,
                          ev.get_time(),
                          jkeyToReturn,
                          (unicodeKey == 0 ? java.awt.event.KeyEvent.CHAR_UNDEFINED : unicodeKey),
                          jkc.getKeyLocation(),
                          ev.get_state(),ev.getPData(), XKeyEvent.getSize(), (long)(ev.get_keycode()),
                          unicodeFromPrimaryKeysym,
                          jkeyExtended);
        if( unicodeKey > 0 ) {
                keyEventLog.fine("fire _TYPED on "+unicodeKey);
                postKeyEvent( java.awt.event.KeyEvent.KEY_TYPED,
                              ev.get_time(),
                              java.awt.event.KeyEvent.VK_UNDEFINED,
                              unicodeKey,
                              java.awt.event.KeyEvent.KEY_LOCATION_UNKNOWN,
                              ev.get_state(),ev.getPData(), XKeyEvent.getSize(), (long)0,
                              unicodeFromPrimaryKeysym,
                              java.awt.event.KeyEvent.VK_UNDEFINED);
        }
    }
    public void handleKeyRelease(XEvent xev) {
        super.handleKeyRelease(xev);
        XKeyEvent ev = xev.get_xkey();
        if (eventLog.isLoggable(PlatformLogger.FINE)) eventLog.fine(ev.toString());
        if (isEventDisabled(xev)) {
            return;
        }
        handleKeyRelease(ev);
    }
    private void handleKeyRelease(XKeyEvent ev) {
        long keysym[] = new long[2];
        int unicodeKey = 0;
        keysym[0] = XConstants.NoSymbol;
        if (keyEventLog.isLoggable(PlatformLogger.FINE)) {
            logIncomingKeyEvent( ev );
        }
        XKeysym.Keysym2JavaKeycode jkc = XKeysym.getJavaKeycode(ev);
        if( jkc == null ) {
            jkc = new XKeysym.Keysym2JavaKeycode(java.awt.event.KeyEvent.VK_UNDEFINED, java.awt.event.KeyEvent.KEY_LOCATION_UNKNOWN);
        }
        if (keyEventLog.isLoggable(PlatformLogger.FINE)) {
            keyEventLog.fine(">>>Fire Event:"+
               (ev.get_type() == XConstants.KeyPress ? "KEY_PRESSED; " : "KEY_RELEASED; ")+
               "jkeycode:decimal="+jkc.getJavaKeycode()+
               ", hex=0x"+Integer.toHexString(jkc.getJavaKeycode())+"; "+
               " legacy jkeycode: decimal="+XKeysym.getLegacyJavaKeycodeOnly(ev)+
               ", hex=0x"+Integer.toHexString(XKeysym.getLegacyJavaKeycodeOnly(ev))+"; "
            );
        }
        unicodeKey = keysymToUnicode( xkeycodeToKeysym(ev), ev.get_state() );
        int unicodeFromPrimaryKeysym = keysymToUnicode( xkeycodeToPrimaryKeysym(ev) ,0);
        int jkeyToReturn = XKeysym.getLegacyJavaKeycodeOnly(ev); 
        int jkeyExtended = jkc.getJavaKeycode() == java.awt.event.KeyEvent.VK_UNDEFINED ?
                           primaryUnicode2JavaKeycode( unicodeFromPrimaryKeysym ) :
                             jkc.getJavaKeycode();
        postKeyEvent(  java.awt.event.KeyEvent.KEY_RELEASED,
                          ev.get_time(),
                          jkeyToReturn,
                          (unicodeKey == 0 ? java.awt.event.KeyEvent.CHAR_UNDEFINED : unicodeKey),
                          jkc.getKeyLocation(),
                          ev.get_state(),ev.getPData(), XKeyEvent.getSize(), (long)(ev.get_keycode()),
                          unicodeFromPrimaryKeysym,
                          jkeyExtended);
    }
    int getWMState() {
        if (stateChanged) {
            stateChanged = false;
            WindowPropertyGetter getter =
                new WindowPropertyGetter(window, XWM.XA_WM_STATE, 0, 1, false,
                                         XWM.XA_WM_STATE);
            try {
                int status = getter.execute();
                if (status != XConstants.Success || getter.getData() == 0) {
                    return savedState = XUtilConstants.WithdrawnState;
                }
                if (getter.getActualType() != XWM.XA_WM_STATE.getAtom() && getter.getActualFormat() != 32) {
                    return savedState = XUtilConstants.WithdrawnState;
                }
                savedState = (int)Native.getCard32(getter.getData());
            } finally {
                getter.dispose();
            }
        }
        return savedState;
    }
    protected void stateChanged(long time, int oldState, int newState) {
    }
    @Override
    public void handlePropertyNotify(XEvent xev) {
        super.handlePropertyNotify(xev);
        XPropertyEvent ev = xev.get_xproperty();
        if (ev.get_atom() == XWM.XA_WM_STATE.getAtom()) {
            stateChanged = true;
            stateChanged(ev.get_time(), savedState, getWMState());
        }
    }
    public void reshape(Rectangle bounds) {
        reshape(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    public void reshape(int x, int y, int width, int height) {
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        xSetBounds(x, y, width, height);
        validateSurface();
        layout();
    }
    public void layout() {}
    boolean isShowing() {
        return visible;
    }
    boolean isResizable() {
        return true;
    }
    boolean isLocationByPlatform() {
        return false;
    }
    void updateSizeHints() {
        updateSizeHints(x, y, width, height);
    }
    void updateSizeHints(int x, int y, int width, int height) {
        long flags = XUtilConstants.PSize | (isLocationByPlatform() ? 0 : (XUtilConstants.PPosition | XUtilConstants.USPosition));
        if (!isResizable()) {
            log.finer("Window {0} is not resizable", this);
            flags |= XUtilConstants.PMinSize | XUtilConstants.PMaxSize;
        } else {
            log.finer("Window {0} is resizable", this);
        }
        setSizeHints(flags, x, y, width, height);
    }
    void updateSizeHints(int x, int y) {
        long flags = isLocationByPlatform() ? 0 : (XUtilConstants.PPosition | XUtilConstants.USPosition);
        if (!isResizable()) {
            log.finer("Window {0} is not resizable", this);
            flags |= XUtilConstants.PMinSize | XUtilConstants.PMaxSize | XUtilConstants.PSize;
        } else {
            log.finer("Window {0} is resizable", this);
        }
        setSizeHints(flags, x, y, width, height);
    }
    void validateSurface() {
        if ((width != oldWidth) || (height != oldHeight)) {
            doValidateSurface();
            oldWidth = width;
            oldHeight = height;
        }
    }
    final void doValidateSurface() {
        SurfaceData oldData = surfaceData;
        if (oldData != null) {
            surfaceData = graphicsConfig.createSurfaceData(this);
            oldData.invalidate();
        }
    }
    public SurfaceData getSurfaceData() {
        return surfaceData;
    }
    public void dispose() {
        SurfaceData oldData = surfaceData;
        surfaceData = null;
        if (oldData != null) {
            oldData.invalidate();
        }
        XToolkit.targetDisposedPeer(target, this);
        destroy();
    }
    public Point getLocationOnScreen() {
        synchronized (target.getTreeLock()) {
            Component comp = target;
            while (comp != null && !(comp instanceof Window)) {
                comp = AWTAccessor.getComponentAccessor().getParent(comp);
            }
            if (comp == null || comp instanceof sun.awt.EmbeddedFrame) {
                return toGlobal(0, 0);
            }
            XToolkit.awtLock();
            try {
                Object wpeer = XToolkit.targetToPeer(comp);
                if (wpeer == null
                    || !(wpeer instanceof XDecoratedPeer)
                    || ((XDecoratedPeer)wpeer).configure_seen)
                {
                    return toGlobal(0, 0);
                }
                Point pt = toOtherWindow(getContentWindow(),
                                         ((XDecoratedPeer)wpeer).getContentWindow(),
                                         0, 0);
                if (pt == null) {
                    pt = new Point(((XBaseWindow)wpeer).getAbsoluteX(), ((XBaseWindow)wpeer).getAbsoluteY());
                }
                pt.x += comp.getX();
                pt.y += comp.getY();
                return pt;
            } finally {
                XToolkit.awtUnlock();
            }
        }
    }
    static Field bdata;
    static void setBData(KeyEvent e, byte[] data) {
        try {
            if (bdata == null) {
                bdata = SunToolkit.getField(java.awt.AWTEvent.class, "bdata");
            }
            bdata.set(e, data);
        } catch (IllegalAccessException ex) {
            assert false;
        }
    }
    public void postKeyEvent(int id, long when, int keyCode, int keyChar,
        int keyLocation, int state, long event, int eventSize, long rawCode,
        int unicodeFromPrimaryKeysym, int extendedKeyCode)
    {
        long jWhen = XToolkit.nowMillisUTC_offset(when);
        int modifiers = getModifiers(state, 0, keyCode);
        if (rawCodeField == null) {
            rawCodeField = XToolkit.getField(KeyEvent.class, "rawCode");
        }
        if (primaryLevelUnicodeField == null) {
            primaryLevelUnicodeField = XToolkit.getField(KeyEvent.class, "primaryLevelUnicode");
        }
        if (extendedKeyCodeField == null) {
            extendedKeyCodeField = XToolkit.getField(KeyEvent.class, "extendedKeyCode");
        }
        KeyEvent ke = new KeyEvent((Component)getEventSource(), id, jWhen,
                                   modifiers, keyCode, (char)keyChar, keyLocation);
        if (event != 0) {
            byte[] data = Native.toBytes(event, eventSize);
            setBData(ke, data);
        }
        try {
            rawCodeField.set(ke, rawCode);
            primaryLevelUnicodeField.set(ke, (long)unicodeFromPrimaryKeysym);
            extendedKeyCodeField.set(ke, (long)extendedKeyCode);
        } catch (IllegalArgumentException e) {
            assert(false);
        } catch (IllegalAccessException e) {
            assert(false);
        }
        postEventToEventQueue(ke);
    }
    static native int getAWTKeyCodeForKeySym(int keysym);
    static native int getKeySymForAWTKeyCode(int keycode);
    public PropMwmHints getMWMHints() {
        if (mwm_hints == null) {
            mwm_hints = new PropMwmHints();
            if (!XWM.XA_MWM_HINTS.getAtomData(getWindow(), mwm_hints.pData, MWMConstants.PROP_MWM_HINTS_ELEMENTS)) {
                mwm_hints.zero();
            }
        }
        return mwm_hints;
    }
    public void setMWMHints(PropMwmHints hints) {
        mwm_hints = hints;
        if (hints != null) {
            XWM.XA_MWM_HINTS.setAtomData(getWindow(), mwm_hints.pData, MWMConstants.PROP_MWM_HINTS_ELEMENTS);
        }
    }
    protected final void initWMProtocols() {
        wm_protocols.setAtomListProperty(this, getWMProtocols());
    }
    protected XAtomList getWMProtocols() {
        return new XAtomList();
    }
    private boolean fullScreenExclusiveModeState = false;
    @Override
    public void setFullScreenExclusiveModeState(boolean state) {
        synchronized (getStateLock()) {
            fullScreenExclusiveModeState = state;
        }
    }
    public final boolean isFullScreenExclusiveMode() {
        synchronized (getStateLock()) {
            return fullScreenExclusiveModeState;
        }
    }
}
