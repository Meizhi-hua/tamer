public class XTextFieldPeer extends XComponentPeer implements TextFieldPeer {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XTextField");
    String text;
    XAWTTextField xtext;
    boolean firstChangeSkipped;
    public XTextFieldPeer(TextField target) {
        super(target);
        int start, end;
        firstChangeSkipped = false;
        text = target.getText();
        xtext = new XAWTTextField(text,this, target.getParent());
        xtext.getDocument().addDocumentListener(xtext);
        xtext.setCursor(target.getCursor());
        target.enableInputMethods(true);
        xtext.enableInputMethods(true);
        XToolkit.specialPeerMap.put(xtext,this);
        TextField txt = (TextField) target;
        initTextField();
        setText(txt.getText());
        if (txt.echoCharIsSet()) {
            setEchoChar(txt.getEchoChar());
        }
        else setEchoChar((char)0);
        start = txt.getSelectionStart();
        end = txt.getSelectionEnd();
        if (end > start) {
            select(start, end);
        }
        int caretPosition = Math.min(end, text.length());
        setCaretPosition(caretPosition);
        setEditable(txt.isEditable());
        firstChangeSkipped = true;
    }
    public void dispose() {
        XToolkit.specialPeerMap.remove(xtext);
        xtext.removeNotify();
        super.dispose();
    }
    void initTextField() {
        setVisible(target.isVisible());
        setBounds(x, y, width, height, SET_BOUNDS);
        AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
        foreground = compAccessor.getForeground(target);
        if (foreground == null)
            foreground = SystemColor.textText;
        setForeground(foreground);
        background = compAccessor.getBackground(target);
        if (background == null) {
            if (((TextField)target).isEditable()) background = SystemColor.text;
            else background = SystemColor.control;
        }
        setBackground(background);
        if (!target.isBackgroundSet()) {
            compAccessor.setBackground(target, background);
        }
        if (!target.isForegroundSet()) {
            target.setForeground(SystemColor.textText);
        }
        setFont(font);
    }
    public void setEditable(boolean editable) {
        if (xtext != null) {
            xtext.setEditable(editable);
            xtext.repaint();
        }
    }
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (xtext != null) {
            xtext.setEnabled(enabled);
            xtext.repaint();
        }
    }
    public InputMethodRequests getInputMethodRequests() {
        if (xtext != null) return xtext.getInputMethodRequests();
        else  return null;
    }
    void handleJavaInputMethodEvent(InputMethodEvent e) {
        if (xtext != null)
            xtext.processInputMethodEventImpl(e);
    }
    public void setEchoChar(char c) {
        if (xtext != null) {
            xtext.setEchoChar(c);
            xtext.putClientProperty("JPasswordField.cutCopyAllowed",
                    xtext.echoCharIsSet() ? Boolean.FALSE : Boolean.TRUE);
        }
    }
    public int getSelectionStart() {
        return xtext.getSelectionStart();
    }
    public int getSelectionEnd() {
        return xtext.getSelectionEnd();
    }
    public String getText() {
        return xtext.getText();
    }
    public void setText(String txt) {
        setXAWTTextField(txt);
        repaint();
    }
    protected boolean setXAWTTextField(String txt) {
        text = txt;
        if (xtext != null)  {
            xtext.getDocument().removeDocumentListener(xtext);
            xtext.setText(txt);
            if (firstChangeSkipped) {
                postEvent(new TextEvent(target, TextEvent.TEXT_VALUE_CHANGED));
            }
            xtext.getDocument().addDocumentListener(xtext);
            xtext.setCaretPosition(0);
        }
        return true;
    }
    public void setCaretPosition(int position) {
        if (xtext != null) xtext.setCaretPosition(position);
    }
    public void setEchoCharacter(char c) {
        setEchoChar(c);
    }
    void repaintText() {
        xtext.repaintNow();
    }
    public void setBackground(Color c) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("target="+ target + ", old=" + background + ", new=" + c);
        background = c;
        if (xtext != null) {
            xtext.setBackground(c);
            xtext.setSelectedTextColor(c);
        }
        repaintText();
    }
    public void setForeground(Color c) {
        foreground = c;
        if (xtext != null) {
            xtext.setForeground(foreground);
            xtext.setSelectionColor(foreground);
            xtext.setCaretColor(foreground);
        }
        repaintText();
    }
    public void setFont(Font f) {
        synchronized (getStateLock()) {
            font = f;
            if (xtext != null) {
                xtext.setFont(font);
            }
        }
        xtext.validate();
    }
    public Dimension preferredSize(int cols) {
        return getPreferredSize(cols);
    }
    public void deselect() {
        int selStart=xtext.getSelectionStart();
        int selEnd=xtext.getSelectionEnd();
        if (selStart != selEnd) {
            xtext.select(selStart,selStart);
        }
    }
    public int getCaretPosition() {
        return xtext.getCaretPosition();
    }
    public void select(int s, int e) {
        xtext.select(s,e);
        xtext.repaint();
    }
    public Dimension getMinimumSize() {
        return xtext.getMinimumSize();
    }
    public Dimension getPreferredSize() {
        return xtext.getPreferredSize();
    }
    public Dimension getPreferredSize(int cols) {
        return getMinimumSize(cols);
    }
    private static final int PADDING = 16;
    public Dimension getMinimumSize(int cols) {
        Font f = xtext.getFont();
        FontMetrics fm = xtext.getFontMetrics(f);
        return new Dimension(fm.charWidth('0') * cols + 10,
                             fm.getMaxDescent() + fm.getMaxAscent() + PADDING);
    }
    public boolean isFocusable() {
        return true;
    }
    public void action(final long when, final int modifiers) {
        postEvent(new ActionEvent(target, ActionEvent.ACTION_PERFORMED,
                                  text, when,
                                  modifiers));
    }
    protected void disposeImpl() {
    }
    public void repaint() {
        if (xtext  != null) xtext.repaint();
    }
    public void paint(Graphics g) {
        if (xtext  != null) xtext.paint(g);
    }
    public void print(Graphics g) {
        if (xtext != null) {
            xtext.print(g);
        }
    }
    public void focusLost(FocusEvent e) {
        super.focusLost(e);
        xtext.forwardFocusLost(e);
    }
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        xtext.forwardFocusGained(e);
    }
    void handleJavaKeyEvent(KeyEvent e) {
        AWTAccessor.getComponentAccessor().processEvent(xtext,e);
    }
    public void handleJavaMouseEvent( MouseEvent mouseEvent ) {
        super.handleJavaMouseEvent(mouseEvent);
        if (xtext != null)  {
            mouseEvent.setSource(xtext);
            int id = mouseEvent.getID();
            if (id == MouseEvent.MOUSE_DRAGGED || id == MouseEvent.MOUSE_MOVED)
                xtext.processMouseMotionEventImpl(mouseEvent);
            else
                xtext.processMouseEventImpl(mouseEvent);
        }
    }
    public Dimension minimumSize() {
        return getMinimumSize();
    }
    public Dimension minimumSize(int cols) {
        return getMinimumSize(cols);
    }
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (xtext != null) xtext.setVisible(b);
    }
    public void setBounds(int x, int y, int width, int height, int op) {
        super.setBounds(x, y, width, height, op);
        if (xtext != null) {
            int childX = x;
            int childY = y;
            Component parent = target.getParent();
            while (parent.isLightweight()){
                childX -= parent.getX();
                childY -= parent.getY();
                parent = parent.getParent();
            }
            xtext.setBounds(childX,childY,width,height);
            xtext.validate();
        }
    }
    public int getIndexAtPoint(int x, int y) { return -1; }
    public Rectangle getCharacterBounds(int i) { return null; }
    public long filterEvents(long mask) { return 0; }
    class  AWTTextFieldUI extends MotifPasswordFieldUI {
        JTextField jtf;
        protected String getPropertyPrefix() {
            JTextComponent comp = getComponent();
            if (comp instanceof JPasswordField && ((JPasswordField)comp).echoCharIsSet()) {
                return "PasswordField";
            } else {
                return "TextField";
            }
        }
        public void installUI(JComponent c) {
            super.installUI(c);
            jtf = (JTextField) c;
            JTextField editor = jtf;
            UIDefaults uidefaults = XToolkit.getUIDefaults();
            String prefix = getPropertyPrefix();
            Font f = editor.getFont();
            if ((f == null) || (f instanceof UIResource)) {
                editor.setFont(uidefaults.getFont(prefix + ".font"));
            }
            Color bg = editor.getBackground();
            if ((bg == null) || (bg instanceof UIResource)) {
                editor.setBackground(uidefaults.getColor(prefix + ".background"));
            }
            Color fg = editor.getForeground();
            if ((fg == null) || (fg instanceof UIResource)) {
                editor.setForeground(uidefaults.getColor(prefix + ".foreground"));
            }
            Color color = editor.getCaretColor();
            if ((color == null) || (color instanceof UIResource)) {
                editor.setCaretColor(uidefaults.getColor(prefix + ".caretForeground"));
            }
            Color s = editor.getSelectionColor();
            if ((s == null) || (s instanceof UIResource)) {
                editor.setSelectionColor(uidefaults.getColor(prefix + ".selectionBackground"));
            }
            Color sfg = editor.getSelectedTextColor();
            if ((sfg == null) || (sfg instanceof UIResource)) {
                editor.setSelectedTextColor(uidefaults.getColor(prefix + ".selectionForeground"));
            }
            Color dfg = editor.getDisabledTextColor();
            if ((dfg == null) || (dfg instanceof UIResource)) {
                editor.setDisabledTextColor(uidefaults.getColor(prefix + ".inactiveForeground"));
            }
            Border b = editor.getBorder();
            if ((b == null) || (b instanceof UIResource)) {
                editor.setBorder(uidefaults.getBorder(prefix + ".border"));
            }
            Insets margin = editor.getMargin();
            if (margin == null || margin instanceof UIResource) {
                editor.setMargin(uidefaults.getInsets(prefix + ".margin"));
            }
        }
        protected void installKeyboardActions() {
            super.installKeyboardActions();
            JTextComponent comp = getComponent();
            UIDefaults uidefaults = XToolkit.getUIDefaults();
            String prefix = getPropertyPrefix();
            InputMap map = (InputMap)uidefaults.get(prefix + ".focusInputMap");
            if (map != null) {
                SwingUtilities.replaceUIInputMap(comp, JComponent.WHEN_FOCUSED,
                                                 map);
            }
        }
        protected Caret createCaret() {
            return new XAWTCaret();
        }
    }
    class XAWTCaret extends DefaultCaret {
        public void focusGained(FocusEvent e) {
            super.focusGained(e);
            getComponent().repaint();
        }
        public void focusLost(FocusEvent e) {
            super.focusLost(e);
            getComponent().repaint();
        }
        public void setSelectionVisible(boolean vis) {
            if (vis){
                super.setSelectionVisible(vis);
            }else{
                setDot(getDot());
            }
        }
    }
    class XAWTTextField extends JPasswordField
        implements ActionListener,
                   DocumentListener
    {
        boolean isFocused = false;
        XComponentPeer peer;
        public XAWTTextField(String text, XComponentPeer peer, Container parent) {
            super(text);
            this.peer = peer;
            setDoubleBuffered(true);
            setFocusable(false);
            AWTAccessor.getComponentAccessor().setParent(this,parent);
            setBackground(peer.getPeerBackground());
            setForeground(peer.getPeerForeground());
            setFont(peer.getPeerFont());
            setCaretPosition(0);
            addActionListener(this);
            addNotify();
        }
        public void actionPerformed( ActionEvent actionEvent ) {
            peer.postEvent(new ActionEvent(peer.target,
                                           ActionEvent.ACTION_PERFORMED,
                                           getText(),
                                           actionEvent.getWhen(),
                                           actionEvent.getModifiers()));
        }
        public void insertUpdate(DocumentEvent e) {
            if (peer != null) {
                peer.postEvent(new TextEvent(peer.target,
                                             TextEvent.TEXT_VALUE_CHANGED));
            }
        }
        public void removeUpdate(DocumentEvent e) {
            if (peer != null) {
                peer.postEvent(new TextEvent(peer.target,
                                             TextEvent.TEXT_VALUE_CHANGED));
            }
        }
        public void changedUpdate(DocumentEvent e) {
            if (peer != null) {
                peer.postEvent(new TextEvent(peer.target,
                                             TextEvent.TEXT_VALUE_CHANGED));
            }
        }
        public ComponentPeer getPeer() {
            return (ComponentPeer) peer;
        }
        public void repaintNow() {
            paintImmediately(getBounds());
        }
        public Graphics getGraphics() {
            return peer.getGraphics();
        }
        public void updateUI() {
            ComponentUI ui = new AWTTextFieldUI();
            setUI(ui);
        }
        void forwardFocusGained( FocusEvent e) {
            isFocused = true;
            FocusEvent fe = CausedFocusEvent.retarget(e, this);
            super.processFocusEvent(fe);
        }
        void forwardFocusLost( FocusEvent e) {
            isFocused = false;
            FocusEvent fe = CausedFocusEvent.retarget(e, this);
            super.processFocusEvent(fe);
        }
        public boolean hasFocus() {
            return isFocused;
        }
        public void processInputMethodEventImpl(InputMethodEvent e) {
            processInputMethodEvent(e);
        }
        public void processMouseEventImpl(MouseEvent e) {
            processMouseEvent(e);
        }
        public void processMouseMotionEventImpl(MouseEvent e) {
            processMouseMotionEvent(e);
        }
        public void setTransferHandler(TransferHandler newHandler) {
            TransferHandler oldHandler = (TransferHandler)
                getClientProperty(XTextTransferHelper.getTransferHandlerKey());
            putClientProperty(XTextTransferHelper.getTransferHandlerKey(),
                              newHandler);
            firePropertyChange("transferHandler", oldHandler, newHandler);
        }
        public void setEchoChar(char c) {
            super.setEchoChar(c);
            ((AWTTextFieldUI)ui).installKeyboardActions();
        }
    }
}
