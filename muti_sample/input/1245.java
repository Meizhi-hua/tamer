public abstract class XTable extends JTable {
    static final int NAME_COLUMN = 0;
    static final int VALUE_COLUMN = 1;
    private Color defaultColor, editableColor, droppableColor, errorColor;
    private Font normalFont, boldFont;
    public XTable () {
        super();
        @SuppressWarnings("serial")
        final TableSorter sorter = new TableSorter();
        setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(this);
        setRowSelectionAllowed(false);
        setColumnSelectionAllowed(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }
    Color getDefaultColor() {
        return defaultColor;
    }
    Color getEditableColor() {
        return editableColor;
    }
    void sortRequested(int column) {
    }
    public int getSelectedIndex() {
        return convertRowToIndex(getSelectedRow());
    }
    public int convertRowToIndex(int row) {
        if (row == -1) return row;
        if (getModel() instanceof TableSorter) {
            return ((TableSorter) getModel()).getIndexOfRow(row);
        } else {
            return row;
        }
    }
    public void emptyTable() {
        DefaultTableModel model = (DefaultTableModel)getModel();
        while (model.getRowCount()>0)
            model.removeRow(0);
    }
    public abstract boolean isTableEditable();
    public abstract boolean isColumnEditable(int column);
    public abstract boolean isReadable(int row);
    public abstract boolean isWritable(int row);
    public abstract boolean isCellError(int row, int col);
    public abstract boolean isAttributeViewable(int row, int col);
    public abstract void setTableValue(Object value,int row);
    public abstract Object getValue(int row);
    public abstract String getClassName(int row);
    public abstract String getValueName(int row);
    public boolean isReadWrite(int row) {
        return (isReadable(row) && isWritable(row));
    }
    @Override
    public boolean isCellEditable(int row, int col) {
        return ((isTableEditable() && isColumnEditable(col)
                 &&  isWritable(row)
                 && Utils.isEditableType(getClassName(row))));
    }
    public boolean isCellDroppable(int row, int col) {
        return (isTableEditable() && isColumnEditable(col)
                && isWritable(row));
    }
    public String getToolTip(int row, int column) {
        return null;
    }
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        DefaultTableCellRenderer tcr =
            (DefaultTableCellRenderer) super.getCellRenderer(row,column);
        tcr.setToolTipText(getToolTip(row,column));
        if (defaultColor == null) {
            defaultColor = tcr.getForeground();
            editableColor = Color.blue;
            droppableColor = Color.green;
            errorColor = Color.red;
            if (defaultColor == null) {
                return tcr;
            }
        }
        if (column != VALUE_COLUMN) {
            tcr.setForeground(defaultColor);
            return tcr;
        }
        if (isCellError(row,column)) {
            tcr.setForeground(errorColor);
        } else if (isCellEditable(row, column)) {
            tcr.setForeground(editableColor);
        } else {
            tcr.setForeground(defaultColor);
        }
        return tcr;
    }
    @Override
    public Component prepareRenderer(TableCellRenderer renderer,
                                     int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        if (normalFont == null) {
            normalFont = comp.getFont();
            boldFont = normalFont.deriveFont(Font.BOLD);
        }
        if (column == VALUE_COLUMN && isAttributeViewable(row, VALUE_COLUMN)) {
            comp.setFont(boldFont);
        } else {
            comp.setFont(normalFont);
        }
        return comp;
    }
}
