class XScrollbarPeer extends XComponentPeer implements ScrollbarPeer, XScrollbarClient {
    private final static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XScrollbarPeer");
    private static final int DEFAULT_LENGTH = 50;
    private static final int DEFAULT_WIDTH_SOLARIS = 19;
    private static final int DEFAULT_WIDTH_LINUX;
    XScrollbar tsb;
    static {
        DEFAULT_WIDTH_LINUX = XToolkit.getUIDefaults().getInt("ScrollBar.defaultWidth");
    }
    public void preInit(XCreateWindowParams params) {
        super.preInit(params);
        Scrollbar target = (Scrollbar) this.target;
        if (target.getOrientation() == Scrollbar.VERTICAL) {
            tsb = new XVerticalScrollbar(this);
        } else {
            tsb = new XHorizontalScrollbar(this);
        }
        int min = target.getMinimum();
        int max = target.getMaximum();
        int vis = target.getVisibleAmount();
        int val = target.getValue();
        int line = target.getLineIncrement();
        int page = target.getPageIncrement();
        tsb.setValues(val, vis, min, max, line, page);
    }
    XScrollbarPeer(Scrollbar target) {
        super(target);
        this.target = target;
        xSetVisible(true);
    }
    private int getDefaultDimension() {
        if (System.getProperty("os.name").equals("Linux")) {
            return DEFAULT_WIDTH_LINUX;
        } else {
            return DEFAULT_WIDTH_SOLARIS;
        }
    }
    public Dimension getMinimumSize() {
        Scrollbar sb = (Scrollbar)target;
        return (sb.getOrientation() == Scrollbar.VERTICAL)
            ? new Dimension(getDefaultDimension(), DEFAULT_LENGTH)
                : new Dimension(DEFAULT_LENGTH, getDefaultDimension());
    }
    public void repaint() {
        Graphics g = getGraphics();
        if (g != null) paint(g);
    }
    public void paint(Graphics g) {
        Scrollbar sb = (Scrollbar)target;
        Color colors[] = getGUIcolors();
        g.setColor(colors[BACKGROUND_COLOR]);
        tsb.paint(g, colors, true);
    }
    public void repaintScrollbarRequest(XScrollbar sb) {
     repaint();
    }
    public void notifyValue(XScrollbar obj, int type, int value, boolean isAdjusting) {
        Scrollbar sb = (Scrollbar)target;
        sb.setValue(value);
        postEvent( new AdjustmentEvent(sb, AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED, type, value, isAdjusting));
    }
    public void handleJavaMouseEvent( MouseEvent mouseEvent ) {
        super.handleJavaMouseEvent(mouseEvent);
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        int modifiers = mouseEvent.getModifiers();
        int id = mouseEvent.getID();
        if ((mouseEvent.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
            return;
        }
        switch (mouseEvent.getID()) {
          case MouseEvent.MOUSE_PRESSED:
              target.requestFocus();
              tsb.handleMouseEvent(id, modifiers,x,y);
              break;
          case MouseEvent.MOUSE_RELEASED:
              tsb.handleMouseEvent(id, modifiers,x,y);
              break;
          case MouseEvent.MOUSE_DRAGGED:
              tsb.handleMouseEvent(id, modifiers,x,y);
              break;
        }
    }
    public void handleJavaKeyEvent(KeyEvent event) {
        super.handleJavaKeyEvent(event);
        if (log.isLoggable(PlatformLogger.FINEST)) log.finer("KeyEvent on scrollbar: " + event);
        if (!(event.isConsumed()) && event.getID() == KeyEvent.KEY_RELEASED) {
            switch(event.getKeyCode()) {
            case KeyEvent.VK_UP:
                log.finer("Scrolling up");
                tsb.notifyValue(tsb.getValue() - tsb.getUnitIncrement());
                break;
            case KeyEvent.VK_DOWN:
                log.finer("Scrolling down");
                tsb.notifyValue(tsb.getValue() + tsb.getUnitIncrement());
                break;
            case KeyEvent.VK_LEFT:
                log.finer("Scrolling up");
                tsb.notifyValue(tsb.getValue() - tsb.getUnitIncrement());
                break;
            case KeyEvent.VK_RIGHT:
                log.finer("Scrolling down");
                tsb.notifyValue(tsb.getValue() + tsb.getUnitIncrement());
                break;
            case KeyEvent.VK_PAGE_UP:
                log.finer("Scrolling page up");
                tsb.notifyValue(tsb.getValue() - tsb.getBlockIncrement());
                break;
            case KeyEvent.VK_PAGE_DOWN:
                log.finer("Scrolling page down");
                tsb.notifyValue(tsb.getValue() + tsb.getBlockIncrement());
                break;
            case KeyEvent.VK_HOME:
                log.finer("Scrolling to home");
                tsb.notifyValue(0);
                break;
            case KeyEvent.VK_END:
                log.finer("Scrolling to end");
                tsb.notifyValue(tsb.getMaximum());
                break;
            }
        }
    }
    public void setValue(int value) {
        tsb.setValue(value);
        repaint();
    }
    public void setValues(int value, int visible, int minimum, int maximum) {
        tsb.setValues(value, visible, minimum, maximum);
        repaint();
    }
    public void setLineIncrement(int l) {
        tsb.setUnitIncrement(l);
    }
    public void setPageIncrement(int p) {
        tsb.setBlockIncrement(p);
    }
    public void layout() {
        super.layout();
        tsb.setSize(width, height);
    }
}
