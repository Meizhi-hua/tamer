public class XOpenTypeViewer extends JPanel implements ActionListener {
    JButton prev, incr, decr, tabularPrev, tabularNext;
    JLabel compositeLabel, tabularLabel;
    JScrollPane container;
    XOpenTypeData current;
    XOpenTypeDataListener listener = new XOpenTypeDataListener();
    private static final String compositeNavigationSingle =
            Resources.getText("MBeansTab.compositeNavigationSingle");
    private static final String tabularNavigationSingle =
            Resources.getText("MBeansTab.tabularNavigationSingle");
    private static TableCellEditor editor =
            new Utils.ReadOnlyTableCellEditor(new JTextField());
    class XOpenTypeDataListener extends MouseAdapter {
        XOpenTypeDataListener() {
        }
        public void mousePressed(MouseEvent e) {
            if(e.getButton() == MouseEvent.BUTTON1) {
                if(e.getClickCount() >= 2) {
                    XOpenTypeData elem = getSelectedViewedOpenType();
                    if(elem != null) {
                        try {
                            elem.viewed(XOpenTypeViewer.this);
                        }catch(Exception ex) {
                        }
                    }
                }
            }
        }
        private XOpenTypeData getSelectedViewedOpenType() {
            int row = XOpenTypeViewer.this.current.getSelectedRow();
            int col = XOpenTypeViewer.this.current.getSelectedColumn();
            Object elem =
                    XOpenTypeViewer.this.current.getModel().getValueAt(row, col);
            if(elem instanceof XOpenTypeData)
                return (XOpenTypeData) elem;
            else
                return null;
        }
    }
    static interface Navigatable {
        public void incrElement();
        public void decrElement();
        public boolean canDecrement();
        public boolean canIncrement();
        public int getElementCount();
        public int getSelectedElementIndex();
    }
    static interface XViewedTabularData extends Navigatable {
    }
    static interface XViewedArrayData extends Navigatable {
    }
    static abstract class XOpenTypeData extends JTable {
        XOpenTypeData parent;
        private Color defaultColor;
        protected int col1Width = -1;
        protected int col2Width = -1;
        private boolean init;
        private Font normalFont, boldFont;
        protected XOpenTypeData(XOpenTypeData parent) {
            this.parent = parent;
        }
        public XOpenTypeData getViewedParent() {
            return parent;
        }
        public String getToolTip(int row, int col) {
            if(col == 1) {
                Object value = getModel().getValueAt(row, col);
                if (value != null) {
                    if(isClickableElement(value))
                        return Resources.getText("Double click to visualize")
                        + ". " + value.toString();
                    else
                        return value.toString();
                }
            }
            return null;
        }
        public TableCellRenderer getCellRenderer(int row, int column) {
            DefaultTableCellRenderer tcr =
                    (DefaultTableCellRenderer)super.getCellRenderer(row,column);
            tcr.setToolTipText(getToolTip(row,column));
            return tcr;
        }
        public void renderKey(String key,  Component comp) {
            comp.setFont(normalFont);
        }
        public Component prepareRenderer(TableCellRenderer renderer,
                int row, int column) {
            Component comp = super.prepareRenderer(renderer, row, column);
            if (normalFont == null) {
                normalFont = comp.getFont();
                boldFont = normalFont.deriveFont(Font.BOLD);
            }
            Object o = ((DefaultTableModel) getModel()).getValueAt(row, column);
            if (column == 0) {
                String key = o.toString();
                renderKey(key, comp);
            } else {
                if (isClickableElement(o)) {
                    comp.setFont(boldFont);
                } else {
                    comp.setFont(normalFont);
                }
            }
            return comp;
        }
        protected boolean isClickableElement(Object obj) {
            if (obj instanceof XOpenTypeData) {
                if (obj instanceof Navigatable) {
                    return (((Navigatable) obj).getElementCount() != 0);
                } else {
                    return (obj instanceof XCompositeData);
                }
            }
            return false;
        }
        protected void updateColumnWidth() {
            if (!init) {
                TableColumnModel colModel = getColumnModel();
                if (col2Width == -1) {
                    col1Width = col1Width * 7;
                    if (col1Width <
                            getPreferredScrollableViewportSize().getWidth()) {
                        col1Width = (int)
                        getPreferredScrollableViewportSize().getWidth();
                    }
                    colModel.getColumn(0).setPreferredWidth(col1Width);
                    init = true;
                    return;
                }
                col1Width = (col1Width * 7) + 7;
                col1Width = Math.max(col1Width, 70);
                col2Width = (col2Width * 7) + 7;
                if (col1Width + col2Width <
                        getPreferredScrollableViewportSize().getWidth()) {
                    col2Width = (int)
                    getPreferredScrollableViewportSize().getWidth() -
                            col1Width;
                }
                colModel.getColumn(0).setPreferredWidth(col1Width);
                colModel.getColumn(1).setPreferredWidth(col2Width);
                init = true;
            }
        }
        public abstract void viewed(XOpenTypeViewer viewer) throws Exception;
        protected void initTable(String[] columnNames) {
            setRowSelectionAllowed(false);
            setColumnSelectionAllowed(false);
            getTableHeader().setReorderingAllowed(false);
            ((DefaultTableModel) getModel()).setColumnIdentifiers(columnNames);
            for (Enumeration<TableColumn> e = getColumnModel().getColumns();
            e.hasMoreElements();) {
                TableColumn tc = e.nextElement();
                tc.setCellEditor(editor);
            }
            addKeyListener(new Utils.CopyKeyAdapter());
            setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            setPreferredScrollableViewportSize(new Dimension(350, 200));
        }
        protected void emptyTable() {
            invalidate();
            while (getModel().getRowCount()>0)
                ((DefaultTableModel) getModel()).removeRow(0);
            validate();
        }
        public void setValueAt(Object value, int row, int col) {
        }
    }
    static class TabularDataComparator implements Comparator<CompositeData> {
        private final List<String> indexNames;
        public TabularDataComparator(TabularType type) {
            indexNames = type.getIndexNames();
        }
        @SuppressWarnings("unchecked")
        public int compare(CompositeData o1, CompositeData o2) {
            for (String key : indexNames) {
                Object c1 = o1.get(key);
                Object c2 = o2.get(key);
                if (c1 instanceof Comparable && c2 instanceof Comparable) {
                    int result = ((Comparable) c1).compareTo(c2);
                    if (result != 0)
                        return result;
                }
            }
            return 0;
        }
    }
    static class XTabularData extends XCompositeData
            implements XViewedTabularData {
        final TabularData tabular;
        final TabularType type;
        int currentIndex = 0;
        final Object[] elements;
        final int size;
        private Font normalFont, italicFont;
        @SuppressWarnings("unchecked")
        public XTabularData(XOpenTypeData parent, TabularData tabular) {
            super(parent, accessFirstElement(tabular));
            this.tabular = tabular;
            type = tabular.getTabularType();
            size = tabular.values().size();
            if (size > 0) {
                List<CompositeData> data = new ArrayList<CompositeData>(
                        (Collection<CompositeData>) tabular.values());
                Collections.sort(data, new TabularDataComparator(type));
                elements = data.toArray();
                loadCompositeData((CompositeData) elements[0]);
            } else {
                elements = new Object[0];
            }
        }
        private static CompositeData accessFirstElement(TabularData tabular) {
            if(tabular.values().size() == 0) return null;
            return (CompositeData) tabular.values().toArray()[0];
        }
        public void renderKey(String key,  Component comp) {
            if (normalFont == null) {
                normalFont = comp.getFont();
                italicFont = normalFont.deriveFont(Font.ITALIC);
            }
            for(Object k : type.getIndexNames()) {
                if(key.equals(k))
                    comp.setFont(italicFont);
            }
        }
        public int getElementCount() {
            return size;
        }
        public int getSelectedElementIndex() {
            return currentIndex;
        }
        public void incrElement() {
            currentIndex++;
            loadCompositeData((CompositeData)elements[currentIndex]);
        }
        public void decrElement() {
            currentIndex--;
            loadCompositeData((CompositeData)elements[currentIndex]);
        }
        public boolean canDecrement() {
            if(currentIndex == 0)
                return false;
            else
                return true;
        }
        public boolean canIncrement(){
            if(size == 0 ||
                    currentIndex == size -1)
                return false;
            else
                return true;
        }
        public String toString() {
            return type == null ? "" : type.getDescription();
        }
    }
    static class XCompositeData extends XOpenTypeData {
        protected final String[] columnNames = {
            Resources.getText("Name"), Resources.getText("Value")
        };
        CompositeData composite;
        public XCompositeData() {
            super(null);
            initTable(columnNames);
        }
        public XCompositeData(XOpenTypeData parent) {
            super(parent);
        }
        public XCompositeData(XOpenTypeData parent,
                CompositeData composite) {
            super(parent);
            initTable(columnNames);
            if(composite != null) {
                this.composite = composite;
                loadCompositeData(composite);
            }
        }
        public void viewed(XOpenTypeViewer viewer) throws Exception {
            viewer.setOpenType(this);
            updateColumnWidth();
        }
        public String toString() {
            return composite == null ? "" :
                composite.getCompositeType().getTypeName();
        }
        protected Object formatKey(String key) {
            return key;
        }
        private void load(CompositeData data) {
            CompositeType type = data.getCompositeType();
            Set keys = type.keySet();
            Iterator it = keys.iterator();
            Object[] rowData = new Object[2];
            while (it.hasNext()) {
                String key = (String) it.next();
                Object val = data.get(key);
                rowData[0] = formatKey(key);
                if (val == null) {
                    rowData[1] = "";
                } else {
                    OpenType openType = type.getType(key);
                    if (openType instanceof CompositeType) {
                        rowData[1] =
                                new XCompositeData(this, (CompositeData) val);
                    } else if (openType instanceof ArrayType) {
                        rowData[1] =
                                new XArrayData(this, (ArrayType) openType, val);
                    } else if (openType instanceof SimpleType) {
                        rowData[1] = val;
                    } else if (openType instanceof TabularType) {
                        rowData[1] = new XTabularData(this, (TabularData) val);
                    }
                }
                String str = null;
                if (rowData[0] != null) {
                    str = rowData[0].toString();
                    if (str.length() > col1Width) {
                        col1Width = str.length();
                    }
                }
                if (rowData[1] != null) {
                    str = rowData[1].toString();
                    if (str.length() > col2Width) {
                        col2Width = str.length();
                    }
                }
                ((DefaultTableModel) getModel()).addRow(rowData);
            }
        }
        protected void loadCompositeData(CompositeData data) {
            composite = data;
            emptyTable();
            load(data);
            DefaultTableModel tableModel = (DefaultTableModel) getModel();
            tableModel.newDataAvailable(new TableModelEvent(tableModel));
        }
    }
    static class XArrayData extends XCompositeData
            implements XViewedArrayData {
        private int dimension;
        private int size;
        private OpenType elemType;
        private Object val;
        private boolean isCompositeType;
        private boolean isTabularType;
        private int currentIndex;
        private CompositeData[] elements;
        private final String[] arrayColumns = {Resources.getText("Value")};
        private Font normalFont, boldFont;
        XArrayData(XOpenTypeData parent, ArrayType type, Object val) {
            this(parent, type.getDimension(), type.getElementOpenType(), val);
        }
        XArrayData(XOpenTypeData parent, int dimension,
                OpenType elemType, Object val) {
            super(parent);
            this.dimension = dimension;
            this.elemType = elemType;
            this.val = val;
            String[] columns = null;
            if (dimension > 1) return;
            isCompositeType = (elemType instanceof CompositeType);
            isTabularType = (elemType instanceof TabularType);
            columns = isCompositeType ? columnNames : arrayColumns;
            initTable(columns);
            loadArray();
        }
        public void viewed(XOpenTypeViewer viewer) throws Exception {
            if (size == 0)
                throw new Exception(Resources.getText("Empty array"));
            if (dimension > 1)
                throw new Exception(Resources.getText("Dimension is not " +
                        "supported:") +
                        dimension);
            super.viewed(viewer);
        }
        public int getElementCount() {
            return size;
        }
        public int getSelectedElementIndex() {
            return currentIndex;
        }
        public void renderKey(String key,  Component comp) {
            if (normalFont == null) {
                normalFont = comp.getFont();
                boldFont = normalFont.deriveFont(Font.BOLD);
            }
            if (isTabularType) {
                comp.setFont(boldFont);
            }
        }
        public void incrElement() {
            currentIndex++;
            loadCompositeData(elements[currentIndex]);
        }
        public void decrElement() {
            currentIndex--;
            loadCompositeData(elements[currentIndex]);
        }
        public boolean canDecrement() {
            if (isCompositeType && currentIndex > 0) {
                return true;
            }
            return false;
        }
        public boolean canIncrement() {
            if (isCompositeType && currentIndex < size - 1) {
                return true;
            }
            return false;
        }
        private void loadArray() {
            if (isCompositeType) {
                elements = (CompositeData[]) val;
                size = elements.length;
                if (size != 0) {
                    loadCompositeData(elements[0]);
                }
            } else {
                load();
            }
        }
        private void load() {
            Object[] rowData = new Object[1];
            size = Array.getLength(val);
            for (int i = 0; i < size; i++) {
                rowData[0] = isTabularType ?
                    new XTabularData(this, (TabularData) Array.get(val, i)) :
                    Array.get(val, i);
                String str = rowData[0].toString();
                if (str.length() > col1Width) {
                    col1Width = str.length();
                }
                ((DefaultTableModel) getModel()).addRow(rowData);
            }
        }
        public String toString() {
            if (dimension > 1) {
                return Resources.getText("Dimension is not supported:") +
                        dimension;
            } else {
                return elemType.getTypeName() + "[" + size + "]";
            }
        }
    }
    public static boolean isViewableValue(Object value) {
        if (value instanceof CompositeData || value instanceof TabularData) {
            return true;
        }
        if (value instanceof CompositeData[] || value instanceof TabularData[]) {
            return Array.getLength(value) > 0;
        }
        if (value instanceof Collection) {
            Collection<?> c = (Collection<?>) value;
            if (c.isEmpty()) {
                return false;
            } else {
                return Utils.isUniformCollection(c, CompositeData.class) ||
                        Utils.isUniformCollection(c, TabularData.class);
            }
        }
        return false;
    }
    public static Component loadOpenType(Object value) {
        Component comp = null;
        if(isViewableValue(value)) {
            XOpenTypeViewer open =
                    new XOpenTypeViewer(value);
            comp = open;
        }
        return comp;
    }
    private XOpenTypeViewer(Object value) {
        XOpenTypeData comp = null;
        if (value instanceof CompositeData) {
            comp = new XCompositeData(null, (CompositeData) value);
        } else if (value instanceof TabularData) {
            comp = new XTabularData(null, (TabularData) value);
        } else if (value instanceof CompositeData[]) {
            CompositeData cda[] = (CompositeData[]) value;
            CompositeType ct = cda[0].getCompositeType();
            comp = new XArrayData(null, 1, ct, cda);
        } else if (value instanceof TabularData[]) {
            TabularData tda[] = (TabularData[]) value;
            TabularType tt = tda[0].getTabularType();
            comp = new XArrayData(null, 1, tt, tda);
        } else if (value instanceof Collection) {
            Object e = ((Collection<?>) value).iterator().next();
            if (e instanceof CompositeData) {
                Collection<?> cdc = (Collection<?>) value;
                CompositeData cda[] = cdc.toArray(new CompositeData[0]);
                CompositeType ct = cda[0].getCompositeType();
                comp = new XArrayData(null, 1, ct, cda);
            } else if (e instanceof TabularData) {
                Collection<?> tdc = (Collection<?>) value;
                TabularData tda[] = tdc.toArray(new TabularData[0]);
                TabularType tt = tda[0].getTabularType();
                comp = new XArrayData(null, 1, tt, tda);
            }
        }
        setupDisplay(comp);
        try {
            comp.viewed(this);
        } catch (Exception e) {
            if (JConsole.isDebug()) {
                System.out.println("Exception viewing openType : " + e);
                e.printStackTrace();
            }
        }
    }
    void setOpenType(XOpenTypeData data) {
        if (current != null) {
            current.removeMouseListener(listener);
        }
        current = data;
        if (current.getViewedParent() == null) {
            prev.setEnabled(false);
        } else {
            prev.setEnabled(true);
        }
        current.addMouseListener(listener);
        if (!(data instanceof XViewedTabularData)) {
            tabularPrev.setEnabled(false);
            tabularNext.setEnabled(false);
            tabularLabel.setText(tabularNavigationSingle);
            tabularLabel.setEnabled(false);
        } else {
            XViewedTabularData tabular = (XViewedTabularData) data;
            tabularNext.setEnabled(tabular.canIncrement());
            tabularPrev.setEnabled(tabular.canDecrement());
            boolean hasMoreThanOneElement =
                    tabular.canIncrement() || tabular.canDecrement();
            if (hasMoreThanOneElement) {
                tabularLabel.setText(
                        Resources.getText("MBeansTab.tabularNavigationMultiple",
                        String.format("%d", tabular.getSelectedElementIndex() + 1),
                        String.format("%d", tabular.getElementCount())));
            } else {
                tabularLabel.setText(tabularNavigationSingle);
            }
            tabularLabel.setEnabled(hasMoreThanOneElement);
        }
        if (!(data instanceof XViewedArrayData)) {
            incr.setEnabled(false);
            decr.setEnabled(false);
            compositeLabel.setText(compositeNavigationSingle);
            compositeLabel.setEnabled(false);
        } else {
            XViewedArrayData array = (XViewedArrayData) data;
            incr.setEnabled(array.canIncrement());
            decr.setEnabled(array.canDecrement());
            boolean hasMoreThanOneElement =
                    array.canIncrement() || array.canDecrement();
            if (hasMoreThanOneElement) {
                compositeLabel.setText(
                        Resources.getText("MBeansTab.compositeNavigationMultiple",
                        String.format("%d", array.getSelectedElementIndex() + 1),
                        String.format("%d", array.getElementCount())));
            } else {
                compositeLabel.setText(compositeNavigationSingle);
            }
            compositeLabel.setEnabled(hasMoreThanOneElement);
        }
        container.invalidate();
        container.setViewportView(current);
        container.validate();
    }
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() instanceof JButton) {
            JButton b = (JButton) event.getSource();
            if (b == prev) {
                XOpenTypeData parent = current.getViewedParent();
                try {
                    parent.viewed(this);
                } catch (Exception e) {
                }
            } else if (b == incr) {
                ((XViewedArrayData) current).incrElement();
                try {
                    current.viewed(this);
                } catch (Exception e) {
                }
            } else if (b == decr) {
                ((XViewedArrayData) current).decrElement();
                try {
                    current.viewed(this);
                } catch (Exception e) {
                }
            } else if (b == tabularNext) {
                ((XViewedTabularData) current).incrElement();
                try {
                    current.viewed(this);
                } catch (Exception e) {
                }
            } else if (b == tabularPrev) {
                ((XViewedTabularData) current).decrElement();
                try {
                    current.viewed(this);
                } catch (Exception e) {
                }
            }
        }
    }
    private void setupDisplay(XOpenTypeData data) {
        setBackground(Color.white);
        container =
                new JScrollPane(data,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tabularPrev = new JButton(Resources.getText("<"));
        tabularNext = new JButton(Resources.getText(">"));
        JPanel tabularButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tabularButtons.add(tabularPrev);
        tabularPrev.addActionListener(this);
        tabularLabel = new JLabel(tabularNavigationSingle);
        tabularLabel.setEnabled(false);
        tabularButtons.add(tabularLabel);
        tabularButtons.add(tabularNext);
        tabularNext.addActionListener(this);
        tabularButtons.setBackground(Color.white);
        prev = new JButton(Resources.getText("<<"));
        prev.addActionListener(this);
        buttons.add(prev);
        incr = new JButton(Resources.getText(">"));
        incr.addActionListener(this);
        decr = new JButton(Resources.getText("<"));
        decr.addActionListener(this);
        JPanel array = new JPanel();
        array.setBackground(Color.white);
        array.add(decr);
        compositeLabel = new JLabel(compositeNavigationSingle);
        compositeLabel.setEnabled(false);
        array.add(compositeLabel);
        array.add(incr);
        buttons.add(array);
        setLayout(new BorderLayout());
        buttons.setBackground(Color.white);
        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.setBackground(Color.white);
        navigationPanel.add(tabularButtons, BorderLayout.NORTH);
        navigationPanel.add(buttons, BorderLayout.WEST);
        add(navigationPanel, BorderLayout.NORTH);
        add(container, BorderLayout.CENTER);
        Dimension d = new Dimension((int)container.getPreferredSize().
                getWidth() + 20,
                (int)container.getPreferredSize().
                getHeight() + 20);
        setPreferredSize(d);
    }
}
