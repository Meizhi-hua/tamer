class ScriptShellPanel extends JPanel {
    private static final long serialVersionUID = 4116273141148726319L;
    interface CommandProcessor {
        public String executeCommand(String cmd);
        public String getPrompt();
    }
    private CommandProcessor commandProcessor;
    private JTextComponent editor;
    private final ExecutorService commandExecutor =
            Executors.newSingleThreadExecutor();
    private boolean updating;
    public ScriptShellPanel(CommandProcessor cmdProc) {
        setLayout(new BorderLayout());
        this.commandProcessor = cmdProc;
        this.editor = new JTextArea();
        editor.setDocument(new EditableAtEndDocument());
        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().add(editor);
        add(scroller, BorderLayout.CENTER);
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (updating) return;
                beginUpdate();
                editor.setCaretPosition(editor.getDocument().getLength());
                if (insertContains(e, '\n')) {
                    String cmd = getMarkedText();
                    if ((cmd.length() == 0) ||
                        (cmd.charAt(cmd.length() - 1) != '\\')) {
                        final String cmd1 = trimContinuations(cmd);
                        commandExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                final String result = executeCommand(cmd1);
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result != null) {
                                            print(result + "\n");
                                        }
                                        printPrompt();
                                        setMark();
                                        endUpdate();
                                    }
                                });
                            }
                        });
                    } else {
                        endUpdate();
                    }
                } else {
                    endUpdate();
                }
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            }
        });
        editor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                int len = editor.getDocument().getLength();
                if (e.getDot() > len) {
                    editor.setCaretPosition(len);
                }
            }
        });
        Box hbox = Box.createHorizontalBox();
        hbox.add(Box.createGlue());
        JButton button = new JButton("Clear"); 
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clear();
            }
        });
        hbox.add(button);
        hbox.add(Box.createGlue());
        add(hbox, BorderLayout.SOUTH);
        clear();
    }
    public void dispose() {
        commandExecutor.shutdown();
    }
    @Override
    public void requestFocus() {
        editor.requestFocus();
    }
    public void clear() {
        clear(true);
    }
    public void clear(boolean prompt) {
        EditableAtEndDocument d = (EditableAtEndDocument) editor.getDocument();
        d.clear();
        if (prompt) printPrompt();
        setMark();
        editor.requestFocus();
    }
    public void setMark() {
        ((EditableAtEndDocument) editor.getDocument()).setMark();
    }
    public String getMarkedText() {
        try {
            String s = ((EditableAtEndDocument) editor.getDocument()).getMarkedText();
            int i = s.length();
            while ((i > 0) && (s.charAt(i - 1) == '\n')) {
                i--;
            }
            return s.substring(0, i);
        } catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }
    public void print(String s) {
        Document d = editor.getDocument();
        try {
            d.insertString(d.getLength(), s, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    private String executeCommand(String cmd) {
        return commandProcessor.executeCommand(cmd);
    }
    private String getPrompt() {
        return commandProcessor.getPrompt();
    }
    private void beginUpdate() {
        editor.setEditable(false);
        updating = true;
    }
    private void endUpdate() {
        editor.setEditable(true);
        updating = false;
    }
    private void printPrompt() {
        print(getPrompt());
    }
    private boolean insertContains(DocumentEvent e, char c) {
        String s = null;
        try {
            s = editor.getText(e.getOffset(), e.getLength());
            for (int i = 0; i < e.getLength(); i++) {
                if (s.charAt(i) == c) {
                    return true;
                }
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    private String trimContinuations(String text) {
        int i;
        while ((i = text.indexOf("\\\n")) >= 0) {
            text = text.substring(0, i) + text.substring(i+1, text.length());
        }
        return text;
    }
}
