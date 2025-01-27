public class WWindowPeer extends WPanelPeer implements WindowPeer,
       DisplayChangedListener
{
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WWindowPeer");
    private static final PlatformLogger screenLog = PlatformLogger.getLogger("sun.awt.windows.screen.WWindowPeer");
    private WWindowPeer modalBlocker = null;
    private boolean isOpaque;
    private TranslucentWindowPainter painter;
    private final static StringBuffer ACTIVE_WINDOWS_KEY =
        new StringBuffer("active_windows_list");
    private static PropertyChangeListener activeWindowListener =
        new ActiveWindowListener();
    private final static PropertyChangeListener guiDisposedListener =
        new GuiDisposedListener();
    private WindowListener windowListener;
    private static native void initIDs();
    static {
        initIDs();
    }
    protected void disposeImpl() {
        AppContext appContext = SunToolkit.targetToAppContext(target);
        synchronized (appContext) {
            List<WWindowPeer> l = (List<WWindowPeer>)appContext.get(ACTIVE_WINDOWS_KEY);
            if (l != null) {
                l.remove(this);
            }
        }
        GraphicsConfiguration gc = getGraphicsConfiguration();
        ((Win32GraphicsDevice)gc.getDevice()).removeDisplayChangedListener(this);
        synchronized (getStateLock()) {
            TranslucentWindowPainter currentPainter = painter;
            if (currentPainter != null) {
                currentPainter.flush();
            }
        }
        super.disposeImpl();
    }
    public void toFront() {
        updateFocusableWindowState();
        _toFront();
    }
    native void _toFront();
    public native void toBack();
    public native void setAlwaysOnTopNative(boolean value);
    public void setAlwaysOnTop(boolean value) {
        if ((value && ((Window)target).isVisible()) || !value) {
            setAlwaysOnTopNative(value);
        }
    }
    public void updateFocusableWindowState() {
        setFocusableWindow(((Window)target).isFocusableWindow());
    }
    native void setFocusableWindow(boolean value);
    public void setTitle(String title) {
        if (title == null) {
            title = "";
        }
        _setTitle(title);
    }
    native void _setTitle(String title);
    public void setResizable(boolean resizable) {
        _setResizable(resizable);
    }
    public native void _setResizable(boolean resizable);
    WWindowPeer(Window target) {
        super(target);
    }
    void initialize() {
        super.initialize();
        updateInsets(insets_);
        Font f = ((Window)target).getFont();
        if (f == null) {
            f = defaultFont;
            ((Window)target).setFont(f);
            setFont(f);
        }
        GraphicsConfiguration gc = getGraphicsConfiguration();
        ((Win32GraphicsDevice)gc.getDevice()).addDisplayChangedListener(this);
        initActiveWindowsTracking((Window)target);
        updateIconImages();
        Shape shape = ((Window)target).getShape();
        if (shape != null) {
            applyShape(Region.getInstance(shape, null));
        }
        float opacity = ((Window)target).getOpacity();
        if (opacity < 1.0f) {
            setOpacity(opacity);
        }
        synchronized (getStateLock()) {
            this.isOpaque = true;
            setOpaque(((Window)target).isOpaque());
        }
    }
    native void createAwtWindow(WComponentPeer parent);
    private volatile Window.Type windowType = Window.Type.NORMAL;
    void preCreate(WComponentPeer parent) {
        windowType = ((Window)target).getType();
    }
    void create(WComponentPeer parent) {
        preCreate(parent);
        createAwtWindow(parent);
    }
    protected void realShow() {
        super.show();
    }
    public void show() {
        updateFocusableWindowState();
        boolean alwaysOnTop = ((Window)target).isAlwaysOnTop();
        updateGC();
        realShow();
        updateMinimumSize();
        if (((Window)target).isAlwaysOnTopSupported() && alwaysOnTop) {
            setAlwaysOnTop(alwaysOnTop);
        }
        synchronized (getStateLock()) {
            if (!isOpaque) {
                updateWindow(true);
            }
        }
    }
    native void updateInsets(Insets i);
    static native int getSysMinWidth();
    static native int getSysMinHeight();
    static native int getSysIconWidth();
    static native int getSysIconHeight();
    static native int getSysSmIconWidth();
    static native int getSysSmIconHeight();
    native void setIconImagesData(int[] iconRaster, int w, int h,
                                  int[] smallIconRaster, int smw, int smh);
    synchronized native void reshapeFrame(int x, int y, int width, int height);
    public boolean requestWindowFocus(CausedFocusEvent.Cause cause) {
        if (!focusAllowedFor()) {
            return false;
        }
        return requestWindowFocus(cause == CausedFocusEvent.Cause.MOUSE_EVENT);
    }
    public native boolean requestWindowFocus(boolean isMouseEventCause);
    public boolean focusAllowedFor() {
        Window window = (Window)this.target;
        if (!window.isVisible() ||
            !window.isEnabled() ||
            !window.isFocusableWindow())
        {
            return false;
        }
        if (isModalBlocked()) {
            return false;
        }
        return true;
    }
    public void hide() {
        WindowListener listener = windowListener;
        if (listener != null) {
            listener.windowClosing(new WindowEvent((Window)target, WindowEvent.WINDOW_CLOSING));
        }
        super.hide();
    }
    void preprocessPostEvent(AWTEvent event) {
        if (event instanceof WindowEvent) {
            WindowListener listener = windowListener;
            if (listener != null) {
                switch(event.getID()) {
                    case WindowEvent.WINDOW_CLOSING:
                        listener.windowClosing((WindowEvent)event);
                        break;
                    case WindowEvent.WINDOW_ICONIFIED:
                        listener.windowIconified((WindowEvent)event);
                        break;
                }
            }
        }
    }
    synchronized void addWindowListener(WindowListener l) {
        windowListener = AWTEventMulticaster.add(windowListener, l);
    }
    synchronized void removeWindowListener(WindowListener l) {
        windowListener = AWTEventMulticaster.remove(windowListener, l);
    }
    public void updateMinimumSize() {
        Dimension minimumSize = null;
        if (((Component)target).isMinimumSizeSet()) {
            minimumSize = ((Component)target).getMinimumSize();
        }
        if (minimumSize != null) {
            int msw = getSysMinWidth();
            int msh = getSysMinHeight();
            int w = (minimumSize.width >= msw) ? minimumSize.width : msw;
            int h = (minimumSize.height >= msh) ? minimumSize.height : msh;
            setMinSize(w, h);
        } else {
            setMinSize(0, 0);
        }
    }
    public void updateIconImages() {
        java.util.List<Image> imageList = ((Window)target).getIconImages();
        if (imageList == null || imageList.size() == 0) {
            setIconImagesData(null, 0, 0, null, 0, 0);
        } else {
            int w = getSysIconWidth();
            int h = getSysIconHeight();
            int smw = getSysSmIconWidth();
            int smh = getSysSmIconHeight();
            DataBufferInt iconData = SunToolkit.getScaledIconData(imageList,
                                                                  w, h);
            DataBufferInt iconSmData = SunToolkit.getScaledIconData(imageList,
                                                                    smw, smh);
            if (iconData != null && iconSmData != null) {
                setIconImagesData(iconData.getData(), w, h,
                                  iconSmData.getData(), smw, smh);
            } else {
                setIconImagesData(null, 0, 0, null, 0, 0);
            }
        }
    }
    native void setMinSize(int width, int height);
    public boolean isModalBlocked() {
        return modalBlocker != null;
    }
    public void setModalBlocked(Dialog dialog, boolean blocked) {
        synchronized (((Component)getTarget()).getTreeLock()) 
        {
            WWindowPeer blockerPeer = (WWindowPeer)dialog.getPeer();
            if (blocked)
            {
                modalBlocker = blockerPeer;
                if (blockerPeer instanceof WFileDialogPeer) {
                    ((WFileDialogPeer)blockerPeer).blockWindow(this);
                } else if (blockerPeer instanceof WPrintDialogPeer) {
                    ((WPrintDialogPeer)blockerPeer).blockWindow(this);
                } else {
                    modalDisable(dialog, blockerPeer.getHWnd());
                }
            } else {
                modalBlocker = null;
                if (blockerPeer instanceof WFileDialogPeer) {
                    ((WFileDialogPeer)blockerPeer).unblockWindow(this);
                } else if (blockerPeer instanceof WPrintDialogPeer) {
                    ((WPrintDialogPeer)blockerPeer).unblockWindow(this);
                } else {
                    modalEnable(dialog);
                }
            }
        }
    }
    native void modalDisable(Dialog blocker, long blockerHWnd);
    native void modalEnable(Dialog blocker);
    public static long[] getActiveWindowHandles() {
        AppContext appContext = AppContext.getAppContext();
        synchronized (appContext) {
            List<WWindowPeer> l = (List<WWindowPeer>)appContext.get(ACTIVE_WINDOWS_KEY);
            if (l == null) {
                return null;
            }
            long[] result = new long[l.size()];
            for (int j = 0; j < l.size(); j++) {
                result[j] = l.get(j).getHWnd();
            }
            return result;
        }
    }
    void draggedToNewScreen() {
        SunToolkit.executeOnEventHandlerThread((Component)target,new Runnable()
        {
            public void run() {
                displayChanged();
            }
        });
    }
    public void updateGC() {
        int scrn = getScreenImOn();
        if (screenLog.isLoggable(PlatformLogger.FINER)) {
            log.finer("Screen number: " + scrn);
        }
        Win32GraphicsDevice oldDev = (Win32GraphicsDevice)winGraphicsConfig
                                     .getDevice();
        Win32GraphicsDevice newDev;
        GraphicsDevice devs[] = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getScreenDevices();
        if (scrn >= devs.length) {
            newDev = (Win32GraphicsDevice)GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();
        } else {
            newDev = (Win32GraphicsDevice)devs[scrn];
        }
        winGraphicsConfig = (Win32GraphicsConfig)newDev
                            .getDefaultConfiguration();
        if (screenLog.isLoggable(PlatformLogger.FINE)) {
            if (winGraphicsConfig == null) {
                screenLog.fine("Assertion (winGraphicsConfig != null) failed");
            }
        }
        if (oldDev != newDev) {
            oldDev.removeDisplayChangedListener(this);
            newDev.addDisplayChangedListener(this);
        }
        AWTAccessor.getComponentAccessor().
            setGraphicsConfiguration((Component)target, winGraphicsConfig);
    }
    public void displayChanged() {
        updateGC();
    }
    public void paletteChanged() {
    }
    private native int getScreenImOn();
    public final native void setFullScreenExclusiveModeState(boolean state);
     public void grab() {
         nativeGrab();
     }
     public void ungrab() {
         nativeUngrab();
     }
     private native void nativeGrab();
     private native void nativeUngrab();
     private final boolean hasWarningWindow() {
         return ((Window)target).getWarningString() != null;
     }
     boolean isTargetUndecorated() {
         return true;
     }
     private volatile int sysX = 0;
     private volatile int sysY = 0;
     private volatile int sysW = 0;
     private volatile int sysH = 0;
     public native void repositionSecurityWarning();
     @Override
     public void setBounds(int x, int y, int width, int height, int op) {
         sysX = x;
         sysY = y;
         sysW = width;
         sysH = height;
         super.setBounds(x, y, width, height, op);
     }
    @Override
    public void print(Graphics g) {
        Shape shape = AWTAccessor.getWindowAccessor().getShape((Window)target);
        if (shape != null) {
            g.setClip(shape);
        }
        super.print(g);
    }
    private void replaceSurfaceDataRecursively(Component c) {
        if (c instanceof Container) {
            for (Component child : ((Container)c).getComponents()) {
                replaceSurfaceDataRecursively(child);
            }
        }
        ComponentPeer cp = c.getPeer();
        if (cp instanceof WComponentPeer) {
            ((WComponentPeer)cp).replaceSurfaceDataLater();
        }
    }
    public final Graphics getTranslucentGraphics() {
        synchronized (getStateLock()) {
            return isOpaque ? null : painter.getBackBuffer(false).getGraphics();
        }
    }
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        synchronized (getStateLock()) {
            if (!isOpaque && ((Window)target).isVisible()) {
                updateWindow(true);
            }
        }
    }
    private native void setOpacity(int iOpacity);
    private float opacity = 1.0f;
    public void setOpacity(float opacity) {
        if (!((SunToolkit)((Window)target).getToolkit()).
            isWindowOpacitySupported())
        {
            return;
        }
        if (opacity < 0.0f || opacity > 1.0f) {
            throw new IllegalArgumentException(
                "The value of opacity should be in the range [0.0f .. 1.0f].");
        }
        if (((this.opacity == 1.0f && opacity <  1.0f) ||
             (this.opacity <  1.0f && opacity == 1.0f)) &&
            !Win32GraphicsEnvironment.isVistaOS())
        {
            replaceSurfaceDataRecursively((Component)getTarget());
        }
        this.opacity = opacity;
        final int maxOpacity = 0xff;
        int iOpacity = (int)(opacity * maxOpacity);
        if (iOpacity < 0) {
            iOpacity = 0;
        }
        if (iOpacity > maxOpacity) {
            iOpacity = maxOpacity;
        }
        setOpacity(iOpacity);
        synchronized (getStateLock()) {
            if (!isOpaque && ((Window)target).isVisible()) {
                updateWindow(true);
            }
        }
    }
    private native void setOpaqueImpl(boolean isOpaque);
    public void setOpaque(boolean isOpaque) {
        synchronized (getStateLock()) {
            if (this.isOpaque == isOpaque) {
                return;
            }
        }
        Window target = (Window)getTarget();
        if (!isOpaque) {
            SunToolkit sunToolkit = (SunToolkit)target.getToolkit();
            if (!sunToolkit.isWindowTranslucencySupported() ||
                !sunToolkit.isTranslucencyCapable(target.getGraphicsConfiguration()))
            {
                return;
            }
        }
        boolean isVistaOS = Win32GraphicsEnvironment.isVistaOS();
        if (this.isOpaque != isOpaque && !isVistaOS) {
            replaceSurfaceDataRecursively(target);
        }
        synchronized (getStateLock()) {
            this.isOpaque = isOpaque;
            setOpaqueImpl(isOpaque);
            if (isOpaque) {
                TranslucentWindowPainter currentPainter = painter;
                if (currentPainter != null) {
                    currentPainter.flush();
                    painter = null;
                }
            } else {
                painter = TranslucentWindowPainter.createInstance(this);
            }
        }
        if (isVistaOS) {
            Shape shape = ((Window)target).getShape();
            if (shape != null) {
                ((Window)target).setShape(shape);
            }
        }
        if (((Window)target).isVisible()) {
            updateWindow(true);
        }
    }
    public native void updateWindowImpl(int[] data, int width, int height);
    public void updateWindow() {
        updateWindow(false);
    }
    private void updateWindow(boolean repaint) {
        Window w = (Window)target;
        synchronized (getStateLock()) {
            if (isOpaque || !w.isVisible() ||
                (w.getWidth() <= 0) || (w.getHeight() <= 0))
            {
                return;
            }
            TranslucentWindowPainter currentPainter = painter;
            if (currentPainter != null) {
                currentPainter.updateWindow(repaint);
            } else if (log.isLoggable(PlatformLogger.FINER)) {
                log.finer("Translucent window painter is null in updateWindow");
            }
        }
    }
    private static void initActiveWindowsTracking(Window w) {
        AppContext appContext = AppContext.getAppContext();
        synchronized (appContext) {
            List<WWindowPeer> l = (List<WWindowPeer>)appContext.get(ACTIVE_WINDOWS_KEY);
            if (l == null) {
                l = new LinkedList<WWindowPeer>();
                appContext.put(ACTIVE_WINDOWS_KEY, l);
                appContext.addPropertyChangeListener(AppContext.GUI_DISPOSED, guiDisposedListener);
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                kfm.addPropertyChangeListener("activeWindow", activeWindowListener);
            }
        }
    }
    private static class GuiDisposedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            boolean isDisposed = (Boolean)e.getNewValue();
            if (isDisposed != true) {
                if (log.isLoggable(PlatformLogger.FINE)) {
                    log.fine(" Assertion (newValue != true) failed for AppContext.GUI_DISPOSED ");
                }
            }
            AppContext appContext = AppContext.getAppContext();
            synchronized (appContext) {
                appContext.remove(ACTIVE_WINDOWS_KEY);
                appContext.removePropertyChangeListener(AppContext.GUI_DISPOSED, this);
                KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
                kfm.removePropertyChangeListener("activeWindow", activeWindowListener);
            }
        }
    }
    private static class ActiveWindowListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            Window w = (Window)e.getNewValue();
            if (w == null) {
                return;
            }
            AppContext appContext = SunToolkit.targetToAppContext(w);
            synchronized (appContext) {
                WWindowPeer wp = (WWindowPeer)w.getPeer();
                List<WWindowPeer> l = (List<WWindowPeer>)appContext.get(ACTIVE_WINDOWS_KEY);
                if (l != null) {
                    l.remove(wp);
                    l.add(wp);
                }
            }
        }
    }
}
