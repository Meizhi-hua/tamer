public class MetalFileChooserUI extends BasicFileChooserUI {
    private JLabel lookInLabel;
    private JComboBox directoryComboBox;
    private DirectoryComboBoxModel directoryComboBoxModel;
    private Action directoryComboBoxAction = new DirectoryComboBoxAction();
    private FilterComboBoxModel filterComboBoxModel;
    private JTextField fileNameTextField;
    private FilePane filePane;
    private JToggleButton listViewButton;
    private JToggleButton detailsViewButton;
    private JButton approveButton;
    private JButton cancelButton;
    private JPanel buttonPanel;
    private JPanel bottomPanel;
    private JComboBox filterComboBox;
    private static final Dimension hstrut5 = new Dimension(5, 1);
    private static final Dimension hstrut11 = new Dimension(11, 1);
    private static final Dimension vstrut5  = new Dimension(1, 5);
    private static final Insets shrinkwrap = new Insets(0,0,0,0);
    private static int PREF_WIDTH = 500;
    private static int PREF_HEIGHT = 326;
    private static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);
    private static int MIN_WIDTH = 500;
    private static int MIN_HEIGHT = 326;
    private static Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);
    private static int LIST_PREF_WIDTH = 405;
    private static int LIST_PREF_HEIGHT = 135;
    private static Dimension LIST_PREF_SIZE = new Dimension(LIST_PREF_WIDTH, LIST_PREF_HEIGHT);
    private int    lookInLabelMnemonic = 0;
    private String lookInLabelText = null;
    private String saveInLabelText = null;
    private int    fileNameLabelMnemonic = 0;
    private String fileNameLabelText = null;
    private int    folderNameLabelMnemonic = 0;
    private String folderNameLabelText = null;
    private int    filesOfTypeLabelMnemonic = 0;
    private String filesOfTypeLabelText = null;
    private String upFolderToolTipText = null;
    private String upFolderAccessibleName = null;
    private String homeFolderToolTipText = null;
    private String homeFolderAccessibleName = null;
    private String newFolderToolTipText = null;
    private String newFolderAccessibleName = null;
    private String listViewButtonToolTipText = null;
    private String listViewButtonAccessibleName = null;
    private String detailsViewButtonToolTipText = null;
    private String detailsViewButtonAccessibleName = null;
    private AlignedLabel fileNameLabel;
    private void populateFileNameLabel() {
        if (getFileChooser().getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
            fileNameLabel.setText(folderNameLabelText);
            fileNameLabel.setDisplayedMnemonic(folderNameLabelMnemonic);
        } else {
            fileNameLabel.setText(fileNameLabelText);
            fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
        }
    }
    public static ComponentUI createUI(JComponent c) {
        return new MetalFileChooserUI((JFileChooser) c);
    }
    public MetalFileChooserUI(JFileChooser filechooser) {
        super(filechooser);
    }
    public void installUI(JComponent c) {
        super.installUI(c);
    }
    public void uninstallComponents(JFileChooser fc) {
        fc.removeAll();
        bottomPanel = null;
        buttonPanel = null;
    }
    private class MetalFileChooserUIAccessor implements FilePane.FileChooserUIAccessor {
        public JFileChooser getFileChooser() {
            return MetalFileChooserUI.this.getFileChooser();
        }
        public BasicDirectoryModel getModel() {
            return MetalFileChooserUI.this.getModel();
        }
        public JPanel createList() {
            return MetalFileChooserUI.this.createList(getFileChooser());
        }
        public JPanel createDetailsView() {
            return MetalFileChooserUI.this.createDetailsView(getFileChooser());
        }
        public boolean isDirectorySelected() {
            return MetalFileChooserUI.this.isDirectorySelected();
        }
        public File getDirectory() {
            return MetalFileChooserUI.this.getDirectory();
        }
        public Action getChangeToParentDirectoryAction() {
            return MetalFileChooserUI.this.getChangeToParentDirectoryAction();
        }
        public Action getApproveSelectionAction() {
            return MetalFileChooserUI.this.getApproveSelectionAction();
        }
        public Action getNewFolderAction() {
            return MetalFileChooserUI.this.getNewFolderAction();
        }
        public MouseListener createDoubleClickListener(JList list) {
            return MetalFileChooserUI.this.createDoubleClickListener(getFileChooser(),
                                                                     list);
        }
        public ListSelectionListener createListSelectionListener() {
            return MetalFileChooserUI.this.createListSelectionListener(getFileChooser());
        }
    }
    public void installComponents(JFileChooser fc) {
        FileSystemView fsv = fc.getFileSystemView();
        fc.setBorder(new EmptyBorder(12, 12, 11, 11));
        fc.setLayout(new BorderLayout(0, 11));
        filePane = new FilePane(new MetalFileChooserUIAccessor());
        fc.addPropertyChangeListener(filePane);
        JPanel topPanel = new JPanel(new BorderLayout(11, 0));
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.LINE_AXIS));
        topPanel.add(topButtonPanel, BorderLayout.AFTER_LINE_ENDS);
        fc.add(topPanel, BorderLayout.NORTH);
        lookInLabel = new JLabel(lookInLabelText);
        lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        topPanel.add(lookInLabel, BorderLayout.BEFORE_LINE_BEGINS);
        directoryComboBox = new JComboBox() {
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 150;
                return d;
            }
        };
        directoryComboBox.putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY,
                                            lookInLabelText);
        directoryComboBox.putClientProperty( "JComboBox.isTableCellEditor", Boolean.TRUE );
        lookInLabel.setLabelFor(directoryComboBox);
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.addActionListener(directoryComboBoxAction);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        directoryComboBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
        directoryComboBox.setMaximumRowCount(8);
        topPanel.add(directoryComboBox, BorderLayout.CENTER);
        JButton upFolderButton = new JButton(getChangeToParentDirectoryAction());
        upFolderButton.setText(null);
        upFolderButton.setIcon(upFolderIcon);
        upFolderButton.setToolTipText(upFolderToolTipText);
        upFolderButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                         upFolderAccessibleName);
        upFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        upFolderButton.setMargin(shrinkwrap);
        topButtonPanel.add(upFolderButton);
        topButtonPanel.add(Box.createRigidArea(hstrut5));
        File homeDir = fsv.getHomeDirectory();
        String toolTipText = homeFolderToolTipText;
        if (fsv.isRoot(homeDir)) {
            toolTipText = getFileView(fc).getName(homeDir); 
        }
        JButton b = new JButton(homeFolderIcon);
        b.setToolTipText(toolTipText);
        b.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                            homeFolderAccessibleName);
        b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        b.setMargin(shrinkwrap);
        b.addActionListener(getGoHomeAction());
        topButtonPanel.add(b);
        topButtonPanel.add(Box.createRigidArea(hstrut5));
        if (!UIManager.getBoolean("FileChooser.readOnly")) {
            b = new JButton(filePane.getNewFolderAction());
            b.setText(null);
            b.setIcon(newFolderIcon);
            b.setToolTipText(newFolderToolTipText);
            b.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                newFolderAccessibleName);
            b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
            b.setMargin(shrinkwrap);
        }
        topButtonPanel.add(b);
        topButtonPanel.add(Box.createRigidArea(hstrut5));
        ButtonGroup viewButtonGroup = new ButtonGroup();
        listViewButton = new JToggleButton(listViewIcon);
        listViewButton.setToolTipText(listViewButtonToolTipText);
        listViewButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                         listViewButtonAccessibleName);
        listViewButton.setSelected(true);
        listViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        listViewButton.setMargin(shrinkwrap);
        listViewButton.addActionListener(filePane.getViewTypeAction(FilePane.VIEWTYPE_LIST));
        topButtonPanel.add(listViewButton);
        viewButtonGroup.add(listViewButton);
        detailsViewButton = new JToggleButton(detailsViewIcon);
        detailsViewButton.setToolTipText(detailsViewButtonToolTipText);
        detailsViewButton.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY,
                                            detailsViewButtonAccessibleName);
        detailsViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        detailsViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        detailsViewButton.setMargin(shrinkwrap);
        detailsViewButton.addActionListener(filePane.getViewTypeAction(FilePane.VIEWTYPE_DETAILS));
        topButtonPanel.add(detailsViewButton);
        viewButtonGroup.add(detailsViewButton);
        filePane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if ("viewType".equals(e.getPropertyName())) {
                    int viewType = filePane.getViewType();
                    switch (viewType) {
                      case FilePane.VIEWTYPE_LIST:
                        listViewButton.setSelected(true);
                        break;
                      case FilePane.VIEWTYPE_DETAILS:
                        detailsViewButton.setSelected(true);
                        break;
                    }
                }
            }
        });
        fc.add(getAccessoryPanel(), BorderLayout.AFTER_LINE_ENDS);
        JComponent accessory = fc.getAccessory();
        if(accessory != null) {
            getAccessoryPanel().add(accessory);
        }
        filePane.setPreferredSize(LIST_PREF_SIZE);
        fc.add(filePane, BorderLayout.CENTER);
        JPanel bottomPanel = getBottomPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        fc.add(bottomPanel, BorderLayout.SOUTH);
        JPanel fileNamePanel = new JPanel();
        fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.LINE_AXIS));
        bottomPanel.add(fileNamePanel);
        bottomPanel.add(Box.createRigidArea(vstrut5));
        fileNameLabel = new AlignedLabel();
        populateFileNameLabel();
        fileNamePanel.add(fileNameLabel);
        fileNameTextField = new JTextField(35) {
            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        fileNamePanel.add(fileNameTextField);
        fileNameLabel.setLabelFor(fileNameTextField);
        fileNameTextField.addFocusListener(
            new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    if (!getFileChooser().isMultiSelectionEnabled()) {
                        filePane.clearSelection();
                    }
                }
            }
        );
        if (fc.isMultiSelectionEnabled()) {
            setFileName(fileNameString(fc.getSelectedFiles()));
        } else {
            setFileName(fileNameString(fc.getSelectedFile()));
        }
        JPanel filesOfTypePanel = new JPanel();
        filesOfTypePanel.setLayout(new BoxLayout(filesOfTypePanel, BoxLayout.LINE_AXIS));
        bottomPanel.add(filesOfTypePanel);
        AlignedLabel filesOfTypeLabel = new AlignedLabel(filesOfTypeLabelText);
        filesOfTypeLabel.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
        filesOfTypePanel.add(filesOfTypeLabel);
        filterComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterComboBoxModel);
        filterComboBox = new JComboBox(filterComboBoxModel);
        filterComboBox.putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY,
                                         filesOfTypeLabelText);
        filesOfTypeLabel.setLabelFor(filterComboBox);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        filesOfTypePanel.add(filterComboBox);
        getButtonPanel().setLayout(new ButtonAreaLayout());
        approveButton = new JButton(getApproveButtonText(fc));
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        getButtonPanel().add(approveButton);
        cancelButton = new JButton(cancelButtonText);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        getButtonPanel().add(cancelButton);
        if(fc.getControlButtonsAreShown()) {
            addControlButtons();
        }
        groupLabels(new AlignedLabel[] { fileNameLabel, filesOfTypeLabel });
    }
    protected JPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new JPanel();
        }
        return buttonPanel;
    }
    protected JPanel getBottomPanel() {
        if(bottomPanel == null) {
            bottomPanel = new JPanel();
        }
        return bottomPanel;
    }
    protected void installStrings(JFileChooser fc) {
        super.installStrings(fc);
        Locale l = fc.getLocale();
        lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText",l);
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText",l);
        fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText",l);
        folderNameLabelMnemonic = UIManager.getInt("FileChooser.folderNameLabelMnemonic");
        folderNameLabelText = UIManager.getString("FileChooser.folderNameLabelText",l);
        filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
        filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText",l);
        upFolderToolTipText =  UIManager.getString("FileChooser.upFolderToolTipText",l);
        upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName",l);
        homeFolderToolTipText =  UIManager.getString("FileChooser.homeFolderToolTipText",l);
        homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName",l);
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText",l);
        newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName",l);
        listViewButtonToolTipText = UIManager.getString("FileChooser.listViewButtonToolTipText",l);
        listViewButtonAccessibleName = UIManager.getString("FileChooser.listViewButtonAccessibleName",l);
        detailsViewButtonToolTipText = UIManager.getString("FileChooser.detailsViewButtonToolTipText",l);
        detailsViewButtonAccessibleName = UIManager.getString("FileChooser.detailsViewButtonAccessibleName",l);
    }
    protected void installListeners(JFileChooser fc) {
        super.installListeners(fc);
        ActionMap actionMap = getActionMap();
        SwingUtilities.replaceUIActionMap(fc, actionMap);
    }
    protected ActionMap getActionMap() {
        return createActionMap();
    }
    protected ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        FilePane.addActionsToMap(map, filePane.getActions());
        return map;
    }
    protected JPanel createList(JFileChooser fc) {
        return filePane.createList();
    }
    protected JPanel createDetailsView(JFileChooser fc) {
        return filePane.createDetailsView();
    }
    public ListSelectionListener createListSelectionListener(JFileChooser fc) {
        return super.createListSelectionListener(fc);
    }
    protected class SingleClickListener extends MouseAdapter {
        public  SingleClickListener(JList list) {
        }
    }
    protected class FileRenderer extends DefaultListCellRenderer  {
    }
    public void uninstallUI(JComponent c) {
        c.removePropertyChangeListener(filterComboBoxModel);
        c.removePropertyChangeListener(filePane);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        fileNameTextField.removeActionListener(getApproveSelectionAction());
        if (filePane != null) {
            filePane.uninstallUI();
            filePane = null;
        }
        super.uninstallUI(c);
    }
    public Dimension getPreferredSize(JComponent c) {
        int prefWidth = PREF_SIZE.width;
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(d.width < prefWidth ? prefWidth : d.width,
                                 d.height < PREF_SIZE.height ? PREF_SIZE.height : d.height);
        } else {
            return new Dimension(prefWidth, PREF_SIZE.height);
        }
    }
    public Dimension getMinimumSize(JComponent c) {
        return MIN_SIZE;
    }
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    private String fileNameString(File file) {
        if (file == null) {
            return null;
        } else {
            JFileChooser fc = getFileChooser();
            if ((fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) ||
                (fc.isDirectorySelectionEnabled() && fc.isFileSelectionEnabled() && fc.getFileSystemView().isFileSystemRoot(file))) {
                return file.getPath();
            } else {
                return file.getName();
            }
        }
    }
    private String fileNameString(File[] files) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; files != null && i < files.length; i++) {
            if (i > 0) {
                buf.append(" ");
            }
            if (files.length > 1) {
                buf.append("\"");
            }
            buf.append(fileNameString(files[i]));
            if (files.length > 1) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }
    private void doSelectedFileChanged(PropertyChangeEvent e) {
        File f = (File) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (f != null
            && ((fc.isFileSelectionEnabled() && !f.isDirectory())
                || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {
            setFileName(fileNameString(f));
        }
    }
    private void doSelectedFilesChanged(PropertyChangeEvent e) {
        File[] files = (File[]) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (files != null
            && files.length > 0
            && (files.length > 1 || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
            setFileName(fileNameString(files));
        }
    }
    private void doDirectoryChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();
        clearIconCache();
        File currentDirectory = fc.getCurrentDirectory();
        if(currentDirectory != null) {
            directoryComboBoxModel.addItem(currentDirectory);
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if (fsv.isFileSystem(currentDirectory)) {
                    setFileName(currentDirectory.getPath());
                } else {
                    setFileName(null);
                }
            }
        }
    }
    private void doFilterChanged(PropertyChangeEvent e) {
        clearIconCache();
    }
    private void doFileSelectionModeChanged(PropertyChangeEvent e) {
        if (fileNameLabel != null) {
            populateFileNameLabel();
        }
        clearIconCache();
        JFileChooser fc = getFileChooser();
        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null
            && fc.isDirectorySelectionEnabled()
            && !fc.isFileSelectionEnabled()
            && fc.getFileSystemView().isFileSystem(currentDirectory)) {
            setFileName(currentDirectory.getPath());
        } else {
            setFileName(null);
        }
    }
    private void doAccessoryChanged(PropertyChangeEvent e) {
        if(getAccessoryPanel() != null) {
            if(e.getOldValue() != null) {
                getAccessoryPanel().remove((JComponent) e.getOldValue());
            }
            JComponent accessory = (JComponent) e.getNewValue();
            if(accessory != null) {
                getAccessoryPanel().add(accessory, BorderLayout.CENTER);
            }
        }
    }
    private void doApproveButtonTextChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
    }
    private void doDialogTypeChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
            lookInLabel.setText(saveInLabelText);
        } else {
            lookInLabel.setText(lookInLabelText);
        }
    }
    private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {
    }
    private void doControlButtonsChanged(PropertyChangeEvent e) {
        if(getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }
    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String s = e.getPropertyName();
                if(s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    doSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    doSelectedFilesChanged(e);
                } else if(s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
                    doDirectoryChanged(e);
                } else if(s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    doFilterChanged(e);
                } else if(s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    doFileSelectionModeChanged(e);
                } else if(s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    doAccessoryChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
                           s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    doApproveButtonTextChanged(e);
                } else if(s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    doDialogTypeChanged(e);
                } else if(s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    doApproveButtonMnemonicChanged(e);
                } else if(s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    doControlButtonsChanged(e);
                } else if (s.equals("componentOrientation")) {
                    ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                    JFileChooser cc = (JFileChooser)e.getSource();
                    if (o != e.getOldValue()) {
                        cc.applyComponentOrientation(o);
                    }
                } else if (s == "FileChooser.useShellFolder") {
                    doDirectoryChanged(e);
                } else if (s.equals("ancestor")) {
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        fileNameTextField.selectAll();
                        fileNameTextField.requestFocus();
                    }
                }
            }
        };
    }
    protected void removeControlButtons() {
        getBottomPanel().remove(getButtonPanel());
    }
    protected void addControlButtons() {
        getBottomPanel().add(getButtonPanel());
    }
    public void ensureFileIsVisible(JFileChooser fc, File f) {
        filePane.ensureFileIsVisible(fc, f);
    }
    public void rescanCurrentDirectory(JFileChooser fc) {
        filePane.rescanCurrentDirectory();
    }
    public String getFileName() {
        if (fileNameTextField != null) {
            return fileNameTextField.getText();
        } else {
            return null;
        }
    }
    public void setFileName(String filename) {
        if (fileNameTextField != null) {
            fileNameTextField.setText(filename);
        }
    }
    protected void setDirectorySelected(boolean directorySelected) {
        super.setDirectorySelected(directorySelected);
        JFileChooser chooser = getFileChooser();
        if(directorySelected) {
            if (approveButton != null) {
                approveButton.setText(directoryOpenButtonText);
                approveButton.setToolTipText(directoryOpenButtonToolTipText);
            }
        } else {
            if (approveButton != null) {
                approveButton.setText(getApproveButtonText(chooser));
                approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
            }
        }
    }
    public String getDirectoryName() {
        return null;
    }
    public void setDirectoryName(String dirname) {
    }
    protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }
    class DirectoryComboBoxRenderer extends DefaultListCellRenderer  {
        IndentIcon ii = new IndentIcon();
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                setText("");
                return this;
            }
            File directory = (File)value;
            setText(getFileChooser().getName(directory));
            Icon icon = getFileChooser().getIcon(directory);
            ii.icon = icon;
            ii.depth = directoryComboBoxModel.getDepth(index);
            setIcon(ii);
            return this;
        }
    }
    final static int space = 10;
    class IndentIcon implements Icon {
        Icon icon = null;
        int depth = 0;
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (c.getComponentOrientation().isLeftToRight()) {
                icon.paintIcon(c, g, x+depth*space, y);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }
        public int getIconWidth() {
            return icon.getIconWidth() + depth*space;
        }
        public int getIconHeight() {
            return icon.getIconHeight();
        }
    }
    protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }
    protected class DirectoryComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object> {
        Vector<File> directories = new Vector<File>();
        int[] depths = null;
        File selectedDirectory = null;
        JFileChooser chooser = getFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        public DirectoryComboBoxModel() {
            File dir = getFileChooser().getCurrentDirectory();
            if(dir != null) {
                addItem(dir);
            }
        }
        private void addItem(File directory) {
            if(directory == null) {
                return;
            }
            boolean useShellFolder = FilePane.usesShellFolder(chooser);
            directories.clear();
            File[] baseFolders;
            if (useShellFolder) {
                baseFolders = AccessController.doPrivileged(new PrivilegedAction<File[]>() {
                    public File[] run() {
                        return (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                    }
                });
            } else {
                baseFolders = fsv.getRoots();
            }
            directories.addAll(Arrays.asList(baseFolders));
            File canonical;
            try {
                canonical = ShellFolder.getNormalizedFile(directory);
            } catch (IOException e) {
                canonical = directory;
            }
            try {
                File sf = useShellFolder ? ShellFolder.getShellFolder(canonical)
                                         : canonical;
                File f = sf;
                Vector<File> path = new Vector<File>(10);
                do {
                    path.addElement(f);
                } while ((f = f.getParentFile()) != null);
                int pathCount = path.size();
                for (int i = 0; i < pathCount; i++) {
                    f = path.get(i);
                    if (directories.contains(f)) {
                        int topIndex = directories.indexOf(f);
                        for (int j = i-1; j >= 0; j--) {
                            directories.insertElementAt(path.get(j), topIndex+i-j);
                        }
                        break;
                    }
                }
                calculateDepths();
                setSelectedItem(sf);
            } catch (FileNotFoundException ex) {
                calculateDepths();
            }
        }
        private void calculateDepths() {
            depths = new int[directories.size()];
            for (int i = 0; i < depths.length; i++) {
                File dir = directories.get(i);
                File parent = dir.getParentFile();
                depths[i] = 0;
                if (parent != null) {
                    for (int j = i-1; j >= 0; j--) {
                        if (parent.equals(directories.get(j))) {
                            depths[i] = depths[j] + 1;
                            break;
                        }
                    }
                }
            }
        }
        public int getDepth(int i) {
            return (depths != null && i >= 0 && i < depths.length) ? depths[i] : 0;
        }
        public void setSelectedItem(Object selectedDirectory) {
            this.selectedDirectory = (File)selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }
        public Object getSelectedItem() {
            return selectedDirectory;
        }
        public int getSize() {
            return directories.size();
        }
        public Object getElementAt(int index) {
            return directories.elementAt(index);
        }
    }
    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }
    public class FilterComboBoxRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null && value instanceof FileFilter) {
                setText(((FileFilter)value).getDescription());
            }
            return this;
        }
    }
    protected FilterComboBoxModel createFilterComboBoxModel() {
        return new FilterComboBoxModel();
    }
    protected class FilterComboBoxModel extends AbstractListModel<Object> implements ComboBoxModel<Object>, PropertyChangeListener {
        protected FileFilter[] filters;
        protected FilterComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if(prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }
        public void setSelectedItem(Object filter) {
            if(filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                fireContentsChanged(this, -1, -1);
            }
        }
        public Object getSelectedItem() {
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if(currentFilter != null) {
                for (FileFilter filter : filters) {
                    if (filter == currentFilter) {
                        found = true;
                    }
                }
                if(found == false) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }
        public int getSize() {
            if(filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }
        public Object getElementAt(int index) {
            if(index > getSize() - 1) {
                return getFileChooser().getFileFilter();
            }
            if(filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }
    public void valueChanged(ListSelectionEvent e) {
        JFileChooser fc = getFileChooser();
        File f = fc.getSelectedFile();
        if (!e.getValueIsAdjusting() && f != null && !getFileChooser().isTraversable(f)) {
            setFileName(fileNameString(f));
        }
    }
    protected class DirectoryComboBoxAction extends AbstractAction {
        protected DirectoryComboBoxAction() {
            super("DirectoryComboBoxAction");
        }
        public void actionPerformed(ActionEvent e) {
            directoryComboBox.hidePopup();
            File f = (File)directoryComboBox.getSelectedItem();
            if (!getFileChooser().getCurrentDirectory().equals(f)) {
                getFileChooser().setCurrentDirectory(f);
            }
        }
    }
    protected JButton getApproveButton(JFileChooser fc) {
        return approveButton;
    }
    private static class ButtonAreaLayout implements LayoutManager {
        private int hGap = 5;
        private int topMargin = 17;
        public void addLayoutComponent(String string, Component comp) {
        }
        public void layoutContainer(Container container) {
            Component[] children = container.getComponents();
            if (children != null && children.length > 0) {
                int         numChildren = children.length;
                Dimension[] sizes = new Dimension[numChildren];
                Insets      insets = container.getInsets();
                int         yLocation = insets.top + topMargin;
                int         maxWidth = 0;
                for (int counter = 0; counter < numChildren; counter++) {
                    sizes[counter] = children[counter].getPreferredSize();
                    maxWidth = Math.max(maxWidth, sizes[counter].width);
                }
                int xLocation, xOffset;
                if (container.getComponentOrientation().isLeftToRight()) {
                    xLocation = container.getSize().width - insets.left - maxWidth;
                    xOffset = hGap + maxWidth;
                } else {
                    xLocation = insets.left;
                    xOffset = -(hGap + maxWidth);
                }
                for (int counter = numChildren - 1; counter >= 0; counter--) {
                    children[counter].setBounds(xLocation, yLocation,
                                                maxWidth, sizes[counter].height);
                    xLocation -= xOffset;
                }
            }
        }
        public Dimension minimumLayoutSize(Container c) {
            if (c != null) {
                Component[] children = c.getComponents();
                if (children != null && children.length > 0) {
                    int       numChildren = children.length;
                    int       height = 0;
                    Insets    cInsets = c.getInsets();
                    int       extraHeight = topMargin + cInsets.top + cInsets.bottom;
                    int       extraWidth = cInsets.left + cInsets.right;
                    int       maxWidth = 0;
                    for (int counter = 0; counter < numChildren; counter++) {
                        Dimension aSize = children[counter].getPreferredSize();
                        height = Math.max(height, aSize.height);
                        maxWidth = Math.max(maxWidth, aSize.width);
                    }
                    return new Dimension(extraWidth + numChildren * maxWidth +
                                         (numChildren - 1) * hGap,
                                         extraHeight + height);
                }
            }
            return new Dimension(0, 0);
        }
        public Dimension preferredLayoutSize(Container c) {
            return minimumLayoutSize(c);
        }
        public void removeLayoutComponent(Component c) { }
    }
    private static void groupLabels(AlignedLabel[] group) {
        for (int i = 0; i < group.length; i++) {
            group[i].group = group;
        }
    }
    private class AlignedLabel extends JLabel {
        private AlignedLabel[] group;
        private int maxWidth = 0;
        AlignedLabel() {
            super();
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
        }
        AlignedLabel(String text) {
            super(text);
            setAlignmentX(JComponent.LEFT_ALIGNMENT);
        }
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(getMaxWidth() + 11, d.height);
        }
        private int getMaxWidth() {
            if (maxWidth == 0 && group != null) {
                int max = 0;
                for (int i = 0; i < group.length; i++) {
                    max = Math.max(group[i].getSuperPreferredWidth(), max);
                }
                for (int i = 0; i < group.length; i++) {
                    group[i].maxWidth = max;
                }
            }
            return maxWidth;
        }
        private int getSuperPreferredWidth() {
            return super.getPreferredSize().width;
        }
    }
}
