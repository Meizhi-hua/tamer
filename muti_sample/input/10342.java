public final class SheetDialog {
    private static Rectangle iconR = new Rectangle();
    private static Rectangle textR = new Rectangle();
    private static Rectangle viewR = new Rectangle();
    private static Insets viewInsets = new Insets(0, 0, 0, 0);
    private SheetDialog() {
    }
    static JOptionPane showOptionDialog(final VMPanel vmPanel, Object message,
                                        int optionType, int messageType,
                                        Icon icon, Object[] options, Object initialValue) {
        JRootPane rootPane = SwingUtilities.getRootPane(vmPanel);
        JPanel glassPane = (JPanel)rootPane.getGlassPane();
        if (!(glassPane instanceof SlideAndFadeGlassPane)) {
            glassPane = new SlideAndFadeGlassPane();
            glassPane.setName(rootPane.getName()+".glassPane");
            rootPane.setGlassPane(glassPane);
            rootPane.revalidate();
        }
        final SlideAndFadeGlassPane safGlassPane = (SlideAndFadeGlassPane)glassPane;
        message = fixWrapping(message, rootPane.getWidth() - 75); 
        final SheetOptionPane optionPane = new SheetOptionPane(message, messageType, optionType,
                                                           icon, options, initialValue);
        optionPane.setComponentOrientation(vmPanel.getComponentOrientation());
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (event.getPropertyName().equals(VALUE_PROPERTY) &&
                    event.getNewValue() != null &&
                    event.getNewValue() != UNINITIALIZED_VALUE) {
                    ((SlideAndFadeGlassPane)optionPane.getParent()).hide(optionPane);
                }
            }
        });
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                safGlassPane.show(optionPane);
            }
        });
        return optionPane;
    }
    private static Object fixWrapping(Object message, final int maxWidth) {
        if (message instanceof Object[]) {
            Object[] arr = (Object[])message;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = fixWrapping(arr[i], maxWidth);
            }
        } else if (message instanceof String &&
                   ((String)message).startsWith("<html>")) {
            message = new JLabel((String)message) {
                public Dimension getPreferredSize() {
                    String text = getText();
                    Insets insets = getInsets(viewInsets);
                    FontMetrics fm = getFontMetrics(getFont());
                    Dimension pref = super.getPreferredSize();
                    Dimension min = getMinimumSize();
                    iconR.x = iconR.y = iconR.width = iconR.height = 0;
                    textR.x = textR.y = textR.width = textR.height = 0;
                    int dx = insets.left + insets.right;
                    int dy = insets.top + insets.bottom;
                    viewR.x = dx;
                    viewR.y = dy;
                    viewR.width = viewR.height = Short.MAX_VALUE;
                    View v = (View)getClientProperty("html");
                    if (v != null) {
                        int w = Math.min(maxWidth,
                                         Math.min(pref.width,
                                                  Math.max(min.width, 300)));
                        v.setSize((float)w, 0F);
                        SwingUtilities.layoutCompoundLabel(this, fm, text, null,
                                                           getVerticalAlignment(),
                                                           getHorizontalAlignment(),
                                                           getVerticalTextPosition(),
                                                           getHorizontalTextPosition(),
                                                           viewR, iconR, textR,
                                                           getIconTextGap());
                        return new Dimension(textR.width + dx,
                                             textR.height + dy);
                    } else {
                        return pref; 
                    }
                }
            };
        }
        return message;
    }
    private static class SlideAndFadeGlassPane extends JPanel {
        SheetOptionPane optionPane;
        int fade = 20;
        boolean slideIn = true;
        SlideAndFadeGlassPane() {
            super(null);
            setVisible(false);
            setOpaque(false);
            addMouseListener(new MouseAdapter() {});
        }
        public void show(SheetOptionPane optionPane) {
            this.optionPane = optionPane;
            removeAll();
            add(optionPane);
            setVisible(true);
            slideIn = true;
            revalidate();
            repaint();
            doSlide();
        }
        public void hide(SheetOptionPane optionPane) {
            if (optionPane != this.optionPane) {
                return;
            }
            slideIn = false;
            revalidate();
            repaint();
            doSlide();
        }
        private void doSlide() {
            if (optionPane.getParent() == null) {
                return;
            }
            if (optionPane.getWidth() == 0) {
                optionPane.setSize(optionPane.getPreferredSize());
            }
            int glassPaneWidth = getWidth();
            if (glassPaneWidth == 0 && getParent() != null) {
                glassPaneWidth = getParent().getWidth();
            }
            int x = (glassPaneWidth - optionPane.getWidth()) / 2;
            if (!slideIn) {
                    remove(optionPane);
                    setVisible(false);
                    return;
            } else {
                    optionPane.setLocation(x, 0);
                    setGrayLevel(fade);
                    return;
            }
        }
        public void setGrayLevel(int gray) {
            gray = gray * 255 / 100;
            setBackground(new Color(0, 0, 0, gray));
        }
        public void paint(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
        }
    }
    static class SheetOptionPane extends JOptionPane {
        SheetOptionPane(Object message, int messageType, int optionType,
                        Icon icon, Object[] options, Object initialValue) {
            super(message, messageType, optionType, icon, options, initialValue);
            setBorder(new CompoundBorder(new LineBorder(new Color(204, 204, 204), 1),
                                         new EmptyBorder(4, 4, 4, 4)));
        }
        private static Composite comp =
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8F);
        private static Color bgColor = new Color(241, 239, 239);
        public void setVisible(boolean visible) {
            SlideAndFadeGlassPane glassPane = (SlideAndFadeGlassPane)getParent();
            if (glassPane != null) {
                if (visible) {
                    glassPane.show(this);
                } else {
                    glassPane.hide(this);
                }
            }
        }
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            Composite oldComp = g2d.getComposite();
            g2d.setComposite(comp);
            Color oldColor = g2d.getColor();
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(oldColor);
            g2d.setComposite(oldComp);
            super.paint(g);
        }
    }
}
