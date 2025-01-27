public class JRadioButtonMenuItem extends JMenuItem implements Accessible {
    private static final String uiClassID = "RadioButtonMenuItemUI";
    public JRadioButtonMenuItem() {
        this(null, null, false);
    }
    public JRadioButtonMenuItem(Icon icon) {
        this(null, icon, false);
    }
    public JRadioButtonMenuItem(String text) {
        this(text, null, false);
    }
    public JRadioButtonMenuItem(Action a) {
        this();
        setAction(a);
    }
    public JRadioButtonMenuItem(String text, Icon icon) {
        this(text, icon, false);
    }
    public JRadioButtonMenuItem(String text, boolean selected) {
        this(text);
        setSelected(selected);
    }
    public JRadioButtonMenuItem(Icon icon, boolean selected) {
        this(null, icon, selected);
    }
    public JRadioButtonMenuItem(String text, Icon icon, boolean selected) {
        super(text, icon);
        setModel(new JToggleButton.ToggleButtonModel());
        setSelected(selected);
        setFocusable(false);
    }
    public String getUIClassID() {
        return uiClassID;
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (getUIClassID().equals(uiClassID)) {
            byte count = JComponent.getWriteObjCounter(this);
            JComponent.setWriteObjCounter(this, --count);
            if (count == 0 && ui != null) {
                ui.installUI(this);
            }
        }
    }
    protected String paramString() {
        return super.paramString();
    }
    boolean shouldUpdateSelectedStateFromAction() {
        return true;
    }
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJRadioButtonMenuItem();
        }
        return accessibleContext;
    }
    protected class AccessibleJRadioButtonMenuItem extends AccessibleJMenuItem {
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.RADIO_BUTTON;
        }
    } 
}
