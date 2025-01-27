public class MotifScrollBarUI extends BasicScrollBarUI
{
    public static ComponentUI createUI(JComponent c) {
        return new MotifScrollBarUI();
    }
    public Dimension getPreferredSize(JComponent c) {
        Insets insets = c.getInsets();
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;
        return (scrollbar.getOrientation() == JScrollBar.VERTICAL)
            ? new Dimension(dx + 11, dy + 33)
            : new Dimension(dx + 33, dy + 11);
    }
    protected JButton createDecreaseButton(int orientation) {
        return new MotifScrollBarButton(orientation);
    }
    protected JButton createIncreaseButton(int orientation) {
        return new MotifScrollBarButton(orientation);
    }
    public void paintTrack(Graphics g, JComponent c, Rectangle trackBounds)  {
        g.setColor(trackColor);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }
    public void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds)
    {
        if(thumbBounds.isEmpty() || !scrollbar.isEnabled())     {
            return;
        }
        int w = thumbBounds.width;
        int h = thumbBounds.height;
        g.translate(thumbBounds.x, thumbBounds.y);
        g.setColor(thumbColor);
        g.fillRect(0, 0, w-1, h-1);
        g.setColor(thumbHighlightColor);
        g.drawLine(0, 0, 0, h-1);
        g.drawLine(1, 0, w-1, 0);
        g.setColor(thumbLightShadowColor);
        g.drawLine(1, h-1, w-1, h-1);
        g.drawLine(w-1, 1, w-1, h-2);
        g.translate(-thumbBounds.x, -thumbBounds.y);
    }
}
