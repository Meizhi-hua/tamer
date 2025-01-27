public abstract class InfoWindow extends Window {
    private Container container;
    private Closer closer;
    protected InfoWindow(Frame parent, Color borderColor) {
        super(parent);
        setType(Window.Type.POPUP);
        container = new Container() {
            @Override
            public Insets getInsets() {
                return new Insets(1, 1, 1, 1);
            }
        };
        setLayout(new BorderLayout());
        setBackground(borderColor);
        add(container, BorderLayout.CENTER);
        container.setLayout(new BorderLayout());
        closer = new Closer();
    }
    public Component add(Component c) {
        container.add(c, BorderLayout.CENTER);
        return c;
    }
    protected void setCloser(Runnable action, int time) {
        closer.set(action, time);
    }
    protected void show(Point corner, int indent) {
        assert SunToolkit.isDispatchThreadForAppContext(this);
        pack();
        Dimension size = getSize();
        Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (corner.x < scrSize.width/2 && corner.y < scrSize.height/2) { 
            setLocation(corner.x + indent, corner.y + indent);
        } else if (corner.x >= scrSize.width/2 && corner.y < scrSize.height/2) { 
            setLocation(corner.x - indent - size.width, corner.y + indent);
        } else if (corner.x < scrSize.width/2 && corner.y >= scrSize.height/2) { 
            setLocation(corner.x + indent, corner.y - indent - size.height);
        } else if (corner.x >= scrSize.width/2 && corner.y >= scrSize.height/2) { 
            setLocation(corner.x - indent - size.width, corner.y - indent - size.height);
        }
        super.show();
        closer.schedule();
    }
    public void hide() {
        closer.close();
    }
    private class Closer implements Runnable {
        Runnable action;
        int time;
        public void run() {
            doClose();
        }
        void set(Runnable action, int time) {
            this.action = action;
            this.time = time;
        }
        void schedule() {
            XToolkit.schedule(this, time);
        }
        void close() {
            XToolkit.remove(this);
            doClose();
        }
        private void doClose() {
            SunToolkit.executeOnEventHandlerThread(InfoWindow.this, new Runnable() {
                public void run() {
                    InfoWindow.super.hide();
                    invalidate();
                    if (action != null) {
                        action.run();
                    }
                }
            });
        }
    }
    private interface LiveArguments {
        boolean isDisposed();
        Rectangle getBounds();
    }
    public static class Tooltip extends InfoWindow {
        public interface LiveArguments extends InfoWindow.LiveArguments {
            String getTooltipString();
        }
        private final Object target;
        private final LiveArguments liveArguments;
        private final Label textLabel = new Label("");
        private final Runnable starter = new Runnable() {
                public void run() {
                    display();
                }};
        private final static int TOOLTIP_SHOW_TIME = 10000;
        private final static int TOOLTIP_START_DELAY_TIME = 1000;
        private final static int TOOLTIP_MAX_LENGTH = 64;
        private final static int TOOLTIP_MOUSE_CURSOR_INDENT = 5;
        private final static Color TOOLTIP_BACKGROUND_COLOR = new Color(255, 255, 220);
        private final static Font TOOLTIP_TEXT_FONT = XWindow.getDefaultFont();
        public Tooltip(Frame parent, Object target,
                LiveArguments liveArguments)
        {
            super(parent, Color.black);
            this.target = target;
            this.liveArguments = liveArguments;
            XTrayIconPeer.suppressWarningString(this);
            setCloser(null, TOOLTIP_SHOW_TIME);
            textLabel.setBackground(TOOLTIP_BACKGROUND_COLOR);
            textLabel.setFont(TOOLTIP_TEXT_FONT);
            add(textLabel);
        }
        private void display() {
            SunToolkit.executeOnEventHandlerThread(target, new Runnable() {
                    public void run() {
                        if (liveArguments.isDisposed()) {
                            return;
                        }
                        String tooltipString = liveArguments.getTooltipString();
                        if (tooltipString == null) {
                            return;
                        } else if (tooltipString.length() >  TOOLTIP_MAX_LENGTH) {
                            textLabel.setText(tooltipString.substring(0, TOOLTIP_MAX_LENGTH));
                        } else {
                            textLabel.setText(tooltipString);
                        }
                        Point pointer = (Point)AccessController.doPrivileged(new PrivilegedAction() {
                                public Object run() {
                                    if (!isPointerOverTrayIcon(liveArguments.getBounds())) {
                                        return null;
                                    }
                                    return MouseInfo.getPointerInfo().getLocation();
                                }
                            });
                        if (pointer == null) {
                            return;
                        }
                        show(new Point(pointer.x, pointer.y), TOOLTIP_MOUSE_CURSOR_INDENT);
                    }
                });
        }
        public void enter() {
            XToolkit.schedule(starter, TOOLTIP_START_DELAY_TIME);
        }
        public void exit() {
            XToolkit.remove(starter);
            if (isVisible()) {
                hide();
            }
        }
        private boolean isPointerOverTrayIcon(Rectangle trayRect) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            return !(p.x < trayRect.x || p.x > (trayRect.x + trayRect.width) ||
                     p.y < trayRect.y || p.y > (trayRect.y + trayRect.height));
        }
    }
    public static class Balloon extends InfoWindow {
        public interface LiveArguments extends InfoWindow.LiveArguments {
            String getActionCommand();
        }
        private final LiveArguments liveArguments;
        private final Object target;
        private final static int BALLOON_SHOW_TIME = 10000;
        private final static int BALLOON_TEXT_MAX_LENGTH = 256;
        private final static int BALLOON_WORD_LINE_MAX_LENGTH = 16;
        private final static int BALLOON_WORD_LINE_MAX_COUNT = 4;
        private final static int BALLOON_ICON_WIDTH = 32;
        private final static int BALLOON_ICON_HEIGHT = 32;
        private final static int BALLOON_TRAY_ICON_INDENT = 0;
        private final static Color BALLOON_CAPTION_BACKGROUND_COLOR = new Color(200, 200 ,255);
        private final static Font BALLOON_CAPTION_FONT = new Font(Font.DIALOG, Font.BOLD, 12);
        private Panel mainPanel = new Panel();
        private Panel captionPanel = new Panel();
        private Label captionLabel = new Label("");
        private Button closeButton = new Button("X");
        private Panel textPanel = new Panel();
        private XTrayIconPeer.IconCanvas iconCanvas = new XTrayIconPeer.IconCanvas(BALLOON_ICON_WIDTH, BALLOON_ICON_HEIGHT);
        private Label[] lineLabels = new Label[BALLOON_WORD_LINE_MAX_COUNT];
        private ActionPerformer ap = new ActionPerformer();
        private Image iconImage;
        private Image errorImage;
        private Image warnImage;
        private Image infoImage;
        private boolean gtkImagesLoaded;
        private Displayer displayer = new Displayer();
        public Balloon(Frame parent, Object target, LiveArguments liveArguments) {
            super(parent, new Color(90, 80 ,190));
            this.liveArguments = liveArguments;
            this.target = target;
            XTrayIconPeer.suppressWarningString(this);
            setCloser(new Runnable() {
                    public void run() {
                        if (textPanel != null) {
                            textPanel.removeAll();
                            textPanel.setSize(0, 0);
                            iconCanvas.setSize(0, 0);
                            XToolkit.awtLock();
                            try {
                                displayer.isDisplayed = false;
                                XToolkit.awtLockNotifyAll();
                            } finally {
                                XToolkit.awtUnlock();
                            }
                        }
                    }
                }, BALLOON_SHOW_TIME);
            add(mainPanel);
            captionLabel.setFont(BALLOON_CAPTION_FONT);
            captionLabel.addMouseListener(ap);
            captionPanel.setLayout(new BorderLayout());
            captionPanel.add(captionLabel, BorderLayout.WEST);
            captionPanel.add(closeButton, BorderLayout.EAST);
            captionPanel.setBackground(BALLOON_CAPTION_BACKGROUND_COLOR);
            captionPanel.addMouseListener(ap);
            closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        hide();
                    }
                });
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBackground(Color.white);
            mainPanel.add(captionPanel, BorderLayout.NORTH);
            mainPanel.add(iconCanvas, BorderLayout.WEST);
            mainPanel.add(textPanel, BorderLayout.CENTER);
            iconCanvas.addMouseListener(ap);
            for (int i = 0; i < BALLOON_WORD_LINE_MAX_COUNT; i++) {
                lineLabels[i] = new Label();
                lineLabels[i].addMouseListener(ap);
                lineLabels[i].setBackground(Color.white);
            }
            displayer.start();
        }
        public void display(String caption, String text, String messageType) {
            if (!gtkImagesLoaded) {
                loadGtkImages();
            }
            displayer.display(caption, text, messageType);
        }
        private void _display(String caption, String text, String messageType) {
            captionLabel.setText(caption);
            BreakIterator iter = BreakIterator.getWordInstance();
            if (text != null) {
                iter.setText(text);
                int start = iter.first(), end;
                int nLines = 0;
                do {
                    end = iter.next();
                    if (end == BreakIterator.DONE ||
                        text.substring(start, end).length() >= 50)
                    {
                        lineLabels[nLines].setText(text.substring(start, end == BreakIterator.DONE ?
                                                                  iter.last() : end));
                        textPanel.add(lineLabels[nLines++]);
                        start = end;
                    }
                    if (nLines == BALLOON_WORD_LINE_MAX_COUNT) {
                        if (end != BreakIterator.DONE) {
                            lineLabels[nLines - 1].setText(
                                new String(lineLabels[nLines - 1].getText() + " ..."));
                        }
                        break;
                    }
                } while (end != BreakIterator.DONE);
                textPanel.setLayout(new GridLayout(nLines, 1));
            }
            if ("ERROR".equals(messageType)) {
                iconImage = errorImage;
            } else if ("WARNING".equals(messageType)) {
                iconImage = warnImage;
            } else if ("INFO".equals(messageType)) {
                iconImage = infoImage;
            } else {
                iconImage = null;
            }
            if (iconImage != null) {
                Dimension tpSize = textPanel.getSize();
                iconCanvas.setSize(BALLOON_ICON_WIDTH, (BALLOON_ICON_HEIGHT > tpSize.height ?
                                                        BALLOON_ICON_HEIGHT : tpSize.height));
                iconCanvas.validate();
            }
            SunToolkit.executeOnEventHandlerThread(target, new Runnable() {
                    public void run() {
                        if (liveArguments.isDisposed()) {
                            return;
                        }
                        Point parLoc = getParent().getLocationOnScreen();
                        Dimension parSize = getParent().getSize();
                        show(new Point(parLoc.x + parSize.width/2, parLoc.y + parSize.height/2),
                             BALLOON_TRAY_ICON_INDENT);
                        if (iconImage != null) {
                            iconCanvas.updateImage(iconImage); 
                        }
                    }
                });
        }
        public void dispose() {
            displayer.interrupt();
            super.dispose();
        }
        private void loadGtkImages() {
            if (!gtkImagesLoaded) {
                errorImage = (Image)Toolkit.getDefaultToolkit().getDesktopProperty(
                    "gtk.icon.gtk-dialog-error.6.rtl");
                warnImage = (Image)Toolkit.getDefaultToolkit().getDesktopProperty(
                    "gtk.icon.gtk-dialog-warning.6.rtl");
                infoImage = (Image)Toolkit.getDefaultToolkit().getDesktopProperty(
                    "gtk.icon.gtk-dialog-info.6.rtl");
                gtkImagesLoaded = true;
            }
        }
        private class ActionPerformer extends MouseAdapter {
            public void mouseClicked(MouseEvent e) {
                hide();
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ActionEvent aev = new ActionEvent(target, ActionEvent.ACTION_PERFORMED,
                                                      liveArguments.getActionCommand(),
                                                      e.getWhen(), e.getModifiers());
                    XToolkit.postEvent(XToolkit.targetToAppContext(aev.getSource()), aev);
                }
            }
        }
        private class Displayer extends Thread {
            final int MAX_CONCURRENT_MSGS = 10;
            ArrayBlockingQueue<Message> messageQueue = new ArrayBlockingQueue<Message>(MAX_CONCURRENT_MSGS);
            boolean isDisplayed;
            Displayer() {
                setDaemon(true);
            }
            public void run() {
                while (true) {
                    Message msg = null;
                    try {
                        msg = (Message)messageQueue.take();
                    } catch (InterruptedException e) {
                        return;
                    }
                    XToolkit.awtLock();
                    try {
                        while (isDisplayed) {
                            try {
                                XToolkit.awtLockWait();
                            } catch (InterruptedException e) {
                                return;
                            }
                        }
                        isDisplayed = true;
                    } finally {
                        XToolkit.awtUnlock();
                    }
                    _display(msg.caption, msg.text, msg.messageType);
                }
            }
            void display(String caption, String text, String messageType) {
                messageQueue.offer(new Message(caption, text, messageType));
            }
        }
        private static class Message {
            String caption, text, messageType;
            Message(String caption, String text, String messageType) {
                this.caption = caption;
                this.text = text;
                this.messageType = messageType;
            }
        }
    }
}
