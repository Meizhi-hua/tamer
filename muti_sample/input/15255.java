public class BoundedZoomAction extends WidgetAction.Adapter {
    private double minFactor = 0.0;
    private double maxFactor = Double.MAX_VALUE;
    private double zoomMultiplier;
    private boolean useAnimator;
    public BoundedZoomAction(double zoomMultiplier, boolean useAnimator) {
        assert zoomMultiplier > 1.0;
        this.zoomMultiplier = zoomMultiplier;
        this.useAnimator = useAnimator;
    }
    public double getMinFactor() {
        return minFactor;
    }
    public void setMinFactor(double d) {
        minFactor = d;
    }
    public double getMaxFactor() {
        return maxFactor;
    }
    public void setMaxFactor(double d) {
        maxFactor = d;
    }
    private JScrollPane findScrollPane(JComponent component) {
        for (;;) {
            if (component == null) {
                return null;
            }
            if (component instanceof JScrollPane) {
                return ((JScrollPane) component);
            }
            Container parent = component.getParent();
            if (!(parent instanceof JComponent)) {
                return null;
            }
            component = (JComponent) parent;
        }
    }
    @Override
    public State mouseWheelMoved(Widget widget, WidgetMouseWheelEvent event) {
        final Scene scene = widget.getScene();
        int amount = event.getWheelRotation();
        JScrollPane scrollPane = findScrollPane(scene.getView());
        Point viewPosition = null;
        Point mouseLocation = scene.convertSceneToView(event.getPoint());
        int xOffset = 0;
        int yOffset = 0;
        Rectangle bounds = new Rectangle(scene.getBounds());
        Dimension componentSize = new Dimension(scene.getView().getPreferredSize());
        if (scrollPane != null) {
            viewPosition = new Point(scrollPane.getViewport().getViewPosition());
            xOffset = (mouseLocation.x - viewPosition.x);
            yOffset = (mouseLocation.y - viewPosition.y);
            viewPosition.x += xOffset;
            viewPosition.y += yOffset;
        }
        if (useAnimator) {
            SceneAnimator sceneAnimator = scene.getSceneAnimator();
            synchronized (sceneAnimator) {
                double zoom = sceneAnimator.isAnimatingZoomFactor() ? sceneAnimator.getTargetZoomFactor() : scene.getZoomFactor();
                while (amount > 0 && zoom / zoomMultiplier >= minFactor) {
                    zoom /= zoomMultiplier;
                    if (viewPosition != null) {
                        viewPosition.x /= zoomMultiplier;
                        viewPosition.y /= zoomMultiplier;
                        bounds.width /= zoomMultiplier;
                        bounds.height /= zoomMultiplier;
                        componentSize.width /= zoomMultiplier;
                        componentSize.height /= zoomMultiplier;
                    }
                    amount--;
                }
                while (amount < 0 && zoom * zoomMultiplier <= maxFactor) {
                    zoom *= zoomMultiplier;
                    if (viewPosition != null) {
                        viewPosition.x *= zoomMultiplier;
                        viewPosition.y *= zoomMultiplier;
                        bounds.width *= zoomMultiplier;
                        bounds.height *= zoomMultiplier;
                        componentSize.width *= zoomMultiplier;
                        componentSize.height *= zoomMultiplier;
                    }
                    amount++;
                }
                sceneAnimator.animateZoomFactor(zoom);
            }
        } else {
            double zoom = scene.getZoomFactor();
            while (amount > 0 && zoom / zoomMultiplier >= minFactor) {
                zoom /= zoomMultiplier;
                if (viewPosition != null) {
                    viewPosition.x /= zoomMultiplier;
                    viewPosition.y /= zoomMultiplier;
                    bounds.width /= zoomMultiplier;
                    bounds.height /= zoomMultiplier;
                    componentSize.width /= zoomMultiplier;
                    componentSize.height /= zoomMultiplier;
                }
                amount--;
            }
            while (amount < 0 && zoom * zoomMultiplier <= maxFactor) {
                zoom *= zoomMultiplier;
                if (viewPosition != null) {
                    viewPosition.x *= zoomMultiplier;
                    viewPosition.y *= zoomMultiplier;
                    bounds.width *= zoomMultiplier;
                    bounds.height *= zoomMultiplier;
                    componentSize.width *= zoomMultiplier;
                    componentSize.height *= zoomMultiplier;
                }
                amount++;
            }
            scene.setZoomFactor(zoom);
        }
        if (scrollPane != null) {
            viewPosition.x -= xOffset;
            viewPosition.y -= yOffset;
            scrollPane.getViewport().setViewPosition(viewPosition);
        }
        return WidgetAction.State.CONSUMED;
    }
}
