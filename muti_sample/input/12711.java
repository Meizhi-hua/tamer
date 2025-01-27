public class XMBeanAttributes extends XTable {
    final Logger LOGGER =
            Logger.getLogger(XMBeanAttributes.class.getPackage().getName());
    private final static String[] columnNames =
    {Resources.getText("Name"),
     Resources.getText("Value")};
    private XMBean mbean;
    private MBeanInfo mbeanInfo;
    private MBeanAttributeInfo[] attributesInfo;
    private HashMap<String, Object> attributes;
    private HashMap<String, Object> unavailableAttributes;
    private HashMap<String, Object> viewableAttributes;
    private WeakHashMap<XMBean, HashMap<String, ZoomedCell>> viewersCache =
            new WeakHashMap<XMBean, HashMap<String, ZoomedCell>>();
    private final TableModelListener attributesListener;
    private MBeansTab mbeansTab;
    private TableCellEditor valueCellEditor = new ValueCellEditor();
    private int rowMinHeight = -1;
    private AttributesMouseListener mouseListener = new AttributesMouseListener();
    private static TableCellEditor editor =
            new Utils.ReadOnlyTableCellEditor(new JTextField());
    public XMBeanAttributes(MBeansTab mbeansTab) {
        super();
        this.mbeansTab = mbeansTab;
        ((DefaultTableModel)getModel()).setColumnIdentifiers(columnNames);
        attributesListener = new AttributesListener(this);
        getModel().addTableModelListener(attributesListener);
        getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(40);
        addMouseListener(mouseListener);
        getTableHeader().setReorderingAllowed(false);
        setColumnEditors();
        addKeyListener(new Utils.CopyKeyAdapter());
    }
    @Override
    public synchronized Component prepareRenderer(TableCellRenderer renderer,
                                                  int row, int column) {
        if(row >= getRowCount())
            return null;
        else
            return super.prepareRenderer(renderer, row, column);
    }
    void updateRowHeight(Object obj, int row) {
        ZoomedCell cell = null;
        if(obj instanceof ZoomedCell) {
            cell = (ZoomedCell) obj;
            if(cell.isInited())
                setRowHeight(row, cell.getHeight());
            else
                if(rowMinHeight != - 1)
                    setRowHeight(row, rowMinHeight);
        } else
            if(rowMinHeight != - 1)
                setRowHeight(row, rowMinHeight);
    }
    @Override
    public synchronized TableCellRenderer getCellRenderer(int row,
            int column) {
        if (row >= getRowCount()) {
            return null;
        } else {
            if (column == VALUE_COLUMN) {
                Object obj = getModel().getValueAt(row, column);
                if (obj instanceof ZoomedCell) {
                    ZoomedCell cell = (ZoomedCell) obj;
                    if (cell.isInited()) {
                        DefaultTableCellRenderer renderer =
                                (DefaultTableCellRenderer) cell.getRenderer();
                        renderer.setToolTipText(getToolTip(row,column));
                        return renderer;
                    }
                }
            }
            DefaultTableCellRenderer renderer = (DefaultTableCellRenderer)
                super.getCellRenderer(row, column);
            if (!isCellError(row, column)) {
                if (!(isColumnEditable(column) && isWritable(row) &&
                      Utils.isEditableType(getClassName(row)))) {
                    renderer.setForeground(getDefaultColor());
                }
            }
            return renderer;
        }
    }
    private void setColumnEditors() {
        TableColumnModel tcm = getColumnModel();
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tc = tcm.getColumn(i);
            if (isColumnEditable(i)) {
                tc.setCellEditor(valueCellEditor);
            } else {
                tc.setCellEditor(editor);
            }
        }
    }
    public void cancelCellEditing() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Cancel Editing Row: "+getEditingRow());
        }
        final TableCellEditor tableCellEditor = getCellEditor();
        if (tableCellEditor != null) {
            tableCellEditor.cancelCellEditing();
        }
    }
    public void stopCellEditing() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Stop Editing Row: "+getEditingRow());
        }
        final TableCellEditor tableCellEditor = getCellEditor();
        if (tableCellEditor != null) {
            tableCellEditor.stopCellEditing();
        }
    }
    @Override
    public final boolean editCellAt(final int row, final int column, EventObject e) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("editCellAt(row="+row+", col="+column+
                    ", e="+e+")");
        }
        if (JConsole.isDebug()) {
            System.err.println("edit: "+getValueName(row)+"="+getValue(row));
        }
        boolean retVal = super.editCellAt(row, column, e);
        if (retVal) {
            final TableCellEditor tableCellEditor =
                    getColumnModel().getColumn(column).getCellEditor();
            if (tableCellEditor == valueCellEditor) {
                ((JComponent) tableCellEditor).requestFocus();
            }
        }
        return retVal;
    }
    @Override
    public boolean isCellEditable(int row, int col) {
        if (!isColumnEditable(col)) {
            return true;
        }
        Object obj = getModel().getValueAt(row, col);
        if (obj instanceof ZoomedCell) {
            ZoomedCell cell = (ZoomedCell) obj;
            return cell.isMaximized();
        }
        return true;
    }
    @Override
    public void setValueAt(Object value, int row, int column) {
        if (!isCellError(row, column) && isColumnEditable(column) &&
            isWritable(row) && Utils.isEditableType(getClassName(row))) {
            if (JConsole.isDebug()) {
                System.err.println("validating [row="+row+", column="+column+
                        "]: "+getValueName(row)+"="+value);
            }
            super.setValueAt(value, row, column);
        }
    }
    public boolean isTableEditable() {
        return true;
    }
    public void setTableValue(Object value, int row) {
    }
    public boolean isColumnEditable(int column) {
        if (column < getColumnCount()) {
            return getColumnName(column).equals(Resources.getText("Value"));
        }
        else {
            return false;
        }
    }
    public String getClassName(int row) {
        int index = convertRowToIndex(row);
        if (index != -1) {
            return attributesInfo[index].getType();
        }
        else {
            return null;
        }
    }
    public String getValueName(int row) {
        int index = convertRowToIndex(row);
        if (index != -1) {
            return attributesInfo[index].getName();
        }
        else {
            return null;
        }
    }
    public Object getValue(int row) {
        final Object val = ((DefaultTableModel) getModel())
                .getValueAt(row, VALUE_COLUMN);
        return val;
    }
    @Override
    public String getToolTip(int row, int column) {
        if (isCellError(row, column)) {
            return (String) unavailableAttributes.get(getValueName(row));
        }
        if (isColumnEditable(column)) {
            Object value = getValue(row);
            String tip = null;
            if (value != null) {
                tip = value.toString();
                if(isAttributeViewable(row, VALUE_COLUMN))
                    tip = Resources.getText("Double click to expand/collapse")+
                        ". " + tip;
            }
            return tip;
        }
        if(column == NAME_COLUMN) {
            int index = convertRowToIndex(row);
            if (index != -1) {
                return attributesInfo[index].getDescription();
            }
        }
        return null;
    }
    public synchronized boolean isWritable(int row) {
        int index = convertRowToIndex(row);
        if (index != -1) {
            return (attributesInfo[index].isWritable());
        }
        else {
            return false;
        }
    }
    @Override
    public synchronized int getRowCount() {
        return super.getRowCount();
    }
    public synchronized boolean isReadable(int row) {
        int index = convertRowToIndex(row);
        if (index != -1) {
            return (attributesInfo[index].isReadable());
        }
        else {
            return false;
        }
    }
    public synchronized boolean isCellError(int row, int col) {
        return (isColumnEditable(col) &&
                (unavailableAttributes.containsKey(getValueName(row))));
    }
    public synchronized boolean isAttributeViewable(int row, int col) {
        boolean isViewable = false;
        if(col == VALUE_COLUMN) {
            Object obj = getModel().getValueAt(row, col);
            if(obj instanceof ZoomedCell)
                isViewable = true;
        }
        return isViewable;
    }
    public void loadAttributes(final XMBean mbean, final MBeanInfo mbeanInfo) {
        final SwingWorker<Runnable,Void> load =
                new SwingWorker<Runnable,Void>() {
            @Override
            protected Runnable doInBackground() throws Exception {
                return doLoadAttributes(mbean,mbeanInfo);
            }
            @Override
            protected void done() {
                try {
                    final Runnable updateUI = get();
                    if (updateUI != null) updateUI.run();
                } catch (RuntimeException x) {
                    throw x;
                } catch (ExecutionException x) {
                    if(JConsole.isDebug()) {
                       System.err.println(
                               "Exception raised while loading attributes: "
                               +x.getCause());
                       x.printStackTrace();
                    }
                } catch (InterruptedException x) {
                    if(JConsole.isDebug()) {
                       System.err.println(
                            "Interrupted while loading attributes: "+x);
                       x.printStackTrace();
                    }
                }
            }
        };
        mbeansTab.workerAdd(load);
    }
    private Runnable doLoadAttributes(final XMBean mbean, MBeanInfo infoOrNull)
        throws JMException, IOException {
        if(mbean == null) return null;
        final MBeanInfo curMBeanInfo =
                (infoOrNull==null)?mbean.getMBeanInfo():infoOrNull;
        final MBeanAttributeInfo[] attrsInfo = curMBeanInfo.getAttributes();
        final HashMap<String, Object> attrs =
            new HashMap<String, Object>(attrsInfo.length);
        final HashMap<String, Object> unavailableAttrs =
            new HashMap<String, Object>(attrsInfo.length);
        final HashMap<String, Object> viewableAttrs =
            new HashMap<String, Object>(attrsInfo.length);
        AttributeList list = null;
        try {
            list = mbean.getAttributes(attrsInfo);
        }catch(Exception e) {
            if (JConsole.isDebug()) {
                System.err.println("Error calling getAttributes() on MBean \"" +
                                   mbean.getObjectName() + "\". JConsole will " +
                                   "try to get them individually calling " +
                                   "getAttribute() instead. Exception:");
                e.printStackTrace(System.err);
            }
            list = new AttributeList();
            for(int i = 0; i < attrsInfo.length; i++) {
                String name = null;
                try {
                    name = attrsInfo[i].getName();
                    Object value =
                        mbean.getMBeanServerConnection().
                        getAttribute(mbean.getObjectName(), name);
                    list.add(new Attribute(name, value));
                }catch(Exception ex) {
                    if(attrsInfo[i].isReadable()) {
                        unavailableAttrs.put(name,
                                Utils.getActualException(ex).toString());
                    }
                }
            }
        }
        try {
            int att_length = list.size();
            for (int i=0;i<att_length;i++) {
                Attribute attribute = (Attribute) list.get(i);
                if(isViewable(attribute)) {
                    viewableAttrs.put(attribute.getName(),
                                           attribute.getValue());
                }
                else
                    attrs.put(attribute.getName(),attribute.getValue());
            }
            if (att_length < attrsInfo.length) {
                for (int i=0;i<attrsInfo.length;i++) {
                    MBeanAttributeInfo attributeInfo = attrsInfo[i];
                    if (!attrs.containsKey(attributeInfo.getName()) &&
                        !viewableAttrs.containsKey(attributeInfo.
                                                        getName()) &&
                        !unavailableAttrs.containsKey(attributeInfo.
                                                           getName())) {
                        if (attributeInfo.isReadable()) {
                            try {
                                Object v =
                                    mbean.getMBeanServerConnection().getAttribute(
                                    mbean.getObjectName(), attributeInfo.getName());
                                attrs.put(attributeInfo.getName(),
                                               v);
                            }catch(Exception e) {
                                unavailableAttrs.put(attributeInfo.getName(),
                                        Utils.getActualException(e).toString());
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            for (int i=0;i<attrsInfo.length;i++) {
                MBeanAttributeInfo attributeInfo = attrsInfo[i];
                if (attributeInfo.isReadable()) {
                    unavailableAttrs.put(attributeInfo.getName(),
                                              Utils.getActualException(e).
                                              toString());
                }
            }
        }
        return new Runnable() {
            public void run() {
                synchronized (XMBeanAttributes.this) {
                    XMBeanAttributes.this.mbean = mbean;
                    XMBeanAttributes.this.mbeanInfo = curMBeanInfo;
                    XMBeanAttributes.this.attributesInfo = attrsInfo;
                    XMBeanAttributes.this.attributes = attrs;
                    XMBeanAttributes.this.unavailableAttributes = unavailableAttrs;
                    XMBeanAttributes.this.viewableAttributes = viewableAttrs;
                    DefaultTableModel tableModel =
                            (DefaultTableModel) getModel();
                    emptyTable(tableModel);
                    addTableData(tableModel,
                            mbean,
                            attrsInfo,
                            attrs,
                            unavailableAttrs,
                            viewableAttrs);
                    tableModel.newDataAvailable(new TableModelEvent(tableModel));
                    tableModel.addTableModelListener(attributesListener);
                }
            }
        };
    }
    void collapse(String attributeName, final Component c) {
        final int row = getSelectedRow();
        Object obj = getModel().getValueAt(row, VALUE_COLUMN);
        if(obj instanceof ZoomedCell) {
            cancelCellEditing();
            ZoomedCell cell = (ZoomedCell) obj;
            cell.reset();
            setRowHeight(row,
                         cell.getHeight());
            editCellAt(row,
                       VALUE_COLUMN);
            invalidate();
            repaint();
        }
    }
    ZoomedCell updateZoomedCell(int row,
                                int col) {
        Object obj = getModel().getValueAt(row, VALUE_COLUMN);
        ZoomedCell cell = null;
        if(obj instanceof ZoomedCell) {
            cell = (ZoomedCell) obj;
            if(!cell.isInited()) {
                Object elem = cell.getValue();
                String attributeName =
                    (String) getModel().getValueAt(row,
                                                   NAME_COLUMN);
                Component comp = mbeansTab.getDataViewer().
                        createAttributeViewer(elem, mbean, attributeName, this);
                if(comp != null){
                    if(rowMinHeight == -1)
                        rowMinHeight = getRowHeight(row);
                    cell.init(super.getCellRenderer(row, col),
                              comp,
                              rowMinHeight);
                    mbeansTab.getDataViewer().registerForMouseEvent(
                            comp, mouseListener);
                } else
                    return cell;
            }
            cell.switchState();
            setRowHeight(row,
                         cell.getHeight());
            if(!cell.isMaximized()) {
                cancelCellEditing();
                editCellAt(row,
                           VALUE_COLUMN);
            }
            invalidate();
            repaint();
        }
        return cell;
    }
    public void refreshAttributes() {
         refreshAttributes(true);
    }
    private void refreshAttributes(final boolean stopCellEditing) {
         SwingWorker<Void,Void> sw = new SwingWorker<Void,Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SnapshotMBeanServerConnection mbsc =
                mbeansTab.getSnapshotMBeanServerConnection();
                mbsc.flush();
                return null;
            }
            @Override
            protected void done() {
                try {
                    get();
                    if (stopCellEditing) stopCellEditing();
                    loadAttributes(mbean, mbeanInfo);
                } catch (Exception x) {
                    if (JConsole.isDebug()) {
                        x.printStackTrace();
                    }
                }
            }
         };
         mbeansTab.workerAdd(sw);
     }
    @Override
    public void columnMarginChanged(ChangeEvent e) {
        if (isEditing()) stopCellEditing();
        super.columnMarginChanged(e);
    }
    @Override
    void sortRequested(int column) {
        if (isEditing()) stopCellEditing();
        super.sortRequested(column);
    }
    @Override
    public synchronized void emptyTable() {
         emptyTable((DefaultTableModel)getModel());
     }
    private void emptyTable(DefaultTableModel model) {
         model.removeTableModelListener(attributesListener);
         super.emptyTable();
    }
    private boolean isViewable(Attribute attribute) {
        Object data = attribute.getValue();
        return XDataViewer.isViewableValue(data);
    }
    synchronized void removeAttributes() {
        if (attributes != null) {
            attributes.clear();
        }
        if (unavailableAttributes != null) {
            unavailableAttributes.clear();
        }
        if (viewableAttributes != null) {
            viewableAttributes.clear();
        }
        mbean = null;
    }
    private ZoomedCell getZoomedCell(XMBean mbean, String attribute, Object value) {
        synchronized (viewersCache) {
            HashMap<String, ZoomedCell> viewers;
            if (viewersCache.containsKey(mbean)) {
                viewers = viewersCache.get(mbean);
            } else {
                viewers = new HashMap<String, ZoomedCell>();
            }
            ZoomedCell cell;
            if (viewers.containsKey(attribute)) {
                cell = viewers.get(attribute);
                cell.setValue(value);
                if (cell.isMaximized() && cell.getType() != XDataViewer.NUMERIC) {
                    Component comp =
                        mbeansTab.getDataViewer().createAttributeViewer(
                            value, mbean, attribute, XMBeanAttributes.this);
                    cell.init(cell.getMinRenderer(), comp, cell.getMinHeight());
                    mbeansTab.getDataViewer().registerForMouseEvent(comp, mouseListener);
                }
            } else {
                cell = new ZoomedCell(value);
                viewers.put(attribute, cell);
            }
            viewersCache.put(mbean, viewers);
            return cell;
        }
    }
    protected void addTableData(DefaultTableModel tableModel,
                                XMBean mbean,
                                MBeanAttributeInfo[] attributesInfo,
                                HashMap<String, Object> attributes,
                                HashMap<String, Object> unavailableAttributes,
                                HashMap<String, Object> viewableAttributes) {
        Object rowData[] = new Object[2];
        int col1Width = 0;
        int col2Width = 0;
        for (int i = 0; i < attributesInfo.length; i++) {
            rowData[0] = (attributesInfo[i].getName());
            if (unavailableAttributes.containsKey(rowData[0])) {
                rowData[1] = Resources.getText("Unavailable");
            } else if (viewableAttributes.containsKey(rowData[0])) {
                rowData[1] = viewableAttributes.get(rowData[0]);
                if (!attributesInfo[i].isWritable() ||
                    !Utils.isEditableType(attributesInfo[i].getType())) {
                    rowData[1] = getZoomedCell(mbean, (String) rowData[0], rowData[1]);
                }
            } else {
                rowData[1] = attributes.get(rowData[0]);
            }
            tableModel.addRow(rowData);
            String str = null;
            if(rowData[0] != null) {
                str = rowData[0].toString();
                if(str.length() > col1Width)
                    col1Width = str.length();
            }
            if(rowData[1] != null) {
                str = rowData[1].toString();
                if(str.length() > col2Width)
                    col2Width = str.length();
            }
        }
        updateColumnWidth(col1Width, col2Width);
    }
    private void updateColumnWidth(int col1Width, int col2Width) {
        TableColumnModel colModel = getColumnModel();
        col1Width = col1Width * 7;
        col2Width = col2Width * 7;
        if(col1Width + col2Width <
           (int) getPreferredScrollableViewportSize().getWidth())
            col2Width = (int) getPreferredScrollableViewportSize().getWidth()
                - col1Width;
        colModel.getColumn(NAME_COLUMN).setPreferredWidth(50);
    }
    class AttributesMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                if(e.getClickCount() >= 2) {
                    int row = XMBeanAttributes.this.getSelectedRow();
                    int col = XMBeanAttributes.this.getSelectedColumn();
                    if(col != VALUE_COLUMN) return;
                    if(col == -1 || row == -1) return;
                    XMBeanAttributes.this.updateZoomedCell(row, col);
                }
            }
        }
    }
    @SuppressWarnings("serial")
    class ValueCellEditor extends XTextFieldEditor {
        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column) {
            Object val = value;
            if(column == VALUE_COLUMN) {
                Object obj = getModel().getValueAt(row,
                                                   column);
                if(obj instanceof ZoomedCell) {
                    ZoomedCell cell = (ZoomedCell) obj;
                    if(cell.getRenderer() instanceof MaximizedCellRenderer) {
                        MaximizedCellRenderer zr =
                            (MaximizedCellRenderer) cell.getRenderer();
                        return zr.getComponent();
                    }
                } else {
                    Component comp = super.getTableCellEditorComponent(
                            table, val, isSelected, row, column);
                    if (isCellError(row, column) ||
                        !isWritable(row) ||
                        !Utils.isEditableType(getClassName(row))) {
                        textField.setEditable(false);
                    }
                    return comp;
                }
            }
            return super.getTableCellEditorComponent(table,
                                                     val,
                                                     isSelected,
                                                     row,
                                                     column);
        }
        @Override
        public boolean stopCellEditing() {
            int editingRow = getEditingRow();
            int editingColumn = getEditingColumn();
            if (editingColumn == VALUE_COLUMN) {
                Object obj = getModel().getValueAt(editingRow, editingColumn);
                if (obj instanceof ZoomedCell) {
                    ZoomedCell cell = (ZoomedCell) obj;
                    if (cell.isMaximized()) {
                        this.cancelCellEditing();
                        return true;
                    }
                }
            }
            return super.stopCellEditing();
        }
    }
    @SuppressWarnings("serial")
    class MaximizedCellRenderer extends  DefaultTableCellRenderer {
        Component comp;
        MaximizedCellRenderer(Component comp) {
            this.comp = comp;
            Dimension d = comp.getPreferredSize();
            if (d.getHeight() > 220) {
                comp.setPreferredSize(new Dimension((int) d.getWidth(), 220));
            }
        }
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            return comp;
        }
        public Component getComponent() {
            return comp;
        }
    }
    class ZoomedCell {
        TableCellRenderer minRenderer;
        MaximizedCellRenderer maxRenderer;
        int minHeight;
        boolean minimized = true;
        boolean init = false;
        int type;
        Object value;
        ZoomedCell(Object value) {
            type = XDataViewer.getViewerType(value);
            this.value = value;
        }
        boolean isInited() {
            return init;
        }
        Object getValue() {
            return value;
        }
        void setValue(Object value) {
            this.value = value;
        }
        void init(TableCellRenderer minRenderer,
                  Component maxComponent,
                  int minHeight) {
            this.minRenderer = minRenderer;
            this.maxRenderer = new MaximizedCellRenderer(maxComponent);
            this.minHeight = minHeight;
            init = true;
        }
        int getType() {
            return type;
        }
        void reset() {
            init = false;
            minimized = true;
        }
        void switchState() {
            minimized = !minimized;
        }
        boolean isMaximized() {
            return !minimized;
        }
        void minimize() {
            minimized = true;
        }
        void maximize() {
            minimized = false;
        }
        int getHeight() {
            if(minimized) return minHeight;
            else
                return (int) maxRenderer.getComponent().
                    getPreferredSize().getHeight() ;
        }
        int getMinHeight() {
            return minHeight;
        }
        @Override
        public String toString() {
            if(value == null) return null;
            if(value.getClass().isArray()) {
                String name =
                    Utils.getArrayClassName(value.getClass().getName());
                int length = Array.getLength(value);
                return name + "[" + length +"]";
            }
            if(value instanceof CompositeData ||
               value instanceof TabularData)
                return value.getClass().getName();
            return value.toString();
        }
        TableCellRenderer getRenderer() {
            if(minimized) return minRenderer;
            else return maxRenderer;
        }
        TableCellRenderer getMinRenderer() {
            return minRenderer;
        }
    }
    class AttributesListener implements  TableModelListener {
        private Component component;
        public AttributesListener(Component component) {
            this.component = component;
        }
        public void tableChanged(final TableModelEvent e) {
            if (isColumnEditable(e.getColumn())) {
                final TableModel model = (TableModel)e.getSource();
                Object tableValue = model.getValueAt(e.getFirstRow(),
                                                 e.getColumn());
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("tableChanged: firstRow="+e.getFirstRow()+
                        ", lastRow="+e.getLastRow()+", column="+e.getColumn()+
                        ", value="+tableValue);
                }
                if (tableValue instanceof String) {
                    try {
                        tableValue =
                            Utils.createObjectFromString(getClassName(e.getFirstRow()), 
                            (String)tableValue);
                    } catch (Throwable ex) {
                        popupAndLog(ex,"tableChanged",
                                "Problem setting attribute");
                    }
                }
                final String attributeName = getValueName(e.getFirstRow());
                final Attribute attribute =
                      new Attribute(attributeName,tableValue);
                setAttribute(attribute, "tableChanged");
            }
        }
        private void setAttribute(final Attribute attribute, final String method) {
            final SwingWorker<Void,Void> setAttribute =
                    new SwingWorker<Void,Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        if (JConsole.isDebug()) {
                            System.err.println("setAttribute("+
                                    attribute.getName()+
                                "="+attribute.getValue()+")");
                        }
                        mbean.setAttribute(attribute);
                    } catch (Throwable ex) {
                        popupAndLog(ex,method,"Problem setting attribute");
                    }
                    return null;
                }
                @Override
                protected void done() {
                    try {
                        get();
                    } catch (Exception x) {
                        if (JConsole.isDebug())
                            x.printStackTrace();
                    }
                    refreshAttributes(false);
                }
            };
            mbeansTab.workerAdd(setAttribute);
        }
        private void popupAndLog(Throwable ex, String method, String key) {
            ex = Utils.getActualException(ex);
            if (JConsole.isDebug()) ex.printStackTrace();
            String message = (ex.getMessage() != null) ? ex.getMessage()
                    : ex.toString();
            EventQueue.invokeLater(
                    new ThreadDialog(component, message+"\n",
                                     Resources.getText(key),
                                     JOptionPane.ERROR_MESSAGE));
        }
    }
}
