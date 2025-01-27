public class XTrayIconPeer implements TrayIconPeer,
       InfoWindow.Balloon.LiveArguments,
       InfoWindow.Tooltip.LiveArguments
{
    private static final PlatformLogger ctrLog = PlatformLogger.getLogger("sun.awt.X11.XTrayIconPeer.centering");
    TrayIcon target;
    TrayIconEventProxy eventProxy;
    XTrayIconEmbeddedFrame eframe;
    TrayIconCanvas canvas;
    InfoWindow.Balloon balloon;
    InfoWindow.Tooltip tooltip;
    PopupMenu popup;
    String tooltipString;
    boolean isTrayIconDisplayed;
    long eframeParentID;
    final XEventDispatcher parentXED, eframeXED;
    static final XEventDispatcher dummyXED = new XEventDispatcher() {
            public void dispatchEvent(XEvent ev) {}
        };
    volatile boolean isDisposed;
    boolean isParentWindowLocated;
    int old_x, old_y;
    int ex_width, ex_height;
    final static int TRAY_ICON_WIDTH = 24;
    final static int TRAY_ICON_HEIGHT = 24;
    XTrayIconPeer(TrayIcon target)
      throws AWTException
    {
        this.target = target;
        eventProxy = new TrayIconEventProxy(this);
        canvas = new TrayIconCanvas(target, TRAY_ICON_WIDTH, TRAY_ICON_HEIGHT);
        eframe = new XTrayIconEmbeddedFrame();
        eframe.setSize(TRAY_ICON_WIDTH, TRAY_ICON_HEIGHT);
        eframe.add(canvas);
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                eframe.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
                return null;
            }
        });
        if (XWM.getWMID() != XWM.METACITY_WM) {
            parentXED = dummyXED; 
        } else {
            parentXED = new XEventDispatcher() {
                public void dispatchEvent(XEvent ev) {
                    if (isDisposed() || ev.get_type() != XConstants.ConfigureNotify) {
                        return;
                    }
                    XConfigureEvent ce = ev.get_xconfigure();
                    ctrLog.fine("ConfigureNotify on parent of {0}: {1}x{2}+{3}+{4} (old: {5}+{6})",
                                XTrayIconPeer.this, ce.get_width(), ce.get_height(),
                                ce.get_x(), ce.get_y(), old_x, old_y);
                    if (ce.get_height() != TRAY_ICON_HEIGHT && ce.get_width() != TRAY_ICON_WIDTH) {
                        ctrLog.fine("ConfigureNotify on parent of {0}. Skipping as intermediate resizing.",
                                    XTrayIconPeer.this);
                        return;
                    } else if (ce.get_height() > TRAY_ICON_HEIGHT) {
                        ctrLog.fine("ConfigureNotify on parent of {0}. Centering by \"Y\".",
                                    XTrayIconPeer.this);
                        XlibWrapper.XMoveResizeWindow(XToolkit.getDisplay(), eframeParentID,
                                                      ce.get_x(),
                                                      ce.get_y()+ce.get_height()/2-TRAY_ICON_HEIGHT/2,
                                                      TRAY_ICON_WIDTH,
                                                      TRAY_ICON_HEIGHT);
                        ex_height = ce.get_height();
                        ex_width = 0;
                    } else if (ce.get_width() > TRAY_ICON_WIDTH) {
                        ctrLog.fine("ConfigureNotify on parent of {0}. Centering by \"X\".",
                                    XTrayIconPeer.this);
                        XlibWrapper.XMoveResizeWindow(XToolkit.getDisplay(), eframeParentID,
                                                      ce.get_x()+ce.get_width()/2 - TRAY_ICON_WIDTH/2,
                                                      ce.get_y(),
                                                      TRAY_ICON_WIDTH,
                                                      TRAY_ICON_HEIGHT);
                        ex_width = ce.get_width();
                        ex_height = 0;
                    } else if (isParentWindowLocated && ce.get_x() != old_x && ce.get_y() != old_y) {
                        if (ex_height != 0) {
                            ctrLog.fine("ConfigureNotify on parent of {0}. Move detected. Centering by \"Y\".",
                                        XTrayIconPeer.this);
                            XlibWrapper.XMoveWindow(XToolkit.getDisplay(), eframeParentID,
                                                    ce.get_x(),
                                                    ce.get_y() + ex_height/2 - TRAY_ICON_HEIGHT/2);
                        } else if (ex_width != 0) {
                            ctrLog.fine("ConfigureNotify on parent of {0}. Move detected. Centering by \"X\".",
                                        XTrayIconPeer.this);
                            XlibWrapper.XMoveWindow(XToolkit.getDisplay(), eframeParentID,
                                                    ce.get_x() + ex_width/2 - TRAY_ICON_WIDTH/2,
                                                    ce.get_y());
                        } else {
                            ctrLog.fine("ConfigureNotify on parent of {0}. Move detected. Skipping.",
                                        XTrayIconPeer.this);
                        }
                    }
                    old_x = ce.get_x();
                    old_y = ce.get_y();
                    isParentWindowLocated = true;
                }
            };
        }
        eframeXED = new XEventDispatcher() {
                XTrayIconPeer xtiPeer = XTrayIconPeer.this;
                public void dispatchEvent(XEvent ev) {
                    if (isDisposed() || ev.get_type() != XConstants.ReparentNotify) {
                        return;
                    }
                    XReparentEvent re = ev.get_xreparent();
                    eframeParentID = re.get_parent();
                    if (eframeParentID == XToolkit.getDefaultRootWindow()) {
                        if (isTrayIconDisplayed) { 
                            SunToolkit.executeOnEventHandlerThread(xtiPeer.target, new Runnable() {
                                    public void run() {
                                        SystemTray.getSystemTray().remove(xtiPeer.target);
                                    }
                                });
                        }
                        return;
                    }
                    if (!isTrayIconDisplayed) {
                        addXED(eframeParentID, parentXED, XConstants.StructureNotifyMask);
                        isTrayIconDisplayed = true;
                        XToolkit.awtLockNotifyAll();
                    }
                }
            };
        addXED(getWindow(), eframeXED, XConstants.StructureNotifyMask);
        XSystemTrayPeer.getPeerInstance().addTrayIcon(this); 
        long start = System.currentTimeMillis();
        final long PERIOD = XToolkit.getTrayIconDisplayTimeout();
        XToolkit.awtLock();
        try {
            while (!isTrayIconDisplayed) {
                try {
                    XToolkit.awtLockWait(PERIOD);
                } catch (InterruptedException e) {
                    break;
                }
                if (System.currentTimeMillis() - start > PERIOD) {
                    break;
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
        if (!isTrayIconDisplayed || eframeParentID == 0 ||
            eframeParentID == XToolkit.getDefaultRootWindow())
        {
            throw new AWTException("TrayIcon couldn't be displayed.");
        }
        eframe.setVisible(true);
        updateImage();
        balloon = new InfoWindow.Balloon(eframe, target, this);
        tooltip = new InfoWindow.Tooltip(eframe, target, this);
        addListeners();
    }
    public void dispose() {
        if (SunToolkit.isDispatchThreadForAppContext(target)) {
            disposeOnEDT();
        } else {
            try {
                SunToolkit.executeOnEDTAndWait(target, new Runnable() {
                        public void run() {
                            disposeOnEDT();
                        }
                    });
            } catch (InterruptedException ie) {
            } catch (InvocationTargetException ite) {}
        }
    }
    private void disposeOnEDT() {
        XToolkit.awtLock();
        isDisposed = true;
        XToolkit.awtUnlock();
        removeXED(getWindow(), eframeXED);
        removeXED(eframeParentID, parentXED);
        eframe.realDispose();
        balloon.dispose();
        isTrayIconDisplayed = false;
        XToolkit.targetDisposedPeer(target, this);
    }
    public static void suppressWarningString(Window w) {
        AWTAccessor.getWindowAccessor().setTrayIconWindow(w, true);
    }
    public void setToolTip(String tooltip) {
        tooltipString = tooltip;
    }
    public String getTooltipString() {
        return tooltipString;
    }
    public void updateImage() {
        Runnable r = new Runnable() {
                public void run() {
                    canvas.updateImage(target.getImage());
                }
            };
        if (!SunToolkit.isDispatchThreadForAppContext(target)) {
            SunToolkit.executeOnEventHandlerThread(target, r);
        } else {
            r.run();
        }
    }
    public void displayMessage(String caption, String text, String messageType) {
        Point loc = getLocationOnScreen();
        Rectangle screen = eframe.getGraphicsConfiguration().getBounds();
        if (!(loc.x < screen.x || loc.x >= screen.x + screen.width ||
              loc.y < screen.y || loc.y >= screen.y + screen.height))
        {
            balloon.display(caption, text, messageType);
        }
    }
    public void showPopupMenu(int x, int y) {
        if (isDisposed())
            return;
        assert SunToolkit.isDispatchThreadForAppContext(target);
        PopupMenu newPopup = target.getPopupMenu();
        if (popup != newPopup) {
            if (popup != null) {
                eframe.remove(popup);
            }
            if (newPopup != null) {
                eframe.add(newPopup);
            }
            popup = newPopup;
        }
        if (popup != null) {
            Point loc = ((XBaseWindow)eframe.getPeer()).toLocal(new Point(x, y));
            popup.show(eframe, loc.x, loc.y);
        }
    }
    private void addXED(long window, XEventDispatcher xed, long mask) {
        if (window == 0) {
            return;
        }
        XToolkit.awtLock();
        try {
            XlibWrapper.XSelectInput(XToolkit.getDisplay(), window, mask);
        } finally {
            XToolkit.awtUnlock();
        }
        XToolkit.addEventDispatcher(window, xed);
    }
    private void removeXED(long window, XEventDispatcher xed) {
        if (window == 0) {
            return;
        }
        XToolkit.awtLock();
        try {
            XToolkit.removeEventDispatcher(window, xed);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private Point getLocationOnScreen() {
        return eframe.getLocationOnScreen();
    }
    public Rectangle getBounds() {
        Point loc = getLocationOnScreen();
        return new Rectangle(loc.x, loc.y, loc.x + TRAY_ICON_WIDTH, loc.y + TRAY_ICON_HEIGHT);
    }
    void addListeners() {
        canvas.addMouseListener(eventProxy);
        canvas.addMouseMotionListener(eventProxy);
    }
    long getWindow() {
        return ((XEmbeddedFramePeer)eframe.getPeer()).getWindow();
    }
    public boolean isDisposed() {
        return isDisposed;
    }
    public String getActionCommand() {
        return target.getActionCommand();
    }
    static class TrayIconEventProxy implements MouseListener, MouseMotionListener {
        XTrayIconPeer xtiPeer;
        TrayIconEventProxy(XTrayIconPeer xtiPeer) {
            this.xtiPeer = xtiPeer;
        }
        public void handleEvent(MouseEvent e) {
            if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
                return;
            }
            if (xtiPeer.isDisposed()) {
                return;
            }
            Point coord = XBaseWindow.toOtherWindow(xtiPeer.getWindow(),
                                                    XToolkit.getDefaultRootWindow(),
                                                    e.getX(), e.getY());
            if (e.isPopupTrigger()) {
                xtiPeer.showPopupMenu(coord.x, coord.y);
            }
            e.translatePoint(coord.x - e.getX(), coord.y - e.getY());
            e.setSource(xtiPeer.target);
            Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
        }
        public void mouseClicked(MouseEvent e) {
            if ((e.getClickCount() > 1 || xtiPeer.balloon.isVisible()) &&
                e.getButton() == MouseEvent.BUTTON1)
            {
                ActionEvent aev = new ActionEvent(xtiPeer.target, ActionEvent.ACTION_PERFORMED,
                                                  xtiPeer.target.getActionCommand(), e.getWhen(),
                                                  e.getModifiers());
                XToolkit.postEvent(XToolkit.targetToAppContext(aev.getSource()), aev);
            }
            if (xtiPeer.balloon.isVisible()) {
                xtiPeer.balloon.hide();
            }
            handleEvent(e);
        }
        public void mouseEntered(MouseEvent e) {
            xtiPeer.tooltip.enter();
            handleEvent(e);
        }
        public void mouseExited(MouseEvent e) {
            xtiPeer.tooltip.exit();
            handleEvent(e);
        }
        public void mousePressed(MouseEvent e) {
            handleEvent(e);
        }
        public void mouseReleased(MouseEvent e) {
            handleEvent(e);
        }
        public void mouseDragged(MouseEvent e) {
            handleEvent(e);
        }
        public void mouseMoved(MouseEvent e) {
            handleEvent(e);
        }
    }
    private static class XTrayIconEmbeddedFrame extends XEmbeddedFrame {
        public XTrayIconEmbeddedFrame(){
            super(XToolkit.getDefaultRootWindow(), true, true);
        }
        public boolean isUndecorated() {
            return true;
        }
        public boolean isResizable() {
            return false;
        }
        public void dispose(){
        }
        public void realDispose(){
            super.dispose();
        }
    };
    static class TrayIconCanvas extends IconCanvas {
        TrayIcon target;
        boolean autosize;
        TrayIconCanvas(TrayIcon target, int width, int height) {
            super(width, height);
            this.target = target;
        }
        protected void repaintImage(boolean doClear) {
            boolean old_autosize = autosize;
            autosize = target.isImageAutoSize();
            curW = autosize ? width : image.getWidth(observer);
            curH = autosize ? height : image.getHeight(observer);
            super.repaintImage(doClear || (old_autosize != autosize));
        }
    }
    public static class IconCanvas extends Canvas {
        volatile Image image;
        IconObserver observer;
        int width, height;
        int curW, curH;
        IconCanvas(int width, int height) {
            this.width = curW = width;
            this.height = curH = height;
        }
        public void updateImage(Image image) {
            this.image = image;
            if (observer == null) {
                observer = new IconObserver();
            }
            repaintImage(true);
        }
        protected void repaintImage(boolean doClear) {
            Graphics g = getGraphics();
            if (g != null) {
                try {
                    if (isVisible()) {
                        if (doClear) {
                            update(g);
                        } else {
                            paint(g);
                        }
                    }
                } finally {
                    g.dispose();
                }
            }
        }
        public void paint(Graphics g) {
            if (g != null && curW > 0 && curH > 0) {
                BufferedImage bufImage = new BufferedImage(curW, curH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gr = bufImage.createGraphics();
                if (gr != null) {
                    try {
                        gr.setColor(getBackground());
                        gr.fillRect(0, 0, curW, curH);
                        gr.drawImage(image, 0, 0, curW, curH, observer);
                        gr.dispose();
                        g.drawImage(bufImage, 0, 0, curW, curH, null);
                    } finally {
                        gr.dispose();
                    }
                }
            }
        }
        class IconObserver implements ImageObserver {
            public boolean imageUpdate(final Image image, final int flags, int x, int y, int width, int height) {
                if (image != IconCanvas.this.image || 
                    !IconCanvas.this.isVisible())
                {
                    return false;
                }
                if ((flags & (ImageObserver.FRAMEBITS | ImageObserver.ALLBITS |
                              ImageObserver.WIDTH | ImageObserver.HEIGHT)) != 0)
                {
                    SunToolkit.executeOnEventHandlerThread(IconCanvas.this, new Runnable() {
                            public void run() {
                                repaintImage(false);
                            }
                        });
                }
                return (flags & ImageObserver.ALLBITS) == 0;
            }
        }
    }
}
