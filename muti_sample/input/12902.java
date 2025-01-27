class DragRecognitionSupport {
    private int motionThreshold;
    private MouseEvent dndArmedEvent;
    private JComponent component;
    public static interface BeforeDrag {
        public void dragStarting(MouseEvent me);
    }
    private static DragRecognitionSupport getDragRecognitionSupport() {
        DragRecognitionSupport support =
            (DragRecognitionSupport)AppContext.getAppContext().
                get(DragRecognitionSupport.class);
        if (support == null) {
            support = new DragRecognitionSupport();
            AppContext.getAppContext().put(DragRecognitionSupport.class, support);
        }
        return support;
    }
    public static boolean mousePressed(MouseEvent me) {
        return getDragRecognitionSupport().mousePressedImpl(me);
    }
    public static MouseEvent mouseReleased(MouseEvent me) {
        return getDragRecognitionSupport().mouseReleasedImpl(me);
    }
    public static boolean mouseDragged(MouseEvent me, BeforeDrag bd) {
        return getDragRecognitionSupport().mouseDraggedImpl(me, bd);
    }
    private void clearState() {
        dndArmedEvent = null;
        component = null;
    }
    private int mapDragOperationFromModifiers(MouseEvent me,
                                              TransferHandler th) {
        if (th == null || !SwingUtilities.isLeftMouseButton(me)) {
            return TransferHandler.NONE;
        }
        return SunDragSourceContextPeer.
            convertModifiersToDropAction(me.getModifiersEx(),
                                         th.getSourceActions(component));
    }
    private boolean mousePressedImpl(MouseEvent me) {
        component = (JComponent)me.getSource();
        if (mapDragOperationFromModifiers(me, component.getTransferHandler())
                != TransferHandler.NONE) {
            motionThreshold = DragSource.getDragThreshold();
            dndArmedEvent = me;
            return true;
        }
        clearState();
        return false;
    }
    private MouseEvent mouseReleasedImpl(MouseEvent me) {
        if (dndArmedEvent == null) {
            return null;
        }
        MouseEvent retEvent = null;
        if (me.getSource() == component) {
            retEvent = dndArmedEvent;
        } 
        clearState();
        return retEvent;
    }
    private boolean mouseDraggedImpl(MouseEvent me, BeforeDrag bd) {
        if (dndArmedEvent == null) {
            return false;
        }
        if (me.getSource() != component) {
            clearState();
            return false;
        }
        int dx = Math.abs(me.getX() - dndArmedEvent.getX());
        int dy = Math.abs(me.getY() - dndArmedEvent.getY());
        if ((dx > motionThreshold) || (dy > motionThreshold)) {
            TransferHandler th = component.getTransferHandler();
            int action = mapDragOperationFromModifiers(me, th);
            if (action != TransferHandler.NONE) {
                if (bd != null) {
                    bd.dragStarting(dndArmedEvent);
                }
                th.exportAsDrag(component, dndArmedEvent, action);
                clearState();
            }
        }
        return true;
    }
}
