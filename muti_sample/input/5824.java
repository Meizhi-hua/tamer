public class  XMSelection {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XMSelection");
    String selectionName;
    Vector listeners;
    XAtom atoms[];
    long owners[];
    long eventMask;
    static int numScreens;
    static XAtom XA_MANAGER;
    static HashMap selectionMap;
    static {
        long display = XToolkit.getDisplay();
        XToolkit.awtLock();
        try {
            numScreens = XlibWrapper.ScreenCount(display);
        } finally {
            XToolkit.awtUnlock();
        }
        XA_MANAGER = XAtom.get("MANAGER");
        for (int screen = 0; screen < numScreens ; screen ++) {
            initScreen(display,screen);
        }
        selectionMap = new HashMap();
    }
    static void initScreen(long display, final int screen) {
        XToolkit.awtLock();
        try {
            long root = XlibWrapper.RootWindow(display,screen);
            XlibWrapper.XSelectInput(display, root, XConstants.StructureNotifyMask);
            XToolkit.addEventDispatcher(root,
                    new XEventDispatcher() {
                        public void dispatchEvent(XEvent ev) {
                                processRootEvent(ev, screen);
                            }
                        });
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public int getNumberOfScreens() {
        return numScreens;
    }
    void select(long extra_mask) {
        eventMask = extra_mask;
        for (int screen = 0; screen < numScreens ; screen ++) {
            selectPerScreen(screen,extra_mask);
        }
    }
    void resetOwner(long owner, final int screen) {
        XToolkit.awtLock();
        try {
            long display = XToolkit.getDisplay();
            synchronized(this) {
                setOwner(owner, screen);
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("New Selection Owner for screen " + screen + " = " + owner );
                XlibWrapper.XSelectInput(display, owner, XConstants.StructureNotifyMask | eventMask);
                XToolkit.addEventDispatcher(owner,
                        new XEventDispatcher() {
                            public void dispatchEvent(XEvent ev) {
                                dispatchSelectionEvent(ev, screen);
                            }
                        });
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void selectPerScreen(final int screen, long extra_mask) {
        XToolkit.awtLock();
        try {
            try {
                long display = XToolkit.getDisplay();
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("Grabbing XServer");
                XlibWrapper.XGrabServer(display);
                synchronized(this) {
                    String selection_name = getName()+"_S"+screen;
                    if (log.isLoggable(PlatformLogger.FINE)) log.fine("Screen = " + screen + " selection name = " + selection_name);
                    XAtom atom = XAtom.get(selection_name);
                    selectionMap.put(Long.valueOf(atom.getAtom()),this); 
                    setAtom(atom,screen);
                    long owner = XlibWrapper.XGetSelectionOwner(display, atom.getAtom());
                    if (owner != 0) {
                        setOwner(owner, screen);
                        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Selection Owner for screen " + screen + " = " + owner );
                        XlibWrapper.XSelectInput(display, owner, XConstants.StructureNotifyMask | extra_mask);
                        XToolkit.addEventDispatcher(owner,
                                new XEventDispatcher() {
                                        public void dispatchEvent(XEvent ev) {
                                            dispatchSelectionEvent(ev, screen);
                                        }
                                    });
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (log.isLoggable(PlatformLogger.FINE)) log.fine("UnGrabbing XServer");
                XlibWrapper.XUngrabServer(XToolkit.getDisplay());
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static boolean processClientMessage(XEvent xev, int screen) {
        XClientMessageEvent xce = xev.get_xclient();
        if (xce.get_message_type() == XA_MANAGER.getAtom()) {
            if (log.isLoggable(PlatformLogger.FINE)) log.fine("client messags = " + xce);
            long timestamp = xce.get_data(0);
            long atom = xce.get_data(1);
            long owner = xce.get_data(2);
            long data = xce.get_data(3);
            XMSelection sel = getInstance(atom);
            if (sel != null) {
                sel.resetOwner(owner,screen);
                sel.dispatchOwnerChangedEvent(xev,screen,owner,data, timestamp);
            }
        }
        return false;
    }
    static  boolean processRootEvent(XEvent xev, int screen) {
        switch (xev.get_type()) {
            case XConstants.ClientMessage: {
                return processClientMessage(xev, screen);
            }
        }
        return false;
    }
    static XMSelection getInstance(long selection) {
        return (XMSelection) selectionMap.get(Long.valueOf(selection));
    }
    public XMSelection (String selname) {
        this(selname, XConstants.PropertyChangeMask);
    }
    public XMSelection (String selname, long extraMask) {
        synchronized (this) {
            selectionName = selname;
            atoms = new XAtom[getNumberOfScreens()];
            owners = new long[getNumberOfScreens()];
        }
        select(extraMask);
    }
    public synchronized void addSelectionListener(XMSelectionListener listener) {
        if (listeners == null) {
            listeners = new Vector();
        }
        listeners.add(listener);
    }
    public synchronized void removeSelectionListener(XMSelectionListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    synchronized Collection getListeners() {
        return listeners;
    }
    synchronized XAtom getAtom(int screen) {
        if (atoms != null) {
            return atoms[screen];
        }
        return null;
    }
    synchronized void setAtom(XAtom a, int screen) {
        if (atoms != null) {
            atoms[screen] = a;
        }
    }
    synchronized long getOwner(int screen) {
        if (owners != null) {
            return owners[screen];
        }
        return 0;
    }
    synchronized void setOwner(long owner, int screen) {
        if (owners != null) {
            owners[screen] = owner;
        }
    }
    synchronized String getName() {
        return selectionName;
    }
    synchronized void dispatchSelectionChanged( XPropertyEvent ev, int screen) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Selection Changed : Screen = " + screen + "Event =" + ev);
        if (listeners != null) {
            Iterator iter = listeners.iterator();
            while (iter.hasNext()) {
                XMSelectionListener disp = (XMSelectionListener) iter.next();
                disp.selectionChanged(screen, this, ev.get_window(), ev);
            }
        }
    }
    synchronized void dispatchOwnerDeath(XDestroyWindowEvent de, int screen) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Owner dead : Screen = " + screen + "Event =" + de);
        if (listeners != null) {
            Iterator iter = listeners.iterator();
            while (iter.hasNext()) {
                XMSelectionListener disp = (XMSelectionListener) iter.next();
                disp.ownerDeath(screen, this, de.get_window());
            }
        }
    }
    void dispatchSelectionEvent(XEvent xev, int screen) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Event =" + xev);
        if (xev.get_type() == XConstants.DestroyNotify) {
            XDestroyWindowEvent de = xev.get_xdestroywindow();
            dispatchOwnerDeath( de, screen);
        }
        else if (xev.get_type() == XConstants.PropertyNotify)  {
            XPropertyEvent xpe = xev.get_xproperty();
            dispatchSelectionChanged( xpe, screen);
        }
    }
    synchronized void dispatchOwnerChangedEvent(XEvent ev, int screen, long owner, long data, long timestamp) {
        if (listeners != null) {
            Iterator iter = listeners.iterator();
            while (iter.hasNext()) {
                XMSelectionListener disp = (XMSelectionListener) iter.next();
                disp.ownerChanged(screen,this, owner, data, timestamp);
            }
        }
    }
}