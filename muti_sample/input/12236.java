public class FontEditor extends Panel implements java.beans.PropertyEditor {
    private static final long serialVersionUID = 6732704486002715933L;
    public FontEditor() {
        setLayout(null);
        toolkit = Toolkit.getDefaultToolkit();
        fonts = toolkit.getFontList();
        familyChoser = new Choice();
        for (int i = 0; i < fonts.length; i++) {
            familyChoser.addItem(fonts[i]);
        }
        add(familyChoser);
        familyChoser.reshape(20, 5, 100, 30);
        styleChoser = new Choice();
        for (int i = 0; i < styleNames.length; i++) {
            styleChoser.addItem(styleNames[i]);
        }
        add(styleChoser);
        styleChoser.reshape(145, 5, 70, 30);
        sizeChoser = new Choice();
        for (int i = 0; i < pointSizes.length; i++) {
            sizeChoser.addItem("" + pointSizes[i]);
        }
        add(sizeChoser);
        sizeChoser.reshape(220, 5, 70, 30);
        resize(300,40);
    }
    public Dimension preferredSize() {
        return new Dimension(300, 40);
    }
    public void setValue(Object o) {
        font = (Font) o;
        if (this.font == null)
            return;
        changeFont(font);
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i].equals(font.getFamily())) {
                familyChoser.select(i);
                break;
            }
        }
        for (int i = 0; i < styleNames.length; i++) {
            if (font.getStyle() == styles[i]) {
                styleChoser.select(i);
                break;
            }
        }
        for (int i = 0; i < pointSizes.length; i++) {
            if (font.getSize() <= pointSizes[i]) {
                sizeChoser.select(i);
                break;
            }
        }
    }
    private void changeFont(Font f) {
        font = f;
        if (sample != null) {
            remove(sample);
        }
        sample = new Label(sampleText);
        sample.setFont(font);
        add(sample);
        Component p = getParent();
        if (p != null) {
            p.invalidate();
            p.layout();
        }
        invalidate();
        layout();
        repaint();
        support.firePropertyChange("", null, null);
    }
    public Object getValue() {
        return (font);
    }
    public String getJavaInitializationString() {
        if (this.font == null)
            return "null";
        return "new java.awt.Font(\"" + font.getName() + "\", " +
                   font.getStyle() + ", " + font.getSize() + ")";
    }
    public boolean action(Event e, Object arg) {
        String family = familyChoser.getSelectedItem();
        int style = styles[styleChoser.getSelectedIndex()];
        int size = pointSizes[sizeChoser.getSelectedIndex()];
        try {
            Font f = new Font(family, style, size);
            changeFont(f);
        } catch (Exception ex) {
            System.err.println("Couldn't create font " + family + "-" +
                        styleNames[style] + "-" + size);
        }
        return (false);
    }
    public boolean isPaintable() {
        return true;
    }
    public void paintValue(java.awt.Graphics gfx, java.awt.Rectangle box) {
        Font oldFont = gfx.getFont();
        gfx.setFont(font);
        FontMetrics fm = gfx.getFontMetrics();
        int vpad = (box.height - fm.getAscent())/2;
        gfx.drawString(sampleText, 0, box.height-vpad);
        gfx.setFont(oldFont);
    }
    public String getAsText() {
        if (this.font == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.font.getName());
        sb.append(' ');
        boolean b = this.font.isBold();
        if (b) {
            sb.append("BOLD");
        }
        boolean i = this.font.isItalic();
        if (i) {
            sb.append("ITALIC");
        }
        if (b || i) {
            sb.append(' ');
        }
        sb.append(this.font.getSize());
        return sb.toString();
    }
    public void setAsText(String text) throws IllegalArgumentException {
        setValue((text == null) ? null : Font.decode(text));
    }
    public String[] getTags() {
        return null;
    }
    public java.awt.Component getCustomEditor() {
        return this;
    }
    public boolean supportsCustomEditor() {
        return true;
    }
    public void addPropertyChangeListener(PropertyChangeListener l) {
        support.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener(PropertyChangeListener l) {
        support.removePropertyChangeListener(l);
    }
    private Font font;
    private Toolkit toolkit;
    private String sampleText = "Abcde...";
    private Label sample;
    private Choice familyChoser;
    private Choice styleChoser;
    private Choice sizeChoser;
    private String fonts[];
    private String[] styleNames = { "plain", "bold", "italic" };
    private int[] styles = { Font.PLAIN, Font.BOLD, Font.ITALIC };
    private int[] pointSizes = { 3, 5, 8, 10, 12, 14, 18, 24, 36, 48 };
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
}
