class MotifDnDDropTargetProtocol extends XDropTargetProtocol {
    private static final Unsafe unsafe = XlibWrapper.unsafe;
    private long sourceWindow = 0;
    private long sourceWindowMask = 0;
    private int sourceProtocolVersion = 0;
    private int sourceActions = DnDConstants.ACTION_NONE;
    private long[] sourceFormats = null;
    private long sourceAtom = 0;
    private int userAction = DnDConstants.ACTION_NONE;
    private int sourceX = 0;
    private int sourceY = 0;
    private XWindow targetXWindow = null;
    private boolean topLevelLeavePostponed = false;
    protected MotifDnDDropTargetProtocol(XDropTargetProtocolListener listener) {
        super(listener);
    }
    static XDropTargetProtocol createInstance(XDropTargetProtocolListener listener) {
        return new MotifDnDDropTargetProtocol(listener);
    }
    public String getProtocolName() {
        return XDragAndDropProtocols.MotifDnD;
    }
    public void registerDropTarget(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        MotifDnDConstants.writeDragReceiverInfoStruct(window);
    }
    public void unregisterDropTarget(long window) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        MotifDnDConstants.XA_MOTIF_ATOM_0.DeleteProperty(window);
    }
    public void registerEmbedderDropSite(long embedder) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        boolean overriden = false;
        int version = 0;
        long proxy = 0;
        long newProxy = XDropTargetRegistry.getDnDProxyWindow();
        int status = 0;
        long data = 0;
        int dataSize = MotifDnDConstants.MOTIF_RECEIVER_INFO_SIZE;
        WindowPropertyGetter wpg =
            new WindowPropertyGetter(embedder,
                                     MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO,
                                     0, 0xFFFF, false,
                                     XConstants.AnyPropertyType);
        try {
            status = wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status == (int)XConstants.Success && wpg.getData() != 0 &&
                wpg.getActualType() != 0 && wpg.getActualFormat() == 8 &&
                wpg.getNumberOfItems() >=
                MotifDnDConstants.MOTIF_RECEIVER_INFO_SIZE) {
                overriden = true;
                data = wpg.getData();
                dataSize = wpg.getNumberOfItems();
                byte byteOrderByte = unsafe.getByte(data);
                {
                    int tproxy = unsafe.getInt(data + 4);
                    if (byteOrderByte != MotifDnDConstants.getByteOrderByte()) {
                        tproxy = MotifDnDConstants.Swapper.swap(tproxy);
                    }
                    proxy = tproxy;
                }
                if (proxy == newProxy) {
                    return;
                }
                {
                    int tproxy = (int)newProxy;
                    if (byteOrderByte != MotifDnDConstants.getByteOrderByte()) {
                        tproxy = MotifDnDConstants.Swapper.swap(tproxy);
                    }
                    unsafe.putInt(data + 4, tproxy);
                }
            } else {
                data = unsafe.allocateMemory(dataSize);
                unsafe.putByte(data, MotifDnDConstants.getByteOrderByte()); 
                unsafe.putByte(data + 1, MotifDnDConstants.MOTIF_DND_PROTOCOL_VERSION); 
                unsafe.putByte(data + 2, (byte)MotifDnDConstants.MOTIF_DYNAMIC_STYLE); 
                unsafe.putByte(data + 3, (byte)0); 
                unsafe.putInt(data + 4, (int)newProxy); 
                unsafe.putShort(data + 8, (short)0); 
                unsafe.putShort(data + 10, (short)0); 
                unsafe.putInt(data + 12, dataSize);
            }
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), embedder,
                                        MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO.getAtom(),
                                        MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO.getAtom(),
                                        8, XConstants.PropModeReplace,
                                        data, dataSize);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (XToolkit.saved_error != null &&
                XToolkit.saved_error.get_error_code() != XConstants.Success) {
                throw new XException("Cannot write Motif receiver info property");
            }
        } finally {
            if (!overriden) {
                unsafe.freeMemory(data);
                data = 0;
            }
            wpg.dispose();
        }
        putEmbedderRegistryEntry(embedder, overriden, version, proxy);
    }
    public void unregisterEmbedderDropSite(long embedder) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        EmbedderRegistryEntry entry = getEmbedderRegistryEntry(embedder);
        if (entry == null) {
            return;
        }
        if (entry.isOverriden()) {
            int status = 0;
            WindowPropertyGetter wpg =
                new WindowPropertyGetter(embedder,
                                         MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO,
                                         0, 0xFFFF, false,
                                         XConstants.AnyPropertyType);
            try {
                status = wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (status == (int)XConstants.Success && wpg.getData() != 0 &&
                    wpg.getActualType() != 0 && wpg.getActualFormat() == 8 &&
                    wpg.getNumberOfItems() >=
                    MotifDnDConstants.MOTIF_RECEIVER_INFO_SIZE) {
                    int dataSize = MotifDnDConstants.MOTIF_RECEIVER_INFO_SIZE;
                    long data = wpg.getData();
                    byte byteOrderByte = unsafe.getByte(data);
                    int tproxy = (int)entry.getProxy();
                    if (MotifDnDConstants.getByteOrderByte() != byteOrderByte) {
                        tproxy = MotifDnDConstants.Swapper.swap(tproxy);
                    }
                    unsafe.putInt(data + 4, tproxy);
                    XToolkit.WITH_XERROR_HANDLER(XErrorHandler.VerifyChangePropertyHandler.getInstance());
                    XlibWrapper.XChangeProperty(XToolkit.getDisplay(), embedder,
                                                MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO.getAtom(),
                                                MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO.getAtom(),
                                                8, XConstants.PropModeReplace,
                                                data, dataSize);
                    XToolkit.RESTORE_XERROR_HANDLER();
                    if (XToolkit.saved_error != null &&
                        XToolkit.saved_error.get_error_code() != XConstants.Success) {
                        throw new XException("Cannot write Motif receiver info property");
                    }
                }
            } finally {
                wpg.dispose();
            }
        } else {
            MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO.DeleteProperty(embedder);
        }
    }
    public void registerEmbeddedDropSite(long embedded) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        boolean overriden = false;
        int version = 0;
        long proxy = 0;
        int status = 0;
        WindowPropertyGetter wpg =
            new WindowPropertyGetter(embedded,
                                     MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO,
                                     0, 0xFFFF, false,
                                     XConstants.AnyPropertyType);
        try {
            status = wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status == (int)XConstants.Success && wpg.getData() != 0 &&
                wpg.getActualType() != 0 && wpg.getActualFormat() == 8 &&
                wpg.getNumberOfItems() >=
                MotifDnDConstants.MOTIF_RECEIVER_INFO_SIZE) {
                overriden = true;
                long data = wpg.getData();
                byte byteOrderByte = unsafe.getByte(data);
                {
                    int tproxy = unsafe.getInt(data + 4);
                    if (byteOrderByte != MotifDnDConstants.getByteOrderByte()) {
                        tproxy = MotifDnDConstants.Swapper.swap(tproxy);
                    }
                    proxy = tproxy;
                }
            }
        } finally {
            wpg.dispose();
        }
        putEmbedderRegistryEntry(embedded, overriden, version, proxy);
    }
    public boolean isProtocolSupported(long window) {
        WindowPropertyGetter wpg =
            new WindowPropertyGetter(window,
                                     MotifDnDConstants.XA_MOTIF_DRAG_RECEIVER_INFO,
                                     0, 0xFFFF, false,
                                     XConstants.AnyPropertyType);
        try {
            int status = wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            if (status == (int)XConstants.Success && wpg.getData() != 0 &&
                wpg.getActualType() != 0 && wpg.getActualFormat() == 8 &&
                wpg.getNumberOfItems() >=
                MotifDnDConstants.MOTIF_RECEIVER_INFO_SIZE) {
                return true;
            } else {
                return false;
            }
        } finally {
            wpg.dispose();
        }
    }
    private boolean processTopLevelEnter(XClientMessageEvent xclient) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        if (targetXWindow != null || sourceWindow != 0) {
            return false;
        }
        if (!(XToolkit.windowToXWindow(xclient.get_window()) instanceof XWindow)
            && getEmbedderRegistryEntry(xclient.get_window()) == null) {
            return false;
        }
        long source_win = 0;
        long source_win_mask = 0;
        int protocol_version = 0;
        long property_atom = 0;
        long[] formats = null;
        {
            long data = xclient.get_data();
            byte eventByteOrder = unsafe.getByte(data + 1);
            source_win = MotifDnDConstants.Swapper.getInt(data + 8, eventByteOrder);
            property_atom = MotifDnDConstants.Swapper.getInt(data + 12, eventByteOrder);
        }
        {
            WindowPropertyGetter wpg =
                new WindowPropertyGetter(source_win,
                                         XAtom.get(property_atom),
                                         0, 0xFFFF,
                                         false,
                                         MotifDnDConstants.XA_MOTIF_DRAG_INITIATOR_INFO.getAtom());
            try {
                int status = wpg.execute(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                if (status == XConstants.Success && wpg.getData() != 0 &&
                    wpg.getActualType() ==
                    MotifDnDConstants.XA_MOTIF_DRAG_INITIATOR_INFO.getAtom() &&
                    wpg.getActualFormat() == 8 &&
                    wpg.getNumberOfItems() ==
                    MotifDnDConstants.MOTIF_INITIATOR_INFO_SIZE) {
                    long data = wpg.getData();
                    byte propertyByteOrder = unsafe.getByte(data);
                    protocol_version = unsafe.getByte(data + 1);
                    if (protocol_version !=
                        MotifDnDConstants.MOTIF_DND_PROTOCOL_VERSION) {
                        return false;
                    }
                    int index =
                        MotifDnDConstants.Swapper.getShort(data + 2, propertyByteOrder);
                    formats = MotifDnDConstants.getTargetListForIndex(index);
                } else {
                    formats = new long[0];
                }
            } finally {
                wpg.dispose();
            }
        }
        XWindowAttributes wattr = new XWindowAttributes();
        try {
            XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
            int status = XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                          source_win, wattr.pData);
            XToolkit.RESTORE_XERROR_HANDLER();
            if (status == 0 ||
                (XToolkit.saved_error != null &&
                 XToolkit.saved_error.get_error_code() != XConstants.Success)) {
                throw new XException("XGetWindowAttributes failed");
            }
            source_win_mask = wattr.get_your_event_mask();
        } finally {
            wattr.dispose();
        }
        XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
        XlibWrapper.XSelectInput(XToolkit.getDisplay(), source_win,
                                 source_win_mask |
                                 XConstants.StructureNotifyMask);
        XToolkit.RESTORE_XERROR_HANDLER();
        if (XToolkit.saved_error != null &&
            XToolkit.saved_error.get_error_code() != XConstants.Success) {
            throw new XException("XSelectInput failed");
        }
        sourceWindow = source_win;
        sourceWindowMask = source_win_mask;
        sourceProtocolVersion = protocol_version;
        sourceActions = DnDConstants.ACTION_NONE;
        sourceFormats = formats;
        sourceAtom = property_atom;
        return true;
    }
    private boolean processDragMotion(XClientMessageEvent xclient) {
        long data = xclient.get_data();
        byte eventByteOrder = unsafe.getByte(data + 1);
        byte eventReason = (byte)(unsafe.getByte(data) &
                                  MotifDnDConstants.MOTIF_MESSAGE_REASON_MASK);
        int x = 0;
        int y = 0;
        short flags = MotifDnDConstants.Swapper.getShort(data + 2, eventByteOrder);
        int motif_action = (flags & MotifDnDConstants.MOTIF_DND_ACTION_MASK) >>
            MotifDnDConstants.MOTIF_DND_ACTION_SHIFT;
        int motif_actions = (flags & MotifDnDConstants.MOTIF_DND_ACTIONS_MASK) >>
            MotifDnDConstants.MOTIF_DND_ACTIONS_SHIFT;
        int java_action = MotifDnDConstants.getJavaActionsForMotifActions(motif_action);
        int java_actions = MotifDnDConstants.getJavaActionsForMotifActions(motif_actions);
        {
            int win = (int)sourceWindow;
            if (eventByteOrder != MotifDnDConstants.getByteOrderByte()) {
                win = MotifDnDConstants.Swapper.swap(win);
            }
            unsafe.putInt(data + 12, win);
        }
        XWindow xwindow = null;
        {
            XBaseWindow xbasewindow = XToolkit.windowToXWindow(xclient.get_window());
            if (xbasewindow instanceof XWindow) {
                xwindow = (XWindow)xbasewindow;
            }
        }
        if (eventReason == MotifDnDConstants.OPERATION_CHANGED) {
            x = sourceX;
            y = sourceY;
            if (xwindow == null) {
                xwindow = targetXWindow;
            }
        } else {
            x = MotifDnDConstants.Swapper.getShort(data + 8, eventByteOrder);
            y = MotifDnDConstants.Swapper.getShort(data + 10, eventByteOrder);
            if (xwindow == null) {
                long receiver =
                    XDropTargetRegistry.getRegistry().getEmbeddedDropSite(
                        xclient.get_window(), x, y);
                if (receiver != 0) {
                    XBaseWindow xbasewindow = XToolkit.windowToXWindow(receiver);
                    if (xbasewindow instanceof XWindow) {
                        xwindow = (XWindow)xbasewindow;
                    }
                }
            }
            if (xwindow != null) {
                Point p = xwindow.toLocal(x, y);
                x = p.x;
                y = p.y;
            }
        }
        if (xwindow == null) {
            if (targetXWindow != null) {
                notifyProtocolListener(targetXWindow, x, y,
                                       DnDConstants.ACTION_NONE, java_actions,
                                       xclient, MouseEvent.MOUSE_EXITED);
            }
        } else {
            int java_event_id = 0;
            if (targetXWindow == null) {
                java_event_id = MouseEvent.MOUSE_ENTERED;
            } else {
                java_event_id = MouseEvent.MOUSE_DRAGGED;
            }
            notifyProtocolListener(xwindow, x, y, java_action, java_actions,
                                   xclient, java_event_id);
        }
        sourceActions = java_actions;
        userAction = java_action;
        sourceX = x;
        sourceY = y;
        targetXWindow = xwindow;
        return true;
    }
    private boolean processTopLevelLeave(XClientMessageEvent xclient) {
        assert XToolkit.isAWTLockHeldByCurrentThread();
        long data = xclient.get_data();
        byte eventByteOrder = unsafe.getByte(data + 1);
        long source_win = MotifDnDConstants.Swapper.getInt(data + 8, eventByteOrder);
        if (source_win != sourceWindow) {
            return false;
        }
        topLevelLeavePostponed = true;
        {
            long proxy;
            if (getEmbedderRegistryEntry(xclient.get_window()) != null) {
                proxy = XDropTargetRegistry.getDnDProxyWindow();
            } else {
                proxy = xclient.get_window();
            }
            XClientMessageEvent dummy = new XClientMessageEvent();
            try {
                dummy.set_type(XConstants.ClientMessage);
                dummy.set_window(xclient.get_window());
                dummy.set_format(32);
                dummy.set_message_type(0);
                dummy.set_data(0, 0);
                dummy.set_data(1, 0);
                dummy.set_data(2, 0);
                dummy.set_data(3, 0);
                dummy.set_data(4, 0);
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                       proxy, false, XConstants.NoEventMask,
                                       dummy.pData);
            } finally {
                dummy.dispose();
            }
        }
        return true;
    }
    private boolean processDropStart(XClientMessageEvent xclient) {
        long data = xclient.get_data();
        byte eventByteOrder = unsafe.getByte(data + 1);
        long source_win =
            MotifDnDConstants.Swapper.getInt(data + 16, eventByteOrder);
        if (source_win != sourceWindow) {
            return false;
        }
        long property_atom =
            MotifDnDConstants.Swapper.getInt(data + 12, eventByteOrder);
        short flags =
            MotifDnDConstants.Swapper.getShort(data + 2, eventByteOrder);
        int motif_action = (flags & MotifDnDConstants.MOTIF_DND_ACTION_MASK) >>
            MotifDnDConstants.MOTIF_DND_ACTION_SHIFT;
        int motif_actions = (flags & MotifDnDConstants.MOTIF_DND_ACTIONS_MASK) >>
            MotifDnDConstants.MOTIF_DND_ACTIONS_SHIFT;
        int java_action = MotifDnDConstants.getJavaActionsForMotifActions(motif_action);
        int java_actions = MotifDnDConstants.getJavaActionsForMotifActions(motif_actions);
        int x = MotifDnDConstants.Swapper.getShort(data + 8, eventByteOrder);
        int y = MotifDnDConstants.Swapper.getShort(data + 10, eventByteOrder);
        XWindow xwindow = null;
        {
            XBaseWindow xbasewindow = XToolkit.windowToXWindow(xclient.get_window());
            if (xbasewindow instanceof XWindow) {
                xwindow = (XWindow)xbasewindow;
            }
        }
        if (xwindow == null) {
            long receiver =
                XDropTargetRegistry.getRegistry().getEmbeddedDropSite(
                    xclient.get_window(), x, y);
            if (receiver != 0) {
                XBaseWindow xbasewindow = XToolkit.windowToXWindow(receiver);
                if (xbasewindow instanceof XWindow) {
                    xwindow = (XWindow)xbasewindow;
                }
            }
        }
        if (xwindow != null) {
            Point p = xwindow.toLocal(x, y);
            x = p.x;
            y = p.y;
        }
        if (xwindow != null) {
            notifyProtocolListener(xwindow, x, y, java_action, java_actions,
                                   xclient, MouseEvent.MOUSE_RELEASED);
        } else if (targetXWindow != null) {
            notifyProtocolListener(targetXWindow, x, y,
                                   DnDConstants.ACTION_NONE, java_actions,
                                   xclient, MouseEvent.MOUSE_EXITED);
        }
        return true;
    }
    public int getMessageType(XClientMessageEvent xclient) {
        if (xclient.get_message_type() !=
            MotifDnDConstants.XA_MOTIF_DRAG_AND_DROP_MESSAGE.getAtom()) {
            return UNKNOWN_MESSAGE;
        }
        long data = xclient.get_data();
        byte reason = (byte)(unsafe.getByte(data) &
                             MotifDnDConstants.MOTIF_MESSAGE_REASON_MASK);
        switch (reason) {
        case MotifDnDConstants.TOP_LEVEL_ENTER :
            return ENTER_MESSAGE;
        case MotifDnDConstants.DRAG_MOTION :
        case MotifDnDConstants.OPERATION_CHANGED :
            return MOTION_MESSAGE;
        case MotifDnDConstants.TOP_LEVEL_LEAVE :
            return LEAVE_MESSAGE;
        case MotifDnDConstants.DROP_START :
            return DROP_MESSAGE;
        default:
            return UNKNOWN_MESSAGE;
        }
    }
    protected boolean processClientMessageImpl(XClientMessageEvent xclient) {
        if (xclient.get_message_type() !=
            MotifDnDConstants.XA_MOTIF_DRAG_AND_DROP_MESSAGE.getAtom()) {
            if (topLevelLeavePostponed) {
                topLevelLeavePostponed = false;
                cleanup();
            }
            return false;
        }
        long data = xclient.get_data();
        byte reason = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_REASON_MASK);
        byte origin = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_SENDER_MASK);
        if (topLevelLeavePostponed) {
            topLevelLeavePostponed = false;
            if (reason != MotifDnDConstants.DROP_START) {
                cleanup();
            }
        }
        if (origin != MotifDnDConstants.MOTIF_MESSAGE_FROM_INITIATOR) {
            return false;
        }
        switch (reason) {
        case MotifDnDConstants.TOP_LEVEL_ENTER :
            return processTopLevelEnter(xclient);
        case MotifDnDConstants.DRAG_MOTION :
        case MotifDnDConstants.OPERATION_CHANGED :
            return processDragMotion(xclient);
        case MotifDnDConstants.TOP_LEVEL_LEAVE :
            return processTopLevelLeave(xclient);
        case MotifDnDConstants.DROP_START :
            return processDropStart(xclient);
        default:
            return false;
        }
    }
    protected void sendEnterMessageToToplevel(long win,
                                              XClientMessageEvent xclient) {
        throw new Error("UNIMPLEMENTED");
    }
    protected void sendLeaveMessageToToplevel(long win,
                                              XClientMessageEvent xclient) {
        throw new Error("UNIMPLEMENTED");
    }
    public boolean forwardEventToEmbedded(long embedded, long ctxt,
                                          int eventID) {
        return false;
    }
    public boolean isXEmbedSupported() {
        return false;
    }
    public boolean sendResponse(long ctxt, int eventID, int action) {
        XClientMessageEvent xclient = new XClientMessageEvent(ctxt);
        if (xclient.get_message_type() !=
            MotifDnDConstants.XA_MOTIF_DRAG_AND_DROP_MESSAGE.getAtom()) {
            return false;
        }
        long data = xclient.get_data();
        byte reason = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_REASON_MASK);
        byte origin = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_SENDER_MASK);
        byte eventByteOrder = unsafe.getByte(data + 1);
        byte response_reason = (byte)0;
        if (origin != MotifDnDConstants.MOTIF_MESSAGE_FROM_INITIATOR) {
            return false;
        }
        switch (reason) {
        case MotifDnDConstants.TOP_LEVEL_ENTER:
        case MotifDnDConstants.TOP_LEVEL_LEAVE:
            return false;
        case MotifDnDConstants.DRAG_MOTION:
            switch (eventID) {
            case MouseEvent.MOUSE_ENTERED:
                response_reason = MotifDnDConstants.DROP_SITE_ENTER;
                break;
            case MouseEvent.MOUSE_DRAGGED:
                response_reason = MotifDnDConstants.DRAG_MOTION;
                break;
            case MouseEvent.MOUSE_EXITED:
                response_reason = MotifDnDConstants.DROP_SITE_LEAVE;
                break;
            }
            break;
        case MotifDnDConstants.OPERATION_CHANGED:
        case MotifDnDConstants.DROP_START:
            response_reason = reason;
            break;
        default:
            assert false;
        }
        XClientMessageEvent msg = new XClientMessageEvent();
        try {
            msg.set_type(XConstants.ClientMessage);
            msg.set_window(MotifDnDConstants.Swapper.getInt(data + 12, eventByteOrder));
            msg.set_format(8);
            msg.set_message_type(MotifDnDConstants.XA_MOTIF_DRAG_AND_DROP_MESSAGE.getAtom());
            long responseData = msg.get_data();
            unsafe.putByte(responseData, (byte)(response_reason |
                           MotifDnDConstants.MOTIF_MESSAGE_FROM_RECEIVER));
            unsafe.putByte(responseData + 1, MotifDnDConstants.getByteOrderByte());
            int response_flags = 0;
            if (response_reason != MotifDnDConstants.DROP_SITE_LEAVE) {
                short flags = MotifDnDConstants.Swapper.getShort(data + 2,
                                                                 eventByteOrder);
                byte dropSiteStatus = (action == DnDConstants.ACTION_NONE) ?
                    MotifDnDConstants.MOTIF_INVALID_DROP_SITE :
                    MotifDnDConstants.MOTIF_VALID_DROP_SITE;
                response_flags = flags &
                    ~MotifDnDConstants.MOTIF_DND_ACTION_MASK &
                    ~MotifDnDConstants.MOTIF_DND_STATUS_MASK;
                response_flags |=
                    MotifDnDConstants.getMotifActionsForJavaActions(action) <<
                    MotifDnDConstants.MOTIF_DND_ACTION_SHIFT;
                response_flags |=
                    dropSiteStatus << MotifDnDConstants.MOTIF_DND_STATUS_SHIFT;
            } else {
                response_flags = 0;
            }
            unsafe.putShort(responseData + 2, (short)response_flags);
            int time = MotifDnDConstants.Swapper.getInt(data + 4, eventByteOrder);
            unsafe.putInt(responseData + 4, time);
            if (response_reason != MotifDnDConstants.DROP_SITE_LEAVE) {
                short x = MotifDnDConstants.Swapper.getShort(data + 8,
                                                             eventByteOrder);
                short y = MotifDnDConstants.Swapper.getShort(data + 10,
                                                             eventByteOrder);
                unsafe.putShort(responseData + 8, x); 
                unsafe.putShort(responseData + 10, y); 
            } else {
                unsafe.putShort(responseData + 8, (short)0); 
                unsafe.putShort(responseData + 10, (short)0); 
            }
            XToolkit.awtLock();
            try {
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                                       msg.get_window(),
                                       false, XConstants.NoEventMask,
                                       msg.pData);
            } finally {
                XToolkit.awtUnlock();
            }
        } finally {
            msg.dispose();
        }
        return true;
    }
    public Object getData(long ctxt, long format)
      throws IllegalArgumentException, IOException {
        XClientMessageEvent xclient = new XClientMessageEvent(ctxt);
        if (xclient.get_message_type() !=
            MotifDnDConstants.XA_MOTIF_DRAG_AND_DROP_MESSAGE.getAtom()) {
            throw new IllegalArgumentException();
        }
        long data = xclient.get_data();
        byte reason = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_REASON_MASK);
        byte origin = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_SENDER_MASK);
        byte eventByteOrder = unsafe.getByte(data + 1);
        if (origin != MotifDnDConstants.MOTIF_MESSAGE_FROM_INITIATOR) {
            throw new IOException("Cannot get data: corrupted context");
        }
        long selatom = 0;
        switch (reason) {
        case MotifDnDConstants.DRAG_MOTION :
        case MotifDnDConstants.OPERATION_CHANGED :
            selatom = sourceAtom;
            break;
        case MotifDnDConstants.DROP_START :
            selatom = MotifDnDConstants.Swapper.getInt(data + 12, eventByteOrder);
            break;
        default:
            throw new IOException("Cannot get data: invalid message reason");
        }
        if (selatom == 0) {
            throw new IOException("Cannot get data: drag source property atom unavailable");
        }
        long time_stamp = MotifDnDConstants.Swapper.getInt(data + 4, eventByteOrder) & 0xffffffffL;
        XAtom selectionAtom = XAtom.get(selatom);
        XSelection selection = XSelection.getSelection(selectionAtom);
        if (selection == null) {
            selection = new XSelection(selectionAtom);
        }
        return selection.getData(format, time_stamp);
    }
    public boolean sendDropDone(long ctxt, boolean success, int dropAction) {
        XClientMessageEvent xclient = new XClientMessageEvent(ctxt);
        if (xclient.get_message_type() !=
            MotifDnDConstants.XA_MOTIF_DRAG_AND_DROP_MESSAGE.getAtom()) {
            return false;
        }
        long data = xclient.get_data();
        byte reason = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_REASON_MASK);
        byte origin = (byte)(unsafe.getByte(data) &
            MotifDnDConstants.MOTIF_MESSAGE_SENDER_MASK);
        byte eventByteOrder = unsafe.getByte(data + 1);
        if (origin != MotifDnDConstants.MOTIF_MESSAGE_FROM_INITIATOR) {
            return false;
        }
        if (reason != MotifDnDConstants.DROP_START) {
            return false;
        }
        long time_stamp = MotifDnDConstants.Swapper.getInt(data + 4, eventByteOrder) & 0xffffffffL;
        long sel_atom = MotifDnDConstants.Swapper.getInt(data + 12, eventByteOrder);
        long status_atom = 0;
        if (success) {
            status_atom = MotifDnDConstants.XA_XmTRANSFER_SUCCESS.getAtom();
        } else {
            status_atom = MotifDnDConstants.XA_XmTRANSFER_FAILURE.getAtom();
        }
        XToolkit.awtLock();
        try {
            XlibWrapper.XConvertSelection(XToolkit.getDisplay(),
                                          sel_atom,
                                          status_atom,
                                          MotifDnDConstants.XA_MOTIF_ATOM_0.getAtom(),
                                          XWindow.getXAWTRootWindow().getWindow(),
                                          time_stamp);
            XlibWrapper.XFlush(XToolkit.getDisplay());
        } finally {
            XToolkit.awtUnlock();
        }
        targetXWindow = null;
        cleanup();
        return true;
    }
    public final long getSourceWindow() {
        return sourceWindow;
    }
    public void cleanup() {
        XDropTargetEventProcessor.reset();
        if (targetXWindow != null) {
            notifyProtocolListener(targetXWindow, 0, 0,
                                   DnDConstants.ACTION_NONE, sourceActions,
                                   null, MouseEvent.MOUSE_EXITED);
        }
        if (sourceWindow != 0) {
            XToolkit.awtLock();
            try {
                XToolkit.WITH_XERROR_HANDLER(XErrorHandler.IgnoreBadWindowHandler.getInstance());
                XlibWrapper.XSelectInput(XToolkit.getDisplay(), sourceWindow,
                                         sourceWindowMask);
                XToolkit.RESTORE_XERROR_HANDLER();
            } finally {
                XToolkit.awtUnlock();
            }
        }
        sourceWindow = 0;
        sourceWindowMask = 0;
        sourceProtocolVersion = 0;
        sourceActions = DnDConstants.ACTION_NONE;
        sourceFormats = null;
        sourceAtom = 0;
        userAction = DnDConstants.ACTION_NONE;
        sourceX = 0;
        sourceY = 0;
        targetXWindow = null;
        topLevelLeavePostponed = false;
    }
    public boolean isDragOverComponent() {
        return targetXWindow != null;
    }
    private void notifyProtocolListener(XWindow xwindow, int x, int y,
                                        int dropAction, int actions,
                                        XClientMessageEvent xclient,
                                        int eventID) {
        long nativeCtxt = 0;
        if (xclient != null) {
            int size = XClientMessageEvent.getSize();
            nativeCtxt = unsafe.allocateMemory(size + 4 * Native.getLongSize());
            unsafe.copyMemory(xclient.pData, nativeCtxt, size);
        }
        getProtocolListener().handleDropTargetNotification(xwindow, x, y,
                                                           dropAction,
                                                           actions,
                                                           sourceFormats,
                                                           nativeCtxt,
                                                           eventID);
    }
}
