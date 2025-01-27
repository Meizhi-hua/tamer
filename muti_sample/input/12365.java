public class Option implements Serializable {
    public Option(AttributeSet attr) {
        this.attr = attr.copyAttributes();
        selected = (attr.getAttribute(HTML.Attribute.SELECTED) != null);
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
    public AttributeSet getAttributes() {
        return attr;
    }
    public String toString() {
        return label;
    }
    protected void setSelection(boolean state) {
        selected = state;
    }
    public boolean isSelected() {
        return selected;
    }
    public String getValue() {
        String value = (String) attr.getAttribute(HTML.Attribute.VALUE);
        if (value == null) {
            value = label;
        }
        return value;
    }
    private boolean selected;
    private String label;
    private AttributeSet attr;
}
