abstract class OverviewPanel extends PlotterPanel {
    private static final Dimension PREFERRED_PLOTTER_SIZE = new Dimension(300, 200);
    private static final Dimension MINIMUM_PLOTTER_SIZE = new Dimension(200, 150);
    static final int VIEW_RANGE = -1;   
    static Color PLOTTER_COLOR = IS_GTK ? new Color(231, 111, 80) : null;
    private JLabel infoLabel;
    public OverviewPanel(String title) {
        this(title, null, null, null);
    }
    public OverviewPanel(String title, String plotterKey,
                         String plotterName, Plotter.Unit plotterUnit) {
        super(title);
        setLayout(new BorderLayout(0, 0));
        if (plotterKey != null && plotterName != null) {
            Plotter plotter = new Plotter();
            plotter.setPreferredSize(PREFERRED_PLOTTER_SIZE);
            plotter.setMinimumSize(MINIMUM_PLOTTER_SIZE);
            plotter.setViewRange(VIEW_RANGE);
            if (plotterUnit != null) {
                plotter.setUnit(plotterUnit);
            }
            plotter.createSequence(plotterKey, plotterName, PLOTTER_COLOR, true);
            setAccessibleName(plotter,
                              getText("OverviewPanel.plotter.accessibleName",
                                      title));
            setPlotter(plotter);
        }
    }
    public JLabel getInfoLabel() {
        if (infoLabel == null) {
            infoLabel = new JLabel("", CENTER) {
                @Override
                public void setText(String text) {
                    if (text.startsWith("<html>")) {
                        StringBuilder buf = new StringBuilder();
                        char[] chars = text.toCharArray();
                        int n = chars.length;
                        for (int i = 0; i < n; i++) {
                            if (chars[i] == ' '
                                && ((i < n-1 && chars[i+1] == ' ')
                                    || ((i == 0 || chars[i-1] != ' ')
                                        && (i == n-1 || chars[i+1] != ' ')))) {
                                buf.append("&nbsp;");
                            } else {
                                buf.append(chars[i]);
                            }
                        }
                        text = buf.toString();
                    }
                    super.setText(text);
                }
            };
            if (IS_GTK) {
                JPanel southPanel = new JPanel(new BorderLayout());
                JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
                southPanel.add(separator, BorderLayout.NORTH);
                southPanel.add(infoLabel, BorderLayout.SOUTH);
                add(southPanel, BorderLayout.SOUTH);
            } else {
                add(infoLabel, BorderLayout.SOUTH);
            }
        }
        return infoLabel;
    }
}
