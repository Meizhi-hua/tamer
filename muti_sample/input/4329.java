public class BasicButtonListener implements MouseListener, MouseMotionListener,
                                   FocusListener, ChangeListener, PropertyChangeListener
{
    private long lastPressedTimestamp = -1;
    private boolean shouldDiscardRelease = false;
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.PRESS));
        map.put(new Actions(Actions.RELEASE));
    }
    public BasicButtonListener(AbstractButton b) {
    }
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if(prop == AbstractButton.MNEMONIC_CHANGED_PROPERTY) {
            updateMnemonicBinding((AbstractButton)e.getSource());
        }
        else if(prop == AbstractButton.CONTENT_AREA_FILLED_CHANGED_PROPERTY) {
            checkOpacity((AbstractButton) e.getSource() );
        }
        else if(prop == AbstractButton.TEXT_CHANGED_PROPERTY ||
                "font" == prop || "foreground" == prop) {
            AbstractButton b = (AbstractButton) e.getSource();
            BasicHTML.updateRenderer(b, b.getText());
        }
    }
    protected void checkOpacity(AbstractButton b) {
        b.setOpaque( b.isContentAreaFilled() );
    }
    public void installKeyboardActions(JComponent c) {
        AbstractButton b = (AbstractButton)c;
        updateMnemonicBinding(b);
        LazyActionMap.installLazyActionMap(c, BasicButtonListener.class,
                                           "Button.actionMap");
        InputMap km = getInputMap(JComponent.WHEN_FOCUSED, c);
        SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_FOCUSED, km);
    }
    public void uninstallKeyboardActions(JComponent c) {
        SwingUtilities.replaceUIInputMap(c, JComponent.
                                         WHEN_IN_FOCUSED_WINDOW, null);
        SwingUtilities.replaceUIInputMap(c, JComponent.WHEN_FOCUSED, null);
        SwingUtilities.replaceUIActionMap(c, null);
    }
    InputMap getInputMap(int condition, JComponent c) {
        if (condition == JComponent.WHEN_FOCUSED) {
            BasicButtonUI ui = (BasicButtonUI)BasicLookAndFeel.getUIOfType(
                         ((AbstractButton)c).getUI(), BasicButtonUI.class);
            if (ui != null) {
                return (InputMap)DefaultLookup.get(
                             c, ui, ui.getPropertyPrefix() + "focusInputMap");
            }
        }
        return null;
    }
    void updateMnemonicBinding(AbstractButton b) {
        int m = b.getMnemonic();
        if(m != 0) {
            InputMap map = SwingUtilities.getUIInputMap(
                                b, JComponent.WHEN_IN_FOCUSED_WINDOW);
            if (map == null) {
                map = new ComponentInputMapUIResource(b);
                SwingUtilities.replaceUIInputMap(b,
                               JComponent.WHEN_IN_FOCUSED_WINDOW, map);
            }
            map.clear();
            map.put(KeyStroke.getKeyStroke(m, InputEvent.ALT_MASK, false),
                    "pressed");
            map.put(KeyStroke.getKeyStroke(m, InputEvent.ALT_MASK, true),
                    "released");
            map.put(KeyStroke.getKeyStroke(m, 0, true), "released");
        }
        else {
            InputMap map = SwingUtilities.getUIInputMap(b, JComponent.
                                             WHEN_IN_FOCUSED_WINDOW);
            if (map != null) {
                map.clear();
            }
        }
    }
    public void stateChanged(ChangeEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        b.repaint();
    }
    public void focusGained(FocusEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        if (b instanceof JButton && ((JButton)b).isDefaultCapable()) {
            JRootPane root = b.getRootPane();
            if (root != null) {
               BasicButtonUI ui = (BasicButtonUI)BasicLookAndFeel.getUIOfType(
                         b.getUI(), BasicButtonUI.class);
               if (ui != null && DefaultLookup.getBoolean(b, ui,
                                   ui.getPropertyPrefix() +
                                   "defaultButtonFollowsFocus", true)) {
                   root.putClientProperty("temporaryDefaultButton", b);
                   root.setDefaultButton((JButton)b);
                   root.putClientProperty("temporaryDefaultButton", null);
               }
            }
        }
        b.repaint();
    }
    public void focusLost(FocusEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        JRootPane root = b.getRootPane();
        if (root != null) {
           JButton initialDefault = (JButton)root.getClientProperty("initialDefaultButton");
           if (b != initialDefault) {
               BasicButtonUI ui = (BasicButtonUI)BasicLookAndFeel.getUIOfType(
                         b.getUI(), BasicButtonUI.class);
               if (ui != null && DefaultLookup.getBoolean(b, ui,
                                   ui.getPropertyPrefix() +
                                   "defaultButtonFollowsFocus", true)) {
                   root.setDefaultButton(initialDefault);
               }
           }
        }
        ButtonModel model = b.getModel();
        model.setPressed(false);
        model.setArmed(false);
        b.repaint();
    }
    public void mouseMoved(MouseEvent e) {
    }
    public void mouseDragged(MouseEvent e) {
    }
    public void mouseClicked(MouseEvent e) {
    }
    public void mousePressed(MouseEvent e) {
       if (SwingUtilities.isLeftMouseButton(e) ) {
          AbstractButton b = (AbstractButton) e.getSource();
          if(b.contains(e.getX(), e.getY())) {
              long multiClickThreshhold = b.getMultiClickThreshhold();
              long lastTime = lastPressedTimestamp;
              long currentTime = lastPressedTimestamp = e.getWhen();
              if (lastTime != -1 && currentTime - lastTime < multiClickThreshhold) {
                  shouldDiscardRelease = true;
                  return;
              }
             ButtonModel model = b.getModel();
             if (!model.isEnabled()) {
                return;
             }
             if (!model.isArmed()) {
                model.setArmed(true);
             }
             model.setPressed(true);
             if(!b.hasFocus() && b.isRequestFocusEnabled()) {
                b.requestFocus();
             }
          }
       }
    }
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (shouldDiscardRelease) {
                shouldDiscardRelease = false;
                return;
            }
            AbstractButton b = (AbstractButton) e.getSource();
            ButtonModel model = b.getModel();
            model.setPressed(false);
            model.setArmed(false);
        }
    }
    public void mouseEntered(MouseEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        ButtonModel model = b.getModel();
        if (b.isRolloverEnabled() && !SwingUtilities.isLeftMouseButton(e)) {
            model.setRollover(true);
        }
        if (model.isPressed())
                model.setArmed(true);
    }
    public void mouseExited(MouseEvent e) {
        AbstractButton b = (AbstractButton) e.getSource();
        ButtonModel model = b.getModel();
        if(b.isRolloverEnabled()) {
            model.setRollover(false);
        }
        model.setArmed(false);
    }
    private static class Actions extends UIAction {
        private static final String PRESS = "pressed";
        private static final String RELEASE = "released";
        Actions(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            AbstractButton b = (AbstractButton)e.getSource();
            String key = getName();
            if (key == PRESS) {
                ButtonModel model = b.getModel();
                model.setArmed(true);
                model.setPressed(true);
                if(!b.hasFocus()) {
                    b.requestFocus();
                }
            }
            else if (key == RELEASE) {
                ButtonModel model = b.getModel();
                model.setPressed(false);
                model.setArmed(false);
            }
        }
        public boolean isEnabled(Object sender) {
            if(sender != null && (sender instanceof AbstractButton) &&
                      !((AbstractButton)sender).getModel().isEnabled()) {
                return false;
            } else {
                return true;
            }
        }
    }
}
