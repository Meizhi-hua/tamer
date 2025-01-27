public class BasicRootPaneUI extends RootPaneUI implements
                  PropertyChangeListener {
    private static RootPaneUI rootPaneUI = new BasicRootPaneUI();
    public static ComponentUI createUI(JComponent c) {
        return rootPaneUI;
    }
    public void installUI(JComponent c) {
        installDefaults((JRootPane)c);
        installComponents((JRootPane)c);
        installListeners((JRootPane)c);
        installKeyboardActions((JRootPane)c);
    }
    public void uninstallUI(JComponent c) {
        uninstallDefaults((JRootPane)c);
        uninstallComponents((JRootPane)c);
        uninstallListeners((JRootPane)c);
        uninstallKeyboardActions((JRootPane)c);
    }
    protected void installDefaults(JRootPane c){
        LookAndFeel.installProperty(c, "opaque", Boolean.FALSE);
    }
    protected void installComponents(JRootPane root) {
    }
    protected void installListeners(JRootPane root) {
        root.addPropertyChangeListener(this);
    }
    protected void installKeyboardActions(JRootPane root) {
        InputMap km = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, root);
        SwingUtilities.replaceUIInputMap(root,
                JComponent.WHEN_IN_FOCUSED_WINDOW, km);
        km = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                root);
        SwingUtilities.replaceUIInputMap(root,
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, km);
        LazyActionMap.installLazyActionMap(root, BasicRootPaneUI.class,
                "RootPane.actionMap");
        updateDefaultButtonBindings(root);
    }
    protected void uninstallDefaults(JRootPane root) {
    }
    protected void uninstallComponents(JRootPane root) {
    }
    protected void uninstallListeners(JRootPane root) {
        root.removePropertyChangeListener(this);
    }
    protected void uninstallKeyboardActions(JRootPane root) {
        SwingUtilities.replaceUIInputMap(root, JComponent.
                                       WHEN_IN_FOCUSED_WINDOW, null);
        SwingUtilities.replaceUIActionMap(root, null);
    }
    InputMap getInputMap(int condition, JComponent c) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap)DefaultLookup.get(c, this,
                                       "RootPane.ancestorInputMap");
        }
        if (condition == JComponent.WHEN_IN_FOCUSED_WINDOW) {
            return createInputMap(condition, c);
        }
        return null;
    }
    ComponentInputMap createInputMap(int condition, JComponent c) {
        return new RootPaneInputMap(c);
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.PRESS));
        map.put(new Actions(Actions.RELEASE));
        map.put(new Actions(Actions.POST_POPUP));
    }
    void updateDefaultButtonBindings(JRootPane root) {
        InputMap km = SwingUtilities.getUIInputMap(root, JComponent.
                                               WHEN_IN_FOCUSED_WINDOW);
        while (km != null && !(km instanceof RootPaneInputMap)) {
            km = km.getParent();
        }
        if (km != null) {
            km.clear();
            if (root.getDefaultButton() != null) {
                Object[] bindings = (Object[])DefaultLookup.get(root, this,
                           "RootPane.defaultButtonWindowKeyBindings");
                if (bindings != null) {
                    LookAndFeel.loadKeyBindings(km, bindings);
                }
            }
        }
    }
    public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals("defaultButton")) {
            JRootPane rootpane = (JRootPane)e.getSource();
            updateDefaultButtonBindings(rootpane);
            if (rootpane.getClientProperty("temporaryDefaultButton") == null) {
                rootpane.putClientProperty("initialDefaultButton", e.getNewValue());
            }
        }
    }
    static class Actions extends UIAction {
        public static final String PRESS = "press";
        public static final String RELEASE = "release";
        public static final String POST_POPUP = "postPopup";
        Actions(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent evt) {
            JRootPane root = (JRootPane)evt.getSource();
            JButton owner = root.getDefaultButton();
            String key = getName();
            if (key == POST_POPUP) { 
                Component c = KeyboardFocusManager
                        .getCurrentKeyboardFocusManager()
                         .getFocusOwner();
                if(c instanceof JComponent) {
                    JComponent src = (JComponent) c;
                    JPopupMenu jpm = src.getComponentPopupMenu();
                    if(jpm != null) {
                        Point pt = src.getPopupLocation(null);
                        if(pt == null) {
                            Rectangle vis = src.getVisibleRect();
                            pt = new Point(vis.x+vis.width/2,
                                           vis.y+vis.height/2);
                        }
                        jpm.show(c, pt.x, pt.y);
                    }
                }
            }
            else if (owner != null
                     && SwingUtilities.getRootPane(owner) == root) {
                if (key == PRESS) {
                    owner.doClick(20);
                }
            }
        }
        public boolean isEnabled(Object sender) {
            String key = getName();
            if(key == POST_POPUP) {
                MenuElement[] elems = MenuSelectionManager
                        .defaultManager()
                        .getSelectedPath();
                if(elems != null && elems.length != 0) {
                    return false;
                }
                Component c = KeyboardFocusManager
                       .getCurrentKeyboardFocusManager()
                        .getFocusOwner();
                if(c instanceof JComponent) {
                    JComponent src = (JComponent) c;
                    return src.getComponentPopupMenu() != null;
                }
                return false;
            }
            if (sender != null && sender instanceof JRootPane) {
                JButton owner = ((JRootPane)sender).getDefaultButton();
                return (owner != null && owner.getModel().isEnabled());
            }
            return true;
        }
    }
    private static class RootPaneInputMap extends ComponentInputMapUIResource {
        public RootPaneInputMap(JComponent c) {
            super(c);
        }
    }
}
