public class CheckRenderer extends JCheckBox implements ListCellRenderer {
    private JList list;
    private Color startBackground;
    public CheckRenderer(final JList list) {
        this.list = list;
        list.addMouseListener(
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int index = list.locationToIndex(e.getPoint());
                        Point p2 = list.indexToLocation(index);
                        Rectangle r = new Rectangle(p2.x, p2.y, getPreferredSize().height, getPreferredSize().height);
                        if (r.contains(e.getPoint())) {
                            CheckNode node = ((CheckNodeListModel) list.getModel()).getCheckNodeAt(index);
                            node.setSelected(!node.isSelected());
                            list.repaint();
                            e.consume();
                        }
                    }
                });
        this.setPreferredSize(new Dimension(getPreferredSize().width, getPreferredSize().height - 5));
        startBackground = this.getBackground();
    }
    public Component getListCellRendererComponent(final JList list, Object value, final int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.toString());
        CheckNode node = ((CheckNodeListModel) list.getModel()).getCheckNodeAt(index);
        this.setSelected(node.isSelected());
        this.setEnabled(list.isEnabled());
        if (isSelected && list.hasFocus()) {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        } else if (isSelected) {
            assert !list.hasFocus();
            this.setBackground(startBackground);
            this.setForeground(list.getForeground());
        } else {
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
        return this;
    }
}
