public class FilePane extends JPanel implements PropertyChangeListener {
    public final static String ACTION_APPROVE_SELECTION = "approveSelection";
    public final static String ACTION_CANCEL            = "cancelSelection";
    public final static String ACTION_EDIT_FILE_NAME    = "editFileName";
    public final static String ACTION_REFRESH           = "refresh";
    public final static String ACTION_CHANGE_TO_PARENT_DIRECTORY = "Go Up";
    public final static String ACTION_NEW_FOLDER        = "New Folder";
    public final static String ACTION_VIEW_LIST         = "viewTypeList";
    public final static String ACTION_VIEW_DETAILS      = "viewTypeDetails";
    private Action[] actions;
    public  static final int VIEWTYPE_LIST     = 0;
    public  static final int VIEWTYPE_DETAILS  = 1;
    private static final int VIEWTYPE_COUNT    = 2;
    private int viewType = -1;
    private JPanel[] viewPanels = new JPanel[VIEWTYPE_COUNT];
    private JPanel currentViewPanel;
    private String[] viewTypeActionNames;
    private JPopupMenu contextMenu;
    private JMenu viewMenu;
    private String viewMenuLabelText;
    private String refreshActionLabelText;
    private String newFolderActionLabelText;
    private String kiloByteString;
    private String megaByteString;
    private String gigaByteString;
    private String renameErrorTitleText;
    private String renameErrorText;
    private String renameErrorFileExistsText;
    private static final Cursor waitCursor =
        Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
    private final KeyListener detailsKeyListener = new KeyAdapter() {
        private final long timeFactor;
        private final StringBuilder typedString = new StringBuilder();
        private long lastTime = 1000L;
        {
            Long l = (Long) UIManager.get("Table.timeFactor");
            timeFactor = (l != null) ? l : 1000L;
        }
        public void keyTyped(KeyEvent e) {
            BasicDirectoryModel model = getModel();
            int rowCount = model.getSize();
            if (detailsTable == null || rowCount == 0 ||
                    e.isAltDown() || e.isControlDown() || e.isMetaDown()) {
                return;
            }
            InputMap inputMap = detailsTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
            if (inputMap != null && inputMap.get(key) != null) {
                return;
            }
            int startIndex = detailsTable.getSelectionModel().getLeadSelectionIndex();
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (startIndex >= rowCount) {
                startIndex = rowCount - 1;
            }
            char c = e.getKeyChar();
            long time = e.getWhen();
            if (time - lastTime < timeFactor) {
                if (typedString.length() == 1 && typedString.charAt(0) == c) {
                    startIndex++;
                } else {
                    typedString.append(c);
                }
            } else {
                startIndex++;
                typedString.setLength(0);
                typedString.append(c);
            }
            lastTime = time;
            if (startIndex >= rowCount) {
                startIndex = 0;
            }
            int index = getNextMatch(startIndex, rowCount - 1);
            if (index < 0 && startIndex > 0) { 
                index = getNextMatch(0, startIndex - 1);
            }
            if (index >= 0) {
                detailsTable.getSelectionModel().setSelectionInterval(index, index);
                Rectangle cellRect = detailsTable.getCellRect(index,
                        detailsTable.convertColumnIndexToView(COLUMN_FILENAME), false);
                detailsTable.scrollRectToVisible(cellRect);
            }
        }
        private int getNextMatch(int startIndex, int finishIndex) {
            BasicDirectoryModel model = getModel();
            JFileChooser fileChooser = getFileChooser();
            DetailsTableRowSorter rowSorter = getRowSorter();
            String prefix = typedString.toString().toLowerCase();
            for (int index = startIndex; index <= finishIndex; index++) {
                File file = (File) model.getElementAt(rowSorter.convertRowIndexToModel(index));
                String fileName = fileChooser.getName(file).toLowerCase();
                if (fileName.startsWith(prefix)) {
                    return index;
                }
            }
            return -1;
        }
    };
    private FocusListener editorFocusListener = new FocusAdapter() {
        public void focusLost(FocusEvent e) {
            if (! e.isTemporary()) {
                applyEdit();
            }
        }
    };
    private static FocusListener repaintListener = new FocusListener() {
        public void focusGained(FocusEvent fe) {
            repaintSelection(fe.getSource());
        }
        public void focusLost(FocusEvent fe) {
            repaintSelection(fe.getSource());
        }
        private void repaintSelection(Object source) {
            if (source instanceof JList) {
                repaintListSelection((JList)source);
            } else if (source instanceof JTable) {
                repaintTableSelection((JTable)source);
            }
        }
        private void repaintListSelection(JList list) {
            int[] indices = list.getSelectedIndices();
            for (int i : indices) {
                Rectangle bounds = list.getCellBounds(i, i);
                list.repaint(bounds);
            }
        }
        private void repaintTableSelection(JTable table) {
            int minRow = table.getSelectionModel().getMinSelectionIndex();
            int maxRow = table.getSelectionModel().getMaxSelectionIndex();
            if (minRow == -1 || maxRow == -1) {
                return;
            }
            int col0 = table.convertColumnIndexToView(COLUMN_FILENAME);
            Rectangle first = table.getCellRect(minRow, col0, false);
            Rectangle last = table.getCellRect(maxRow, col0, false);
            Rectangle dirty = first.union(last);
            table.repaint(dirty);
        }
    };
    private boolean smallIconsView = false;
    private Border  listViewBorder;
    private Color   listViewBackground;
    private boolean listViewWindowsStyle;
    private boolean readOnly;
    private boolean fullRowSelection = false;
    private ListSelectionModel listSelectionModel;
    private JList list;
    private JTable detailsTable;
    private static final int COLUMN_FILENAME = 0;
    private File newFolderFile;
    private FileChooserUIAccessor fileChooserUIAccessor;
    private DetailsTableModel detailsTableModel;
    private DetailsTableRowSorter rowSorter;
    public FilePane(FileChooserUIAccessor fileChooserUIAccessor) {
        super(new BorderLayout());
        this.fileChooserUIAccessor = fileChooserUIAccessor;
        installDefaults();
        createActionMap();
    }
    public void uninstallUI() {
        if (getModel() != null) {
            getModel().removePropertyChangeListener(this);
        }
    }
    protected JFileChooser getFileChooser() {
        return fileChooserUIAccessor.getFileChooser();
    }
    protected BasicDirectoryModel getModel() {
        return fileChooserUIAccessor.getModel();
    }
    public int getViewType() {
        return viewType;
    }
    public void setViewType(int viewType) {
        if (viewType == this.viewType) {
            return;
        }
        int oldValue = this.viewType;
        this.viewType = viewType;
        JPanel createdViewPanel = null;
        Component newFocusOwner = null;
        switch (viewType) {
          case VIEWTYPE_LIST:
            if (viewPanels[viewType] == null) {
                createdViewPanel = fileChooserUIAccessor.createList();
                if (createdViewPanel == null) {
                    createdViewPanel = createList();
                }
                list = (JList) findChildComponent(createdViewPanel, JList.class);
                if (listSelectionModel == null) {
                    listSelectionModel = list.getSelectionModel();
                    if (detailsTable != null) {
                        detailsTable.setSelectionModel(listSelectionModel);
                    }
                } else {
                    list.setSelectionModel(listSelectionModel);
                }
            }
            list.setLayoutOrientation(JList.VERTICAL_WRAP);
            newFocusOwner = list;
            break;
          case VIEWTYPE_DETAILS:
            if (viewPanels[viewType] == null) {
                createdViewPanel = fileChooserUIAccessor.createDetailsView();
                if (createdViewPanel == null) {
                    createdViewPanel = createDetailsView();
                }
                detailsTable = (JTable) findChildComponent(createdViewPanel, JTable.class);
                detailsTable.setRowHeight(Math.max(detailsTable.getFont().getSize() + 4, 16 + 1));
                if (listSelectionModel != null) {
                    detailsTable.setSelectionModel(listSelectionModel);
                }
            }
            newFocusOwner = detailsTable;
            break;
        }
        if (createdViewPanel != null) {
            viewPanels[viewType] = createdViewPanel;
            recursivelySetInheritsPopupMenu(createdViewPanel, true);
        }
        boolean isFocusOwner = false;
        if (currentViewPanel != null) {
            Component owner = DefaultKeyboardFocusManager.
                    getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            isFocusOwner = owner == detailsTable || owner == list;
            remove(currentViewPanel);
        }
        currentViewPanel = viewPanels[viewType];
        add(currentViewPanel, BorderLayout.CENTER);
        if (isFocusOwner && newFocusOwner != null) {
            newFocusOwner.requestFocusInWindow();
        }
        revalidate();
        repaint();
        updateViewMenu();
        firePropertyChange("viewType", oldValue, viewType);
    }
    class ViewTypeAction extends AbstractAction {
        private int viewType;
        ViewTypeAction(int viewType) {
            super(viewTypeActionNames[viewType]);
            this.viewType = viewType;
            String cmd;
            switch (viewType) {
                case VIEWTYPE_LIST:    cmd = ACTION_VIEW_LIST;    break;
                case VIEWTYPE_DETAILS: cmd = ACTION_VIEW_DETAILS; break;
                default:               cmd = (String)getValue(Action.NAME);
            }
            putValue(Action.ACTION_COMMAND_KEY, cmd);
        }
        public void actionPerformed(ActionEvent e) {
            setViewType(viewType);
        }
    }
    public Action getViewTypeAction(int viewType) {
        return new ViewTypeAction(viewType);
    }
    private static void recursivelySetInheritsPopupMenu(Container container, boolean b) {
        if (container instanceof JComponent) {
            ((JComponent)container).setInheritsPopupMenu(b);
        }
        int n = container.getComponentCount();
        for (int i = 0; i < n; i++) {
            recursivelySetInheritsPopupMenu((Container)container.getComponent(i), b);
        }
    }
    protected void installDefaults() {
        Locale l = getFileChooser().getLocale();
        listViewBorder       = UIManager.getBorder("FileChooser.listViewBorder");
        listViewBackground   = UIManager.getColor("FileChooser.listViewBackground");
        listViewWindowsStyle = UIManager.getBoolean("FileChooser.listViewWindowsStyle");
        readOnly             = UIManager.getBoolean("FileChooser.readOnly");
        viewMenuLabelText =
                        UIManager.getString("FileChooser.viewMenuLabelText", l);
        refreshActionLabelText =
                        UIManager.getString("FileChooser.refreshActionLabelText", l);
        newFolderActionLabelText =
                        UIManager.getString("FileChooser.newFolderActionLabelText", l);
        viewTypeActionNames = new String[VIEWTYPE_COUNT];
        viewTypeActionNames[VIEWTYPE_LIST] =
                        UIManager.getString("FileChooser.listViewActionLabelText", l);
        viewTypeActionNames[VIEWTYPE_DETAILS] =
                        UIManager.getString("FileChooser.detailsViewActionLabelText", l);
        kiloByteString = UIManager.getString("FileChooser.fileSizeKiloBytes", l);
        megaByteString = UIManager.getString("FileChooser.fileSizeMegaBytes", l);
        gigaByteString = UIManager.getString("FileChooser.fileSizeGigaBytes", l);
        fullRowSelection = UIManager.getBoolean("FileView.fullRowSelection");
        renameErrorTitleText = UIManager.getString("FileChooser.renameErrorTitleText", l);
        renameErrorText = UIManager.getString("FileChooser.renameErrorText", l);
        renameErrorFileExistsText = UIManager.getString("FileChooser.renameErrorFileExistsText", l);
    }
    public Action[] getActions() {
        if (actions == null) {
            class FilePaneAction extends AbstractAction {
                FilePaneAction(String name) {
                    this(name, name);
                }
                FilePaneAction(String name, String cmd) {
                    super(name);
                    putValue(Action.ACTION_COMMAND_KEY, cmd);
                }
                public void actionPerformed(ActionEvent e) {
                    String cmd = (String)getValue(Action.ACTION_COMMAND_KEY);
                    if (cmd == ACTION_CANCEL) {
                        if (editFile != null) {
                           cancelEdit();
                        } else {
                           getFileChooser().cancelSelection();
                        }
                    } else if (cmd == ACTION_EDIT_FILE_NAME) {
                        JFileChooser fc = getFileChooser();
                        int index = listSelectionModel.getMinSelectionIndex();
                        if (index >= 0 && editFile == null &&
                            (!fc.isMultiSelectionEnabled() ||
                             fc.getSelectedFiles().length <= 1)) {
                            editFileName(index);
                        }
                    } else if (cmd == ACTION_REFRESH) {
                        getFileChooser().rescanCurrentDirectory();
                    }
                }
                public boolean isEnabled() {
                    String cmd = (String)getValue(Action.ACTION_COMMAND_KEY);
                    if (cmd == ACTION_CANCEL) {
                        return getFileChooser().isEnabled();
                    } else if (cmd == ACTION_EDIT_FILE_NAME) {
                        return !readOnly && getFileChooser().isEnabled();
                    } else {
                        return true;
                    }
                }
            }
            ArrayList<Action> actionList = new ArrayList<Action>(8);
            Action action;
            actionList.add(new FilePaneAction(ACTION_CANCEL));
            actionList.add(new FilePaneAction(ACTION_EDIT_FILE_NAME));
            actionList.add(new FilePaneAction(refreshActionLabelText, ACTION_REFRESH));
            action = fileChooserUIAccessor.getApproveSelectionAction();
            if (action != null) {
                actionList.add(action);
            }
            action = fileChooserUIAccessor.getChangeToParentDirectoryAction();
            if (action != null) {
                actionList.add(action);
            }
            action = getNewFolderAction();
            if (action != null) {
                actionList.add(action);
            }
            action = getViewTypeAction(VIEWTYPE_LIST);
            if (action != null) {
                actionList.add(action);
            }
            action = getViewTypeAction(VIEWTYPE_DETAILS);
            if (action != null) {
                actionList.add(action);
            }
            actions = actionList.toArray(new Action[actionList.size()]);
        }
        return actions;
    }
    protected void createActionMap() {
        addActionsToMap(super.getActionMap(), getActions());
    }
    public static void addActionsToMap(ActionMap map, Action[] actions) {
        if (map != null && actions != null) {
            for (Action a : actions) {
                String cmd = (String)a.getValue(Action.ACTION_COMMAND_KEY);
                if (cmd == null) {
                    cmd = (String)a.getValue(Action.NAME);
                }
                map.put(cmd, a);
            }
        }
    }
    private void updateListRowCount(JList list) {
        if (smallIconsView) {
            list.setVisibleRowCount(getModel().getSize() / 3);
        } else {
            list.setVisibleRowCount(-1);
        }
    }
    public JPanel createList() {
        JPanel p = new JPanel(new BorderLayout());
        final JFileChooser fileChooser = getFileChooser();
        final JList list = new JList() {
            public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
                ListModel model = getModel();
                int max = model.getSize();
                if (prefix == null || startIndex < 0 || startIndex >= max) {
                    throw new IllegalArgumentException();
                }
                boolean backwards = (bias == Position.Bias.Backward);
                for (int i = startIndex; backwards ? i >= 0 : i < max; i += (backwards ?  -1 : 1)) {
                    String filename = fileChooser.getName((File)model.getElementAt(i));
                    if (filename.regionMatches(true, 0, prefix, 0, prefix.length())) {
                        return i;
                    }
                }
                return -1;
            }
        };
        list.setCellRenderer(new FileRenderer());
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.putClientProperty("List.isFileList", Boolean.TRUE);
        if (listViewWindowsStyle) {
            list.addFocusListener(repaintListener);
        }
        updateListRowCount(list);
        getModel().addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                updateListRowCount(list);
            }
            public void intervalRemoved(ListDataEvent e) {
                updateListRowCount(list);
            }
            public void contentsChanged(ListDataEvent e) {
                if (isShowing()) {
                    clearSelection();
                }
                updateListRowCount(list);
            }
        });
        getModel().addPropertyChangeListener(this);
        if (fileChooser.isMultiSelectionEnabled()) {
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        list.setModel(new SortableListModel());
        list.addListSelectionListener(createListSelectionListener());
        list.addMouseListener(getMouseHandler());
        JScrollPane scrollpane = new JScrollPane(list);
        if (listViewBackground != null) {
            list.setBackground(listViewBackground);
        }
        if (listViewBorder != null) {
            scrollpane.setBorder(listViewBorder);
        }
        p.add(scrollpane, BorderLayout.CENTER);
        return p;
    }
    private class SortableListModel extends AbstractListModel
            implements TableModelListener, RowSorterListener {
        public SortableListModel() {
            getDetailsTableModel().addTableModelListener(this);
            getRowSorter().addRowSorterListener(this);
        }
        public int getSize() {
            return getModel().getSize();
        }
        public Object getElementAt(int index) {
            return getModel().getElementAt(getRowSorter().convertRowIndexToModel(index));
        }
        public void tableChanged(TableModelEvent e) {
            fireContentsChanged(this, 0, getSize());
        }
        public void sorterChanged(RowSorterEvent e) {
            fireContentsChanged(this, 0, getSize());
        }
    }
    private DetailsTableModel getDetailsTableModel() {
        if(detailsTableModel == null) {
            detailsTableModel = new DetailsTableModel(getFileChooser());
        }
        return detailsTableModel;
    }
    class DetailsTableModel extends AbstractTableModel implements ListDataListener {
        JFileChooser chooser;
        BasicDirectoryModel directoryModel;
        ShellFolderColumnInfo[] columns;
        int[] columnMap;
        DetailsTableModel(JFileChooser fc) {
            this.chooser = fc;
            directoryModel = getModel();
            directoryModel.addListDataListener(this);
            updateColumnInfo();
        }
        void updateColumnInfo() {
            File dir = chooser.getCurrentDirectory();
            if (dir != null && usesShellFolder(chooser)) {
                try {
                    dir = ShellFolder.getShellFolder(dir);
                } catch (FileNotFoundException e) {
                }
            }
            ShellFolderColumnInfo[] allColumns = ShellFolder.getFolderColumns(dir);
            ArrayList<ShellFolderColumnInfo> visibleColumns =
                    new ArrayList<ShellFolderColumnInfo>();
            columnMap = new int[allColumns.length];
            for (int i = 0; i < allColumns.length; i++) {
                ShellFolderColumnInfo column = allColumns[i];
                if (column.isVisible()) {
                    columnMap[visibleColumns.size()] = i;
                    visibleColumns.add(column);
                }
            }
            columns = new ShellFolderColumnInfo[visibleColumns.size()];
            visibleColumns.toArray(columns);
            columnMap = Arrays.copyOf(columnMap, columns.length);
            List<? extends RowSorter.SortKey> sortKeys =
                    (rowSorter == null) ? null : rowSorter.getSortKeys();
            fireTableStructureChanged();
            restoreSortKeys(sortKeys);
        }
        private void restoreSortKeys(List<? extends RowSorter.SortKey> sortKeys) {
            if (sortKeys != null) {
                for (int i = 0; i < sortKeys.size(); i++) {
                    RowSorter.SortKey sortKey = sortKeys.get(i);
                    if (sortKey.getColumn() >= columns.length) {
                        sortKeys = null;
                        break;
                    }
                }
                if (sortKeys != null) {
                    rowSorter.setSortKeys(sortKeys);
                }
            }
        }
        public int getRowCount() {
            return directoryModel.getSize();
        }
        public int getColumnCount() {
            return columns.length;
        }
        public Object getValueAt(int row, int col) {
            return getFileColumnValue((File)directoryModel.getElementAt(row), col);
        }
        private Object getFileColumnValue(File f, int col) {
            return (col == COLUMN_FILENAME)
                    ? f 
                    : ShellFolder.getFolderColumnValue(f, columnMap[col]);
        }
        public void setValueAt(Object value, int row, int col) {
            if (col == COLUMN_FILENAME) {
                final JFileChooser chooser = getFileChooser();
                File f = (File)getValueAt(row, col);
                if (f != null) {
                    String oldDisplayName = chooser.getName(f);
                    String oldFileName = f.getName();
                    String newDisplayName = ((String)value).trim();
                    String newFileName;
                    if (!newDisplayName.equals(oldDisplayName)) {
                        newFileName = newDisplayName;
                        int i1 = oldFileName.length();
                        int i2 = oldDisplayName.length();
                        if (i1 > i2 && oldFileName.charAt(i2) == '.') {
                            newFileName = newDisplayName + oldFileName.substring(i2);
                        }
                        FileSystemView fsv = chooser.getFileSystemView();
                        final File f2 = fsv.createFileObject(f.getParentFile(), newFileName);
                        if (f2.exists()) {
                            JOptionPane.showMessageDialog(chooser, MessageFormat.format(renameErrorFileExistsText,
                                    oldFileName), renameErrorTitleText, JOptionPane.ERROR_MESSAGE);
                        } else {
                            if (FilePane.this.getModel().renameFile(f, f2)) {
                                if (fsv.isParent(chooser.getCurrentDirectory(), f2)) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            if (chooser.isMultiSelectionEnabled()) {
                                                chooser.setSelectedFiles(new File[]{f2});
                                            } else {
                                                chooser.setSelectedFile(f2);
                                            }
                                        }
                                    });
                                } else {
                                }
                            } else {
                                JOptionPane.showMessageDialog(chooser, MessageFormat.format(renameErrorText, oldFileName),
                                        renameErrorTitleText, JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        }
        public boolean isCellEditable(int row, int column) {
            File currentDirectory = getFileChooser().getCurrentDirectory();
            return (!readOnly && column == COLUMN_FILENAME && canWrite(currentDirectory));
        }
        public void contentsChanged(ListDataEvent e) {
            new DelayedSelectionUpdater();
            fireTableDataChanged();
        }
        public void intervalAdded(ListDataEvent e) {
            int i0 = e.getIndex0();
            int i1 = e.getIndex1();
            if (i0 == i1) {
                File file = (File)getModel().getElementAt(i0);
                if (file.equals(newFolderFile)) {
                    new DelayedSelectionUpdater(file);
                    newFolderFile = null;
                }
            }
            fireTableRowsInserted(e.getIndex0(), e.getIndex1());
        }
        public void intervalRemoved(ListDataEvent e) {
            fireTableRowsDeleted(e.getIndex0(), e.getIndex1());
        }
        public ShellFolderColumnInfo[] getColumns() {
            return columns;
        }
    }
    private void updateDetailsColumnModel(JTable table) {
        if (table != null) {
            ShellFolderColumnInfo[] columns = detailsTableModel.getColumns();
            TableColumnModel columnModel = new DefaultTableColumnModel();
            for (int i = 0; i < columns.length; i++) {
                ShellFolderColumnInfo dataItem = columns[i];
                TableColumn column = new TableColumn(i);
                String title = dataItem.getTitle();
                if (title != null && title.startsWith("FileChooser.") && title.endsWith("HeaderText")) {
                    String uiTitle = UIManager.getString(title, table.getLocale());
                    if (uiTitle != null) {
                        title = uiTitle;
                    }
                }
                column.setHeaderValue(title);
                Integer width = dataItem.getWidth();
                if (width != null) {
                    column.setPreferredWidth(width);
                }
                columnModel.addColumn(column);
            }
            if (!readOnly && columnModel.getColumnCount() > COLUMN_FILENAME) {
                columnModel.getColumn(COLUMN_FILENAME).
                        setCellEditor(getDetailsTableCellEditor());
            }
            table.setColumnModel(columnModel);
        }
    }
    private DetailsTableRowSorter getRowSorter() {
        if (rowSorter == null) {
            rowSorter = new DetailsTableRowSorter();
        }
        return rowSorter;
    }
    private class DetailsTableRowSorter extends TableRowSorter<TableModel> {
        public DetailsTableRowSorter() {
            setModelWrapper(new SorterModelWrapper());
        }
        public void updateComparators(ShellFolderColumnInfo [] columns) {
            for (int i = 0; i < columns.length; i++) {
                Comparator c = columns[i].getComparator();
                if (c != null) {
                    c = new DirectoriesFirstComparatorWrapper(i, c);
                }
                setComparator(i, c);
            }
        }
        @Override
        public void sort() {
            ShellFolder.invoke(new Callable<Void>() {
                public Void call() {
                    DetailsTableRowSorter.super.sort();
                    return null;
                }
            });
        }
        public void modelStructureChanged() {
            super.modelStructureChanged();
            updateComparators(detailsTableModel.getColumns());
        }
        private class SorterModelWrapper extends ModelWrapper<TableModel, Integer> {
            public TableModel getModel() {
                return getDetailsTableModel();
            }
            public int getColumnCount() {
                return getDetailsTableModel().getColumnCount();
            }
            public int getRowCount() {
                return getDetailsTableModel().getRowCount();
            }
            public Object getValueAt(int row, int column) {
                return FilePane.this.getModel().getElementAt(row);
            }
            public Integer getIdentifier(int row) {
                return row;
            }
        }
    }
    private class DirectoriesFirstComparatorWrapper implements Comparator<File> {
        private Comparator comparator;
        private int column;
        public DirectoriesFirstComparatorWrapper(int column, Comparator comparator) {
            this.column = column;
            this.comparator = comparator;
        }
        public int compare(File f1, File f2) {
            if (f1 != null && f2 != null) {
                boolean traversable1 = getFileChooser().isTraversable(f1);
                boolean traversable2 = getFileChooser().isTraversable(f2);
                if (traversable1 && !traversable2) {
                    return -1;
                }
                if (!traversable1 && traversable2) {
                    return 1;
                }
            }
            if (detailsTableModel.getColumns()[column].isCompareByColumn()) {
                return comparator.compare(
                        getDetailsTableModel().getFileColumnValue(f1, column),
                        getDetailsTableModel().getFileColumnValue(f2, column)
                );
            }
            return comparator.compare(f1, f2);
        }
    }
    private DetailsTableCellEditor tableCellEditor;
    private DetailsTableCellEditor getDetailsTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new DetailsTableCellEditor(new JTextField());
        }
        return tableCellEditor;
    }
    private class DetailsTableCellEditor extends DefaultCellEditor {
        private final JTextField tf;
        public DetailsTableCellEditor(JTextField tf) {
            super(tf);
            this.tf = tf;
            tf.setName("Table.editor");
            tf.addFocusListener(editorFocusListener);
        }
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            Component comp = super.getTableCellEditorComponent(table, value,
                    isSelected, row, column);
            if (value instanceof File) {
                tf.setText(getFileChooser().getName((File) value));
                tf.selectAll();
            }
            return comp;
        }
    }
    class DetailsTableCellRenderer extends DefaultTableCellRenderer {
        JFileChooser chooser;
        DateFormat df;
        DetailsTableCellRenderer(JFileChooser chooser) {
            this.chooser = chooser;
            df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
                                                chooser.getLocale());
        }
        public void setBounds(int x, int y, int width, int height) {
        if (getHorizontalAlignment() == SwingConstants.LEADING &&
                    !fullRowSelection) {
                width = Math.min(width, this.getPreferredSize().width+4);
            } else {
                x -= 4;
            }
            super.setBounds(x, y, width, height);
        }
        public Insets getInsets(Insets i) {
            i = super.getInsets(i);
            i.left  += 4;
            i.right += 4;
            return i;
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                              boolean isSelected, boolean hasFocus, int row, int column) {
            if ((table.convertColumnIndexToModel(column) != COLUMN_FILENAME ||
                    (listViewWindowsStyle && !table.isFocusOwner())) &&
                    !fullRowSelection) {
                isSelected = false;
            }
            super.getTableCellRendererComponent(table, value, isSelected,
                                                       hasFocus, row, column);
            setIcon(null);
            int modelColumn = table.convertColumnIndexToModel(column);
            ShellFolderColumnInfo columnInfo = detailsTableModel.getColumns()[modelColumn];
            Integer alignment = columnInfo.getAlignment();
            if (alignment == null) {
                alignment = (value instanceof Number)
                        ? SwingConstants.RIGHT
                        : SwingConstants.LEADING;
            }
            setHorizontalAlignment(alignment);
            String text;
            if (value == null) {
                text = "";
            } else if (value instanceof File) {
                File file = (File)value;
                text = chooser.getName(file);
                Icon icon = chooser.getIcon(file);
                setIcon(icon);
            } else if (value instanceof Long) {
                long len = ((Long) value) / 1024L;
                if (listViewWindowsStyle) {
                    text = MessageFormat.format(kiloByteString, len + 1);
                } else if (len < 1024L) {
                    text = MessageFormat.format(kiloByteString, (len == 0L) ? 1L : len);
                } else {
                    len /= 1024L;
                    if (len < 1024L) {
                        text = MessageFormat.format(megaByteString, len);
                    } else {
                        len /= 1024L;
                        text = MessageFormat.format(gigaByteString, len);
                    }
                }
            } else if (value instanceof Date) {
                text = df.format((Date)value);
            } else {
                text = value.toString();
            }
            setText(text);
            return this;
        }
    }
    public JPanel createDetailsView() {
        final JFileChooser chooser = getFileChooser();
        JPanel p = new JPanel(new BorderLayout());
        final JTable detailsTable = new JTable(getDetailsTableModel()) {
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE && getCellEditor() == null) {
                    chooser.dispatchEvent(e);
                    return true;
                }
                return super.processKeyBinding(ks, e, condition, pressed);
            }
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                    updateDetailsColumnModel(this);
                }
            }
        };
        detailsTable.setRowSorter(getRowSorter());
        detailsTable.setAutoCreateColumnsFromModel(false);
        detailsTable.setComponentOrientation(chooser.getComponentOrientation());
        detailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        detailsTable.setShowGrid(false);
        detailsTable.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        detailsTable.addKeyListener(detailsKeyListener);
        Font font = list.getFont();
        detailsTable.setFont(font);
        detailsTable.setIntercellSpacing(new Dimension(0, 0));
        TableCellRenderer headerRenderer =
                new AlignableTableHeaderRenderer(detailsTable.getTableHeader().getDefaultRenderer());
        detailsTable.getTableHeader().setDefaultRenderer(headerRenderer);
        TableCellRenderer cellRenderer = new DetailsTableCellRenderer(chooser);
        detailsTable.setDefaultRenderer(Object.class, cellRenderer);
        detailsTable.getColumnModel().getSelectionModel().
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailsTable.addMouseListener(getMouseHandler());
        detailsTable.putClientProperty("Table.isFileList", Boolean.TRUE);
        if (listViewWindowsStyle) {
            detailsTable.addFocusListener(repaintListener);
        }
        ActionMap am = SwingUtilities.getUIActionMap(detailsTable);
        am.remove("selectNextRowCell");
        am.remove("selectPreviousRowCell");
        am.remove("selectNextColumnCell");
        am.remove("selectPreviousColumnCell");
        detailsTable.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                     null);
        detailsTable.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                     null);
        JScrollPane scrollpane = new JScrollPane(detailsTable);
        scrollpane.setComponentOrientation(chooser.getComponentOrientation());
        LookAndFeel.installColors(scrollpane.getViewport(), "Table.background", "Table.foreground");
        scrollpane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                JScrollPane sp = (JScrollPane)e.getComponent();
                fixNameColumnWidth(sp.getViewport().getSize().width);
                sp.removeComponentListener(this);
            }
        });
        scrollpane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                JScrollPane jsp = ((JScrollPane)e.getComponent());
                JTable table = (JTable)jsp.getViewport().getView();
                if (!e.isShiftDown() || table.getSelectionModel().getSelectionMode() == ListSelectionModel.SINGLE_SELECTION) {
                    clearSelection();
                    TableCellEditor tce = table.getCellEditor();
                    if (tce != null) {
                        tce.stopCellEditing();
                    }
                }
            }
        });
        detailsTable.setForeground(list.getForeground());
        detailsTable.setBackground(list.getBackground());
        if (listViewBorder != null) {
            scrollpane.setBorder(listViewBorder);
        }
        p.add(scrollpane, BorderLayout.CENTER);
        detailsTableModel.fireTableStructureChanged();
        return p;
    } 
    private class AlignableTableHeaderRenderer implements TableCellRenderer {
        TableCellRenderer wrappedRenderer;
        public AlignableTableHeaderRenderer(TableCellRenderer wrappedRenderer) {
            this.wrappedRenderer = wrappedRenderer;
        }
        public Component getTableCellRendererComponent(
                                JTable table, Object value, boolean isSelected,
                                boolean hasFocus, int row, int column) {
            Component c = wrappedRenderer.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);
            int modelColumn = table.convertColumnIndexToModel(column);
            ShellFolderColumnInfo columnInfo = detailsTableModel.getColumns()[modelColumn];
            Integer alignment = columnInfo.getAlignment();
            if (alignment == null) {
                alignment = SwingConstants.CENTER;
            }
            if (c instanceof JLabel) {
                ((JLabel) c).setHorizontalAlignment(alignment);
            }
            return c;
        }
    }
    private void fixNameColumnWidth(int viewWidth) {
        TableColumn nameCol = detailsTable.getColumnModel().getColumn(COLUMN_FILENAME);
        int tableWidth = detailsTable.getPreferredSize().width;
        if (tableWidth < viewWidth) {
            nameCol.setPreferredWidth(nameCol.getPreferredWidth() + viewWidth - tableWidth);
        }
    }
    private class DelayedSelectionUpdater implements Runnable {
        File editFile;
        DelayedSelectionUpdater() {
            this(null);
        }
        DelayedSelectionUpdater(File editFile) {
            this.editFile = editFile;
            if (isShowing()) {
                SwingUtilities.invokeLater(this);
            }
        }
        public void run() {
            setFileSelected();
            if (editFile != null) {
                editFileName(getRowSorter().convertRowIndexToView(
                        getModel().indexOf(editFile)));
                editFile = null;
            }
        }
    }
    public ListSelectionListener createListSelectionListener() {
        return fileChooserUIAccessor.createListSelectionListener();
    }
    int lastIndex = -1;
    File editFile = null;
    private int getEditIndex() {
        return lastIndex;
    }
    private void setEditIndex(int i) {
        lastIndex = i;
    }
    private void resetEditIndex() {
        lastIndex = -1;
    }
    private void cancelEdit() {
        if (editFile != null) {
            editFile = null;
            list.remove(editCell);
            repaint();
        } else if (detailsTable != null && detailsTable.isEditing()) {
            detailsTable.getCellEditor().cancelCellEditing();
        }
    }
    JTextField editCell = null;
    private void editFileName(int index) {
        JFileChooser chooser = getFileChooser();
        File currentDirectory = chooser.getCurrentDirectory();
        if (readOnly || !canWrite(currentDirectory)) {
            return;
        }
        ensureIndexIsVisible(index);
        switch (viewType) {
          case VIEWTYPE_LIST:
            editFile = (File)getModel().getElementAt(getRowSorter().convertRowIndexToModel(index));
            Rectangle r = list.getCellBounds(index, index);
            if (editCell == null) {
                editCell = new JTextField();
                editCell.setName("Tree.cellEditor");
                editCell.addActionListener(new EditActionListener());
                editCell.addFocusListener(editorFocusListener);
                editCell.setNextFocusableComponent(list);
            }
            list.add(editCell);
            editCell.setText(chooser.getName(editFile));
            ComponentOrientation orientation = list.getComponentOrientation();
            editCell.setComponentOrientation(orientation);
            Icon icon = chooser.getIcon(editFile);
            int editX = icon == null ? 20 : icon.getIconWidth() + 4;
            if (orientation.isLeftToRight()) {
                editCell.setBounds(editX + r.x, r.y, r.width - editX, r.height);
            } else {
                editCell.setBounds(r.x, r.y, r.width - editX, r.height);
            }
            editCell.requestFocus();
            editCell.selectAll();
            break;
          case VIEWTYPE_DETAILS:
            detailsTable.editCellAt(index, COLUMN_FILENAME);
            break;
        }
    }
    class EditActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            applyEdit();
        }
    }
    private void applyEdit() {
        if (editFile != null && editFile.exists()) {
            JFileChooser chooser = getFileChooser();
            String oldDisplayName = chooser.getName(editFile);
            String oldFileName = editFile.getName();
            String newDisplayName = editCell.getText().trim();
            String newFileName;
            if (!newDisplayName.equals(oldDisplayName)) {
                newFileName = newDisplayName;
                int i1 = oldFileName.length();
                int i2 = oldDisplayName.length();
                if (i1 > i2 && oldFileName.charAt(i2) == '.') {
                    newFileName = newDisplayName + oldFileName.substring(i2);
                }
                FileSystemView fsv = chooser.getFileSystemView();
                File f2 = fsv.createFileObject(editFile.getParentFile(), newFileName);
                if (f2.exists()) {
                    JOptionPane.showMessageDialog(chooser, MessageFormat.format(renameErrorFileExistsText, oldFileName),
                            renameErrorTitleText, JOptionPane.ERROR_MESSAGE);
                } else {
                    if (getModel().renameFile(editFile, f2)) {
                        if (fsv.isParent(chooser.getCurrentDirectory(), f2)) {
                            if (chooser.isMultiSelectionEnabled()) {
                                chooser.setSelectedFiles(new File[]{f2});
                            } else {
                                chooser.setSelectedFile(f2);
                            }
                        } else {
                        }
                    } else {
                        JOptionPane.showMessageDialog(chooser, MessageFormat.format(renameErrorText, oldFileName),
                                renameErrorTitleText, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
        if (detailsTable != null && detailsTable.isEditing()) {
            detailsTable.getCellEditor().stopCellEditing();
        }
        cancelEdit();
    }
    protected Action newFolderAction;
    public Action getNewFolderAction() {
        if (!readOnly && newFolderAction == null) {
            newFolderAction = new AbstractAction(newFolderActionLabelText) {
                private Action basicNewFolderAction;
                {
                    putValue(Action.ACTION_COMMAND_KEY, FilePane.ACTION_NEW_FOLDER);
                    File currentDirectory = getFileChooser().getCurrentDirectory();
                    if (currentDirectory != null) {
                        setEnabled(canWrite(currentDirectory));
                    }
                }
                public void actionPerformed(ActionEvent ev) {
                    if (basicNewFolderAction == null) {
                        basicNewFolderAction = fileChooserUIAccessor.getNewFolderAction();
                    }
                    JFileChooser fc = getFileChooser();
                    File oldFile = fc.getSelectedFile();
                    basicNewFolderAction.actionPerformed(ev);
                    File newFile = fc.getSelectedFile();
                    if (newFile != null && !newFile.equals(oldFile) && newFile.isDirectory()) {
                        newFolderFile = newFile;
                    }
                }
            };
        }
        return newFolderAction;
    }
    protected class FileRenderer extends DefaultListCellRenderer  {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (listViewWindowsStyle && !list.isFocusOwner()) {
                isSelected = false;
            }
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            File file = (File) value;
            String fileName = getFileChooser().getName(file);
            setText(fileName);
            setFont(list.getFont());
            Icon icon = getFileChooser().getIcon(file);
            if (icon != null) {
                setIcon(icon);
            } else {
                if (getFileChooser().getFileSystemView().isTraversable(file)) {
                    setText(fileName+File.separator);
                }
            }
            return this;
        }
    }
    void setFileSelected() {
        if (getFileChooser().isMultiSelectionEnabled() && !isDirectorySelected()) {
            File[] files = getFileChooser().getSelectedFiles(); 
            Object[] selectedObjects = list.getSelectedValues(); 
            listSelectionModel.setValueIsAdjusting(true);
            try {
                int lead = listSelectionModel.getLeadSelectionIndex();
                int anchor = listSelectionModel.getAnchorSelectionIndex();
                Arrays.sort(files);
                Arrays.sort(selectedObjects);
                int shouldIndex = 0;
                int actuallyIndex = 0;
                while (shouldIndex < files.length &&
                       actuallyIndex < selectedObjects.length) {
                    int comparison = files[shouldIndex].compareTo((File)selectedObjects[actuallyIndex]);
                    if (comparison < 0) {
                        doSelectFile(files[shouldIndex++]);
                    } else if (comparison > 0) {
                        doDeselectFile(selectedObjects[actuallyIndex++]);
                    } else {
                        shouldIndex++;
                        actuallyIndex++;
                    }
                }
                while (shouldIndex < files.length) {
                    doSelectFile(files[shouldIndex++]);
                }
                while (actuallyIndex < selectedObjects.length) {
                    doDeselectFile(selectedObjects[actuallyIndex++]);
                }
                if (listSelectionModel instanceof DefaultListSelectionModel) {
                    ((DefaultListSelectionModel)listSelectionModel).
                        moveLeadSelectionIndex(lead);
                    listSelectionModel.setAnchorSelectionIndex(anchor);
                }
            } finally {
                listSelectionModel.setValueIsAdjusting(false);
            }
        } else {
            JFileChooser chooser = getFileChooser();
            File f;
            if (isDirectorySelected()) {
                f = getDirectory();
            } else {
                f = chooser.getSelectedFile();
            }
            int i;
            if (f != null && (i = getModel().indexOf(f)) >= 0) {
                int viewIndex = getRowSorter().convertRowIndexToView(i);
                listSelectionModel.setSelectionInterval(viewIndex, viewIndex);
                ensureIndexIsVisible(viewIndex);
            } else {
                clearSelection();
            }
        }
    }
    private void doSelectFile(File fileToSelect) {
        int index = getModel().indexOf(fileToSelect);
        if (index >= 0) {
            index = getRowSorter().convertRowIndexToView(index);
            listSelectionModel.addSelectionInterval(index, index);
        }
    }
    private void doDeselectFile(Object fileToDeselect) {
        int index = getRowSorter().convertRowIndexToView(
                                getModel().indexOf(fileToDeselect));
        listSelectionModel.removeSelectionInterval(index, index);
    }
    private void doSelectedFileChanged(PropertyChangeEvent e) {
        applyEdit();
        File f = (File) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (f != null
            && ((fc.isFileSelectionEnabled() && !f.isDirectory())
                || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {
            setFileSelected();
        }
    }
    private void doSelectedFilesChanged(PropertyChangeEvent e) {
        applyEdit();
        File[] files = (File[]) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (files != null
            && files.length > 0
            && (files.length > 1 || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
            setFileSelected();
        }
    }
    private void doDirectoryChanged(PropertyChangeEvent e) {
        getDetailsTableModel().updateColumnInfo();
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();
        applyEdit();
        resetEditIndex();
        ensureIndexIsVisible(0);
        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null) {
            if (!readOnly) {
                getNewFolderAction().setEnabled(canWrite(currentDirectory));
            }
            fileChooserUIAccessor.getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(currentDirectory));
        }
        if (list != null) {
            list.clearSelection();
        }
    }
    private void doFilterChanged(PropertyChangeEvent e) {
        applyEdit();
        resetEditIndex();
        clearSelection();
    }
    private void doFileSelectionModeChanged(PropertyChangeEvent e) {
        applyEdit();
        resetEditIndex();
        clearSelection();
    }
    private void doMultiSelectionChanged(PropertyChangeEvent e) {
        if (getFileChooser().isMultiSelectionEnabled()) {
            listSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        } else {
            listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            clearSelection();
            getFileChooser().setSelectedFiles(null);
        }
    }
    public void propertyChange(PropertyChangeEvent e) {
            if (viewType == -1) {
                setViewType(VIEWTYPE_LIST);
            }
        String s = e.getPropertyName();
        if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            doSelectedFileChanged(e);
        } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
            doSelectedFilesChanged(e);
        } else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
            doDirectoryChanged(e);
        } else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
            doFilterChanged(e);
        } else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
            doFileSelectionModeChanged(e);
        } else if (s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
            doMultiSelectionChanged(e);
        } else if (s.equals(JFileChooser.CANCEL_SELECTION)) {
            applyEdit();
        } else if (s.equals("busy")) {
            setCursor((Boolean)e.getNewValue() ? waitCursor : null);
        } else if (s.equals("componentOrientation")) {
            ComponentOrientation o = (ComponentOrientation)e.getNewValue();
            JFileChooser cc = (JFileChooser)e.getSource();
            if (o != e.getOldValue()) {
                cc.applyComponentOrientation(o);
            }
            if (detailsTable != null) {
                detailsTable.setComponentOrientation(o);
                detailsTable.getParent().getParent().setComponentOrientation(o);
            }
        }
    }
    private void ensureIndexIsVisible(int i) {
        if (i >= 0) {
            if (list != null) {
                list.ensureIndexIsVisible(i);
            }
            if (detailsTable != null) {
                detailsTable.scrollRectToVisible(detailsTable.getCellRect(i, COLUMN_FILENAME, true));
            }
        }
    }
    public void ensureFileIsVisible(JFileChooser fc, File f) {
        int modelIndex = getModel().indexOf(f);
        if (modelIndex >= 0) {
            ensureIndexIsVisible(getRowSorter().convertRowIndexToView(modelIndex));
        }
    }
    public void rescanCurrentDirectory() {
        getModel().validateFileCache();
    }
    public void clearSelection() {
        if (listSelectionModel != null) {
            listSelectionModel.clearSelection();
            if (listSelectionModel instanceof DefaultListSelectionModel) {
                ((DefaultListSelectionModel)listSelectionModel).moveLeadSelectionIndex(0);
                listSelectionModel.setAnchorSelectionIndex(0);
            }
        }
    }
    public JMenu getViewMenu() {
        if (viewMenu == null) {
            viewMenu = new JMenu(viewMenuLabelText);
            ButtonGroup viewButtonGroup = new ButtonGroup();
            for (int i = 0; i < VIEWTYPE_COUNT; i++) {
                JRadioButtonMenuItem mi =
                    new JRadioButtonMenuItem(new ViewTypeAction(i));
                viewButtonGroup.add(mi);
                viewMenu.add(mi);
            }
            updateViewMenu();
        }
        return viewMenu;
    }
    private void updateViewMenu() {
        if (viewMenu != null) {
            Component[] comps = viewMenu.getMenuComponents();
            for (Component comp : comps) {
                if (comp instanceof JRadioButtonMenuItem) {
                    JRadioButtonMenuItem mi = (JRadioButtonMenuItem) comp;
                    if (((ViewTypeAction)mi.getAction()).viewType == viewType) {
                        mi.setSelected(true);
                    }
                }
            }
        }
    }
    public JPopupMenu getComponentPopupMenu() {
        JPopupMenu popupMenu = getFileChooser().getComponentPopupMenu();
        if (popupMenu != null) {
            return popupMenu;
        }
        JMenu viewMenu = getViewMenu();
        if (contextMenu == null) {
            contextMenu = new JPopupMenu();
            if (viewMenu != null) {
                contextMenu.add(viewMenu);
                if (listViewWindowsStyle) {
                    contextMenu.addSeparator();
                }
            }
            ActionMap actionMap = getActionMap();
            Action refreshAction   = actionMap.get(ACTION_REFRESH);
            Action newFolderAction = actionMap.get(ACTION_NEW_FOLDER);
            if (refreshAction != null) {
                contextMenu.add(refreshAction);
                if (listViewWindowsStyle && newFolderAction != null) {
                    contextMenu.addSeparator();
                }
            }
            if (newFolderAction != null) {
                contextMenu.add(newFolderAction);
            }
        }
        if (viewMenu != null) {
            viewMenu.getPopupMenu().setInvoker(viewMenu);
        }
        return contextMenu;
    }
    private Handler handler;
    protected Handler getMouseHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    private class Handler implements MouseListener {
        private MouseListener doubleClickListener;
        public void mouseClicked(MouseEvent evt) {
            JComponent source = (JComponent)evt.getSource();
            int index;
            if (source instanceof JList) {
                index = SwingUtilities2.loc2IndexFileList(list, evt.getPoint());
            } else if (source instanceof JTable) {
                JTable table = (JTable)source;
                Point p = evt.getPoint();
                index = table.rowAtPoint(p);
                boolean pointOutsidePrefSize =
                        SwingUtilities2.pointOutsidePrefSize(
                            table, index, table.columnAtPoint(p), p);
                if (pointOutsidePrefSize && !fullRowSelection) {
                    return;
                }
                if (index >= 0 && list != null &&
                    listSelectionModel.isSelectedIndex(index)) {
                    Rectangle r = list.getCellBounds(index, index);
                    evt = new MouseEvent(list, evt.getID(),
                                         evt.getWhen(), evt.getModifiers(),
                                         r.x + 1, r.y + r.height/2,
                                         evt.getXOnScreen(),
                                         evt.getYOnScreen(),
                                         evt.getClickCount(), evt.isPopupTrigger(),
                                         evt.getButton());
                }
            } else {
                return;
            }
            if (index >= 0 && SwingUtilities.isLeftMouseButton(evt)) {
                JFileChooser fc = getFileChooser();
                if (evt.getClickCount() == 1 && source instanceof JList) {
                    if ((!fc.isMultiSelectionEnabled() || fc.getSelectedFiles().length <= 1)
                        && index >= 0 && listSelectionModel.isSelectedIndex(index)
                        && getEditIndex() == index && editFile == null) {
                        editFileName(index);
                    } else {
                        if (index >= 0) {
                            setEditIndex(index);
                        } else {
                            resetEditIndex();
                        }
                    }
                } else if (evt.getClickCount() == 2) {
                    resetEditIndex();
                }
            }
            if (getDoubleClickListener() != null) {
                getDoubleClickListener().mouseClicked(evt);
            }
        }
        public void mouseEntered(MouseEvent evt) {
            JComponent source = (JComponent)evt.getSource();
            if (source instanceof JTable) {
                JTable table = (JTable)evt.getSource();
                TransferHandler th1 = getFileChooser().getTransferHandler();
                TransferHandler th2 = table.getTransferHandler();
                if (th1 != th2) {
                    table.setTransferHandler(th1);
                }
                boolean dragEnabled = getFileChooser().getDragEnabled();
                if (dragEnabled != table.getDragEnabled()) {
                    table.setDragEnabled(dragEnabled);
                }
            } else if (source instanceof JList) {
                if (getDoubleClickListener() != null) {
                    getDoubleClickListener().mouseEntered(evt);
                }
            }
        }
        public void mouseExited(MouseEvent evt) {
            if (evt.getSource() instanceof JList) {
                if (getDoubleClickListener() != null) {
                    getDoubleClickListener().mouseExited(evt);
                }
            }
        }
        public void mousePressed(MouseEvent evt) {
            if (evt.getSource() instanceof JList) {
                if (getDoubleClickListener() != null) {
                    getDoubleClickListener().mousePressed(evt);
                }
            }
        }
        public void mouseReleased(MouseEvent evt) {
            if (evt.getSource() instanceof JList) {
                if (getDoubleClickListener() != null) {
                    getDoubleClickListener().mouseReleased(evt);
                }
            }
        }
        private MouseListener getDoubleClickListener() {
            if (doubleClickListener == null && list != null) {
                doubleClickListener =
                    fileChooserUIAccessor.createDoubleClickListener(list);
            }
            return doubleClickListener;
        }
    }
    protected boolean isDirectorySelected() {
        return fileChooserUIAccessor.isDirectorySelected();
    }
    protected File getDirectory() {
        return fileChooserUIAccessor.getDirectory();
    }
    private Component findChildComponent(Container container, Class cls) {
        int n = container.getComponentCount();
        for (int i = 0; i < n; i++) {
            Component comp = container.getComponent(i);
            if (cls.isInstance(comp)) {
                return comp;
            } else if (comp instanceof Container) {
                Component c = findChildComponent((Container)comp, cls);
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }
    public boolean canWrite(File f) {
        if (!f.exists()) {
            return false;
        }
        if (f instanceof ShellFolder) {
            return ((ShellFolder) f).isFileSystem();
        } else {
            if (usesShellFolder(getFileChooser())) {
                try {
                    return ShellFolder.getShellFolder(f).isFileSystem();
                } catch (FileNotFoundException ex) {
                    return false;
                }
            } else {
                return true;
            }
        }
    }
    public static boolean usesShellFolder(JFileChooser chooser) {
        Boolean prop = (Boolean) chooser.getClientProperty("FileChooser.useShellFolder");
        return prop == null ? chooser.getFileSystemView().equals(FileSystemView.getFileSystemView())
                : prop.booleanValue();
    }
    public interface FileChooserUIAccessor {
        public JFileChooser getFileChooser();
        public BasicDirectoryModel getModel();
        public JPanel createList();
        public JPanel createDetailsView();
        public boolean isDirectorySelected();
        public File getDirectory();
        public Action getApproveSelectionAction();
        public Action getChangeToParentDirectoryAction();
        public Action getNewFolderAction();
        public MouseListener createDoubleClickListener(JList list);
        public ListSelectionListener createListSelectionListener();
    }
}
