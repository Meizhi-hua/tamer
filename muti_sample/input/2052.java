public class XComponentPeer extends XWindow implements ComponentPeer, DropTargetPeer,
    BackBufferCapsProvider
{
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XComponentPeer");
    private static final PlatformLogger buffersLog = PlatformLogger.getLogger("sun.awt.X11.XComponentPeer.multibuffer");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.X11.focus.XComponentPeer");
    private static final PlatformLogger fontLog = PlatformLogger.getLogger("sun.awt.X11.font.XComponentPeer");
    private static final PlatformLogger enableLog = PlatformLogger.getLogger("sun.awt.X11.enable.XComponentPeer");
    private static final PlatformLogger shapeLog = PlatformLogger.getLogger("sun.awt.X11.shape.XComponentPeer");
    boolean paintPending = false;
    boolean isLayouting = false;
    boolean enabled;
    protected int boundsOperation;
    Color foreground;
    Color background;
    Color darkShadow;
    Color lightShadow;
    Color selectColor;
    Font font;
    private long backBuffer = 0;
    private VolatileImage xBackBuffer = null;
    static Color[] systemColors;
    XComponentPeer() {
    }
    XComponentPeer (XCreateWindowParams params) {
        super(params);
    }
    XComponentPeer(Component target, long parentWindow, Rectangle bounds) {
        super(target, parentWindow, bounds);
    }
    XComponentPeer(Component target) {
        super(target);
    }
    void preInit(XCreateWindowParams params) {
        super.preInit(params);
        boundsOperation = DEFAULT_OPERATION;
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
        Color c;
        Font  f;
        Cursor cursor;
        pSetCursor(target.getCursor());
        foreground = target.getForeground();
        background = target.getBackground();
        font = target.getFont();
        if (isInitialReshape()) {
            Rectangle r = target.getBounds();
            reshape(r.x, r.y, r.width, r.height);
        }
        enabled = target.isEnabled();
        Component comp = target;
        while( !(comp == null || comp instanceof Window) ) {
            comp = comp.getParent();
            if( comp != null && !comp.isEnabled() && !comp.isLightweight() ){
                setEnabled(false);
                break;
            }
        }
        enableLog.fine("Initial enable state: {0}", Boolean.valueOf(enabled));
        if (target.isVisible()) {
            setVisible(true);
        }
    }
    protected boolean isInitialReshape() {
        return true;
    }
    public void reparent(ContainerPeer newNativeParent) {
        XComponentPeer newPeer = (XComponentPeer)newNativeParent;
        XToolkit.awtLock();
        try {
            XlibWrapper.XReparentWindow(XToolkit.getDisplay(), getWindow(), newPeer.getContentWindow(), x, y);
            parentWindow = newPeer;
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public boolean isReparentSupported() {
        return System.getProperty("sun.awt.X11.XComponentPeer.reparentNotSupported", "false").equals("false");
    }
    public boolean isObscured() {
        Container container  = (target instanceof Container) ?
            (Container)target : target.getParent();
        if (container == null) {
            return true;
        }
        Container parent;
        while ((parent = container.getParent()) != null) {
            container = parent;
        }
        if (container instanceof Window) {
            XWindowPeer wpeer = (XWindowPeer)(container.getPeer());
            if (wpeer != null) {
                return (wpeer.winAttr.visibilityState !=
                        wpeer.winAttr.AWT_UNOBSCURED);
            }
        }
        return true;
    }
    public boolean canDetermineObscurity() {
        return true;
    }
    boolean bHasFocus = false;
    final public boolean hasFocus() {
        return bHasFocus;
    }
    public void focusGained(FocusEvent e) {
        focusLog.fine("{0}", e);
        bHasFocus = true;
    }
    public void focusLost(FocusEvent e) {
        focusLog.fine("{0}", e);
        bHasFocus = false;
    }
    public boolean isFocusable() {
        return false;
    }
    private static Class seClass;
    private static Constructor seCtor;
    final static AWTEvent wrapInSequenced(AWTEvent event) {
        try {
            if (seClass == null) {
                seClass = Class.forName("java.awt.SequencedEvent");
            }
            if (seCtor == null) {
                seCtor = (Constructor) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            Constructor ctor = seClass.getConstructor(new Class[] { AWTEvent.class });
                            ctor.setAccessible(true);
                            return ctor;
                        }
                    });
            }
            return (AWTEvent) seCtor.newInstance(new Object[] { event });
        }
        catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError("java.awt.SequencedEvent.");
        }
        catch (PrivilegedActionException ex) {
            throw new NoClassDefFoundError("java.awt.SequencedEvent.");
        }
        catch (InstantiationException e) {
            assert false;
        }
        catch (IllegalAccessException e) {
            assert false;
        }
        catch (InvocationTargetException e) {
            assert false;
        }
        return null;
    }
    final public boolean requestFocus(Component lightweightChild, boolean temporary,
                                      boolean focusedWindowChangeAllowed, long time,
                                      CausedFocusEvent.Cause cause)
    {
        if (XKeyboardFocusManagerPeer.
            processSynchronousLightweightTransfer(target, lightweightChild, temporary,
                                                  focusedWindowChangeAllowed, time))
        {
            return true;
        }
        int result = XKeyboardFocusManagerPeer.
            shouldNativelyFocusHeavyweight(target, lightweightChild,
                                           temporary, focusedWindowChangeAllowed,
                                           time, cause);
        switch (result) {
          case XKeyboardFocusManagerPeer.SNFH_FAILURE:
              return false;
          case XKeyboardFocusManagerPeer.SNFH_SUCCESS_PROCEED:
              if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer("Proceeding with request to " +
                  lightweightChild + " in " + target);
              Window parentWindow = SunToolkit.getContainingWindow(target);
              if (parentWindow == null) {
                  return rejectFocusRequestHelper("WARNING: Parent window is null");
              }
              XWindowPeer wpeer = (XWindowPeer)parentWindow.getPeer();
              if (wpeer == null) {
                  return rejectFocusRequestHelper("WARNING: Parent window's peer is null");
              }
              boolean res = wpeer.requestWindowFocus(null);
              if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer("Requested window focus: " + res);
              if (!(res && parentWindow.isFocused())) {
                  return rejectFocusRequestHelper("Waiting for asynchronous processing of the request");
              }
              return XKeyboardFocusManagerPeer.deliverFocus(lightweightChild,
                                                            (Component)target,
                                                            temporary,
                                                            focusedWindowChangeAllowed,
                                                            time, cause);
          case XKeyboardFocusManagerPeer.SNFH_SUCCESS_HANDLED:
              return true;
        }
        return false;
    }
    private boolean rejectFocusRequestHelper(String logMsg) {
        if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer(logMsg);
        XKeyboardFocusManagerPeer.removeLastFocusRequest(target);
        return false;
    }
    void handleJavaFocusEvent(AWTEvent e) {
        if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer(e.toString());
        if (e.getID() == FocusEvent.FOCUS_GAINED) {
            focusGained((FocusEvent)e);
        } else {
            focusLost((FocusEvent)e);
        }
    }
    void handleJavaWindowFocusEvent(AWTEvent e) {
    }
    public void setVisible(boolean b) {
        xSetVisible(b);
    }
    public void hide() {
        setVisible(false);
    }
    public void setEnabled(boolean value) {
        enableLog.fine("{0}ing {1}", (value?"Enabl":"Disabl"), this);
        boolean repaintNeeded = (enabled != value);
        enabled = value;
        if (target instanceof Container) {
            Component list[] = ((Container)target).getComponents();
            for (int i = 0; i < list.length; ++i) {
                boolean childEnabled = list[i].isEnabled();
                ComponentPeer p = list[i].getPeer();
                if ( p != null ) {
                    p.setEnabled(value && childEnabled);
                }
            }
        }
        if (repaintNeeded) {
            repaint();
        }
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void enable() {
        setEnabled(true);
    }
    public void disable() {
        setEnabled(false);
    }
    public void paint(Graphics g) {
    }
    public void repaint(long tm, int x, int y, int width, int height) {
        repaint();
    }
    public Graphics getGraphics() {
        return getGraphics(surfaceData, getPeerForeground(), getPeerBackground(), getPeerFont());
    }
    public void print(Graphics g) {
        g.setColor(target.getBackground());
        g.fillRect(0, 0, target.getWidth(), target.getHeight());
        g.setColor(target.getForeground());
        paint(g);
        target.print(g);
    }
    public void setBounds(int x, int y, int width, int height, int op) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        xSetBounds(x,y,width,height);
        validateSurface();
        layout();
    }
    public void reshape(int x, int y, int width, int height) {
        setBounds(x, y, width, height, SET_BOUNDS);
    }
    public void coalescePaintEvent(PaintEvent e) {
        Rectangle r = e.getUpdateRect();
        if (!(e instanceof IgnorePaintEvent)) {
            paintArea.add(r, e.getID());
        }
        if (true) {
            switch(e.getID()) {
              case PaintEvent.UPDATE:
                  log.finer("XCP coalescePaintEvent : UPDATE : add : x = " +
                            r.x + ", y = " + r.y + ", width = " + r.width + ",height = " + r.height);
                  return;
              case PaintEvent.PAINT:
                  log.finer("XCP coalescePaintEvent : PAINT : add : x = " +
                            r.x + ", y = " + r.y + ", width = " + r.width + ",height = " + r.height);
                  return;
            }
        }
    }
    XWindowPeer getParentTopLevel() {
        AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
        Container parent = (target instanceof Container) ? ((Container)target) : (compAccessor.getParent(target));
        while (parent != null && !(parent instanceof Window)) {
            parent = compAccessor.getParent(parent);
        }
        if (parent != null) {
            return (XWindowPeer)compAccessor.getPeer(parent);
        } else {
            return null;
        }
    }
    void handleJavaMouseEvent(MouseEvent e) {
        switch (e.getID()) {
          case MouseEvent.MOUSE_PRESSED:
              if (target == e.getSource() &&
                  !target.isFocusOwner() &&
                  XKeyboardFocusManagerPeer.shouldFocusOnClick(target))
              {
                  XWindowPeer parentXWindow = getParentTopLevel();
                  Window parentWindow = ((Window)parentXWindow.getTarget());
                  XKeyboardFocusManagerPeer.requestFocusFor(target, CausedFocusEvent.Cause.MOUSE_EVENT);
              }
              break;
        }
    }
    void handleJavaKeyEvent(KeyEvent e) {
    }
    void handleJavaMouseWheelEvent(MouseWheelEvent e) {
    }
    void handleJavaInputMethodEvent(InputMethodEvent e) {
    }
    void handleF10JavaKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_F10) {
            XWindowPeer winPeer = this.getToplevelXWindow();
            if (winPeer instanceof XFramePeer) {
                XMenuBarPeer mPeer = ((XFramePeer)winPeer).getMenubarPeer();
                if (mPeer != null) {
                    mPeer.handleF10KeyPress(e);
                }
            }
        }
    }
    public void handleEvent(java.awt.AWTEvent e) {
        if ((e instanceof InputEvent) && !((InputEvent)e).isConsumed() && target.isEnabled())  {
            if (e instanceof MouseEvent) {
                if (e instanceof MouseWheelEvent) {
                    handleJavaMouseWheelEvent((MouseWheelEvent) e);
                }
                else
                    handleJavaMouseEvent((MouseEvent) e);
            }
            else if (e instanceof KeyEvent) {
                handleF10JavaKeyEvent((KeyEvent)e);
                handleJavaKeyEvent((KeyEvent)e);
            }
        }
        else if (e instanceof KeyEvent && !((InputEvent)e).isConsumed()) {
            handleF10JavaKeyEvent((KeyEvent)e);
        }
        else if (e instanceof InputMethodEvent) {
            handleJavaInputMethodEvent((InputMethodEvent) e);
        }
        int id = e.getID();
        switch(id) {
          case PaintEvent.PAINT:
              paintPending = false;
          case PaintEvent.UPDATE:
              if (!isLayouting && !paintPending) {
                  paintArea.paint(target,false);
              }
              return;
          case FocusEvent.FOCUS_LOST:
          case FocusEvent.FOCUS_GAINED:
              handleJavaFocusEvent(e);
              break;
          case WindowEvent.WINDOW_LOST_FOCUS:
          case WindowEvent.WINDOW_GAINED_FOCUS:
              handleJavaWindowFocusEvent(e);
              break;
          default:
              break;
        }
    }
    public void handleButtonPressRelease(XEvent xev) {
        if (xev.get_type() == XConstants.ButtonPress) {
            final XWindowPeer parentXWindow = getParentTopLevel();
            Window parentWindow = (Window)parentXWindow.getTarget();
            if (parentXWindow.isFocusableWindow() && parentXWindow.isSimpleWindow() &&
                XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow() != parentWindow)
            {
                postEvent(new InvocationEvent(parentWindow, new  Runnable() {
                        public void run() {
                            parentXWindow.requestXFocus();
                        }
                    }));
            }
        }
        super.handleButtonPressRelease(xev);
    }
    public Dimension getMinimumSize() {
        return target.getSize();
    }
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    public void layout() {}
    public java.awt.Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }
    void updateMotifColors(Color bg) {
        int red = bg.getRed();
        int green = bg.getGreen();
        int blue = bg.getBlue();
        darkShadow = new Color(MotifColorUtilities.calculateBottomShadowFromBackground(red,green,blue));
        lightShadow = new Color(MotifColorUtilities.calculateTopShadowFromBackground(red,green,blue));
        selectColor= new Color(MotifColorUtilities.calculateSelectFromBackground(red,green,blue));
    }
    public void drawMotif3DRect(Graphics g,
                                          int x, int y, int width, int height,
                                          boolean topShadow) {
        g.setColor(topShadow ? darkShadow : lightShadow);
        g.drawLine(x, y, x+width, y);       
        g.drawLine(x, y+height, x, y);      
        g.setColor(topShadow ? lightShadow : darkShadow );
        g.drawLine(x+1, y+height, x+width, y+height); 
        g.drawLine(x+width, y+height, x+width, y+1);  
    }
    public void setBackground(Color c) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Set background to " + c);
        synchronized (getStateLock()) {
            background = c;
        }
        super.setBackground(c);
        repaint();
    }
    public void setForeground(Color c) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Set foreground to " + c);
        synchronized (getStateLock()) {
            foreground = c;
        }
        repaint();
    }
    public FontMetrics getFontMetrics(Font font) {
        if (fontLog.isLoggable(PlatformLogger.FINE)) fontLog.fine("Getting font metrics for " + font);
        return sun.font.FontDesignMetrics.getMetrics(font);
    }
    public void setFont(Font f) {
        synchronized (getStateLock()) {
            if (f == null) {
                f = XWindow.getDefaultFont();
            }
            font = f;
        }
    }
    public Font getFont() {
        return font;
    }
    public void updateCursorImmediately() {
        XGlobalCursorManager.getCursorManager().updateCursorImmediately();
    }
    public final void pSetCursor(Cursor cursor) {
        this.pSetCursor(cursor, true);
    }
    public void pSetCursor(Cursor cursor, boolean ignoreSubComponents) {
        XToolkit.awtLock();
        try {
            long xcursor = XGlobalCursorManager.getCursor(cursor);
            XSetWindowAttributes xwa = new XSetWindowAttributes();
            xwa.set_cursor(xcursor);
            long valuemask = XConstants.CWCursor;
            XlibWrapper.XChangeWindowAttributes(XToolkit.getDisplay(),getWindow(),valuemask,xwa.pData);
            XlibWrapper.XFlush(XToolkit.getDisplay());
            xwa.dispose();
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public Image createImage(ImageProducer producer) {
        return new ToolkitImage(producer);
    }
    public Image createImage(int width, int height) {
        return graphicsConfig.createAcceleratedImage(target, width, height);
    }
    public VolatileImage createVolatileImage(int width, int height) {
        return new SunVolatileImage(target, width, height);
    }
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        return getToolkit().prepareImage(img, w, h, o);
    }
    public int checkImage(Image img, int w, int h, ImageObserver o) {
        return getToolkit().checkImage(img, w, h, o);
    }
    public Dimension preferredSize() {
        return getPreferredSize();
    }
    public Dimension minimumSize() {
        return getMinimumSize();
    }
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }
    public void beginValidate() {
    }
    public void endValidate() {
    }
    public Insets insets() {
        return getInsets();
    }
    public boolean isPaintPending() {
        return paintPending && isLayouting;
    }
    public boolean handlesWheelScrolling() {
        return false;
    }
    public void beginLayout() {
        isLayouting = true;
    }
    public void endLayout() {
        if (!paintPending && !paintArea.isEmpty()
            && !AWTAccessor.getComponentAccessor().getIgnoreRepaint(target))
        {
            postEvent(new PaintEvent(target, PaintEvent.PAINT,
                                     new Rectangle()));
        }
        isLayouting = false;
    }
    public Color getWinBackground() {
        return getPeerBackground();
    }
    static int[] getRGBvals(Color c) {
        int rgbvals[] = new int[3];
        rgbvals[0] = c.getRed();
        rgbvals[1] = c.getGreen();
        rgbvals[2] = c.getBlue();
        return rgbvals;
    }
    static final int BACKGROUND_COLOR = 0;
    static final int HIGHLIGHT_COLOR = 1;
    static final int SHADOW_COLOR = 2;
    static final int FOREGROUND_COLOR = 3;
    public Color[] getGUIcolors() {
        Color c[] = new Color[4];
        float backb, highb, shadowb, hue, saturation;
        c[BACKGROUND_COLOR] = getWinBackground();
        if (c[BACKGROUND_COLOR] == null) {
            c[BACKGROUND_COLOR] = super.getWinBackground();
        }
        if (c[BACKGROUND_COLOR] == null) {
            c[BACKGROUND_COLOR] = Color.lightGray;
        }
        int[] rgb = getRGBvals(c[BACKGROUND_COLOR]);
        float[] hsb = Color.RGBtoHSB(rgb[0],rgb[1],rgb[2],null);
        hue = hsb[0];
        saturation = hsb[1];
        backb = hsb[2];
        highb = backb + 0.2f;
        shadowb = backb - 0.4f;
        if ((highb > 1.0) ) {
            if  ((1.0 - backb) < 0.05) {
                highb = shadowb + 0.25f;
            } else {
                highb = 1.0f;
            }
        } else {
            if (shadowb < 0.0) {
                if ((backb - 0.0) < 0.25) {
                    highb = backb + 0.75f;
                    shadowb = highb - 0.2f;
                } else {
                    shadowb = 0.0f;
                }
            }
        }
        c[HIGHLIGHT_COLOR] = Color.getHSBColor(hue,saturation,highb);
        c[SHADOW_COLOR] = Color.getHSBColor(hue,saturation,shadowb);
        c[FOREGROUND_COLOR] = getPeerForeground();
        if (c[FOREGROUND_COLOR] == null) {
            c[FOREGROUND_COLOR] = Color.black;
        }
        if (! isEnabled()) {
            c[BACKGROUND_COLOR] = c[BACKGROUND_COLOR].darker();
            Color tc = c[BACKGROUND_COLOR];
            int bg = tc.getRed() * 30 + tc.getGreen() * 59 + tc.getBlue() * 11;
            tc = c[FOREGROUND_COLOR];
            int fg = tc.getRed() * 30 + tc.getGreen() * 59 + tc.getBlue() * 11;
            float ave = (float) ((fg + bg) / 51000.0);
            Color newForeground = new Color((int) (tc.getRed() * ave),
                                            (int) (tc.getGreen() * ave),
                                            (int) (tc.getBlue() * ave));
            if (newForeground.equals(c[FOREGROUND_COLOR])) {
                newForeground = new Color(ave, ave, ave);
            }
            c[FOREGROUND_COLOR] = newForeground;
        }
        return c;
    }
    static Color[] getSystemColors() {
        if (systemColors == null) {
            systemColors = new Color[4];
            systemColors[BACKGROUND_COLOR] = SystemColor.window;
            systemColors[HIGHLIGHT_COLOR] = SystemColor.controlLtHighlight;
            systemColors[SHADOW_COLOR] = SystemColor.controlShadow;
            systemColors[FOREGROUND_COLOR] = SystemColor.windowText;
        }
        return systemColors;
    }
    public void draw3DOval(Graphics g, Color colors[],
                           int x, int y, int w, int h, boolean raised)
        {
        Color c = g.getColor();
        g.setColor(raised ? colors[HIGHLIGHT_COLOR] : colors[SHADOW_COLOR]);
        g.drawArc(x, y, w, h, 45, 180);
        g.setColor(raised ? colors[SHADOW_COLOR] : colors[HIGHLIGHT_COLOR]);
        g.drawArc(x, y, w, h, 225, 180);
        g.setColor(c);
    }
    public void draw3DRect(Graphics g, Color colors[],
                           int x, int y, int width, int height, boolean raised)
        {
            Color c = g.getColor();
            g.setColor(raised ? colors[HIGHLIGHT_COLOR] : colors[SHADOW_COLOR]);
            g.drawLine(x, y, x, y + height);
            g.drawLine(x + 1, y, x + width - 1, y);
            g.setColor(raised ? colors[SHADOW_COLOR] : colors[HIGHLIGHT_COLOR]);
            g.drawLine(x + 1, y + height, x + width, y + height);
            g.drawLine(x + width, y, x + width, y + height - 1);
            g.setColor(c);
        }
    void draw3DOval(Graphics g, Color bg,
                    int x, int y, int w, int h, boolean raised)
        {
            Color c = g.getColor();
            Color shadow = bg.darker();
            Color highlight = bg.brighter();
            g.setColor(raised ? highlight : shadow);
            g.drawArc(x, y, w, h, 45, 180);
            g.setColor(raised ? shadow : highlight);
            g.drawArc(x, y, w, h, 225, 180);
            g.setColor(c);
        }
    void draw3DRect(Graphics g, Color bg,
                    int x, int y, int width, int height,
                    boolean raised) {
        Color c = g.getColor();
        Color shadow = bg.darker();
        Color highlight = bg.brighter();
        g.setColor(raised ? highlight : shadow);
        g.drawLine(x, y, x, y + height);
        g.drawLine(x + 1, y, x + width - 1, y);
        g.setColor(raised ? shadow : highlight);
        g.drawLine(x + 1, y + height, x + width, y + height);
        g.drawLine(x + width, y, x + width, y + height - 1);
        g.setColor(c);
    }
    void drawScrollbar(Graphics g, Color bg, int thickness, int length,
               int min, int max, int val, int vis, boolean horizontal) {
        Color c = g.getColor();
        double f = (double)(length - 2*(thickness-1)) / Math.max(1, ((max - min) + vis));
        int v1 = thickness + (int)(f * (val - min));
        int v2 = (int)(f * vis);
        int w2 = thickness-4;
        int tpts_x[] = new int[3];
        int tpts_y[] = new int[3];
        if (length < 3*w2 ) {
            v1 = v2 = 0;
            if (length < 2*w2 + 2) {
                w2 = (length-2)/2;
            }
        } else  if (v2 < 7) {
            v1 = Math.max(0, v1 - ((7 - v2)>>1));
            v2 = 7;
        }
        int ctr   = thickness/2;
        int sbmin = ctr - w2/2;
        int sbmax = ctr + w2/2;
        {
            Color d = new Color((int) (bg.getRed()   * 0.85),
                                (int) (bg.getGreen() * 0.85),
                                (int) (bg.getBlue()  * 0.85));
            g.setColor(d);
            if (horizontal) {
                g.fillRect(0, 0, length, thickness);
            } else {
                g.fillRect(0, 0, thickness, length);
            }
        }
        g.setColor(bg);
        if (v1 > 0) {
            if (horizontal) {
                g.fillRect(v1, 3, v2, thickness-3);
            } else {
                g.fillRect(3, v1, thickness-3, v2);
            }
        }
        tpts_x[0] = ctr;    tpts_y[0] = 2;
        tpts_x[1] = sbmin;  tpts_y[1] = w2;
        tpts_x[2] = sbmax;  tpts_y[2] = w2;
        if (horizontal) {
            g.fillPolygon(tpts_y, tpts_x, 3);
        } else {
            g.fillPolygon(tpts_x, tpts_y, 3);
        }
        tpts_y[0] = length-2;
        tpts_y[1] = length-w2;
        tpts_y[2] = length-w2;
        if (horizontal) {
            g.fillPolygon(tpts_y, tpts_x, 3);
        } else {
            g.fillPolygon(tpts_x, tpts_y, 3);
        }
        Color highlight = bg.brighter();
        g.setColor(highlight);
        if (horizontal) {
            g.drawLine(1, thickness, length - 1, thickness);
            g.drawLine(length - 1, 1, length - 1, thickness);
            g.drawLine(1, ctr, w2, sbmin);
            g.drawLine(length - w2, sbmin, length - w2, sbmax);
            g.drawLine(length - w2, sbmin, length - 2, ctr);
        } else {
            g.drawLine(thickness, 1, thickness, length - 1);
            g.drawLine(1, length - 1, thickness, length - 1);
            g.drawLine(ctr, 1, sbmin, w2);
            g.drawLine(sbmin, length - w2, sbmax, length - w2);
            g.drawLine(sbmin, length - w2, ctr, length - 2);
        }
        if (v1 > 0) {
            if (horizontal) {
                g.drawLine(v1, 2, v1 + v2, 2);
                g.drawLine(v1, 2, v1, thickness-3);
            } else {
                g.drawLine(2, v1, 2, v1 + v2);
                g.drawLine(2, v1, thickness-3, v1);
            }
        }
        Color shadow = bg.darker();
        g.setColor(shadow);
        if (horizontal) {
            g.drawLine(0, 0, 0, thickness);
            g.drawLine(0, 0, length - 1, 0);
            g.drawLine(w2, sbmin, w2, sbmax);
            g.drawLine(w2, sbmax, 1, ctr);
            g.drawLine(length-2, ctr, length-w2, sbmax);
        } else {
            g.drawLine(0, 0, thickness, 0);
            g.drawLine(0, 0, 0, length - 1);
            g.drawLine(sbmin, w2, sbmax, w2);
            g.drawLine(sbmax, w2, ctr, 1);
            g.drawLine(ctr, length-2, sbmax, length-w2);
        }
        if (v1 > 0) {
            if (horizontal) {
                g.drawLine(v1 + v2, 2, v1 + v2, thickness-2);
                g.drawLine(v1, thickness-2, v1 + v2, thickness-2);
            } else {
                g.drawLine(2, v1 + v2, thickness-2, v1 + v2);
                g.drawLine(thickness-2, v1, thickness-2, v1 + v2);
            }
        }
        g.setColor(c);
    }
    private BufferCapabilities backBufferCaps;
    public void createBuffers(int numBuffers, BufferCapabilities caps)
      throws AWTException
    {
        if (buffersLog.isLoggable(PlatformLogger.FINE)) {
            buffersLog.fine("createBuffers(" + numBuffers + ", " + caps + ")");
        }
        backBufferCaps = caps;
        backBuffer = graphicsConfig.createBackBuffer(this, numBuffers, caps);
        xBackBuffer = graphicsConfig.createBackBufferImage(target,
                                                           backBuffer);
    }
    @Override
    public BufferCapabilities getBackBufferCaps() {
        return backBufferCaps;
    }
    public void flip(int x1, int y1, int x2, int y2,
                     BufferCapabilities.FlipContents flipAction)
    {
        if (buffersLog.isLoggable(PlatformLogger.FINE)) {
            buffersLog.fine("flip(" + flipAction + ")");
        }
        if (backBuffer == 0) {
            throw new IllegalStateException("Buffers have not been created");
        }
        graphicsConfig.flip(this, target, xBackBuffer,
                            x1, y1, x2, y2, flipAction);
    }
    public Image getBackBuffer() {
        if (buffersLog.isLoggable(PlatformLogger.FINE)) {
            buffersLog.fine("getBackBuffer()");
        }
        if (backBuffer == 0) {
            throw new IllegalStateException("Buffers have not been created");
        }
        return xBackBuffer;
    }
    public void destroyBuffers() {
        if (buffersLog.isLoggable(PlatformLogger.FINE)) {
            buffersLog.fine("destroyBuffers()");
        }
        graphicsConfig.destroyBackBuffer(backBuffer);
        backBuffer = 0;
        xBackBuffer = null;
    }
    public void notifyTextComponentChange(boolean add){
        Container parent = AWTAccessor.getComponentAccessor().getParent(target);
        while(!(parent == null ||
                parent instanceof java.awt.Frame ||
                parent instanceof java.awt.Dialog)) {
            parent = AWTAccessor.getComponentAccessor().getParent(parent);
        }
    }
    protected boolean isEventDisabled(XEvent e) {
        enableLog.finest("Component is {1}, checking for disabled event {0}", e, (isEnabled()?"enabled":"disable"));
        if (!isEnabled()) {
            switch (e.get_type()) {
              case XConstants.ButtonPress:
              case XConstants.ButtonRelease:
              case XConstants.KeyPress:
              case XConstants.KeyRelease:
              case XConstants.EnterNotify:
              case XConstants.LeaveNotify:
              case XConstants.MotionNotify:
                  enableLog.finer("Event {0} is disable", e);
                  return true;
            }
        }
        switch(e.get_type()) {
          case XConstants.MapNotify:
          case XConstants.UnmapNotify:
              return true;
        }
        return super.isEventDisabled(e);
    }
    Color getPeerBackground() {
        return background;
    }
    Color getPeerForeground() {
        return foreground;
    }
    Font getPeerFont() {
        return font;
    }
    Dimension getPeerSize() {
        return new Dimension(width,height);
    }
    public void setBoundsOperation(int operation) {
        synchronized(getStateLock()) {
            if (boundsOperation == DEFAULT_OPERATION) {
                boundsOperation = operation;
            } else if (operation == RESET_OPERATION) {
                boundsOperation = DEFAULT_OPERATION;
            }
        }
    }
    static String operationToString(int operation) {
        switch (operation) {
          case SET_LOCATION:
              return "SET_LOCATION";
          case SET_SIZE:
              return "SET_SIZE";
          case SET_CLIENT_SIZE:
              return "SET_CLIENT_SIZE";
          default:
          case SET_BOUNDS:
              return "SET_BOUNDS";
        }
    }
    public void setZOrder(ComponentPeer above) {
        long aboveWindow = (above != null) ? ((XComponentPeer)above).getWindow() : 0;
        XToolkit.awtLock();
        try{
            XlibWrapper.SetZOrder(XToolkit.getDisplay(), getWindow(), aboveWindow);
        }finally{
            XToolkit.awtUnlock();
        }
    }
    private void addTree(Collection order, Set set, Container cont) {
        for (int i = 0; i < cont.getComponentCount(); i++) {
            Component comp = cont.getComponent(i);
            ComponentPeer peer = comp.getPeer();
            if (peer instanceof XComponentPeer) {
                Long window = Long.valueOf(((XComponentPeer)peer).getWindow());
                if (!set.contains(window)) {
                    set.add(window);
                    order.add(window);
                }
            } else if (comp instanceof Container) {
                addTree(order, set, (Container)comp);
            }
        }
    }
    public void addDropTarget(DropTarget dt) {
        Component comp = target;
        while(!(comp == null || comp instanceof Window)) {
            comp = comp.getParent();
        }
        if (comp instanceof Window) {
            XWindowPeer wpeer = (XWindowPeer)(comp.getPeer());
            if (wpeer != null) {
                wpeer.addDropTarget();
            }
        }
    }
    public void removeDropTarget(DropTarget dt) {
        Component comp = target;
        while(!(comp == null || comp instanceof Window)) {
            comp = comp.getParent();
        }
        if (comp instanceof Window) {
            XWindowPeer wpeer = (XWindowPeer)(comp.getPeer());
            if (wpeer != null) {
                wpeer.removeDropTarget();
            }
        }
    }
    public void applyShape(Region shape) {
        if (XlibUtil.isShapingSupported()) {
            if (shapeLog.isLoggable(PlatformLogger.FINER)) {
                shapeLog.finer(
                        "*** INFO: Setting shape: PEER: " + this
                        + "; WINDOW: " + getWindow()
                        + "; TARGET: " + target
                        + "; SHAPE: " + shape);
            }
            XToolkit.awtLock();
            try {
                if (shape != null) {
                    XlibWrapper.SetRectangularShape(
                            XToolkit.getDisplay(),
                            getWindow(),
                            shape.getLoX(), shape.getLoY(),
                            shape.getHiX(), shape.getHiY(),
                            (shape.isRectangular() ? null : shape)
                            );
                } else {
                    XlibWrapper.SetRectangularShape(
                            XToolkit.getDisplay(),
                            getWindow(),
                            0, 0,
                            0, 0,
                            null
                            );
                }
            } finally {
                XToolkit.awtUnlock();
            }
        } else {
            if (shapeLog.isLoggable(PlatformLogger.FINER)) {
                shapeLog.finer("*** WARNING: Shaping is NOT supported!");
            }
        }
    }
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        int oldVisual = -1, newVisual = -1;
        if (graphicsConfig != null) {
            oldVisual = graphicsConfig.getVisual();
        }
        if (gc != null && gc instanceof X11GraphicsConfig) {
            newVisual = ((X11GraphicsConfig)gc).getVisual();
        }
        if (oldVisual != -1 && oldVisual != newVisual) {
            return true;
        }
        initGraphicsConfiguration();
        doValidateSurface();
        return false;
    }
}
