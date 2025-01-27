class Notepad extends JPanel {
    private static ResourceBundle resources;
    private final static String EXIT_AFTER_PAINT = "-exit";
    private static boolean exitAfterFirstPaint;
    static {
        try {
            resources = ResourceBundle.getBundle("resources.Notepad",
                    Locale.getDefault());
        } catch (MissingResourceException mre) {
            System.err.println("resources/Notepad.properties not found");
            System.exit(1);
        }
    }
    @Override
    public void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (exitAfterFirstPaint) {
            System.exit(0);
        }
    }
    @SuppressWarnings("OverridableMethodCallInConstructor")
    Notepad() {
        super(true);
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        editor = createEditor();
        editor.getDocument().addUndoableEditListener(undoHandler);
        commands = new HashMap<Object, Action>();
        Action[] actions = getActions();
        for (int i = 0; i < actions.length; i++) {
            Action a = actions[i];
            commands.put(a.getValue(Action.NAME), a);
        }
        JScrollPane scroller = new JScrollPane();
        JViewport port = scroller.getViewport();
        port.add(editor);
        try {
            String vpFlag = resources.getString("ViewportBackingStore");
            Boolean bs = Boolean.valueOf(vpFlag);
            port.setScrollMode(bs.booleanValue()
                    ? JViewport.BACKINGSTORE_SCROLL_MODE
                    : JViewport.BLIT_SCROLL_MODE);
        } catch (MissingResourceException ignored) {
        }
        menuItems = new HashMap<String, JMenuItem>();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add("North", createToolbar());
        panel.add("Center", scroller);
        add("Center", panel);
        add("South", createStatusbar());
    }
    public static void main(String[] args) {
        try {
            if (args.length > 0 && args[0].equals(EXIT_AFTER_PAINT)) {
                exitAfterFirstPaint = true;
            }
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JFrame frame = new JFrame();
                    frame.setTitle(resources.getString("Title"));
                    frame.setBackground(Color.lightGray);
                    frame.getContentPane().setLayout(new BorderLayout());
                    Notepad notepad = new Notepad();
                    frame.getContentPane().add("Center", notepad);
                    frame.setJMenuBar(notepad.createMenubar());
                    frame.addWindowListener(new AppCloser());
                    frame.pack();
                    frame.setSize(500, 600);
                    frame.setVisible(true);
                }
            });
        } catch (Throwable t) {
            Logger.getLogger(Notepad.class.getName()).log(Level.SEVERE,
                    "uncaught exception", t);
        }
    }
    public Action[] getActions() {
        return TextAction.augmentList(editor.getActions(), defaultActions);
    }
    protected JTextComponent createEditor() {
        JTextComponent c = new JTextArea();
        c.setDragEnabled(true);
        c.setFont(new Font("monospaced", Font.PLAIN, 12));
        return c;
    }
    protected JTextComponent getEditor() {
        return editor;
    }
    protected static final class AppCloser extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            System.exit(0);
        }
    }
    protected Frame getFrame() {
        for (Container p = getParent(); p != null; p = p.getParent()) {
            if (p instanceof Frame) {
                return (Frame) p;
            }
        }
        return null;
    }
    protected JMenuItem createMenuItem(String cmd) {
        JMenuItem mi = new JMenuItem(getResourceString(cmd + labelSuffix));
        URL url = getResource(cmd + imageSuffix);
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }
        String astr = getResourceString(cmd + actionSuffix);
        if (astr == null) {
            astr = cmd;
        }
        mi.setActionCommand(astr);
        Action a = getAction(astr);
        if (a != null) {
            mi.addActionListener(a);
            a.addPropertyChangeListener(createActionChangeListener(mi));
            mi.setEnabled(a.isEnabled());
        } else {
            mi.setEnabled(false);
        }
        menuItems.put(cmd, mi);
        return mi;
    }
    protected JMenuItem getMenuItem(String cmd) {
        return menuItems.get(cmd);
    }
    protected Action getAction(String cmd) {
        return commands.get(cmd);
    }
    protected String getResourceString(String nm) {
        String str;
        try {
            str = resources.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }
        return str;
    }
    protected URL getResource(String key) {
        String name = getResourceString(key);
        if (name != null) {
            URL url = this.getClass().getResource(name);
            return url;
        }
        return null;
    }
    protected Container getToolbar() {
        return toolbar;
    }
    protected JMenuBar getMenubar() {
        return menubar;
    }
    protected Component createStatusbar() {
        status = new StatusBar();
        return status;
    }
    protected void resetUndoManager() {
        undo.discardAllEdits();
        undoAction.update();
        redoAction.update();
    }
    private Component createToolbar() {
        toolbar = new JToolBar();
        String[] toolKeys = tokenize(getResourceString("toolbar"));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.add(Box.createHorizontalStrut(5));
            } else {
                toolbar.add(createTool(toolKeys[i]));
            }
        }
        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }
    protected Component createTool(String key) {
        return createToolbarButton(key);
    }
    protected JButton createToolbarButton(String key) {
        URL url = getResource(key + imageSuffix);
        JButton b = new JButton(new ImageIcon(url)) {
            @Override
            public float getAlignmentY() {
                return 0.5f;
            }
        };
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));
        String astr = getResourceString(key + actionSuffix);
        if (astr == null) {
            astr = key;
        }
        Action a = getAction(astr);
        if (a != null) {
            b.setActionCommand(astr);
            b.addActionListener(a);
        } else {
            b.setEnabled(false);
        }
        String tip = getResourceString(key + tipSuffix);
        if (tip != null) {
            b.setToolTipText(tip);
        }
        return b;
    }
    protected String[] tokenize(String input) {
        List<String> v = new ArrayList<String>();
        StringTokenizer t = new StringTokenizer(input);
        String cmd[];
        while (t.hasMoreTokens()) {
            v.add(t.nextToken());
        }
        cmd = new String[v.size()];
        for (int i = 0; i < cmd.length; i++) {
            cmd[i] = v.get(i);
        }
        return cmd;
    }
    protected JMenuBar createMenubar() {
        JMenuItem mi;
        JMenuBar mb = new JMenuBar();
        String[] menuKeys = tokenize(getResourceString("menubar"));
        for (int i = 0; i < menuKeys.length; i++) {
            JMenu m = createMenu(menuKeys[i]);
            if (m != null) {
                mb.add(m);
            }
        }
        this.menubar = mb;
        return mb;
    }
    protected JMenu createMenu(String key) {
        String[] itemKeys = tokenize(getResourceString(key));
        JMenu menu = new JMenu(getResourceString(key + "Label"));
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                menu.addSeparator();
            } else {
                JMenuItem mi = createMenuItem(itemKeys[i]);
                menu.add(mi);
            }
        }
        return menu;
    }
    protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
        return new ActionChangedListener(b);
    }
    private class ActionChangedListener implements PropertyChangeListener {
        JMenuItem menuItem;
        ActionChangedListener(JMenuItem mi) {
            super();
            this.menuItem = mi;
        }
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME)) {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
            } else if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
            }
        }
    }
    private JTextComponent editor;
    private Map<Object, Action> commands;
    private Map<String, JMenuItem> menuItems;
    private JMenuBar menubar;
    private JToolBar toolbar;
    private JComponent status;
    private JFrame elementTreeFrame;
    protected ElementTreePanel elementTreePanel;
    protected FileDialog fileDialog;
    protected UndoableEditListener undoHandler = new UndoHandler();
    protected UndoManager undo = new UndoManager();
    public static final String imageSuffix = "Image";
    public static final String labelSuffix = "Label";
    public static final String actionSuffix = "Action";
    public static final String tipSuffix = "Tooltip";
    public static final String openAction = "open";
    public static final String newAction = "new";
    public static final String saveAction = "save";
    public static final String exitAction = "exit";
    public static final String showElementTreeAction = "showElementTree";
    class UndoHandler implements UndoableEditListener {
        public void undoableEditHappened(UndoableEditEvent e) {
            undo.addEdit(e.getEdit());
            undoAction.update();
            redoAction.update();
        }
    }
    class StatusBar extends JComponent {
        public StatusBar() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        }
        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }
    }
    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
    private Action[] defaultActions = {
        new NewAction(),
        new OpenAction(),
        new SaveAction(),
        new ExitAction(),
        new ShowElementTreeAction(),
        undoAction,
        redoAction
    };
    class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }
        public void actionPerformed(ActionEvent e) {
            try {
                undo.undo();
            } catch (CannotUndoException ex) {
                Logger.getLogger(UndoAction.class.getName()).log(Level.SEVERE,
                        "Unable to undo", ex);
            }
            update();
            redoAction.update();
        }
        protected void update() {
            if (undo.canUndo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getUndoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Undo");
            }
        }
    }
    class RedoAction extends AbstractAction {
        public RedoAction() {
            super("Redo");
            setEnabled(false);
        }
        public void actionPerformed(ActionEvent e) {
            try {
                undo.redo();
            } catch (CannotRedoException ex) {
                Logger.getLogger(RedoAction.class.getName()).log(Level.SEVERE,
                        "Unable to redo", ex);
            }
            update();
            undoAction.update();
        }
        protected void update() {
            if (undo.canRedo()) {
                setEnabled(true);
                putValue(Action.NAME, undo.getRedoPresentationName());
            } else {
                setEnabled(false);
                putValue(Action.NAME, "Redo");
            }
        }
    }
    class OpenAction extends NewAction {
        OpenAction() {
            super(openAction);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            Frame frame = getFrame();
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showOpenDialog(frame);
            if (ret != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File f = chooser.getSelectedFile();
            if (f.isFile() && f.canRead()) {
                Document oldDoc = getEditor().getDocument();
                if (oldDoc != null) {
                    oldDoc.removeUndoableEditListener(undoHandler);
                }
                if (elementTreePanel != null) {
                    elementTreePanel.setEditor(null);
                }
                getEditor().setDocument(new PlainDocument());
                frame.setTitle(f.getName());
                Thread loader = new FileLoader(f, editor.getDocument());
                loader.start();
            } else {
                JOptionPane.showMessageDialog(getFrame(),
                        "Could not open file: " + f,
                        "Error opening file",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    class SaveAction extends AbstractAction {
        SaveAction() {
            super(saveAction);
        }
        public void actionPerformed(ActionEvent e) {
            Frame frame = getFrame();
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showSaveDialog(frame);
            if (ret != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File f = chooser.getSelectedFile();
            frame.setTitle(f.getName());
            Thread saver = new FileSaver(f, editor.getDocument());
            saver.start();
        }
    }
    class NewAction extends AbstractAction {
        NewAction() {
            super(newAction);
        }
        NewAction(String nm) {
            super(nm);
        }
        public void actionPerformed(ActionEvent e) {
            Document oldDoc = getEditor().getDocument();
            if (oldDoc != null) {
                oldDoc.removeUndoableEditListener(undoHandler);
            }
            getEditor().setDocument(new PlainDocument());
            getEditor().getDocument().addUndoableEditListener(undoHandler);
            resetUndoManager();
            getFrame().setTitle(resources.getString("Title"));
            revalidate();
        }
    }
    class ExitAction extends AbstractAction {
        ExitAction() {
            super(exitAction);
        }
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
    class ShowElementTreeAction extends AbstractAction {
        ShowElementTreeAction() {
            super(showElementTreeAction);
        }
        ShowElementTreeAction(String nm) {
            super(nm);
        }
        public void actionPerformed(ActionEvent e) {
            if (elementTreeFrame == null) {
                try {
                    String title = resources.getString("ElementTreeFrameTitle");
                    elementTreeFrame = new JFrame(title);
                } catch (MissingResourceException mre) {
                    elementTreeFrame = new JFrame();
                }
                elementTreeFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent weeee) {
                        elementTreeFrame.setVisible(false);
                    }
                });
                Container fContentPane = elementTreeFrame.getContentPane();
                fContentPane.setLayout(new BorderLayout());
                elementTreePanel = new ElementTreePanel(getEditor());
                fContentPane.add(elementTreePanel);
                elementTreeFrame.pack();
            }
            elementTreeFrame.setVisible(true);
        }
    }
    class FileLoader extends Thread {
        FileLoader(File f, Document doc) {
            setPriority(4);
            this.f = f;
            this.doc = doc;
        }
        @Override
        public void run() {
            try {
                status.removeAll();
                JProgressBar progress = new JProgressBar();
                progress.setMinimum(0);
                progress.setMaximum((int) f.length());
                status.add(progress);
                status.revalidate();
                Reader in = new FileReader(f);
                char[] buff = new char[4096];
                int nch;
                while ((nch = in.read(buff, 0, buff.length)) != -1) {
                    doc.insertString(doc.getLength(), new String(buff, 0, nch),
                            null);
                    progress.setValue(progress.getValue() + nch);
                }
            } catch (IOException e) {
                final String msg = e.getMessage();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(getFrame(),
                                "Could not open file: " + msg,
                                "Error opening file",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (BadLocationException e) {
                System.err.println(e.getMessage());
            }
            doc.addUndoableEditListener(undoHandler);
            status.removeAll();
            status.revalidate();
            resetUndoManager();
            if (elementTreePanel != null) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        elementTreePanel.setEditor(getEditor());
                    }
                });
            }
        }
        Document doc;
        File f;
    }
    class FileSaver extends Thread {
        Document doc;
        File f;
        FileSaver(File f, Document doc) {
            setPriority(4);
            this.f = f;
            this.doc = doc;
        }
        @Override
        @SuppressWarnings("SleepWhileHoldingLock")
        public void run() {
            try {
                status.removeAll();
                JProgressBar progress = new JProgressBar();
                progress.setMinimum(0);
                progress.setMaximum(doc.getLength());
                status.add(progress);
                status.revalidate();
                Writer out = new FileWriter(f);
                Segment text = new Segment();
                text.setPartialReturn(true);
                int charsLeft = doc.getLength();
                int offset = 0;
                while (charsLeft > 0) {
                    doc.getText(offset, Math.min(4096, charsLeft), text);
                    out.write(text.array, text.offset, text.count);
                    charsLeft -= text.count;
                    offset += text.count;
                    progress.setValue(offset);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Logger.getLogger(FileSaver.class.getName()).log(
                                Level.SEVERE,
                                null, e);
                    }
                }
                out.flush();
                out.close();
            } catch (IOException e) {
                final String msg = e.getMessage();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(getFrame(),
                                "Could not save file: " + msg,
                                "Error saving file",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (BadLocationException e) {
                System.err.println(e.getMessage());
            }
            status.removeAll();
            status.revalidate();
        }
    }
}
