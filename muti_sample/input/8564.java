public class SynthTableHeaderUI extends BasicTableHeaderUI
                                implements PropertyChangeListener, SynthUI {
    private TableCellRenderer prevRenderer = null;
    private SynthStyle style;
    public static ComponentUI createUI(JComponent h) {
        return new SynthTableHeaderUI();
    }
    @Override
    protected void installDefaults() {
        prevRenderer = header.getDefaultRenderer();
        if (prevRenderer instanceof UIResource) {
            header.setDefaultRenderer(new HeaderRenderer());
        }
        updateStyle(header);
    }
    private void updateStyle(JTableHeader c) {
        SynthContext context = getContext(c, ENABLED);
        SynthStyle oldStyle = style;
        style = SynthLookAndFeel.updateStyle(context, this);
        if (style != oldStyle) {
            if (oldStyle != null) {
                uninstallKeyboardActions();
                installKeyboardActions();
            }
        }
        context.dispose();
    }
    @Override
    protected void installListeners() {
        super.installListeners();
        header.addPropertyChangeListener(this);
    }
    @Override
    protected void uninstallDefaults() {
        if (header.getDefaultRenderer() instanceof HeaderRenderer) {
            header.setDefaultRenderer(prevRenderer);
        }
        SynthContext context = getContext(header, ENABLED);
        style.uninstallDefaults(context);
        context.dispose();
        style = null;
    }
    @Override
    protected void uninstallListeners() {
        header.removePropertyChangeListener(this);
        super.uninstallListeners();
    }
    @Override
    public void update(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        SynthLookAndFeel.update(context, g);
        context.getPainter().paintTableHeaderBackground(context,
                          g, 0, 0, c.getWidth(), c.getHeight());
        paint(context, g);
        context.dispose();
    }
    @Override
    public void paint(Graphics g, JComponent c) {
        SynthContext context = getContext(c);
        paint(context, g);
        context.dispose();
    }
    protected void paint(SynthContext context, Graphics g) {
        super.paint(g, context.getComponent());
    }
    @Override
    public void paintBorder(SynthContext context, Graphics g, int x,
                            int y, int w, int h) {
        context.getPainter().paintTableHeaderBorder(context, g, x, y, w, h);
    }
    @Override
    public SynthContext getContext(JComponent c) {
        return getContext(c, SynthLookAndFeel.getComponentState(c));
    }
    private SynthContext getContext(JComponent c, int state) {
        return SynthContext.getContext(SynthContext.class, c,
                    SynthLookAndFeel.getRegion(c), style, state);
    }
    @Override
    protected void rolloverColumnUpdated(int oldColumn, int newColumn) {
        header.repaint(header.getHeaderRect(oldColumn));
        header.repaint(header.getHeaderRect(newColumn));
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SynthLookAndFeel.shouldUpdateStyle(evt)) {
            updateStyle((JTableHeader)evt.getSource());
        }
    }
    private class HeaderRenderer extends DefaultTableCellHeaderRenderer {
        HeaderRenderer() {
            setHorizontalAlignment(JLabel.LEADING);
            setName("TableHeader.renderer");
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row, int column) {
            boolean hasRollover = (column == getRolloverColumn());
            if (isSelected || hasRollover || hasFocus) {
                SynthLookAndFeel.setSelectedUI((SynthLabelUI)SynthLookAndFeel.
                             getUIOfType(getUI(), SynthLabelUI.class),
                             isSelected, hasFocus, table.isEnabled(),
                             hasRollover);
            } else {
                SynthLookAndFeel.resetSelectedUI();
            }
            RowSorter rs = table == null ? null : table.getRowSorter();
            java.util.List<? extends RowSorter.SortKey> sortKeys = rs == null ? null : rs.getSortKeys();
            if (sortKeys != null && sortKeys.size() > 0 && sortKeys.get(0).getColumn() ==
                    table.convertColumnIndexToModel(column)) {
                switch(sortKeys.get(0).getSortOrder()) {
                    case ASCENDING:
                        putClientProperty("Table.sortOrder", "ASCENDING");
                        break;
                    case DESCENDING:
                        putClientProperty("Table.sortOrder", "DESCENDING");
                        break;
                    case UNSORTED:
                        putClientProperty("Table.sortOrder", "UNSORTED");
                        break;
                    default:
                        throw new AssertionError("Cannot happen");
                }
            } else {
                putClientProperty("Table.sortOrder", "UNSORTED");
            }
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus, row, column);
            return this;
        }
        @Override
        public void setBorder(Border border) {
            if (border instanceof SynthBorder) {
                super.setBorder(border);
            }
        }
    }
}
