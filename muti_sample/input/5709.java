public abstract class SlotWidget extends Widget {
    private Slot slot;
    private FigureWidget figureWidget;
    private Image bufferImage;
    private static double TEXT_ZOOM_FACTOR = 0.9;
    private static double ZOOM_FACTOR = 0.6;
    private DiagramScene scene;
    public SlotWidget(Slot slot, DiagramScene scene, Widget parent, FigureWidget fw) {
        super(scene);
        this.scene = scene;
        this.slot = slot;
        figureWidget = fw;
        this.setToolTipText("<HTML>" + slot.getName() + "</HTML>");
        this.setCheckClipping(true);
    }
    public Point getAnchorPosition() {
        Point p = new Point(figureWidget.getFigure().getPosition());
        Point p2 = slot.getRelativePosition();
        p.translate(p2.x, p2.y);
        return p;
    }
    protected void init() {
        Point p = calculateRelativeLocation();
        Rectangle r = calculateClientArea();
        p = new Point(p.x, p.y - r.height / 2);
        this.setPreferredLocation(p);
    }
    public Slot getSlot() {
        return slot;
    }
    public FigureWidget getFigureWidget() {
        return figureWidget;
    }
    @Override
    protected void paintWidget() {
        if (scene.getRealZoomFactor() < ZOOM_FACTOR) {
            return;
        }
        if (bufferImage == null) {
            Graphics2D g = this.getGraphics();
            g.setColor(Color.DARK_GRAY);
            int w = this.getBounds().width;
            int h = this.getBounds().height;
            if (getSlot().getShortName() != null && getSlot().getShortName().length() > 0 && scene.getRealZoomFactor() >= TEXT_ZOOM_FACTOR) {
                Font f = new Font("Arial", Font.PLAIN, 8);
                g.setFont(f.deriveFont(7.5f));
                Rectangle2D r1 = g.getFontMetrics().getStringBounds(getSlot().getShortName(), g);
                g.drawString(getSlot().getShortName(), (int) (this.getBounds().width - r1.getWidth()) / 2, (int) (this.getBounds().height + r1.getHeight()) / 2);
            } else {
                if (slot instanceof OutputSlot) {
                    g.fillArc(w / 4, -h / 4 - 1, w / 2, h / 2, 180, 180);
                } else {
                    g.fillArc(w / 4, 3 * h / 4, w / 2, h / 2, 0, 180);
                }
            }
        }
    }
    @Override
    protected Rectangle calculateClientArea() {
        return new Rectangle(0, 0, Figure.SLOT_WIDTH, Figure.SLOT_WIDTH);
    }
    protected abstract Point calculateRelativeLocation();
    protected double calculateRelativeY(int size, int index) {
        assert index >= 0 && index < size;
        assert size > 0;
        double height = getFigureWidget().getBounds().getHeight();
        return height * (index + 1) / (size + 1);
    }
}
