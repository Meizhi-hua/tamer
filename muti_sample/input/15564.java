public class MotifInternalFrameUI extends BasicInternalFrameUI {
    Color color;
    Color highlight;
    Color shadow;
    MotifInternalFrameTitlePane titlePane;
    @Deprecated
    protected KeyStroke closeMenuKey;
    public static ComponentUI createUI(JComponent w)    {
        return new MotifInternalFrameUI((JInternalFrame)w);
    }
    public MotifInternalFrameUI(JInternalFrame w)   {
        super(w);
    }
    public void installUI(JComponent c)   {
        super.installUI(c);
        setColors((JInternalFrame)c);
    }
    protected void installDefaults() {
        Border frameBorder = frame.getBorder();
        frame.setLayout(internalFrameLayout = createLayoutManager());
        if (frameBorder == null || frameBorder instanceof UIResource) {
            frame.setBorder(new MotifBorders.InternalFrameBorder(frame));
        }
    }
    protected void installKeyboardActions(){
      super.installKeyboardActions();
      closeMenuKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    }
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(frame);
        frame.setLayout(null);
        internalFrameLayout = null;
    }
    private JInternalFrame getFrame(){
      return frame;
    }
    public JComponent createNorthPane(JInternalFrame w) {
        titlePane = new MotifInternalFrameTitlePane(w);
        return titlePane;
    }
    public Dimension getMaximumSize(JComponent x) {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
    protected void uninstallKeyboardActions(){
      super.uninstallKeyboardActions();
      if (isKeyBindingRegistered()){
        JInternalFrame.JDesktopIcon di = frame.getDesktopIcon();
        SwingUtilities.replaceUIActionMap(di, null);
        SwingUtilities.replaceUIInputMap(di, JComponent.WHEN_IN_FOCUSED_WINDOW,
                                         null);
      }
    }
    protected void setupMenuOpenKey(){
        super.setupMenuOpenKey();
        ActionMap map = SwingUtilities.getUIActionMap(frame);
        if (map != null) {
            map.put("showSystemMenu", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    titlePane.showSystemMenu();
                }
                public boolean isEnabled(){
                    return isKeyBindingActive();
                }
            });
        }
    }
    protected void setupMenuCloseKey(){
        ActionMap map = SwingUtilities.getUIActionMap(frame);
        if (map != null) {
            map.put("hideSystemMenu", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    titlePane.hideSystemMenu();
                }
                public boolean isEnabled(){
                    return isKeyBindingActive();
                }
            });
        }
        JInternalFrame.JDesktopIcon di = frame.getDesktopIcon();
        InputMap diInputMap = SwingUtilities.getUIInputMap
                          (di, JComponent.WHEN_IN_FOCUSED_WINDOW);
        if (diInputMap == null) {
            Object[] bindings = (Object[])UIManager.get
                                          ("DesktopIcon.windowBindings");
            if (bindings != null) {
                diInputMap = LookAndFeel.makeComponentInputMap(di, bindings);
                SwingUtilities.replaceUIInputMap(di, JComponent.
                                               WHEN_IN_FOCUSED_WINDOW,
                                               diInputMap);
            }
        }
        ActionMap diActionMap = SwingUtilities.getUIActionMap(di);
        if (diActionMap == null) {
            diActionMap = new ActionMapUIResource();
            diActionMap.put("hideSystemMenu", new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    JInternalFrame.JDesktopIcon icon = getFrame().
                                     getDesktopIcon();
                    MotifDesktopIconUI micon = (MotifDesktopIconUI)icon.
                                               getUI();
                    micon.hideSystemMenu();
                }
                public boolean isEnabled(){
                    return isKeyBindingActive();
                }
            });
            SwingUtilities.replaceUIActionMap(di, diActionMap);
        }
    }
    protected void activateFrame(JInternalFrame f) {
        super.activateFrame(f);
        setColors(f);
    }
    protected void deactivateFrame(JInternalFrame f) {
        setColors(f);
        super.deactivateFrame(f);
    }
    void setColors(JInternalFrame frame) {
        if (frame.isSelected()) {
            color = UIManager.getColor("InternalFrame.activeTitleBackground");
        } else {
            color = UIManager.getColor("InternalFrame.inactiveTitleBackground");
        }
        highlight = color.brighter();
        shadow = color.darker().darker();
        titlePane.setColors(color, highlight, shadow);
    }
}
