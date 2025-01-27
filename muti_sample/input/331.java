public class ToolTipManager extends MouseAdapter implements MouseMotionListener  {
    Timer enterTimer, exitTimer, insideTimer;
    String toolTipText;
    Point  preferredLocation;
    JComponent insideComponent;
    MouseEvent mouseEvent;
    boolean showImmediately;
    private static final Object TOOL_TIP_MANAGER_KEY = new Object();
    transient Popup tipWindow;
    private Window window;
    JToolTip tip;
    private Rectangle popupRect = null;
    private Rectangle popupFrameRect = null;
    boolean enabled = true;
    private boolean tipShowing = false;
    private FocusListener focusChangeListener = null;
    private MouseMotionListener moveBeforeEnterListener = null;
    private KeyListener accessibilityKeyListener = null;
    private KeyStroke postTip;
    private KeyStroke hideTip;
    protected boolean lightWeightPopupEnabled = true;
    protected boolean heavyWeightPopupEnabled = false;
    ToolTipManager() {
        enterTimer = new Timer(750, new insideTimerAction());
        enterTimer.setRepeats(false);
        exitTimer = new Timer(500, new outsideTimerAction());
        exitTimer.setRepeats(false);
        insideTimer = new Timer(4000, new stillInsideTimerAction());
        insideTimer.setRepeats(false);
        moveBeforeEnterListener = new MoveBeforeEnterListener();
        accessibilityKeyListener = new AccessibilityKeyListener();
        postTip = KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_MASK);
        hideTip =  KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    }
    public void setEnabled(boolean flag) {
        enabled = flag;
        if (!flag) {
            hideTipWindow();
        }
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setLightWeightPopupEnabled(boolean aFlag){
        lightWeightPopupEnabled = aFlag;
    }
    public boolean isLightWeightPopupEnabled() {
        return lightWeightPopupEnabled;
    }
    public void setInitialDelay(int milliseconds) {
        enterTimer.setInitialDelay(milliseconds);
    }
    public int getInitialDelay() {
        return enterTimer.getInitialDelay();
    }
    public void setDismissDelay(int milliseconds) {
        insideTimer.setInitialDelay(milliseconds);
    }
    public int getDismissDelay() {
        return insideTimer.getInitialDelay();
    }
    public void setReshowDelay(int milliseconds) {
        exitTimer.setInitialDelay(milliseconds);
    }
    public int getReshowDelay() {
        return exitTimer.getInitialDelay();
    }
    void showTipWindow() {
        if(insideComponent == null || !insideComponent.isShowing())
            return;
        String mode = UIManager.getString("ToolTipManager.enableToolTipMode");
        if ("activeApplication".equals(mode)) {
            KeyboardFocusManager kfm =
                    KeyboardFocusManager.getCurrentKeyboardFocusManager();
            if (kfm.getFocusedWindow() == null) {
                return;
            }
        }
        if (enabled) {
            Dimension size;
            Point screenLocation = insideComponent.getLocationOnScreen();
            Point location = new Point();
            GraphicsConfiguration gc;
            gc = insideComponent.getGraphicsConfiguration();
            Rectangle sBounds = gc.getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit()
                                             .getScreenInsets(gc);
            sBounds.x += screenInsets.left;
            sBounds.y += screenInsets.top;
            sBounds.width -= (screenInsets.left + screenInsets.right);
            sBounds.height -= (screenInsets.top + screenInsets.bottom);
        boolean leftToRight
                = SwingUtilities.isLeftToRight(insideComponent);
            hideTipWindow();
            tip = insideComponent.createToolTip();
            tip.setTipText(toolTipText);
            size = tip.getPreferredSize();
            if(preferredLocation != null) {
                location.x = screenLocation.x + preferredLocation.x;
                location.y = screenLocation.y + preferredLocation.y;
        if (!leftToRight) {
            location.x -= size.width;
        }
            } else {
                location.x = screenLocation.x + mouseEvent.getX();
                location.y = screenLocation.y + mouseEvent.getY() + 20;
        if (!leftToRight) {
            if(location.x - size.width>=0) {
                location.x -= size.width;
            }
        }
            }
        if (popupRect == null){
        popupRect = new Rectangle();
        }
        popupRect.setBounds(location.x,location.y,
                size.width,size.height);
            if (location.x < sBounds.x) {
                location.x = sBounds.x;
            }
            else if (location.x - sBounds.x + size.width > sBounds.width) {
                location.x = sBounds.x + Math.max(0, sBounds.width - size.width)
;
            }
            if (location.y < sBounds.y) {
                location.y = sBounds.y;
            }
            else if (location.y - sBounds.y + size.height > sBounds.height) {
                location.y = sBounds.y + Math.max(0, sBounds.height - size.height);
            }
            PopupFactory popupFactory = PopupFactory.getSharedInstance();
            if (lightWeightPopupEnabled) {
        int y = getPopupFitHeight(popupRect, insideComponent);
        int x = getPopupFitWidth(popupRect,insideComponent);
        if (x>0 || y>0) {
            popupFactory.setPopupType(PopupFactory.MEDIUM_WEIGHT_POPUP);
        } else {
            popupFactory.setPopupType(PopupFactory.LIGHT_WEIGHT_POPUP);
        }
            }
            else {
                popupFactory.setPopupType(PopupFactory.MEDIUM_WEIGHT_POPUP);
            }
        tipWindow = popupFactory.getPopup(insideComponent, tip,
                          location.x,
                          location.y);
            popupFactory.setPopupType(PopupFactory.LIGHT_WEIGHT_POPUP);
        tipWindow.show();
            Window componentWindow = SwingUtilities.windowForComponent(
                                                    insideComponent);
            window = SwingUtilities.windowForComponent(tip);
            if (window != null && window != componentWindow) {
                window.addMouseListener(this);
            }
            else {
                window = null;
            }
            insideTimer.start();
        tipShowing = true;
        }
    }
    void hideTipWindow() {
        if (tipWindow != null) {
            if (window != null) {
                window.removeMouseListener(this);
                window = null;
            }
            tipWindow.hide();
            tipWindow = null;
            tipShowing = false;
            tip = null;
            insideTimer.stop();
        }
    }
    public static ToolTipManager sharedInstance() {
        Object value = SwingUtilities.appContextGet(TOOL_TIP_MANAGER_KEY);
        if (value instanceof ToolTipManager) {
            return (ToolTipManager) value;
        }
        ToolTipManager manager = new ToolTipManager();
        SwingUtilities.appContextPut(TOOL_TIP_MANAGER_KEY, manager);
        return manager;
    }
    public void registerComponent(JComponent component) {
        component.removeMouseListener(this);
        component.addMouseListener(this);
        component.removeMouseMotionListener(moveBeforeEnterListener);
        component.addMouseMotionListener(moveBeforeEnterListener);
        component.removeKeyListener(accessibilityKeyListener);
        component.addKeyListener(accessibilityKeyListener);
    }
    public void unregisterComponent(JComponent component) {
        component.removeMouseListener(this);
        component.removeMouseMotionListener(moveBeforeEnterListener);
        component.removeKeyListener(accessibilityKeyListener);
    }
    public void mouseEntered(MouseEvent event) {
        initiateToolTip(event);
    }
    private void initiateToolTip(MouseEvent event) {
        if (event.getSource() == window) {
            return;
        }
        JComponent component = (JComponent)event.getSource();
        component.removeMouseMotionListener(moveBeforeEnterListener);
        exitTimer.stop();
        Point location = event.getPoint();
        if (location.x < 0 ||
            location.x >=component.getWidth() ||
            location.y < 0 ||
            location.y >= component.getHeight()) {
            return;
        }
        if (insideComponent != null) {
            enterTimer.stop();
        }
        component.removeMouseMotionListener(this);
        component.addMouseMotionListener(this);
        boolean sameComponent = (insideComponent == component);
        insideComponent = component;
    if (tipWindow != null){
            mouseEvent = event;
            if (showImmediately) {
                String newToolTipText = component.getToolTipText(event);
                Point newPreferredLocation = component.getToolTipLocation(
                                                         event);
                boolean sameLoc = (preferredLocation != null) ?
                            preferredLocation.equals(newPreferredLocation) :
                            (newPreferredLocation == null);
                if (!sameComponent || !toolTipText.equals(newToolTipText) ||
                         !sameLoc) {
                    toolTipText = newToolTipText;
                    preferredLocation = newPreferredLocation;
                    showTipWindow();
                }
            } else {
                enterTimer.start();
            }
        }
    }
    public void mouseExited(MouseEvent event) {
        boolean shouldHide = true;
        if (insideComponent == null) {
        }
        if (window != null && event.getSource() == window && insideComponent != null) {
            Container insideComponentWindow = insideComponent.getTopLevelAncestor();
            if (insideComponentWindow != null) {
                Point location = event.getPoint();
                SwingUtilities.convertPointToScreen(location, window);
                location.x -= insideComponentWindow.getX();
                location.y -= insideComponentWindow.getY();
                location = SwingUtilities.convertPoint(null, location, insideComponent);
                if (location.x >= 0 && location.x < insideComponent.getWidth() &&
                        location.y >= 0 && location.y < insideComponent.getHeight()) {
                    shouldHide = false;
                } else {
                    shouldHide = true;
                }
            }
        } else if(event.getSource() == insideComponent && tipWindow != null) {
            Window win = SwingUtilities.getWindowAncestor(insideComponent);
            if (win != null) {  
                Point location = SwingUtilities.convertPoint(insideComponent,
                                                             event.getPoint(),
                                                             win);
                Rectangle bounds = insideComponent.getTopLevelAncestor().getBounds();
                location.x += bounds.x;
                location.y += bounds.y;
                Point loc = new Point(0, 0);
                SwingUtilities.convertPointToScreen(loc, tip);
                bounds.x = loc.x;
                bounds.y = loc.y;
                bounds.width = tip.getWidth();
                bounds.height = tip.getHeight();
                if (location.x >= bounds.x && location.x < (bounds.x + bounds.width) &&
                    location.y >= bounds.y && location.y < (bounds.y + bounds.height)) {
                    shouldHide = false;
                } else {
                    shouldHide = true;
                }
            }
        }
        if (shouldHide) {
            enterTimer.stop();
        if (insideComponent != null) {
                insideComponent.removeMouseMotionListener(this);
            }
            insideComponent = null;
            toolTipText = null;
            mouseEvent = null;
            hideTipWindow();
            exitTimer.restart();
        }
    }
    public void mousePressed(MouseEvent event) {
        hideTipWindow();
        enterTimer.stop();
        showImmediately = false;
        insideComponent = null;
        mouseEvent = null;
    }
    public void mouseDragged(MouseEvent event) {
    }
    public void mouseMoved(MouseEvent event) {
        if (tipShowing) {
            checkForTipChange(event);
        }
        else if (showImmediately) {
            JComponent component = (JComponent)event.getSource();
            toolTipText = component.getToolTipText(event);
            if (toolTipText != null) {
                preferredLocation = component.getToolTipLocation(event);
                mouseEvent = event;
                insideComponent = component;
                exitTimer.stop();
                showTipWindow();
            }
        }
        else {
            insideComponent = (JComponent)event.getSource();
            mouseEvent = event;
            toolTipText = null;
            enterTimer.restart();
        }
    }
    private void checkForTipChange(MouseEvent event) {
        JComponent component = (JComponent)event.getSource();
        String newText = component.getToolTipText(event);
        Point  newPreferredLocation = component.getToolTipLocation(event);
        if (newText != null || newPreferredLocation != null) {
            mouseEvent = event;
            if (((newText != null && newText.equals(toolTipText)) || newText == null) &&
                ((newPreferredLocation != null && newPreferredLocation.equals(preferredLocation))
                 || newPreferredLocation == null)) {
                if (tipWindow != null) {
                    insideTimer.restart();
                } else {
                    enterTimer.restart();
                }
            } else {
                toolTipText = newText;
                preferredLocation = newPreferredLocation;
                if (showImmediately) {
                    hideTipWindow();
                    showTipWindow();
                    exitTimer.stop();
                } else {
                    enterTimer.restart();
                }
            }
        } else {
            toolTipText = null;
            preferredLocation = null;
            mouseEvent = null;
            insideComponent = null;
            hideTipWindow();
            enterTimer.stop();
            exitTimer.restart();
        }
    }
    protected class insideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if(insideComponent != null && insideComponent.isShowing()) {
                if (toolTipText == null && mouseEvent != null) {
                    toolTipText = insideComponent.getToolTipText(mouseEvent);
                    preferredLocation = insideComponent.getToolTipLocation(
                                              mouseEvent);
                }
                if(toolTipText != null) {
                    showImmediately = true;
                    showTipWindow();
                }
                else {
                    insideComponent = null;
                    toolTipText = null;
                    preferredLocation = null;
                    mouseEvent = null;
                    hideTipWindow();
                }
            }
        }
    }
    protected class outsideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showImmediately = false;
        }
    }
    protected class stillInsideTimerAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            hideTipWindow();
            enterTimer.stop();
            showImmediately = false;
            insideComponent = null;
            mouseEvent = null;
        }
    }
    private class MoveBeforeEnterListener extends MouseMotionAdapter {
        public void mouseMoved(MouseEvent e) {
            initiateToolTip(e);
        }
    }
    static Frame frameForComponent(Component component) {
        while (!(component instanceof Frame)) {
            component = component.getParent();
        }
        return (Frame)component;
    }
  private FocusListener createFocusChangeListener(){
    return new FocusAdapter(){
      public void focusLost(FocusEvent evt){
        hideTipWindow();
        insideComponent = null;
        JComponent c = (JComponent)evt.getSource();
        c.removeFocusListener(focusChangeListener);
      }
    };
  }
  private int getPopupFitWidth(Rectangle popupRectInScreen, Component invoker){
    if (invoker != null){
      Container parent;
      for (parent = invoker.getParent(); parent != null; parent = parent.getParent()){
        if(parent instanceof JFrame || parent instanceof JDialog ||
           parent instanceof JWindow) { 
          return getWidthAdjust(parent.getBounds(),popupRectInScreen);
        } else if (parent instanceof JApplet || parent instanceof JInternalFrame) {
          if (popupFrameRect == null){
            popupFrameRect = new Rectangle();
          }
          Point p = parent.getLocationOnScreen();
          popupFrameRect.setBounds(p.x,p.y,
                                   parent.getBounds().width,
                                   parent.getBounds().height);
          return getWidthAdjust(popupFrameRect,popupRectInScreen);
        }
      }
    }
    return 0;
  }
  private int getPopupFitHeight(Rectangle popupRectInScreen, Component invoker){
    if (invoker != null){
      Container parent;
      for (parent = invoker.getParent(); parent != null; parent = parent.getParent()){
        if(parent instanceof JFrame || parent instanceof JDialog ||
           parent instanceof JWindow) {
          return getHeightAdjust(parent.getBounds(),popupRectInScreen);
        } else if (parent instanceof JApplet || parent instanceof JInternalFrame) {
          if (popupFrameRect == null){
            popupFrameRect = new Rectangle();
          }
          Point p = parent.getLocationOnScreen();
          popupFrameRect.setBounds(p.x,p.y,
                                   parent.getBounds().width,
                                   parent.getBounds().height);
          return getHeightAdjust(popupFrameRect,popupRectInScreen);
        }
      }
    }
    return 0;
  }
  private int getHeightAdjust(Rectangle a, Rectangle b){
    if (b.y >= a.y && (b.y + b.height) <= (a.y + a.height))
      return 0;
    else
      return (((b.y + b.height) - (a.y + a.height)) + 5);
  }
  private int getWidthAdjust(Rectangle a, Rectangle b){
    if (b.x >= a.x && (b.x + b.width) <= (a.x + a.width)){
      return 0;
    }
    else {
      return (((b.x + b.width) - (a.x +a.width)) + 5);
    }
  }
    private void show(JComponent source) {
        if (tipWindow != null) { 
            hideTipWindow();
            insideComponent = null;
        }
        else {
            hideTipWindow(); 
            enterTimer.stop();
            exitTimer.stop();
            insideTimer.stop();
            insideComponent = source;
            if (insideComponent != null){
                toolTipText = insideComponent.getToolTipText();
                preferredLocation = new Point(10,insideComponent.getHeight()+
                                              10);  
                showTipWindow();
                if (focusChangeListener == null){
                    focusChangeListener = createFocusChangeListener();
                }
                insideComponent.addFocusListener(focusChangeListener);
            }
        }
    }
    private void hide(JComponent source) {
        hideTipWindow();
        source.removeFocusListener(focusChangeListener);
        preferredLocation = null;
        insideComponent = null;
    }
    private class AccessibilityKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (!e.isConsumed()) {
                JComponent source = (JComponent) e.getComponent();
                KeyStroke keyStrokeForEvent = KeyStroke.getKeyStrokeForEvent(e);
                if (hideTip.equals(keyStrokeForEvent)) {
                    if (tipWindow != null) {
                        hide(source);
                        e.consume();
                    }
                } else if (postTip.equals(keyStrokeForEvent)) {
                    ToolTipManager.this.show(source);
                    e.consume();
                }
            }
        }
    }
}
