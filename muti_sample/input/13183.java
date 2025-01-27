class EventDispatchThread extends Thread {
    private static final PlatformLogger eventLog = PlatformLogger.getLogger("java.awt.event.EventDispatchThread");
    private EventQueue theQueue;
    private boolean doDispatch = true;
    private boolean threadDeathCaught = false;
    private static final int ANY_EVENT = -1;
    private Vector<EventFilter> eventFilters = new Vector<EventFilter>();
    EventDispatchThread(ThreadGroup group, String name, EventQueue queue) {
        super(group, name);
        setEventQueue(queue);
    }
    public void stopDispatching() {
        doDispatch = false;
    }
    public void run() {
        while (true) {
            try {
                pumpEvents(new Conditional() {
                    public boolean evaluate() {
                        return true;
                    }
                });
            } finally {
                EventQueue eq = getEventQueue();
                if (eq.detachDispatchThread(this) || threadDeathCaught) {
                    break;
                }
            }
        }
    }
    void pumpEvents(Conditional cond) {
        pumpEvents(ANY_EVENT, cond);
    }
    void pumpEventsForHierarchy(Conditional cond, Component modalComponent) {
        pumpEventsForHierarchy(ANY_EVENT, cond, modalComponent);
    }
    void pumpEvents(int id, Conditional cond) {
        pumpEventsForHierarchy(id, cond, null);
    }
    void pumpEventsForHierarchy(int id, Conditional cond, Component modalComponent) {
        pumpEventsForFilter(id, cond, new HierarchyEventFilter(modalComponent));
    }
    void pumpEventsForFilter(Conditional cond, EventFilter filter) {
        pumpEventsForFilter(ANY_EVENT, cond, filter);
    }
    void pumpEventsForFilter(int id, Conditional cond, EventFilter filter) {
        addEventFilter(filter);
        doDispatch = true;
        while (doDispatch && cond.evaluate()) {
            if (isInterrupted() || !pumpOneEventForFilters(id)) {
                doDispatch = false;
            }
        }
        removeEventFilter(filter);
    }
    void addEventFilter(EventFilter filter) {
        eventLog.finest("adding the event filter: " + filter);
        synchronized (eventFilters) {
            if (!eventFilters.contains(filter)) {
                if (filter instanceof ModalEventFilter) {
                    ModalEventFilter newFilter = (ModalEventFilter)filter;
                    int k = 0;
                    for (k = 0; k < eventFilters.size(); k++) {
                        EventFilter f = eventFilters.get(k);
                        if (f instanceof ModalEventFilter) {
                            ModalEventFilter cf = (ModalEventFilter)f;
                            if (cf.compareTo(newFilter) > 0) {
                                break;
                            }
                        }
                    }
                    eventFilters.add(k, filter);
                } else {
                    eventFilters.add(filter);
                }
            }
        }
    }
    void removeEventFilter(EventFilter filter) {
        eventLog.finest("removing the event filter: " + filter);
        synchronized (eventFilters) {
            eventFilters.remove(filter);
        }
    }
    boolean pumpOneEventForFilters(int id) {
        AWTEvent event = null;
        boolean eventOK = false;
        try {
            EventQueue eq = null;
            EventQueueDelegate.Delegate delegate = null;
            do {
                eq = getEventQueue();
                delegate = EventQueueDelegate.getDelegate();
                if (delegate != null && id == ANY_EVENT) {
                    event = delegate.getNextEvent(eq);
                } else {
                    event = (id == ANY_EVENT) ? eq.getNextEvent() : eq.getNextEvent(id);
                }
                eventOK = true;
                synchronized (eventFilters) {
                    for (int i = eventFilters.size() - 1; i >= 0; i--) {
                        EventFilter f = eventFilters.get(i);
                        EventFilter.FilterAction accept = f.acceptEvent(event);
                        if (accept == EventFilter.FilterAction.REJECT) {
                            eventOK = false;
                            break;
                        } else if (accept == EventFilter.FilterAction.ACCEPT_IMMEDIATELY) {
                            break;
                        }
                    }
                }
                eventOK = eventOK && SunDragSourceContextPeer.checkEvent(event);
                if (!eventOK) {
                    event.consume();
                }
            }
            while (eventOK == false);
            if (eventLog.isLoggable(PlatformLogger.FINEST)) {
                eventLog.finest("Dispatching: " + event);
            }
            Object handle = null;
            if (delegate != null) {
                handle = delegate.beforeDispatch(event);
            }
            eq.dispatchEvent(event);
            if (delegate != null) {
                delegate.afterDispatch(event, handle);
            }
            return true;
        }
        catch (ThreadDeath death) {
            threadDeathCaught = true;
            return false;
        }
        catch (InterruptedException interruptedException) {
            return false; 
        }
        catch (Throwable e) {
            processException(e);
        }
        return true;
    }
    private void processException(Throwable e) {
        if (eventLog.isLoggable(PlatformLogger.FINE)) {
            eventLog.fine("Processing exception: " + e);
        }
        getUncaughtExceptionHandler().uncaughtException(this, e);
    }
    public synchronized EventQueue getEventQueue() {
        return theQueue;
    }
    public synchronized void setEventQueue(EventQueue eq) {
        theQueue = eq;
    }
    private static class HierarchyEventFilter implements EventFilter {
        private Component modalComponent;
        public HierarchyEventFilter(Component modalComponent) {
            this.modalComponent = modalComponent;
        }
        public FilterAction acceptEvent(AWTEvent event) {
            if (modalComponent != null) {
                int eventID = event.getID();
                boolean mouseEvent = (eventID >= MouseEvent.MOUSE_FIRST) &&
                                     (eventID <= MouseEvent.MOUSE_LAST);
                boolean actionEvent = (eventID >= ActionEvent.ACTION_FIRST) &&
                                      (eventID <= ActionEvent.ACTION_LAST);
                boolean windowClosingEvent = (eventID == WindowEvent.WINDOW_CLOSING);
                if (Component.isInstanceOf(modalComponent, "javax.swing.JInternalFrame")) {
                    return windowClosingEvent ? FilterAction.REJECT : FilterAction.ACCEPT;
                }
                if (mouseEvent || actionEvent || windowClosingEvent) {
                    Object o = event.getSource();
                    if (o instanceof sun.awt.ModalExclude) {
                        return FilterAction.ACCEPT;
                    } else if (o instanceof Component) {
                        Component c = (Component) o;
                        boolean modalExcluded = false;
                        if (modalComponent instanceof Container) {
                            while (c != modalComponent && c != null) {
                                if ((c instanceof Window) &&
                                    (sun.awt.SunToolkit.isModalExcluded((Window)c))) {
                                    modalExcluded = true;
                                    break;
                                }
                                c = c.getParent();
                            }
                        }
                        if (!modalExcluded && (c != modalComponent)) {
                            return FilterAction.REJECT;
                        }
                    }
                }
            }
            return FilterAction.ACCEPT;
        }
    }
}
